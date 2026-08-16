package dev.byankit.finstream_data_ingestion.config;

import org.apache.hadoop.fs.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("hdfs")
public class HdfsHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(HdfsHealthIndicator.class);
    private final FileSystem hdfsFileSystem;

    @Autowired
    public HdfsHealthIndicator(FileSystem hdfsFileSystem) {
        this.hdfsFileSystem = hdfsFileSystem;
    }

    @Override
    public Health health() {
        try {
            if (hdfsFileSystem != null && hdfsFileSystem.getScheme() != null) {
                return Health.up()
                        .withDetail("scheme", hdfsFileSystem.getScheme())
                        .withDetail("uri", hdfsFileSystem.getUri().toString())
                        .build();
            }
            return Health.down().withDetail("reason", "HDFS FileSystem bean unavailable").build();
        } catch (Exception e) {
            log.warn("HDFS health check failed: {}", e.getMessage());
            return Health.down(e)
                    .withDetail("uri", hdfsFileSystem != null ? hdfsFileSystem.getUri().toString() : "unknown")
                    .build();
        }
    }
}
