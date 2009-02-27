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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CategoriesBuilder;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Login;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.OpenTab;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.CheckExperimentTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ChooseTypeOfNewExperiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ListExperiments;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.CheckSampleTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.ListSamples;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.SampleRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertyField;

/**
 * A {@link AbstractGWTTestCase} extension to test {@link GenericExperimentRegistrationForm}.
 * 
 * @author Izabela Adamczyk
 */
public class GenericExperimentRegistrationTest extends AbstractGWTTestCase
{

    private final void loginAndPreprareRegistration(final String sampleType)
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.MenuCategoryKind.EXPERIMENTS,
                CategoriesBuilder.MenuElementKind.REGISTER));
        remoteConsole.prepare(new ChooseTypeOfNewExperiment(sampleType));
    }

    public final void testRegister()
    {
        final String experimentTypeCode = "SIRNA_HCS";
        loginAndPreprareRegistration(experimentTypeCode);
        remoteConsole.prepare(new FillExperimentRegistrationForm("DEFAULT", "NEW_EXP_1", "")
                .addProperty(
                        new PropertyField(
                                GenericExperimentRegistrationForm.ID + "user-description",
                                "New test experiment description.")).addProperty(
                        new PropertyField(GenericExperimentRegistrationForm.ID + "user-gender",
                                "MALE")).addProperty(
                        new PropertyField(GenericExperimentRegistrationForm.ID
                                + "user-purchase-date", "2008-12-17")));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.MenuCategoryKind.EXPERIMENTS,
                CategoriesBuilder.MenuElementKind.BROWSE,
                GenericExperimentRegistrationForm.RegisterExperimentCallback.class));
        remoteConsole.prepare(new ListExperiments("DEFAULT", experimentTypeCode));
        remoteConsole.prepare(new CheckExperimentTable()
                .expectedRow(new ExperimentRow("NEW_EXP_1")));
        launchTest(20000);
    }

    public final void testRegisterExperimentWithSamples()
    {
        final String experimentTypeCode = "SIRNA_HCS";
        final String experimentCode = "NEW_EXP_WITH_SAMPLES";
        final String sampleCode = "3VCP8";
        final String project = "DEFAULT";
        loginAndPreprareRegistration(experimentTypeCode);
        remoteConsole.prepare(new FillExperimentRegistrationForm(project, experimentCode,
                sampleCode).addProperty(
                new PropertyField(GenericExperimentRegistrationForm.ID + "user-description",
                        "New test experiment with samples.")).addProperty(
                new PropertyField(GenericExperimentRegistrationForm.ID + "user-gender", "MALE"))
                .addProperty(
                        new PropertyField(GenericExperimentRegistrationForm.ID
                                + "user-purchase-date", "2008-12-18")));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.MenuCategoryKind.SAMPLES,
                CategoriesBuilder.MenuElementKind.BROWSE,
                GenericExperimentRegistrationForm.RegisterExperimentCallback.class));
        remoteConsole.prepare(new ListSamples("CISD", "CELL_PLATE"));
        CheckSampleTable table = new CheckSampleTable();
        table.expectedRow(new SampleRow(sampleCode).identifier("CISD", "CISD").valid().experiment(
                project, experimentCode));
        launchTest(20000);
    }
}
