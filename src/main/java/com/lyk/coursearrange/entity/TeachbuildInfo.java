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
 * @since 2020-03-20
 * 教学楼信息
 */
@TableName("tb_teach_build_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeachbuildInfo extends Model<TeachbuildInfo> {

    private static final long serialVersionUID=1L;

    /**
     * id,教学楼信息表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 教学楼编号
     */
    private String teachBuildNo;

    /**
     * 教学楼名称
     */
    private String teachBuildName;

    /**
     * 教学楼位置
     */
    private String teachBuildLocation;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
