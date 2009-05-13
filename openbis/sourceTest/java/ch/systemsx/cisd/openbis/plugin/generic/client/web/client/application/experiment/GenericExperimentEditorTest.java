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

import java.util.Date;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.CheckExperimentTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ListExperiments;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ShowExperiment;
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

    private static final String EXP_TYPE_SIRNA_HCS = "SIRNA_HCS";

    private static final String GROUP_CISD = "CISD";

    private static final String PROJ_CODE_NEMO = "NEMO";

    private static final String PROJ_CODE_DEFAULT = "DEFAULT";

    private static final String PROJ_ID_CISD_DEFAULT = "/" + GROUP_CISD + "/" + PROJ_CODE_DEFAULT;

    private static final String PROJ_WITH_GROUP_DEFAULT_CISD =
            PROJ_CODE_DEFAULT + " (" + GROUP_CISD + ")";

    private static final String PROJ_WITH_GROUP_NEMO_CISD =
            PROJ_CODE_NEMO + " (" + GROUP_CISD + ")";

    private static final String EXP_CODE_EXP1 = "EXP1";

    private static final String EXP_ID_CISD_NEMO_EXP1 =
            "/" + GROUP_CISD + "/" + PROJ_CODE_NEMO + "/" + EXP_CODE_EXP1;

    public final void testEditExperimentDescription()
    {
        prepareShowExperimentEditor(PROJ_WITH_GROUP_NEMO_CISD, EXP_TYPE_SIRNA_HCS, EXP_CODE_EXP1);
        String description = "description from " + new Date();
        remoteConsole.prepare(new FillExperimentEditForm(EXP_ID_CISD_NEMO_EXP1)
                .addProperty(new PropertyField("user-description", description)));
        remoteConsole.prepare(new ListExperiments(PROJ_WITH_GROUP_NEMO_CISD, EXP_TYPE_SIRNA_HCS,
                GenericExperimentEditForm.UpdateExperimentCallback.class));
        CheckExperimentTable table = new CheckExperimentTable();
        table.expectedRow(new ExperimentRow(EXP_CODE_EXP1).withUserPropertyCell("description",
                description));
        remoteConsole.prepare(table);
        launchTest(25 * SECOND);
    }

    public final void testEditExperimentProject()
    {
        prepareShowExperimentEditor(PROJ_WITH_GROUP_NEMO_CISD, EXP_TYPE_SIRNA_HCS, EXP_CODE_EXP1);
        remoteConsole.prepare(new FillExperimentEditForm(EXP_ID_CISD_NEMO_EXP1)
                .changeProject(PROJ_ID_CISD_DEFAULT));
        remoteConsole.prepare(new ListExperiments(PROJ_WITH_GROUP_DEFAULT_CISD, EXP_TYPE_SIRNA_HCS,
                GenericExperimentEditForm.UpdateExperimentCallback.class));
        CheckExperimentTable table = new CheckExperimentTable();
        table.expectedRow(new ExperimentRow(EXP_CODE_EXP1));
        remoteConsole.prepare(table);
        launchTest(20 * SECOND);
    }

    private void prepareShowExperimentEditor(final String projectNameWithGroup,
            final String experimentTypeName, final String code)
    {
        loginAndInvokeAction(ActionMenuKind.EXPERIMENT_MENU_BROWSE);
        remoteConsole.prepare(new ListExperiments(projectNameWithGroup, experimentTypeName));
        remoteConsole.prepare(new ShowExperiment(code));
        remoteConsole.prepare(new ShowExperimentEditor(EXP_ID_CISD_NEMO_EXP1));
    }

}
