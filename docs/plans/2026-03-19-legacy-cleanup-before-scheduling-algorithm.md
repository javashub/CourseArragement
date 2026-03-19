# Legacy Cleanup Before Scheduling Algorithm Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 在不改动排课算法本体的前提下，先删除运行时已经失去价值的 legacy 课表/任务兼容链路，为后续算法重构清空历史包袱。

**Architecture:** 先从 `CoursePlan` 旧链路开始，删除已经不再被标准运行链使用的兼容组件、旧控制器和旧实体引用；然后再收缩 `ClassTask` 的 legacy 服务继承与旧类型暴露面。整个过程保持标准 `sch_task`、`sch_schedule_result`、资源接口和前端标准页面可用，旧行为通过测试先红后绿验证。

**Tech Stack:** Spring Boot, MyBatis-Plus, Vue 2, Element UI, Maven, Vite

---

### Task 1: 清点并删除 `CoursePlan` 旧链路的最后运行时依赖

**Files:**
- Modify: `/Users/lyk/dev/project_source/毕业设计/CourseArrange/src/main/java/com/lyk/coursearrange/service/impl/CoursePlanServiceImpl.java`
- Modify: `/Users/lyk/dev/project_source/毕业设计/CourseArrange/src/test/java/com/lyk/coursearrange/service/impl/CoursePlanServiceImplTest.java`
- Modify: `/Users/lyk/dev/project_source/毕业设计/CourseArrange/UI/coursearrange/src/views/course/ScheduleView.vue`
- Modify: `/Users/lyk/dev/project_source/毕业设计/CourseArrange/UI/coursearrange/src/home/components/EmptyClassroom.vue`

**Step 1: 写失败测试**

覆盖：
- 标准课表查询不再依赖任何 `CoursePlanLegacySupport`
- 空教室查询失败时不再回退 `tb_course_plan`

**Step 2: 跑定向测试并确认失败**

Run: `mvn -q -Dtest=CoursePlanServiceImplTest test`

**Step 3: 写最小实现**

删除 `CoursePlanServiceImpl` 中最后的 `CoursePlanLegacySupport` 运行时调用，保持日志、返回值和前端提示与标准链一致。

**Step 4: 跑测试和构建**

Run:
- `mvn -q -Dtest=CoursePlanServiceImplTest,ClassroomControllerTest test`
- `mvn -q -DskipTests compile`
- `npm run build`

**Step 5: Commit**

```bash
git add src/main/java/com/lyk/coursearrange/service/impl/CoursePlanServiceImpl.java src/test/java/com/lyk/coursearrange/service/impl/CoursePlanServiceImplTest.java UI/coursearrange/src/views/course/ScheduleView.vue UI/coursearrange/src/home/components/EmptyClassroom.vue
git commit -m "移除课表查询残留旧链路"
```

### Task 2: 删除 `CoursePlanLegacySupport`、`CoursePlanDao` 和旧 `CoursePlan` 控制器入口

**Files:**
- Delete: `/Users/lyk/dev/project_source/毕业设计/CourseArrange/src/main/java/com/lyk/coursearrange/service/impl/CoursePlanLegacySupport.java`
- Delete: `/Users/lyk/dev/project_source/毕业设计/CourseArrange/src/main/java/com/lyk/coursearrange/dao/CoursePlanDao.java`
- Modify/Delete: `/Users/lyk/dev/project_source/毕业设计/CourseArrange/src/main/java/com/lyk/coursearrange/controller/CoursePlanController.java`
- Modify: `/Users/lyk/dev/project_source/毕业设计/CourseArrange/src/test/java/com/lyk/coursearrange/service/impl/CoursePlanLegacySupportTest.java`
- Search: `/Users/lyk/dev/project_source/毕业设计/CourseArrange/src/main/java`

**Step 1: 写失败测试或失败编译检查**

先验证删除后不会留下真实调用方；如果还存在运行时依赖，测试或编译应先失败。

**Step 2: 跑编译/定向测试确认红灯**

Run:
- `mvn -q -Dtest=CoursePlanLegacySupportTest,CoursePlanServiceImplTest test`
- `mvn -q -DskipTests compile`

**Step 3: 写最小实现**

删除没有运行时价值的 legacy 课表兼容组件；若旧 `/courseplan/*` 仍需保留，则改为标准服务壳层，不再保留对旧实体的直接暴露。

**Step 4: 跑测试和构建**

