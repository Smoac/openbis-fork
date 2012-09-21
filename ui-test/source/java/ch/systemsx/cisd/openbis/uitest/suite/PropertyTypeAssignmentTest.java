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

import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;

/**
 * @author anttil
 */
@Test(groups =
    { "login-admin" })
public class PropertyTypeAssignmentTest extends SeleniumTest
{
    @Test
    public void newPropertyTypeAssignmentIsListedInPropertyTypeAssignmentBrowser() throws Exception
    {
        PropertyTypeAssignment assignment = create(aSamplePropertyTypeAssignment());

        assertThat(propertyTypeAssignmentBrowser(), lists(assignment));
    }

    @Test
    public void existingSamplesGetInitialValueSetForNewProperty()
            throws Exception
    {
        SampleType sampleType = create(aSampleType());
        Sample sample = create(aSample().ofType(sampleType));

        PropertyType propertyType = create(aVarcharPropertyType());

        create(aSamplePropertyTypeAssignment()
                .with(sampleType)
                .with(propertyType)
                .thatIsMandatory()
                .havingInitialValueOf("Test Initial Value"));

        assertThat(cell(sample, propertyType.getLabel()).of(sampleBrowser()),
                displays("Test Initial Value"));
    }
}
