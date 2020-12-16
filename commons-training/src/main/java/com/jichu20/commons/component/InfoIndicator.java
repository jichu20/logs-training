package com.jichu20.commons.component;

import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.InfoContributor;

@Component
public class InfoIndicator implements InfoContributor {

    @Autowired
    BuildProperties buildProperties;

    @Value("${spring.application.name:empty}")
    private String serviceName;

    @Value("${info.deploy.region:empty}")
    private String region;

    @Override
    public void contribute(Builder builder) {

        Map<String, Object> buildInfo = new HashMap<String, Object>() {
            {
                put("commit", buildProperties.get("commit"));
                put("uuid", buildProperties.get("uuid"));
                put("date", buildProperties.getTime());
                put("version", buildProperties.getVersion());
                put("name", serviceName);
            }
        };

        Map<String, Object> info = new HashMap<String, Object>() {
            {
                put("buildInfo", buildInfo);
            }
        };

        builder.withDetail("info", info);
    }

}
