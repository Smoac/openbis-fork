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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A class to represent a namespace for segmented objects.
 * 
 * @author Bernd Rinn
 */
public class ObjectNamespace implements IId
{
    private final File file;

    private final String datasetCode;

    private final String id;

    private final Set<ObjectType> objectTypes;

    private int numberOfSegmentedObjects;

    ObjectNamespace(File file, String datasetCode, String id)
    {
        this(file, datasetCode, id, new HashSet<ObjectType>());
    }

    ObjectNamespace(File file, String datasetCode, String id, Set<ObjectType> objectTypes)
    {
        this.file = file;
        this.datasetCode = datasetCode;
        this.id = id.toUpperCase();
        this.numberOfSegmentedObjects = -1;
        for (ObjectType c : objectTypes)
        {
            if (c == null)
            {
                continue;
            }
            if (isSameDataset(c) == false)
            {
                throw new WrongDatasetException(datasetCode, c.getDatasetCode());
            }
            c.setObjectNamespace(this);
        }
        this.objectTypes = objectTypes;
    }

    public File getFile()
    {
        return file;
    }

    public String getDatasetCode()
    {
        return datasetCode;
    }

    public String getId()
    {
        return id;
    }

    void add(ObjectType objectType)
    {
        if (isSameDataset(objectType) == false)
        {
            throw new WrongDatasetException(datasetCode, objectType.getDatasetCode());
        }
        objectType.setObjectNamespace(this);
        objectTypes.add(objectType);
    }

    public Set<ObjectType> getObjectTypes()
    {
        return Collections.unmodifiableSet(objectTypes);
    }

    /**
     * Returns the number of elements that a segmentation of this namespace has, or -1, if not
     * yet known.
     */
    public int getNumberOfSegmentedObjects()
    {
        return numberOfSegmentedObjects;
    }

    void setOrCheckNumberOfSegmentedObjects(int numberOfSegmentedObjects)
    {
        if (this.numberOfSegmentedObjects == -1)
        {
            this.numberOfSegmentedObjects = numberOfSegmentedObjects;
        } else if (this.numberOfSegmentedObjects != numberOfSegmentedObjects)
        {
            throw new WrongNumberOfSegmentedObjectsException(datasetCode,
                    this.numberOfSegmentedObjects, numberOfSegmentedObjects);
        }
    }

    /**
     * Returns <code>true</code> if <var>other</var> is an object type of the same dataset.
     */
    boolean isSameDataset(ObjectType other)
    {
        return other.getFile().equals(file) && other.getDatasetCode().equals(datasetCode);
    }

    @Override
    public String toString()
    {
        return "ObjectNamespace [datasetCode=" + datasetCode + ", id=" + id + "]";
    }

}
