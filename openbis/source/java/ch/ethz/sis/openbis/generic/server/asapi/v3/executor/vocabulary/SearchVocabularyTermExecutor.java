/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary;

import java.util.List;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyCodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermCodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.AbstractSearchObjectManuallyExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchVocabularyTermExecutor extends AbstractSearchObjectManuallyExecutor<VocabularyTermSearchCriteria, VocabularyTermPE> implements
        ISearchVocabularyTermExecutor
{

    @Override
    protected List<VocabularyTermPE> listAll()
    {
        return daoFactory.getVocabularyTermDAO().listAllEntities();
    }

    @Override
    protected Matcher getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof PermIdSearchCriteria)
        {
            return new PermIdMatcher();
        } else if (criteria instanceof VocabularyCodeSearchCriteria)
        {
            return new VocabularyCodeMatcher();
        } else if (criteria instanceof VocabularyTermCodeSearchCriteria)
        {
            return new VocabularyTermCodeMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private class IdMatcher extends SimpleFieldMatcher
    {

        @Override
        protected boolean isMatching(IOperationContext context, VocabularyTermPE object, ISearchCriteria criteria)
        {
            Object id = ((IdSearchCriteria<?>) criteria).getId();

            if (id == null)
            {
                return true;
            } else if (id instanceof VocabularyTermPermId)
            {
                VocabularyTermPermId termId = (VocabularyTermPermId) id;
                return object.getCode().equals(termId.getCode()) && object.getVocabulary().getCode().equals(termId.getVocabularyCode());
            } else
            {
                throw new IllegalArgumentException("Unknown id: " + id.getClass());
            }
        }

    }

    private class PermIdMatcher extends StringFieldMatcher
    {

        @Override
        protected String getFieldValue(VocabularyTermPE object)
        {
            return new VocabularyTermPermId(object.getCode(), object.getVocabulary().getCode()).toString();
        }

    }

    private class VocabularyCodeMatcher extends StringFieldMatcher
    {

        @Override
        protected String getFieldValue(VocabularyTermPE object)
        {
            return object.getVocabulary().getCode();
        }

    }

    private class VocabularyTermCodeMatcher extends StringFieldMatcher
    {

        @Override
        protected String getFieldValue(VocabularyTermPE object)
        {
            return object.getCode();
        }

    }

}
