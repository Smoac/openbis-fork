package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion;

import java.util.Date;

public class RelationshipHistoryEntry
{
    public String permId;

    public String relationType;

    public String relatedEntity;

    public String userId;

    public Date validFromTimestamp;

    public Date validUntilTimestamp;

    @Override
    public String toString()
    {
        return "RelationshipHistoryEntry [permId=" + permId + ", relationType=" + relationType + ", relatedEntity="
                + relatedEntity + ", userId=" + userId + ", validFromTimestamp=" + validFromTimestamp + ", validUntilTimestamp="
                + validUntilTimestamp + "]";
    }
}