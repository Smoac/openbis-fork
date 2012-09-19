/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.suite;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.page.dialog.AddSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.tab.RoleAssignmentBrowser;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;
import ch.systemsx.cisd.openbis.uitest.type.Project;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;

/**
 * @author anttil
 */
@Test(groups =
    { "login-admin" })
public class ManualTest extends SeleniumTest
{

    @Test(enabled = false)
    public void basic()
    {
        // 0) Cleanup
        experimentBrowser().space("sprint-test").deleteAll();
        trash().empty();
        projectBrowser().deleteIfExists("Code", "p1");
        spaceBrowser().deleteIfExists("Code", "sprint-test");
        sampleTypeBrowser().deleteIfExists("Code", "sprint_test");
        experimentTypeBrowser().deleteIfExists("Code", "sprint_test");
        propertyTypeBrowser().deleteIfExists("Code", "sprint-test.text");
        propertyTypeBrowser().deleteIfExists("Code", "sprint-test.real");
        propertyTypeBrowser().deleteIfExists("Code", "sprint-test.animal");
        vocabularyBrowser().deleteIfExists("Code", "sprint-test-animal");

        // 1) Login and authorization
        openbis.browseToRoleAssignmentBrowser();
        assertThat(browser(), isShowing(RoleAssignmentBrowser.class));

        // 2) Space
        Space space = create(aSpace().withCode("sprint-test"));
        assertThat(spaceBrowser(), lists(space));

        // 3) Sample types and properties
        create(aSampleType().withCode("sprint test"));
        assertThat(browser(), isShowing(AddSampleTypeDialog.class));
        get(AddSampleTypeDialog.class).cancel();

        SampleType sampleType =
                create(aSampleType()
                        .withCode("sprint_test")
                        .thatIsNotListable()
                        .thatShowsContainer()
                        .thatShowsParents());
        assertThat(sampleTypeBrowser(), lists(sampleType));
        assertThat(sampleBrowser(), doesNotShowInToolBar(sampleType));

        perform(anUpdateOf(sampleType).settingItListable());
        assertThat(sampleBrowser(), showsInToolBar(sampleType));

        Vocabulary vocabulary =
                create(aVocabulary()
                        .withCode("SPRINT-TEST-ANIMAL")
                        .withTerms("mouse", "fly", "tiger")
                        .withUrl("http://www.ask.com/web?q=${term}"));

        PropertyType varcharPropertyType =
                create(aVarcharPropertyType()
                        .withCode("SPRINT-TEST.TEXT")
                        .withLabel("Sprint Test Text")
                        .withDescription("some text"));

        @SuppressWarnings("unused")
        PropertyType realPropertyType =
                create(aRealPropertyType()
                        .withCode("SPRINT-TEST.REAL")
                        .withLabel("Sprint Test Real")
                        .withDescription("some text"));

        PropertyType animalPropertyType =
                create(aVocabularyPropertyType(vocabulary)
                        .withCode("SPRINT-TEST.ANIMAL")
                        .withLabel("Sprint Test Animal")
                        .withDescription("some text"));

        create(aSamplePropertyTypeAssignment()
                .with(sampleType)
                .thatIsMandatory()
                .with(varcharPropertyType));
        create(aSamplePropertyTypeAssignment().with(sampleType).with(animalPropertyType));

        assertThat(sampleRegistrationPageFor(sampleType),
                hasInputsForProperties(varcharPropertyType, animalPropertyType));

        // 4) Sample
        Sample sample =
                create(aSample().ofType(sampleType)
                        .withCode("SPRINT1")
                        .in(space)
                        .withProperty(varcharPropertyType, "some text")
                        .withProperty(animalPropertyType, "mouse"));

        assertThat(sampleBrowser(), lists(sample));
        assertThat(sampleBrowser().cell(sample, animalPropertyType.getLabel()), displays("mouse"));
        assertThat(sampleBrowser().cell(sample, animalPropertyType.getLabel()),
                linksTo("http://www.ask.com/web?q=MOUSE"));

        // 5) Project and experiment
        Project project = create(aProject().withCode("P1").in(space));
        ExperimentType experimentType = create(anExperimentType().withCode("SPRINT_TEST"));

        Experiment experiment =
                create(anExperiment().ofType(experimentType).in(project).withCode("exp1")
                        .withSamples(sample));
        assertThat(sampleBrowser().cell(sample, "Experiment"), displays(experiment.getCode()));
        assertThat(sampleBrowser().cell(sample, "Project"), displays(project.getCode()));
    }
}
