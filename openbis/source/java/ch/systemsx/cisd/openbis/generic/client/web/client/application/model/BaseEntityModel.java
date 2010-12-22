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
import java.util.Map.Entry;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.CommonColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.GridCustomColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.MultilineHTML;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.IInvalidationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.PrimitiveValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractRegistrationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * The generic model which can be created automatically using the array of
 * {@link IColumnDefinitionKind} or {@link IColumnDefinition}.
 * 
 * @author Franz-Josef Elmer
 * @author Tomasz Pylak
 */
public class BaseEntityModel<T> extends SimplifiedBaseModelData
{
    private static final long serialVersionUID = 1L;

    public BaseEntityModel(final GridRowModel<T> entity,
            IColumnDefinitionKind<T>[] staticColumnDefinitions)
    {
        this(entity, createColumnsDefinition(staticColumnDefinitions, null));
    }

    /** NOTE: it's assumed that columnDefinitions do not contain custom columns */
    public BaseEntityModel(final GridRowModel<T> entity,
            List<? extends IColumnDefinition<T>> columnDefinitions)
    {
        this(entity, columnDefinitions, false);
    }
    
    public BaseEntityModel(final GridRowModel<T> entity,
            List<? extends IColumnDefinition<T>> columnDefinitions, boolean ignoreCustomColumns)
    {
        set(ModelDataPropertyNames.OBJECT, entity.getOriginalObject());

        for (IColumnDefinition<T> column : columnDefinitions)
        {
            if (ignoreCustomColumns && column instanceof GridCustomColumnDefinition)
            {
                continue;
            }
            String value = renderColumnValue(entity, column);
            set(column.getIdentifier(), value);
            if (column instanceof IColumnDefinitionUI<?>)
            {
                set(ModelDataPropertyNames.link(column.getIdentifier()),
                        ((IColumnDefinitionUI<T>) column).tryGetLink(entity.getOriginalObject()));
            }
        }
        addCustomColumns(entity);
    }

    private void addCustomColumns(final GridRowModel<T> model)
    {
        for (Entry<String, PrimitiveValue> entry : model.getCalculatedColumnValues().entrySet())
        {
            set(entry.getKey(), entry.getValue());
        }
    }

    public final T getBaseObject()
    {
        return get(ModelDataPropertyNames.OBJECT);
    }

    public final String tryGetLink(String columnId)
    {
        return get(ModelDataPropertyNames.link(columnId));
    }

    /** render specified column as a link (using div) */
    public void renderAsLink(String columnId)
    {
        String value = this.get(columnId);
        if (value.length() > 0)
        {// only for not null value
            value = LinkRenderer.renderAsLink(value);
        }
        this.set(columnId, value);
    }

    /** render specified column as a link with anchor */
    public void renderAsLinkWithAnchor(String columnId)
    {
        String value = this.get(columnId);
        if (value.length() > 0)
        {// only for not null value
            value = LinkRenderer.renderAsLinkWithAnchor(value);
        }
        this.set(columnId, value);
    }

    /** render specified column as a multiline string with a tooltip (no word wrapping available) */
    public void renderAsMultilineStringWithTooltip(String columnId)
    {
        String value = this.get(columnId);
        if (value.length() > 0)
        {// only for not null value
            value = new MultilineHTML(value).toString();
        }
        this.set(columnId, value);
    }

    // TODO 2010-05-18, IA: unify renderers and remove code below
    // ugly, ugly, ugly!
    protected String renderColumnValue(final GridRowModel<T> entity, IColumnDefinition<T> column)
    {
        String value = column.getValue(entity);
        if (column instanceof CommonColumnDefinition<?>)
        {
            String headerMsgKey = ((CommonColumnDefinition<?>) column).getHeaderMsgKey();
            T originalRecord = entity.getOriginalObject();
            if (headerMsgKey.equals(Dict.REGISTRATOR)
                    && originalRecord instanceof AbstractRegistrationHolder)
            {
                Person registrator = ((AbstractRegistrationHolder) originalRecord).getRegistrator();
                if (registrator != null)
                {
                    value = PersonRenderer.createPersonAnchor(registrator, value);
                }
            } else if (headerMsgKey.equals(Dict.CODE)
                    && originalRecord instanceof IInvalidationProvider
                    && column instanceof AbstractColumnDefinition<?>)
            {
                String linkHref = ((AbstractColumnDefinition<T>) column).tryGetLink(originalRecord);
                boolean invalidate =
                        ((IInvalidationProvider) originalRecord).getInvalidation() != null;
                value = LinkRenderer.getLinkWidget(value, null, linkHref, invalidate).toString();
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
