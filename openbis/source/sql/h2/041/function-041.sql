-- Creating Functions

------------------------------------------------------------------------------------
--  Purpose:  Create trigger SAMPLE_CODE_UNIQUENESS_CHECK 
------------------------------------------------------------------------------------
CREATE TRIGGER SAMPLE_CODE_UNIQUENESS_CHECK_INS BEFORE INSERT ON SAMPLES FOR EACH ROW CALL "ch.systemsx.cisd.openbis.generic.server.dataaccess.db.h2.SampleCodeUniquenessCheckTrigger";
CREATE TRIGGER SAMPLE_CODE_UNIQUENESS_CHECK_UPD BEFORE UPDATE ON SAMPLES FOR EACH ROW CALL "ch.systemsx.cisd.openbis.generic.server.dataaccess.db.h2.SampleCodeUniquenessCheckTrigger";