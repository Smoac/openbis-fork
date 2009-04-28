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
import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.InvalidableWithCodeRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.CommonColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IInvalidationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractRegistrationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * The generic model which can be created automatically using the array of
 * {@link IColumnDefinitionKind} or {@link IColumnDefinition}.
 * 
 * @author Franz-Josef Elmer
 * @author Tomasz Pylak
 */
public class BaseEntityModel<T> extends BaseModelData
{
    private static final long serialVersionUID = 1L;

    public BaseEntityModel(final T entity, IColumnDefinitionKind<T>[] staticColumnDefinitions)
    {
        this(entity, createColumnsDefinition(staticColumnDefinitions, null));
    }

    public BaseEntityModel(final T entity, List<? extends IColumnDefinition<T>> columnDefinitions)
    {
        set(ModelDataPropertyNames.OBJECT, entity);

        for (IColumnDefinition<T> column : columnDefinitions)
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
            String headerMsgKey = ((CommonColumnDefinition<?>) column).getHeaderMsgKey();
            if (headerMsgKey.equals(Dict.REGISTRATOR)
                    && entity instanceof AbstractRegistrationHolder)
            {
                Person registrator = ((AbstractRegistrationHolder) entity).getRegistrator();
                value = PersonRenderer.createPersonAnchor(registrator, value);
            } else if (headerMsgKey.equals(Dict.CODE))
            {
                if (entity instanceof IInvalidationProvider)
                {
                    value = InvalidableWithCodeRenderer.render((IInvalidationProvider) entity, value);
                }
                value = LinkRenderer.renderAsLink(value);
            }
        }
        return value;
    }

    /** @param msgProviderOrNull if null, no headers labels will be generated */
    public static <T> ColumnDefsAndConfigs<T> createColumnConfigs(
            IColumnDefinitionKind<T>[] colDefKinds, IMessageProvider msgProviderOrNull)
    {
        List<IColumnDefinitionUI<T>> colDefs =
                createColumnsDefinition(colDefKinds, msgProviderOrNull);
        return ColumnDefsAndConfigs.create(colDefs);
    }

    /** @param msgProviderOrNull if null, no headers labels will be generated */
    public static <T> List<IColumnDefinitionUI<T>> createColumnsDefinition(
            IColumnDefinitionKind<T>[] columnKinds, IMessageProvider msgProviderOrNull)
    {
        return createColumnsDefinition(Arrays.asList(columnKinds), msgProviderOrNull);
    }

    /** @param msgProviderOrNull if null, no headers labels will be generated */
    public static <T> List<IColumnDefinitionUI<T>> createColumnsDefinition(
            List<IColumnDefinitionKind<T>> columnKinds, IMessageProvider msgProviderOrNull)
    {
        List<IColumnDefinitionUI<T>> list = new ArrayList<IColumnDefinitionUI<T>>();
        for (IColumnDefinitionKind<T> columnKind : columnKinds)
        {
            list.add(createColumnDefinition(columnKind, msgProviderOrNull));
        }
        return list;
    }

    public static <T> IColumnDefinitionUI<T> createColumnDefinition(
            IColumnDefinitionKind<T> columnKind, IMessageProvider messageProviderOrNull)
    {
        String headerText = null;
        if (messageProviderOrNull != null)
        {
            headerText =
                    messageProviderOrNull.getMessage(columnKind.getDescriptor().getHeaderMsgKey());
        }
        return new CommonColumnDefinition<T>(columnKind, headerText);
    }

}
