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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import ch.systemsx.cisd.common.annotation.BeanProperty;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * @author Izabela Adamczyk
 */
public class NewProperty implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    String property;

    String value;

    public NewProperty()
    {
    }

    public NewProperty(String name, String value)
    {
        this.property = name;
        this.value = value;
    }

    public String getPropertyCode()
    {
        return property;
    }

    @BeanProperty(label = "property", optional = false)
    public void setPropertyCode(String name)
    {
        this.property = name;
    }

    public String getValue()
    {
        return value;
    }

    @BeanProperty(label = "value", optional = false)
    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }

}
