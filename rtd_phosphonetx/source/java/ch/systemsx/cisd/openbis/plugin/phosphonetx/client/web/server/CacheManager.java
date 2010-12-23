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
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.Constants;

final class CacheManager implements ICacheManager
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
        keyToFileNameMap = new HashMap<Key, String>();
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
                            + dataFile);
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
                if (object instanceof Serializable == false)
                {
                    operationLog.warn("Can not store unserializable data for key " + key + ": "
                            + object);
                } else
                {
                    long t0 = timeProvider.getTimeInMilliseconds();
                    save(fileName + KEY_FILE_TYPE, key);
                    File dataFile = save(fileName + DATA_FILE_TYPE, (Serializable) object);
                    keyToFileNameMap.put(key, fileName);
                    long duration = timeProvider.getTimeInMilliseconds() - t0;
                    if (operationLog.isInfoEnabled())
                    {
                        operationLog.info(duration + " msec for storing data for key " + key
                                + " in file " + dataFile);
                    }
                }
            } catch (Exception ex)
            {
                // ignored
            }
        }
        
    }

    private File save(String fileName, Serializable object)
    {
        File file = new File(cacheFolder, fileName);
        try
        {
            byte[] bytes = SerializationUtils.serialize(object);
            FileUtils.writeByteArrayToFile(file, bytes);
            return file;
        } catch (Throwable t)
        {
            file.delete();
            operationLog.warn("Caching error: Couldn't save '" + file + "': " + t, t);
            throw CheckedExceptionTunnel.wrapIfNecessary(t);
        }
    }
}