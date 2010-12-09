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

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;

/**
 * @author Franz-Josef Elmer
 */
public class TableModelRowWithObject<T extends ISerializable> extends TableModelRow implements IIdHolder
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private T objectOrNull;
    
    private Long id;

    public TableModelRowWithObject(T objectOrNull, List<ISerializableComparable> values)
    {
        super(values);
        this.objectOrNull = objectOrNull;
        if (objectOrNull instanceof IIdHolder)
        {
            id = ((IIdHolder) objectOrNull).getId();
        } else
        {
            for (ISerializableComparable value : values)
            {
                if (value instanceof SerializableComparableIDDecorator)
                {
                    id = ((SerializableComparableIDDecorator) value).getID();
                    break;
                }
            }
        }
    }

    // GWT only
    @SuppressWarnings("unused")
    private TableModelRowWithObject()
    {
    }

    public T getObjectOrNull()
    {
        return objectOrNull;
    }

    public Long getId()
    {
        return id;
    }

}
