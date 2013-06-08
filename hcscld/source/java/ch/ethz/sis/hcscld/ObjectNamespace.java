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

package ch.ethz.sis.hcscld;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A class to represent a namespace for segmented objects.
 * 
 * @author Bernd Rinn
 */
public class ObjectNamespace implements IId, Comparable<ObjectNamespace>
{
    private final File file;

    private final String datasetCode;

    private final String id;

    private final Set<ObjectType> objectTypes;

    private final Map<ImageId, Integer> numberOfSegmentedObjects;

    ObjectNamespace(File file, String datasetCode, String id)
    {
        this(file, datasetCode, id, new LinkedHashSet<ObjectType>());
    }

    ObjectNamespace(File file, String datasetCode, String id, Set<ObjectType> objectTypes)
    {
        this.file = file;
        this.datasetCode = datasetCode;
        this.id = id.toUpperCase();
        this.numberOfSegmentedObjects = new HashMap<ImageId, Integer>();
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

    @Override
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

    void checkNumberOfSegmentedObjects(ImageQuantityStructure structure, ImageId imageId,
            @SuppressWarnings("hiding") int numberOfSegmentedObjects)
    {
        final ImageId imageIdRef;
        if (structure.isObjectsIdenticalInSequence() && structure.getSequenceLength() > 1)
        {
            imageIdRef = new ImageId(imageId.getRow(), imageId.getColumn(), imageId.getField(), 0);
        } else
        {
            imageIdRef = imageId;
        }
        final Integer segObjects = this.numberOfSegmentedObjects.get(imageIdRef);
        if (segObjects == null)
        {
            this.numberOfSegmentedObjects.put(imageIdRef, numberOfSegmentedObjects);
        } else if (segObjects != numberOfSegmentedObjects)
        {
            throw new WrongNumberOfSegmentedObjectsException(datasetCode, imageId, segObjects,
                    numberOfSegmentedObjects);
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

    //
    // Comparable
    //
    
    @Override
    public int compareTo(ObjectNamespace o)
    {
        return id.compareTo(o.id);
    }

}
