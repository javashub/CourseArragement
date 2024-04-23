package com.lyk.coursearrange.common.exceptions;

import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author lyk
 * @version 1.0
 * @date 2024/4/23 21:46
 * @description
 */
@NoArgsConstructor
public abstract class AbstractCourseArrangeException extends RuntimeException implements Serializable {

    protected String message;
    protected int errorCode;

    private static final long serialVersionUID = 1L;

    protected AbstractCourseArrangeException(String message, int errorCode) {
        super(message);
        this.message = message;
        this.errorCode = errorCode;
    }
}
