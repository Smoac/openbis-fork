package ch.ethz.sis.afsclient.client;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.ethz.sis.afsserver.server.Server;
import ch.ethz.sis.afsserver.server.observer.impl.DummyServerObserver;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.shared.startup.Configuration;

public class ClientTest
{
    
    private static Server afsServer;

    @BeforeClass
    public static void classSetUp() throws Exception {
        final Configuration configuration = new Configuration(List.of(AtomicFileSystemServerParameter.class),
                "src/test/resources/afs-server-config.properties");
        final DummyServerObserver dummyServerObserver = new DummyServerObserver();
        afsServer = new Server<>(configuration, dummyServerObserver, dummyServerObserver);
    }

    @AfterClass
    public static void classTearDown() throws Exception {
        afsServer.shutdown(true);
    }

    @Test
    public void testLogin() {
    }

    @Test
    public void testIsSessionValid() {
    }

    @Test
    public void testLogout() {
    }

    @Test
    public void testList() {
    }

    @Test
    public void testRead() {
    }

    @Test
    public void testWrite() {
    }

    @Test
    public void testDelete() {
    }

    @Test
    public void testCopy() {
    }

    @Test
    public void testMove() {
    }

    @Test
    public void testBegin() {
    }

    @Test
    public void testPrepare() {
    }

    @Test
    public void testCommit() {
    }

    @Test
    public void testRollback() {
    }

    @Test
    public void testRecover() {
    }

}