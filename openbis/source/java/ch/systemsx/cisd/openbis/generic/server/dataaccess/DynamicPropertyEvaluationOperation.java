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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;

/**
 * Encapsulates operation kind and data for an update operation.
 * 
 * @author Piotr Buczek
 */
public class DynamicPropertyEvaluationOperation implements Serializable
{
    private static final long serialVersionUID = 1L;

    // we don't store Class<?> not to cause problems with deserialization
    private final String className;

    // null means all
    private final Set<Long> ids;

    public static DynamicPropertyEvaluationOperation evaluate(
            Class<? extends IEntityInformationWithPropertiesHolder> clazz, Collection<Long> ids)
    {
        return new DynamicPropertyEvaluationOperation(clazz, ids);
    }

    public static DynamicPropertyEvaluationOperation evaluateAll(
            Class<? extends IEntityInformationWithPropertiesHolder> clazz)
    {
        return new DynamicPropertyEvaluationOperation(clazz, null);
    }

    private DynamicPropertyEvaluationOperation(
            Class<? extends IEntityInformationWithPropertiesHolder> clazz, Collection<Long> ids)
    {
        this.className = clazz.getName();
        if (ids == null)
        {
            this.ids = null;
        }
        else
        {
            this.ids = new HashSet<Long>();
            this.ids.addAll(ids);
        }
    }

    public String getClassName()
    {
        return className;
    }

    public Set<Long> getIds()
    {
        return ids;
    }

    @Override
    public String toString()
    {
        return className + ": " + (ids == null ? "all" : CollectionUtils.abbreviate(ids, 10));
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((ids == null) ? 0 : ids.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DynamicPropertyEvaluationOperation other = (DynamicPropertyEvaluationOperation) obj;
        if (className == null)
        {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
        {
            return false;
        }
        if (ids == null)
        {
            if (other.ids != null)
            {
                return false;
            }
        } else if (!ids.equals(other.ids))
        {
            return false;
        }
        return true;
    }

}
