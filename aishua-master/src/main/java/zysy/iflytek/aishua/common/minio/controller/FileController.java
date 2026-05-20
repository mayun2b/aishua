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

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final MinioService minioService;

    public FileController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        String objectName = minioService.upload(file);

        return ResponseEntity.ok(Map.of(
                "objectName", objectName,
                "message", "上传成功"
        ));
    }

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

    @GetMapping("/preview-url")
    public ResponseEntity<?> previewUrl(@RequestParam String objectName) {
        String url = minioService.getPreviewUrl(objectName);

        return ResponseEntity.ok(Map.of(
                "url", url
        ));
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam String objectName) {
        minioService.remove(objectName);

        return ResponseEntity.ok(Map.of(
                "message", "删除成功"
        ));
    }
}
