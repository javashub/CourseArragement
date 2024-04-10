package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author lequal
 * @since 2020-03-23
 */
@TableName("tb_classroom")
@Data
public class Classroom extends Model<Classroom> {

    private static final long serialVersionUID=1L;

    /**
     * 教室id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 教室编号
     */
    private String classroomNo;

    /**
     * 教室名称
     */
    private String classroomName;

    /**
     * 所在教学楼编号
     */
    private String teachbuildNo;

    /**
     * 教室人数容量
     */
    private Integer capacity;

    /**
     * 教室属性
     */
    private String attr;

    /**
     * 备注
     */
    private String remark;

    /**
     * 逻辑删除（默认0显示，1删除）
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
