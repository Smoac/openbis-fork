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
import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class CreatePropertyTypeTest extends AbstractTest
{
    private static String EXAMPLE_SCHEMA =
            "<?xml version='1.0'?>\n"
            + "<xs:schema targetNamespace='http://my.host.org' xmlns:xs='http://www.w3.org/2001/XMLSchema'>\n"
            + "<xs:element name='note'>\n" 
            + "  <xs:complexType>\n"
            + "    <xs:sequence>\n"
            + "      <xs:element name='to' type='xs:string'/>\n"
            + "      <xs:element name='from' type='xs:string'/>\n"
            + "      <xs:element name='heading' type='xs:string'/>\n"
            + "      <xs:element name='body' type='xs:string'/>\n" 
            + "    </xs:sequence>\n"
            + "  </xs:complexType>\n" 
            + "</xs:element>\n" 
            + "</xs:schema>";


    private static String EXAMPLE_XSLT = "<?xml version='1.0'?>\n"
            + "<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>"
            + "<xsl:output method='html'/>\n                            "
            + "<xsl:template match='child::person'>\n                   "
            + " <html>\n                                                "
            + "  <head>\n                                               "
            + "   <title>\n                                             "
            + "    <xsl:value-of select='descendant::firstname' />\n    "
            + "    <xsl:text> </xsl:text>\n                             "
            + "    <xsl:value-of select='descendant::lastname' />\n     "
            + "   </title>\n                                            "
            + "  </head>\n                                              "
            + "  <body>\n                                               "
            + "   <xsl:value-of select='descendant::firstname' />\n     "
            + "   <xsl:text> </xsl:text>\n                              "
            + "   <xsl:value-of select='descendant::lastname' />\n      "
            + "  </body>\n                                              "
            + " </html>\n                                               "
            + "</xsl:template>\n                                        "
            + "</xsl:stylesheet>";

    private static String EXAMPLE_INCORRECT_XSLT = EXAMPLE_XSLT.replaceAll("xsl:stylesheet",
            "xsl:styleshet");

    @Test
    public void testCreateInternalNamespacePropertyType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode("test-property");
        creation.setDataType(DataType.REAL);
        creation.setDescription("only for testing");
        creation.setLabel("Test Property");
        creation.setInternalNameSpace(true);
        creation.setManagedInternally(true);

        // When
        List<PropertyTypePermId> ids = v3api.createPropertyTypes(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(ids.toString(), "[$TEST-PROPERTY]");
        PropertyTypeSearchCriteria searchCriteria = new PropertyTypeSearchCriteria();
        searchCriteria.withCode().thatEquals("$" + creation.getCode().toUpperCase());
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        Map<IPropertyTypeId, PropertyType> types = v3api.getPropertyTypes(sessionToken, ids, fetchOptions);
        PropertyType propertyType = types.get(ids.get(0));
        assertEquals(propertyType.getCode(), creation.getCode().toUpperCase());
        assertEquals(propertyType.getPermId(), ids.get(0));
        assertEquals(propertyType.getDataType(), creation.getDataType());
        assertEquals(propertyType.getDescription(), creation.getDescription());
        assertEquals(propertyType.getLabel(), creation.getLabel());
        assertEquals(propertyType.isInternalNameSpace().booleanValue(), creation.isInternalNameSpace());
        assertEquals(propertyType.isManagedInternally().booleanValue(), creation.isManagedInternally());
        assertEquals(types.size(), 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testCreateXmlPropertyType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode("test-property");
        creation.setDataType(DataType.XML);
        creation.setDescription("only for testing");
        creation.setLabel("Test Property");
        creation.setSchema(EXAMPLE_SCHEMA);
        creation.setTransformation(EXAMPLE_XSLT);

        // When
        List<PropertyTypePermId> ids = v3api.createPropertyTypes(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(ids.toString(), "[TEST-PROPERTY]");
        PropertyTypeSearchCriteria searchCriteria = new PropertyTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(creation.getCode().toUpperCase());
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        Map<IPropertyTypeId, PropertyType> types = v3api.getPropertyTypes(sessionToken, ids, fetchOptions);
        PropertyType propertyType = types.get(ids.get(0));
        assertEquals(propertyType.getCode(), creation.getCode().toUpperCase());
        assertEquals(propertyType.getPermId(), ids.get(0));
        assertEquals(propertyType.getDataType(), creation.getDataType());
        assertEquals(propertyType.getDescription(), creation.getDescription());
        assertEquals(propertyType.getLabel(), creation.getLabel());
        assertEquals(propertyType.isInternalNameSpace().booleanValue(), false);
        assertEquals(propertyType.isManagedInternally().booleanValue(), false);
        assertEquals(propertyType.getSchema(), creation.getSchema());
        assertEquals(propertyType.getTransformation(), creation.getTransformation());
        assertEquals(types.size(), 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testCreateVocabularyPropertyType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode("test-voca-property");
        creation.setDataType(DataType.CONTROLLEDVOCABULARY);
        creation.setDescription("only for testing");
        creation.setLabel("Test Vocabulary Property");
        creation.setVocabularyId(new VocabularyPermId("test_vocabulary"));

        // When
        List<PropertyTypePermId> ids = v3api.createPropertyTypes(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(ids.toString(), "[TEST-VOCA-PROPERTY]");
        PropertyTypeSearchCriteria searchCriteria = new PropertyTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(creation.getCode().toUpperCase());
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        fetchOptions.withVocabulary();
        Map<IPropertyTypeId, PropertyType> types = v3api.getPropertyTypes(sessionToken, ids, fetchOptions);
        PropertyType propertyType = types.get(ids.get(0));
        assertEquals(propertyType.getCode(), creation.getCode().toUpperCase());
        assertEquals(propertyType.getPermId(), ids.get(0));
        assertEquals(propertyType.getDataType(), creation.getDataType());
        assertEquals(propertyType.getDescription(), creation.getDescription());
        assertEquals(propertyType.getLabel(), creation.getLabel());
        assertEquals(propertyType.isInternalNameSpace().booleanValue(), false);
        assertEquals(propertyType.isManagedInternally().booleanValue(), false);
        assertEquals(propertyType.getVocabulary().getCode(), "TEST_VOCABULARY");
        assertEquals(propertyType.getVocabulary().getDescription(), "Test vocabulary");
        assertEquals(types.size(), 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testCreateMaterialPropertyType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode("test-material-property");
        creation.setDataType(DataType.MATERIAL);
        creation.setDescription("only for testing");
        creation.setLabel("Test Material Property");
        creation.setMaterialTypeId(new EntityTypePermId("SIRNA", EntityKind.MATERIAL));

        // When
        List<PropertyTypePermId> ids = v3api.createPropertyTypes(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(ids.toString(), "[TEST-MATERIAL-PROPERTY]");
        PropertyTypeSearchCriteria searchCriteria = new PropertyTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(creation.getCode().toUpperCase());
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        fetchOptions.withMaterialType();
        Map<IPropertyTypeId, PropertyType> types = v3api.getPropertyTypes(sessionToken, ids, fetchOptions);
        PropertyType propertyType = types.get(ids.get(0));
        assertEquals(propertyType.getCode(), creation.getCode().toUpperCase());
        assertEquals(propertyType.getPermId(), ids.get(0));
        assertEquals(propertyType.getDataType(), creation.getDataType());
        assertEquals(propertyType.getDescription(), creation.getDescription());
        assertEquals(propertyType.getLabel(), creation.getLabel());
        assertEquals(propertyType.isInternalNameSpace().booleanValue(), false);
        assertEquals(propertyType.isManagedInternally().booleanValue(), false);
        assertEquals(propertyType.getMaterialType().getCode(), "SIRNA");
        assertEquals(propertyType.getMaterialType().getDescription(), "Oligo nucleotide");
        assertEquals(types.size(), 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMissingCode()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setCode(null);
        
        assertUserFailureException(creation, "Code cannot be empty.");
    }

    @Test
    public void testEmptyCode()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setCode("");

        assertUserFailureException(creation, "Code cannot be empty.");
    }

    @Test
    public void testMissingLabel()
    {        
        PropertyTypeCreation creation = createBasic();
        creation.setLabel(null);

        assertUserFailureException(creation, "Label cannot be empty.");
    }
    
    @Test
    public void testEmptyLabel()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setLabel("");
        
        assertUserFailureException(creation, "Label cannot be empty.");
    }
    
    @Test
    public void testMissingDescription()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDescription(null);
        
        assertUserFailureException(creation, "Description cannot be empty.");
    }
    
    @Test
    public void testEmptyDescription()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDescription("");
        
        assertUserFailureException(creation, "Description cannot be empty.");
    }
    
    @Test
    public void testMissingDataType()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(null);;

        assertUserFailureException(creation, "Data type not specified.");
    }

    @Test
    public void testVocabularyTypeMissingVocabulary()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.CONTROLLEDVOCABULARY);

        assertUserFailureException(creation, "Data type has been specified as CONTROLLEDVOCABULARY but vocabulary id is missing.");
    }

    @Test
    public void testVocabularyIdButWrongDataType()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.REAL);
        creation.setVocabularyId(new VocabularyPermId("GENDER"));

        assertUserFailureException(creation, "Vocabulary id has been specified but data type is REAL.");
    }

    @Test
    public void testMaterialTypeIdButWrongDataType()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.REAL);
        creation.setMaterialTypeId(new EntityTypePermId("SIRNA", EntityKind.MATERIAL));

        assertUserFailureException(creation, "Material type id has been specified but data type is REAL.");
    }

    @Test
    public void testEntityTypeIdButWrongEntityKind()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.MATERIAL);
        creation.setMaterialTypeId(new EntityTypePermId("UNKNOWN", EntityKind.DATA_SET));

        assertUserFailureException(creation, "Specified entity type id (UNKNOWN, DATA_SET) is not a MATERIAL type.");
    }

    @Test
    public void testVocabularyTypeWithVocabularyIdAndMaterialTypeId()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.CONTROLLEDVOCABULARY);
        creation.setVocabularyId(new VocabularyPermId("GENDER"));
        creation.setMaterialTypeId(new EntityTypePermId("SIRNA", EntityKind.MATERIAL));

        assertUserFailureException(creation, "Material type id has been specified but data type is CONTROLLEDVOCABULARY.");
    }

    @Test
    public void testMaterialTypeWithVocabularyIdAndMaterialTypeId()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.MATERIAL);
        creation.setVocabularyId(new VocabularyPermId("GENDER"));
        creation.setMaterialTypeId(new EntityTypePermId("SIRNA", EntityKind.MATERIAL));

        assertUserFailureException(creation, "Vocabulary id has been specified but data type is MATERIAL.");
    }

    @Test
    public void testInvalidSchema()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.XML);
        creation.setSchema("blabla");

        assertUserFailureException(creation, "isn't a well formed XML document. Content is not allowed in prolog.");
    }

    @Test
    public void testInvalidTransformation()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.XML);
        creation.setTransformation(EXAMPLE_INCORRECT_XSLT);

        assertUserFailureException(creation, "Provided XSLT isn't valid.");
    }

    @Test(dataProvider = "usersNotAllowedToCreatePropertyTypes")
    public void testCreateWithUserCausingAuthorizationFailure(final String user)
    {
        assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    PropertyTypeCreation creation = new PropertyTypeCreation();
                    creation.setCode("TEST");
                    creation.setDescription("test");
                    creation.setDataType(DataType.REAL);
                    v3api.createPropertyTypes(sessionToken, Arrays.asList(creation));
                }
            });
    }

    @DataProvider
    Object[][] usersNotAllowedToCreatePropertyTypes()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER);
    }
    
    private PropertyTypeCreation createBasic()
    {
        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode("TEST");
        creation.setLabel("Test");
        creation.setDescription("Testing");
        creation.setDataType(DataType.REAL);
        return creation;
    }

    private void assertUserFailureException(PropertyTypeCreation creation, String expectedMessage)
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    v3api.createPropertyTypes(sessionToken, Arrays.asList(creation));
                }
            },

                // Then
                expectedMessage);
        v3api.logout(sessionToken);
    }

}
