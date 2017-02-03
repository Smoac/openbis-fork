/*
 * Copyright 2012 ETH Zuerich, CISD
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Persistence entity representing a historical sample.
 * 
 * @author Franz-Josef Elmer
 */
@Entity
@Table(name = TableNames.SAMPLE_HISTORY_VIEW)
public class SampleHistoryPE extends AbstractEntityHistoryPE
{
    private static final long serialVersionUID = IServer.VERSION;

    private SamplePE sample;

    private DataPE dataSet;

    private SpacePE space;

    private ExperimentPE experiment;

    private ProjectPE project;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = SamplePE.class)
    @JoinColumn(name = ColumnNames.MAIN_SAMPLE_COLUMN)
    SamplePE getEntityInternal()
    {
        return (SamplePE) entity;
    }

    @SuppressWarnings("unused")
    private void setEntityInternal(SamplePE sample)
    {
        entity = sample;
    }

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = SampleTypePropertyTypePE.class)
    @JoinColumn(name = ColumnNames.SAMPLE_TYPE_PROPERTY_TYPE_COLUMN)
    public EntityTypePropertyTypePE getEntityTypePropertyTypeInternal()
    {
        return entityTypePropertyType;
    }

    @SuppressWarnings("unused")
    private void setEntityTypePropertyTypeInternal(SampleTypePropertyTypePE sampleTypePropertyType)
    {
        entityTypePropertyType = sampleTypePropertyType;
    }

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = SamplePE.class)
    @JoinColumn(name = ColumnNames.SAMPLE_COLUMN)
    public SamplePE getSample()
    {
        return sample;
    }

    public void setSample(SamplePE sample)
    {
        this.sample = sample;
    }

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = DataPE.class)
    @JoinColumn(name = ColumnNames.DATA_ID_COLUMN)
    public DataPE getDataSet()
    {
        return dataSet;
    }

    public void setDataSet(DataPE dataSet)
    {
        this.dataSet = dataSet;
    }

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = SpacePE.class)
    @JoinColumn(name = ColumnNames.SPACE_COLUMN)
    private SpacePE getSpaceInternal()
    {
        return space;
    }

    @Override
    @Transient
    public SpacePE getSpace()
    {
        return getSpaceInternal();
    }

    @SuppressWarnings("unused")
    private void setSpaceInternal(SpacePE space)
    {
        this.space = space;
    }

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = ExperimentPE.class)
    @JoinColumn(name = ColumnNames.EXPERIMENT_COLUMN)
    public ExperimentPE getExperiment()
    {
        return experiment;
    }

    public void setExperiment(ExperimentPE experiment)
    {
        this.experiment = experiment;
    }

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = ProjectPE.class)
    @JoinColumn(name = ColumnNames.PROJECT_COLUMN)
    public ProjectPE getProject()
    {
        return project;
    }

    public void setProject(ProjectPE project)
    {
        this.project = project;
    }

    @Override
    @Transient
    public IMatchingEntity getRelatedEntity()
    {
        if (experiment != null)
        {
            return experiment;
        } else if (sample != null)
        {
            return sample;
        } else if (dataSet != null)
        {
            return dataSet;
        }
        return null;
    }
}
