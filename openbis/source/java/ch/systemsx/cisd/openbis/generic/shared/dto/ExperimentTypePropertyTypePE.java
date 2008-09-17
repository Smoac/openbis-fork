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

package ch.systemsx.cisd.openbis.generic.shared.dto;

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

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * Persistence entity representing experiment type - property type relation.
 * 
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.EXPERIMENT_TYPE_PROPERTY_TYPE_TABLE, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.ID_COLUMN }) })
public class ExperimentTypePropertyTypePE extends EntityTypePropertyTypePE
{

    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    public static final ExperimentTypePropertyTypePE[] EMPTY_ARRAY =
            new ExperimentTypePropertyTypePE[0];

    //
    // EntityTypePropertyTypePE
    //

    @NotNull(message = ValidationMessages.EXPERIMENT_TYPE_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER, targetEntity = ExperimentTypePE.class)
    @JoinColumn(name = ColumnNames.EXPERIMENT_TYPE_COLUMN)
    public EntityTypePE getEntityType()
    {
        return entityType;
    }

    @SequenceGenerator(name = SequenceNames.EXPERIMENT_TYPE_PROPERTY_TYPE_SEQUENCE, sequenceName = SequenceNames.EXPERIMENT_TYPE_PROPERTY_TYPE_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.EXPERIMENT_TYPE_PROPERTY_TYPE_SEQUENCE)
    public Long getId()
    {
        return id;
    }

}
