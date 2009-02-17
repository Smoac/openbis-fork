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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Kinds of fields connected with the data set which can be used in text queries.
 * 
 * @author Tomasz Pylak
 */
public enum DataSetSearchFieldKind implements IsSerializable
{
    ANY_FIELD("Any Field", true),

    ANY_EXPERIMENT_PROPERTY("Any Experiment Property", true),

    ANY_SAMPLE_PROPERTY("Any Sample Property", true),

    DATA_SET_CODE("Data Set Code"),

    DATA_SET_TYPE("Data Set Type"),

    EXPERIMENT("Experiment Code"),

    EXPERIMENT_TYPE("Experiment Type"),

    FILE_TYPE("File Type"),

    GROUP("Group Code"),

    PROJECT("Project Code"),

    SAMPLE("Sample Code"),

    SAMPLE_TYPE("Sample Type"),

    EXPERIMENT_PROPERTY("Experiment Property", true),

    SAMPLE_PROPERTY("Sample Property", true);

    private final String description;

    // if field is complex, it needs some additional information to be interpreted (e.g.
    // property code)
    private final boolean isComplex;

    private DataSetSearchFieldKind(String description)
    {
        this(description, false);
    }

    private DataSetSearchFieldKind(String description, boolean isComplex)
    {
        this.description = description;
        this.isComplex = isComplex;
    }

    public String description()
    {
        return description;
    }

    public static List<DataSetSearchFieldKind> getSimpleFields()
    {
        List<DataSetSearchFieldKind> result = new ArrayList<DataSetSearchFieldKind>();
        for (DataSetSearchFieldKind field : DataSetSearchFieldKind.values())
        {
            if (field.isComplex == false)
            {
                result.add(field);

            }
        }
        return result;
    }

    boolean isComplex()
    {
        return isComplex;
    }
}