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

import java.io.BufferedReader;
import java.io.Reader;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;

/**
 * <code>IReaderParser</code> is able to parse a given {@link Reader} and to returns <code>Object</code> instances.
 * 
 * @author Christian Ribeaud
 */
public interface IReaderParser<E>
{

    /**
     * Parses given <code>Reader</code>. Encapsulates given <code>Reader</code> in a {@link BufferedReader} for
     * better performance (if not already done).
     * <p>
     * Note that this does not close given <code>Parser</code>. It is your responsability to do so.
     * </p>
     * 
     * @param lineFilter you could define a filter for the lines found in given <code>reader</code>.
     * @return a <code>List</code> of elements.
     * @throws CheckedExceptionTunnel if an <code>IOException</code> occurs.
     */
    public List<E> parse(Reader reader, ILineFilter lineFilter) throws CheckedExceptionTunnel;

    /**
     * Parses given <code>Reader</code>. Encapsulates given <code>Reader</code> in a {@link BufferedReader} for
     * better performance (if not already done).
     * <p>
     * Note that this does not close given <code>Parser</code>. It is your responsability to do so.
     * </p>
     * 
     * @return a <code>List</code> of elements.
     * @throws CheckedExceptionTunnel if an <code>IOException</code> occurs.
     */
    public List<E> parse(Reader reader) throws CheckedExceptionTunnel;

    /**
     * Sets the <code>IParserObjectFactory</code>.
     * <p>
     * Typically, the given <code>factory</code> transforms a line into an element.
     * </p>
     */
    public void setObjectFactory(IParserObjectFactory<E> factory);

    public void setPropertyMapperFactory(IPropertyMapperFactory factory);
}
