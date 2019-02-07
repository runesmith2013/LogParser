package com.cs.logparser.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class Record {

    @Id
    private String id;

    @Transient
    private String state;
    private String type;

    private String host;

    @Transient
    private long timestamp;
    private long duration;
    private boolean alert;

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isAlert() {
        return alert;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    public Record() {}

    public Record(String id, String type, String host, long duration, boolean alert) {
        this.id = id;
        this.type = type;
        this.host = host;
        this.duration = duration;
        this.alert = alert;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }



    public void setValues(Record record) {
        if (this.host == null) {
            this.host = record.host;
        }

        if (this.type == null) {
            this.type = record.type;
        }

        if (this.state.equals("STARTED") && record.state.equals("FINISHED")) {
            this.duration = record.timestamp - this.timestamp;
        }

        if (this.state.equals("FINISHED") && record.state.equals("STARTED")) {
            this.duration = this.timestamp - record.timestamp;
        }

        if (this.duration > 4) {
            alert = true;
        }
    }

    @Override
    public String toString() {
        return "Record{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", host='" + host + '\'' +
                ", duration=" + duration +
                ", alert=" + alert +
                '}';
    }


}
