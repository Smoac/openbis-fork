package ch.ethz.sis.microservices.download.api.configuration;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Config
{
    private int port;

    private ServiceConfig[] services;
}
