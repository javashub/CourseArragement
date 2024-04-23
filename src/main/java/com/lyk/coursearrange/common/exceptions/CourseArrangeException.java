package com.lyk.coursearrange.common.exceptions;

import com.lyk.coursearrange.common.enums.ErrorCodeEnum;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author lyk
 * @version 1.0
 * @date 2024/4/23 21:52
 * @description
 */
@NoArgsConstructor
public class CourseArrangeException extends AbstractCourseArrangeException implements Serializable {

    private static final long serialVersionUID = 1L;

    public CourseArrangeException(ErrorCodeEnum error) {
        super(error.getDesc(), error.getCode());
    }

    public CourseArrangeException(String message) {
        super(message, ErrorCodeEnum.ERROR.getCode());
    }
}
