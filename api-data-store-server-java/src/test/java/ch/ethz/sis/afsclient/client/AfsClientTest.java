package ch.ethz.sis.afsclient.client;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.net.URI;

import org.junit.*;

public class AfsClientTest
{

    private static DummyHttpServer httpServer;

    private AfsClient afsClient;

    private static final int HTTP_SERVER_PORT = 8085;

    private static final String HTTP_SERVER_PATH = "/fileserver";

    @Before
    public void setUp() throws Exception
    {
        httpServer = new DummyHttpServer(HTTP_SERVER_PORT, HTTP_SERVER_PATH);
        httpServer.start();
        afsClient = new AfsClient(
                new URI("http", null, "localhost", HTTP_SERVER_PORT,
                        HTTP_SERVER_PATH, null, null));
    }

    @After
    public void tearDown()
    {
        httpServer.stop();
    }

    @Test
    public void login_methodIsPost() throws Exception
    {
        final String token = afsClient.login("test", "test");
        assertNotNull(token);
        assertEquals(token, afsClient.getSessionToken());
        assertEquals("POST", httpServer.getHttpExchange().getRequestMethod());
        assertTrue(httpServer.getLastRequestBody().length > 0);
    }

    @Test
    public void isSessionValid_withoutLogin_throwsException() throws Exception
    {
        try
        {
            afsClient.isSessionValid();
            fail();
        } catch (IllegalStateException e)
        {
            assertThat(e.getMessage(), containsString("No session information detected!"));
        }
    }

    @Test
    public void isSessionValid_afterLogin_methodIsGet() throws Exception
    {
        afsClient.login("test", "test");
        httpServer.setNextResponse("{\"result\": true}");

        Boolean result = afsClient.isSessionValid();

        assertTrue(result);
        assertEquals("GET", httpServer.getHttpExchange().getRequestMethod());
        assertArrayEquals(httpServer.getLastRequestBody(), new byte[0]);
    }

    @Test
    public void logout_withoutLogin_throwsException() throws Exception
    {
        try
        {
            afsClient.logout();
            fail();
        } catch (IllegalStateException e)
        {
            assertThat(e.getMessage(), containsString("No session information detected!"));
        }
    }

    @Test
    public void logout_sessionTokenIsCleared() throws Exception
    {
        afsClient.login("test", "test");
        assertNotNull(afsClient.getSessionToken());

        httpServer.setNextResponse("{\"result\": true}");

        Boolean result = afsClient.logout();
        assertTrue(result);
        assertNull(afsClient.getSessionToken());
        assertEquals("POST", httpServer.getHttpExchange().getRequestMethod());
        assertTrue(httpServer.getLastRequestBody().length > 0);
    }

    @Test
    public void list_methodIsGet() throws Exception
    {
        login();

        httpServer.setNextResponse("{\"result\": null}");
        afsClient.list("", "", true);

        assertEquals("GET", httpServer.getHttpExchange().getRequestMethod());
        assertArrayEquals(httpServer.getLastRequestBody(), new byte[0]);
    }

    @Test
    public void read_methodIsGet() throws Exception
    {
        login();

        byte[] data = "ABCD".getBytes();
        httpServer.setNextResponse(data);

        byte[] result = afsClient.read("", "", 0L, 1000);

        assertEquals("GET", httpServer.getHttpExchange().getRequestMethod());
        assertArrayEquals(data, result);
        assertArrayEquals(httpServer.getLastRequestBody(), new byte[0]);
    }

    @Test
    public void write_methodIsPost() throws Exception
    {
        login();

        httpServer.setNextResponse("{\"result\": true}");
        Boolean result = afsClient.write("", "", 0L, new byte[0], new byte[0]);

        assertEquals("POST", httpServer.getHttpExchange().getRequestMethod());
        assertTrue(result);
        assertTrue(httpServer.getLastRequestBody().length > 0);
    }

    @Test
    public void delete_methodIsDelete() throws Exception
    {
        login();

        httpServer.setNextResponse("{\"result\": true}");
        Boolean result = afsClient.delete("", "");

        assertEquals("DELETE", httpServer.getHttpExchange().getRequestMethod());
        assertTrue(result);
        assertTrue(httpServer.getLastRequestBody().length > 0);
    }

    @Test
    public void copy_methodIsPost() throws Exception
    {
        login();

        httpServer.setNextResponse("{\"result\": true}");
        Boolean result = afsClient.copy("", "", "", "");

        assertEquals("POST", httpServer.getHttpExchange().getRequestMethod());
        assertTrue(result);
        assertTrue(httpServer.getLastRequestBody().length > 0);
    }

    @Test
    public void move_methodIsPost() throws Exception
    {
        login();

        httpServer.setNextResponse("{\"result\": true}");
        Boolean result = afsClient.move("", "", "", "");

        assertEquals("POST", httpServer.getHttpExchange().getRequestMethod());
        assertTrue(result);
        assertTrue(httpServer.getLastRequestBody().length > 0);
    }

    @Test
    public void testBegin()
    {
    }

    @Test
    public void testPrepare()
    {
    }

    @Test
    public void testCommit()
    {
    }

    @Test
    public void testRollback()
    {
    }

    @Test
    public void testRecover()
    {
    }

    private void login() throws Exception
    {
        afsClient.login("test", "test");
    }

}