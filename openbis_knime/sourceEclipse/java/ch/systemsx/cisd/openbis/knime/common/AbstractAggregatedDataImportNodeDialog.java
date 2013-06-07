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

package ch.systemsx.cisd.openbis.knime.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;

/**
 * Abstract super class of nodes dialogs for importing data from an aggregation service.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractAggregatedDataImportNodeDialog
        extends AbstractParameterDescriptionBasedNodeDialog<AggregatedDataImportDescription>
{
    protected AbstractAggregatedDataImportNodeDialog(String tabTitle)
    {
        super(tabTitle);
    }

    @Override
    protected List<FieldDescription> getFieldDescriptions(
            AggregatedDataImportDescription description)
    {
        return Util.getFieldDescriptions(createFacade(), description, logger);
    }

    @Override
    protected List<AggregatedDataImportDescription> getSortedDescriptions(IQueryApiFacade facade)
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
        List<AggregatedDataImportDescription> descriptions =
                new ArrayList<AggregatedDataImportDescription>();
        Set<String> dssCodes = new HashSet<String>();

        for (AggregationServiceDescription aggregationServiceDescription : services)
        {
            addDescription(descriptions, aggregationServiceDescription);
            dssCodes.add(aggregationServiceDescription.getDataStoreCode());
        }

        if (dssCodes.size() > 1)
        {
            for (AggregatedDataImportDescription description : descriptions)
            {
                description.setShowFullDescription(true);
            }
        }

        return descriptions;
    }

    protected abstract void addDescription(List<AggregatedDataImportDescription> descriptions,
            AggregationServiceDescription aggregationServiceDescription);

    @Override
    protected String getDescriptionKey()
    {
        return AggregatedDataImportDescription.AGGREGATION_DESCRIPTION_KEY;
    }

    @Override
    protected String getParametersSectionLabel()
    {
        return "Service Parameters";
    }

}
