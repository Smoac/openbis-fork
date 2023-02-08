/*
 * Copyright 2017 ETH Zuerich, SIS
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

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.SearchPersonsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.SearchPersonsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.person.IPersonTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class SearchPersonsOperationExecutor
        extends SearchObjectsPEOperationExecutor<Person, PersonPE, PersonSearchCriteria, PersonFetchOptions>
        implements ISearchPersonsOperationExecutor
{
    @Autowired
    private ISearchPersonExecutor searchExecutor;
    
    @Autowired
    private IPersonTranslator translator;
    
    @Override
    protected ISearchObjectExecutor<PersonSearchCriteria, PersonPE> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, Person, PersonFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<Person> getOperationResult(SearchResult<Person> searchResult)
    {
        return new SearchPersonsOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<PersonSearchCriteria, Person, Long> getSearchManager() {
        throw new RuntimeException("This method is not implemented yet.");
    }

    @Override
    protected Class<? extends SearchObjectsOperation<PersonSearchCriteria, PersonFetchOptions>> getOperationClass()
    {
        return SearchPersonsOperation.class;
    }

}
