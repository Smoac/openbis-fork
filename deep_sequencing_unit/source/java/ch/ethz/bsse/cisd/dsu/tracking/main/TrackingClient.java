/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.ethz.bsse.cisd.dsu.tracking.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;

import ch.ethz.bsse.cisd.dsu.tracking.email.EntityTrackingEmailGenerator;
import ch.ethz.bsse.cisd.dsu.tracking.email.IEntityTrackingEmailGenerator;
import ch.ethz.bsse.cisd.dsu.tracking.utils.LogUtils;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.io.PropertyIOUtils;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.shared.ITrackingServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

// v3
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;

/**
 * @author Tomasz Pylak
 * @author Manuel Kohler
 */
public class TrackingClient
{
    private static final String SERVICE_PROPERTIES_FILE = "etc/service.properties";

    private static final String LOCAL_SAMPLE_DB = "etc/tracking-local-database";

    private static final String LOCAL_DATASET_DB = "etc/tracking-sample-database";

    private static final String EMAIL_TEMPLATE_FILE = "etc/email-template.txt";

    private static final String OPENBIS_RMI_TRACKING = "/rmi-tracking";

    public static final String CL_PARAMETER_LANES = "lanes";

    public static final String CL_PARAMETER_ALL = "all";

    public static final String CL_PARAMETER_CHANGED_LANES = "changed_lanes";

    public static final String CL_PARAMETER_COPY_DATA_SETS = "copy_data_sets";
    
    public static final String CL_PARAMETER_REMOVE_LANES = "remove";
    
    public static final String CL_PARAMETER_LIST_SPACES = "list_spaces";
    
    private static IApplicationServerApi v3;

    private static String v3SessionToken;
    
    public static void main(String[] args)
    {
        try
        {
            HashMap<String, String[]> commandLineMap = parseCommandLine(args);
            track(commandLineMap);
        } catch (EnvironmentFailureException ex)
        {
            LogUtils.notify(ex);
        } catch (Throwable ex)
        {
            LogUtils.notify(ex);
        }
    }

