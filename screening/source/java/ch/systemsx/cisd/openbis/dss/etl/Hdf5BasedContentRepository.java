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

package ch.systemsx.cisd.openbis.dss.etl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.io.HDF5DataSetBasedContent;
import ch.systemsx.cisd.common.io.IContent;

/**
 * A content repository that is backed by an HDF5 container.
 * 
 * @author Franz-Josef Elmer
 */
public class Hdf5BasedContentRepository implements IContentRepository
{
    private final File hdf5ContainerFile;

    private final List<HDF5DataSetBasedContent> contentList;

    public Hdf5BasedContentRepository(File hdf5ContainerFile)
    {
        this.hdf5ContainerFile = hdf5ContainerFile;
        this.contentList = new ArrayList<HDF5DataSetBasedContent>();
    }

    public void open()
    {
        // That's a no-op.
    }

    public IContent getContent(String path)
    {
        final HDF5DataSetBasedContent content =
                new HDF5DataSetBasedContent(hdf5ContainerFile, path);
        contentList.add(content);
        return content;
    }

    public void close()
    {
        for (HDF5DataSetBasedContent content : contentList)
        {
            content.close();
        }
    }

}
