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

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicExperimentUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * Description of the updates which should be performed on the experiment.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentUpdatesDTO extends BasicExperimentUpdates
{
    private ExperimentIdentifier experimentIdentifier;

    // ----- the data which should be changed:

    // points to the project which will be set for the experiment
    // TODO 2009-04-21, Tomasz Pylak: can it be null?
    private ProjectIdentifier projectIdentifier;

    // new attachments which will be added to the old ones
    List<AttachmentPE> attachments;

    private SampleType sampleType;

    public List<AttachmentPE> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(List<AttachmentPE> attachments)
    {
        this.attachments = attachments;
    }

    public ExperimentIdentifier getExperimentIdentifier()
    {
        return experimentIdentifier;
    }

    public void setExperimentIdentifier(ExperimentIdentifier experimentIdentifier)
    {
        this.experimentIdentifier = experimentIdentifier;
    }

    public ProjectIdentifier getProjectIdentifier()
    {
        return projectIdentifier;
    }

    public void setProjectIdentifier(ProjectIdentifier projectIdentifier)
    {
        this.projectIdentifier = projectIdentifier;
    }

    public void setSampleType(SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    public SampleType getSampleType()
    {
        return sampleType;
    }
}
