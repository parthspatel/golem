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

package cloud.golem.types;

import java.util.Objects;
import java.util.Optional;

/**
 * Configuration for retry behavior on failures.
 */
public final class RetryPolicy {

    private final int maxAttempts;
    private final long minDelayMs;
    private final long maxDelayMs;
    private final double multiplier;
    private final Double maxJitterFactor;

    private RetryPolicy(int maxAttempts, long minDelayMs, long maxDelayMs,
                        double multiplier, Double maxJitterFactor) {
        this.maxAttempts = maxAttempts;
        this.minDelayMs = minDelayMs;
        this.maxDelayMs = maxDelayMs;
        this.multiplier = multiplier;
        this.maxJitterFactor = maxJitterFactor;
    }

    /**
     * Creates a default retry policy.
     */
    public static RetryPolicy defaults() {
        return new RetryPolicy(3, 1000, 60000, 2.0, null);
    }

    /**
     * Creates a policy that doesn't retry.
     */
    public static RetryPolicy noRetry() {
        return new RetryPolicy(1, 0, 0, 1.0, null);
    }

    /**
     * Creates an exponential backoff retry policy.
     */
    public static RetryPolicy exponentialBackoff(int maxAttempts, long minDelayMs, long maxDelayMs) {
        return new RetryPolicy(maxAttempts, minDelayMs, maxDelayMs, 2.0, null);
    }

    /**
     * Creates a builder for custom retry policies.
     */
    public static Builder builder() {
        return new Builder();
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getMinDelayMs() {
        return minDelayMs;
    }

    public long getMaxDelayMs() {
        return maxDelayMs;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public Optional<Double> getMaxJitterFactor() {
        return Optional.ofNullable(maxJitterFactor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RetryPolicy that = (RetryPolicy) o;
        return maxAttempts == that.maxAttempts &&
               minDelayMs == that.minDelayMs &&
               maxDelayMs == that.maxDelayMs &&
               Double.compare(that.multiplier, multiplier) == 0 &&
               Objects.equals(maxJitterFactor, that.maxJitterFactor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxAttempts, minDelayMs, maxDelayMs, multiplier, maxJitterFactor);
    }

    @Override
    public String toString() {
        return "RetryPolicy{" +
               "maxAttempts=" + maxAttempts +
               ", minDelayMs=" + minDelayMs +
               ", maxDelayMs=" + maxDelayMs +
               ", multiplier=" + multiplier +
               ", maxJitterFactor=" + maxJitterFactor +
               '}';
    }

    /**
     * Builder for creating custom retry policies.
     */
    public static final class Builder {
        private int maxAttempts = 3;
        private long minDelayMs = 1000;
        private long maxDelayMs = 60000;
        private double multiplier = 2.0;
        private Double maxJitterFactor = null;

        public Builder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder minDelay(long ms) {
            this.minDelayMs = ms;
            return this;
        }

        public Builder maxDelay(long ms) {
            this.maxDelayMs = ms;
            return this;
        }

        public Builder multiplier(double multiplier) {
            this.multiplier = multiplier;
            return this;
        }

        public Builder maxJitterFactor(double factor) {
            this.maxJitterFactor = factor;
            return this;
        }

        public RetryPolicy build() {
            return new RetryPolicy(maxAttempts, minDelayMs, maxDelayMs, multiplier, maxJitterFactor);
        }
    }
}
