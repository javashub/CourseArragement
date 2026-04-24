package com.lyk.coursearrange.schedule.engine;

import com.lyk.coursearrange.schedule.engine.model.OptimizationContext;
import com.lyk.coursearrange.schedule.engine.model.SchedulingAssignment;
import com.lyk.coursearrange.schedule.engine.model.SchedulingChromosome;
import com.lyk.coursearrange.schedule.engine.model.SchedulingClassroom;
import com.lyk.coursearrange.schedule.engine.model.SchedulingEngineRequest;
import com.lyk.coursearrange.schedule.engine.model.SchedulingFailureCode;
import com.lyk.coursearrange.schedule.engine.model.SchedulingFitnessContext;
import com.lyk.coursearrange.schedule.engine.model.SchedulingGene;
import com.lyk.coursearrange.schedule.engine.model.SchedulingPopulation;
import com.lyk.coursearrange.schedule.engine.model.SchedulingTask;
import com.lyk.coursearrange.schedule.engine.model.UnscheduledTaskDetail;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 面向初中/高中排课场景的第二阶段优化器。
 *
 * <p>这里采用“可行解 + 轻量遗传优化”的做法：
 * 第一阶段先由 {@link FeasibleSolutionBuilder} 给出一份肯定可落库的可行方案，
 * 第二阶段在不破坏硬约束的前提下，尝试把教师分布、班级分布和教室匹配度做得更均衡。</p>
 *
 * <p>当前版本刻意使用对象化染色体和基因结构，不再沿用旧版字符串拼接编码，
 * 这样课程编码、教师编码、班级编码长度不一致时也能自然兼容，后续继续扩展 GA 参数也更容易。</p>
 */
@Component
public class GeneticScheduleOptimizer implements ScheduleOptimizer {

    /**
     * 先保障管理员端“能排完且可返回”，高密度任务集不再强行跑完整遗传优化。
     */
    private static final int MAX_TASKS_FOR_FULL_OPTIMIZATION = 40;

    /**
     * 初版优化不追求非常激进的演化代数，优先保证稳定、可解释、容易调试。
     */
    private static final int POPULATION_SIZE = 10;
    private static final int MAX_GENERATIONS = 8;
    private static final int ELITE_SIZE = 2;
    private static final int MUTATION_TRIES = 2;
    /**
     * 候选上限略微放宽，给行政班补排多一点可选空间，但仍保持轻量级。
     */
    private static final int MAX_CANDIDATES_PER_GENE = 32;
    private static final int MAX_REARRANGE_DEPTH = 4;
    private static final int MAX_CONFLICTING_GENES = 4;

    private final SchedulingConstraintEvaluator constraintEvaluator;
    private final SchedulingFailureReporter failureReporter;
    private final Random random;

    public GeneticScheduleOptimizer() {
        this(new SchedulingConstraintEvaluator(), new Random(20260330L));
    }

    GeneticScheduleOptimizer(SchedulingConstraintEvaluator constraintEvaluator, Random random) {
        this.constraintEvaluator = constraintEvaluator;
        this.failureReporter = new SchedulingFailureReporter();
        this.random = random;
    }

    @Override
    public OptimizationContext optimize(OptimizationContext context) {
        if (context == null) {
            return null;
        }
        List<SchedulingAssignment> seedAssignments = context.getAssignments() == null
                ? List.of()
                : context.getAssignments();
        SchedulingFitnessContext fitnessContext = buildFitnessContext(context.getRequest());
        SchedulingChromosome seedChromosome = evaluateChromosome(
                buildSeedChromosome(seedAssignments, fitnessContext),
                fitnessContext
        );
        List<UnscheduledTaskDetail> originalUnscheduled = context.getUnscheduledTasks() == null
                ? new ArrayList<>()
                : new ArrayList<>(context.getUnscheduledTasks());
        if (shouldBypassFullOptimization(context, seedChromosome, originalUnscheduled)) {
            return context.toBuilder()
                    .assignments(toAssignments(seedChromosome))
                    .seedChromosome(seedChromosome)
                    .optimizedChromosome(deepCopy(seedChromosome))
                    .population(SchedulingPopulation.builder()
                            .generation(0)
                            .chromosomes(List.of(deepCopy(seedChromosome)))
                            .build())
                    .unscheduledTasks(originalUnscheduled)
                    .build();
        }
        if (seedChromosome.getGenes().size() <= 1 && originalUnscheduled.isEmpty()) {
            return context.toBuilder()
                    .assignments(toAssignments(seedChromosome))
                    .seedChromosome(seedChromosome)
                    .optimizedChromosome(deepCopy(seedChromosome))
                    .population(SchedulingPopulation.builder()
                            .generation(0)
                            .chromosomes(List.of(deepCopy(seedChromosome)))
                            .build())
                    .build();
        }
        SchedulingPopulation population = initializePopulation(seedChromosome, fitnessContext);
        SchedulingChromosome bestChromosome = deepCopy(seedChromosome);

        for (int generation = 1; generation <= MAX_GENERATIONS; generation++) {
            List<SchedulingChromosome> ranked = population.getChromosomes().stream()
                    .map(chromosome -> evaluateChromosome(repairChromosome(chromosome, seedChromosome, fitnessContext), fitnessContext))
                    .sorted(Comparator.comparingDouble(SchedulingChromosome::getFitnessScore).reversed())
                    .toList();
            if (!ranked.isEmpty() && ranked.get(0).getFitnessScore() > bestChromosome.getFitnessScore()) {
                bestChromosome = deepCopy(ranked.get(0));
            }
            population = evolvePopulation(ranked, generation, seedChromosome, fitnessContext);
        }

        // 最终再补一次评估，确保返回的最佳染色体和 assignments 完全一致。
        bestChromosome = evaluateChromosome(repairChromosome(bestChromosome, seedChromosome, fitnessContext), fitnessContext);
        SchedulingRecoveryResult recoveryResult = recoverUnscheduledTasks(bestChromosome, originalUnscheduled, fitnessContext);
        return context.toBuilder()
                .assignments(toAssignments(recoveryResult.chromosome()))
                .seedChromosome(seedChromosome)
                .optimizedChromosome(recoveryResult.chromosome())
                .population(population)
                .unscheduledTasks(recoveryResult.unscheduledTasks())
                .build();
    }

    private boolean shouldBypassFullOptimization(OptimizationContext context,
                                                 SchedulingChromosome seedChromosome,
                                                 List<UnscheduledTaskDetail> originalUnscheduled) {
        if (context == null || context.getRequest() == null) {
            return false;
        }
        int taskCount = context.getRequest().getTasks() == null ? 0 : context.getRequest().getTasks().size();
        if (taskCount >= MAX_TASKS_FOR_FULL_OPTIMIZATION) {
            return true;
        }
        return seedChromosome.getGenes().size() <= 1 && originalUnscheduled.isEmpty();
    }

