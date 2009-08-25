import java.util.List;

import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
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
 * @author walshs
 */
public class BisUtil
{

    final String userId = "test";

    String serverURL = "";

    final String commonServerPath = "rmi-common";

    final String genericServerPath = "rmi-plugin-generic";

    ICommonServer commonServer;

    IGenericServer genericServer;

    Session session;

    Session sessionGeneric;

    String password;

    public BisUtil(String su, String pass)
    {

        serverURL = su;

        password = pass;

        commonServer =
                HttpInvokerUtils.createServiceStub(ICommonServer.class, serverURL + "/"
                        + commonServerPath, 5);

        session = commonServer.tryToAuthenticate(userId, password);

        genericServer =
                HttpInvokerUtils.createServiceStub(IGenericServer.class, serverURL + "/"
                        + genericServerPath, 5);

    }

    public void getSampleCodeFromId(Long id)
    {

        SampleGenerationDTO sampleInfo =
                genericServer.getSampleInfo(session.getSessionToken(), new TechId(id));
        System.out.println(sampleInfo.getGenerator().getCode());

    }

    public void listUsersAndRoles()
    {

        List<RoleAssignment> roles = commonServer.listRoleAssignments(session.getSessionToken());
        for (RoleAssignment r : roles)
        {
            System.out.print(r.getRoleSetCode() + "\t");
            Person p = r.getPerson();
            System.out.println(p.getUserId() + "\t" + p.getEmail());
        }
    }

    public void dumpUsersAndRoles()
    {

        List<RoleAssignment> roles = commonServer.listRoleAssignments(session.getSessionToken());
        for (RoleAssignment r : roles)
        {
            System.out.println(r.toString() + "\n");
        }

    }

    public void enableUsers()
    {

        List<Person> persons = commonServer.listPersons(session.getSessionToken());

        for (@SuppressWarnings("unused")
        Person p : persons)
        {

        }

    }

}
