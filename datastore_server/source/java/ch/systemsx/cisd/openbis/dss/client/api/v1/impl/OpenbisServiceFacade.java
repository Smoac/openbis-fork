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
import java.util.EnumSet;
import java.util.List;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.api.client.ServiceFinder;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DssComponentFactory;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.SearchOperator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
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
    public static OpenbisServiceFacade tryCreate(String username, String password,
            String openbisUrl,
            long timeoutInMillis)
    {
        ServiceFinder generalInformationServiceFinder =
                new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
        IGeneralInformationService service =
                generalInformationServiceFinder.createService(IGeneralInformationService.class,
                        openbisUrl, timeoutInMillis);

        // TODO KE: wrap the facade into a re-authenticating java.lang.reflect.Proxy
        // this will hide any re-authentication complexity from the clients
        String token = service.tryToAuthenticateForAllServices(username, password);
        if (token == null)
        {
            throw UserFailureException.fromTemplate(
                    "Failed to authenticate user '%s' against the openBIS at '%s'.", username,
                    openbisUrl);
        }

        IDssComponent dssComponent =
                DssComponentFactory.tryCreate(token, openbisUrl, timeoutInMillis);
        return new OpenbisServiceFacade(token, service, dssComponent);
    }

    private final String sessionToken;

    private final IGeneralInformationService service;

    private final IDssComponent dssComponent;

    /**
     * ctor.
     */
    OpenbisServiceFacade(String sessionToken, IGeneralInformationService service,
            IDssComponent dssComponent)
    {
        this.sessionToken = sessionToken;
        this.service = service;
        this.dssComponent = dssComponent;
    }

    //
    // ISimpleOpenbisServiceFacade
    //

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
        List<Project> projects = new ArrayList<Project>();
        for (ProjectIdentifier identifier : parseProjectIdentifiers(projectIdentifiers))
        {
            Project project = new Project(identifier.getSpaceCode(), identifier.getProjectCode());
            projects.add(project);
        }

        return service.listExperiments(sessionToken, projects, null);
    }

    public List<Sample> getSamples(final List<String> sampleIdentifiers)
            throws EnvironmentFailureException
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setOperator(SearchOperator.MATCH_ANY_CLAUSES);
        for (SampleIdentifier sampleIdentifier : parseSampleIdentifiers(sampleIdentifiers))
        {
            searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                    MatchClauseAttribute.CODE,
                    sampleIdentifier.getSampleCode()));
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
                    MatchClauseAttribute.PROJECT,
                    projectIdentifier.getProjectCode()));
        }

        List<Sample> samples = service.searchForSamples(sessionToken, searchCriteria);
        return samples;
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

        List<DataSet> samples = service.searchForDataSets(sessionToken, searchCriteria);
        return samples;
    }

    public List<DataSet> listDataSetsForExperiments(final List<String> experimentIdentifiers)
            throws EnvironmentFailureException
    {
        SearchCriteria searchCriteria =
                searchCriteriaForExperimentIdentifiers(experimentIdentifiers);

        List<DataSet> dataSets = service.searchForDataSets(sessionToken, searchCriteria);
        List<DataSet> filteredSamples =
                CollectionUtils.filter(dataSets, new CollectionUtils.ICollectionFilter<DataSet>()
                    {
                        public boolean isPresent(DataSet dataSet)
                        {
                            String identifier = dataSet.getExperimentIdentifier();
                            return identifier != null && experimentIdentifiers.contains(identifier);
                        }
                    });
        return filteredSamples;
    }

    public List<DataSet> listDataSetsForSamples(final List<String> sampleIdentifiers)
            throws EnvironmentFailureException
    {
        SearchCriteria searchCriteria = searchCriteriaForSampleIdentifiers(sampleIdentifiers);
        List<DataSet> dataSets = service.searchForDataSets(sessionToken, searchCriteria);
        List<DataSet> filteredSamples =
                CollectionUtils.filter(dataSets, new CollectionUtils.ICollectionFilter<DataSet>()
                    {
                        public boolean isPresent(DataSet dataSet)
                        {
                            String identifier = dataSet.getSampleIdentifierOrNull();
                            return identifier != null && sampleIdentifiers.contains(identifier);
                        }
                    });
        return filteredSamples;
    }

    public IDataSetDss getDataSetDss(String code) throws EnvironmentFailureException
    {
        return dssComponent.getDataSet(code);
    }

    public IDataSetDss putDataSet(NewDataSetDTO newDataset, File dataSetFile)
            throws EnvironmentFailureException
    {
        return dssComponent.putDataSet(newDataset, dataSetFile);
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
        return service.searchForDataSets(sessionToken, searchCriteria);
    }

    public List<DataSet> listDataSets(List<Sample> samples, EnumSet<Connections> connectionsToGet)
    {
        return service.listDataSets(sessionToken, samples, connectionsToGet);
    }
}
