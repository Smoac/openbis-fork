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

import java.util.UUID;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.dsl.IdentifiedBy;
import ch.systemsx.cisd.openbis.uitest.type.GeneralBatchImportFile;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.type.User;

/**
 * @author anttil
 */
public class GeneralBatchImportUpdateTest extends MainSuite
{

    // TODO
    // The test framework thinks it can uniquely identify a sample with it's subcode.
    // This is why we get IllegalStateException here, as there are multiple rows found
    // with same subcode.
    @Test(expectedExceptions = IllegalStateException.class)
    public void containerOfSampleCannotBeChangedButNewSampleIsCreated() throws Exception
    {
        Sample newContainer =
                create(aSample().ofType(basic).withCodePrefix("NEW_CONTAINER"));

        GeneralBatchImportFile file = create(aGeneralBatchImportFile().withSampleContainerColumn());

        create(in(file), anUpdateOf(component).settingContainerTo(newContainer),
                IdentifiedBy.SPACE_AND_CODE);

        generalBatchImport(file);

        assertThat(browserEntryOf(component), hasContainer(newContainer));
    }

    @Test
    public void updateOfSampleIdentifiedWithSpaceAndCodeWorks() throws Exception
    {
        String newValue = UUID.randomUUID().toString();

        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile().withDefaultSpace(defaultSpace));

        create(in(file), anUpdateOf(sample).settingProperty(propertyType, newValue),
                IdentifiedBy.SPACE_AND_CODE);

        generalBatchImport(file);

        assertThat(browserEntryOf(sample), containsValue(propertyType.getLabel(), newValue));
    }

    @Test
    public void updateOfSampleIdentifiedWithCodeAndDefaultSpaceWorks() throws Exception
    {
        String newValue = UUID.randomUUID().toString();

        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile().withDefaultSpace(sampleSpace));

        create(in(file), anUpdateOf(sample).settingProperty(propertyType, newValue),
                IdentifiedBy.CODE);

        generalBatchImport(file);

        assertThat(browserEntryOf(sample), containsValue(propertyType.getLabel(), newValue));
    }

    @Override
    public void fixture()
    {
        defaultSpace = create(aSpace().withCodePrefix("DEFAULT_SPACE"));
        sampleSpace = create(aSpace().withCodePrefix("SAMPLE_SPACE"));
        homeSpace = create(aSpace().withCodePrefix("HOME_SPACE"));
        componentSpace = create(aSpace().withCodePrefix("COMPONENT_SPACE"));

        basic = create(aSampleType().withCodePrefix("BASIC"));
        componentType = create(aSampleType().thatCanBeComponent().withCodePrefix("COMPONENT"));

        withHomeSpace = using(publicApi(), create(aUser().withHomeSpace(homeSpace)));
        withoutHomeSpace = using(publicApi(), create(aUser()));

        propertyType = create(aVarcharPropertyType());

        create(aSamplePropertyTypeAssignment()
                .with(basic)
                .with(propertyType));

        create(aSamplePropertyTypeAssignment()
                .with(componentType)
                .with(propertyType));

        container = create(aSample().ofType(basic).in(sampleSpace)
                .withProperty(propertyType, UUID.randomUUID().toString()));
        component =
                create(aSample().ofType(componentType).in(componentSpace).containedBy(container)
                        .withProperty(propertyType, UUID.randomUUID().toString()));
        sample = create(aSample().ofType(basic).in(sampleSpace)
                .withProperty(propertyType, UUID.randomUUID().toString()));

    }

    SampleType basic;

    SampleType componentType;

    Space sampleSpace;

    Space defaultSpace;

    Space homeSpace;

    Space componentSpace;

    User withHomeSpace;

    User withoutHomeSpace;

    PropertyType propertyType;

    Sample container;

    Sample component;

    Sample sample;
}
