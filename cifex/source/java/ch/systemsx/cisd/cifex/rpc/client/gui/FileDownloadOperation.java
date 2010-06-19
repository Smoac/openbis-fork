/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.rpc.client.gui;

import java.io.File;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.openpgp.PGPDataValidationException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.cifex.rpc.client.encryption.OpenPGPSymmetricKeyEncryption;
import ch.systemsx.cisd.cifex.rpc.client.gui.FileDownloadClientModel.FileDownloadInfo;

/**
 * FileDownloadOperation represents a request to download a file from the CIFEX server. The download
 * operation runs in its own thread, which is managed by this class. The client model is notified
 * when the download completes.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
final class FileDownloadOperation implements Runnable
{
    private final FileDownloadClientModel tableModel;

    private final FileDownloadInfo fileDownloadInfo;

    private final File downloadDirectory;

    private String passphrase;

    FileDownloadOperation(FileDownloadClientModel model, FileDownloadInfo info,
            File downloadDirectory, String passphrase)
    {
        this.tableModel = model;
        this.fileDownloadInfo = info;
        this.downloadDirectory = downloadDirectory;
        this.passphrase = passphrase;
    }

    public void run()
    {
        String operationName = "Downloading";
        try
        {
            final File file =
                    tableModel.getDownloader().download(fileDownloadInfo.getFileInfoDTO().getID(),
                            downloadDirectory, null);

            if (passphrase.length() > 0)
            {
                operationName = "Decrypting";
                decrypt(file);
            }
        } catch (Throwable th)
        {
            notifyUserOfException(operationName, th);
        }
    }

    private void decrypt(final File file)
    {
        boolean ok = false;
        while (ok == false)
        {
            try
            {
                final File clearTextFile =
                        OpenPGPSymmetricKeyEncryption.decrypt(file, null, passphrase);
                JOptionPane.showMessageDialog(tableModel.getMainWindow(), "File on Server: "
                        + fileDownloadInfo.getFileInfoDTO().getName() + "\n" + "Decrypted file: "
                        + clearTextFile.getPath(), "File Decryption",
                        JOptionPane.INFORMATION_MESSAGE);
                ok = true;
            } catch (CheckedExceptionTunnel ex)
            {
                if (ex.getCause() instanceof PGPDataValidationException == false)
                {
                    throw ex;
                }
                passphrase =
                        PassphraseDialog.tryGetPassphraseForDecryptRetry(
                                tableModel.getMainWindow(), "File: '"
                                        + fileDownloadInfo.getFileInfoDTO().getName() + "'",
                                "<div color='red'>Wrong passphrase, " + "please try again.</div>");
                if (StringUtils.isEmpty(passphrase))
                {
                    JOptionPane.showMessageDialog(tableModel.getMainWindow(),
                            "Decryption cancelled.");
                    ok = true; // Cancel
                }
            }
        }
    }

    private void notifyUserOfException(String operationName, Throwable th)
    {
        final Throwable th2 =
                (th instanceof Error) ? th : CheckedExceptionTunnel
                        .unwrapIfNecessary((Exception) th);
        final String msg;
        if (StringUtils.isBlank(th2.getMessage()))
        {
            msg = th2.getClass().getSimpleName();
        } else
        {
            msg = th2.getClass().getSimpleName() + ": " + th2.getMessage();
        }
        th2.printStackTrace();
        JOptionPane.showMessageDialog(tableModel.getMainWindow(), operationName + " file '"
                + fileDownloadInfo.getFileInfoDTO().getName() + "' failed:\n" + msg, "Error "
                + operationName + " File", JOptionPane.ERROR_MESSAGE);
    }

}
