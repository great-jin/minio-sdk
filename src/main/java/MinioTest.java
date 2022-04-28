import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.junit.Before;
import org.junit.Test;

import java.awt.print.PrinterException;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class MinioTest {

    /**
     * 参考文档
     *
     * https://docs.min.io/docs/java-client-api-reference.html
     * https://docs.aws.amazon.com/AmazonS3/latest/userguide/BucketRestrictions.html
     */

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
     * Describe：从创建存储桶到上传、下载文件、查看信息到删除的完整流程
     */
    @Test
    public void EntireProcessMinio() throws Exception{
        String bucketName = "testbucket";

        // 1.判断桶是否存在，如果不存在则创建对应的桶
        boolean isExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build());
        if (!isExists) {
            // 不存在，进行创建
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build());
        }

        // 2.读取本地文件存入对应存储桶中
        StringBuilder fileName = new StringBuilder();
        // 定义文件名
        fileName.append(UUID.randomUUID());
        fileName.append("_");
        fileName.append("user.csv");

        // 读取 user.csv 文件存入桶中
        File file = new File("src/main/resources/files/user.csv");
        if (file.isFile()) {
            FileInputStream fis = new FileInputStream(file);
            try {
                // 将文件存入存储桶内
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName.toString())
                        .stream(fis, fis.available(), -1)
                        .build());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("文件不存在！");
        }

        // 3.获取桶内所有文件信息
        // 查看文件信息多次调用，这里对方法 ListMinio(String bucketName) 简单封装
        System.out.println("File Info: " + ListMinio(bucketName));

        // 4.获取上传的文件
        try (InputStream is = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName.toString())
                        .build())) {
            int ch;
            while ((ch = is.read()) != -1) {
                // 控制台输出文件内容
                System.out.write(ch);
            }
        }catch (XmlParserException | ServerException | NoSuchAlgorithmException
                | InsufficientDataException | InvalidKeyException | IOException e) {
            // 生产环境中应针对异常进行分类处理，这里demo测试就简单抛出。
            throw new PrinterException();
        } catch (InvalidResponseException | ErrorResponseException | InternalException e) {
            // 生产环境中应针对异常进行分类处理，这里demo测试就简单抛出。
            throw new PrinterException();
        }

        // 5.删除上传的文件
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName.toString())
                        .build());

        // 6.再次打印桶内文件信息
        System.out.println("\nFile Info Print Again : " + ListMinio(bucketName));

        // 7.删除存储桶
        minioClient.removeBucket(RemoveBucketArgs.builder()
                .bucket(bucketName)
                .build());

        System.out.println("\n完成流程已经结束。");
    }


    /**
     * Describe: 对查看文件信息方法进行封装
     */
    public String ListMinio(String bucketName) throws Exception{
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .build());

        StringBuilder fileInfo = new StringBuilder();
        for (Result<Item> result : results) {
            fileInfo.append(result.get().objectName());
            fileInfo.append(", ");
            fileInfo.append(result.get().lastModified());
            fileInfo.append(", ");
            fileInfo.append(result.get().size());
            fileInfo.append("\n");
        }
        return  fileInfo.toString();
    }
}
