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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Default <code>IDataStrategyStore</code> implementation.
 * <p>
 * Decides which {@link IDataStoreStrategy} will be applied for an incoming data set.
 * </p>
 * 
 * @author Christian Ribeaud
 */
final class DataStrategyStore implements IDataStrategyStore
{
    static final String SUBJECT_FORMAT = "ATTENTION: experiment '%s'";

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, DataStrategyStore.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DataStrategyStore.class);

    private final IEncapsulatedOpenBISService limsService;

    private final IMailClient mailClient;

    private final Map<DataStoreStrategyKey, IDataStoreStrategy> dataStoreStrategies;

    DataStrategyStore(final IEncapsulatedOpenBISService limsService, final IMailClient mailClient)
    {
        this.mailClient = mailClient;
        dataStoreStrategies = createDataStoreStrategies();
        this.limsService = limsService;
    }

    private final static void putDataStoreStrategy(
            final Map<DataStoreStrategyKey, IDataStoreStrategy> map,
            final IDataStoreStrategy dataStoreStrategy)
    {
        map.put(dataStoreStrategy.getKey(), dataStoreStrategy);
    }

    private final static Map<DataStoreStrategyKey, IDataStoreStrategy> createDataStoreStrategies()
    {
        final Map<DataStoreStrategyKey, IDataStoreStrategy> map =
                new EnumMap<DataStoreStrategyKey, IDataStoreStrategy>(DataStoreStrategyKey.class);
        putDataStoreStrategy(map, new IdentifiedDataStrategy());
        putDataStoreStrategy(map, new NamedDataStrategy(DataStoreStrategyKey.UNIDENTIFIED));
        putDataStoreStrategy(map, new NamedDataStrategy(DataStoreStrategyKey.INVALID));
        return map;
    }

    final static String createInvalidSampleCodeMessage(final DataSetInformation dataSetInfo)
    {
        return "ETL server: Sample '" + dataSetInfo.getSampleIdentifier()
                + "' is not valid for experiment '" + dataSetInfo.getExperimentIdentifier()
                + "' (it has maybe been invalidated?).";
    }

    private static String createNotificationMessage(final DataSetInformation dataSetInfo,
            final File incomingDataSetPathForLogging)
    {
        final SampleIdentifier sampleIdentifier = dataSetInfo.getSampleIdentifier();
        return String.format("Dataset '%s', sample identifier '%s': unknown to openBIS",
                incomingDataSetPathForLogging, sampleIdentifier);
    }

    //
    // IDataStrategyStore
    //

    public final IDataStoreStrategy getDataStoreStrategy(final DataSetInformation dataSetInfo,
            final File incomingDataSetPath)
    {

        assert incomingDataSetPath != null : "Incoming data set path can not be null.";
        if (dataSetInfo == null)
        {
            return dataStoreStrategies.get(DataStoreStrategyKey.UNIDENTIFIED);
        }
        String emailOrNull = dataSetInfo.tryGetUploadingUserEmail();
        ExperimentIdentifier experimentIdentifier;
        final SampleIdentifier sampleIdentifier = dataSetInfo.getSampleIdentifier();
        if (sampleIdentifier == null)
        {
            experimentIdentifier = dataSetInfo.getExperimentIdentifier();
            Experiment experiment = limsService.tryToGetExperiment(experimentIdentifier);
            if (experiment == null)
            {
                error(emailOrNull, "Unknown experiment identifier '" + experimentIdentifier + "'.");
                return dataStoreStrategies.get(DataStoreStrategyKey.UNIDENTIFIED);
            }
            if (experiment.getInvalidation() != null)
            {
                error(emailOrNull, "Experiment '" + experimentIdentifier
                        + "' has been invalidated.");
                return dataStoreStrategies.get(DataStoreStrategyKey.UNIDENTIFIED);
            }
            dataSetInfo.setExperiment(experiment);
        } else
        {
            final Sample sample = tryGetSample(sampleIdentifier);
            final Experiment experiment = (sample == null) ? null : sample.getExperiment();
            if (experiment == null)
            {
                error(emailOrNull, createNotificationMessage(dataSetInfo, incomingDataSetPath));
                return dataStoreStrategies.get(DataStoreStrategyKey.UNIDENTIFIED);
            } else if (experiment.getInvalidation() != null)
            {
                error(emailOrNull, "Data set for sample '" + sampleIdentifier
                        + "' can not be registered because experiment '" + experiment.getCode()
                        + "' has been invalidated.");
                return dataStoreStrategies.get(DataStoreStrategyKey.UNIDENTIFIED);
            }
            dataSetInfo.setSample(sample);
            experimentIdentifier = new ExperimentIdentifier(experiment);
            dataSetInfo.setExperimentIdentifier(experimentIdentifier);
            
            final IEntityProperty[] properties =
                limsService.getPropertiesOfTopSampleRegisteredFor(sampleIdentifier);
            if (properties == null)
            {
                final Person registrator = experiment.getRegistrator();
                assert registrator != null : "Registrator must be known";
                final String message = createInvalidSampleCodeMessage(dataSetInfo);
                final String recipientMail = registrator.getEmail();
                if (StringUtils.isNotBlank(recipientMail))
                {
                    sendEmail(message, experimentIdentifier, recipientMail);
                } else
                {
                    error(emailOrNull, "The registrator '" + registrator
                            + "' has a blank email, sending the following email failed:\n" + message);
                }
                operationLog.error(String.format("Incoming data set '%s' claims to "
                        + "belong to experiment '%s' and sample"
                        + " identifier '%s', but according to the openBIS server "
                        + "there is no such sample for this "
                        + "experiment (it has maybe been invalidated?). We thus consider it invalid.",
                        incomingDataSetPath, experimentIdentifier, sampleIdentifier));
                return dataStoreStrategies.get(DataStoreStrategyKey.INVALID);
            }
            dataSetInfo.setProperties(properties);
        }

        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Identified that database knows experiment '"
                    + experimentIdentifier + "'"
                    + (sampleIdentifier == null ? "." : " and sample '" + sampleIdentifier + "'."));
        }
        return dataStoreStrategies.get(DataStoreStrategyKey.IDENTIFIED);
    }
    
    private void error(String emailOrNull, String message)
    {
        if (emailOrNull == null)
        {
            notificationLog.error(message);
        } else
        {
            mailClient.sendMessage("Error during registration of a data set", message, null, null, emailOrNull);
            operationLog.error(message);
        }
    }

    private Sample tryGetSample(final SampleIdentifier sampleIdentifier)
    {
        try
        {
            return limsService.tryGetSampleWithExperiment(sampleIdentifier);
        } catch (UserFailureException ex)
        {
            return null;
        }
    }

    private void sendEmail(final String message, final ExperimentIdentifier experimentIdentifier,
            final String recipientMail)
    {
        final String subject = String.format(SUBJECT_FORMAT, experimentIdentifier);
        try
        {
            mailClient.sendMessage(subject, message, null, null, recipientMail);
        } catch (final EnvironmentFailureException ex)
        {
            operationLog.error(ex.getMessage());
        }
    }
}