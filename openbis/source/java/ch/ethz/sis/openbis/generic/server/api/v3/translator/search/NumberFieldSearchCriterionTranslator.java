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
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ISearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.NumberEqualToValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.NumberFieldSearchCriterion;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;

/**
 * @author pkupczyk
 */
public class NumberFieldSearchCriterionTranslator extends AbstractFieldSearchCriterionTranslator
{

    public NumberFieldSearchCriterionTranslator(IDAOFactory daoFactory, IEntityAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriterion criterion)
    {
        return criterion instanceof NumberFieldSearchCriterion;
    }

    @Override
    protected SearchCriterionTranslationResult doTranslate(SearchTranslationContext context, ISearchCriterion criterion)
    {
        NumberFieldSearchCriterion numberCriterion = (NumberFieldSearchCriterion) criterion;
        AbstractNumberValue valueObject = numberCriterion.getFieldValue();

        if (valueObject instanceof NumberEqualToValue)
        {
            NumberEqualToValue equalToValue = (NumberEqualToValue) valueObject;
            return new SearchCriterionTranslationResult(new DetailedSearchCriterion(getDetailedSearchField(context, numberCriterion), equalToValue.getValue()
                    .toString()));
        } else
        {
            throw new IllegalArgumentException("Unknown number field value: " + valueObject);
        }

    }

}
