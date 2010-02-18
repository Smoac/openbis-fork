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

package ch.systemsx.cisd.openbis.metadataimport;

import java.util.List;

/**
 * Provides structured information from one row of the library.
 * 
 * @author Tomasz Pylak
 */
public interface IScreeningLibraryColumnExtractor
{
    public String getPlateCode(String[] row);

    public String getWellCode(String[] row);

    public String getRNASequence(String[] row);

    public String getOligoId(String[] row);

    public String getGeneId(String[] row);

    public String getGeneCode(String[] row);

    public String getGeneDescription(String[] row);

    public List<String> getAdditionalOligoPropertyNames();

    public List<String> getAdditionalOligoPropertyValues(String[] row, List<String> columnNames);

}