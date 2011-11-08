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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains summary meta data for images within an experiment.
 * <p>
 * Most of the class member fields are optional and will have data populated only if it is valid for
 * all entities within the experiment. For example, the field <code>imageSize</code> will not be
 * NULL only if all the images in the experiment have the same size.
 * 
 * @author Kaloyan Enimanev
 */
public class ExperimentImageMetadata implements Serializable
{

    private static final long serialVersionUID = 1L;

    private final ExperimentIdentifier identifier;

    private final List<ImageChannel> channels;

    private final Geometry plateGeometry;

    private final Geometry tileGeometry;

    // private final ImageSize imageSize;
    //
    // private final ImageSize thumbnailSize;

    public ExperimentImageMetadata(ExperimentIdentifier identifier, Geometry plateGeometry,
            Geometry tileGeometry, List<ImageChannel> channels)
    {
        this.identifier = identifier;
        this.channels = new ArrayList<ImageChannel>(channels);
        this.plateGeometry = plateGeometry;
        this.tileGeometry = tileGeometry;
        // this.imageSize = imageSize;
        // this.thumbnailSize = thumbnailSize;
    }

    /**
     * Return the experiment identifier.
     */
    public ExperimentIdentifier getIdentifier()
    {
        return identifier;
    }

    /**
     * Return all channels of images contained in the experiment.
     */
    public List<ImageChannel> getChannels()
    {
        return Collections.unmodifiableList(channels);
    }

    /**
     * Returns the plate geometry if all plates in the experiment have the same geometry. Returns
     * <code>NULL</code> if two or more of the experiment's plates have different geometries.
     */
    public Geometry getPlateGeometry()
    {
        return plateGeometry;
    }

    /**
     * Returns the tiles geometry if all tiles in the experiment have the same geometry. Returns
     * <code>NULL</code> if two or more of the experiment's tiles have different geometries.
     */
    public Geometry getTileGeometry()
    {
        return tileGeometry;
    }

    // /**
    // * Returns the image size if all images in the experiment have the same size. Returns
    // * <code>NULL</code> if two or more of the experiment's images have different sizes.
    // */
    // public ImageSize getImageSize()
    // {
    // return imageSize;
    // }
    //
    // /**
    // * Returns the thumbnail size if all images in the experiment have the same thumbnail size.
    // * Returns <code>NULL</code> if two or more of the experiment's images have different
    // thumbnail
    // * sizes.
    // */
    // public ImageSize getThumbnailSize()
    // {
    // return thumbnailSize;
    // }

}
