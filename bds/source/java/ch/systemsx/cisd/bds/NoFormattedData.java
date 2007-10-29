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
 * Most simplest implementation of {@link IFormattedData}. It is associated with {@link UnknownFormat1_0}.
 *
 * @author Franz-Josef Elmer
 */
public class NoFormattedData extends AbstractFormattedData
{
    /**
     * Creates a new instance for the specified context. The format has to be backward-compatible with 
     * {@link UnknownFormat1_0}. The format parameters are ignored.
     */
    public NoFormattedData(FormattedDataContext context)
    {
        super(context);
    }

    /**
     * Returns {@link UnknownFormat1_0#UNKNOWN_1_0}.
     */
    public Format getFormat()
    {
        return UnknownFormat1_0.UNKNOWN_1_0;
    }
    
    @Override
    protected void assertValidFormatAndFormatParameters()
    {
        if (format.getCode().equals(getFormat().getCode()) == false)
        {
            throw new DataStructureException("Invalid format code: " + format.getCode());
        }
        if (format.getVersion().isBackwardsCompatibleWith(getFormat().getVersion()) == false)
        {
            throw new DataStructureException("Incompatible format version: " + format.getVersion());
            
        }
    }

}
