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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import ch.systemsx.cisd.common.collections.UnmodifiableListDecorator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Persistent entity representing script definition.
 * 
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.SCRIPTS_TABLE)
public class ScriptPE extends HibernateAbstractRegistrationHolder implements IIdHolder,
        Comparable<ScriptPE>, Serializable
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    protected Long id;

    private String name;

    private String description;

    private DatabaseInstancePE databaseInstance;

    private String script;

    //
    // assignments using the script - readonly
    //

    private List<SampleTypePropertyTypePE> sampleAssignments =
            new ArrayList<SampleTypePropertyTypePE>();

    private List<ExperimentTypePropertyTypePE> experimentAssignments =
            new ArrayList<ExperimentTypePropertyTypePE>();

    private List<MaterialTypePropertyTypePE> materialAssignments =
            new ArrayList<MaterialTypePropertyTypePE>();

    private List<DataSetTypePropertyTypePE> dataSetAssignments =
            new ArrayList<DataSetTypePropertyTypePE>();

    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    @Length(max = GenericConstants.DESCRIPTION_2000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.DATABASE_INSTANCE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.DATABASE_INSTANCE_COLUMN, updatable = false)
    public DatabaseInstancePE getDatabaseInstance()
    {
        return databaseInstance;
    }

    public void setDatabaseInstance(final DatabaseInstancePE databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @SequenceGenerator(name = SequenceNames.SCRIPT_SEQUENCE, sequenceName = SequenceNames.SCRIPT_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.SCRIPT_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @Column(name = ColumnNames.NAME_COLUMN)
    @NotNull(message = ValidationMessages.NAME_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 200, message = ValidationMessages.NAME_LENGTH_MESSAGE)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Column(name = ColumnNames.SCRIPT_COLUMN)
    @NotNull(message = ValidationMessages.SCRIPT_NOT_NULL_MESSAGE)
    @Length(min = 1, message = ValidationMessages.EXPRESSION_LENGTH_MESSAGE)
    public String getScript()
    {
        return script;
    }

    public void setScript(String script)
    {
        this.script = script;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ScriptPE == false)
        {
            return false;
        }
        final ScriptPE that = (ScriptPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getName(), that.getName());
        builder.append(getDatabaseInstance(), that.getDatabaseInstance());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getName());
        builder.append(getDatabaseInstance());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        return getName();
    }

    public int compareTo(ScriptPE that)
    {
        final String thatName = that.getName();
        final String thisName = getName();
        if (thisName == null)
        {
            return thatName == null ? 0 : -1;
        }
        if (thatName == null)
        {
            return 1;
        }
        return thisName.compareTo(thatName);
    }

    //
    // assignments using the script - readonly
    //

    @Transient
    /** all dynamic property assignments using the script */
    public List<EntityTypePropertyTypePE> getPropertyAssignments()
    {
        List<EntityTypePropertyTypePE> assignments = new ArrayList<EntityTypePropertyTypePE>();
        assignments.addAll(getDataSetAssignments());
        assignments.addAll(getExperimentAssignments());
        assignments.addAll(getMaterialAssignments());
        assignments.addAll(getSampleAssignments());
        return new UnmodifiableListDecorator<EntityTypePropertyTypePE>(assignments);
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "script")
    private List<SampleTypePropertyTypePE> getSampleAssignments()
    {
        return sampleAssignments;
    }

    @SuppressWarnings("unused")
    private void setSampleAssignments(List<SampleTypePropertyTypePE> sampleAssignments)
    {
        this.sampleAssignments = sampleAssignments;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "script")
    private List<ExperimentTypePropertyTypePE> getExperimentAssignments()
    {
        return experimentAssignments;
    }

    @SuppressWarnings("unused")
    private void setExperimentAssignments(List<ExperimentTypePropertyTypePE> experimentAssignments)
    {
        this.experimentAssignments = experimentAssignments;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "script")
    private List<MaterialTypePropertyTypePE> getMaterialAssignments()
    {
        return materialAssignments;
    }

    @SuppressWarnings("unused")
    private void setMaterialAssignments(List<MaterialTypePropertyTypePE> materialAssignments)
    {
        this.materialAssignments = materialAssignments;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "script")
    private List<DataSetTypePropertyTypePE> getDataSetAssignments()
    {
        return dataSetAssignments;
    }

    @SuppressWarnings("unused")
    private void setDataSetAssignments(List<DataSetTypePropertyTypePE> dataSetAssignments)
    {
        this.dataSetAssignments = dataSetAssignments;
    }
}
