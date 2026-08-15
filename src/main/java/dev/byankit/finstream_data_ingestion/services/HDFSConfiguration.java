package dev.byankit.finstream_data_ingestion.services;

import org.apache.hadoop.fs.FileSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class HDFSConfiguration {

    @Value("${hdfs.uri}")
    private String hdfsUri;

    @Bean
    public FileSystem getHdfsFileSystem() {
        org.apache.hadoop.conf.Configuration configuration = new org.apache.hadoop.conf.Configuration();
        configuration.set(
            "fs.defaultFS",
            hdfsUri
        );

        try {
            return FileSystem.get(configuration);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
