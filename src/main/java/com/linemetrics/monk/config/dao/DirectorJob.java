package com.linemetrics.monk.config.dao;

import com.linemetrics.monk.config.ConfigException;
import com.linemetrics.monk.config.ISystemConfigStore;
import org.joda.time.Period;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.TimeZone;

public class DirectorJob {

    ISystemConfigStore store;

    int id;
    String schedulerMask;
    JSONObject properties;

    public DirectorJob(ISystemConfigStore store) {
        this.store = store;
    }

    public int getId() {
        return id;
    }

    public DirectorJob setId(int id) {
        this.id = id;
        return this;
    }

    public String getSchedulerMask() {
        return this.properties.containsKey("scheduler_mask")
            ? (String)this.properties.get("scheduler_mask")
            : "";
    }

    public String getTimeZone() {
        return this.properties.containsKey("timezone")
            ? (String)this.properties.get("timezone")
            : "Europe/Vienna";
    }

    public TimeZone getTimeZoneObject() {
        return TimeZone.getTimeZone(getTimeZone());
    }

    public String getBatchSize() {
        return this.properties.containsKey("batch_size")
            ? (String)this.properties.get("batch_size")
            : "PT1m";
    }

    public long getBatchSizeInMillis() {
        return new Period(getBatchSize()).toStandardDuration().getMillis();
    }

    public String getDuration() {
        return this.properties.containsKey("duration")
            ? (String)this.properties.get("duration")
            : "PT1H";
    }

    public long getDurationInMillis() {
        return new Period(getDuration()).toStandardDuration().getMillis();
    }

    public String getDurationSlice() {
        return this.properties.containsKey("duration_slice")
            ? (String)this.properties.get("duration_slice")
            : null;
    }

    public long getDurationSliceInMillis() throws ConfigException {
        if(getDurationSlice() == null) {
            throw new ConfigException("Duration Slice is not defined for Job-ID: " + this.id);
        }
        return new Period(getDurationSlice()).toStandardDuration().getMillis();
    }

    public String getProperties() {
        return properties == null ? "{}" : properties.toJSONString();
    }

    public DirectorJob setProperties(String properties) {
        Object json = JSONValue.parse(properties);
        return this.setPropertiesObject(json);
    }

    public DirectorJob setPropertiesObject(Object json) {
        if(json instanceof JSONObject) {
            this.properties = (JSONObject) json;
        } else {
            this.properties = new JSONObject();
        }
        return this;
    }

    public boolean save() {
        try {
            this.store.createDirectorJob(this);
        } catch(Exception exp) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DirectorJob{" +
            "id=" + id +
            ", settings=" + properties +
            '}';
    }
}
