package com.orderservice.util;

import com.orderservice.enums.TrendWindow;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public final class TrendWindowUtils {

    private TrendWindowUtils() {
    }

    public static LocalDate windowStart(TrendWindow trendWindow, LocalDate today) {
        return switch (trendWindow) {
            case DAILY -> today;
            case WEEKLY -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTHLY -> today.withDayOfMonth(1);
        };
    }
}
