package ch.ethz.sis.openbis.systemtest.asapi.v3;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.entity.AbstractEntity;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.entity.AbstractEntityCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.entity.AbstractEntityUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.create.IEntityTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.update.IEntityTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Abstract test class for creation of tests that share common logic among Sample, Experiment and DataSet entities
 * @param <TYPE_CREATION>
 * @param <TYPE>
 * @param <ENTITY_CREATION>
 * @param <ENTITY>
 */
public abstract class AbstractCommonEntityTest<TYPE_CREATION extends IEntityTypeCreation, TYPE extends IEntityType, TYPE_UPDATE extends IEntityTypeUpdate,
        ENTITY_CREATION extends AbstractEntityCreation, ENTITY extends AbstractEntity<ENTITY>, ENTITY_UPDATE extends AbstractEntityUpdate, ENTITY_ID extends IObjectId> extends AbstractTest
{
    private static final String PATTERN_VALIDATION_PROVIDER = "providePatterns";
    private static final String DATA_TYPE_PATTERN_VALIDATION_PROVIDER = "providePatternsAndDataTypes";
    private static final String DATA_TYPE_PATTERN_VALIDATION_UPDATE_PROVIDER = "providePatternsAndDataTypesForUpdate";
    private static final String UPDATE_ENTITY_AND_PATTERN_PROVIDER = "providePatternAdnEntityDataForUpdate";

    private static final List<DataType> DATA_TYPES_THAT_ACCEPT_PATTERN = Arrays.asList(DataType.REAL,
            DataType.INTEGER, DataType.VARCHAR, DataType.MULTILINE_VARCHAR, DataType.TIMESTAMP, DataType.DATE, DataType.HYPERLINK);


    protected abstract TYPE_CREATION newTypeCreation();

    protected abstract ENTITY_CREATION newEntityCreation(TYPE type, String code);

    protected abstract TYPE_UPDATE newTypeUpdate(String typeCode);

    protected abstract ENTITY_UPDATE newEntityUpdate(String permId);

    protected abstract void createType(String sessionToken, TYPE_CREATION creation);

    protected abstract TYPE getType(String sessionToken, String typeCode);

    protected abstract ENTITY_ID createEntity(String sessionToken, ENTITY_CREATION creation);

    protected abstract ENTITY getEntity(String sessionToken, ENTITY_ID id);

    protected abstract void updateType(String sessionToken, TYPE_UPDATE update);

    protected abstract void updateEntity(String sessionToken, ENTITY_UPDATE update);

    @Test(dataProvider = PATTERN_VALIDATION_PROVIDER)
    public void testCreateWithPatternValidation(DataType dataType, String patternType, String pattern, String input, boolean shouldFail) {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final String propertyCode = "MY_TEST_PROPERTY";
        createPropertyType(sessionToken, propertyCode, dataType);

        PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
        assignmentCreation.setPropertyTypeId(new PropertyTypePermId(propertyCode));
        assignmentCreation.setPatternType(patternType);
        assignmentCreation.setPattern(pattern);

        final TYPE_CREATION typeCreation = newTypeCreation();
        typeCreation.setCode("NEW_ENITTY_TYPE");
        typeCreation.setDescription("test description");
        typeCreation.setPropertyAssignments(Arrays.asList(assignmentCreation));

        createType(sessionToken, typeCreation);
        TYPE type = getType(sessionToken, typeCreation.getCode());

        final ENTITY_CREATION creation = newEntityCreation(type, "NEW_ENTITY_1");
        creation.setProperty(propertyCode, input);

        if(shouldFail) {
            assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createEntity(sessionToken, creation);
                }
            }, dataType == DataType.TIMESTAMP ? " is not matching defined pattern!" :
                                "Value: '"+input+"' is not matching defined pattern!");
        } else {
            ENTITY_ID entityId = createEntity(sessionToken, creation);
            ENTITY entity = getEntity(sessionToken, entityId);
            if(dataType == DataType.TIMESTAMP)
            {
                assertTrue(entity.getProperty(propertyCode).toString().startsWith(input));
            } else {
                assertEquals(entity.getProperty(propertyCode), input);
            }
        }
    }

    @DataProvider(name = PATTERN_VALIDATION_PROVIDER)
    protected Object[][] providePatterns()
    {
        List<Object[]> result = new ArrayList<>();
        for(DataType dataType : DATA_TYPES_THAT_ACCEPT_PATTERN)
        {
            if(dataType == DataType.DATE)
            {
                result.add(new Object[] { dataType, "PATTERN", "2022.*", "2022-04-08", false});
                result.add(new Object[] { dataType, "PATTERN", "2022.*", "2024-05-16", true});
                result.add(new Object[] { dataType, "VALUES", "\"2022-04-08\"", "2022-04-08", false});
                result.add(new Object[] { dataType, "VALUES", "\"2022-04-08\"", "2024-04-08", true});
            } else if(dataType == DataType.TIMESTAMP)
            {
                result.add(new Object[] { dataType, "PATTERN", "2022.*", "2022-05-16 04:24:00", false});
                result.add(new Object[] { dataType, "PATTERN", "2022.*", "2024-05-16 04:22:00", true});
            } else if(dataType == DataType.REAL) {
                result.add(new Object[] { dataType, "PATTERN", "[1-3]{3}\\.0", "123.0", false});
                result.add(new Object[] { dataType, "PATTERN", "[1-3]{3}\\.0", "124.0", true});
                result.add(new Object[] { dataType, "RANGES", "1-10", "5.1", false});
                result.add(new Object[] { dataType, "RANGES", "1-10", "11.0", true});
                result.add(new Object[] { dataType, "VALUES", "\"2.0\"", "2.0", false});
                result.add(new Object[] { dataType, "VALUES", "\"2.0\"", "2.1", true});
            } else if(dataType == DataType.INTEGER) {
                result.add(new Object[] { dataType, "PATTERN", "[1-3]{3}", "123", false});
                result.add(new Object[] { dataType, "PATTERN", "[1-3]{3}", "124", true});
                result.add(new Object[] { dataType, "RANGES", "1-10", "5", false});
                result.add(new Object[] { dataType, "RANGES", "1-10", "11", true});
                result.add(new Object[] { dataType, "VALUES", "\"2\"", "2", false});
                result.add(new Object[] { dataType, "VALUES", "\"2\"", "3", true});
            } else if(dataType == DataType.HYPERLINK) {
                result.add(new Object[] { dataType, "PATTERN", "http://.*", "http://b.com", false});
                result.add(new Object[] { dataType, "PATTERN", "http://.*", "https://a.com", true});
                result.add(new Object[] { dataType, "VALUES", "\"http://b.com\"", "http://b.com", false});
                result.add(new Object[] { dataType, "VALUES", "\"http://b.com\"", "http://bc.com", true});
            }else {
                result.add(new Object[] { dataType, "PATTERN", ".*", "abc", false});
                result.add(new Object[] { dataType, "PATTERN", "[a-z]{3}\\d", "abc1", false});
                result.add(new Object[] { dataType, "PATTERN", "[a-z]{3}\\d", "Abc1", true});
                result.add(new Object[] { dataType, "PATTERN", "[a-z]{3}\\d", "abc31", true});
                result.add(new Object[] { dataType, "RANGES", "1-5, 10-100, (-3)-(-5)", "-4", false});
                result.add(new Object[] { dataType, "RANGES", "1-5, 10-100, (-5)-(-3)", "11", false});
                result.add(new Object[] { dataType, "RANGES", "1-5, 10-100, (-5)-(-3)", "6", true});
                result.add(new Object[] { dataType, "RANGES", "1-5, 10-100, (-5)-(-3)", "4.5000", false});
                result.add(new Object[] { dataType, "VALUES", "\"a\", \"b\", \"c\"", "a", false});
                result.add(new Object[] { dataType, "VALUES", "\"a\", \"b\", \"c\"", "d", true});
            }
        }
        return result.toArray(new Object[result.size()][6]);
    }

    @Test(dataProvider = DATA_TYPE_PATTERN_VALIDATION_PROVIDER)
    public void testAssignPatternsToPropertyTypes(DataType dataType, boolean isCorrect, String patternType, String pattern) {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final String propertyCode = "MY_TEST_PROPERTY";
        createPropertyType(sessionToken, propertyCode, dataType);

        PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
        assignmentCreation.setPropertyTypeId(new PropertyTypePermId(propertyCode));
        assignmentCreation.setPatternType(patternType);
        assignmentCreation.setPattern(pattern);

        final TYPE_CREATION typeCreation = newTypeCreation();
        typeCreation.setCode("NEW_ENITTY_TYPE");
        typeCreation.setDescription("test description");
        typeCreation.setPropertyAssignments(Arrays.asList(assignmentCreation));

        if(isCorrect) {
            createType(sessionToken, typeCreation);
            TYPE type = getType(sessionToken, typeCreation.getCode());
            assertEquals(type.getPropertyAssignments().size(), 1);

            PropertyAssignment assignment = type.getPropertyAssignments().get(0);
            assertEquals(assignment.getPatternType(), patternType);
            assertEquals(assignment.getPattern(), pattern);
        } else {
            assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createType(sessionToken, typeCreation);
                }
            }, "Pattern validation can not be assigned for property of data type: " + dataType);
        }

    }

    @DataProvider(name = DATA_TYPE_PATTERN_VALIDATION_PROVIDER)
    protected Object[][] providePatternsAndDataTypes()
    {
        List<Object[]> result = new ArrayList<>();
        for(Object dataType : DataType.values())
        {
            for(Object[] patterns : properPatterns())
            {
                List<Object> test = new ArrayList<>();
                test.add(dataType);
                test.add(DATA_TYPES_THAT_ACCEPT_PATTERN.contains(dataType));
                test.addAll(Arrays.asList(patterns));
                result.add(test.toArray(new Object[4]));
            }
        }
        return result.toArray(new Object[result.size()][4]);
    }


    private Object[][] properPatterns()
    {
        return new Object[][] { {"PATTERN", ".*"}, {"PATTERN", "[a-c]{3}\\d"}, {"PATTERN", "\\d{3}"},
                { "RANGES", "1-10"}, {"RANGES", "(-1)-5"}, {"RANGES", "100-10"},
                { "VALUES", "\"a\", \"b\", \"c\""}, { "VALUES", "\"Foo\", \"Bar\""},
                { "VALUES", "\"Piotr\", \"Juan\", \"Viktor\", \"Adam\", \"Mihai\""}
        };
    }

    @Test(dataProvider = DATA_TYPE_PATTERN_VALIDATION_UPDATE_PROVIDER)
    public void testCreateAndUpdatePattern(DataType dataType, String patternType, String pattern, String patternTypeUpdate, String patternUpdate)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        // PROPERTY CREATION
        final String propertyCode = "MY_TEST_PROPERTY";
        createPropertyType(sessionToken, propertyCode, dataType);

        // TYPE CREATION
        PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
        assignmentCreation.setPropertyTypeId(new PropertyTypePermId(propertyCode));
        assignmentCreation.setPatternType(patternType);
        assignmentCreation.setPattern(pattern);

        final TYPE_CREATION typeCreation = newTypeCreation();
        typeCreation.setCode("NEW_ENITTY_TYPE");
        typeCreation.setDescription("test description");
        typeCreation.setPropertyAssignments(Arrays.asList(assignmentCreation));

        createType(sessionToken, typeCreation);
        TYPE type = getType(sessionToken, typeCreation.getCode());
        assertEquals(type.getPropertyAssignments().size(), 1);

        PropertyAssignment assignment = type.getPropertyAssignments().get(0);
        assertEquals(assignment.getPatternType(), patternType);
        assertEquals(assignment.getPattern(), pattern);

        // TYPE UPDATE
        PropertyAssignmentCreation propertyAssignmentForUpdate = new PropertyAssignmentCreation();
        propertyAssignmentForUpdate.setPropertyTypeId(new PropertyTypePermId(propertyCode));
        propertyAssignmentForUpdate.setPatternType(patternTypeUpdate);
        propertyAssignmentForUpdate.setPattern(patternUpdate);

        final TYPE_UPDATE typeUpdate = newTypeUpdate(typeCreation.getCode());
        typeUpdate.getPropertyAssignments().set(propertyAssignmentForUpdate);

        updateType(sessionToken, typeUpdate);
        TYPE typeUpdated = getType(sessionToken, typeCreation.getCode());
        assertEquals(typeUpdated.getPropertyAssignments().size(), 1);

        PropertyAssignment assignmentUpdated = typeUpdated.getPropertyAssignments().get(0);
        assertEquals(assignmentUpdated.getPatternType(), patternTypeUpdate);
        assertEquals(assignmentUpdated.getPattern(), patternUpdate);
    }

    @DataProvider(name = DATA_TYPE_PATTERN_VALIDATION_UPDATE_PROVIDER)
    protected Object[][] providePatternsAndDataTypesForUpdate()
    {
        List<Object[]> result = new ArrayList<>();
        for(Object dataType : DATA_TYPES_THAT_ACCEPT_PATTERN)
        {
            Object[][] patterns = properPatterns();
            int size = patterns.length;
            for(int i=0; i<size; i++)
            {
                List<Object> test = new ArrayList<>();
                test.add(dataType);
                test.addAll(Arrays.asList(patterns[i]));
                test.addAll(Arrays.asList(patterns[(i+1)%size]));
                result.add(test.toArray(new Object[5]));
            }
        }
        return result.toArray(new Object[result.size()][5]);
    }

    @Test(dataProvider = UPDATE_ENTITY_AND_PATTERN_PROVIDER)
    public void testCreateAndUpdateEntityPatternProperty(DataType dataType, String baseInput, String updatePatternType, String updatePattern, String updateValue, boolean shouldFail)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final String propertyCode = "MY_TEST_PROPERTY";
        createPropertyType(sessionToken, propertyCode, dataType);

        PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
        assignmentCreation.setPropertyTypeId(new PropertyTypePermId(propertyCode));
        assignmentCreation.setPatternType("PATTERN");
        assignmentCreation.setPattern(".*");

        final TYPE_CREATION typeCreation = newTypeCreation();
        typeCreation.setCode("NEW_ENITTY_TYPE");
        typeCreation.setDescription("test description");
        typeCreation.setPropertyAssignments(Arrays.asList(assignmentCreation));

        createType(sessionToken, typeCreation);
        TYPE type = getType(sessionToken, typeCreation.getCode());

        final ENTITY_CREATION creation = newEntityCreation(type, "NEW_ENTITY_1");
        creation.setProperty(propertyCode, baseInput);
        ENTITY_ID entityId = createEntity(sessionToken, creation);
        ENTITY entity = getEntity(sessionToken, entityId);
        if(dataType == DataType.TIMESTAMP) {
            assertTrue(entity.getProperty(propertyCode).toString().startsWith(baseInput));
        } else {
            assertEquals(entity.getProperty(propertyCode), baseInput);
        }

        // TYPE UPDATE
        PropertyAssignmentCreation propertyAssignmentForUpdate = new PropertyAssignmentCreation();
        propertyAssignmentForUpdate.setPropertyTypeId(new PropertyTypePermId(propertyCode));
        propertyAssignmentForUpdate.setPatternType(updatePatternType);
        propertyAssignmentForUpdate.setPattern(updatePattern);

        final TYPE_UPDATE typeUpdate = newTypeUpdate(typeCreation.getCode());
        typeUpdate.getPropertyAssignments().set(propertyAssignmentForUpdate);

        updateType(sessionToken, typeUpdate);
        TYPE typeUpdated = getType(sessionToken, typeCreation.getCode());
        assertEquals(typeUpdated.getPropertyAssignments().size(), 1);

        PropertyAssignment assignmentUpdated = typeUpdated.getPropertyAssignments().get(0);
        assertEquals(assignmentUpdated.getPatternType(), updatePatternType);
        assertEquals(assignmentUpdated.getPattern(), updatePattern);

        // ENTITY UPDATE
        final ENTITY_UPDATE entityUpdate = newEntityUpdate(entityId.toString());
        entityUpdate.setProperty(propertyCode, updateValue);

        if(shouldFail) {
            assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateEntity(sessionToken, entityUpdate);
                }
            }, dataType == DataType.TIMESTAMP ? "is not matching defined pattern!"
                    : "Value: '"+updateValue+"' is not matching defined pattern!");

        } else {
            updateEntity(sessionToken, entityUpdate);
            ENTITY entityUpdated = getEntity(sessionToken, entityId);
            if(dataType == DataType.TIMESTAMP) {
                assertTrue(entityUpdated.getProperty(propertyCode).toString().startsWith(updateValue));
            } else {
                assertEquals(entityUpdated.getProperty(propertyCode), updateValue);
            }

        }
    }

    @DataProvider(name = UPDATE_ENTITY_AND_PATTERN_PROVIDER)
    protected Object[][] getDataForUpdate()
    {
        List<Object[]> result = new ArrayList<>();
        for(DataType dataType : DATA_TYPES_THAT_ACCEPT_PATTERN)
        {
            if(dataType == DataType.DATE)
            {
                result.add(new Object[] { dataType, "2022-05-16", "PATTERN", "2022.*", "2022-04-08", false});
                result.add(new Object[] { dataType, "2022-05-16", "PATTERN", "2022.*", "2024-05-16", true});
                result.add(new Object[] { dataType, "2022-04-08", "VALUES", "\"2022-04-08\"", "2022-04-08", false});
                result.add(new Object[] { dataType, "2022-04-08", "VALUES", "\"2022-04-08\"", "2024-04-08", true});
            } else if(dataType == DataType.TIMESTAMP)
            {
                result.add(new Object[] { dataType, "2022-05-16 04:22", "PATTERN", "2022.*", "2022-05-16 04:24:00", false});
                result.add(new Object[] { dataType, "2022-05-16 04:22", "PATTERN", "2022.*", "2024-05-16 04:22:00", true});
            } else if(dataType == DataType.REAL) {
                result.add(new Object[] { dataType, "111.0", "PATTERN", "[1-3]{3}\\.0", "123.0", false});
                result.add(new Object[] { dataType, "123.0", "PATTERN", "[1-3]{3}\\.0", "124.0", true});
                result.add(new Object[] { dataType, "1.0", "RANGES", "1-10", "5.1", false});
                result.add(new Object[] { dataType, "1.0", "RANGES", "1-10", "11.0", true});
                result.add(new Object[] { dataType, "2.0", "VALUES", "\"2.0\"", "2.0", false});
                result.add(new Object[] { dataType, "2.0", "VALUES", "\"2.0\"", "2.1", true});
            } else if(dataType == DataType.HYPERLINK) {
                result.add(new Object[] { dataType, "http://a.com", "PATTERN", "http://.*", "http://b.com", false});
                result.add(new Object[] { dataType, "http://a.com", "PATTERN", "http://.*", "https://a.com", true});
                result.add(new Object[] { dataType, "http://b.com", "VALUES", "\"http://b.com\"", "http://b.com", false});
                result.add(new Object[] { dataType, "http://b.com", "VALUES", "\"http://b.com\"", "http://bc.com", true});
            }else {
                result.add(new Object[] { dataType, "111", "PATTERN", "[1-3]{3}", "123", false});
                result.add(new Object[] { dataType, "123", "PATTERN", "[1-3]{3}", "124", true});
                result.add(new Object[] { dataType, "1", "RANGES", "1-10", "5", false});
                result.add(new Object[] { dataType, "1", "RANGES", "1-10", "11", true});
                result.add(new Object[] { dataType, "1", "VALUES", "\"1\"", "1", false});
                result.add(new Object[] { dataType, "1", "VALUES", "\"1\"", "2", true});
            }
        }
        return result.toArray(new Object[result.size()][6]);
    }

    protected void createPropertyType(String sessionToken, String propertyCode, DataType dataType)
    {
        PropertyTypeCreation propertyTypeCreation = new PropertyTypeCreation();
        propertyTypeCreation.setCode(propertyCode);
        propertyTypeCreation.setDataType(dataType);
        if(dataType == DataType.CONTROLLEDVOCABULARY) {
            VocabularyCreation vocabularyCreation = new VocabularyCreation();
            vocabularyCreation.setCode("TEST_VOCABULARY_PATTERN");

            VocabularyTermCreation vtCreation = new VocabularyTermCreation();
            vtCreation.setCode("TERM1");
            vtCreation.setLabel("term1");

            vocabularyCreation.setTerms(Arrays.asList(vtCreation));

            List<VocabularyPermId> ids = v3api.createVocabularies(sessionToken, Arrays.asList(vocabularyCreation));
            propertyTypeCreation.setVocabularyId(ids.get(0));
        }
        propertyTypeCreation.setLabel("label");
        propertyTypeCreation.setDescription("description");

        v3api.createPropertyTypes(sessionToken, Arrays.asList(propertyTypeCreation));
    }

}
