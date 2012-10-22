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

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.uitest.request.Request;
import ch.systemsx.cisd.openbis.uitest.webdriver.Pages;

/**
 * @author anttil
 */
public class Application
{

    private Map<String, Object> map;

    private Pages pages;

    private ICommonServer commonServer;

    private IETLLIMSService etlService;

    private IDssServiceRpcGeneric dss;

    private String session;

    private IGenericServer genericServer;

    private IGeneralInformationService generalInformationService;

    private IGeneralInformationChangingService generalInformationChangingService;

    public Application(String asUrl, String dssUrl, Pages pages)
    {
        map = new HashMap<String, Object>();

        this.pages = pages;
        this.commonServer =
                HttpInvokerUtils.createServiceStub(ICommonServer.class,
                        asUrl + "/openbis/rmi-common", 60000);

        this.genericServer =
                HttpInvokerUtils.createServiceStub(IGenericServer.class,
                        asUrl + "/openbis/rmi-plugin-generic", 60000);

        this.generalInformationService =
                HttpInvokerUtils.createServiceStub(IGeneralInformationService.class,
                        asUrl + "/openbis/rmi-general-information-v1", 60000);

        this.generalInformationChangingService =
                HttpInvokerUtils.createServiceStub(IGeneralInformationChangingService.class,
                        asUrl + "/openbis/rmi-general-information-changing-v1", 60000);

        this.etlService =
                HttpInvokerUtils.createServiceStub(IETLLIMSService.class,
                        asUrl + "/openbis/rmi-etl", 60000);

        this.dss =
                HttpInvokerUtils.createStreamSupportingServiceStub(IDssServiceRpcGeneric.class,
                        dssUrl + "/datastore_server/rmi-dss-api-v1", 60000);

        this.session =
                commonServer
                        .tryToAuthenticate(SeleniumTest.ADMIN_USER, SeleniumTest.ADMIN_PASSWORD)
                        .getSessionToken();

    }

    public Application()
    {
        map = new HashMap<String, Object>();
    }

    public <T extends Request<U>, U> void setExecutor(Class<? extends Request<U>> clazz,
            Executor<T, U> execution)
    {
        map.put(clazz.getName(), execution);
    }

    public <T extends Request<U>, U> U execute(T request)
    {

        // There is no way to express this in type of field 'map'.
        // But as the field 'map' is private and the methods manipulating it are defined to ensure
        // this requirement, we can safely do the unchecked cast.
        @SuppressWarnings("unchecked")
        Executor<T, U> execution =
                (Executor<T, U>) map.get(request.getClass().getName());
        execution.setApplicationRunner(this);
        execution.setPages(pages);
        execution.setCommonServer(commonServer);
        execution.setDss(dss);
        execution.setEtlService(etlService);
        execution.setSession(session);
        execution.setGenericServer(genericServer);
        execution.setGeneralInformationService(generalInformationService);
        execution.setGeneralInformationChangingService(generalInformationChangingService);
        return execution.run(request);
    }
}
