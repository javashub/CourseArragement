package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 *
 * @author lequal
 * @since 2020-04-03
 */
@TableName("tb_course_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseInfo extends Model<CourseInfo> {

    private static final long serialVersionUID=1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 课程编号
     */
    private String courseNo;

    /**
     * 课程名
     */
    private String courseName;

    /**
     * 课程属性
     */
    private String courseAttr;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * 课程状态
     */
    private Integer status;

    /**
     * 优先级
     */
    private Integer piority;

    /**
     * 备注
     */
    private String remark;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
