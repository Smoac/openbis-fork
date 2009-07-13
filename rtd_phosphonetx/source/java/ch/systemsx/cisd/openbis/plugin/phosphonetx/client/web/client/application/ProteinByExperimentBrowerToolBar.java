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
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntityListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;

/**
 * @author Franz-Josef Elmer
 */
class ProteinByExperimentBrowerToolBar extends ToolBar
{
    private static final String FDR_COMBO_BOX_PROPERTY = "FDR";
    
    private static final class FalseDiscoveryRate extends BaseModelData
    {
        private static final long serialVersionUID = 1L;
        
        private final double falseDiscoveryRate;

        FalseDiscoveryRate(double falseDiscoveryRate)
        {
            this.falseDiscoveryRate = falseDiscoveryRate;
            set(FDR_COMBO_BOX_PROPERTY, toString());
        }

        public final double getFalseDiscoveryRate()
        {
            return falseDiscoveryRate;
        }

        @Override
        public String toString()
        {
            return Integer.toString((int) (100 * falseDiscoveryRate)) + "%";
        }
    }
    
    private final ComboBox<FalseDiscoveryRate> fdrComboBox;
    private final ExperimentChooserFieldAdaptor chooser;
    
    private ProteinByExperimentBrowserGrid browserGrid;
    private Experiment experiment;

    ProteinByExperimentBrowerToolBar(IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        setBorders(true);
        add(new LabelToolItem(viewContext.getMessage(Dict.SELECTED_EXPERIMENT_LABEL)
                + GenericConstants.LABEL_SEPARATOR));
        chooser = ExperimentChooserField.create("", false, null, viewContext.getCommonViewContext());
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
        add(new AdapterToolItem(chooser.getChooseButton()));
        
        add(new LabelToolItem(viewContext.getMessage(Dict.FDR_FILTER_LABEL)));
        fdrComboBox = new ComboBox<FalseDiscoveryRate>();
        ListStore<FalseDiscoveryRate> listStore = new ListStore<FalseDiscoveryRate>();
        FalseDiscoveryRate fdr0 = new FalseDiscoveryRate(0);
        listStore.add(fdr0);
        listStore.add(new FalseDiscoveryRate(0.01));
        listStore.add(new FalseDiscoveryRate(0.02));
        listStore.add(new FalseDiscoveryRate(0.03));
        listStore.add(new FalseDiscoveryRate(0.05));
        listStore.add(new FalseDiscoveryRate(0.1));
        listStore.add(new FalseDiscoveryRate(0.2));
        fdrComboBox.setStore(listStore);
        fdrComboBox.setDisplayField(FDR_COMBO_BOX_PROPERTY);
        fdrComboBox.setValue(fdr0);
        fdrComboBox.addSelectionChangedListener(new SelectionChangedListener<FalseDiscoveryRate>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent<FalseDiscoveryRate> se)
                {
                    update();
                }
            });
        add(new AdapterToolItem(fdrComboBox));
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
            double falseDiscoveryRate = 0;
            List<FalseDiscoveryRate> selection = fdrComboBox.getSelection();
            if (selection.isEmpty() == false)
            {
                falseDiscoveryRate = selection.get(0).getFalseDiscoveryRate();
            }
            browserGrid.update(TechId.create(experiment), falseDiscoveryRate);
        }
    }
}
