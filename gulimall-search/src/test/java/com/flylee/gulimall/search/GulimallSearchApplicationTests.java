package com.flylee.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.flylee.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;


@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Resource
    private RestHighLevelClient client;

    @Test
    public void searchData() throws IOException {
        // 1、创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        // 指定索引
        searchRequest.indices("bank");

        // 指定DSL检索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 1.1）、构造检索条件
        sourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));

        // 1.2）、按照年龄的值分布进行聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        sourceBuilder.aggregation(ageAgg);

        // 1.3）、计算平均薪资
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        sourceBuilder.aggregation(balanceAvg);

        System.out.println("检索条件：" + sourceBuilder);
        searchRequest.source(sourceBuilder);

        // 2、执行检索
        SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        // 3、分析结果
        System.out.println("检索结果：" + searchResponse);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit : searchHits) {
            Account account = JSON.parseObject(searchHit.getSourceAsString(), Account.class);
            System.out.println("account: " + account);
        }

        // 3.2）、获取这次检索到的分析信息
        Aggregations aggregations = searchResponse.getAggregations();
        Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            System.out.println("年龄：" + bucket.getKeyAsString() + "人数：" + bucket.getDocCount());
        }

        Avg balanceAvg1 = aggregations.get("balanceAvg");
        System.out.println("平均薪资：" + balanceAvg1.getValue());
    }

    /**
     * 索引数据到ES，也可以更新
     * @throws IOException
     */
    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        // 以键值对形式建立索引内容
        //indexRequest.source("userName", "test", "age", 18, "gender", "male");
        User user = new User();
        user.setUserName("test");
        user.setAge(18);
        user.setGender("male");
        // 以JSON字符串形式建立索引内容
        indexRequest.source(JSON.toJSONString(user), XContentType.JSON);
        IndexResponse indexResponse = client.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(indexResponse);
    }
    @Test
    public void contextLoads() {
        System.out.println(client);
    }

    @Data
    class User {
        private String userName;
        private Integer age;
        private String gender;
    }

    @Data
    static class Account {

        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;

    }
}
