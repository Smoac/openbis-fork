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

package ch.systemsx.cisd.openbis.uitest.suite.main;

import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.dsl.CommandNotSuccessful;
import ch.systemsx.cisd.openbis.uitest.dsl.IdentifiedBy;
import ch.systemsx.cisd.openbis.uitest.type.GeneralBatchImportFile;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.type.User;

/**
 * @author anttil
 */
public class GeneralBatchImportRegistrationTest extends MainSuite
{
    SampleType basic;

    SampleType componentType;

    Space sampleSpace;

    Space defaultSpace;

    Space homeSpace;

    Space componentSpace;

    User withHomeSpace;

    User withoutHomeSpace;

    @Override
    public void fixture()
    {
        System.out.println("GeneralBatchImportRegistrationTest - Fixture");
        defaultSpace = create(aSpace().withCodePrefix("DEFAULT_SPACE"));
        sampleSpace = create(aSpace().withCodePrefix("SAMPLE_SPACE"));
        homeSpace = create(aSpace().withCodePrefix("HOME_SPACE"));
        componentSpace = create(aSpace().withCodePrefix("COMPONENT_SPACE"));

        basic = create(aSampleType().withCodePrefix("BASIC"));
        componentType = create(aSampleType().thatCanBeComponent().withCodePrefix("COMPONENT"));

        withHomeSpace = using(publicApi(), create(aUser().withHomeSpace(homeSpace)));
        withoutHomeSpace = using(publicApi(), create(aUser()));

    }

    @Test
    public void spaceOfBasicSampleIsReadFromIdentifier(Method method)
            throws Exception
    {
        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile().withDefaultSpace(defaultSpace));

        Sample sample =
                create(in(file),
                        aSample().in(sampleSpace).ofType(basic),
                        IdentifiedBy.SPACE_AND_CODE);

        as(user(withHomeSpace), generalBatchImport(file));

        assertThat(browserEntryOf(sample), hasSpace(sampleSpace));
    }

    @Test
    public void spaceOfBasicSampleIsSetToDefaultSpaceOfImportFileIfIdentifierDoesNotHaveSpaceInformation()
            throws Exception
    {
        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile().withDefaultSpace(defaultSpace));
        Sample sample = create(in(file), aSample().ofType(basic), IdentifiedBy.CODE);

        as(user(withHomeSpace), generalBatchImport(file));

        assertThat(browserEntryOf(sample), hasSpace(defaultSpace));
    }

    @Test
    public void spaceOfBasicSampleIsSetToUserHomeSpaceIfIdentifierDoesNotHaveSpaceInformationAndDefaultSpaceOfImportFileIsNotSet()
            throws Exception
    {
        GeneralBatchImportFile file = create(aGeneralBatchImportFile());
        Sample sample = create(in(file), aSample().ofType(basic), IdentifiedBy.CODE);

        as(user(withHomeSpace), generalBatchImport(file));

        assertThat(browserEntryOf(sample), hasSpace(homeSpace));
    }

    @Test(expectedExceptions = CommandNotSuccessful.class)
    public void importFailsIfSpaceOfSampleCannotBeDerivedFromIdentifierOrDefaultSpaceOfImportFileOrHomeSpace()
            throws Exception
    {
        GeneralBatchImportFile file = create(aGeneralBatchImportFile());
        create(in(file), aSample().ofType(basic), IdentifiedBy.CODE);

        as(user(withoutHomeSpace), generalBatchImport(file));
    }

    @Test
    public void spaceOfComponentSampleIsReadFromIdentifier(Method m)
            throws Exception
    {
        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile()
                        .withDefaultSpace(defaultSpace)
                        .withSampleContainerColumn()
                        .withName(m.getName()));

        Sample container =
                create(in(file),
                        aSample()
                                .in(sampleSpace)
                                .ofType(basic),
                        IdentifiedBy.SPACE_AND_CODE);

        Sample component =
                create(in(file),
                        aSample()
                                .in(componentSpace)
                                .ofType(componentType)
                                .containedBy(container),
                        IdentifiedBy.SPACE_AND_CODE);

        as(user(withHomeSpace), generalBatchImport(file));

        assertThat(browserEntryOf(container), hasSpace(sampleSpace));
        assertThat(browserEntryOf(component), hasSpace(componentSpace));
        assertThat(browserEntryOf(component), hasContainer(container));
    }

    @Test
    public void spaceOfComponentSampleIsSetToDefaultSpaceOfImportFileIfIdentifierDoesNotHaveSpaceInformation(
            Method m) throws Exception
    {
        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile()
                        .withSampleContainerColumn()
                        .withDefaultSpace(defaultSpace)
                        .withName(m.getName()));

        Sample container =
                create(in(file),
                        aSample().in(sampleSpace).ofType(basic),
                        IdentifiedBy.SPACE_AND_CODE);

        Sample component =
                create(in(file),
                        aSample().ofType(componentType).containedBy(container),
                        IdentifiedBy.CODE);

        as(user(withHomeSpace), generalBatchImport(file));

        assertThat(browserEntryOf(component), hasSpace(defaultSpace));
        assertThat(browserEntryOf(component), hasContainer(container));

    }

    @Test
    public void importFailsIfContainerIdentifierHasMismatchInSpaces() throws Exception
    {

    }

}
