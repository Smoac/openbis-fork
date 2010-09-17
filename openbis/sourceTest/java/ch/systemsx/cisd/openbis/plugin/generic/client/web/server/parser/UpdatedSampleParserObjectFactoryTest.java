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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.parser.DefaultPropertyMapper;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedSample;

/**
 * Test cases for corresponding {@link UpdatedSampleParserObjectFactory} class.
 * 
 * @author Piotr Buczek
 */
public final class UpdatedSampleParserObjectFactoryTest
{

    private final static UpdatedSampleParserObjectFactory createUpdatedSampleParserObjectFactory(
            IPropertyMapper propertyMapper, boolean expectIdentifierInFile)
    {
        final SampleType sampleType = new SampleType();
        sampleType.setCode("SAMPLE_TYPE");
        final UpdatedSampleParserObjectFactory parserObjectFactory =
                new UpdatedSampleParserObjectFactory(sampleType, propertyMapper,
                        expectIdentifierInFile, true);
        return parserObjectFactory;
    }

    private final static String PROPERTY_1 = "prop1";

    private final static String PROPERTY_2 = "prop2";

    private final static IPropertyMapper createPropertyMapper()
    {
        final String[] properties = new String[]
            { UpdatedSample.IDENTIFIER_COLUMN, UpdatedSample.CONTAINER, PROPERTY_1, PROPERTY_2 };
        final DefaultPropertyMapper propertyMapper = new DefaultPropertyMapper(properties);
        return propertyMapper;
    }

    @SuppressWarnings("unused")
    @DataProvider
    private final static Object[][] getLineTokens()
    {
        return new Object[][]
            {
                { new String[]
                    { "", "", "", "" }, 0 },
                { new String[]
                    { "id1", "cont1", "1", "hello" }, 2 }, };
    }

    @Test(dataProvider = "getLineTokens")
    public final void testCreateObject(final String[] lineTokens, final int numberOfProperties)
    {
        final UpdatedSampleParserObjectFactory parserObjectFactory =
                createUpdatedSampleParserObjectFactory(createPropertyMapper(), true);
        final UpdatedSample objectCreated =
                (UpdatedSample) parserObjectFactory.createObject(lineTokens);
        // assert that all NewSample properties are set properly
        assertEquals(objectCreated.getIdentifier(), lineTokens[0]);
        assertEquals(objectCreated.getContainerIdentifier(),
                StringUtils.isEmpty(lineTokens[1]) ? null : lineTokens[1]);
        final IEntityProperty[] properties = objectCreated.getProperties();
        assertEquals(numberOfProperties, properties.length);
        int index = 2;
        for (final IEntityProperty sampleProperty : properties)
        {
            sampleProperty.getValue().equals(lineTokens[index++]);
        }
        // assert that SampleBatchUpdateDetails contains proper data
        final SampleBatchUpdateDetails batchUpdateDetails = objectCreated.getBatchUpdateDetails();
        final Set<String> propertiesToUpdate = batchUpdateDetails.getPropertiesToUpdate();
        assertEquals(2, propertiesToUpdate.size());
        assertTrue(propertiesToUpdate.contains(PROPERTY_1));
        assertTrue(propertiesToUpdate.contains(PROPERTY_2));
        assertEquals(StringUtils.isBlank(lineTokens[2]) == false, batchUpdateDetails
                .isContainerUpdateRequested());
        assertFalse(batchUpdateDetails.isParentUpdateRequested());
        assertFalse(batchUpdateDetails.isExperimentUpdateRequested());
    }
}
