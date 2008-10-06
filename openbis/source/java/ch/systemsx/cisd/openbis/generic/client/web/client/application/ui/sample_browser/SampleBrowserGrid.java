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

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.i18n.client.DateTimeFormat;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

class SampleBrowserGrid extends LayoutContainer
{

    private final GenericViewContext viewContext;

    public SampleBrowserGrid(GenericViewContext viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FitLayout());
    }

    private void display(List<Sample> samples, ColumnModel columnModel, String header)
    {
        ListStore<SampleModel> sampleStore = new ListStore<SampleModel>();
        for (Sample s : samples)
        {
            sampleStore.add(new SampleModel(s));
        }
        final ContentPanel cp = new ContentPanel();
        cp.setHeading(header);
        cp.setLayout(new FitLayout());

        Grid<SampleModel> grid = new Grid<SampleModel>(sampleStore, columnModel);
        cp.add(grid);

        removeAll();
        add(cp);
        layout();

    }

    private String createHeader(SampleType sampleType, String selectedGroupCode, boolean showGroup,
            boolean showInstance)
    {
        StringBuilder sb = new StringBuilder("Samples");
        sb.append(" ");
        sb.append("of type");
        sb.append(" ");
        sb.append(sampleType.getCode());
        if (showGroup)
        {
            sb.append(" ");
            sb.append("from group");
            sb.append(" ");
            sb.append(selectedGroupCode);
        }
        if (showInstance)
        {
            if (showGroup)
            {
                sb.append(" ");
                sb.append("and");
            }
            sb.append(" ");
            sb.append("from instance level");
        }
        return sb.toString();
    }

    public void refresh(final SampleType sampleType, final String selectedGroupCode,
            final boolean showGroup, final boolean showInstance)
    {
        removeAll();
        add(new Text("data loading..."));
        viewContext.getService().listSamples(sampleType, selectedGroupCode, showGroup,
                showInstance, new AbstractAsyncCallback<List<Sample>>(viewContext)
                    {
                        @Override
                        public void process(List<Sample> samples)
                        {
                            display(samples, createColumnModel(sampleType), createHeader(
                                    sampleType, selectedGroupCode, showGroup, showInstance));
                        }

                    });
    }

    ColumnModel createColumnModel(SampleType type)
    {
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(createCodeColumn());
        configs.add(createIdentifierColumn());
        configs.add(createAssignedToIdentifierColumn());
        configs.add(createRegistratorColumn());
        configs.add(createRegistionDateColumn());
        addGeneratedFromParentColumns(configs, 1, type.getGeneratedFromHierarchyDepth());
        addContainerParentColumns(configs, 1, type.getPartOfHierarchyDepth());
        return new ColumnModel(configs);
    }

    private ColumnConfig createIdentifierColumn()
    {
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setId(SampleModel.SAMPLE_IDENTIFIER);
        columnConfig.setHeader("Identifier");
        columnConfig.setHidden(true);
        columnConfig.setWidth(150);
        return columnConfig;
    }

    private ColumnConfig createCodeColumn()
    {
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setId(SampleModel.SAMPLE_CODE);
        columnConfig.setHeader("Code");
        columnConfig.setWidth(100);
        return columnConfig;
    }

    private ColumnConfig createAssignedToIdentifierColumn()
    {
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setId(SampleModel.ATTACHED_TO_IDENTIFIER);
        columnConfig.setHeader("Attached to");
        columnConfig.setWidth(100);
        return columnConfig;
    }

    private ColumnConfig createRegistratorColumn()
    {
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setId(SampleModel.REGISTRATOR);
        columnConfig.setHeader("Registrator");
        columnConfig.setWidth(100);
        columnConfig.setRenderer(new PersonRenderer());
        columnConfig.setHidden(true);
        return columnConfig;
    }

    private ColumnConfig createRegistionDateColumn()
    {
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setId(SampleModel.REGISTRATION_DATE);
        columnConfig.setHeader("Registration Date");
        columnConfig.setWidth(100);
        columnConfig.setDateTimeFormat(DateTimeFormat.getShortDateFormat());
        columnConfig.setHidden(true);
        return columnConfig;
    }

    private void addGeneratedFromParentColumns(List<ColumnConfig> configs, int dep, int maxDep)
    {
        if (dep <= maxDep)
        {
            configs.add(createGeneratedFromParentColumn(dep));
            addGeneratedFromParentColumns(configs, dep + 1, maxDep);
        }
    }

    private void addContainerParentColumns(List<ColumnConfig> configs, int dep, int maxDep)
    {
        if (dep <= maxDep)
        {
            configs.add(createContainerParentColumn(dep));
            addContainerParentColumns(configs, dep + 1, maxDep);
        }
    }

    private ColumnConfig createGeneratedFromParentColumn(int i)
    {
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setId(SampleModel.GENERATED_FROM_PARENT_PREFIX + i);
        columnConfig.setHeader("Parent (gener.) " + i);
        columnConfig.setWidth(150);
        return columnConfig;
    }

    private ColumnConfig createContainerParentColumn(int i)
    {
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setId(SampleModel.CONTAINER_PARENT_PREFIX + i);
        columnConfig.setHeader("Parent (cont.) " + i);
        columnConfig.setWidth(150);
        return columnConfig;
    }

}