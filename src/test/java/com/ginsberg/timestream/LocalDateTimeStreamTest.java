/*
 * MIT License
 *
 * Copyright (c) 2016 Todd Ginsberg
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.ginsberg.timestream;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalDateTimeStreamTest {

    final LocalDateTime now = LocalDateTime.now();

    @Test
    public void stopsBeforeUntilDateGivenByChronoUnits() {
        final Stream<LocalDateTime> stream = LocalDateTimeStream
                .from(now)
                .until(2, ChronoUnit.SECONDS)
                .stream();
        assertThat(stream)
                .isNotNull()
                .containsExactly(now, now.plusSeconds(1));
    }

    @Test
    public void stopsBeforeUntilDateGivenByLocalDateTime() {
        final Stream<LocalDateTime> stream = LocalDateTimeStream
                .from(now)
                .until(now.plusSeconds(2))
                .stream();
        assertThat(stream)
                .isNotNull()
                .containsExactly(now, now.plusSeconds(1));
    }

    @Test
    public void stopsOnToDateGivenByChronoUnits() {
        final Stream<LocalDateTime> stream = LocalDateTimeStream
                .from(now)
                .to(2, ChronoUnit.SECONDS)
                .stream();
        assertThat(stream)
                .isNotNull()
                .containsExactly(now, now.plusSeconds(1), now.plusSeconds(2));
    }

    @Test
    public void stopsOnToDateGivenByLocalDateTime() {
        final Stream<LocalDateTime> stream = LocalDateTimeStream
                .from(now)
                .to(now.plusSeconds(2))
                .stream();
        assertThat(stream)
                .isNotNull()
                .containsExactly(now, now.plusSeconds(1), now.plusSeconds(2));
    }

    @Test
    public void stopsBeforeToWhenEveryIsAfterEndDate() {
        final Stream<LocalDateTime> stream = LocalDateTimeStream
                .from(now)
                .to(3, ChronoUnit.SECONDS)
                .every(2, ChronoUnit.SECONDS)
                .stream();
        assertThat(stream)
                .isNotNull()
                .containsExactly(now, now.plusSeconds(2));
    }

    @Test
    public void identicalFromAndToCreateOnePointStream() {
        final Stream<LocalDateTime> stream = LocalDateTimeStream
                .from(now)
                .to(now)
                .stream();
        assertThat(stream)
                .isNotNull()
                .containsExactly(now);
    }

    @Test
    public void noToDateRunsForever() {
        // No real way to test that a stream never ends so we will just make sure that this generates a lot of iterations.
        final int iterations = 1_000_000;
        final Stream<LocalDateTime> stream = LocalDateTimeStream
                .from(now)
                .stream()
                .limit(iterations);
        assertThat(stream)
                .isNotNull()
                .endsWith(now.plus(iterations - 1, ChronoUnit.SECONDS))
                .hasSize(iterations);
    }

    @Test
    public void toBeforeFromRunsBackThroughTime() {
        final Stream<LocalDateTime> stream = LocalDateTimeStream
                .from(now)
                .to(-2, ChronoUnit.SECONDS)
                .stream();
        assertThat(stream)
                .isNotNull()
                .containsExactly(now, now.minusSeconds(1), now.minusSeconds(2));
    }

    @Test
    public void stopsBeforeToWhenEveryDurationIsAfterEndDate() {
        final Duration duration = Duration.parse("PT2S");
        final Stream<LocalDateTime> stream = LocalDateTimeStream
                .from(now)
                .to(3, ChronoUnit.SECONDS)
                .every(duration)
                .stream();
        assertThat(stream)
                .isNotNull()
                .containsExactly(now, now.plusSeconds(2));
    }

    @Test
    public void positiveEveryUnitStillGoesBackward() {
        final Stream<LocalDateTime> stream = LocalDateTimeStream
                .from(now)
                .to(-3, ChronoUnit.SECONDS)
                .every(2, ChronoUnit.SECONDS)
                .stream();
        assertThat(stream)
                .isNotNull()
                .containsExactly(now, now.minusSeconds(2));
    }

    @Test
    public void positiveEveryDurationStillGoesBackward() {
        final Stream<LocalDateTime> stream = LocalDateTimeStream
                .from(now)
                .to(-3, ChronoUnit.SECONDS)
                .every(Duration.parse("PT2S"))
                .stream();
        assertThat(stream)
                .isNotNull()
                .containsExactly(now, now.minusSeconds(2));
    }

    @Test
    public void negativeEveryUnitStillGoesForward() {
        final Stream<LocalDateTime> stream = LocalDateTimeStream
                .from(now)
                .to(3, ChronoUnit.SECONDS)
                .every(-2, ChronoUnit.SECONDS)
                .stream();
        assertThat(stream)
                .isNotNull()
                .containsExactly(now, now.plusSeconds(2));
    }

    @Test
    public void negativeEveryDurationStillGoesForward() {
        final Stream<LocalDateTime> stream = LocalDateTimeStream
                .from(now)
                .to(3, ChronoUnit.SECONDS)
                .every(Duration.parse("-PT2S"))
                .stream();
        assertThat(stream)
                .isNotNull()
                .containsExactly(now, now.plusSeconds(2));
    }

    @Test(expected = NullPointerException.class)
    public void mustHaveFromDate() {
        LocalDateTimeStream.from(null);
    }

    @Test(expected = NullPointerException.class)
    public void toByUnitsMustHaveUnit() {
        LocalDateTimeStream.fromNow().to(1, null);
    }

    @Test(expected = NullPointerException.class)
    public void untilByUnitsMustHaveUnit() {
        LocalDateTimeStream.fromNow().until(1, null);
    }

    @Test(expected = NullPointerException.class)
    public void everyMustHaveDuration() {
        LocalDateTimeStream.fromNow().every(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void everyMustHaveNonZeroAmount() {
        LocalDateTimeStream.fromNow().every(0, ChronoUnit.SECONDS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void everyMustHaveNonZeroAmountFromPeriod() {
        LocalDateTimeStream.fromNow().every(Duration.parse("PT0S"));
    }
}