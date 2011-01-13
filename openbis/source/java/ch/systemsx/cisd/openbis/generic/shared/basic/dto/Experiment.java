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
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IInvalidationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.IPermIdHolder;

/**
 * The <i>GWT</i> equivalent to ExperimentPE.
 * 
 * @author Tomasz Pylak
 */
public class Experiment extends CodeWithRegistration<Experiment> implements IInvalidationProvider,
        IEntityInformationHolderWithProperties, IAttachmentHolder, IIdAndCodeHolder, IPermIdHolder
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Project project;

    private ExperimentType experimentType;

    private String identifier;

    private List<IEntityProperty> properties;

    private Invalidation invalidation;

    private List<Attachment> attachments;

    private Long id;

    private Date modificationDate;

    private String permId;

    private String permlink;

    public String getPermlink()
    {
        return permlink;
    }

    public void setPermlink(String permlink)
    {
        this.permlink = permlink;
    }

    public AttachmentHolderKind getAttachmentHolderKind()
    {
        return AttachmentHolderKind.EXPERIMENT;
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject(final Project project)
    {
        this.project = project;
    }

    public ExperimentType getExperimentType()
    {
        return experimentType;
    }

    public void setExperimentType(final ExperimentType experimentType)
    {
        this.experimentType = experimentType;
    }

    public final void setIdentifier(final String experimentIdentifier)
    {
        this.identifier = experimentIdentifier;
    }

    public List<IEntityProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(final List<IEntityProperty> properties)
    {
        this.properties = properties;
    }

    public Invalidation getInvalidation()
    {
        return invalidation;
    }

    public void setInvalidation(final Invalidation invalidation)
    {
        this.invalidation = invalidation;
    }

    public List<Attachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(final List<Attachment> attachments)
    {
        this.attachments = attachments;
    }

    //
    // IIdentifierHolder
    //

    public final String getIdentifier()
    {
        return identifier;
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return "Experiment [project=" + project + ", experimentType=" + experimentType
                + ", identifier=" + identifier + ", properties=" + properties + ", invalidation="
                + invalidation + ", attachments=" + attachments + ", id=" + id
                + ", modificationDate=" + modificationDate + ", permId=" + permId + ", permlink="
                + permlink + "]";
    }

    //
    // Comparable
    //

    @Override
    public final int compareTo(final Experiment o)
    {
        return getIdentifier().compareTo(o.getIdentifier());
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    public EntityType getEntityType()
    {
        return getExperimentType();
    }

    public EntityKind getEntityKind()
    {
        return EntityKind.EXPERIMENT;
    }

    public void setPermId(String permId)
    {
        this.permId = permId;
    }

    public String getPermId()
    {
        return permId;
    }

}
