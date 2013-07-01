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

import java.util.ArrayList;
import java.util.Iterator;

import com.clcbio.api.base.persistence.PersistenceException;
import com.clcbio.api.base.persistence.model.PersistenceContainer;
import com.clcbio.api.base.persistence.model.PersistenceModel;
import com.clcbio.api.base.persistence.model.PersistenceStructure;
import com.clcbio.api.free.datatypes.ClcObject;
import com.clcbio.api.free.datatypes.project.FolderObject;

/**
 * @author anttil
 */
public class Folder extends BasePersistenceContainer
{

    public Folder(String name, PersistenceContainer parent, PersistenceModel model)
    {
        super(name, parent, model);
    }

    @Override
    public Iterator<PersistenceStructure> list() throws PersistenceException
    {
        System.err.println(id + ": list()");
        return new ArrayList<PersistenceStructure>().iterator();
    }

    @Override
    public ClcObject getObject()
    {
        System.err.println(id + ": getObject()");
        return new FolderObject(this);
    }

    @Override
    public Class<?> getType()
    {
        System.err.println(id + ": Folder.getType()");
        return FolderObject.class;
    }

}
