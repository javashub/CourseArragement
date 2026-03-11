package com.lyk.coursearrange.system.dict.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典项实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_item")
public class SysDictItem extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String dictTypeCode;

    private String itemCode;

    private String itemName;

    private String itemValue;

    private Integer status;

    private Integer sortNo;

    private Integer isDefault;

    private String extJson;
}
