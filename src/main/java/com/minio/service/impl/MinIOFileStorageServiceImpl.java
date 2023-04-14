package com.minio.service.impl;

import com.minio.autoconfigure.MinIOConfigProperties;
import com.minio.service.MinIOFileStorageService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @author PBJ
 * @since 15.04.2023
 */
@Slf4j
public class MinIOFileStorageServiceImpl implements MinIOFileStorageService {

    private final MinioClient minioClient;

    private final MinIOConfigProperties minIOConfigProperties;

    private final static String SEPARATOR = "/";

    public MinIOFileStorageServiceImpl(MinioClient minioClient, MinIOConfigProperties minIOConfigProperties) {
        this.minioClient = minioClient;
        this.minIOConfigProperties = minIOConfigProperties;
    }

    private String getFileType(String fileName) {

        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    /**
     * @param dirPath
     * @param filename
     * @return
     */
    private String builderFilePath(String dirPath, String filename) {
        if (!StringUtils.hasText(filename)) {
            log.error("文件名为空");
            throw new RuntimeException("上传文件失败");
        }
        String name = filename.substring(0, filename.lastIndexOf("."));
        String fileType = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.hasText(dirPath)) {
            stringBuilder.append(dirPath).append(SEPARATOR);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String todayStr = sdf.format(new Date());
        stringBuilder.append(todayStr).append(SEPARATOR);
        stringBuilder.append(name).append("_").append(UUID.randomUUID().toString().replaceAll("-", "")).append(".").append(fileType);
        return stringBuilder.toString();
    }

    @Override
    public String uploadImgFile(String prefix, String filename, InputStream inputStream) {
        String filePath = builderFilePath(prefix, filename);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object(filePath)
                    .contentType(MediaType.IMAGE_JPEG_VALUE)
                    .bucket(minIOConfigProperties.getBucket()).stream(inputStream, inputStream.available(), -1)
                    .build();
            minioClient.putObject(putObjectArgs);
            StringBuilder urlPath = new StringBuilder(minIOConfigProperties.getReadPath());
            urlPath.append(SEPARATOR + minIOConfigProperties.getBucket()).append(SEPARATOR).append(filePath);
            return urlPath.toString();
        } catch (Exception ex) {
            log.error("minio put file error.", ex);
            throw new RuntimeException("上传文件失败");
        }
    }

    @Override
    public String uploadHtmlFile(String prefix, String filename, InputStream inputStream) {
        String filePath = builderFilePath(prefix, filename);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object(filePath)
                    .contentType(MediaType.TEXT_HTML_VALUE)
                    .bucket(minIOConfigProperties.getBucket()).stream(inputStream, inputStream.available(), -1)
                    .build();
            minioClient.putObject(putObjectArgs);
            StringBuilder urlPath = new StringBuilder(minIOConfigProperties.getReadPath());
            urlPath.append(SEPARATOR + minIOConfigProperties.getBucket()).append(SEPARATOR).append(filePath);
            return urlPath.toString();
        } catch (Exception ex) {
            log.error("minio put file error.", ex);
            throw new RuntimeException("上传文件失败");
        }
    }

    @Override
    public void delete(String pathUrl) {
        String key = pathUrl.replace(minIOConfigProperties.getEndpoint() + SEPARATOR, "");
        int index = key.indexOf(SEPARATOR);
        String bucket = key.substring(0, index);
        String filePath = key.substring(index + 1);
        // 删除Objects
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket(bucket).object(filePath).build();
        try {
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            log.error("minio remove file error.  pathUrl:{}", pathUrl);
        }
    }

    @Override
    public byte[] downLoadFile(String pathUrl) {
        String key = pathUrl.replace(minIOConfigProperties.getEndpoint() + SEPARATOR, "");
        int index = key.indexOf(SEPARATOR);
        String filePath = key.substring(index + 1);
        InputStream inputStream = null;
        try {
            inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(minIOConfigProperties.getBucket()).object(filePath).build());
        } catch (Exception e) {
            log.error("minio down file error.  pathUrl:{}", pathUrl);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byte[] buff = new byte[100];
            int rc = 0;
            while ((rc = inputStream.read(buff, 0, 100)) > 0) {
                byteArrayOutputStream.write(buff, 0, rc);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
        } finally {
            try {
                byteArrayOutputStream.close();
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
            }
        }
        return new byte[0];
    }
}
