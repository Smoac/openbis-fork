----------------------------------------------------------------------
--  Purpose:  Create an initial admin (password: admin) in USERS
-----------------------------------------------------------------------

insert into users
(id
,email
,user_name
,encrypted_password
,is_admin
,is_permanent)
values
(nextval('USER_ID_SEQ')
,'admin@localhost'
,'admin'
,'21232f297a57a5a743894a0e4a801fc3'
,'T'
,'T'
);
