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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.DataSetInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment.ExperimentInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.AttributeMatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseFieldType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.SearchOperator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchableEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;

/**
 * @author Kaloyan Enimanev
 */
public class OpenbisServiceFacadeTest extends AssertJUnit
{
    private static final String SESSION_TOKEN = "session-token";

    private Mockery context;

    private IGeneralInformationService service;

    private IDssComponent dssComponent;

    private IOpenbisServiceFacade openbisFacade;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        service = context.mock(IGeneralInformationService.class);
        dssComponent = context.mock(IDssComponent.class);

        openbisFacade = new OpenbisServiceFacade(SESSION_TOKEN, service, dssComponent);
    }

    @AfterMethod
    public void afterMethod(Method m)
    {
        try
        {
            context.assertIsSatisfied();
        } catch (Throwable t)
        {
            throw new Error(m.getName() + "() : ", t);
        }
    }

    @Test
    public final void testGetSpacesWithProjects()
    {
        final List<SpaceWithProjectsAndRoleAssignments> list =
                unmodifiableList(new SpaceWithProjectsAndRoleAssignments("space"));
        context.checking(new Expectations()
            {
                {
                    one(service).listSpacesWithProjectsAndRoleAssignments(SESSION_TOKEN, null);
                    will(returnValue(list));
                }
            });
        List<SpaceWithProjectsAndRoleAssignments> result = openbisFacade.getSpacesWithProjects();
        assertEquals(list, result);
    }

    @Test
    public final void testGetExperiments()
    {
        final List<String> identifiers =
                unmodifiableList(experimentIdentifier("e1"), experimentIdentifier("e2"));
        final List<Experiment> experiments =
                unmodifiableList(createExperiment("e1"), createExperiment("e2"));

        context.checking(new Expectations()
            {
                {
                    one(service).listExperiments(SESSION_TOKEN, identifiers);
                    will(returnValue(experiments));
                }
            });

        List<Experiment> result = openbisFacade.getExperiments(identifiers);
        assertEquals(experiments, result);
    }

    @Test
    public final void testListExperimentsForProjects()
    {
        final List<String> identifiers =
                unmodifiableList(projectIdentifier("PROJECT"), projectIdentifier("p1"));
        final List<Experiment> experiments =
                unmodifiableList(createExperiment("e1"), createExperiment("e2"));

        context.checking(new Expectations()
            {
                {
                    List<Project> projects = new ArrayList<Project>();
                    for (String stringId : identifiers)
                    {
                        ProjectIdentifier id = new ProjectIdentifierFactory(stringId).createIdentifier();
                        projects.add(new Project(id.getSpaceCode(), id.getProjectCode()));
                    }
                    one(service).listExperiments(SESSION_TOKEN, projects, null);
                    will(returnValue(experiments));
                }
            });

        List<Experiment> result = openbisFacade.listExperimentsForProjects(identifiers);
        assertEquals(experiments, result);
    }

    @Test
    public final void testGetSamples()
    {
        final List<String> identifiers =
                unmodifiableList(sampleIdentifier("s1"), sampleIdentifier("s2"));
        final List<Sample> samples =
                unmodifiableList(createSample("s1", null), createSample("s2", null));

        final RecordingMatcher<SearchCriteria> criteriaMatcher =
                new RecordingMatcher<SearchCriteria>();
        context.checking(new Expectations()
            {
                {
                    List<Sample> moreSamples = new ArrayList<Sample>(samples);
                    // will be filtered out in the facade
                    moreSamples.add(createSample("s3", null));
                    one(service)
                            .searchForSamples(with(equal(SESSION_TOKEN)), with(criteriaMatcher));
                    will(returnValue(moreSamples));
                }
            });

        List<Sample> result = openbisFacade.getSamples(identifiers);
        assertEquals(samples, result);
        SearchCriteria criteria = criteriaMatcher.recordedObject();
        assertNotNull(criteria);
        assertEquals(SearchOperator.MATCH_ANY_CLAUSES, criteria.getOperator());
        assertEquals(identifiers.size(), criteria.getMatchClauses().size());
        assertMatchClauseForCode("s1", criteria.getMatchClauses().get(0));
        assertMatchClauseForCode("s2", criteria.getMatchClauses().get(1));
    }

    @Test
    public final void testListSamplesForExperiments()
    {
        final List<String> identifiers =
                unmodifiableList(experimentIdentifier("e1"), experimentIdentifier("e2"));
        final List<Sample> samples =
                unmodifiableList(createSample("s1", identifiers.get(0)),
                        createSample("s2", identifiers.get(0)));

        final RecordingMatcher<SearchCriteria> criteriaMatcher =
                new RecordingMatcher<SearchCriteria>();
        context.checking(new Expectations()
            {
                {
                    List<Sample> moreSamples = new ArrayList<Sample>(samples);
                    // will be filtered out in the facade
                    moreSamples.add(createSample("s3", null));
                    one(service)
                            .searchForSamples(with(equal(SESSION_TOKEN)), with(criteriaMatcher));
                    will(returnValue(moreSamples));
                }
            });

        List<Sample> result = openbisFacade.listSamplesForExperiments(identifiers);
        assertEquals(samples, result);
        SearchCriteria criteria = criteriaMatcher.recordedObject();
        assertNotNull(criteria);
        assertEquals(SearchOperator.MATCH_ANY_CLAUSES, criteria.getOperator());
        assertEquals(identifiers.size(), criteria.getSubCriterias().size());
        SearchSubCriteria subCriteria1 = criteria.getSubCriterias().get(0);
        assertMatchClauseForCode("E1", subCriteria1.getCriteria().getMatchClauses().get(0));
        assertEquals(SearchableEntityKind.EXPERIMENT, subCriteria1.getTargetEntityKind());
        SearchSubCriteria subCriteria2 = criteria.getSubCriterias().get(1);
        assertMatchClauseForCode("E2", subCriteria2.getCriteria().getMatchClauses().get(0));
        assertEquals(SearchableEntityKind.EXPERIMENT, subCriteria2.getTargetEntityKind());
    }

    @Test
    public final void testListSamplesForProjects()
    {
        final List<String> identifiers =
                unmodifiableList(projectIdentifier("P1"), projectIdentifier("P2"));
        final List<Sample> samples =
                unmodifiableList(createSample("S1", null), createSample("S2", null));

        final RecordingMatcher<SearchCriteria> criteriaMatcher =
                new RecordingMatcher<SearchCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(service)
                            .searchForSamples(with(equal(SESSION_TOKEN)), with(criteriaMatcher));
                    will(returnValue(samples));
                }
            });

        List<Sample> result = openbisFacade.listSamplesForProjects(identifiers);
        assertEquals(samples, result);
        SearchCriteria criteria = criteriaMatcher.recordedObject();
        assertNotNull(criteria);
        assertEquals(SearchOperator.MATCH_ANY_CLAUSES, criteria.getOperator());
        assertEquals(identifiers.size(), criteria.getMatchClauses().size());
        assertMatchClauseForProject("P1", criteria.getMatchClauses().get(0));
        assertMatchClauseForProject("P2", criteria.getMatchClauses().get(1));
    }

    @Test
    public final void testGetDataSets()
    {
        final List<String> codes = unmodifiableList("DATA-SET-1", "DATA-SET-2");
        final List<DataSet> dataSets =
                unmodifiableList(createDataSet("DATA-SET-1", experimentIdentifier("E1"), null),
                        createDataSet("DATA-SET-2", experimentIdentifier("E2"), null));

        final RecordingMatcher<SearchCriteria> criteriaMatcher =
                new RecordingMatcher<SearchCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(service).searchForDataSets(with(equal(SESSION_TOKEN)),
                            with(criteriaMatcher));
                    will(returnValue(dataSets));
                }
            });

        List<DataSet> result = openbisFacade.getDataSets(codes);
        assertEquals(dataSets, result);
        SearchCriteria criteria = criteriaMatcher.recordedObject();
        assertNotNull(criteria);
        assertEquals(SearchOperator.MATCH_ANY_CLAUSES, criteria.getOperator());
        assertEquals(codes.size(), criteria.getMatchClauses().size());
        assertMatchClauseForCode("DATA-SET-1", criteria.getMatchClauses().get(0));
        assertMatchClauseForCode("DATA-SET-2", criteria.getMatchClauses().get(1));
    }

    @Test
    public final void testListDataSetsForExperiments()
    {
        final List<String> identifiers =
                unmodifiableList(experimentIdentifier("E1"), experimentIdentifier("E2"));
        final List<DataSet> dataSets =
                unmodifiableList(createDataSet("DATA-SET-1", identifiers.get(0), null),
                        createDataSet("DATA-SET-2", identifiers.get(1), null));

        final RecordingMatcher<SearchCriteria> criteriaMatcher =
                new RecordingMatcher<SearchCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(service).searchForDataSets(with(equal(SESSION_TOKEN)),
                            with(criteriaMatcher));
                    will(returnValue(dataSets));
                }
            });

        List<DataSet> result = openbisFacade.listDataSetsForExperiments(identifiers);
        assertEquals(dataSets, result);
        SearchCriteria criteria = criteriaMatcher.recordedObject();
        assertNotNull(criteria);
        assertEquals(SearchOperator.MATCH_ANY_CLAUSES, criteria.getOperator());
        assertEquals(identifiers.size(), criteria.getSubCriterias().size());
        SearchSubCriteria subCriteria1 = criteria.getSubCriterias().get(0);
        assertMatchClauseForCode("E1", subCriteria1.getCriteria().getMatchClauses().get(0));
        assertEquals(SearchableEntityKind.EXPERIMENT, subCriteria1.getTargetEntityKind());
        SearchSubCriteria subCriteria2 = criteria.getSubCriterias().get(1);
        assertMatchClauseForCode("E2", subCriteria2.getCriteria().getMatchClauses().get(0));
        assertEquals(SearchableEntityKind.EXPERIMENT, subCriteria2.getTargetEntityKind());
    }

    @Test
    public final void testListDataSetsForSamples()
    {
        final List<String> identifiers =
                unmodifiableList(sampleIdentifier("S1"), sampleIdentifier("S2"));
        final List<DataSet> dataSets =
                unmodifiableList(
                        createDataSet("DATA-SET-1", experimentIdentifier("E1"), identifiers.get(0)),
                        createDataSet("DATA-SET-2", experimentIdentifier("E2"), identifiers.get(1)));

        final RecordingMatcher<SearchCriteria> criteriaMatcher =
                new RecordingMatcher<SearchCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(service).searchForDataSets(with(equal(SESSION_TOKEN)),
                            with(criteriaMatcher));
                    will(returnValue(dataSets));
                }
            });

        List<DataSet> result = openbisFacade.listDataSetsForSamples(identifiers);
        assertEquals(dataSets, result);
        SearchCriteria criteria = criteriaMatcher.recordedObject();
        assertNotNull(criteria);
        assertEquals(SearchOperator.MATCH_ANY_CLAUSES, criteria.getOperator());
        assertEquals(identifiers.size(), criteria.getSubCriterias().size());
        SearchSubCriteria subCriteria1 = criteria.getSubCriterias().get(0);
        assertMatchClauseForCode("S1", subCriteria1.getCriteria().getMatchClauses().get(0));
        assertEquals(SearchableEntityKind.SAMPLE, subCriteria1.getTargetEntityKind());
        SearchSubCriteria subCriteria2 = criteria.getSubCriterias().get(1);
        assertMatchClauseForCode("S2", subCriteria2.getCriteria().getMatchClauses().get(0));
        assertEquals(SearchableEntityKind.SAMPLE, subCriteria2.getTargetEntityKind());
    }

    @Test
    public final void testGetDataSetDss()
    {
        final String dataSetCode = "dataSetCode";
        final IDataSetDss dataSet = context.mock(IDataSetDss.class);
        context.checking(new Expectations()
            {
                {
                    one(dssComponent).getDataSet(dataSetCode);
                    will(returnValue(dataSet));
                }
            });

        assertEquals(dataSet, openbisFacade.getDataSetDss(dataSetCode));
    }

    @Test
    public final void testPutDataSet()
    {
        final NewDataSetDTO dataSetDTO = new NewDataSetDTO(null, null, null);
        final File file = new File("");
        final IDataSetDss dataSet = context.mock(IDataSetDss.class);
        context.checking(new Expectations()
        {
            {
                    one(dssComponent).putDataSet(dataSetDTO, file);
                    will(returnValue(dataSet));
            }
        });

        assertEquals(dataSet, openbisFacade.putDataSet(dataSetDTO, file));
    }

    @Test
    public final void testLogout()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).logout(SESSION_TOKEN);
                }
            });

        openbisFacade.logout();
    }

    @Test
    public final void testSearchForSamples()
    {
        final SearchCriteria sc = new SearchCriteria();
        final List<Sample> result = unmodifiableList();
        context.checking(new Expectations()
        {
            {
                    one(service).searchForSamples(SESSION_TOKEN, sc);
                    will(returnValue(result));
            }
        });

        assertEquals(result, openbisFacade.searchForSamples(sc));
    }

    @Test
    public final void testSearchForDataSets()
    {
        final SearchCriteria sc = new SearchCriteria();
        final List<DataSet> result = unmodifiableList();
        context.checking(new Expectations()
            {
                {
                    one(service).searchForDataSets(SESSION_TOKEN, sc);
                    will(returnValue(result));
                }
            });

        assertEquals(result, openbisFacade.searchForDataSets(sc));
    }

    @Test
    public final void testListDataSets()
    {
        final List<Sample> samples = Arrays.asList(createSample("S1", null));
        final EnumSet<Connections> connections = EnumSet.allOf(Connections.class);
        final List<DataSet> result = unmodifiableList(createDataSet("dataset", "E1", "S1"));
        context.checking(new Expectations()
            {
                {
                    one(service).listDataSets(SESSION_TOKEN, samples, connections);
                    will(returnValue(result));
                }
            });

        assertEquals(result, openbisFacade.listDataSets(samples, connections));
    }

    private String projectIdentifier(String code)
    {
        return "/DB/" + code;
    }

    private String experimentIdentifier(String code)
    {
        return "/DB/PROJECT/" + code;
    }
    
    private String sampleIdentifier(String code)
    {
        return "/DB/" + code;
    }

    private Experiment createExperiment(String code)
    {
        ExperimentInitializer init = new ExperimentInitializer();
        init.setCode(code);
        init.setExperimentTypeCode("typeCode");
        init.setId(1L);
        init.setPermId("permid");
        init.setIdentifier(experimentIdentifier(code));
        return new Experiment(init);
    }
    
    private Sample createSample(String code, String experimentIdentifierOrNull)
    {
        SampleInitializer init = new SampleInitializer();
        init.setCode(code);
        init.setIdentifier(sampleIdentifier(code));
        init.setId(1L);
        init.setPermId("permid");
        init.setSampleTypeCode("sample-type-code");
        init.setSampleTypeId(1L);
        init.setExperimentIdentifierOrNull(experimentIdentifierOrNull);
        return new Sample(init);
    }

    private DataSet createDataSet(String code, String exprimentId, String sampleIdOrNull)
    {
        DataSetInitializer init = new DataSetInitializer();
        init.setCode(code);
        init.setDataSetTypeCode("data-set-type");
        init.setRegistrationDate(new Date());
        init.setExperimentIdentifier(exprimentId);
        init.setSampleIdentifierOrNull(sampleIdOrNull);
        return new DataSet(init);
    }

    private <E> List<E> unmodifiableList(E... args)
    {
        List<E> list = Arrays.asList(args);
        return Collections.unmodifiableList(list);
    }

    private void assertMatchClauseForCode(String code, MatchClause matchClause)
    {
        assertEquals(code, matchClause.getDesiredValue());
        assertEquals(MatchClauseFieldType.ATTRIBUTE, matchClause.getFieldType());
        assertEquals(MatchClauseAttribute.CODE, ((AttributeMatchClause) matchClause).getAttribute());
    }

    private void assertMatchClauseForProject(String projectCode, MatchClause matchClause)
    {
        assertEquals(projectCode, matchClause.getDesiredValue());
        assertEquals(MatchClauseFieldType.ATTRIBUTE, matchClause.getFieldType());
        assertEquals(MatchClauseAttribute.PROJECT,
                ((AttributeMatchClause) matchClause).getAttribute());
    }

}
