/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.cifs;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.SearchContext;
import org.alfresco.jlan.util.WildCard;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;

import ch.systemsx.cisd.openbis.dss.generic.server.ftp.Cache;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.DSSFileSystemView;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.NonExistingFtpFile;

/**
 * @author Franz-Josef Elmer
 */
public class DSSFileSearchContext extends SearchContext
{
    private List<FtpFile> files = new ArrayList<>();

    private int index;

    public DSSFileSearchContext(DSSFileSystemView view, String normalizedSearchPath, int fileAttributes,
            Cache cache) throws FileNotFoundException
    {
        String[] pathStr = FileName.splitPath(normalizedSearchPath, java.io.File.separatorChar);
        try
        {
            if (pathStr[1] != null && WildCard.containsWildcards(pathStr[1]))
            {
                WildCard wildCard = new WildCard(pathStr[1], true);
                FtpFile directory = getFile(view, pathStr[0], cache);
                List<FtpFile> list = directory.listFiles();
                for (FtpFile file : list)
                {
                    if (matches(wildCard, fileAttributes, file))
                    {
                        files.add(file);
                    }
                }
            } else
            {
                FtpFile file = getFile(view, normalizedSearchPath, cache);
                if (matches(null, fileAttributes, file))
                {
                    files.add(file);
                }
            }
        } catch (FtpException ex)
        {
            throw new FileNotFoundException("Invalid search path '" + normalizedSearchPath + "'. Reason: " + ex);
        }
    }

    private FtpFile getFile(DSSFileSystemView view, String path, Cache cache) throws FtpException
    {
        FtpFile file = view.getFile(path, cache);
        if (file instanceof NonExistingFtpFile)
        {
            throw new FtpException(file.getAbsolutePath() + " doesn't exist. Reason: "
                    + ((NonExistingFtpFile) file).getErrorMessage());
        }
        return file;
    }

    private boolean matches(WildCard wildcardOrNull, int fileAttributes, FtpFile file)
    {
        return wildcardOrNull == null || wildcardOrNull.matchesPattern(file.getName());
    }

    @Override
    public int getResumeId()
    {
        return index;
    }

    @Override
    public boolean hasMoreFiles()
    {
        return index < files.size();
    }

    @Override
    public boolean nextFileInfo(FileInfo info)
    {
        if (hasMoreFiles() == false)
        {
            return false;
        }
        Utils.populateFileInfo(info, files.get(index++));
        return true;
    }

    @Override
    public String nextFileName()
    {
        return null;
    }

    @Override
    public boolean restartAt(int resumeId)
    {
        if (resumeId < 0 || resumeId >= files.size())
        {
            return false;
        }
        index = resumeId;
        return true;
    }

    @Override
    public boolean restartAt(FileInfo info)
    {
        return false;
    }

}
