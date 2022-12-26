package com.flylee.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.flylee.gulimall.common.constraints.ListValue;
import com.flylee.gulimall.common.validator.group.AddGroup;
import com.flylee.gulimall.common.validator.group.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 品牌
 * 
 * @author flylee
 * @email flyleekungfu@163.com
 * @date 2022-06-23 07:53:32
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@TableId
	@Null(message = "添加时品牌id必须为空", groups = AddGroup.class)
	@NotNull(message = "修改时品牌id不能为空", groups = UpdateGroup.class)
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名必须提交", groups = {AddGroup.class, UpdateGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@URL(message = "提交品牌logo的地址不是有效网址", groups = {AddGroup.class, UpdateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(message = "显示状态不能为空", groups = {AddGroup.class, UpdateGroup.class})
	@ListValue(vals = {0, 1}, groups = AddGroup.class)
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotBlank(message = "检索首字母不能为空", groups = {AddGroup.class, UpdateGroup.class})
	@Pattern(regexp = "/^[a-zA-Z]$/")
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(message = "排序不能为空", groups = {AddGroup.class, UpdateGroup.class})
	@Min(value = 0)
	private Integer sort;

}
