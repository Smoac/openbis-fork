-- Migration from 083 to 084
--
-- mark screening core plugin as initialized for existing installation
--
insert into CORE_PLUGINS (ID, NAME, VERSION) values (1, "screening", 1);

