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

import ch.systemsx.cisd.cina.client.util.v1.ICinaUtilities;
import ch.systemsx.cisd.cina.shared.constants.CinaConstants;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.cli.GlobalArguments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CommandSampleLister extends AbstractCommand<GlobalArguments>
{
    private static class SampleListerExecutor extends AbstractExecutor<GlobalArguments>
    {
        /**
         * @param command The parent command
         */
        SampleListerExecutor(CommandSampleLister command)
        {
            super(command);
        }
        
        @Override
        protected int doExecute(ICinaUtilities component)
        {
            SearchCriteria searchCriteria = new SearchCriteria();
            searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                    MatchClauseAttribute.TYPE, CinaConstants.REPLICA_SAMPLE_TYPE_CODE));
            List<Sample> results = component.searchForSamples(searchCriteria);
            for (Sample sample : results)
            {
                System.out.println(sample.toString());
            }
            return 0;
        }

    }

    public CommandSampleLister()
    {
        super(new GlobalArguments());
    }

    public int execute(String[] args) throws UserFailureException, EnvironmentFailureException
    {
        return new SampleListerExecutor(this).execute(args);
    }

    public String getName()
    {
        return "list-samples";
    }
}
