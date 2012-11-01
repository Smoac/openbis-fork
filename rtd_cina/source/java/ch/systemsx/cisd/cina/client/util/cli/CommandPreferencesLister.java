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

import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.systemsx.cisd.cina.client.util.v1.ICinaUtilities;
import ch.systemsx.cisd.cina.shared.constants.CinaConstants;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.cli.GlobalArguments;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ResultCode;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CommandPreferencesLister extends
        AbstractCinaCommand<CommandPreferencesLister.CommandListPreferencesArguments>
{
    static class CommandListPreferencesArguments extends GlobalArguments
    {

        public String getSampleTypeCode()
        {
            if (getArguments().size() < 1)
            {
                return CinaConstants.CINA_BROWSER_PREFERENCES_TYPE_CODE;
            }

            String sampleTypeCode = getArguments().get(0);
            if (sampleTypeCode.length() > 0)
            {
                return sampleTypeCode.toUpperCase();
            }
            return CinaConstants.CINA_BROWSER_PREFERENCES_TYPE_CODE;
        }

        @Override
        public boolean isComplete()
        {

            if (null == getSampleTypeCode())
            {
                return false;
            }

            if (false == super.isComplete())
                return false;

            return true;
        }
    }

    private static class PreferencesListerExecutor extends
            AbstractExecutor<CommandListPreferencesArguments>
    {
        private static final String FIELD_SEPARATOR = "\t";

        /**
         * @param command The parent command
         */
        PreferencesListerExecutor(CommandPreferencesLister command)
        {
            super(command);
        }

        @Override
        protected ResultCode doExecute(ICinaUtilities component)
        {
            SearchCriteria searchCriteria = new SearchCriteria();
            searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                    MatchClauseAttribute.TYPE, arguments.getSampleTypeCode()));
            List<Sample> samples = component.searchForSamples(searchCriteria);
            List<DataSet> results = component.listDataSets(samples);
            printHeader();
            printResults(results);
            return ResultCode.OK;
        }

        private void printHeader()
        {

            StringBuilder sb = new StringBuilder();
            sb.append("DATE");
            sb.append(FIELD_SEPARATOR);
            sb.append("DATA SET CODE");
            System.out.println(sb.toString());
        }

        private void printResults(List<DataSet> results)
        {
            Collections.sort(results, new Comparator<DataSet>()
                {
                    @Override
                    public int compare(DataSet o1, DataSet o2)
                    {
                        // We want earlier data sets later in the list
                        return o2.getRegistrationDate().compareTo(o1.getRegistrationDate());
                    }
                });
            for (DataSet dataSet : results)
            {
                StringBuilder sb = new StringBuilder();
                String registrationDate =
                        DateFormat.getDateTimeInstance().format(dataSet.getRegistrationDate());
                sb.append(registrationDate);
                sb.append(FIELD_SEPARATOR);
                sb.append(dataSet.getCode());

                System.out.println(sb.toString());
            }
        }

    }

    public CommandPreferencesLister()
    {
        super(new CommandListPreferencesArguments());
    }

    @Override
    public ResultCode execute(String[] args) throws UserFailureException,
            EnvironmentFailureException
    {
        return new PreferencesListerExecutor(this).execute(args);
    }

    @Override
    public String getName()
    {
        return "listprefs";
    }

    @Override
    protected String getRequiredArgumentsString()
    {
        return "[<sample type code>]";
    }
}
