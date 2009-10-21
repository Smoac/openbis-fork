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

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.etlserver.IDataSetUploader;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.yeastx.db.DBUtils;
import ch.systemsx.cisd.yeastx.db.DMDataSetDTO;
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

    private static final String DATABASE_PROPERTIES_PREFIX = "database.";

    private final EICML2Database eicML2Database;

    private final FIAML2Database fiaML2Database;

    private final QuantML2Database quantML2Database;

    private final MzXml2Database mzXml2Database;

    private final String uniqueSampleNamePropertyCode;

    private final String uniqueExperimentNamePropertyCode;

    public ML2DatabaseUploader(Properties properties)
    {
        final Properties dbProps =
                ExtendedProperties.getSubset(properties, DATABASE_PROPERTIES_PREFIX, true);
        final DatabaseConfigurationContext dbContext =
                (dbProps.isEmpty() ? DBUtils.createDefaultDBContext() : BeanUtils.createBean(
                        DatabaseConfigurationContext.class, dbProps));
        DBUtils.init(dbContext);
        this.eicML2Database = new EICML2Database(dbContext.getDataSource());
        this.fiaML2Database = new FIAML2Database(dbContext.getDataSource());
        this.quantML2Database = new QuantML2Database(dbContext.getDataSource());
        this.mzXml2Database = new MzXml2Database(dbContext.getDataSource());
        this.uniqueExperimentNamePropertyCode =
                DatasetMappingResolver.getUniqueExperimentNamePropertyCode(properties);
        this.uniqueSampleNamePropertyCode =
                DatasetMappingResolver.getUniqueSampleNamePropertyCode(properties);
    }

    /** uploads files with recognized extensions to the additional database */
    public void upload(File dataSet, DataSetInformation dataSetInformation)
            throws EnvironmentFailureException
    {
        try
        {
            uoloadFile(dataSet, dataSetInformation);
        } catch (SQLException e)
        {
            throw EnvironmentFailureException
                    .fromTemplate(
                            e,
                            "A database error occured while extracting additional information from '%s' file content for '%s' dataset.",
                            dataSet.getPath(), dataSetInformation.getDataSetCode());
        }
    }

    private void uoloadFile(File dataSet, DataSetInformation dataSetInformation)
            throws SQLException
    {
        DMDataSetDTO openbisBacklink = createBacklink(dataSetInformation);
        String extension = getExtension(dataSet);
        if (extension.equalsIgnoreCase(ConstantsYeastX.FIAML_EXT))
        {
            fiaML2Database.uploadFiaMLFile(dataSet, openbisBacklink);
        } else if (extension.equalsIgnoreCase(ConstantsYeastX.EICML_EXT))
        {
            eicML2Database.uploadEicMLFile(dataSet, openbisBacklink);
        } else if (extension.equalsIgnoreCase(ConstantsYeastX.QUANTML_EXT))
        {
            quantML2Database.uploadQuantMLFile(dataSet, openbisBacklink);
        } else if (extension.equalsIgnoreCase(ConstantsYeastX.MZXML_EXT))
        {
            DataSetInformationYeastX info = (DataSetInformationYeastX) dataSetInformation;
            if (info.getConversion() == MLConversionType.NONE)
            {
                mzXml2Database.uploadFile(dataSet, openbisBacklink);
            }
        } else
        {
            // do nothing
        }
    }

    private DMDataSetDTO createBacklink(DataSetInformation dataSetInformation)
    {
        String datasetPermId = dataSetInformation.getDataSetCode();
        Sample sample = dataSetInformation.tryToGetSample();
        String sampleName = UNKNOWN_NAME;
        String sampPermIdOrNull = null;
        if (sample != null)
        {
            sampleName = findSampleName(sample.getProperties());
            sampPermIdOrNull = sample.getPermId();
        }
        Experiment experiment = dataSetInformation.tryToGetExperiment();
        if (experiment == null)
        {
            throw new EnvironmentFailureException(
                    "No information about the experiment connected to a dataset "
                            + dataSetInformation);
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
