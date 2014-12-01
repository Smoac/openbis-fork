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

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.AnyStringValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ISearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.StringContainsValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.StringEndsWithValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.StringEqualToValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.StringFieldSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.StringStartsWithValue;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;

/**
 * @author pkupczyk
 */
public class StringFieldSearchCriterionTranslator extends AbstractFieldSearchCriterionTranslator
{

    public StringFieldSearchCriterionTranslator(IDAOFactory daoFactory, IEntityAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriterion criterion)
    {
        return criterion instanceof StringFieldSearchCriterion;
    }

    @Override
    protected SearchCriterionTranslationResult doTranslate(SearchTranslationContext context, ISearchCriterion criterion)
    {
        StringFieldSearchCriterion stringCriterion = (StringFieldSearchCriterion) criterion;

        AbstractStringValue valueObject = stringCriterion.getFieldValue();
        String value;

        if (valueObject instanceof StringEqualToValue)
        {
            StringEqualToValue equalToValue = (StringEqualToValue) valueObject;
            value = equalToValue.getValue();
        } else if (valueObject instanceof StringStartsWithValue)
        {
            StringStartsWithValue startsWithValue = (StringStartsWithValue) valueObject;
            value = startsWithValue.getValue() + "*";
        } else if (valueObject instanceof StringEndsWithValue)
        {
            StringEndsWithValue endsWithValue = (StringEndsWithValue) valueObject;
            value = "*" + endsWithValue.getValue();
        } else if (valueObject instanceof StringContainsValue)
        {
            StringContainsValue endsWithValue = (StringContainsValue) valueObject;
            value = "*" + endsWithValue.getValue() + "*";
        } else if (valueObject instanceof AnyStringValue)
        {
            value = "*";
        } else if (valueObject != null)
        {
            throw new IllegalArgumentException("Unknown string field value: " + valueObject);
        } else
        {
            throw new IllegalArgumentException("Unspecified value of criterion '" + criterion + "'.");
        }

        return new SearchCriterionTranslationResult(new DetailedSearchCriterion(getDetailedSearchField(context, stringCriterion), value));
    }

}
