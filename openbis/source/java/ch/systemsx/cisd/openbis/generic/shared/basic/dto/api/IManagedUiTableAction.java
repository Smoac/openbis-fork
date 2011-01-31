/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto.api;

/**
 * <p>
 * All methods of this interface are part of the Managed Properties API.
 * 
 * <pre>
 * action has
 * - id (both for display and usage)
 * - description (for tooltip/message shown in dialog)
 * 
 * actionInput
 * - action -> list of input widgets
 * 
 * actions can be assigned to table
 * - selection mode:
 * -- Selection.NOT_REQUIRED       (e.g. create)
 * -- Selection.REQUIRED_SINGLE    (e.g. edit)
 * -- Selection.REQUIRED           (e.g. delete)
 * </pre>
 * 
 * @author Piotr Buczek
 */
public interface IManagedUiTableAction extends IManagedUiAction
{
    /** Returns selection type specifying when the action should be enabled. */
    ManagedTableActionRowSelectionType getSelectionType();

    IManagedUiTableAction setRowSelectionNotRequired();

    IManagedUiTableAction setRowSelectionRequired();

    IManagedUiTableAction setRowSingleSelectionRequired();
}
