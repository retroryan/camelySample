package camely.camelHttp;

import akka.actor.*;
import akka.actor.Status;
import akka.camel.CamelMessage;
import akka.dispatch.Mapper;
import akka.util.Timeout;
import camely.CountingActor;
import camely.MessageReplacementService;
import org.springframework.context.annotation.Scope;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;

@Named("HttpTransformer")
@Scope("prototype")
public class HttpTransformer extends UntypedActor {

    // the services that will be automatically injected
    ActorRef countingActor;
    //used to get the identity of the counting object - i.e. if we wanted to look up multiple actors
    int countingActorCorrelationId = 1;

    final MessageReplacementService messageReplacementService;

    @Inject
    public HttpTransformer(@Named("MessageReplacementService") MessageReplacementService messageReplacementService) {
        this.messageReplacementService = messageReplacementService;

        ActorSelection actorSelection = context().actorSelection("/user/counter");
        actorSelection.tell(new Identify(countingActorCorrelationId), self());
    }


    public void onReceive(Object message) {
        if (message instanceof ActorIdentity) {
            ActorIdentity actorIdentity = (ActorIdentity) message;
            Integer correlationId = (Integer) actorIdentity.correlationId();
            if (correlationId == countingActorCorrelationId) {
                countingActor = actorIdentity.getRef();
            }
        } else if (message instanceof CamelMessage) {
            FiniteDuration duration = FiniteDuration.create(3, TimeUnit.SECONDS);
            Future<Object> result = ask(countingActor, new CountingActor.Get(), Timeout.durationToTimeout(duration));
            long currentCount = -1;
            try {
                currentCount = (Long) Await.result(result, duration);
            } catch (Exception e) {
                System.err.println("Failed getting result: " + e.getMessage());
            }

            CamelMessage camelMessage = (CamelMessage) message;
            final long finalCurrentCount = currentCount;

            CamelMessage replacedMessage =
                    camelMessage.mapBody(new Mapper<Object, String>() {
                        @Override
                        public String apply(Object body) {
                            String text = new String((byte[]) body);
                            String newMessage = messageReplacementService.getReplacementMessage() + "[" + finalCurrentCount + "] ";
                            return text.replaceAll("Akka ", newMessage);
                        }
                    });
            getSender().tell(replacedMessage, getSelf());

            countingActor.tell(new CountingActor.Count(), self());

        } else if (message instanceof Status.Failure) {
            getSender().tell(message, getSelf());
        } else
            unhandled(message);
    }
}
//#HttpExample