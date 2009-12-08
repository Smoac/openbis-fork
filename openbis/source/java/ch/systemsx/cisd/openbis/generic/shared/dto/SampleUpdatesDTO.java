/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicSampleUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author Izabela Adamczyk
 */
public class SampleUpdatesDTO extends BasicSampleUpdates
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private ExperimentIdentifier experimentIdentifierOrNull;

    private SampleIdentifier sampleIdentifier;

    private Collection<NewAttachment> attachments;

    public SampleUpdatesDTO(TechId sampleId, List<IEntityProperty> properties,
            ExperimentIdentifier experimentIdentifierOrNull, Collection<NewAttachment> attachments,
            Date version, SampleIdentifier sampleIdentifier, String parentIdentifierOrNull,
            String containerIdentifierOrNull)
    {
        super(sampleId, properties, version, parentIdentifierOrNull, containerIdentifierOrNull);
        this.experimentIdentifierOrNull = experimentIdentifierOrNull;
        this.attachments = attachments;
        this.sampleIdentifier = sampleIdentifier;
    }

    public SampleIdentifier getSampleIdentifier()
    {
        return sampleIdentifier;
    }

    public void setSampleIdentifier(SampleIdentifier sampleIdentifier)
    {
        this.sampleIdentifier = sampleIdentifier;
    }

    public ExperimentIdentifier getExperimentIdentifierOrNull()
    {
        return experimentIdentifierOrNull;
    }

    public void setExperimentIdentifierOrNull(ExperimentIdentifier experimentIdentifierOrNull)
    {
        this.experimentIdentifierOrNull = experimentIdentifierOrNull;
    }

    public Collection<NewAttachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(Collection<NewAttachment> attachments)
    {
        this.attachments = attachments;
    }
}
