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

package ch.systemsx.cisd.openbis.uitest.dsl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.LinkedBlockingQueue;

import com.sun.jna.Platform;

import ch.systemsx.cisd.openbis.dss.generic.DataStoreServer;

/**
 * @author anttil
 */
public class StartDataStoreServer
{

    public static String go() throws Exception
    {
        System.out.println("STARTING DSS");

        DataStoreServer.main(new String[0]);

        System.out.println("DSS STARTED");

        String command;
        if (Platform.isLinux())
        {
            command =
                    "java -ea -Dpython.path=../../libraries/jython/jython-lib -Dfile.encoding=UTF-8 -classpath ../../datastore_server/targets/ant/classes:../../common/targets/ant/classes:../../libraries/log4j/log4j.jar:../../libraries/testng/testng-jdk15.jar:../../libraries/commons-lang/commons-lang.jar:../../libraries/commons-io/commons-io.jar:../../libraries/mail/mail.jar:../../libraries/jmock/jmock.jar:../../libraries/activation/activation.jar:../../libraries/commons-codec/commons-codec.jar:../../libraries/restrictionchecker/restrictions.jar:../../libraries/cglib/cglib-nodep.jar:../../libraries/jmock/hamcrest/hamcrest-core.jar:../../libraries/jmock/objenesis/objenesis-1.0.jar:../../libraries/jmock/hamcrest/hamcrest-library.jar:../../libraries/unix:../../libraries/cisd-base/cisd-base.jar:../../libraries/fast-md5/fast-md5.jar:../../libraries/jython/jython.jar:../../libraries/commons-httpclient/commons-httpclient.jar:../../libraries/commons-logging/commons-logging.jar:../../libraries/jaxb/jaxb-api.jar:../../libraries/jaxb/jsr173_1.0_api.jar:../../libraries/jaxb/jaxb-impl.jar:../../libraries/classycle/classycle.jar:../../libraries/commons-collections/commons-collections.jar:../../libraries/spring/spring-core.jar:../../libraries/spring/spring-context.jar:../../libraries/spring/spring-aop.jar:../../libraries/spring/third-party/aopalliance.jar:../../libraries/cisd-args4j/cisd-args4j.jar:../../libraries/jetty8/lib/server/servlet-api-3.0.jar:../../libraries/spring/spring.jar:../../libraries/spring/webmvc/spring-webmvc.jar:../../libraries/gwt2.4/gwt-servlet.jar:../../libraries/spring/third-party/stream-supporting-httpinvoker.jar:../../libraries/cisd-jhdf5/hdf5-linux-intel.jar:../../libraries/cisd-jhdf5/hdf5-macosx-intel.jar:../../libraries/cisd-jhdf5/hdf5-windows-intel.jar:../../libraries/poi/poi-ooxml-schemas.jar:../../libraries/poi/poi-ooxml.jar:../../libraries/poi/poi.jar:../../openbis/targets/www/WEB-INF/classes:../../authentication/targets/ant/classes:../../libraries/jmock/jmock-legacy.jar:../../libraries/jline/jline.jar:../../openbis-common/targets/ant/classes:../../libraries/hibernate-search/jms.jar:../../libraries/eodsql/eodsql.jar:../../libraries/hibernate-search/jsr250-api.jar:../../libraries/spring/spring-beans.jar:../../libraries/spring/spring-web.jar:../../libraries/spring/spring-jdbc.jar:../../libraries/hibernate-core/hibernate-core.jar:../../libraries/jetty8/lib/common/jetty-http.jar:../../libraries/jetty8/lib/common/jetty-io.jar:../../libraries/jetty8/lib/common/jetty-util.jar:../../libraries/jetty8/lib/server/jetty-continuation.jar:../../libraries/jetty8/lib/server/jetty-security.jar:../../libraries/jetty8/lib/server/jetty-server.jar:../../libraries/jetty8/lib/server/jetty-servlet.jar:../../libraries/jmock/hamcrest/hamcrest-integration.jar:../../libraries/dom4j/dom4j.jar:../../libraries/javassist/javassist.jar:../../libraries/reflections/lib/gson-1.4.jar:../../libraries/reflections/lib/guava-r08.jar:../../libraries/reflections/lib/jboss-vfs-3.0.0.CR5.jar:../../libraries/reflections/lib/xml-apis-1.0.b2.jar:../../libraries/reflections/reflections.jar:../../libraries/slf4j/slf4j.jar:../../libraries/jackson/jackson-annotations.jar:../../libraries/jackson/jackson-core.jar:../../libraries/jackson/jackson-databind.jar:../../libraries/jsonrpc4j/jsonrpc4j.jar:../../libraries/cisd-jhdf5/cisd-jhdf5-core.jar:../../libraries/cisd-jhdf5/cisd-jhdf5-tools.jar:../../openbis_api/targets/ant/classes:../../dbmigration/targets/ant/classes:../../libraries/commons-dbcp/commons-dbcp.jar:../../libraries/postgresql/postgresql.jar:../../libraries/h2/h2.jar:../../libraries/commons-pool/commons-pool.jar:../../libraries/apgdiff/apgdiff.jar:../../libraries/hibernate-validator/hibernate-validator.jar:../../libraries/antlr/antlr.jar:../../libraries/slf4j/log4j12/slf4j-log4j12.jar:../../libraries/spring/test/spring-test.jar:../../libraries/junit/junit.jar:../../libraries/hibernate-search/hibernate-search.jar:../../libraries/ehcache/ehcache.jar:../../libraries/lucene/lucene-core.jar:../../libraries/commons-fileupload/commons-fileupload.jar:../../libraries/lucene-highlighter/lucene-highlighter.jar:../../libraries/fastutil/fastutil.jar:../../libraries/gwt-debug-panel/gwt-debug-panel.jar:../../libraries/gwt-image-loader/gwt-image-loader.jar:../../libraries/gwt2.4/gwt-user.jar:../../libraries/gxt2.2.5/gxt.jar:../../libraries/gwt2.4/validation-api-1.0.0.GA.jar:../../libraries/gwt2.4/validation-api-1.0.0.GA-sources.jar:../../libraries/hibernate-commons-annotations/hibernate-commons-annotations.jar:../../libraries/jta/jta.jar:../../libraries/hibernate-jpa-2.0-api/hibernate-jpa-2.0-api.jar:../../libraries/validation-api/validation-api.jar:../../libraries/jetty8/lib/server/jetty-deploy.jar:../../libraries/jetty8/lib/server/jetty-webapp.jar:../../libraries/csv/csv.jar:../../libraries/imagej/ij.jar:../../libraries/jfreechart/jcommon-1.0.16.jar:../../libraries/jfreechart/jfreechart-1.0.13.jar:../../libraries/jai/jai_codec.jar:../../libraries/jai/jai_core.jar:../../libraries/cifex/cifex.jar:../../libraries/truezip/truezip.jar:../../libraries/cisd-image_readers/cisd-image_readers-bioformats.jar:../../libraries/cisd-image_readers/cisd-image_readers-jai.jar:../../libraries/cisd-image_readers/cisd-image_readers.jar:../../libraries/cisd-image_readers/cisd-image_readers-imagej.jar:../../libraries/poi/ooxml-lib/dom4j-1.6.1.jar:../../libraries/poi/ooxml-lib/geronimo-stax-api_1.0_spec-1.0.jar:../../libraries/poi/ooxml-lib/xmlbeans-2.3.0.jar:../../libraries/ftpserver/ftpserver-core.jar:../../libraries/mina/mina-core.jar:../../libraries/pngj/pngj.jar:../../libraries/gwt2.4/gwt-isserializable.jar:../../libraries/sshd/sshd-core.jar:etc/log.xml:../../libraries/cisd-hotdeploy/cisd-hotdeploy.jar ch.systemsx.cisd.openbis.dss.generic.DataStoreServer";
        } else
        {
            command =
                    "java -ea -Dpython.path=../../libraries/jython/jython-lib -Dfile.encoding=UTF-8 -classpath ../../datastore_server/targets/classes:../../common/targets/classes:../../libraries/log4j/log4j.jar:../../libraries/testng/testng-jdk15.jar:../../libraries/commons-lang/commons-lang.jar:../../libraries/commons-io/commons-io.jar:../../libraries/mail/mail.jar:../../libraries/jmock/jmock.jar:../../libraries/activation/activation.jar:../../libraries/commons-codec/commons-codec.jar:../../libraries/restrictionchecker/restrictions.jar:../../libraries/cglib/cglib-nodep.jar:../../libraries/jmock/hamcrest/hamcrest-core.jar:../../libraries/jmock/objenesis/objenesis-1.0.jar:../../libraries/jmock/hamcrest/hamcrest-library.jar:../../libraries/unix:../../libraries/cisd-base/cisd-base.jar:../../libraries/fast-md5/fast-md5.jar:../../libraries/jython/jython.jar:../../libraries/commons-httpclient/commons-httpclient.jar:../../libraries/commons-logging/commons-logging.jar:../../libraries/jaxb/jaxb-api.jar:../../libraries/jaxb/jsr173_1.0_api.jar:../../libraries/jaxb/jaxb-impl.jar:../../libraries/classycle/classycle.jar:../../libraries/commons-collections/commons-collections.jar:../../libraries/spring/spring-core.jar:../../libraries/spring/spring-context.jar:../../libraries/spring/spring-aop.jar:../../libraries/spring/third-party/aopalliance.jar:../../libraries/cisd-args4j/cisd-args4j.jar:../../libraries/jetty8/lib/server/servlet-api-3.0.jar:../../libraries/spring/spring.jar:../../libraries/spring/webmvc/spring-webmvc.jar:../../libraries/gwt2.4/gwt-servlet.jar:../../libraries/spring/third-party/stream-supporting-httpinvoker.jar:../../libraries/cisd-jhdf5/hdf5-linux-intel.jar:../../libraries/cisd-jhdf5/hdf5-macosx-intel.jar:../../libraries/cisd-jhdf5/hdf5-windows-intel.jar:../../libraries/poi/poi-ooxml-schemas.jar:../../libraries/poi/poi-ooxml.jar:../../libraries/poi/poi.jar:../../openbis/targets/www/WEB-INF/classes:../../authentication/targets/classes:../../libraries/jmock/jmock-legacy.jar:../../libraries/jline/jline.jar:../../openbis-common/targets/classes:../../libraries/hibernate-search/jms.jar:../../libraries/eodsql/eodsql.jar:../../libraries/hibernate-search/jsr250-api.jar:../../libraries/spring/spring-beans.jar:../../libraries/spring/spring-web.jar:../../libraries/spring/spring-jdbc.jar:../../libraries/hibernate-core/hibernate-core.jar:../../libraries/jetty8/lib/common/jetty-http.jar:../../libraries/jetty8/lib/common/jetty-io.jar:../../libraries/jetty8/lib/common/jetty-util.jar:../../libraries/jetty8/lib/server/jetty-continuation.jar:../../libraries/jetty8/lib/server/jetty-security.jar:../../libraries/jetty8/lib/server/jetty-server.jar:../../libraries/jetty8/lib/server/jetty-servlet.jar:../../libraries/jmock/hamcrest/hamcrest-integration.jar:../../libraries/dom4j/dom4j.jar:../../libraries/javassist/javassist.jar:../../libraries/reflections/lib/gson-1.4.jar:../../libraries/reflections/lib/guava-r08.jar:../../libraries/reflections/lib/jboss-vfs-3.0.0.CR5.jar:../../libraries/reflections/lib/xml-apis-1.0.b2.jar:../../libraries/reflections/reflections.jar:../../libraries/slf4j/slf4j.jar:../../libraries/jackson/jackson-annotations.jar:../../libraries/jackson/jackson-core.jar:../../libraries/jackson/jackson-databind.jar:../../libraries/jsonrpc4j/jsonrpc4j.jar:../../libraries/cisd-jhdf5/cisd-jhdf5-core.jar:../../libraries/cisd-jhdf5/cisd-jhdf5-tools.jar:../../openbis_api/targets/classes:../../dbmigration/targets/classes:../../libraries/commons-dbcp/commons-dbcp.jar:../../libraries/postgresql/postgresql.jar:../../libraries/h2/h2.jar:../../libraries/commons-pool/commons-pool.jar:../../libraries/apgdiff/apgdiff.jar:../../libraries/hibernate-validator/hibernate-validator.jar:../../libraries/antlr/antlr.jar:../../libraries/slf4j/log4j12/slf4j-log4j12.jar:../../libraries/spring/test/spring-test.jar:../../libraries/junit/junit.jar:../../libraries/hibernate-search/hibernate-search.jar:../../libraries/ehcache/ehcache.jar:../../libraries/lucene/lucene-core.jar:../../libraries/commons-fileupload/commons-fileupload.jar:../../libraries/lucene-highlighter/lucene-highlighter.jar:../../libraries/fastutil/fastutil.jar:../../libraries/gwt-debug-panel/gwt-debug-panel.jar:../../libraries/gwt-image-loader/gwt-image-loader.jar:../../libraries/gwt2.4/gwt-user.jar:../../libraries/gxt2.2.5/gxt.jar:../../libraries/gwt2.4/validation-api-1.0.0.GA.jar:../../libraries/gwt2.4/validation-api-1.0.0.GA-sources.jar:../../libraries/hibernate-commons-annotations/hibernate-commons-annotations.jar:../../libraries/jta/jta.jar:../../libraries/hibernate-jpa-2.0-api/hibernate-jpa-2.0-api.jar:../../libraries/validation-api/validation-api.jar:../../libraries/jetty8/lib/server/jetty-deploy.jar:../../libraries/jetty8/lib/server/jetty-webapp.jar:../../libraries/csv/csv.jar:../../libraries/imagej/ij.jar:../../libraries/jfreechart/jcommon-1.0.16.jar:../../libraries/jfreechart/jfreechart-1.0.13.jar:../../libraries/jai/jai_codec.jar:../../libraries/jai/jai_core.jar:../../libraries/cifex/cifex.jar:../../libraries/truezip/truezip.jar:../../libraries/cisd-image_readers/cisd-image_readers-bioformats.jar:../../libraries/cisd-image_readers/cisd-image_readers-jai.jar:../../libraries/cisd-image_readers/cisd-image_readers.jar:../../libraries/cisd-image_readers/cisd-image_readers-imagej.jar:../../libraries/poi/ooxml-lib/dom4j-1.6.1.jar:../../libraries/poi/ooxml-lib/geronimo-stax-api_1.0_spec-1.0.jar:../../libraries/poi/ooxml-lib/xmlbeans-2.3.0.jar:../../libraries/ftpserver/ftpserver-core.jar:../../libraries/mina/mina-core.jar:../../libraries/pngj/pngj.jar:../../libraries/gwt2.4/gwt-isserializable.jar:../../libraries/sshd/sshd-core.jar:etc/log.xml:../../libraries/cisd-hotdeploy/cisd-hotdeploy.jar ch.systemsx.cisd.openbis.dss.generic.DataStoreServer";

        }

        ProcessHandler p =
                new ProcessHandler(
                        command,
                        "./dss-root");
        p.addListener(new Listener()
            {
                @Override
                public void newLine(String line)
                {
                    System.out.println("External DSS: " + line);
                }
            });

        LogLineReader reader = new LogLineReader();
        p.addListener(reader);

        Thread t = new Thread(p);
        t.setDaemon(true);
        t.start();

        String line;
        while ((line = reader.readLine()) != null)
        {
            if (line.contains("Data Store Server ready and waiting for data"))
            {
                reader.disable();
                break;
            }
        }

        System.out.println("EXTERNAL DSS STARTED");

        return "http://localhost:10001";
    }

