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

package ch.systemsx.cisd.openbis.generic.shared.authorization.predicate;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Predicate based on {@link Experiment}.
 * 
 * @author Franz-Josef Elmer
 */
@ShouldFlattenCollections(true)
public class ExperimentPredicate extends DelegatedPredicate<SpaceIdentifier, IIdentifierHolder>
{
    public ExperimentPredicate()
    {
        super(new SpaceIdentifierPredicate(false));
    }

    @Override
    public SpaceIdentifier tryConvert(IIdentifierHolder value)
    {
        return new ExperimentIdentifierFactory(value.getIdentifier()).createIdentifier();
    }

    @Override
    public String getCandidateDescription()
    {
        return "experiment";
    }

}
