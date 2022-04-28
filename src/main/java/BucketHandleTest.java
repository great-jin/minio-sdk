import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
import io.minio.messages.Bucket;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BucketHandleTest {

    public MinioClient minioClient;

    /**
     * Describe：初始化 Minio 对象
     */
    @Before
    public void init() throws Exception {
        minioClient = MinioClient.builder()
                // 填入 Minio API
                .endpoint("http://10.231.6.61:9000")
                // 填入用户名、密码
                .credentials("minioadmin", "minio123456")
                .build();
    }

    /**
     * Describe：查询所有存储桶
     *
     */
    @Test
    public void ListBucket() throws Exception {
        List<Bucket> bucketList = minioClient.listBuckets();
        List<String> list = new ArrayList<>();
        for (Bucket bucket : bucketList) {
            list.add(bucket.name());
        }

        System.out.println(list);
    }

    /**
     * Describe：判断存储桶是否已存在
     *
     * 当用户存放文件时可以进行判断，如果用户选择的存储桶存在则存入文件
     * 如果用户选择的存储桶不存在则新建相应的桶再存入文件
     */
    @Test
    public void ExistBucket() throws Exception {
        boolean found = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket("bucket")
                        .build());
        if (found) {
            System.out.println("bucket 已存在");
        } else {
            System.out.println("bucket 不存在");
        }
    }

    /**
     * Describe：新建存储桶
     *
     * 存储桶名不能重复且命名必须遵守Amazon S3标准
     * 不能含有下划线等特殊字符，不能包含大写字符
     */
    @Test
    public void CreateBucket() throws Exception {
        minioClient.makeBucket(MakeBucketArgs.builder()
                .bucket("newbucket")
                .build());
    }

    /**
     * Describe：删除存储桶
     *
     * 只能删除空的存储桶，非空存储桶想要删除必须先清空桶内文件
     */
    @Test
    public void DeleteBucket() throws Exception {
        minioClient.removeBucket(RemoveBucketArgs.builder()
                .bucket("newbucket")
                .build());
    }
}
