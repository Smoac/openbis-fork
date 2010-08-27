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

package ch.systemsx.cisd.openbis.dss.etl.dataaccess.migration;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.dbmigration.java.IMigrationStep;
import ch.systemsx.cisd.etlserver.plugins.ChainedDataSetMigrationTask;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CanonicalFeatureVector;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CsvToCanonicalFeatureVector;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.FeatureVectorUploader;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CsvToCanonicalFeatureVector.CsvToCanonicalFeatureVectorConfiguration;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.utils.CsvFileReaderHelper;
import ch.systemsx.cisd.utils.CsvFileReaderHelper.DefaultCsvFileReaderConfiguration;
import ch.systemsx.cisd.utils.CsvFileReaderHelper.ICsvFileReaderConfiguration;

/**
 * Reads all feature vector files and reuploads them to the imaging database.
 * 
 * @author Tomasz Pylak
 */
public class MigrationStepFrom003To004 implements IMigrationStep
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, MigrationStepFrom003To004.class);

    // name of the columns which contain row and column info
    private static final String ROW_COLNAME = "row";

    private static final String COLUMN_COLNAME = "col";

    public MigrationStepFrom003To004()
    {
    }

    private static File getStoreRootDir()
    {
        ExtendedProperties properties = DssPropertyParametersUtil.loadServiceProperties();
        File storeRootDir = DssPropertyParametersUtil.getStoreRootDir(properties);
        return storeRootDir;
    }

    private static CsvToCanonicalFeatureVectorConfiguration createCsvConfig()
    {
        return new CsvToCanonicalFeatureVectorConfiguration(ROW_COLNAME, COLUMN_COLNAME);
    }

    private final static ParameterizedRowMapper<MigrationDatasetRef> DATASET_ROW_MAPPER =
            new ParameterizedRowMapper<MigrationDatasetRef>()
                {
                    public final MigrationDatasetRef mapRow(final ResultSet rs, final int rowNum)
                            throws SQLException
                    {
                        long id = rs.getLong("id");
                        String permId = rs.getString("perm_id");
                        int plateWidth = rs.getInt("plate_width");
                        int plateHeight = rs.getInt("plate_height");
                        return new MigrationDatasetRef(id, permId, plateWidth, plateHeight);

                    }
                };

    private static class MigrationDatasetRef extends AbstractHashable
    {
        final private long id;

        final private String permId;

        final private int plateWidth;

        final private int plateHeight;

        public MigrationDatasetRef(long id, String permId, int plateWidth, int plateHeight)
        {
            this.id = id;
            this.permId = permId;
            this.plateWidth = plateWidth;
            this.plateHeight = plateHeight;
        }

        public long getId()
        {
            return id;
        }

        public String getPermId()
        {
            return permId;
        }

        public int getPlateWidth()
        {
            return plateWidth;
        }

        public int getPlateHeight()
        {
            return plateHeight;
        }
    }

    public void performPostMigration(SimpleJdbcTemplate jdbc, DataSource dataSource)
            throws DataAccessException
    {
        IImagingQueryDAO dao = QueryTool.getQuery(dataSource, IImagingQueryDAO.class);
        File storeRootDir = getStoreRootDir();
        String dbUUID = tryGetDatabaseInstanceUUID(storeRootDir);
        if (dbUUID == null)
        {
            operationLog.warn("Store is empty - there is nothing to migrate.");
            return;
        }
        List<MigrationDatasetRef> datasets = fetchImagingDatasets(jdbc);
        Map<MigrationDatasetRef, DatasetFileLines> fileMap =
                createFileMap(datasets, storeRootDir, dbUUID);
        boolean ok = migrateDatasets(fileMap, jdbc, dao);
        dao.commit();
        dao.close();
        if (ok == false)
        {
            operationLog.warn("There were some error during feature vector migration!");
        }
    }

    private boolean migrateDatasets(Map<MigrationDatasetRef, DatasetFileLines> fileMap,
            SimpleJdbcTemplate jdbc, IImagingQueryDAO dao)
    {
        boolean wholeMigrationOk = true;
        for (Entry<MigrationDatasetRef, DatasetFileLines> entry : fileMap.entrySet())
        {
            MigrationDatasetRef datasetRef = entry.getKey();
            String permId = datasetRef.getPermId();
            DatasetFileLines featureVectorLines = entry.getValue();
            try
            {
                operationLog.info("Migrating dataset: " + permId);
                migrateDataset(jdbc, dao, datasetRef, featureVectorLines);
            } catch (Exception ex)
            {
                operationLog.error("Cannot migrate dataset " + permId + ": " + ex.getMessage());
                if (ex instanceof IllegalArgumentException == false)
                {
                    ex.printStackTrace();
                }
                wholeMigrationOk = false;
            }
        }
        return wholeMigrationOk;
    }

    private void migrateDataset(SimpleJdbcTemplate jdbc, IImagingQueryDAO dao,
            MigrationDatasetRef datasetRef, DatasetFileLines featureVectorLines)
    {
        long datasetId = datasetRef.getId();
        List<CanonicalFeatureVector> fvecs = extractFeatureVectors(featureVectorLines, datasetRef);
        int deleted = deleteFeatureVectors(datasetId, jdbc);
        if (deleted != fvecs.size())
        {
            operationLog.error(String.format(
                    "Dataset techId(%d) had %d features, but now it has %d.", datasetId, deleted,
                    fvecs.size()));
        }
        uploadFeatureVectors(datasetId, fvecs, dao);
    }

    private void uploadFeatureVectors(long datasetId, List<CanonicalFeatureVector> fvecs,
            IImagingQueryDAO dao)
    {
        FeatureVectorUploader.uploadFeatureVectors(dao, fvecs, datasetId);
    }

    private List<CanonicalFeatureVector> extractFeatureVectors(DatasetFileLines featureVectorLines,
            MigrationDatasetRef datasetRef)

    {
        CsvToCanonicalFeatureVectorConfiguration convertorConfig = createCsvConfig();
        return new CsvToCanonicalFeatureVector(featureVectorLines, convertorConfig, datasetRef
                .getPlateHeight(), datasetRef.getPlateWidth()).convert();
    }

    private static DatasetFileLines getDatasetFileLines(File file, final char separator)
            throws IOException
    {
        ICsvFileReaderConfiguration configuration = new DefaultCsvFileReaderConfiguration()
            {
                @Override
                public char getColumnDelimiter()
                {
                    return separator;
                }
            };
        return CsvFileReaderHelper.getDatasetFileLines(file, configuration);
    }

    private int deleteFeatureVectors(long datasetId, SimpleJdbcTemplate jdbc)
    {
        return jdbc.update("delete from feature_defs defs where defs.ds_id = ?", datasetId);
    }

    private List<MigrationDatasetRef> fetchImagingDatasets(SimpleJdbcTemplate simpleJdbcTemplate)
    {
        return simpleJdbcTemplate.query(
                "select distinct d.id, d.perm_id, c.spots_width plate_width, c.spots_height plate_height "
                        + "   from feature_defs defs, data_sets d, containers c "
                        + "   where d.id = defs.ds_id and c.id = d.cont_id;", DATASET_ROW_MAPPER);
    }

    private String tryGetDatabaseInstanceUUID(File storeRootDir)
    {
        File dbInstanceDir = ChainedDataSetMigrationTask.tryGetDatabaseInstanceDir(storeRootDir);
        if (dbInstanceDir == null)
        {
            return null;
        } else
        {
            return dbInstanceDir.getName();
        }
    }

    private static Map<MigrationDatasetRef, DatasetFileLines/* lines with feature vectors */> createFileMap(
            List<MigrationDatasetRef> datasets, File storeRootDir, String dbUUID)
    {
        Map<MigrationDatasetRef, DatasetFileLines> fileMap =
                new HashMap<MigrationDatasetRef, DatasetFileLines>();
        for (MigrationDatasetRef dataset : datasets)
        {
            String permId = dataset.getPermId();
            File datasetDir =
                    DatasetLocationUtil.getDatasetLocationPath(storeRootDir, permId, dbUUID);
            DatasetFileLines featureVectorLines = tryFindFeatureVectorsFile(datasetDir);
            if (featureVectorLines != null)
            {
                fileMap.put(dataset, featureVectorLines);
            }
        }
        return fileMap;
    }

    private static DatasetFileLines tryFindFeatureVectorsFile(File datasetDir)
    {
        File origDir = new File(datasetDir, ScreeningConstants.ORIGINAL_DATA_DIR);
        File[] datasetFiles = origDir.listFiles();
        if (datasetFiles == null || datasetFiles.length == 0)
        {
            operationLog.warn("Empty dataset dir: " + datasetDir);
            return null;
        }

        for (File datasetFile : datasetFiles)
        {
            DatasetFileLines fileLines = tryReadFeatureVectors(datasetFile, ',');
            if (fileLines == null || fileLines.getHeaderLabels().length <= 2)
            {
                fileLines = tryReadFeatureVectors(datasetFile, ';');
            }
            if (fileLines != null && fileLines.getHeaderLabels().length > 2)
            {
                return fileLines;
            }
        }
        throw new EnvironmentFailureException(
                "Cannot find the file with feature vectors for the dataset. "
                        + "Delete this dataset from openBIS and restart the server to perform migration again. Dataset: "
                        + datasetDir.getName() + ". Directory: " + datasetDir);
    }

    private static DatasetFileLines tryReadFeatureVectors(File datasetFile, char separator)
    {
        try
        {
            return getDatasetFileLines(datasetFile, separator);
        } catch (Exception ex)
        {
            operationLog.warn("Cannot read the file or file has the wrong format: " + datasetFile
                    + ": " + ex.getMessage());
            return null;
        }
    }

    public void performPreMigration(SimpleJdbcTemplate simpleJdbcTemplate, DataSource dataSource)
            throws DataAccessException
    {
        // do nothing
    }

}
