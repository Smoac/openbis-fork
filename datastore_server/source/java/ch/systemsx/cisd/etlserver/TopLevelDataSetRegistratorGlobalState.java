/*
 * Copyright 2011 ETH Zuerich, CISD
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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSourceQueryService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * Global state needed by top level data set registrators.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class TopLevelDataSetRegistratorGlobalState
{
    // can be used from dropboxes
    public static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            TopLevelDataSetRegistratorGlobalState.class);

    private final String dssCode;

    private final String shareId;

    private final File storeRootDir;

    private final File dssInternalTempDir;

    private final IEncapsulatedOpenBISService openBisService;

    private final IMailClient mailClient;

    private final IDataSetValidator dataSetValidator;

    private final IDataSourceQueryService dataSourceQueryService;

    private final boolean notifySuccessfulRegistration;

    private final ThreadParameters threadParameters;

    private final boolean useIsFinishedMarkerFile;

    private final boolean deleteUnidentified;

    private final DynamicTransactionQueryFactory dynamicTransactionQueryFactory;

    private final String preRegistrationScriptOrNull;

    private final String postRegistrationScriptOrNull;

    private final String[] validationScriptsOrNull;

    /**
     * Constructor that takes some values from the thread parameters.
     * 
     * @param dssCode
     * @param shareId
     * @param storeRootDir
     * @param openBisService
     * @param mailClient
     * @param dataSetValidator
     * @param notifySuccessfulRegistration
     * @param threadParameters
     */
    public TopLevelDataSetRegistratorGlobalState(String dssCode, String shareId, File storeRootDir,
            File dssInternalTempDir, IEncapsulatedOpenBISService openBisService,
            IMailClient mailClient, IDataSetValidator dataSetValidator,
            IDataSourceQueryService dataSourceQueryService,
            DynamicTransactionQueryFactory dynamicTransactionQueryFactory,
            boolean notifySuccessfulRegistration, ThreadParameters threadParameters)
    {
        this(dssCode, shareId, storeRootDir, dssInternalTempDir, openBisService, mailClient,
                dataSetValidator, dataSourceQueryService, dynamicTransactionQueryFactory,
                notifySuccessfulRegistration, threadParameters, threadParameters
                        .useIsFinishedMarkerFile(), threadParameters.deleteUnidentified(),
                threadParameters.tryGetPreRegistrationScript(), threadParameters
                        .tryGetPostRegistrationScript(), threadParameters.tryGetValidationScripts());
    }

    public TopLevelDataSetRegistratorGlobalState(String dssCode, String shareId, File storeRootDir,
            File dssInternalTempDir, IEncapsulatedOpenBISService openBisService,
            IMailClient mailClient, IDataSetValidator dataSetValidator,
            IDataSourceQueryService dataSourceQueryService,
            DynamicTransactionQueryFactory dynamicTransactionQueryFactory,
            boolean notifySuccessfulRegistration, ThreadParameters threadParameters,
            boolean useIsFinishedMarkerFile, boolean deleteUnidentified,

            String preRegistrationScriptOrNull, String postRegistrationScriptOrNull,
            String[] validationScriptsOrNull)
    {
        this.dssCode = dssCode;
        this.shareId = shareId;
        this.storeRootDir = storeRootDir;
        this.dssInternalTempDir = dssInternalTempDir;
        this.openBisService = openBisService;
        this.mailClient = mailClient;
        this.dataSetValidator = dataSetValidator;
        this.dataSourceQueryService = dataSourceQueryService;
        this.dynamicTransactionQueryFactory = dynamicTransactionQueryFactory;
        this.notifySuccessfulRegistration = notifySuccessfulRegistration;
        this.threadParameters = threadParameters;
        this.useIsFinishedMarkerFile = useIsFinishedMarkerFile;
        this.deleteUnidentified = deleteUnidentified;
        this.preRegistrationScriptOrNull = preRegistrationScriptOrNull;
        this.postRegistrationScriptOrNull = postRegistrationScriptOrNull;
        this.validationScriptsOrNull = validationScriptsOrNull;
    }

    public String getDssCode()
    {
        return dssCode;
    }

    public String getShareId()
    {
        return shareId;
    }

    public File getStoreRootDir()
    {
        return storeRootDir;
    }

    /**
     * Get a directory that can be used for temporary files, and is local to the server.
     */
    public File getDssInternalTempDir()
    {
        return dssInternalTempDir;
    }

    public IEncapsulatedOpenBISService getOpenBisService()
    {
        return openBisService;
    }

    public IMailClient getMailClient()
    {
        return mailClient;
    }

    public IDataSetValidator getDataSetValidator()
    {
        return dataSetValidator;
    }

    public IDataSourceQueryService getDataSourceQueryService()
    {
        return dataSourceQueryService;
    }

    public DynamicTransactionQueryFactory getDynamicTransactionQueryFactory()
    {
        return dynamicTransactionQueryFactory;
    }

    public boolean isNotifySuccessfulRegistration()
    {
        return notifySuccessfulRegistration;
    }

    public ThreadParameters getThreadParameters()
    {
        return threadParameters;
    }

    public boolean isUseIsFinishedMarkerFile()
    {
        return useIsFinishedMarkerFile;
    }

    public boolean isDeleteUnidentified()
    {
        return deleteUnidentified;
    }

    public String getPreRegistrationScript()
    {
        return preRegistrationScriptOrNull;
    }

    public String getPostRegistrationScript()
    {
        return postRegistrationScriptOrNull;
    }

    public String[] getValidationScriptsOrNull()
    {
        return validationScriptsOrNull;
    }

    /**
     * Return the email addresses of all administrator users registered on the openBIS AS.
     */
    public List<String> getAdministratorEmails()
    {
        List<String> emails = new ArrayList<String>();

        List<Person> administrators = openBisService.listAdministrators();
        for (Person admin : administrators)
        {
            String email = admin.getEmail();
            if (StringUtils.isNotBlank(email))
            {
                emails.add(email);
            }
        }
        return emails;
    }
}
