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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.IPropertyChecker;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.IValueAssertion;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.PropertyCheckingManager;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentViewer.ExperimentInfoCallback;

/**
 * @author Izabela Adamczyk
 */
public class CheckExperiment extends AbstractDefaultTestCommand implements
        IPropertyChecker<CheckExperiment>
{

    private final String identifier;

    private final PropertyCheckingManager propertyCheckingManager;

    private CheckTableCommand attachmentsSection;

    private CheckTableCommand sampleSection;

    public CheckExperiment(final String identifierPrefix, final String code)
    {
        this.identifier = identifierPrefix + "/" + code;
        propertyCheckingManager = new PropertyCheckingManager();
        addCallbackClass(ExperimentInfoCallback.class);
        addCallbackClass(SampleBrowserGrid.ListEntitiesCallback.class);
        addCallbackClass(ExperimentDataSetBrowser.ListEntitiesCallback.class);
    }

    public Property property(final String name)
    {
        return new Property(name, this);
    }

    public CheckExperiment property(final String name, final IValueAssertion<?> valueAssertion)
    {
        propertyCheckingManager.addExcpectedProperty(name, valueAssertion);
        return this;
    }

    public void execute()
    {
        propertyCheckingManager.assertPropertiesOf(ExperimentPropertiesSection.PROPERTIES_ID_PREFIX
                + identifier);

        if (sampleSection != null)
        {
            sampleSection.execute();
        }
    }

    public CheckTableCommand attachmentsTable()
    {
        attachmentsSection =
                new CheckTableCommand(AttachmentsSection.ATTACHMENTS_ID_PREFIX
                        + identifier);
        return attachmentsSection;
    }

    public CheckTableCommand sampleTable()
    {
        sampleSection = new CheckTableCommand(ExperimentSamplesSection.createId(identifier));
        return sampleSection;
    }

    public CheckTableCommand dataSetTable()
    {
        return new CheckTableCommand(ExperimentDataSetBrowser.ID_PREFIX + identifier
                + ExperimentDataSetBrowser.GRID_POSTFIX);
    }

}
