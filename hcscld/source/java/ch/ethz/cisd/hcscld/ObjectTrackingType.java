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

import ch.systemsx.cisd.hdf5.CompoundType;

/**
 * A type of object tracking.
 * 
 * @author Bernd Rinn
 */
@CompoundType(mapAllFields = false)
public class ObjectTrackingType
{
    private String parentObjectNamespaceId;

    private String childObjectNamespaceId;

    private int parentImageSequenceIdx;

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
        this.parentObjectNamespaceId = parentObjectNamespace.getId();
        this.parentImageSequenceIdx = parentImageSequenceIdx;
        this.childObjectNamespace = childObjectNamespace;
        this.childObjectNamespaceId = childObjectNamespace.getId();
        this.childImageSequenceIdx = childImageSequenceIdx;
    }

    /**
     * Sets the dataset. Needs to be called after reading the compound from the HDF5 file.
     */
    void setDataset(CellLevelTrackingDataset dataset)
    {
        this.dataset = dataset;
        this.parentObjectNamespace = dataset.getObjectNamespace(parentObjectNamespaceId);
        this.childObjectNamespace = dataset.getObjectNamespace(childObjectNamespaceId);
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

    String getParentObjectNamespaceId()
    {
        return parentObjectNamespaceId;
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

    String getChildObjectNamespaceId()
    {
        return childObjectNamespaceId;
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
        return dataset.getObjectPath(imageSequenceId, "ParentNS", parentObjectNamespaceId,
                "ParentSID", Integer.toString(parentImageSequenceIdx), "ChildNS",
                childObjectNamespaceId, "ChildSID", Integer.toString(childImageSequenceIdx));
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
                        + ((childObjectNamespaceId == null) ? 0 : childObjectNamespaceId.hashCode());
        result = prime * result + parentImageSequenceIdx;
        result =
                prime
                        * result
                        + ((parentObjectNamespaceId == null) ? 0 : parentObjectNamespaceId
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
        if (childObjectNamespaceId == null)
        {
            if (other.childObjectNamespaceId != null)
            {
                return false;
            }
        } else if (childObjectNamespaceId.equals(other.childObjectNamespaceId) == false)
        {
            return false;
        }
        if (parentImageSequenceIdx != other.parentImageSequenceIdx)
        {
            return false;
        }
        if (parentObjectNamespaceId == null)
        {
            if (other.parentObjectNamespaceId != null)
            {
                return false;
            }
        } else if (parentObjectNamespaceId.equals(other.parentObjectNamespaceId) == false)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "ObjectTrackingType [dataset=" + dataset + ", parentObjectNamespaceId="
                + parentObjectNamespaceId + ", parentImageSequenceId=" + parentImageSequenceIdx
                + ", childObjectNamespaceId=" + childObjectNamespaceId + ", childImageSequenceId="
                + childImageSequenceIdx + "]";
    }

}
