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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.server;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.Constants;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.DataSetProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinByExperiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSequence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSummary;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.SampleWithPropertiesAndAbundance;

/**
 * Proxy which caches the results of some methods.
 *
 * @author Franz-Josef Elmer
 */
class ServerWithCache implements IPhosphoNetXServer
{
    private static final String CACHE_VERSION = "1"; // Sprint S97
    
    private static final class Key implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private final Serializable[] objects;
        
        Key(Serializable... objects)
        {
            this.objects = objects;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj instanceof Key == false)
            {
                return false;
            }
            Key key = (Key) obj;
            if (key.objects.length != objects.length)
            {
                return false;
            }
            for (int i = 0, n = objects.length; i < n; i++)
            {
                Serializable object = objects[i];
                Serializable keyObject = key.objects[i];
                if (object == null ? object != keyObject : object.equals(keyObject) == false)
                {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public int hashCode()
        {
            int sum = 0;
            for (Object object : objects)
            {
                sum = 37 * sum + (object == null ? 0 : object.hashCode());
            }
            return sum;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            for (Object object : objects)
            {
                if (builder.length() > 0)
                {
                    builder.append(", ");
                }
                builder.append(object);
            }
            return "[" + builder.toString() + "]";
        }
    }
    
    private static interface ICacheManager
    {

        public abstract Object tryToGetData(Key key);

        public abstract void storeData(Key key, Object object);

    }    
    
    private static final class CacheManager implements ICacheManager
    {
        private static final String CACHE_FOLDER_DEFAULT_VALUE = "cache";
        private static final String CHACHE_FOLDER_KEY = "chache-folder";
        private final static String CACHE_VERSION_FILE_NAME = ".cache-version";
        private final static String FILE_NAME_FORMAT = "{0,date,yyyyMMddHHmmssSSS}-{1}";
        private final static String KEY_FILE_TYPE = ".key";
        private final static String DATA_FILE_TYPE = ".data";
        
        private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
                CacheManager.class);
        
        private final ITimeProvider timeProvider;
        private final Map<Key, String> keyToFileNameMap;
        private final File cacheFolder;
        
        private int counter;
        
        CacheManager(WebClientConfiguration configuration, ITimeProvider timeProvider, String cacheVersion)
        {
            this.timeProvider = timeProvider;
            keyToFileNameMap = new HashMap<ServerWithCache.Key, String>();
            cacheFolder = new File(getProperty(configuration, CHACHE_FOLDER_KEY, CACHE_FOLDER_DEFAULT_VALUE));
            String actualCacheVersion = tryToGetActualCacheVersion();
            if (cacheVersion.equals(actualCacheVersion))
            {
                File[] keyFiles = cacheFolder.listFiles(new FilenameFilter()
                {
                    public boolean accept(File dir, String name)
                    {
                        return name.endsWith(KEY_FILE_TYPE);
                    }
                });
                for (File keyFile : keyFiles)
                {
                    String name = keyFile.getName();
                    String fileName = name.substring(0, name.length() - KEY_FILE_TYPE.length());
                    Key key = getKey(keyFile);
                    keyToFileNameMap.put(key, fileName);
                }
            } else
            {
                FileUtilities.deleteRecursively(cacheFolder);
                cacheFolder.mkdirs();
                FileUtilities.writeToFile(new File(cacheFolder, CACHE_VERSION_FILE_NAME), cacheVersion);
            }
        }

        private String tryToGetActualCacheVersion()
        {
            File file = new File(cacheFolder, CACHE_VERSION_FILE_NAME);
            if (file.exists() == false)
            {
                return null;
            }
            return FileUtilities.loadToString(file).trim();
        }

        private Key getKey(File keyFile)
        {
            Key key;
            try
            {
                key = (Key) SerializationUtils.deserialize(FileUtils.readFileToByteArray(keyFile));
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
            return key;
        }

        private String getProperty(WebClientConfiguration configuration, String key,
                String defaultValue)
        {
            String value = configuration.getPropertyOrNull(Constants.TECHNOLOGY_NAME, key);
            if (value == null)
            {
                value = defaultValue;
            }
            return value;
        }
        
        public Object tryToGetData(Key key)
        {
            synchronized (keyToFileNameMap)
            {
                String fileName = keyToFileNameMap.get(key);
                if (fileName == null)
                {
                    return null;
                }
                File dataFile = new File(cacheFolder, fileName + DATA_FILE_TYPE);
                try
                {
                    long t0 = timeProvider.getTimeInMilliseconds();
                    byte[] bytes = FileUtils.readFileToByteArray(dataFile);
                    Object object = SerializationUtils.deserialize(bytes);
                    long duration = timeProvider.getTimeInMilliseconds() - t0;
                    if (operationLog.isInfoEnabled())
                    {
                        operationLog.info(duration + " msec for retrieving cached data ("
                                + bytes.length + " bytes) for key " + key + " from file "
                                + fileName);
                    }
                    return object;
                } catch (IOException ex)
                {
                    operationLog.warn("Couldn't read data file '" + fileName + "' key " + key
                            + ": " + ex.toString());
                    return null;
                }
            }
        }

        public void storeData(Key key, Object object)
        {
            synchronized (keyToFileNameMap)
            {
                MessageFormat messageFormat = new MessageFormat(FILE_NAME_FORMAT);
                Date timeStamp = new Date(timeProvider.getTimeInMilliseconds());
                String fileName = messageFormat.format(new Object[]
                    { timeStamp, counter++ });
                try
                {
                    long t0 = timeProvider.getTimeInMilliseconds();
                    save(fileName + KEY_FILE_TYPE, key);
                    save(fileName + DATA_FILE_TYPE, (Serializable) object);
                    keyToFileNameMap.put(key, fileName);
                    long duration = timeProvider.getTimeInMilliseconds() - t0;
                    if (operationLog.isInfoEnabled())
                    {
                        operationLog.info(duration + " msec for storing data for key " + key
                                + " in file " + fileName);
                    }
                } catch (Exception ex)
                {
                    // ignored
                }
            }
            
        }

        private void save(String fileName, Serializable object)
        {
            File file = new File(cacheFolder, fileName);
            try
            {
                byte[] bytes = SerializationUtils.serialize(object);
                FileUtils.writeByteArrayToFile(file, bytes);
            } catch (Throwable t)
            {
                file.delete();
                operationLog.warn("Caching error: Couldn't save '" + file + "': " + t, t);
                throw CheckedExceptionTunnel.wrapIfNecessary(t);
            }
        }
    }
    
    private final IPhosphoNetXServer server;
    private final ICacheManager cacheManager;

    ServerWithCache(IPhosphoNetXServer server, WebClientConfiguration webClientConfiguration)
    {
        this(server, webClientConfiguration, SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }
    
    ServerWithCache(IPhosphoNetXServer server, WebClientConfiguration webClientConfiguration,
            ITimeProvider timeProvider)
    {
        this.server = server;
        cacheManager = new CacheManager(webClientConfiguration, timeProvider, CACHE_VERSION);
    }

    public IAuthSession getAuthSession(String sessionToken) throws UserFailureException
    {
        return server.getAuthSession(sessionToken);
    }

    public int getVersion()
    {
        return server.getVersion();
    }

    public boolean isArchivingConfigured(String sessionToken)
    {
        return server.isArchivingConfigured(sessionToken);
    }

    public SessionContextDTO tryToAuthenticate(String user, String password)
    {
        return server.tryToAuthenticate(user, password);
    }

    public Vocabulary getTreatmentTypeVocabulary(String sessionToken) throws UserFailureException
    {
        return server.getTreatmentTypeVocabulary(sessionToken);
    }

    public List<AbundanceColumnDefinition> getAbundanceColumnDefinitionsForProteinByExperiment(
            String sessionToken, TechId experimentID, String treatmentTypeOrNull)
            throws UserFailureException
    {
        Key key = new Key(experimentID, treatmentTypeOrNull);
        @SuppressWarnings("unchecked")
        List<AbundanceColumnDefinition> abundanceColumnDefinitions =
                (List<AbundanceColumnDefinition>) cacheManager.tryToGetData(key);
        if (abundanceColumnDefinitions == null)
        {
            abundanceColumnDefinitions =
                    server.getAbundanceColumnDefinitionsForProteinByExperiment(sessionToken,
                            experimentID, treatmentTypeOrNull);
            cacheManager.storeData(key, abundanceColumnDefinitions);
        }
        return abundanceColumnDefinitions;
    }

    public SessionContextDTO tryGetSession(String sessionToken)
    {
        return server.tryGetSession(sessionToken);
    }

    public void setBaseIndexURL(String sessionToken, String baseIndexURL)
    {
        server.setBaseIndexURL(sessionToken, baseIndexURL);
    }

    public List<ProteinInfo> listProteinsByExperiment(String sessionToken, TechId experimentId,
            double falseDiscoveryRate, AggregateFunction function, String treatmentTypeCode,
            boolean aggregateOnOriginal) throws UserFailureException
    {
        Key key =
                new Key(experimentId, falseDiscoveryRate, function, treatmentTypeCode,
                        aggregateOnOriginal);
        @SuppressWarnings("unchecked")
        List<ProteinInfo> proteinInfos = (List<ProteinInfo>) cacheManager.tryToGetData(key);
        if (proteinInfos == null)
        {
            proteinInfos =
                    server.listProteinsByExperiment(sessionToken, experimentId, falseDiscoveryRate,
                            function, treatmentTypeCode, aggregateOnOriginal);
            cacheManager.storeData(key, proteinInfos);
        }
        return proteinInfos;
    }

    public DisplaySettings getDefaultDisplaySettings(String sessionToken)
    {
        return server.getDefaultDisplaySettings(sessionToken);
    }

    public void saveDisplaySettings(String sessionToken, DisplaySettings displaySettings)
    {
        server.saveDisplaySettings(sessionToken, displaySettings);
    }

    public List<GridCustomColumn> listGridCustomColumns(String sessionToken, String gridDisplayId)
    {
        return server.listGridCustomColumns(sessionToken, gridDisplayId);
    }

    public List<ProteinSummary> listProteinSummariesByExperiment(String sessionToken,
            TechId experimentId) throws UserFailureException
    {
        return server.listProteinSummariesByExperiment(sessionToken, experimentId);
    }

    public void changeUserHomeSpace(String sessionToken, TechId spaceIdOrNull)
    {
        server.changeUserHomeSpace(sessionToken, spaceIdOrNull);
    }

    public void logout(String sessionToken) throws UserFailureException
    {
        server.logout(sessionToken);
    }

    public ProteinByExperiment getProteinByExperiment(String sessionToken, TechId experimentId,
            TechId proteinReferenceID) throws UserFailureException
    {
        return server.getProteinByExperiment(sessionToken, experimentId, proteinReferenceID);
    }

    public void setSessionUser(String sessionToken, String userID)
    {
        server.setSessionUser(sessionToken, userID);
    }

    public List<ProteinSequence> listProteinSequencesByProteinReference(String sessionToken,
            TechId proteinReferenceID) throws UserFailureException
    {
        return server.listProteinSequencesByProteinReference(sessionToken, proteinReferenceID);
    }

    public List<DataSetProtein> listProteinsByExperimentAndReference(String sessionToken,
            TechId experimentId, TechId proteinReferenceID) throws UserFailureException
    {
        return server.listProteinsByExperimentAndReference(sessionToken, experimentId,
                proteinReferenceID);
    }

    public List<SampleWithPropertiesAndAbundance> listSamplesWithAbundanceByProtein(
            String sessionToken, TechId experimentID, TechId proteinReferenceID)
            throws UserFailureException
    {
        return server.listSamplesWithAbundanceByProtein(sessionToken, experimentID,
                proteinReferenceID);
    }
}
