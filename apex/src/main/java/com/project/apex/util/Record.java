package com.project.apex.util;

public record Record<T>(String type, T data) {
    public Record(T data) {
        this("unknown", data);
    }
}