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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;

/**
 * @author Tomasz Pylak
 */
public enum DisplayTypeIDGenerator implements IDisplayTypeIDGenerator
{
    PLATE_MATERIAL_REVIEWER("plate-material-reviewer"),

    PLATE_LAYOUT_SAMPLE_SECTION("plate-layout-sample-section"),

    PLATE_LAYOUT_DATASET_SECTION("plate-layout-dataset-section"),

    PLATE_LOCATIONS_MATERIAL_SECTION("plate-locations-material-section"),

    EXPERIMENT_PLATE_LOCATIONS_SECTION("plate-locations-experiment-section"),

    EXPERIMENT_WELL_MATERIALS_SECTION("experiment-well-materials-section"),

    ;

    private final String genericNameOrPrefix;

    private DisplayTypeIDGenerator(String genericNameOrPrefix)
    {
        this.genericNameOrPrefix = genericNameOrPrefix;
    }

    public String createID()
    {
        return genericNameOrPrefix;
    }

    public String createID(String suffix)
    {
        return genericNameOrPrefix + suffix;
    }

}
