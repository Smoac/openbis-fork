package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion;

import java.util.Date;

public class PropertyHistoryEntry
{
    public String permId;

    public String propertyCode;

    public String value;

    public String vocabulary_term;

    public String material;

    public String user_id;

    public Date valid_from_timestamp;

    public Date valid_until_timestamp;

    @Override
    public String toString()
    {
        return "HistoryPropertyEntry [permId=" + permId + ", propertyCode=" + propertyCode + ", value=" + value
                + ", vocabulary_term=" + vocabulary_term + ", material=" + material + ", user_id=" + user_id + ", valid_from_timestamp="
                + valid_from_timestamp + ", valid_until_timestamp=" + valid_until_timestamp + "]";
    }
}
