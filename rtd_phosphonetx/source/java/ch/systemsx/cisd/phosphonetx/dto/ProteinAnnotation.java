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

package ch.systemsx.cisd.phosphonetx.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@XmlType
public class ProteinAnnotation
{
    private String description;
    private String ipiName;

    @XmlAttribute(name = "protein_description", required = true)
    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(String description)
    {
        this.description = description;
    }

    @XmlAttribute(name = "ipi_name")
    public final String getIpiName()
    {
        return ipiName;
    }

    public final void setIpiName(String ipiName)
    {
        this.ipiName = ipiName;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Annotation[description=").append(description);
        if (ipiName != null)
        {
            builder.append(", ipiName=").append(ipiName);
        }
        builder.append("]");
        return builder.toString();
    }
}
