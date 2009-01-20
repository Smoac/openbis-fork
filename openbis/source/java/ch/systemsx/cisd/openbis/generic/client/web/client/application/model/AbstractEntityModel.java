/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.InvalidableWithCodeRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.CommonColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.AbstractRegistrationHolder;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IInvalidationProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;

/**
 * @author Franz-Josef Elmer
 */
public class AbstractEntityModel<T> extends BaseModelData
{
    private static final long serialVersionUID = 1L;

    protected AbstractEntityModel(final T entity, List<? extends IColumnDefinition<T>> columnsSchema)
    {
        set(ModelDataPropertyNames.OBJECT, entity);

        for (IColumnDefinition<T> column : columnsSchema)
        {
            String value = renderColumnValue(entity, column);
            set(column.getIdentifier(), value);
        }
    }

    public final T getBaseObject()
    {
        return get(ModelDataPropertyNames.OBJECT);
    }

    // ugly, ugly, ugly!
    protected String renderColumnValue(final T entity, IColumnDefinition<T> column)
    {
        String value = column.getValue(entity);
        if (column instanceof CommonColumnDefinition)
        {
            IColumnDefinitionKind<?> columnKind =
                    ((CommonColumnDefinition<?>) column).getColumnDefinitionKind();
            String headerMsgKey = columnKind.getHeaderMsgKey();
            if (headerMsgKey.equals(Dict.REGISTRATOR)
                    && entity instanceof AbstractRegistrationHolder)
            {
                Person registrator = ((AbstractRegistrationHolder) entity).getRegistrator();
                value = PersonRenderer.createPersonAnchor(registrator, value);
            } else if (headerMsgKey.equals(Dict.CODE) && entity instanceof IInvalidationProvider)
            {
                value = InvalidableWithCodeRenderer.render((IInvalidationProvider) entity, value);
            }
        }
        return value;
    }

    protected static <T> List<IColumnDefinitionUI<T>> createColumnsSchemaFrom(
            IColumnDefinitionKind<T>[] columnKinds, IMessageProvider msgProviderOrNull)
    {
        List<IColumnDefinitionUI<T>> list = new ArrayList<IColumnDefinitionUI<T>>();
        for (IColumnDefinitionKind<T> columnKind : columnKinds)
        {
            list.add(createColumn(columnKind, msgProviderOrNull));
        }
        return list;
    }

    private static <T> IColumnDefinitionUI<T> createColumn(IColumnDefinitionKind<T> columnKind,
            IMessageProvider messageProviderOrNull)
    {
        String headerText = null;
        if (messageProviderOrNull != null)
        {
            headerText = messageProviderOrNull.getMessage(columnKind.getHeaderMsgKey());
        }
        return new CommonColumnDefinition<T>(columnKind, headerText);
    }

}
