/*
 * Copyright 2010 ETH Zuerich, CISD
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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Resolution;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * <i>Persistent Entity</i> object representing data set relationship.
 * 
 * @author Pawel Glyzewski
 */
@Entity
@Table(name = TableNames.DATA_SET_RELATIONSHIPS_VIEW, uniqueConstraints = @UniqueConstraint(columnNames =
    { ColumnNames.DATA_PARENT_COLUMN, ColumnNames.DATA_CHILD_COLUMN }))
@IdClass(DataSetRelationshipId.class)
public class DataSetRelationshipPE implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private DataPE parentDataSet;

    private DataPE childDataSet;

    private PersonPE author;

    private Date registrationDate;

    private Date modificationDate;

    /**
     * Deletion information.
     * <p>
     * If not <code>null</code>, then this data set is considered <i>deleted</i> (moved to trash).
     * </p>
     */
    private DeletionPE deletion;

    @SuppressWarnings("unused")
    private DataSetRelationshipPE()
    {
    }

    public DataSetRelationshipPE(DataPE parentDataSet, DataPE childDataSet, PersonPE author)
    {
        this.parentDataSet = parentDataSet;
        this.childDataSet = childDataSet;
        this.author = author;
    }

    @NotNull(message = ValidationMessages.PARENT_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.DATA_PARENT_COLUMN)
    @Id
    public DataPE getParentDataSet()
    {
        return parentDataSet;
    }

    public void setParentDataSet(DataPE parentDataSet)
    {
        this.parentDataSet = parentDataSet;
    }

    @NotNull(message = ValidationMessages.CHILD_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.DATA_CHILD_COLUMN)
    @Id
    public DataPE getChildDataSet()
    {
        return childDataSet;
    }

    public void setChildDataSet(DataPE childDataSet)
    {
        this.childDataSet = childDataSet;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.PERSON_AUTHOR_COLUMN)
    public PersonPE getAuthor()
    {
        return author;
    }

    public void setAuthor(PersonPE author)
    {
        this.author = author;
    }

    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false, insertable = false, updatable = false)
    @Generated(GenerationTime.INSERT)
    @DateBridge(resolution = Resolution.SECOND)
    public Date getRegistrationDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(registrationDate);
    }

    public void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    @Version
    @Column(name = ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, nullable = false)
    @DateBridge(resolution = Resolution.SECOND)
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date versionDate)
    {
        this.modificationDate = versionDate;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.DELETION_COLUMN)
    public DeletionPE getDeletion()
    {
        return deletion;
    }

    public void setDeletion(final DeletionPE deletion)
    {
        this.deletion = deletion;
    }
}
