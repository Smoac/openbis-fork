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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateFormatUtils;
import org.jmock.Expectations;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.util.PropertyValidator;
import ch.systemsx.cisd.openbis.generic.server.util.PropertyValidator.SupportedDatePattern;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * Test cases for corresponding {@link PropertyValidator} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = PropertyValidator.class)
public final class PropertyValidatorTest extends AbstractBOTest
{

    private final PropertyValidator createPropertyValidator()
    {
        return new PropertyValidator(daoFactory);
    }

    private final static PropertyTypePE createPropertyType(final DataTypeCode entityDataType)
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
        final PropertyTypePE propertyType = createPropertyType(DataTypeCode.BOOLEAN);
        return propertyType;
    }

    private final static PropertyTypePE createRealPropertyType()
    {
        final PropertyTypePE propertyType = createPropertyType(DataTypeCode.REAL);
        return propertyType;
    }

    private final static PropertyTypePE createIntegerPropertyType()
    {
        final PropertyTypePE propertyType = createPropertyType(DataTypeCode.INTEGER);
        return propertyType;
    }

    private final static PropertyTypePE createTimestampPropertyType()
    {
        final PropertyTypePE propertyType = createPropertyType(DataTypeCode.TIMESTAMP);
        return propertyType;
    }

    private final static PropertyTypePE createVarcharPropertyType()
    {
        final PropertyTypePE propertyType = createPropertyType(DataTypeCode.VARCHAR);
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
                                        SupportedDatePattern.CANONICAL_DATE_PATTERN.getPattern()) },
                        {
                                createTimestampPropertyType(),
                                DateFormatUtils.format(new Date(),
                                        SupportedDatePattern.US_DATE_TIME_24_PATTERN.getPattern()) },
                        { createIntegerPropertyType(), "1" },
                        { createRealPropertyType(), "1" },
                        { createRealPropertyType(), "1.1" },
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

    //
    // Controlled Vocabulary with DAO access
    //

    private final static PropertyTypePE createControlledVocabularyPropertyType()
    {
        final PropertyTypePE propertyType = createPropertyType(DataTypeCode.CONTROLLEDVOCABULARY);
        final VocabularyPE vocabularyPE = new VocabularyPE();
        // terms list is currently not used in validation but lets keep it here
        final List<VocabularyTermPE> terms = new ArrayList<VocabularyTermPE>();
        terms.add(createVocabularyTerm("GOODVALUE"));
        vocabularyPE.setTerms(terms);
        propertyType.setVocabulary(vocabularyPE);
        return propertyType;
    }

    @Test
    public final void testValidateControlledVocabularyPropertyValue()
    {
        final PropertyTypePE propertyType = createControlledVocabularyPropertyType();
        final VocabularyPE vocabulary = propertyType.getVocabulary();
        final String value = "goodValue";
        final String code = value.toUpperCase();
        final VocabularyTermPE term = createVocabularyTerm(code);
        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyTermByCode(vocabulary, code);
                    will(returnValue(term));
                }
            });
        final PropertyValidator propertyValidator = createPropertyValidator();
        propertyValidator.validatePropertyValue(propertyType, value);
    }

    @Test
    public final void testValidateControlledVocabularyPropertyValueFailed()
    {
        PropertyTypePE propertyType = createControlledVocabularyPropertyType();
        final VocabularyPE vocabulary = propertyType.getVocabulary();
        final String value = "wrongValue";
        final String code = value.toUpperCase();
        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyTermByCode(vocabulary, code);
                    will(returnValue(null));
                }
            });
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
