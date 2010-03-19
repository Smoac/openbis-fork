package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import java.util.Date;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;

/**
 * A record object representing one row of the dataset table.
 */
@Private
public class DatasetRecord extends CodeRecord
{
    // --- from data table

    public long dsty_id;

    public long dast_id;

    public long expe_id;

    public String data_producer_code;

    public Date production_timestamp;

    public Long samp_id;

    public Date registration_timestamp;

    public boolean is_placeholder;

    public boolean is_valid;

    public boolean is_derived;

    // ---- from external_data table

    public String location;

    public Long ffty_id;

    public Long loty_id;

    public String is_complete; // maps to BooleanOrUnknown

    public String status;
}
