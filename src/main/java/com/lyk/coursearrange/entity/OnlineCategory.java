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
@TableName("tb_online_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineCategory extends Model<OnlineCategory> {

    private static final long serialVersionUID=1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 父类别id,默认为0表示该分类为一级分类
     */
    private Integer parentId;

    /**
     * 类别编号
     */
    private String categoryNo;

    /**
     * 网课类别名称
     */
    private String categoryName;

    /**
     * 优先级
     */
    private Integer piority;

    /**
     * 备注
     */
    private String remark;

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
