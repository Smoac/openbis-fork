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

package ch.systemsx.cisd.openbis.dss.client.api.v1.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.api.client.ServiceFinder;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.retry.RetryCaller;
import ch.systemsx.cisd.common.retry.RetryProxyFactory;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DssComponentFactory;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.NewVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.SearchOperator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Default implementation for {@link IOpenbisServiceFacade}.
 * 
 * @author Kaloyan Enimanev
 */
public class OpenbisServiceFacade implements IOpenbisServiceFacade
{

    @Private
    public static IOpenbisServiceFacade tryCreate(final String username, final String password,
            final String openbisUrl, final long timeoutInMillis)
    {
        RetryCaller<IOpenbisServiceFacade, RuntimeException> caller =
                new RetryCaller<IOpenbisServiceFacade, RuntimeException>()
                    {
                        @Override
                        protected IOpenbisServiceFacade call()
                        {
                            IGeneralInformationService service =
                                    createGeneralInformationService(openbisUrl, timeoutInMillis);
                            IGeneralInformationChangingService changingService =
                                    createGeneralInformationChangingService(openbisUrl,
                                            timeoutInMillis);

                            // TODO KE: wrap the facade into a re-authenticating
                            // java.lang.reflect.Proxy
                            // this will hide any re-authentication complexity from the clients
                            String token =
                                    service.tryToAuthenticateForAllServices(username, password);
                            if (token == null)
                            {
                                throw UserFailureException
                                        .fromTemplate(
                                                "Failed to authenticate user '%s' against the openBIS at '%s'.",
                                                username, openbisUrl);
                            }

                            IDssComponent dssComponent =
                                    DssComponentFactory.tryCreate(token, openbisUrl,
                                            timeoutInMillis);
                            IOpenbisServiceFacade facade =
                                    new OpenbisServiceFacade(token, service, changingService,
                                            dssComponent);

                            return RetryProxyFactory.createProxy(facade);
                        }
                    };
        return caller.callWithRetry();
    }

    @Private
    public static IOpenbisServiceFacade tryCreate(final String sessionToken,
            final String openbisUrl, final long timeoutInMillis)
    {
        RetryCaller<IOpenbisServiceFacade, RuntimeException> caller =
                new RetryCaller<IOpenbisServiceFacade, RuntimeException>()
                    {
                        @Override
                        protected IOpenbisServiceFacade call()
                        {
                            IGeneralInformationService service =
                                    createGeneralInformationService(openbisUrl, timeoutInMillis);
                            IGeneralInformationChangingService changingService =
                                    createGeneralInformationChangingService(openbisUrl,
                                            timeoutInMillis);

                            IDssComponent dssComponent =
                                    DssComponentFactory.tryCreate(sessionToken, openbisUrl,
                                            timeoutInMillis);
                            IOpenbisServiceFacade facade =
                                    new OpenbisServiceFacade(sessionToken, service,
                                            changingService, dssComponent);

                            return RetryProxyFactory.createProxy(facade);
                        }
                    };
        return caller.callWithRetry();
    }

    private static IGeneralInformationService createGeneralInformationService(String openbisUrl,
            long timeoutInMillis)
    {
        ServiceFinder generalInformationServiceFinder =
                new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
        IGeneralInformationService service =
                generalInformationServiceFinder.createService(IGeneralInformationService.class,
                        openbisUrl, timeoutInMillis);
        return service;
    }

    private static IGeneralInformationChangingService createGeneralInformationChangingService(
            String openbisUrl, long timeoutInMillis)
    {
        ServiceFinder generalInformationServiceFinder =
                new ServiceFinder("openbis", IGeneralInformationChangingService.SERVICE_URL);
        IGeneralInformationChangingService service =
                generalInformationServiceFinder.createService(
                        IGeneralInformationChangingService.class, openbisUrl, timeoutInMillis);
        return service;
    }

    private final String sessionToken;

    private final IGeneralInformationService service;

    private final IGeneralInformationChangingService changingService;

    private final IDssComponent dssComponent;

