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

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Describes images in one dataset and the way to access them.
 * 
 * @author Tomasz Pylak
 */
public class DatasetImagesReference implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final DatasetImagesReference create(DatasetReference dataset,
            PlateImageParameters imageParams)
    {
        return new DatasetImagesReference(dataset, imageParams);
    }

    private DatasetReference dataset;

    private PlateImageParameters imageParameters;

    // GWT only
    private DatasetImagesReference()
    {
    }

    private DatasetImagesReference(DatasetReference dataset, PlateImageParameters imageParameters)
    {
        this.dataset = dataset;
        this.imageParameters = imageParameters;
    }

    public String getDatastoreCode()
    {
        return dataset.getDatastoreCode();
    }

    public String getDownloadUrl()
    {
        return dataset.getDownloadUrl();
    }

    public String getDatasetCode()
    {
        return dataset.getCode();
    }

    public Long getDatasetId()
    {
        return dataset.getId();
    }

    public DatasetReference getDatasetReference()
    {
        return dataset;
    }

    public PlateImageParameters getImageParameters()
    {
        return imageParameters;
    }
}
