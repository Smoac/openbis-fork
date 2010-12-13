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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Describes the well and its location on the plate. Contains pointers to well and plate samples, a
 * material inside the well and the images from one dataset (if available) acquired for the well.
 * 
 * @author Tomasz Pylak
 */
public class WellContent implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // ------------ Metadata -----------

    private WellLocation locationOrNull; // null only if well code was incorrect

    private EntityReference well;

    private EntityReference plate;

    private ExperimentReference experiment;

    // Material which was being searched for inside a well. Enriched with properties.
    private Material materialContent;

    // ------------ Dataset Data -------------

    // dataset which contains images for this well, null if no images have been acquired
    private DatasetImagesReference imagesDatasetOrNull;

    // dataset which contains feature vectors for this well, null if images have not been analyzed
    private DatasetReference featureVectorDatasetOrNull;

    // Feature vector values, null if images have not been analyzed.
    private NamedFeatureVector featureVectorOrNull;

    // GWT only
    @SuppressWarnings("unused")
    private WellContent()
    {
    }

    public WellContent(WellLocation locationOrNull, EntityReference well, EntityReference plate,
            ExperimentReference experiment, Material materialContent)
    {
        this(locationOrNull, well, plate, experiment, materialContent, null, null, null);
    }

    private WellContent(WellLocation locationOrNull, EntityReference well, EntityReference plate,
            ExperimentReference experiment, Material materialContent,
            DatasetImagesReference imagesDatasetOrNull,
            DatasetReference featureVectorDatasetOrNull, NamedFeatureVector featureVectorOrNull)
    {
        this.locationOrNull = locationOrNull;
        this.well = well;
        this.plate = plate;
        this.experiment = experiment;
        this.materialContent = materialContent;
        this.imagesDatasetOrNull = imagesDatasetOrNull;
        this.featureVectorDatasetOrNull = featureVectorDatasetOrNull;
        this.featureVectorOrNull = featureVectorOrNull;
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

    public Material getMaterialContent()
    {
        return materialContent;
    }

    public DatasetImagesReference tryGetImageDataset()
    {
        return imagesDatasetOrNull;
    }

    public DatasetReference tryGetFeatureVectorDataset()
    {
        return featureVectorDatasetOrNull;
    }

    public NamedFeatureVector tryGetFeatureVectorValues()
    {
        return featureVectorOrNull;
    }

    public ExperimentReference getExperiment()
    {
        return experiment;
    }

    public WellContent cloneWithDatasets(DatasetImagesReference newImagesDatasetOrNull,
            DatasetReference newFeatureVectorDatasetOrNull)
    {
        return new WellContent(this.locationOrNull, this.well, this.plate, this.experiment,
                this.materialContent, newImagesDatasetOrNull, newFeatureVectorDatasetOrNull,
                this.featureVectorOrNull);
    }

    public WellContent cloneWithFeatureVector(NamedFeatureVector newFeatureVectorOrNull)
    {
        return new WellContent(this.locationOrNull, this.well, this.plate, this.experiment,
                this.materialContent, this.imagesDatasetOrNull, this.featureVectorDatasetOrNull,
                newFeatureVectorOrNull);
    }

    @Override
    public String toString()
    {
        return "location = " + locationOrNull + ", experiment = " + experiment + ", plate = "
                + plate + ", well = " + well + ", content = " + materialContent;
    }
}
