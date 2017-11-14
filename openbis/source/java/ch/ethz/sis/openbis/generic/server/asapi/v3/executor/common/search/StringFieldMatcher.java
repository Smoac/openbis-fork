/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringContainsValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEndsWithValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringStartsWithValue;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;

public abstract class StringFieldMatcher<OBJECT> extends SimpleFieldMatcher<OBJECT>
{

    @Override
    protected boolean isMatching(IOperationContext context, OBJECT object, ISearchCriteria criteria)
    {
        AbstractStringValue fieldValue = ((StringFieldSearchCriteria) criteria).getFieldValue();

        if (fieldValue == null || fieldValue.getValue() == null || fieldValue instanceof AnyStringValue)
        {
            return true;
        }

        String actualValue = getFieldValue(object);

        if (actualValue == null)
        {
            actualValue = "";
        } else
        {
            actualValue = actualValue.toLowerCase();
        }

        String searchedValue = fieldValue.getValue().toLowerCase();

        if (fieldValue instanceof StringEqualToValue)
        {
            return actualValue.equals(searchedValue);
        } else if (fieldValue instanceof StringContainsValue)
        {
            return actualValue.contains(searchedValue);
        } else if (fieldValue instanceof StringStartsWithValue)
        {
            return actualValue.startsWith(searchedValue);
        } else if (fieldValue instanceof StringEndsWithValue)
        {
            return actualValue.endsWith(searchedValue);
        } else
        {
            throw new IllegalArgumentException("Unknown string value: " + criteria.getClass());
        }
    }

    protected abstract String getFieldValue(OBJECT object);

}