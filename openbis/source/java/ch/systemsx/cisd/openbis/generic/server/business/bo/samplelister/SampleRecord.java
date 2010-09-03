package ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister;

import java.util.Date;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;

/**
 * A record object representing one row of the sample table.
 */
// CREATE TABLE SAMPLES (
// ID TECH_ID NOT NULL,
// PERM_ID CODE NOT NULL,
// CODE CODE NOT NULL,
// EXPE_ID TECH_ID,
// SATY_ID TECH_ID NOT NULL,
// REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,
// MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP,
// PERS_ID_REGISTERER TECH_ID NOT NULL,
// INVA_ID TECH_ID,
// DBIN_ID TECH_ID,
// GROU_ID TECH_ID,
// SAMP_ID_PART_OF TECH_ID);
@Private
public class SampleRecord extends CodeRecord
{
    public String perm_id;

    public Long expe_id;

    public Long grou_id;

    public Long dbin_id;

    public Long samp_id_part_of;

    public Date registration_timestamp;

    public Date modification_timestamp;

    public long pers_id_registerer;

    public Long inva_id;

    public long saty_id;
}
