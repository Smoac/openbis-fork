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

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A store for {@link ObjectType}s and {@link ObjectTypeCompanionGroup}s.
 * 
 * @author Bernd Rinn
 */
class ObjectTypeStore
{
    /** objectTypeId -> objectType */
    private final Map<String, ObjectType> objectTypes;

    /** companionGroupId -> companionGroup */
    private final Map<String, ObjectTypeCompanionGroup> companionGroups;

    private final File cldFile;

    private final String datasetCode;

    ObjectTypeStore(File cldFile, String datasetCode)
    {
        this.objectTypes = new LinkedHashMap<String, ObjectType>();
        this.companionGroups = new LinkedHashMap<String, ObjectTypeCompanionGroup>();
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

    ObjectType addObjectType(String id, ObjectTypeCompanionGroup group)
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

    ObjectTypeCompanionGroup addObjectTypeCompanionGroup(String id)
            throws UniqueViolationException
    {
        final String idUpperCase = id.toUpperCase();
        if (companionGroups.containsKey(idUpperCase))
        {
            throw new UniqueViolationException("Object type companion group", idUpperCase);
        }
        final ObjectTypeCompanionGroup result =
                new ObjectTypeCompanionGroup(cldFile, datasetCode, idUpperCase);
        companionGroups.put(idUpperCase, result);
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

    ObjectTypeCompanionGroup tryGetObjectTypeCompanionGroup(String id)
    {
        return companionGroups.get(id.toUpperCase());
    }

    Collection<ObjectTypeCompanionGroup> getObjectTypeCompanionGroups()
    {
        return Collections.unmodifiableCollection(companionGroups.values());
    }

}
