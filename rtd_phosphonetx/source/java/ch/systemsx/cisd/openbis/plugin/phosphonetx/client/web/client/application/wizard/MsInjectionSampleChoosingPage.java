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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.wizard;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.layout.RowData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.wizard.WizardPage;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;

/**
 * Wizard for guiding the user to annotate an MS_INJECTION sample.
 *
 * @author Franz-Josef Elmer
 */
public class MsInjectionSampleChoosingPage extends WizardPage<MsInjectionSampleAnnotationModel>
{
    private final IViewContext<IPhosphoNetXClientServiceAsync> viewContext;
    
    private ParentlessMsInjectionSampleGrid sampleGrid;

    public MsInjectionSampleChoosingPage(IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            MsInjectionSampleAnnotationModel model)
    {
        super(viewContext, MsInjectionAnnotationWizardState.MS_INJECTION_SAMPLE_CHOOSING, model);
        this.viewContext = viewContext;
        setLeftContentBy(new Html(
                "MS data are added to openBIS in an automated process. "
                        + "The corresponding data sets are associated with samples of type <tt>MS_INJECTION</tt>."
                        + "<p>Before MS data of such samples can be processed in a protein identification pipeline "
                        + "they have to be <b>annotated</b>. In the terminology of openBIS this means: "
                        + "An <tt>MS_INJECTION</tt> sample is linked to biological sample where "
                        + "the biological sample is the parent and the <tt>MS_INJECTION</tt> sample is the child. "
                        + "The biological sample has all annotations. "
                        + "They define the scientific context of proteins found. "
                        + "<p>This wizard helps you add these important annotations to openBIS."));
    }

    @Override
    public void init()
    {
        addToRightContent(new Label(
                "Please choose one or more MS_INJECTION samples to be annotated:"), new RowData(1,
                -1, new Margins(10)));

        sampleGrid = new ParentlessMsInjectionSampleGrid(viewContext);
        sampleGrid.addGridSelectionChangeListener(new Listener<SelectionChangedEvent<ModelData>>()
            {
                public void handleEvent(SelectionChangedEvent<ModelData> se)
                {
                    List<ModelData> selection = se.getSelection();
                    boolean enabled = selection.size() > 0;
                    enableNextButton(enabled);
                }

            });
        addToRightContent(sampleGrid, new RowData(1, 500, new Margins(20, 10, 10, 10)));
    }

    @Override
    public void deactivate()
    {
        List<BaseEntityModel<TableModelRowWithObject<Sample>>> selectedItems =
                sampleGrid.getSelectedItems();
        ArrayList<Sample> samples = new ArrayList<Sample>();
        for (BaseEntityModel<TableModelRowWithObject<Sample>> item : selectedItems)
        {
            samples.add(item.getBaseObject().getObjectOrNull());
        }
        model.setSelectedMsInjectionSample(samples);
    }

    @Override
    public void destroy()
    {
        sampleGrid.dispose();
    }
    
    

}
