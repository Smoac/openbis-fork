/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.utils.SortableNumberBridgeUtils;

/**
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public class DetailedSearchCriterion implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final String SERVER_TIMEZONE = "server";

    private DetailedSearchField field;

    private CompareType type;

    private String value;

    private String timezone;

    public DetailedSearchCriterion()
    {

    }

    public DetailedSearchCriterion(DetailedSearchField field, String value)
    {
        this.field = field;
        this.value = value;
        this.type = CompareType.EQUALS;
    }

    public DetailedSearchCriterion(DetailedSearchField field, CompareType type, String date)
    {
        this(field, type, date, SERVER_TIMEZONE);
    }
    
    public DetailedSearchCriterion(DetailedSearchField field, CompareType type, Number value)
    {
        this.field = field;
        this.value = SortableNumberBridgeUtils.getNumberForLucene((Number) value);
        this.type = type;
    }
    
    public DetailedSearchCriterion(DetailedSearchField field, CompareType type, String value, String timezoneOrNull)
    {
        this.field = field;
        this.value = value;
        this.type = type;
        this.timezone = timezoneOrNull;
    }

    public DetailedSearchField getField()
    {
        return field;
    }

    public void setField(DetailedSearchField field)
    {
        this.field = field;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public CompareType getType()
    {
        return this.type;
    }

    public String getTimeZone()
    {
        return this.timezone;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getField());
        sb.append(": ");
        sb.append(getValue());
        return sb.toString();
    }
}
