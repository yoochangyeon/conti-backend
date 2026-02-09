package com.conti.infra.s3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class S3FileService {

    private final S3Client s3Client;
    private final String bucket;
    private final String region;
    private final boolean enabled;

    public S3FileService(
            @Value("${aws.s3.enabled:false}") boolean enabled,
            @Value("${aws.s3.bucket:conti-files}") String bucket,
            @Value("${aws.s3.region:ap-northeast-2}") String region,
            org.springframework.beans.factory.ObjectProvider<S3Client> s3ClientProvider
    ) {
        this.enabled = enabled;
        this.bucket = bucket;
        this.region = region;
        this.s3Client = s3ClientProvider.getIfAvailable();
    }

    public String uploadFile(MultipartFile file, String directory) {
        if (!enabled || s3Client == null) {
            log.warn("S3 is not enabled. Returning placeholder URL for file: {}", file.getOriginalFilename());
            return "local://" + directory + "/" + file.getOriginalFilename();
        }

        String fileName = directory + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, fileName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    public void deleteFile(String fileUrl) {
        if (!enabled || s3Client == null) {
            log.warn("S3 is not enabled. Skipping delete for: {}", fileUrl);
            return;
        }

        String key = extractKeyFromUrl(fileUrl);

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    private String extractKeyFromUrl(String fileUrl) {
        String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);
        if (fileUrl.startsWith(prefix)) {
            return fileUrl.substring(prefix.length());
        }
        return fileUrl;
    }
}
