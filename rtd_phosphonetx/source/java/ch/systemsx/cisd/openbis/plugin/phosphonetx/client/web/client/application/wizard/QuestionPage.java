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

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.RowData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.wizard.WizardPage;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class QuestionPage extends WizardPage<MsInjectionSampleAnnotationModel>
{

    private Radio chooseSampleRadioButton;
    private RadioGroup radioGroup;

    public QuestionPage(IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            MsInjectionSampleAnnotationModel model)
    {
        super(viewContext, MsInjectionAnnotationWizardState.CHOOSE_OR_CREATE_QUESTION, model);
        setLeftContentBy(new Html(
                "Annotating the <tt>MS_INJECTION</tt> samples you have chosen means to link them to a <b>biological sample</b>. "
                        + "You can choose an existing biological sample or you can create a new one."));
        radioGroup = new RadioGroup("What do you want to annotate your samples?");
        radioGroup.setFieldLabel("choose");
        chooseSampleRadioButton = new Radio();
        chooseSampleRadioButton.setBoxLabel("I want to choose an existing biological sample.");
        chooseSampleRadioButton.setValue(true);
        radioGroup.add(chooseSampleRadioButton);
        Radio createSampleRadioButton = new Radio();
        createSampleRadioButton.setBoxLabel("I want to create a new biological sample.");
        radioGroup.add(createSampleRadioButton);
        addToRightContent(radioGroup, new RowData(1, -1, new Margins(10)));
        enableNextButton(true);
    }

    @Override
    public void deactivate()
    {
        model.setChooseBiologicalSampleFlag(radioGroup.getValue().equals(chooseSampleRadioButton));
    }
    

}
