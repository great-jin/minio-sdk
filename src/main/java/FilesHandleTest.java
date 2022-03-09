import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import org.junit.Before;
import org.junit.Test;

import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class FilesHandleTest {

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
        }catch (XmlParserException | ServerException | NoSuchAlgorithmException
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
     *
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
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket("bucket")
                        .object(builder.toString())
                        .stream(fis, fis.available(), -1)
                        .build());
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

}
