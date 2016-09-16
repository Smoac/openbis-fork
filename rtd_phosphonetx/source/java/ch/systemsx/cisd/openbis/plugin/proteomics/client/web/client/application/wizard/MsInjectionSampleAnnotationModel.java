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

import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application.wizard.MsInjectionAnnotationWizardState.BIOLOGICAL_SAMPLE_CHOOSING;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application.wizard.MsInjectionAnnotationWizardState.BIOLOGICAL_SAMPLE_CREATING;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application.wizard.MsInjectionAnnotationWizardState.CHOOSE_OR_CREATE_QUESTION;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application.wizard.MsInjectionAnnotationWizardState.MS_INJECTION_SAMPLE_CHOOSING;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.VoidAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.wizard.IWizardDataModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.wizard.IWizardState;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.wizard.WizardWorkflowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.IPhosphoNetXClientServiceAsync;

/**
 * @author Franz-Josef Elmer
 */
public class MsInjectionSampleAnnotationModel implements IWizardDataModel
{
    private final IViewContext<IPhosphoNetXClientServiceAsync> context;

    private final WizardWorkflowModel workflowModel;

    private List<Sample> msInjectionSamples = new ArrayList<Sample>();

    private boolean chooseBiologicalSampleFlag;

    private Sample biologicalSample;

    private NewSample newBiologicalSample;

    public MsInjectionSampleAnnotationModel(IViewContext<IPhosphoNetXClientServiceAsync> context)
    {
        this.context = context;
        workflowModel = new WizardWorkflowModel(this);
        workflowModel.addTransition(MS_INJECTION_SAMPLE_CHOOSING, CHOOSE_OR_CREATE_QUESTION);
        workflowModel.addTransition(CHOOSE_OR_CREATE_QUESTION, BIOLOGICAL_SAMPLE_CHOOSING);
        workflowModel.addTransition(CHOOSE_OR_CREATE_QUESTION, BIOLOGICAL_SAMPLE_CREATING);
    }

    @Override
    public WizardWorkflowModel getWorkflow()
    {
        return workflowModel;
    }

    @Override
    public IWizardState determineNextState(IWizardState currentState)
    {
        return chooseBiologicalSampleFlag ? BIOLOGICAL_SAMPLE_CHOOSING : BIOLOGICAL_SAMPLE_CREATING;
    }

    public void setSelectedMsInjectionSample(List<Sample> samples)
    {
        msInjectionSamples = samples;
    }

    public List<Sample> getMsInjectionSamples()
    {
        return msInjectionSamples;
    }

    public void setChooseBiologicalSampleFlag(boolean flag)
    {
        chooseBiologicalSampleFlag = flag;
    }

    public void setBiologicalSample(Sample biologicalSample)
    {
        this.biologicalSample = biologicalSample;
    }

    public void defineBiologicalSample(SampleType sampleType, String identifier,
            String experimentIdentifierOrNull, List<IEntityProperty> properties)
    {
        newBiologicalSample = NewSample.createWithParents(identifier, sampleType, null, null);
        newBiologicalSample.setProperties(properties.toArray(IEntityProperty.EMPTY_ARRAY));
        newBiologicalSample.setExperimentIdentifier(experimentIdentifierOrNull);
    }

    @Override
    public String finish()
    {
        VoidAsyncCallback<Void> callback = new VoidAsyncCallback<Void>(context);
        if (chooseBiologicalSampleFlag)
        {
            context.getService().linkSamples(biologicalSample, msInjectionSamples, callback);
        } else
        {
            context.getService().createAndLinkSamples(newBiologicalSample, msInjectionSamples, callback);
        }
        return msInjectionSamples.size() + " MS_INJECTION " + context.getMessage(Dict.SAMPLES).toLowerCase() 
                + " have been annotated.";
    }

}
