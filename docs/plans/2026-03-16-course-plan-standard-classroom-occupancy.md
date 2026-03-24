# CoursePlan Standard Classroom Occupancy Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Remove `ClassroomController`'s direct dependence on legacy `CoursePlan` entities by exposing a standard-first occupied-classroom query in `CoursePlanService`.

**Architecture:** Keep `CoursePlanServiceImpl` as the compatibility boundary. Add a service method that first resolves occupied classroom codes from `sch_schedule_result` plus `res_classroom`, and only falls back to legacy `CoursePlan` copies when the standard path is empty or unavailable. Update `ClassroomController` to consume occupied classroom codes instead of legacy plan rows.

**Tech Stack:** Spring Boot 2.7, MyBatis-Plus, JUnit 5, Mockito

---

### Task 1: Lock standard occupied-classroom behavior

**Files:**
- Modify: `src/test/java/com/lyk/coursearrange/service/impl/CoursePlanServiceImplTest.java`
- Modify: `src/main/java/com/lyk/coursearrange/service/CoursePlanService.java`
- Modify: `src/main/java/com/lyk/coursearrange/service/impl/CoursePlanServiceImpl.java`

**Step 1: Write the failing test**

Add a unit test proving `listOccupiedClassroomNos("01")` returns classroom codes from standard `sch_schedule_result` + `res_classroom` without reading legacy `CoursePlanDao`.

**Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=CoursePlanServiceImplTest test`
Expected: FAIL because the new service method does not exist yet.

**Step 3: Write minimal implementation**

Add `listOccupiedClassroomNos(String teachbuildNo)` to `CoursePlanService`, inject `ResClassroomService` into `CoursePlanServiceImpl`, and implement standard-first occupied-classroom lookup with legacy fallback.

**Step 4: Run test to verify it passes**

Run: `mvn -q -Dtest=CoursePlanServiceImplTest test`
Expected: PASS

### Task 2: Move classroom empty-query controller off legacy entities

**Files:**
- Create: `src/test/java/com/lyk/coursearrange/controller/ClassroomControllerTest.java`
- Modify: `src/main/java/com/lyk/coursearrange/controller/ClassroomController.java`

**Step 1: Write the failing test**

Add a controller unit test proving `/classroom/empty/{teachbuildno}` logic excludes occupied classrooms based on the new service method and does not require `CoursePlan` entity access.

**Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=ClassroomControllerTest test`
Expected: FAIL until the controller uses the new occupied-classroom interface and correct set-difference logic.

**Step 3: Write minimal implementation**

Replace the legacy `CoursePlan` loop with occupied classroom codes from `CoursePlanService`, fetch matching used classrooms only when the occupied set is non-empty, and simplify difference calculation so it returns actual empty classrooms.

**Step 4: Run test to verify it passes**

Run: `mvn -q -Dtest=ClassroomControllerTest test`
Expected: PASS

### Task 3: Verify compile safety

**Files:**
- Modify: `src/main/java/com/lyk/coursearrange/service/impl/CoursePlanServiceImpl.java`
- Modify: `src/main/java/com/lyk/coursearrange/controller/ClassroomController.java`
- Test: `src/test/java/com/lyk/coursearrange/service/impl/CoursePlanServiceImplTest.java`
- Test: `src/test/java/com/lyk/coursearrange/controller/ClassroomControllerTest.java`

**Step 1: Run targeted verification**

Run: `mvn -q -Dtest=CoursePlanServiceImplTest,ClassroomControllerTest test`
Expected: PASS

**Step 2: Run compile verification**

Run: `mvn -q -DskipTests compile`
Expected: PASS
