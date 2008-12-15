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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateFormatUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;

/**
 * Test cases for corresponding {@link PropertyValidator} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = PropertyValidator.class)
public final class PropertyValidatorTest
{

    private final static PropertyValidator createPropertyValidator()
    {
        return new PropertyValidator();
    }

    private final static PropertyTypePE createPropertyType(final EntityDataType entityDataType)
    {
        final PropertyTypePE propertyType = new PropertyTypePE();
        propertyType.setSimpleCode("PROP");
        final DataTypePE dataType = new DataTypePE();
        dataType.setCode(entityDataType);
        propertyType.setType(dataType);
        return propertyType;
    }

    private final static VocabularyTermPE createVocabularyTerm(final String code)
    {
        final VocabularyTermPE vocabularyTermPE = new VocabularyTermPE();
        vocabularyTermPE.setCode(code);
        return vocabularyTermPE;
    }

    private final static PropertyTypePE createBooleanPropertyType()
    {
        final PropertyTypePE propertyType = createPropertyType(EntityDataType.BOOLEAN);
        return propertyType;
    }

    private final static PropertyTypePE createControlledVocabularyPropertyType()
    {
        final PropertyTypePE propertyType = createPropertyType(EntityDataType.CONTROLLEDVOCABULARY);
        final VocabularyPE vocabularyPE = new VocabularyPE();
        final List<VocabularyTermPE> terms = new ArrayList<VocabularyTermPE>();
        terms.add(createVocabularyTerm("RED"));
        terms.add(createVocabularyTerm("YELLOW"));
        terms.add(createVocabularyTerm("GREEN"));
        vocabularyPE.setTerms(terms);
        propertyType.setVocabulary(vocabularyPE);
        return propertyType;
    }

    private final static PropertyTypePE createRealPropertyType()
    {
        final PropertyTypePE propertyType = createPropertyType(EntityDataType.REAL);
        return propertyType;
    }

    private final static PropertyTypePE createIntegerPropertyType()
    {
        final PropertyTypePE propertyType = createPropertyType(EntityDataType.INTEGER);
        return propertyType;
    }

    private final static PropertyTypePE createTimestampPropertyType()
    {
        final PropertyTypePE propertyType = createPropertyType(EntityDataType.TIMESTAMP);
        return propertyType;
    }

    private final static PropertyTypePE createVarcharPropertyType()
    {
        final PropertyTypePE propertyType = createPropertyType(EntityDataType.VARCHAR);
        return propertyType;
    }

    @SuppressWarnings("unused")
    @DataProvider
    private final static Object[][] getNonWorkingValues()
    {
        return new Object[][]
            {
                { createTimestampPropertyType(), DateFormatUtils.format(new Date(), "yyyy") },
                { createIntegerPropertyType(), "a" },
                { createIntegerPropertyType(), "1.1" },
                { createRealPropertyType(), "b" },
                { createControlledVocabularyPropertyType(), "BLACK" },
                { createBooleanPropertyType(), "BOB" }, };
    }

    @SuppressWarnings("unused")
    @DataProvider
    private final static Object[][] getWorkingValues()
    {
        return new Object[][]
            {
                        { createVarcharPropertyType(), "" },
                        { createVarcharPropertyType(), "varchar" },
                        {
                                createTimestampPropertyType(),
                                DateFormatUtils.format(new Date(),
                                        PropertyValidator.CANONICAL_DATE_PATTERN) },
                        { createIntegerPropertyType(), "1" },
                        { createRealPropertyType(), "1" },
                        { createRealPropertyType(), "1.1" },
                        { createControlledVocabularyPropertyType(), "RED" },
                        { createControlledVocabularyPropertyType(), "red" },
                        { createBooleanPropertyType(), "yes" },
                        { createBooleanPropertyType(), "1" },
                        { createBooleanPropertyType(), "true" } };
    }

    @Test
    public final void testValidatePropertyValueWithNull()
    {
        boolean fail = true;
        try
        {
            createPropertyValidator().validatePropertyValue(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test(dataProvider = "getWorkingValues")
    public final void testValidatePropertyValue(final PropertyTypePE propertyType,
            final String value)
    {
        final PropertyValidator propertyValidator = createPropertyValidator();
        propertyValidator.validatePropertyValue(propertyType, value);
    }

    @Test(dataProvider = "getNonWorkingValues")
    public final void testValidatePropertyValueFailed(final PropertyTypePE propertyType,
            final String value)
    {
        final PropertyValidator propertyValidator = createPropertyValidator();
        try
        {
            propertyValidator.validatePropertyValue(propertyType, value);
            fail(String.format("'%s' expected.", UserFailureException.class.getSimpleName()));
        } catch (final UserFailureException ex)
        {
            // Nothing to do here.
        }
    }
}
