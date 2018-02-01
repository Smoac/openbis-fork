package ch.ethz.sis.microservices.download.api.configuration;

import java.util.HashMap;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ServiceConfig
{
    private String className;

    private String url;

    private HashMap<String, String> parameters;
}
