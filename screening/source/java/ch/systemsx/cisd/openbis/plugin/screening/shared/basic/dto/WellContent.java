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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityReference;

/**
 * Describes the well and its location on the plate. Contains pointers to well and plate samples, a
 * material inside the well and the images from one dataset (if available) acquired for the well.
 * 
 * @author Tomasz Pylak
 */
public class WellContent implements IsSerializable
{
    private WellLocation locationOrNull; // null if well code was incorrect

    private EntityReference well;

    private EntityReference plate;

    // a pointer to a material which was being searched for inside a well
    private EntityReference materialContent;

    // contains only images for this well, null if no images have been acquired
    private DatasetImagesReference imagesOrNull;

    // GWT only
    @SuppressWarnings("unused")
    private WellContent()
    {
    }

    public WellContent(WellLocation locationOrNull, EntityReference well, EntityReference plate,
            EntityReference materialContent)
    {
        this.locationOrNull = locationOrNull;
        this.well = well;
        this.plate = plate;
        this.materialContent = materialContent;
    }

    public WellLocation tryGetLocation()
    {
        return locationOrNull;
    }

    public EntityReference getWell()
    {
        return well;
    }

    public EntityReference getPlate()
    {
        return plate;
    }

    public EntityReference getMaterialContent()
    {
        return materialContent;
    }

    public DatasetImagesReference tryGetImages()
    {
        return imagesOrNull;
    }

    public WellContent cloneWithImages(DatasetImagesReference images)
    {
        WellContent clone = new WellContent(locationOrNull, well, plate, materialContent);
        clone.imagesOrNull = images;
        return clone;
    }

    @Override
    public String toString()
    {
        return "location = " + locationOrNull + ", plate = " + plate + ", well = " + well
                + ", content = " + materialContent;
    }
}
