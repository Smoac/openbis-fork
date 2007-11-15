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

package ch.systemsx.cisd.bds.hcs;

import ch.systemsx.cisd.bds.DataStructureException;
import ch.systemsx.cisd.bds.IFormattedData;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.ILink;
import ch.systemsx.cisd.bds.storage.INode;

/**
 * {@link IFormattedData} extension specific to <i>HCS (High-Content Screening) with Images</i>.
 * 
 * @author Christian Ribeaud
 */
public interface IHCSFormattedData
{

    /**
     * For given <var>channel</var>, given <var>plateLocation</var> and given <var>wellLocation</var> returns the
     * corresponding <code>INode</code> (found in <code>data/standard</code> directory).
     * 
     * @return this could be, for instance, a {@link ILink} pointing to the <code>data/original</code> directory or a
     *         {@link IFile} that can be extracted somewhere. Never returns <code>null</code>.
     * @throws DataStructureException if a problem occurs while trying to find out the node in the whole data structure.
     */
    public INode getNodeAt(final int channel, final Location plateLocation, final Location wellLocation)
            throws DataStructureException;
}
