package com.minio.autoconfigure;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

@Data
@ConfigurationProperties(prefix = "minio")  // 文件上传 配置前缀
public class MinIOConfigProperties implements Serializable {

    /**
     * minio控制台账号
     */
    private String accessKey;
    /**
     * minio控制台密码
     */
    private String secretKey;
    /**
     * 桶名称，相当于文件夹名称
     */
    private String bucket;
    /**
     * 连接地址，如http://127.0.0.1:9000
     */
    private String endpoint;
    /**
     * 读取地址，如http://127.0.0.1:9000
     */
    private String readPath;
}
