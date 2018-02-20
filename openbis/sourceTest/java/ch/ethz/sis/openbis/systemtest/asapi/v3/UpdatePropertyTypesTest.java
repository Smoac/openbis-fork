/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.update.PropertyTypeUpdate;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class UpdatePropertyTypesTest extends AbstractTest
{
    @Test
    public void testUpdatePropertyTypeFromInternalNamespace()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId id = new PropertyTypePermId("$PLATE_GEOMETRY");
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(id);
        update.setDescription("Test description");

        // When
        v3api.updatePropertyTypes(sessionToken, Arrays.asList(update));

        // Then
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        PropertyType propertyType = v3api.getPropertyTypes(sessionToken, Arrays.asList(id), fetchOptions).get(id);
        assertEquals(propertyType.getDescription(), update.getDescription().getValue());
        assertEquals(propertyType.getLabel(), "Plate Geometry");
        assertEquals(propertyType.isInternalNameSpace().booleanValue(), true);

        v3api.logout(sessionToken);
    }

    @Test
    public void testUpdatePropertyTypeFromExternalNamespace()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId id = new PropertyTypePermId("COMMENT");
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(id);
        update.setLabel("Test label");

        // When
        v3api.updatePropertyTypes(sessionToken, Arrays.asList(update));

        // Then
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        PropertyType propertyType = v3api.getPropertyTypes(sessionToken, Arrays.asList(id), fetchOptions).get(id);
        assertEquals(propertyType.getDescription(), "Any other comments");
        assertEquals(propertyType.getLabel(), update.getLabel().getValue());
        assertEquals(propertyType.isInternalNameSpace().booleanValue(), false);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMissingId()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();

        assertUserFailureException(update, "Property type id cannot be null.");
    }
    
    @Test
    public void testNullDescription()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(new PropertyTypePermId("COMMENT"));
        update.setDescription(null);
        
        assertUserFailureException(update, "Description cannot be empty.");
    }
    
    @Test
    public void testEmptyDescription()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(new PropertyTypePermId("COMMENT"));
        update.setDescription("");
        
        assertUserFailureException(update, "Description cannot be empty.");
    }
    
    @Test
    public void testNullLabel()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(new PropertyTypePermId("COMMENT"));
        update.setLabel(null);
        
        assertUserFailureException(update, "Label cannot be empty.");
    }
    
    @Test
    public void testEmptyLabel()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(new PropertyTypePermId("COMMENT"));
        update.setLabel("");
        
        assertUserFailureException(update, "Label cannot be empty.");
    }

    @Test
    public void testInvalidSchema()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(new PropertyTypePermId("COMMENT"));
        update.setSchema("blabla");

        assertUserFailureException(update, "isn't a well formed XML document. Content is not allowed in prolog.");
    }

    @Test
    public void testInvalidTransformation()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(new PropertyTypePermId("COMMENT"));
        update.setTransformation(CreatePropertyTypeTest.EXAMPLE_INCORRECT_XSLT);

        assertUserFailureException(update, "Provided XSLT isn't valid.");
    }


    @Test(dataProvider = "usersNotAllowedToUpdatePropertyTypes")
    public void testUpdateWithUserCausingAuthorizationFailure(final String user)
    {
        PropertyTypePermId typeId = new PropertyTypePermId("COMMENT");
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    PropertyTypeUpdate update = new PropertyTypeUpdate();
                    update.setTypeId(typeId);
                    update.setDescription("test");
                    v3api.updatePropertyTypes(sessionToken, Arrays.asList(update));
                }
            }, typeId);
    }

    @DataProvider
    Object[][] usersNotAllowedToUpdatePropertyTypes()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER);
    }
        private void assertUserFailureException(PropertyTypeUpdate update, String expectedMessage)
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    v3api.updatePropertyTypes(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                expectedMessage);
        v3api.logout(sessionToken);
    }

}
