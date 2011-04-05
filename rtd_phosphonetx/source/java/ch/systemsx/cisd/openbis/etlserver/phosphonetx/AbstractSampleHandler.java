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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Experiment;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.CommonConstants;

/**
 * Abstract super class of classes getting or creating {@link Sample} instances.
 *
 * @author Franz-Josef Elmer
 */
abstract class AbstractSampleHandler extends AbstractHandler
{
    @Private
    static final String MZXML_FILENAME = "MZXML_FILENAME";

    protected static final class SampleOrError
    {
        Sample sample;

        String error;
    }

    protected final IEncapsulatedOpenBISService openbisService;

    protected final ExperimentIdentifier experimentIdentifier;

    private final SpaceIdentifier space;

    private final Experiment experiment;

    private final Map<String, SampleOrError> samplesOrErrors = new HashMap<String, SampleOrError>();

    AbstractSampleHandler(IEncapsulatedOpenBISService openbisService, IProtDAO dao,
            ExperimentIdentifier experimentIdentifier, Experiment experiment)
    {
        super(dao);
        this.openbisService = openbisService;
        this.experimentIdentifier = experimentIdentifier;
        this.experiment = experiment;
        String databaseInstanceCode = experimentIdentifier.getDatabaseInstanceCode();
        space = new SpaceIdentifier(databaseInstanceCode, CommonConstants.MS_DATA_SPACE);
    }
    
    protected SampleOrError getOrCreateSampleOrError(String sampleName)
    {
        SampleOrError sampleOrError = samplesOrErrors.get(sampleName);
        if (sampleOrError == null)
        {
            // first we look for a sample in space MS_DATA
            SampleIdentifier sampleIdentifier = new SampleIdentifier(space, sampleName);
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample =
                    openbisService.tryGetSampleWithExperiment(sampleIdentifier);
            sampleOrError = new SampleOrError();
            if (sample != null)
            {
                sampleOrError.sample = getOrCreateSample(sample.getPermId());
            } else
            {
                // second we look for a sample in same space as search experiment with
                // a property specified by 'sampleName'
                String spaceCode = experimentIdentifier.getSpaceCode();
                List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample> list =
                        openbisService.listSamplesByCriteria(new ListSamplesByPropertyCriteria(
                                MZXML_FILENAME, sampleName, spaceCode, null));
                if (list == null || list.size() == 0)
                {
                    sampleOrError.error = "an unidentified sample: " + sampleName;
                } else if (list.size() > 1)
                {
                    sampleOrError.error =
                            "a not uniquely specified sample (" + list.size()
                                    + " samples are found): " + sampleName;
                } else
                {
                    sample = list.get(0);
                    sampleOrError.sample = getOrCreateSample(sample.getPermId());
                }
            }
            if (sample != null)
            {
                handleSample(sampleName, sample);
            }
            samplesOrErrors.put(sampleName, sampleOrError);
        }
        return sampleOrError;
    }

    private Sample getOrCreateSample(String samplePermID)
    {
        Sample sample = dao.tryToGetSampleByPermID(samplePermID);
        if (sample == null)
        {
            sample = new Sample();
            sample.setPermID(samplePermID);
            sample.setId(dao.createSample(experiment.getId(), samplePermID));
        }
        return sample;
    }
    
    protected abstract void handleSample(String parameterName,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample);
}
