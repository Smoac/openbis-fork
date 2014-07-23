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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.AbstractCompositeSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ISearchCriterion;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;

/**
 * @author pkupczyk
 */
public abstract class AbstractFieldFromCompositeSearchCriterionTranslator extends AbstractFieldSearchCriterionTranslator
{

    public AbstractFieldFromCompositeSearchCriterionTranslator(IDAOFactory daoFactory, IEntityAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected SearchCriterionTranslationResult doTranslate(SearchTranslationContext context, ISearchCriterion criterion)
    {
        AbstractCompositeSearchCriterion compositeCriterion = (AbstractCompositeSearchCriterion) criterion;
        Collection<ISearchCriterion> subCriteria = compositeCriterion.getCriteria();

        if (subCriteria != null && false == subCriteria.isEmpty())
        {
            List<DetailedSearchCriterion> detailedCriteria = new LinkedList<DetailedSearchCriterion>();

            for (ISearchCriterion subCriterion : subCriteria)
            {
                DetailedSearchCriterion detailedCriterion = doTranslateField(context, compositeCriterion, subCriterion);
                detailedCriteria.add(detailedCriterion);
            }

            return new SearchCriterionTranslationResult(detailedCriteria.toArray(new DetailedSearchCriterion[0]));
        }

        return null;
    }

    protected DetailedSearchCriterion doTranslateField(SearchTranslationContext context, ISearchCriterion criterion, ISearchCriterion subCriterion)
    {
        ISearchCriterionTranslator subTranslator =
                new SearchCriterionTranslatorFactory(getDaoFactory(), getEntityAttributeProviderFactory())
                        .getTranslator(subCriterion);

        SearchCriterionTranslationResult translationResult = subTranslator.translate(context, subCriterion);
        String value = translationResult.getCriterion().getValue();
        DetailedSearchField searchField = doTranslateSearchField(context, criterion, subCriterion);
        DetailedSearchCriterion detailedSearchCriterion = new DetailedSearchCriterion(searchField, value);
        return detailedSearchCriterion;
    }

    protected DetailedSearchField doTranslateSearchField(SearchTranslationContext context, ISearchCriterion criterion,
            ISearchCriterion subCriterion)
    {
        IAttributeSearchFieldKind attribute = getEntityAttributeProvider(context).getAttribute(criterion);
        DetailedSearchField searchField = DetailedSearchField.createAttributeField(attribute);
        return searchField;
    }

}
