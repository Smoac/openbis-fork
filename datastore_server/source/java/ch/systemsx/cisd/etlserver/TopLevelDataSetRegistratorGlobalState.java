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

    private final IEncapsulatedOpenBISService openBisService;

    private final IMailClient mailClient;

    private final IDataSetValidator dataSetValidator;

    private final boolean notifySuccessfulRegistration;

    private final ThreadParameters threadParameters;

    private final boolean useIsFinishedMarkerFile;

    private final boolean deleteUnidentified;

    private final String preRegistrationScriptOrNull;

    private final String postRegistrationScriptOrNull;

    public TopLevelDataSetRegistratorGlobalState(String dssCode,
            IEncapsulatedOpenBISService openBisService, IMailClient mailClient,
            IDataSetValidator dataSetValidator, boolean notifySuccessfulRegistration,
            ThreadParameters threadParameters)

    {
        this.dssCode = dssCode;
        this.openBisService = openBisService;
        this.mailClient = mailClient;
        this.dataSetValidator = dataSetValidator;
        this.notifySuccessfulRegistration = notifySuccessfulRegistration;
        this.threadParameters = threadParameters;
        this.useIsFinishedMarkerFile = threadParameters.useIsFinishedMarkerFile();
        this.deleteUnidentified = threadParameters.deleteUnidentified();
        this.preRegistrationScriptOrNull = threadParameters.tryGetPreRegistrationScript();
        this.postRegistrationScriptOrNull = threadParameters.tryGetPostRegistrationScript();
    }

    public String getDssCode()
    {
        return dssCode;
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

    public String getPreRegistrationScriptOrNull()
    {
        return preRegistrationScriptOrNull;
    }

    public String getPostRegistrationScriptOrNull()
    {
        return postRegistrationScriptOrNull;
    }
}
