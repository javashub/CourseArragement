package com.lyk.coursearrange.entity.response;

import com.lyk.coursearrange.entity.ClassInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: 15760
 * @Date: 2020/5/19
 * @Descripe:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassInfoVO extends ClassInfo {

    // 班主任姓名
    private String realname;

    private String gradeName;
}
