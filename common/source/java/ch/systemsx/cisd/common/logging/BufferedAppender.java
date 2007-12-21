/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.logging;

import java.io.ByteArrayOutputStream;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

/**
 * A <code>WriterAppender</code> extension that buffers its output in a <code>ByteArrayOutputStream</code> until you
 * ask for it by using {@link #getLogContent()}.
 * <p>
 * It internally uses a <code>ByteArrayOutputStream</code> which collect the log output and can return it using
 * {@link #getLogContent()}. It is a good idea to reset the log recorder by calling {@link #resetLogContent()} before
 * calling a unit test method.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class BufferedAppender extends WriterAppender
{
    private final ByteArrayOutputStream logRecorder;

    /**
     * Constructor with default pattern layout (which is {@link PatternLayout#DEFAULT_CONVERSION_PATTERN}) and
     * {@link Level#DEBUG} as log level.
     */
    public BufferedAppender()
    {
        this(Level.DEBUG);
    }

    /**
     * Constructor with default pattern layout (which is {@link PatternLayout#DEFAULT_CONVERSION_PATTERN}).
     * 
     * @param logLevel
     */
    public BufferedAppender(final Level logLevel)
    {
        this(null, logLevel);
    }

    public BufferedAppender(final String pattern, final Level logLevel)
    {
        super();
        logRecorder = new ByteArrayOutputStream();
        setWriter(createWriter(logRecorder));
        setLayout(createLayout(pattern));
        configureRootLogger();
        setThreshold(logLevel);
    }

    private final void configureRootLogger()
    {
        Logger.getRootLogger().addAppender(this);
    }

    protected Layout createLayout(final String pattern)
    {
        return new PatternLayout(pattern);
    }

    /**
     * Returns the content of this log appender.
     * <p>
     * Never returns <code>null</code> but could return an empty string.
     * </p>
     */
    public final String getLogContent()
    {
        return new String(logRecorder.toByteArray()).trim();
    }

    public final void resetLogContent()
    {
        logRecorder.reset();
    }

    //
    // WriterAppender
    //

    @Override
    public final void reset()
    {
        Logger.getRootLogger().removeAppender(this);
        super.reset();
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return getEncoding();
    }
}