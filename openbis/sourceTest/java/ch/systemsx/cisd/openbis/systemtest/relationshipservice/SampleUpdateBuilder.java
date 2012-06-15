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

import java.util.ArrayList;
import java.util.Date;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

public class SampleUpdateBuilder extends Builder<SampleUpdatesDTO>
{
    private SampleUpdatesDTO updates;

    private TechId sampleId;

    private ExperimentIdentifier experimentId;

    private Date version;

    private SampleIdentifier sampleIdentifier;

    public SampleUpdateBuilder(ICommonServerForInternalUse commonServer,
            IGenericServer genericServer, Sample sample)
    {
        super(commonServer, genericServer);
        this.sampleId = new TechId(sample.getId());
        if (sample.getExperiment() != null)
        {
            this.experimentId = new ExperimentIdentifier(sample.getExperiment());
        }
        this.version = sample.getModificationDate();
        this.sampleIdentifier = getSampleIdentifier(sample);
    }

    public SampleUpdateBuilder inExperiment(Experiment experiment)
    {
        this.experimentId = new ExperimentIdentifier(experiment);
        return this;
    }

    public SampleUpdateBuilder withoutExperiment()
    {
        this.experimentId = null;
        return this;
    }

    /*
     *     public SampleUpdatesDTO(TechId sampleId, List<IEntityProperty> properties,
            ExperimentIdentifier experimentIdentifierOrNull, Collection<NewAttachment> attachments,
            Date version, SampleIdentifier sampleIdentifier, String containerIdentifierOrNull,
            String[] modifiedParentCodesOrNull)
     */

    @Override
    public SampleUpdatesDTO create()
    {
        return new SampleUpdatesDTO(this.sampleId, new ArrayList<IEntityProperty>(),
                this.experimentId, new ArrayList<NewAttachment>(), this.version,
                this.sampleIdentifier, null, null);
    }
}