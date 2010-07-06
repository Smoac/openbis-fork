-- Create index on email address of users.
CREATE INDEX USER_EMAIL_I ON USERS (EMAIL);

-- Implement CALC_ACCOUNTING_FOR_QUOTA_GROUPS() without variadic parameters to make it work also 
-- with versions before PostgreSQL 8.4.

-------------------------------------------------------------------------------------------
--  Purpose:  Re-calculate the accounting information for given quota groups
-------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION CALC_ACCOUNTING_FOR_QUOTA_GROUPS(BIGINT, BIGINT) RETURNS void AS $$
        LOCK TABLE QUOTA_GROUPS IN ACCESS EXCLUSIVE MODE;
        UPDATE QUOTA_GROUPS Q SET FILE_COUNT = (
                SELECT COUNT(*) FROM FILES F JOIN USERS U ON F.USER_ID = U.ID 
                        WHERE U.QUOTA_GROUP_ID = Q.ID 
          ) 
          WHERE Q.ID = $1 OR Q.ID = $2;
        UPDATE QUOTA_GROUPS Q SET FILE_SIZE = (
                SELECT COALESCE(SUM(F.COMPLETE_SIZE), 0) FROM FILES F JOIN USERS U ON F.USER_ID = U.ID
                        WHERE U.QUOTA_GROUP_ID = Q.ID
          )
          WHERE Q.ID = $1 OR Q.ID = $2;
$$ LANGUAGE 'sql';