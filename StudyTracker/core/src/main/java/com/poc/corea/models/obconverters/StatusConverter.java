package com.poc.corea.models.obconverters;

import com.google.gson.Gson;
import com.poc.corea.models.session.Status;

import io.objectbox.converter.PropertyConverter;

public class StatusConverter implements PropertyConverter<Status, String> {
    @Override
    public Status convertToEntityProperty(String databaseValue) {
        return new Gson().fromJson(databaseValue, Status.class);
    }

    @Override
    public String convertToDatabaseValue(Status entityProperty) {
        return new Gson().toJson(entityProperty);
    }
}
