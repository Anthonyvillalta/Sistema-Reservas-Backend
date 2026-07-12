package com.campestre.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ReservationCodeGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String PREFIX = "RES";

    public static String generateCode(LocalDate fecha, long sequence) {
        return String.format("%s-%s-%04d", PREFIX, fecha.format(DATE_FORMAT), sequence);
    }
}
