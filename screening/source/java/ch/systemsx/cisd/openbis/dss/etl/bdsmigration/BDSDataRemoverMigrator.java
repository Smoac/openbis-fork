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

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Removes unnecessary BDS data as a part of the migration.
 * 
 * @author Tomasz Pylak
 */
class BDSDataRemoverMigrator implements IBDSMigrator
{

    public String getDescription()
    {
        return "removing unnecessary BDS data";
    }

    public boolean migrate(File dataset)
    {
        if (BDSMigrationMaintananceTask.tryGetOriginalDir(dataset) != null)
        {
            BDSMigrationMaintananceTask.logError(dataset, "original data has not been moved");
            return false;
        }
        try
        {
            removeDir(dataset, BDSMigrationMaintananceTask.METADATA_DIR);
            removeDir(dataset, BDSMigrationMaintananceTask.VERSION_DIR);
            removeDir(dataset, BDSMigrationMaintananceTask.ANNOTATIONS_DIR);
            removeDir(dataset, BDSMigrationMaintananceTask.DATA_DIR);
        } catch (EnvironmentFailureException ex)
        {
            return false;
        }
        return true;
    }

    private void removeDir(File dataset, String relativeDirPath) throws EnvironmentFailureException
    {
        File dir = new File(dataset, relativeDirPath);
        boolean ok = FileUtilities.deleteRecursively(dir);
        if (ok == false)
        {
            String errorMsg = "Cannot delete the directory: " + dir.getAbsolutePath();
            BDSMigrationMaintananceTask.operationLog.error(errorMsg);
            throw new EnvironmentFailureException(errorMsg);
        }
    }
}