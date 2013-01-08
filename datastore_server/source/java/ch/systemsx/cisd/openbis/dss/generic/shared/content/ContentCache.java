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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
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
    private IDssServiceRpcGeneric remoteDss;

    private OpenBISSessionHolder sessionHolder;

    private File cacheWorkspace;

    public ContentCache(IDssServiceRpcGeneric remoteDss, OpenBISSessionHolder sessionHolder,
            File cacheWorkSpace)
    {
        this.remoteDss = remoteDss;
        this.sessionHolder = sessionHolder;
        cacheWorkspace = cacheWorkSpace;
    }

    IDssServiceRpcGeneric getRemoteDss()
    {
        return remoteDss;
    }

    File getFile(String dataSetCode, DataSetPathInfo path)
    {
        String pathInCache = dataSetCode + "/" + path.getRelativePath();
        File file = new File(cacheWorkspace, pathInCache);

        if (file.exists())
        {
            return file;
        }

        String url =
                remoteDss.getDownloadUrlForFileForDataSet(sessionHolder.getSessionToken(),
                        dataSetCode, path.getRelativePath());
        InputStream input = null;
        try
        {
            if (url.toLowerCase().startsWith("https"))
            {
                input =
                        new URL(null, url, new sun.net.www.protocol.https.Handler())
                                .openConnection().getInputStream();
            } else
            {
                input = new URL(url).openStream();
            }
            putFileToSessionWorkspace(pathInCache, input);
        } catch (MalformedURLException ex)
        {
            throw new ConfigurationFailureException("Malformed URL: " + url);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(input);
        }

        return file;

    }

    private void putFileToSessionWorkspace(String filePath, InputStream inputStream)
    {
        final String subDir = FilenameUtils.getFullPath(filePath);
        final String filename = FilenameUtils.getName(filePath);
        final File dir = new File(cacheWorkspace, subDir);
        dir.mkdirs();
        final File file = new File(dir, filename);
        OutputStream ostream = null;
        try
        {
            ostream = new FileOutputStream(file);
            IOUtils.copyLarge(inputStream, ostream);
        } catch (IOException ex)
        {
            file.delete();
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(ostream);
        }
    }

}
