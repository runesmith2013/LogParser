# LogParser

## Overview
The LogParser reads JSON lines from a sample file into a buffer.
These log files contain START or FINISHED data for a given event.
When writing to the buffer, if we've seen one portion of the event already we update the record with the other portion
and calculate the duration (FINISHED - START). If the duration is longer than 4ms, the Alert flag is set

Once the buffer reaches its limit, records which are complete (we've seen both START and FINISHED lines) are flushed to
the database.

Once we reach the end of the file, the remaining elements in the buffer are also flushed.

TO RUN
======
- clone the repository
- run the gradle classes task
- run the com.cs.logparser.App class from your IDE (I used IntelliJ)

Note: for a prod system, I'd use the gradle bootJar task to create an executable jar file.

Design decisions
----------------
In the interests of time, I chose to use Spring Boot as the base framework for the application.
This gave the following advantages:
- easy database integration with Spring Data JPA, abstracting away the complexities of managing databases and ORM mapping
- pre-configured database connection pool with HikariCP
- creates the database tables at start (if configured)
- easy testing
- easy logging using slf4j
- its fun to work with

When reading the log file lines, I use Files.lines() to construct a Stream.
This reads lazily from the log file, so we should be able to handle any amount of log data
(Note: there should be a formal test for handling large amounts of data)

Test cases were developed using TDD.

Edge cases:
In the case where a large amount of data is incomplete, i.e only one record exists, the buffer will grow until it consumes
the entire heap and the application will crash.
How to mitigate is something to discuss with the business users. Potential options are:
1. drop the incomplete records
2. flush them to the database with incomplete columns.

Multi-threading

I ran over time before being able to implement multi-threading.
My plan would be:
- use the ExecutorService to create a fixed thread pool based on the number of cpu cores.
- set up the ExecutorService to shutdown if not given work for a period of time (5s?).
This prevents the app hanging while the service waits for new work
- when flushing the buffer, collect all the completed records into a list and give it to a worker thread to write to the
database.

Batching the write of multiple records into a single transaction will be more efficient than the current process of
one write per record.


Note: some quick research suggests that a single thread to read the log file is typically faster than muti-threaded reads.
This is an area that would bear testing out.

Other
1. I ran over time before setting up HSQL DB as a standalone file-based store.
This should be a simple matter of installing and starting it, and configuring the application.properties to point to the
new URL.

2. The app looks on the classpath for the sample.json file to read in.
In a prod system this is likely a parameter to be passed in as a command line arg.

