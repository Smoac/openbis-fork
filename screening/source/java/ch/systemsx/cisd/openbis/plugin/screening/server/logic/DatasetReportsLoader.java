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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Loads content of datasets by contacting DSS.
 * 
 * @author Tomasz Pylak
 */
public class DatasetReportsLoader
{
    public static TableModel loadAnalysisResults(List<String> datasets, String datastoreCode,
            IExternalDataTable externalDataTable)
    {
        return externalDataTable.createReportFromDatasets(
                ScreeningConstants.PLATE_IMAGE_ANALYSIS_REPORT_KEY, datastoreCode, datasets);
    }

}
