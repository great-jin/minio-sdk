import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;

public class ListFilesTest {

    public MinioClient minioClient;

    /**
     * Describe：初始化 Minio 对象
     */
    @Before
    public void init() throws Exception {
        minioClient = MinioClient.builder()
                // 填入 Minio API
                .endpoint("http://{url}:{port}")
                // 填入用户名、密码
                .credentials("username", "password")
                .build();
    }

    /**
     * Describe：列出当前存储桶下所有文件相关信息
     */
    @Test
    public void ListEntireMinio() throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket("bucket")
                        .build());

        StringBuilder builder = new StringBuilder();
        for (Result<Item> result : results) {
            // 对查询结果进行简单拼接
            builder.append(result.get().objectName());
            builder.append(", ");
            builder.append(result.get().lastModified());
            builder.append(", ");
            builder.append(result.get().size());
            builder.append("\n");
        }
        System.out.println(builder);
    }

    /**
     * Describe：列出当前存储桶下某个时间点之后存入的文件相关信息
     */
    @Test
    public void ListPartMinio() throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket("bucket")
                        .build());

        StringBuilder builder = new StringBuilder();
        // 设置时间点
        Timestamp earliest = Timestamp.valueOf("2021-12-01 00:00:001");

        for (Result<Item> result : results) {
            Timestamp timestamp = Timestamp.from(result.get().lastModified().toInstant());
            // 如果文件最新更新时间在上述指定的时间之后进行打印
            if(timestamp.after(earliest)){
                // 打印：文件名，最后创建时间，文件大小
                builder.append(result.get().objectName());
                builder.append(", ");
                builder.append(timestamp);
                builder.append(", ");
                builder.append(result.get().size());
                builder.append("\n");
            }
        }
        System.out.println(builder);
    }

    /**
     * Describe：指定从某一个字符之后开始列出文件信息
     *
     * 例如 newbucket 桶内存有三个文件： a.csv, b.csv, c.csv,
     * .startAfter("b") 最终只会输出：b.csv, c.csv
     */
    @Test
    public void ListAlphabetMinio() throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket("newbucket")
                        .startAfter("b")
                        .build());

        StringBuilder builder = new StringBuilder();
        for (Result<Item> result : results) {
            // 打印文件名，最后创建时间，文件大小
            builder.append(result.get().objectName());
            builder.append(", ");
            builder.append(result.get().lastModified());
            builder.append(", ");
            builder.append(result.get().size());
            builder.append("\n");
        }
        System.out.println(builder);
    }

}
