import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.junit.Before;
import org.junit.Test;

import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class FilesHandleTest {

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
     * Describe：从指定存储桶获取文件
     */
    @Test
    public void GetMinioFile() throws Exception {
        try (InputStream is = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket("newbucket")
                        .object("user.csv")
                        .build())) {
            int ch;
            while ((ch = is.read()) != -1) {
                // 控制台打印内容
                System.out.write(ch);
            }
        } catch (XmlParserException | ServerException | NoSuchAlgorithmException
                | InsufficientDataException | InvalidKeyException | IOException e) {
            // 应针对异常进行分类处理，这里demo测试就简单抛出。
            throw new PrinterException();
        } catch (InvalidResponseException | ErrorResponseException | InternalException e) {
            // 应针对异常进行分类处理，这里demo测试就简单抛出。
            throw new PrinterException();
        }
    }

    /**
     * Describe：将文件存入指定顶存储桶内
     * <p>
     * Minio 以完整文件名为唯一标识，如果文件名重复，则会直接覆盖
     * 在存入文件时，建议在原有文件名之前拼接一个UUID或者时间戳
     */
    @Test
    public void PutMinioFile() throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append(UUID.randomUUID());
        builder.append("_");
        builder.append("user.csv");
        // 最终在存储桶中的文件名格式：<uuid>_user.csv

        // 生产环境中文件流通常通过接口参数传入
        File file = new File("src/main/resources/files/user.csv");
        if (file.isFile()) {
            FileInputStream fis = new FileInputStream(file);
            try {
                ObjectWriteResponse objectWriteResponse = minioClient.putObject(PutObjectArgs.builder()
                        .bucket("testbucket")
                        .object(builder.toString())
                        .stream(fis, fis.available(), -1)
                        .build());

                System.out.println(objectWriteResponse.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("文件不存在！");
        }
    }

    /**
     * Describe：从指定存储桶内删除文件
     */
    @Test
    public void DeleteMinioFile() throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket("newbucket")
                        .object("user.csv")
                        .build());
    }

    @Test
    public void MinioUrl() throws Exception {
        String bucketName = "testbucket";
        String objectName = "bg.csv";
        Integer expires = 7;

        String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(expires, TimeUnit.HOURS)
                        .build());
        System.out.println(url);
    }

    @Test
    public void FileExist() throws Exception {
        String bucketName = "testbucket";
        String objectName = "bdg.jpg";
        Integer expires = 7;

        List<String> list = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .build());
        for (Result<Item> result : results) {
            if (!result.get().isDir()) {
                list.add(result.get().objectName());
            }
        }
        System.out.println(list);

        if (list.contains(objectName)) {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expires, TimeUnit.HOURS)
                            .build());
            System.out.println(url);
        } else {
            System.out.println("File doesn't exist.");
        }
    }

    /*static {
        System.out.println(Method.GET);
        System.out.println(Method.POST);
        System.out.println(Method.DELETE);
        System.out.println(Method.PUT);
        System.out.println(Method.HEAD);

        System.out.println(TimeUnit.SECONDS);
        System.out.println(TimeUnit.MINUTES);
        System.out.println(TimeUnit.HOURS);
        System.out.println(TimeUnit.DAYS);
    }*/
}
