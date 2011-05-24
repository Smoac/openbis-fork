/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Utility methods to support the building of material-related UI components.
 * 
 * @author Tomasz Pylak
 */
public class MaterialComponentUtils
{

    /** @return the best short description of the material. */
    public static String getMaterialName(Material material)
    {
        if (material.getEntityType().getCode()
                .equalsIgnoreCase(ScreeningConstants.GENE_PLUGIN_TYPE_CODE))
        {
            String geneSymbol =
                    PropertiesUtil.tryFindProperty(material, ScreeningConstants.GENE_SYMBOLS);
            if (geneSymbol != null)
            {
                return geneSymbol;
            }
        }
        return material.getCode();
    }

    /** @return the material code as title */
    public static String getMaterialTypeAsTitle(Material material)
    {
        String materialTypeCode = material.getMaterialType().getCode();

        return formatAsTitle(materialTypeCode);
    }

    // chnages CODE to Code
    private static String formatAsTitle(String text)
    {
        return ("" + text.charAt(0)).toUpperCase() + text.substring(1).toLowerCase();
    }
}
