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

/**
 * An interface for a writable dataset of object tracking results.
 * 
 * @author Bernd Rinn
 */
public interface ICellLevelTrackingWritableDataset extends ICellLevelWritableDataset,
        ICellLevelTrackingDataset
{
    /**
     * Creates an object tracking type between two different object namespaces and two different
     * images.
     * 
     * @param parentObjectNamespace The object namespace of the parent objects.
     * @param parentImageSequenceIdx The image sequence id of the parent objects.
     * @param childObjectNamespace The object namespace of the child objects.
     * @param childImageSequenceIdx The image sequence id of the child objects.
     * @return The new tracking type.
     */
    public ObjectTrackingType createObjectTrackingType(ObjectNamespace parentObjectNamespace,
            int parentImageSequenceIdx, ObjectNamespace childObjectNamespace,
            int childImageSequenceIdx);

    /**
     * Creates an object tracking type between two different object namespaces in the same image.
     * 
     * @param parentObjectNamespace The object namespace of the parent objects.
     * @param childObjectNamespace The object namespace of the child objects.
     * @return The new tracking type.
     */
    public ObjectTrackingType createObjectTrackingType(ObjectNamespace parentObjectNamespace,
            ObjectNamespace childObjectNamespace);

    /**
     * Creates an object tracking type between the same object namespace in two different images.
     * 
     * @param objectNamespace The object namespace of the objects.
     * @param parentImageSequenceIdx The image sequence id of the parent objects.
     * @param childImageSequenceIdx The image sequence id of the child objects.
     * @return The new tracking type.
     */
    public ObjectTrackingType createObjectTrackingType(ObjectNamespace objectNamespace,
            int parentImageSequenceIdx, int childImageSequenceIdx);

    /**
     * Writes the <var>tracking</var> for the given <var>imageSequenceId</var>,
     * <var>objectTrackingType</var>.
     */
    public void writeObjectTracking(ImageSequenceId imageSequenceId,
            ObjectTrackingType objectTrackingType, ObjectTrackingBuilder tracking);
}
