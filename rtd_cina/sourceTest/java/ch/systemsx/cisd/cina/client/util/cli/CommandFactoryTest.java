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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.client.api.cli.CompositeCommandFactory;
import ch.systemsx.cisd.openbis.dss.client.api.cli.DssCommandFactory;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ICommand;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ICommandFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CommandFactoryTest extends AssertJUnit
{
    CompositeCommandFactory factory;

    @BeforeMethod
    public void setUp()
    {
        ArrayList<ICommandFactory> factories = new ArrayList<ICommandFactory>(2);
        factories.add(new CinaCommandFactory());
        factories.add(new DssCommandFactory());
        factory = new CompositeCommandFactory(CinaCommandFactory.PROGRAM_CALL_STRING, factories);
    }

    @Test
    public void testNameMapping()
    {
        ICommand cmd;
        cmd = factory.tryCommandForName("listsamps");
        assertEquals(CommandSampleLister.class, cmd.getClass());

        cmd = factory.tryCommandForName("gencode");
        assertEquals(CommandGenerateSampleCode.class, cmd.getClass());

        cmd = factory.tryCommandForName("ls");
        assertNotNull(cmd);

        cmd = factory.tryCommandForName("get");
        assertNotNull(cmd);
    }

    @Test
    public void testHelp()
    {
        ICommand cmd;
        cmd = factory.getHelpCommand();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bos);
        cmd.printUsage(out);
        String helpText =
                "usage: cina_client.sh COMMAND [options...] <command arguments>\n" + "\n"
                        + "Commands:\n" + " listsamps\n" + " gencode\n" + " listexps\n"
                        + " getreplica\n" + " getbundle\n" + " ls\n" + " get\n" + " put\n" + "\n"
                        + "Options:\n" + " [-p,--password] VAL        : User login password\n"
                        + " [-s,--server-base-url] VAL : URL for openBIS Server (required)\n"
                        + " [-u,--username] VAL        : User login name\n";
        assertEquals(helpText, bos.toString());
    }
}
