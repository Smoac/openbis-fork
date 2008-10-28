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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Defines the sample parents columns for sample grid/table.
 * 
 * @author Izabela Adamczyk
 */
public class ParentColumns
{
    private List<ColumnConfig> columns;

    private final IMessageProvider messageProvider;

    public ParentColumns(final IMessageProvider messageProvider)
    {
        assert messageProvider != null : "Unspecified message provider.";
        this.messageProvider = messageProvider;
        columns = new ArrayList<ColumnConfig>();
    }

    public void define(SampleType type)
    {
        columns.clear();
        addGeneratedFromParentColumns(1, type.getGeneratedFromHierarchyDepth());
        addContainerParentColumns(1, type.getPartOfHierarchyDepth());
    }

    private final void addGeneratedFromParentColumns(final int dep, final int maxDep)
    {
        if (dep <= maxDep)
        {
            columns.add(createGeneratedFromParentColumn(dep));
            addGeneratedFromParentColumns(dep + 1, maxDep);
        }
    }

    private final void addContainerParentColumns(final int dep, final int maxDep)
    {
        if (dep <= maxDep)
        {
            columns.add(createContainerParentColumn(dep));
            addContainerParentColumns(dep + 1, maxDep);
        }
    }

    private final ColumnConfig createGeneratedFromParentColumn(final int i)
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setMenuDisabled(true);
        columnConfig.setId(SampleModel.GENERATED_FROM_PARENT_PREFIX + i);
        columnConfig.setHeader(messageProvider.getMessage("generated_from", i));
        columnConfig.setWidth(150);
        return columnConfig;
    }

    private final ColumnConfig createContainerParentColumn(final int i)
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setMenuDisabled(true);
        columnConfig.setId(SampleModel.CONTAINER_PARENT_PREFIX + i);
        columnConfig.setHeader(messageProvider.getMessage("part_of", i));
        columnConfig.setWidth(150);
        return columnConfig;
    }

    public List<ColumnConfig> getColumns()
    {
        return columns;
    }
}