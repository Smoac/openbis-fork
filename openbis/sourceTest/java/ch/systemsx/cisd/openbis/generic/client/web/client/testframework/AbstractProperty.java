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

package ch.systemsx.cisd.openbis.generic.client.web.client.testframework;

import junit.framework.Assert;

import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IInvalidationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Invalidation;

/**
 * Abstract superclass of a generic {@link IProperty} which implements convenient property value
 * expectations. It uses a fluent API approach for its methods to prepare expectations.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractProperty<C extends IPropertyChecker<?>> implements IProperty<C>
{
    private final String key;

    protected final C checker;

    protected final String message;

    /**
     * Creates an instance with the specified property name for the specified checker.
     */
    protected AbstractProperty(final String key, final C checker)
    {
        this.key = key;
        this.checker = checker;
        message = "Property '" + key + "':";
    }

    public C by(final IValueAssertion<?> valueAssertion)
    {
        checker.property(key, valueAssertion);
        return checker;
    }

    /**
     * Sets assertion that the toString()ed property value equals the specified string.
     */
    public C asString(final String expectedValue)
    {
        return by(new IValueAssertion<Object>()
            {
                public void assertValue(final Object value)
                {
                    Assert.assertEquals(message, expectedValue, String.valueOf(value));
                }
            });
    }

    /**
     * Sets assertion that the toString()ed property value matches the specified regular expression.
     */
    public C matchingPattern(final String pattern)
    {
        return by(new IValueAssertion<Object>()
            {
                public void assertValue(final Object value)
                {
                    final String valueAsString = String.valueOf(value);
                    if (valueAsString.matches(pattern) == false)
                    {
                        Assert.fail(message + " expected pattern <" + pattern + "> but was <"
                                + value + ">");
                    }
                }
            });
    }

    /**
     * Sets assertion that the property value equals the specified object.
     */
    public C asObject(final Object expectedValue)
    {
        return by(new IValueAssertion<Object>()
            {
                public void assertValue(final Object value)
                {
                    Assert.assertEquals(message, expectedValue, value);
                }
            });
    }

    /**
     * Sets assertion that the property value is of type {@link ICodeHolder} with a code equals
     * the specified code.
     */
    public C asCode(final String expectedCode)
    {
        return by(new IValueAssertion<ICodeHolder>()
            {
                public void assertValue(final ICodeHolder code)
                {
                    Assert.assertEquals(message, expectedCode, code.getCode());
                }
            });
    }

    /**
     * Sets assertion that the property value is of type {@link IInvalidationProvider} with no
     * {@link Invalidation} object.
     */
    public C asValidEntity()
    {
        return by(new IValueAssertion<IInvalidationProvider>()
            {
                public void assertValue(final IInvalidationProvider provider)
                {
                    final Invalidation invalidation = provider.getInvalidation();
                    Assert.assertNull(message + " expected to be a valid entity.", invalidation);
                }
            });
    }

    /**
     * Sets assertion that the property value is of type {@link IInvalidationProvider} with an
     * {@link Invalidation} object.
     */
    public C asInvalidEntity()
    {
        return by(new IValueAssertion<IInvalidationProvider>()
            {
                public void assertValue(final IInvalidationProvider provider)
                {
                    final Invalidation invalidation = provider.getInvalidation();
                    Assert.assertNotNull(message + " expected to be an invalid entity.",
                            invalidation);
                }
            });
    }

    /**
     * Sets assertion that the property value is of type {@link EntityProperty} with a value equals
     * the specified string.
     */
    public C asProperty(final String expectedValue)
    {
        return by(new IValueAssertion<IEntityProperty>()
            {
                public void assertValue(final IEntityProperty value)
                {
                    Assert.assertEquals(message, expectedValue, value.tryGetAsString());
                }
            });
    }

}
