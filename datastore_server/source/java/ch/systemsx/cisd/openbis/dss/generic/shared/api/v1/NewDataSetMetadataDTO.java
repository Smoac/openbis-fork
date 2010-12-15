/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents optional metadata (data set type and properties) of a new data set that the DSS should
 * register. The metadata will override those inferred by the server.
 * 
 * @author Piotr Buczek
 */
public class NewDataSetMetadataDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final Map<String, String> properties = new HashMap<String, String>();

    private String dataSetTypeOrNull;

    public NewDataSetMetadataDTO(String dataSetTypeOrNull, Map<String, String> propertiesOrNull)
    {
        this.dataSetTypeOrNull = dataSetTypeOrNull;
        setProperties(propertiesOrNull);
    }

    public NewDataSetMetadataDTO()
    {
        this(null, null);
    }

    /**
     * The code for the type of the new data set. May be null.
     */
    public String tryDataSetType()
    {
        return dataSetTypeOrNull;
    }

    public void setDataSetTypeOrNull(String dataSetTypeOrNull)
    {
        // Capitalize the data set type
        this.dataSetTypeOrNull =
                (dataSetTypeOrNull != null) ? dataSetTypeOrNull.toUpperCase() : null;
    }

    /**
     * The properties for the new data set. Key is the property code, value is the value (as a
     * string).
     */
    public Map<String, String> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, String> props)
    {
        properties.clear();
        if (props != null)
        {
            properties.putAll(props);
        }
    }

    @Override
    public String toString()
    {
        ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        String myDataSetTypeOrNull = tryDataSetType();
        if (null != myDataSetTypeOrNull)
        {
            sb.append("data set type", myDataSetTypeOrNull);
        }
        for (Entry<String, String> entry : getProperties().entrySet())
        {
            sb.append("property " + entry.getKey(), entry.getValue());
        }
        return sb.toString();
    }
}
