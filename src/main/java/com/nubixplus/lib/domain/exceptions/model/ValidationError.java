package com.nubixplus.lib.domain.exceptions.model;

import org.springframework.validation.ObjectError;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public record ValidationError(String field, String message) {

    public static ValidationError of(ObjectError error) {

        final String name = Stream.of(Objects.requireNonNull(error.getCodes())).map((value) -> {
            String[] split = value.split("\\.");
            return String.join(".", Arrays.copyOfRange(split, 1, split.length));
        }).findFirst().orElse(error.getObjectName());

        return new ValidationError(name, error.getDefaultMessage());
    }
}
