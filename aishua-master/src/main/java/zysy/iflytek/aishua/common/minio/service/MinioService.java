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
 * MinIO 文件服务，封装对象存储的上传、下载、删除与预签名访问能力。
 */
@Service
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    /**
     * 构造方法，注入当前类所需依赖。
     */
    public MinioService(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    /**
     * 创建存储桶（若不存在）。
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
     * 上传文件并返回对象名。
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
     * 下载对象并返回输入流。
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
     * 查询对象元信息。
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
     * 删除指定对象。
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
     * 生成对象预览链接。
     */
    public String getPreviewUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Http.Method.GET)
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .expiry(resolvePreviewExpiryDays(), TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("生成对象存储预览链接失败", e);
        }
    }

    /**
     * MinIO 预签名链接最长建议 7 天，这里做上下界保护，避免配置异常导致运行时报错。
     */
    private int resolvePreviewExpiryDays() {
        Integer configured = properties.getPreviewExpiryDays();
        if (configured == null) {
            return 7;
        }
        return Math.min(Math.max(configured, 1), 7);
    }
}
