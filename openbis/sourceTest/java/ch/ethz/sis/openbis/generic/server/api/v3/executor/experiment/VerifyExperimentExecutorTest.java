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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.AbstractExecutorTest;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment.VerifyExperimentExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IVerifyEntityPropertyExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * @author pkupczyk
 */
public class VerifyExperimentExecutorTest extends AbstractExecutorTest
{

    private IVerifyEntityPropertyExecutor verifyEntityPropertyExecutor;

    @Override
    protected void init()
    {
        verifyEntityPropertyExecutor = context.mock(IVerifyEntityPropertyExecutor.class);
    }

    @Test
    public void testVerifyNonEmptyList()
    {
        final List<ExperimentPE> experiments = Arrays.asList(new ExperimentPE(), new ExperimentPE());

        context.checking(new Expectations()
            {
                {
                    one(verifyEntityPropertyExecutor).verify(operationContext, experiments);
                }
            });

        execute(experiments);
    }

    @Test
    public void testVerifyEmptyList()
    {
        final List<ExperimentPE> experiments = Collections.emptyList();
        execute(experiments);
    }

    @Test
    public void testVerifyNull()
    {
        final List<ExperimentPE> experiments = null;
        execute(experiments);
    }

    private void execute(Collection<ExperimentPE> experiments)
    {
        VerifyExperimentExecutor executor = new VerifyExperimentExecutor(verifyEntityPropertyExecutor);
        executor.verify(operationContext, experiments);
    }

}
