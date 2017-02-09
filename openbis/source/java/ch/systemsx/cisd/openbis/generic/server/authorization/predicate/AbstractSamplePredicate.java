/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;

/**
 * An abstract <code>IPredicate</code> for samples.
 * 
 * @author Bernd Rinn
 */
abstract class AbstractSamplePredicate<T> extends AbstractPredicate<T>
{
    protected final SampleTechIdPredicate sampleTechIdPredicate;

    protected final SpaceIdentifierPredicate spacePredicate;

    protected final SamplePermIdPredicate samplePermIdPredicate;

    protected final SampleOwnerIdentifierPredicate sampleOwnerPredicate;

    protected final SampleAugmentedCodePredicate sampleAugmentedCodePredicate;

    public AbstractSamplePredicate()
    {
        this.sampleTechIdPredicate = new SampleTechIdPredicate();
        this.spacePredicate = new SpaceIdentifierPredicate();
        this.samplePermIdPredicate = new SamplePermIdPredicate();
        this.sampleOwnerPredicate = new SampleOwnerIdentifierPredicate();
        this.sampleAugmentedCodePredicate = new SampleAugmentedCodePredicate(sampleOwnerPredicate);
    }

    boolean initialized;
    
    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        sampleTechIdPredicate.init(provider);
        spacePredicate.init(provider);
        samplePermIdPredicate.init(provider);
        sampleAugmentedCodePredicate.init(provider);
        initialized = true;
    }
}
