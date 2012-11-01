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
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.openpgp.PGPDataValidationException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.cifex.rpc.client.encryption.OpenPGPSymmetricKeyEncryption;
import ch.systemsx.cisd.cifex.rpc.client.gui.FileDownloadClientModel.FileDownloadInfo;
import ch.systemsx.cisd.cifex.rpc.client.gui.FileDownloadClientModel.FileDownloadInfo.Status;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.common.exceptions.FileExistsException;
import ch.systemsx.cisd.common.filesystem.IFileOverwriteStrategy;

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
    
    private boolean decryptionCancelled = false;

    private boolean deleteEncryptedFileAfterSuccessfulDecryption;

    FileDownloadOperation(final FileDownloadClientModel model, final FileDownloadInfo info,
            final File downloadDirectory, final String passphrase,
            final boolean deleteEncryptedFileAfterSuccessfulDecryption)
    {
        this.tableModel = model;
        this.fileDownloadInfo = info;
        this.downloadDirectory = downloadDirectory;
        this.passphrase = passphrase;
        this.deleteEncryptedFileAfterSuccessfulDecryption =
                deleteEncryptedFileAfterSuccessfulDecryption;
    }

    @Override
    public void run()
    {
        try
        {
            final File file;
            if (fileDownloadInfo.getStatus() == FileDownloadInfo.Status.QUEUED_FOR_DOWNLOAD)
            {
                tableModel.fireChanged(fileDownloadInfo.getFileInfoDTO().getID(),
                        Status.DOWNLOADING);
                file =
                        tableModel.getDownloader().download(
                                fileDownloadInfo.getFileInfoDTO().getID(),
                                downloadDirectory,
                                null,
                                createFileResumeOrOverwriteStrategyAskUser(fileDownloadInfo
                                        .getFileInfoDTO()));
                fileDownloadInfo.setFile(file);
            } else
            {
                file = fileDownloadInfo.getFile();
            }
            if (passphrase.length() > 0)
            {
                try
                {
                    decrypt(file);
                    removeEncryptedIfRequested(file);
                } catch (Throwable th)
                {
                    tableModel.fireChanged(Status.COMPLETED_DOWNLOAD);
                    AbstractSwingGUI.notifyUserOfThrowable(tableModel.getMainWindow(),
                            fileDownloadInfo.getFileInfoDTO().getName(), "Decrypting", th, null);
                }
            }
        } catch (Throwable th)
        {
            tableModel.fireChanged(fileDownloadInfo.getFileInfoDTO().getID(), Status.FAILED);
            final Throwable actualTh =
                    (th instanceof Error) ? th : CheckedExceptionTunnel
                            .unwrapIfNecessary((Exception) th);
            if (actualTh instanceof FileExistsException == false)
            {
                AbstractSwingGUI.notifyUserOfThrowable(tableModel.getMainWindow(),
                        fileDownloadInfo.getFileInfoDTO().getName(), "Downloading", th, null);
            }
        } finally
        {
            tableModel.resetCurrentlyDownloadingFile();
        }
    }

    private void removeEncryptedIfRequested(final File file)
    {
        if (deleteEncryptedFileAfterSuccessfulDecryption && decryptionCancelled == false)
        {
            if (file.delete() == false)
            {
                JOptionPane.showMessageDialog(tableModel.getMainWindow(), "Failed to delete file '"
                        + file.getAbsolutePath() + "'.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void decrypt(final File file)
    {
        boolean ok = false;
        while (ok == false)
        {
            try
            {
                tableModel
                        .fireChanged(fileDownloadInfo.getFileInfoDTO().getID(), Status.DECRYPTING);
                final File clearTextFile =
                        OpenPGPSymmetricKeyEncryption.decrypt(file, null, passphrase,
                                createDecryptFileOverwriteStrategyAskUser());
                tableModel.fireChanged(Status.COMPLETED_DOWNLOAD_AND_DECRYPTION);
                final String filenameEncrypted = fileDownloadInfo.getFileInfoDTO().getName();
                final String filenameDecrypted = clearTextFile.getName();
                // Show message dialog only for a clear text file that does not follow trivially
                // from the encrypted file.
                if (filenameEncrypted.equals(filenameDecrypted
                        + OpenPGPSymmetricKeyEncryption.PGP_FILE_EXTENSION) == false)
                {
                    JOptionPane.showMessageDialog(tableModel.getMainWindow(), "File on Server: "
                            + fileDownloadInfo.getFileInfoDTO().getName() + "\n"
                            + "Decrypted file: " + clearTextFile.getPath(), "File Decryption",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                ok = true;
            } catch (CheckedExceptionTunnel ex)
            {
                final Exception actualEx = CheckedExceptionTunnel.unwrapIfNecessary(ex);
                if (actualEx instanceof PGPDataValidationException)
                {
                    passphrase =
                            PassphraseDialog.tryGetPassphraseForDecryptRetry(tableModel
                                    .getMainWindow(), "File: '"
                                    + fileDownloadInfo.getFileInfoDTO().getName() + "'",
                                    "<div color='red'>Wrong passphrase, "
                                            + "please try again.</div>");
                    if (StringUtils.isEmpty(passphrase))
                    {
                        cancelDecryption();
                        ok = true;
                    }
                } else if (actualEx instanceof FileExistsException)
                {
                    cancelDecryption();
                    ok = true;
                } else
                {
                    decryptionCancelled = true;
                    throw ex;
                }
            }
        }
    }

    private IFileOverwriteStrategy createDecryptFileOverwriteStrategyAskUser()
    {
        return new IFileOverwriteStrategy()
            {
                @Override
                public boolean overwriteAllowed(File outputFile)
                {
                    final int answer =
                            JOptionPane.showOptionDialog(tableModel.getMainWindow(),
                                    "The decrypted file '" + outputFile.getAbsolutePath()
                                            + "' already exists. " + "Overwrite?", "File exists",
                                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                    new Object[]
                                        { "Yes", "No" }, "No");
                    return (answer == JOptionPane.YES_OPTION);
                }
            };
    }

    private enum FileOverwriteAction
    {
        REPLACE, RESUME, VETO
    }

    private IFileOverwriteStrategy createFileResumeOrOverwriteStrategyAskUser(
            final FileInfoDTO fileInfo)
    {
        return new IFileOverwriteStrategy()
            {
                @Override
                public boolean overwriteAllowed(File outputFile)
                {
                    final long outputFileLength = outputFile.length();
                    final FileOverwriteAction action;
                    if (outputFileLength <= fileInfo.getSize())
                    {
                        // Resume case
                        final int answer =
                                JOptionPane.showOptionDialog(tableModel.getMainWindow(),
                                        "<html>The file to download '"
                                                + outputFile.getAbsolutePath()
                                                + "' already exists.<br>Your options are:<ol>"
                                                + "<li>Skip file (don't touch local file)"
                                                + "<li>Resume download of file from server"
                                                + "<li>Replace local file with file from server"
                                                + "</ol></html>", "File exists",
                                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                                        null, new Object[]
                                            { "Skip", "Resume", "Replace" }, "Skip");
                        action =
                                (answer == 0) ? FileOverwriteAction.VETO
                                        : (answer == 1 ? FileOverwriteAction.RESUME
                                                : FileOverwriteAction.REPLACE);
                    } else
                    {
                        final int answer =
                                JOptionPane.showOptionDialog(tableModel.getMainWindow(), "A file '"
                                        + outputFile.getAbsolutePath()
                                        + "' already exists but is unsuitable "
                                        + "for resume.\nReplace with file from server?",
                                        "File exists", JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE, null, new Object[]
                                            { "Yes", "No" }, "No");
                        action =
                                (answer == JOptionPane.YES_OPTION) ? FileOverwriteAction.REPLACE
                                        : FileOverwriteAction.VETO;
                    }
                    if (FileOverwriteAction.REPLACE == action)
                    {
                        if (outputFile.delete() == false)
                        {
                            throw CheckedExceptionTunnel.wrapIfNecessary(new IOException(
                                    "Cannot delete file '" + outputFile.getAbsolutePath() + "'."));
                        }
                    }
                    return FileOverwriteAction.VETO != action;
                }
            };
    }

    private void cancelDecryption()
    {
        decryptionCancelled = true;
        tableModel.fireChanged(Status.COMPLETED_DOWNLOAD);
    }

}
