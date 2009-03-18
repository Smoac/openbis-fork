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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ListExperiments;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ShowExperimentEditor;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertyField;

/**
 * A {@link AbstractGWTTestCase} extension to test {@link GenericExperimentEditForm}.
 * 
 * @author Izabela Adamczyk
 */
public class GenericExperimentEditorTest extends AbstractGWTTestCase
{

    private static final String SIRNA_HCS = "SIRNA_HCS";

    private static final String NEMO = "NEMO (CISD)";

    private static final String EXP1 = "EXP1";

    public final void testShowExperimentDetails()
    {
        prepareShowExperimentEditor(NEMO, SIRNA_HCS, EXP1);
        remoteConsole.prepare(new FillExperimentEditForm("NEMO").addProperty(new PropertyField(
                GenericExperimentRegistrationForm.ID + "user-description",
                "New test experiment description.")));

        launchTest(60000);
    }

    private void prepareShowExperimentEditor(final String projectName,
            final String experimentTypeName, final String experimentCode)
    {
        loginAndGotoTab(ActionMenuKind.EXPERIMENT_MENU_BROWSE);
        remoteConsole.prepare(new ListExperiments(projectName, experimentTypeName));
        remoteConsole.prepare(new ShowExperimentEditor(experimentCode));
    }
}
