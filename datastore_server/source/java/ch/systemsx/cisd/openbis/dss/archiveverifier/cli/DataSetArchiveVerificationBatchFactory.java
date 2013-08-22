/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.archiveverifier.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.DataSetArchiveVerificationBatch;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.DataSetArchiveVerifier;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.FailedResult;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.IArchiveFileVerifier;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.IDataSetArchiveVerificationBatch;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.IResult;
import ch.systemsx.cisd.openbis.dss.archiveverifier.filesystem.FileSystemArchiveFileRepository;
import ch.systemsx.cisd.openbis.dss.archiveverifier.filesystem.FlatFileLocator;
import ch.systemsx.cisd.openbis.dss.archiveverifier.filesystem.IFileLocator;
import ch.systemsx.cisd.openbis.dss.archiveverifier.filesystem.ShardingFileLocator;
import ch.systemsx.cisd.openbis.dss.archiveverifier.pathinfo.JdbcPathInfoRepository;
import ch.systemsx.cisd.openbis.dss.archiveverifier.verifier.CompositeVerifier;
import ch.systemsx.cisd.openbis.dss.archiveverifier.verifier.CrcEnabled;
import ch.systemsx.cisd.openbis.dss.archiveverifier.verifier.IArchiveFileMetaDataRepository;
import ch.systemsx.cisd.openbis.dss.archiveverifier.verifier.ZipFileHeaderVerifier;
import ch.systemsx.cisd.openbis.dss.archiveverifier.verifier.ZipFileIntegrityVerifier;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;

/**
 * Creates a DataSetArchiveVerificationBatch based on command line arguments. First argument should point to service.properties of the local DSS. Rest
 * of the arguments are the codes of datasets whose archive files should be verified.
 * 
 * @author anttil
 */
public class DataSetArchiveVerificationBatchFactory
{

    private static final String DEFAULT_ARCHIVE_FOLDER = "archiver.default-archive-folder";

    private static final String MAPPING_FILE = "archiver.mapping-file";

    private static final String SHARDING = "archiver.with-sharding";

    private static final String DATABASE_NAME = "path-info-db.basicDatabaseName";

    private static final String DATABASE_KIND = "path-info-db.databaseKind";

    private static final String DATABASE_USER = "path-info-db.owner";

    private static final String DATABASE_PASSWORD = "path-info-db.password";

    private static final String PATHINFO_CHECKSUMS_COMPUTED = "post-registration.pathinfo-feeding.compute-checksum";

    private final String[] args;

    /**
     * @param args
     */
    public DataSetArchiveVerificationBatchFactory(String[] args)
    {
        this.args = args;
    }

    public IDataSetArchiveVerificationBatch build()
    {
        if (args.length < 2)
        {
            return error("Usage: java -jar ..adsfadf");
        }

        String servicePropertiesPath = args[0];

        File serviceProperties = new File(servicePropertiesPath);
        if (serviceProperties.exists() == false)
        {
            return error("No service.properties given");
        }

        Properties properties = DssPropertyParametersUtil.loadProperties(serviceProperties.getAbsolutePath());
        String defaultArchiveDirectoryPath = properties.getProperty(DEFAULT_ARCHIVE_FOLDER);
        if (defaultArchiveDirectoryPath == null)
        {
            return error("Given service.properties files does not contain mandatory property " + DEFAULT_ARCHIVE_FOLDER);
        }

        IArchiveFileMetaDataRepository pathInfoRepository = null;

        String databaseName = properties.getProperty(DATABASE_NAME);
        String databaseKind = properties.getProperty(DATABASE_KIND);

        if (databaseName != null && databaseKind != null)
        {
            String user = properties.getProperty(DATABASE_USER);
            String password = properties.getProperty(DATABASE_PASSWORD);

            if (user == null || user.isEmpty())
            {
                user = "postgres";
            }

            if (password == null)
            {
                password = "";
            }

            String url = "jdbc:postgresql://localhost/" + databaseName + "_" + databaseKind;

            try
            {
                Connection connection = DriverManager.getConnection(url, user, password);
                pathInfoRepository = new JdbcPathInfoRepository(connection);
            } catch (SQLException ex)
            {
                return error("Could not connect to pathinfo db at " + url + " with user name '" + user + "', password '" + password + "'");
            }
        }

        File defaultArchiveDirectory = new File(defaultArchiveDirectoryPath);
        if (defaultArchiveDirectory.exists() == false)
        {
            return error("Default archive directory " + defaultArchiveDirectoryPath + " does not exist");
        }

        String mappingFilePath = properties.getProperty(MAPPING_FILE);
        List<File> mappedArchiveDirectories = new ArrayList<File>();
        if (mappingFilePath != null)
        {
            File mappingFile = new File(mappingFilePath);
            if (mappingFile.exists() == false)
            {
                return error("Mapping file " + mappingFilePath + " defined in " + servicePropertiesPath + " does not exist");
            }

            try
            {
                BufferedReader br = new BufferedReader(new FileReader(mappingFile));
                String line;
                int lineNumber = 0;
                while ((line = br.readLine()) != null)
                {
                    lineNumber++;
                    if (lineNumber == 1)
                    {
                        continue;
                    }
                    String archiveDirectoryPath = line.substring(line.lastIndexOf("\t") + 1);
                    File archiveDirectory = new File(archiveDirectoryPath);
                    if (archiveDirectory.exists() == false)
                    {
                        return error("Archive directory " + archiveDirectoryPath + " specified in mapping file " + mappingFilePath
                                + " does not exist");
                    }
                    mappedArchiveDirectories.add(archiveDirectory);
                }
                br.close();
            } catch (IOException e)
            {
                return error("I/O error: " + e.getMessage());
            }
        }

        List<File> archiveDirectories = new ArrayList<File>();
        archiveDirectories.add(defaultArchiveDirectory);
        archiveDirectories.addAll(mappedArchiveDirectories);

        boolean sharding = "true".equalsIgnoreCase(properties.getProperty(SHARDING));
        IFileLocator fileLocator;
        if (sharding)
        {
            fileLocator = new ShardingFileLocator();
        } else
        {
            fileLocator = new FlatFileLocator();
        }

        FileSystemArchiveFileRepository fileFinder = new FileSystemArchiveFileRepository(archiveDirectories, fileLocator);

        List<IArchiveFileVerifier> verifiers = new ArrayList<IArchiveFileVerifier>();
        verifiers.add(new ZipFileIntegrityVerifier());

        if (pathInfoRepository != null)
        {
            verifiers.add(new ZipFileHeaderVerifier(pathInfoRepository,
                    "true".equalsIgnoreCase(properties.getProperty(PATHINFO_CHECKSUMS_COMPUTED)) ? CrcEnabled.TRUE : CrcEnabled.FALSE));
        }

        DataSetArchiveVerifier verifier = new DataSetArchiveVerifier(
                fileFinder,
                new CompositeVerifier(verifiers));

        return new DataSetArchiveVerificationBatch(verifier, Arrays.copyOfRange(args, 1, args.length));
    }

    private IDataSetArchiveVerificationBatch error(final String error)
    {
        return new IDataSetArchiveVerificationBatch()
            {
                @Override
                public SortedMap<String, IResult> run()
                {
                    SortedMap<String, IResult> result = new TreeMap<String, IResult>();
                    result.put("Failed to start", new FailedResult(error));
                    return result;
                }
            };
    }
}
