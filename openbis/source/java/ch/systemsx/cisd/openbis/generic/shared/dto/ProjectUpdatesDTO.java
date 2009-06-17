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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractProjectUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * Server side description of the updates which should be performed on the project.
 * 
 * @author Tomasz Pylak
 */
public class ProjectUpdatesDTO extends AbstractProjectUpdates
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private ProjectIdentifier identifier;

    // ----- the data which should be changed:

    // new attachments which will be added to the old ones
    private List<AttachmentPE> attachments;

    public ProjectIdentifier getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(ProjectIdentifier identifier)
    {
        this.identifier = identifier;
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
