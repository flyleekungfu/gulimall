package com.flylee.gulimall.ware.controller;

import com.flylee.gulimall.common.param.BasePageParam;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.common.utils.R;
import com.flylee.gulimall.ware.entity.PurchaseEntity;
import com.flylee.gulimall.ware.service.PurchaseService;
import com.flylee.gulimall.ware.vo.MergeVO;
import com.flylee.gulimall.ware.vo.PurchaseDoneVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 采购信息
 *
 * @author flylee
 * @email flyleekungfu@163.com
 * @date 2022-06-23 19:49:23
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    @PostMapping("/done")
    public R done(@RequestBody PurchaseDoneVO purchaseDoneVO) {
        purchaseService.done(purchaseDoneVO);
        return R.ok();
    }
    @PostMapping("/received")
    public R received(@RequestBody List<Long> ids) {
        purchaseService.received(ids);
        return R.ok();
    }

    @PostMapping("/merge")
    public R merge(@RequestBody MergeVO mergeVO) {
        purchaseService.mergePurchase(mergeVO);
        return R.ok();
    }

    @GetMapping("/unreceive/list")
    public R unreceiveList(BasePageParam pageParam) {
        PageUtils page = purchaseService.queryPageUnreceived(pageParam);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody PurchaseEntity purchase){
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
