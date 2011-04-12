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

package ch.systemsx.cisd.common.hdf5;

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;

/**
 * {@link IHierarchicalContent} implementation for an HDF5 container.
 * 
 * @author Piotr Buczek
 */
public class HDF5ContainerTraverser
{
    public static void main(String[] args)
    {
        File container = new File("container.h5");
        System.err.println(container.getAbsolutePath());
        if (container.exists())
        {
            Hdf5Container hdf5Container = new Hdf5Container(container);
            IHDF5SimpleReader reader = hdf5Container.createSimpleReader();
            try
            {
                traverse(reader, "/", "");
            } finally
            {
                reader.close();
            }
        }
    }

    public static void traverse(IHDF5SimpleReader reader, String relativePath, String indent)
    {
        System.err.println(indent + "traverse: '" + relativePath + "'");
        List<String> childPaths = reader.getGroupMembers(relativePath);
        String newIndent = indent + "\t";
        for (String childPath : childPaths)
        {
            String newRelativePath =
                    (relativePath.equals(File.separator)) ? childPath : relativePath
                            + File.separator + childPath;
            if (reader.isGroup(newRelativePath))
            {
                System.err.println(newIndent + "group: '" + childPath + "'");
                traverse(reader, newRelativePath, newIndent);
            } else
            {
                System.err.println(newIndent + "file: '" + childPath + "'");
            }
        }
    }

}