package com.lyk.coursearrange.auth.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 当前用户界面主题保存请求。
 */
@Data
public class AuthUiThemeUpdateRequest implements Serializable {

    private static final long serialVersionUID = 6308217548193203451L;

    /**
     * 主题标识：daylight / deep-teal / top-nav。
     */
    private String uiTheme;
}
