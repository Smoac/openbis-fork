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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleModel;

/**
 * Defines the common columns of sample grid/table.
 * 
 * @author Izabela Adamczyk
 */
public final class CommonColumns
{
    private List<ColumnConfig> columns;

    public CommonColumns()
    {
        columns = new ArrayList<ColumnConfig>();
        define();

    }

    public List<ColumnConfig> getColumns()
    {
        return columns;
    }

    private void define()
    {
        columns.add(createCodeColumn());
        columns.add(createIdentifierColumn());
        columns.add(createIsSharedColumn());
        columns.add(createRegistratorColumn());
        columns.add(createRegistionDateColumn());
        columns.add(createIsInvalidColumn());
        columns.add(createExperimentColumn());
        disableColumnMenu();
    }

    private void disableColumnMenu()
    {
        for (ColumnConfig columnConfig : columns)
        {
            columnConfig.setMenuDisabled(true);
        }
    }

    private final ColumnConfig createCodeColumn()
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setId(SampleModel.SAMPLE_CODE);
        columnConfig.setHeader("Code");
        columnConfig.setWidth(100);
        columnConfig.setRenderer(new SampleRenderer());
        return columnConfig;
    }

    private final ColumnConfig createIdentifierColumn()
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setId(SampleModel.SAMPLE_IDENTIFIER);
        columnConfig.setHeader("Identifier");
        columnConfig.setHidden(true);
        columnConfig.setWidth(150);
        return columnConfig;
    }

    private final ColumnConfig createIsSharedColumn()
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setId(SampleModel.IS_INSTANCE_SAMPLE_COLUMN);
        columnConfig.setHeader("Is shared?");
        columnConfig.setHidden(true);
        columnConfig.setWidth(100);
        return columnConfig;
    }

    private final ColumnConfig createRegistratorColumn()
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setId(SampleModel.REGISTRATOR);
        columnConfig.setHeader("Registrator");
        columnConfig.setWidth(100);
        columnConfig.setHidden(true);
        return columnConfig;
    }

    private final ColumnConfig createRegistionDateColumn()
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setId(SampleModel.REGISTRATION_DATE);
        columnConfig.setHeader("Registration Date");
        columnConfig.setWidth(100);
        columnConfig.setHidden(true);
        return columnConfig;
    }

    private final ColumnConfig createIsInvalidColumn()
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setId(SampleModel.IS_INVALID);
        columnConfig.setHeader("Is invalid?");
        columnConfig.setWidth(100);
        columnConfig.setHidden(true);
        return columnConfig;
    }

    private ColumnConfig createExperimentColumn()
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setId(SampleModel.EXPERIMENT);
        columnConfig.setHeader("Experiment");
        columnConfig.setWidth(100);
        return columnConfig;
    }
}