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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseListLoadConfig;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExternalDataModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.CommonColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.SampleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * The <i>generic</i> sample viewer.
 * 
 * @author Christian Ribeaud
 */
public final class GenericSampleViewer extends Dialog
{
    private final SampleGeneration sampleGeneration;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private Grid<SampleModel> partOfSamplesGrid;

    private Grid<ExternalDataModel> externalDataGrid;

    public GenericSampleViewer(final String heading,
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final SampleGeneration sampleGeneration)
    {
        this.sampleGeneration = sampleGeneration;
        this.viewContext = viewContext;
        init(heading);
        // Left panel
        add(createLeftPanel(), createLeftBorderLayoutData());
        // Right panel
        add(createRightPanel(), createRightBorderLayoutData());
    }

    private final static BorderLayoutData createRightBorderLayoutData()
    {
        final BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
        return data;
    }

    private final Component createRightPanel()
    {
        final VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setSpacing(20);
        // 'Part of' samples
        ContentPanel panel = new ContentPanel();
        panel.setLayout(new FitLayout());
        partOfSamplesGrid =
                new Grid<SampleModel>(new ListStore<SampleModel>(),
                        createPartOfSamplesColumnModel());
        partOfSamplesGrid.setLoadMask(true);
        panel.add(partOfSamplesGrid);
        // External data
        panel = new ContentPanel();
        panel.setLayout(new FitLayout());
        // externalDataGrid =
        // new Grid<ExternalDataModel>(new ListStore<ExternalData>(),
        // createExternalDataColumnModel());
        externalDataGrid.setLoadMask(true);
        panel.add(externalDataGrid);
        return verticalPanel;
    }

    private final <T> ListLoader<BaseListLoadConfig> createListLoader(
            final RpcProxy<BaseListLoadConfig, BaseListLoadResult<T>> rpcProxy)
    {
        final BaseListLoader<BaseListLoadConfig, BaseListLoadResult<T>> baseListLoader =
                new BaseListLoader<BaseListLoadConfig, BaseListLoadResult<T>>(rpcProxy);
        return baseListLoader;
    }

    private final RpcProxy<BaseListLoadConfig, BaseListLoadResult<SampleModel>> createRpcProxyForPartOfSamples()
    {
        return new RpcProxy<BaseListLoadConfig, BaseListLoadResult<SampleModel>>()
            {

                //
                // RpcProxy
                //

                @Override
                public final void load(final BaseListLoadConfig loadConfig,
                        final AsyncCallback<BaseListLoadResult<SampleModel>> callback)
                {
                    final ListSampleCriteria sampleCriteria = new ListSampleCriteria();
                    sampleCriteria.setContainer(sampleGeneration.getGenerator());
                    viewContext.getService().listSamples(sampleCriteria,
                            new ArrayList<PropertyType>(),
                            new ListSamplesCallback(viewContext, callback));
                }
            };
    }

    private final static ListStore<SampleModel> createListStore(final ListLoader<?> loader)
    {
        return new ListStore<SampleModel>(loader);
    }

