package ch.ethz.sis.openbis.systemtests;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

public class IntegrationTest extends AbstractIntegrationTest
{

    @Test
    public void helloWorld()
    {
        OpenBIS openBIS = new OpenBIS("http://localhost:" + TestInstanceHostUtils.getOpenBISPort() + "/openbis/openbis", "OLD_DSS_IS_NOT_USED", "http://localhost:8085/data-store-server");

        String sessionToken = openBIS.login("test","password");

        openBIS.getAfsServerFacade().write("another-sample", "anotherdir/anotherfile", 0L, "Hello World!".getBytes(StandardCharsets.UTF_8),
                calculateMD5("Hello World!".getBytes(StandardCharsets.UTF_8)));

        System.out.println("Session token: " + sessionToken);
    }

    public static byte[] calculateMD5(byte[] data)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            return md.digest();
        } catch (Exception e)
        {
            throw new RuntimeException("Checksum calculation failed", e);
        }
    }

}
