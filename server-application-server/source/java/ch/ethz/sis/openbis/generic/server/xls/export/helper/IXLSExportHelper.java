/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.Collection;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public interface IXLSExportHelper
{

    AdditionResult add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, final int rowNumber,
            final Map<String, Collection<String>> entityTypeExportPropertiesMap,
            final XLSExport.TextFormatting textFormatting);

    IEntityType getEntityType(final IApplicationServerApi api, final String sessionToken, final String permId);

    class AdditionResult
    {
        private final int rowNumber;

        private final Collection<String> warnings;

        public AdditionResult(final int rowNumber, final Collection<String> warnings)
        {
            this.rowNumber = rowNumber;
            this.warnings = warnings;
        }

        public int getRowNumber()
        {
            return rowNumber;
        }

        public Collection<String> getWarnings()
        {
            return warnings;
        }
        
    }

}
