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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import java.io.File;

import ch.systemsx.cisd.args4j.CmdLineException;
import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.ExampleMode;
import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.common.cli.ConsoleClientArguments;
import ch.systemsx.cisd.common.logging.ConsoleLogger;

/**
 * A standalone command line tool allowing the execution of Jython scripts to initialize the master
 * data on an openBIS AS.
 * 
 * @author Kaloyan Enimanev
 */
public class MasterDataRegistrationScriptRunnerStandalone
{

    public static class Arguments extends ConsoleClientArguments
    {
        @Option(name = "f", longName = "script-file", usage = "Jython script file", required = true)
        private String scriptFileName = "";

        public String getScriptFileName()
        {
            return scriptFileName;
        }
    }

    void runMe(String[] args)
    {
        Arguments arguments = new Arguments();
        CmdLineParser argumentsParser = new CmdLineParser(arguments);

        try
        {
            argumentsParser.parseArgument(args);
        } catch (CmdLineException cmdEx)
        {
            printHelp(argumentsParser);
            return;
        }

        if (arguments.isComplete())
        {
            runMe(arguments);
        } else
        {
            printHelp(argumentsParser);
        }
    }

    private void printHelp(CmdLineParser argumentsParser)
    {
        argumentsParser.printUsage(System.out);
        String example = argumentsParser.printExample(ExampleMode.ALL);
        System.out.println(String.format("Example: java %s %s", getClass().getName(), example));
    }

    private void runMe(Arguments arguments)
    {
        EncapsulatedCommonServer commonServer =
                EncapsulatedCommonServer.create(arguments.getServerBaseUrl(),
                        arguments.getUsername(), arguments.getPassword());

        MasterDataRegistrationScriptRunner scriptRunner =
                new MasterDataRegistrationScriptRunner(commonServer, new ConsoleLogger());
        scriptRunner.executeScript(new File(arguments.scriptFileName));
    }

    public static void main(String[] args)
    {
        args =
                new String[]
                    { "-s", "http://localhost:8888/openbis/openbis", "-u", "admin", "-p",
                            "123",
                            // "-s", "https://localhost:8443/openbis/openbis", "-u", "admin", "-p",
                            // "123",
                            "-f",
                            "/Users/kaloyane/cisd/modules/openbis/dist/server/export-master-data.py" };

        MasterDataRegistrationScriptRunnerStandalone cliApplication =
                new MasterDataRegistrationScriptRunnerStandalone();
        cliApplication.runMe(args);
    }

}
