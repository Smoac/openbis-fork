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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import junit.framework.Assert;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentViewer;

/**
 * @author Izabela Adamczyk
 */
public class ShowExperimentEditor extends AbstractDefaultTestCommand
{
    private final String identifier;

    public ShowExperimentEditor(String identifier)
    {
        this.identifier = identifier;
        addCallbackClass(GenericExperimentViewer.ExperimentInfoCallback.class);
    }

    public void execute()
    {
        final Button edit =
                (Button) GWTTestUtil.getWidgetWithID(GenericExperimentViewer.createId(identifier)
                        + GenericExperimentViewer.ID_EDIT_SUFFIX);
        Assert.assertTrue(edit.isEnabled());
        edit.fireEvent(Events.Select);
    }

}
