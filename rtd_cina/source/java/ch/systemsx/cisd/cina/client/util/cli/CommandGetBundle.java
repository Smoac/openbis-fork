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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.cina.client.util.v1.ICinaUtilities;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.cli.GlobalArguments;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ResultCode;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CommandGetBundle extends
        AbstractCinaCommand<CommandGetBundle.CommandGetBundleArguments>
{
    static class CommandGetBundleArguments extends GlobalArguments
    {
        @Option(name = "o", longName = "output", usage = "Path for output")
        private String output = "";

        public String tryBundleMetadataOwnerIdentifier()
        {
            String replicaId = getArguments().get(0);
            if (replicaId.length() > 0)
            {
                return replicaId.toUpperCase();
            }
            return null;
        }

        public ArrayList<String> getReplicaIdentifiers()
        {
            ArrayList<String> replicaIds = new ArrayList<String>();
            List<String> args = getArguments();
            int size = args.size();
            for (int i = 1; i < size; ++i)
            {
                String replicaId = args.get(i);
                if (replicaId.length() > 0)
                {
                    replicaIds.add(replicaId.toUpperCase());
                }
            }
            return replicaIds;
        }

        public String getOutput()
        {
            return output;
        }

        @Override
        public boolean isComplete()
        {
            if (getArguments().size() < 1)
            {
                return false;
            }

            if (null == tryBundleMetadataOwnerIdentifier())
            {
                return false;
            }

            if (false == super.isComplete())
                return false;

            return true;
        }
    }

    private static class GetBundleExecutor extends AbstractExecutor<CommandGetBundleArguments>
    {
        /**
         * @param command The parent command
         */
        GetBundleExecutor(CommandGetBundle command)
        {
            super(command);
        }

        @Override
        protected ResultCode doExecute(ICinaUtilities component)
        {
            // Create the output directory
            File outputDir = getOutputDir();
            outputDir.mkdirs();

            // Grid Id must be non-null & non-empty -- otherwise, we wouldn't be here
            String gridIdentifier = arguments.tryBundleMetadataOwnerIdentifier();
            List<String> replicaIdentifiers = arguments.getReplicaIdentifiers();

            BundleDownloader downloader =
                    new BundleDownloader(component, gridIdentifier, replicaIdentifiers, outputDir);
            downloader.download();

            return ResultCode.OK;
        }

        private File getOutputDir()
        {
            File outputDir;
            if (arguments.getOutput().length() > 0)
            {
                // create the directory specified by output
                outputDir = new File(arguments.getOutput());
            } else
            {
                // gridId can't be null if we are here
                String gridId = arguments.tryBundleMetadataOwnerIdentifier();
                String[] gridIdComponents = gridId.split("/");
                String bundleName = gridIdComponents[gridIdComponents.length - 1];
                outputDir = new File(bundleName);
            }
            return outputDir;
        }
    }

    public CommandGetBundle()
    {
        super(new CommandGetBundleArguments());
    }

    public ResultCode execute(String[] args) throws UserFailureException,
            EnvironmentFailureException
    {
        return new GetBundleExecutor(this).execute(args);
    }

    public String getName()
    {
        return "getbundle";
    }

    @Override
    protected String getRequiredArgumentsString()
    {
        return "<grid identifier> [<replica identifier> ...]";
    }
}
