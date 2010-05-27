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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Data set handler for plasmid data sets.
 * 
 * @author Piotr Buczek
 */
public class PlasmidDataSetHandler implements IDataSetHandler
{
    @SuppressWarnings("unused")
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
        List<DataSetInformation> result = new ArrayList<DataSetInformation>();
        File[] files = dataSet.listFiles();
        for (File file : files)
        {
            result.addAll(delegator.handleDataSet(file));
        }
        return result;
    }

}
