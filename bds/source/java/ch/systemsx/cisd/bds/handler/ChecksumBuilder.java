/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.bds.Constants;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Class which builds checksums of all files in a {@link IDirectory}.
 * 
 * @author Franz-Josef Elmer
 */
class ChecksumBuilder
{
    private final IChecksumCalculator checksumCalculator;

    ChecksumBuilder(final IChecksumCalculator checksumCalculator)
    {
        this.checksumCalculator = checksumCalculator;
    }

    /**
     * Builds a multiline string which contains checksum and path of all files in the specified directory.
     */
    String buildChecksumsForAllFilesIn(IDirectory directory)
    {
        assert directory != null : "Unspecified directory";
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        addChecksums(printWriter, null, directory);
        return writer.toString();
    }

    private void addChecksums(PrintWriter printWriter, String nodePath, IDirectory directory)
    {
        List<INode> children = new ArrayList<INode>();
        for (INode child : directory)
        {
            children.add(child);
        }
        Collections.sort(children, new Comparator<INode>()
            {
                public int compare(INode n1, INode n2)
                {
                    return n1.getName().compareToIgnoreCase(n2.getName());
                }
            });
        for (INode child : children)
        {
            addChecksum(printWriter, nodePath, child);
        }
    }

    private void addChecksum(PrintWriter printWriter, String path, INode node)
    {
        String nodePath = (path == null ? "" : path + Constants.PATH_SEPARATOR) + node.getName();
        if (node instanceof IFile)
        {
            IFile file = (IFile) node;
            InputStream inputStream = file.getInputStream();
            try
            {
                printWriter.print(checksumCalculator.calculateChecksum(inputStream));
                printWriter.print("  ");
                printWriter.println(nodePath);
            } catch (IOException ex)
            {
                throw new EnvironmentFailureException("Couldn't calculate checksum for file " + nodePath);
            } finally
            {
                IOUtils.closeQuietly(inputStream);
            }
        } else if (node instanceof IDirectory)
        {
            IDirectory directory = (IDirectory) node;
            addChecksums(printWriter, nodePath, directory);
        }
    }
}
