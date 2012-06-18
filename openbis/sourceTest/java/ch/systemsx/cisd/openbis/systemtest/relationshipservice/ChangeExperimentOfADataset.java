/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.relationshipservice;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;

/**
 * @author anttil
 */
public class ChangeExperimentOfADataset extends RelationshipServiceTest
{
    @Test
    public void changeExperimentOfADataset()
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment sourceExperiment = create(anExperiment().inProject(project));
        Experiment destinationExperiment = create(anExperiment().inProject(project));
        DataSet dataset = create(aDataSet().inExperiment(sourceExperiment));
        String session = create(aSession().withInstanceRole(RoleCode.ADMIN));

        DataSetUpdatesDTO updates =
                create(anUpdateOf(dataset).withExperiment(destinationExperiment));

        commonServer.updateDataSet(session, updates);

        assertThat(serverSays(dataset), is(inExperiment(destinationExperiment)));

    }
}
