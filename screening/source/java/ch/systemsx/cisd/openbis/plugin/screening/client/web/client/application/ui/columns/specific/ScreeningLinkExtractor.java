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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.URLListEncoder;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCodesCriteria;

/**
 * Defines the ways simple view mode links for screening specific views are created.
 * 
 * @author Piotr Buczek
 */
public class ScreeningLinkExtractor extends LinkExtractor
{
    public final static String PLATE_METADATA_BROWSER_ACTION = "PLATE_METADATA_REPORT";

    public final static String EXPERIMENT_PARAMETER_KEY = "experiment";

    public final static String WELL_SEARCH_ACTION = "WELL_SEARCH";

    public final static String EXPERIMENT_PERM_ID_PARAMETER_KEY = "experimentPermId";

    public final static String WELL_SEARCH_IS_EXACT_PARAMETER_KEY = "isExactSearch";

    public final static String WELL_SEARCH_MATERIAL_TYPES_PARAMETER_KEY = "types";

    public final static String WELL_SEARCH_MATERIAL_ITEMS_PARAMETER_KEY = "items";

    public static final String extractPlateMetadataBrowserLink(String platePermId)
    {
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(BasicConstant.LOCATOR_ACTION_PARAMETER, PLATE_METADATA_BROWSER_ACTION);
        url.addParameter(PermlinkUtilities.PERM_ID_PARAMETER_KEY, platePermId);
        return tryPrint(url);
    }

    // action=WELL_SEARCH&experimentPermId=8127361723172863&isExactSearch=true&types=typeCode1,typeCode2&items=code1,property2"
    public static String createWellsSearchLink(final String experimentPermId,
            final MaterialSearchCodesCriteria materialCodesCriteria)
    {
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(BasicConstant.LOCATOR_ACTION_PARAMETER, WELL_SEARCH_ACTION);
        url.addParameter(EXPERIMENT_PERM_ID_PARAMETER_KEY, experimentPermId);
        url.addParameter(WELL_SEARCH_IS_EXACT_PARAMETER_KEY,
                materialCodesCriteria.isExactMatchOnly());
        url.addParameterWithoutEncoding(WELL_SEARCH_MATERIAL_TYPES_PARAMETER_KEY,
                URLListEncoder.encodeItemList(materialCodesCriteria.getMaterialTypeCodes()));
        url.addParameterWithoutEncoding(WELL_SEARCH_MATERIAL_ITEMS_PARAMETER_KEY,
                URLListEncoder.encodeItemList(materialCodesCriteria.getMaterialCodesOrProperties()));
        return tryPrint(url);
    }

    public static final String tryExtractMaterialWithExperiment(IEntityInformationHolder material,
            String experimentIdentifier)
    {
        URLMethodWithParameters url =
                tryCreateMaterialWithExperimentLink(material.getCode(), material.getEntityType()
                        .getCode(), experimentIdentifier);
        return tryPrint(url);
    }

    private static final URLMethodWithParameters tryCreateMaterialWithExperimentLink(
            String materialCode, String materialTypeCode, String experimentIdentifier)
    {
        if (materialCode == null || materialTypeCode == null || experimentIdentifier == null)
        {
            return null;
        }
        URLMethodWithParameters url = tryCreateMaterialLink(materialCode, materialTypeCode);
        // We know that experiment identifier cannot contain characters that should be encoded
        // apart from '/'. Encoding '/' makes the URL less readable and on the other hand
        // leaving it as it is doesn't cause us any problems.
        url.addParameterWithoutEncoding(EXPERIMENT_PARAMETER_KEY,
                StringEscapeUtils.unescapeHtml(experimentIdentifier));
        return url;
    }

}
