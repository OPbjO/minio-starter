package com.minio.service;

import java.io.InputStream;

/**
 * @author itheima
 */
public interface MinIOFileStorageService {

    /**
     * 上传图片文件
     *
     * @param prefix      文件前缀 默认传空
     * @param filename    文件名
     * @param inputStream 文件流
     * @param addUUID     是否在文件名上添加UUID，默认否
     * @return 文件全路径
     */
    String uploadImgFile(String prefix, String filename, InputStream inputStream, boolean addUUID);

    /**
     * 上传html文件
     *
     * @param prefix      文件前缀 默认传空
     * @param filename    文件名
     * @param inputStream 文件流
     * @param addUUID     是否在文件名上添加UUID，默认否
     * @return 文件全路径
     */
    String uploadHtmlFile(String prefix, String filename, InputStream inputStream, boolean addUUID);

    /**
     * 删除文件
     *
     * @param pathUrl 文件全路径
     */
    void delete(String pathUrl);

    /**
     * 下载文件
     *
     * @param pathUrl 文件全路径
     * @return
     */
    byte[] downLoadFile(String pathUrl);

}
