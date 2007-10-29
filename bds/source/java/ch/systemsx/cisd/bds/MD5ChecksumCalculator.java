package ch.systemsx.cisd.bds;

import java.io.IOException;
import java.io.InputStream;

import com.twmacinta.util.MD5;
import com.twmacinta.util.MD5InputStream;

/**
 * A {@link IChecksumCalculator} implementation based on <i>MD5</i>.
 * 
 * @author Christian Ribeaud
 */
final class MD5ChecksumCalculator implements IChecksumCalculator
{

    //
    // IChecksum
    //

    public String calculateChecksum(InputStream inputStream) throws IOException
    {
        byte[] buf = new byte[4096];
        MD5InputStream in = new MD5InputStream(inputStream);
        while (in.read(buf) != -1)
        {
        }
        return MD5.asHex(in.hash());
    }
}