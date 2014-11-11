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

import java.util.ArrayList;
import java.util.List;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.AbstractCompositeSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.AbstractEntitySearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ISearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SearchOperator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;

/**
 * @author pkupczyk
 */
public abstract class AbstractCompositeSearchCriterionTranslator extends AbstractSearchCriterionTranslator
{

    private IDAOFactory daoFactory;

    private IEntityAttributeProviderFactory entityAttributeProviderFactory;

    protected AbstractCompositeSearchCriterionTranslator(IDAOFactory daoFactory, IEntityAttributeProviderFactory entityAttributeProviderFactory)
    {
        this.daoFactory = daoFactory;
        this.entityAttributeProviderFactory = entityAttributeProviderFactory;
    }

    @Override
    protected SearchCriterionTranslationResult doTranslate(SearchTranslationContext context, ISearchCriterion criterion)
    {
        AbstractCompositeSearchCriterion compositeCriterion = (AbstractCompositeSearchCriterion) criterion;

        DetailedSearchCriteria detailedSearchCriteria = new DetailedSearchCriteria();
        detailedSearchCriteria.setUseWildcardSearchMode(true);

        if (criterion instanceof AbstractEntitySearchCriterion)
        {
            detailedSearchCriteria.setConnection(translateOperator(((AbstractEntitySearchCriterion<?>) compositeCriterion).getOperator()));
        }

        List<DetailedSearchCriterion> detailedCriterionList = new ArrayList<DetailedSearchCriterion>();
        List<DetailedSearchSubCriteria> detailedSubCriteriaList = new ArrayList<DetailedSearchSubCriteria>();

        for (ISearchCriterion subCriterion : compositeCriterion.getCriteria())
        {
            ISearchCriterionTranslator subTranslator =
                    new SearchCriterionTranslatorFactory(getDaoFactory(), getEntityAttributeProviderFactory()).getTranslator(subCriterion);

            SearchCriterionTranslationResult translationResult = subTranslator.translate(context, subCriterion);

            if (translationResult == null)
            {
                continue;
            } else
            {
                detailedCriterionList.addAll(translationResult.getCriterionList());
                detailedSubCriteriaList.addAll(translationResult.getSubCriteriaList());
            }
        }

        detailedSearchCriteria.setCriteria(detailedCriterionList);
        detailedSearchCriteria.setSubCriterias(detailedSubCriteriaList);
        return new SearchCriterionTranslationResult(detailedSearchCriteria);
    }

    private SearchCriteriaConnection translateOperator(SearchOperator operator)
    {
        switch (operator)
        {
            case AND:
                return SearchCriteriaConnection.MATCH_ALL;
            case OR:
                return SearchCriteriaConnection.MATCH_ANY;

        }
        throw new IllegalArgumentException("Unknown search operator: " + operator);
    }

    public IDAOFactory getDaoFactory()
    {
        return daoFactory;
    }

    public IEntityAttributeProviderFactory getEntityAttributeProviderFactory()
    {
        return entityAttributeProviderFactory;
    }

}
