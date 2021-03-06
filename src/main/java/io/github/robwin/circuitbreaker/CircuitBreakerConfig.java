/*
 *
 *  Copyright 2015 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.robwin.circuitbreaker;

import java.util.ArrayList;
import java.util.List;

public class CircuitBreakerConfig {

    private static final int DEFAULT_MAX_FAILURES = 3;
    private static final int DEFAULT_WAIT_INTERVAL = 60000;

    // The maximum number of allowed failures
    private final int maxFailures;
    // The wait interval which specifies how long the CircuitBreaker should stay OPEN
    private final int waitInterval;
    // Exceptions which do not count as failures and thus not trigger the circuit breaker.
    private final List<Class<? extends Throwable>> ignoredExceptions;

    private CircuitBreakerConfig(int maxFailures, int waitInterval, List<Class<? extends Throwable>> ignoredExceptions){
        this.maxFailures = maxFailures;
        this.waitInterval = waitInterval;
        this.ignoredExceptions = ignoredExceptions;
    }

    public Integer getMaxFailures() {
        return maxFailures;
    }

    public Integer getWaitInterval() {
        return waitInterval;
    }

    public List<Class<? extends Throwable>> getIgnoredExceptions() {
        return ignoredExceptions;
    }

    public static CircuitBreakerConfig.Builder custom(){
        return new Builder();
    }

    public static class Builder {
        private int maxFailures = DEFAULT_MAX_FAILURES;
        private int waitInterval = DEFAULT_WAIT_INTERVAL;
        private List<Class<? extends Throwable>> ignoredExceptions = new ArrayList<>();

        public Builder maxFailures(int maxFailures) {
            if (maxFailures < 1) {
                throw new IllegalArgumentException("maxFailures must be greater than or equal to 1");
            }
            this.maxFailures = maxFailures;
            return this;
        }

        public Builder waitInterval(int waitInterval) {
            if (waitInterval < 100) {
                throw new IllegalArgumentException("waitInterval must be at least 100[ms]");
            }
            this.waitInterval = waitInterval;
            return this;
        }

        public Builder ignoredException(Class<? extends Throwable> ignoredException) {
            if (ignoredException == null) {
                throw new IllegalArgumentException("ignoredException must not be null");
            }
            ignoredExceptions.add(ignoredException);
            return this;
        }

        public Builder ignoredExceptions(List<Class<? extends Throwable>> ignoredExceptions) {
            if (ignoredExceptions == null) {
                throw new IllegalArgumentException("ignoredExceptions must not be null");
            }
            this.ignoredExceptions = ignoredExceptions;
            return this;
        }

        public CircuitBreakerConfig build() {
            return new CircuitBreakerConfig(maxFailures, waitInterval, ignoredExceptions);
        }
    }
}
