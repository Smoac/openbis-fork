/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.datasource;

import java.util.Date;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.IDataSourceQueryService;

/**
 * @author Franz-Josef Elmer
 *
 * @param <T>
 */
interface IDeliverer
{

    void deliverEntities(XMLStreamWriter writer, IDataSourceQueryService queryService, String sessionToken, 
            Set<String> spaces, Date requestTimestamp) throws XMLStreamException;

}