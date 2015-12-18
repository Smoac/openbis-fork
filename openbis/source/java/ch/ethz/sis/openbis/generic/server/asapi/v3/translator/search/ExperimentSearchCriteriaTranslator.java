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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.NoExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AssociatedEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * @author pkupczyk
 */
public class ExperimentSearchCriteriaTranslator extends AbstractCompositeSearchCriteriaTranslator
{

    protected ExperimentSearchCriteriaTranslator(IDAOFactory idaoFactory, IEntityAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(idaoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriteria criteria)
    {
        return criteria instanceof ExperimentSearchCriteria || criteria instanceof NoExperimentSearchCriteria;
    }

    @Override
    protected SearchCriteriaTranslationResult doTranslate(SearchTranslationContext context, ISearchCriteria criteria)
    {
        if (criteria instanceof NoExperimentSearchCriteria)
        {
            return new SearchCriteriaTranslationResult(new DetailedSearchSubCriteria(AssociatedEntityKind.EXPERIMENT, null));
        } else
        {
            context.pushEntityKind(EntityKind.EXPERIMENT);
            SearchCriteriaTranslationResult translationResult = super.doTranslate(context, criteria);
            context.popEntityKind();

            if (context.peekEntityKind() == null)
            {
                return translationResult;
            } else
            {
                return new SearchCriteriaTranslationResult(new DetailedSearchSubCriteria(AssociatedEntityKind.EXPERIMENT,
                        translationResult.getCriteria()));
            }
        }
    }
}
