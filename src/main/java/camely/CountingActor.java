package camely;

import akka.actor.UntypedActor;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * An actor that can count using an injected CountingService.
 *
 * @note The scope here is prototype since we want to create a new actor
 * instance for each use of this bean.
 */
@Named("CountingActor")
@Scope("prototype")
public class CountingActor extends UntypedActor {

    public static class Count {
    }

    public static class Get {
    }

    // the service that will be automatically injected
    final CountingService countingService;

    @Inject
    public CountingActor(@Named("CountingService") CountingService countingService) {
        this.countingService = countingService;
    }

    private long count = 0;

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Count) {
            count = countingService.increment();
        } else if (message instanceof Get) {
            getSender().tell(count, getSelf());
        } else {
            unhandled(message);
        }
    }
}
