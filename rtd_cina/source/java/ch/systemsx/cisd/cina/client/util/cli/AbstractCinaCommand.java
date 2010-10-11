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

import ch.systemsx.cisd.cina.client.util.v1.CinaUtilitiesFacadeFactory;
import ch.systemsx.cisd.cina.client.util.v1.ICinaUtilities;
import ch.systemsx.cisd.openbis.dss.client.api.cli.AbstractCommand;
import ch.systemsx.cisd.openbis.dss.client.api.cli.GlobalArguments;

/**
 * Superclass for cina command-line client commands.
 * <p>
 * Provides services used by most subclasses. In order for the AbstractCommand to work, subclasses
 * must do the following:
 * <ul>
 * <li>Set the parser ivar in their constructor
 * </ul>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class AbstractCinaCommand<T extends GlobalArguments> extends AbstractCommand<T>
{
    protected AbstractCinaCommand(T arguments)
    {
        super(arguments);
    }

    /**
     * How is this program invoked from the command line?
     */
    @Override
    protected String getProgramCallString()
    {
        return CinaCommandFactory.PROGRAM_CALL_STRING;
    }

    /**
     * Creates the CinaUtilities facade object and logs into the server.
     */
    protected ICinaUtilities login()
    {
        ICinaUtilities component =
                CinaUtilitiesFacadeFactory.tryCreate(arguments.getUsername(),
                        arguments.getPassword(), arguments.getServerBaseUrl());
        if (null == component)
        {
            System.out.println("Username / password is invalid");
        }
        return component;
    }
}
