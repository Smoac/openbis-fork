/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.file;

import java.util.List;

import ch.systemsx.cisd.openbis.knime.common.AbstractAggregatedDataImportNodeDialog;
import ch.systemsx.cisd.openbis.knime.common.AggregatedDataImportDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;

/**
 * Node dialog for get a data file created by an aggregation service.
 *
 * @author Franz-Josef Elmer
 */
class AggregatedDataFileImportNodeDialog extends AbstractAggregatedDataImportNodeDialog
{
    AggregatedDataFileImportNodeDialog()
    {
        super("Aggregated Data File Importing Settings");
    }

    @Override
    protected void addDescription(List<AggregatedDataImportDescription> descriptions,
            AggregationServiceDescription aggregationServiceDescription)
    {
        AggregatedDataImportDescription.addDescriptionIfDataFile(descriptions,
                aggregationServiceDescription);
    }

    @Override
    protected String getDescriptionComboBoxLabel()
    {
        return "Data File Importing Service";
    }

}
