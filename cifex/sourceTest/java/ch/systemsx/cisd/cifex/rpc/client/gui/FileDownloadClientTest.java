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

package ch.systemsx.cisd.cifex.rpc.client.gui;

import java.io.IOException;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class FileDownloadClientTest extends AssertJUnit
{

    @Test
    public void testLaunchingWithBadArguments() throws IOException
    {
        try
        {
            String[] args =
                { "" };
            new CIFEXCommunicationState(args);
        } catch (ConfigurationFailureException e)
        {
            // worked correctly
            return;
        } catch (EnvironmentFailureException ex)
        {
            fail("Launch with bad arguments should have thrown a ConfigurationFailureException.");
        } catch (ch.systemsx.cisd.cifex.shared.basic.UserFailureException ex)
        {
            fail("Launch with bad arguments should have thrown a ConfigurationFailureException");
        }
        fail("Launch with bad arguments should have thrown an exception.");
    }

}
