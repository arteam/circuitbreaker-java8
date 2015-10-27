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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class CircuitBreakerTest {

    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Before
    public void setUp() {
        circuitBreakerRegistry = CircuitBreakerRegistry.of(new CircuitBreakerConfig.Builder()
                .maxFailures(1)
                .waitInterval(1000)
                .build());
    }

    @Test
    public void shouldReturnFailureWithCircuitBreakerOpenException() {
        // Given
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName");
        assertThat(circuitBreaker.isCallPermitted()).isTrue();
        circuitBreaker.recordFailure(new RuntimeException());
        assertThat(circuitBreaker.isCallPermitted()).isTrue();
        circuitBreaker.recordFailure(new RuntimeException());
        assertThat(circuitBreaker.isCallPermitted()).isFalse();

        //When
        try {
            CircuitBreaker.decorateRunnable(() -> {
                throw new RuntimeException("BAM!");
            }, circuitBreaker).run();
        } catch (CircuitBreakerOpenException e) {
            //Then
            assertThat(circuitBreaker.isCallPermitted()).isFalse();
        }
    }

    @Test
    public void shouldReturnFailureWithRuntimeException() {
        // Given
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName");
        assertThat(circuitBreaker.isCallPermitted()).isTrue();

        //When
        try {
            CircuitBreaker.decorateRunnable(() -> {
                throw new RuntimeException("BAM!");
            }, circuitBreaker).run();
            Assert.fail();
        } catch (RuntimeException e) {
            //Then
            assertThat(circuitBreaker.isCallPermitted()).isTrue();
        }
    }

    @Test
    public void shouldNotTriggerCircuitBreakerOpenException() {
        // Given
        CircuitBreakerConfig circuitBreakerConfig = new CircuitBreakerConfig.Builder()
                .maxFailures(1)
                .waitInterval(1000)
                .ignoredException(IOException.class)
                .build();
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName", circuitBreakerConfig);

        circuitBreaker.recordFailure(new RuntimeException());
        // CircuitBreaker is still CLOSED, because 1 failure is allowed
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    public void shouldReturnSuccess() {
        // Given
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName");
        assertThat(circuitBreaker.isCallPermitted()).isTrue();

        //When
        Supplier<String> checkedSupplier = CircuitBreaker.decorateSupplier(() -> "Hello world", circuitBreaker);

        //Then
        assertThat(checkedSupplier.get()).isEqualTo("Hello world");
    }

    @Test
    public void testReadmeExample() {
        // Given
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();

        // First parameter is maximum number of failures allowed
        // Second parameter is the wait interval [ms] and specifies how long the CircuitBreaker should stay OPEN
        CircuitBreakerConfig circuitBreakerConfig = new CircuitBreakerConfig.Builder().maxFailures(1).waitInterval(1000).build();

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("uniqueName", circuitBreakerConfig);
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED); // CircuitBreaker is initially CLOSED
        circuitBreaker.recordFailure(new RuntimeException());
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED); // CircuitBreaker is still CLOSED, because 1 failure is allowed
        circuitBreaker.recordFailure(new RuntimeException());
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN); // CircuitBreaker is OPEN, because maxFailures > 1
    }

    @Test
    public void shouldReturnWithRecoveryAsync() throws ExecutionException, InterruptedException {
        // Given
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName");

        // When
        Supplier<String> decoratedSupplier = CircuitBreaker
                .decorateSupplier(() -> "This can be any method which returns: 'Hello", circuitBreaker);

        CompletableFuture<String> future = CompletableFuture.supplyAsync(decoratedSupplier)
                .thenApply(value -> value + " world'");

        //Then
        assertThat(future.get()).isEqualTo("This can be any method which returns: 'Hello world'");
    }


}
