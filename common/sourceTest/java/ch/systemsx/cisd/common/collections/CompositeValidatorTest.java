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

package ch.systemsx.cisd.common.collections;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

/**
 * Tests for {@link CompositeValidator}.
 * 
 * @author Christian Ribeaud
 */
public final class CompositeValidatorTest
{

    @Test
    public final void testNonNullConvention()
    {
        boolean exceptionThrown = false;
        final CompositeValidator<Object> validator = new CompositeValidator<Object>();
        try
        {
            validator.addValidator(null);
        } catch (final AssertionError e)
        {
            exceptionThrown = true;
        }
        assertTrue("Null validator not allowed.", exceptionThrown);

        exceptionThrown = false;
        try
        {
            validator.removeValidator(null);
        } catch (final AssertionError e)
        {
            exceptionThrown = true;
        }
        assertTrue("Null validator not allowed.", exceptionThrown);
    }

    @Test
    public final void testIsValid()
    {
        final CompositeValidator<Object> validator = new CompositeValidator<Object>();
        validator.addValidator(new IValidator<Object>()
            {

                //
                // Validator
                //

                public final boolean isValid(final Object object)
                {
                    return object instanceof String;
                }
            });
        validator.addValidator(new IValidator<Object>()
            {

                //
                // Validator
                //

                public final boolean isValid(final Object object)
                {
                    return object instanceof Long;
                }
            });
        assertEquals(true, validator.isValid(1L));
        assertEquals(false, validator.isValid(true));
        assertEquals(false, validator.isValid(null));
    }
}
