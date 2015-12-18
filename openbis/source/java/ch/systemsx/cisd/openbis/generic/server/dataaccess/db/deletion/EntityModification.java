package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion;

import java.util.Date;

public class EntityModification
{
    public final String key;

    public final String value;

    public final Date validFrom;

    public final Date validUntil;

    public EntityModification(String key, String value, Date validFrom, Date validUntil)
    {
        this.key = key;
        this.value = value;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }
}
