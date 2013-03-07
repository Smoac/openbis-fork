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

import ch.systemsx.cisd.hdf5.CompoundElement;
import ch.systemsx.cisd.hdf5.CompoundType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationType;
import ch.systemsx.cisd.hdf5.HDF5EnumerationValue;

/**
 * A type of object tracking.
 * 
 * @author Bernd Rinn
 */
@CompoundType(mapAllFields = false)
public class ObjectTrackingType implements Comparable<ObjectTrackingType>
{
    @CompoundElement(memberName = "parentObjectNamespace")
    private HDF5EnumerationValue parentObjectNamespaceEnum;

    @CompoundElement(memberName = "childObjectNamespace")
    private HDF5EnumerationValue childObjectNamespaceEnum;

    @CompoundElement
    private int parentImageSequenceIdx;

    @CompoundElement
    private int childImageSequenceIdx;

    private ObjectNamespace parentObjectNamespace;

    private ObjectNamespace childObjectNamespace;

    private CellLevelTrackingDataset dataset;

    // Used by compound reading from HDF5 file.
    ObjectTrackingType()
    {
    }

    ObjectTrackingType(CellLevelTrackingDataset dataset, ObjectNamespace parentObjectNamespace,
            int parentImageSequenceIdx, ObjectNamespace childObjectNamespace,
            int childImageSequenceIdx)
    {
        this.dataset = dataset;
        this.parentObjectNamespace = parentObjectNamespace;
        this.parentImageSequenceIdx = parentImageSequenceIdx;
        this.childObjectNamespace = childObjectNamespace;
        this.childImageSequenceIdx = childImageSequenceIdx;
    }

    void setNamespacesEnumType(HDF5EnumerationType namespacesEnumType)
    {
        parentObjectNamespaceEnum =
                new HDF5EnumerationValue(namespacesEnumType, parentObjectNamespace.getId());
        childObjectNamespaceEnum =
                new HDF5EnumerationValue(namespacesEnumType, childObjectNamespace.getId());
    }

    /**
     * Sets the dataset. Needs to be called after reading the compound from the HDF5 file.
     */
    void setDataset(CellLevelTrackingDataset dataset)
    {
        this.dataset = dataset;
        this.parentObjectNamespace =
                dataset.getObjectNamespace(parentObjectNamespaceEnum.getValue());
        this.childObjectNamespace = dataset.getObjectNamespace(childObjectNamespaceEnum.getValue());
    }

    CellLevelTrackingDataset getDataset()
    {
        return dataset;
    }

    /**
     * Returns the namespace of the parent objects.
     */
    public ObjectNamespace getParentObjectNamespace()
    {
        return parentObjectNamespace;
    }

    /**
     * Returns the image sequence index of the parent objects.
     */
    public int getParentImageSequenceIdx()
    {
        return parentImageSequenceIdx;
    }

    /**
     * Returns the namespace of the child objects.
     */
    public ObjectNamespace getChildObjectNamespace()
    {
        return childObjectNamespace;
    }

    /**
     * Returns the image sequence index of the child objects.
     */
    public int getChildImageSequenceIdx()
    {
        return childImageSequenceIdx;
    }

    String getObjectPath(ImageSequenceId imageSequenceId)
    {
        return dataset.getObjectPath(
                imageSequenceId,
                "Links__Parent::" + parentObjectNamespace.getId() + "::"
                        + Integer.toString(parentImageSequenceIdx),
                "Child::" + childObjectNamespace.getId() + "::"
                        + Integer.toString(childImageSequenceIdx));
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + childImageSequenceIdx;
        result =
                prime
                        * result
                        + ((childObjectNamespace == null) ? 0 : childObjectNamespace.getId()
                                .hashCode());
        result = prime * result + parentImageSequenceIdx;
        result =
                prime
                        * result
                        + ((parentObjectNamespace == null) ? 0 : parentObjectNamespace.getId()
                                .hashCode());
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
        final ObjectTrackingType other = (ObjectTrackingType) obj;
        if (childImageSequenceIdx != other.childImageSequenceIdx)
        {
            return false;
        }
        if (childObjectNamespace == null)
        {
            if (other.childObjectNamespace != null)
            {
                return false;
            }
        } else if (childObjectNamespace.getId().equals(other.childObjectNamespace.getId()) == false)
        {
            return false;
        }
        if (parentImageSequenceIdx != other.parentImageSequenceIdx)
        {
            return false;
        }
        if (parentObjectNamespace == null)
        {
            if (other.parentObjectNamespace != null)
            {
                return false;
            }
        } else if (parentObjectNamespace.getId().equals(other.parentObjectNamespace.getId()) == false)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "ObjectTrackingType [dataset=" + dataset + ", parentObjectNamespaceId="
                + parentObjectNamespace.getId() + ", parentImageSequenceId="
                + parentImageSequenceIdx + ", childObjectNamespaceId="
                + childObjectNamespace.getId() + ", childImageSequenceId=" + childImageSequenceIdx
                + "]";
    }

    //
    // Comparable
    //
    
    @Override
    public int compareTo(ObjectTrackingType o)
    {
        int comp;
        comp = parentObjectNamespace.compareTo(o.parentObjectNamespace);
        if (comp == 0)
        {
            comp = parentImageSequenceIdx - o.parentImageSequenceIdx;
        }
        if (comp == 0)
        {
            comp = childObjectNamespace.compareTo(o.childObjectNamespace);
        }
        if (comp == 0)
        {
            comp = childImageSequenceIdx - o.childImageSequenceIdx;
        }
        return comp;
    }

}
