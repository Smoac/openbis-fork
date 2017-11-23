/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.person;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.EmailSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.FirstNameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.LastNameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.UserIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.UserIdsSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.StringFieldMatcher;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchPersonExecutor extends AbstractSearchObjectManuallyExecutor<PersonSearchCriteria, PersonPE>
        implements ISearchPersonExecutor
{

    @Override
    protected List<PersonPE> listAll()
    {
        return daoFactory.getPersonDAO().listAllEntities();
    }

    @Override
    protected Matcher<PersonPE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof UserIdSearchCriteria)
        {
            return new UserIdMatcher();
        } else if (criteria instanceof UserIdsSearchCriteria)
        {
            return new UserIdsMatcher();
        } else if (criteria instanceof FirstNameSearchCriteria)
        {
            return new FirstNameMatcher();
        } else if (criteria instanceof LastNameSearchCriteria)
        {
            return new LastNameMatcher();
        } else if (criteria instanceof EmailSearchCriteria)
        {
            return new EmailMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }
    
    private class UserIdMatcher extends StringFieldMatcher<PersonPE>
    {
        
        @Override
        protected String getFieldValue(PersonPE object)
        {
            return object.getUserId();
        }
        
    }
    
    private class UserIdsMatcher extends SimpleFieldMatcher<PersonPE>
    {
        @Override
        protected boolean isMatching(IOperationContext context, PersonPE person, ISearchCriteria criteria)
        {
            Collection<String> userIds = ((UserIdsSearchCriteria) criteria).getFieldValue();
            if (userIds == null || userIds.isEmpty())
            {
                return true;
            }
            
            for (String userId : userIds)
            {
                if (person.getUserId().equals(userId))
                {
                    return true;
                }
            }
            return false;
        }
    }

    private class FirstNameMatcher extends StringFieldMatcher<PersonPE>
    {

        @Override
        protected String getFieldValue(PersonPE object)
        {
            return object.getFirstName();
        }

    }

    private class LastNameMatcher extends StringFieldMatcher<PersonPE>
    {

        @Override
        protected String getFieldValue(PersonPE object)
        {
            return object.getLastName();
        }

    }

    private class EmailMatcher extends StringFieldMatcher<PersonPE>
    {

        @Override
        protected String getFieldValue(PersonPE object)
        {
            return object.getEmail();
        }

    }

}
