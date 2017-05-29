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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Persistence entity representing a historical data set property.
 * 
 * @author Franz-Josef Elmer
 */
@Entity
@Table(name = TableNames.DATA_SET_HISTORY_VIEW)
public class DataSetHistoryPE extends AbstractEntityHistoryPE
{
    private static final long serialVersionUID = IServer.VERSION;

    private SamplePE sample;

    private DataPE dataSet;

    private ExperimentPE experiment;

    private ExternalDataManagementSystemPE externalDms;

    private String externalCode;

    private String path;

    private String gitCommitHash;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = DataPE.class)
    @JoinColumn(name = ColumnNames.MAIN_DATA_SET_COLUMN)
    DataPE getEntityInternal()
    {
        return (DataPE) entity;
    }

    @SuppressWarnings("unused")
    private void setEntityInternal(DataPE dataSet)
    {
        entity = dataSet;
    }

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = DataSetTypePropertyTypePE.class)
    @JoinColumn(name = ColumnNames.DATA_SET_TYPE_PROPERTY_TYPE_COLUMN)
    public EntityTypePropertyTypePE getEntityTypePropertyTypeInternal()
    {
        return entityTypePropertyType;
    }

    @SuppressWarnings("unused")
    private void setEntityTypePropertyTypeInternal(DataSetTypePropertyTypePE dataSetTypePropertyType)
    {
        entityTypePropertyType = dataSetTypePropertyType;
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

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = ExternalDataManagementSystemPE.class)
    @JoinColumn(name = ColumnNames.EXTERNAL_DATA_MANAGEMENT_SYSTEM_ID_COLUMN)
    public ExternalDataManagementSystemPE getExternalDms()
    {
        return externalDms;
    }

    public void setExternalDms(ExternalDataManagementSystemPE externalDms)
    {
        this.externalDms = externalDms;
    }

    @Column(name = ColumnNames.EXTERNAL_CODE_COLUMN)
    public String getExternalCode()
    {
        return externalCode;
    }

    public void setExternalCode(String externalCode)
    {
        this.externalCode = externalCode;
    }

    @Column(name = ColumnNames.PATH_COLUMN)
    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    @Column(name = ColumnNames.GIT_COMMIT_HASH_COLUMN)
    public String getGitCommitHash()
    {
        return gitCommitHash;
    }

    public void setGitCommitHash(String gitCommitHash)
    {
        this.gitCommitHash = gitCommitHash;
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
        } else if (externalDms != null)
        {
            return new MatchingContentCopy(externalCode, path, gitCommitHash, externalDms);
        }
        return null;
    }

}
