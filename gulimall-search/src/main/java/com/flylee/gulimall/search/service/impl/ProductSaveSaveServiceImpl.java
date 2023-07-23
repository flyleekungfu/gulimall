package com.flylee.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.flylee.gulimall.common.to.es.SkuEsModel;
import com.flylee.gulimall.search.config.GulimallElasticSearchConfig;
import com.flylee.gulimall.search.constant.EsConstant;
import com.flylee.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductSaveSaveServiceImpl implements ProductSaveService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();

        skuEsModels.forEach(skuEsModel -> {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            indexRequest.source(JSON.toJSONString(skuEsModel), XContentType.JSON);
            bulkRequest.add(indexRequest);
        });

        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        // 如果批量错误
        if (bulkResponse.hasFailures()) {
            List<String> ids = Arrays.stream(bulkResponse.getItems()).map(BulkItemResponse::getId).collect(Collectors.toList());
            log.error("商品上架错误：{}", ids);
        }

        return !bulkResponse.hasFailures();
    }
}
