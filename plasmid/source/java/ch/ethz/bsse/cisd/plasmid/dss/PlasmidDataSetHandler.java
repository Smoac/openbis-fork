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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Data set handler for plasmid data sets.
 * <p>
 * All visible files (with names not starting with '.') in the handled directory are delegated to
 * the handler provided in the constructor. As the result a new data set will be created for each
 * file in the directory.
 * 
 * @author Piotr Buczek
 */
public class PlasmidDataSetHandler implements IDataSetHandler
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PlasmidDataSetHandler.class);

    private final IDataSetHandler delegator;

    public PlasmidDataSetHandler(Properties parentProperties, IDataSetHandler delegator,
            IEncapsulatedOpenBISService openbisService)
    {
        this.delegator = delegator;
    }

    public List<DataSetInformation> handleDataSet(File dataSet)
    {
        if (dataSet.isDirectory() == false)
        {
            throw UserFailureException.fromTemplate(
                    "Failed to handle file '%s'. Expected a directory.", dataSet);
        }

        // handle all visible files by delegator
        List<DataSetInformation> result = new ArrayList<DataSetInformation>();
        final File[] visibleFiles = dataSet.listFiles((FileFilter) HiddenFileFilter.VISIBLE);
        for (File file : visibleFiles)
        {
            result.addAll(delegator.handleDataSet(file));
        }

        // delete all hidden files and the directory
        final File[] hiddenFiles = dataSet.listFiles((FileFilter) HiddenFileFilter.HIDDEN);
        for (File file : hiddenFiles)
        {
            String deletionStatus = file.delete() ? "Deleted" : "Failed to delete";
            operationLog.info(String.format("%s hidden file: %s", deletionStatus, file));
        }
        if (dataSet.delete() == false)
        {
            throw new EnvironmentFailureException(String.format("Failed to delete '%s' directory.",
                    dataSet));
        }

        return result;
    }
}
