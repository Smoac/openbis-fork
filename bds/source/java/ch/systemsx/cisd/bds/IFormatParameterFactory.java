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

import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.INode;

/**
 * Interface to be implemented if you want to set a different <code>IFormatParameterFactory</code>
 * in {@link FormatParameters}.
 * 
 * @author Christian Ribeaud
 */
public interface IFormatParameterFactory
{

    public final static IFormatParameterFactory DEFAULT_FORMAT_PARAMETER_FACTORY =
            new IFormatParameterFactory()
                {

                    //
                    // IFormatParameterFactory
                    //

                    public final FormatParameter createFormatParameter(final INode node)
                    {
                        if (node instanceof IFile)
                        {
                            final IFile file = (IFile) node;
                            return new FormatParameter(file.getName(), file.getStringContent()
                                    .trim());
                        }
                        return null;
                    }

                    public final FormatParameter createFormatParameter(final String name,
                            final String value)
                    {
                        return new FormatParameter(name, value);
                    }
                };

    /**
     * Creates a <code>FormatParameter</code> from given <var>node</var>.
     * 
     * @return <code>null</code> if no appropriate <code>FormatParameter</code> could be created
     *         from given <code>INode</code>.
     */
    public FormatParameter createFormatParameter(final INode node);

    /**
     * Creates a <code>FormatParameter</code> from given <var>value</var>.
     * 
     * @param name name of the format parameter. Usually it is one of the values returned by
     *            {@link Format#getMandatoryParameterNames()}.
     * @param value generic value that should help to construct the <code>FormatParameter</code>.
     * @return <code>null</code> if no appropriate <code>FormatParameter</code> could be created
     *         from given <code>value</code>.
     */
    public FormatParameter createFormatParameter(final String name, final String value);
}
