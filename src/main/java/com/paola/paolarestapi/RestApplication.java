package com.paola.paolarestapi;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api")
public class RestApplication extends ResourceConfig {
    public RestApplication() {
        packages("com.paola.paolarestapi");
    }
}
