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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ch.systemsx.cisd.common.annotation.CollectionMapping;
import ch.systemsx.cisd.openbis.generic.shared.basic.IAttachmentHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IInvalidationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.IPermIdHolder;

/**
 * <i>Java Bean</i> which contain information about <i>sample</i>.
 * 
 * @author Izabela Adamczyk
 */
public final class Sample extends CodeWithRegistration<Sample> implements IInvalidationProvider,
        Comparable<Sample>, IEntityInformationHolderWithIdentifier,
        IEntityInformationHolderWithPermId, IAttachmentHolder, IEntityPropertiesHolder,
        IIdAndCodeHolder, IPermIdHolder
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final Sample[] EMPTY_ARRAY = new Sample[0];

    private SampleType sampleType;

    private Space space;

    private DatabaseInstance databaseInstance;

    private String identifier;

    private Sample container;

    private Set<Sample> parents = new TreeSet<Sample>();

    private List<IEntityProperty> properties;

    private Invalidation invalidation;

    private Experiment experiment;

    private Long id;

    private Date modificationDate;

    private List<Attachment> attachments;

    private String permId;

    private String permlink;

    private String searchlink;

    private String subCode;

    public String getSubCode()
    {
        return subCode;
    }

    public void setSubCode(String subCode)
    {
        this.subCode = subCode;
    }

    public String getPermlink()
    {
        return permlink;
    }

    public void setPermlink(String permlink)
    {
        this.permlink = permlink;
    }

    public String getSearchlink()
    {
        return searchlink;
    }

    public void setSearchlink(String searchlink)
    {
        this.searchlink = searchlink;
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

    public void setSpace(final Space space)
    {
        this.space = space;

    }

    /** can be null */
    public Space getSpace()
    {
        return space;
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

    public Set<Sample> getParents()
    {
        return parents;
    }

    public void setParents(Set<Sample> parents)
    {
        this.parents = parents;
    }

    public void addParent(final Sample parent)
    {
        parents.add(parent);
    }

    public Sample getGeneratedFrom()
    {
        if (parents.size() == 0)
        {
            return null;
        }
        if (parents.size() > 1)
        {
            throw new IllegalStateException("Sample " + getIdentifier()
                    + " has more than one parent");
        }
        return parents.iterator().next();
    }

    // used only for testing
    public void setGeneratedFrom(final Sample generatedFrom)
    {
        parents = new TreeSet<Sample>();
        parents.add(generatedFrom);
    }

    public List<IEntityProperty> getProperties()
    {
        return properties;
    }

    @CollectionMapping(collectionClass = ArrayList.class, elementClass = EntityProperty.class)
    public void setProperties(final List<IEntityProperty> properties)
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

    @Override
    public String toString()
    {
        return "Sample [sampleType=" + sampleType + ", space=" + space + ", databaseInstance="
                + databaseInstance + ", identifier=" + identifier + ", container=" + container
                + ", parents=" + parents + ", properties=" + properties + ", invalidation="
                + invalidation + ", experiment=" + experiment + ", id=" + id
                + ", modificationDate=" + modificationDate + ", attachments=" + attachments
                + ", permId=" + permId + ", permlink=" + permlink + ", searchlink=" + searchlink
                + ", subCode=" + subCode + "]";
    }
}
