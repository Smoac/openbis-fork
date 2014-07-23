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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IVerifyEntityPropertyExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class VerifySampleExecutor implements IVerifySampleExecutor
{

    @Autowired
    private IVerifyEntityPropertyExecutor verifyEntityPropertyExecutor;

    @Autowired
    private IVerifySampleExperimentExecutor verifySampleExperimentExecutor;

    @Autowired
    private IVerifySampleContainerExecutor verifySampleContainerExecutor;

    @Autowired
    private IVerifySampleParentsExecutor verifySampleParentsExecutor;

    @SuppressWarnings("unused")
    private VerifySampleExecutor()
    {
    }

    public VerifySampleExecutor(IVerifyEntityPropertyExecutor verifyEntityPropertyExecutor,
            IVerifySampleExperimentExecutor verifySampleExperimentExecutor, IVerifySampleContainerExecutor verifySampleContainerExecutor,
            IVerifySampleParentsExecutor verifySampleParentsExecutor)
    {
        this.verifyEntityPropertyExecutor = verifyEntityPropertyExecutor;
        this.verifySampleExperimentExecutor = verifySampleExperimentExecutor;
        this.verifySampleContainerExecutor = verifySampleContainerExecutor;
        this.verifySampleParentsExecutor = verifySampleParentsExecutor;
    }

    @Override
    public void verify(IOperationContext context, Collection<SamplePE> samples)
    {
        verifyEntityPropertyExecutor.verify(context, samples);
        verifySampleExperimentExecutor.verify(context, samples);
        verifySampleContainerExecutor.verify(context, samples);
        verifySampleParentsExecutor.verify(context, samples);
    }

}
