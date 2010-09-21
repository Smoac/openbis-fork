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

/**
 * An <i>abstract</i> entity type.
 * 
 * @author Christian Ribeaud
 */
public class BasicEntityType extends AbstractType
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private DatabaseInstance databaseInstance;

    public BasicEntityType()
    {
    }

    public BasicEntityType(String code)
    {
        setCode(code);
    }

    public final DatabaseInstance getDatabaseInstance()
    {
        return databaseInstance;
    }

    public final void setDatabaseInstance(final DatabaseInstance databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof BasicEntityType == false)
        {
            return false;
        }
        final BasicEntityType that = (BasicEntityType) obj;
        return getCode().equals(that.getCode())
                && (databaseInstance == null || databaseInstance.equals(that.databaseInstance));
    }

    @Override
    public final int hashCode()
    {
        int hashCode = getCode().hashCode();
        if (databaseInstance != null && databaseInstance.getCode() != null)
        {
            hashCode ^= databaseInstance.getCode().hashCode();
        }
        return hashCode;
    }

}
