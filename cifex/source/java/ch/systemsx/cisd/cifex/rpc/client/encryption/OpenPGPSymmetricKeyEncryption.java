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

package ch.systemsx.cisd.cifex.rpc.client.encryption;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPBEEncryptedData;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * A class that provides routines to encrypt and decrypt a file using a symmetric key and
 * phase-phrase based encryption to and from an OpenPGP container using the AES-256 algorithm.
 * <p>
 * The file produced by {@link #encrypt(File, File, String)} is equivalent to calling "
 * <code>gpg -c --cipher-alg=aes256 &lt;file&gt;</code>" and can be decrypted by any OpenPGP
 * compliant program.
 * <p>
 * <i>This class is inspired by the <code>PBEFileProcessor</code> utility from the Legion of the
 * Bouncy Castle.</i>
 * 
 * @author Bernd Rinn
 */
public class OpenPGPSymmetricKeyEncryption
{
    public static final String PGP_FILE_EXTENSION = ".pgp";

    public static final String CLEAR_PREFIX = "CLEAR_";

    static
    {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Encrypts the <var>inFile</var> to an OpenPGP <var>outFile</var>, using an AES-256 and a
     * passphase-based encryption schema with <var>passPhrase</var>.
     * 
     * @return The actual outfile file written which may differ from <var>outFile</var> if
     *         <var>inFile</var> and <var>outFile</var> would have been the same otherwise (in which
     *         case {{@link #PGP_FILE_EXTENSION} will be used as a suffix).
     */
    public static File encrypt(File inFile, File outFile, String passPhrase)
    {
        final List<Closeable> closeables = new ArrayList<Closeable>();
        try
        {
            FileInputStream inStream = null;
            File actualOutFile = outFile.getCanonicalFile();
            if (inFile.getCanonicalFile().equals(actualOutFile))
            {
                actualOutFile =
                        new File(actualOutFile.getParent(), actualOutFile.getName()
                                + PGP_FILE_EXTENSION);
            }
            final OutputStream outStream =
                    getPGPOutStream(inFile, actualOutFile, passPhrase, closeables);
            try
            {
                inStream = new FileInputStream(inFile);
                IOUtils.copyLarge(inStream, outStream);
            } finally
            {
                IOUtils.closeQuietly(inStream);
                closeInReverseOrder(closeables, false);
                outStream.close();
            }
            return actualOutFile;
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    private static OutputStream getPGPOutStream(File inFile, File outFile, String passPhrase,
            List<Closeable> closeables) throws NoSuchProviderException, PGPException,
            FileNotFoundException
    {
        final PGPEncryptedDataGenerator encryptedDataGenerator =
                new PGPEncryptedDataGenerator(PGPEncryptedData.AES_128, true, new SecureRandom(),
                        "BC");
        encryptedDataGenerator.addMethod(passPhrase.toCharArray());

        final FileOutputStream outStream = new FileOutputStream(outFile);
        closeables.add(outStream);
        try
        {
            final OutputStream encryptingOutStream =
                    encryptedDataGenerator.open(outStream, inFile.length()
                            + getPGPLiteralDataHeaderLength(PGPLiteralData.BINARY, inFile));
            closeables.add(encryptingOutStream);
            final PGPLiteralDataGenerator literalData = new PGPLiteralDataGenerator();
            final OutputStream pgpOutStream =
                    literalData.open(encryptingOutStream, PGPLiteralData.BINARY, inFile.getName(),
                            inFile.length(), new Date(inFile.lastModified()));
            closeables.add(pgpOutStream);
            return pgpOutStream;
        } catch (Exception ex)
        {
            closeInReverseOrder(closeables, true);
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private static void closeInReverseOrder(List<Closeable> closeables, boolean quietOnException)
    {
        ListIterator<Closeable> it = closeables.listIterator(closeables.size());
        while (it.hasPrevious())
        {
            try
            {
                final Closeable closeable = it.previous();
                closeable.close();
            } catch (IOException ex)
            {
                if (quietOnException == false)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }
    }

    private static int getPGPLiteralDataHeaderLength(char fileType, File file) throws IOException
    {
        final CountingOutputStream outStream = new CountingOutputStream(new NullOutputStream());
        final PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
        // Opening the stream already writes the header to out.
        try
        {
            literalDataGenerator.open(outStream, fileType, file.getName(), file.length(), new Date(
                    file.lastModified()));
        } finally
        {
            literalDataGenerator.close();
        }

        return outStream.getCount();
    }

    /**
     * Decrypt the <var>inFile</var> which needs to be PGP file with symmetric key encryption, using
     * the provided <var>passPhrase</var>. If <var>outFileNameOrNull</var> is not <code>null</code>,
     * this name will be used instead of the name contained in the PGP container.
     * 
     * @return The actual outfile file written which may contain a prefix {@link #CLEAR_PREFIX} if
     *         it would have overwritten the <var>inFile</var> otherwise.
     */
    public static File decrypt(File inFile, String outFileNameOrNull, String passPhrase)
    {
        try
        {
            final PGPPBEEncryptedData encryptedData = getPGPEncryptedData(inFile);
            final PGPLiteralData literalData = getLiteralData(encryptedData, passPhrase);
            final String directory = inFile.getParent();
            final String outFileName =
                    (outFileNameOrNull == null) ? literalData.getFileName() : outFileNameOrNull;
            File outFile = new File(directory, outFileName).getCanonicalFile();
            if (inFile.getCanonicalFile().equals(outFile))
            {
                outFile = new File(outFile.getParentFile(), CLEAR_PREFIX + outFileName);
            }
            final FileOutputStream outStream = new FileOutputStream(outFile);

            try
            {
                final InputStream literalDataStream = literalData.getInputStream();
                try
                {
                    IOUtils.copyLarge(literalDataStream, outStream);
                    checkIntegrity(encryptedData, inFile);
                } finally
                {
                    IOUtils.closeQuietly(literalDataStream);
                }
            } finally
            {
                outStream.close();
            }
            return outFile;
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    private static void checkIntegrity(final PGPPBEEncryptedData encryptedData, File inFile)
    {
        try
        {
            if (encryptedData.isIntegrityProtected() && encryptedData.verify() == false)
            {
                throw new FailedPGPIntegrityCheck(inFile);
            }
        } catch (PGPException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private static PGPLiteralData getLiteralData(final PGPPBEEncryptedData encryptedData,
            String passPhrase) throws PGPException, NoSuchProviderException, IOException
    {
        final InputStream decryptedDataStream =
                encryptedData.getDataStream(passPhrase.toCharArray(), "BC");

        PGPObjectFactory pgpFactory = new PGPObjectFactory(decryptedDataStream);

        Object openPGPObject = pgpFactory.nextObject();
        // The data stream may be compressed, handle this here.
        if (openPGPObject instanceof PGPCompressedData)
        {
            PGPCompressedData compressedData = (PGPCompressedData) openPGPObject;

            pgpFactory = new PGPObjectFactory(compressedData.getDataStream());

            openPGPObject = pgpFactory.nextObject();
        }

        return (PGPLiteralData) openPGPObject;
    }

    private static PGPPBEEncryptedData getPGPEncryptedData(File inFile)
            throws FileNotFoundException, IOException, PGPException
    {
        try
        {
            final InputStream pgpIn;
            switch (getPGPMessageType(inFile))
            {
                case BINARY:
                    pgpIn = new FileInputStream(inFile);
                    break;
                case ARMORED:
                    pgpIn = new ArmoredInputStream(new FileInputStream(inFile));
                    break;
                default:
                    throw new PGPException("No PGP file.");
            }
            final PGPObjectFactory pgpFactory = new PGPObjectFactory(pgpIn);
            final PGPEncryptedDataList encryptedDataList;

            final Object openPGPObject = pgpFactory.nextObject();
            // The first object may be a PGP marker packet which is save to skip.
            if (openPGPObject instanceof PGPEncryptedDataList)
            {
                encryptedDataList = (PGPEncryptedDataList) openPGPObject;
            } else
            {
                encryptedDataList = (PGPEncryptedDataList) pgpFactory.nextObject();
            }

            return (PGPPBEEncryptedData) encryptedDataList.get(0);
        } catch (Exception ex)
        {
            throw new PGPException("Cannot decrypt file", ex);
        }
    }

    private static final String ARMORED_PGP_HEADER = "-----BEGIN PGP MESSAGE-----";

    private enum PGP_MESSAGE_TYPE
    {
        BINARY, ARMORED, NONE;
    }

    private static PGP_MESSAGE_TYPE getPGPMessageType(File inFile) throws IOException
    {
        final InputStream in = new FileInputStream(inFile);
        try
        {
            final byte[] buf = new byte[ARMORED_PGP_HEADER.length()];
            in.read(buf, 0, buf.length);
            if ((buf[0] & 0x80) != 0)
            {
                return PGP_MESSAGE_TYPE.BINARY;
            }
            return new String(buf).equals(ARMORED_PGP_HEADER) ? PGP_MESSAGE_TYPE.ARMORED
                    : PGP_MESSAGE_TYPE.NONE;
        } finally
        {
            IOUtils.closeQuietly(in);
        }
    }
}
