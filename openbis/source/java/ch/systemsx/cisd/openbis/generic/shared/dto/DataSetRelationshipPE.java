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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

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

    /**
     * Deletion information.
     * <p>
     * If not <code>null</code>, then this data set is considered <i>deleted</i> (moved to trash).
     * </p>
     */
    private DeletionPE deletion;

    @Deprecated
    public DataSetRelationshipPE()
    {
    }

    public DataSetRelationshipPE(DataPE parentDataSet, DataPE childDataSet)
    {
        this.parentDataSet = parentDataSet;
        this.childDataSet = childDataSet;
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
