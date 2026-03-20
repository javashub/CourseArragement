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
}
