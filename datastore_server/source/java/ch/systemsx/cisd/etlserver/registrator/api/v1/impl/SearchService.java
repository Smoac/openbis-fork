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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.collection.CollectionUtils.ICollectionFilter;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedBasicOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IMaterialImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISearchService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IVocabularyImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.MaterialIdentifierCollection;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SearchService implements ISearchService
{
    private final IEncapsulatedBasicOpenBISService openBisService;

    public SearchService(IEncapsulatedBasicOpenBISService openBisService)
    {
        this.openBisService = openBisService;
    }

    @Override
    public List<IExperimentImmutable> listExperiments(String projectIdentifierString)
    {
        ProjectIdentifier projectIdentifier =
                new ProjectIdentifierFactory(projectIdentifierString.toUpperCase())
                        .createIdentifier();
        List<Experiment> serverExperiments = openBisService.listExperiments(projectIdentifier);
        ArrayList<IExperimentImmutable> experiments =
                new ArrayList<IExperimentImmutable>(serverExperiments.size());
        for (Experiment experiment : serverExperiments)
        {
            experiments.add(new ExperimentImmutable(experiment));
        }
        return experiments;
    }

    @Override
    public List<IDataSetImmutable> searchForDataSets(String property, String value,
            String typeOrNull)
    {
        return searchForDataSets(property, value, typeOrNull, false);
    }

    @Override
    public List<IDataSetImmutable> searchForDataSets(String property, String value,
            String typeOrNull, boolean escape)
    {
        SearchCriteria sc = new SearchCriteria();
        if (null != typeOrNull)
        {
            sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                    typeOrNull));
        }
        sc.addMatchClause(MatchClause.createPropertyMatch(property,
                escape ? MatchClause.escape(value) : value));
        return searchForDataSets(sc);
    }

    @Override
    public List<ISampleImmutable> searchForSamples(String property, String value, String typeOrNull)
    {
        return searchForSamples(property, value, typeOrNull, false);
    }

    @Override
    public List<ISampleImmutable> searchForSamples(String property, String value,
            String typeOrNull, boolean escape)
    {
        SearchCriteria sc = new SearchCriteria();
        if (null != typeOrNull)
        {
            sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                    typeOrNull));
        }
        sc.addMatchClause(MatchClause.createPropertyMatch(property,
                escape ? MatchClause.escape(value) : value));
        return searchForSamples(sc);
    }

    @Override
    public List<IDataSetImmutable> searchForDataSets(SearchCriteria searchCriteria)
    {
        List<ExternalData> serverDataSets = openBisService.searchForDataSets(searchCriteria);
        ArrayList<IDataSetImmutable> dataSets =
                new ArrayList<IDataSetImmutable>(serverDataSets.size());
        for (ExternalData dataSet : serverDataSets)
        {
            dataSets.add(new DataSetImmutable(dataSet, openBisService));
        }
        return dataSets;
    }

    @Override
    public List<ISampleImmutable> searchForSamples(SearchCriteria searchCriteria)
    {
        List<Sample> serverSamples = openBisService.searchForSamples(searchCriteria);
        ArrayList<ISampleImmutable> samples = new ArrayList<ISampleImmutable>(serverSamples.size());
        for (Sample sample : serverSamples)
        {
            // Search for samples only returns the basic object with with properties.
            samples.add(new SampleImmutable(sample, EnumSet.of(SampleFetchOption.BASIC,
                    SampleFetchOption.PROPERTIES)));
        }
        return samples;
    }

    @Override
    public List<IMaterialImmutable> listMaterials(MaterialIdentifierCollection identifierCollection)
    {
        final Set<String> identifiers = new HashSet<String>(identifierCollection.getIdentifiers());
        Set<String> searchedTypes = extractMaterialTypes(identifierCollection);

        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material> accumulatedResults =
                findAllMaterials(searchedTypes);

        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material> filteredByIdentifier =
                CollectionUtils
                        .filter(accumulatedResults,
                                new ICollectionFilter<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material>()
                                    {

                                        @Override
                                        public boolean isPresent(Material element)
                                        {
                                            return identifiers.contains(element.getIdentifier());
                                        }

                                    });
        return translate(filteredByIdentifier);
    }

    private Set<String> extractMaterialTypes(MaterialIdentifierCollection identifierCollection)
    {
        Set<String> searchedTypes = new HashSet<String>();
        for (String stringIdentifier : identifierCollection.getIdentifiers())
        {
            MaterialIdentifier identifier = MaterialIdentifier.tryParseIdentifier(stringIdentifier);
            searchedTypes.add(identifier.getTypeCode());
        }
        return searchedTypes;
    }

    private List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material> findAllMaterials(
            Set<String> searchedTypes)
    {
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material> accumulatedResults =
                new ArrayList<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material>();
        for (String typeCode : searchedTypes)
        {
            MaterialType materialType = new MaterialType();
            materialType.setCode(typeCode);
            List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material> materialsOfType =
                    openBisService.listMaterials(
                            ListMaterialCriteria.createFromMaterialType(materialType), true);
            accumulatedResults.addAll(materialsOfType);
        }
        return accumulatedResults;
    }

    private List<IMaterialImmutable> translate(List<Material> materials)
    {
        List<IMaterialImmutable> result = new ArrayList<IMaterialImmutable>();
        for (Material material : materials)
        {
            result.add(new MaterialImmutable(material));
        }
        return result;
    }

    @Override
    public IVocabularyImmutable searchForVocabulary(String code)
    {
        return getVocabulary(code);
    }

    @Override
    public IVocabularyImmutable getVocabulary(String code)
    {
        Vocabulary vocabulary = openBisService.tryGetVocabulary(code);
        return (vocabulary == null) ? null : new VocabularyImmutable(vocabulary);
    }
}
