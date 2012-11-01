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

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.cina.client.util.v1.ICinaUtilities;
import ch.systemsx.cisd.cina.shared.constants.CinaConstants;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.cli.GlobalArguments;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ResultCode;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CommandExperimentLister extends
        AbstractCinaCommand<CommandExperimentLister.CommandExperimentListerArguments>
{
    static class CommandExperimentListerArguments extends GlobalArguments
    {
        public String getExperimentTypeCode()
        {
            return getArguments().get(0).toString().toUpperCase();
        }

        @Override
        public boolean isComplete()
        {
            if (getArguments().size() < 1)
            {
                return false;
            }
            if (getExperimentTypeCode().length() < 1)
            {
                return false;
            }

            if (false == super.isComplete())
                return false;

            return true;
        }
    }

    private static class ExperimentListerExecutor extends
            AbstractExecutor<CommandExperimentListerArguments>
    {
        private static final String FIELD_SEPARATOR = "\t";

        /**
         * @param command The parent command
         */
        ExperimentListerExecutor(CommandExperimentLister command)
        {
            super(command);
        }

        @Override
        protected ResultCode doExecute(ICinaUtilities component)
        {
            List<Experiment> results =
                    component.listVisibleExperiments(arguments.getExperimentTypeCode());
            printHeader();
            printResults(results);
            return ResultCode.OK;
        }

        private void printHeader()
        {

            StringBuilder sb = new StringBuilder();
            sb.append("EXPERIMENT");
            sb.append(FIELD_SEPARATOR);
            sb.append("TYPE");
            sb.append(FIELD_SEPARATOR);
            sb.append(CinaConstants.DESCRIPTION_PROPERTY_CODE);

            System.out.println(sb.toString());

        }

        private void printResults(List<Experiment> results)
        {
            for (Experiment experiment : results)
            {
                StringBuilder sb = new StringBuilder();
                sb.append(experiment.getIdentifier());
                sb.append(FIELD_SEPARATOR);
                sb.append(experiment.getExperimentTypeCode());
                sb.append(FIELD_SEPARATOR);
                Map<String, String> properties = experiment.getProperties();
                // Show the value of the creator email and the description properties
                String propValue = properties.get(CinaConstants.DESCRIPTION_PROPERTY_CODE);
                if (null != propValue)
                {
                    sb.append(propValue);
                }

                System.out.println(sb.toString());
            }
        }
    }

    public CommandExperimentLister()
    {
        super(new CommandExperimentListerArguments());
    }

    @Override
    public ResultCode execute(String[] args) throws UserFailureException,
            EnvironmentFailureException
    {
        return new ExperimentListerExecutor(this).execute(args);
    }

    @Override
    public String getName()
    {
        return "listexps";
    }

    @Override
    protected String getRequiredArgumentsString()
    {
        return "<experiment type>";
    }
}
