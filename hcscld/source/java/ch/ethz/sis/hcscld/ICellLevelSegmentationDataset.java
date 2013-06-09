/*
 * Copyright 2011-2013 ETH Zuerich, Scientific IT Services
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
 * An interface for image segmentation datasets on the cell level.
 * 
 * @author Bernd Rinn
 */
public interface ICellLevelSegmentationDataset extends ICellLevelDataset
{
    /**
     * Returns the image geometry of this image segmentation data set.
     * 
     * @return The image geometry object.
     */
    public ImageGeometry getImageGeometry();

    /**
     * Returns the number of segmented objects for the given image and object objectType.
     */
    public int getNumberOfSegmentedObjects(ImageId imageId, ObjectType objectType);
    
    /**
     * Returns the segmented object with given <var>objectId</var> in the given <var>wellId</var>.
     * 
     * @param wellId The well id to read the the object from.
     * @param objectType The type of segmented object to get.
     * @param objectId The object id to read.
     * @param withEdge If <code>true</code>, the edge of the object will be read or, if not saved,
     *            computed.
     * @return The object as found by <code>segmentation</code>.
     */
    public SegmentedObject getObject(ImageId wellId, ObjectType objectType, int objectId,
            boolean withEdge);

    /**
     * Returns the segmented object in the given <var>wellId</var> that is at point (x,y).
     * 
     * @param wellId The well id to read the the object from.
     * @param objectType The type of segmented object to look for.
     * @param x The x coordinate to look up the object for.
     * @param y The y coordinate to look up the object for.
     * @param withEdge If <code>true</code>, the edge of the object will be read or, if not saved,
     *            computed.
     * @return The object as found by <code>segmentation</code> at point (x,y), or <code>null</code>
     *         , if no object was found at this point.
     */
    public SegmentedObject tryFindObject(ImageId wellId, ObjectType objectType, int x, int y,
            boolean withEdge);

    /**
     * Returns the segmented object in the given <var>wellId</var> that is at point (x,y).
     * 
     * @param wellId The well id to read the the object from.
     * @param x The x coordinate to look up the object for.
     * @param y The y coordinate to look up the object for.
     * @param withEdge If <code>true</code>, the edge of the object will be read or, if not saved,
     *            computed.
     * @return The object as found by <code>segmentation</code> at point (x,y), or <code>null</code>
     *         , if no object was found at this point. Returns the first object it finds.
     */
    public SegmentedObject tryFindObject(ImageId wellId, int x, int y, boolean withEdge);

    /**
     * Returns all segmented objects in the given <var>wellId</var>.
     * 
     * @param wellId The well id to read the the object from.
     * @param objectType The type of segmented objects to get.
     * @param withEdge If <code>true</code>, the edge of the object will be read or, if not saved,
     *            computed.
     * @return All objects as found by <code>segmentation</code>.
     */
    public SegmentedObject[] getObjects(ImageId wellId, ObjectType objectType, boolean withEdge);

    /**
     * Returns <code>true</code> if this dataset has objects for the given parameters and
     * <code>false</code> otherwise.
     * 
     * @param wellId The well id to check for existing objects.
     * @param objectType The type of segmented objects to check for existing objects.
     * @return <code>true</code> if this dataset has objects for the given parameters.
     */
    public boolean hasObjects(ImageId wellId, ObjectType objectType);

}
