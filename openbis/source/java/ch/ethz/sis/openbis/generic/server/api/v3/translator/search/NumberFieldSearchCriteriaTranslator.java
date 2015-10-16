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

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.AbstractNumberValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.NumberEqualToValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.NumberFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.NumberGreaterOrEqualThanValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.NumberGreaterThanValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.NumberLessOrEqualThanValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.NumberLessThanValue;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CompareType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;

/**
 * @author pkupczyk
 */
public class NumberFieldSearchCriteriaTranslator extends AbstractFieldSearchCriteriaTranslator
{

    public NumberFieldSearchCriteriaTranslator(IDAOFactory daoFactory, IEntityAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriteria criteria)
    {
        return criteria instanceof NumberFieldSearchCriteria;
    }

    @Override
    protected SearchCriteriaTranslationResult doTranslate(SearchTranslationContext context, ISearchCriteria criteria)
    {
        NumberFieldSearchCriteria numberCriteria = (NumberFieldSearchCriteria) criteria;
        AbstractNumberValue valueObject = numberCriteria.getFieldValue();

        if (valueObject instanceof NumberLessThanValue)
        {
            NumberLessThanValue lessToValue = (NumberLessThanValue) valueObject;
            return new SearchCriteriaTranslationResult(new DetailedSearchCriterion(getDetailedSearchField(context, numberCriteria), CompareType.LESS_THAN, lessToValue.getValue()));
        } else if (valueObject instanceof NumberLessOrEqualThanValue)
        {
            NumberLessOrEqualThanValue lessOrEqualToValue = (NumberLessOrEqualThanValue) valueObject;
            return new SearchCriteriaTranslationResult(new DetailedSearchCriterion(getDetailedSearchField(context, numberCriteria), CompareType.LESS_THAN_OR_EQUAL, lessOrEqualToValue.getValue()));
        } else if (valueObject instanceof NumberEqualToValue)
        {
            NumberEqualToValue equalToValue = (NumberEqualToValue) valueObject;
            return new SearchCriteriaTranslationResult(new DetailedSearchCriterion(getDetailedSearchField(context, numberCriteria), CompareType.EQUALS, equalToValue.getValue()));
        } else if (valueObject instanceof NumberGreaterOrEqualThanValue)
        {
            NumberGreaterOrEqualThanValue greaterOrEqualToValue = (NumberGreaterOrEqualThanValue) valueObject;
            return new SearchCriteriaTranslationResult(new DetailedSearchCriterion(getDetailedSearchField(context, numberCriteria), CompareType.MORE_THAN_OR_EQUAL, greaterOrEqualToValue.getValue()));
        } else if (valueObject instanceof NumberGreaterThanValue)
        {
            NumberGreaterThanValue greaterToValue = (NumberGreaterThanValue) valueObject;
            return new SearchCriteriaTranslationResult(new DetailedSearchCriterion(getDetailedSearchField(context, numberCriteria), CompareType.MORE_THAN, greaterToValue.getValue()));
        } else
        {
            throw new IllegalArgumentException("Unknown number field value: " + valueObject);
        }
    }

}
