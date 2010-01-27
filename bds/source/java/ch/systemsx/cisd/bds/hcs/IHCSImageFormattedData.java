/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds.hcs;

import java.io.File;

import ch.systemsx.cisd.bds.IFormattedData;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.ILink;
import ch.systemsx.cisd.bds.storage.INode;

/**
 * {@link IFormattedData} extension specific to <i>HCS (High-Content Screening) with Images</i>.
 * 
 * @author Christian Ribeaud
 */
public interface IHCSImageFormattedData extends IFormattedData
{
    /**
     * Returns <code>true</code> if the data structure contains the original data.
     */
    public boolean containsOriginalData();

    /** see {@link HCSImageFormatV1_0#IS_INCOMING_SYMBOLIC_LINK} */
    public boolean isIncomingSymbolicLink();

    /**
     * Returns the number of channels.
     */
    public int getChannelCount();

    /**
     * Returns the geometric arrangement of the tiles of a well.
     */
    public Geometry getWellGeometry();

    /**
     * Returns the geometric arrangement of the wells of a plate.
     */
    public Geometry getPlateGeometry();

    /**
     * For given <var>channel</var>, given <var>wellLocation</var> and given <var>tileLocation</var>
     * returns the corresponding <code>INode</code> (found in <code>data/standard</code> directory).
     * 
     * @return this could be, for instance, a {@link ILink} pointing to the
     *         <code>data/original</code> directory or a {@link IFile} that can be extracted
     *         somewhere. Might return <code>null</code>.
     */
    public INode tryGetStandardNodeAt(final int channel, final Location wellLocation,
            final Location tileLocation);

    /**
     * Adds a new image file at given coordinates.
     * 
     * @param imageRootDirectory the root directory where the image is located. This usually is the
     *            incoming data set directory.
     * @param imageRelativePath relative path (to <var>imageRootDirectory</var>) name of the image
     *            file that is going to be added in the <code>standard</code> directory.
     * @return the new <code>INode</code> just added (encapsulated in returned <code>NodePath</code>
     *         ) with its path in the <code>standard</code> directory. Never returns
     *         <code>null</code>.
     * @throws DataStructureException if a node already exists at given coordinates.
     */
    public NodePath addStandardNode(final File imageRootDirectory, final String imageRelativePath,
            final int channel, final Location wellLocation, final Location tileLocation)
            throws DataStructureException;

    //
    // Helper classes
    //

    public final static class NodePath
    {
        private final INode node;

        private final String path;

        public NodePath(final INode node, final String path)
        {
            assert node != null : "Given node could not be null.";
            assert path != null : "Given path could not be null.";
            this.node = node;
            this.path = path;
        }

        public INode getNode()
        {
            return node;
        }

        public String getPath()
        {
            return path;
        }
    }
}
