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

    private String builderFilePath(String dirPath, String filename, boolean addUUID) {
        if (!StringUtils.hasText(filename)) {
            log.error("文件名为空");
            throw new RuntimeException("上传文件失败");
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.hasText(dirPath)) {
            stringBuilder.append(dirPath).append(SEPARATOR);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String todayStr = sdf.format(new Date());
        stringBuilder.append(todayStr).append(SEPARATOR);
        if (addUUID) {
            if (filename.contains(".")) {
                String name = filename.substring(0, filename.lastIndexOf("."));
                String fileType = filename.substring(filename.lastIndexOf("."), filename.length());
                stringBuilder.append(name).append("_").append(UUID.randomUUID().toString().replaceAll("-", "")).append(fileType);
            } else {
                stringBuilder.append(filename).append("_").append(UUID.randomUUID().toString().replaceAll("-", ""));
            }
        } else {
            stringBuilder.append(filename);
        }
        return stringBuilder.toString();
    }

    @Override
    public String uploadImgFile(String prefix, String filename, InputStream inputStream, boolean addUUID) {
        String postfix = getImgPostfix(filename);
        return doUploadImgFile(prefix, filename, inputStream, addUUID, postfix);
    }

    private String getImgPostfix(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            log.error("文件名为空");
            throw new RuntimeException("上传文件失败");
        }
        String postfix = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        if ("gif".equals(postfix)) {
            return MediaType.IMAGE_GIF_VALUE;
        } else if ("jpeg".equals(postfix) || "jpg".equals(postfix)) {
            return MediaType.IMAGE_JPEG_VALUE;
        } else if ("png".equals(postfix)) {
            return MediaType.IMAGE_PNG_VALUE;
        } else {
            log.error("不支持该类型图片");
            throw new RuntimeException("上传文件失败");
        }
    }

    private String doUploadImgFile(String prefix, String filename, InputStream inputStream, boolean addUUID, String postfix) {
        String filePath = builderFilePath(prefix, filename, addUUID);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object(filePath)
                    .contentType(postfix)
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
    public String uploadHtmlFile(String prefix, String filename, InputStream inputStream, boolean addUUID) {
        String filePath = builderFilePath(prefix, filename, addUUID);
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
