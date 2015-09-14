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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Helper class which extract identifiers from a {@link NewSample} instance.
 * 
 * @author Franz-Josef Elmer
 */
class IdentifersExtractor
{
    private final SampleIdentifier oldSampleIdentifier;

    private final SampleIdentifier newSampleIdentifier;

    private final ExperimentIdentifier experimentIdentifierOrNull;

    private final String containerIdentifierOrNull;

    IdentifersExtractor(NewSample updatedSample)
    {
        oldSampleIdentifier = SampleIdentifierFactory.parse(updatedSample);
        if (updatedSample.getExperimentIdentifier() != null)
        {
            // experiment is provided - new sample identifier takes experiment space
            experimentIdentifierOrNull =
                    new ExperimentIdentifierFactory(updatedSample.getExperimentIdentifier())
                            .createIdentifier(updatedSample.getDefaultSpaceIdentifier());
            // TODO 2011-08-31, Tomasz Pylak: container is ignored, what does it break?
            newSampleIdentifier =
                    new SampleIdentifier(new SpaceIdentifier(
                            experimentIdentifierOrNull.getSpaceCode()),
                            oldSampleIdentifier.getSampleCode());
        } else
        {
            // no experiment - leave sample identifier unchanged
            experimentIdentifierOrNull = null;
            newSampleIdentifier = oldSampleIdentifier;
        }
        containerIdentifierOrNull = updatedSample.getContainerIdentifier();
    }

    public SampleIdentifier getOldSampleIdentifier()
    {
        return oldSampleIdentifier;
    }

    public SampleIdentifier getNewSampleIdentifier()
    {
        return newSampleIdentifier;
    }

    public ExperimentIdentifier getExperimentIdentifierOrNull()
    {
        return experimentIdentifierOrNull;
    }

    public String getContainerIdentifierOrNull()
    {
        return containerIdentifierOrNull;
    }
}