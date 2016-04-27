/*
 * Copyright 2011 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.basynthec.cisd.client.examples;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.PropertyConfigurator;

import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.OpenbisServiceFacadeFactory;

/**
 * An example of communcating with openBIS. This example retrieves metadata about an experiment and downaloads data from one data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class BaSynthecClient extends AbstractBaSynthecClient
{
    private static final long CONNECTION_TIMEOUT_MILLIS = 15 * DateUtils.MILLIS_PER_SECOND;

    /**
     * The main method: logs into openBIS and runs the client.
     */
    public static void main(String[] args)
    {
        if (args.length < 2)
        {
            printUsage();
            return;
        }

        configureLogging();

        IOpenbisServiceFacade facade = createServiceFacade(args);
        if (facade == null)
        {
            System.err.println("Authentication failed: check the user name and password.");
            System.exit(1);
            return;
        }

        BaSynthecClient newMe = new BaSynthecClient(facade);

        newMe.run();

        newMe.logout();

    }

    private static IOpenbisServiceFacade createServiceFacade(String[] args)
    {
        String userId = args[0];
        String userPassword = args[1];
        String serverUrl;
        if (args.length > 2)
        {
            serverUrl = args[2];
        } else
        {
            // Default to the basynthec server
            serverUrl = "https://basynthec.ethz.ch";
        }

        logInfo(String.format("Connecting to the server '%s' as a user '%s'.", serverUrl, userId));
        IOpenbisServiceFacade facade =
                OpenbisServiceFacadeFactory.tryCreate(userId, userPassword, serverUrl,
                        CONNECTION_TIMEOUT_MILLIS);
        return facade;
    }

    private static void printUsage()
    {
        System.err.println("Usage: <user> <password> [<openbis-server-url>]");
        System.err.println("Example: test-user my-password");
        System.err.println("Example: test-user my-password https://basynthec.ethz.ch");
        System.err.println("Example: test-user my-password http://localhost:8888");
        System.exit(1);
    }

    private static void configureLogging()
    {
        Properties props = new Properties();
        props.put("log4j.appender.STDOUT", "org.apache.log4j.ConsoleAppender");
        props.put("log4j.appender.STDOUT.layout", "org.apache.log4j.PatternLayout");
        props.put("log4j.appender.STDOUT.layout.ConversionPattern", "%d %-5p [%t] %c - %m%n");
        props.put("log4j.rootLogger", "INFO, STDOUT");
        PropertyConfigurator.configure(props);
    }

    /**
     * Private constructor.
     * 
     * @param facade The facade for interacting with openBIS.
     */
    private BaSynthecClient(IOpenbisServiceFacade facade)
    {
        super(facade);
    }

    /**
     * Using the connection to openBIS, do the following:<br>
     * <ul>
     * <li>List the experiments, with their metadata, in the project <i>/PRIVATE/TEST</i></li>
     * <li>Search and print data sets that refer to a particular strain.</li>
     * </ul>
     */
    public void run()
    {
        List<String> projectIdentifiers = Arrays.asList("/PRIVATE/TEST");
        logInfo("Listing experiments in projects " + projectIdentifiers + "...");
        ExperimentLister experimentLister = new ExperimentLister(openBis);
        experimentLister.run(projectIdentifiers);

        println("\n");

        List<String> strainNames = Arrays.asList("MGP100");
        logInfo("Listing data sets containing data for strains " + strainNames + "...");
        File downloadFolder = new File("targets/downloads/");
        DataSetSearch dataSetSearch = new DataSetSearch(openBis, downloadFolder);
        dataSetSearch.run(strainNames);

    }

    private void logout()
    {
        openBis.logout();
    }
}
