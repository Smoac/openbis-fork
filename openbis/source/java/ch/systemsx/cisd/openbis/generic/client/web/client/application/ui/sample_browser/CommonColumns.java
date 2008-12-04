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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.YesNoRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.InvalidableWithCodeRenderer;
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

    private void define()
    {
        columns.add(createHiddenColumn(ModelDataPropertyNames.DATABASE_INSTANCE,
                "database_instance"));
        columns.add(ColumnConfigFactory.createDefaultColumnConfig(messageProvider
                .getMessage("group"), ModelDataPropertyNames.GROUP));
        columns.add(createSampleCodeColumnConfig());
        columns.add(createSampleIdentifierColumn());
        columns.add(createHiddenBooleanColumn(ModelDataPropertyNames.IS_INSTANCE_SAMPLE,
                "is_instance_sample"));
        columns.add(createHiddenRegistratorColumn());
        columns.add(createHiddenRegistrationDateColumnConfig());
        columns.add(createHiddenBooleanColumn(ModelDataPropertyNames.IS_INVALID, "is_invalid"));
        columns.add(ColumnConfigFactory.createDefaultColumnConfig(messageProvider
                .getMessage("project"), ModelDataPropertyNames.PROJECT_FOR_SAMPLE));
        columns.add(ColumnConfigFactory.createDefaultColumnConfig(messageProvider
                .getMessage("experiment"), ModelDataPropertyNames.EXPERIMENT_FOR_SAMPLE));
        columns.add(createHiddenExperimentIdentifierColumn());
    }

    private final ColumnConfig createHiddenRegistrationDateColumnConfig()
    {
        final ColumnConfig columnConfig =
                ColumnConfigFactory.createRegistrationDateColumnConfig(messageProvider);
        columnConfig.setHidden(true);
        return columnConfig;
    }

    private final ColumnConfig createSampleCodeColumnConfig()
    {
        final ColumnConfig codeColumn = ColumnConfigFactory.createCodeColumnConfig(messageProvider);
        codeColumn.setRenderer(new InvalidableWithCodeRenderer());
        return codeColumn;
    }

    private final ColumnConfig createHiddenBooleanColumn(final String id, final String headerKey)
    {
        final ColumnConfig config = createHiddenColumn(id, headerKey);
        config.setRenderer(new YesNoRenderer());
        return config;
    }

    private final ColumnConfig createHiddenColumn(final String id, final String headerKey)
    {
        final ColumnConfig config =
                ColumnConfigFactory.createDefaultColumnConfig(
                        messageProvider.getMessage(headerKey), id);
        config.setHidden(true);
        return config;
    }

    private final ColumnConfig createHiddenRegistratorColumn()
    {
        final ColumnConfig registratorColumn =
                ColumnConfigFactory.createRegistratorColumnConfig(messageProvider);
        registratorColumn.setHidden(true);
        return registratorColumn;
    }

    private final ColumnConfig createSampleIdentifierColumn()
    {
        final ColumnConfig columnConfig =
                ColumnConfigFactory.createDefaultColumnConfig(messageProvider
                        .getMessage("sample_identifier"), ModelDataPropertyNames.SAMPLE_IDENTIFIER);
        columnConfig.setHidden(true);
        columnConfig.setWidth(150);
        return columnConfig;
    }

    private final ColumnConfig createHiddenExperimentIdentifierColumn()
    {
        final ColumnConfig columnConfig =
                ColumnConfigFactory.createDefaultColumnConfig(messageProvider
                        .getMessage("experiment_identifier"),
                        ModelDataPropertyNames.EXPERIMENT_IDENTIFIER_FOR_SAMPLE);
        columnConfig.setHidden(true);
        columnConfig.setWidth(200);
        return columnConfig;
    }

    public final List<ColumnConfig> getColumns()
    {
        return columns;
    }
}