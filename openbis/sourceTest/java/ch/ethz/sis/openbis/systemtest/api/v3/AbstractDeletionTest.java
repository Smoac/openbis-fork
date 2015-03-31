/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.ProjectCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.deletion.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.IDeletionId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.IMaterialId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;

/**
 * @author pkupczyk
 */
public class AbstractDeletionTest extends AbstractTest
{

    protected ProjectPermId createCisdProject()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectCreation creation = new ProjectCreation();
        creation.setCode("PROJECT_TO_DELETE");
        creation.setSpaceId(new SpacePermId("CISD"));

        List<ProjectPermId> permIds = v3api.createProjects(sessionToken, Collections.singletonList(creation));

        return permIds.get(0);
    }

    protected ExperimentPermId createCisdExperiment()
    {
        return createCisdExperiment(new ProjectIdentifier("/CISD/DEFAULT"));
    }

    protected ExperimentPermId createCisdExperiment(IProjectId projectId)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_TO_DELETE");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(projectId);
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> permIds = v3api.createExperiments(sessionToken, Collections.singletonList(creation));
        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, permIds, new ExperimentFetchOptions());
        List<Experiment> experiments = new ArrayList<Experiment>(map.values());

        Assert.assertEquals(1, experiments.size());
        Assert.assertEquals("EXPERIMENT_TO_DELETE", experiments.get(0).getCode());

        return permIds.get(0);
    }

    protected SamplePermId createCisdSample(IExperimentId experimentId)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_TO_DELETE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setExperimentId(experimentId);

        List<SamplePermId> permIds = v3api.createSamples(sessionToken, Collections.singletonList(creation));
        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, permIds, new SampleFetchOptions());
        List<Sample> samples = new ArrayList<Sample>(map.values());

        Assert.assertEquals(1, samples.size());
        Assert.assertEquals("SAMPLE_TO_DELETE", samples.get(0).getCode());

        return permIds.get(0);
    }

    protected void assertProjectExists(IProjectId projectId)
    {
        assertProjectExists(projectId, true);
    }

    protected void assertProjectDoesNotExist(IProjectId projectId)
    {
        assertProjectExists(projectId, false);
    }

    private void assertProjectExists(IProjectId projectId, boolean exists)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IProjectId, Project> map =
                v3api.mapProjects(sessionToken, Collections.singletonList(projectId), new ProjectFetchOptions());
        Assert.assertEquals(exists ? 1 : 0, map.size());
    }

    protected void assertExperimentExists(IExperimentId experimentId)
    {
        assertExperimentExists(experimentId, true);
    }

    protected void assertExperimentDoesNotExist(IExperimentId experimentId)
    {
        assertExperimentExists(experimentId, false);
    }

    private void assertExperimentExists(IExperimentId experimentId, boolean exists)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IExperimentId, Experiment> map =
                v3api.mapExperiments(sessionToken, Collections.singletonList(experimentId), new ExperimentFetchOptions());
        Assert.assertEquals(exists ? 1 : 0, map.size());
    }

    protected void assertSampleExists(ISampleId sampleId)
    {
        assertSampleExists(sampleId, true);
    }

    protected void assertSampleDoesNotExist(ISampleId sampleId)
    {
        assertSampleExists(sampleId, false);
    }

    private void assertSampleExists(ISampleId sampleId, boolean exists)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ISampleId, Sample> map = v3api.mapSamples(sessionToken, Collections.singletonList(sampleId), new SampleFetchOptions());
        Assert.assertEquals(exists ? 1 : 0, map.size());
    }

    protected void assertMaterialExists(IMaterialId materialId)
    {
        assertMaterialExists(materialId, true);
    }

    protected void assertMaterialDoesNotExist(IMaterialId materialId)
    {
        assertMaterialExists(materialId, false);
    }

    private void assertMaterialExists(IMaterialId materialId, boolean exists)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IMaterialId, Material> map = v3api.mapMaterials(sessionToken, Collections.singletonList(materialId), new MaterialFetchOptions());
        Assert.assertEquals(exists ? 1 : 0, map.size());
    }

    protected void assertDataSetExists(DataSetPermId dataSetId)
    {
        assertDataSetExists(dataSetId, true);
    }

    protected void assertDataSetDoesNotExist(DataSetPermId dataSetId)
    {
        assertDataSetExists(dataSetId, false);
    }

    private void assertDataSetExists(DataSetPermId dataSetId, boolean exists)
    {
        String sessionToken = generalInformationService.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);

        ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria criteria =
                new ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria();
        criteria.addMatchClause(ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause.createAttributeMatch(
                ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute.CODE, dataSetId.getPermId()));

        List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> result = generalInformationService.searchForDataSets(sessionToken, criteria);
        Assert.assertEquals(exists ? 1 : 0, result.size());
    }

    protected void assertDeletionExists(IDeletionId deletionId)
    {
        assertDeletionExists(deletionId, true);
    }

    protected void assertDeletionDoesNotExist(IDeletionId deletionId)
    {
        assertDeletionExists(deletionId, false);
    }

    private void assertDeletionExists(IDeletionId deletionId, boolean exists)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<Deletion> result = v3api.listDeletions(sessionToken, new DeletionFetchOptions());
        Deletion found = null;

        for (Deletion item : result)
        {
            if (item.getId().equals(deletionId))
            {
                found = item;
                break;
            }
        }

        if (exists)
        {
            Assert.assertNotNull(found);
        } else
        {
            Assert.assertNull(found);
        }
    }

}
