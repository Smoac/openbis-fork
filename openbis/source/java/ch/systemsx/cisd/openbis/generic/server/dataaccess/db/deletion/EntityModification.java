package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class EntityModification
{
    @JsonIgnore
    public String userId;
    
    @JsonIgnore
    public String permId;

    public Date validFrom;

    public Date validUntil;

}
