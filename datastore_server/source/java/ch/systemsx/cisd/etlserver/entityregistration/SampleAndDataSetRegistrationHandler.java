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

package ch.systemsx.cisd.etlserver.entityregistration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.etlserver.IDataSetHandlerWithMailClient;
import ch.systemsx.cisd.etlserver.entityregistration.SampleAndDataSetRegistrationGlobalState.SampleRegistrationMode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Dropbox that registers samples and datasets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleAndDataSetRegistrationHandler implements IDataSetHandlerWithMailClient
{
    static final String DATA_SPACE_CONTROL_FILE_KEY = "DEFAULT_SPACE";

    static final String DATA_SPACE_PROPERTIES_KEY = "default-space";

    static final String SAMPLE_TYPE_CONTROL_FILE_KEY = "SAMPLE_TYPE";

    static final String SAMPLE_TYPE_PROPERTIES_KEY = "sample-type";
    
    static final String DATA_SET_TYPE_CONTROL_FILE_KEY = "DATA_SET_TYPE";

    static final String DATA_SET_TYPE_PROPERTIES_KEY = "data-set-type";

    static final String SAMPLE_REGISTRATION_MODE_PROPERTIES_KEY = "sample-registration-mode";

    static final String USER_CONTROL_FILE_KEY = "USERID";

    static final String ERROR_EMAIL_RECIPIENTS_PROPERTIES_KEY = "error-mail-recipients";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            SampleAndDataSetRegistrationHandler.class);

    private final SampleAndDataSetRegistrationGlobalState globalState;

    public SampleAndDataSetRegistrationHandler(Properties parentProperties,
            IDataSetHandler delegator, IEncapsulatedOpenBISService service)
    {
        Properties specificProperties =
                ExtendedProperties.getSubset(parentProperties,
                        IDataSetHandler.DATASET_HANDLER_KEY + '.', true);
        globalState = createGlobalState(specificProperties, service);
    }

    private static SampleAndDataSetRegistrationGlobalState createGlobalState(
            Properties specificProperties, IEncapsulatedOpenBISService service)
    {
        String spaceIdentifierStringOrNull =
                PropertyUtils.getProperty(specificProperties, DATA_SPACE_PROPERTIES_KEY);
        SpaceIdentifier spaceIdentifier =
                (null == spaceIdentifierStringOrNull) ? null : new SpaceIdentifier(
                        DatabaseInstanceIdentifier.createHome(), spaceIdentifierStringOrNull);

        String sampleTypeCodeOrNull =
                PropertyUtils.getProperty(specificProperties, SAMPLE_TYPE_PROPERTIES_KEY);
        SampleType sampleTypeOrNull = null;
        if (null != sampleTypeCodeOrNull)
        {
            sampleTypeOrNull = new SampleType();
            sampleTypeOrNull.setCode(sampleTypeCodeOrNull);
        }

        String dataSetTypeCodeOrNull =
                PropertyUtils.getProperty(specificProperties, DATA_SET_TYPE_PROPERTIES_KEY);
        DataSetType dataSetTypeOrNull = null;
        if (null != dataSetTypeCodeOrNull)
        {
            dataSetTypeOrNull = new DataSetType();
            dataSetTypeOrNull.setCode(dataSetTypeCodeOrNull);
        }

        String sampleRegistrationModeStringOrNull =
                PropertyUtils.getProperty(specificProperties,
                        SAMPLE_REGISTRATION_MODE_PROPERTIES_KEY);
        SampleRegistrationMode registrationMode;
        try
        {
            registrationMode =
                    (null == sampleRegistrationModeStringOrNull) ? SampleRegistrationMode.ACCEPT_ALL
                            : SampleRegistrationMode.valueOf(sampleRegistrationModeStringOrNull);
        } catch (IllegalArgumentException e)
        {
            operationLog.warn(sampleRegistrationModeStringOrNull
                    + " is an unknown registration mode, defaulting to ACCEPT_ALL");
            registrationMode = SampleRegistrationMode.ACCEPT_ALL;
        }

        List<String> errorEmailRecipients =
                PropertyUtils.tryGetList(specificProperties, ERROR_EMAIL_RECIPIENTS_PROPERTIES_KEY);

        return new SampleAndDataSetRegistrationGlobalState(service, spaceIdentifier,
                sampleTypeOrNull, dataSetTypeOrNull, registrationMode, errorEmailRecipients,
                operationLog);
    }

    public void initializeMailClient(IMailClient mailClient)
    {
        globalState.initializeMailClient(mailClient);
    }

    public List<DataSetInformation> handleDataSet(File file)
    {
        try
        {
            SampleAndDataSetFolderProcessor folderProcessor =
                    new SampleAndDataSetFolderProcessor(globalState, file);
            folderProcessor.register();
        } catch (Throwable ex)
        {
            operationLog.error(ex);
        } finally
        {
            FileOperations.getMonitoredInstanceForCurrentThread().deleteRecursively(file);
            logFileDeletion(file);
        }
        return createReturnValue();
    }

    private ArrayList<DataSetInformation> createReturnValue()
    {
        return new ArrayList<DataSetInformation>();
    }

    private void logFileDeletion(File file)
    {
        String message = String.format("Deleting file '%s'.", file.getName());
        operationLog.debug(message);
    }
}
