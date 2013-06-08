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

package ch.ethz.sis.hcscld;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A class representing an object type that can be identified in an image. An
 * <code>ObjectType</code> has an id which establishes a name space for segmentations and thus
 * features.
 * <p>
 * Object types may be companions. Two companion object types are required to have the same number
 * of segmented objects and it is implied that objects with the same object id in the two object
 * types refer to each other. An example for companion objects are cells and cell nuclei. A
 * companion set of object types forms an {@link ObjectNamespace}.
 * 
 * @author Bernd Rinn
 */
public class ObjectType implements IId
{
    private final File file;

    private final String datasetCode;

    private final String id;

    private ObjectNamespace objectNamespace;

    ObjectType(String id, File file, String datasetCode, ObjectType... companions)
    {
        this.id = id.toUpperCase();
        this.file = file;
        this.datasetCode = datasetCode;
        final Set<ObjectType> companionSet = new LinkedHashSet<ObjectType>(Arrays.asList(companions));
        companionSet.add(this);
        this.objectNamespace = new ObjectNamespace(file, datasetCode, id, companionSet);
    }

    /**
     * Returns the object type id.
     */
    @Override
    public String getId()
    {
        return id;
    }

    /**
     * Returns the file that this object object type is defined in.
     */
    public File getFile()
    {
        return file;
    }

    /**
     * Returns the dataset code that this object type is defined in.
     */
    public String getDatasetCode()
    {
        return datasetCode;
    }

    /**
     * Returns the namespace that this object type is a member of.
     */
    public ObjectNamespace getObjectNamespace()
    {
        return objectNamespace;
    }

    void setObjectNamespace(ObjectNamespace objectNamespace)
    {
        this.objectNamespace = objectNamespace;
    }

    /**
     * Returns the set of companion objects types.
     */
    public Set<ObjectType> getCompanions()
    {
        return objectNamespace.getObjectTypes();
    }

    /**
     * Returns <code>true</code> if <var>other</var> is an object type of the same dataset.
     */
    public boolean isSameDataset(ObjectType other)
    {
        return other.file.equals(file) && other.datasetCode.equals(datasetCode);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((datasetCode == null) ? 0 : datasetCode.hashCode());
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        final ObjectType other = (ObjectType) obj;
        if (datasetCode == null)
        {
            if (other.datasetCode != null)
            {
                return false;
            }
        } else if (datasetCode.equals(other.datasetCode) == false)
        {
            return false;
        }
        if (file == null)
        {
            if (other.file != null)
            {
                return false;
            }
        } else if (file.equals(other.file) == false)
        {
            return false;
        }
        if (id == null)
        {
            if (other.id != null)
            {
                return false;
            }
        } else if (id.equals(other.id) == false)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "ObjectType [id=" + id + ", datasetCode=" + datasetCode + ", file=" + file + "]";
    }
}
