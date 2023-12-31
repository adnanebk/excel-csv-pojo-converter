package com.adnanebk.excelcsvconverter.excelcsv.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Optional;

public class DateParserFormatterUtil {

    private static final String[] DATE_TIME_PATTERNS = {
            "yyyy-MM-dd HH:mm:ss",
            "MM/dd/yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "EEE, dd MMM yyyy HH:mm:ss z",
            "EEE MMM dd HH:mm:ss zzz yyyy",
            "dd-MM-yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm",
            "dd/MM/yyyy HH:mm:ss",
            "EEE, dd MMM yyyy HH:mm:ss z",
            "dd MMM yyyy HH:mm",
            "dd MMM yyyy HH:mm:ss",
            "dd MMM yyyy HH:mm:ss z"
    };
    private static final String[] DATE_PATTERNS = {
            "yyyy-MM-dd",
            "MM/dd/yyyy",
            "dd/MM/yyyy",
            "dd-MMM-yyyy",
            "dd/MMM/yyyy",
            "dd/MM/yyyy",
            "dd-MM-yyyy",
            "dd MMM yyyy",
    };
    private SimpleDateFormat dateFormatter;
    private final DateTimeFormatter localedDateFormatter;
    private final DateTimeFormatter localedDateTimeFormatter;
    private final DateTimeFormatter zonedDateTimeFormatter;

    public DateParserFormatterUtil(String datePattern,String dateTimePattern) {
        dateFormatter = Optional.of(dateTimePattern).filter(s->!s.isEmpty())
                .map(SimpleDateFormat::new).orElse(new SimpleDateFormat());
        localedDateFormatter = Optional.of(datePattern).filter(s->!s.isEmpty())
                .map(DateTimeFormatter::ofPattern).orElse(DateTimeFormatter.ISO_LOCAL_DATE);
        localedDateTimeFormatter = Optional.of(dateTimePattern).filter(s->!s.isEmpty())
                .map(DateTimeFormatter::ofPattern).orElse(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        zonedDateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    }

    public synchronized Date parseToDate(String date) throws ParseException {
        try {
            return dateFormatter.parse(date);
        } catch (ParseException | NumberFormatException ex) {
            for (String pattern : DATE_TIME_PATTERNS) {
                dateFormatter.applyPattern(pattern);
                try {
                    return dateFormatter.parse(date);
                } catch (ParseException | NumberFormatException ignored) {
                }
            }
            throw ex;
        }
    }

    public LocalDateTime parseToLocalDateTime(String date) {
        try {
            return LocalDateTime.parse(date, localedDateTimeFormatter);
        } catch (DateTimeParseException ex) {
            for (String pattern : DATE_TIME_PATTERNS) {
                try {
                    return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(pattern));
                } catch (DateTimeParseException ignored) {
                }
            }
            throw ex;
        }
    }

    public LocalDate parseToLocalDate(String date) {
        try {
            return LocalDate.parse(date, localedDateFormatter);
        } catch (DateTimeParseException ex) {
            for (String pattern : DATE_PATTERNS) {
                try {
                    return LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern));
                } catch (DateTimeParseException ignored) {
                }
            }
            throw ex;
        }
    }

    public ZonedDateTime parseToZonedDateTime(String date) {
        return ZonedDateTime.parse(date, zonedDateTimeFormatter);
    }

    public String format(Date date) {
       return dateFormatter.format(date);
    }
    public String format(LocalDate date) {
        return localedDateFormatter.format(date);
    }
    public String format(LocalDateTime date) {
        return localedDateTimeFormatter.format(date);
    }
    public String format(ZonedDateTime date) {
        return zonedDateTimeFormatter.format(date);
    }
}
