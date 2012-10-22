/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.rmi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.uitest.dsl.Executor;
import ch.systemsx.cisd.openbis.uitest.request.SearchForSamples;
import ch.systemsx.cisd.openbis.uitest.rmi.eager.SampleRmi;
import ch.systemsx.cisd.openbis.uitest.type.Sample;

/**
 * @author anttil
 */
public class SearchForSamplesRmi extends Executor<SearchForSamples, List<Sample>>
{

    @Override
    public List<Sample> run(SearchForSamples request)
    {

        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, request
                .getCode()));

        return convert(generalInformationService.searchForSamples(session, criteria, EnumSet
                .allOf(SampleFetchOption.class)));
    }

    private List<Sample> convert(
            Collection<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample> rmiSamples)
    {
        List<Sample> result = new ArrayList<Sample>();
        for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample s : rmiSamples)
        {
            result.add(new SampleRmi(s, session, commonServer));
        }
        return result;
    }
}
