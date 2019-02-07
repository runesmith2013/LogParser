package com.cs.logparser.commandlinerunner;

import com.cs.logparser.App;
import com.cs.logparser.domain.Record;
import com.cs.logparser.repository.RecordRepository;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = App.class, initializers = ConfigFileApplicationContextInitializer.class)
public class LogParserTest {

    @Autowired
    private LogParser logParser;

    @Autowired
    private RecordRepository repository;

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    /*
     * Test that we can process the sample file correctly
     */
    @Test
    public void testSampleFile_allRecordsSaveCorrectly() throws Exception {

        logParser.setFileName("sample.json");
        logParser.run();

        assertThat(repository.count(), is(3L));

        Record record = repository.findById("scsmbstgra").get();
        assertThat(record.getDuration(), is(5L));
        assertThat(record.getType(), is ("APPLICATION_LOG"));
        assertThat(record.getHost(), is ("12345"));
        assertThat(record.isAlert(), is (true));

        record = repository.findById("scsmbstgrb").get();
        assertThat(record.getDuration(), is(3L));
        assertThat(record.getType(), isEmptyOrNullString ());
        assertThat(record.getHost(), isEmptyOrNullString ());
        assertThat(record.isAlert(), is (false));

        record = repository.findById("scsmbstgrc").get();
        assertThat(record.getDuration(), is(8L));
        assertThat(record.getType(), isEmptyOrNullString ());
        assertThat(record.getType(), isEmptyOrNullString ());
        assertThat(record.isAlert(), is (true));

        //-- all logs parsed correctly
        assertThat(logParser.getRecordBuffer().size(), is (0));

    }

    /*
     * Test that incomplete data is handled correctly
     */
    @Test
    public void testIncompleteFile_allRecordsSaveCorrectly() throws Exception {

        logParser.setFileName("incomplete.json");
        logParser.run();

        assertThat(repository.count(), is(2L));

        Record record = repository.findById("scsmbstgra").get();
        assertThat(record.getDuration(), is(5L));
        assertThat(record.getType(), is ("APPLICATION_LOG"));
        assertThat(record.getHost(), is ("12345"));
        assertThat(record.isAlert(), is (true));

        record = repository.findById("scsmbstgrc").get();
        assertThat(record.getDuration(), is(8L));
        assertThat(record.getType(), isEmptyOrNullString ());
        assertThat(record.getType(), isEmptyOrNullString ());
        assertThat(record.isAlert(), is (true));

        //-- one incomplete log record left in the buffer
        assertThat(logParser.getRecordBuffer().size(), is (1));
    }


    /*
     * Test that a missing file failure is handled correctly
     */
    @Test
    public void testMissingFile_failsCorrectly() throws Exception {

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Unable to find the requested file unknown.json");

        logParser.setFileName("unknown.json");
        logParser.run();

    }


    /*
     * Test that a file with invalid data is handled correctly
     */
    @Test
    public void testInvalidData_failsCorrectly() throws Exception {

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Error parsing lines from the JSON file invalid.json ");

        logParser.setFileName("invalid.json");
        logParser.run();

    }

}
