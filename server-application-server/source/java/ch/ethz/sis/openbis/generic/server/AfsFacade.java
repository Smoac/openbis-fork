package ch.ethz.sis.openbis.generic.server;

import java.util.List;
import java.util.UUID;

import ch.ethz.sis.afsclient.client.AfsClient;

public class AfsFacade
{

    private final AfsClient afsClient;

    public AfsFacade(final AfsClient afsClient)
    {
        this.afsClient = afsClient;
    }

    public void delete(List<String> owners)
    {
        UUID afsTransactionId = null;

        try
        {
            afsTransactionId = UUID.randomUUID();
            afsClient.begin(afsTransactionId);

            for (String owner : owners)
            {
                try
                {
                    afsClient.delete(owner, "");
                } catch (Exception e)
                {
                    if (!e.getMessage().contains("NoSuchFileException"))
                    {
                        throw new RuntimeException("Deletion of data set '" + owner + "' on AFS server has failed.", e);
                    }
                }
            }

            afsClient.commit();
        } catch (Exception e)
        {
            if (afsTransactionId != null)
            {
                try
                {
                    afsClient.rollback();
                } catch (Exception ignore)
                {
                }
            }
            throw new RuntimeException("Deletion of data sets on AFS server has failed.", e);
        }
    }

}
