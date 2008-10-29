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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Defines the common columns of sample grid/table.
 * 
 * @author Izabela Adamczyk
 */
public final class CommonColumns
{
    private final List<ColumnConfig> columns;

    private final IMessageProvider messageProvider;

    public CommonColumns(final IMessageProvider messageProvider)
    {
        columns = new ArrayList<ColumnConfig>();
        this.messageProvider = messageProvider;
        define();

    }

    public final List<ColumnConfig> getColumns()
    {
        return columns;
    }

    private void define()
    {
        columns.add(createCodeColumn());
        columns.add(createIdentifierColumn());
        columns.add(createIsSharedColumn());
        columns.add(createRegistratorColumn());
        columns.add(createRegistrationDateColumnConfig());
        columns.add(createIsInvalidColumn());
        columns.add(createExperimentColumn());
    }

    private ColumnConfig createRegistrationDateColumnConfig()
    {
        final ColumnConfig columnConfig =
                ColumnConfigFactory.createRegistrationDateColumnConfig(messageProvider);
        columnConfig.setHidden(true);
        return columnConfig;
    }

    private ColumnConfig createCodeColumn()
    {
        final ColumnConfig codeColumn = ColumnConfigFactory.createCodeColumnConfig(messageProvider);
        codeColumn.setRenderer(new SampleRenderer());
        return codeColumn;
    }

    private final ColumnConfig createRegistratorColumn()
    {
        final ColumnConfig registratorColumn =
                ColumnConfigFactory.createRegistratorColumnConfig(messageProvider);
        registratorColumn.setHidden(true);
        return registratorColumn;
    }

    public final static ColumnConfig createIdentifierColumn()
    {
        final ColumnConfig columnConfig = ColumnConfigFactory.createMenuDisableColumnConfig();
        columnConfig.setId(ModelDataPropertyNames.SAMPLE_IDENTIFIER);
        columnConfig.setHeader("Identifier");
        columnConfig.setHidden(true);
        columnConfig.setWidth(150);
        return columnConfig;
    }

    public final static ColumnConfig createIsSharedColumn()
    {
        final ColumnConfig columnConfig = ColumnConfigFactory.createMenuDisableColumnConfig();
        columnConfig.setId(ModelDataPropertyNames.IS_INSTANCE_SAMPLE_COLUMN);
        columnConfig.setHeader("Is shared?");
        columnConfig.setHidden(true);
        columnConfig.setWidth(100);
        return columnConfig;
    }

    public final static ColumnConfig createIsInvalidColumn()
    {
        final ColumnConfig columnConfig = ColumnConfigFactory.createMenuDisableColumnConfig();
        columnConfig.setId(ModelDataPropertyNames.IS_INVALID);
        columnConfig.setHeader("Is invalid?");
        columnConfig.setWidth(100);
        columnConfig.setHidden(true);
        return columnConfig;
    }

    public final static ColumnConfig createExperimentColumn()
    {
        final ColumnConfig columnConfig = ColumnConfigFactory.createMenuDisableColumnConfig();
        columnConfig.setId(ModelDataPropertyNames.EXPERIMENT);
        columnConfig.setHeader("Experiment");
        columnConfig.setWidth(100);
        return columnConfig;
    }
}