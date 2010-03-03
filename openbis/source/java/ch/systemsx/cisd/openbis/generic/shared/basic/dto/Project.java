/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.IAttachmentHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;

/**
 * The <i>GWT</i> equivalent to ProjectPE.
 * 
 * @author Tomasz Pylak
 */
public class Project extends CodeWithRegistration<Project> implements IAttachmentHolder,
        IIdentifiable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Space space;

    private String description;

    private Person projectLeader;

    private String identifier;

    private Long id;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    private List<Attachment> attachments;

    // TODO 2009-06-17, Piotr Buczek: remove and create NewProject with NewAttachments, ...
    private List<NewAttachment> newAttachments;

    private Date modificationDate;

    public AttachmentHolderKind getAttachmentHolderKind()
    {
        return AttachmentHolderKind.PROJECT;
    }

    public Space getSpace()
    {
        return space;
    }

    public void setSpace(final Space space)
    {
        this.space = space;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public Person getProjectLeader()
    {
        return projectLeader;
    }

    public void setProjectLeader(final Person projectLeader)
    {
        this.projectLeader = projectLeader;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public void setAttachments(List<Attachment> attachments)
    {
        this.attachments = attachments;
    }

    public List<Attachment> getAttachments()
    {
        return attachments;
    }

    public void setNewAttachments(List<NewAttachment> newAttachments)
    {
        this.newAttachments = newAttachments;
    }

    public List<NewAttachment> getNewAttachments()
    {
        return newAttachments;
    }

    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

}
