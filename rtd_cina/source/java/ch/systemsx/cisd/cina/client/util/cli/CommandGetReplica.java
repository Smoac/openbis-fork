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

import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.cina.client.util.v1.ICinaUtilities;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.cli.GlobalArguments;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ResultCode;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CommandGetReplica extends
        AbstractCinaCommand<CommandGetReplica.CommandGetReplicaArguments>
{
    static class CommandGetReplicaArguments extends GlobalArguments
    {
        @Option(name = "o", longName = "output", usage = "Output folder")
        private String outputFolder = "";

        public ArrayList<String> getReplicaIdentifiers()
        {
            ArrayList<String> replicaIds = new ArrayList<String>();
            for (String replicaId : getArguments())
            {
                if (replicaId.length() > 0)
                {
                    replicaIds.add(replicaId.toUpperCase());
                }
            }
            return replicaIds;
        }

        public String getOutputFolder()
        {
            return outputFolder;
        }

        @Override
        public boolean isComplete()
        {
            if (getArguments().size() < 1)
            {
                return false;
            }

            if (getReplicaIdentifiers().size() < 1)
            {
                return false;
            }

            if (false == super.isComplete())
                return false;

            return true;
        }
    }

    private static class GetReplicaExecutor extends AbstractExecutor<CommandGetReplicaArguments>
    {
        /**
         * @param command The parent command
         */
        GetReplicaExecutor(CommandGetReplica command)
        {
            super(command);
        }

        @Override
        protected ResultCode doExecute(ICinaUtilities component)
        {
            // Find all datasets connected to this sample
            for (String sampleCode : arguments.getReplicaIdentifiers())
            {
                executeForSampleCode(component, sampleCode);
            }
            return ResultCode.OK;
        }

        protected void executeForSampleCode(ICinaUtilities component, String sampleCode)
        {
            // Find all datasets connected to this sample
            component.listDataSetsForSampleCode(sampleCode);
            // List<DataSet> dataSets = component.listDataSetsForSampleCode(sampleCode);
            // Download the raw-data dataset
            // Download the ...
            // List<Experiment> results =
            // component.listVisibleExperiments(arguments.getReplicaIdentifier());
        }
    }

    public CommandGetReplica()
    {
        super(new CommandGetReplicaArguments());
    }

    public ResultCode execute(String[] args) throws UserFailureException,
            EnvironmentFailureException
    {
        return new GetReplicaExecutor(this).execute(args);
    }

    public String getName()
    {
        return "getreplica";
    }

    @Override
    protected String getRequiredArgumentsString()
    {
        return "<replica identifier> [<replica identifier> ...]";
    }
}
