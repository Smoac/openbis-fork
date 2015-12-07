/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.search;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.search.NoSampleContainerSearchCriteria;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.search.NoSampleSearchCriteria;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.search.SampleChildrenSearchCriteria;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.search.SampleContainerSearchCriteria;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.search.SampleParentsSearchCriteria;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.search.SampleSearchCriteria;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AssociatedEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * @author pkupczyk
 */
public class SampleSearchCriteriaTranslator extends AbstractCompositeSearchCriteriaTranslator
{

    protected SampleSearchCriteriaTranslator(IDAOFactory idaoFactory, IEntityAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(idaoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriteria criteria)
    {
        return criteria instanceof SampleSearchCriteria || criteria instanceof NoSampleSearchCriteria;
    }

    @Override
    protected SearchCriteriaTranslationResult doTranslate(SearchTranslationContext context, ISearchCriteria criteria)
    {
        if (criteria instanceof NoSampleSearchCriteria)
        {
            AssociatedEntityKind entityKind;

            if (criteria instanceof NoSampleContainerSearchCriteria)
            {
                entityKind = AssociatedEntityKind.SAMPLE_CONTAINER;
            } else
            {
                entityKind = AssociatedEntityKind.SAMPLE;
            }

            return new SearchCriteriaTranslationResult(new DetailedSearchSubCriteria(entityKind, null));
        } else
        {
            context.pushEntityKind(EntityKind.SAMPLE);
            SearchCriteriaTranslationResult translationResult = super.doTranslate(context, criteria);
            context.popEntityKind();

            if (criteria instanceof SampleSearchCriteria && context.peekEntityKind() == null)
            {
                return translationResult;
            }

            AssociatedEntityKind entityKind;
            if (criteria instanceof SampleParentsSearchCriteria)
            {
                entityKind = AssociatedEntityKind.SAMPLE_PARENT;
            } else if (criteria instanceof SampleChildrenSearchCriteria)
            {
                entityKind = AssociatedEntityKind.SAMPLE_CHILD;
            } else if (criteria instanceof SampleContainerSearchCriteria)
            {
                entityKind = AssociatedEntityKind.SAMPLE_CONTAINER;
            } else if (criteria instanceof SampleSearchCriteria)
            {
                entityKind = AssociatedEntityKind.SAMPLE;
            } else
            {
                throw new IllegalArgumentException("Unknown criteria: " + criteria);
            }
            DetailedSearchSubCriteria subCriteria = new DetailedSearchSubCriteria(entityKind, translationResult.getCriteria());
            return new SearchCriteriaTranslationResult(subCriteria);
        }
    }

}
