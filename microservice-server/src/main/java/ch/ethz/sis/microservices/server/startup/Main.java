package ch.ethz.sis.microservices.server.startup;

import java.io.File;
import java.io.FileInputStream;

import ch.ethz.sis.microservices.api.configuration.Config;
import ch.ethz.sis.microservices.server.json.jackson.JacksonObjectMapper;
import ch.ethz.sis.microservices.server.logging.LogManager;
import ch.ethz.sis.microservices.server.logging.Logger;
import ch.ethz.sis.microservices.server.logging.log4j.Log4J2LogFactory;

public class Main
{
    static
    {
        // Configuring Logging
        LogManager.setLogFactory(new Log4J2LogFactory());
    }

    private static Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws Exception
    {
        logger.info("Current Workspace: " + (new File("").getAbsolutePath()));

        File configFile;
        if (args.length < 1)
        {
            configFile = new File("./conf/config.json");
            logger.info("No arguments given, starting with default config file: " + (configFile.getAbsolutePath()));
        } else
        {
            configFile = new File(args[0]);
        }

        Config config = JacksonObjectMapper.getInstance().readValue(new FileInputStream(configFile), Config.class);
        ServerLauncher servicesStarter = new ServerLauncher(config);
        servicesStarter.getServer().join();
    }
}
