/*
 * Copyright 2002-2005 the original author or authors.
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

package com.marathon.util.spring;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.springframework.remoting.httpinvoker.HttpComponentsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.rmi.CodebaseAwareObjectInputStream;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * <p>
 * An HttpInvokerRequestExecutor that supports InputStream return types from remote methods, as well
 * as (at most one) InputStream parameter. If no InputStream return type or parameter is present for
 * a particular remote invocation then this class will delegate the invocation to
 * CommonsHttpInvokerRequestExecutor, otherwise it will enable &quot;chunking&quot; on the
 * PostMethod being used and append the InputStream to the end of the post body (after the
 * serialized RemoteInvocation), in the case of an InputStream parameter. For InputStream return
 * types, this implementation relies on a corresponding StreamSupportingHttpInvokerServiceExporter
 * (on the server side) to append the returned InputStream content on the response to the post
 * method, immediately following the serialized RemoteInvocationResult.
 * </p>
 * <p>
 * One of the major reasons for supporting InputStreams via the invoker is to transport large
 * amounts of data across the wire (often more data than what is reasonable to fit into memory),
 * therefore care is taken to ensure that the InputStream is not fully buffered or read into memory,
 * but rather streamed as it is read across the wire.
 * </p>
 * <p>
 * See <code>StreamSupportingHttpInvokerProxyFactoryBean</code> for more detail.
 * </p>
 * 
 * @author Andy DePue
 * @since 1.2.3
 * @see StreamSupportingHttpInvokerProxyFactoryBean
 * @see org.springframework.remoting.httpinvoker.HttpInvokerRequestExecutor
 * @see org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor
 * @see org.springframework.remoting.support.RemoteInvocation
 * @see org.springframework.remoting.support.RemoteInvocationResult
 * @see org.apache.commons.httpclient.methods.PostMethod
 * @see com.marathon.util.spring.StreamSupportingHttpInvokerServiceExporter
 * @see java.io.InputStream
 */
public class StreamSupportingHttpInvokerRequestExecutor extends HttpComponentsHttpInvokerRequestExecutor
{
    private static final Log log =
            LogFactory.getLog(StreamSupportingHttpInvokerRequestExecutor.class);

    public static final String CONTENT_TYPE_SERIALIZED_OBJECT_WITH_STREAM =
            "application/x-java-serialized-object-with-stream";

    //
    // METHODS FROM CLASS CommonsHttpInvokerRequestExecutor
    //

    // It sure would have been nice to override executeRequest(...),
    // BUT, AbstractHttpInvokerRequestExecutor implements
    // executeRequest(...) as final, and since the final
    // executeRequest(...) makes some poor assumptions (such as the assumption
    // that doExecuteRequest has no need of the original RemoteInvocation), we
    // are forced to either employ some hack (such as creating a custom
    // ByteArrayOutputStream) OR duplicating a ton of code. I decided to employ
    // the hack.

    @Override
    protected ByteArrayOutputStream getByteArrayOutputStream(final RemoteInvocation invocation)
            throws IOException
    {
        // The constant is private in AbstractHttpInvokerRequestExecutor.
        final WorkaroundByteArrayOutputStream baos =
                new WorkaroundByteArrayOutputStream(1024, invocation);
        writeRemoteInvocation(invocation, baos);
        return baos;
    }

    @Override
    protected RemoteInvocationResult doExecuteRequest(final HttpInvokerClientConfiguration config,
            final ByteArrayOutputStream baos) throws IOException, ClassNotFoundException
    {
        final WorkaroundByteArrayOutputStream wbaos = (WorkaroundByteArrayOutputStream) baos;
        if (wbaos.getRemoteInvocation() instanceof StreamSupportingRemoteInvocation)
        {
            return doExecuteRequest(config, wbaos, (StreamSupportingRemoteInvocation) wbaos
                    .getRemoteInvocation());
        } else
        {
            return super.doExecuteRequest(config, baos);
        }
    }

    @Override
    protected RemoteInvocationResult readRemoteInvocationResult(final InputStream is,
            final String codebaseUrl) throws IOException, ClassNotFoundException
    {
        final RemoteInvocationResult ret = super.readRemoteInvocationResult(is, codebaseUrl);
        if (!(ret instanceof StreamSupportingRemoteInvocationResult))
        {
            is.close();
        }
        return ret;
    }

    @Override
    protected ObjectInputStream createObjectInputStream(final InputStream is,
            final String codebaseUrl) throws IOException
    {
        return new SourceStreamPreservingObjectInputStream(is, codebaseUrl);
    }

