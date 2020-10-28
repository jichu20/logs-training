package com.jichu20.loggerlib.configuration;

import com.jichu20.loggerlib.util.Constant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import brave.Tracing;
import brave.propagation.B3Propagation;
import brave.propagation.ExtraFieldPropagation;

@Configuration
public class configuration {

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    Tracing tracing() {

        return Tracing.newBuilder()
                .propagationFactory(ExtraFieldPropagation.newFactoryBuilder(B3Propagation.FACTORY).addField(Constant.X_TRACE_ID).build()).build();

    }
}
