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

package ch.systemsx.cisd.openbis.uitest.dsl;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.uitest.rmi.SearchForSamplesOnBehalfOfUserRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.SearchForSamplesRmi;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.User;

/**
 * @author anttil
 */
public class SampleSearchCommandBuilder implements SearchCommandBuilder<Sample>
{

    private User user;

    private SearchCriteria criteria;

    public SampleSearchCommandBuilder()
    {
        this.criteria = new SearchCriteria();
    }

    public SampleSearchCommandBuilder withCode(String code)
    {
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, code));
        return this;
    }

    @SuppressWarnings("hiding")
    public SampleSearchCommandBuilder onBehalfOf(User user)
    {
        this.user = user;
        return this;
    }

    public SampleSearchCommandBuilder withMetaProjects(MetaProject first, MetaProject... rest)
    {
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.METAPROJECT,
                first.getName()));
        for (MetaProject metaProject : rest)
        {
            criteria.addMatchClause(MatchClause.createAttributeMatch(
                    MatchClauseAttribute.METAPROJECT,
                    metaProject.getName()));
        }
        return this;
    }

    @Override
    public Command<List<Sample>> build()
    {
        if (this.user == null)
        {
            return new SearchForSamplesRmi(criteria);
        } else
        {
            return new SearchForSamplesOnBehalfOfUserRmi(criteria, user);
        }
    }

}
