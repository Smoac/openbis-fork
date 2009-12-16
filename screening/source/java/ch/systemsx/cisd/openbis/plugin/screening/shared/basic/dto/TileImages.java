/*
 * Copyright 2009 ETH Zuerich, CISD
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

/**
 * Describes images in one dataset and URLs to them.
 * 
 * @author Tomasz Pylak
 */
public class TileImages implements IsSerializable
{
    public static final TileImages create(DatasetReference dataset, List<TileImage> plateImages,
            PlateImageParameters imageParams)
    {
        return new TileImages(dataset, plateImages, imageParams);
    }

    private DatasetReference dataset;

    private List<TileImage> images;

    private PlateImageParameters imageParameters;

    // GWT only
    @SuppressWarnings("unused")
    private TileImages()
    {
    }

    public TileImages(DatasetReference dataset, List<TileImage> images,
            PlateImageParameters imageParameters)
    {
        this.dataset = dataset;
        this.images = images;
        this.imageParameters = imageParameters;
    }

    public String getDownloadUrl()
    {
        return dataset.getDownloadUrl();
    }

    public String getDatasetCode()
    {
        return dataset.getCode();
    }

    public String getDatastoreCode()
    {
        return dataset.getDatastoreCode();
    }

    public List<TileImage> getImages()
    {
        return images;
    }

    public PlateImageParameters getImageParameters()
    {
        return imageParameters;
    }
}
