package com.pharmacy.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateUtil {
    
    public static boolean isExpired(LocalDate expDate) {
        return expDate.isBefore(LocalDate.now());
    }
    
    public static boolean isNearExpiry(LocalDate expDate) {
        LocalDate now = LocalDate.now();
        if (expDate.isBefore(now)) {
            return false; // Already expired
        }
        long monthsBetween = ChronoUnit.MONTHS.between(now, expDate);
        return monthsBetween >= 0 && monthsBetween <= 6;
    }
    
    public static boolean isValidDateRange(LocalDate mfgDate, LocalDate expDate) {
        return !expDate.isBefore(mfgDate);
    }
    
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return String.format("%02d/%02d/%04d", date.getDayOfMonth(), 
                           date.getMonthValue(), date.getYear());
    }
}
