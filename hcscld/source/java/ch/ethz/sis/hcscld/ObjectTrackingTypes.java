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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A class representing the object tracking types of a {@link ICellLevelTrackingDataset}.
 * 
 * @author Bernd Rinn
 */
public class ObjectTrackingTypes
{
    private final List<ObjectTrackingType> objectTrackingTypes;

    ObjectTrackingTypes(ObjectTrackingType[] objectTrackingTypes)
    {
        this.objectTrackingTypes = Collections.unmodifiableList(Arrays.asList(objectTrackingTypes));
    }

    ObjectTrackingTypes(List<ObjectTrackingType> objectTrackingTypes)
    {
        this.objectTrackingTypes = Collections.unmodifiableList(objectTrackingTypes);
    }

    /**
     * Returns the list of all {@link ObjectTrackingType}s.
     */
    public List<ObjectTrackingType> list()
    {
        return objectTrackingTypes;
    }

    /**
     * Returns the list of all {@link ObjectTrackingType}s where parent namespace and child
     * namespace are both equal to <var>namespace</var>.
     */
    public List<ObjectTrackingType> list(ObjectNamespace namespace)
    {
        return list(namespace, namespace);
    }

    /**
     * Returns the list of all {@link ObjectTrackingType}s for given <var>parentNamespace</var> and
     * <var>childNamespace</var>.
     */
    public List<ObjectTrackingType> list(ObjectNamespace parentNamespace,
            ObjectNamespace childNamespace)
    {
        final List<ObjectTrackingType> result =
                new ArrayList<ObjectTrackingType>(objectTrackingTypes.size());
        for (ObjectTrackingType type : objectTrackingTypes)
        {
            if (type.getParentObjectNamespace().equals(parentNamespace)
                    && type.getChildObjectNamespace().equals(childNamespace))
            {
                result.add(type);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the list of all {@link ObjectTrackingType}s defined in this dataset with given
     * <var>parentImageSequenceIdx</var> and <var>childImageSequenceIdx</var>.
     */
    public List<ObjectTrackingType> list(int parentImageSequenceIdx, int childImageSequenceIdx)
    {
        final List<ObjectTrackingType> result =
                new ArrayList<ObjectTrackingType>(objectTrackingTypes.size());
        for (ObjectTrackingType type : objectTrackingTypes)
        {
            if (type.getParentImageSequenceIdx() == parentImageSequenceIdx
                    && type.getChildImageSequenceIdx() == childImageSequenceIdx)
            {
                result.add(type);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the list of all {@link ObjectTrackingType}s defined in this dataset with given
     * <var>parentImageSequenceIdx</var> and <var>childImageSequenceIdx</var>.
     */
    public List<ObjectTrackingType> list(int imageSequenceIdx)
    {
        return list(imageSequenceIdx, imageSequenceIdx);
    }

    /**
     * Returns {@link ObjectTrackingType} defined in this dataset with the given parameters, if it
     * exists, and <code>null</code> otherwise.
     */
    public ObjectTrackingType tryGet(ObjectNamespace parentNamespace, int parentImageSequenceIdx,
            ObjectNamespace childNamespace, int childImageSequenceIdx)
    {
        final ObjectTrackingType key =
                new ObjectTrackingType(null, parentNamespace, parentImageSequenceIdx,
                        childNamespace, childImageSequenceIdx);
        final int index = Collections.binarySearch(objectTrackingTypes, key);
        return (index < 0) ? null : objectTrackingTypes.get(index);
    }

    /**
     * Returns {@link ObjectTrackingType} defined in this dataset with the given parameters.
     * 
     * @throws IllegalArgumentException If a object tracking type for the given parameters does not
     *             exist.
     */
    public ObjectTrackingType get(ObjectNamespace parentNamespace, int parentImageSequenceIdx,
            ObjectNamespace childNamespace, int childImageSequenceIdx)
            throws IllegalArgumentException
    {
        final ObjectTrackingType typeOrNull =
                tryGet(parentNamespace, parentImageSequenceIdx, childNamespace,
                        childImageSequenceIdx);
        if (typeOrNull == null)
        {
            throw new IllegalArgumentException(String.format("No object tracking type %s found.",
                    new ObjectTrackingType(null, parentNamespace, parentImageSequenceIdx,
                            childNamespace, childImageSequenceIdx)));
        }
        return typeOrNull;
    }

    /**
     * Returns {@link ObjectTrackingType} defined in this dataset with the given namespaces (and
     * sequence index 0), if it exists, and <code>null</code> otherwise.
     */
    public ObjectTrackingType tryGet(ObjectNamespace parentNamespace, ObjectNamespace childNamespace)
    {
        return tryGet(parentNamespace, 0, childNamespace, 0);
    }

    /**
     * Returns {@link ObjectTrackingType} defined in this dataset with the given namespaces (and
     * sequence index 0).
     * 
     * @throws IllegalArgumentException If a object tracking type for the given parameters does not
     *             exist.
     */
    public ObjectTrackingType get(ObjectNamespace parentNamespace, ObjectNamespace childNamespace)
            throws IllegalArgumentException
    {
        final ObjectTrackingType typeOrNull = tryGet(parentNamespace, childNamespace);
        if (typeOrNull == null)
        {
            throw new IllegalArgumentException(String.format("No object tracking type %s found.",
                    new ObjectTrackingType(null, parentNamespace, 0, childNamespace, 0)));
        }
        return typeOrNull;
    }

    /**
     * Returns {@link ObjectTrackingType} defined in this dataset with the given image sequence
     * indices and the given <var>namespace</var>, if it exists, and <code>null</code> otherwise.
     */
    public ObjectTrackingType tryGet(ObjectNamespace namespace, int parentImageSequenceIdx,
            int childImageSequenceIdx)
    {
        return tryGet(namespace, parentImageSequenceIdx, namespace, childImageSequenceIdx);
    }

    /**
     * Returns {@link ObjectTrackingType} defined in this dataset with the given image sequence
     * indices and the given <var>namespace</var>.
     * 
     * @throws IllegalArgumentException If a object tracking type for the given parameters does not
     *             exist.
     */
    public ObjectTrackingType get(ObjectNamespace namespace, int parentImageSequenceIdx,
            int childImageSequenceIdx) throws IllegalArgumentException
    {
        final ObjectTrackingType typeOrNull =
                tryGet(namespace, parentImageSequenceIdx, childImageSequenceIdx);
        if (typeOrNull == null)
        {
            throw new IllegalArgumentException(String.format("No object tracking type %s found.",
                    new ObjectTrackingType(null, namespace, parentImageSequenceIdx, namespace,
                            childImageSequenceIdx)));
        }
        return typeOrNull;
    }

    @Override
    public String toString()
    {
        return "ObjectTrackingTypes [objectTrackingTypes=" + objectTrackingTypes + "]";
    }

}
