/*
 * Copyright 2008 ETH Zuerich, CISD
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

/**
 * An {@link ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict} extension for
 * <i>screening</i> specific message keys.
 * 
 * @author Tomasz Pylak
 */
public final class Dict extends ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict
{
    public static final String GENE_LIBRARY_URL = "GENE_LIBRARY_URL";

    public static final String GENE_LIBRARY_SEARCH_URL = "GENE_LIBRARY_SEARCH_URL";

    public static final String WELL_ROW = "WELL_ROW";

    public static final String WELL_COLUMN = "WELL_COLUMN";

    public static final String WELL_CONTENT_MATERIAL = "WELL_CONTENT_MATERIAL";

    public static final String WELL_CONTENT_MATERIAL_TYPE = "WELL_CONTENT_MATERIAL_TYPE";

    public static final String WELL_CONTENT_PROPERTIES = "WELL_CONTENT_PROPERTIES";

    public static final String WELL_CONTENT_FEATURE_VECTORS = "WELL_CONTENT_FEATURE_VECTORS";

    public static final String IMAGE_VIEWER_BUTTON = "image_viewer_button";

    public static final String IMAGE_ANALYSIS_DATA_SET = "IMAGE_ANALYSIS_DATA_SET";

    public static final String IMAGE_DATA_SET = "IMAGE_DATA_SET";

    public static final String PLATE = "PLATE";

    public static final String PLATE_VIEWER_TITLE = "PLATE_VIEWER_TITLE";

    public static final String WELL = "WELL";

    public static final String WELL_IMAGES = "WELL_IMAGES";

    public static final String PREVIEW = "PREVIEW";

    public static final String IMPORT_SCHEDULED_MESSAGE = "import_scheduled";

    public static final String REGISTER = "register";

    public static final String SEPARATOR = "separator";

    public static final String PLATE_LOCATIONS = "plate_locations";

    public static final String MATERIAL_MERGED_SUMMARY_SECTION_TITLE =
            "MATERIAL_MERGED_SUMMARY_SECTION_TITLE";

    public static final String EXPERIMENT_PLATE_MATERIAL_REVIEWER_SECTION =
            "EXPERIMENT_PLATE_MATERIAL_REVIEWER_SECTION";

    public static final String EXPERIMENT_PLATE_MATERIAL_BROWSER_SECTION =
            "EXPERIMENT_PLATE_MATERIAL_BROWSER_SECTION";

    public static final String EXPERIMENT_FEATURE_VECTOR_SUMMARY_SECTION =
            "EXPERIMENT_FEATURE_VECTOR_SUMMARY_SECTION";

    public static final String SCREENING_MODULE_TITLE = "SCREENING_MODULE_TITLE";

    public static final String PLATE_MATERIAL_REVIEWER_TITLE = "PLATE_MATERIAL_REVIEWER_TITLE";

    public static final String PLATE_MATERIAL_REVIEWER_SPECIFY_METERIAL_ITEMS =
            "PLATE_MATERIAL_REVIEWER_SPECIFY_METERIAL_ITEMS";

    public static final String EXACT_MATCH_ONLY = "EXACT_MATCH_ONLY";

    public static final String WELLS_SEARCH_MENU_ITEM = "WELLS_SEARCH";

    public static final String WELLS_SEARCH_SHOW_COMBINED_RESULTS =
            "WELLS_SEARCH_SHOW_COMBINED_RESULTS";

    public static final String WELL_SEARCH_NO_RESULTS_IN_ANY_EXP_FOUND =
            "WELL_SEARCH_NO_RESULTS_IN_ANY_EXP_FOUND";

    public static final String WELL_SEARCH_NO_RESULTS_IN_SELECTED_EXP_FOUND =
            "WELL_SEARCH_NO_RESULTS_IN_SELECTED_EXP_FOUND";

    public static final String WELL_SEARCH_PERFORM_IN_ALL_EXP = "WELL_SEARCH_PERFORM_IN_ALL_EXP";

    public static final String MATERIAL_DISAMBIGUATION_TITLE = "MATERIAL_DISAMBIGUATION_TITLE";

    public static final String MATERIAL_DISAMBIGUATION_GRID_EXPLANATION =
            "MATERIAL_DISAMBIGUATION_GRID_EXPLANATION";

    public static final String ASSAY_HEADER = "ASSAY_HEADER";

    public static final String FIND_IN_ALL_ASSAYS = "FIND_IN_ALL_ASSAYS";

    public static final String MATERIAL_IN_ASSAY = "MATERIAL_IN_ASSAY";

    public static final String SHOW_ASSAY = "SHOW_ASSAY";

    public static final String MATERIAL_IN_ALL_ASSAYS = "MATERIAL_IN_ALL_ASSAYS";

    private Dict()
    {
        // Can not be instantiated.
    }
}
