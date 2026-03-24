# CoursePlan Legacy Decoupling Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Remove `CoursePlanService` from the `IService<CoursePlan>` / `ServiceImpl<CoursePlanDao, CoursePlan>` inheritance chain while keeping standard `sch_schedule_result` behavior intact.

**Architecture:** Keep the standard schedule result flow as the primary path in `CoursePlanServiceImpl`, and treat legacy `CoursePlan` access as a compatibility detail hidden behind direct DAO helpers. This reduces old model coupling without changing controller contracts.

**Tech Stack:** Spring Boot 2.7, MyBatis-Plus, JUnit 5, Mockito

---

### Task 1: Lock standard class schedule query behavior

**Files:**
- Create: `src/test/java/com/lyk/coursearrange/service/impl/CoursePlanServiceImplTest.java`
- Modify: `src/main/java/com/lyk/coursearrange/service/impl/CoursePlanServiceImpl.java`

**Step 1: Write the failing test**

Write a unit test proving `queryCoursePlanByClassNo` returns standard `sch_schedule_result` data without touching legacy service inheritance behavior.

**Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=CoursePlanServiceImplTest test`
Expected: FAIL because the test references behavior not yet isolated for plain-service instantiation.

**Step 3: Write minimal implementation**

Refactor `CoursePlanServiceImpl` to a plain Spring service, inject `CoursePlanDao` directly, and keep the standard query path unchanged.

**Step 4: Run test to verify it passes**

Run: `mvn -q -Dtest=CoursePlanServiceImplTest test`
Expected: PASS

### Task 2: Remove service inheritance coupling

**Files:**
- Modify: `src/main/java/com/lyk/coursearrange/service/CoursePlanService.java`
- Modify: `src/main/java/com/lyk/coursearrange/service/impl/CoursePlanServiceImpl.java`

**Step 1: Write the failing test**

Extend the same test class with a case covering standard adjust conflict checking or successful standard adjustment through mocked collaborators.

**Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=CoursePlanServiceImplTest test`
Expected: FAIL until the implementation no longer depends on inherited CRUD methods.

**Step 3: Write minimal implementation**

Remove `IService<CoursePlan>` from `CoursePlanService`, remove `ServiceImpl` inheritance from `CoursePlanServiceImpl`, and replace inherited CRUD calls with explicit DAO usage in legacy helper methods.

**Step 4: Run test to verify it passes**

Run: `mvn -q -Dtest=CoursePlanServiceImplTest test`
Expected: PASS

### Task 3: Verify compile safety

**Files:**
- Modify: `src/main/java/com/lyk/coursearrange/service/impl/CoursePlanServiceImpl.java`
- Test: `src/test/java/com/lyk/coursearrange/service/impl/CoursePlanServiceImplTest.java`

**Step 1: Run targeted verification**

Run: `mvn -q -Dtest=CoursePlanServiceImplTest test`
Expected: PASS

**Step 2: Run compile verification**

Run: `mvn -q -DskipTests compile`
Expected: PASS
