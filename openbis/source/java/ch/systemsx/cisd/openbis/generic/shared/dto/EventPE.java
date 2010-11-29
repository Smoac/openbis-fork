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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * Persistent entity representing an event which changed some entities in the persistent layer.
 * 
 * @author     Franz-Josef Elmer
 */
@Entity
@Table(name = TableNames.EVENTS_TABLE)
@Friend(toClasses = DataPE.class)
public class EventPE extends HibernateAbstractRegistrationHolder implements IIdHolder, Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    public enum EntityType
    {
        ATTACHMENT, DATASET, EXPERIMENT, SPACE, MATERIAL, PROJECT, PROPERTY_TYPE, SAMPLE,
        VOCABULARY, AUTHORIZATION_GROUP;
    }

    private transient Long id;

    private EventType eventType;

    private EntityType entityType;

    private String identifier;

    private String description;

    private String reason;

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @Id
    @SequenceGenerator(name = SequenceNames.EVENT_SEQUENCE, sequenceName = SequenceNames.EVENT_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.EVENT_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @NotNull(message = ValidationMessages.EVENT_TYPE_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.EVENT_TYPE)
    @Enumerated(EnumType.STRING)
    public EventType getEventType()
    {
        return eventType;
    }

    public void setEventType(final EventType eventType)
    {
        this.eventType = eventType;
    }

    @NotNull(message = ValidationMessages.ENTITY_TYPE_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.ENTITY_TYPE)
    @Enumerated(EnumType.STRING)
    public EntityType getEntityType()
    {
        return entityType;
    }

    public void setEntityType(EntityType entityType)
    {
        this.entityType = entityType;
    }

    @NotNull(message = ValidationMessages.IDENTIFIER_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.IDENTIFIER)
    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    @Length(max = GenericConstants.DESCRIPTION_2000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @Length(max = GenericConstants.DESCRIPTION_2000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public String getReason()
    {
        return reason;
    }

    public void setReason(final String reason)
    {
        this.reason = reason;
    }

    //
    // Object
    // 

    @Override
    public final String toString()
    {
        return ToStringBuilder
                .reflectionToString(ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }

    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj, true);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this, true);
    }

}
