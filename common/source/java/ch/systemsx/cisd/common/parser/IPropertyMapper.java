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

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 *
 * @author Christian Ribeaud
 */
public interface IPropertyMapper
{
    
    public Property getProperty(String name);
    
    ///////////////////////////////////////////////////////
    // Helper Classes
    ///////////////////////////////////////////////////////

    /**
     *
     * 
     * @author Christian Ribeaud
     */
    public final static class Property {
        
        public final int column;
        
        public final String name;
        
        public final String format;

        protected Property(final int column, final String name, final String format)
        {
            this.column = column;
            this.name = name;
            this.format = format;
        }
        
        ///////////////////////////////////////////////////////
        // Object
        ///////////////////////////////////////////////////////
        
        @Override
        public final String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
        
    }
}