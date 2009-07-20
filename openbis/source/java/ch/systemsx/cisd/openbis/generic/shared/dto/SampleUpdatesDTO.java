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

import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicSampleUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * @author Izabela Adamczyk
 */
public class SampleUpdatesDTO extends BasicSampleUpdates
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private ExperimentIdentifier experimentIdentifierOrNull;

    private List<AttachmentPE> attachments;

    public SampleUpdatesDTO(TechId sampleId, List<SampleProperty> properties,
            ExperimentIdentifier experimentIdentifierOrNull, List<AttachmentPE> attachments,
            Date version)
    {
        super(sampleId, properties, version);
        this.experimentIdentifierOrNull = experimentIdentifierOrNull;
        this.attachments = attachments;
    }

    public ExperimentIdentifier getExperimentIdentifierOrNull()
    {
        return experimentIdentifierOrNull;
    }

    public void setExperimentIdentifierOrNull(ExperimentIdentifier experimentIdentifierOrNull)
    {
        this.experimentIdentifierOrNull = experimentIdentifierOrNull;
    }

    public List<AttachmentPE> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(List<AttachmentPE> attachments)
    {
        this.attachments = attachments;
    }
}