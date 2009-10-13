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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework;

import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;

/**
 * Definition of table columns for entities of type <code>T</code> together with the instructions to
 * render each column value.
 * 
 * @author Tomasz Pylak
 * @author Franz-Josef Elmer
 */
public class CommonColumnDefinition<T> extends AbstractColumnDefinition<T>
{
    protected IColumnDefinitionKind<T> columnDefinitionKind;

    /** Default constructor for GWT. */
    public CommonColumnDefinition()
    {
    }

    /**
     * Creates an instance for the specified column definition kind and header text.
     */
    public CommonColumnDefinition(final IColumnDefinitionKind<T> columnDefinitionKind,
            final String headerText)
    {
        super(headerText, columnDefinitionKind.getDescriptor().getWidth(), columnDefinitionKind
                .getDescriptor().isHidden());
        this.columnDefinitionKind = columnDefinitionKind;
    }

    /**
     * Returns the column definition kind.
     */
    public String getHeaderMsgKey()
    {
        return columnDefinitionKind.getDescriptor().getHeaderMsgKey();
    }

    @Override
    protected String tryGetValue(T entity)
    {
        return columnDefinitionKind.getDescriptor().tryGetValue(entity);
    }

    @Override
    public Comparable<?> getComparableValue(GridRowModel<T> rowModel)
    {
        return columnDefinitionKind.getDescriptor().getComparableValue(rowModel);
    }

    public String getIdentifier()
    {
        return columnDefinitionKind.id();
    }

}
