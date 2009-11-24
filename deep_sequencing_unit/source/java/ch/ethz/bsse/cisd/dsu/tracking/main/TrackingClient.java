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

import java.util.Properties;

import ch.ethz.bsse.cisd.dsu.tracking.EntityTrackingEmailGenerator;
import ch.ethz.bsse.cisd.dsu.tracking.IEntityTrackingEmailGenerator;
import ch.ethz.bsse.cisd.dsu.tracking.utils.LogUtils;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.ITrackingServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Tomasz Pylak
 */
public class TrackingClient
{
    private static final String SERVICE_PROPERTIES_FILE = "etc/tracking-service.properties";

    private static final String LOCAL_STORAGE_FILE = "etc/tracking-local-database";

    public static void main(String[] args)
    {
        try
        {
            track();
        } catch (EnvironmentFailureException ex)
        {
            LogUtils.notify(ex);
        } catch (Throwable ex)
        {
            LogUtils.notify(ex);
        }
    }

    private static void track()
    {
        LogInitializer.init();
        Properties props = PropertyUtils.loadProperties(SERVICE_PROPERTIES_FILE);
        Parameters params = new Parameters(props);

        ITrackingServer trackingServer = createOpenBISTrackingServer(params);
        IEntityTrackingEmailGenerator emailGenerator = new EntityTrackingEmailGenerator(props);
        IMailClient mailClient = params.getMailClient();
        TrackingBO trackingBO = new TrackingBO(trackingServer, emailGenerator, mailClient);

        ITrackingDAO trackingDAO = new FileBasedTrackingDAO(LOCAL_STORAGE_FILE);

        SessionContextDTO session = authentificateInOpenBIS(params, trackingServer);

        trackingBO.trackAndNotify(trackingDAO, session);
    }

    private static ITrackingServer createOpenBISTrackingServer(Parameters params)
    {
        return HttpInvokerUtils.createServiceStub(ITrackingServer.class, params
                .getOpenbisServerURL(), 5);
    }

    private static SessionContextDTO authentificateInOpenBIS(Parameters params,
            ITrackingServer trackingServer)
    {
        try
        {
            String openbisUser = params.getOpenbisUser();
            SessionContextDTO session =
                    trackingServer.tryToAuthenticate(openbisUser, params.getOpenbisPassword());
            if (session == null)
            {
                throw createAuthentificationException(params, null);
            }
            return session;
        } catch (Exception ex)
        {
            throw createAuthentificationException(params, ex);
        }
    }

    private static EnvironmentFailureException createAuthentificationException(Parameters params,
            Exception exOrNull)
    {
        String exceptionMsg =
                (exOrNull == null) ? "" : " Unexpected exception has occured: "
                        + exOrNull.getMessage();
        return LogUtils
                .environmentError(
                        "Cannot authentificate in openBIS as a user '%s'. Check that the password is correct and that openBIS service URL is correct.%s",
                        params.getOpenbisUser(), exceptionMsg);
    }

}
