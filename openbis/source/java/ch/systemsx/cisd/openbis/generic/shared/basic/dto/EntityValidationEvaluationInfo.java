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

/**
 * @author Izabela Adamczyk
 */
public class EntityValidationEvaluationInfo extends BasicEntityDescription
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String script;

    private boolean isNew;

    public EntityValidationEvaluationInfo()
    {
    }

    public EntityValidationEvaluationInfo(EntityKind entityKind, String entityIdentifier,
            boolean isNew, String script)
    {
        super(entityKind, entityIdentifier);
        this.script = script;
        this.isNew = isNew;
    }

    public String getScript()
    {
        return script;
    }

    public void setScript(String script)
    {
        this.script = script;
    }

    public boolean isNew()
    {
        return isNew;
    }

    public void setNew(boolean isNew)
    {
        this.isNew = isNew;
    }

}
