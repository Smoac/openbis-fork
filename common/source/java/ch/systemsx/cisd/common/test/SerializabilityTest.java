package ch.systemsx.cisd.common.test;

import java.io.Serializable;

import org.apache.commons.lang.SerializationUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Extend me in order to test the serializability of a class. Override my {@link #createInstance() createInstance}
 * methods to provide me with an object to test against. The object's class must implement
 * {@link java.io.Serializable Serializable}.
 * 
 * @see java.io.Serializable
 * @author Christian Ribeaud
 */
public abstract class SerializabilityTest
{

    private Serializable obj;

    /**
     * Creates and returns an instance of the class under test.
     * 
     * @return a new instance of the class under test
     * @throws Exception
     */
    protected abstract Serializable createInstance() throws Exception;

    /**
     * Sets up the test fixture.
     * 
     * @throws Exception
     */
    @BeforeClass
    public final void setUp() throws Exception
    {
        obj = createInstance();
        assert obj != null : "createInstance() returned null";
    }

    /**
     * Verifies that an instance of the class under test can be serialized and deserialized without error.
     */
    @Test
    public final void testSerializability() throws Exception
    {
        byte[] serial = SerializationUtils.serialize(obj);
        Serializable deserial = (Serializable) SerializationUtils.deserialize(serial);
        checkThawedObject(obj, deserial);
    }

    /**
     * Template method--override this to perform checks on the deserialized form of the object serialized in
     * {@link #testSerializability}. If not overridden, this asserts that the pre-serialization and deserialized forms
     * of the object compare equal via {@link java.lang.Object#equals(Object) equals}.
     * 
     * @param expected the pre-serialization form of the object
     * @param actual the deserialized form of the object
     */
    public void checkThawedObject(Serializable expected, Serializable actual) throws Exception
    {
        assert expected.equals(actual) : "thawed object comparison";
    }

}
