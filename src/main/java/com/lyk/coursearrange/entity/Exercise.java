package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * @author lequal
 * @since 2020-05-21
 */
@TableName("tb_exercise")
@Data
public class Exercise extends Model<Exercise> {

    private static final long serialVersionUID=1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 题目类别id
     */
    private Integer categoryId;

    /**
     * 题目所属班级，编号
     */
    private String classNo;

    /**
     * 题目名称
     */
    private String exerciseTitle;

    /**
     * 是否多选，默认0单选，，1多选
     */
    private Integer multiselect;

    /**
     * 选项
     */
    private String answer;

    /**
     * 选项A的值
     */
    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    private String optionE;

    private String optionF;

    /**
     * 分值
     */
    private Integer fraction;

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
