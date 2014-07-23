/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.AbstractExecutorTest;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment.ListExperimentByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * @author pkupczyk
 */
public class ListExperimentByIdExecutorTest extends AbstractExecutorTest
{

    private IExperimentDAO experimentDao;

    @Override
    protected void init()
    {
        experimentDao = context.mock(IExperimentDAO.class);
    }

    @Test
    public void test()
    {

        final ExperimentPermId permId1 = new ExperimentPermId("PERM_1");
        final ExperimentPermId permId2 = new ExperimentPermId("PERM_2");
        final ExperimentPermId permId3 = new ExperimentPermId("PERM_3");

        final ExperimentPE experiment1 = new ExperimentPE();
        experiment1.setCode("EXP_1");
        experiment1.setPermId(permId1.getPermId());
        final ExperimentPE experiment2 = new ExperimentPE();
        experiment2.setCode("EXP_2");
        experiment2.setPermId(permId2.getPermId());
        final ExperimentPE experiment3 = new ExperimentPE();
        experiment3.setCode("EXP_3");
        experiment3.setPermId(permId3.getPermId());

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getExperimentDAO();
                    will(returnValue(experimentDao));

                    one(experimentDao).listByPermID(Arrays.asList(permId1.getPermId(), permId2.getPermId(), permId3.getPermId()));
                    will(returnValue(Arrays.asList(experiment2, experiment1, experiment3)));
                }
            });

        List<ExperimentPE> experiments = execute(permId1, permId2, permId3);

        Assert.assertEquals(experiment1.getPermId(), experiments.get(0).getPermId());
        Assert.assertEquals(experiment2.getPermId(), experiments.get(1).getPermId());
        Assert.assertEquals(experiment3.getPermId(), experiments.get(2).getPermId());
    }

    private List<ExperimentPE> execute(IExperimentId... experimentIds)
    {
        ListExperimentByIdExecutor executor = new ListExperimentByIdExecutor(daoFactory);
        return executor.list(operationContext, Arrays.asList(experimentIds));
    }
}
