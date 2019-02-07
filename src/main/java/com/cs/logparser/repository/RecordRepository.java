package com.cs.logparser.repository;

import com.cs.logparser.domain.Record;
import org.springframework.data.repository.CrudRepository;

public interface RecordRepository extends CrudRepository<Record, String> {
}
