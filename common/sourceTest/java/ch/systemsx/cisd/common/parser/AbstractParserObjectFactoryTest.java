/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.parser;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.annotation.Mandatory;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Test cases for corresponding {@link AbstractParserObjectFactory} class.
 * 
 * @author Christian Ribeaud
 */
public final class AbstractParserObjectFactoryTest
{

    private final static IPropertyMapper createPropertyMapper()
    {
        return new HeaderFilePropertyMapper(new String[]
            { "Name", "Description", "Number" });
    }

    private final static String[] createDefaultLineTokens()
    {
        return new String[]
            { "Bean Name", "Bean Description", "1" };
    }

    private final void checkBean(Bean bean)
    {
        assertEquals("Bean Name", bean.name);
        assertEquals("Bean Description", bean.description);
        assertEquals(1, bean.number);
    }

    @Test(expectedExceptions = AssertionError.class)
    public final void testConstructorWithNull()
    {
        new BeanFactory(null, null);
    }

    @Test
    public final void testPropertyMapperWithNoExperimentProperties()
    {
        IPropertyMapper propertyMapper = new HeaderFilePropertyMapper(new String[]
            { "Name", "Description", "IsNotIn" });
        try
        {
            new BeanFactory(Bean.class, propertyMapper);
            fail("Following properties '[isnotin]' are not part of 'Bean'.");
        } catch (UserFailureException ex)
        {
            assertEquals("Following properties '[isnotin]' are not part of 'Bean'.", ex.getMessage());
        }
    }

    @Test
    public final void testMandatoryFields()
    {
        HeaderFilePropertyMapper propertyMapper = new HeaderFilePropertyMapper(new String[]
            { "description" });
        try
        {
            BeanFactory beanFactory = new BeanFactory(Bean.class, propertyMapper);
            String[] lineTokens = new String[]
                { "1. experiment" };
            beanFactory.createObject(lineTokens);
            fail("Field/Property name 'name' is mandatory.");
        } catch (UserFailureException ex)
        {
            assertEquals("Field/Property name 'name' is mandatory.", ex.getMessage());
        }
    }

    @Test
    public final void testTooManyDataColumns()
    {
        IPropertyMapper propertyMapper = createPropertyMapper();
        BeanFactory beanFactory = new BeanFactory(Bean.class, propertyMapper);
        String[] lineTokens = (String[]) ArrayUtils.add(createDefaultLineTokens(), "notUsed");
        Bean bean = beanFactory.createObject(lineTokens);
        checkBean(bean);
    }

    @Test
    public final void testNotEnoughDataColumns()
    {
        IPropertyMapper propertyMapper = createPropertyMapper();
        BeanFactory beanFactory = new BeanFactory(Bean.class, propertyMapper);
        String[] defaultTokens = createDefaultLineTokens();
        String[] lineTokens = (String[]) ArrayUtils.remove(defaultTokens, defaultTokens.length - 1);
        String msg = String.format("Not enough tokens are available (index: 2, available: 2)", 5, lineTokens.length);
        try
        {
            beanFactory.createObject(lineTokens);
            fail(msg);
        } catch (UserFailureException ex)
        {
            assertEquals(msg, ex.getMessage());
        }
    }

    @Test
    public final void testRegisterConverterWithNull()
    {
        IPropertyMapper propertyMapper = createPropertyMapper();
        BeanFactory beanFactory = new BeanFactory(Bean.class, propertyMapper);
        try
        {
            beanFactory.registerConverter(null, null);
            fail("Null type is not allowed.");
        } catch (AssertionError ex)
        {
            // Nothing to do here.
        }
        beanFactory.registerConverter(String.class, null);
    }

    //
    // Helper Classes
    //

    private final static class BeanFactory extends AbstractParserObjectFactory<Bean>
    {

        BeanFactory(Class<Bean> clazz, IPropertyMapper propertyMapper)
        {
            super(clazz, propertyMapper);
        }

    }

    public final static class Bean
    {
        @Mandatory
        private String name;

        private String description;

        private int number;

        public final String getName()
        {
            return name;
        }

        public final void setName(String name)
        {
            this.name = name;
        }

        public final int getNumber()
        {
            return number;
        }

        public final void setNumber(int number)
        {
            this.number = number;
        }

        public final String getDescription()
        {
            return description;
        }

        public final void setDescription(String description)
        {
            this.description = description;
        }
    }
}