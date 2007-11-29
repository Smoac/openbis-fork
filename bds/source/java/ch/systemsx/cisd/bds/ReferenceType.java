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

package ch.systemsx.cisd.bds;

/**
 * Type of reference between a file in <tt>data/standard</tt> and <tt>data/original</tt>.
 * 
 * @author Franz-Josef Elmer
 */
public enum ReferenceType
{
    IDENTICAL("I"), TRANSFORMED("T");

    /**
     * Resolves the reference type from the specified short name.
     */
    public final static ReferenceType resolveByShortName(final String shortName)
    {
        for (ReferenceType type : values())
        {
            if (type.getShortName().equals(shortName))
            {
                return type;
            }
        }
        throw new IllegalArgumentException(String.format("No type for short name '%s'", shortName));
    }

    private final String shortName;

    private ReferenceType(final String shortName)
    {
        this.shortName = shortName;
    }

    /**
     * Returns the short name of this type.
     */
    public final String getShortName()
    {
        return shortName;
    }

}
