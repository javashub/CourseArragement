package com.lyk.coursearrange.schedule.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 染色体对象。
 *
 * <p>一个染色体表示一套完整的排课方案，内部由多个对象化基因组成。</p>
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingChromosome {

    private List<SchedulingGene> genes;

    /**
     * 适应度分数，值越大代表方案质量越好。
     */
    private double fitnessScore;
}
