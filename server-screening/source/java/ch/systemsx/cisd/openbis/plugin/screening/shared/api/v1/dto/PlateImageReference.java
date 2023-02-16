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
 * Identifies one image acquired in a screening context.
 * 
 * @author Tomasz Pylak
 */
@SuppressWarnings("unused")
@JsonObject("PlateImageReference")
public class PlateImageReference extends DatasetIdentifier
{
    private static final long serialVersionUID = 1L;

    private WellPosition wellPosition;

    private int tile;

    private String channelOrNull;

    /**
     * @param dataset if image dataset is specified, image will be fetched from it. If a feature vector dataset is specified, a connected image
     *            dataset will be found and image will be fetched from it.
     * @param channelOrNull if null then merged channels will be delivered
     */
    public PlateImageReference(int tile, String channelOrNull, WellPosition wellPosition,
            IDatasetIdentifier dataset)
    {
        super(dataset.getDatasetCode(), dataset.getDatastoreServerUrl());
        this.wellPosition = wellPosition;
        this.tile = tile;
        this.channelOrNull = channelOrNull != null ? channelOrNull.toUpperCase() : null;
    }

    /**
     * @param dataset if image dataset is specified, image will be fetched from it. If a feature vector dataset is specified, a connected image
     *            dataset will be found and image will be fetched from it.
     */
    public PlateImageReference(int wellRow, int wellColumn, int tile, String channel,
            IDatasetIdentifier dataset)
    {
        this(tile, channel, new WellPosition(wellRow, wellColumn), dataset);
    }

    /** Well position on the plate */
    public WellPosition getWellPosition()
    {
        return wellPosition;
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
        return channelOrNull;
    }

    @Override
    public String toString()
    {
        String wellDesc = wellPosition != null ? ", well " + wellPosition : "";
        return "Image for [dataset " + getDatasetCode() + wellDesc + ", channel " + channelOrNull
                + ", tile " + tile + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + super.hashCode();
        result = prime * result + (channelOrNull == null ? 0 : channelOrNull.hashCode());
        result = prime * result + tile;
        result = prime * result + wellPosition.hashCode();
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

        final PlateImageReference other = (PlateImageReference) obj;
        if ((channelOrNull == null && other.channelOrNull != null)
                || channelOrNull.equals(other.channelOrNull) == false)
        {
            return false;
        }
        if (tile != other.tile)
        {
            return false;
        }
        if (wellPosition.equals(other.wellPosition) == false)
        {
            return false;
        }
        return true;
    }

    //
    // JSON-RPC
    //

    private PlateImageReference()
    {
        super(null, null);
    }

    private void setWellPosition(WellPosition wellPosition)
    {
        this.wellPosition = wellPosition;
    }

    private void setTile(int tile)
    {
        this.tile = tile;
    }

    private void setChannel(String channelOrNull)
    {
        this.channelOrNull = channelOrNull;
    }

}