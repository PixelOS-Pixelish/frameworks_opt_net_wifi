/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.wifi.coex;

import static android.net.wifi.WifiScanner.WIFI_BAND_24_GHZ;
import static android.net.wifi.WifiScanner.WIFI_BAND_5_GHZ;

import static com.android.server.wifi.coex.CoexUtils.INVALID_FREQ;
import static com.android.server.wifi.coex.CoexUtils.getLowerFreqKhz;
import static com.android.server.wifi.coex.CoexUtils.getNeighboringCoexUnsafeChannels;
import static com.android.server.wifi.coex.CoexUtils.getUpperFreqKhz;

import static com.google.common.truth.Truth.assertThat;

import android.net.wifi.CoexUnsafeChannel;

import androidx.test.filters.SmallTest;

import org.junit.Test;

import java.util.HashSet;

/**
 * Unit tests for {@link com.android.server.wifi.coex.CoexUtils}.
 */
@SmallTest
public class CoexUtilsTest {

    /**
     * Verifies that getNeighboringCoexUnsafeChannels returns an empty set if there is no overlap.
     */
    @Test
    public void testGetNeighboringCoexUnsafeChannels_noOverlap_returnsEmptySet() {
        // Below/Above 2.4GHz
        assertThat(getNeighboringCoexUnsafeChannels(getLowerFreqKhz(1, WIFI_BAND_24_GHZ) - 100_000,
                50_000, 50_000)).isEmpty();
        assertThat(getNeighboringCoexUnsafeChannels(getUpperFreqKhz(14, WIFI_BAND_24_GHZ) + 100_000,
                50_000, 50_000)).isEmpty();
        assertThat(getNeighboringCoexUnsafeChannels(2595_000, 50_000, 50_000)).isEmpty();

        // Below/Above 5GHz
        assertThat(getNeighboringCoexUnsafeChannels(getLowerFreqKhz(32, WIFI_BAND_5_GHZ) - 100_000,
                50_000, 50_000)).isEmpty();
        assertThat(getNeighboringCoexUnsafeChannels(getUpperFreqKhz(173, WIFI_BAND_5_GHZ) + 100_000,
                50_000, 50_000)).isEmpty();
    }

