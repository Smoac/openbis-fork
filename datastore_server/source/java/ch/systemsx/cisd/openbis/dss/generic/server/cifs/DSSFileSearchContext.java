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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.SearchContext;
import org.alfresco.jlan.util.WildCard;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.DSSFileSystemView;

/**
 * @author Franz-Josef Elmer
 */
public class DSSFileSearchContext extends SearchContext
{
    private List<FtpFile> files = new ArrayList<>();

    private int index;

    public DSSFileSearchContext(DSSFileSystemView view, String normalizedSearchPath, int fileAttributes)
    {
        String[] pathStr = FileName.splitPath(normalizedSearchPath, java.io.File.separatorChar);
        try
        {
            if (pathStr[1] != null && WildCard.containsWildcards(pathStr[1]))
            {
                WildCard wildCard = new WildCard(pathStr[1], true);
                FtpFile directory = view.getFile(pathStr[0]);
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
                FtpFile file = view.getFile(normalizedSearchPath);
                if (matches(null, fileAttributes, file))
                {
                    files.add(file);
                }
            }
        } catch (FtpException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
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
        System.out.println("DSSFileSearchContext.nextFileName()");
        // TODO Auto-generated method stub
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
        System.out.println("DSSFileSearchContext.restartAt() "+info);
        // TODO Auto-generated method stub
        return false;
    }

}
