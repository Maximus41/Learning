package com.poc.corea.models.obconverters;

import com.google.gson.Gson;
import com.poc.corea.models.summary.CumulativeStatus;

import io.objectbox.converter.PropertyConverter;

public class CumulativeStatusConverter implements PropertyConverter<CumulativeStatus, String> {
    @Override
    public CumulativeStatus convertToEntityProperty(String databaseValue) {
        return new Gson().fromJson(databaseValue, CumulativeStatus.class);
    }

    @Override
    public String convertToDatabaseValue(CumulativeStatus entityProperty) {
        return new Gson().toJson(entityProperty);
    }
}
