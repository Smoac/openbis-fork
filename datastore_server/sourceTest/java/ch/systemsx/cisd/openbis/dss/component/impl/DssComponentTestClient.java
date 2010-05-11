/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.component.impl;

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DssComponentTestClient
{

    public static void main(String[] args)
    {
        configureLogging();
        System.out.println("Logging in");
        DssComponent component =
                DssComponent.tryCreate("test", "foobar", "http://localhost:8888/openbis");
        component.getDataSet("20100318094819344-4");
        component.logout();
        System.out.println("Logging out");
    }

    private static void configureLogging()
    {
        Properties props = new Properties();
        props.put("log4j.appender.STDOUT", "org.apache.log4j.ConsoleAppender");
        props.put("log4j.appender.STDOUT.layout", "org.apache.log4j.PatternLayout");
        props.put("log4j.appender.STDOUT.layout.ConversionPattern", "%d %-5p [%t] %c - %m%n");
        props.put("log4j.rootLogger", "INFO, STDOUT");
        PropertyConfigurator.configure(props);
    }

}
