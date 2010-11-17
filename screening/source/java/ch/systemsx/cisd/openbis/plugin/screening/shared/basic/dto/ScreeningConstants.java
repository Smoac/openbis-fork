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

/**
 * Constants which are specific for screening. Here some assumptions are made about:
 * <ul>
 * <li>initial database content (metadata) of each screening module</li>
 * <li>mandatory part of datastore server configuration</li>
 * </ul>
 * 
 * @author Tomasz Pylak
 */
public class ScreeningConstants
{
    // name of the directory inside the dataset where files in original form are stored
    public static final String ORIGINAL_DATA_DIR = "original";

    // name of the data source (configured in service.properties) which allows to access imaging db
    public static final String IMAGING_DATA_SOURCE = "imaging-db";

    // ---- required entity types

    // type of the dataset which stores plate images, there should be at most one
    public static final String IMAGE_DATASET_TYPE = "HCS_IMAGE";

    // type of the dataset which stores image analysis data, there should be at most one
    public static final String IMAGE_ANALYSIS_DATASET_TYPE = "HCS_IMAGE_ANALYSIS_DATA";

    public static final String SIRNA_WELL_TYPE_CODE = "SIRNA_WELL";

    // the well is considered to be a control well if its type code contains this string
    public static final String CONTROL_WELL_CODE_MARKER = "CONTROL";

    public static final String PLATE_PLUGIN_TYPE_CODE = "PLATE";

    public static final String LIBRARY_PLUGIN_TYPE_CODE = "LIBRARY";

    // code of the gene material type
    public static final String GENE_PLUGIN_TYPE_CODE = "GENE";

    // code of the siRNA material type
    public static final String SIRNA_PLUGIN_TYPE_NAME = "SIRNA";

    // code of plate geometry vocabulary
    public static final String PLATE_GEOMETRY = "$PLATE_GEOMETRY";

    public static final String DESCRIPTION = "DESCRIPTION";

    public static final String IMAGE_DATASET_PLUGIN_TYPE_CODE = IMAGE_DATASET_TYPE;

    // ---- required DSS servlets

    /** path to the datastore screening servlet able to display images in different channels */
    public static final String DATASTORE_SCREENING_SERVLET_URL = "datastore_server_screening";

    public static final String IMAGE_VIEWER_LAUNCH_URL = "image-viewer-launch";

    public static final String MERGED_CHANNELS = "Merged Channels";

    public static final String GENE_SYMBOLS = "GENE_SYMBOLS";

    public static final String SIRNA_HCS = "SIRNA_HCS";

}
