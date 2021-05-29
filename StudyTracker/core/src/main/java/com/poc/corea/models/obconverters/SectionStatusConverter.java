package com.poc.corea.models.obconverters;

import com.google.gson.Gson;
import com.poc.corea.models.summary.SectionStatus;

import io.objectbox.converter.PropertyConverter;

public class SectionStatusConverter implements PropertyConverter<SectionStatus, String> {
    @Override
    public SectionStatus convertToEntityProperty(String databaseValue) {
        return new Gson().fromJson(databaseValue, SectionStatus.class);
    }

    @Override
    public String convertToDatabaseValue(SectionStatus entityProperty) {
        return new Gson().toJson(entityProperty);
    }
}
