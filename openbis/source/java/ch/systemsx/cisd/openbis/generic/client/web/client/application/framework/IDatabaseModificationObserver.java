/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * @author Tomasz Pylak
 */
public interface IDatabaseModificationObserver
{
    /**
     * Informs about new modifications in the database.
     * 
     * @param observedModifications The new database modifications which have occurred. This is a
     *            subset of the modifications in which this observer is interested.
     */
    void update(Set<DatabaseModificationKind> observedModifications);

    /**
     * @return the list of database object modifications in which this observer is interested. Used
     *         to call the refresh method only when it is really necessary.
     */
    DatabaseModificationKind[] getRelevantModifications();

}
