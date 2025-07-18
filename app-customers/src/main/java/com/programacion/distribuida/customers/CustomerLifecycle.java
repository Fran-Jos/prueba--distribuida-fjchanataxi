package com.programacion.distribuida.customers;

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
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class CustomerLifecycle {

    private static final Logger LOGGER = Logger.getLogger(CustomerLifecycle.class.getName());

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
        LOGGER.info("Iniciando servicio customersss...");

        ConsulClientOptions options = new ConsulClientOptions()
                .setHost(consulHost)
                .setPort(consulPort);

        ConsulClient consulClient = ConsulClient.create(vertx, options);

        serviceId = UUID.randomUUID().toString();
        var ipAddress = InetAddress.getLocalHost();
        var tags = List.of(
                "traefik.enable=true",
                "traefik.http.routers.app-customers.rule=PathPrefix(`/app-customers`)",
                "traefik.http.routers.app-customers.middlewares=strip-prefix-customers",
                "traefik.http.middlewares.strip-prefix-customers.stripprefix.prefixes=/app-customers"
        );

        var checkOptions = new CheckOptions()
                .setHttp(String.format("http://%s:%s/ping", ipAddress.getHostAddress(), appPort))
                .setInterval("10s")
                .setDeregisterAfter("20s");

        ServiceOptions serviceOptions = new ServiceOptions()
                .setName("app-customers")
                .setId(serviceId)
                .setAddress(ipAddress.getHostAddress())
                .setPort(appPort)
                .setTags(tags)
                .setCheckOptions(checkOptions);

        consulClient.registerServiceAndAwait(serviceOptions);
        LOGGER.info("Servicio registrado con ID: " + serviceId);
    }

    void stop(@Observes ShutdownEvent event, Vertx vertx) {
        LOGGER.info("Deteniendo servicio customersss...");

        if (serviceId == null) {
            LOGGER.warning("El serviceId es nulo. No se puede desregistrar el servicio.");
            return;
        }

        try {
            ConsulClientOptions options = new ConsulClientOptions()
                    .setHost(consulHost)
                    .setPort(consulPort);
            ConsulClient consulClient = ConsulClient.create(vertx, options);

            consulClient.deregisterServiceAndAwait(serviceId);
            LOGGER.info("Servicio desregistrado con ID: " + serviceId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al desregistrar el servicio: " + e.getMessage(), e);
        }
    }
}