    private static HashMap<String, String[]> parseCommandLine(String[] args)
    {
        HashMap<String, String[]> commandLineMap = new HashMap<String, String[]>();
        CommandLineParser parser = new GnuParser();

        Options options = new Options();
        Option lanes = OptionBuilder.withArgName(CL_PARAMETER_LANES)
                .hasArg()
                .withDescription("list of lanes to track")
                .create(CL_PARAMETER_LANES);
        lanes.setArgs(Option.UNLIMITED_VALUES);
        // Option all = new Option(CL_PARAMETER_ALL, "track all lanes, only for testing, never use in production!");
        
        Option new_lanes = new Option(CL_PARAMETER_CHANGED_LANES, "only list lanes which have new datasets");
        
        Option copy_data_sets = new Option(CL_PARAMETER_COPY_DATA_SETS, "also copy the corresponding data sets to an extra"
        		+ " folder. Sample must be part of the space which is set in the property 'space-whitelist'. "
        		+ " Only in combination with parameter \"" + CL_PARAMETER_LANES + "\"");
        
        Option remove = OptionBuilder.withArgName(CL_PARAMETER_REMOVE_LANES)
        		.hasArg()
        		.withDescription("remove lanes from tracking list and do not send an email")
        		.create(CL_PARAMETER_REMOVE_LANES);
        remove.setArgs(Option.UNLIMITED_VALUES);

        Option list_spaces = new Option(CL_PARAMETER_LIST_SPACES, "list spaces which are configured for an extra copy");
        
        options.addOption(lanes);
        // options.addOption(all);
        options.addOption(new_lanes);
        options.addOption(copy_data_sets);
        options.addOption(remove);
        options.addOption(list_spaces);

        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        if (args.length < 1)
        {
            formatter.printHelp("help", options);
            System.exit(0);
        }

        try
        {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption(CL_PARAMETER_LANES))
            {
                String[] laneArray = line.getOptionValues(CL_PARAMETER_LANES);
                commandLineMap.put(CL_PARAMETER_LANES, laneArray);
            }
            if (line.hasOption(CL_PARAMETER_ALL))
            {
                // commandLineMap.put(CL_PARAMETER_ALL, null);
                System.out.println("This option is deactivated.");

            }
            if (line.hasOption(CL_PARAMETER_CHANGED_LANES))
            {
                commandLineMap.put(CL_PARAMETER_CHANGED_LANES, null);
            }
            if (line.hasOption(CL_PARAMETER_COPY_DATA_SETS))
            {
            	commandLineMap.put(CL_PARAMETER_COPY_DATA_SETS, null);
            }
            if (line.hasOption(CL_PARAMETER_LIST_SPACES))
            {
            	commandLineMap.put(CL_PARAMETER_LIST_SPACES, null);
            }
            if (line.hasOption(CL_PARAMETER_REMOVE_LANES))
            {
                commandLineMap.put(CL_PARAMETER_REMOVE_LANES, line.getOptionValues(CL_PARAMETER_REMOVE_LANES));
            }
        } catch (ParseException exp)
        {
            LogUtils.environmentError("Parsing of command line parameters failed.", exp.getMessage());
            System.out.println("Parsing of command line parameters failed. " + exp.getMessage());
            System.exit(1);
        }
        return commandLineMap;
    }

    private static void track(HashMap<String, String[]> commandLineMap)
    {
        LogInitializer.init();
        Properties props = PropertyIOUtils.loadProperties(SERVICE_PROPERTIES_FILE);
        Parameters params = new Parameters(props);

        ITrackingServer trackingServer = createOpenBISTrackingServer(params);
        SessionContextDTO session = authentificateInOpenBIS(params, trackingServer);
        // also login to the v3 API 
        IApplicationServerApi v3 = initV3(params);
        v3SessionToken = v3.login(params.getOpenbisUser(), params.getOpenbisPassword());
       

        IEntityTrackingEmailGenerator emailGenerator =
                new EntityTrackingEmailGenerator(props, retrieveEmailTemplate(), session);
        IMailClient mailClient = params.getMailClient();
        TrackingBO trackingBO = new TrackingBO(trackingServer, emailGenerator, mailClient);

        ITrackingDAO trackingDAO = new FileBasedTrackingDAO(LOCAL_SAMPLE_DB, LOCAL_DATASET_DB);

        trackingBO.trackAndNotify(trackingDAO, commandLineMap, params, session, v3, v3SessionToken);
    }

    private static ITrackingServer createOpenBISTrackingServer(Parameters params)
    {
        String serviceURL = params.getOpenbisServerURL() + OPENBIS_RMI_TRACKING;
        return HttpInvokerUtils.createServiceStub(ITrackingServer.class, serviceURL,
                5 * DateUtils.MILLIS_PER_MINUTE);
    }

    private static SessionContextDTO authentificateInOpenBIS(Parameters params,
            ITrackingServer trackingServer)
    {
        try
        {
            String openbisUser = params.getOpenbisUser();
            SessionContextDTO session =
                    trackingServer.tryAuthenticate(openbisUser, params.getOpenbisPassword());
            if (session == null)
            {
                throw createAuthentificationException(params, null);
            }
            trackingServer.setBaseIndexURL(session.getSessionToken(), params.getPermlinkURL());
            return session;
        } catch (Exception ex)
        {
            throw createAuthentificationException(params, ex);
        }
    }

    private static String retrieveEmailTemplate()
    {
        try
        {
            return IOUtils.toString(new FileReader(new File(EMAIL_TEMPLATE_FILE)));
        } catch (FileNotFoundException ex)
        {
            throw LogUtils.environmentError("Couldn't find email template file '%s'.",
                    EMAIL_TEMPLATE_FILE);
        } catch (IOException ex)
        {
            throw LogUtils.environmentError(
                    "Exception has occured while trying to read template file '%s':%s",
                    EMAIL_TEMPLATE_FILE, ex.getMessage());
        }
    }

    private static EnvironmentFailureException createAuthentificationException(Parameters params,
            Exception exOrNull)
    {
        String exceptionMsg =
                (exOrNull == null) ? "" : " Unexpected exception has occured: "
                        + exOrNull.getMessage();

        EnvironmentFailureException ret =
                LogUtils.environmentError(
                        "Cannot authentificate in openBIS as a user '%s'. Check that the password is correct and that openBIS service URL is correct.%s",
                        params.getOpenbisUser(), exceptionMsg);

        if (false == StringUtils.isBlank(params.getAdminEmail()))
        {
            List<EMailAddress> adminEmails = new ArrayList<EMailAddress>();
            for (String adminEmail : params.getAdminEmail().split(","))
            {
                adminEmails.add(new EMailAddress(adminEmail.trim()));
            }

            IMailClient emailClient = params.getMailClient();
            emailClient.sendEmailMessage("[DSU Tracker] DSU Tracking client NOT working",
                    ret.getLocalizedMessage(), null,
                    new EMailAddress(params.getNotificationEmail()),
                    adminEmails.toArray(new EMailAddress[0]));
        }
        return ret;
    }
    
    private static IApplicationServerApi initV3(Parameters params) {
        v3 = HttpInvokerUtils
                .createServiceStub(IApplicationServerApi.class, params.getOpenbisServerURL()
                        + IApplicationServerApi.SERVICE_URL, 10000);
        return v3;
   }
}
