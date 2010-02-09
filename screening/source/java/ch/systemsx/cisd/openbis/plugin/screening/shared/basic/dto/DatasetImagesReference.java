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

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;

/**
 * Describes images in one dataset and the way to access them.
 * 
 * @author Tomasz Pylak
 */
public class DatasetImagesReference implements IsSerializable
{
    public static final DatasetImagesReference create(DatasetReference dataset,
            PlateImageParameters imageParams)
    {
        return new DatasetImagesReference(dataset, imageParams);
    }

    private DatasetReference dataset;

    private PlateImageParameters imageParameters;

    // GWT only
    @SuppressWarnings("unused")
    private DatasetImagesReference()
    {
    }

    public DatasetImagesReference(DatasetReference dataset, PlateImageParameters imageParameters)
    {
        this.dataset = dataset;
        this.imageParameters = imageParameters;
    }

    public String getDownloadUrl()
    {
        return dataset.getDownloadUrl();
    }

    public IEntityInformationHolder getDatasetReference()
    {
        return dataset;
    }

    public PlateImageParameters getImageParameters()
    {
        return imageParameters;
    }
}
