package com.lyk.coursearrange.organization.service;

import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.organization.service.RegionDivisionParser.ParsedRegion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 行政区划解析器测试。
 * 覆盖：三级展开与父子归属、空数组、非数组结构、必填字段缺失（含定位信息）、JSON 语法非法。
 */
class RegionDivisionParserTest {

    /**
     * 一份小型省市区三级样例数据，结构与真实 pca-code.json 一致。
     */
    private static final String THREE_LEVEL_JSON = "["
            + "{\"code\":\"110000\",\"name\":\"北京市\",\"children\":["
            + "{\"code\":\"110100\",\"name\":\"北京市辖区\",\"children\":["
            + "{\"code\":\"110101\",\"name\":\"东城区\"},"
            + "{\"code\":\"110102\",\"name\":\"朝阳区\"}"
            + "]}]}]";

    @Test
    void parse_shouldFlattenThreeLevelsAndAssignParents() {
        List<ParsedRegion> records = RegionDivisionParser.parse(THREE_LEVEL_JSON);

        // 一省 + 一市 + 两区县共 4 条
        assertEquals(4, records.size());
        // 按出现顺序排列，便于生成 sortNo
        Map<String, ParsedRegion> byCode = records.stream()
                .collect(Collectors.toMap(ParsedRegion::getCode, Function.identity()));

        ParsedRegion province = byCode.get("110000");
        assertEquals("北京市", province.getName());
        assertEquals(1, province.getLevel());
        // 省级 parentCode 固定为 '0'
        assertEquals("0", province.getParentCode());

        ParsedRegion city = byCode.get("110100");
        assertEquals("北京市辖区", city.getName());
        assertEquals(2, city.getLevel());
        // 市级父级为所属省的代码
        assertEquals("110000", city.getParentCode());

        ParsedRegion dongcheng = byCode.get("110101");
        assertEquals(3, dongcheng.getLevel());
        assertEquals("110100", dongcheng.getParentCode());
        ParsedRegion chaoyang = byCode.get("110102");
        assertEquals("朝阳区", chaoyang.getName());
        assertEquals("110100", chaoyang.getParentCode());
    }

    @Test
    void parse_shouldRejectEmptyArray() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> RegionDivisionParser.parse("[]"));

        assertEquals(ResultCode.BAD_REQUEST.getCode(), exception.getCode());
        // 错误消息需能让人定位是数据源为空
        assertTrue(exception.getMessage().contains("空数组"), exception.getMessage());
    }

    @Test
    void parse_shouldRejectNonArrayRoot() {
        // 顶层是对象而非数组
        String notArray = "{\"code\":\"110000\",\"name\":\"北京市\"}";
        BusinessException exception = assertThrows(BusinessException.class,
                () -> RegionDivisionParser.parse(notArray));

        assertEquals(ResultCode.BAD_REQUEST.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("不是数组"), exception.getMessage());
    }

    @Test
    void parse_shouldRejectNodeMissingNameWithLocation() {
        // 第二个省级节点缺少 name 字段，错误信息应包含层级与序号定位
        String missingName = "[{\"code\":\"110000\",\"name\":\"北京市\"},{\"code\":\"120000\"}]";
        BusinessException exception = assertThrows(BusinessException.class,
                () -> RegionDivisionParser.parse(missingName));

        assertEquals(ResultCode.BAD_REQUEST.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("第 1 级"), exception.getMessage());
        assertTrue(exception.getMessage().contains("第 2 个"), exception.getMessage());
        assertTrue(exception.getMessage().contains("name"), exception.getMessage());
    }

    @Test
    void parse_shouldRejectNodeMissingCodeWithLocation() {
        // 第一个市级节点缺少 code 字段，定位到第 2 级第 1 个
        String missingCode = "[{\"code\":\"110000\",\"name\":\"北京市\",\"children\":[{\"name\":\"北京市辖区\"}]}]";
        BusinessException exception = assertThrows(BusinessException.class,
                () -> RegionDivisionParser.parse(missingCode));

        assertEquals(ResultCode.BAD_REQUEST.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("第 2 级"), exception.getMessage());
        assertTrue(exception.getMessage().contains("第 1 个"), exception.getMessage());
        assertTrue(exception.getMessage().contains("code"), exception.getMessage());
    }

    @Test
    void parse_shouldRejectInvalidJsonSyntax() {
        // 非法 JSON 文本，应包装为业务异常而非把 Jackson 异常直接抛给上层
        BusinessException exception = assertThrows(BusinessException.class,
                () -> RegionDivisionParser.parse("not a json"));

        assertEquals(ResultCode.BAD_REQUEST.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("JSON 解析失败"), exception.getMessage());
    }

    @Test
    void parse_shouldIgnoreLevel4Children() {
        // 第四级（街道）不在能力范围内：不报错，也不纳入结果
        String withLevel4 = "[{\"code\":\"110000\",\"name\":\"北京市\",\"children\":["
                + "{\"code\":\"110100\",\"name\":\"北京市辖区\",\"children\":["
                + "{\"code\":\"110101\",\"name\":\"东城区\",\"children\":[{\"code\":\"110101001\",\"name\":\"东华门街道\"}]}"
                + "]}]}]";
        List<ParsedRegion> records = RegionDivisionParser.parse(withLevel4);

        assertEquals(3, records.size());
        // 结果中只允许出现 1/2/3 级，街道代码不应入库
        assertTrue(records.stream().allMatch(r -> r.getLevel() <= 3));
        assertTrue(records.stream().noneMatch(r -> "110101001".equals(r.getCode())));
    }
}
