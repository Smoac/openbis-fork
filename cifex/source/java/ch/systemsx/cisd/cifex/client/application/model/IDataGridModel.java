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

package ch.systemsx.cisd.cifex.client.application.model;

import java.util.List;

import ch.systemsx.cisd.cifex.client.application.ModelBasedGrid;

/**
 * An interface that encapsulates model functionality of {@link ModelBasedGrid}.
 * 
 * @author Christian Ribeaud
 */
public interface IDataGridModel
{

    /** Returns the data present in this grid */
    public List/* <Object[]> */getData(Object[] data);

    /** Returns the field definitions. */
    public List/* <FieldDef> */getFieldDefs();

    /** Returns the column configurations. */
    public List/* <ColumnConfig> */getColumnConfigs();
}