package com.minio.autoconfigure;

import com.minio.service.MinIOFileStorageService;
import com.minio.service.impl.MinIOFileStorageServiceImpl;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author PBJ
 * @since 15.04.2023
 */
@Configuration
@EnableConfigurationProperties({MinIOConfigProperties.class})
@ConditionalOnClass(MinioClient.class)
public class MinIOAutoConfiguration {

    private final MinIOConfigProperties minIOConfigProperties;

    public MinIOAutoConfiguration(MinIOConfigProperties minIOConfigProperties) {
        this.minIOConfigProperties = minIOConfigProperties;
    }

    @Bean
    public MinioClient buildMinioClient() {
        return MinioClient
                .builder()
                .credentials(minIOConfigProperties.getAccessKey(), minIOConfigProperties.getSecretKey())
                .endpoint(minIOConfigProperties.getEndpoint())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(MinIOFileStorageService.class)
    public MinIOFileStorageService weatherService(MinioClient minioClient) {
        return new MinIOFileStorageServiceImpl(minioClient, minIOConfigProperties);
    }

}
