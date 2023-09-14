package com.tangl.pan.core.constants;

import org.apache.commons.lang3.StringUtils;

/**
 * @author tangl
 * @description 公用基础常量类
 * @create 2023-06-22 14:22
 */
public interface TPanConstants {

    /**
     * 公用的字符串分隔符
     */
    String COMMON_SEPARATOR = "__,__";

    /**
     * 空字符串
     */
    String EMPTY_STR = StringUtils.EMPTY;

    /**
     * 点
     */
    String POINT_STR = ".";

    /**
     * 斜杠字符串
     */
    String SLASH_STR = "/";

    /**
     * Long 常量 0
     */
    Long ZERO_LONG = 0L;

    /**
     * Integer 常量 0
     */
    Integer ZERO_INT = 0;

    /**
     * Integer 常量 1
     */
    Integer ONE_INT = 1;

    /**
     * Integer 常量 2
     */
    Integer TWO_INT = 2;

    /**
     * Integer 常量 -1
     */
    Integer MINUS_ONE_INT = -1;

    /**
     * TRUE 字符串
     */
    String TRUE_STR = "true";

    /**
     * FALSE 字符串
     */
    String FALSE_STR = "false";

    /**
     * 组件扫描基础路径
     */
    String BASE_COMPONENT_SCAN_PATH = "com.tangl.pan";

    /**
     * 问号常量
     */
    String QUESTION_MARK_STR = "?";

    /**
     * 等号常量
     */
    String EQUALS_MARK_STR = "=";

    /**
     * 逻辑与常量
     */
    String AND_MARK_STR = "&";

    /**
     * 左中括号常量
     */
    String LEFT_BRACKET_STR = "[";

    /**
     * 右中括号常量
     */
    String RIGHT_BRACKET_STR = "]";
}
