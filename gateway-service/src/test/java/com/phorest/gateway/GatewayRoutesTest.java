package com.phorest.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GatewayRoutesTest {

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @Test
    void shouldExposeRoutesForAllServices() {
        Mono<Set<String>> routeIds = routeDefinitionLocator.getRouteDefinitions()
                .map(RouteDefinition::getId)
                .collectList()
                .map(Set::copyOf);

        StepVerifier.create(routeIds)
                .assertNext(ids -> assertThat(ids).contains(
                        "client-service-clients",
                        "client-service-import",
                        "booking-appointments",
                        "booking-services",
                        "booking-purchases",
                        "booking-import-appointments",
                        "booking-import-services",
                        "booking-import-purchases",
                        "loyalty-service"
                ))
                .verifyComplete();
    }
}
