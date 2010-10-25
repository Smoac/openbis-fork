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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.common.collections.CollectionUtils;

/**
 * Encapsulates operation kind and data for an update operation to be performed on an index.
 * 
 * @author Piotr Buczek
 */
public class IndexUpdateOperation implements Serializable
{
    public enum IndexUpdateOperationKind
    {
        /** update indexed entity */
        REINDEX,

        /** remove from index */
        REMOVE
    }

    private static final long serialVersionUID = 2L;

    // we don't store Class<?> not to cause problems with deserialization
    private final String className;

    private final List<Long> ids;

    private final IndexUpdateOperationKind operationKind;

    public static IndexUpdateOperation remove(Class<?> clazz, List<Long> ids)
    {
        return new IndexUpdateOperation(IndexUpdateOperationKind.REMOVE, clazz, ids);
    }

    public static IndexUpdateOperation reindex(Class<?> clazz, List<Long> ids)
    {
        return new IndexUpdateOperation(IndexUpdateOperationKind.REINDEX, clazz, ids);
    }

    private IndexUpdateOperation(IndexUpdateOperationKind operationKind, Class<?> clazz,
            List<Long> ids)
    {
        this.className = clazz.getName();
        this.ids = ids;
        this.operationKind = operationKind;
    }

    public String getClassName()
    {
        return className;
    }

    public List<Long> getIds()
    {
        return ids;
    }

    public IndexUpdateOperationKind getOperationKind()
    {
        return operationKind;
    }

    @Override
    public String toString()
    {
        return operationKind + " " + className + ": "
                + (ids == null ? "all" : CollectionUtils.abbreviate(ids, 10));
    }

}
