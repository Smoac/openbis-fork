package ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister;

import java.util.Date;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;

/**
 * A record object representing one row of the material table.
 */
// CREATE TABLE materials (
// id tech_id NOT NULL,
// code code NOT NULL,
// maty_id tech_id NOT NULL,
// pers_id_registerer tech_id NOT NULL,
// registration_timestamp time_stamp_dfl NOT NULL DEFAULT now(),
// dbin_id tech_id NOT NULL);
@Private
public class MaterialRecord extends CodeRecord
{
    public Long dbin_id;

    public long maty_id;

    public long pers_id_registerer;

    public Date registration_timestamp;
}
