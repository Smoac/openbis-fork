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

package ch.ethz.bsse.cisd.dsu.dss.plugins;

import java.io.File;

import ch.systemsx.cisd.common.xml.JaxbXmlParser;

/**
 * Loader of Illumina summary XML file.
 * 
 * @author Manuel Kohler
 */
class IlluminaSummaryXMLLoader
{

    public static IlluminaSummary readSummaryXML(File dataSet, boolean validate)
    {
        return JaxbXmlParser.parse(IlluminaSummary.class, dataSet, validate);
    }

}
