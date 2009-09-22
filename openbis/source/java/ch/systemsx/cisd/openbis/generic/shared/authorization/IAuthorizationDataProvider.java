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

package ch.systemsx.cisd.openbis.generic.shared.authorization;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.IDatabaseInstanceFinder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.FilterPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * Interface of providers of data needed for authorization.
 * 
 * @author    Franz-Josef Elmer
 */
public interface IAuthorizationDataProvider extends IDatabaseInstanceFinder
{
    /**
     * Returns a list of all groups.
     */
    public List<GroupPE> listGroups();

    /**
     * Returns the project of the experiment to which the specified data set belongs.
     * 
     * @return <code>null</code> if no data set found.
     */
    public ProjectPE tryToGetProject(String dataSetCode);

    /**
     * Returns the group of an entity with given <var>entityKind</var> and <var>techId</var>
     * 
     * @return <code>null</code> if entity has no group set.
     */
    public GroupPE tryToGetGroup(EntityWithGroupKind entityKind, TechId techId);

    /**
     * Returns the sample with given <var>techId</var>.
     */
    public SamplePE getSample(TechId techId);

    /**
     * Returns the filter with given <var>techId</var>
     */
    public FilterPE getFilter(TechId techId);

}
