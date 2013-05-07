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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import ch.systemsx.cisd.openbis.knime.common.AbstractParameterDescriptionBasedNodeDialog;
import ch.systemsx.cisd.openbis.knime.common.FieldDescription;
import ch.systemsx.cisd.openbis.knime.common.FieldType;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

/**
 * Node dialog for get a data file created by an aggregation service.
 *
 * @author Franz-Josef Elmer
 */
class AggregatedDataFileImportNodeDialog
        extends AbstractParameterDescriptionBasedNodeDialog<AggregatedDataFileImportDescription>
{
    AggregatedDataFileImportNodeDialog()
    {
        super("Aggregated Data File Importer Settings");
    }

    @Override
    protected List<FieldDescription> getFieldDescriptions(
            AggregatedDataFileImportDescription description)
    {
        List<FieldDescription> fieldDescriptions = new ArrayList<FieldDescription>();
        HashMap<String, Object> serviceParameters = new HashMap<String, Object>();
        serviceParameters.put("requestKey", "getParameters");
        IQueryApiFacade facade = createFacade();
        QueryTableModel report =
                facade.createReportFromAggregationService(
                        description.getAggregationServiceDescription(), serviceParameters);

        List<Serializable[]> rows = report.getRows();
        for (Serializable[] row : rows)
        {
            if (row == null || row.length == 0 || row[0] == null)
            {
                throw new IllegalArgumentException("Empty row.");
            }
            String name = String.valueOf(row[0]);
            FieldType fieldType = FieldType.VARCHAR;
            String fieldParameters = "";
            if (row.length > 0)
            {
                Serializable parameter = row[1];
                if (parameter != null)
                {
                    String type = String.valueOf(parameter);
                    int indexOfSeparator = type.indexOf(':');
                    if (indexOfSeparator >= 0)
                    {
                        fieldParameters = type.substring(indexOfSeparator + 1);
                        type = type.substring(0, indexOfSeparator);
                    }
                    try
                    {
                        fieldType = FieldType.valueOf(type.trim().toUpperCase());
                    } catch (IllegalArgumentException ex)
                    {
                        logger.warn("Unknown field type '" + type + "' using VARCHAR instead.");
                    }
                }
            }
            fieldDescriptions.add(new FieldDescription(name, fieldType, fieldParameters));
        }
        return fieldDescriptions;
    }

    @Override
    protected List<AggregatedDataFileImportDescription> getSortedDescriptions(IQueryApiFacade facade)
    {
        List<AggregationServiceDescription> services = facade.listAggregationServices();
        Collections.sort(services, new Comparator<AggregationServiceDescription>()
            {
                @Override
                public int compare(AggregationServiceDescription d1,
                        AggregationServiceDescription d2)
                {
                    return d1.getServiceKey().compareTo(d2.getServiceKey());
                }
            });
        List<AggregatedDataFileImportDescription> descriptions =
                new ArrayList<AggregatedDataFileImportDescription>();
        for (AggregationServiceDescription aggregationServiceDescription : services)
        {
            if (aggregationServiceDescription.getServiceKey()
                    .startsWith(AggregatedDataFileImportDescription.KEY_PREFIX))
            {
                descriptions.add(new AggregatedDataFileImportDescription(
                        aggregationServiceDescription));
            }
        }
        return descriptions;
    }

    @Override
    protected String getDescriptionKey()
    {
        return AggregatedDataFileImportNodeModel.AGGREGATION_DESCRIPTION_KEY;
    }

    @Override
    protected String getDescriptionComboBoxLabel()
    {
        return "Importer";
    }

}
