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

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyAssignment;

/**
 * Wrapper of {@link PropertyAssignmentImmutable} as {@link IPropertyAssignment} where setters do nothing.
 * 
 * @author Franz-Josef Elmer
 */
class PropertyAssignmentWrapper extends PropertyAssignmentImmutable implements IPropertyAssignment
{
    PropertyAssignmentWrapper(PropertyAssignmentImmutable entityTypePropType)
    {
        super(entityTypePropType.getEntityTypePropType());
    }

    @Override
    public void setMandatory(boolean mandatory)
    {
    }

    @Override
    public void setSection(String section)
    {
    }

    @Override
    public void setDefaultValue(String defaultValue)
    {
    }

    @Override
    public void setPositionInForms(Long position)
    {
    }

    @Override
    public void setScriptName(String scriptName)
    {
    }

    @Override
    public void setDynamic(boolean dynamic)
    {
    }

    @Override
    public void setManaged(boolean managed)
    {
    }

    @Override
    public void setShownEdit(boolean edit)
    {
    }

}
