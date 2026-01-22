/*
 * Copyright 2024-2025 Golem Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.golem.sdk.host;

import cloud.golem.sdk.types.Option;

import java.time.Duration;
import java.util.Objects;

/**
 * Configuration for retry behavior on failures.
 */
public final class RetryPolicy {

    private final int maxAttempts;
    private final Duration minDelay;
    private final Duration maxDelay;
    private final double multiplier;
    private final Double maxJitterFactor;

    /**
     * Creates a new RetryPolicy.
     *
     * @param maxAttempts Maximum number of retry attempts
     * @param minDelay Minimum delay between retries
     * @param maxDelay Maximum delay between retries
     * @param multiplier Multiplier for exponential backoff
     * @param maxJitterFactor Optional jitter factor (0.0 to 1.0)
     */
    public RetryPolicy(
            int maxAttempts,
            Duration minDelay,
            Duration maxDelay,
            double multiplier,
            Double maxJitterFactor) {
        this.maxAttempts = maxAttempts;
        this.minDelay = Objects.requireNonNull(minDelay);
        this.maxDelay = Objects.requireNonNull(maxDelay);
        this.multiplier = multiplier;
        this.maxJitterFactor = maxJitterFactor;
    }

    /**
     * Create a default retry policy.
     *
     * @return A default retry policy
     */
    public static RetryPolicy defaults() {
        return new RetryPolicy(
            3,
            Duration.ofSeconds(1),
            Duration.ofSeconds(60),
            2.0,
            null
        );
    }

    /**
     * Create a policy that doesn't retry.
     *
     * @return A no-retry policy
     */
    public static RetryPolicy noRetry() {
        return new RetryPolicy(
            1,
            Duration.ZERO,
            Duration.ZERO,
            1.0,
            null
        );
    }

    /**
     * Create an exponential backoff retry policy.
     *
     * @param maxAttempts Maximum number of attempts
     * @param minDelay Minimum delay between retries
     * @param maxDelay Maximum delay between retries
     * @return An exponential backoff policy
     */
    public static RetryPolicy exponentialBackoff(
            int maxAttempts,
            Duration minDelay,
            Duration maxDelay) {
        return new RetryPolicy(maxAttempts, minDelay, maxDelay, 2.0, null);
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Duration getMinDelay() {
        return minDelay;
    }

    public Duration getMaxDelay() {
        return maxDelay;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public Option<Double> getMaxJitterFactor() {
        return maxJitterFactor != null ? Option.some(maxJitterFactor) : Option.none();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RetryPolicy that = (RetryPolicy) o;
        return maxAttempts == that.maxAttempts &&
               Double.compare(that.multiplier, multiplier) == 0 &&
               Objects.equals(minDelay, that.minDelay) &&
               Objects.equals(maxDelay, that.maxDelay) &&
               Objects.equals(maxJitterFactor, that.maxJitterFactor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxAttempts, minDelay, maxDelay, multiplier, maxJitterFactor);
    }

    @Override
    public String toString() {
        return "RetryPolicy{" +
               "maxAttempts=" + maxAttempts +
               ", minDelay=" + minDelay +
               ", maxDelay=" + maxDelay +
               ", multiplier=" + multiplier +
               ", maxJitterFactor=" + maxJitterFactor +
               '}';
    }
}
