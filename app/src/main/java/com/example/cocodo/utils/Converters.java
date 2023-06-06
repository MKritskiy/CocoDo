package com.example.cocodo.utils;

import androidx.room.TypeConverter;

import java.sql.Date;

public class Converters {
    // Пре��бразование Timestamp в обычное время
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    // Преобразование обычного времени во Timestamp
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}