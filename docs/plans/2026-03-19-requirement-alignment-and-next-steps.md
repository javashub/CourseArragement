# Requirement Alignment And Next Steps Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Align the refactor plan with the actual codebase, publish a concrete todo list, then continue front-end and back-end synchronized development against the remaining requirement gaps.

**Architecture:** Keep the current standard chain as the main path: Vue 3 + Vite front end calls unified `/api` modules, Spring Boot 2.7 back end exposes standard resource, schedule, auth, organization and RBAC endpoints. Remaining work should prefer incremental replacement of legacy endpoints and overgrown service logic instead of another large rewrite.

**Tech Stack:** Vue 3, Vite, Element Plus, Pinia, Vue Router 4, Spring Boot 2.7, Java 17, MyBatis-Plus, Sa-Token, JUnit 5, Mockito.

---

### Task 1: Sync planning documents with actual progress

**Files:**
- Modify: `项目重构优化方案.md`
- Create: `docs/plans/2026-03-19-requirement-alignment-and-next-steps.md`
- Create: `待办.md`

**Step 1: Read source documents and current code**

Read:
- `需求文件.md`
- `项目重构优化方案.md`
- `pom.xml`
- `UI/coursearrange/package.json`
- `UI/coursearrange/src/router/index.js`
- `UI/coursearrange/src/api/request.js`
- `src/main/java/com/lyk/coursearrange/service/impl/ClassTaskServiceImpl.java`
- `src/main/java/com/lyk/coursearrange/service/impl/CoursePlanServiceImpl.java`

**Step 2: Mark completed checklist items conservatively**

Rules:
- Only mark `[x]` when the current codebase already supports the requirement on the main path.
- Keep `[ ]` when capability is partial, transitional, legacy-only, or untested.

**Step 3: Write root todo list**

`待办.md` must contain:
- completed summary
- requirement gaps grouped by phase
- exact next development tasks
- verification commands after each task
- commit rule: one feature per commit, Chinese commit message only

**Step 4: Verify docs render and are internally consistent**

Run:
```bash
sed -n '1,240p' 项目重构优化方案.md
sed -n '1,240p' 待办.md
```

**Step 5: Commit**

```bash
git add 项目重构优化方案.md docs/plans/2026-03-19-requirement-alignment-and-next-steps.md 待办.md
git commit -m "更新重构方案与阶段待办"
git push origin master
```

### Task 2: Add missing schedule-task filters required by the spec

**Files:**
- Modify: `src/main/java/com/lyk/coursearrange/schedule/controller/ScheduleTaskController.java`
- Modify: `src/main/java/com/lyk/coursearrange/controller/ClassTaskController.java`
- Modify: `src/main/java/com/lyk/coursearrange/schedule/vo/ScheduleTaskPageVO.java` if needed
- Modify: `UI/coursearrange/src/api/modules/course.js`
- Modify: `UI/coursearrange/src/manager/components/ClassTaskList.vue`
- Test: `src/test/java/com/lyk/coursearrange/schedule/controller/ScheduleTaskControllerTest.java`
- Test: `src/test/java/com/lyk/coursearrange/controller/ClassTaskControllerTest.java`

**Step 1: Write failing tests**

Add tests for:
- filter by `classNo`
- filter by `teacherNo`
- filter by `courseNo`
- old and new task page endpoints behave consistently

**Step 2: Run the targeted tests and confirm failure**

Run:
```bash
mvn -q -Dtest=ClassTaskControllerTest,ScheduleTaskControllerTest test
```

Expected:
- at least one assertion fails because current page endpoints only filter by semester

**Step 3: Implement minimal back-end query support**

Add optional query params:
- `classNo`
- `teacherNo`
- `courseNo`

Implementation notes:
- still use standard `sch_task`
- filter on parsed remark metadata after fetching current page input set, or refactor query shape if needed
- keep legacy and standard controllers consistent

**Step 4: Implement front-end filters**

In `ClassTaskList.vue`:
- add filter inputs
- reset page number on search
- send filters through `fetchClassTaskPage`
- keep current semester selection behavior

**Step 5: Verify**

Run:
```bash
mvn -q -Dtest=ClassTaskControllerTest,ScheduleTaskControllerTest test
mvn -q -DskipTests compile
npm run build
```

**Step 6: Commit**

```bash
git add src/main/java/com/lyk/coursearrange/schedule/controller/ScheduleTaskController.java src/main/java/com/lyk/coursearrange/controller/ClassTaskController.java UI/coursearrange/src/api/modules/course.js UI/coursearrange/src/manager/components/ClassTaskList.vue src/test/java/com/lyk/coursearrange/schedule/controller/ScheduleTaskControllerTest.java src/test/java/com/lyk/coursearrange/controller/ClassTaskControllerTest.java
git commit -m "补齐课程任务多条件筛选"
git push origin master
```

### Task 3: Continue remaining requirement gaps in small vertical slices

**Files:**
- Follow `待办.md`

**Step 1: Always choose one vertical slice**

Good examples:
- task edit support
- schedule result success/conflict summary
- admin dashboard missing statistics
- student/teacher side missing standard query page

**Step 2: For each slice**

Do:
1. failing test
2. minimal back-end change
3. matching front-end change
4. verification
5. Chinese commit message

**Step 3: Do not mix unrelated cleanups**

Avoid:
- unrelated workspace noise
- package-lock changes
- broad refactors without a matching requirement gap
