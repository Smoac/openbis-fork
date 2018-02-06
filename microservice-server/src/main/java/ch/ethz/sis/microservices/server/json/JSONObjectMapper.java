package ch.ethz.sis.microservices.server.json;

import java.io.FileInputStream;

public interface JSONObjectMapper
{

    <T> T readValue(FileInputStream src, Class<T> valueType) throws Exception;

    byte[] writeValue(Object value) throws Exception;

}
