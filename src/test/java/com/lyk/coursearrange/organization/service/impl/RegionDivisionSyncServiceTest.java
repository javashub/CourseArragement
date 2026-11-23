package com.lyk.coursearrange.organization.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.organization.config.RegionDivisionProperties;
import com.lyk.coursearrange.organization.entity.OrgRegionDivision;
import com.lyk.coursearrange.organization.mapper.OrgRegionDivisionMapper;
import com.lyk.coursearrange.organization.vo.RegionSyncResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 行政区划同步服务测试。
 * 说明：RestTemplate 用 Mockito mock 隔离真实网络；BaseMapper 注入 mock 后
 * 覆盖首次插入、重复更新零新增、缺失代码软停用、空集合失败、并发拒绝五种场景。
 */
@ExtendWith(MockitoExtension.class)
class RegionDivisionSyncServiceTest {

    /**
     * 测试用数据源地址，断言失败消息时要用到。
     */
    private static final String SOURCE_URL = "http://example.test/pca-code.json";

    /**
     * 一省 + 一市 + 一区县的三级样例数据。
     */
    private static final String SOURCE_JSON = "[{\"code\":\"110000\",\"name\":\"北京市\","
            + "\"children\":[{\"code\":\"110100\",\"name\":\"北京市辖区\","
            + "\"children\":[{\"code\":\"110101\",\"name\":\"东城区\"}]}]}]";

    @Mock
    private OrgRegionDivisionMapper mapper;

    @Mock
    private RestTemplate restTemplate;

    private RegionDivisionProperties properties;

