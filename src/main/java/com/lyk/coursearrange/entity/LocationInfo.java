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
 * @since 2020-03-20
 * 位置信息，例如高一的教学楼有哪些
 */
@TableName("tb_location_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationInfo extends Model<LocationInfo> {

    private static final long serialVersionUID=1L;

    /**
     * id,位置信息，高一在哪栋楼，高二在哪
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 教学楼编号,放教学楼表中编号
     */
    private String teachbuildNo;

    /**
     * 年级编号,放年级表中的id
     */
    private String gradeNo;

    /**
     * 逻辑删除
     */
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
