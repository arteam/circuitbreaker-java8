package io.github.robwin.metrics;

import com.codahale.metrics.Timer;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Metrics {

    static <T> Supplier<T> timedSupplier(Supplier<T> supplier, Timer timer){
        return () -> {
            Timer.Context context = timer.time();
            try {
                return supplier.get();
            } finally{
                context.stop();
            }
        };
    }

    static Runnable timedRunnable(Runnable runnable, Timer timer){
        return () -> {
            Timer.Context context = timer.time();
            try{
                runnable.run();
            } finally{
                context.stop();
            }
        };
    }

    static <T, R> Function<T, R> timedFunction(Function<T, R> function, Timer timer){
        return (T t) -> {
            Timer.Context context = timer.time();
            try{
                return function.apply(t);
            } finally{
                context.stop();
            }
        };
    }
}