Run:
- `mvn -q -Dtest=CoursePlanServiceImplTest,ClassroomControllerTest test`
- `mvn -q -DskipTests compile`
- `npm run build`

**Step 5: Commit**

```bash
git add src/main/java/com/lyk/coursearrange/controller/CoursePlanController.java src/main/java/com/lyk/coursearrange/service/impl/CoursePlanServiceImpl.java
git rm src/main/java/com/lyk/coursearrange/service/impl/CoursePlanLegacySupport.java src/main/java/com/lyk/coursearrange/dao/CoursePlanDao.java
git commit -m "删除旧课表兼容组件"
```

### Task 3: 收缩 `ClassTaskService` 的 legacy 服务继承和旧类型暴露

**Files:**
- Modify: `/Users/lyk/dev/project_source/毕业设计/CourseArrange/src/main/java/com/lyk/coursearrange/service/ClassTaskService.java`
- Modify: `/Users/lyk/dev/project_source/毕业设计/CourseArrange/src/main/java/com/lyk/coursearrange/service/impl/ClassTaskServiceImpl.java`
- Modify: `/Users/lyk/dev/project_source/毕业设计/CourseArrange/src/test/java/com/lyk/coursearrange/service/impl/ClassTaskServiceImplTest.java`
- Search: `/Users/lyk/dev/project_source/毕业设计/CourseArrange/src/main/java`

**Step 1: 写失败测试**

覆盖：
- `ClassTaskServiceImpl` 不再依赖 `ServiceImpl<ClassTaskDao, ClassTask>`
- 统计/排课入口继续走标准 `sch_task`

**Step 2: 跑定向测试确认失败**

Run: `mvn -q -Dtest=ClassTaskServiceImplTest,ClassTaskControllerTest,ScheduleTaskControllerTest test`

**Step 3: 写最小实现**

把 `ClassTaskService` 改成标准任务语义接口；把 `ClassTaskServiceImpl` 改成普通 `@Service`，删除不再必要的 MyBatis-Plus 泛型继承。

**Step 4: 跑测试和构建**

Run:
- `mvn -q -Dtest=ClassTaskServiceImplTest,ClassTaskControllerTest,ScheduleTaskControllerTest test`
- `mvn -q -DskipTests compile`
- `npm run build`

**Step 5: Commit**

```bash
git add src/main/java/com/lyk/coursearrange/service/ClassTaskService.java src/main/java/com/lyk/coursearrange/service/impl/ClassTaskServiceImpl.java src/test/java/com/lyk/coursearrange/service/impl/ClassTaskServiceImplTest.java
git commit -m "收缩排课任务服务的旧模型依赖"
```

### Task 4: 清理旧控制器的兼容壳层与前端历史入口

**Files:**
- Modify: `/Users/lyk/dev/project_source/毕业设计/CourseArrange/src/main/java/com/lyk/coursearrange/controller/ClassTaskController.java`
- Modify: `/Users/lyk/dev/project_source/毕业设计/CourseArrange/src/main/java/com/lyk/coursearrange/schedule/controller/ScheduleTaskController.java`
- Modify: `/Users/lyk/dev/project_source/毕业设计/CourseArrange/UI/coursearrange/src/manager/components/ClassTaskList.vue`
- Modify: `/Users/lyk/dev/project_source/毕业设计/CourseArrange/UI/coursearrange/src/home/components/EmptyClassroom.vue`

**Step 1: 写失败测试**

覆盖旧控制器仍然可用，但返回值完全来自标准链，不再混入 legacy 实体字段。

**Step 2: 跑定向测试确认失败**

Run: `mvn -q -Dtest=ClassTaskControllerTest,ScheduleTaskControllerTest,ClassroomControllerTest test`

**Step 3: 写最小实现**

继续把旧控制器压缩成标准链壳层；前端同步删除剩余 legacy 提示和无效兼容文案。

**Step 4: 跑测试和构建**

Run:
- `mvn -q -Dtest=ClassTaskControllerTest,ScheduleTaskControllerTest,ClassroomControllerTest test`
- `mvn -q -DskipTests compile`
- `npm run build`

**Step 5: Commit**

```bash
git add src/main/java/com/lyk/coursearrange/controller/ClassTaskController.java src/main/java/com/lyk/coursearrange/schedule/controller/ScheduleTaskController.java UI/coursearrange/src/manager/components/ClassTaskList.vue UI/coursearrange/src/home/components/EmptyClassroom.vue
git commit -m "压缩旧控制器兼容壳层"
```
