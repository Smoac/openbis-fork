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

import ch.systemsx.cisd.common.io.ByteArrayBasedContent;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.etlserver.hdf5.Hdf5Container;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;

/**
 * @author Franz-Josef Elmer
 */
public class Hdf5BasedContentRepository implements IContentRepository
{
    private final Hdf5Container hdf5Container;

    private IHDF5SimpleReader reader;

    public Hdf5BasedContentRepository(File hdf5ContainerFile)
    {
        this.hdf5Container = new Hdf5Container(hdf5ContainerFile);
    }

    public void open()
    {
        reader = hdf5Container.createSimpleReader();
    }

    public IContent getContent(String path)
    {
        if (reader == null)
        {
            throw new IllegalStateException("open() method hasn't be invoked.");
        }
        byte[] content = reader.readAsByteArray(path);
        int index = path.lastIndexOf('/');
        return new ByteArrayBasedContent(content, index < 0 ? path : path.substring(index + 1));
    }

    public void close()
    {
        reader.close();
    }

}
