/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application.wizard;

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.layout.RowData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.wizard.WizardPage;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.IPhosphoNetXClientServiceAsync;

/**
 * Wizard for guiding the user to annotate an MS_INJECTION sample.
 *
 * @author Franz-Josef Elmer
 */
public class BiologicalSampleChoosingPage extends WizardPage<MsInjectionSampleAnnotationModel>
{
    private final IViewContext<IPhosphoNetXClientServiceAsync> viewContext;
    
    private BiologicalSampleGrid sampleGrid;

    public BiologicalSampleChoosingPage(IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            MsInjectionSampleAnnotationModel model)
    {
        super(viewContext, MsInjectionAnnotationWizardState.BIOLOGICAL_SAMPLE_CHOOSING, model);
        this.viewContext = viewContext;
        setLeftContentByDictionary();
    }

    @Override
    public void init()
    {
        addToRightContent(
                new Label(
                        "Please choose a biological sample as annotation of the MS_INJECTION samples selected previously:"),
                new RowData(1, -1, new Margins(10)));

        sampleGrid = new BiologicalSampleGrid(viewContext);
        sampleGrid.addGridSelectionChangeListener(new Listener<SelectionChangedEvent<ModelData>>()
            {
                @Override
                public void handleEvent(SelectionChangedEvent<ModelData> se)
                {
                    List<ModelData> selection = se.getSelection();
                    boolean enabled = selection.size() == 1;
                    enableNextButton(enabled);
                }

            });
        addToRightContent(sampleGrid, new RowData(1, 400, new Margins(20, 10, 10, 10)));
    }

    @Override
    public void deactivate()
    {
        List<BaseEntityModel<TableModelRowWithObject<Sample>>> selectedItems =
                sampleGrid.getSelectedItems();
        if (selectedItems.isEmpty() == false)
        {
            model.setBiologicalSample(selectedItems.get(0).getBaseObject().getObjectOrNull());
        }
    }

    @Override
    public void destroy()
    {
        sampleGrid.dispose();
    }
    
    

}
