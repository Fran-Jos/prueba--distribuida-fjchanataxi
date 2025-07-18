package com.programacion.distribuida.authors.servicios;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.vertx.ext.consul.CheckOptions;
import io.vertx.ext.consul.ConsulClientOptions;
import io.vertx.ext.consul.ServiceOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.consul.ConsulClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AuthorsLifecycle {

    @Inject
    @ConfigProperty(name = "consul.host", defaultValue = "127.0.0.1")
    String consulHost;

    @Inject
    @ConfigProperty(name = "consul.port", defaultValue = "8500")
    Integer consulPort;

    @Inject
    @ConfigProperty(name = "quarkus.http.port")
    Integer appPort;

    String serviceId;

    void init(@Observes StartupEvent event, Vertx vertx) throws Exception {
        System.out.println("Starting Authors service...");


        try {
            ConsulClientOptions options = new ConsulClientOptions()
                    .setHost(consulHost)
                    .setPort(consulPort);

            ConsulClient consulClient = ConsulClient.create(vertx, options);

            serviceId = UUID.randomUUID().toString();
            var ipAddress = InetAddress.getLocalHost();

            //--registro
            var tags = List.of(
                    "traefik.enable=true",
                    //PathPrefix
                    "traefik.http.routers.app-authors.rule=PathPrefix(`/authors`)",
                    "traefik.http.routers.app-authors.middlewares=strip-prefix-authors",
                    "traefik.http.middlewares.strip-prefix-authors.stripPrefix.prefixes=/app-authors"
            );

            var CheckOptions = new CheckOptions()
                    //.setHttp("http://127.0.0.1:8080/ping")
                    .setHttp(String.format("http://%s:%d/q/health/live", ipAddress.getHostAddress(), appPort))
                    .setInterval("10s")
                    .setDeregisterAfter("20s");


            ServiceOptions serviceOptions = new ServiceOptions()
                    .setName("app-authors")
                    .setId(serviceId)
                    .setAddress(ipAddress.getHostAddress())
                    .setPort(appPort)
                    .setTags(tags)
                    .setCheckOptions(CheckOptions);


            consulClient.registerServiceAndAwait(serviceOptions);
        }catch (Exception e) {
            System.err.println("Error during Authors service initialization: " + e.getMessage());
        }finally {
            System.out.println("Authors service initialization completed.");
        }

    }

    void stop(@Observes ShutdownEvent event, Vertx vertx) {
        System.out.println("Stopping Authors service...");

        ConsulClientOptions options = new ConsulClientOptions()
                .setHost(consulHost)
                .setPort(consulPort);
        ConsulClient consulClient = ConsulClient.create(vertx, options);

        consulClient.deregisterServiceAndAwait(serviceId);

    }
}