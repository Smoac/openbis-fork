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

package ch.systemsx.cisd.openbis.uitest.infra;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

public class ScreenShotProxy implements InvocationHandler 
{

    private Object obj;

    public static Object newInstance(Object obj) {
        Collection<Class<?>> interfaces = new HashSet<Class<?>>();
        
        Class<?> current = obj.getClass();
        while (current != null) {
            for (Class<?> c : current.getInterfaces()) {
                interfaces.add(c);
            }
            current = current.getSuperclass();
        }
        
        return java.lang.reflect.Proxy.newProxyInstance(
            obj.getClass().getClassLoader(),
            interfaces.toArray(new Class<?>[0]),
            new ScreenShotProxy(obj));
    }

    private ScreenShotProxy(Object obj) {
        this.obj = obj;
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
    {
        Object result;
        try {
            if (m.getName().equals("click") || m.getName().equals("sendKeys")) {
                screenshot();
            }
            result = m.invoke(this.obj, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
            throw new RuntimeException("unexpected invocation exception: " +
                           e.getMessage());
        }
        return result;    
    }
    
    public static void screenshot() throws IOException {
        File file = ((TakesScreenshot)SeleniumTest.driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(file, new File("/tmp/screenshot-"+ new Date().getTime()+".png"));        
    }

}
