/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * The <i>GWT</i> equivalent to ScriptPE.
 * 
 * @author Izabela Adamczyk
 */
public class Script extends AbstractRegistrationHolder implements Comparable<Script>, IIdHolder,
        IScriptUpdates
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Long id;

    private ScriptType scriptType;

    private String name;

    private String description;

    private DatabaseInstance databaseInstance;

    private String script;

    private EntityKind entityKind;

    public Script()
    {
    }

    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    public void setEntityKind(EntityKind entityKind)
    {
        this.entityKind = entityKind;
    }

    public String getScript()
    {
        return script;
    }

    public void setScript(String script)
    {
        this.script = script;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public DatabaseInstance getDatabaseInstance()
    {
        return databaseInstance;
    }

    public void setDatabaseInstance(DatabaseInstance databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    public Long getId()
    {
        return id;
    }

    public ScriptType getScriptType()
    {
        return scriptType;
    }

    public void setScriptType(ScriptType scriptType)
    {
        this.scriptType = scriptType;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public int compareTo(final Script o)
    {
        if (o == null)
        {
            return -1;
        } else
        {
            return this.toString().compareTo(o.toString());
        }
    }

    public void setId(Long id)
    {
        this.id = id;
    }

}
