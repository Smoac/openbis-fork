package ch.ethz.sis.afsserver.server.common;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.log4j2.Log4J2LogFactory;

public class TestLogger
{

    private static LoggerContext loggerContext;

    private static Appender recordingAppender;

    private static ByteArrayOutputStream recordedLog;

    private static class TestOutputStreamManager extends OutputStreamManager
    {

        public TestOutputStreamManager(final OutputStream os, final String streamName, final Layout<?> layout, final boolean writeHeader)
        {
            super(os, streamName, layout, writeHeader);
        }
    }

    private static class TestAppender extends AbstractOutputStreamAppender<OutputStreamManager>
    {

        protected TestAppender(final String name, final Layout layout, final Filter filter, final boolean ignoreExceptions,
                final boolean immediateFlush, final OutputStreamManager manager)
        {
            super(name, layout, filter, ignoreExceptions, immediateFlush, manager);
        }

        private static TestAppender createAppender(ByteArrayOutputStream output, Level logLevel, Layout logLayout)
        {
            OutputStreamManager manager = OutputStreamManager.getManager("test-appender-stream-manager", (Object) null,
                    (name, data) -> new TestOutputStreamManager(output, name, logLayout, true));

            return new TestAppender("test-appender", logLayout,
                    ThresholdFilter.createFilter(logLevel, Filter.Result.ACCEPT, Filter.Result.DENY), false, true, manager);
        }
    }

    public static void configure()
    {
        loggerContext = Configurator.initialize(null, "log.xml");
        LogManager.setLogFactory(new Log4J2LogFactory());
    }

    public static void startLogRecording(Level level, String pattern, String loggerNameRegex)
    {
        if (loggerContext == null)
        {
            throw new RuntimeException("Test logger hasn't been configured yet.");
        }

        if (recordingAppender != null)
        {
            throw new RuntimeException("Test log recording has been already started.");
        }

        recordedLog = new ByteArrayOutputStream();
        recordingAppender = TestAppender.createAppender(recordedLog, level, PatternLayout.newBuilder().withPattern(pattern).build());
        recordingAppender.start();
        loggerContext.getRootLogger().addAppender(recordingAppender);
    }

    public static void stopLogRecording()
    {
        if (loggerContext == null)
        {
            throw new RuntimeException("Test logger hasn't been configured yet.");
        }

        if (recordingAppender == null)
        {
            throw new RuntimeException("Test log recording hasn't been started yet.");
        }

        recordingAppender.stop();
        loggerContext.getRootLogger().removeAppender(recordingAppender);
        recordedLog = null;
    }

    public static void resetRecordedLog()
    {
        recordedLog.reset();
    }

    public static String getRecordedLog()
    {
        return recordedLog.toString();
    }

}
