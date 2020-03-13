package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author lequal
 * @since 2020-03-06
 */
@TableName("tb_class_info")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClassInfo extends Model<ClassInfo> {

    private static final long serialVersionUID=1L;

    /**
     * id,班级表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 班级编号
     */
    private String classNo;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 班级人数
     */
    private Integer num;

    /**
     * 班主任id
     */
    private Integer teacher;

    /**
     * 备注
     */
    private String remark;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
