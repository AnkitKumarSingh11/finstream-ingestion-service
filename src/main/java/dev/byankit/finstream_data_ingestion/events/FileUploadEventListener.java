package dev.byankit.finstream_data_ingestion.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class FileUploadEventListener {

    private static final Logger log = LoggerFactory.getLogger(FileUploadEventListener.class);

    @Async
    @EventListener
    public void handleFileUploadEvent(FileUploadEvent event) {
        log.info("FileUploadEvent logged: fileId '{}' (docId '{}') from source '{}' is stored at '{}' and ready for external Spark/Airflow ingestion.",
                event.fileId(), event.documentId(), event.fileSource(), event.filePath());
        //  send the event to the configured Kafka topic
    }
}
