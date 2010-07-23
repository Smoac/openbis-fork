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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.validator.NotNull;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * <i>Persistent Entity</i> object representing sample relationship.
 * 
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.SAMPLE_RELATIONSHIPS_TABLE, uniqueConstraints = @UniqueConstraint(columnNames =
    { ColumnNames.PARENT_SAMPLE_COLUMN, ColumnNames.CHILD_SAMPLE_COLUMN,
            ColumnNames.RELATIONSHIP_COLUMN }))
public class SampleRelationshipPE implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    protected transient Long id;

    private SamplePE parentSample;

    private SamplePE childSample;

    private RelationshipTypePE relationship;

    @Deprecated
    public SampleRelationshipPE()
    {
    }

    public SampleRelationshipPE(SamplePE parentSample, SamplePE childSample,
            RelationshipTypePE relationship)
    {
        this.parentSample = parentSample;
        this.childSample = childSample;
        this.relationship = relationship;
        parentSample.addChildRelationship(this);
        childSample.addParentRelationship(this);
    }

    @NotNull(message = ValidationMessages.PARENT_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PARENT_SAMPLE_COLUMN)
    public SamplePE getParentSample()
    {
        return parentSample;
    }

    public void setParentSample(SamplePE parentSample)
    {
        this.parentSample = parentSample;
    }

    @NotNull(message = ValidationMessages.CHILD_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.CHILD_SAMPLE_COLUMN)
    public SamplePE getChildSample()
    {
        return childSample;
    }

    public void setChildSample(SamplePE childSample)
    {
        this.childSample = childSample;
    }

    @NotNull(message = ValidationMessages.RELATIONSHIP_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.RELATIONSHIP_COLUMN)
    public RelationshipTypePE getRelationship()
    {
        return relationship;
    }

    public void setRelationship(RelationshipTypePE relationship)
    {
        this.relationship = relationship;
    }

    @SequenceGenerator(name = SequenceNames.SAMPLE_RELATIONSHIPS_SEQUENCE, sequenceName = SequenceNames.SAMPLE_RELATIONSHIPS_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.SAMPLE_RELATIONSHIPS_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

}
