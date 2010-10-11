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

import ch.systemsx.cisd.cina.client.util.v1.ICinaUtilities;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.cli.GlobalArguments;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CommandGenerateSampleCode extends
        AbstractCinaCommand<CommandGenerateSampleCode.CommandGenerateSampleCodeArguments>
{
    static class CommandGenerateSampleCodeArguments extends GlobalArguments
    {
        public String getSampleTypeCode()
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
            if (getSampleTypeCode().length() < 1)
            {
                return false;
            }

            if (false == super.isComplete())
                return false;

            return true;
        }
    }

    private static class GenerateSampleIdExecutor extends
            AbstractExecutor<CommandGenerateSampleCodeArguments>
    {
        /**
         * @param command The parent command
         */
        GenerateSampleIdExecutor(CommandGenerateSampleCode command)
        {
            super(command);
        }

        @Override
        protected int doExecute(ICinaUtilities component)
        {
            String sampleTypeCode = arguments.getSampleTypeCode();
            String result = component.generateSampleCode(sampleTypeCode);
            System.out.println(result);
            return 0;
        }

    }

    public CommandGenerateSampleCode()
    {
        super(new CommandGenerateSampleCodeArguments());
    }

    public int execute(String[] args) throws UserFailureException, EnvironmentFailureException
    {
        return new GenerateSampleIdExecutor(this).execute(args);
    }

    public String getName()
    {
        return "gencode";
    }

    @Override
    protected String getRequiredArgumentsString()
    {
        return "<sample type code>";
    }

}
