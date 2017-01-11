/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.test;

import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.datasourceconnector.IDataSourceConnector;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
public class MockDataSourceConnector implements IDataSourceConnector
{

    @Override
    public Document getResourceListAsXMLDoc(List<String> spaceBlackList) throws Exception
    {
        URL xml = this.getClass().getResource("ResourceList.xml");
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml.toURI().toString());
        return doc;
    }

}
