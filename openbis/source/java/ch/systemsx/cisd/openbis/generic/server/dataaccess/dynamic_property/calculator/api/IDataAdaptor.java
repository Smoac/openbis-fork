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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.INonAbstractEntityAdapter;

/**
 * @author Jakub Straszewski
 */
public interface IDataAdaptor extends INonAbstractEntityAdapter
{
    /**
     * Returns the experiment of this data set, or null if not exists.
     */
    IExperimentAdaptor experiment();

    /**
     * Returns the list of all parent data sets.
     */
    public List<IDataAdaptor> parents();

    /**
     * Returns the list of all child data sets.
     */
    public List<IDataAdaptor> children();

    /**
     * Returns the list of contained data sets.
     */
    public List<IDataAdaptor> contained();

    /**
     * Returs the container data set, or null if not exists.
     */
    public IDataAdaptor container();
}
