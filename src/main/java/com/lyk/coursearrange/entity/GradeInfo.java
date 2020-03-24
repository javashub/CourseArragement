package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author lequal
 * @since 2020-03-20
 */
@TableName("tb_grade_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GradeInfo extends Model<GradeInfo> {

    private static final long serialVersionUID=1L;

    /**
     * id,年级表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 年级编号
     */
    private String gradeNo;

    /**
     * 年级名称
     */
    private String gradeName;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
