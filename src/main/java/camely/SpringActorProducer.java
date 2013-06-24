package camely;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;
import org.springframework.context.ApplicationContext;

/**
 * An actor producer that lets Spring create the Actor instances.
 * <p/>
 * Copied from Björn Antonsson @bantonsson
 */
public class SpringActorProducer implements IndirectActorProducer {
    final ApplicationContext applicationContext;
    final String actorBeanName;
    final Object[] arguments;

    public SpringActorProducer(ApplicationContext applicationContext,
                               String actorBeanName, Object... arguments) {
        this.applicationContext = applicationContext;
        this.actorBeanName = actorBeanName;
        this.arguments = arguments;
    }

    @Override
    public Actor produce() {
        Actor bean;
        if (arguments.length > 0) {
            bean = (Actor) applicationContext.getBean(actorBeanName, arguments);
        } else {
            bean = (Actor) applicationContext.getBean(actorBeanName);
        }

        return bean;
    }

    @Override
    public Class<? extends Actor> actorClass() {
        return (Class<? extends Actor>) applicationContext.getType(actorBeanName);
    }
}
