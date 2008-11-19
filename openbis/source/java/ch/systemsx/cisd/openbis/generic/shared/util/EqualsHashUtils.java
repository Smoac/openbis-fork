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

package ch.systemsx.cisd.openbis.generic.shared.util;

/**
 * Utility class containing methods useful in defining equals and hashCode methods.
 * 
 * @author Izabela Adamczyk
 */
public class EqualsHashUtils
{

    public static void assertDefined(final Object o, final String name)
    {
        if (o == null)
        {
            throw new IllegalStateException("Field should be defined but is null: " + name
                    + ". Equals operation cannot be performed.");
        }
    }

}
