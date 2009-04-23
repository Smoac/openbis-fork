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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeWithRegistration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * The <i>GWT</i> equivalent to {@link ExperimentPE}.
 * 
 * @author Tomasz Pylak
 */
public class Experiment extends CodeWithRegistration<Experiment> implements IInvalidationProvider,
        IEntityInformationHolder, IAttachmentHolder, IEntityPropertiesHolder
{
    private Project project;

    private ExperimentType experimentType;

    private String identifier;

    private List<ExperimentProperty> properties;

    private Invalidation invalidation;

    private List<Attachment> attachments;

    private Long id;

    private Date modificationDate;

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

    public List<ExperimentProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(final List<ExperimentProperty> properties)
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
}
