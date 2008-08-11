/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.filesystem;

import java.io.IOException;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.WrappedIOException;

/**
 * An adapter for {@link java.io.InputStream} that implements {@link IInputStream}.
 *
 * @author Bernd Rinn
 */
public class InputStreamAdapter implements IInputStream
{
    
    private final java.io.InputStream delegate;
    
    public InputStreamAdapter(java.io.InputStream delegate)
    {
        this.delegate = delegate;
    }

    public int available() throws WrappedIOException
    {
        try
        {
            return delegate.available();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void close() throws WrappedIOException
    {
        try
        {
            delegate.close();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void mark(int readlimit)
    {
        delegate.mark(readlimit);
    }

    public boolean markSupported()
    {
        return delegate.markSupported();
    }

    public int read() throws WrappedIOException
    {
        try
        {
            return delegate.read();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public int read(byte[] b, int off, int len) throws WrappedIOException
    {
        try
        {
            return delegate.read(b, off, len);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public int read(byte[] b) throws WrappedIOException
    {
        try
        {
            return delegate.read(b);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void reset() throws WrappedIOException
    {
        try
        {
            delegate.reset();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public long skip(long n) throws WrappedIOException
    {
        try
        {
            return delegate.skip(n);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    //
    // Object
    //
    
    @Override
    public String toString()
    {
        return delegate.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        return delegate.equals(obj);
    }

    @Override
    public int hashCode()
    {
        return delegate.hashCode();
    }

}
