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

package ch.ethz.bsse.cisd.dsu.dss;

import java.util.Properties;

import ch.systemsx.cisd.etlserver.DelegatingStorageProcessorWithDropbox;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

/**
 * Storage processor which feeds flow-line drop boxes. Needs the property
 * <code>flow-line-drop-box-template</code>. A <code>{0}</code> is the place holder
 * for the flow-line number.
 *
 * @author Franz-Josef Elmer
 */
public class StorageProcessor extends DelegatingStorageProcessorWithDropbox
{
    public StorageProcessor(Properties properties)
    {
        super(properties, new FlowLineFeeder(properties, ServiceProvider.getOpenBISService()));
    }

}
