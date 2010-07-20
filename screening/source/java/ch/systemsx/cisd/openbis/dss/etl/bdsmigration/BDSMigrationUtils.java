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

package ch.systemsx.cisd.openbis.dss.etl.bdsmigration;

import java.io.File;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Constants and utility methods usful to migrate BDS datasets.
 * 
 * @author Tomasz Pylak
 */
public class BDSMigrationUtils
{
    static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, BDSMigrationUtils.class);

    static final String ANNOTATIONS_DIR = "annotations";

    static final String METADATA_DIR = "metadata";

    static final String DATA_DIR = "data";

    static final String VERSION_DIR = "version";

    static final String ORIGINAL_DIR = ScreeningConstants.ORIGINAL_DATA_DIR;

    static final String DIR_SEP = "/";

    static File tryGetOriginalDir(File dataset)
    {
        File orgDir = new File(dataset, DATA_DIR + DIR_SEP + ORIGINAL_DIR);
        if (orgDir.isDirectory() == false)
        {
            return null;
        }
        return orgDir;
    }

    static void logError(File dataset, String reason)
    {
        operationLog.error("Cannot migrate dataset '" + dataset.getName() + "'. " + reason);
    }

}
