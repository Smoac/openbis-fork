package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;

/**
 * A record object for a generic entity property.
 */
@Friend(toClasses=BaseEntityPropertyRecord.class)
@Private
public class GenericEntityPropertyRecord extends BaseEntityPropertyRecord
{
    public String value;
}