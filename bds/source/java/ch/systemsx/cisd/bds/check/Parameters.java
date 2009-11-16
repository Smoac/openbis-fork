/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds.check;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.args4j.Argument;
import ch.systemsx.cisd.args4j.CmdLineException;
import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.ExampleMode;
import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.bds.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.bds.StringUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.IExitHandler;
import ch.systemsx.cisd.common.utilities.SystemExit;

/**
 * The class to process the command line parameters.
 * 
 * @author Christian Ribeaud
 */
final class Parameters
{
    @Argument()
    private final List<String> args = new ArrayList<String>();

    // @Option(name = "v", longName = "verbose", skipForExample = true, usage = "Show more detailed
    // problem report.")
    private boolean verbose;

    private final String programName;

    Parameters(final String[] args, final String programName)
    {
        this(args, programName, SystemExit.SYSTEM_EXIT);
    }

    Parameters(final String[] args, final String programName, final IExitHandler systemExitHandler)
    {
        this.programName = programName;
        try
        {
            parser.parseArgument(args);
            if (getArgs().size() != 1)
            {
                System.err.println(String.format("Exactly one %s file must be specified.",
                        "base directory"));
                printHelp(true);
            }
        } catch (final Exception ex)
        {
            outputException(ex);
            systemExitHandler.exit(1);
            // Only reached in unit tests.
            throw new AssertionError(ex.getMessage());
        }
    }

    /**
     * The command line parser.
     */
    private final CmdLineParser parser = new CmdLineParser(this);

    @Option(name = "h", longName = "help", skipForExample = true, usage = "Shows this help text.")
    void printHelp(final boolean exit)
    {
        parser.printHelp(programName, "[option [...]]", "<base-directory>", ExampleMode.ALL);
        if (exit)
        {
            System.exit(0);
        }
    }

    @Option(longName = "version", skipForExample = true, usage = "Prints out the version information.")
    void printVersion(final boolean exit)
    {
        System.err.println(programName + " version "
                + BuildAndEnvironmentInfo.INSTANCE.getFullVersion());
        if (exit)
        {
            System.exit(0);
        }
    }

    private final void outputException(final Exception ex)
    {
        if (ex instanceof UserFailureException || ex instanceof CmdLineException)
        {
            System.err.println(ex.getMessage());
        } else
        {
            System.err.print("An exception has occurred: ");
            ex.printStackTrace();
        }
        if (ex instanceof CmdLineException)
        {
            printHelp(false);
        }
    }

    final List<String> getArgs()
    {
        return Collections.unmodifiableList(args);
    }

    /**
     * Returns the base directory as {@link File}.
     * <p>
     * Note that no check is performed on the returned <code>File</code>.
     * </p>
     */
    final File getFile()
    {
        assert args.isEmpty() == false : "Argument list can not be empty.";
        final String filePath = args.get(0);
        if (StringUtils.isBlank(filePath))
        {
            throw UserFailureException.fromTemplate("Given base directory path is blank.");
        }
        return new File(filePath);
    }

    final boolean isVerbose()
    {
        return verbose;
    }
}
