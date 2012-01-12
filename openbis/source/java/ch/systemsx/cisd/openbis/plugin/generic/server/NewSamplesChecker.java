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

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Helper class to check sample before actual registration/updating.
 * 
 * @author Franz-Josef Elmer
 */
class NewSamplesChecker
{
    static void check(EntityExistenceChecker entityExistenceChecker,
            List<NewSamplesWithTypes> newSamplesWithType)
    {
        for (NewSamplesWithTypes newSamplesWithTypes : newSamplesWithType)
        {
            SampleType sampleType = newSamplesWithTypes.getEntityType();
            entityExistenceChecker.assertSampleTypeExists(sampleType);
            List<NewSample> newSamples = newSamplesWithTypes.getNewEntities();
            for (NewSample newSample : newSamples)
            {
                IdentifersExtractor extractor = new IdentifersExtractor(newSample);
                ExperimentIdentifier experimentIdentifier =
                        extractor.getExperimentIdentifierOrNull();
                if (experimentIdentifier != null)
                {
                    entityExistenceChecker.assertExperimentExists(experimentIdentifier);
                }
                String containerIdentifier = newSample.getContainerIdentifierForNewSample();
                if (containerIdentifier != null)
                {
                    String defaultSpaceIdentifier = newSample.getDefaultSpaceIdentifier();
                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(containerIdentifier,
                                    defaultSpaceIdentifier);
                    entityExistenceChecker.assertSampleExists(sampleIdentifier);
                }
                entityExistenceChecker.addSample(extractor.getNewSampleIdentifier());
            }
        }
    }
}
