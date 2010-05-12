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

package ch.systemsx.cisd.openbis.dss.client.api.cli;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.client.api.cli.CommandFactory;
import ch.systemsx.cisd.openbis.dss.client.api.cli.CommandGet;
import ch.systemsx.cisd.openbis.dss.client.api.cli.CommandLs;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ICommand;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CommandFactoryTest extends AssertJUnit
{
    CommandFactory factory;

    @BeforeMethod
    public void setUp()
    {
        factory = new CommandFactory();
    }

    @Test
    public void testNameMapping()
    {
        ICommand cmd;
        cmd = factory.tryCommandForName("ls");
        assertEquals(CommandLs.class, cmd.getClass());

        cmd = factory.tryCommandForName("get");
        assertEquals(CommandGet.class, cmd.getClass());
    }

    @Test
    public void testHelp()
    {
        ICommand cmd;
        cmd = factory.tryCommandForName("help");
        String[] noArgs = {};
        cmd.execute(noArgs);

        System.out.print("\n");
        cmd = factory.tryCommandForName("help");
        String[] lsArgs =
            { "ls" };
        cmd.execute(lsArgs);

        System.out.print("\n");
        cmd = factory.tryCommandForName("help");
        String[] getArgs =
            { "get" };
        cmd.execute(getArgs);
    }
}
