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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator.PlateLocationsMaterialLocatorResolver;

/**
 * Defines the ways simple view mode links for screening specific views are created.
 * 
 * @author Piotr Buczek
 */
public class ScreeningLinkExtractor extends LinkExtractor
{

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
        url.addParameterWithoutEncoding(
                PlateLocationsMaterialLocatorResolver.EXPERIMENT_PARAMETER_KEY, StringEscapeUtils
                        .unescapeHtml(experimentIdentifier));
        return url;
    }

}
