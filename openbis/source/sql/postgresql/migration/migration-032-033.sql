-- -------
-- Modify PERSON
-- -------

ALTER TABLE persons
    ADD COLUMN display_settings file;


-------------------------------------------------------------------------
--  Purpose:  Insert additional initial data set into the table FILE_FORMAT_TYPES
-------------------------------------------------------------------------

insert into file_format_types
(id
,code
,description
,dbin_id)
values 
(nextval('FILE_FORMAT_TYPE_ID_SEQ')
,'HDF5'
,'Hierarchical Data Format File, version 5'
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);

insert into file_format_types
(id
,code
,description
,dbin_id)
values 
(nextval('FILE_FORMAT_TYPE_ID_SEQ')
,'PROPRIETARY'
,'Proprietary Format File'
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);

insert into file_format_types
(id
,code
,description
,dbin_id)
values 
(nextval('FILE_FORMAT_TYPE_ID_SEQ')
,'SRF'
,'Sequence Read Format File'
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);

insert into file_format_types
(id
,code
,description
,dbin_id)
values 
(nextval('FILE_FORMAT_TYPE_ID_SEQ')
,'TSV'
,'Tab Separated Values File'
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);

insert into file_format_types
(id
,code
,description
,dbin_id)
values 
(nextval('FILE_FORMAT_TYPE_ID_SEQ')
,'XML'
,'XML File'
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);

