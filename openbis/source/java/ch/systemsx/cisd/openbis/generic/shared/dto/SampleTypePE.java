/*
 * Copyright 2007 ETH Zuerich, CISD
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.SampleTypeCode;

/**
 * Persistence Entity representing 'sample type'.
 * 
 * @author Christian Ribeaud
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.SAMPLE_TYPES_TABLE, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.CODE_COLUMN, ColumnNames.DATABASE_INSTANCE_COLUMN }) })
public final class SampleTypePE extends EntityTypePE
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private Boolean isListable;

    private Integer generatedFromHierarchyDepth;

    private Integer containerHierarchyDepth;

    @Column(name = ColumnNames.IS_LISTABLE)
    public Boolean isListable()
    {
        return isListable;
    }

    public void setListable(boolean isListable)
    {
        this.isListable = isListable;
    }

    @Column(name = ColumnNames.GENERATED_FROM_DEPTH)
    public Integer getGeneratedFromHierarchyDepth()
    {
        return generatedFromHierarchyDepth;
    }

    public void setGeneratedFromHierarchyDepth(int generatedFromHierarchyDepth)
    {
        this.generatedFromHierarchyDepth = generatedFromHierarchyDepth;
    }

    @Column(name = ColumnNames.PART_OF_DEPTH)
    public Integer getContainerHierarchyDepth()
    {
        return containerHierarchyDepth;
    }

    public void setContainerHierarchyDepth(int partOfHierarchyDepth)
    {
        this.containerHierarchyDepth = partOfHierarchyDepth;
    }

    @Transient
    public final boolean isPlate()
    {
        return getCode().endsWith("_PLATE");
    }

    @Transient
    public final boolean isControlLayout()
    {
        return SampleTypeCode.CONTROL_LAYOUT.getCode().equals(getCode());
    }

    //
    // EntityTypePE
    //

    @SequenceGenerator(name = SequenceNames.SAMPLE_TYPE_SEQUENCE, sequenceName = SequenceNames.SAMPLE_TYPE_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.SAMPLE_TYPE_SEQUENCE)
    public final Long getId()
    {
        return id;
    }
}