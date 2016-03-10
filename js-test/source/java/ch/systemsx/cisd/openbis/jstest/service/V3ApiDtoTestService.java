/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.jstest.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.plugin.service.ICustomASServiceExecutor;
import ch.ethz.sis.openbis.generic.asapi.v3.plugin.service.context.CustomASServiceContext;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class V3ApiDtoTestService implements ICustomASServiceExecutor
{
    public V3ApiDtoTestService(Properties properties)
    {
    }

    @Override
    public Object executeService(CustomASServiceContext context, CustomASServiceExecutionOptions options)
    {
        Map<String, Object> parameters = options.getParameters();
        String sessionToken = context.getSessionToken();
        System.out.println("SESSION TOKEN: " + sessionToken);
        System.out.println("PARAMETERS:");
        
        Object obj = parameters.get("object");
        return parameters.containsKey("echo") ? obj : populate(obj);
    }

    private Object populate(Object obj)
    {
        for (Method method : obj.getClass().getMethods())
        {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1) {
                Class<?> type = parameterTypes[0];
                if (type.isPrimitive() || type.equals(String.class) || type.equals(Date.class)){
                    try
                    {
                        setItUp(obj, method, type);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                } 
            }
        }
        return obj;
    }

    private void setItUp(Object obj, Method method, Class<?> type) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        method.invoke(obj, getValue(type));
    }

    private Object getValue(Class<?> type)
    {
        double random = Math.random();
        long rnd = (long) (random*1000000);
        if (type == String.class) {
            return String.valueOf(random);
        } 
        
        if (type == Date.class) {
            return new Date(rnd);
        } 

        if (type == Long.class) {
            return rnd;
        } 
        
        if (type == Boolean.class) {
            return random < 0.5;
        } 
        
        
        return null;
    }

}
