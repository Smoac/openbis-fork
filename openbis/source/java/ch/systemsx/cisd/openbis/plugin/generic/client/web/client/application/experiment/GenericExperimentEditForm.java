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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdates;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> experiment edit form.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericExperimentEditForm extends AbstractGenericExperimentRegisterEditForm
{
    private Experiment originalExperiment;

    public static DatabaseModificationAwareComponent create(
            IViewContext<IGenericClientServiceAsync> viewContext, IIdentifiable identifiable)
    {
        GenericExperimentEditForm form = new GenericExperimentEditForm(viewContext, identifiable);
        return new DatabaseModificationAwareComponent(form, form);
    }

    private GenericExperimentEditForm(IViewContext<IGenericClientServiceAsync> viewContext,
            IIdentifiable identifiable)
    {
        super(viewContext, identifiable);
    }

    private void loadSamplesInBackground()
    {
        final ListSampleCriteria sampleCriteria =
                ListSampleCriteria.createForExperiment(techIdOrNull, GWTUtils.getBaseIndexURL());
        viewContext.getCommonService().listSamples(sampleCriteria,
                new ListSamplesCallback(viewContext));
    }

    public class ListSamplesCallback extends AbstractAsyncCallback<ResultSet<Sample>>
    {

        public ListSamplesCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(ResultSet<Sample> result)
        {
            samplesArea.setSamples(result.getList());
            samplesArea.setEnabled(true);
        }
    }

    @Override
    protected void save()
    {
        ExperimentUpdates updates = new ExperimentUpdates();
        updates.setExperimentId(new TechId(originalExperiment));
        updates.setVersion(originalExperiment.getModificationDate());
        updates.setProperties(extractProperties());
        updates.setProjectIdentifier(extractProjectIdentifier());
        updates.setAttachmentSessionKey(attachmentsSessionKey);
        updates.setSampleCodes(getSamples());
        updates.setSampleType(getSampleType());
        updates.setGenerateCodes(autoGenerateCodes.getValue().booleanValue());
        updates.setRegisterSamples(existingSamplesRadio.getValue() == false);
        updates.setSamplesSessionKey(samplesSessionKey);
        viewContext.getService().updateExperiment(updates,
                new UpdateExperimentCallback(viewContext));
    }

    public final class UpdateExperimentCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<ExperimentUpdateResult>
    {

        UpdateExperimentCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final ExperimentUpdateResult result)
        {
            originalExperiment.setModificationDate(result.getModificationDate());
            updateOriginalValues(result.getSamples());
            super.process(result);
        }

        @Override
        protected String createSuccessfullRegistrationInfo(ExperimentUpdateResult result)
        {
            return "Experiment successfully updated";
        }

    }

    public void updateOriginalValues(String[] samples)
    {
        updatePropertyFieldsOriginalValues();
        updateFieldOriginalValue(projectChooser);
        samplesArea.setSampleCodes(samples);
    }

    private void setOriginalExperiment(Experiment experiment)
    {
        this.originalExperiment = experiment;
    }

    @Override
    protected void initializeFormFields()
    {
        propertiesEditor.initWithProperties(originalExperiment.getExperimentType()
                .getAssignedPropertyTypes(), originalExperiment.getProperties());
        samplesArea.setEnabled(false);
        samplesArea.setValue(viewContext.getMessage(Dict.LOAD_IN_PROGRESS));
        loadSamplesInBackground();
        updateSamples();
        codeField.setValue(originalExperiment.getCode());
        projectChooser.selectProjectAndUpdateOriginal(originalExperiment.getProject()
                .getIdentifier());
    }

    @Override
    protected void loadForm()
    {
        viewContext.getService().getExperimentInfo(techIdOrNull, GWTUtils.getBaseIndexURL(),
                new ExperimentInfoCallback(viewContext));
    }

    public final class ExperimentInfoCallback extends AbstractAsyncCallback<Experiment>
    {

        private ExperimentInfoCallback(final IViewContext<IGenericClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected final void process(final Experiment result)
        {
            setOriginalExperiment(result);
            initGUI();
        }
    }
}
