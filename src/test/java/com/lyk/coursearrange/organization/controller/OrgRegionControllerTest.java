package com.lyk.coursearrange.organization.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.api.PageResponse;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.organization.entity.OrgRegionDivision;
import com.lyk.coursearrange.organization.query.RegionDivisionPageQuery;
import com.lyk.coursearrange.organization.service.RegionDivisionSyncService;
import com.lyk.coursearrange.organization.vo.RegionSyncResultVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 行政区划控制器测试。
 * 覆盖：sync 方法的按钮权限注解、分页参数透传、子级查询缺参校验与结果封装。
 */
@ExtendWith(MockitoExtension.class)
class OrgRegionControllerTest {

    @Mock
    private RegionDivisionSyncService regionDivisionSyncService;

    @Test
    void sync_shouldDeclareSyncButtonPermission() throws Exception {
        // 更新动作必须要求按钮权限 btn:region:sync
        Method sync = OrgRegionController.class.getMethod("sync");
        SaCheckPermission permission = sync.getAnnotation(SaCheckPermission.class);

        assertNotNull(permission);
        assertArrayEquals(new String[]{"btn:region:sync"}, permission.value());
    }

    @Test
    void sync_shouldReturnResultFromService() {
        RegionSyncResultVO vo = new RegionSyncResultVO();
        vo.setInserted(3200);
        vo.setUpdated(0);
        vo.setDisabled(0);
        vo.setTotal(3200);
        vo.setSourceUrl("http://example.test/pca-code.json");
        vo.setSyncedAt("2026-11-23 22:00:00");
        vo.setDurationMs(3000);
        when(regionDivisionSyncService.syncFromSource()).thenReturn(vo);
        OrgRegionController controller = new OrgRegionController(regionDivisionSyncService);

        ServerResponse<?> response = controller.sync();

        assertTrue(response.isSuccess());
        assertSame(vo, response.getData());
    }

    @Test
    void page_shouldPassPaginationParamsToService() {
        // 分页入参应原样透传给 service：页码 2、每页 20
        Page<OrgRegionDivision> page = new Page<>(2, 20, 40);
        page.setRecords(List.of(new OrgRegionDivision()));
        when(regionDivisionSyncService.page(any(Page.class), any(Wrapper.class))).thenReturn(page);
        OrgRegionController controller = new OrgRegionController(regionDivisionSyncService);

        RegionDivisionPageQuery query = new RegionDivisionPageQuery();
        query.setPageNum(2);
        query.setPageSize(20);
        query.setKeyword("北京");
        query.setRegionLevel(1);
        query.setParentCode("0");
        ServerResponse<?> response = controller.page(query);

        assertTrue(response.isSuccess());
        // 返回统一分页结构：页码 / 总数 / 记录列表
        PageResponse<?> body = assertInstanceOf(PageResponse.class, response.getData());
        assertEquals(2L, body.getPageNum());
        assertEquals(20L, body.getPageSize());
        assertEquals(40L, body.getTotal());
        assertEquals(1, body.getRecords().size());

        // 捕获分页对象，校验页码与每页数量透传
        ArgumentCaptor<Page<OrgRegionDivision>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(regionDivisionSyncService).page(pageCaptor.capture(), any(Wrapper.class));
        assertEquals(2L, pageCaptor.getValue().getCurrent());
        assertEquals(20L, pageCaptor.getValue().getSize());
    }

    @Test
    void page_shouldReturnEmptyPageWhenNoData() {
        // 空数据时返回空分页结构而非 null
        Page<OrgRegionDivision> emptyPage = new Page<>(1, 20, 0);
        emptyPage.setRecords(List.of());
        when(regionDivisionSyncService.page(any(Page.class), any(Wrapper.class))).thenReturn(emptyPage);
        OrgRegionController controller = new OrgRegionController(regionDivisionSyncService);

        ServerResponse<?> response = controller.page(new RegionDivisionPageQuery());

        assertTrue(response.isSuccess());
        PageResponse<?> body = assertInstanceOf(PageResponse.class, response.getData());
        assertEquals(0L, body.getTotal());
        assertTrue(body.getRecords().isEmpty());
    }

    @Test
    void children_shouldRequireParentCode() {
        // parentCode 为空白时直接按参数错误处理
        OrgRegionController controller = new OrgRegionController(regionDivisionSyncService);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.children(" "));

        assertEquals(400, exception.getCode());
        assertEquals("parentCode 不能为空", exception.getMessage());
    }

    @Test
    void children_shouldReturnDirectChildren() {
        OrgRegionDivision child = new OrgRegionDivision();
        child.setRegionCode("110100");
        child.setRegionName("北京市辖区");
        when(regionDivisionSyncService.list(any(Wrapper.class))).thenReturn(List.of(child));
        OrgRegionController controller = new OrgRegionController(regionDivisionSyncService);

        ServerResponse<?> response = controller.children("110000");

        assertTrue(response.isSuccess());
        assertInstanceOf(List.class, response.getData());
        List<?> children = (List<?>) response.getData();
        assertEquals(1, children.size());
        assertEquals("110100", ((OrgRegionDivision) children.get(0)).getRegionCode());
    }
}