    private static class LogLineReader implements Listener
    {
        private LinkedBlockingQueue<String> queue;

        private boolean enabled;

        public LogLineReader()
        {
            this.queue = new LinkedBlockingQueue<String>();
            enabled = true;
        }

        public void disable()
        {
            enabled = false;
        }

        @Override
        public void newLine(String line)
        {
            try
            {
                if (enabled)
                {
                    queue.put(line);
                }
            } catch (InterruptedException ex)
            {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }
        }

        public String readLine()
        {
            try
            {
                return queue.take();
            } catch (InterruptedException ex)
            {
                ex.printStackTrace();
                return null;
            }
        }
    }

    private static interface Listener
    {
        public void newLine(String line);
    }

    private static class ProcessHandler implements Runnable
    {
        private final String[] command;

        private final String directory;

        private final Collection<Listener> listeners;

        public ProcessHandler(String command, String directory)
        {
            StringTokenizer tokenizer = new StringTokenizer(command, " ");
            List<String> strings = new ArrayList<String>();
            while (tokenizer.hasMoreElements())
            {
                strings.add(tokenizer.nextToken());
            }
            this.command = strings.toArray(new String[0]);

            this.directory = directory;
            this.listeners = new ArrayList<Listener>();
        }

        public void addListener(Listener listener)
        {
            this.listeners.add(listener);
        }

        @Override
        public void run()
        {
            try
            {
                Runtime runtime = Runtime.getRuntime();
                final Process process = runtime.exec(command, null, new File(directory));

                runtime.addShutdownHook(new Thread()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                process.getInputStream().close();
                                process.getErrorStream().close();
                                process.getOutputStream().close();
                                process.destroy();
                                process.waitFor();
                                System.out.println("EXTERNAL DSS DESTROYED");
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });

                InputStream in = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null)
                {
                    for (Listener listener : listeners)
                    {
                        listener.newLine(line);
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}
