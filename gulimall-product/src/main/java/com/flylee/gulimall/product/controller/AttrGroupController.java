package com.flylee.gulimall.product.controller;

import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.common.utils.R;
import com.flylee.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.flylee.gulimall.product.entity.AttrEntity;
import com.flylee.gulimall.product.entity.AttrGroupEntity;
import com.flylee.gulimall.product.service.AttrGroupService;
import com.flylee.gulimall.product.service.AttrService;
import com.flylee.gulimall.product.service.CategoryService;
import com.flylee.gulimall.product.vo.AttrGroupRelationVo;
import com.flylee.gulimall.product.vo.AttrGroupWithAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 属性分组
 *
 * @author flylee
 * @email flyleekungfu@163.com
 * @date 2022-06-23 07:53:32
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private AttrService attrService;

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params, @PathVariable long catelogId){
        //PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long[] catelogPath = categoryService.findCategoryPathById(attrGroup.getCatelogId());
        attrGroup.setCatelogPath(catelogPath);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    ///111/attr/relation?t=1672393319607
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable Long attrgroupId) {
        List<AttrEntity> attrEntities= attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", attrEntities);
    }

    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable Long attrgroupId, @RequestParam Map<String, Object> params) {
        PageUtils page = attrService.getNoRelationAttr(attrgroupId, params);
        return R.ok().put("page", page);
    }

    @PostMapping("/attr/relation")
    public R saveBatch(@RequestBody List<AttrAttrgroupRelationEntity> relationEntities) {
        attrService.saveRelationBatch(relationEntities);
        return R.ok();
    }

    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos) {
        attrService.deleteRelation(vos);
        return R.ok();
    }

    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrByCatelogId(@PathVariable("catelogId") Long catelogId) {
        List<AttrGroupWithAttrVo> attrGroupWithAttrVos = attrGroupService.getAttrGroupWithAttrByCatelogId(catelogId);
        return R.ok().put("data", attrGroupWithAttrVos);
    }

}
