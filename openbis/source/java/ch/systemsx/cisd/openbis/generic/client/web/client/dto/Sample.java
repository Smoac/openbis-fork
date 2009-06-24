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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.common.annotation.CollectionMapping;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.IPermIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeWithRegistration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * <i>Java Bean</i> which contain information about <i>sample</i>.
 * 
 * @author Izabela Adamczyk
 */
public final class Sample extends CodeWithRegistration<Sample> implements IInvalidationProvider,
        Comparable<Sample>, IEntityInformationHolder, IAttachmentHolder, IEntityPropertiesHolder,
        IIdentifiable, IPermIdHolder
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final Sample[] EMPTY_ARRAY = new Sample[0];

    private SampleType sampleType;

    private Group group;

    private DatabaseInstance databaseInstance;

    private String identifier;

    private Sample container;

    private Sample generatedFrom;

    private List<SampleProperty> properties;

    private Invalidation invalidation;

    private Experiment experiment;

    private Long id;

    private Date modificationDate;

    private List<Attachment> attachments;

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
        return AttachmentHolderKind.SAMPLE;
    }

    public SampleType getSampleType()
    {
        return sampleType;
    }

    public void setSampleType(final SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    public void setGroup(final Group group)
    {
        this.group = group;

    }

    public Group getGroup()
    {
        return group;
    }

    public DatabaseInstance getDatabaseInstance()
    {
        return databaseInstance;
    }

    public void setDatabaseInstance(final DatabaseInstance databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    public void setIdentifier(final String sampleIdentifer)
    {
        this.identifier = sampleIdentifer;
    }

    public Sample getContainer()
    {
        return container;
    }

    public void setContainer(final Sample container)
    {
        this.container = container;
    }

    public Sample getGeneratedFrom()
    {
        return generatedFrom;
    }

    public void setGeneratedFrom(final Sample generatedFrom)
    {
        this.generatedFrom = generatedFrom;
    }

    public List<SampleProperty> getProperties()
    {
        return properties;
    }

    @CollectionMapping(collectionClass = ArrayList.class, elementClass = SampleProperty.class)
    public void setProperties(final List<SampleProperty> properties)
    {
        this.properties = properties;
    }

    public final void setInvalidation(final Invalidation invalidation)
    {
        this.invalidation = invalidation;
    }

    public final Experiment getExperiment()
    {
        return experiment;
    }

    public final void setExperiment(Experiment experiment)
    {
        this.experiment = experiment;
    }

    //
    // IIdentifierHolder
    //

    public String getIdentifier()
    {
        return identifier;
    }

    //
    // IInvalidationProvider
    //

    public final Invalidation getInvalidation()
    {
        return invalidation;
    }

    //
    // Comparable
    //

    @Override
    public final int compareTo(final Sample o)
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
        return getSampleType();
    }

    public EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

    public void setAttachments(List<Attachment> attachments)
    {
        this.attachments = attachments;
    }

    public List<Attachment> getAttachments()
    {
        return attachments;
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
