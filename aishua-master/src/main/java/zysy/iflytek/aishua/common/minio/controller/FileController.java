package zysy.iflytek.aishua.common.minio.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zysy.iflytek.aishua.common.minio.service.MinioService;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 文件控制器，提供上传、下载、预览链接与删除接口。
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final MinioService minioService;

    /**
     * 构造方法，注入当前类所需依赖。
     */
    public FileController(MinioService minioService) {
        this.minioService = minioService;
    }

    /**
     * 接口处理入口，负责参数接收与服务调用。
     */
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        String objectName = minioService.upload(file);

        return ResponseEntity.ok(Map.of(
                "objectName", objectName,
                "message", "上传成功"
        ));
    }

    /**
     * 接口处理入口，负责参数接收与服务调用。
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam String objectName) {
        try (InputStream inputStream = minioService.download(objectName)) {
            byte[] bytes = inputStream.readAllBytes();

            String filename = objectName.substring(objectName.lastIndexOf("/") + 1);
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + encodedFilename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(bytes);
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }

    /**
     * 接口处理入口，负责参数接收与服务调用。
     */
    @GetMapping("/preview-url")
    public ResponseEntity<?> previewUrl(@RequestParam String objectName) {
        String url = minioService.getPreviewUrl(objectName);

        return ResponseEntity.ok(Map.of(
                "url", url
        ));
    }

    /**
     * 删除接口入口，负责资源删除与结果返回。
     */
    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam String objectName) {
        minioService.remove(objectName);

        return ResponseEntity.ok(Map.of(
                "message", "删除成功"
        ));
    }
}
