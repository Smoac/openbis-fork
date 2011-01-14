/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.managed_property.api;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;

/**
 * Builder of simple table models.
 * <p>
 * All methods of this interface are part of the Managed Properties API.
 * 
 * @author Piotr Buczek
 */
public interface ISimpleTableModelBuilderAdaptor
{

    /**
     * Returns table model to be set in as output.
     * <p>
     * NOTE: scripts shouldn't rely on the returned object's interface
     */
    TableModel getTableModel();

    /**
     * Adds an empty row and returns a row builder for setting values of this row.
     * 
     * @throws UnsupportedOperationException if header titles are not forced to be unique
     */
    IRowBuilderAdaptor addRow();

    /**
     * Adds header with specified title and default column width 150.
     * 
     * @throws UserFailureException if non-unique header titles are not allowed and a header with
     *             same title has already been added.
     */
    void addHeader(String title);

    /**
     * Adds header with specified title and specified default column width.
     * 
     * @throws UserFailureException if non-unique header titles are not allowed and a header with
     *             same title has already been added.
     */
    void addHeader(String title, int defaultColumnWidth);

    /**
     * Adds header with specified title, specified code and default column width 150.
     * 
     * @throws UserFailureException if non-unique header titles are not allowed and a header with
     *             same title has already been added.
     */
    void addHeader(String title, String code);

}
