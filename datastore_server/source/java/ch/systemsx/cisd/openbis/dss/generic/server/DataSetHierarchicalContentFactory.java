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

package ch.systemsx.cisd.openbis.dss.generic.server;

import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class DataSetHierarchicalContentFactory
{
    protected final EncapsulatedOpenBISService service;

    // public static DataSetHierarchicalContentFactory getSharedFactory();

    DataSetHierarchicalContentFactory(EncapsulatedOpenBISService service)
    {
        this.service = service;
    }

    public abstract IHierarchicalContent asContent(String dataSetCode);

    public abstract IHierarchicalContent asContent(ExternalData externalData);

    public abstract IHierarchicalContent asContent(DatasetDescription dataset);
}