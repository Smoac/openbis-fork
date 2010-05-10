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

package ch.systemsx.cisd.yeastx.etl;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.etlserver.IDataSetUploader;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.yeastx.db.IDatasetLoader;
import ch.systemsx.cisd.yeastx.db.generic.DMDataSetDTO;
import ch.systemsx.cisd.yeastx.eicml.EICML2Database;
import ch.systemsx.cisd.yeastx.fiaml.FIAML2Database;
import ch.systemsx.cisd.yeastx.mzxml.MzXml2Database;
import ch.systemsx.cisd.yeastx.quant.QuantML2Database;

/**
 * Extracts and uploads information from dataset files (like <code>eicML</code> or
 * <code>fiaML</code> files) to the additional database.
 * 
 * @author Tomasz Pylak
 */
public class ML2DatabaseUploader implements IDataSetUploader
{
    private static final String UNKNOWN_NAME = "unknown";

    private final IDatasetLoader eicML2Database;

    private final IDatasetLoader fiaML2Database;

    private final IDatasetLoader quantML2Database;

    private final IDatasetLoader mzXml2Database;

    private final String uniqueSampleNamePropertyCode;

    private final String uniqueExperimentNamePropertyCode;

    // ------------

    private IDatasetLoader currentTransaction;

    public ML2DatabaseUploader(Properties properties)
    {
        DataSource dataSource = ServiceProvider.getDataSourceProvider().getDataSource(properties);
        this.eicML2Database = new EICML2Database(dataSource);
        this.fiaML2Database = new FIAML2Database(dataSource);
        this.quantML2Database = new QuantML2Database(dataSource);
        this.mzXml2Database = new MzXml2Database(dataSource);
        this.currentTransaction = null;
        this.uniqueExperimentNamePropertyCode =
                DatasetMappingResolver.getUniqueExperimentNamePropertyCode(properties);
        this.uniqueSampleNamePropertyCode =
                DatasetMappingResolver.getUniqueSampleNamePropertyCode(properties);
    }

    /** uploads files with recognized extensions to the additional database */
    public void upload(File dataSet, DataSetInformation dataSetInformation)
            throws EnvironmentFailureException
    {
        uploadData(dataSet, dataSetInformation.tryToGetSample(), dataSetInformation
                .tryToGetExperiment(), dataSetInformation.getDataSetCode(), dataSetInformation);
    }

    /**
     * Uploads files with recognized extensions to the additional database.
     */
    public void upload(File dataSet, Sample sampleOrNull, Experiment experiment, String dataSetCode)
    {
        uploadData(dataSet, sampleOrNull, experiment, dataSetCode, null);
    }

    private void uploadData(File dataSet, Sample sample, Experiment experiment, String dataSetCode,
            DataSetInformation dataSetInformationOrNull)
    {
        try
        {
            if (currentTransaction != null)
            {
                throw new IllegalStateException(
                        "Current transaction has been neither commited nor rollbacked.");
            }
            this.currentTransaction =
                    tryGetDatasetUploader(dataSet, isMZXMLWithNoConversion(dataSet,
                            dataSetInformationOrNull));
            if (currentTransaction != null)
            {
                DMDataSetDTO openbisBacklink = createBacklink(dataSetCode, sample, experiment);
                currentTransaction.upload(dataSet, openbisBacklink);
            }
        } catch (SQLException e)
        {
            throw EnvironmentFailureException
                    .fromTemplate(
                            e,
                            "A database error occured while extracting additional information from '%s' file content for '%s' dataset.",
                            dataSet.getPath(), dataSetCode);
        }
    }

    private IDatasetLoader tryGetDatasetUploader(File dataSet, boolean isMZXML) throws SQLException
    {
        String extension = getExtension(dataSet);
        if (extension.equalsIgnoreCase(ConstantsYeastX.FIAML_EXT))
        {
            return fiaML2Database;
        } else if (extension.equalsIgnoreCase(ConstantsYeastX.EICML_EXT))
        {
            return eicML2Database;
        } else if (extension.equalsIgnoreCase(ConstantsYeastX.QUANTML_EXT))
        {
            return quantML2Database;
        } else if (isMZXML)
        {
            return mzXml2Database;
        }
        return null;
    }

    private static boolean isMZXMLWithNoConversion(File dataSet,
            DataSetInformation dataSetInformationOrNull)
    {
        boolean isMZXMLExtension =
                getExtension(dataSet).equalsIgnoreCase(ConstantsYeastX.MZXML_EXT);
        boolean dataSetUndefined = dataSetInformationOrNull == null;
        return (isMZXMLExtension && (dataSetUndefined || ((dataSetInformationOrNull instanceof DataSetInformationYeastX && ((DataSetInformationYeastX) dataSetInformationOrNull)
                .getConversion() == MLConversionType.NONE))));
    }

    public void commit()
    {
        try
        {
            if (currentTransaction != null)
            {
                currentTransaction.commit();
            }
        } finally
        {
            currentTransaction = null;
        }
    }

    public void rollback()
    {
        try
        {
            if (currentTransaction != null)
            {
                currentTransaction.rollback();
            }
        } finally
        {
            currentTransaction = null;
        }
    }

    private DMDataSetDTO createBacklink(String datasetPermId, Sample sample, Experiment experiment)
    {
        String sampleName = UNKNOWN_NAME;
        String sampPermIdOrNull = null;
        if (sample != null)
        {
            sampleName = findSampleName(sample.getProperties());
            sampPermIdOrNull = sample.getPermId();
        }
        if (experiment == null)
        {
            throw new EnvironmentFailureException(
                    "No information about the experiment connected to a dataset " + datasetPermId);
        }
        String experimentName = findExperimentName(experiment.getProperties());

        return new DMDataSetDTO(datasetPermId, sampPermIdOrNull, sampleName,
                experiment.getPermId(), experimentName);
    }

    private String findSampleName(List<? extends IEntityProperty> properties)
    {
        return findProperty(properties, uniqueSampleNamePropertyCode);
    }

    private String findExperimentName(List<? extends IEntityProperty> properties)
    {
        return findProperty(properties, uniqueExperimentNamePropertyCode);
    }

    private static String findProperty(List<? extends IEntityProperty> properties,
            String propertyTypeCode)
    {
        for (IEntityProperty property : properties)
        {
            final String currentPropertyCode = property.getPropertyType().getCode();
            if (currentPropertyCode.equalsIgnoreCase(propertyTypeCode))
            {
                return property.getValue();
            }
        }
        return UNKNOWN_NAME;
    }

    private static String getExtension(final File incomingDataSetPath)
    {
        return FilenameUtils.getExtension(incomingDataSetPath.getName()).toLowerCase();
    }
}
