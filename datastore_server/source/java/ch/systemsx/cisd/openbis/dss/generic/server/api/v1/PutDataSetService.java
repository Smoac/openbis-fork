/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.api.v1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.etlserver.IETLServerPlugin;
import ch.systemsx.cisd.etlserver.Parameters;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;

/**
 * Helper class that maintains the state for handling put requests. The requests themselves are
 * serviced by the {@link PutDataSetExecutor}.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class PutDataSetService
{
    private final IEncapsulatedOpenBISService openBisService;

    private final MailClient mailClient;

    private final File incomingDir;

    PutDataSetService(IEncapsulatedOpenBISService openBisService)
    {
        this.openBisService = openBisService;

        PutDataSetServiceInitializer initializer = new PutDataSetServiceInitializer();

        incomingDir = initializer.getIncomingDir();
        incomingDir.mkdir();

        mailClient = new MailClient(initializer.getMailProperties());
    }

    void putDataSet(String sessionToken, NewDataSetDTO newDataSet, InputStream inputStream)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        try
        {
            new PutDataSetExecutor(this, sessionToken, newDataSet, inputStream).execute();

        } catch (UserFailureException e)
        {
            throw new IllegalArgumentException(e);
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        } finally
        {
            // Close the input stream now that we are done with it
            try
            {
                inputStream.close();
            } catch (IOException ex)
            {

            }
        }
    }

    IEncapsulatedOpenBISService getOpenBisService()
    {
        return openBisService;
    }

    MailClient getMailClient()
    {
        return mailClient;
    }

    File getIncomingDir()
    {
        return incomingDir;
    }
}

/**
 * Helper class to simplify initializing the final fields of the {@link PutDataSetService}.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class PutDataSetServiceInitializer
{
    private final Parameters params = new Parameters();

    File getIncomingDir()
    {
        return new File(System.getProperty("java.io.tmpdir"), "dss_rpc_incoming");
    }

    Properties getMailProperties()
    {
        return Parameters.createMailProperties(params.getProperties());
    }

    IETLServerPlugin getPlugin()
    {
        ThreadParameters[] threadParams = params.getThreads();
        return threadParams[0].getPlugin();
    }

}
