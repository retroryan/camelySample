package camely.camelHttp;

import akka.actor.Status;
import akka.actor.UntypedActor;
import akka.camel.CamelMessage;
import akka.dispatch.Mapper;
import camely.CountingService;
import camely.MessageReplacementService;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;
import javax.inject.Named;

@Named("HttpTransformer")
@Scope("prototype")
public class HttpTransformer extends UntypedActor {


    // the services that will be automatically injected
    final CountingService countingService;
    final MessageReplacementService messageReplacementService;

    @Inject
    public HttpTransformer(@Named("CountingService") CountingService countingService,
                           @Named("MessageReplacementService")MessageReplacementService messageReplacementService) {
        this.countingService = countingService;
        this.messageReplacementService = messageReplacementService;
    }


    public void onReceive(Object message) {
        if (message instanceof CamelMessage) {

            CamelMessage camelMessage = (CamelMessage) message;
            CamelMessage replacedMessage =
                    camelMessage.mapBody(new Mapper<Object, String>() {
                        @Override
                        public String apply(Object body) {
                            String text = new String((byte[]) body);
                            String newMessage = messageReplacementService.getReplacementMessage() + "[" + countingService.getCount() + "] ";
                            return text.replaceAll("Akka ", newMessage);
                        }
                    });
            getSender().tell(replacedMessage, getSelf());

            countingService.increment();

        } else if (message instanceof Status.Failure) {
            getSender().tell(message, getSelf());
        } else
            unhandled(message);
    }
}
//#HttpExample