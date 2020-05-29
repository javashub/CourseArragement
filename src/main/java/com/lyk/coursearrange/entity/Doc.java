package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * @author lequal
 * @since 2020-05-27
 * 学习文档，作业(以文档形式下发，学生下载查看或者在线预览)
 */
@TableName("tb_doc")
@Data
public class Doc extends Model<Doc> {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * doc文件名
     */
    private String docName;

    /**
     * 自定义的名字
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String docUrl;

    /**
     * 文件描述
     */
    private String description;

    /**
     * 目标班级
     */
    private String toClassNo;

    /**
     * 发布者id
     */
    private Integer fromUserId;

    /**
     * 发布者名字
     */
    private String fromUserName;

    /**
     * 来自的用户类型 1:管理员。2：讲师
     */
    private Integer fromUserType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 阅读次数
     */
    private Long clicks;

    /**
     * 有效天数
     */
    private Integer expire;

    @TableLogic
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
