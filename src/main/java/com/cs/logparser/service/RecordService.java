package com.cs.logparser.service;


import com.cs.logparser.domain.Record;
import com.cs.logparser.repository.RecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RecordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordService.class);

    @Autowired
    private RecordRepository recordRepository;


    public void save(Record record) {
        recordRepository.save(record);

    }

    public Map<String, Record> save(Map<String, Record> records) {

        Map<String, Record> incompleteRecords = new HashMap<>();

        records.forEach((k,v) -> {
            if (v.getDuration() != 0) {
                LOGGER.debug("Saving record {}", v);
                recordRepository.save(v);

            } else {
                incompleteRecords.put(k,v);
            }
        });

        return incompleteRecords;
    }


}
