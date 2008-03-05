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

import ch.systemsx.cisd.common.annotation.BeanProperty;

/**
 * Test cases for corresponding {@link AbstractParserObjectFactory} class.
 * 
 * @author Christian Ribeaud
 */
public final class AbstractParserObjectFactoryTest
{

    private final static IAliasPropertyMapper createPropertyMapper()
    {
        return new DefaultAliasPropertyMapper(new String[]
            { "name", "description", "number" });
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
        final IAliasPropertyMapper propertyMapper = new DefaultAliasPropertyMapper(new String[]
            { "name", "description", "IsNotIn" });
        try
        {
            new BeanFactory(Bean.class, propertyMapper);
            fail("Following properties '[isnotin]' are not part of 'Bean'.");
        } catch (UnmatchedPropertiesException ex)
        {
            assertEquals("Following header columns are not part of 'Bean': IsNotIn", ex.getMessage());
        }
    }

    @Test
    public final void testMandatoryFields()
    {
        DefaultAliasPropertyMapper propertyMapper = new DefaultAliasPropertyMapper(new String[]
            { "description" });
        try
        {
            BeanFactory beanFactory = new BeanFactory(Bean.class, propertyMapper);
            String[] lineTokens = new String[]
                { "1. experiment" };
            beanFactory.createObject(lineTokens);
            fail("Field/Property name 'name' is mandatory.");
        } catch (MandatoryPropertyMissingException ex)
        {
            assertEquals(String.format(MandatoryPropertyMissingException.MESSAGE_FORMAT, "name"), ex.getMessage());
        }
    }

    @Test
    public final void testTooManyDataColumns()
    {
        final IAliasPropertyMapper propertyMapper = createPropertyMapper();
        BeanFactory beanFactory = new BeanFactory(Bean.class, propertyMapper);
        String[] lineTokens = (String[]) ArrayUtils.add(createDefaultLineTokens(), "notUsed");
        Bean bean = beanFactory.createObject(lineTokens);
        checkBean(bean);
    }

    @Test
    public final void testNotEnoughDataColumns()
    {
        final IAliasPropertyMapper propertyMapper = createPropertyMapper();
        BeanFactory beanFactory = new BeanFactory(Bean.class, propertyMapper);
        String[] defaultTokens = createDefaultLineTokens();
        String[] lineTokens = (String[]) ArrayUtils.remove(defaultTokens, defaultTokens.length - 1);
        String msg = String.format(IndexOutOfBoundsException.MESSAGE_FORMAT, 2, lineTokens.length);
        try
        {
            beanFactory.createObject(lineTokens);
            fail(msg);
        } catch (IndexOutOfBoundsException ex)
        {
            assertEquals(msg, ex.getMessage());
        }
    }

    @Test
    public final void testRegisterConverterWithNull()
    {
        final IAliasPropertyMapper propertyMapper = createPropertyMapper();
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

        BeanFactory(final Class<Bean> clazz, final IAliasPropertyMapper propertyMapper)
        {
            super(clazz, propertyMapper);
        }

    }

    public final static class Bean
    {
        @BeanProperty
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