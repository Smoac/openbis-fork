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
import java.util.Collections;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
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
    public void testAddMetaData()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId id = new PropertyTypePermId("COMMENT");
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(id);
        update.getMetaData().put("greetings", "hello world");

        // When
        v3api.updatePropertyTypes(sessionToken, Arrays.asList(update));

        // Then
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        PropertyType propertyType = v3api.getPropertyTypes(sessionToken, Arrays.asList(id), fetchOptions).get(id);
        assertEquals(propertyType.getDescription(), "Any other comments");
        assertEquals(propertyType.getLabel(), "Comment");
        assertEquals(propertyType.isInternalNameSpace().booleanValue(), false);
        assertEquals(propertyType.getMetaData().toString(), "{greetings=hello world}");

        v3api.logout(sessionToken);
    }

    @Test
    public void testRemoveMetaData()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId id = new PropertyTypePermId("DESCRIPTION");
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(id);
        update.getMetaData().remove("answer");

        // When
        v3api.updatePropertyTypes(sessionToken, Arrays.asList(update));

        // Then
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        PropertyType propertyType = v3api.getPropertyTypes(sessionToken, Arrays.asList(id), fetchOptions).get(id);
        assertEquals(propertyType.getMetaData().toString(), "{}");

        v3api.logout(sessionToken);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetMetaData()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId id = new PropertyTypePermId("DESCRIPTION");
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(id);
        update.getMetaData().put("greetings", "hello world");
        update.getMetaData().set(Collections.singletonMap("new key", "new value"));

        // When
        v3api.updatePropertyTypes(sessionToken, Arrays.asList(update));

        // Then
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        PropertyType propertyType = v3api.getPropertyTypes(sessionToken, Arrays.asList(id), fetchOptions).get(id);
        assertEquals(propertyType.getMetaData().toString(), "{new key=new value}");

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
        update.setTypeId(createXmlPropertyType());
        update.setSchema("blabla");

        assertUserFailureException(update, "isn't a well formed XML document. Content is not allowed in prolog.");
    }

    @Test(groups = "broken")
    public void testInvalidTransformation()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(createXmlPropertyType());
        update.setTransformation(CreatePropertyTypeTest.EXAMPLE_INCORRECT_XSLT);

        assertUserFailureException(update, "Provided XSLT isn't valid.");
    }

    @Test
    public void testSchemaSpecifiedButDataTypeNotXML()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(new PropertyTypePermId("COMMENT"));
        update.setSchema(CreatePropertyTypeTest.EXAMPLE_SCHEMA);

        assertUserFailureException(update, "XML schema is specified but data type is VARCHAR.");
    }

    @Test
    public void testTransformationSpecifiedButDataTypeNotXML()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(new PropertyTypePermId("COMMENT"));
        update.setTransformation(CreatePropertyTypeTest.EXAMPLE_XSLT);

        assertUserFailureException(update, "XSLT transformation is specified but data type is VARCHAR.");
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

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(new PropertyTypePermId("BACTERIUM"));

        PropertyTypeUpdate update2 = new PropertyTypeUpdate();
        update2.setTypeId(new PropertyTypePermId("$PLATE_GEOMETRY"));

        v3api.updatePropertyTypes(sessionToken, Arrays.asList(update, update2));

        assertAccessLog(
                "update-property-types  PROPERTY_TYPE_UPDATES('[PropertyTypeUpdate[typeId=BACTERIUM], PropertyTypeUpdate[typeId=$PLATE_GEOMETRY]]')");
    }

    @DataProvider
    Object[][] usersNotAllowedToUpdatePropertyTypes()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER);
    }

    private PropertyTypePermId createXmlPropertyType()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode("TEST-" + System.currentTimeMillis());
        creation.setLabel("Test");
        creation.setDescription("Testing");
        creation.setDataType(DataType.XML);
        PropertyTypePermId permId = v3api.createPropertyTypes(sessionToken, Arrays.asList(creation)).get(0);
        v3api.logout(sessionToken);
        return permId;
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
