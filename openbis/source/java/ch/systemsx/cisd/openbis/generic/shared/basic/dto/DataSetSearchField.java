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
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Full specification of the field connected with the data set which can be used in text queries.
 * 
 * @author Tomasz Pylak
 */
public class DataSetSearchField implements IsSerializable, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private DataSetSearchFieldKind kind;

    private String propertyCodeOrNull;

    private List<String> allDataSetPropertyCodesOrNull;

    public static DataSetSearchField createAnyField(List<String> allDataSetPropertyCodes)
    {
        return new DataSetSearchField(DataSetSearchFieldKind.ANY_FIELD, null,
                allDataSetPropertyCodes);
    }

    public static DataSetSearchField createAnyDataSetProperty(List<String> allDataSetPropertyCodes)
    {
        return new DataSetSearchField(DataSetSearchFieldKind.ANY_DATA_SET_PROPERTY, null,
                allDataSetPropertyCodes);
    }

    public static DataSetSearchField createDataSetProperty(String propertyCode)
    {
        return new DataSetSearchField(DataSetSearchFieldKind.DATA_SET_PROPERTY, propertyCode);
    }

    public static DataSetSearchField createSimpleField(DataSetSearchFieldKind fieldKind)
    {
        assert fieldKind.isComplex() == false : "only simple field can be created with this method";

        return new DataSetSearchField(fieldKind, null);
    }

    // GWT only
    private DataSetSearchField()
    {
        this(null, null);
    }

    private DataSetSearchField(DataSetSearchFieldKind kind, String propertyCodeOrNull)
    {
        this(kind, propertyCodeOrNull, null);
    }

    private DataSetSearchField(DataSetSearchFieldKind kind, String propertyCodeOrNull,
            List<String> allDataSetPropertyCodesOrNull)
    {
        this.kind = kind;
        this.propertyCodeOrNull = propertyCodeOrNull;
        this.allDataSetPropertyCodesOrNull = allDataSetPropertyCodesOrNull;
    }

    public DataSetSearchFieldKind getKind()
    {
        return kind;
    }

    public String getPropertyCode()
    {
        assert kind == DataSetSearchFieldKind.DATA_SET_PROPERTY;
        return propertyCodeOrNull;
    }

    public List<String> getAllDataSetPropertyCodesOrNull()
    {
        assert kind == DataSetSearchFieldKind.ANY_DATA_SET_PROPERTY
                || kind == DataSetSearchFieldKind.ANY_FIELD;
        return allDataSetPropertyCodesOrNull;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getKind());
        if (getKind().equals(DataSetSearchFieldKind.DATA_SET_PROPERTY))
        {
            sb.append(".");
            sb.append(getPropertyCode());
        }
        return sb.toString();
    }
}