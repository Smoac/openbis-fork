-- Migration from 129 to 130

alter table post_registration_dataset_queue drop CONSTRAINT prdq_ds_fk;
alter table post_registration_dataset_queue add CONSTRAINT prdq_ds_fk FOREIGN KEY (ds_id) REFERENCES data_all (id) ON DELETE CASCADE;
