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

    // ---- required entity type patterns -----------

    // type of the dataset which stores image analysis data, there should be at most one
    public static final String HCS_IMAGE_ANALYSIS_DATASET_TYPE_PATTERN =
            "HCS_IMAGE_ANALYSIS_DATA|.*HCS_WELL_FV.*";

    // type of the dataset which stores plate image overlays
    public static final String IMAGE_OVERLAY_DATASET_TYPE_PATTERN = ".*OVERLAY.*";

    // type of the dataset which stores plate image overlays
    public static final String HCS_SEGMENTATION_IMAGE_DATASET_TYPE_PATTERN =
            "HCS_IMAGE_SEGMENTATION.*";

    // Type of the dataset which stores raw plate images.
    public static final String HCS_RAW_IMAGE_DATASET_TYPE_PATTERN = "HCS_IMAGE_RAW.*";

    // The plain old legacy type for raw image data sets.
    public static final String HCS_RAW_IMAGE_LEGACY_DATASET_TYPE = "HCS_IMAGE";
    
    // Type of the dataset which stores plate images.
    // We do not want old analysis data to match to this pattern.
    public static final String HCS_IMAGE_DATASET_TYPE_PATTERN =
            ".*" + HCS_RAW_IMAGE_LEGACY_DATASET_TYPE + "($|[^_].*|_[^A].*|_A[^N].*|_AN[^A].*)";

    public static final String HCS_PLATE_SAMPLE_TYPE_PATTERN = ".*PLATE.*";

    // type of the dataset which stores microscopy images
    public static final String MICROSCOPY_IMAGE_DATASET_TYPE_PATTERN = "MICROSCOPY_IMAGE|.*IMG.*";

    // the sample is considered to be a microscopy sample if its type code contains this string
    public static final String IMAGE_SAMPLE_TYPE_PATTERN = ".*IMG.*";

    // the non-control well has to have a type code which contains this string
    public static final String NON_CONTROL_WELL_SAMPLE_TYPE_PATTERN =
            ".*WELL.*|.*CHAMBER.*|OLIGO|GENE";

    // the well is considered to be a control well if its type code contains this string
    public static final String CONTROL_WELL_SAMPLE_TYPE_PATTERN = ".*CONTROL.*";

    // ----

    // code of plate geometry vocabulary
    public static final String PLATE_GEOMETRY = "$PLATE_GEOMETRY";

    // Used to import Qiagen siRNA libraries
    public static final String LIBRARY_PLUGIN_TYPE_CODE = "LIBRARY";

    // --- !!!!!! It's discouraged to use this constant, try hard not to do that !!!!!!!! ---

    /** It's discouraged to use this constant, use {@link #HCS_PLATE_SAMPLE_TYPE_PATTERN} instead. */
    public static final String DEFAULT_PLATE_SAMPLE_TYPE_CODE = "PLATE";

    /** It's discouraged to use this constant. Code of the gene material type */
    public static final String GENE_PLUGIN_TYPE_CODE = "GENE";

    /** It's discouraged to use this constant. Code of the siRNA material type. */
    public static final String SIRNA_PLUGIN_TYPE_NAME = "SIRNA";

    public static final String SIRNA_WELL_TYPE_CODE = "SIRNA_WELL";

    /** It's discouraged to use this constant. */
    public static final String GENE_SYMBOLS = "GENE_SYMBOLS";

    /** It's discouraged to use this constant. */
    public static final String MERGED_CHANNELS = "Merged Channels";

    // ---- required DSS servlets

    /** path to the datastore screening servlet able to display images in different channels */
    public static final String DATASTORE_SCREENING_SERVLET_URL = "datastore_server_screening";

    public static final String IMAGE_VIEWER_LAUNCH_URL = "image-viewer-launch";

    public static class ImageServletUrlParameters
    {
        // -- optional servlet parameters

        public final static String CHANNEL_PARAM = "channel";

        // allows to merge all channels of the basic dataset without specifying all of them
        public final static String MERGE_CHANNELS_PARAM = "mergeChannels";

        public final static String OVERLAY_CHANNEL_PREFIX_PARAM = "overlayChannel-";

        public final static String CHANNEL_STACK_ID_PARAM = "channelStackId";

        public final static String WELL_ROW_PARAM = "wellRow";

        public final static String WELL_COLUMN_PARAM = "wellCol";

        // -- mandatory servlet parameters

        public final static String DATASET_CODE_PARAM = "dataset";

        public final static String TILE_ROW_PARAM = "tileRow";

        public final static String TILE_COL_PARAM = "tileCol";
    }

}
