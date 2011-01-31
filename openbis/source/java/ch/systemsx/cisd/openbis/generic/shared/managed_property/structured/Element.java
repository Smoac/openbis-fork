/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.managed_property.structured;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElement;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElementAttribute;

/**
 * TODO KE: document
 * 
 * @author Kaloyan Enimanev
 */
public class Element implements IElement
{

    private final String name;

    // TODO KE: no final fields needed
    private final Map<String /* key */, IElementAttribute> attributes =
            new HashMap<String, IElementAttribute>();

    private final List<IElement> children = new ArrayList<IElement>();

    private String data;

    public Element(String name)
    {
        assert Pattern.matches("[a-zA-Z]\\w*", name) : "Element names must be non-emtpy strings "
                + "containing characters from the English alphabet and digits.";
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String getAttribute(String key)
    {
        IElementAttribute attribute = attributes.get(key);
        if (attribute != null)
        {
            return attribute.getValue();
        }
        return null;
    }

    public String getData()
    {
        return data;
    }

    public List<IElement> getChildren()
    {
        return children;
    }

    public List<IElementAttribute> getAttributes()
    {
        // TODO KE: have another datastructure
        return new ArrayList<IElementAttribute>(attributes.values());
    }


    public IElement setAttributes(List<IElementAttribute> newAttributes)
    {
        attributes.clear();
        for (IElementAttribute attribute : newAttributes)
        {
            attributes.put(attribute.getKey(), attribute);
        }
        return this;
    }


    public IElement setChildren(List<IElement> newChildren)
    {
        children.clear();
        children.addAll(newChildren);
        return this;
    }

    public IElement setData(String data)
    {
        this.data = data;
        return this;
    }

    public IElement addChildren(IElement... newChildren)
    {
        for (IElement child : newChildren)
        {
            children.add(child);
        }
        return this;
    }

    public IElement addAttributes(IElementAttribute... newAttributes)
    {
        for (IElementAttribute attribute : newAttributes)
        {
            attributes.put(attribute.getKey(), attribute);
        }
        return this;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + ((children == null) ? 0 : children.hashCode());
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Element other = (Element) obj;
        if (attributes == null)
        {
            if (other.attributes != null)
            {
                return false;
            }
        } else if (!attributes.equals(other.attributes))
        {
            return false;
        }
        if (children == null)
        {
            if (other.children != null)
            {
                return false;
            }
        } else if (!children.equals(other.children))
        {
            return false;
        }
        if (data == null)
        {
            if (other.data != null)
            {
                return false;
            }
        } else if (!data.equals(other.data))
        {
            return false;
        }
        if (name == null)
        {
            if (other.name != null)
            {
                return false;
            }
        } else if (!name.equals(other.name))
        {
            return false;
        }
        return true;
    }
}
