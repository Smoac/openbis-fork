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

package ch.systemsx.cisd.openbis.dss.etl;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateDimension;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateDimensionParser;

/**
 * Describes one dataset container (e.g. plate) with images.
 * 
 * @author Tomasz Pylak
 */
public class ScreeningContainerDatasetInfo
{
    private String experimentPermId;

    private String containerPermId;

    private String datasetPermId;

    private int containerRows, containerColumns;

    public String getExperimentPermId()
    {
        return experimentPermId;
    }

    public void setExperimentPermId(String experimentPermId)
    {
        this.experimentPermId = experimentPermId;
    }

    public String getContainerPermId()
    {
        return containerPermId;
    }

    public void setContainerPermId(String containerPermId)
    {
        this.containerPermId = containerPermId;
    }

    public String getDatasetPermId()
    {
        return datasetPermId;
    }

    public void setDatasetPermId(String datasetPermId)
    {
        this.datasetPermId = datasetPermId;
    }

    public int getContainerRows()
    {
        return containerRows;
    }

    public void setContainerRows(int containerRows)
    {
        this.containerRows = containerRows;
    }

    public int getContainerColumns()
    {
        return containerColumns;
    }

    public void setContainerColumns(int containerColumns)
    {
        this.containerColumns = containerColumns;
    }

    public static ScreeningContainerDatasetInfo createScreeningDatasetInfo(
            DataSetInformation dataSetInformation)
    {
        Sample sample = dataSetInformation.tryToGetSample();
        assert sample != null : "no sample connected to a dataset";
        PlateDimension plateGeometry = getPlateGeometry(dataSetInformation);
        ScreeningContainerDatasetInfo info =
                createBasicScreeningDataSetInfo(dataSetInformation, sample, plateGeometry);
        return info;
    }

    /**
     * Create a screening data set info given sample.
     */
    public static ScreeningContainerDatasetInfo createScreeningDatasetInfoWithSample(
            DataSetInformation dataSetInformation, Sample containingSample)
    {
        Sample sample = containingSample;
        assert sample != null : "no sample connected to a dataset";
        PlateDimension plateGeometry = getPlateGeometry(sample);
        ScreeningContainerDatasetInfo info =
                createBasicScreeningDataSetInfo(dataSetInformation, sample, plateGeometry);
        return info;
    }

    private static ScreeningContainerDatasetInfo createBasicScreeningDataSetInfo(
            DataSetInformation dataSetInformation, Sample sample, PlateDimension plateGeometry)
    {
        Experiment experiment = dataSetInformation.tryToGetExperiment();
        ScreeningContainerDatasetInfo info = new ScreeningContainerDatasetInfo();
        info.setExperimentPermId(experiment.getPermId());
        info.setContainerPermId(sample.getPermId());
        info.setDatasetPermId(dataSetInformation.getDataSetCode());
        info.setContainerRows(plateGeometry.getRowsNum());
        info.setContainerColumns(plateGeometry.getColsNum());
        return info;
    }

    private static PlateDimension getPlateGeometry(Sample sample)
    {
        final IEntityProperty[] sampleProperties =
                (sample.getProperties()).toArray(new IEntityProperty[0]);
        final PlateDimension plateDimension =
                PlateDimensionParser.tryToGetPlateDimension(sampleProperties);
        if (plateDimension == null)
        {
            throw new EnvironmentFailureException(
                    "Missing plate geometry for the plate registered for sample identifier '"
                            + sample.getIdentifier() + "'.");
        }
        return plateDimension;
    }

    static PlateDimension getPlateGeometry(final DataSetInformation dataSetInformation)
    {
        final IEntityProperty[] sampleProperties = dataSetInformation.getProperties();
        final PlateDimension plateDimension =
                PlateDimensionParser.tryToGetPlateDimension(sampleProperties);
        if (plateDimension == null)
        {
            throw new EnvironmentFailureException(
                    "Missing plate geometry for the plate registered for sample identifier '"
                            + dataSetInformation.getSampleIdentifier() + "'.");
        }
        return plateDimension;
    }

}