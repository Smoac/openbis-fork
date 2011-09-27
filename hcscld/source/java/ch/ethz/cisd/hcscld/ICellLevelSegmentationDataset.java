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

package ch.ethz.cisd.hcscld;


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
     * Returns the name of the object type that was segmented for.
     */
    public String getSegmentedObjectTypeName();

    /**
     * Returns the segmented object with given <var>objectId</var> in the given <var>wellId</var>.
     * 
     * @param wellId The well id to read the the object from.
     * @param objectId The object id to read.
     * @param withEdge If <code>true</code>, the edge of the object will be read or, if not saved,
     *            computed.
     * @return The object as found by <code>segmentation</code>.
     */
    public SegmentedObject getObject(WellFieldId wellId, int objectId, boolean withEdge);

    /**
     * Returns the segmented object in the given <var>wellId</var> that is at point (x,y).
     * 
     * @param wellId The well id to read the the object from.
     * @param x The x coordinate to look up the object for.
     * @param y The y coordinate to look up the object for.
     * @param withEdge If <code>true</code>, the edge of the object will be read or, if not saved,
     *            computed.
     * @return The object as found by <code>segmentation</code> at point (x,y), or <code>null</code>
     *         , if no object was found at this point.
     */
    public SegmentedObject tryFindObject(WellFieldId wellId, int x, int y, boolean withEdge);

    /**
     * Returns all segmented objects in the given <var>wellId</var>.
     * 
     * @param wellId The well id to read the the object from.
     * @param withEdge If <code>true</code>, the edge of the object will be read or, if not saved,
     *            computed.
     * @return All objects as found by <code>segmentation</code>.
     */
    public SegmentedObject[] getObjects(WellFieldId wellId, boolean withEdge);

}
