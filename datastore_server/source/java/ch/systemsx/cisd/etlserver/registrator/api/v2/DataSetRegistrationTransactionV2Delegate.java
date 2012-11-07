/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v2;

import java.io.File;

import net.lemnik.eodsql.DynamicTransactionQuery;

import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationContext;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetUpdatable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperiment;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperimentUpdatable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IMaterial;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IMetaproject;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IProject;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISample;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISpace;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.IAuthorizationService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExternalDataManagementSystemImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IMaterialImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IProjectImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISearchService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISpaceImmutable;

/**
 * @author Jakub Straszewski
 */
public class DataSetRegistrationTransactionV2Delegate implements IDataSetRegistrationTransactionV2
{
    private IDataSetRegistrationTransaction transaction;

    public DataSetRegistrationTransactionV2Delegate(IDataSetRegistrationTransaction transaction)
    {
        this.transaction = transaction;
    }

    @Override
    public IDataSet createNewDataSet()
    {
        return transaction.createNewDataSet();
    }

    @Override
    public IDataSet createNewDataSet(String dataSetType)
    {
        return transaction.createNewDataSet(dataSetType);
    }

    @Override
    public IDataSet createNewDataSet(String dataSetType, String dataSetCode)
    {
        return transaction.createNewDataSet(dataSetType, dataSetCode);
    }

    @Override
    public IDataSetImmutable getDataSet(String dataSetCode)
    {
        return transaction.getDataSet(dataSetCode);
    }

    @Override
    public IDataSetUpdatable getDataSetForUpdate(String dataSetCode)
    {
        return transaction.getDataSetForUpdate(dataSetCode);
    }

    @Override
    public IDataSetUpdatable makeDataSetMutable(IDataSetImmutable dataSet)
    {
        return transaction.makeDataSetMutable(dataSet);
    }

    @Override
    public ISampleImmutable getSample(String sampleIdentifierString)
    {
        return transaction.getSample(sampleIdentifierString);
    }

    @Override
    public ISample getSampleForUpdate(String sampleIdentifierString)
    {
        return transaction.getSampleForUpdate(sampleIdentifierString);
    }

    @Override
    public ISample makeSampleMutable(ISampleImmutable sample)
    {
        return transaction.makeSampleMutable(sample);
    }

    @Override
    public ISample createNewSample(String sampleIdentifierString, String sampleTypeCode)
    {
        return transaction.createNewSample(sampleIdentifierString, sampleTypeCode);
    }

    @Override
    public IExperimentImmutable getExperiment(String experimentIdentifierString)
    {
        return transaction.getExperiment(experimentIdentifierString);
    }

    @Override
    public IExperimentUpdatable getExperimentForUpdate(String experimentIdentifierString)
    {
        return transaction.getExperimentForUpdate(experimentIdentifierString);
    }

    @Override
    public IExperimentUpdatable makeExperimentMutable(IExperimentImmutable experiment)
    {
        return transaction.makeExperimentMutable(experiment);
    }

    @Override
    public IExperiment createNewExperiment(String experimentIdentifierString,
            String experimentTypeCode)
    {
        return transaction.createNewExperiment(experimentIdentifierString, experimentTypeCode);
    }

    @Override
    public IProject createNewProject(String projectIdentifier)
    {
        return transaction.createNewProject(projectIdentifier);
    }

    @Override
    public IProjectImmutable getProject(String projectIdentifier)
    {
        return transaction.getProject(projectIdentifier);
    }

    @Override
    public ISpace createNewSpace(String spaceCode, String spaceAdminUserIdOrNull)
    {
        return transaction.createNewSpace(spaceCode, spaceAdminUserIdOrNull);
    }

    @Override
    public ISpaceImmutable getSpace(String spaceCode)
    {
        return transaction.getSpace(spaceCode);
    }

    @Override
    public IMaterialImmutable getMaterial(String materialCode, String materialType)
    {
        return transaction.getMaterial(materialCode, materialType);
    }

    @Override
    public IMaterial getMaterialForUpdate(String materialCode, String materialType)
    {
        return transaction.getMaterialForUpdate(materialCode, materialType);
    }

    @Override
    public IMaterial makeMaterialMutable(IMaterialImmutable material)
    {
        return transaction.makeMaterialMutable(material);
    }

    @Override
    public IMaterial createNewMaterial(String materialCode, String materialType)
    {
        return transaction.createNewMaterial(materialCode, materialType);
    }

    @Override
    public IMetaproject createNewMetaproject(String name, String description)
    {
        return transaction.createNewMetaproject(name, description);
    }

    @Override
    public IMetaproject createNewMetaproject(String name, String description, String ownerId)
    {
        return transaction.createNewMetaproject(name, description, ownerId);
    }

    @Override
    public String moveFile(String src, IDataSet dst)
    {
        return transaction.moveFile(src, dst);
    }

    @Override
    public String moveFile(String src, IDataSet dst, String dstInDataset)
    {
        return transaction.moveFile(src, dst, dstInDataset);
    }

    @Override
    public String createNewDirectory(IDataSet dst, String dirName)
    {
        return transaction.createNewDirectory(dst, dirName);
    }

    @Override
    public String createNewFile(IDataSet dst, String fileName)
    {
        return transaction.createNewFile(dst, fileName);
    }

    @Override
    public String createNewFile(IDataSet dst, String dstInDataset, String fileName)
    {
        return transaction.createNewFile(dst, dstInDataset, fileName);
    }

    @Override
    public ISearchService getSearchService()
    {
        return transaction.getSearchService();
    }

    @Override
    public ISearchService getSearchServiceUnfiltered()
    {
        return transaction.getSearchServiceUnfiltered();
    }

    @Override
    public IAuthorizationService getAuthorizationService()
    {
        return transaction.getAuthorizationService();
    }

    @Override
    public DynamicTransactionQuery getDatabaseQuery(String dataSourceName)
            throws IllegalArgumentException
    {
        return transaction.getDatabaseQuery(dataSourceName);
    }

    @Override
    public DataSetRegistrationContext getRegistrationContext()
    {
        return transaction.getRegistrationContext();
    }

    @Override
    public TopLevelDataSetRegistratorGlobalState getGlobalState()
    {
        return transaction.getGlobalState();
    }

    @Override
    public File getIncoming()
    {
        return transaction.getIncoming();
    }

    @Override
    public String getUserId()
    {
        return transaction.getUserId();
    }

    @Override
    public void setUserId(String userIdOrNull)
    {
        transaction.setUserId(userIdOrNull);
    }

    @Override
    public IExternalDataManagementSystemImmutable getExternalDataManagementSystem(
            String externalDataManagementSystemCode)
    {
        return transaction.getExternalDataManagementSystem(externalDataManagementSystemCode);
    }

}
