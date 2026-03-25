package com.lyk.coursearrange;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class LegacyCleanupGuardTest {

    @Test
    void shouldRemoveUnusedFrontendPagesDirectory() {
        assertFalse(Files.exists(Path.of("UI/coursearrange/src/pages")));
    }

    @Test
    void shouldRemoveUnusedLegacyControllers() {
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/controller/CoursePlanController.java")));
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/controller/TeachBuildInfoController.java")));
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/controller/LocationInfoController.java")));
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/controller/ClassInfoController.java")));
    }

    @Test
    void shouldRemoveLegacyPageOnlyDtosAndDaos() {
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/entity/request/ClassAddVO.java")));
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/entity/request/TeachbuildAddRequest.java")));
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/entity/request/LocationSetVO.java")));
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/entity/response/LocationVO.java")));
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/entity/LocationInfo.java")));
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/dao/LocationInfoDao.java")));
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/service/LocationInfoService.java")));
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/service/impl/LocationInfoServiceImpl.java")));
        assertFalse(Files.exists(Path.of("src/test/java/com/lyk/coursearrange/controller/ClassInfoControllerTest.java")));
    }

    @Test
    void shouldRemoveUnusedJwtHelperClasses() {
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/common/AuthenticationInterceptor.java")));
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/common/PassToken.java")));
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/common/UserLoginToken.java")));
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/config/InterceptorConfig.java")));
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/service/impl/TokenService.java")));
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/util/TokenUtil.java")));
    }

    @Test
    void shouldRemoveUnusedOnlineCourseLegacyControllers() {
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/controller/OnlineCategoryController.java")));
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/controller/OnlineCourseController.java")));
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/entity/request/OnlineCourseAddVO.java")));
    }

    @Test
    void shouldRemoveLegacyClassTaskControllerShell() {
        assertFalse(Files.exists(Path.of("src/main/java/com/lyk/coursearrange/controller/ClassTaskController.java")));
        assertFalse(Files.exists(Path.of("src/test/java/com/lyk/coursearrange/controller/ClassTaskControllerTest.java")));
    }

    @Test
    void shouldRemoveLegacyMapperXmlResources() {
        assertFalse(Files.exists(Path.of("src/main/resources/mapper/AdminMapper.xml")));
        assertFalse(Files.exists(Path.of("src/main/resources/mapper/TeacherMapper.xml")));
        assertFalse(Files.exists(Path.of("src/main/resources/mapper/StudentMapper.xml")));
        assertFalse(Files.exists(Path.of("src/main/resources/mapper/ClassInfoMapper.xml")));
        assertFalse(Files.exists(Path.of("src/main/resources/mapper/ClassTaskMapper.xml")));
        assertFalse(Files.exists(Path.of("src/main/resources/mapper/ClassroomMapper.xml")));
        assertFalse(Files.exists(Path.of("src/main/resources/mapper/CourseInfoMapper.xml")));
        assertFalse(Files.exists(Path.of("src/main/resources/mapper/CoursePlanMapper.xml")));
        assertFalse(Files.exists(Path.of("src/main/resources/mapper/GradeInfoMapper.xml")));
        assertFalse(Files.exists(Path.of("src/main/resources/mapper/TeachBuildInfoDao.xml")));
    }
}
