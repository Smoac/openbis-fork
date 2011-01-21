/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.openbisauth.OpenBISSessionHolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.OpenBisServiceFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * A class that encapsulates the {@link IETLLIMSService}.
 * 
 * @author Bernd Rinn
 */
public final class EncapsulatedOpenBISService implements IEncapsulatedOpenBISService, FactoryBean
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            EncapsulatedOpenBISService.class);

    private final IETLLIMSService service;

    private Integer version;

    private DatabaseInstance homeDatabaseInstance;

    // this session object is automatically kept up-to-date by an aspect
    private OpenBISSessionHolder session;

    public static IETLLIMSService createOpenBisService(String openBISURL)
    {
        return new OpenBisServiceFactory(openBISURL, ResourceNames.ETL_SERVICE_URL).createService();
    }

    public EncapsulatedOpenBISService(IETLLIMSService service, OpenBISSessionHolder sessionHolder)
    {
        assert service != null : "Given IETLLIMSService implementation can not be null.";
        assert sessionHolder != null : "Given OpenBISSessionHolder can not be null.";
        this.service = service;
        this.session = sessionHolder;
    }


    public Object getObject() throws Exception
    {
        return this;
    }

    @SuppressWarnings("unchecked")
    public Class getObjectType()
    {
        return IEncapsulatedOpenBISService.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

    //
    // IEncapsulatedOpenBISService
    //

    public Experiment tryToGetExperiment(ExperimentIdentifier experimentIdentifier)
    {
        assert experimentIdentifier != null : " Unspecified experiment identifier.";
        return service.tryToGetExperiment(session.getToken(), experimentIdentifier);
    }

    public List<Sample> listSamples(ListSampleCriteria criteria)
    {
        assert criteria != null : "Unspecifed criteria.";
        return service.listSamples(session.getToken(), criteria);
    }

    public final Sample tryGetSampleWithExperiment(
            final SampleIdentifier sampleIdentifier)
    {
        assert sampleIdentifier != null : "Given sample identifier can not be null.";
        return service.tryGetSampleWithExperiment(session.getToken(), sampleIdentifier);
    }

    public SampleIdentifier tryToGetSampleIdentifier(String samplePermID)
            throws UserFailureException
    {
        return service.tryToGetSampleIdentifier(session.getToken(), samplePermID);
    }

    public ExperimentType getExperimentType(String experimentTypeCode)
            throws UserFailureException
    {
        return service.getExperimentType(session.getToken(), experimentTypeCode);
    }

    public Collection<VocabularyTerm> listVocabularyTerms(String vocabularyCode)
            throws UserFailureException
    {
        return service.listVocabularyTerms(session.getToken(), vocabularyCode);
    }

    public SampleType getSampleType(String sampleTypeCode) throws UserFailureException
    {
        return service.getSampleType(session.getToken(), sampleTypeCode);
    }

    public DataSetTypeWithVocabularyTerms getDataSetType(String dataSetTypeCode)
    {
        return service.getDataSetType(session.getToken(), dataSetTypeCode);
    }

    public List<ExternalData> listDataSetsByExperimentID(long experimentID)
            throws UserFailureException
    {
        TechId id = new TechId(experimentID);
        return service.listDataSetsByExperimentID(session.getToken(), id);
    }

    public List<ExternalData> listDataSetsBySampleID(long sampleID,
            boolean showOnlyDirectlyConnected)
    {
        TechId id = new TechId(sampleID);
        return service.listDataSetsBySampleID(session.getToken(), id, showOnlyDirectlyConnected);
    }

    public long registerExperiment(NewExperiment experiment) throws UserFailureException
    {
        assert experiment != null : "Unspecified experiment.";
        return service.registerExperiment(session.getToken(), experiment);
    }

    public void registerSamples(List<NewSamplesWithTypes> newSamples,
            String userIDOrNull) throws UserFailureException
    {
        assert newSamples != null : "Unspecified samples.";

        service.registerSamples(session.getToken(), newSamples, userIDOrNull);
    }

    public long registerSample(NewSample newSample, String userIDOrNull)
            throws UserFailureException
    {
        assert newSample != null : "Unspecified sample.";

        return service.registerSample(session.getToken(), newSample, userIDOrNull);
    }

    public void updateSample(SampleUpdatesDTO sampleUpdate)
            throws UserFailureException
    {
        assert sampleUpdate != null : "Unspecified sample.";

        service.updateSample(session.getToken(), sampleUpdate);
    }

    public final void registerDataSet(final DataSetInformation dataSetInformation,
            final NewExternalData data)
    {
        assert dataSetInformation != null : "missing sample identifier";
        assert data != null : "missing data";

        SampleIdentifier sampleIdentifier = dataSetInformation.getSampleIdentifier();
        if (sampleIdentifier == null)
        {
            ExperimentIdentifier experimentIdentifier =
                    dataSetInformation.getExperimentIdentifier();
            service.registerDataSet(session.getToken(), experimentIdentifier, data);
        } else
        {
            service.registerDataSet(session.getToken(), sampleIdentifier, data);
        }

        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Registered in openBIS: data set " + dataSetInformation.describe()
                    + ".");
        }
    }

    public void deleteDataSet(String dataSetCode, String reason)
            throws UserFailureException
    {
        service.deleteDataSet(session.getToken(), dataSetCode, reason);
    }

    public final void updateDataSet(String code, List<NewProperty> properties,
            SpaceIdentifier space) throws UserFailureException

    {
        assert code != null : "missing data set code";
        assert properties != null : "missing data";
        assert space != null : "space missing";

        service.addPropertiesToDataSet(session.getToken(), properties, code, space);

        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Updated in openBIS: data set " + code + ".");
        }
    }


    public final void updateDataSetStatuses(List<String> codes,
            DataSetArchivingStatus newStatus) throws UserFailureException

    {
        assert codes != null : "missing data set codes";
        assert newStatus != null : "missing status";

        service.updateDataSetStatuses(session.getToken(), codes, newStatus);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Updated in openBIS: data sets " + codes + ", status=" + newStatus);
        }
    }

    public final IEntityProperty[] getPropertiesOfTopSampleRegisteredFor(
            final SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        assert sampleIdentifier != null : "Given sample identifier can not be null.";
        return service.tryToGetPropertiesOfTopSampleRegisteredFor(session.getToken(),
                sampleIdentifier);
    }

    public final List<Sample> listSamplesByCriteria(
            final ListSamplesByPropertyCriteria criteria) throws UserFailureException
    {
        return service.listSamplesByCriteria(session.getToken(), criteria);
    }

    public final int getVersion()
    {
        if (version == null)
        {
            version = service.getVersion();
        }
        return version;
    }

    public final DatabaseInstance getHomeDatabaseInstance()
    {
        if (homeDatabaseInstance == null)
        {
            homeDatabaseInstance = service.getHomeDatabaseInstance(session.getToken());
        }
        return homeDatabaseInstance;
    }

    public final String createDataSetCode()
    {
        return service.createDataSetCode(session.getToken());
    }

    public long drawANewUniqueID()
    {
        return service.drawANewUniqueID(session.getToken());
    }

    public ExternalData tryGetDataSet(String dataSetCode) throws UserFailureException
    {
        return service.tryGetDataSet(session.getToken(), dataSetCode);
    }

    public ExternalData tryGetDataSet(String sToken, String dataSetCode)
            throws UserFailureException
    {
        return service.tryGetDataSet(sToken, dataSetCode);
    }

    public void checkInstanceAdminAuthorization(String sToken)
            throws UserFailureException
    {
        service.checkInstanceAdminAuthorization(sToken);
    }

    public void checkDataSetAccess(String sToken, String dataSetCode)
            throws UserFailureException
    {
        service.checkDataSetAccess(sToken, dataSetCode);
    }

    public void checkDataSetCollectionAccess(String sToken, List<String> dataSetCodes)
            throws UserFailureException
    {
        service.checkDataSetCollectionAccess(sToken, dataSetCodes);
    }

    public void checkSpaceAccess(String sToken, SpaceIdentifier spaceId)
    {
        service.checkSpaceAccess(sToken, spaceId);
    }

    public List<SimpleDataSetInformationDTO> listDataSets()
            throws UserFailureException
    {
        return service.listDataSets(session.getToken(), session.getDataStoreCode());
    }

    public List<ExternalData> listAvailableDataSets(ArchiverDataSetCriteria criteria)
            throws UserFailureException
    {
        return service.listAvailableDataSets(session.getToken(), session.getDataStoreCode(),
                criteria);
    }

    public List<DeletedDataSet> listDeletedDataSets(Long lastSeenDeletionEventIdOrNull)
    {
        return service.listDeletedDataSets(session.getToken(), lastSeenDeletionEventIdOrNull);
    }

    public void archiveDataSets(List<String> dataSetCodes) throws UserFailureException
    {
        service.archiveDatasets(session.getToken(), dataSetCodes);
    }

    public void unarchiveDataSets(List<String> dataSetCodes) throws UserFailureException
    {
        service.unarchiveDatasets(session.getToken(), dataSetCodes);
    }

    public SessionContextDTO tryGetSession(String sToken)
    {
        return service.tryGetSession(sToken);
    }

    public ExternalData tryGetDataSetForServer(String dataSetCode) throws UserFailureException
    {
        return service.tryGetDataSetForServer(session.getToken(), dataSetCode);
    }

    public List<String> generateCodes(String prefix, int size)
    {
        return service.generateCodes(session.getToken(), prefix, size);
    }

    public List<Person> listAdministrators()
    {
        return service.listAdministrators(session.getToken());
    }

    public Person tryPersonWithUserIdOrEmail(String useridOrEmail)
    {
        return service.tryPersonWithUserIdOrEmail(session.getToken(), useridOrEmail);
    }

    public Sample registerSampleAndDataSet(NewSample newSample, NewExternalData externalData,
            String userIdOrNull) throws UserFailureException
    {
        return service.registerSampleAndDataSet(session.getToken(), newSample, externalData,
                    userIdOrNull);
    }

    public Sample updateSampleAndRegisterDataSet(SampleUpdatesDTO newSample,
            NewExternalData externalData)
    {
        return service.updateSampleAndRegisterDataSet(session.getToken(), newSample, externalData);
    }

}