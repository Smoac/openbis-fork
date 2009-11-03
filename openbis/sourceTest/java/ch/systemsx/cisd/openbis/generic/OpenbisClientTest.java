package ch.systemsx.cisd.openbis.generic;

import java.util.List;

import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

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

/**
 * Demo of the openBIS client which uses HTTPInvoker. This test is not run automatically.
 * 
 * @author Tomasz Pylak
 */
public class OpenbisClientTest
{
    public static void main(String[] args)

    {
        String userId = "user";
        String userPassword = "password";

        String serverURL = "http://localhost:8888/openbis";

        String commonServerPath = "rmi-common";// CommonServiceServer
        ICommonServer commonServer =

        HttpInvokerUtils.createServiceStub(ICommonServer.class, serverURL + "/"

        + commonServerPath, 5);
        SessionContextDTO session = commonServer.tryToAuthenticate(userId, userPassword);
        List<Person> persons = commonServer.listPersons(session.getSessionToken());

        for (Person p : persons)
        {

            System.out.println(p.getUserId());
        }

        String genericServerPath = "rmi-plugin-generic"; // GenericServiceServer

        IGenericServer genericServer =

        HttpInvokerUtils.createServiceStub(IGenericServer.class, serverURL + "/"

        + genericServerPath, 5);
        SessionContextDTO sessionGeneric = genericServer.tryToAuthenticate(userId, userPassword);

        SampleParentWithDerived sampleInfo =
                genericServer.getSampleInfo(sessionGeneric.getSessionToken(), new TechId(1L));
        System.out.println(sampleInfo.getParent().getCode());
    }
}
