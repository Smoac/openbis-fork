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
import java.util.Properties;

/**
 * Second step of BDS migration, moves data from data/original to original.
 * 
 * @author Tomasz Pylak
 */
public class BDSOriginalDataRelocatorMigrator extends AbstractBDSMigrator
{

    BDSOriginalDataRelocatorMigrator()
    {
    }

    // Every IMigrator needs the following constructor.
    public BDSOriginalDataRelocatorMigrator(Properties properties)
    {
    }

    public String getDescription()
    {
        return "moving data from data/original to original/";
    }

    @Override
    protected boolean doMigration(File dataset)
    {
        File originalDir = BDSMigrationUtils.tryGetOriginalDir(dataset);
        if (originalDir == null)
        {
            BDSMigrationUtils.operationLog.warn("No original data directory in dataset " + dataset);
            return false;
        }
        File destinationDir = new File(dataset, BDSMigrationUtils.ORIGINAL_DIR);
        boolean ok = originalDir.renameTo(destinationDir);
        if (ok == false)
        {
            BDSMigrationUtils.operationLog.error("Cannot move " + originalDir + " to "
                    + destinationDir);
            return false;
        } else
        {
            return true;
        }
    }
}