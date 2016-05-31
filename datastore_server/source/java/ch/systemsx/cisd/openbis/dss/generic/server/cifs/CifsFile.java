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

package ch.systemsx.cisd.openbis.dss.generic.server.cifs;

import java.io.IOException;

import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.smb.SeekType;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.FtpFileImpl;

/**
 * @author Franz-Josef Elmer
 */
final class CifsFile extends NetworkFile
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, CifsFile.class);

    private final FtpFile file;

    private IRandomAccessFile randomAccessFile;

    CifsFile(FtpFile file)
    {
        super(file.getName());
        this.file = file;
    }

    @Override
    public void writeFile(byte[] buf, int len, int pos, long fileOff) throws IOException
    {
        throw new IOException("Can not write into the read-only virtual file '" + file.getAbsolutePath() + "'.");
    }

    @Override
    public void truncateFile(long siz) throws IOException
    {
        throw new IOException("Can not truncate the read-only virtual file '" + file.getAbsolutePath() + "'.");
    }

    @Override
    public long seekFile(long pos, int type) throws IOException
    {
        operationLog.debug("Seek in virtual file '" + file.getAbsolutePath() + "' to position " + pos
                + " (seek type: " + type + ").");
        IRandomAccessFile raFile = getRandomAccessFile();
        long currentPosition = raFile.getFilePointer();
        switch (type)
        {
            case SeekType.StartOfFile:
                if (currentPosition != pos)
                {
                    raFile.seek(pos);
                }
                break;
            case SeekType.CurrentPos:
                raFile.seek(currentPosition + pos);
                break;
            case SeekType.EndOfFile:
            {
                long newPos = raFile.length() + pos;
                raFile.seek(newPos);
            }
                break;
        }
        return raFile.getFilePointer();
    }

    @Override
    public int readFile(byte[] buf, int len, int pos, long fileOff) throws IOException
    {
        operationLog.debug("Read from virtual file '" + file.getAbsolutePath() + "' at position " + fileOff + " "
                + len + " bytes into the buffer of size " + buf.length + " at position " + pos + ".");
        IRandomAccessFile raFile = getRandomAccessFile();
        raFile.seek(fileOff);
        return raFile.read(buf, pos, len);
    }

    @Override
    public void openFile(boolean createFlag) throws IOException
    {
        if (createFlag)
        {
            throw new IOException("Virtual file '" + file.getAbsolutePath() + "' can not be created.");
        }
        operationLog.info("Open virtual file '" + file.getAbsolutePath() + "'.");
        synchronized (file)
        {
            if (randomAccessFile == null && file instanceof FtpFileImpl)
            {
                randomAccessFile = ((FtpFileImpl) file).getFileContent();
            }
        }
    }

    @Override
    public void flushFile() throws IOException
    {
        operationLog.debug("Flush virtual file '" + file.getAbsolutePath() + "'.");
        getRandomAccessFile().synchronize();
    }

    @Override
    public void closeFile() throws IOException
    {
        operationLog.debug("Close virtual file '" + file.getAbsolutePath() + "'.");
        synchronized (file)
        {
            if (randomAccessFile != null)
            {
                randomAccessFile.close();
            }
        }
    }

    private IRandomAccessFile getRandomAccessFile() throws IOException
    {
        synchronized (file)
        {
            if (randomAccessFile == null)
            {
                openFile(false);
            }
            if (randomAccessFile != null)
            {
                return randomAccessFile;
            }
            throw new IOException("Can not open file '" + file.getAbsolutePath() + "'.");
        }
    }
}