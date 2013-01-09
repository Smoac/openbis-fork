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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;

/**
 * Cache for files remotely retrieved from a DSS.
 * 
 * @author Franz-Josef Elmer
 */
public class ContentCache
{
    static final String CHACHED_FOLDER = "cached";

    static final String DOWNLOADING_FOLDER = "downloading";
    
    private final IDssServiceRpcGeneric remoteDss;

    private final OpenBISSessionHolder sessionHolder;

    private final File cachedFiles;

    private final File downloadingFolder;

    private Map<String, Lock> locks;

    public ContentCache(IDssServiceRpcGeneric remoteDss, OpenBISSessionHolder sessionHolder,
            File cacheWorkSpace, IFileOperations fileOperations)
    {
        this.remoteDss = remoteDss;
        this.sessionHolder = sessionHolder;
        cachedFiles = new File(cacheWorkSpace, CHACHED_FOLDER);
        creatFolder(cachedFiles);
        downloadingFolder = new File(cacheWorkSpace, DOWNLOADING_FOLDER);
        fileOperations.removeRecursivelyQueueing(downloadingFolder);
        creatFolder(downloadingFolder);
        locks = new HashMap<String, Lock>();
    }

    public void unlockFilesFor(String dataSetCode)
    {
    }

    IDssServiceRpcGeneric getRemoteDss()
    {
        return remoteDss;
    }

    File getFile(String dataSetCode, DataSetPathInfo path)
    {
        String pathInCache = dataSetCode + "/" + path.getRelativePath();
        lock(pathInCache);
        try
        {
            File file = new File(cachedFiles, pathInCache);
            if (file.exists() == false)
            {
                downloadFile(dataSetCode, path, pathInCache);
            }
            return file;
        } finally
        {
            unlock(pathInCache);
        }
    }

    private void downloadFile(String dataSetCode, DataSetPathInfo path, String pathInCache)
    {
        InputStream input = null;
        try
        {
            String url =
                    remoteDss.getDownloadUrlForFileForDataSet(sessionHolder.getSessionToken(),
                            dataSetCode, path.getRelativePath());
            input = createURL(url).openStream();
            File downloadedFile = createFileFromInputStream(pathInCache, input);
            File file = new File(cachedFiles, pathInCache);
            creatFolder(file.getParentFile());
            downloadedFile.renameTo(file);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(input);
        }
    }
    
    private URL createURL(String url)
    {
        try
        {
            if (url.toLowerCase().startsWith("https"))
            {
                return new URL(null, url, new sun.net.www.protocol.https.Handler());
            }
            return new URL(url);
        } catch (MalformedURLException ex)
        {
            throw new ConfigurationFailureException("Malformed URL: " + url);
        }
    }

    private File createFileFromInputStream(String filePath, InputStream inputStream)
    {
        final String subDir = FilenameUtils.getFullPath(filePath);
        final String filename = FilenameUtils.getName(filePath);
        final File dir = new File(downloadingFolder, subDir);
        dir.mkdirs();
        final File file = new File(dir, filename);
        OutputStream ostream = null;
        try
        {
            ostream = new FileOutputStream(file);
            IOUtils.copyLarge(inputStream, ostream);
            return file;
        } catch (IOException ex)
        {
            file.delete();
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(ostream);
        }
    }
    
    private void creatFolder(File folder)
    {
        if (folder.exists() == false)
        {
            boolean result = folder.mkdirs();
            if (result == false)
            {
                throw new EnvironmentFailureException("Couldn't create folder: " + folder);
            }
        }
    }

    private void lock(String pathInCache)
    {
        getLock(pathInCache).lock();
    }
    
    private Lock getLock(String pathInCache)
    {
        synchronized (locks)
        {
            Lock lock = locks.get(pathInCache);
            if (lock == null)
            {
                lock = new ReentrantLock();
                locks.put(pathInCache, lock);
            }
            return lock;
        }
    }
    
    private void unlock(String pathInCache)
    {
        synchronized (locks)
        {
            Lock lock = locks.remove(pathInCache);
            if (lock != null)
            {
                lock.unlock();
            }
        }
    }

}
