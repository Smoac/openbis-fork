/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.ftplet.FtpFile;

import ch.systemsx.cisd.common.io.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpConstants;

/**
 * An {@link FtpFile} implementation adapting an underlying {@link IHierarchicalContentNode}.
 * <p>
 * The resources represented by {@link HierarchicalContentToFtpFileAdapter} exist in the data store.
 * 
 * @author Kaloyan Enimanev
 */
public class HierarchicalContentToFtpFileAdapter extends AbstractFtpFile
{
    private final IHierarchicalContentNode contentNode;

    public HierarchicalContentToFtpFileAdapter(String path, IHierarchicalContentNode contentNode)
    {
        super(path);
        this.contentNode = contentNode;
    }

    public InputStream createInputStream(long offset) throws IOException
    {
        InputStream result = contentNode.getInputStream();
        if (offset > 0)
        {
            result.skip(offset);
        }
        return result;
    }

    public long getLastModified()
    {
        try
        {
            return contentNode.getFile().lastModified();
        } catch (UnsupportedOperationException uoe)
        {
            return 0;
        }
    }


    public long getSize()
    {
        if (isFile())
        {
            return contentNode.getFileLength();
        }
        return 0;
    }

    public boolean isDirectory()
    {
        return contentNode.isDirectory();
    }

    public boolean isFile()
    {
        return isDirectory() == false;
    }

    public List<org.apache.ftpserver.ftplet.FtpFile> listFiles()
    {
        if (isDirectory())
        {
            List<IHierarchicalContentNode> children = contentNode.getChildNodes();
            List<org.apache.ftpserver.ftplet.FtpFile> result =
                    new ArrayList<org.apache.ftpserver.ftplet.FtpFile>();
            for (IHierarchicalContentNode childNode : children)
            {
                String childPath = absolutePath + FtpConstants.FILE_SEPARATOR + childNode.getName();
                HierarchicalContentToFtpFileAdapter childFile =
                        new HierarchicalContentToFtpFileAdapter(childPath, childNode);
                result.add(childFile);
            }
            return result;

        } else
        {
            throw new UnsupportedOperationException();
        }
    }

}
