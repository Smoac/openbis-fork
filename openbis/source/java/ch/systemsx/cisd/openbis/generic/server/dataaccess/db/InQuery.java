package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

public class InQuery<I, O>
{
    private static final int POSTGRES_DRIVER_MAX_ARGS = 32767; // Uses a signed 2 bytes integer

    public List<O> withBatch(Session session, String inQuery, String inParameter, List<I> inArguments)
    {
        List<O> result = new ArrayList<O>(inArguments.size());

        int fromIndex = 0;
        while (fromIndex < inArguments.size())
        {
            int toIndex = fromIndex + POSTGRES_DRIVER_MAX_ARGS;
            if (toIndex > inArguments.size())
            {
                toIndex = inArguments.size();
            }

            List<I> partialInArguments = inArguments.subList(fromIndex, toIndex);

            SQLQuery query = session.createSQLQuery(inQuery);
            query.setParameterList(inParameter, partialInArguments);
            List<O> partialResult = query.list();
            result.addAll(partialResult);
            fromIndex = toIndex;
        }

        return result;
    }
}