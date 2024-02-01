/*
 * Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.importer.data.ImportValue")
public class ImportValue implements Serializable
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String name;

    @JsonProperty
    private String value;

    @SuppressWarnings("unused")
    public ImportValue()
    {
    }

    public ImportValue(final String name, final String value)
    {
        this.name = name;
        this.value = value;
    }

    @JsonIgnore
    public String getName()
    {
        return name;
    }

    @JsonIgnore
    public void setName(final String name)
    {
        this.name = name;
    }

    @JsonIgnore
    public String getValue()
    {
        return value;
    }

    @JsonIgnore
    public void setValue(final String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        final ObjectToString builder = new ObjectToString(this).append("name", name);
        if (value == null)
        {
            builder.append("value", null);
        } else if (value.length() <= 255)
        {
            builder.append("value", value);
        } else
        {
            builder.append("value", value.substring(0, 255) + "...");
        }
        return builder.toString();
    }

}
