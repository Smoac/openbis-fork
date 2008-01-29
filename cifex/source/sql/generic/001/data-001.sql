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
,'a'
,'admin'
,'0cc175b9c0f1b6a831c399e269772661'
,'T'
,'T'
);

insert into users
(id
,email
,user_name
,encrypted_password
,is_admin
,is_permanent)
values
(nextval('USER_ID_SEQ')
,'p'
,'dummy user'
,'0cc175b9c0f1b6a831c399e269772661'
,'F'
,'T'
);

