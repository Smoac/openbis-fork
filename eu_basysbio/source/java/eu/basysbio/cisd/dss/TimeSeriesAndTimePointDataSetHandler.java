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

package eu.basysbio.cisd.dss;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.etlserver.ETLDaemon;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;

/**
 * Data set handler for time series data sets and time point data sets.
 *
 * @author Franz-Josef Elmer
 */
public class TimeSeriesAndTimePointDataSetHandler implements IDataSetHandler
{
    private static final String CIFEX_DIR_ENDING = ".dir";
    @Private static final String HELPDESK_EMAIL = "helpdesk.openbis.basysbio@bsse.ethz.ch";

    private static final IDataSetValidator DUMMY_DATA_SET_VALIDATOR = new IDataSetValidator()
        {
            public void assertValidDataSet(DataSetType dataSetType, File incomingDataSetFileOrFolder)
            {
            }
        };

    private static final class MessageBuilder
    {
        private final String userEMail;

        private final ITimeProvider timeProvider;
        
        private String dataSetFileName;

        private int numberOfTimePoints;

        private int count;

        MessageBuilder(String userEMail, ITimeProvider timeProvider)
        {
            this.userEMail = userEMail;
            this.timeProvider = timeProvider;
        }

        void setTimeSeriesDataSetFileName(File dataSet)
        {
            dataSetFileName = dataSet.getName();
            if (dataSetFileName.endsWith(CIFEX_DIR_ENDING))
            {
                int until = dataSetFileName.length() - CIFEX_DIR_ENDING.length();
                dataSetFileName = dataSetFileName.substring(0, until);
            }
        }

        void setNumberOfExpectedTimePointDataSets(int numberOfTimePoints)
        {
            this.numberOfTimePoints = numberOfTimePoints;
        }

        void addTimePointDataSetCode(String code)
        {
            count++;
        }

        void logAndSendEMail(Logger logger, IMailClient mailClient)
        {
            if (count < numberOfTimePoints)
            {
                operationLog.error("Only " + count + " time point data sets instead of "
                        + numberOfTimePoints + " have been registered.");
                String subject =
                        "BaSysBio: Failed uploading of time series data set '" + dataSetFileName
                                + "'";
                String timeStamp =
                        Constants.DATE_FORMAT.get().format(
                                new Date(timeProvider.getTimeInMilliseconds()));
                String message =
                        "Uploading of time series data set '" + dataSetFileName
                                + "' failed because only " + count + " of " + numberOfTimePoints
                                + " time point data sets could be registered in openBIS.\n\n"
                                + "Please, contact the help desk for support: " + HELPDESK_EMAIL
                                + "\n(Time stamp of failure: " + timeStamp + ")";
                mailClient.sendMessage(subject, message, null, null, userEMail, HELPDESK_EMAIL);
            } else
            {
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info(count + " time point data sets have been registered.");
                }
                String subject =
                        "BaSysBio: Successful uploading of time series data set '"
                                + dataSetFileName + "'";
                String message =
                        "The time series data set '" + dataSetFileName
                                + "' has been successfully uploaded and registered in openBIS.";

                mailClient.sendMessage(subject, message, null, null, userEMail);
            }
        }

    }

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, TimeSeriesAndTimePointDataSetHandler.class);

    private final IDataSetHandler delegator;

    private final IMailClient mailClient;

    private final IDataSetHandler timePointDataSetHandler;

    private final File timePointDataSetFolder;
    
    private final ITimeProvider timeProvider;

    public TimeSeriesAndTimePointDataSetHandler(Properties parentProperties,
            IDataSetHandler delegator, IEncapsulatedOpenBISService openbisService)
    {
        this.delegator = delegator;
        this.mailClient = new MailClient(parentProperties);
        Properties specificProperties =
                ExtendedProperties.getSubset(parentProperties,
                        IDataSetHandler.DATASET_HANDLER_KEY + '.', true);
        ThreadParameters threadParameters = new ThreadParameters(specificProperties, "time point");
        timePointDataSetFolder = threadParameters.getIncomingDataDirectory();
        timePointDataSetHandler =
                ETLDaemon.createDataSetHandler(parentProperties, threadParameters, openbisService,
                        DUMMY_DATA_SET_VALIDATOR, false);
        timeProvider = SystemTimeProvider.SYSTEM_TIME_PROVIDER;
    }
    
    @Private
    TimeSeriesAndTimePointDataSetHandler(IDataSetHandler delegator, IMailClient mailClient,
            IDataSetHandler timePointDataSetHandler, File timePointDataSetFolder, ITimeProvider timeProvider)
    {
        this.delegator = delegator;
        this.mailClient = mailClient;
        this.timePointDataSetHandler = timePointDataSetHandler;
        this.timePointDataSetFolder = timePointDataSetFolder;
        this.timeProvider = timeProvider;
    }

    public List<DataSetInformation> handleDataSet(File dataSet)
    {
        List<DataSetInformation> dataSetInfos = new ArrayList<DataSetInformation>();
        List<DataSetInformation> result = delegator.handleDataSet(dataSet);
        dataSetInfos.addAll(result);
        boolean successful = result.isEmpty() == false;
        if (successful)
        {
            MessageBuilder builder =
                    new MessageBuilder(result.get(0).tryGetUploadingUserEmail(), timeProvider);
            builder.setTimeSeriesDataSetFileName(dataSet);
            File[] files = timePointDataSetFolder.listFiles();
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Starting registration of " + files.length
                        + " time point data sets.");
            }
            builder.setNumberOfExpectedTimePointDataSets(files.length);
            for (File file : files)
            {
                result = timePointDataSetHandler.handleDataSet(file);
                dataSetInfos.addAll(result);
                if (result.isEmpty() == false)
                {
                    builder.addTimePointDataSetCode(getDataSetCode(result));
                }
            }
            builder.logAndSendEMail(operationLog, mailClient);
        }
        return dataSetInfos;
    }
    
    private String getDataSetCode(List<DataSetInformation> result)
    {
        return result.get(0).getDataSetCode();
    }


}
