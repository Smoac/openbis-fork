/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.systemtest.authorization;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;

/**
 * @author Franz-Josef Elmer
 */
public class CommonServerAuthorizationTest extends BaseTest
{
    @Test
    public void testGetEntityInformationHolderForExperiment()
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        String sessionToken = create(aSession().withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, space));
        BasicEntityDescription entityDescription = new BasicEntityDescription(EntityKind.EXPERIMENT, experiment.getIdentifier());

        IEntityInformationHolderWithPermId informationHolder = commonServer.getEntityInformationHolder(sessionToken, entityDescription);

        assertEntity(experiment, informationHolder);
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testGetEntityInformationHolderForExperimentWithNoAccessRight()
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Space anotherSpace = create(aSpace());
        String sessionToken = create(aSession().withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, anotherSpace));
        BasicEntityDescription entityDescription = new BasicEntityDescription(EntityKind.EXPERIMENT, experiment.getIdentifier());

        commonServer.getEntityInformationHolder(sessionToken, entityDescription);
    }

    @Test
    public void testGetEntityInformationHolderForSample()
    {
        Space space = create(aSpace());
        Sample sample = create(aSample().inSpace(space));
        String sessionToken = create(aSession().withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, space));
        BasicEntityDescription entityDescription = new BasicEntityDescription(EntityKind.SAMPLE, sample.getIdentifier());

        IEntityInformationHolderWithPermId informationHolder = commonServer.getEntityInformationHolder(sessionToken, entityDescription);

        assertEntity(sample, informationHolder);
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testGetEntityInformationHolderForSampleWithNoAccessRight()
    {
        Space space = create(aSpace());
        Sample sample = create(aSample().inSpace(space));
        Space anotherSpace = create(aSpace());
        String sessionToken = create(aSession().withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, anotherSpace));
        BasicEntityDescription entityDescription = new BasicEntityDescription(EntityKind.SAMPLE, sample.getIdentifier());

        commonServer.getEntityInformationHolder(sessionToken, entityDescription);
    }

    @Test
    public void testGetEntityInformationHolderForDataSet()
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        AbstractExternalData dataSet = create(aDataSet().inExperiment(experiment));
        String sessionToken = create(aSession().withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, space));
        BasicEntityDescription entityDescription = new BasicEntityDescription(EntityKind.DATA_SET, dataSet.getIdentifier());

        IEntityInformationHolderWithPermId informationHolder = commonServer.getEntityInformationHolder(sessionToken, entityDescription);

        assertEntity(dataSet, informationHolder);
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testGetEntityInformationHolderForDataSetWithNoAccessRight()
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        AbstractExternalData dataSet = create(aDataSet().inExperiment(experiment));
        Space anotherSpace = create(aSpace());
        String sessionToken = create(aSession().withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, anotherSpace));
        BasicEntityDescription entityDescription = new BasicEntityDescription(EntityKind.DATA_SET, dataSet.getIdentifier());

        commonServer.getEntityInformationHolder(sessionToken, entityDescription);
    }

    private void assertEntity(IEntityInformationHolderWithPermId expectedEntity, IEntityInformationHolderWithPermId actualEntity)
    {
        assertEquals(expectedEntity.getCode(), actualEntity.getCode());
        assertEquals(expectedEntity.getEntityKind(), actualEntity.getEntityKind());
        assertEquals(expectedEntity.getEntityType().toString(), actualEntity.getEntityType().toString());
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getPermId(), actualEntity.getPermId());
    }
}
