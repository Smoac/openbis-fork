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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.common.collections.CollectionUtils;
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
    private final List<Long> ids;

    public static DynamicPropertyEvaluationOperation evaluate(
            Class<? extends IEntityInformationWithPropertiesHolder> clazz, List<Long> ids)
    {
        return new DynamicPropertyEvaluationOperation(clazz, ids);
    }

    public static DynamicPropertyEvaluationOperation evaluateAll(
            Class<? extends IEntityInformationWithPropertiesHolder> clazz)
    {
        return new DynamicPropertyEvaluationOperation(clazz, null);
    }

    private DynamicPropertyEvaluationOperation(
            Class<? extends IEntityInformationWithPropertiesHolder> clazz, List<Long> ids)
    {
        this.className = clazz.getName();
        this.ids = ids;
    }

    public String getClassName()
    {
        return className;
    }

    public List<Long> getIds()
    {
        return ids;
    }

    @Override
    public String toString()
    {
        return className + ": " + (ids == null ? "all" : CollectionUtils.abbreviate(ids, 10));
    }

}
