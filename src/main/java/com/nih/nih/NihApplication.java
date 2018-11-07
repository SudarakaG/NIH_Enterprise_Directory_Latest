package com.nih.nih;

import com.google.common.collect.Table;
import com.nih.nih.config.BrowseNIH;
import jdk.management.resource.internal.SimpleResourceContext;
import jdk.nashorn.internal.runtime.Context;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class NihApplication {

    public static void main(String[] args) {
        SpringApplication.run(NihApplication.class, args);
    }

}
