package zysy.iflytek.aishua.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 系统配置属性，承载外部配置项并提供类型化读取。
 */
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private Integer previewExpiryDays = 7;

    /**
     * 查询并返回处理结果。
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * 保存并更新业务处理结果。
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * 查询并返回处理结果。
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * 保存并更新业务处理结果。
     */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    /**
     * 查询并返回处理结果。
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * 保存并更新业务处理结果。
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * 查询并返回处理结果。
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * 保存并更新业务处理结果。
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * 查询并返回处理结果。
     */
    public Integer getPreviewExpiryDays() {
        return previewExpiryDays;
    }

    /**
     * 保存并更新业务处理结果。
     */
    public void setPreviewExpiryDays(Integer previewExpiryDays) {
        this.previewExpiryDays = previewExpiryDays;
    }
}
