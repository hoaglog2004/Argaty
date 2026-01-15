package com.argaty.config;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatMultipartConfig {

    @Bean
    public WebServerFactoryCustomizer<WebServerFactory> tomcatMultipartCustomizer(
            @Value("${server.tomcat.max-part-count:200}") int maxPartCount) {

        return (WebServerFactory factory) -> {
            // Avoid a hard dependency on embedded Tomcat classes.
            // If the runtime isn't Tomcat, do nothing.
            try {
                Class<?> tomcatFactoryClass = Class.forName(
                        "org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory");
                if (!tomcatFactoryClass.isInstance(factory)) {
                    return;
                }

                Class<?> connectorCustomizerInterface = Class.forName(
                        "org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer");

                Object customizer = Proxy.newProxyInstance(
                        connectorCustomizerInterface.getClassLoader(),
                        new Class<?>[] { connectorCustomizerInterface },
                        (proxy, method, args) -> {
                            if ("customize".equals(method.getName()) && args != null && args.length == 1
                                    && args[0] != null) {
                                Object connector = args[0];
                                // Prefer the strongly-typed API when available, but keep this resilient
                                // across Tomcat minor versions by using reflection.
                                try {
                                    Method setMaxPartCount = connector.getClass().getMethod("setMaxPartCount", int.class);
                                    setMaxPartCount.invoke(connector, maxPartCount);
                                    return null;
                                } catch (ReflectiveOperationException ignored) {
                                    // Fall back to setting the Connector attribute by name.
                                }

                                try {
                                    Method setProperty = connector.getClass().getMethod("setProperty", String.class,
                                            String.class);
                                    setProperty.invoke(connector, "maxPartCount", String.valueOf(maxPartCount));
                                } catch (ReflectiveOperationException ignored) {
                                    // If Tomcat changes internals again, we still don't want startup to fail.
                                }
                                return null;
                            }
                            return null;
                        });

                Object customizerArray = Array.newInstance(connectorCustomizerInterface, 1);
                Array.set(customizerArray, 0, customizer);

                Method addConnectorCustomizers = tomcatFactoryClass.getMethod("addConnectorCustomizers",
                        customizerArray.getClass());
                addConnectorCustomizers.invoke(factory, customizerArray);
            } catch (ClassNotFoundException ignored) {
                // Not running with embedded Tomcat on the classpath.
            } catch (ReflectiveOperationException ignored) {
                // Don't block startup if Spring Boot/Tomcat internals differ.
            }
        };
    }
}
