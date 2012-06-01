/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.EntityKind;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IScriptImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;

/**
 * @author kohleman
 */
public class ScriptImmutable implements IScriptImmutable
{
    private Script script;

    public ScriptImmutable(Script script)
    {
        this.script = script;
    }

    @Override
    public EntityKind getEntityKind()
    {
        return EntityKind.valueOf(script.getEntityKind().name());
    }

    @Override
    public String getDescription()
    {
        return script.getDescription();
    }

    @Override
    public String getName()
    {
        return script.getName();
    }

    @Override
    public String getScript()
    {
        return script.getScript();
    }

    public DatabaseInstance getDatabaseInstance()
    {
        return script.getDatabaseInstance();
    }

    public Long getId()
    {
        return script.getId();
    }

    public ScriptType getScriptType()
    {
        return script.getScriptType();
    }

    @Override
    public String toString()
    {
        return script.toString();
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

    @Override
    public String getCode()
    {
        return script.getId().toString();
    }

}
