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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.Treatment;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class AbundanceColumnDefinitionTable extends AbstractBusinessObject implements IAbundanceColumnDefinitionTable
{
    private final TreatmentFinder treatmentFinder;
    private final Map<Long, AbundanceColumnDefinition> columnDefinitions;

    AbundanceColumnDefinitionTable(IDAOFactory daoFactory,
            IPhosphoNetXDAOFactory specificDAOFactory, Session session)
    {
        super(daoFactory, specificDAOFactory, session);
        treatmentFinder = new TreatmentFinder();
        columnDefinitions = new TreeMap<Long, AbundanceColumnDefinition>();
    }

    public void add(Sample sample)
    {
        Sample parent = sample.getGeneratedFrom();
        Sample sampleOrParent = parent == null ? sample : parent;
        Long sampleID = sampleOrParent.getId();
        AbundanceColumnDefinition columnDefinition = columnDefinitions.get(sampleID);
        if (columnDefinition == null)
        {
            columnDefinition = new AbundanceColumnDefinition();
            columnDefinition.addSampleID(sampleID);
            columnDefinition.setSampleCode(sampleOrParent.getCode());
            columnDefinition.setTreatments(treatmentFinder.findTreatmentsOf(sampleOrParent));
            columnDefinitions.put(sampleID, columnDefinition);
        }
    }

    public List<AbundanceColumnDefinition> getSortedAndAggregatedDefinitions(
            String treatmentTypeOrNull)
    {
        Collection<AbundanceColumnDefinition> values = columnDefinitions.values();
        List<AbundanceColumnDefinition> definitions = new ArrayList<AbundanceColumnDefinition>();
        if (treatmentTypeOrNull == null)
        {
            definitions.addAll(values);
        } else
        {
            Collection<List<AbundanceColumnDefinition>> groupedDefinitions =
                    groupDefinitions(values, definitions, treatmentTypeOrNull);
            for (List<AbundanceColumnDefinition> group : groupedDefinitions)
            {
                AbundanceColumnDefinition definition = new AbundanceColumnDefinition();
                AbundanceColumnDefinition firstDefinition = group.get(0);
                Treatment treatment =
                        tryToFindTreatmentByTypeCode(firstDefinition, treatmentTypeOrNull);
                definition.setTreatments(Arrays.asList(treatment));
                for (AbundanceColumnDefinition abundanceColumnDefinition : group)
                {
                    definition.addSampleIDsOf(abundanceColumnDefinition);
                }
                definitions.add(definition);
            }
        }

        Collections.sort(definitions);
        return definitions;
    }

    private Collection<List<AbundanceColumnDefinition>> groupDefinitions(
            Collection<AbundanceColumnDefinition> values,
            List<AbundanceColumnDefinition> definitions, String treatmentType)
    {
        Map<String, List<AbundanceColumnDefinition>> groupedDefinitions =
                new HashMap<String, List<AbundanceColumnDefinition>>();
        for (AbundanceColumnDefinition definition : values)
        {
            Treatment treatment = tryToFindTreatmentByTypeCode(definition, treatmentType);
            if (treatment == null)
            {
                definitions.add(definition);
            } else
            {
                String treatmentValue = treatment.getValue();
                List<AbundanceColumnDefinition> list = groupedDefinitions.get(treatmentValue);
                if (list == null)
                {
                    list = new ArrayList<AbundanceColumnDefinition>();
                    groupedDefinitions.put(treatmentValue, list);
                }
                list.add(definition);
            }
        }
        return groupedDefinitions.values();
    }

    private Treatment tryToFindTreatmentByTypeCode(AbundanceColumnDefinition definition,
            String typeCode)
    {
        List<Treatment> treatments = definition.getTreatments();
        for (Treatment treatment : treatments)
        {
            if (treatment.getTypeCode().equals(typeCode))
            {
                return treatment;
            }
        }
        return null;
    }

}
