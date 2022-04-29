package minio.test;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MyTest {

    public MinioClient minioClient;

    @Before
    public void init() throws Exception {
        minioClient = MinioClient.builder()
                // 填入 Minio API
                .endpoint("http://10.231.6.61:9000")
                // 填入用户名、密码
                .credentials("minioadmin", "minio123456")
                .build();
    }

    @Test
    public void PutMinio1File() throws Exception {
        String bucketName = "testbucket";
        String fileName = "123_user.csv";

        File file = new File("src/main/resources/files/user.csv");
        if (file.isFile()) {
            try (InputStream in = new FileInputStream(file)) {
                DefaultDetector detector = new DefaultDetector();
                TikaInputStream tikaStream = TikaInputStream.get(in);
                Metadata metadata = new Metadata();
                metadata.set(Metadata.RESOURCE_NAME_KEY, fileName);
                MediaType mediatype = detector.detect(tikaStream, metadata);
                String contentType = mediatype.toString();

                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .contentType(contentType)
                        .stream(in, in.available(), -1)
                        .build());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("文件不存在！");
        }
    }

    @Test
    public void PutMinio2File() throws Exception {
        File file = new File("src/main/resources/files/user.csv");
        if (file.isFile()) {
            try (InputStream in = new FileInputStream(file)) {
                String bucketName = "testbucket";
                String fileName = "minio_user.csv";
                ContentInfo info = ContentInfoUtil.findExtensionMatch(fileName);
                String contentType = info.getMimeType();

                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .contentType(contentType)
                        .stream(in, in.available(), -1)
                        .build());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("文件不存在！");
        }
    }
}
