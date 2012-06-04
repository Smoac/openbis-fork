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

package ch.systemsx.cisd.common.conversation;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.common.serviceconversation.server.ServiceConversationServer;

/**
 * MethodInvocation represents a remote method invocation. It contains the name and the arguments of a 
 * method to be executed on a remote server. It is Serializable to be transferable through the service 
 * conversation framework.
 *
 * @author anttil
 */
public class MethodInvocation implements Serializable
{
    private static final long serialVersionUID = 8679256131459236150L;

    private String methodName;
    private Object[] arguments;
    
    public MethodInvocation(String methodName, Object[] arguments) {
        this.methodName = methodName;
        this.arguments = arguments;
    }

    /**
     * Executes the method on given target object. Adds a ProgressListener instance as a last argument
     * of the call.
     * 
     * @param target The target object on which the method call will be executed
     * @param server ServiceConversationServer that will receive the progress reports
     * @param conversationId Id of the conversation 
     * @param clientTimeOut The remote client making this method call will abort if it has not 
     * received any messages from the server within the timeout (represented in milliseconds)
     * @returns The return value of the method call
     */
    public Serializable executeOn(Object target, ServiceConversationServer server, String conversationId, int clientTimeOut) {

        RateLimitedProgressListener progressListener = new RateLimitedProgressListener(server, conversationId, clientTimeOut / 10);        
        Object[] argumentsWithProgressListener = createArgumentsWithProgressListener(progressListener);
        
        try
        {
            Method m = findMethodOn(target, this.methodName, argumentsWithProgressListener); 
            return (Serializable)m.invoke(target, argumentsWithProgressListener);
        } catch (InvocationTargetException e)
        {
            throw (RuntimeException)e.getCause();
        } catch (Exception e) {
            throw new RuntimeException("Method call failed", e);            
        } finally { 
            progressListener.close();
        }
    }
    
    private Object[] createArgumentsWithProgressListener(IProgressListener progressListener) {
        List<Object> argumentsWithProgressListener = new ArrayList<Object>();
        argumentsWithProgressListener.addAll(Arrays.asList(this.arguments));
        argumentsWithProgressListener.add(progressListener);
        return argumentsWithProgressListener.toArray(new Object[0]);
    }
    
    private Method findMethodOn(Object o, String name, Object[] args) throws SecurityException, NoSuchMethodException {

        for (Method method : o.getClass().getMethods()) {
            if (!method.getName().equals(name)) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != args.length) {
                continue;
            }

            boolean matches = true;
            for (int i = 0; i < parameterTypes.length; i++) {
                if (!parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return method;
            }
        }
        throw new NoSuchMethodException();
    }
    
    @Override
    public String toString() {
        return "MethodCall "+this.methodName+"("+Arrays.asList(this.arguments)+")";
    }
    
    public String getMethodName() {
        return this.methodName;
    } 
    
    public Object[] getArguments() {
        return this.arguments;
    }
}
