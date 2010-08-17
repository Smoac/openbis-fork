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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
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

    @Private
    static final String HELPDESK_EMAIL = "helpdesk.openbis.basysbio@bsse.ethz.ch";


    private static enum TypeOfDerivedDataSet
    {
        TIME_POINT("time point"), LCA_MIC_TIME_SERIES("LCA MIC time series");

        private final String name;

        TypeOfDerivedDataSet(String name)
        {
            this.name = name;
        }
    }

    private static final class MessageBuilder
    {

        private final String userEMail;

        private final ITimeProvider timeProvider;

        private String dataSetFileName;

        private int numberOfDerivedDataSets;

        private int count;

        private TypeOfDerivedDataSet type;

        MessageBuilder(String userEMail, ITimeProvider timeProvider)
        {
            this.userEMail = userEMail;
            this.timeProvider = timeProvider;
        }

        void setDataSetFileName(File dataSet)
        {
            dataSetFileName = dataSet.getName();
            if (dataSetFileName.endsWith(CIFEX_DIR_ENDING))
            {
                int until = dataSetFileName.length() - CIFEX_DIR_ENDING.length();
                dataSetFileName = dataSetFileName.substring(0, until);
            }
        }

        void setNumberOfExpectedDerivedDataSets(TypeOfDerivedDataSet type,
                int numberOfDerivedDataSets)
        {
            this.type = type;
            this.numberOfDerivedDataSets = numberOfDerivedDataSets;
        }

        void addDerivedDataSetCode(String code)
        {
            count++;
        }

        void logSendEMailAndHandlerError(Logger logger, IMailClient mailClient, boolean sendEMail)
        {
            if (count < numberOfDerivedDataSets)
            {
                int numberOfFailures = numberOfDerivedDataSets - count;
                operationLog.error(numberOfFailures + " " + type.name
                        + " data sets couldn't be registered.");
                if (sendEMail)
                {
                    String subject =
                            "BaSysBio: Failed uploading of data set '" + dataSetFileName + "'";
                    String timeStamp =
                            Constants.DATE_FORMAT.get().format(
                                    new Date(timeProvider.getTimeInMilliseconds()));
                    String message =
                            "Uploading of data set '" + dataSetFileName + "' failed because "
                                    + numberOfFailures + " of " + numberOfDerivedDataSets + " "
                                    + type.name + " data sets couldn't be registered.\n\n"
                                    + "Please, contact the help desk for support: "
                                    + HELPDESK_EMAIL + "\n(Time stamp of failure: " + timeStamp
                                    + ")";
                    mailClient.sendMessage(subject, message, null, null, userEMail, HELPDESK_EMAIL);
                }
                throw new UserFailureException("Not all data sets could be registered. "
                        + "For more details see error messages in the log.");
            } else
            {
                if (count > 0 && operationLog.isInfoEnabled())
                {
                    operationLog.info(count + " " + type.name + " data sets have been registered.");
                }
                if (sendEMail)
                {
                    String subject =
                            "BaSysBio: Successful uploading of data set '" + dataSetFileName + "'";
                    String message =
                            "The data set '" + dataSetFileName
                                    + "' has been successfully uploaded and registered in openBIS.";

                    mailClient.sendMessage(subject, message, null, null, userEMail);
                }
            }
        }

    }

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, TimeSeriesAndTimePointDataSetHandler.class);

    private static final FilenameFilter LCA_MIC_TIME_SERIES_FILE_FILTER = new FilenameFilter()
    {

        public boolean accept(File dir, String name)
        {
            return name.startsWith(DataSetHandler.LCA_MIC_TIME_SERIES);
        }
    };

    private final IDataSetHandler delegator;

    private final IMailClient mailClient;

    private final ITimeProvider timeProvider;

    private final IEncapsulatedOpenBISService service;

    public TimeSeriesAndTimePointDataSetHandler(Properties parentProperties,
            IDataSetHandler delegator, IEncapsulatedOpenBISService openbisService)
    {
        this.delegator = delegator;
        this.service = openbisService;
        this.mailClient = new MailClient(parentProperties);
        timeProvider = SystemTimeProvider.SYSTEM_TIME_PROVIDER;
    }

    @Private
    TimeSeriesAndTimePointDataSetHandler(IDataSetHandler delegator,
            IEncapsulatedOpenBISService openbisService, IMailClient mailClient,
            ITimeProvider timeProvider)
    {
        this.delegator = delegator;
        service = openbisService;
        this.mailClient = mailClient;
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
            try
            {
                DataSetInformation dataSetInformation = result.get(0);
                MessageBuilder builder =
                    new MessageBuilder(dataSetInformation.tryGetUploadingUserEmail(), timeProvider);
                builder.setDataSetFileName(dataSet);
                File[] files = dataSet.getParentFile().listFiles(LCA_MIC_TIME_SERIES_FILE_FILTER);
                handleDerivedDataSets(files, TypeOfDerivedDataSet.LCA_MIC_TIME_SERIES, delegator,
                        dataSetInfos, builder);                
                DataSetType dataSetType = dataSetInformation.getDataSetType();
                boolean lcaMicTimeSeries =
                    dataSetType.getCode().equals(DataSetHandler.LCA_MIC_TIME_SERIES);
                builder.logSendEMailAndHandlerError(operationLog, mailClient, lcaMicTimeSeries == false);
            } catch (RuntimeException ex)
            {
                // Data sets are deleted in reverse order because of possible parent-child relationships.
                for (int i = dataSetInfos.size() - 1; i >= 0; i--)
                {
                    String dataSetCode = dataSetInfos.get(i).getDataSetCode();
                    if (operationLog.isInfoEnabled())
                    {
                        operationLog.info("Delete data set " + dataSetCode);
                    }
                    service.deleteDataSet(dataSetCode, "Rollback registration");
                }
                throw ex;
            }
        }
        return dataSetInfos;
    }

    private void handleDerivedDataSets(File[] files, TypeOfDerivedDataSet type,
            IDataSetHandler handler, List<DataSetInformation> dataSetInfos, MessageBuilder builder)
    {
        if (files != null && files.length > 0)
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Starting registration of " + files.length + " " + type.name
                        + " data sets.");
            }
            builder.setNumberOfExpectedDerivedDataSets(type, files.length);
            for (File file : files)
            {
                List<DataSetInformation> result = handler.handleDataSet(file);
                dataSetInfos.addAll(result);
                if (result.isEmpty() == false)
                {
                    builder.addDerivedDataSetCode(getDataSetCode(result));
                }
            }
        }
    }

    private String getDataSetCode(List<DataSetInformation> result)
    {
        return result.get(0).getDataSetCode();
    }

}
