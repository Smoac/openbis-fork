package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.base.utilities.OSUtilities;

/**
 * Creates soft links commands.
 * 
 * @author Izabela Adamczyk
 */
public class SoftLinkMaker
{

    private File lnExec;

    public SoftLinkMaker()
    {
        lnExec = OSUtilities.findExecutable("ln");
        if (lnExec == null)
        {
            throw new IllegalStateException("Linking command not found");
        }
    }

    public final List<String> createCommand(final File sourceFile, final File targetDir)
    {
        final List<String> tokens = new ArrayList<String>();
        tokens.add(lnExec.getAbsolutePath());
        tokens.add("-sf");
        tokens.add(sourceFile.getAbsolutePath());
        tokens.add(targetDir.getAbsolutePath());
        return tokens;
    }
}