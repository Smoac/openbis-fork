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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Special class loader for classes based on jython 2.7. The jython JAR file has to be provided in the
 * constructor. 
 * <ul>
 * <li>This class loader always try first to find and load the class from the specified JAR file.
 * <li>If it isn't found and if its fully-qualified class name does not match <tt>*.v27.*</tt> the class is 
 * loaded by the application class loader. 
 * <li>A class not found in the provided JAR file and fully-qualified class name does match <tt>*.v27.*</tt>
 * are loaded with this class loader.
 * </ul>
 *
 * @author Franz-Josef Elmer
 */
public class Jython27ClassLoader extends ClassLoader
{
    private final ClassLoader jythonJarClassLoader;

    public Jython27ClassLoader(File jythonJar)
    {
        if (jythonJar.exists() == false)
        {
            throw new EnvironmentFailureException("JAR file does not exist: " + jythonJar.getAbsolutePath());
        }
        if (jythonJar.isDirectory())
        {
            throw new EnvironmentFailureException("JAR file is a directory: " + jythonJar.getAbsolutePath());
        }
        try
        {
            URL url = jythonJar.toURI().toURL();
            jythonJarClassLoader = new URLClassLoader(new URL[] { url }, null);
        } catch (MalformedURLException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException
    {
        try
        {
            return jythonJarClassLoader.loadClass(name);
        } catch (ClassNotFoundException ex)
        {
            if (name.contains(".v27.") == false)
            {
                return super.loadClass(name);
            }
            synchronized (getClassLoadingLock(name))
            {
                InputStream stream = getResourceAsStream(name.replace('.', '/') + ".class");
                if (stream != null)
                {
                    try
                    {
                        byte[] bytes = IOUtils.toByteArray(stream);
                        return defineClass(name, bytes, 0, bytes.length);
                    } catch (IOException ex2)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex2);
                    } finally
                    {
                        IOUtils.closeQuietly(stream);
                    }
                }
                return super.loadClass(name);
            }
        }
    }

}