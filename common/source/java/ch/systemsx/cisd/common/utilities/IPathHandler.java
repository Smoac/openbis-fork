package ch.systemsx.cisd.common.utilities;

import java.io.File;

/**
 * A role for handling paths. The paths are supposed to go away when they have been handled successfully.
 * 
 * @author Bernd Rinn
 */
public interface IPathHandler
{
    /**
     * Handles the <var>path</var>. Successful handling is indicated by <var>path</var> being gone when the method
     * returns.
     */
    public void handle(File path);
}