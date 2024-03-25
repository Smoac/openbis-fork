package ch.ethz.sis.openbis.generic.typescript.dto;

import java.util.List;

import ch.ethz.sis.afsapi.dto.File;
import ch.ethz.sis.openbis.generic.typescript.TypeScriptMethod;
import ch.ethz.sis.openbis.generic.typescript.TypeScriptObject;

@TypeScriptObject
public class OpenBISJavaScriptAFSFacade
{

    private OpenBISJavaScriptAFSFacade()
    {
    }

    @TypeScriptMethod(sessionToken = false)
    public List<File> list(final String owner, final String source, final Boolean recursively)
    {
        return null;
    }

    @TypeScriptMethod(sessionToken = false)
    public byte[] read(final String source, final long offset, final int limit)
    {
        return null;
    }

    @TypeScriptMethod(sessionToken = false)
    public boolean write(final String source, final long offset, final byte[] data)
    {
        return false;
    }

    @TypeScriptMethod(sessionToken = false)
    public boolean delete(final String source)
    {
        return false;
    }

    @TypeScriptMethod(sessionToken = false)
    public boolean copy(final String source, final String target)
    {
        return false;
    }

    @TypeScriptMethod(sessionToken = false)
    public boolean move(final String source, final String target)
    {
        return false;
    }

    @TypeScriptMethod(sessionToken = false)
    public boolean create(final String source, final boolean directory)
    {
        return false;
    }

}
