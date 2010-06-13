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

package ch.systemsx.cisd.cifex.rpc.client.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * @author Bernd Rinn
 */
public class ClipboardUtils
{

    private ClipboardUtils()
    {
        // Not to be instantiated.
    }

    /**
     * Copy <var>text</var> to clipboard.
     */
    public static void copyToClipboard(final String text)
    {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final StringSelection data = new StringSelection(text);
        clipboard.setContents(data, data);
    }

    /**
     * Returns the content of the clipboard, if it is not empty and is a string, and
     * <code>null</code> otherwise.
     */
    public static String tryPasteClipboard()
    {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final Transferable contentOrNull = clipboard.getContents(null);
        try
        {
            return (contentOrNull == null || contentOrNull
                    .isDataFlavorSupported(DataFlavor.stringFlavor) == false) ? null
                    : contentOrNull.getTransferData(DataFlavor.stringFlavor).toString();
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }
}
