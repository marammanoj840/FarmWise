package com.farmwise.backend.storage;

import com.farmwise.backend.config.IntegrationConfig.IntegrationProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * S3-compatible file storage using AWS Signature V4 with the standard JDK HttpClient.
 * Works with AWS S3, Cloudflare R2, MinIO, DigitalOcean Spaces, and similar services.
 * No external SDK dependency required.
 */
@Component
public class S3CompatibleStorageProvider implements FileStorageProvider {

    private static final Logger log = LoggerFactory.getLogger(S3CompatibleStorageProvider.class);
    private static final DateTimeFormatter AMZ_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String ALGORITHM = "AWS4-HMAC-SHA256";

    private final IntegrationProperties.Storage storageConfig;
    private final HttpClient httpClient;

    public S3CompatibleStorageProvider(IntegrationProperties integrationProperties) {
        this.storageConfig = integrationProperties.getStorage();
        this.httpClient = HttpClient.newBuilder().build();
    }

    @Override
    public boolean isConfigured() {
        return storageConfig.isS3Configured();
    }

    @Override
    public String upload(MultipartFile file, String storageKey) {
        if (!isConfigured()) {
            throw new IllegalStateException("S3-compatible storage is not configured. Set S3_BUCKET, S3_ACCESS_KEY, and S3_SECRET_KEY.");
        }

        try {
            byte[] fileBytes = file.getBytes();
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
            String endpoint = resolveEndpoint();
            String bucket = storageConfig.getBucket();
            String region = storageConfig.getRegion().isBlank() ? "auto" : storageConfig.getRegion();

            URI uri = URI.create(endpoint + "/" + bucket + "/" + storageKey);
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            String amzDate = now.format(AMZ_DATE_FMT);
            String dateStamp = now.format(DATE_FMT);
            String payloadHash = sha256Hex(fileBytes);

            String canonicalHeaders = "content-type:" + contentType + "\n"
                    + "host:" + uri.getHost() + "\n"
                    + "x-amz-content-sha256:" + payloadHash + "\n"
                    + "x-amz-date:" + amzDate + "\n";
            String signedHeaders = "content-type;host;x-amz-content-sha256;x-amz-date";

            String canonicalRequest = "PUT\n"
                    + "/" + bucket + "/" + storageKey + "\n"
                    + "\n"
                    + canonicalHeaders + "\n"
                    + signedHeaders + "\n"
                    + payloadHash;

            String credentialScope = dateStamp + "/" + region + "/s3/aws4_request";
            String stringToSign = ALGORITHM + "\n"
                    + amzDate + "\n"
                    + credentialScope + "\n"
                    + sha256Hex(canonicalRequest.getBytes(StandardCharsets.UTF_8));

            byte[] signingKey = getSignatureKey(storageConfig.getSecretKey(), dateStamp, region, "s3");
            String signature = HexFormat.of().formatHex(hmacSha256(signingKey, stringToSign));
            String authHeader = ALGORITHM + " Credential=" + storageConfig.getAccessKey() + "/" + credentialScope
                    + ", SignedHeaders=" + signedHeaders + ", Signature=" + signature;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                    .header("Content-Type", contentType)
                    .header("x-amz-date", amzDate)
                    .header("x-amz-content-sha256", payloadHash)
                    .header("Authorization", authHeader)
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Uploaded file to S3: key={}, size={}", storageKey, fileBytes.length);
                // Return public URL if configured, otherwise the S3 URL
                if (storageConfig.getPublicBaseUrl() != null && !storageConfig.getPublicBaseUrl().isBlank()) {
                    return storageConfig.getPublicBaseUrl().replaceAll("/$", "") + "/" + storageKey;
                }
                return uri.toString();
            } else {
                log.error("S3 upload failed: status={}, body={}", response.statusCode(), response.body());
                throw new IOException("S3 upload failed with status " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            log.error("S3 upload error for key={}", storageKey, e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    @Override
    public void delete(String storageKey) {
        if (!isConfigured()) return;

        try {
            String endpoint = resolveEndpoint();
            String bucket = storageConfig.getBucket();
            String region = storageConfig.getRegion().isBlank() ? "auto" : storageConfig.getRegion();

            URI uri = URI.create(endpoint + "/" + bucket + "/" + storageKey);
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            String amzDate = now.format(AMZ_DATE_FMT);
            String dateStamp = now.format(DATE_FMT);
            String payloadHash = sha256Hex(new byte[0]);

            String canonicalHeaders = "host:" + uri.getHost() + "\n"
                    + "x-amz-content-sha256:" + payloadHash + "\n"
                    + "x-amz-date:" + amzDate + "\n";
            String signedHeaders = "host;x-amz-content-sha256;x-amz-date";

            String canonicalRequest = "DELETE\n"
                    + "/" + bucket + "/" + storageKey + "\n"
                    + "\n"
                    + canonicalHeaders + "\n"
                    + signedHeaders + "\n"
                    + payloadHash;

            String credentialScope = dateStamp + "/" + region + "/s3/aws4_request";
            String stringToSign = ALGORITHM + "\n"
                    + amzDate + "\n"
                    + credentialScope + "\n"
                    + sha256Hex(canonicalRequest.getBytes(StandardCharsets.UTF_8));

            byte[] signingKey = getSignatureKey(storageConfig.getSecretKey(), dateStamp, region, "s3");
            String signature = HexFormat.of().formatHex(hmacSha256(signingKey, stringToSign));
            String authHeader = ALGORITHM + " Credential=" + storageConfig.getAccessKey() + "/" + credentialScope
                    + ", SignedHeaders=" + signedHeaders + ", Signature=" + signature;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .DELETE()
                    .header("x-amz-date", amzDate)
                    .header("x-amz-content-sha256", payloadHash)
                    .header("Authorization", authHeader)
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("S3 delete: key={}, status={}", storageKey, response.statusCode());
        } catch (IOException | InterruptedException e) {
            log.error("S3 delete error for key={}", storageKey, e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private String resolveEndpoint() {
        if (storageConfig.getEndpoint() != null && !storageConfig.getEndpoint().isBlank()) {
            return storageConfig.getEndpoint().replaceAll("/$", "");
        }
        String region = storageConfig.getRegion().isBlank() ? "us-east-1" : storageConfig.getRegion();
        return "https://s3." + region + ".amazonaws.com";
    }

    // --- AWS Signature V4 helpers ---

    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] hmacSha256(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] getSignatureKey(String key, String dateStamp, String region, String service) {
        byte[] kDate = hmacSha256(("AWS4" + key).getBytes(StandardCharsets.UTF_8), dateStamp);
        byte[] kRegion = hmacSha256(kDate, region);
        byte[] kService = hmacSha256(kRegion, service);
        return hmacSha256(kService, "aws4_request");
    }
}
