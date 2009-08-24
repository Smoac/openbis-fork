package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;

/**
 * A record object for an entity property of type MATERIAL.
 */
@Friend(toClasses=BaseEntityPropertyRecord.class)
@Private
public class MaterialEntityPropertyRecord extends BaseEntityPropertyRecord
{
    public long id;

    public long maty_id;

    public String code;
}