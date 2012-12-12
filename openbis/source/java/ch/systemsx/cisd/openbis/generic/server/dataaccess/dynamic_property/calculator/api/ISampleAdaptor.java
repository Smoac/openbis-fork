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

/**
 * @author Jakub Straszewski
 */
public interface ISampleAdaptor extends IEntityAdaptor
{
    /**
     * Returns the experiment of this sample, or null if not exists.
     */
    public IExperimentAdaptor experiment();

    /**
     * Returns all parent samples.
     */
    public Iterable<ISampleAdaptor> parents();

    /**
     * Returns all child samples.
     */
    public Iterable<ISampleAdaptor> children();

    /**
     * Returns the container sample, or null if not exists.
     */
    public ISampleAdaptor container();

    /**
     * Returns contained samples of this sample.
     */
    public Iterable<ISampleAdaptor> contained();

    /**
     * Returns contained samples of this sample. Types of the returned contained samples must match
     * the specified regular expression.
     */
    public Iterable<ISampleAdaptor> containedOfType(String typeCodeRegexp);

    /**
     * Returns data sets of this sample.
     */
    public Iterable<IDataAdaptor> dataSets();

    /**
     * Returns data sets of this sample. Types of the returned data sets must match the specified
     * regular expression.
     */
    public Iterable<IDataAdaptor> dataSetsOfType(String typeCodeRegexp);

}
