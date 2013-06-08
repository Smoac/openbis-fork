/*
 * Copyright 2012-2013 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.hcscld;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A store for {@link ObjectType}s and {@link ObjectNamespace}s.
 * 
 * @author Bernd Rinn
 */
class ObjectTypeStore
{
    /** objectTypeId -> objectType */
    private final Map<String, ObjectType> objectTypes;

    /** namespaceId -> namespace */
    private final Map<String, ObjectNamespace> namespaces;

    private final File cldFile;

    private final String datasetCode;

    ObjectTypeStore(File cldFile, String datasetCode)
    {
        this.objectTypes = new LinkedHashMap<String, ObjectType>();
        this.namespaces = new LinkedHashMap<String, ObjectNamespace>();
        this.cldFile = cldFile;
        this.datasetCode = datasetCode.toUpperCase();
    }

    ObjectType tryGetObjectType(String id)
    {
        return objectTypes.get(id.toUpperCase());
    }

    boolean hasObjectType(String id)
    {
        return objectTypes.containsKey(id.toUpperCase());
    }
    
    boolean hasObjectTypes()
    {
        return objectTypes.isEmpty() == false;
    }

    ObjectType addObjectType(String id, ObjectNamespace group)
            throws UniqueViolationException
    {
        final String idUpperCase = id.toUpperCase();
        if (objectTypes.containsKey(idUpperCase))
        {
            throw new UniqueViolationException("Object type", idUpperCase);
        }
        final ObjectType result = new ObjectType(idUpperCase, cldFile, datasetCode);
        group.add(result);
        objectTypes.put(idUpperCase, result);
        return result;
    }

    ObjectNamespace addObjectNamespace(String id)
            throws UniqueViolationException
    {
        final String idUpperCase = id.toUpperCase();
        if (namespaces.containsKey(idUpperCase))
        {
            throw new UniqueViolationException("Object type namespace", idUpperCase);
        }
        final ObjectNamespace result =
                new ObjectNamespace(cldFile, datasetCode, idUpperCase);
        namespaces.put(idUpperCase, result);
        return result;
    }

    Collection<ObjectType> getObjectTypes()
    {
        return Collections.unmodifiableCollection(objectTypes.values());
    }

    ObjectType[] getObjectTypeArray()
    {
        return objectTypes.values().toArray(new ObjectType[objectTypes.size()]);
    }

    ObjectNamespace tryGetObjectNamespace(String id)
    {
        return namespaces.get(id.toUpperCase());
    }

    Collection<ObjectNamespace> getObjectNamespaces()
    {
        return Collections.unmodifiableCollection(namespaces.values());
    }
    
    boolean hasObjectNamespaces()
    {
        return namespaces.isEmpty() == false;
    }

}
