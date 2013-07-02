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
import java.util.List;
import java.util.UUID;

import com.clcbio.api.base.attribute_custom.CustomAttribute;
import com.clcbio.api.base.persistence.PersistenceException;
import com.clcbio.api.base.persistence.model.IndexStatus;
import com.clcbio.api.base.persistence.model.PersistenceListener;
import com.clcbio.api.base.persistence.model.PersistenceModel;
import com.clcbio.api.base.persistence.model.PersistenceStructure;
import com.clcbio.api.base.persistence.model.PersistenceStructureScore;
import com.clcbio.api.base.persistence.model.PersistenceTransaction;
import com.clcbio.api.base.persistence.model.RecycleBin;
import com.clcbio.api.free.datatypes.ClcObject;
import com.clcbio.api.free.datatypes.project.LocationObject;

/**
 * @author anttil
 */
public class OpenBISPersistenceModel extends Folder implements PersistenceModel
{

    public OpenBISPersistenceModel(String name, ContentProvider content)
    {
        super(name, null, null, content);
        this.id = UUID.randomUUID().toString();
        this.model = this;
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }
        if (o == null || !(o instanceof PersistenceModel))
        {
            return false;
        }
        PersistenceModel other = (PersistenceModel) o;
        return getId().equals(other.getId());
    }

    @Override
    public void addListener(PersistenceStructure arg0, PersistenceListener arg1)
    {
        System.err.println(id + ": addListener(" + arg0 + ", " + arg1 + ")");
    }

    @Override
    public PersistenceTransaction beginTransaction()
    {
        System.err.println(id + ": beginTransaction()");
        return null;
    }

    @Override
    public boolean exists(String arg0)
    {
        System.err.println(id + ": exists(" + arg0 + ")");
        return true;
    }

    @Override
    public PersistenceStructure fetch(String structureId) throws PersistenceException
    {
        try
        {
            ContentSearch search = new ContentSearch(structureId);
            return search.runOn(this);
        } catch (RuntimeException e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public CustomAttribute[] getAllAttributes() throws PersistenceException
    {
        System.err.println(id + ": getAllAttributes()");
        return new CustomAttribute[0];
    }

    @Override
    public Object[] getArgumentsToStaticCreate()
    {
        System.err.println(id + ": getArgumentsToStaticCreate()");
        return new Object[] { "openBIS" };
    }

    @Override
    public CustomAttribute getAttribute(String arg0) throws PersistenceException
    {
        System.err.println(id + ": getAttribute(" + arg0 + ")");
        return null;
    }

    @Override
    public IndexStatus getIndexStatus()
    {
        System.err.println(id + ": getIndexStatus()");
        return IndexStatus.FINISHED;
    }

    @Override
    public List<PersistenceListener> getListeners(PersistenceStructure arg0)
    {
        System.err.println(id + ": getListeners(" + arg0 + ")");
        return new ArrayList<PersistenceListener>();
    }

    @Override
    public String getLongDescription()
    {
        System.err.println(id + ": getLongDescrption()");
        return "long description";
    }

    @Override
    public RecycleBin getRecycleBin()
    {
        System.err.println(id + ": getRecycleBin()");
        return null;
    }

    @Override
    public void insertAttribute(CustomAttribute arg0) throws PersistenceException
    {
        System.err.println(id + ": insertAttribute(" + arg0 + ")");
    }

    @Override
    public boolean isActive()
    {
        System.err.println(id + ": isActive()");
        return true;
    }

    @Override
    public void removeAttribute(CustomAttribute arg0) throws PersistenceException
    {
        System.err.println(id + ": removeAttribute(" + arg0 + ")");
    }

    @Override
    public void removeListener(PersistenceStructure arg0, PersistenceListener arg1)
    {
        System.err.println(id + ": removeListener(" + arg0 + ", " + arg1 + ")");
    }

    @Override
    public Iterator<PersistenceStructureScore> searchWithScore(String arg0, int arg1, int arg2) throws PersistenceException
    {
        System.err.println(id + ": searchWithScore(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
        return null;
    }

    @Override
    public void updateActive()
    {
        System.err.println(id + ": updateActive()");
    }

    @Override
    public ClcObject getObject()
    {
        System.err.println(id + ": getObject()");
        return new LocationObject(this);
    }

    @Override
    public Class<?> getType()
    {
        System.err.println(id + ": getType()");
        return LocationObject.class;
    }
}
