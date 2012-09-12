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

package ch.systemsx.cisd.openbis.uitest;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.infra.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;

/**
 * @author anttil
 */
@Test(groups =
    { "login-admin" })
public class SampleTest extends SeleniumTest
{
    @Test
    public void newSampleIsListedInSampleBrowser() throws Exception
    {
        Sample sample = create(aSample());

        assertThat(sampleBrowser(), lists(sample));
    }

    @Test
    public void propertiesOfSampleTypeAreAskedForInSampleRegistration() throws Exception
    {

        PropertyType booleanType = create(aBooleanPropertyType());
        PropertyType integerType = create(anIntegerPropertyType());
        SampleType sampleType = create(aSampleType());

        create(aSamplePropertyTypeAssignment().with(sampleType).with(booleanType));
        create(aSamplePropertyTypeAssignment().with(sampleType).with(integerType));

        assertThat(sampleRegistrationPageFor(sampleType), hasInputsForProperties(booleanType,
                integerType));
    }

    @Test
    public void vocabularyPropertiesLinkToExternalPagesFromSampleBrowser() throws Exception
    {
        Vocabulary vocabulary =
                create(aVocabulary()
                        .withUrl("http://www.ask.com/web?q=${term}")
                        .withTerms("mouse", "fly", "tiger"));

        PropertyType vocabularyType =
                create(aVocabularyPropertyType(vocabulary));

        SampleType sampleType = create(aSampleType());

        create(aSamplePropertyTypeAssignment()
                .with(sampleType)
                .with(vocabularyType)
                .thatIsMandatory());

        Sample sample =
                create(aSample().ofType(sampleType).withProperty(vocabularyType, "mouse"));

        assertThat(sampleBrowser().allSpaces().dataOf(sample).get(vocabularyType.getLabel()),
                containsLink("MOUSE", "http://www.ask.com/web?q=MOUSE"));

    }

    @Test(enabled = false)
    public void visibilityOfPropertiesOfSampleTypeCanBeSetFromSampleBrowserSettings()
            throws Exception
    {

    }
}
