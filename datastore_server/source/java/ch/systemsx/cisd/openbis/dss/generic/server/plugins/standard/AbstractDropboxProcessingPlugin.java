/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.common.utilities.Template;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IPostRegistrationDatasetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * The base class for processing plugins that employ a {@link IPostRegistrationDatasetHandler}.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractDropboxProcessingPlugin extends AbstractDatastorePlugin implements
        IProcessingPluginTask
{

    private static final long serialVersionUID = 1L;

    private static final String EMAIL_MESSAGE_TEMPLATE_LINE1 =
            "Data set ${data-set} of experiment ${experiment} has been successfully processed.\n";

    private static final String EMAIL_MESSAGE_TEMPLATE_LINE1_WITH_SAMPLE =
            "Data set ${data-set} of experiment ${experiment} and sample ${sample}"
                    + " has been successfully processed.\n";

    private static final String EMAIL_MESSAGE_TEMPLATE_LINE1_WITH_ERROR =
            "Processing data set ${data-set} of experiment ${experiment} failed. Reason: ${error}\n";

    private static final String EMAIL_MESSAGE_TEMPLATE_LINE1_WITH_ERROR_WITH_SAMPLE =
            "Processing data set ${data-set} of experiment ${experiment} and sample ${sample}"
                    + " failed. Reason: ${error}\n";

    final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractDropboxProcessingPlugin.class);

    @Private static final String SEND_DETAILED_EMAIL_KEY = "send-detailed-email";

    private final static String MISSING_DIRECTORY_MSG = "with missing directory";

    private final static String EMPTY_DIRECTORY_MSG = "with empty directory";

    private final static String MORE_THAN_ONE_ITEM_MSG = "with more than one item in the directory";

    private final IPostRegistrationDatasetHandler dropboxHandler;

    private final ITimeProvider timeProvider;
    
    private final boolean sendingDetailedEMail;

    /**
     * Note that this class is not a valid processing plugin as it does not provide the appropriate
     * constructor.
     */
    public AbstractDropboxProcessingPlugin(Properties properties, File storeRoot,
            IPostRegistrationDatasetHandler dropboxHandler)
    {
        this(properties, storeRoot, dropboxHandler, SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }

    public AbstractDropboxProcessingPlugin(Properties properties, File storeRoot,
            IPostRegistrationDatasetHandler dropboxHandler, ITimeProvider timeProvider)
    {
        super(properties, storeRoot);
        this.dropboxHandler = dropboxHandler;
        this.timeProvider = timeProvider;
        sendingDetailedEMail = PropertyUtils.getBoolean(properties, SEND_DETAILED_EMAIL_KEY, false);
    }
    
    public ProcessingStatus process(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        final ProcessingStatus result = new ProcessingStatus();
        for (DatasetDescription dataset : datasets)
        {
            long startTime = timeProvider.getTimeInMilliseconds();
            Status status = processDataset(dataset, context.getParameterBindings());
            if (sendingDetailedEMail)
            {
                sendDetailedEMail(startTime, dataset, status, context);
            }
            result.addDatasetStatus(dataset, status);
        }
        return result;
    }

    private void sendDetailedEMail(long startTime, DatasetDescription dataSetDescription,
            Status status, DataSetProcessingContext context)
    {
        String dataSet =
                render(dataSetDescription.getDatasetCode(), dataSetDescription.getDatasetTypeCode());
        String experiment =
                render(dataSetDescription.getExperimentIdentifier(),
                        dataSetDescription.getExperimentTypeCode());
        String sampleOrNull =
                render(dataSetDescription.getSampleIdentifier(),
                        dataSetDescription.getSampleTypeCode());
        boolean withSample = sampleOrNull != null;
        boolean processingFailed = status.isError();
        String processingDescription = getProcessingDescription();
        Template template = getEMailMessageTemplate(processingFailed, withSample).createFreshCopy();
        String subject;
        if (processingFailed)
        {
            template.bind("error", status.tryGetErrorMessage());
            subject = "Processing of data set " + dataSet + " failed";
        } else
        {
            subject = "Data set " + dataSet + " successfully processed";
        }
        template.bind("processing-description", processingDescription);
        template.bind("data-set", dataSet);
        template.bind("experiment", experiment);
        if (withSample)
        {
            template.bind("sample", sampleOrNull);
        }
        SimpleDateFormat dateFormat =
                new SimpleDateFormat(BasicConstant.RENDERED_CANONICAL_DATE_FORMAT_PATTERN);
        template.bind("start-time", dateFormat.format(new Date(startTime)));
        template.bind("end-time", dateFormat.format(new Date(timeProvider.getTimeInMilliseconds())));
        IMailClient mailClient = context.getMailClient();
        EMailAddress eMailAddress = new EMailAddress(context.getUserEmailOrNull());
        mailClient.sendEmailMessage(subject, template.createText(), null, null, eMailAddress);
    }
    
    private Template getEMailMessageTemplate(boolean withError, boolean withSample)
    {
        return new Template(
                (withError ? (withSample ? EMAIL_MESSAGE_TEMPLATE_LINE1_WITH_ERROR_WITH_SAMPLE
                        : EMAIL_MESSAGE_TEMPLATE_LINE1_WITH_ERROR)
                        : (withSample ? EMAIL_MESSAGE_TEMPLATE_LINE1_WITH_SAMPLE
                                : EMAIL_MESSAGE_TEMPLATE_LINE1))
                        + "Processing description: ${processing-description}\n"
                        + "Processing started at ${start-time}.\n"
                        + "Processing finished at ${end-time}.");
    }
    
    /**
     * Returns a description to be used in e-mails.
     */
    protected abstract String getProcessingDescription();
    
    private String render(String entity, String entityType)
    {
        return entity == null ? null : entity + " [" + entityType + "]";
    }

    private Status processDataset(DatasetDescription dataset, Map<String, String> parameterBindings)
    {
        File originalDir = getDataSubDir(dataset);
        if (originalDir.isDirectory() == false)
        {
            operationLog
                    .warn("Dataset directory does not exist and will be silently excluded from the processing: "
                            + originalDir.getPath());
            return Status.createError(MISSING_DIRECTORY_MSG);
        }
        File[] datasetFiles = FileUtilities.listFiles(originalDir);
        if (datasetFiles.length == 1)
        {
            DataSetInformation datasetInfo = createDatasetInfo(dataset);
            return dropboxHandler.handle(datasetFiles[0], datasetInfo, parameterBindings);
        } else
        {
            operationLog.error(String.format("Exactly one item was expected in the '%s' directory,"
                    + " but %d have been found. Nothing will be processed.", originalDir
                    .getParent(), datasetFiles));
            final String errorMsg =
                    datasetFiles.length > 1 ? MORE_THAN_ONE_ITEM_MSG : EMPTY_DIRECTORY_MSG;
            return Status.createError(errorMsg);
        }
    }

    private DataSetInformation createDatasetInfo(DatasetDescription dataset)
    {
        DataSetInformation datasetInfo = new DataSetInformation();
        String datasetTypeCode = dataset.getDatasetTypeCode();
        datasetInfo.setDataSetType(new DataSetType(datasetTypeCode));
        datasetInfo.setSampleCode(dataset.getSampleCode());
        datasetInfo.setSpaceCode(dataset.getGroupCode());
        datasetInfo.setDataSetCode(dataset.getDatasetCode());
        ExperimentIdentifier expIdent =
                new ExperimentIdentifier(null, dataset.getGroupCode(), dataset.getProjectCode(),
                        dataset.getExperimentCode());
        datasetInfo.setExperimentIdentifier(expIdent);
        return datasetInfo;
    }

}
