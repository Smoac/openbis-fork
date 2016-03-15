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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
        System.out.println("PARAMETERS:");
        Object obj = parameters.get("object");
        boolean echo = parameters.containsKey("echo");
        System.out.println("echo: " + echo + ", object: " + obj + " (" + obj.getClass().getName() + ")");
        return echo ? obj : populate(obj);
    }

    public static Object populate(Object obj)
    {
        List<String> ignore = new ArrayList<>();
        for (Method ign : Object.class.getMethods()) {
            ignore.add(ign.getName());
        }
        
        List<String> methds = new ArrayList<>();
        
        for (Method method : obj.getClass().getMethods())
        {
            if (ignore.contains(method.getName())) {
                continue;
            }
            methds.add(method.getName());
            
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1) {
                Class<?> type = parameterTypes[0];
                {
                    try
                    {
                        setItUp(obj, method, type);
                    } catch (Exception e)
                    {
//                        return method.getName() + " " + getValue(type);
//                        e.printStackTrace();
                    }
                } 
            } else {
                
            }
        }
        return obj;
    }

    public static void setItUp(Object obj, Method method, Class<?> type) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        method.invoke(obj, getValue(type));
    }

    public static Object getValue(Class<?> type)
    {
        double random = Math.random();
        long rnd = (long) (random*1000000);
        if (type == String.class) {
            return String.valueOf(random);
        } 
        
        if (type == Date.class) {
            return new Date(rnd);
        } 

        if (type == Long.class || type == Long.TYPE) {
            return rnd;
        } 
        
        if (type == Boolean.class || type == Boolean.TYPE) {
            return random < 0.5;
        } 
        System.out.println("Complex type: " + type.getName());
        
        return null;
    }
}
