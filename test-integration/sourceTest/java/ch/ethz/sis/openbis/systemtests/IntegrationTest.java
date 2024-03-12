package ch.ethz.sis.openbis.systemtests;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;

public class IntegrationTest extends AbstractIntegrationTest
{

    @Test
    public void helloWorld()
    {
        IApplicationServerApi v3 = applicationContext.getBean(IApplicationServerApi.class);
        String sessionToken = v3.login("test", "password");
        System.out.println("Session token: " + sessionToken);
    }

}
