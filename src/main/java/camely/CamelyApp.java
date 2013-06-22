package camely;


import akka.actor.*;
import akka.util.Timeout;
import camely.camelHttp.HttpConsumer;
import camely.camelHttp.HttpProducer;
import camely.camelHttp.HttpTransformer;
import com.typesafe.config.Config;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static akka.pattern.Patterns.ask;
import static camely.SpringExtension.SpringExtProvider;

public class CamelyApp {

    final Pattern msgPattern = Pattern
            .compile("(?:m|M)\\s+(.++)");
    final Pattern quitPattern = Pattern.compile("(?:q|quit)");
    final Pattern countPattern = Pattern.compile("(?:c|count)");

    public static void main(String[] args) throws Exception {
        CamelyApp camelyApp = new CamelyApp();
        camelyApp.initSpring();
    }

    private void initSpring() throws Exception {
        // create a spring context and scan the classes
        AnnotationConfigApplicationContext ctx =
                new AnnotationConfigApplicationContext();
        ctx.scan("camely");
        ctx.refresh();

        // get hold of the actor system
        ActorSystem system = ctx.getBean(ActorSystem.class);

        // use the Spring Extension to create props for the named actor bean
        ActorRef counter = system.actorOf(
                SpringExtProvider.get(system).props("CountingActor"), "counter");

        ActorRef httpTransformer = system.actorOf(
                SpringExtProvider.get(system).props("HttpTransformer"), "httpTransformer");

        final ActorRef httpProducer = system.actorOf(HttpProducer.mkProps(httpTransformer));
        final ActorRef httpConsumer = system.actorOf(HttpConsumer.mkProps(httpProducer));

        MessageReplacementService messageReplacementService = ctx.getBean(MessageReplacementService.class);
        CountingService countingService = ctx.getBean(CountingService.class);
        try {
            commandLoop(countingService, messageReplacementService);
        }
        finally {
            system.shutdown();
            system.awaitTermination();
        }

        // tell it to count three times
/*
        counter.tell(new CountingActor.Count(), null);
        counter.tell(new CountingActor.Count(), null);
        counter.tell(new CountingActor.Count(), null);
*/

        // print the result
/*
        FiniteDuration duration = FiniteDuration.create(3, TimeUnit.SECONDS);
        Future<Object> result = ask(counter, new CountingActor.Get(),
                Timeout.durationToTimeout(duration));
        try {
            System.out.println("Got back " + Await.result(result, duration));
        } catch (Exception e) {
            System.err.println("Failed getting result: " + e.getMessage());
            throw e;
        } finally {
            system.shutdown();
            system.awaitTermination();
        }
*/
    }

    protected void commandLoop(CountingService countingService, MessageReplacementService messageReplacementService) throws IOException {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(isr);
        try {
            boolean finished = false;
            while (!finished) {
                String command = reader.readLine();
                Matcher msgMatcher = msgPattern.matcher(command);
                Matcher countMatcher = countPattern.matcher(command);
                Matcher quitMatcher = quitPattern.matcher(command);

                if (msgMatcher.find()) {
                    String newMessage = msgMatcher.group(1);
                    messageReplacementService.setReplacementMessage(newMessage);
                    System.out.println("replacement message set to " + newMessage);
                } else if (countMatcher.find()) {
                    long count = countingService.getCount();
                    System.out.println("current count: " + count);
                } else if (quitMatcher.find()) {
                    finished = true;
                } else {
                    System.out.println("Unknown command! " + command
                            + ". Try:\n"
                            + "'m Akka Message' to set a new akka message\n"
                            + "'c' current count of the Counting Service\n"
                            + "'q' for quit");
                }
            }
        } finally {
            reader.close();
            isr.close();
        }
    }

}
