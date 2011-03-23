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

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;

/**
 * Global state needed by top level data set registrators.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class TopLevelDataSetRegistratorGlobalState
{
    private final String dssCode;

    private final String shareId;

    private final File storeRootDir;

    private final IEncapsulatedOpenBISService openBisService;

    private final IMailClient mailClient;

    private final IDataSetValidator dataSetValidator;

    private final boolean notifySuccessfulRegistration;

    private final ThreadParameters threadParameters;

    private final boolean useIsFinishedMarkerFile;

    private final boolean deleteUnidentified;

    private final String preRegistrationScriptOrNull;

    private final String postRegistrationScriptOrNull;

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
            IEncapsulatedOpenBISService openBisService, IMailClient mailClient,
            IDataSetValidator dataSetValidator, boolean notifySuccessfulRegistration,
            ThreadParameters threadParameters)
    {
        this(dssCode, shareId, storeRootDir, openBisService, mailClient, dataSetValidator,
                notifySuccessfulRegistration, threadParameters, threadParameters
                        .useIsFinishedMarkerFile(), threadParameters.deleteUnidentified(),
                threadParameters.tryGetPreRegistrationScript(), threadParameters
                        .tryGetPostRegistrationScript());

    }

    public TopLevelDataSetRegistratorGlobalState(String dssCode, String shareId, File storeRootDir,
            IEncapsulatedOpenBISService openBisService, IMailClient mailClient,
            IDataSetValidator dataSetValidator, boolean notifySuccessfulRegistration,
            ThreadParameters threadParameters, boolean useIsFinishedMarkerFile,
            boolean deleteUnidentified, String preRegistrationScriptOrNull,
            String postRegistrationScriptOrNull)
    {
        this.dssCode = dssCode;
        this.shareId = shareId;
        this.storeRootDir = storeRootDir;
        this.openBisService = openBisService;
        this.mailClient = mailClient;
        this.dataSetValidator = dataSetValidator;
        this.notifySuccessfulRegistration = notifySuccessfulRegistration;
        this.threadParameters = threadParameters;
        this.useIsFinishedMarkerFile = useIsFinishedMarkerFile;
        this.deleteUnidentified = deleteUnidentified;
        this.preRegistrationScriptOrNull = preRegistrationScriptOrNull;
        this.postRegistrationScriptOrNull = postRegistrationScriptOrNull;
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
}
