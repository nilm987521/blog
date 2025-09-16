package cc.nilm.blog.service;

import cc.nilm.blog.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    public String storeFile(String postId, MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            if (originalFileName.contains("..")) {
                log.error("Sorry! Filename contains invalid path sequence {}", originalFileName);
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }

            // 確保 bucket 存在
            ensureBucketExists();

            // 上傳文件到 MinIO
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(minioConfig.getBucketName().concat("/").concat(postId))
                        .object(originalFileName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
                );
            }

            log.info("文件 {} 已成功上傳到 MinIO", originalFileName);
            return postId.concat("/").concat(originalFileName);
        } catch (Exception ex) {
            log.error("Could not store file {}. Please try again!", originalFileName, ex);
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build()
            );
            
            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .build()
                );
                log.info("創建 bucket: {}", minioConfig.getBucketName());
            }
        } catch (Exception ex) {
            log.error("Error creating bucket", ex);
            throw new RuntimeException("Error creating bucket", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .build()
            );
            
            byte[] content = inputStream.readAllBytes();
            inputStream.close();
            
            return new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };
        } catch (Exception ex) {
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .build()
            );
            log.info("文件 {} 已從 MinIO 刪除", fileName);
        } catch (Exception ex) {
            throw new RuntimeException("Error deleting file " + fileName, ex);
        }
    }

    public String getFileUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .expiry(24, TimeUnit.HOURS)
                    .build()
            );
        } catch (Exception ex) {
            throw new RuntimeException("Error generating file URL " + fileName, ex);
        }
    }
}