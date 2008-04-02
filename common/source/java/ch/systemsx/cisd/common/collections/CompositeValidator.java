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

import java.util.ArrayList;
import java.util.List;

/**
 * An <code>Validator</code> implementation which validates using a list of individual
 * <code>Validator</code>.
 * <p>
 * This is an <code>OR</code> operation: the tested object should only be validated by at least
 * one <code>Validator</code> to be valid.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class CompositeValidator<T> implements Validator<T>
{
    private final List<Validator<T>> validators;

    public CompositeValidator()
    {
        this.validators = new ArrayList<Validator<T>>();
    }

    public final void addValidator(final Validator<T> validator)
    {
        assert validator != null : "Unspecified validator.";
        validators.add(validator);
    }

    public final void removeValidator(final Validator<T> validator)
    {
        assert validator != null : "Unspecified validator.";
        validators.remove(validator);
    }

    //
    // Validator
    //

    public final boolean isValid(final T t)
    {
        for (final Validator<T> validator : validators)
        {
            if (validator.isValid(t))
            {
                return true;
            }
        }
        return false;
    }

}