    private RegionDivisionSyncServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new RegionDivisionProperties();
        properties.setSourceUrl(SOURCE_URL);
        // 初始化 MyBatis-Plus 的实体元数据缓存：LambdaUpdateWrapper 解析字段需要 TableInfo
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), OrgRegionDivision.class);
        // 走测试专用构造器注入 mock 的 RestTemplate，避免真实 HTTP 请求
        service = new RegionDivisionSyncServiceImpl(properties, () -> restTemplate);
        // ServiceImpl 的 baseMapper 字段由 Spring 注入，单测中手动补上
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
    }

    /**
     * 构造一条库中既有记录。
     */
    private OrgRegionDivision existing(String code, String name, String parentCode, int level, int sortNo, int deleted) {
        OrgRegionDivision entity = new OrgRegionDivision();
        entity.setId((long) sortNo);
        entity.setRegionCode(code);
        entity.setRegionName(name);
        entity.setParentCode(parentCode);
        entity.setRegionLevel(level);
        entity.setSortNo(sortNo);
        entity.setStatus(1);
        entity.setDeleted(deleted);
        return entity;
    }

    @Test
    void sync_shouldInsertAllRecordsWhenTableEmpty() {
        // 首次同步：库中无任何记录，3 条全部插入
        when(mapper.selectAllIncludingDeleted()).thenReturn(List.of());
        when(restTemplate.getForObject(SOURCE_URL, String.class)).thenReturn(SOURCE_JSON);
        when(mapper.insert(any(OrgRegionDivision.class))).thenReturn(1);

        RegionSyncResultVO result = service.syncFromSource();

        assertEquals(3, result.getInserted());
        assertEquals(0, result.getUpdated());
        assertEquals(0, result.getDisabled());
        assertEquals(3, result.getTotal());
        assertEquals(SOURCE_URL, result.getSourceUrl());
        assertNotNull(result.getSyncedAt());
        assertTrue(result.getDurationMs() >= 0);

        // 逐条校验插入内容：省级 parentCode 为 '0'，sortNo 按出现顺序
        ArgumentCaptor<OrgRegionDivision> captor = ArgumentCaptor.forClass(OrgRegionDivision.class);
        verify(mapper, times(3)).insert(captor.capture());
        OrgRegionDivision province = captor.getAllValues().get(0);
        assertEquals("110000", province.getRegionCode());
        assertEquals("北京市", province.getRegionName());
        assertEquals("0", province.getParentCode());
        assertEquals(1, province.getRegionLevel());
        assertEquals(1, province.getSortNo());
        OrgRegionDivision district = captor.getAllValues().get(2);
        assertEquals("110101", district.getRegionCode());
        assertEquals("110100", district.getParentCode());
        assertEquals(3, district.getRegionLevel());
        // 首次同步没有代码缺失，不应执行任何停用
        verify(mapper, never()).update(any(), any(Wrapper.class));
    }

    @Test
    void sync_shouldDoNothingWhenSourceUnchanged() {
        // 重复同步同一数据源：3 条记录完全一致，零新增零更新零停用
        List<OrgRegionDivision> existing = List.of(
                existing("110000", "北京市", "0", 1, 1, 0),
                existing("110100", "北京市辖区", "110000", 2, 2, 0),
                existing("110101", "东城区", "110100", 3, 3, 0));
        when(mapper.selectAllIncludingDeleted()).thenReturn(existing);
        when(restTemplate.getForObject(SOURCE_URL, String.class)).thenReturn(SOURCE_JSON);

        RegionSyncResultVO result = service.syncFromSource();

        assertEquals(0, result.getInserted());
        assertEquals(0, result.getUpdated());
        assertEquals(0, result.getDisabled());
        assertEquals(3, result.getTotal());
        // 未插入、未更新、未停用：字典数据保持原状
        verify(mapper, never()).insert(any(OrgRegionDivision.class));
        verify(mapper, never()).updateByRegionCode(any(OrgRegionDivision.class));
        verify(mapper, never()).update(any(), any(Wrapper.class));
    }

    @Test
    void sync_shouldSoftDisableMissingCodes() {
        // 库中多一条数据源里没有的代码（如 999999），同步后应软停用该记录
        List<OrgRegionDivision> existing = new ArrayList<>(List.of(
                existing("110000", "北京市", "0", 1, 1, 0),
                existing("110100", "北京市辖区", "110000", 2, 2, 0),
                existing("110101", "东城区", "110100", 3, 3, 0),
                existing("999999", "已废弃区划", "0", 1, 4, 0)));
        when(mapper.selectAllIncludingDeleted()).thenReturn(existing);
        when(restTemplate.getForObject(SOURCE_URL, String.class)).thenReturn(SOURCE_JSON);
        when(mapper.update(any(), any(Wrapper.class))).thenReturn(1);

        RegionSyncResultVO result = service.syncFromSource();

        assertEquals(0, result.getInserted());
        assertEquals(0, result.getUpdated());
        assertEquals(1, result.getDisabled());
        // 停用走逻辑删除（deleted=1），不物理删除
        verify(mapper, times(1)).update(any(), any(Wrapper.class));
        ArgumentCaptor<Wrapper<OrgRegionDivision>> wrapperCaptor =
                ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).update(any(), wrapperCaptor.capture());
        String sqlSet = wrapperCaptor.getValue().getSqlSet();
        assertTrue(sqlSet.contains("deleted"), sqlSet);
    }

    @Test
    void sync_shouldFailWithoutTouchingDataWhenSourceEmpty() {
        // 数据源返回空数组：判定失败，不做任何写入，更不允许全量停用
        when(restTemplate.getForObject(SOURCE_URL, String.class)).thenReturn("[]");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.syncFromSource());

        assertEquals(ResultCode.BAD_REQUEST.getCode(), exception.getCode());
        // 失败消息需包含数据源地址，便于定位
        assertTrue(exception.getMessage().contains(SOURCE_URL), exception.getMessage());
        verifyNoInteractions(mapper);
    }

    @Test
    void sync_shouldFailWithoutTouchingDataWhenHttpError() {
        // HTTP 非 2xx：判定失败且不改数据
        when(restTemplate.getForObject(SOURCE_URL, String.class))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "boom"));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.syncFromSource());

        assertEquals(ResultCode.SYSTEM_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains(SOURCE_URL), exception.getMessage());
        verifyNoInteractions(mapper);
    }

    @Test
    void sync_shouldFailWithoutTouchingDataWhenSourceUnparsable() {
        // 数据源结构漂移（必填字段缺失）：解析失败，不改数据
        when(restTemplate.getForObject(SOURCE_URL, String.class))
                .thenReturn("[{\"code\":\"110000\",\"name\":\"北京市\"},{\"code\":\"120000\"}]");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.syncFromSource());

        assertEquals(ResultCode.BAD_REQUEST.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains(SOURCE_URL), exception.getMessage());
        verifyNoInteractions(mapper);
    }

    @Test
    void sync_shouldRejectConcurrentTrigger() {
        // 并发拒绝：先把互斥标记置为 true，再触发同步应被拒绝
        ReflectionTestUtils.setField(service, "syncing", new AtomicBoolean(true));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.syncFromSource());

        assertEquals(ResultCode.BUSINESS_ERROR.getCode(), exception.getCode());
        assertEquals("行政区划正在更新中，请稍后再试", exception.getMessage());
        // 并发拒绝不应发起任何网络请求与数据库写入
        verifyNoInteractions(restTemplate, mapper);
    }

    @Test
    void sync_shouldRecoverSoftDisabledRecordWhenCodeReappears() {
        // 数据源重新出现已软停用的代码：应恢复 deleted=0 并按需更新
        List<OrgRegionDivision> existing = List.of(
                existing("110000", "北京市", "0", 1, 1, 1));
        when(mapper.selectAllIncludingDeleted()).thenReturn(existing);
        when(restTemplate.getForObject(SOURCE_URL, String.class))
                .thenReturn("[{\"code\":\"110000\",\"name\":\"北京市\"}]");
        when(mapper.updateByRegionCode(any(OrgRegionDivision.class))).thenReturn(1);

        RegionSyncResultVO result = service.syncFromSource();

        assertEquals(0, result.getInserted());
        assertEquals(1, result.getUpdated());
        assertEquals(0, result.getDisabled());
        // 恢复操作走按代码更新的自定义 SQL
        ArgumentCaptor<OrgRegionDivision> captor = ArgumentCaptor.forClass(OrgRegionDivision.class);
        verify(mapper).updateByRegionCode(captor.capture());
        assertEquals("110000", captor.getValue().getRegionCode());
        assertEquals(0, captor.getValue().getDeleted());
    }

    @Test
    void buildRestTemplate_shouldApplyConfiguredTimeouts() {
        // 生产构造器应按配置构建 RestTemplate：连接 5s、读取 30s
        RegionDivisionSyncServiceImpl productionService = new RegionDivisionSyncServiceImpl(properties);
        @SuppressWarnings("unchecked")
        Supplier<RestTemplate> supplier = (Supplier<RestTemplate>)
                ReflectionTestUtils.getField(productionService, "restTemplateSupplier");
        assertNotNull(supplier);
        RestTemplate template = supplier.get();
        SimpleClientHttpRequestFactory factory =
                (SimpleClientHttpRequestFactory) template.getRequestFactory();
        // Spring 5 的该工厂未暴露超时 getter，用反射读取内部字段核对配置生效
        assertEquals(5000, ReflectionTestUtils.getField(factory, "connectTimeout"));
        assertEquals(30000, ReflectionTestUtils.getField(factory, "readTimeout"));
    }
}
