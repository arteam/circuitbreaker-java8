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
package io.github.robwin.retry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;

import javax.xml.ws.WebServiceException;
import java.util.function.Supplier;

import static org.assertj.core.api.BDDAssertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class SupplierRetryTest {

    private HelloWorldService helloWorldService;

    @Before
    public void setUp(){
        helloWorldService = mock(HelloWorldService.class);
    }

    @Test
    public void shouldReturnSuccessfullyAfterSecondAttempt() {
        // Given the HelloWorldService throws an exception
        given(helloWorldService.returnHelloWorld()).willThrow(new WebServiceException("BAM!")).willReturn("Hello world");

        // Create a Retry with default configuration
        Retry retryContext = Retry.ofDefaults();
        // Decorate the invocation of the HelloWorldService
        Supplier<String> retryableSupplier = Retry.retryableSupplier(helloWorldService::returnHelloWorld, retryContext);

        // When
        String result = retryableSupplier.get();

        // Then the helloWorldService should be invoked 2 times
        BDDMockito.then(helloWorldService).should(times(2)).returnHelloWorld();
        assertThat(result).isEqualTo("Hello world");
    }

    @Test
    public void shouldReturnAfterThreeAttempts() {
        // Given the HelloWorldService throws an exception
        given(helloWorldService.returnHelloWorld()).willThrow(new WebServiceException("BAM!"));

        // Create a Retry with default configuration
        Retry retryContext = Retry.ofDefaults();
        // Decorate the invocation of the HelloWorldService
        Supplier<String> retryableSupplier = Retry.retryableSupplier(helloWorldService::returnHelloWorld, retryContext);

        // When
        try {
            retryableSupplier.get();
            Assert.fail();
        }  catch (WebServiceException e){
            // Then the helloWorldService should be invoked 3 times
            BDDMockito.then(helloWorldService).should(times(3)).returnHelloWorld();
        }
    }

    @Test
    public void shouldReturnAfterOneAttempt() {
        // Given the HelloWorldService throws an exception
        given(helloWorldService.returnHelloWorld()).willThrow(new WebServiceException("BAM!"));

        // Create a Retry with default configuration
        Retry retryContext = Retry.custom().maxAttempts(1).build();
        // Decorate the invocation of the HelloWorldService
        Supplier<String> retryableSupplier = Retry.retryableSupplier(helloWorldService::returnHelloWorld, retryContext);

        // When
        try {
            retryableSupplier.get();
            Assert.fail();
        }  catch (WebServiceException e){
            // Then the helloWorldService should be invoked 1 time
            BDDMockito.then(helloWorldService).should(times(1)).returnHelloWorld();
        }
    }

    @Test
    public void shouldReturnAfterOneAttemptAndIgnoreException() {
        // Given the HelloWorldService throws an exception
        given(helloWorldService.returnHelloWorld()).willThrow(new WebServiceException("BAM!"));

        // Create a Retry with default configuration
        Retry retryContext = Retry.custom().ignoredException(WebServiceException.class).build();
        // Decorate the invocation of the HelloWorldService
        Supplier<String> retryableSupplier = Retry.retryableSupplier(helloWorldService::returnHelloWorld, retryContext);

        // When
        try {
            retryableSupplier.get();
            Assert.fail();
        }  catch (WebServiceException e){
            // Then the helloWorldService should be invoked 1 time
            BDDMockito.then(helloWorldService).should(times(1)).returnHelloWorld();
        }
    }
}
