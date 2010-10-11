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

package ch.systemsx.cisd.cina.client.util.cli;

import java.util.ArrayList;

import ch.systemsx.cisd.common.utilities.SystemExit;
import ch.systemsx.cisd.openbis.dss.client.api.cli.AbstractClient;
import ch.systemsx.cisd.openbis.dss.client.api.cli.CompositeCommandFactory;
import ch.systemsx.cisd.openbis.dss.client.api.cli.DssCommandFactory;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ICommandFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CinaClient extends AbstractClient
{
    private final static boolean ENABLE_LOGGING = false;

    static
    {
        // Disable any logging output.
        if (ENABLE_LOGGING)
            enableDebugLogging();
        else
            disableLogging();
    }

    private CinaClient()
    {
        super(SystemExit.SYSTEM_EXIT, createCommandFactory());
    }

    private static CompositeCommandFactory createCommandFactory()
    {
        ArrayList<ICommandFactory> factories = new ArrayList<ICommandFactory>(2);
        factories.add(new CinaCommandFactory());
        factories.add(new DssCommandFactory());
        return new CompositeCommandFactory(CinaCommandFactory.PROGRAM_CALL_STRING, factories);
    }

    public static void main(String[] args)
    {
        CinaClient newMe = new CinaClient();
        newMe.runWithArgs(args);
    }
}
