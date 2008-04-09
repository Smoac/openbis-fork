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

package ch.systemsx.cisd.common.collections;

/**
 * Defines a functor interface implemented by classes that perform a predicate test on an object.
 * <p>
 * A <code>Validator</code> is the object equivalent of an <code>if</code> statement. It uses
 * the input object to return a <code>true</code> or <code>false</code> value, and is often used
 * in validation or filtering.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public interface Validator<E>
{

    /**
     * Use the specified parameter to perform a test that returns <code>true</code> or
     * <code>false</code>.
     * 
     * @param object the typed object to evaluate
     * @return <code>true</code> or <code>false</code>
     */
    public boolean isValid(E object);

}
