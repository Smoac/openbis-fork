/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Identifies one image acquired in a microscopy context.
 * 
 * @author Tomasz Pylak
 */
@SuppressWarnings("unused")
@JsonObject("MicroscopyImageReference")
public class MicroscopyImageReference extends DatasetIdentifier
{
    private static final long serialVersionUID = 1L;

    private int tile;

    private String channel;

    /**
     * Reference to image dataset which has no wells (like e.g. in microscopy).
     * 
     * @param dataset if image dataset is specified, image will be fetched from it. If a feature vector dataset is specified, a connected image
     *            dataset will be found and image will be fetched from it.
     */
    public MicroscopyImageReference(int tile, String channel, IDatasetIdentifier dataset)
    {
        super(dataset.getDatasetCode(), dataset.getDatastoreServerUrl());
        this.tile = tile;
        this.channel = channel.toUpperCase();
    }

    /** a sequential number of an image tile, starts from 0 */
    public int getTile()
    {
        return tile;
    }

    /**
     * channel code
     */
    public String getChannel()
    {
        return channel;
    }

    @Override
    public String toString()
    {
        return "Image for [dataset " + getDatasetCode() + ", channel " + channel + ", tile " + tile
                + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + super.hashCode();
        result = prime * result + channel.hashCode();
        result = prime * result + tile;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        if (super.equals(obj) == false)
        {
            return false;
        }

        final MicroscopyImageReference other = (MicroscopyImageReference) obj;
        if (channel.equals(other.channel) == false)
        {
            return false;
        }
        if (tile != other.tile)
        {
            return false;
        }
        return true;
    }

    //
    // JSON-RPC
    //

    private MicroscopyImageReference()
    {
        super(null, null);
    }

    private void setTile(int tile)
    {
        this.tile = tile;
    }

    private void setChannel(String channel)
    {
        this.channel = channel;
    }

}