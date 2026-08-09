package io.github.prakash.kqe.operator.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "kqe.api")
public class KqeApiProperties {

    private String basePath = "";
    private String queryPath = "/query";
    private String messagesPath = "/messages";
    private String indexStatusPath = "/index/status";
    private String indexPath = "/index";
}
