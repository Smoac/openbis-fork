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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntityListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AbundanceColumnDefinition;

/**
 * @author Franz-Josef Elmer
 */
class ProteinByExperimentBrowerToolBar extends ToolBar
{
    private static final AggregateFunction DEFAULT_AGGREGATE_FUNCTION = AggregateFunction.MEAN;

    private abstract static class SimpleModel<T> extends BaseModelData
    {
        private static final long serialVersionUID = 1L;

        private static final String PROPERTY = "property";

        private final T object;

        SimpleModel(T object)
        {
            this.object = object;
            set(PROPERTY, render(object));
        }

        public final T getObject()
        {
            return object;
        }

        protected abstract String render(T modelObject);
    }

    private static final class FalseDiscoveryRateModel extends SimpleModel<Double>
    {
        private static final long serialVersionUID = 1L;

        FalseDiscoveryRateModel(Double falseDiscoveryRate)
        {
            super(falseDiscoveryRate);
        }

        @Override
        protected String render(Double modelObject)
        {
            return Integer.toString((int) (100 * modelObject)) + "%";
        }
    }

    private static final class AggregateFunctionModel extends SimpleModel<AggregateFunction>
    {
        private static final long serialVersionUID = 1L;

        AggregateFunctionModel(AggregateFunction aggregateFunction)
        {
            super(aggregateFunction);
        }

        @Override
        protected String render(AggregateFunction modelObject)
        {
            return modelObject.getLabel();
        }
    }

    private final IViewContext<IPhosphoNetXClientServiceAsync> viewContext;

    private final ComboBox<FalseDiscoveryRateModel> fdrComboBox;

    private final ExperimentChooserFieldAdaptor chooser;

    private final ComboBox<AggregateFunctionModel> aggregateFunctionComboBox;

    private ProteinByExperimentBrowserGrid browserGrid;

    private Experiment experiment;

    ProteinByExperimentBrowerToolBar(IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        setBorders(true);
        add(new LabelToolItem(viewContext.getMessage(Dict.SELECTED_EXPERIMENT_LABEL)
                + GenericConstants.LABEL_SEPARATOR));
        chooser =
                ExperimentChooserField.create("", false, null, viewContext.getCommonViewContext());
        ExperimentChooserField chooserField = chooser.getChooserField();
        chooserField.setReadOnly(true);
        chooserField.addChosenEntityListener(new IChosenEntityListener<Experiment>()
            {
                public void entityChosen(Experiment entity)
                {
                    if (entity != null)
                    {
                        experiment = entity;
                        update();
                    }
                }
            });
        add(new AdapterToolItem(chooserField));

        SelectionChangedListener<ModelData> changedListener =
                new SelectionChangedListener<ModelData>()
                    {
                        @Override
                        public void selectionChanged(SelectionChangedEvent<ModelData> se)
                        {
                            update();
                        }
                    };
        add(new LabelToolItem(viewContext.getMessage(Dict.FDR_FILTER_LABEL) + ":"));
        fdrComboBox = createFDRComboBox(changedListener);
        add(new AdapterToolItem(fdrComboBox));
        add(new LabelToolItem(viewContext.getMessage(Dict.AGGREGATE_FUNCTION_LABEL) + ":"));
        aggregateFunctionComboBox = createAggregateFunctionComboBox(changedListener);
        add(new AdapterToolItem(aggregateFunctionComboBox));
    }

    private ComboBox<FalseDiscoveryRateModel> createFDRComboBox(
            SelectionChangedListener<ModelData> changedListener)
    {
        ComboBox<FalseDiscoveryRateModel> comboBox = new ComboBox<FalseDiscoveryRateModel>();
        ListStore<FalseDiscoveryRateModel> listStore = new ListStore<FalseDiscoveryRateModel>();
        FalseDiscoveryRateModel fdr0 = new FalseDiscoveryRateModel(0.0);
        listStore.add(fdr0);
        listStore.add(new FalseDiscoveryRateModel(0.01));
        listStore.add(new FalseDiscoveryRateModel(0.02));
        listStore.add(new FalseDiscoveryRateModel(0.03));
        listStore.add(new FalseDiscoveryRateModel(0.05));
        listStore.add(new FalseDiscoveryRateModel(0.1));
        listStore.add(new FalseDiscoveryRateModel(0.2));
        comboBox.setStore(listStore);
        comboBox.setDisplayField(SimpleModel.PROPERTY);
        comboBox.setValue(fdr0);
        comboBox.addSelectionChangedListener(changedListener);
        return comboBox;
    }

    private ComboBox<AggregateFunctionModel> createAggregateFunctionComboBox(
            SelectionChangedListener<ModelData> changedListener)
    {
        ComboBox<AggregateFunctionModel> comboBox = new ComboBox<AggregateFunctionModel>();
        ListStore<AggregateFunctionModel> store = new ListStore<AggregateFunctionModel>();
        AggregateFunctionModel defaultModel = null;
        for (AggregateFunction aggregateFunction : AggregateFunction.values())
        {
            AggregateFunctionModel model = new AggregateFunctionModel(aggregateFunction);
            store.add(model);
            if (aggregateFunction == DEFAULT_AGGREGATE_FUNCTION)
            {
                defaultModel = model;
            }
        }
        comboBox.setStore(store);
        comboBox.setDisplayField(SimpleModel.PROPERTY);
        comboBox.setValue(defaultModel);
        comboBox.addSelectionChangedListener(changedListener);
        return comboBox;
    }

    Experiment getExperimentOrNull()
    {
        return experiment;
    }

    void setBrowserGrid(ProteinByExperimentBrowserGrid browserGrid)
    {
        this.browserGrid = browserGrid;
    }

    private void update()
    {
        if (experiment != null)
        {
            double falseDiscoveryRate = getSelection(fdrComboBox, 0.0);
            AggregateFunction aggregateFunction =
                    getSelection(aggregateFunctionComboBox, DEFAULT_AGGREGATE_FUNCTION);
            TechId experimentID = TechId.create(experiment);
            AsyncCallback<List<AbundanceColumnDefinition>> callback =
                    new AbundanceColumnDefinitionsCallback(viewContext, browserGrid, experimentID,
                            falseDiscoveryRate, aggregateFunction);
            viewContext.getService().getAbundanceColumnDefinitionsForProteinByExperiment(
                    experimentID, callback);
        }
    }

    private <T> T getSelection(ComboBox<? extends SimpleModel<T>> comboBox, T defaultValue)
    {
        List<? extends SimpleModel<T>> selection = comboBox.getSelection();
        return selection.isEmpty() ? defaultValue : selection.get(0).getObject();
    }

    private static final class AbundanceColumnDefinitionsCallback extends
            AbstractAsyncCallback<List<AbundanceColumnDefinition>>
    {
        private final ProteinByExperimentBrowserGrid browserGrid;

        private final TechId experimentID;

        private final double falseDiscoveryRate;

        private final AggregateFunction aggregateFunction;

        public AbundanceColumnDefinitionsCallback(IViewContext<?> viewContext,
                ProteinByExperimentBrowserGrid browserGrid, TechId experimentID,
                double falseDiscoveryRate, AggregateFunction aggregateFunction)
        {
            super(viewContext);
            this.browserGrid = browserGrid;
            this.experimentID = experimentID;
            this.falseDiscoveryRate = falseDiscoveryRate;
            this.aggregateFunction = aggregateFunction;
        }

        @Override
        protected void process(List<AbundanceColumnDefinition> result)
        {
            browserGrid.update(experimentID, falseDiscoveryRate, aggregateFunction, result);
        }

    }
}
