package com.budai;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.junit.Before;
import org.junit.Test;

import java.awt.print.PrinterException;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.UUID;

public class DemoApplicationTests {

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
                // 填入 minio API
                .endpoint("http://{url}:{port}")
                // 填入用户名、密码
                .credentials("username", "password")
                .build();
    }


    /**
     * Describe：判断存储桶是否已存在
     *
     * 当用户存放文件时可以进行判断，如果用户选择的存储桶存在则存入文件
     * 如果用户选择的存储桶不存在则新建相应的桶再存入文件
     */
    @Test
    public void ExistMinioBucket() throws Exception {
        boolean found = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket("lwbucket")
                    .build());
        if (found) {
            System.out.println("lwbucket 已存在");
        } else {
            System.out.println("lwbucket 不存在");
        }
    }


    /**
     * Describe：新建存储桶
     *
     * 存储桶名不能重复且命名必须遵守Amazon S3标准
     * 不能含有下划线等特殊字符，不能包含大写字符
     */
    @Test
    public void CreateMinioBucket() throws Exception {
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
    public void DeleteMinioBucket() throws Exception {
        minioClient.removeBucket(RemoveBucketArgs.builder()
                .bucket("newbucket")
                .build());
    }


    /**
     * Describe：从指定存储桶获取文件
     */
    @Test
    public void GetMinioFile() throws Exception {
        try (InputStream is = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket("newbucket")
                        .object("1.csv")        
                        .build())) {
            int ch;
            while ((ch = is.read()) != -1) {
                // 控制台打印内容，可根据需要进行额外操作
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
        builder.append("1.csv");
        // 最终在存储桶中的文件名格式：4569d587-514e-4caa-852a-af277eadb48b_1.csv

        // 生产环境中文件流通常通过接口参数传入
        File file = new File("./resources/Files/test.csv");
        if (file.isFile()) {
            FileInputStream fis = new FileInputStream(file);
            try {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket("lwbucket")
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
                        .object("1.csv")
                        .build());
    }


    /**
     * Describe：列出当前存储桶下所有文件相关信息
     */
    @Test
    public void ListEntireMinio() throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket("lwbucket")
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
                        .bucket("lwbucket")
                        .build());

        StringBuilder builder = new StringBuilder();
        // 设置时间点
        Timestamp earliest = Timestamp.valueOf("1970-01-01 00:00:001");

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
        fileName.append("1.csv");

        // 从本地 E:\TEMP\CSV 下读取 1.csv 文件存入桶中
        File file = new File("E:\\TEMP\\CSV\\1.csv");
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
