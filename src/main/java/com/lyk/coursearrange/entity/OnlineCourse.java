package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * @author lequal
 * @since 2020-06-04
 */
@TableName("tb_online_course")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineCourse extends Model<OnlineCourse> {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 网课编号，100000，6位数
     */
    private String onlineNo;

    /**
     * 网课课程名
     */
    private String onlineName;

    /**
     * 课程简介
     */
    private String description;

    /**
     * 封面
     */
    private String cover;

    /**
     * 网课类型id
     */
    private Integer onlineCategoryId;

    /**
     * 网课类别名称
     */
    private String onlineCategoryName;

    /**
     * 优先级
     */
    private Integer piority;

    /**
     * 发布者类型，1：管理员，2：讲师
     */
    private Integer fromUserType;

    /**
     * 发布者id
     */
    private Integer fromUserId;

    /**
     * 发布者名称
     */
    private String fromUserName;

    /**
     * 点击次数
     */
    private Long clicks;

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
