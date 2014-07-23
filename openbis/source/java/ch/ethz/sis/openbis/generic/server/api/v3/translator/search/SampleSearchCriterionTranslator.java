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

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ISearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleChildrenSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleContainerSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleParentsSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleSearchCriterion;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AssociatedEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * @author pkupczyk
 */
public class SampleSearchCriterionTranslator extends AbstractCompositeSearchCriterionTranslator
{

    protected SampleSearchCriterionTranslator(IDAOFactory idaoFactory, IEntityAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(idaoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriterion criterion)
    {
        return criterion instanceof SampleSearchCriterion;
    }

    @Override
    protected SearchCriterionTranslationResult doTranslate(SearchTranslationContext context, ISearchCriterion criterion)
    {
        context.pushEntityKind(EntityKind.SAMPLE);

        SearchCriterionTranslationResult translationResult = super.doTranslate(context, criterion);

        context.popEntityKind();

        if (criterion instanceof SampleParentsSearchCriterion)
        {
            return new SearchCriterionTranslationResult(
                    new DetailedSearchSubCriteria(AssociatedEntityKind.SAMPLE_PARENT, translationResult.getCriteria()));
        } else if (criterion instanceof SampleChildrenSearchCriterion)
        {
            return new SearchCriterionTranslationResult(new DetailedSearchSubCriteria(AssociatedEntityKind.SAMPLE_CHILD, translationResult.getCriteria()));
        } else if (criterion instanceof SampleContainerSearchCriterion)
        {
            return new SearchCriterionTranslationResult(new DetailedSearchSubCriteria(AssociatedEntityKind.SAMPLE_CONTAINER,
                    translationResult.getCriteria()));
        } else if (criterion instanceof SampleSearchCriterion)
        {
            return translationResult;
        } else
        {
            throw new IllegalArgumentException("Unknown criterion: " + criterion);
        }
    }

}
