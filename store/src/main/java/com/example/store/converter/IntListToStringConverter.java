package com.example.store.converter;

import jakarta.persistence.AttributeConverter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IntListToStringConverter implements AttributeConverter<List<Integer>, String> {
    @Override
    public String convertToDatabaseColumn(List<Integer> integers) {
        return integers == null ? null : integers.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @Override
    public List<Integer> convertToEntityAttribute(String data) {
        return (data == null || data.isEmpty()) ? List.of() :
            Arrays.stream(data.split(","))
            .map(Integer::valueOf)
            .collect(Collectors.toList());

    }
}
