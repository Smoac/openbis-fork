/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * DTO with metadata of a plate and all its wells + reference to a dataset with plate images.
 * 
 * @author Tomasz Pylak
 */
public class PlateImages implements IsSerializable
{
    private Sample plate;

    private List<WellMetadata> wellsMetadata;

    // null if dataset with images does not exist
    private DatasetImagesReference imagesOrNull;

    // GWT only
    @SuppressWarnings("unused")
    private PlateImages()
    {
    }

    public PlateImages(Sample plate, List<WellMetadata> wellsMetadata,
            DatasetImagesReference imagesOrNull)
    {
        this.plate = plate;
        this.wellsMetadata = wellsMetadata;
        this.imagesOrNull = imagesOrNull;
    }

    public List<WellMetadata> getWells()
    {
        return wellsMetadata;
    }

    /** can be null */
    public DatasetImagesReference tryGetImages()
    {
        return imagesOrNull;
    }

    public Sample getPlate()
    {
        return plate;
    }

    public int getRowsNum()
    {
        if (imagesOrNull != null)
        {
            return imagesOrNull.getImageParameters().getRowsNum();
        } else
        {
            // TODO 2009-12-09, Tomasz Pylak: calculate rows number on the basis of metadata
            return 16;
        }
    }

    public int getColsNum()
    {
        if (imagesOrNull != null)
        {
            return imagesOrNull.getImageParameters().getColsNum();
        } else
        {
            // TODO 2009-12-09, Tomasz Pylak: calculate rows number on the basis of metadata
            return 24;
        }
    }
}
