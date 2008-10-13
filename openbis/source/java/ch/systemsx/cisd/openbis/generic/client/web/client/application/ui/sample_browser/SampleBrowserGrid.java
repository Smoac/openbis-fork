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

import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleTypePropertyType;

/**
 * The samples browser.
 * 
 * @author Christian Ribeaud
 */
class SampleBrowserGrid extends LayoutContainer
{
    private final GenericViewContext viewContext;

    private ContentPanel contentPanel;

    private Grid<SampleModel> grid;

    public SampleBrowserGrid(final GenericViewContext viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FitLayout());
    }

    private void display(final SampleType sampleType, final String selectedGroupCode,
            final boolean showGroup, final boolean showInstance)
    {
        final RpcProxy<Object, List<SampleModel>> proxy = new RpcProxy<Object, List<SampleModel>>()
            {
                //
                // RpcProxy
                //

                @Override
                public final void load(final Object loadConfig,
                        final AsyncCallback<List<SampleModel>> callback)
                {
                    viewContext.getService().listSamples(sampleType, selectedGroupCode, showGroup,
                            showInstance, new ListSamplesCallback(viewContext, callback));
                }
            };
        final ColumnModel columnModel = createColumnModel(sampleType, sampleType);
        final String header = createHeader(sampleType, selectedGroupCode, showGroup, showInstance);
        final ListLoader<?> loader = createListLoader(proxy);
        final ListStore<SampleModel> sampleStore = new ListStore<SampleModel>(loader);
        getContentPanel().setHeading(header);
        createOrReconfigureGrid(columnModel, sampleStore);
        loader.load();
        layout();
    }

    @SuppressWarnings("unchecked")
    private final static ListLoader<?> createListLoader(
            final RpcProxy<Object, List<SampleModel>> proxy)
    {
        return new BaseListLoader(proxy);
    }

    private final void createOrReconfigureGrid(final ColumnModel columnModel,
            final ListStore<SampleModel> sampleStore)
    {
        if (grid == null)
        {
            grid = new Grid<SampleModel>(sampleStore, columnModel);
            grid.setLoadMask(true);
            getContentPanel().add(grid);
        } else
        {
            grid.reconfigure(sampleStore, columnModel);
        }
    }

    private final ContentPanel getContentPanel()
    {
        if (contentPanel == null)
        {
            contentPanel = new ContentPanel();
            contentPanel.setLayout(new FitLayout());
            add(contentPanel);
        }
        return contentPanel;
    }

    private String createHeader(final SampleType sampleType, final String selectedGroupCode,
            final boolean showGroup, final boolean showInstance)
    {
        final StringBuilder sb = new StringBuilder("Samples");
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

    public final void refresh(final SampleType sampleType, final String selectedGroupCode,
            final boolean showGroup, final boolean showInstance)
    {
        display(sampleType, selectedGroupCode, showGroup, showInstance);
    }

    private final static ColumnModel createColumnModel(final SampleType type,
            final SampleType sampleType)
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(createCodeColumn());
        configs.add(createIdentifierColumn());
        configs.add(createAssignedToIdentifierColumn());
        configs.add(createRegistratorColumn());
        configs.add(createRegistionDateColumn());
        addGeneratedFromParentColumns(configs, 1, type.getGeneratedFromHierarchyDepth());
        addContainerParentColumns(configs, 1, type.getPartOfHierarchyDepth());
        addPropertyColumns(configs, sampleType);
        return new ColumnModel(configs);
    }

    private final static ColumnConfig createIdentifierColumn()
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setMenuDisabled(true);
        columnConfig.setId(SampleModel.SAMPLE_IDENTIFIER);
        columnConfig.setHeader("Identifier");
        columnConfig.setHidden(true);
        columnConfig.setWidth(150);
        return columnConfig;
    }

    private final static ColumnConfig createCodeColumn()
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setMenuDisabled(true);
        columnConfig.setId(SampleModel.SAMPLE_CODE);
        columnConfig.setHeader("Code");
        columnConfig.setWidth(100);
        return columnConfig;
    }

    private final static ColumnConfig createAssignedToIdentifierColumn()
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setMenuDisabled(true);
        columnConfig.setId(SampleModel.ATTACHED_TO_IDENTIFIER);
        columnConfig.setHeader("Attached to");
        columnConfig.setWidth(100);
        return columnConfig;
    }

    private final static ColumnConfig createRegistratorColumn()
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setMenuDisabled(true);
        columnConfig.setId(SampleModel.REGISTRATOR);
        columnConfig.setHeader("Registrator");
        columnConfig.setWidth(100);
        columnConfig.setRenderer(new PersonRenderer());
        columnConfig.setHidden(true);
        return columnConfig;
    }

    private final static ColumnConfig createRegistionDateColumn()
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setMenuDisabled(true);
        columnConfig.setId(SampleModel.REGISTRATION_DATE);
        columnConfig.setHeader("Registration Date");
        columnConfig.setWidth(100);
        columnConfig.setDateTimeFormat(DateTimeFormat.getShortDateFormat());
        columnConfig.setHidden(true);
        return columnConfig;
    }

    private final static void addGeneratedFromParentColumns(final List<ColumnConfig> configs,
            final int dep, final int maxDep)
    {
        if (dep <= maxDep)
        {
            configs.add(createGeneratedFromParentColumn(dep));
            addGeneratedFromParentColumns(configs, dep + 1, maxDep);
        }
    }

    private final static void addContainerParentColumns(final List<ColumnConfig> configs,
            final int dep, final int maxDep)
    {
        if (dep <= maxDep)
        {
            configs.add(createContainerParentColumn(dep));
            addContainerParentColumns(configs, dep + 1, maxDep);
        }
    }

    private final static ColumnConfig createGeneratedFromParentColumn(final int i)
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setMenuDisabled(true);
        columnConfig.setId(SampleModel.GENERATED_FROM_PARENT_PREFIX + i);
        columnConfig.setHeader("Parent (gener.) " + i);
        columnConfig.setWidth(150);
        return columnConfig;
    }

    private final static ColumnConfig createContainerParentColumn(final int i)
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setMenuDisabled(true);
        columnConfig.setId(SampleModel.CONTAINER_PARENT_PREFIX + i);
        columnConfig.setHeader("Parent (cont.) " + i);
        columnConfig.setWidth(150);
        return columnConfig;
    }

    private final static void addPropertyColumns(final List<ColumnConfig> configs,
            final SampleType sampleType)
    {
        for (final SampleTypePropertyType stpt : sampleType.getSampleTypePropertyTypes())
        {
            configs.add(createPropertyColumn(stpt.getPropertyType().getCode(),
                    stpt.isDisplayed() == false));
        }
    }

    private final static ColumnConfig createPropertyColumn(final String code, final boolean isHidden)
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setMenuDisabled(true);
        columnConfig.setId(SampleModel.PROPERTY_PREFIX + code);
        columnConfig.setHeader(code);
        columnConfig.setWidth(80);
        columnConfig.setHidden(isHidden);
        return columnConfig;
    }

    private final class ListSamplesCallback extends AbstractAsyncCallback<List<Sample>>
    {
        private final AsyncCallback<List<SampleModel>> delegate;

        ListSamplesCallback(final GenericViewContext viewContext,
                final AsyncCallback<List<SampleModel>> delegate)
        {
            super(viewContext);
            this.delegate = delegate;
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected void finishOnFailure(final Throwable caught)
        {
            delegate.onFailure(caught);
        }

        @Override
        protected final void process(final List<Sample> result)
        {
            final List<SampleModel> sampleModels = new ArrayList<SampleModel>(result.size());
            for (final Sample sample : result)
            {
                sampleModels.add(new SampleModel(sample));
            }
            delegate.onSuccess(sampleModels);
        }
    }
}