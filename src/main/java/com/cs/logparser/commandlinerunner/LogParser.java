package com.cs.logparser.commandlinerunner;

import com.cs.logparser.domain.Record;
import com.cs.logparser.service.RecordService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class LogParser implements CommandLineRunner {

    private String fileName;
    private static final Logger LOGGER = LoggerFactory.getLogger(LogParser.class);

    @Autowired
    private RecordService recordService;

    private static final int BUFFER_SIZE = 5;
    private Map<String, Record> recordBuffer;

    @Override
    public void run(String... args) throws Exception {

        String pathToFile = locateFile();

        ObjectMapper mapper = new ObjectMapper();
        recordBuffer = new HashMap<>();

        //-- read file into a stream
        //-- Files.lines will lazily read lines, ensuring that we can handle large files
        //-- try-with-resources ensures that the file will be auto closed
        try (Stream<String> stream = Files.lines(Paths.get(pathToFile))) {

            stream.forEach((s) -> {
                try {

                    //-- read the record and convert from JSON to a Record object
                    Record record = mapper.readValue(s, Record.class);

                    //-- If the record already exists in the buffer, then update it
                    //-- this would be the case if we've seen one state but not the other
                    Record existingRecord = recordBuffer.putIfAbsent(record.getId(), record);
                    if (existingRecord != null) {
                        record.setValues(existingRecord);
                        recordBuffer.put(record.getId(), record);
                    }

                    //-- if the buffer gets big enough, flush it to the database
                    if (recordBuffer.size() >= BUFFER_SIZE) {

                        //-- TODO: use the ExecutorService to run this in a multi-threaded manner
                        flushRecords(recordBuffer);

                    }
                } catch (IOException mie) {
                    String errorMessage = String.format("Error parsing lines from the JSON file %s ", fileName);
                    LOGGER.error(errorMessage, mie);
                    throw new IllegalStateException(errorMessage);
                }

            });


        } catch (IOException e) {
            String errorMessage = String.format("Error reading the JSON file %s ", fileName);
            LOGGER.error(errorMessage, e);
            throw new IOException(errorMessage);
        }

        //-- once we've read all the records, flush the remaining buffer
        flushRecords(recordBuffer);

        //-- we may have incomplete records
        if (recordBuffer.size() > 0) {
            LOGGER.warn("Incomplete records found! {} ", recordBuffer);
        }
    }

    /**
     * Look up the file that we'll be processing
     * @return
     */
    private String locateFile() {
        URL systemResource  = ClassLoader.getSystemResource(fileName);
        if (systemResource == null) {
            LOGGER.error("Unable to find the requested file {} ", fileName);
            throw new IllegalStateException("Unable to find the requested file " + fileName);
        }

        return systemResource.getFile();
    }


    private void flushRecords(Map<String,Record> records) {
        Map<String, Record> incompleteRecords = recordService.save(records);
        records.clear();
        records.putAll(incompleteRecords);
    }

    @Value("${json.filename}")
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Map<String, Record> getRecordBuffer() {
        return this.recordBuffer;
    }
}
