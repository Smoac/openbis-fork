/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityPropertyUpdatesResult;

/** Manager of table modifications */
public interface ITableModificationsManager<M extends ModelData>
{
    /** @return <code>true</code> iff there are any uncommitted modifications */
    boolean isTableDirty();

    /** save all modifications made in the table to the DB */
    void saveModifications();

    /** save all modifications made in the table to the DB and call the after save action */
    void saveModifications(IDelegatedAction afterSaveAction);

    /** cancel all modifications made in the table */
    void cancelModifications();

    /** handle cell editing event */
    void handleEditingEvent(M model, String columnID, String stringOrNull);

    /** @return callback for given modifications made to specified model. */
    AsyncCallback<EntityPropertyUpdatesResult> createApplyModificationsCallback(final M model,
            final List<IModification> modifications);
}