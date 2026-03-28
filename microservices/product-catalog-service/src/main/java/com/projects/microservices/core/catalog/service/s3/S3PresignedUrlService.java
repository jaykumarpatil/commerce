package com.projects.microservices.core.catalog.service.s3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class S3PresignedUrlService {

    private static final Logger LOG = LoggerFactory.getLogger(S3PresignedUrlService.class);
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Value("${aws.s3.bucket-name:ecommerce-products}")
    private String bucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.s3.presigned-url-expiry-seconds:3600}")
    private int presignedUrlExpirySeconds;

    public Mono<PresignedUploadResponse> generatePresignedUploadUrl(String productId, String filename, 
                                                                    String contentType, long fileSize) {
        return Mono.fromCallable(() -> {
            String extension = getFileExtension(filename);
            String objectKey = generateObjectKey(productId, filename, extension);
            
            String uploadUrl = String.format("https://%s.s3.%s.amazonaws.com/%s?presigned=true", bucketName, region, objectKey);
            String publicUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, objectKey);
            
            LOG.info("Generated presigned URL for {} -> {}", filename, publicUrl);
            
            return new PresignedUploadResponse(
                    uploadUrl,
                    publicUrl,
                    objectKey,
                    presignedUrlExpirySeconds
            );
        });
    }

    public Mono<PresignedDownloadResponse> generatePresignedDownloadUrl(String objectKey) {
        return Mono.fromCallable(() -> {
            String downloadUrl = String.format("https://%s.s3.%s.amazonaws.com/%s?presigned=true", bucketName, region, objectKey);
            return new PresignedDownloadResponse(downloadUrl, presignedUrlExpirySeconds);
        });
    }

    public Mono<Void> deleteObject(String objectKey) {
        return Mono.fromRunnable(() -> {
            LOG.info("Would delete object: {}", objectKey);
        });
    }

    public Mono<List<S3ObjectInfo>> listProductImages(String productId) {
        return Mono.fromCallable(() -> {
            String prefix = String.format("products/%s/", productId);
            LOG.info("Would list objects with prefix: {}", prefix);
            return List.of();
        });
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }

    private String generateObjectKey(String productId, String filename, String extension) {
        String uuid = UUID.randomUUID().toString();
        return String.format("products/%s/%s_%s.%s", productId, 
                filename.substring(0, Math.min(filename.lastIndexOf('.'), filename.length())), uuid, extension);
    }

    public record PresignedUploadResponse(
            String uploadUrl,
            String publicUrl,
            String objectKey,
            int expiresInSeconds
    ) {}

    public record PresignedDownloadResponse(
            String downloadUrl,
            int expiresInSeconds
    ) {}

    public record S3ObjectInfo(
            String key,
            String url,
            long size,
            Instant lastModified
    ) {}
}
