package com.flylee.gulimall.ware.controller;

import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.common.utils.R;
import com.flylee.gulimall.ware.WareSkuPageParam;
import com.flylee.gulimall.ware.entity.WareSkuEntity;
import com.flylee.gulimall.ware.service.WareSkuService;
import com.flylee.gulimall.common.to.SkuHasStockVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;


/**
 * 商品库存
 *
 * @author flylee
 * @email flyleekungfu@163.com
 * @date 2022-06-23 19:49:23
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * sku是否有库存
     * @param skuIds sku主键列表
     * @return sku是否有库存
     */
    @PostMapping("/hasstock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds) {
        List<SkuHasStockVO> skuHasStockVOList = wareSkuService.getSkuHasStock(skuIds);
        return R.ok().setData(skuHasStockVOList);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(WareSkuPageParam pageParam){
        PageUtils page = wareSkuService.queryByCondition(pageParam);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