    /**
     * 第二阶段除了微调已排结果，还要继续尝试消化第一阶段遗留的未排成任务。
     *
     * <p>这里采用“增量插入 + 有限深度换位”的方式：
     * 先直接寻找空闲位置；
     * 如果没有空闲位置，就尝试把冲突的已排块临时挪走，再把当前任务塞进去；
     * 挪走的块再递归寻找新位置，但递归深度受控，避免算法失控。</p>
     */
    private SchedulingRecoveryResult recoverUnscheduledTasks(SchedulingChromosome chromosome,
                                                             List<UnscheduledTaskDetail> unscheduledTasks,
                                                             SchedulingFitnessContext fitnessContext) {
        if (unscheduledTasks == null || unscheduledTasks.isEmpty()) {
            return new SchedulingRecoveryResult(chromosome, List.of());
        }
        SchedulingChromosome working = deepCopy(chromosome);
        List<UnscheduledTaskDetail> remaining = new ArrayList<>();

        // 行政班补排优先处理“更难放”的任务，减少后续被简单任务挤占稀缺时段/教室的概率。
        for (UnscheduledTaskDetail detail : sortRecoveryTasks(unscheduledTasks, fitnessContext)) {
            SchedulingTask task = detail.getTaskId() == null ? null : fitnessContext.getTaskMap().get(detail.getTaskId());
            if (task == null) {
                remaining.add(detail);
                continue;
            }
            SchedulingChromosome inserted = insertTaskWithRearrangement(task, working, fitnessContext, 0, new LinkedHashSet<>());
            if (inserted == null) {
                remaining.add(detail);
                continue;
            }
            working = inserted;
        }
        SchedulingChromosome evaluated = evaluateChromosome(working, fitnessContext);
        return new SchedulingRecoveryResult(evaluated, recalculateFailureReasons(remaining, evaluated, fitnessContext));
    }

    /**
     * 未补排成功的任务要基于最终状态重算失败原因，避免沿用第一阶段的过时原因。
     */
    private List<UnscheduledTaskDetail> recalculateFailureReasons(List<UnscheduledTaskDetail> unscheduledTasks,
                                                                  SchedulingChromosome chromosome,
                                                                  SchedulingFitnessContext fitnessContext) {
        if (unscheduledTasks == null || unscheduledTasks.isEmpty()) {
            return List.of();
        }
        List<UnscheduledTaskDetail> refreshed = new ArrayList<>();
        for (UnscheduledTaskDetail detail : unscheduledTasks) {
            if (detail == null || detail.getTaskId() == null) {
                refreshed.add(detail);
                continue;
            }
            SchedulingTask task = fitnessContext.getTaskMap().get(detail.getTaskId());
            if (task == null) {
                refreshed.add(detail);
                continue;
            }
            Map<SchedulingFailureCode, Integer> failureCounter = recalculateFailureCounter(task, chromosome, fitnessContext);
            refreshed.add(failureReporter.buildFailureDetail(task, failureCounter));
        }
        return refreshed;
    }

    private Map<SchedulingFailureCode, Integer> recalculateFailureCounter(SchedulingTask task,
                                                                          SchedulingChromosome chromosome,
                                                                          SchedulingFitnessContext fitnessContext) {
        SchedulingConstraintEvaluator.SchedulingState state = buildStateFromChromosome(chromosome, fitnessContext);
        Map<SchedulingFailureCode, Integer> failureCounter = new LinkedHashMap<>();
        List<SchedulingGene> templates = buildTaskGeneTemplates(task);
        for (SchedulingGene template : templates) {
            List<SchedulingGene> feasible = enumerateFeasiblePlacements(task, template, state, fitnessContext);
            if (!feasible.isEmpty()) {
                reserveGene(feasible.get(0), state, fitnessContext);
                continue;
            }
            collectPlacementFailures(task, template, state, fitnessContext, failureCounter);
            if (failureCounter.isEmpty()) {
                incrementFailureCounter(failureCounter, SchedulingFailureCode.NO_AVAILABLE_TIME_SLOT);
            }
            return failureCounter;
        }
        if (failureCounter.isEmpty()) {
            incrementFailureCounter(failureCounter, SchedulingFailureCode.NO_AVAILABLE_TIME_SLOT);
        }
        return failureCounter;
    }

    private void collectPlacementFailures(SchedulingTask task,
                                          SchedulingGene template,
                                          SchedulingConstraintEvaluator.SchedulingState state,
                                          SchedulingFitnessContext fitnessContext,
                                          Map<SchedulingFailureCode, Integer> failureCounter) {
        List<List<String>> blocks = candidateSlotBlocks(task, template, fitnessContext.getTimeSlotCodes());
        if (blocks.isEmpty()) {
            if (task.isFixedTime()) {
                incrementFailureCounter(failureCounter, SchedulingFailureCode.FIXED_TIME_UNSATISFIED);
            } else if (task.isNeedContinuous()) {
                incrementFailureCounter(failureCounter, SchedulingFailureCode.CONTINUOUS_SLOT_UNAVAILABLE);
            } else {
                incrementFailureCounter(failureCounter, SchedulingFailureCode.NO_AVAILABLE_TIME_SLOT);
            }
            return;
        }
        for (List<String> block : blocks) {
            List<SchedulingClassroom> classrooms = constraintEvaluator.selectCandidateClassrooms(
                    task,
                    safeClassrooms(fitnessContext),
                    state,
                    block
            );
            if (classrooms.isEmpty()) {
                incrementFailureCounter(failureCounter, task.isNeedSpecialRoom()
                        ? SchedulingFailureCode.SPECIAL_ROOM_UNAVAILABLE
                        : SchedulingFailureCode.NO_AVAILABLE_TIME_SLOT);
                continue;
            }
            for (SchedulingClassroom classroom : classrooms) {
                SchedulingConstraintEvaluator.EvaluationResult evaluation = constraintEvaluator.evaluate(task, block, classroom, state);
                if (!evaluation.success()) {
                    evaluation.failures().forEach(code -> incrementFailureCounter(failureCounter, code));
                }
            }
        }
    }

    private void incrementFailureCounter(Map<SchedulingFailureCode, Integer> failureCounter,
                                         SchedulingFailureCode failureCode) {
        if (failureCode == null) {
            return;
        }
        failureCounter.merge(failureCode, 1, Integer::sum);
    }

