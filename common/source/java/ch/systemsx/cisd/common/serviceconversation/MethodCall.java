/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.serviceconversation;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MethodCall implements Serializable
{
    private static final long serialVersionUID = 8679256131459236150L;

    private String methodName;
    private Object[] arguments;
    
    public MethodCall(String methodName, Object[] arguments) {
        this.methodName = methodName;
        this.arguments = arguments;
    }

    public Serializable executeOn(Object o) {

        List<Class<?>> argClasses = new ArrayList<Class<?>>();
        for (Object s : this.arguments) {
            argClasses.add(s.getClass());
        }
        Exception ex;
        try
        {
            Method m = o.getClass().getMethod(this.methodName, argClasses.toArray(new Class<?>[0]));
            try
            {
                return (Serializable)m.invoke(o, this.arguments);
            } catch (IllegalArgumentException e)
            {
                ex = e;
            } catch (IllegalAccessException e)
            {
                ex = e;
            } catch (InvocationTargetException e)
            {
                ex = e;
            }
        } catch (SecurityException e)
        {
            ex = e;
        } catch (NoSuchMethodException e)
        {
            ex = e;
        }
        throw new RuntimeException("Method call failed", ex);
    }
    
    @Override
    public String toString() {
        return "MethodCall "+this.methodName+"("+Arrays.asList(this.arguments)+")";
    }
    
}
