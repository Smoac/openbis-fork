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

package ch.systemsx.cisd.cina.dss.bundle;

import java.io.File;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.cina.dss.bundle.BundleDataSetHelper.BundleRegistrationGlobalState;
import ch.systemsx.cisd.cina.shared.constants.CinaConstants;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.etlserver.IDataSetHandlerRpc;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * CINA registers data in the form of a bundle. A bundle contains several different kinds data sets.
 * The CinaBundleDataSetHandler breaks apart the data into several pieces and takes action on each
 * of those pieces.
 * <p>
 * The structure of the bundle mirrors the structure of CINA experiments and is made up of the
 * following pieces:
 * <ul>
 * <li>Grid Preparation (a template for grids which are imaged by microscopes)</li>
 * <ul>
 * <li>Replica (a grid derived from the grid preparation)
 * <ul>
 * <li>Images (the images made from a replica)</li>
 * </ul>
 * </li>
 * </ul>
 * </li> </ul>
 * <p>
 * A bundle is broken down into the following pieces:
 * <ul>
 * <li>Grid Preparation Metadata</li>
 * <li>Replica
 * <ul>
 * <li>Replica Metadata
 * <ul>
 * <li>Individual Image Metadata</li>
 * <li>Individual Image Thumbnails</li>
 * </ul>
 * </li>
 * <li>Replica Raw Images</li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * This data set handler does the following:
 * <ul>
 * <li>Create a grid preparation sample if one does not exist</li>
 * <li>For each replica:
 * <ul>
 * <li>If the replica sample does not exist, create it and register a raw images data set</li>
 * <li>Create a metadata data set and associate it with the replica; update the replica's metadata</li>
 * <li>Create datasets for each of the annotated images in the data set</li>
 * </ul>
 * </li>
 * <li>Create a bundle data set which points to the metadata and original data sets we registered
 * above</li>
 * <li>Update the grid preparation's metadata</li>
 * </ul>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class CinaBundleDataSetHandler implements IDataSetHandler
{
    private final IDataSetHandler delegator;

    private final BundleRegistrationGlobalState bundleRegistrationState;

    public CinaBundleDataSetHandler(Properties parentProperties, IDataSetHandler delegator,
            IEncapsulatedOpenBISService openbisService)
    {
        this.delegator = delegator;
        this.bundleRegistrationState = createBundleRegistrationState(delegator, openbisService);
    }

    public List<DataSetInformation> handleDataSet(File dataSet)
    {
        BundleDataSetHelper helper;
        if (delegator instanceof IDataSetHandlerRpc)
        {
            helper = new BundleDataSetHelperRpc(bundleRegistrationState, dataSet);
            helper.process();
            return helper.getDataSetInformation();
        } else
        {
            // We are not being invoked from the command line, so we don't have enough contextual
            // information to do the additional processing associated with these data sets. We need
            // to know who the user is to do the additional processing.
            return delegator.handleDataSet(dataSet);
        }
    }

    private static BundleRegistrationGlobalState createBundleRegistrationState(
            IDataSetHandler delegator, IEncapsulatedOpenBISService openbisService)
    {
        SampleType replicaSampleType =
                openbisService.getSampleType(CinaConstants.REPLICA_SAMPLE_TYPE_CODE);
        DataSetTypeWithVocabularyTerms imageDataSetType =
                openbisService.getDataSetType(CinaConstants.IMAGE_DATA_SET_TYPE_CODE);
        return new BundleRegistrationGlobalState(delegator, openbisService, replicaSampleType,
                imageDataSetType);
    }
}