    private List<UnscheduledTaskDetail> sortRecoveryTasks(List<UnscheduledTaskDetail> unscheduledTasks,
                                                          SchedulingFitnessContext fitnessContext) {
        return unscheduledTasks.stream()
                .sorted(Comparator
                        .comparingInt((UnscheduledTaskDetail detail) -> recoveryDifficulty(detail, fitnessContext))
                        .reversed()
                        .thenComparing(UnscheduledTaskDetail::getTaskCode, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    private int recoveryDifficulty(UnscheduledTaskDetail detail,
                                   SchedulingFitnessContext fitnessContext) {
        if (detail == null || detail.getTaskId() == null) {
            return 0;
        }
        SchedulingTask task = fitnessContext.getTaskMap().get(detail.getTaskId());
        if (task == null) {
            return 0;
        }
        int weekHours = task.getWeekHours() == null ? 0 : task.getWeekHours();
        int priority = task.getPriorityLevel() == null ? 0 : task.getPriorityLevel();
        int forbiddenCount = (task.getTeacherForbiddenTimeSlots() == null ? 0 : task.getTeacherForbiddenTimeSlots().size())
                + (task.getClassForbiddenTimeSlots() == null ? 0 : task.getClassForbiddenTimeSlots().size());
        int score = weekHours + priority + forbiddenCount;
        if (task.isFixedTime()) {
            score += 100;
        }
        if (task.isNeedContinuous()) {
            score += 40;
        }
        if (task.isNeedSpecialRoom() || (task.getRequiredRoomType() != null && !task.getRequiredRoomType().isBlank())) {
            score += 20;
        }
        return score;
    }

    private SchedulingChromosome insertTaskWithRearrangement(SchedulingTask task,
                                                             SchedulingChromosome chromosome,
                                                             SchedulingFitnessContext fitnessContext,
                                                             int depth,
                                                             Set<String> lockedGeneCodes) {
        List<SchedulingGene> templates = buildTaskGeneTemplates(task);
        if (templates.isEmpty()) {
            return chromosome;
        }
        return placeTaskGeneRecursive(task, templates, 0, chromosome, fitnessContext, depth, lockedGeneCodes);
    }

    private SchedulingChromosome placeTaskGeneRecursive(SchedulingTask task,
                                                        List<SchedulingGene> templates,
                                                        int templateIndex,
                                                        SchedulingChromosome chromosome,
                                                        SchedulingFitnessContext fitnessContext,
                                                        int depth,
                                                        Set<String> lockedGeneCodes) {
        if (templateIndex >= templates.size()) {
            return chromosome;
        }
        SchedulingGene template = templates.get(templateIndex);
        SchedulingConstraintEvaluator.SchedulingState state = buildStateFromChromosome(chromosome, fitnessContext);

        // 第一步先尝试不挪动任何已排结果，直接找到可用位置。
        for (SchedulingGene candidate : enumerateFeasiblePlacements(task, template, state, fitnessContext)) {
            SchedulingChromosome trial = deepCopy(chromosome);
            trial.getGenes().add(copyGene(candidate));
            SchedulingChromosome placed = placeTaskGeneRecursive(
                    task,
                    templates,
                    templateIndex + 1,
                    trial,
                    fitnessContext,
                    depth,
                    extendLockedGenes(lockedGeneCodes, candidate.getGeneCode())
            );
            if (placed != null) {
                return placed;
            }
        }

        if (depth >= MAX_REARRANGE_DEPTH) {
            return null;
        }

        // 第二步允许小范围换位，把冲突块移走后再尝试插入当前任务块。
        for (SchedulingGene candidate : enumerateStaticPlacements(task, template, fitnessContext)) {
            List<SchedulingGene> blockers = collectConflictingGenes(task, candidate, chromosome, lockedGeneCodes, fitnessContext);
            if (blockers.isEmpty() || blockers.size() > MAX_CONFLICTING_GENES) {
                continue;
            }
            SchedulingChromosome trial = deepCopy(chromosome);
            removeGenes(trial, blockers);
            SchedulingConstraintEvaluator.SchedulingState trialState = buildStateFromChromosome(trial, fitnessContext);
            if (!isPlacementFeasible(task, candidate, trialState, fitnessContext)) {
                continue;
            }
            trial.getGenes().add(copyGene(candidate));

            SchedulingChromosome rearranged = trial;
            boolean success = true;
            Set<String> nextLocked = extendLockedGenes(lockedGeneCodes, candidate.getGeneCode());
            for (SchedulingGene blocker : blockers) {
                rearranged = reinsertGeneWithRearrangement(blocker, rearranged, fitnessContext, depth + 1, extendLockedGenes(nextLocked, blocker.getGeneCode()));
                if (rearranged == null) {
                    success = false;
                    break;
                }
            }
            if (!success) {
                continue;
            }
            SchedulingChromosome placed = placeTaskGeneRecursive(
                    task,
                    templates,
                    templateIndex + 1,
                    rearranged,
                    fitnessContext,
                    depth,
                    nextLocked
            );
            if (placed != null) {
                return placed;
            }
        }
        return null;
    }

    private SchedulingChromosome reinsertGeneWithRearrangement(SchedulingGene gene,
                                                               SchedulingChromosome chromosome,
                                                               SchedulingFitnessContext fitnessContext,
                                                               int depth,
                                                               Set<String> lockedGeneCodes) {
        SchedulingTask task = fitnessContext.getTaskMap().get(gene.getTaskId());
        if (task == null) {
            return null;
        }
        SchedulingChromosome working = deepCopy(chromosome);
        removeGeneByCode(working, gene.getGeneCode());
        return placeTaskGeneRecursive(
                task,
                List.of(copyGene(gene)),
                0,
                working,
                fitnessContext,
                depth,
                extendLockedGenes(lockedGeneCodes, gene.getGeneCode())
        );
    }

    private SchedulingPopulation initializePopulation(SchedulingChromosome seedChromosome,
                                                      SchedulingFitnessContext fitnessContext) {
        List<SchedulingChromosome> chromosomes = new ArrayList<>();
        chromosomes.add(deepCopy(seedChromosome));
        while (chromosomes.size() < POPULATION_SIZE) {
            SchedulingChromosome mutated = mutateChromosome(deepCopy(seedChromosome), seedChromosome, fitnessContext);
            chromosomes.add(evaluateChromosome(mutated, fitnessContext));
        }
        return SchedulingPopulation.builder()
                .generation(0)
                .chromosomes(chromosomes)
                .build();
    }

    private SchedulingPopulation evolvePopulation(List<SchedulingChromosome> ranked,
                                                  int generation,
                                                  SchedulingChromosome seedChromosome,
                                                  SchedulingFitnessContext fitnessContext) {
        if (ranked == null || ranked.isEmpty()) {
            return SchedulingPopulation.builder()
                    .generation(generation)
                    .chromosomes(List.of(deepCopy(seedChromosome)))
                    .build();
        }
        List<SchedulingChromosome> nextGeneration = new ArrayList<>();
        ranked.stream()
                .limit(Math.min(ELITE_SIZE, ranked.size()))
                .map(this::deepCopy)
                .forEach(nextGeneration::add);

        while (nextGeneration.size() < POPULATION_SIZE) {
            SchedulingChromosome parentA = selectParent(ranked);
            SchedulingChromosome parentB = selectParent(ranked);
            SchedulingChromosome child = crossover(parentA, parentB, seedChromosome);
            child = repairChromosome(child, seedChromosome, fitnessContext);
            child = mutateChromosome(child, seedChromosome, fitnessContext);
            child = evaluateChromosome(repairChromosome(child, seedChromosome, fitnessContext), fitnessContext);
            nextGeneration.add(child);
        }

        return SchedulingPopulation.builder()
                .generation(generation)
                .chromosomes(nextGeneration)
                .build();
    }

    private SchedulingChromosome selectParent(List<SchedulingChromosome> ranked) {
        int bound = Math.max(1, Math.min(ranked.size(), POPULATION_SIZE / 2 + 1));
        SchedulingChromosome candidateA = ranked.get(random.nextInt(bound));
        SchedulingChromosome candidateB = ranked.get(random.nextInt(bound));
        return candidateA.getFitnessScore() >= candidateB.getFitnessScore() ? candidateA : candidateB;
    }

    private SchedulingChromosome crossover(SchedulingChromosome parentA,
                                           SchedulingChromosome parentB,
                                           SchedulingChromosome seedChromosome) {
        Map<String, SchedulingGene> parentAMap = indexGeneMap(parentA);
        Map<String, SchedulingGene> parentBMap = indexGeneMap(parentB);
        List<SchedulingGene> childGenes = new ArrayList<>();

        // 交叉阶段只决定“每个任务块优先继承谁的排法”，真正的可行性修复交给 repair。
        for (SchedulingGene seedGene : seedChromosome.getGenes()) {
            SchedulingGene fromA = parentAMap.get(seedGene.getGeneCode());
            SchedulingGene fromB = parentBMap.get(seedGene.getGeneCode());
            SchedulingGene selected;
            if (fromA == null && fromB == null) {
                selected = seedGene;
            } else if (fromA == null) {
                selected = fromB;
            } else if (fromB == null) {
                selected = fromA;
            } else {
                selected = random.nextBoolean() ? fromA : fromB;
            }
            childGenes.add(copyGene(selected));
        }
        return SchedulingChromosome.builder()
                .genes(childGenes)
                .fitnessScore(0D)
                .build();
    }

    private SchedulingChromosome mutateChromosome(SchedulingChromosome chromosome,
                                                  SchedulingChromosome seedChromosome,
                                                  SchedulingFitnessContext fitnessContext) {
        if (chromosome.getGenes() == null || chromosome.getGenes().isEmpty()) {
            return chromosome;
        }
        SchedulingChromosome working = deepCopy(chromosome);
        int mutationCount = Math.min(MUTATION_TRIES, working.getGenes().size());
        for (int i = 0; i < mutationCount; i++) {
            int index = random.nextInt(working.getGenes().size());
            SchedulingGene improvedGene = searchBetterPlacement(working, index, seedChromosome, fitnessContext);
            if (improvedGene != null) {
                working.getGenes().set(index, improvedGene);
            }
        }
        return working;
    }

    private SchedulingGene searchBetterPlacement(SchedulingChromosome chromosome,
                                                 int geneIndex,
                                                 SchedulingChromosome seedChromosome,
                                                 SchedulingFitnessContext fitnessContext) {
        SchedulingGene currentGene = chromosome.getGenes().get(geneIndex);
        SchedulingTask task = fitnessContext.getTaskMap().get(currentGene.getTaskId());
        if (task == null || task.isFixedTime()) {
            return null;
        }

        SchedulingConstraintEvaluator.SchedulingState state = buildStateExcept(chromosome, geneIndex, fitnessContext);
        List<SchedulingGene> candidateGenes = enumerateFeasiblePlacements(task, currentGene, state, fitnessContext);
        if (candidateGenes.isEmpty()) {
            return null;
        }

        SchedulingChromosome baseline = evaluateChromosome(repairChromosome(chromosome, seedChromosome, fitnessContext), fitnessContext);
        SchedulingGene bestGene = null;
        double bestScore = baseline.getFitnessScore();

        // 变异不盲目随机替换，而是在一批可行候选里挑对整体适应度更好的位置。
        for (SchedulingGene candidate : candidateGenes) {
            SchedulingChromosome candidateChromosome = deepCopy(chromosome);
            candidateChromosome.getGenes().set(geneIndex, copyGene(candidate));
            SchedulingChromosome repaired = evaluateChromosome(
                    repairChromosome(candidateChromosome, seedChromosome, fitnessContext),
                    fitnessContext
            );
            if (repaired.getFitnessScore() > bestScore + 0.0001D) {
                bestGene = repaired.getGenes().stream()
                        .filter(item -> Objects.equals(item.getGeneCode(), currentGene.getGeneCode()))
                        .findFirst()
                        .map(this::copyGene)
                        .orElse(copyGene(candidate));
                bestScore = repaired.getFitnessScore();
            }
        }
        return bestGene;
    }

    private SchedulingChromosome repairChromosome(SchedulingChromosome chromosome,
                                                  SchedulingChromosome seedChromosome,
                                                  SchedulingFitnessContext fitnessContext) {
        if (chromosome == null || chromosome.getGenes() == null || chromosome.getGenes().isEmpty()) {
            return deepCopy(seedChromosome);
        }
        SchedulingConstraintEvaluator.SchedulingState state = new SchedulingConstraintEvaluator.SchedulingState();
        List<SchedulingGene> repairedGenes = new ArrayList<>();
        Map<String, SchedulingGene> currentGeneMap = indexGeneMap(chromosome);
        Map<String, SchedulingGene> seedGeneMap = indexGeneMap(seedChromosome);

        // 修复阶段按种子染色体的顺序重建状态，避免交叉后的局部冲突扩散到整条染色体。
        for (SchedulingGene seedGene : seedChromosome.getGenes()) {
            SchedulingGene preferredGene = currentGeneMap.getOrDefault(seedGene.getGeneCode(), seedGene);
            SchedulingGene repaired = tryRepairGene(preferredGene, seedGene, state, fitnessContext);
            if (repaired == null) {
                SchedulingGene fallback = seedGeneMap.get(seedGene.getGeneCode());
                repaired = tryRepairGene(fallback, seedGene, state, fitnessContext);
            }
            if (repaired == null) {
                return deepCopy(seedChromosome);
            }
            repairedGenes.add(repaired);
            reserveGene(repaired, state, fitnessContext);
        }
        return SchedulingChromosome.builder()
                .genes(repairedGenes)
                .fitnessScore(0D)
                .build();
    }

    private SchedulingGene tryRepairGene(SchedulingGene preferredGene,
                                         SchedulingGene seedGene,
                                         SchedulingConstraintEvaluator.SchedulingState state,
                                         SchedulingFitnessContext fitnessContext) {
        SchedulingTask task = fitnessContext.getTaskMap().get(seedGene.getTaskId());
        if (task == null) {
            return null;
        }

        if (preferredGene != null && isPlacementFeasible(task, preferredGene, state, fitnessContext)) {
            return copyGene(preferredGene);
        }

        List<SchedulingGene> alternatives = enumerateFeasiblePlacements(task, seedGene, state, fitnessContext);
        if (alternatives.isEmpty()) {
            return null;
        }
        return copyGene(alternatives.get(0));
    }

    private List<SchedulingGene> enumerateFeasiblePlacements(SchedulingTask task,
                                                             SchedulingGene baseGene,
                                                             SchedulingConstraintEvaluator.SchedulingState state,
                                                             SchedulingFitnessContext fitnessContext) {
        List<SchedulingGene> candidates = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        List<List<String>> candidateBlocks = candidateSlotBlocks(task, baseGene, fitnessContext.getTimeSlotCodes());

        for (List<String> slotBlock : candidateBlocks) {
            List<SchedulingClassroom> classrooms = constraintEvaluator.selectCandidateClassrooms(
                    task,
                    fitnessContext.getRequest().getClassrooms(),
                    state,
                    slotBlock
            );
            for (SchedulingClassroom classroom : classrooms) {
                SchedulingGene candidate = baseGene.toBuilder()
                        .classroomId(classroom.getClassroomId())
                        .classroomCode(classroom.getClassroomCode())
                        .timeSlotCodes(new ArrayList<>(slotBlock))
                        .build();
                if (!isPlacementFeasible(task, candidate, state, fitnessContext)) {
                    continue;
                }
                String uniqueKey = candidate.getClassroomCode() + "::" + String.join(",", candidate.getTimeSlotCodes());
                if (seen.add(uniqueKey)) {
                    candidates.add(candidate);
                }
                if (candidates.size() >= MAX_CANDIDATES_PER_GENE) {
                    return sortCandidateGenes(candidates, task, state, fitnessContext);
                }
            }
        }
        return sortCandidateGenes(candidates, task, state, fitnessContext);
    }

    private List<SchedulingGene> enumerateStaticPlacements(SchedulingTask task,
                                                           SchedulingGene baseGene,
                                                           SchedulingFitnessContext fitnessContext) {
        SchedulingConstraintEvaluator.SchedulingState emptyState = new SchedulingConstraintEvaluator.SchedulingState();
        List<SchedulingGene> candidates = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        List<List<String>> candidateBlocks = candidateSlotBlocks(task, baseGene, fitnessContext.getTimeSlotCodes());
        for (List<String> slotBlock : candidateBlocks) {
            List<SchedulingClassroom> classrooms = constraintEvaluator.selectCandidateClassrooms(
                    task,
                    fitnessContext.getRequest().getClassrooms(),
                    emptyState,
                    slotBlock
            );
            for (SchedulingClassroom classroom : classrooms) {
                SchedulingGene candidate = baseGene.toBuilder()
                        .classroomId(classroom.getClassroomId())
                        .classroomCode(classroom.getClassroomCode())
                        .timeSlotCodes(new ArrayList<>(slotBlock))
                        .build();
                String uniqueKey = candidate.getClassroomCode() + "::" + String.join(",", candidate.getTimeSlotCodes());
                if (seen.add(uniqueKey)) {
                    candidates.add(candidate);
                }
                if (candidates.size() >= MAX_CANDIDATES_PER_GENE) {
                    return candidates;
                }
            }
        }
        return candidates;
    }

    private List<SchedulingGene> collectConflictingGenes(SchedulingTask task,
                                                         SchedulingGene candidate,
                                                         SchedulingChromosome chromosome,
                                                         Set<String> lockedGeneCodes,
                                                         SchedulingFitnessContext fitnessContext) {
        if (chromosome == null || chromosome.getGenes() == null) {
            return List.of();
        }
        return chromosome.getGenes().stream()
                .filter(Objects::nonNull)
                .filter(gene -> lockedGeneCodes == null || !lockedGeneCodes.contains(gene.getGeneCode()))
                .filter(gene -> overlaps(gene, candidate))
                .filter(gene -> Objects.equals(gene.getTeacherNo(), task.getTeacherNo())
                        || Objects.equals(gene.getClassNo(), task.getClassNo())
                        || sameClassroom(gene, candidate))
                .collect(Collectors.toMap(SchedulingGene::getGeneCode, this::copyGene, (left, right) -> left, LinkedHashMap::new))
                .values()
                .stream()
                // 先挪“更难重新安置”的块，减少回溯深度。
                .sorted(Comparator
                        .comparingInt((SchedulingGene gene) -> blockerRearrangeDifficulty(gene, fitnessContext))
                        .reversed()
                        .thenComparing(SchedulingGene::getGeneCode, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    private int blockerRearrangeDifficulty(SchedulingGene gene,
                                           SchedulingFitnessContext fitnessContext) {
        if (gene == null || gene.getTaskId() == null) {
            return 0;
        }
        SchedulingTask blockerTask = fitnessContext.getTaskMap().get(gene.getTaskId());
        if (blockerTask == null) {
            return 0;
        }
        int score = 0;
        if (blockerTask.isFixedTime()) {
            score += 100;
        }
        if (blockerTask.isNeedContinuous()) {
            score += 30;
        }
        if (blockerTask.isNeedSpecialRoom() || (blockerTask.getRequiredRoomType() != null && !blockerTask.getRequiredRoomType().isBlank())) {
            score += 15;
        }
        return score + sameDayExtremePenalty(gene);
    }

    private boolean overlaps(SchedulingGene left, SchedulingGene right) {
        if (left.getTimeSlotCodes() == null || right.getTimeSlotCodes() == null) {
            return false;
        }
        Set<String> slotSet = new LinkedHashSet<>(left.getTimeSlotCodes());
        return right.getTimeSlotCodes().stream().anyMatch(slotSet::contains);
    }

    private boolean sameClassroom(SchedulingGene left, SchedulingGene right) {
        return left.getClassroomCode() != null
                && right.getClassroomCode() != null
                && left.getClassroomCode().equals(right.getClassroomCode());
    }

    private void removeGenes(SchedulingChromosome chromosome, List<SchedulingGene> genes) {
        if (chromosome == null || chromosome.getGenes() == null || genes == null || genes.isEmpty()) {
            return;
        }
        Set<String> geneCodes = genes.stream()
                .map(SchedulingGene::getGeneCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        chromosome.getGenes().removeIf(gene -> geneCodes.contains(gene.getGeneCode()));
    }

    private void removeGeneByCode(SchedulingChromosome chromosome, String geneCode) {
        if (chromosome == null || chromosome.getGenes() == null || geneCode == null) {
            return;
        }
        chromosome.getGenes().removeIf(gene -> geneCode.equals(gene.getGeneCode()));
    }

    private Set<String> extendLockedGenes(Set<String> lockedGeneCodes, String geneCode) {
        Set<String> next = new LinkedHashSet<>();
        if (lockedGeneCodes != null) {
            next.addAll(lockedGeneCodes);
        }
        if (geneCode != null && !geneCode.isBlank()) {
            next.add(geneCode);
        }
        return next;
    }

    private List<SchedulingGene> sortCandidateGenes(List<SchedulingGene> candidates,
                                                    SchedulingTask task,
                                                    SchedulingConstraintEvaluator.SchedulingState state,
                                                    SchedulingFitnessContext fitnessContext) {
        return candidates.stream()
                .sorted(Comparator
                        .comparingInt((SchedulingGene gene) -> distributionPenalty(task, gene, state))
                        .thenComparingInt(this::sameDayExtremePenalty)
                        .thenComparingInt(gene -> classroomScopePenalty(task, gene, fitnessContext))
                        .thenComparingInt(gene -> seatWastePenalty(gene, fitnessContext))
                        .thenComparingInt(this::periodPenalty)
                        .thenComparing(SchedulingGene::getClassroomCode, Comparator.nullsLast(String::compareTo))
                        .thenComparing(gene -> String.join(",", gene.getTimeSlotCodes())))
                .toList();
    }

    private int distributionPenalty(SchedulingTask task,
                                    SchedulingGene gene,
                                    SchedulingConstraintEvaluator.SchedulingState state) {
        Map<Integer, Long> grouped = gene.getTimeSlotCodes().stream()
                .collect(Collectors.groupingBy(constraintEvaluator::resolveWeekday, Collectors.counting()));
        int teacherPenalty = grouped.entrySet().stream()
                .mapToInt(entry -> state.teacherDayLoad(task.getTeacherNo(), entry.getKey()) + entry.getValue().intValue())
                .map(value -> value * value)
                .sum();
        int classPenalty = grouped.entrySet().stream()
                .mapToInt(entry -> existingClassDayLoad(task.getClassNo(), entry.getKey(), state) + entry.getValue().intValue())
                .map(value -> value * value)
                .sum();
        return teacherPenalty + classPenalty;
    }

    private int existingClassDayLoad(String classNo,
                                     int weekday,
                                     SchedulingConstraintEvaluator.SchedulingState state) {
        return state.classDayLoad(classNo, weekday);
    }

    private int seatWastePenalty(SchedulingGene gene, SchedulingFitnessContext fitnessContext) {
        if (gene.getStudentCount() == null || gene.getStudentCount() <= 0) {
            return 0;
        }
        return Math.abs(resolveSeatCount(gene, fitnessContext) - gene.getStudentCount());
    }

    private int resolveSeatCount(SchedulingGene gene, SchedulingFitnessContext fitnessContext) {
        SchedulingClassroom classroom = resolveClassroom(gene, fitnessContext);
        return classroom == null || classroom.getSeatCount() == null ? 0 : classroom.getSeatCount();
    }

    private int periodPenalty(SchedulingGene gene) {
        return gene.getTimeSlotCodes().stream()
                .mapToInt(slot -> {
                    int period = constraintEvaluator.resolvePeriod(slot);
                    return switch (period) {
                        case 1, 5 -> 2;
                        case 2, 4 -> 1;
                        default -> 0;
                    };
                })
                .sum();
    }

    private int sameDayExtremePenalty(SchedulingGene gene) {
        return gene.getTimeSlotCodes().stream()
                .mapToInt(slot -> {
                    int period = constraintEvaluator.resolvePeriod(slot);
                    if (period == 1 || period == 5 || period >= 7) {
                        return 2;
                    }
                    if (period == 2 || period == 4 || period == 6) {
                        return 1;
                    }
                    return 0;
                })
                .sum();
    }

    private int classroomScopePenalty(SchedulingTask task,
                                      SchedulingGene gene,
                                      SchedulingFitnessContext fitnessContext) {
        SchedulingClassroom classroom = resolveClassroom(gene, fitnessContext);
        if (classroom == null) {
            return 4;
        }
        int score = 0;
        if (task.getCampusId() != null && !Objects.equals(task.getCampusId(), classroom.getCampusId())) {
            score += 2;
        }
        if (task.getCollegeId() != null && !Objects.equals(task.getCollegeId(), classroom.getCollegeId())) {
            score += 1;
        }
        return score;
    }

    private boolean isPlacementFeasible(SchedulingTask task,
                                        SchedulingGene gene,
                                        SchedulingConstraintEvaluator.SchedulingState state,
                                        SchedulingFitnessContext fitnessContext) {
        SchedulingClassroom classroom = resolveClassroom(gene, fitnessContext);
        SchedulingConstraintEvaluator.EvaluationResult evaluation = constraintEvaluator.evaluate(
                task,
                gene.getTimeSlotCodes(),
                classroom,
                state
        );
        return evaluation.success();
    }

    private SchedulingConstraintEvaluator.SchedulingState buildStateExcept(SchedulingChromosome chromosome,
                                                                           int excludedIndex,
                                                                           SchedulingFitnessContext fitnessContext) {
        SchedulingConstraintEvaluator.SchedulingState state = new SchedulingConstraintEvaluator.SchedulingState();
        for (int i = 0; i < chromosome.getGenes().size(); i++) {
            if (i == excludedIndex) {
                continue;
            }
            reserveGene(chromosome.getGenes().get(i), state, fitnessContext);
        }
        return state;
    }

    private SchedulingConstraintEvaluator.SchedulingState buildStateFromChromosome(SchedulingChromosome chromosome,
                                                                                   SchedulingFitnessContext fitnessContext) {
        SchedulingConstraintEvaluator.SchedulingState state = new SchedulingConstraintEvaluator.SchedulingState();
        if (chromosome == null || chromosome.getGenes() == null) {
            return state;
        }
        for (SchedulingGene gene : chromosome.getGenes()) {
            reserveGene(gene, state, fitnessContext);
        }
        return state;
    }

    private void reserveGene(SchedulingGene gene,
                             SchedulingConstraintEvaluator.SchedulingState state,
                             SchedulingFitnessContext fitnessContext) {
        SchedulingTask task = fitnessContext.getTaskMap().get(gene.getTaskId());
        SchedulingClassroom classroom = resolveClassroom(gene, fitnessContext);
        if (task != null) {
            state.reserve(task, classroom, gene.getTimeSlotCodes(), constraintEvaluator);
        }
    }

    private SchedulingChromosome buildSeedChromosome(List<SchedulingAssignment> assignments,
                                                     SchedulingFitnessContext fitnessContext) {
        Map<Long, List<SchedulingAssignment>> grouped = assignments.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(SchedulingAssignment::getTaskId, LinkedHashMap::new, Collectors.toList()));
        List<SchedulingGene> genes = new ArrayList<>();

        for (Map.Entry<Long, List<SchedulingAssignment>> entry : grouped.entrySet()) {
            SchedulingTask task = fitnessContext.getTaskMap().get(entry.getKey());
            if (task == null) {
                continue;
            }
            List<SchedulingAssignment> taskAssignments = entry.getValue().stream()
                    .sorted(Comparator.comparingInt(item -> parseSlotCode(item.getTimeSlotCode())))
                    .toList();
            genes.addAll(buildGenesForTask(task, taskAssignments));
        }
        return SchedulingChromosome.builder()
                .genes(genes)
                .fitnessScore(0D)
                .build();
    }

    private List<SchedulingGene> buildGenesForTask(SchedulingTask task, List<SchedulingAssignment> assignments) {
        List<SchedulingGene> genes = new ArrayList<>();
        if (assignments == null || assignments.isEmpty()) {
            return genes;
        }
        int blockSize = resolveBlockSize(task);
        List<SchedulingAssignment> buffer = new ArrayList<>();
        int index = 0;

        for (SchedulingAssignment assignment : assignments) {
            if (!buffer.isEmpty() && shouldSplitBuffer(task, buffer, assignment, blockSize)) {
                genes.add(toGene(task, buffer, index++));
                buffer = new ArrayList<>();
            }
            buffer.add(assignment);
            if (!task.isNeedContinuous() || buffer.size() >= blockSize) {
                genes.add(toGene(task, buffer, index++));
                buffer = new ArrayList<>();
            }
        }
        if (!buffer.isEmpty()) {
            genes.add(toGene(task, buffer, index));
        }
        return genes;
    }

    private List<SchedulingGene> buildTaskGeneTemplates(SchedulingTask task) {
        List<Integer> chunkSizes = splitChunkSizes(task);
        if (chunkSizes.isEmpty()) {
            return List.of();
        }
        List<String> fixedSlots = task.getFixedTimeSlots() == null ? List.of() : task.getFixedTimeSlots();
        List<SchedulingGene> templates = new ArrayList<>();
        int fixedIndex = 0;
        for (int i = 0; i < chunkSizes.size(); i++) {
            int chunkSize = chunkSizes.get(i);
            List<String> timeSlots;
            if (task.isFixedTime()) {
                int endIndex = Math.min(fixedIndex + chunkSize, fixedSlots.size());
                timeSlots = new ArrayList<>(fixedSlots.subList(fixedIndex, endIndex));
                fixedIndex = endIndex;
            } else {
                timeSlots = new ArrayList<>(java.util.Collections.nCopies(chunkSize, ""));
            }
            templates.add(SchedulingGene.builder()
                    .geneCode(task.getTaskId() + "#" + i)
                    .taskId(task.getTaskId())
                    .taskCode(task.getTaskCode())
                    .classNo(task.getClassNo())
                    .courseNo(task.getCourseNo())
                    .teacherNo(task.getTeacherNo())
                    .timeSlotCodes(timeSlots)
                    .fixedTime(task.isFixedTime())
                    .needContinuous(task.isNeedContinuous())
                    .continuousSize(task.getContinuousSize())
                    .studentCount(task.getStudentCount())
                    .priorityLevel(task.getPriorityLevel())
                    .teacherMaxDayHours(task.getTeacherMaxDayHours())
                    .requiredRoomType(task.getRequiredRoomType())
                    .campusId(task.getCampusId())
                    .collegeId(task.getCollegeId())
                    .build());
        }
        return templates;
    }

    private boolean shouldSplitBuffer(SchedulingTask task,
                                      List<SchedulingAssignment> buffer,
                                      SchedulingAssignment next,
                                      int blockSize) {
        if (!task.isNeedContinuous()) {
            return true;
        }
        SchedulingAssignment last = buffer.get(buffer.size() - 1);
        if (!Objects.equals(last.getClassroomCode(), next.getClassroomCode())) {
            return true;
        }
        if (buffer.size() >= blockSize) {
            return true;
        }
        int lastSlot = parseSlotCode(last.getTimeSlotCode());
        int nextSlot = parseSlotCode(next.getTimeSlotCode());
        return nextSlot != lastSlot + 1
                || constraintEvaluator.resolveWeekday(last.getTimeSlotCode()) != constraintEvaluator.resolveWeekday(next.getTimeSlotCode());
    }

    private SchedulingGene toGene(SchedulingTask task, List<SchedulingAssignment> assignments, int index) {
        SchedulingAssignment first = assignments.get(0);
        return SchedulingGene.builder()
                .geneCode(task.getTaskId() + "#" + index)
                .taskId(task.getTaskId())
                .taskCode(task.getTaskCode())
                .classNo(task.getClassNo())
                .courseNo(task.getCourseNo())
                .teacherNo(task.getTeacherNo())
                .classroomId(first.getClassroomId())
                .classroomCode(first.getClassroomCode())
                .timeSlotCodes(assignments.stream().map(SchedulingAssignment::getTimeSlotCode).toList())
                .fixedTime(task.isFixedTime())
                .needContinuous(task.isNeedContinuous())
                .continuousSize(task.getContinuousSize())
                .studentCount(task.getStudentCount())
                .priorityLevel(task.getPriorityLevel())
                .teacherMaxDayHours(task.getTeacherMaxDayHours())
                .requiredRoomType(task.getRequiredRoomType())
                .campusId(task.getCampusId())
                .collegeId(task.getCollegeId())
                .build();
    }

    private List<List<String>> candidateSlotBlocks(SchedulingTask task,
                                                   SchedulingGene baseGene,
                                                   List<String> allSlotCodes) {
        if (task.isFixedTime()) {
            return List.of(new ArrayList<>(baseGene.getTimeSlotCodes()));
        }
        if (allSlotCodes == null || allSlotCodes.isEmpty()) {
            return List.of();
        }
        int blockSize = Math.max(1, baseGene.getTimeSlotCodes() == null ? 1 : baseGene.getTimeSlotCodes().size());
        List<List<String>> blocks = new ArrayList<>();
        for (int i = 0; i <= allSlotCodes.size() - blockSize; i++) {
            List<String> block = new ArrayList<>(allSlotCodes.subList(i, i + blockSize));
            if (task.isNeedContinuous() && !isContinuousBlock(block)) {
                continue;
            }
            blocks.add(block);
        }
        return blocks;
    }

    private boolean isContinuousBlock(List<String> block) {
        if (block.size() <= 1) {
            return true;
        }
        for (int i = 1; i < block.size(); i++) {
            int previous = parseSlotCode(block.get(i - 1));
            int current = parseSlotCode(block.get(i));
            if (current != previous + 1
                    || constraintEvaluator.resolveWeekday(block.get(i - 1)) != constraintEvaluator.resolveWeekday(block.get(i))) {
                return false;
            }
        }
        return true;
    }

    private SchedulingChromosome evaluateChromosome(SchedulingChromosome chromosome,
                                                    SchedulingFitnessContext fitnessContext) {
        if (chromosome == null) {
            return emptyChromosome();
        }
        double fitnessScore = calculateFitness(chromosome, fitnessContext);
        return SchedulingChromosome.builder()
                .genes(chromosome.getGenes() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(chromosome.getGenes().stream().map(this::copyGene).toList()))
                .fitnessScore(fitnessScore)
                .build();
    }

    private double calculateFitness(SchedulingChromosome chromosome,
                                    SchedulingFitnessContext fitnessContext) {
        if (chromosome.getGenes() == null || chromosome.getGenes().isEmpty()) {
            return 0D;
        }
        Map<String, Map<Integer, Integer>> teacherLoads = new HashMap<>();
        Map<String, Map<Integer, Integer>> classLoads = new HashMap<>();
        int seatWastePenalty = 0;
        int periodPenalty = 0;

        for (SchedulingGene gene : chromosome.getGenes()) {
            SchedulingClassroom classroom = resolveClassroom(gene, fitnessContext);
            int seatCount = classroom == null || classroom.getSeatCount() == null ? 0 : classroom.getSeatCount();
            if (gene.getStudentCount() != null && gene.getStudentCount() > 0 && seatCount > 0) {
                seatWastePenalty += Math.abs(seatCount - gene.getStudentCount());
            }
            for (String slotCode : gene.getTimeSlotCodes()) {
                int weekday = constraintEvaluator.resolveWeekday(slotCode);
                teacherLoads.computeIfAbsent(gene.getTeacherNo(), key -> new HashMap<>())
                        .merge(weekday, 1, Integer::sum);
                classLoads.computeIfAbsent(gene.getClassNo(), key -> new HashMap<>())
                        .merge(weekday, 1, Integer::sum);
                int period = constraintEvaluator.resolvePeriod(slotCode);
                periodPenalty += switch (period) {
                    case 1, 5 -> 2;
                    case 2, 4 -> 1;
                    default -> 0;
                };
            }
        }

        int teacherDistributionPenalty = teacherLoads.values().stream()
                .mapToInt(this::sumSquaredLoad)
                .sum();
        int classDistributionPenalty = classLoads.values().stream()
                .mapToInt(this::sumSquaredLoad)
                .sum();

        // 分数越高越好，因此这里统一转成“基础分 - 惩罚项”的形式，便于比较优化效果。
        return 10_000D
                - teacherDistributionPenalty * 18D
                - classDistributionPenalty * 12D
                - seatWastePenalty * 0.35D
                - periodPenalty * 2D;
    }

    private int sumSquaredLoad(Map<Integer, Integer> dayLoadMap) {
        return dayLoadMap.values().stream()
                .mapToInt(value -> value * value)
                .sum();
    }

    private List<SchedulingAssignment> toAssignments(SchedulingChromosome chromosome) {
        if (chromosome.getGenes() == null || chromosome.getGenes().isEmpty()) {
            return List.of();
        }
        List<SchedulingAssignment> assignments = new ArrayList<>();
        for (SchedulingGene gene : chromosome.getGenes()) {
            for (String slotCode : gene.getTimeSlotCodes()) {
                assignments.add(SchedulingAssignment.builder()
                        .taskId(gene.getTaskId())
                        .taskCode(gene.getTaskCode())
                        .classNo(gene.getClassNo())
                        .courseNo(gene.getCourseNo())
                        .teacherNo(gene.getTeacherNo())
                        .classroomId(gene.getClassroomId())
                        .classroomCode(gene.getClassroomCode())
                        .weekdayNo(constraintEvaluator.resolveWeekday(slotCode))
                        .periodNo(constraintEvaluator.resolvePeriod(slotCode))
                        .timeSlotCode(slotCode)
                        .build());
            }
        }
        return assignments.stream()
                .sorted(Comparator
                        .comparing(SchedulingAssignment::getTaskId, Comparator.nullsLast(Long::compareTo))
                        .thenComparingInt(item -> parseSlotCode(item.getTimeSlotCode())))
                .toList();
    }

    private SchedulingFitnessContext buildFitnessContext(SchedulingEngineRequest request) {
        Map<Long, SchedulingTask> taskMap = request == null || request.getTasks() == null
                ? Map.of()
                : request.getTasks().stream()
                .filter(Objects::nonNull)
                .filter(task -> task.getTaskId() != null)
                .collect(Collectors.toMap(SchedulingTask::getTaskId, task -> task, (left, right) -> left, LinkedHashMap::new));
        Map<Long, SchedulingClassroom> classroomMap = request == null || request.getClassrooms() == null
                ? Map.of()
                : request.getClassrooms().stream()
                .filter(Objects::nonNull)
                .filter(room -> room.getClassroomId() != null)
                .collect(Collectors.toMap(SchedulingClassroom::getClassroomId, room -> room, (left, right) -> left, LinkedHashMap::new));
        return SchedulingFitnessContext.builder()
                .request(request)
                .taskMap(taskMap)
                .classroomMap(classroomMap)
                .timeSlotCodes(request == null || request.getTimeSlotCodes() == null ? List.of() : request.getTimeSlotCodes())
                .build();
    }

    private SchedulingClassroom resolveClassroom(SchedulingGene gene, SchedulingFitnessContext fitnessContext) {
        if (gene.getClassroomId() != null) {
            SchedulingClassroom classroom = fitnessContext.getClassroomMap().get(gene.getClassroomId());
            if (classroom != null) {
                return classroom;
            }
        }
        return safeClassrooms(fitnessContext).stream()
                .filter(room -> Objects.equals(room.getClassroomCode(), gene.getClassroomCode()))
                .findFirst()
                .orElse(null);
    }

    private List<SchedulingClassroom> safeClassrooms(SchedulingFitnessContext fitnessContext) {
        if (fitnessContext.getRequest() == null || fitnessContext.getRequest().getClassrooms() == null) {
            return List.of();
        }
        return fitnessContext.getRequest().getClassrooms();
    }

    private Map<String, SchedulingGene> indexGeneMap(SchedulingChromosome chromosome) {
        if (chromosome == null || chromosome.getGenes() == null) {
            return Map.of();
        }
        return chromosome.getGenes().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(SchedulingGene::getGeneCode, this::copyGene, (left, right) -> left, LinkedHashMap::new));
    }

    private SchedulingChromosome deepCopy(SchedulingChromosome chromosome) {
        if (chromosome == null) {
            return emptyChromosome();
        }
        return SchedulingChromosome.builder()
                .genes(chromosome.getGenes() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(chromosome.getGenes().stream().map(this::copyGene).toList()))
                .fitnessScore(chromosome.getFitnessScore())
                .build();
    }

    private SchedulingGene copyGene(SchedulingGene gene) {
        if (gene == null) {
            return null;
        }
        return gene.toBuilder()
                .timeSlotCodes(gene.getTimeSlotCodes() == null ? List.of() : new ArrayList<>(gene.getTimeSlotCodes()))
                .build();
    }

    private SchedulingChromosome emptyChromosome() {
        return SchedulingChromosome.builder()
                .genes(List.of())
                .fitnessScore(0D)
                .build();
    }

    private int resolveBlockSize(SchedulingTask task) {
        if (!task.isNeedContinuous()) {
            return 1;
        }
        return Math.max(2, task.getContinuousSize() == null ? 2 : task.getContinuousSize());
    }

    private List<Integer> splitChunkSizes(SchedulingTask task) {
        // weekHours 直接对应每周时间格数量，不再沿用“2 学时折算 1 格”的旧语义。
        int requiredUnits = Math.max(1, task.getWeekHours() == null ? 0 : task.getWeekHours());
        if (!task.isNeedContinuous()) {
            return java.util.stream.IntStream.range(0, requiredUnits)
                    .mapToObj(index -> 1)
                    .toList();
        }
        int chunkSize = Math.max(2, task.getContinuousSize() == null ? 2 : task.getContinuousSize());
        List<Integer> chunks = new ArrayList<>();
        int remaining = requiredUnits;
        while (remaining > 0) {
            int current = Math.min(chunkSize, remaining);
            chunks.add(current);
            remaining -= current;
        }
        return chunks;
    }

    private int parseSlotCode(String slotCode) {
        return Integer.parseInt(slotCode);
    }

    private record SchedulingRecoveryResult(SchedulingChromosome chromosome,
                                            List<UnscheduledTaskDetail> unscheduledTasks) {
    }
}
