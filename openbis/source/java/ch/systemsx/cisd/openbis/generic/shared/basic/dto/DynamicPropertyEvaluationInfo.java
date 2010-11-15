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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Izabela Adamczyk
 */
public class DynamicPropertyEvaluationInfo implements IsSerializable, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private EntityKind entityKind;

    private String script;

    private String entityIdentifier;

    public DynamicPropertyEvaluationInfo()
    {
    }

    public DynamicPropertyEvaluationInfo(EntityKind entityKind, String entityIdentifier,
            String script)
    {
        this.entityKind = entityKind;
        this.entityIdentifier = entityIdentifier;
        this.script = script;
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

    public String getEntityIdentifier()
    {
        return entityIdentifier;
    }

    public void setEntityIdentifier(String entityIdentifier)
    {
        this.entityIdentifier = entityIdentifier;
    }

}
