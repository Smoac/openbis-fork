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

/**
 * Implementation know how to deal with given parsed text line and convert it into an appropriate <code>Object</code>.
 * A <code>IParserObjectFactory</code> needs a <code>IPropertyMapper</code> to do its job. This
 * <code>IPropertyMapper</code> helps to map <code>Object</code> properties to given tokens.
 * <p>
 * A <code>IParserObjectFactory</code> is typically registered in {@link IReaderParser}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public interface IParserObjectFactory<E>
{

    /**
     * This <code>IParserObjectFactory</code> implementation does nothing and returns the passed
     * <code>lineTokens</code> as <code>String[]</code>.
     * 
     * @author Christian Ribeaud
     */
    public final static IParserObjectFactory<String[]> DO_NOTHING_OBJECT_FACTORY = new IParserObjectFactory<String[]>()
        {

            // /////////////////////////////////////////////////////
            // IParserObjectFactory
            // /////////////////////////////////////////////////////

            public String[] createObject(String[] lineTokens)
            {
                return lineTokens;
            }

            public void setPropertyMapper(IPropertyMapper propertyMapper)
            {
            }
        };

    /**
     * Parses given text line and returns an appropriate <i>Object</i>.
     */
    public E createObject(String[] lineTokens);

    public void setPropertyMapper(IPropertyMapper propertyMapper);
}