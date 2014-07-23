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

import java.text.SimpleDateFormat;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.AbstractDateObjectValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.AbstractDateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.DateEarlierThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.DateEqualToValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.DateFieldSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.DateLaterThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.DateObjectEarlierThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.DateObjectEqualToValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.DateObjectLaterThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.IDate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ISearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ITimeZone;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.LongDateFormat;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ServerTimeZone;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.TimeZone;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CompareType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;

/**
 * @author pkupczyk
 */
public class DateFieldSearchCriterionTranslator extends AbstractFieldSearchCriterionTranslator
{

    public DateFieldSearchCriterionTranslator(IDAOFactory daoFactory, IEntityAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriterion criterion)
    {
        return criterion instanceof DateFieldSearchCriterion;
    }

    @Override
    protected SearchCriterionTranslationResult doTranslate(SearchTranslationContext context, ISearchCriterion criterion)
    {
        DateFieldSearchCriterion dateCriterion = (DateFieldSearchCriterion) criterion;

        DetailedSearchField detailedSearchField = getDetailedSearchField(context, dateCriterion);
        CompareType compareType = getCompareType(dateCriterion.getFieldValue());
        String value = getValue(dateCriterion.getFieldValue());
        String timeZone = getTimeZone(dateCriterion.getTimeZone());

        return new SearchCriterionTranslationResult(new DetailedSearchCriterion(detailedSearchField, compareType, value, timeZone));
    }

    private CompareType getCompareType(IDate value)
    {
        if (value instanceof DateObjectEqualToValue || value instanceof DateEqualToValue)
        {
            return CompareType.EQUALS;
        } else if (value instanceof DateObjectEarlierThanOrEqualToValue || value instanceof DateEarlierThanOrEqualToValue)
        {
            return CompareType.LESS_THAN_OR_EQUAL;
        } else if (value instanceof DateObjectLaterThanOrEqualToValue || value instanceof DateLaterThanOrEqualToValue)
        {
            return CompareType.MORE_THAN_OR_EQUAL;
        } else
        {
            throw new IllegalArgumentException("Unknown date field value: " + value);
        }
    }

    private String getValue(IDate value)
    {
        if (value instanceof AbstractDateValue)
        {
            return ((AbstractDateValue) value).getValue();
        } else if (value instanceof AbstractDateObjectValue)
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat(new LongDateFormat().getFormat());
            dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
            return dateFormat.format(((AbstractDateObjectValue) value).getValue());
        } else
        {
            throw new IllegalArgumentException("Unknown date field value: " + value);
        }
    }

    private String getTimeZone(ITimeZone timeZone)
    {
        if (timeZone instanceof ServerTimeZone)
        {
            return DetailedSearchCriterion.SERVER_TIMEZONE;
        } else if (timeZone instanceof TimeZone)
        {
            return String.valueOf(((TimeZone) timeZone).getHourOffset());
        } else
        {
            throw new IllegalArgumentException("Unknown date field time zone: " + timeZone);
        }
    }
}