    /**
     * Verifies that getNeighboringCoexUnsafeChannels returns the correct subset of 2.4GHz channels
     * from interference above and below the band.
     */
    @Test
    public void testGetNeighboringCoexUnsafeChannels_2g_returnsCorrectOverlap() {
        // Test channel 7 from below
        HashSet<CoexUnsafeChannel> lowerCoexUnsafeChannels = new HashSet<>();
        for (int i = 1; i <= 7; i += 1) {
            lowerCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_24_GHZ, i));
        }
        assertThat(getNeighboringCoexUnsafeChannels(2401_000,
                0, 2431_000 - 2401_000 + 1))
                .containsExactlyElementsIn(lowerCoexUnsafeChannels);

        // Test channel 7 from above
        HashSet<CoexUnsafeChannel> upperCoexUnsafeChannels = new HashSet<>();
        for (int i = 7; i <= 14; i += 1) {
            upperCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_24_GHZ, i));
        }
        assertThat(getNeighboringCoexUnsafeChannels(2495_000,
                0, 2495_000 - 2453_000 + 1))
                .containsExactlyElementsIn(upperCoexUnsafeChannels);
    }

    /**
     * Verifies that getNeighboringCoexUnsafeChannels returns the correct subset of 5GHz channels
     * from interference above and below the band.
     */
    @Test
    public void testGetNeighboringCoexUnsafeChannels_5g_returnsCorrectOverlap() {
        // Test channel 100 from below
        HashSet<CoexUnsafeChannel> lowerCoexUnsafeChannels = new HashSet<>();
        for (int i = 32; i <= 64; i += 2) {
            lowerCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, i));
        }
        lowerCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, 68));
        lowerCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, 96));
        lowerCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, 100));
        // Verify that parent channels above channel 100 are included
        lowerCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, 102));
        lowerCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, 106));
        lowerCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, 114));

        assertThat(getNeighboringCoexUnsafeChannels(5150_000,
                0, 5490_000 - 5150_000 + 1))
                .containsExactlyElementsIn(lowerCoexUnsafeChannels);

        // Test channel 64 from above
        HashSet<CoexUnsafeChannel> upperCoexUnsafeChannels = new HashSet<>();
        upperCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, 64));
        upperCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, 68));
        upperCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, 96));
        for (int i = 100; i <= 128; i += 2) {
            upperCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, i));
        }
        for (int i = 132; i <= 144; i += 2) {
            upperCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, i));
        }
        for (int i = 149; i <= 161; i += 2) {
            upperCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, i));
        }
        upperCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, 165));
        upperCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, 169));
        upperCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, 173));
        // Verify that parent channels below channel 64 are included
        upperCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, 50));
        upperCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, 58));
        upperCoexUnsafeChannels.add(new CoexUnsafeChannel(WIFI_BAND_5_GHZ, 62));

        assertThat(getNeighboringCoexUnsafeChannels(5875_000,
                0, 5875_000 - 5330_000 + 1))
                .containsExactlyElementsIn(upperCoexUnsafeChannels);
    }

    /**
     * Verifies that getLowerFreqKhz() returns the correct values for an example set of inputs.
     */
    @Test
    public void testGetLowerFreqKhz_returnsCorrectValues() {
        assertThat(getLowerFreqKhz(1, WIFI_BAND_24_GHZ)).isEqualTo(2401_000);
        assertThat(getLowerFreqKhz(4, WIFI_BAND_24_GHZ)).isEqualTo(2416_000);
        assertThat(getLowerFreqKhz(6, WIFI_BAND_24_GHZ)).isEqualTo(2426_000);
        assertThat(getLowerFreqKhz(9, WIFI_BAND_24_GHZ)).isEqualTo(2441_000);
        assertThat(getLowerFreqKhz(0, WIFI_BAND_24_GHZ)).isEqualTo(INVALID_FREQ);
        assertThat(getLowerFreqKhz(14, WIFI_BAND_24_GHZ)).isEqualTo(2473_000);
        assertThat(getLowerFreqKhz(32, WIFI_BAND_5_GHZ)).isEqualTo(5150_000);
        assertThat(getLowerFreqKhz(50, WIFI_BAND_5_GHZ)).isEqualTo(5170_000);
        assertThat(getLowerFreqKhz(64, WIFI_BAND_5_GHZ)).isEqualTo(5310_000);
        assertThat(getLowerFreqKhz(96, WIFI_BAND_5_GHZ)).isEqualTo(5470_000);
        assertThat(getLowerFreqKhz(120, WIFI_BAND_5_GHZ)).isEqualTo(5590_000);
        assertThat(getLowerFreqKhz(0, WIFI_BAND_5_GHZ)).isEqualTo(INVALID_FREQ);
    }

    /**
     * Verifies that getUpperFreqKhz() returns the correct values for an example set of inputs.
     */
    @Test
    public void testGetUpperFreqKhz_returnsCorrectValues() {
        assertThat(getUpperFreqKhz(1, WIFI_BAND_24_GHZ)).isEqualTo(2423_000);
        assertThat(getUpperFreqKhz(4, WIFI_BAND_24_GHZ)).isEqualTo(2438_000);
        assertThat(getUpperFreqKhz(6, WIFI_BAND_24_GHZ)).isEqualTo(2448_000);
        assertThat(getUpperFreqKhz(9, WIFI_BAND_24_GHZ)).isEqualTo(2463_000);
        assertThat(getUpperFreqKhz(14, WIFI_BAND_24_GHZ)).isEqualTo(2495_000);
        assertThat(getUpperFreqKhz(32, WIFI_BAND_5_GHZ)).isEqualTo(5170_000);
        assertThat(getUpperFreqKhz(50, WIFI_BAND_5_GHZ)).isEqualTo(5330_000);
        assertThat(getUpperFreqKhz(64, WIFI_BAND_5_GHZ)).isEqualTo(5330_000);
        assertThat(getUpperFreqKhz(96, WIFI_BAND_5_GHZ)).isEqualTo(5490_000);
        assertThat(getUpperFreqKhz(120, WIFI_BAND_5_GHZ)).isEqualTo(5610_000);
    }
}
