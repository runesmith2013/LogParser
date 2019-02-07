package com.cs.logparser.repository;

import com.cs.logparser.domain.Record;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest
public class RecordRepositoryTest {

    @Autowired
    private RecordRepository recordRepository;


    /*
     * Basic test to ensure that we can write to and read from the database
     */
    @Test
    public void whenSavingRecords_thenCorrect() {

        recordRepository.deleteAll();

        Record testRecord = new Record("id1", "someType", "host", 100, true );
        recordRepository.save(testRecord);

        Record testRecord2 = new Record("id2", null, null, 4, true );
        recordRepository.save(testRecord2);

        Record testRecord3 = new Record("id3", "someType", "host", 3, false );
        recordRepository.save(testRecord3);

        assertThat(recordRepository.count(), is (3L));

        Record storedRecord = recordRepository.findById("id3").get();
        assertThat(storedRecord.getDuration(), is (3L));

    }

}