    @Override
    protected RemoteInvocationResult doReadRemoteInvocationResult(final ObjectInputStream ois)
            throws IOException, ClassNotFoundException
    {
        final RemoteInvocationResult source = super.doReadRemoteInvocationResult(ois);
        if (source instanceof StreamSupportingRemoteInvocationResult)
        {
            ((StreamSupportingRemoteInvocationResult) source)
                    .setClientSideInputStream(((SourceStreamPreservingObjectInputStream) ois)
                            .getSourceInputStream());
        }

        return source;
    }

    //
    // HELPER METHODS
    //

    /**
     * Execute a request to send the given serialized remote invocation.
     * <p>
     * Implementations will usually call <code>readRemoteInvocationResult</code> to deserialize a
     * returned RemoteInvocationResult object.
     * 
     * @param config the HTTP invoker configuration that specifies the target service
     * @param baos the ByteArrayOutputStream that contains the serialized RemoteInvocation object
     * @return the RemoteInvocationResult object
     * @throws IOException if thrown by I/O operations
     * @throws ClassNotFoundException if thrown during deserialization
     * @see #readRemoteInvocationResult(java.io.InputStream, String)
     */
    protected RemoteInvocationResult doExecuteRequest(final HttpInvokerClientConfiguration config,
            final ByteArrayOutputStream baos, final StreamSupportingRemoteInvocation invocation)
            throws IOException, ClassNotFoundException
    {
        final ByteArrayInputStream serializedInvocation =
                new ByteArrayInputStream(baos.toByteArray());

        final HttpPost postMethod;
        final InputStream body;

        EntityBuilder e = EntityBuilder.create();
        if (invocation.getClientSideInputStream() != null)
        {
            // We don't want to close the client side input stream unless the remote
            // method closes the input stream, so we "shield" the close for now.
            body =
                    new CompositeInputStream(
                            new InputStream[]
                                {
                                        serializedInvocation,
                                        new CloseShieldedInputStream(invocation
                                                .getClientSideInputStream()) });
            postMethod = createPostMethodForStreaming(config);
            e.chunked();
            e.setContentType(ContentType.create(CONTENT_TYPE_SERIALIZED_OBJECT_WITH_STREAM));
        } else
        {
            body = serializedInvocation;
            postMethod = createHttpPost(config);
        }

        boolean delayReleaseConnection = false;

        try
        {
            // Instead of 
            //   HttpEntity requestEntity = e.setStream(body).build();
            // we have to do the following because there's a bug in httpclient 4.3.6.
            // The length of the stream is set to 1 instead of -1 (which means 'unknown').
            AbstractHttpEntity re = new InputStreamEntity(body, -1, e.getContentType());
            HttpEntity requestEntity = re;
            
            if (re.getContentType() != null && e.getContentType() != null) {
                re.setContentType(e.getContentType().toString());
            }
            re.setContentEncoding(e.getContentEncoding());
            re.setChunked(e.isChunked());
            if (e.isGzipCompress()) {
                requestEntity =  new GzipCompressingEntity(re);
            }
            
            postMethod.setEntity(requestEntity);
            HttpResponse response = executeHttpPost(config, getHttpClient(), postMethod);
            final RemoteInvocationResult ret =
                    readRemoteInvocationResult(response.getEntity().getContent(), config
                            .getCodebaseUrl());
            if (ret instanceof StreamSupportingRemoteInvocationResult)
            {
                final StreamSupportingRemoteInvocationResult ssrir =
                        (StreamSupportingRemoteInvocationResult) ret;

                // Close the local InputStream parameter if the remote method
                // explicitly closed the InputStream parameter on the other side.
                if (invocation.getClientSideInputStream() != null)
                {
                    if (ssrir.getMethodClosedParamInputStream() != null)
                    {
                        if (Boolean.TRUE.equals(ssrir.getMethodClosedParamInputStream()))
                        {
                            invocation.getClientSideInputStream().close();
                        }
                    } else
                    {
                        warnInputStreamParameterStateNotSpecified(invocation);
                    }
                }

                // If there is a return stream, then we need to leave the PostMethod
                // connection open until the return stream is closed, so augment the
                // return stream for this.
                if (ssrir.getHasReturnStream())
                {
                    final InputStream sourceRetIs = ssrir.getClientSideInputStream();
                    if (sourceRetIs != null)
                    {
                        ssrir.setClientSideInputStream(new FilterInputStream(sourceRetIs)
                            {
                                @Override
                                public void close() throws IOException
                                {
                                    super.close();
                                    postMethod.releaseConnection();
                                }
                            });
                        delayReleaseConnection = true;
                    }
                }
            } else if (invocation.getClientSideInputStream() != null)
            {
                warnInputStreamParameterStateNotSpecified(invocation);
            }
            return ret;
        } finally
        {
            // need to explicitly release because it might be pooled
            if (!delayReleaseConnection)
            {
                postMethod.releaseConnection();
            }
        }
    }

