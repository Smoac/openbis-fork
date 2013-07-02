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

package ch.systemsx.cisd.openbis.clc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.clcbio.api.base.persistence.model.PersistenceContainer;
import com.clcbio.api.base.persistence.model.PersistenceModel;
import com.clcbio.api.base.persistence.model.PersistenceStructure;
import com.clcbio.api.free.datatypes.project.FileObject;

import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;

/**
 * @author anttil
 */
public class FileSystemProvider implements ContentProvider
{

    private IOpenbisServiceFacade openbis;

    private DataSet data;

    private String path;

    public FileSystemProvider(IOpenbisServiceFacade openbis, DataSet data)
    {
        this(openbis, data, "");
    }

    private FileSystemProvider(IOpenbisServiceFacade openbis, DataSet data, String path)
    {
        this.openbis = openbis;
        this.data = data;
        this.path = path;
    }

    @Override
    public Collection<PersistenceStructure> getContent(PersistenceContainer parent, PersistenceModel model)
    {
        List<PersistenceStructure> result = new ArrayList<PersistenceStructure>();
        for (FileInfoDssDTO file : data.getDataSetDss().listFiles(path, false))
        {

            String[] splitPath = file.getPathInListing().split("/");
            String filename = splitPath[splitPath.length - 1];

            if (file.isDirectory())
            {
                result.add(new Folder(filename, parent, model, new FileSystemProvider(openbis, data, file.getPathInDataSet())));
            } else
            {
                InputStream in = null;
                OutputStream out = null;
                java.io.File f;
                try
                {
                    in = data.getFile(file.getPathInDataSet());
                    f = new java.io.File("/tmp/" + filename);
                    if (f.exists())
                    {
                        f.delete();
                    }
                    out = new FileOutputStream(f);

                    byte[] buffer = new byte[1024];
                    int len = in.read(buffer);
                    while (len != -1)
                    {
                        out.write(buffer, 0, len);
                        len = in.read(buffer);
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                    continue;
                } finally
                {
                    try
                    {
                        if (in != null)
                        {
                            in.close();
                        }
                    } catch (IOException e)
                    {
                    }

                    try
                    {
                        if (out != null)
                        {
                            out.close();
                        }
                    } catch (IOException e)
                    {
                    }
                }

                FileObject fileObject = new FileObject(f);
                fileObject.setParent(parent);
                fileObject.setName(filename);
                fileObject.setId(parent.getId() + "/" + filename);
                result.add(fileObject);
            }
        }
        return result;
    }
}
