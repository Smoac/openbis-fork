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

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;

/**
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public class DetailedSearchCriterion implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private DetailedSearchField field;

    private String value;

    public DetailedSearchCriterion()
    {
    }

    public DetailedSearchCriterion(DetailedSearchField field, String value)
    {
        this.field = field;
        this.value = value;
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