    private final static ColumnModel createPartOfSamplesColumnModel()
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(CommonColumns.createCodeColumn());
        return new ColumnModel(configs);
    }

    private final void init(final String heading)
    {
        setHeading(heading);
        setButtons(OK);
        setScrollMode(Scroll.AUTO);
        setWidth(800);
        setHeight(600);
        setBodyStyle("backgroundColor: #ffffff;");
        setHideOnButtonClick(true);
        setLayout(new BorderLayout());
        setBodyBorder(false);
        setInsetBorder(false);
    }

    private final static BorderLayoutData createLeftBorderLayoutData()
    {
        final BorderLayoutData data = new BorderLayoutData(LayoutRegion.WEST, 300, 100, 500);
        data.setMargins(new Margins(0, 5, 0, 0));
        data.setSplit(true);
        data.setCollapsible(true);
        data.setFloatable(true);
        return data;
    }

    private final static Map<String, Object> createProperties(
            final IMessageProvider messageProvider, final SampleGeneration sampleGeneration)
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        final Sample sample = sampleGeneration.getGenerator();
        final SampleType sampleType = sample.getSampleType();
        final Invalidation invalidation = sample.getInvalidation();
        final Sample[] generated = sampleGeneration.getGenerated();
        properties.put(messageProvider.getMessage("sample"), sample.getCode());
        properties.put(messageProvider.getMessage("sample_type"), sampleType);
        properties.put(messageProvider.getMessage("registrator"), sample.getRegistrator());
        properties.put(messageProvider.getMessage("registration_date"), sample
                .getRegistrationDate());
        if (generated.length > 0)
        {
            properties.put(messageProvider.getMessage("generated_samples"), generated);
        }
        if (invalidation != null)
        {
            properties.put(messageProvider.getMessage("invalidation"), invalidation);
        }
        Sample generatedFrom = sample;
        for (int i = 0; i < sampleType.getGeneratedFromHierarchyDepth() && generatedFrom != null; i++)
        {
            generatedFrom = generatedFrom.getGeneratedFrom();
            properties.put(messageProvider.getMessage("generated_from", i + 1), generatedFrom);
        }
        for (final SampleProperty property : sample.getProperties())
        {
            final String simpleCode =
                    property.getEntityTypePropertyType().getPropertyType().getLabel();
            properties.put(simpleCode, property);
        }
        return properties;
    }

    private final Component createLeftPanel()
    {
        final ContentPanel panel = new ContentPanel();
        panel.setHeading(viewContext.getMessageProvider().getMessage("sample_properties"));
        panel.add(createPropertyGrid());
        return panel;
    }

    private final PropertyGrid createPropertyGrid()
    {
        final IMessageProvider messageProvider = viewContext.getMessageProvider();
        final Map<String, Object> properties = createProperties(messageProvider, sampleGeneration);
        final PropertyGrid propertyGrid = new PropertyGrid(messageProvider, properties.size());
        propertyGrid.registerPropertyValueRenderer(Person.class, PropertyValueRenderers
                .createPersonPropertyValueRenderer(messageProvider));
        propertyGrid.registerPropertyValueRenderer(SampleType.class, PropertyValueRenderers
                .createSampleTypePropertyValueRenderer(messageProvider));
        propertyGrid.registerPropertyValueRenderer(Sample.class, PropertyValueRenderers
                .createSamplePropertyValueRenderer(messageProvider, true));
        propertyGrid.registerPropertyValueRenderer(Invalidation.class, PropertyValueRenderers
                .createInvalidationPropertyValueRenderer(messageProvider));
        propertyGrid.registerPropertyValueRenderer(SampleProperty.class, PropertyValueRenderers
                .createSamplePropertyPropertyValueRenderer(messageProvider));
        propertyGrid.setProperties(properties);
        return propertyGrid;
    }

    //
    // Dialog
    //

    @Override
    public final void show()
    {
        super.show();
        final ListLoader<BaseListLoadConfig> loader =
                createListLoader(createRpcProxyForPartOfSamples());
        partOfSamplesGrid.reconfigure(createListStore(loader), createPartOfSamplesColumnModel());
        loader.load();
    }

    //
    // Helper classes
    //

    final class ListSamplesCallback extends AbstractAsyncCallback<List<Sample>>
    {
        private final AsyncCallback<BaseListLoadResult<SampleModel>> delegate;

        ListSamplesCallback(final IViewContext<IGenericClientServiceAsync> viewContext,
                final AsyncCallback<BaseListLoadResult<SampleModel>> callback)
        {
            super(viewContext);
            this.delegate = callback;
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
            final List<SampleModel> sampleModels = SampleBrowserGrid.asSampleModels(result);
            final BaseListLoadResult<SampleModel> baseListLoadResult =
                    new BaseListLoadResult<SampleModel>(sampleModels);
            delegate.onSuccess(baseListLoadResult);
        }
    }
}