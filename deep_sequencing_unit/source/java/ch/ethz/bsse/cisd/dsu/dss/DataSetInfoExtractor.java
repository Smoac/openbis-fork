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

package ch.ethz.bsse.cisd.dsu.dss;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.DefaultDataSetInfoExtractor;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * Extension of {@link DefaultDataSetInfoExtractor} for DSU which tries to get version information
 * from the content and store it into the data set property <code>ILLUMINA_GA_OUTPUT</code>. The
 * path to the configuration file inside a flow cell folder is specified by the property
 * <code>path-to-config-file</code>. Its default value is <code>Data/Intensities/config.xml</code>.
 * <p>
 * If version information couldn't be fetched a warning is logged.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetInfoExtractor implements IDataSetInfoExtractor
{
    static final String VERSION_KEY = "ILLUMINA_PIPELINE_VERSION";
    static final String PATH_TO_CONFIG_FILE_KEY = "path-to-config-file";
    static final String DEFAULT_PATH_TO_CONFIG_FILE = "Data/Intensities/config.xml";
    
    private final static Logger operationLog =
        LogFactory.getLogger(LogCategory.OPERATION, DataSetInfoExtractor.class);

    private final IDataSetInfoExtractor dataSetInfoExtractor;
    private final String pathToConfigFile;
    private final Pattern pattern;

    public DataSetInfoExtractor(Properties properties)
    {
        dataSetInfoExtractor = new DefaultDataSetInfoExtractor(properties);
        pathToConfigFile = properties.getProperty(PATH_TO_CONFIG_FILE_KEY, DEFAULT_PATH_TO_CONFIG_FILE);
        pattern = Pattern.compile(".*<Software.*Version=('|\")(.*)('|\").*");
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        DataSetInformation info =
                dataSetInfoExtractor.getDataSetInformation(incomingDataSetPath, openbisService);
        File configFile = new File(incomingDataSetPath, pathToConfigFile);
        if (configFile.isFile())
        {
            String version = tryToExtractVersion(configFile);
            if (version != null)
            {
                NewProperty property = new NewProperty();
                property.setPropertyCode(VERSION_KEY);
                property.setValue(version);
                info.setDataSetProperties(Arrays.asList(property));
            } else
            {
                operationLog.warn("No version found in config file '" + pathToConfigFile + "'.");
            }
        } else
        {
            operationLog.warn("Config file '" + pathToConfigFile
                    + "' does not exists or is a directory.");
        }
        return info;
    }
    
    private String tryToExtractVersion(File configFile)
    {
        List<String> configFileContent = FileUtilities.loadToStringList(configFile);
        for (String line : configFileContent)
        {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches())
            {
                return matcher.group(2);
            }
        }
        return null;
    }

}
