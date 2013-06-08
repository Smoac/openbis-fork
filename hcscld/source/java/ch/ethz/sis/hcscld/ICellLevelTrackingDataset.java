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
 * An interface for object tracking between different object namespaces and different images of a
 * sequence.
 * 
 * @author Bernd Rinn
 */
public interface ICellLevelTrackingDataset extends ICellLevelDataset
{
    /**
     * Returns all {@link ObjectTrackingType}s defined in this dataset.
     */
    public ObjectTrackingTypes getObjectTrackingTypes();

    /**
     * Returns the object tracking for the given <var>imageSequenceId> and
     * <var>objectTrackingType</var>.
     * 
     * @throws IllegalArgumentException If the object tracking for the given parameter doesn't
     *             exist.
     */
    public ObjectTracking getObjectTracking(ImageSequenceId imageSequenceId,
            ObjectTrackingType objectTrackingType) throws IllegalArgumentException;

    /**
     * Returns <code>true</code> if this dataset has an object tracking for the given
     * <var>imageSequenceId> and <var>objectTrackingType</var> and <code>false</code> otherwise.
     * 
     * @throws IllegalArgumentException If the object tracking for the given parameter doesn't
     *             exist.
     */
    public boolean hasObjectTracking(ImageSequenceId imageSequenceId,
            ObjectTrackingType objectTrackingType) throws IllegalArgumentException;

}
