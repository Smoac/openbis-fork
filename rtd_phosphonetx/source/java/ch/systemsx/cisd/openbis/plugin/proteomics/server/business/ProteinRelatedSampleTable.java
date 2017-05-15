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

package ch.systemsx.cisd.openbis.plugin.proteomics.server.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.lemnik.eodsql.DataSet;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.EntityPropertyTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.Occurrence;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.OccurrenceUtil;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinRelatedSample;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto.AbstractSample;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto.SampleAbundance;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto.SamplePeptideModification;

/**
 * Implementation of {@link IProteinRelatedSampleTable}.
 * 
 * @author Franz-Josef Elmer
 */
class ProteinRelatedSampleTable implements IProteinRelatedSampleTable
{
    private final IDAOFactory daoFactory;

    private final IPhosphoNetXDAOFactory specificDAOFactory;

    private final IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    private List<ProteinRelatedSample> result;

    ProteinRelatedSampleTable(IDAOFactory daoFactory, IPhosphoNetXDAOFactory specificDAOFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        this.daoFactory = daoFactory;
        this.specificDAOFactory = specificDAOFactory;
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
    }

    @Override
    public List<ProteinRelatedSample> getSamples()
    {
        return result;
    }

    @Override
    public void load(TechId experimentID, TechId proteinReferenceID, String sequenceOrNull)
    {
        String experimentPermID =
                daoFactory.getExperimentDAO().getByTechId(experimentID).getPermId();
        IProteinQueryDAO proteinQueryDAO = specificDAOFactory.getProteinQueryDAO(experimentID);
        Map<String, List<SampleAbundance>> sampleAbundanceMap =
                createSampleMap(proteinQueryDAO.listSampleAbundanceByProtein(experimentPermID,
                        proteinReferenceID.getId()));
        Map<String, List<SamplePeptideModification>> samplePeptideModificationMap =
                createSampleMap(proteinQueryDAO.listSamplePeptideModificatioByProtein(
                        experimentPermID, proteinReferenceID.getId()));
        result = new ArrayList<ProteinRelatedSample>();
        SampleIDProvider sampleIDProvider = new SampleIDProvider(daoFactory.getSampleDAO());
        Map<PropertyTypePE, PropertyType> cache = new HashMap<PropertyTypePE, PropertyType>();
        Map<MaterialTypePE, MaterialType> materialTypeCache = new HashMap<MaterialTypePE, MaterialType>();
        for (Entry<String, List<SampleAbundance>> entry : sampleAbundanceMap.entrySet())
        {
            String key = entry.getKey();
            SamplePE sample = sampleIDProvider.getSampleOrParentSample(key);
            List<SampleAbundance> sampleAbundances = entry.getValue();
            List<SamplePeptideModification> samplePeptideModifications =
                    samplePeptideModificationMap.get(key);
            if (samplePeptideModifications == null)
            {
                for (SampleAbundance sampleAbundance : sampleAbundances)
                {
                    ProteinRelatedSample s = createFrom(sample, materialTypeCache, cache);
                    s.setAbundance(sampleAbundance.getAbundance());
                    result.add(s);
                }
            } else
            {
                for (SampleAbundance sampleAbundance : sampleAbundances)
                {
                    Double abundance = sampleAbundance.getAbundance();
                    result.addAll(createSamplesForPeptideModifications(samplePeptideModifications,
                            sample, abundance, sequenceOrNull, materialTypeCache, cache));
                }
            }
        }
        for (Entry<String, List<SamplePeptideModification>> entry : samplePeptideModificationMap
                .entrySet())
        {
            String key = entry.getKey();
            if (sampleAbundanceMap.containsKey(key) == false)
            {
                SamplePE sample = sampleIDProvider.getSampleOrParentSample(key);
                List<SamplePeptideModification> samplePeptideModifications = entry.getValue();
                result.addAll(createSamplesForPeptideModifications(samplePeptideModifications,
                        sample, null, sequenceOrNull, materialTypeCache, cache));
            }
        }
    }

    private List<ProteinRelatedSample> createSamplesForPeptideModifications(
            List<SamplePeptideModification> samplePeptideModifications, SamplePE sample,
            Double abundanceOrNull, String sequenceOrNull, 
            Map<MaterialTypePE, MaterialType> materialTypeCache, Map<PropertyTypePE, PropertyType> cache)
    {
        List<ProteinRelatedSample> samples = new ArrayList<ProteinRelatedSample>();
        for (SamplePeptideModification samplePeptideModification : samplePeptideModifications)
        {
            int position = samplePeptideModification.getPosition();
            if (sequenceOrNull != null)
            {
                List<Occurrence> occurances =
                        OccurrenceUtil.findAllOccurrences(sequenceOrNull,
                                samplePeptideModification.getSequence());
                for (Occurrence occurrence : occurances)
                {
                    samples.add(createProteinRelatedSample(samplePeptideModification, sample,
                            abundanceOrNull, position + occurrence.getStartIndex(), materialTypeCache, cache));
                }
            } else
            {
                samples.add(createProteinRelatedSample(samplePeptideModification, sample,
                        abundanceOrNull, position, materialTypeCache, cache));
            }
        }
        return samples;
    }

    private ProteinRelatedSample createProteinRelatedSample(
            SamplePeptideModification samplePeptideModification, SamplePE sample,
            Double abundanceOrNull, int position, 
            Map<MaterialTypePE, MaterialType> materialTypeCache, Map<PropertyTypePE, PropertyType> cache)
    {
        ProteinRelatedSample s = createFrom(sample, materialTypeCache, cache);
        s.setAbundance(abundanceOrNull);
        int index = samplePeptideModification.getPosition() - 1;
        String sequence = samplePeptideModification.getSequence();
        if (index >= 0 && index < sequence.length())
        {
            s.setModifiedAminoAcid(sequence.charAt(index));
        }
        s.setModificationFraction(samplePeptideModification.getFraction());
        s.setModificationMass(samplePeptideModification.getMass());
        s.setModificationPosition((long) position);
        return s;
    }

    private ProteinRelatedSample createFrom(SamplePE sample, Map<MaterialTypePE, MaterialType> materialTypeCache, 
            Map<PropertyTypePE, PropertyType> cache)
    {
        ProteinRelatedSample s = new ProteinRelatedSample();
        s.setCode(sample.getCode());
        s.setEntityType(SampleTypeTranslator.translate(sample.getSampleType(), materialTypeCache, cache));
        s.setId(sample.getId());
        s.setIdentifier(sample.getIdentifier());
        s.setPermId(sample.getPermId());
        s.setProperties(EntityPropertyTranslator.translate(sample.getProperties(), materialTypeCache, cache,
                managedPropertyEvaluatorFactory));
        return s;
    }

    private <T extends AbstractSample> Map<String, List<T>> createSampleMap(DataSet<T> items)
    {
        Map<String, List<T>> map = new LinkedHashMap<String, List<T>>();
        try
        {
            for (T item : items)
            {
                String samplePermID = item.getSamplePermID();
                List<T> list = map.get(samplePermID);
                if (list == null)
                {
                    list = new ArrayList<T>();
                    map.put(samplePermID, list);
                }
                list.add(item);
            }
        } finally
        {
            items.close();
        }
        return map;
    }

}
