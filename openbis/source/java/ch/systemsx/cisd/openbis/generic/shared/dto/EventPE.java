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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * Persistent entity representing an event which changed some entities in the persistent layer.
 *
 * @author Franz-Josef Elmer
 */
@Entity
@Table(name = TableNames.EVENTS_TABLE)
public class EventPE extends HibernateAbstractRegistrationHolder implements IIdHolder, Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private transient Long id;
    
    private EventType eventType;
    
    private DataPE data;

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
    public final EventType getEventType()
    {
        return eventType;
    }

    public final void setEventType(final EventType eventType)
    {
        this.eventType = eventType;
    }
    
    @NotNull(message = ValidationMessages.DATA_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.DATA_ID_COLUMN, updatable = false)
    @Private
    public final DataPE getDataInternal()
    {
        return data;
    }

    @Private
    public final void setDataInternal(DataPE data)
    {
        this.data = data;
    }

    @Transient
    public final DataPE getData()
    {
        return getDataInternal();
    }
    
    @Length(max = 250, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(String description)
    {
        this.description = description;
    }

    @Length(max = 250, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public final String getReason()
    {
        return reason;
    }

    public final void setReason(final String reason)
    {
        this.reason = reason;
    }

}
