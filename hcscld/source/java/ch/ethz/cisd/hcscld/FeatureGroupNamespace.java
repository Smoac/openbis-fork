/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.ethz.cisd.hcscld;

/**
 * A class that represents a namespace for a feature group.
 *
 * @author Bernd Rinn
 */
public final class FeatureGroupNamespace
{
    /**
     * The kind of namespace of a feature group.
     * 
     * @author Bernd Rinn
     */
    public enum FeatureNamespaceKind
    {
        /**
         * Name space is an object type.
         */
        OBJECT_TYPE,
        /**
         * Name space is a companion group.
         */
        COMPANION_GROUP
    }    
    
    private final String id;

    private final FeatureNamespaceKind kind;

    FeatureGroupNamespace(String id, FeatureNamespaceKind kind)
    {
        this.id = id.toUpperCase();
        this.kind = kind;
    }

    /**
     * Returns the id of the namespace. Only unique together with the kind ({@link #getKind()}.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Returns the kind of namespace. May be a object type or companion group.
     */
    public FeatureNamespaceKind getKind()
    {
        return kind;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((kind == null) ? 0 : kind.hashCode());
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
        FeatureGroupNamespace other = (FeatureGroupNamespace) obj;
        if (id == null)
        {
            if (other.id != null)
            {
                return false;
            }
        } else if (!id.equals(other.id))
        {
            return false;
        }
        if (kind != other.kind)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "FeatureGroupNamespace [id=" + id + ", kind=" + kind + "]";
    }

}