    private void warnInputStreamParameterStateNotSpecified(
            final StreamSupportingRemoteInvocation invocation)
    {
        log
                .warn("Remote method invocation with InputStream parameter did not indicate if remote method closed the InputStream parameter!  Will leave the stream open.  RemoteInvocation: "
                        + invocation);
    }

    protected HttpPost createPostMethodForStreaming(final HttpInvokerClientConfiguration config)
            throws IOException
    {
        final HttpPost postMethod = new HttpPost(config.getServiceUrl());
        postMethod.setHeader(HTTP_HEADER_CONTENT_TYPE,
                CONTENT_TYPE_SERIALIZED_OBJECT_WITH_STREAM);
        return postMethod;
    }

    //
    // INNER CLASSES
    //

    /**
     * Works around the "final" executeRequest(...) method and sneaks in the RemoteInvocation
     * reference.
     */
    public static class WorkaroundByteArrayOutputStream extends ByteArrayOutputStream
    {
        private final RemoteInvocation remoteInvocation;

        public WorkaroundByteArrayOutputStream(final int size,
                final RemoteInvocation remoteInvocation)
        {
            super(size);
            this.remoteInvocation = remoteInvocation;
        }

        public RemoteInvocation getRemoteInvocation()
        {
            return this.remoteInvocation;
        }
    }

    /**
     * Prevents the source InputStream from being closed, but allows the ObjectInputStream to go
     * through the motions of closing the stream so that its internal state is properly cleared.
     */
    public static class SourceStreamPreservingObjectInputStream extends
            CodebaseAwareObjectInputStream
    {
        private InputStream sourceInputStream;

        public SourceStreamPreservingObjectInputStream(final InputStream in,
                final String codebaseUrl) throws IOException
        {
            // Prevent the source InputStream from being closed when the
            // ObjectInputStream is closed. We do it this way rather than
            // overriding close() to ensure that ObjectInputStream has a chance to
            // clear out itself when close() is called.
            super(new CloseShieldedInputStream(in), codebaseUrl);
            this.sourceInputStream = in;
        }

        public InputStream getSourceInputStream()
        {
            return this.sourceInputStream;
        }
    }

    /**
     * Shields an underlying InputStream from being closed.
     */
    public static class CloseShieldedInputStream extends FilterInputStream
    {
        public CloseShieldedInputStream(final InputStream in)
        {
            super(in);
        }

        @Override
        public void close() throws IOException
        {
        }
    }

    /**
     * Allows multiple InputStreams to be composited into a single InputStream.
     */
    public static class CompositeInputStream extends FilterInputStream
    {
        private InputStream[] inputStreams;

        private int currentInputStreamIdx;

        public CompositeInputStream(final InputStream[] inputStreams)
        {
            super(inputStreams[0]);
            this.inputStreams = inputStreams;
            this.currentInputStreamIdx = 0;
        }

        public InputStream[] getInputStreams()
        {
            return this.inputStreams;
        }

        public int getCurrentInputStreamIdx()
        {
            return this.currentInputStreamIdx;
        }

        protected InputStream incCurrentInputStream() throws IOException
        {
            if ((++this.currentInputStreamIdx) >= getInputStreams().length)
            {
                return null;
            } else
            {
                this.in.close();
                return this.in = getInputStreams()[this.currentInputStreamIdx];
            }
        }

        @Override
        public int read() throws IOException
        {
            final int read = super.read();
            if (read == -1)
            {
                if (incCurrentInputStream() == null)
                {
                    return -1;
                } else
                {
                    return read();
                }
            } else
            {
                return read;
            }
        }

        @Override
        public int read(byte b[]) throws IOException
        {
            final int read = super.read(b);
            if (read == -1)
            {
                if (incCurrentInputStream() == null)
                {
                    return -1;
                } else
                {
                    return read(b);
                }
            } else
            {
                return read;
            }
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException
        {
            final int read = super.read(b, off, len);
            if (read == -1)
            {
                if (incCurrentInputStream() == null)
                {
                    return -1;
                } else
                {
                    return read(b, off, len);
                }
            } else
            {
                return read;
            }
        }

        @Override
        public void close() throws IOException
        {
            // All InputStreams preceeding the current one have already been closed.
            // Be sure to close all InputStreams following the current one as well.
            final InputStream[] myInputStreams = getInputStreams();
            for (int i = getCurrentInputStreamIdx(); i < myInputStreams.length; i++)
            {
                myInputStreams[i].close();
            }
        }
    }
}
