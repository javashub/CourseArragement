package com.lyk.coursearrange.organization.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 行政区划数据源解析器。
 * 说明：
 * 1. 无状态纯静态工具，输入为数据源 JSON 字符串，输出为扁平的区划记录列表，
 *    供同步服务逐条 upsert，解析失败时抛出携带定位信息的业务异常。
 * 2. 目标结构为嵌套数组 [{code, name, children:[...]}]，层级深度 1/2/3 对应
 *    省 / 市 / 区县；省级 parentCode 固定为 '0'，其余取父节点 code。
 * 3. 第四级（街道 / 乡镇）不在本次能力范围内，出现时直接忽略，不报错。
 */
public final class RegionDivisionParser {

    /**
     * Jackson 解析器，RegionDivisionParser 无状态可复用。
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 数据源允许的最大层级深度（省 / 市 / 区县）。
     */
    private static final int MAX_LEVEL = 3;

    private RegionDivisionParser() {
    }

    /**
     * 把数据源 JSON 字符串递归展开为扁平的行政区划记录列表。
     *
     * @param json 数据源原始 JSON 文本
     * @return 展开后的记录列表，按数据源出现顺序排列
     * @throws BusinessException 输入不是 JSON 数组、数组为空、JSON 语法非法或节点缺失必填字段时抛出，
     *                           消息携带第几级第几个节点、缺失字段名等定位信息
     */
    public static List<ParsedRegion> parse(String json) {
        JsonNode root;
        try {
            root = OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            // JSON 语法非法：按数据源格式错误处理，附原因
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "行政区划数据源 JSON 解析失败：" + e.getOriginalMessage());
        }
        if (root == null || !root.isArray()) {
            // 顶层必须是数组，否则判定为数据源结构错误
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "行政区划数据源格式错误：顶层应为区划节点数组，实际不是数组");
        }
        if (root.isEmpty()) {
            // 空数组无任何区划数据，直接判定失败，由同步服务拒绝执行
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "行政区划数据源为空数组，没有任何区划记录");
        }
        List<ParsedRegion> result = new ArrayList<>();
        flatten(root, "0", 1, result);
        return result;
    }

    /**
     * 递归展开一层节点数组。
     *
     * @param nodes     当前层级的节点数组
     * @param parentCode 父级代码，第一层传入 '0' 表示省级无父级
     * @param level     当前层级（1 省 / 2 市 / 3 区县）
     * @param result    输出收集容器
     */
    private static void flatten(JsonNode nodes, String parentCode, int level, List<ParsedRegion> result) {
        for (int i = 0; i < nodes.size(); i++) {
            JsonNode node = nodes.get(i);
            // 逐节点校验必填字段，缺失时报出层级与序号，便于排查数据源结构漂移
            if (node == null || !node.isObject()) {
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        "行政区划数据源格式错误：第 " + level + " 级第 " + (i + 1) + " 个节点不是对象");
            }
            JsonNode codeNode = node.get("code");
            if (codeNode == null || codeNode.isNull() || codeNode.asText().isBlank()) {
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        "行政区划数据源格式错误：第 " + level + " 级第 " + (i + 1) + " 个节点缺少 code 字段");
            }
            JsonNode nameNode = node.get("name");
            if (nameNode == null || nameNode.isNull() || nameNode.asText().isBlank()) {
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        "行政区划数据源格式错误：第 " + level + " 级第 " + (i + 1) + " 个节点缺少 name 字段");
            }
            result.add(new ParsedRegion(codeNode.asText(), nameNode.asText(), parentCode, level));
            // 递归展开子级；超过三级的数据按能力边界忽略
            JsonNode children = node.get("children");
            if (children != null && !children.isNull() && children.isArray() && level < MAX_LEVEL) {
                flatten(children, codeNode.asText(), level + 1, result);
            }
        }
    }

    /**
     * 解析后的单条行政区划记录。
     */
    @Data
    @AllArgsConstructor
    public static class ParsedRegion {

        /**
         * 行政区划代码。
         */
        private String code;

        /**
         * 行政区划名称。
         */
        private String name;

        /**
         * 父级行政区划代码，省级为 '0'。
         */
        private String parentCode;

        /**
         * 层级：1 省 / 2 市 / 3 区县。
         */
        private int level;
    }
}
