package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.util.List;

public class RSyncConfig
{
    private final List<String> additionalCommandLineOptions;

    private static RSyncConfig instance = new RSyncConfig();

    private RSyncConfig()
    {
        this.additionalCommandLineOptions = null;
    }

    private RSyncConfig(List<String> additionalCommandLineOptions)
    {
        this.additionalCommandLineOptions = additionalCommandLineOptions;
    }

    public List<String> getAdditionalCommandLineOptions()
    {
        return additionalCommandLineOptions;
    }

    public synchronized static RSyncConfig getInstance(List<String> additionalCommandLineOptions)
    {
        return instance = new RSyncConfig(additionalCommandLineOptions);
    }

    public synchronized static RSyncConfig getInstance()
    {
        return instance;
    }
}
