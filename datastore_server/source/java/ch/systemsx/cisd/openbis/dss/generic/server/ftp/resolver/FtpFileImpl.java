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

import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.common.io.IHierarchicalContentNode;
import ch.systemsx.cisd.common.io.IHierarchicalContentNodeFilter;
import ch.systemsx.cisd.common.utilities.HierarchicalContentUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpConstants;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpFileFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

/**
 * An {@link FtpFile} implementation which lazily creates and uses {@link IHierarchicalContent} when
 * needed.
 * <p>
 * The resources represented by {@link FtpFileImpl} exist in the data store.
 * 
 * @author Kaloyan Enimanev
 */
public class FtpFileImpl extends AbstractFtpFile
{
    private final String dataSetCode;

    private final String pathInDataSet;

    private final boolean isDirectory;

    private final long size;

    private final long lastModified;

    private final IHierarchicalContentNodeFilter childrenFilter;

    public FtpFileImpl(String dataSetCode, String path, String pathInDataSet, boolean isDirectory,
            long size, long lastModified, IHierarchicalContentNodeFilter childrenFilter)
    {
        super(path);
        this.dataSetCode = dataSetCode;
        this.pathInDataSet = pathInDataSet;
        this.isDirectory = isDirectory;
        this.size = size;
        this.lastModified = lastModified;
        this.childrenFilter = childrenFilter;
    }

    public InputStream createInputStream(long offset) throws IOException
    {
        IHierarchicalContent content = createHierarchicalContent();
        try
        {
            IHierarchicalContentNode contentNode = getContentNodeForThisFile(content);
            InputStream result =
                    HierarchicalContentUtils.getInputStreamAutoClosingContent(contentNode, content);

            if (offset > 0)
            {
                result.skip(offset);
            }
            return result;
        } catch (IOException ioex)
        {
            content.close();
            return null;
        }
    }

    public long getLastModified()
    {
        return lastModified;
    }

    public long getSize()
    {
        if (isFile())
        {
            return size;
        }
        return 0;
    }

    public boolean isDirectory()
    {
        return isDirectory;
    }

    public boolean isFile()
    {
        return isDirectory() == false;
    }

    @Override
    public List<org.apache.ftpserver.ftplet.FtpFile> unsafeListFiles()
    {
        if (isFile())
        {
            throw new UnsupportedOperationException();
        }

        IHierarchicalContent content = createHierarchicalContent();
        try
        {
            IHierarchicalContentNode contentNode = getContentNodeForThisFile(content);
            List<IHierarchicalContentNode> children = contentNode.getChildNodes();
            List<org.apache.ftpserver.ftplet.FtpFile> result =
                    new ArrayList<org.apache.ftpserver.ftplet.FtpFile>();

            for (IHierarchicalContentNode childNode : children)
            {
                if (childrenFilter.accept(childNode))
                {
                    String childPath =
                            absolutePath + FtpConstants.FILE_SEPARATOR + childNode.getName();
                    FtpFile childFile =
                            FtpFileFactory.createFtpFile(dataSetCode, childPath, childNode,
                                    childrenFilter);
                    result.add(childFile);
                }
            }
            return result;
        } finally
        {
            content.close();
        }
    }

    private IHierarchicalContent createHierarchicalContent()
    {
        return ServiceProvider.getHierarchicalContentProvider().asContent(dataSetCode);
    }

    private IHierarchicalContentNode getContentNodeForThisFile(IHierarchicalContent content)
    {
        return content.getNode(pathInDataSet);
    }
}
