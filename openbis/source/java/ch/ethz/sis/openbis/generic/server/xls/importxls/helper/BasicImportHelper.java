package ch.ethz.sis.openbis.generic.server.xls.importxls.helper;

import ch.ethz.sis.openbis.generic.server.xls.importxls.XLSImport;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportModes;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

public abstract class BasicImportHelper extends AbstractImportHelper
{
    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, XLSImport.class);

    protected final ImportModes mode;

    public BasicImportHelper(ImportModes mode)
    {
        this.mode = mode;
    }

    protected abstract String getTypeName();

    protected boolean isNewVersion(Map<String, Integer> header, List<String> values)
    {
        return true;
    }

    protected void updateVersion(Map<String, Integer> header, List<String> values)
    {
        // do nothing
    }

    protected abstract boolean isObjectExist(Map<String, Integer> header, List<String> values);

    protected abstract void createObject(Map<String, Integer> header, List<String> values, int page, int line);

    protected abstract void updateObject(Map<String, Integer> header, List<String> values, int page, int line);

    public void importBlock(List<List<String>> page, int pageIndex, int start, int end)
    {
        int lineIndex = start;

        try
        {
            Map<String, Integer> header = parseHeader(page.get(lineIndex), true);
            lineIndex++;

            while (lineIndex < end)
            {
                validateLine(header, page.get(lineIndex));
                if (isNewVersion(header, page.get(lineIndex)))
                {
                    if (!isObjectExist(header, page.get(lineIndex)))
                    {
                        createObject(header, page.get(lineIndex), pageIndex, lineIndex);
                        updateVersion(header, page.get(lineIndex));
                    } else
                    {
                        switch (mode)
                        {
                            case FAIL_IF_EXISTS:
                                throw new UserFailureException("Mode FAIL_IF_EXISTS - Found existing " + getTypeName());
                            case UPDATE_IF_EXISTS:
                                updateObject(header, page.get(lineIndex), pageIndex, lineIndex);
                                updateVersion(header, page.get(lineIndex));
                                break;
                            case IGNORE_EXISTING:
                                // do nothing
                                break;
                            default:
                                throw new UserFailureException("Unknown mode");
                        }
                    }
                }

                lineIndex++;
            }

        } catch (Exception e)
        {
            UserFailureException userFailureException = new UserFailureException(
                    "Exception at page " + (pageIndex + 1) + " and line " + (lineIndex + 1) + " with class " + e.getClass().getSimpleName() + "message: " + e.getMessage());
            userFailureException.setStackTrace(e.getStackTrace());
            throw userFailureException;
        }
    }
}
