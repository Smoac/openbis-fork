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

package ch.ethz.bsse.cisd.plasmid.dss;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * Helper class for creation of data set property of type {@code FILE_NAME} holding name of the file
 * stored for the data set.
 * 
 * @author Piotr Buczek
 */
class DataSetFileNamePropertyHelper
{
    private final static String FILE_NAME_PROPERTY = "FILE_NAME";

    static NewProperty createProperty(File incomingDataSetFile, boolean stripExtension)
    {
        String fileName = incomingDataSetFile.getName();
        if (stripExtension)
        {
            fileName = FilenameUtils.removeExtension(fileName);
        }
        return new NewProperty(FILE_NAME_PROPERTY, fileName);
    }

}
