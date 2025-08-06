package com.example.demo.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for DateUtil class.
 */
@SpringBootTest
class DateUtilTest {

    @Test
    void testFormatDate() {
        // Null handling
        assertThat(DateUtil.formatDate(null)).isNull();
        
        // Valid date formatting
        LocalDate date = LocalDate.of(2023, 12, 25);
        assertThat(DateUtil.formatDate(date)).isEqualTo("2023-12-25");
    }

    @Test
    void testFormatDateTime() {
        // Null handling
        assertThat(DateUtil.formatDateTime(null)).isNull();
        
        // Valid datetime formatting
        LocalDateTime dateTime = LocalDateTime.of(2023, 12, 25, 14, 30, 45);
        assertThat(DateUtil.formatDateTime(dateTime)).isEqualTo("2023-12-25 14:30:45");
    }

    @Test
    void testFormatDateWithCustomPattern() {
        // Null handling
        assertThat(DateUtil.formatDate(null, "dd/MM/yyyy")).isNull();
        assertThat(DateUtil.formatDate(LocalDate.of(2023, 12, 25), null)).isNull();
        
        // Custom pattern formatting
        LocalDate date = LocalDate.of(2023, 12, 25);
        assertThat(DateUtil.formatDate(date, "dd/MM/yyyy")).isEqualTo("25/12/2023");
        assertThat(DateUtil.formatDate(date, "MMM dd, yyyy")).isEqualTo("Dec 25, 2023");
    }

    @Test
    void testFormatDateTimeWithCustomPattern() {
        // Null handling
        assertThat(DateUtil.formatDateTime(null, "dd/MM/yyyy HH:mm")).isNull();
        assertThat(DateUtil.formatDateTime(LocalDateTime.of(2023, 12, 25, 14, 30), null)).isNull();
        
        // Custom pattern formatting
        LocalDateTime dateTime = LocalDateTime.of(2023, 12, 25, 14, 30, 45);
        assertThat(DateUtil.formatDateTime(dateTime, "dd/MM/yyyy HH:mm")).isEqualTo("25/12/2023 14:30");
        assertThat(DateUtil.formatDateTime(dateTime, "yyyy-MM-dd'T'HH:mm:ss")).isEqualTo("2023-12-25T14:30:45");
    }

    @Test
    void testParseDate() {
        // Null and empty handling
        assertThat(DateUtil.parseDate(null)).isNull();
        assertThat(DateUtil.parseDate("")).isNull();
        assertThat(DateUtil.parseDate("   ")).isNull();
        
        // Valid date parsing
        assertThat(DateUtil.parseDate("2023-12-25")).isEqualTo(LocalDate.of(2023, 12, 25));
        
        // Invalid date format
        assertThatThrownBy(() -> DateUtil.parseDate("25/12/2023"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid date format");
        
        assertThatThrownBy(() -> DateUtil.parseDate("invalid-date"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid date format");
    }

    @Test
    void testParseDateTime() {
        // Null and empty handling
        assertThat(DateUtil.parseDateTime(null)).isNull();
        assertThat(DateUtil.parseDateTime("")).isNull();
        assertThat(DateUtil.parseDateTime("   ")).isNull();
        
        // Valid datetime parsing
        assertThat(DateUtil.parseDateTime("2023-12-25 14:30:45"))
                .isEqualTo(LocalDateTime.of(2023, 12, 25, 14, 30, 45));
        
        // Invalid datetime format
        assertThatThrownBy(() -> DateUtil.parseDateTime("2023-12-25T14:30:45"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid datetime format");
        
        assertThatThrownBy(() -> DateUtil.parseDateTime("invalid-datetime"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid datetime format");
    }

    @Test
    void testDaysBetween() {
        // Null handling
        assertThat(DateUtil.daysBetween(null, LocalDate.now())).isEqualTo(0);
        assertThat(DateUtil.daysBetween(LocalDate.now(), null)).isEqualTo(0);
        assertThat(DateUtil.daysBetween(null, null)).isEqualTo(0);
        
        // Valid calculations
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 11);
        assertThat(DateUtil.daysBetween(startDate, endDate)).isEqualTo(10);
        
        // Negative days (end before start)
        assertThat(DateUtil.daysBetween(endDate, startDate)).isEqualTo(-10);
        
        // Same date
        assertThat(DateUtil.daysBetween(startDate, startDate)).isEqualTo(0);
    }

    @Test
    void testYearsBetween() {
        // Null handling
        assertThat(DateUtil.yearsBetween(null, LocalDate.now())).isEqualTo(0);
        assertThat(DateUtil.yearsBetween(LocalDate.now(), null)).isEqualTo(0);
        assertThat(DateUtil.yearsBetween(null, null)).isEqualTo(0);
        
        // Valid calculations
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 1);
        assertThat(DateUtil.yearsBetween(startDate, endDate)).isEqualTo(3);
        
        // Partial year
        LocalDate partialEndDate = LocalDate.of(2022, 6, 1);
        assertThat(DateUtil.yearsBetween(startDate, partialEndDate)).isEqualTo(2);
        
        // Same year
        assertThat(DateUtil.yearsBetween(startDate, startDate)).isEqualTo(0);
    }

    @Test
    void testIsPast() {
        // Null handling
        assertThat(DateUtil.isPast(null)).isFalse();
        
        // Past date
        LocalDate pastDate = LocalDate.now().minusDays(1);
        assertThat(DateUtil.isPast(pastDate)).isTrue();
        
        // Today (not past)
        assertThat(DateUtil.isPast(LocalDate.now())).isFalse();
        
        // Future date
        LocalDate futureDate = LocalDate.now().plusDays(1);
        assertThat(DateUtil.isPast(futureDate)).isFalse();
    }

    @Test
    void testIsFuture() {
        // Null handling
        assertThat(DateUtil.isFuture(null)).isFalse();
        
        // Future date
        LocalDate futureDate = LocalDate.now().plusDays(1);
        assertThat(DateUtil.isFuture(futureDate)).isTrue();
        
        // Today (not future)
        assertThat(DateUtil.isFuture(LocalDate.now())).isFalse();
        
        // Past date
        LocalDate pastDate = LocalDate.now().minusDays(1);
        assertThat(DateUtil.isFuture(pastDate)).isFalse();
    }

    @Test
    void testGetCurrentDateString() {
        String currentDateString = DateUtil.getCurrentDateString();
        assertThat(currentDateString).isNotNull();
        assertThat(currentDateString).matches("\\d{4}-\\d{2}-\\d{2}");
        
        // Should be today's date
        String expectedDate = DateUtil.formatDate(LocalDate.now());
        assertThat(currentDateString).isEqualTo(expectedDate);
    }

    @Test
    void testGetCurrentDateTimeString() {
        String currentDateTimeString = DateUtil.getCurrentDateTimeString();
        assertThat(currentDateTimeString).isNotNull();
        assertThat(currentDateTimeString).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    }

    @Test
    void testConstants() {
        assertThat(DateUtil.DEFAULT_DATE_FORMAT).isEqualTo("yyyy-MM-dd");
        assertThat(DateUtil.DEFAULT_DATETIME_FORMAT).isEqualTo("yyyy-MM-dd HH:mm:ss");
        assertThat(DateUtil.ISO_DATE_FORMAT).isEqualTo("yyyy-MM-dd");
        assertThat(DateUtil.ISO_DATETIME_FORMAT).isEqualTo("yyyy-MM-dd'T'HH:mm:ss");
    }
}