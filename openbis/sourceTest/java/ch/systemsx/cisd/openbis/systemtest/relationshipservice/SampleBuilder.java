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
import java.util.List;
import java.util.UUID;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

public class SampleBuilder extends Builder<Sample>
{

    public SampleBuilder(ICommonServerForInternalUse commonServer, IGenericServer genericServer)
    {
        super(commonServer, genericServer);
    }

    private Experiment experiment;

    private Space space;

    @SuppressWarnings("hiding")
    public SampleBuilder inExperiment(Experiment experiment)
    {
        this.experiment = experiment;
        return this;
    }

    @SuppressWarnings("hiding")
    public SampleBuilder inSpace(Space space)
    {
        this.space = space;
        return this;
    }

    @Override
    public Sample create()
    {

        SampleType sampleType = new SampleType();
        sampleType.setCode(UUID.randomUUID().toString());
        sampleType.setContainerHierarchyDepth(0);
        sampleType.setDatabaseInstance(this.experiment.getProject().getSpace().getInstance());
        sampleType.setDescription("description");
        sampleType.setGeneratedCodePrefix("prefix");
        sampleType.setListable(true);
        sampleType.setGeneratedFromHierarchyDepth(0);
        sampleType.setSampleTypePropertyTypes(new ArrayList<SampleTypePropertyType>());
        commonServer.registerSampleType(systemSession, sampleType);

        String identifier;
        if (this.experiment != null)
        {
            identifier = "/" + this.experiment.getProject().getSpace().getCode() + "/"
                    + UUID.randomUUID().toString().toUpperCase();
        } else
        {
            identifier = "/" + this.space.getCode() + "/"
                    + UUID.randomUUID().toString().toUpperCase();
        }

        NewSample data = new NewSample();
        data.setIdentifier(identifier);
        data.setAttachments(new ArrayList<NewAttachment>());
        data.setContainerIdentifier(null);
        data.setCurrentContainerIdentifier(null);
        if (this.experiment != null)
        {
            data.setExperimentIdentifier(this.experiment.getIdentifier());
        }

        data.setParentsOrNull(null);
        data.setProperties(new IEntityProperty[0]);
        data.setSampleType(sampleType);

        genericServer.registerSample(systemSession, data, new ArrayList<NewAttachment>());

        ListSampleCriteria criteria =
                ListSampleCriteria.createForExperiment(new TechId(experiment.getId()));
        List<Sample> samples = commonServer.listSamples(systemSession, criteria);

        for (Sample s : samples)
        {
            if (s.getIdentifier().equals(identifier))
            {
                return s;
            }
        }
        throw new IllegalStateException("Sample registration failed");
    }

}