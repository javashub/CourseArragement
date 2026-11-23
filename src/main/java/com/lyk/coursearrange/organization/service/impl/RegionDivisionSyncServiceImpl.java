package com.lyk.coursearrange.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.organization.config.RegionDivisionProperties;
import com.lyk.coursearrange.organization.entity.OrgRegionDivision;
import com.lyk.coursearrange.organization.mapper.OrgRegionDivisionMapper;
import com.lyk.coursearrange.organization.service.RegionDivisionParser;
import com.lyk.coursearrange.organization.service.RegionDivisionSyncService;
import com.lyk.coursearrange.organization.vo.RegionSyncResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 行政区划同步服务实现。
 * 说明：
 * 1. 继承 ServiceImpl 复用 MyBatis-Plus 的查询能力（分页 / 子级列表接口共用本服务）。
 * 2. 同步流程：拉取数据源 -> 解析为扁平记录 -> 按 region_code 与库中全量记录比对 ->
 *    新增缺失代码、更新有变化记录、软停用数据源中已不存在的代码；任一前置环节失败
 *    （网络错误 / 非 2xx / 解析失败 / 空结果）都不触碰库数据。
 * 3. 用 AtomicBoolean 做进程内互斥，同一时刻只允许一个同步任务执行。
 */
@Service
public class RegionDivisionSyncServiceImpl extends ServiceImpl<OrgRegionDivisionMapper, OrgRegionDivision>
        implements RegionDivisionSyncService {

    /**
     * 单批处理条数，用于分批写入与分批停用，避免一次性处理规模过大。
     */
    private static final int BATCH_SIZE = 500;

    /**
     * 同步完成时间的展示格式，与项目 Jackson 全局时间格式保持一致。
     */
    private static final DateTimeFormatter SYNCED_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 数据源配置（地址与超时）。
     */
    private final RegionDivisionProperties properties;

    /**
     * RestTemplate 供应器：生产环境按配置构建，测试环境注入 mock 以隔离真实网络。
     */
    private final Supplier<RestTemplate> restTemplateSupplier;

    /**
     * 同步互斥标记：true 表示已有一次同步在执行。
     */
    private final java.util.concurrent.atomic.AtomicBoolean syncing = new java.util.concurrent.atomic.AtomicBoolean(false);

    /**
     * Spring 使用的构造器：按配置中的超时构建 RestTemplate。
     */
    @Autowired
    public RegionDivisionSyncServiceImpl(RegionDivisionProperties properties) {
        this(properties, () -> buildRestTemplate(properties));
    }

    /**
     * 测试用构造器：直接注入 RestTemplate 供应器，便于 mock HTTP 行为。
     */
    RegionDivisionSyncServiceImpl(RegionDivisionProperties properties, Supplier<RestTemplate> restTemplateSupplier) {
        this.properties = properties;
        this.restTemplateSupplier = restTemplateSupplier;
    }

    /**
     * 按配置超时构建 RestTemplate；spring-web 自带，无需新增依赖。
     *
     * @param properties 数据源配置
     * @return 配置好连接 / 读取超时的 RestTemplate
     */
    private static RestTemplate buildRestTemplate(RegionDivisionProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 连接超时与读取超时均来自配置项，便于按环境调整
        factory.setConnectTimeout((int) properties.getConnectTimeoutMs());
        factory.setReadTimeout((int) properties.getReadTimeoutMs());
        return new RestTemplate(factory);
    }

    @Override
    public RegionSyncResultVO syncFromSource() {
        // 互斥：已有同步在执行时直接拒绝，避免并发写入
        if (!syncing.compareAndSet(false, true)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "行政区划正在更新中，请稍后再试");
        }
        long start = System.currentTimeMillis();
        try {
            String sourceUrl = properties.getSourceUrl();
            String body = fetchSource(sourceUrl);
            // 解析失败抛出的业务异常统一带上数据源地址，便于运维定位
            List<RegionDivisionParser.ParsedRegion> parsed;
            try {
                parsed = RegionDivisionParser.parse(body);
            } catch (BusinessException e) {
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        "解析行政区划数据源失败（" + sourceUrl + "）：" + e.getMessage());
            }
            // 空数据判定为失败：不做任何写入，更不允许触发全量停用
            if (CollectionUtils.isEmpty(parsed)) {
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        "行政区划数据源解析结果为空（" + sourceUrl + "），已取消本次更新");
            }
            return doSync(parsed, sourceUrl, start);
        } finally {
            // 无论成功失败都释放互斥标记，避免失败后永久卡死
            syncing.set(false);
        }
    }

    /**
     * 拉取数据源文本。
     *
     * @param sourceUrl 数据源地址
     * @return 数据源原始文本
     * @throws BusinessException 网络错误、超时或 HTTP 非 2xx 时抛出（RestTemplate 对非 2xx 抛 RestClientException）
     */
    private String fetchSource(String sourceUrl) {
        String body;
        try {
            body = restTemplateSupplier.get().getForObject(sourceUrl, String.class);
        } catch (RestClientException e) {
            // 覆盖 HTTP 非 2xx、连接失败与读取超时三类失败，统一带数据源地址与原因
            throw new BusinessException(ResultCode.SYSTEM_ERROR,
                    "拉取行政区划数据源失败（" + sourceUrl + "）：" + e.getMessage());
        }
        if (body == null || body.isBlank()) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR,
                    "拉取行政区划数据源失败（" + sourceUrl + "）：返回内容为空");
        }
        return body;
    }

    /**
     * 执行增量比对与写入。
     *
     * @param parsed    解析后的全量扁平记录
     * @param sourceUrl 本次使用的数据源地址
     * @param start     同步开始时间戳（毫秒）
     * @return 同步结果摘要
     */
    private RegionSyncResultVO doSync(List<RegionDivisionParser.ParsedRegion> parsed, String sourceUrl, long start) {
        // 库中全量记录（含软停用的），按 code 建立索引
        Map<String, OrgRegionDivision> existingMap = baseMapper.selectAllIncludingDeleted().stream()
                .collect(Collectors.toMap(OrgRegionDivision::getRegionCode, Function.identity(), (a, b) -> a));
        Set<String> sourceCodes = new HashSet<>();

        List<OrgRegionDivision> toInsert = new ArrayList<>();
        List<OrgRegionDivision> toUpdate = new ArrayList<>();
        for (int i = 0; i < parsed.size(); i++) {
            RegionDivisionParser.ParsedRegion record = parsed.get(i);
            sourceCodes.add(record.getCode());
            OrgRegionDivision existing = existingMap.get(record.getCode());
            if (existing == null) {
                // 库中不存在：准备插入，sortNo 按数据源出现顺序（从 1 起）
                OrgRegionDivision entity = new OrgRegionDivision();
                entity.setRegionCode(record.getCode());
                entity.setRegionName(record.getName());
                entity.setParentCode(record.getParentCode());
                entity.setRegionLevel(record.getLevel());
                entity.setSortNo(i + 1);
                entity.setStatus(1);
                entity.setDeleted(0);
                toInsert.add(entity);
            } else if (needUpdate(existing, record)) {
                // 已存在且有变化：更新名称 / 父级 / 层级并恢复 deleted=0（含重新启用软停用记录）
                existing.setRegionName(record.getName());
                existing.setParentCode(record.getParentCode());
                existing.setRegionLevel(record.getLevel());
                // 同步内存态与 SQL 一致：记录恢复正常状态
                existing.setDeleted(0);
                // 已存在记录保持原排序号不变，仅新插入记录按出现顺序生成
                toUpdate.add(existing);
            }
        }

        // 数据源中已不存在的代码：软停用而非物理删除，保留历史引用
        List<String> missingCodes = existingMap.keySet().stream()
                .filter(code -> !sourceCodes.contains(code))
                .collect(Collectors.toList());

        long inserted = insertBatch(toInsert);
        long updated = updateBatch(toUpdate);
        long disabled = disableBatch(missingCodes);

        RegionSyncResultVO vo = new RegionSyncResultVO();
        vo.setInserted(inserted);
        vo.setUpdated(updated);
        vo.setDisabled(disabled);
        vo.setTotal(parsed.size());
        vo.setSourceUrl(sourceUrl);
        vo.setSyncedAt(LocalDateTime.now().format(SYNCED_AT_FORMATTER));
        vo.setDurationMs(System.currentTimeMillis() - start);
        return vo;
    }

    /**
     * 判断既有记录是否需要更新：名称 / 父级 / 层级任一变化，或处于软停用状态需要恢复。
     *
     * @param existing 库中既有记录
     * @param record   数据源解析记录
     * @return true 表示需要执行更新
     */
    private boolean needUpdate(OrgRegionDivision existing, RegionDivisionParser.ParsedRegion record) {
        return existing.getDeleted() != null && existing.getDeleted() == 1
                || !existing.getRegionName().equals(record.getName())
                || !existing.getParentCode().equals(record.getParentCode())
                || !existing.getRegionLevel().equals(record.getLevel());
    }

    /**
     * 分批插入新记录，每批 500 条。
     *
     * @param toInsert 待插入记录
     * @return 实际插入条数
     */
    private long insertBatch(List<OrgRegionDivision> toInsert) {
        long count = 0;
        for (int from = 0; from < toInsert.size(); from += BATCH_SIZE) {
            List<OrgRegionDivision> chunk = toInsert.subList(from, Math.min(from + BATCH_SIZE, toInsert.size()));
            for (OrgRegionDivision entity : chunk) {
                // 走 BaseMapper 标准插入，触发 MyBatis-Plus 审计字段自动填充
                baseMapper.insert(entity);
                count++;
            }
        }
        return count;
    }

    /**
     * 分批更新变化记录，每批 500 条。
     *
     * @param toUpdate 待更新记录（按 region_code 定位）
     * @return 实际更新条数
     */
    private long updateBatch(List<OrgRegionDivision> toUpdate) {
        long count = 0;
        for (int from = 0; from < toUpdate.size(); from += BATCH_SIZE) {
            List<OrgRegionDivision> chunk = toUpdate.subList(from, Math.min(from + BATCH_SIZE, toUpdate.size()));
            for (OrgRegionDivision entity : chunk) {
                // 自定义 SQL：不受逻辑删除过滤影响，可同时恢复软停用记录
                baseMapper.updateByRegionCode(entity);
                count++;
            }
        }
        return count;
    }

    /**
     * 分批软停用数据源中已不存在的代码，每批 500 个。
     *
     * @param missingCodes 数据源中不存在的既有代码
     * @return 实际停用条数（已停用的记录不会重复计入）
     */
    private long disableBatch(List<String> missingCodes) {
        long count = 0;
        for (int from = 0; from < missingCodes.size(); from += BATCH_SIZE) {
            List<String> chunk = missingCodes.subList(from, Math.min(from + BATCH_SIZE, missingCodes.size()));
            // 逻辑删除字段经 wrapper.set 写入，WHERE 自动带 deleted=0，避免重复停用
            LambdaUpdateWrapper<OrgRegionDivision> wrapper = new LambdaUpdateWrapper<>();
            wrapper.set(OrgRegionDivision::getDeleted, 1)
                    .in(OrgRegionDivision::getRegionCode, chunk);
            count += baseMapper.update(null, wrapper);
        }
        return count;
    }
}
