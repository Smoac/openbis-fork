package ch.ethz.sis.microservices.download.server.json;

import java.io.FileInputStream;

public interface JSONObjectMapper
{

    <T> T readValue(FileInputStream src, Class<T> valueType) throws Exception;

    byte[] writeValue(Object value) throws Exception;

}
