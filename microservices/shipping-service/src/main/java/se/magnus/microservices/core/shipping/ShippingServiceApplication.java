package se.magnus.microservices.core.shipping;

import brave.baggage.BaggagePropagation;
import brave.baggage.BaggagePropagationCustomizer;
import brave.propagation.B3Propagation;
import brave.propagation.Propagation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class ShippingServiceApplication {

    private static final org.slf4j.Logger LOG = 
            org.slf4j.LoggerFactory.getLogger(ShippingServiceApplication.class);

    @Bean
    BaggagePropagation.FactoryBuilder myPropagationFactoryBuilder(
            ObjectProvider<BaggagePropagationCustomizer> baggagePropagationCustomizers) {
        Propagation.Factory delegate = B3Propagation.newFactoryBuilder().injectFormat(B3Propagation.Format.MULTI).build();
        BaggagePropagation.FactoryBuilder builder = BaggagePropagation.newFactoryBuilder(delegate);
        baggagePropagationCustomizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
        return builder;
    }

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(ShippingServiceApplication.class, args);
    }
}
