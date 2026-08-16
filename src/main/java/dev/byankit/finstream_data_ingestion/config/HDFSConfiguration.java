package dev.byankit.finstream_data_ingestion.config;

import org.apache.hadoop.fs.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class HDFSConfiguration {

    private static final Logger log = LoggerFactory.getLogger(HDFSConfiguration.class);

    private final HdfsProperties hdfsProperties;

    @Autowired
    public HDFSConfiguration(HdfsProperties hdfsProperties) {
        this.hdfsProperties = hdfsProperties;
    }

    @Bean
    public FileSystem getHdfsFileSystem() {
        org.apache.hadoop.conf.Configuration configuration = new org.apache.hadoop.conf.Configuration();
        configuration.set("fs.defaultFS", hdfsProperties.getUri());

        log.info("Initializing HDFS FileSystem bean with URI: {}", hdfsProperties.getUri());

        try {
            return FileSystem.get(configuration);
        } catch (IOException exception) {
            log.error("Failed to initialize HDFS FileSystem with URI: {}", hdfsProperties.getUri(), exception);
            throw new RuntimeException(exception);
        }
    }
}
