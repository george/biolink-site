package dev.george.biolink.model;

import com.google.gson.TypeAdapter;

import java.time.format.DateTimeFormatter;

public abstract class JsonModel<T> extends TypeAdapter<T> {

    protected static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:dd:mm");
}