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

import javax.swing.Icon;

import com.clcbio.api.base.persistence.model.PersistenceContainer;
import com.clcbio.api.base.persistence.model.PersistenceListener;
import com.clcbio.api.base.persistence.model.PersistenceModel;
import com.clcbio.api.base.persistence.model.PersistenceStructure;
import com.clcbio.api.free.datatypes.ClcObject;

/**
 * @author anttil
 */
public abstract class BasePersistenceStructure implements PersistenceStructure
{

    protected String id;

    protected String name;

    protected PersistenceContainer parent;

    protected PersistenceModel model;

    public BasePersistenceStructure(String name, PersistenceContainer parent, PersistenceModel model)
    {
        if (parent != null)
        {
            this.id = parent.getId() + "/" + name;
        } else
        {
            this.id = name;
        }
        this.name = name;
        this.parent = parent;
        this.model = model;
    }

    @Override
    public String getId()
    {
        System.err.println(id + ": getId()");
        return id;
    }

    @Override
    public String getName()
    {
        System.err.println(id + ": : getName()");
        return name;
    }

    @Override
    public void addListener(PersistenceListener arg0)
    {
        System.err.println(id + ": addListener(" + arg0 + ")");
    }

    @Override
    public Icon getIcon()
    {
        System.err.println(id + ": getIcon()");
        return new DummyIcon();
    }

    @Override
    public abstract ClcObject getObject();

    @Override
    public PersistenceContainer getParent()
    {
        System.err.println(id + ": getParent()");
        return parent;
    }

    @Override
    public PersistenceModel getPersistenceModel()
    {
        System.err.println(id + ": getPersistenceModel()");
        return model;
    }

    @Override
    public abstract Class<?> getType();

    @Override
    public int getUsage()
    {
        System.err.println(id + ": getUsage()");
        return 0;
    }

    @Override
    public void removeListener(PersistenceListener arg0)
    {
        System.err.println(id + ": removeListener(" + arg0 + ")");
    }

    @Override
    public void setParent(PersistenceContainer arg0)
    {
        System.err.println(id + ": setParent(" + arg0 + ")");
        this.parent = arg0;
    }

}
