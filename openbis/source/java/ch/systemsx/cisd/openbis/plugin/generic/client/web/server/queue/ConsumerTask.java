package ch.systemsx.cisd.openbis.plugin.generic.client.web.server.queue;

import java.io.IOException;
import java.io.Writer;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

public abstract class ConsumerTask
{
    public abstract String getName();
    public abstract String getUserEmail();
    public abstract void doActionOrThrowException(Writer writer);
    
    public boolean doAction(Writer messageWriter)
    {
        try
        {
            doActionOrThrowException(messageWriter);
        } catch (RuntimeException ex)
        {
            try
            {
                messageWriter.write(getName() + " has failed with a following exception: ");
                messageWriter.write(ex.getMessage());
                messageWriter.write("\n\nPlease correct the error or contact your administrator.");
            } catch (IOException writingEx)
            {
                throw new UserFailureException(writingEx.getMessage()
                        + " when trying to throw exception: " + ex.getMessage(), ex);
            }
            throw ex;
        }
        return true;

    }
}
