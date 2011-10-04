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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;

/**
 * An object holding all necessary context information for ftp path resolution.
 * 
 * @author Kaloyan Enimanev
 */
public class FtpPathResolverContext
{

    private final String sessionToken;

    private final IETLLIMSService service;

    private final IGeneralInformationService generalInfoService;
    
    private final IFtpPathResolverRegistry resolverRegistry;

    public FtpPathResolverContext(String sessionToken, IETLLIMSService service, IGeneralInformationService generalInfoService,
            IFtpPathResolverRegistry resolverRegistry)
    {
        this.sessionToken = sessionToken;
        this.service = service;
        this.generalInfoService = generalInfoService;
        this.resolverRegistry = resolverRegistry;
    }

    public String getSessionToken()
    {
        return sessionToken;
    }

    public IETLLIMSService getService()
    {
        return service;
    }

    public IGeneralInformationService getGeneralInfoService()
    {
        return generalInfoService;
    }
    
    public IFtpPathResolverRegistry getResolverRegistry()
    {
        return resolverRegistry;
    }

}
