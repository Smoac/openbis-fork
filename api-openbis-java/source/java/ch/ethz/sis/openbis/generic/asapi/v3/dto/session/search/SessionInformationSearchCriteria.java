/*
 * Copyright ETH 2007 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.id.ISessionInformationId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.session.search.SessionInformationSearchCriteria")
public class SessionInformationSearchCriteria extends AbstractObjectSearchCriteria<ISessionInformationId>
{

    private static final long serialVersionUID = 1L;

    public SessionInformationSearchCriteria()
    {
    }

    public UserNameSearchCriteria withUserName()
    {
        return with(new UserNameSearchCriteria());
    }

    public PersonalAccessTokenSessionSearchCriteria withPersonalAccessTokenSession()
    {
        return with(new PersonalAccessTokenSessionSearchCriteria());
    }

    public PersonalAccessTokenSessionNameSearchCriteria withPersonalAccessTokenSessionName()
    {
        return with(new PersonalAccessTokenSessionNameSearchCriteria());
    }

    public SessionInformationSearchCriteria withOrOperator()
    {
        return (SessionInformationSearchCriteria) withOperator(SearchOperator.OR);
    }

    public SessionInformationSearchCriteria withAndOperator()
    {
        return (SessionInformationSearchCriteria) withOperator(SearchOperator.AND);
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("SESSION_INFORMATION");
        return builder;
    }

}