    /**
     * ctor.
     */
    public OpenbisServiceFacade(String sessionToken, IGeneralInformationService service,
            IGeneralInformationChangingService changingService, IDssComponent dssComponent)
    {
        this.sessionToken = sessionToken;
        this.service = service;
        this.changingService = changingService;
        this.dssComponent = dssComponent;
    }

    //
    // ISimpleOpenbisServiceFacade
    //

    public List<Project> listProjects()
    {
        return service.listProjects(sessionToken);
    }

    public List<SpaceWithProjectsAndRoleAssignments> getSpacesWithProjects()
            throws EnvironmentFailureException
    {
        return service.listSpacesWithProjectsAndRoleAssignments(sessionToken, null);
    }

    public List<Experiment> getExperiments(List<String> experimentIdentifiers)
            throws EnvironmentFailureException
    {
        return service.listExperiments(sessionToken, experimentIdentifiers);
    }

    public List<Experiment> listExperimentsForProjects(List<String> projectIdentifiers)
            throws EnvironmentFailureException
    {
        return service.listExperiments(sessionToken, getProjects(projectIdentifiers), null);
    }

    public List<Experiment> listExperimentsHavingSamplesForProjects(List<String> projectIdentifiers)
            throws EnvironmentFailureException
    {
        return service.listExperimentsHavingSamples(sessionToken, getProjects(projectIdentifiers),
                null);
    }

    public List<Experiment> listExperimentsHavingDataSetsForProjects(List<String> projectIdentifiers)
            throws EnvironmentFailureException
    {
        return service.listExperimentsHavingDataSets(sessionToken, getProjects(projectIdentifiers),
                null);
    }

    private List<Project> getProjects(List<String> projectIdentifiers)
    {
        List<Project> projects = new ArrayList<Project>();
        for (ProjectIdentifier identifier : parseProjectIdentifiers(projectIdentifiers))
        {
            Project project = new Project(identifier.getSpaceCode(), identifier.getProjectCode());
            projects.add(project);
        }

        return projects;
    }

