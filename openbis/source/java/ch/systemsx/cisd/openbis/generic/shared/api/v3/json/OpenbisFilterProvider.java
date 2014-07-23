package ch.systemsx.cisd.openbis.generic.shared.api.v3.json;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

/**
 * Filter which modifies openbis api internal data transfer objects into simple json structures
 * 
 * @author Jakub Straszewski
 */
public class OpenbisFilterProvider extends FilterProvider
{

    public static class OpenisPropertyFilter extends SimpleBeanPropertyFilter
    {
        @Override
        public void serializeAsField(Object bean, JsonGenerator jgen,
                SerializerProvider provider,
                BeanPropertyWriter writer) throws Exception
        {
            Method method = getMethod(writer);
            if (method.getReturnType()
                    .equals(FieldUpdateValue.class))
            {
                FieldUpdateValue<?> result = (FieldUpdateValue<?>) method.invoke(bean);
                generateFieldUpdateValue(jgen, writer, result);
            }
            else if (method.getReturnType()
                    .equals(ListUpdateValue.class))
            {
                ListUpdateValue<?> result = (ListUpdateValue<?>) method.invoke(bean);
                generateListUpdateValue(jgen, writer, result);
            }
            else
            {
                // default behavior
                writer.serializeAsField(bean, jgen, provider);
            }
        }

        private Method getMethod(BeanPropertyWriter writer)
        {
            return getAnnotatedMethod(writer).getMember();
        }

        private AnnotatedMethod getAnnotatedMethod(BeanPropertyWriter writer)
        {
            return (com.fasterxml.jackson.databind.introspect.AnnotatedMethod) writer.getMember();
        }

        private void generateFieldUpdateValue(JsonGenerator jgen, BeanPropertyWriter writer, FieldUpdateValue<?> result) throws IOException,
                JsonGenerationException, JsonProcessingException
        {
            if (result.isModified())
            {
                jgen.writeFieldName(writer.getName());
                jgen.writeObject(result.getValue());
            }
        }

        private void generateListUpdateValue(JsonGenerator jgen, BeanPropertyWriter writer, ListUpdateValue<?> result)
                throws JsonGenerationException, IOException
        {
            List<?> actions = result.getActions();
            if (actions.size() > 0)
            {
                jgen.writeArrayFieldStart(writer.getName());
                for (Object action : actions)
                {
                    jgen.writeObject(action);
                }
                jgen.writeEndArray();
            }
        }

    }

    @Override
    public BeanPropertyFilter findFilter(Object paramObject)
    {
        return new OpenisPropertyFilter();
    }

}
