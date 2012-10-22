/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.dsl;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.uitest.layout.Location;
import ch.systemsx.cisd.openbis.uitest.request.Request;
import ch.systemsx.cisd.openbis.uitest.webdriver.Pages;

/**
 * @author anttil
 */
public abstract class Executor<T extends Request<U>, U>
{
    private Application openbis;

    protected ICommonServer commonServer;

    protected IETLLIMSService etlService;

    protected IGenericServer genericServer;

    protected IGeneralInformationService generalInformationService;

    protected IGeneralInformationChangingService generalInformationChangingService;

    protected IDssServiceRpcGeneric dss;

    protected String session;

    private Pages pages;

    abstract public U run(T request);

    protected <V> V load(Class<V> pageClass)
    {
        return pages.load(pageClass);
    }

    protected <V> V goTo(Location<V> location)
    {
        return pages.goTo(location);
    }

    protected <V> V execute(Request<V> function)
    {
        return openbis.execute(function);
    }

    public void setPages(Pages pages)
    {
        this.pages = pages;
    }

    public void setCommonServer(ICommonServer commonServer)
    {
        this.commonServer = commonServer;
    }

    public void setEtlService(IETLLIMSService etlService)
    {
        this.etlService = etlService;
    }

    public void setDss(IDssServiceRpcGeneric dss)
    {
        this.dss = dss;
    }

    public void setSession(String session)
    {
        this.session = session;
    }

    public void setApplicationRunner(Application openbis)
    {
        this.openbis = openbis;
    }

    public void setGenericServer(IGenericServer genericServer)
    {
        this.genericServer = genericServer;
    }

    public void setGeneralInformationService(IGeneralInformationService info)
    {
        this.generalInformationService = info;
    }

    public void setGeneralInformationChangingService(IGeneralInformationChangingService change)
    {
        this.generalInformationChangingService = change;
    }
}