    public List<Sample> getSamples(final List<String> sampleIdentifiers)
            throws EnvironmentFailureException
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setOperator(SearchOperator.MATCH_ANY_CLAUSES);
        for (SampleIdentifier sampleIdentifier : parseSampleIdentifiers(sampleIdentifiers))
        {
            searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                    MatchClauseAttribute.CODE, sampleIdentifier.getSampleCode()));
        }

        List<Sample> samples = service.searchForSamples(sessionToken, searchCriteria);
        List<Sample> filteredSamples =
                CollectionUtils.filter(samples, new CollectionUtils.ICollectionFilter<Sample>()
                    {
                        public boolean isPresent(Sample element)
                        {
                            String identifier = element.getIdentifier();
                            return identifier != null && sampleIdentifiers.contains(identifier);
                        }
                    });
        return filteredSamples;
    }

    public List<Sample> listSamplesForExperiments(final List<String> experimentIdentifiers)
            throws EnvironmentFailureException
    {
        SearchCriteria searchCriteria =
                searchCriteriaForExperimentIdentifiers(experimentIdentifiers);

        List<Sample> samples = service.searchForSamples(sessionToken, searchCriteria);
        List<Sample> filteredSamples =
                CollectionUtils.filter(samples, new CollectionUtils.ICollectionFilter<Sample>()
                    {
                        public boolean isPresent(Sample element)
                        {
                            String identifier = element.getExperimentIdentifierOrNull();
                            return identifier != null && experimentIdentifiers.contains(identifier);
                        }
                    });
        return filteredSamples;
    }

    public List<Sample> listSamplesForProjects(List<String> projectIdentifiers)
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setOperator(SearchOperator.MATCH_ANY_CLAUSES);
        for (ProjectIdentifier projectIdentifier : parseProjectIdentifiers(projectIdentifiers))
        {
            searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                    MatchClauseAttribute.PROJECT, projectIdentifier.getProjectCode()));
        }

        List<Sample> samples = service.searchForSamples(sessionToken, searchCriteria);
        return samples;
    }

    public DataSet getDataSet(String dataSetCode) throws EnvironmentFailureException
    {
        List<DataSet> dataSets = getDataSets(Collections.singletonList(dataSetCode));
        return (dataSets.size() > 0) ? dataSets.get(0) : null;
    }

    public List<DataSet> getDataSets(List<String> dataSetCodes) throws EnvironmentFailureException
    {
        enforceNotEmpty("Dataset codes", dataSetCodes);
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setOperator(SearchOperator.MATCH_ANY_CLAUSES);
        for (String dataSetCode : dataSetCodes)
        {
            searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                    MatchClauseAttribute.CODE, dataSetCode));
        }

        return convertDataSets(service.searchForDataSets(sessionToken, searchCriteria));
    }

    public List<DataSet> listDataSetsForExperiments(final List<String> experimentIdentifiers)
            throws EnvironmentFailureException
    {
        SearchCriteria searchCriteria =
                searchCriteriaForExperimentIdentifiers(experimentIdentifiers);

        List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> dataSets =
                service.searchForDataSets(sessionToken, searchCriteria);
        List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> filteredDataSets =
                CollectionUtils
                        .filter(dataSets,
                                new CollectionUtils.ICollectionFilter<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet>()
                                    {
                                        public boolean isPresent(
                                                ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet dataSet)
                                        {
                                            String identifier = dataSet.getExperimentIdentifier();
                                            return identifier != null
                                                    && experimentIdentifiers.contains(identifier);
                                        }
                                    });
        return convertDataSets(filteredDataSets);
    }

    public List<DataSet> listDataSetsForSamples(final List<String> sampleIdentifiers)
            throws EnvironmentFailureException
    {
        SearchCriteria searchCriteria = searchCriteriaForSampleIdentifiers(sampleIdentifiers);
        List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> dataSets =
                service.searchForDataSets(sessionToken, searchCriteria);
        List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> filteredDataSets =
                CollectionUtils
                        .filter(dataSets,
                                new CollectionUtils.ICollectionFilter<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet>()
                                    {
                                        public boolean isPresent(
                                                ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet dataSet)
                                        {
                                            String identifier = dataSet.getSampleIdentifierOrNull();
                                            return identifier != null
                                                    && sampleIdentifiers.contains(identifier);
                                        }
                                    });
        return convertDataSets(filteredDataSets);
    }

    public List<DataSetType> listDataSetTypes()
    {
        return service.listDataSetTypes(sessionToken);
    }

    public IDataSetDss getDataSetDss(String code) throws EnvironmentFailureException
    {
        return RetryProxyFactory.createProxy(dssComponent.getDataSet(code));
    }

    public DataSet putDataSet(NewDataSetDTO newDataset, File dataSetFile)
            throws EnvironmentFailureException
    {
        IDataSetDss dataSetDss = dssComponent.putDataSet(newDataset, dataSetFile);
        return RetryProxyFactory.createProxy(new DataSet(this, dssComponent, null, dataSetDss));
    }

    public List<ValidationError> validateDataSet(NewDataSetDTO newDataset, File dataSetFile)
            throws IllegalStateException, EnvironmentFailureException
    {
        return dssComponent.validateDataSet(newDataset, dataSetFile);
    }

    public Map<String, String> extractMetadata(NewDataSetDTO newDataset, File dataSetFile)
            throws IllegalStateException, EnvironmentFailureException
    {
        return dssComponent.extractMetadata(newDataset, dataSetFile);
    }

    public void checkSession() throws InvalidSessionException
    {
        dssComponent.checkSession();
    }

    public synchronized void logout()
    {
        service.logout(sessionToken);
    }

    private List<ProjectIdentifier> parseProjectIdentifiers(List<String> identifiers)
    {
        enforceNotEmpty("Project identifiers", identifiers);
        ArrayList<ProjectIdentifier> list = new ArrayList<ProjectIdentifier>();
        for (String identifier : identifiers)
        {
            ProjectIdentifier projectIdentifier =
                    new ProjectIdentifierFactory(identifier).createIdentifier();
            list.add(projectIdentifier);
        }
        return list;
    }

    private List<ExperimentIdentifier> parseExperimentIdentifiers(List<String> identifiers)
    {
        enforceNotEmpty("Experiment identifiers", identifiers);
        return ExperimentIdentifierFactory.parse(identifiers);
    }

    private List<SampleIdentifier> parseSampleIdentifiers(List<String> identifiers)
    {
        enforceNotEmpty("Sample identifiers", identifiers);
        ArrayList<SampleIdentifier> list = new ArrayList<SampleIdentifier>();
        for (String identifier : identifiers)
        {
            SampleIdentifier sampleIdentifier =
                    new SampleIdentifierFactory(identifier).createIdentifier();
            list.add(sampleIdentifier);
        }
        return list;
    }

    private SearchCriteria searchCriteriaForExperimentIdentifiers(
            final List<String> experimentIdentifiers)
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setOperator(SearchOperator.MATCH_ANY_CLAUSES);
        for (ExperimentIdentifier experimentIdentifier : parseExperimentIdentifiers(experimentIdentifiers))
        {
            SearchCriteria experimentCriteria = new SearchCriteria();
            experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(
                    MatchClauseAttribute.CODE, experimentIdentifier.getExperimentCode()));
            searchCriteria.addSubCriteria(SearchSubCriteria
                    .createExperimentCriteria(experimentCriteria));
        }
        return searchCriteria;
    }

    private SearchCriteria searchCriteriaForSampleIdentifiers(final List<String> sampleIdentifiers)
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setOperator(SearchOperator.MATCH_ANY_CLAUSES);
        for (SampleIdentifier sampleIdentifier : parseSampleIdentifiers(sampleIdentifiers))
        {
            SearchCriteria sampleCriteria = new SearchCriteria();
            sampleCriteria.addMatchClause(MatchClause.createAttributeMatch(
                    MatchClauseAttribute.CODE, sampleIdentifier.getSampleCode()));
            searchCriteria.addSubCriteria(SearchSubCriteria.createSampleCriteria(sampleCriteria));
        }
        return searchCriteria;
    }

    private void enforceNotEmpty(String parameterName, List<String> identifiers)
    {
        if (identifiers == null || identifiers.isEmpty())
        {
            throw new IllegalArgumentException(parameterName
                    + " must contain at least one element.");
        }
    }

    //
    // IOpenbisServiceFacade
    //
    public List<Sample> searchForSamples(SearchCriteria searchCriteria)
    {
        return service.searchForSamples(sessionToken, searchCriteria);
    }

    public List<DataSet> searchForDataSets(SearchCriteria searchCriteria)
    {
        return convertDataSets(service.searchForDataSets(sessionToken, searchCriteria));
    }

    public List<DataSet> listDataSets(List<Sample> samples, EnumSet<Connections> connectionsToGet)
    {
        return convertDataSets(service.listDataSets(sessionToken, samples, connectionsToGet));
    }

    private List<DataSet> convertDataSets(
            List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> internalDataSets)
    {
        ArrayList<DataSet> convertedDataSets = new ArrayList<DataSet>(internalDataSets.size());
        for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet dataSet : internalDataSets)
        {
            DataSet converted = new DataSet(this, dssComponent, dataSet, null);
            convertedDataSets.add(RetryProxyFactory.createProxy(converted));
        }

        return convertedDataSets;
    }

    @SuppressWarnings("deprecation")
    public void addAdHocVocabularyTerm(TechId vocabularyId, String code, String label,
            String description, Long previousTermOrdinal)
    {
        changingService.addUnofficialVocabularyTerm(sessionToken, vocabularyId, code, label,
                description, previousTermOrdinal);
    }

    public void addAdHocVocabularyTerm(Long vocabularyId, NewVocabularyTerm term)
    {
        changingService.addUnofficialVocabularyTerm(sessionToken, vocabularyId, term);
    }

    @SuppressWarnings("deprecation")
    public HashMap<Vocabulary, List<VocabularyTerm>> getVocabularyTermsMap()
    {
        return service.getVocabularyTermsMap(sessionToken);
    }

    public List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary> listVocabularies()
    {
        return service.listVocabularies(sessionToken);
    }

}
