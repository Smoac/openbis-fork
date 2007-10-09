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
 * Enumeration of processing types.
 *
 * @author Franz-Josef Elmer
 */
public enum ProcessingType
{
    OTHER, RAW_DATA, COMPUTED_DATA;
    
    private static final String PROCESSING_TYPE = "processing_type";
    
    /**
     * Resolves the specified string representation of a processing type.
     * 
     * @return {@link #OTHER} if <code>processingTypeString</code> is unknown.
     */
    public static ProcessingType resolve(String processingTypeString)
    {
        ProcessingType type = valueOf(processingTypeString);
        return type == null ? OTHER : type;
    }
    
    public static ProcessingType loadFrom(IDirectory directory)
    {
        return resolve(Utilities.getString(directory, PROCESSING_TYPE));
    }
    
    public void saveTo(IDirectory directory)
    {
        directory.appendKeyValuePair(PROCESSING_TYPE, toString());
    }
}
