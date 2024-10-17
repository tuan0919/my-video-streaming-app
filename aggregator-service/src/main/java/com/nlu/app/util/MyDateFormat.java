package com.nlu.app.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class MyDateFormat {

    // Sử dụng LinkedHashMap để giữ thứ tự của các entries
    Map<Long, Function<LocalDateTime, String>> strategy = new LinkedHashMap<>();

    public MyDateFormat() {
        strategy.put(60L, this::formatSeconds);
        strategy.put(3600L, this::formatMinutes);
        strategy.put(86400L, this::formatHours);
        strategy.put(86400L * 29, this::formatDays);
        strategy.put(86400L * 30 * 12, this::formatMonths);
        strategy.put(86400L * 365 * 10, this::formatYears);
        strategy.put(Long.MAX_VALUE, this::formatDate);
    }

    private String formatDate(LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return time.format(formatter);
    }

    private String formatSeconds(LocalDateTime time) {
        long elapseSeconds = ChronoUnit.SECONDS.between(time, LocalDateTime.now());
        return elapseSeconds + " giây trước";
    }

    private String formatMinutes(LocalDateTime time) {
        long elapseMinutes = ChronoUnit.MINUTES.between(time, LocalDateTime.now());
        return elapseMinutes + " phút trước";
    }

    private String formatHours(LocalDateTime time) {
        long elapseHours = ChronoUnit.HOURS.between(time, LocalDateTime.now());
        return elapseHours + " giờ trước";
    }

    private String formatDays(LocalDateTime time) {
        long elapseDays = ChronoUnit.DAYS.between(time, LocalDateTime.now());
        return elapseDays + " ngày trước";
    }

    private String formatMonths(LocalDateTime time) {
        long elapseMonths = ChronoUnit.MONTHS.between(time, LocalDateTime.now());
        return elapseMonths + " tháng trước";
    }

    private String formatYears(LocalDateTime time) {
        long elapseYears = ChronoUnit.YEARS.between(time, LocalDateTime.now());
        return elapseYears + " năm trước";
    }

    public String relativeToCurrentTime(LocalDateTime time) {
        long elapseSeconds = ChronoUnit.SECONDS.between(time, LocalDateTime.now());
        return strategy.entrySet().stream()
                .filter(entry -> elapseSeconds < entry.getKey())
                .findFirst()
                .get()
                .getValue()
                .apply(time);
    }
}
