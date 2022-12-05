package com.flylee.gulimall.product;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.flylee.gulimall.product.entity.BrandEntity;
import com.flylee.gulimall.product.service.BrandService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallProductApplicationTests {

    @Resource
    private BrandService brandService;

    @Resource
    private OSSClient ossClient;

    @Test
    public void testUpload() throws FileNotFoundException {
        //// Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        //String endpoint = "";
        //// 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        //String accessKeyId = "";
        //String accessKeySecret = "";
        // 创建OSSClient实例。
        //OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 填写Bucket名称，例如examplebucket。
        String bucketName = "gulimall-flylee";
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        String objectName = "exampledir/exampleobject.txt";
        // 填写本地文件的完整路径，例如D:\\localpath\\examplefile.txt。
        // 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
        String filePath= "D:\\test\\test.txt";

        try {
            InputStream inputStream = new FileInputStream(filePath);
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, inputStream);
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Test
    public void contextLoads() {
        //BrandEntity brandEntity = new BrandEntity();
        //brandEntity.setName("测试");
        //brandService.save(brandEntity);

        BrandEntity brandEntity = brandService.getOne(Wrappers.<BrandEntity>lambdaQuery()
                .eq(BrandEntity::getName, "测试"));
        System.out.println(brandEntity);
    }

}
