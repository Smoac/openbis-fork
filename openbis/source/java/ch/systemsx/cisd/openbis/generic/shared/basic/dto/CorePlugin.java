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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

/**
 * Represents a versioned openBIS core plugin.
 * 
 * @author Kaloyan Enimanev
 */
public class CorePlugin implements Comparable<CorePlugin>
{
    private final String name;

    private final int version;

    public CorePlugin(String name, int version)
    {
        this.name = name;
        this.version = version;
    }

    public String getName()
    {
        return name;
    }

    public int getVersion()
    {
        return version;
    }

    public int compareTo(CorePlugin other)
    {
        int result = name.compareTo(other.name);
        if (result == 0)
        {
            result = this.version - other.version;
        }
        return result;
    }

    @Override
    public String toString()
    {
        return String.format("Core Plugin[name='%s', version='%s']", name, version);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + version;
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
        CorePlugin other = (CorePlugin) obj;
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
        if (version != other.version)
        {
            return false;
        }
        return true;
    }

}
