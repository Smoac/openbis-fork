/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.api.v3;

import static junit.framework.Assert.fail;
import static org.testng.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IModifierHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.ProjectCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.SpaceCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SearchResult;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.util.UpdateUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;
import ch.systemsx.cisd.openbis.systemtest.base.builder.SessionBuilder;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@TransactionConfiguration(transactionManager = "transaction-manager", defaultRollback = false)
@Test(groups = "project-samples")
public class ProjectSampleTest extends BaseTest
{
    private static final String SYSTEM_USER = "system";
    private static final SpacePermId HOME_SPACE_ID = new SpacePermId("DEFAULT");
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, ProjectSampleTest.class);
    private static final EntityTypePermId ENTITY_TYPE_UNKNOWN = new EntityTypePermId("UNKNOWN");
    
    @Autowired
    protected IApplicationServerApi v3api;
    
    private ISpaceId space1;
    private ISpaceId space2;
    private ProjectPermId project1inSpace1;
    private ProjectPermId project2inSpace1;
    private ProjectPermId project1inSpace2;
    private ProjectPermId project2inSpace2;
    private ProjectPermId project1inHomeSpace;
    private String adminSessionToken;
    private String adminUser;
    
    @BeforeClass
    public void createData()
    {
        List<SpacePermId> spaces = createSpaces(systemSessionToken, "SPACE1", "SPACE2");
        space1 = spaces.get(0);
        space2 = spaces.get(1);
        List<ProjectPermId> projects = createProjects(systemSessionToken, space1, "PROJECT1", "PROJECT2");
        project1inSpace1 = projects.get(0);
        project2inSpace1 = projects.get(1);
        project1inSpace2 = createProjects(systemSessionToken, space2, "PROJECT1").get(0);
        project2inSpace2 = createProjects(systemSessionToken, space2, "PROJECT2").get(0);
        project1inHomeSpace = createProjects(systemSessionToken, HOME_SPACE_ID, "PROJECT1").get(0);
        createSamples(systemSessionToken, null, null, null, "SHARED1", "SHARED2");
        createSamples(systemSessionToken, space1, null, null, "SAMPLE1", "SAMPLE2");
        createSamples(systemSessionToken, space1, project1inSpace1, null, "SAMPLE3", "SAMPLE4");
        createSamples(systemSessionToken, space2, project2inSpace2, null, "SAMPLE5", "SAMPLE6");
        waitAtLeastASecond(); // to allow checks on modification time stamps 
        UpdateUtils.waitUntilIndexUpdaterIsIdle(applicationContext, operationLog);
        SessionBuilder session = aSession().withInstanceRole(RoleCode.ADMIN);
        adminUser = session.getUserID();
        adminSessionToken = create(session);
        commonServer.changeUserHomeSpace(adminSessionToken, new TechId(1)); // home space = DEFAULT
    }
    
    @Override
    @AfterTransaction
    @Test(enabled = false)
    public void cleanDatabase()
    {
        // super method deletes samples, experiments and data sets from the database
    }

    private List<SpacePermId> createSpaces(String sessionToken, String...spaceCodes)
    {
        List<SpaceCreation> newSpaces = new ArrayList<SpaceCreation>();
        for (String spaceCode : spaceCodes)
        {
            SpaceCreation space = new SpaceCreation();
            space.setCode(spaceCode);
            newSpaces.add(space);
        }
        return v3api.createSpaces(sessionToken, newSpaces);
    }
    
    private List<ProjectPermId> createProjects(String sessionToken, ISpaceId spaceId, String...projectCodes)
    {
        List<ProjectCreation> newProjects = new ArrayList<ProjectCreation>();
        for (String projectCode : projectCodes)
        {
            ProjectCreation project = new ProjectCreation();
            project.setSpaceId(spaceId);
            project.setCode(projectCode);
            newProjects.add(project);
        }
        return v3api.createProjects(sessionToken, newProjects);
    }
    
    private List<SamplePermId> createSamples(String sessionToken, ISpaceId spaceOrNull, 
            IProjectId projectOrNull, IExperimentId experimentOrNull, String...codes)
    {
        List<SampleCreation> newSamples = new ArrayList<SampleCreation>();
        for (String code : codes)
        {
            SampleCreation sample = new SampleCreation();
            sample.setTypeId(ENTITY_TYPE_UNKNOWN);
            sample.setSpaceId(spaceOrNull);
            sample.setProjectId(projectOrNull);
            sample.setExperimentId(experimentOrNull);
            sample.setCode(code);
            newSamples.add(sample);
        }
        return v3api.createSamples(sessionToken, newSamples);
    }
    
    @Test
    public void testCreateSampleAndMapSamplesByPermId()
    {
        String sampleCode = createUniqueCode("S");
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode(sampleCode);
        sampleCreation.setTypeId(ENTITY_TYPE_UNKNOWN);
        sampleCreation.setSpaceId(space1);
        sampleCreation.setProjectId(project1inSpace1);
        
        List<SamplePermId> ids = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation));
        
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        Map<ISampleId, Sample> samples = v3api.mapSamples(systemSessionToken, ids, fetchOptions);
        Sample sample = samples.get(ids.get(0));
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE1/PROJECT1/" + sampleCode);
        assertEquals(sample.getProject().getIdentifier().getIdentifier(), "/SPACE1/PROJECT1");
    }
    
    @Test
    public void testCreateThreeSamplesWithSameCodeInDifferentProjectOfSameSpaceAndMapSamplesByIdentifier()
    {
        String sampleCode = createUniqueCode("S");
        SampleCreation s1 = new SampleCreation();
        s1.setCode(sampleCode);
        s1.setTypeId(ENTITY_TYPE_UNKNOWN);
        s1.setSpaceId(space1);
        s1.setProjectId(project1inSpace1);
        SampleCreation s2 = new SampleCreation();
        s2.setCode(sampleCode);
        s2.setTypeId(ENTITY_TYPE_UNKNOWN);
        s2.setSpaceId(space1);
        s2.setProjectId(project2inSpace1);
        SampleCreation s3 = new SampleCreation();
        s3.setCode(sampleCode);
        s3.setTypeId(ENTITY_TYPE_UNKNOWN);
        s3.setSpaceId(space1);
        
        v3api.createSamples(systemSessionToken, Arrays.asList(s1, s2, s3));
        
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        List<ISampleId> ids = new ArrayList<>();
        ids.add(new SampleIdentifier("/SPACE1/PROJECT1/" + sampleCode));
        ids.add(new SampleIdentifier("/SPACE1/PROJECT2/" + sampleCode));
        ids.add(new SampleIdentifier("/SPACE1/" + sampleCode));
        Map<ISampleId, Sample> samples = v3api.mapSamples(systemSessionToken, ids, fetchOptions);
        assertEquals(samples.get(ids.get(0)).getProject().getIdentifier().toString(), "/SPACE1/PROJECT1");
        assertEquals(samples.get(ids.get(1)).getProject().getIdentifier().toString(), "/SPACE1/PROJECT2");
        assertEquals(samples.get(ids.get(2)).getProject(), null);
    }
    
    @Test
    public void testCreateProjectSampleWithComponent()
    {
        String sampleCode = createUniqueCode("S");
        SampleCreation s1 = new SampleCreation();
        s1.setCode(sampleCode);
        s1.setTypeId(ENTITY_TYPE_UNKNOWN);
        s1.setSpaceId(space1);
        s1.setProjectId(project1inSpace1);
        SamplePermId s1PermId = v3api.createSamples(systemSessionToken, Arrays.asList(s1)).get(0);
        SampleCreation s2 = new SampleCreation();
        s2.setCode("A01");
        s2.setTypeId(ENTITY_TYPE_UNKNOWN);
        s2.setSpaceId(space1);
        s2.setProjectId(project1inSpace1);
        s2.setContainerId(s1PermId);
        
        v3api.createSamples(systemSessionToken, Arrays.asList(s2)).get(0);
        
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        fetchOptions.withContainer();
        List<ISampleId> ids = new ArrayList<>();
        ids.add(new SampleIdentifier("/SPACE1/PROJECT1/" + sampleCode));
        ids.add(new SampleIdentifier("/SPACE1/PROJECT1/" + sampleCode + ":A01"));
        Map<ISampleId, Sample> samples = v3api.mapSamples(systemSessionToken, ids, fetchOptions);
        assertEquals(samples.get(ids.get(0)).getProject().getIdentifier().toString(), "/SPACE1/PROJECT1");
        assertEquals(samples.get(ids.get(1)).getProject().getIdentifier().toString(), "/SPACE1/PROJECT1");
    }

    @Test
    public void testCreateProjectSampleWithComponentInHomeSpace()
    {
        String sampleCode = createUniqueCode("S");
        SampleCreation s1 = new SampleCreation();
        s1.setCode(sampleCode);
        s1.setTypeId(ENTITY_TYPE_UNKNOWN);
        s1.setSpaceId(HOME_SPACE_ID);
        s1.setProjectId(project1inHomeSpace);
        SamplePermId s1PermId = v3api.createSamples(systemSessionToken, Arrays.asList(s1)).get(0);
        SampleCreation s2 = new SampleCreation();
        s2.setCode("A01");
        s2.setTypeId(ENTITY_TYPE_UNKNOWN);
        s2.setSpaceId(HOME_SPACE_ID);
        s2.setProjectId(project1inHomeSpace);
        s2.setContainerId(s1PermId);
        
        v3api.createSamples(systemSessionToken, Arrays.asList(s2)).get(0);
        
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        fetchOptions.withContainer();
        List<ISampleId> ids = new ArrayList<>();
        ids.add(new SampleIdentifier("//PROJECT1/" + sampleCode));
        ids.add(new SampleIdentifier("//PROJECT1/" + sampleCode + ":A01"));
        Map<ISampleId, Sample> samples = v3api.mapSamples(adminSessionToken, ids, fetchOptions);
        assertEquals(samples.get(ids.get(0)).getProject().getIdentifier().toString(), "/DEFAULT/PROJECT1");
        assertEquals(samples.get(ids.get(1)).getProject().getIdentifier().toString(), "/DEFAULT/PROJECT1");
    }
    
    @Test
    public void testAssignSpaceSampleToAProject()
    {
        String sampleCode = createUniqueCode("S");
        createSamples(systemSessionToken, space1, null, null, sampleCode);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(new SampleIdentifier("/SPACE1/" + sampleCode));
        sampleUpdate.setProjectId(project1inSpace1);
        Date now = new Date();
        
        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));
        
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        SampleIdentifier newSampleIdentifier = new SampleIdentifier("/SPACE1/PROJECT1/" + sampleCode);
        Map<ISampleId, Sample> samples = v3api.mapSamples(systemSessionToken, Arrays.asList(newSampleIdentifier), fetchOptions);
        Sample sample = samples.get(newSampleIdentifier);
        assertModification(sample, sample, now, adminUser);
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE1/PROJECT1/" + sampleCode);
        Project project = sample.getProject();
        assertEquals(project.getIdentifier().getIdentifier(), "/SPACE1/PROJECT1");
        assertModification(project, project, now, adminUser);
    }
    
    @Test
    public void testAssignHomeSpaceSampleToAProjectInHomeSpace()
    {
        String sampleCode = createUniqueCode("S");
        createSamples(systemSessionToken, HOME_SPACE_ID, null, null, sampleCode);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(new SampleIdentifier("//" + sampleCode));
        sampleUpdate.setProjectId(project1inHomeSpace);
        Date now = new Date();
        
        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));
        
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        SampleIdentifier newSampleIdentifier = new SampleIdentifier("//PROJECT1/" + sampleCode);
        Map<ISampleId, Sample> samples = v3api.mapSamples(adminSessionToken, Arrays.asList(newSampleIdentifier), fetchOptions);
        Sample sample = samples.get(newSampleIdentifier);
        assertModification(sample, sample, now, adminUser);
        assertEquals(sample.getIdentifier().getIdentifier(), "/DEFAULT/PROJECT1/" + sampleCode);
        Project project = sample.getProject();
        assertEquals(project.getIdentifier().getIdentifier(), "/DEFAULT/PROJECT1");
        assertModification(project, project, now, adminUser);
    }
    
    @Test
    public void testAssignProjectSampleToADifferentProjectInTheSameSpace()
    {
        String sampleCode = createUniqueCode("S");
        SamplePermId spaceSample = createSamples(systemSessionToken, space1, project1inSpace1, null, sampleCode).get(0);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(new SampleIdentifier("/SPACE1/PROJECT1/" + sampleCode));
        sampleUpdate.setProjectId(project2inSpace1);
        Date now = new Date();
        
        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));
        
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        Map<ISampleId, Sample> samples = v3api.mapSamples(systemSessionToken, Arrays.asList(spaceSample), fetchOptions);
        Sample sample = samples.get(spaceSample);
        assertModification(sample, sample, now, adminUser);
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE1/PROJECT2/" + sampleCode);
        Project project = sample.getProject();
        assertEquals(project.getIdentifier().getIdentifier(), "/SPACE1/PROJECT2");
        assertModification(project, project, now, adminUser);
    }
    
    @Test
    public void testAssignProjectSampleWithComponentToAProjectInADifferentSpace()
    {
        String sampleCode = createUniqueCode("S");
        SampleCreation s1 = new SampleCreation();
        s1.setCode(sampleCode);
        s1.setTypeId(ENTITY_TYPE_UNKNOWN);
        s1.setSpaceId(space1);
        s1.setProjectId(project1inSpace1);
        SampleCreation s2 = new SampleCreation();
        s2.setCode("A01");
        s2.setTypeId(ENTITY_TYPE_UNKNOWN);
        s2.setSpaceId(space1);
        s2.setProjectId(project1inSpace1);
        s2.setContainerId(new SampleIdentifier("/SPACE1/PROJECT1/" + sampleCode));
        v3api.createSamples(systemSessionToken, Arrays.asList(s1, s2));
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(new SampleIdentifier("/SPACE1/PROJECT1/" + sampleCode));
        sampleUpdate.setSpaceId(space2);
        sampleUpdate.setProjectId(project2inSpace2);
        Date now = new Date();
        
        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));
        
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        SampleIdentifier containerID = new SampleIdentifier("/SPACE2/PROJECT2/" + sampleCode);
        SampleIdentifier componentID = new SampleIdentifier("/SPACE1/PROJECT1/" + sampleCode + ":A01");
        Map<ISampleId, Sample> samples = v3api.mapSamples(systemSessionToken, Arrays.asList(containerID, componentID), fetchOptions);
        Sample sample = samples.get(containerID);
        assertModification(sample, sample, now, adminUser);
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE2/PROJECT2/" + sampleCode);
        Project project = sample.getProject();
        assertEquals(project.getIdentifier().getIdentifier(), "/SPACE2/PROJECT2");
        assertModification(project, project, now, adminUser);
        Sample component = samples.get(componentID);
        assertEquals(component.getModifier().getUserId(), SYSTEM_USER);
        assertEquals(component.getIdentifier().getIdentifier(), "/SPACE1/PROJECT1/" + sampleCode + ":A01");
        Project componentProject = component.getProject();
        assertEquals(componentProject.getIdentifier().getIdentifier(), "/SPACE1/PROJECT1");
        assertModification(componentProject, componentProject, now, adminUser);
    }
    
    @Test
    public void testAssignProjectSampleToAProjectInADifferentSpace()
    {
        String sampleCode = createUniqueCode("S");
        SamplePermId spaceSample = createSamples(systemSessionToken, space1, project1inSpace1, null, sampleCode).get(0);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setSpaceId(space2);
        sampleUpdate.setProjectId(project1inSpace2);
        Date now = new Date();
        
        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));
        
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        Map<ISampleId, Sample> samples = v3api.mapSamples(systemSessionToken, Arrays.asList(spaceSample), fetchOptions);
        Sample sample = samples.get(spaceSample);
        assertModification(sample, sample, now, adminUser);
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE2/PROJECT1/" + sampleCode);
        Project project = sample.getProject();
        assertEquals(project.getIdentifier().getIdentifier(), "/SPACE2/PROJECT1");
        assertModification(project, project, now, adminUser);
    }
    
    @Test
    public void testUnassignProjectSampleFromProject()
    {
        String sampleCode = createUniqueCode("S");
        SamplePermId spaceSample = createSamples(systemSessionToken, space1, project1inSpace1, null, sampleCode).get(0);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setProjectId(null);
        Date now = new Date();
        
        v3api.updateSamples(adminSessionToken, Arrays.asList(sampleUpdate));
        
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withProject().withModifier();
        Map<ISampleId, Sample> samples = v3api.mapSamples(systemSessionToken, Arrays.asList(spaceSample), fetchOptions);
        Sample sample = samples.get(spaceSample);
        assertModification(sample, sample, now, adminUser);
        assertEquals(sample.getIdentifier().getIdentifier(), "/SPACE1/" + sampleCode);
        assertEquals(sample.getProject(), null);
        ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        projectFetchOptions.withModifier();
        Map<IProjectId, Project> projects = v3api.mapProjects(systemSessionToken, 
                Arrays.asList(project1inSpace1), projectFetchOptions);
        Project project = projects.values().iterator().next();
        assertModification(project, project, now, adminUser);
    }
    
    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void testCreateWithProjectAndSpaceInconsistent()
    {
        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_INCONSISTENT_PROJECT_AND_SPACE");
        creation.setTypeId(ENTITY_TYPE_UNKNOWN);
        creation.setSpaceId(space1);
        creation.setProjectId(project1inSpace2);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(systemSessionToken, Collections.singletonList(creation));
                }
            }, "Sample space must be the same as project space. "
                + "Sample: /SPACE1/SAMPLE_WITH_INCONSISTENT_PROJECT_AND_SPACE, "
                + "Project: /SPACE2/PROJECT1 "
                + "(Context: [verify project for sample SAMPLE_WITH_INCONSISTENT_PROJECT_AND_SPACE])");
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void testCreateWithProjectAndNoSpaceInconsistent()
    {
        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_INCONSISTENT_PROJECT_AND_NOSPACE");
        creation.setTypeId(ENTITY_TYPE_UNKNOWN);
        creation.setProjectId(project1inSpace1);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(systemSessionToken, Collections.singletonList(creation));
                }
            }, "Shared samples cannot be attached to projects. "
                + "Sample: /SAMPLE_WITH_INCONSISTENT_PROJECT_AND_NOSPACE, "
                + "Project: /SPACE1/PROJECT1 "
                + "(Context: [verify project for sample SAMPLE_WITH_INCONSISTENT_PROJECT_AND_NOSPACE])");
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void testCreateWithProjectAndExperimentInconsistent()
    {
        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_INCONSISTENT_PROJECT_AND_EXPERIMENT");
        creation.setTypeId(ENTITY_TYPE_UNKNOWN);
        creation.setSpaceId(space1);
        creation.setProjectId(project2inSpace1);
        String expCode = createUniqueCode("E");
        creation.setExperimentId(createExperimentInProject1OfSpace1(expCode));
        
        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.createSamples(systemSessionToken, Collections.singletonList(creation));
            }
        }, "Sample project must be the same as experiment project. "
                + "Sample: /SPACE1/SAMPLE_WITH_INCONSISTENT_PROJECT_AND_EXPERIMENT, "
                + "Project: /SPACE1/PROJECT2, "
                + "Experiment: /SPACE1/PROJECT1/" + expCode + " "
                + "(Context: [verify experiment for sample SAMPLE_WITH_INCONSISTENT_PROJECT_AND_EXPERIMENT])");
    }
    
    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void testAssignSpaceSampleToProjectInDifferentSpace()
    {
        String code = createUniqueCode("S");
        ISampleId sampleId = createSamples(systemSessionToken, space1, null, null, code).get(0);
        final SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleId);
        sampleUpdate.setProjectId(project2inSpace2);

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(systemSessionToken, Collections.singletonList(sampleUpdate));
            }
        }, "Sample space must be the same as project space. "
                + "Sample: /SPACE1/" + code + ", "
                + "Project: /SPACE2/PROJECT2 "
                + "(Context: [verify project for sample " + code + "])");
    }
    
    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void testAssignSampleOfAnExperimentToProjectDifferentToTheExperimentProject()
    {
        String sampleCode = createUniqueCode("S");
        String expCode = createUniqueCode("E");
        IExperimentId experiment = createExperimentInProject1OfSpace1(expCode);
        SamplePermId sample = createSamples(systemSessionToken, space1, null, experiment, sampleCode).get(0);
        final SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample);
        sampleUpdate.setProjectId(project2inSpace1);
        
        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(systemSessionToken, Collections.singletonList(sampleUpdate));
            }
        }, "Sample project must be the same as experiment project. "
                + "Sample: /SPACE1/" + sampleCode + ", "
                + "Project: /SPACE1/PROJECT2, "
                + "Experiment: /SPACE1/PROJECT1/" + expCode + " "
                + "(Context: [verify experiment for sample " + sampleCode + "])");
        
    }
    
    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void testAssignSharedSampleToProject()
    {
        String code = createUniqueCode("S");
        ISampleId sharedSample = createSamples(systemSessionToken, null, null, null, code).get(0);
        final SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sharedSample);
        sampleUpdate.setProjectId(project1inSpace1);
        
        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(systemSessionToken, Collections.singletonList(sampleUpdate));
            }
        }, "Shared samples cannot be attached to projects. "
                + "Sample: /" + code + ", "
                + "Project: /SPACE1/PROJECT1 "
                + "(Context: [verify project for sample " + code + "])");
    }
    
    @Test(priority = -1)
    public void testSearchForSamplesWithProject() throws InterruptedException
    {
        
        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withProject();
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        
        SearchResult<Sample> result = v3api.searchSamples(systemSessionToken, searchCriteria, fetchOptions);
        
        assertEquals(result.getObjects().get(0).getIdentifier().getIdentifier(), "/SPACE1/PROJECT1/SAMPLE3");
        assertEquals(result.getObjects().get(0).getProject().getIdentifier().getIdentifier(), "/SPACE1/PROJECT1");
        assertEquals(result.getObjects().get(1).getIdentifier().getIdentifier(), "/SPACE1/PROJECT1/SAMPLE4");
        assertEquals(result.getObjects().get(1).getProject().getIdentifier().getIdentifier(), "/SPACE1/PROJECT1");
        assertEquals(result.getObjects().get(2).getIdentifier().getIdentifier(), "/SPACE2/PROJECT2/SAMPLE5");
        assertEquals(result.getObjects().get(2).getProject().getIdentifier().getIdentifier(), "/SPACE2/PROJECT2");
        assertEquals(result.getObjects().get(3).getIdentifier().getIdentifier(), "/SPACE2/PROJECT2/SAMPLE6");
        assertEquals(result.getObjects().get(3).getProject().getIdentifier().getIdentifier(), "/SPACE2/PROJECT2");
        assertEquals(result.getTotalCount(), 4);
    }
    
    @Test(priority = -1)
    public void testSearchForSamplesWithCodeAndWithProject()
    {
        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withCode().thatEndsWith("3");
        searchCriteria.withProject();
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        
        SearchResult<Sample> result = v3api.searchSamples(systemSessionToken, searchCriteria, fetchOptions);
        
        assertEquals(result.getObjects().get(0).getIdentifier().getIdentifier(), "/SPACE1/PROJECT1/SAMPLE3");
        assertEquals(result.getObjects().get(0).getProject().getIdentifier().getIdentifier(), "/SPACE1/PROJECT1");
        assertEquals(result.getTotalCount(), 1);
    }
    
    @Test(priority = -1)
    public void testSearchForSamplesWithProjectWithSpaceWithCode()
    {
        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withProject().withSpace().withCode().thatEquals("SPACE1");
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        
        SearchResult<Sample> result = v3api.searchSamples(systemSessionToken, searchCriteria, fetchOptions);
        
        assertEquals(result.getObjects().get(0).getIdentifier().getIdentifier(), "/SPACE1/PROJECT1/SAMPLE3");
        assertEquals(result.getObjects().get(0).getProject().getIdentifier().getIdentifier(), "/SPACE1/PROJECT1");
        assertEquals(result.getObjects().get(1).getIdentifier().getIdentifier(), "/SPACE1/PROJECT1/SAMPLE4");
        assertEquals(result.getObjects().get(1).getProject().getIdentifier().getIdentifier(), "/SPACE1/PROJECT1");
        assertEquals(result.getTotalCount(), 2);
    }
    
    @Test(priority = -1)
    public void testSearchForSamplesWithProjectWithPermId()
    {
        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withProject().withPermId().thatEquals(project2inSpace2.getPermId());
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        
        SearchResult<Sample> result = v3api.searchSamples(systemSessionToken, searchCriteria, fetchOptions);
        
        assertEquals(result.getObjects().get(0).getIdentifier().getIdentifier(), "/SPACE2/PROJECT2/SAMPLE5");
        assertEquals(result.getObjects().get(0).getProject().getIdentifier().getIdentifier(), "/SPACE2/PROJECT2");
        assertEquals(result.getObjects().get(1).getIdentifier().getIdentifier(), "/SPACE2/PROJECT2/SAMPLE6");
        assertEquals(result.getObjects().get(1).getProject().getIdentifier().getIdentifier(), "/SPACE2/PROJECT2");
        assertEquals(result.getTotalCount(), 2);
    }
    
    @Test(priority = -1)
    public void testSearchForSamplesWithoutProjects()
    {
        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withCode().thatStartsWith("SAMP");
        searchCriteria.withoutProject();
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        
        SearchResult<Sample> result = v3api.searchSamples(systemSessionToken, searchCriteria, fetchOptions);
        
        assertEquals(result.getObjects().get(0).getIdentifier().getIdentifier(), "/SPACE1/SAMPLE1");
        assertEquals(result.getObjects().get(0).getProject(), null);
        assertEquals(result.getObjects().get(1).getIdentifier().getIdentifier(), "/SPACE1/SAMPLE2");
        assertEquals(result.getObjects().get(1).getProject(), null);
        assertEquals(result.getTotalCount(), 2);
    }

    private void assertUserFailureException(IDelegatedAction action, String expectedExceptionMessage)
    {
        try
        {
            action.execute();
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals(ex.getMessage(), expectedExceptionMessage);
        }
    }

    private void assertModification(IModificationDateHolder modificationDateHolder, IModifierHolder modifierHolder, 
            Date date, String modifier)
    {
        assertNotOlder(modificationDateHolder.getModificationDate(), date);
        assertEquals(modifierHolder.getModifier().getUserId(), modifier);
    }
    
    private void assertNotOlder(Date actualDate, Date referenceDate)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String renderedReferenceDate = format.format(referenceDate.getTime());
        String renderedActualDate = format.format(actualDate);
        assertEquals(renderedReferenceDate.compareTo(renderedActualDate) <= 0, true,
                renderedActualDate + " > " + renderedReferenceDate);
    }
    
    private IExperimentId createExperimentInProject1OfSpace1(String code)
    {
        ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode(code);
        experiment.setTypeId(ENTITY_TYPE_UNKNOWN);
        experiment.setProjectId(project1inSpace1);
        return v3api.createExperiments(systemSessionToken, Arrays.asList(experiment)).get(0);
    }

    private String createUniqueCode(String prefix)
    {
        return prefix + "-" + System.currentTimeMillis();
    }
    
    private void waitAtLeastASecond()
    {
        try
        {
            Thread.sleep(1100);
        } catch (InterruptedException ex)
        {
            // silently ignored
        }
    }
}
