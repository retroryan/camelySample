package camely;

import javax.inject.Named;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A simple service that keeps a counter.
 */
@Named("CountingService")
public class CountingService {

    private final AtomicLong counter = new AtomicLong(0);

    public long increment() {
        return counter.incrementAndGet();
    }

    public long getCount() {
        return counter.get();
    }
}
