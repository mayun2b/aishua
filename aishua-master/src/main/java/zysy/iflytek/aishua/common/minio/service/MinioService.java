package zysy.iflytek.aishua.common.minio.service;

import io.minio.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import zysy.iflytek.aishua.config.properties.MinioProperties;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 通用基础服务组件，负责相关业务逻辑与流程处理。
 */
@Service
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    /**
     * 构造方法，负责注入依赖组件。
     */
    public MinioService(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    /**
     * 定义业务能力接口。
     */
    public void createBucketIfNotExists() {
        try {
            String bucketName = properties.getBucketName();

            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("创建对象存储桶失败", e);
        }
    }

    /**
     * 定义业务能力接口。
     */
    public String upload(MultipartFile file) {
        try {
            createBucketIfNotExists();

            String originalFilename = file.getOriginalFilename();
            String suffix = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String objectName = LocalDate.now() + "/" + UUID.randomUUID() + suffix;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1L)
                            .contentType(file.getContentType())
                            .build()
            );

            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("文件上传到对象存储失败", e);
        }
    }

    /**
     * 定义业务能力接口。
     */
    public InputStream download(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("从对象存储下载文件失败", e);
        }
    }

    /**
     * 定义业务能力接口。
     */
    public StatObjectResponse getObjectInfo(String objectName) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("获取对象存储文件信息失败", e);
        }
    }

    /**
     * 定义业务能力接口。
     */
    public void remove(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("删除对象存储文件失败", e);
        }
    }

    /**
     * 定义业务能力接口。
     */
    public String getPreviewUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Http.Method.GET)
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("生成对象存储预览链接失败", e);
        }
    }
}
