ALTER TABLE ONLY authorization_groups
    ADD CONSTRAINT ag_bk_uk UNIQUE (code);
ALTER TABLE ONLY authorization_groups
    ADD CONSTRAINT ag_pk PRIMARY KEY (id);
ALTER TABLE ONLY authorization_group_persons
    ADD CONSTRAINT agp_pk PRIMARY KEY (pers_id, ag_id);
ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_expe_bk_uk UNIQUE (expe_id, file_name, version);
ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_pk PRIMARY KEY (id);
ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_proj_bk_uk UNIQUE (proj_id, file_name, version);
ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_samp_bk_uk UNIQUE (samp_id, file_name, version);
ALTER TABLE ONLY core_plugins
    ADD CONSTRAINT copl_name_ver_uk UNIQUE (name, version);
ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_bk_uk UNIQUE (code, is_internal_namespace);
ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_pk PRIMARY KEY (id);
ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_bk_uk UNIQUE (code, covo_id);
ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_pk PRIMARY KEY (id);
ALTER TABLE ONLY data_stores
    ADD CONSTRAINT dast_bk_uk UNIQUE (code, uuid);
ALTER TABLE ONLY data_stores
    ADD CONSTRAINT dast_pk PRIMARY KEY (id);
ALTER TABLE ONLY data_all
    ADD CONSTRAINT data_bk_uk UNIQUE (code);
ALTER TABLE ONLY data_all
    ADD CONSTRAINT data_pk PRIMARY KEY (id);
ALTER TABLE ONLY data_set_relationships_history
    ADD CONSTRAINT datarelh_pk PRIMARY KEY (id);
ALTER TABLE ONLY data_types
    ADD CONSTRAINT daty_bk_uk UNIQUE (code);
ALTER TABLE ONLY data_types
    ADD CONSTRAINT daty_pk PRIMARY KEY (id);
ALTER TABLE ONLY deletions
    ADD CONSTRAINT del_pk PRIMARY KEY (id);
ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_bk_uk UNIQUE (ds_id, dstpt_id);
ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_pk PRIMARY KEY (id);
ALTER TABLE ONLY data_set_properties_history
    ADD CONSTRAINT dsprh_pk PRIMARY KEY (id);
ALTER TABLE ONLY data_set_relationships_all
    ADD CONSTRAINT dsre_bk_uk UNIQUE (data_id_child, data_id_parent, relationship_id);
ALTER TABLE ONLY data_store_service_data_set_types
    ADD CONSTRAINT dssdst_bk_uk UNIQUE (data_store_service_id, data_set_type_id);
ALTER TABLE ONLY data_store_services
    ADD CONSTRAINT dsse_bk_uk UNIQUE (key, data_store_id);
ALTER TABLE ONLY data_store_services
    ADD CONSTRAINT dsse_pk PRIMARY KEY (id);
ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_bk_uk UNIQUE (dsty_id, prty_id);
ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_pk PRIMARY KEY (id);
ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT dsty_bk_uk UNIQUE (code);
ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT dsty_pk PRIMARY KEY (id);
ALTER TABLE ONLY external_data_management_systems
    ADD CONSTRAINT edms_code_uk UNIQUE (code);
ALTER TABLE ONLY external_data_management_systems
    ADD CONSTRAINT edms_pk PRIMARY KEY (id);
ALTER TABLE ONLY entity_operations_log
    ADD CONSTRAINT eol_pk PRIMARY KEY (id);
ALTER TABLE ONLY entity_operations_log
    ADD CONSTRAINT eol_reg_id_uk UNIQUE (registration_id);
ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_bk_uk UNIQUE (exty_id, prty_id);
ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_pk PRIMARY KEY (id);
ALTER TABLE ONLY events
    ADD CONSTRAINT evnt_pk PRIMARY KEY (id);
ALTER TABLE ONLY attachment_contents
    ADD CONSTRAINT exac_pk PRIMARY KEY (id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_bk_uk UNIQUE (location, loty_id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_pk PRIMARY KEY (data_id);
ALTER TABLE ONLY experiments_all
    ADD CONSTRAINT expe_bk_uk UNIQUE (code, proj_id);
ALTER TABLE ONLY experiments_all
    ADD CONSTRAINT expe_pi_uk UNIQUE (perm_id);
ALTER TABLE ONLY experiments_all
    ADD CONSTRAINT expe_pk PRIMARY KEY (id);
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_bk_uk UNIQUE (expe_id, etpt_id);
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_pk PRIMARY KEY (id);
ALTER TABLE ONLY experiment_properties_history
    ADD CONSTRAINT exprh_pk PRIMARY KEY (id);
ALTER TABLE ONLY experiment_relationships_history
    ADD CONSTRAINT exrelh_pk PRIMARY KEY (id);
ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_bk_uk UNIQUE (code);
ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_pk PRIMARY KEY (id);
ALTER TABLE ONLY file_format_types
    ADD CONSTRAINT ffty_bk_uk UNIQUE (code);
ALTER TABLE ONLY file_format_types
    ADD CONSTRAINT ffty_pk PRIMARY KEY (id);
ALTER TABLE ONLY filters
    ADD CONSTRAINT filt_bk_uk UNIQUE (name, grid_id);
ALTER TABLE ONLY filters
    ADD CONSTRAINT filt_pk PRIMARY KEY (id);
ALTER TABLE ONLY grid_custom_columns
    ADD CONSTRAINT grid_custom_columns_bk_uk UNIQUE (code, grid_id);
ALTER TABLE ONLY grid_custom_columns
    ADD CONSTRAINT grid_custom_columns_pk PRIMARY KEY (id);
ALTER TABLE ONLY link_data
    ADD CONSTRAINT lnda_pk PRIMARY KEY (data_id);
ALTER TABLE ONLY locator_types
    ADD CONSTRAINT loty_bk_uk UNIQUE (code);
ALTER TABLE ONLY locator_types
    ADD CONSTRAINT loty_pk PRIMARY KEY (id);
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_bk_uk UNIQUE (mate_id, mtpt_id);
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_pk PRIMARY KEY (id);
ALTER TABLE ONLY material_properties_history
    ADD CONSTRAINT maprh_pk PRIMARY KEY (id);
ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_bk_uk UNIQUE (code, maty_id);
ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_pk PRIMARY KEY (id);
ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_bk_uk UNIQUE (code);
ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_pk PRIMARY KEY (id);
ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_mepr_id_data_id_uk UNIQUE (mepr_id, data_id);
ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_mepr_id_expe_id_uk UNIQUE (mepr_id, expe_id);
ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_mepr_id_mate_id_uk UNIQUE (mepr_id, mate_id);
ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_mepr_id_samp_id_uk UNIQUE (mepr_id, samp_id);
ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_pk PRIMARY KEY (id);
ALTER TABLE ONLY metaprojects
    ADD CONSTRAINT metaprojects_pk PRIMARY KEY (id);
ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_bk_uk UNIQUE (maty_id, prty_id);
ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_pk PRIMARY KEY (id);
ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_bk_uk UNIQUE (user_id);
ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_pk PRIMARY KEY (id);
ALTER TABLE ONLY post_registration_dataset_queue
    ADD CONSTRAINT prdq_pk PRIMARY KEY (id);
ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_bk_uk UNIQUE (code, space_id);
ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pi_uk UNIQUE (perm_id);
ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pk PRIMARY KEY (id);
ALTER TABLE ONLY project_relationships_history
    ADD CONSTRAINT prrelh_pk PRIMARY KEY (id);
ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_bk_uk UNIQUE (code, is_internal_namespace);
ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_pk PRIMARY KEY (id);
ALTER TABLE ONLY queries
    ADD CONSTRAINT quer_bk_uk UNIQUE (name);
ALTER TABLE ONLY queries
    ADD CONSTRAINT quer_pk PRIMARY KEY (id);
ALTER TABLE ONLY relationship_types
    ADD CONSTRAINT rety_pk PRIMARY KEY (id);
ALTER TABLE ONLY relationship_types
    ADD CONSTRAINT rety_uk UNIQUE (code);
ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_ag_space_bk_uk UNIQUE (ag_id_grantee, role_code, space_id);
ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pe_space_bk_uk UNIQUE (pers_id_grantee, role_code, space_id);
ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pk PRIMARY KEY (id);
ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_code_unique_check_uk UNIQUE (code_unique_check);
ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_pi_uk UNIQUE (perm_id);
ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_pk PRIMARY KEY (id);
ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_subcode_unique_check_uk UNIQUE (subcode_unique_check);
ALTER TABLE ONLY sample_relationships_history
    ADD CONSTRAINT samprelh_pk PRIMARY KEY (id);
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_bk_uk UNIQUE (samp_id, stpt_id);
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_pk PRIMARY KEY (id);
ALTER TABLE ONLY sample_properties_history
    ADD CONSTRAINT saprh_pk PRIMARY KEY (id);
ALTER TABLE ONLY sample_relationships_all
    ADD CONSTRAINT sare_bk_uk UNIQUE (sample_id_child, sample_id_parent, relationship_id);
ALTER TABLE ONLY sample_relationships_all
    ADD CONSTRAINT sare_pk PRIMARY KEY (id);
ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_bk_uk UNIQUE (code);
ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_pk PRIMARY KEY (id);
ALTER TABLE ONLY scripts
    ADD CONSTRAINT scri_pk PRIMARY KEY (id);
ALTER TABLE ONLY scripts
    ADD CONSTRAINT scri_uk UNIQUE (name);
ALTER TABLE ONLY spaces
    ADD CONSTRAINT space_bk_uk UNIQUE (code);
ALTER TABLE ONLY spaces
    ADD CONSTRAINT space_pk PRIMARY KEY (id);
ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_bk_uk UNIQUE (saty_id, prty_id);
ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_pk PRIMARY KEY (id);
CREATE INDEX atta_exac_fk_i ON attachments USING btree (exac_id);
CREATE INDEX atta_expe_fk_i ON attachments USING btree (expe_id);
CREATE INDEX atta_pers_fk_i ON attachments USING btree (pers_id_registerer);
CREATE INDEX atta_proj_fk_i ON attachments USING btree (proj_id);
CREATE INDEX atta_samp_fk_i ON attachments USING btree (samp_id);
CREATE INDEX covo_pers_fk_i ON controlled_vocabularies USING btree (pers_id_registerer);
CREATE INDEX cvte_covo_fk_i ON controlled_vocabulary_terms USING btree (covo_id);
CREATE INDEX cvte_pers_fk_i ON controlled_vocabulary_terms USING btree (pers_id_registerer);
CREATE INDEX data_del_fk_i ON data_all USING btree (del_id);
CREATE INDEX data_dsty_fk_i ON data_all USING btree (dsty_id);
CREATE INDEX data_expe_fk_i ON data_all USING btree (expe_id);
CREATE INDEX data_samp_fk_i ON data_all USING btree (samp_id);
CREATE INDEX datarelh_data_fk_i ON data_set_relationships_history USING btree (data_id);
CREATE INDEX datarelh_main_data_fk_data_fk_i ON data_set_relationships_history USING btree (main_data_id, data_id);
CREATE INDEX datarelh_main_data_fk_expe_fk_i ON data_set_relationships_history USING btree (main_data_id, expe_id);
CREATE INDEX datarelh_main_data_fk_i ON data_set_relationships_history USING btree (main_data_id);
CREATE INDEX datarelh_main_data_fk_samp_fk_i ON data_set_relationships_history USING btree (main_data_id, samp_id);
CREATE INDEX del_pers_fk_i ON deletions USING btree (pers_id_registerer);
CREATE INDEX dspr_cvte_fk_i ON data_set_properties USING btree (cvte_id);
CREATE INDEX dspr_ds_fk_i ON data_set_properties USING btree (ds_id);
CREATE INDEX dspr_dstpt_fk_i ON data_set_properties USING btree (dstpt_id);
CREATE INDEX dspr_mapr_fk_i ON data_set_properties USING btree (mate_prop_id);
CREATE INDEX dspr_pers_fk_i ON data_set_properties USING btree (pers_id_registerer);
CREATE INDEX dsprh_etpt_fk_i ON data_set_properties_history USING btree (dstpt_id);
CREATE INDEX dsprh_expe_fk_i ON data_set_properties_history USING btree (ds_id);
CREATE INDEX dsprh_vuts_fk_i ON data_set_properties_history USING btree (valid_until_timestamp);
CREATE INDEX dsre_data_fk_i_child ON data_set_relationships_all USING btree (data_id_child);
CREATE INDEX dsre_data_fk_i_parent ON data_set_relationships_all USING btree (data_id_parent);
CREATE INDEX dsre_del_fk_i ON data_set_relationships_all USING btree (del_id);
CREATE INDEX dssdst_ds_fk_i ON data_store_service_data_set_types USING btree (data_store_service_id);
CREATE INDEX dssdst_dst_fk_i ON data_store_service_data_set_types USING btree (data_set_type_id);
CREATE INDEX dsse_ds_fk_i ON data_store_services USING btree (data_store_id);
CREATE INDEX dstpt_dsty_fk_i ON data_set_type_property_types USING btree (dsty_id);
CREATE INDEX dstpt_pers_fk_i ON data_set_type_property_types USING btree (pers_id_registerer);
CREATE INDEX dstpt_prty_fk_i ON data_set_type_property_types USING btree (prty_id);
CREATE INDEX entity_operations_log_rid_i ON entity_operations_log USING btree (registration_id);
CREATE INDEX etpt_exty_fk_i ON experiment_type_property_types USING btree (exty_id);
CREATE INDEX etpt_pers_fk_i ON experiment_type_property_types USING btree (pers_id_registerer);
CREATE INDEX etpt_prty_fk_i ON experiment_type_property_types USING btree (prty_id);
CREATE INDEX evnt_pers_fk_i ON events USING btree (pers_id_registerer);
CREATE INDEX exda_cvte_fk_i ON external_data USING btree (cvte_id_stor_fmt);
CREATE INDEX exda_cvte_stored_on_fk_i ON external_data USING btree (cvte_id_store);
CREATE INDEX exda_ffty_fk_i ON external_data USING btree (ffty_id);
CREATE INDEX exda_loty_fk_i ON external_data USING btree (loty_id);
CREATE INDEX expe_del_fk_i ON experiments_all USING btree (del_id);
CREATE INDEX expe_exty_fk_i ON experiments_all USING btree (exty_id);
CREATE INDEX expe_pers_fk_i ON experiments_all USING btree (pers_id_registerer);
CREATE INDEX expe_proj_fk_i ON experiments_all USING btree (proj_id);
CREATE INDEX expr_cvte_fk_i ON experiment_properties USING btree (cvte_id);
CREATE INDEX expr_etpt_fk_i ON experiment_properties USING btree (etpt_id);
CREATE INDEX expr_expe_fk_i ON experiment_properties USING btree (expe_id);
CREATE INDEX expr_mapr_fk_i ON experiment_properties USING btree (mate_prop_id);
CREATE INDEX expr_pers_fk_i ON experiment_properties USING btree (pers_id_registerer);
CREATE INDEX exprh_etpt_fk_i ON experiment_properties_history USING btree (etpt_id);
CREATE INDEX exprh_expe_fk_i ON experiment_properties_history USING btree (expe_id);
CREATE INDEX exprh_vuts_fk_i ON experiment_properties_history USING btree (valid_until_timestamp);
CREATE INDEX exrelh_data_id_fk_i ON experiment_relationships_history USING btree (data_id);
CREATE INDEX exrelh_main_expe_fk_data_fk_i ON experiment_relationships_history USING btree (main_expe_id, data_id);
CREATE INDEX exrelh_main_expe_fk_i ON experiment_relationships_history USING btree (main_expe_id);
CREATE INDEX exrelh_main_expe_fk_proj_fk_i ON experiment_relationships_history USING btree (main_expe_id, proj_id);
CREATE INDEX exrelh_main_expe_fk_samp_fk_i ON experiment_relationships_history USING btree (main_expe_id, samp_id);
CREATE INDEX exrelh_samp_id_fk_i ON experiment_relationships_history USING btree (samp_id);
CREATE INDEX filt_pers_fk_i ON filters USING btree (pers_id_registerer);
CREATE INDEX grid_custom_columns_pers_fk_i ON grid_custom_columns USING btree (pers_id_registerer);
CREATE INDEX mapr_cvte_fk_i ON material_properties USING btree (cvte_id);
CREATE INDEX mapr_mapr_fk_i ON material_properties USING btree (mate_prop_id);
CREATE INDEX mapr_mate_fk_i ON material_properties USING btree (mate_id);
CREATE INDEX mapr_mtpt_fk_i ON material_properties USING btree (mtpt_id);
CREATE INDEX mapr_pers_fk_i ON material_properties USING btree (pers_id_registerer);
CREATE INDEX maprh_etpt_fk_i ON material_properties_history USING btree (mtpt_id);
CREATE INDEX maprh_expe_fk_i ON material_properties_history USING btree (mate_id);
CREATE INDEX maprh_vuts_fk_i ON material_properties_history USING btree (valid_until_timestamp);
CREATE INDEX mate_maty_fk_i ON materials USING btree (maty_id);
CREATE INDEX mate_pers_fk_i ON materials USING btree (pers_id_registerer);
CREATE INDEX metaproject_assignments_all_data_fk_i ON metaproject_assignments_all USING btree (data_id);
CREATE INDEX metaproject_assignments_all_del_fk_i ON metaproject_assignments_all USING btree (del_id);
CREATE INDEX metaproject_assignments_all_expe_fk_i ON metaproject_assignments_all USING btree (expe_id);
CREATE INDEX metaproject_assignments_all_mate_fk_i ON metaproject_assignments_all USING btree (mate_id);
CREATE INDEX metaproject_assignments_all_mepr_fk_i ON metaproject_assignments_all USING btree (mepr_id);
CREATE INDEX metaproject_assignments_all_samp_fk_i ON metaproject_assignments_all USING btree (samp_id);
CREATE INDEX metaprojects_name_i ON metaprojects USING btree (name);
CREATE UNIQUE INDEX metaprojects_name_owner_uk ON metaprojects USING btree (lower((name)::text), owner);
CREATE INDEX metaprojects_owner_fk_i ON metaprojects USING btree (owner);
CREATE INDEX mtpt_maty_fk_i ON material_type_property_types USING btree (maty_id);
CREATE INDEX mtpt_pers_fk_i ON material_type_property_types USING btree (pers_id_registerer);
CREATE INDEX mtpt_prty_fk_i ON material_type_property_types USING btree (prty_id);
CREATE INDEX pers_is_active_i ON persons USING btree (is_active);
CREATE INDEX pers_space_fk_i ON persons USING btree (space_id);
CREATE INDEX proj_pers_fk_i_leader ON projects USING btree (pers_id_leader);
CREATE INDEX proj_pers_fk_i_registerer ON projects USING btree (pers_id_registerer);
CREATE INDEX proj_space_fk_i ON projects USING btree (space_id);
CREATE INDEX prrelh_main_proj_fk_expe_fk_i ON project_relationships_history USING btree (main_proj_id, expe_id);
CREATE INDEX prrelh_main_proj_fk_i ON project_relationships_history USING btree (main_proj_id);
CREATE INDEX prrelh_main_proj_fk_space_fk_i ON project_relationships_history USING btree (main_proj_id, space_id);
CREATE INDEX prty_covo_fk_i ON property_types USING btree (covo_id);
CREATE INDEX prty_daty_fk_i ON property_types USING btree (daty_id);
CREATE INDEX prty_pers_fk_i ON property_types USING btree (pers_id_registerer);
CREATE INDEX roas_ag_fk_i_grantee ON role_assignments USING btree (ag_id_grantee);
CREATE INDEX roas_pers_fk_i_grantee ON role_assignments USING btree (pers_id_grantee);
CREATE INDEX roas_pers_fk_i_registerer ON role_assignments USING btree (pers_id_registerer);
CREATE INDEX roas_space_fk_i ON role_assignments USING btree (space_id);
CREATE INDEX samp_code_i ON samples_all USING btree (code);
CREATE INDEX samp_del_fk_i ON samples_all USING btree (del_id);
CREATE INDEX samp_expe_fk_i ON samples_all USING btree (expe_id);
CREATE INDEX samp_pers_fk_i ON samples_all USING btree (pers_id_registerer);
CREATE INDEX samp_samp_fk_i_part_of ON samples_all USING btree (samp_id_part_of);
CREATE INDEX samp_saty_fk_i ON samples_all USING btree (saty_id);
CREATE INDEX samprelh_data_id_fk_i ON sample_relationships_history USING btree (data_id);
CREATE INDEX samprelh_main_samp_fk_data_fk_i ON sample_relationships_history USING btree (main_samp_id, data_id);
CREATE INDEX samprelh_main_samp_fk_expe_fk_i ON sample_relationships_history USING btree (main_samp_id, expe_id);
CREATE INDEX samprelh_main_samp_fk_i ON sample_relationships_history USING btree (main_samp_id);
CREATE INDEX samprelh_main_samp_fk_samp_fk_i ON sample_relationships_history USING btree (main_samp_id, samp_id);
CREATE INDEX samprelh_main_samp_fk_space_fk_i ON sample_relationships_history USING btree (main_samp_id, space_id);
CREATE INDEX samprelh_samp_id_fk_i ON sample_relationships_history USING btree (samp_id);
CREATE INDEX sapr_cvte_fk_i ON sample_properties USING btree (cvte_id);
CREATE INDEX sapr_mapr_fk_i ON sample_properties USING btree (mate_prop_id);
CREATE INDEX sapr_pers_fk_i ON sample_properties USING btree (pers_id_registerer);
CREATE INDEX sapr_samp_fk_i ON sample_properties USING btree (samp_id);
CREATE INDEX sapr_stpt_fk_i ON sample_properties USING btree (stpt_id);
CREATE INDEX saprh_etpt_fk_i ON sample_properties_history USING btree (stpt_id);
CREATE INDEX saprh_expe_fk_i ON sample_properties_history USING btree (samp_id);
CREATE INDEX saprh_vuts_fk_i ON sample_properties_history USING btree (valid_until_timestamp);
CREATE INDEX sare_data_fk_i_child ON sample_relationships_all USING btree (sample_id_child);
CREATE INDEX sare_data_fk_i_parent ON sample_relationships_all USING btree (sample_id_parent);
CREATE INDEX sare_data_fk_i_relationship ON sample_relationships_all USING btree (relationship_id);
CREATE INDEX sare_del_fk_i ON sample_relationships_all USING btree (del_id);
CREATE INDEX script_pers_fk_i ON scripts USING btree (pers_id_registerer);
CREATE INDEX space_pers_registered_by_fk_i ON spaces USING btree (pers_id_registerer);
CREATE INDEX stpt_pers_fk_i ON sample_type_property_types USING btree (pers_id_registerer);
CREATE INDEX stpt_prty_fk_i ON sample_type_property_types USING btree (prty_id);
CREATE INDEX stpt_saty_fk_i ON sample_type_property_types USING btree (saty_id);
CREATE RULE data_all AS
    ON DELETE TO data DO INSTEAD  DELETE FROM data_all
  WHERE ((data_all.id)::bigint = (old.id)::bigint);
CREATE RULE data_deleted_delete AS
    ON DELETE TO data_deleted DO INSTEAD  DELETE FROM data_all
  WHERE ((data_all.id)::bigint = (old.id)::bigint);
CREATE RULE data_deleted_update AS
    ON UPDATE TO data_deleted DO INSTEAD  UPDATE data_all SET del_id = new.del_id, orig_del = new.orig_del, modification_timestamp = new.modification_timestamp, version = new.version
  WHERE ((data_all.id)::bigint = (new.id)::bigint);
CREATE RULE data_insert AS
    ON INSERT TO data DO INSTEAD  INSERT INTO data_all (id, code, del_id, orig_del, expe_id, dast_id, data_producer_code, dsty_id, is_derived, is_placeholder, is_valid, modification_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, production_timestamp, registration_timestamp, samp_id, version)
  VALUES (new.id, new.code, new.del_id, new.orig_del, new.expe_id, new.dast_id, new.data_producer_code, new.dsty_id, new.is_derived, new.is_placeholder, new.is_valid, new.modification_timestamp, new.access_timestamp, new.pers_id_registerer, new.pers_id_modifier, new.production_timestamp, new.registration_timestamp, new.samp_id, new.version);
CREATE RULE data_relationship_delete AS
    ON DELETE TO data_set_relationships_all
   WHERE (old.del_id IS NULL) DO  UPDATE data_set_relationships_history SET valid_until_timestamp = now()
  WHERE ((((((data_set_relationships_history.main_data_id)::bigint = (old.data_id_parent)::bigint) AND ((data_set_relationships_history.data_id)::bigint = (old.data_id_child)::bigint)) AND ((data_set_relationships_history.relation_type)::text = ( SELECT upper((relationship_types.parent_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (old.relationship_id)::bigint)))) AND (data_set_relationships_history.valid_until_timestamp IS NULL)) OR (((((data_set_relationships_history.main_data_id)::bigint = (old.data_id_child)::bigint) AND ((data_set_relationships_history.data_id)::bigint = (old.data_id_parent)::bigint)) AND ((data_set_relationships_history.relation_type)::text = ( SELECT upper((relationship_types.child_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (old.relationship_id)::bigint)))) AND (data_set_relationships_history.valid_until_timestamp IS NULL)));
CREATE RULE data_relationship_insert AS
    ON INSERT TO data_set_relationships_all
   WHERE (new.del_id IS NULL) DO ( INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, ordinal)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.data_id_parent, ( SELECT upper((relationship_types.parent_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (new.relationship_id)::bigint)), new.data_id_child, ( SELECT data_all.code
           FROM data_all
          WHERE ((data_all.id)::bigint = (new.data_id_child)::bigint)), new.pers_id_author, new.modification_timestamp, new.ordinal);
 INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, ordinal)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.data_id_child, ( SELECT upper((relationship_types.child_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (new.relationship_id)::bigint)), new.data_id_parent, ( SELECT data_all.code
           FROM data_all
          WHERE ((data_all.id)::bigint = (new.data_id_parent)::bigint)), new.pers_id_author, new.modification_timestamp, new.ordinal);
);
CREATE RULE data_relationship_trash_revert_update AS
    ON UPDATE TO data_set_relationships_all
   WHERE ((old.del_id IS NOT NULL) AND (new.del_id IS NULL)) DO ( INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, ordinal)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.data_id_parent, ( SELECT upper((relationship_types.parent_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (new.relationship_id)::bigint)), new.data_id_child, ( SELECT data_all.code
           FROM data_all
          WHERE ((data_all.id)::bigint = (new.data_id_child)::bigint)), new.pers_id_author, new.modification_timestamp, new.ordinal);
 INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, ordinal)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.data_id_child, ( SELECT upper((relationship_types.child_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (new.relationship_id)::bigint)), new.data_id_parent, ( SELECT data_all.code
           FROM data_all
          WHERE ((data_all.id)::bigint = (new.data_id_parent)::bigint)), new.pers_id_author, new.modification_timestamp, new.ordinal);
);
CREATE RULE data_relationship_trash_update AS
    ON UPDATE TO data_set_relationships_all
   WHERE ((new.del_id IS NOT NULL) AND (old.del_id IS NULL)) DO  UPDATE data_set_relationships_history SET valid_until_timestamp = now()
  WHERE ((((((data_set_relationships_history.main_data_id)::bigint = (old.data_id_parent)::bigint) AND ((data_set_relationships_history.data_id)::bigint = (old.data_id_child)::bigint)) AND ((data_set_relationships_history.relation_type)::text = ( SELECT upper((relationship_types.parent_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (old.relationship_id)::bigint)))) AND (data_set_relationships_history.valid_until_timestamp IS NULL)) OR (((((data_set_relationships_history.main_data_id)::bigint = (old.data_id_child)::bigint) AND ((data_set_relationships_history.data_id)::bigint = (old.data_id_parent)::bigint)) AND ((data_set_relationships_history.relation_type)::text = ( SELECT upper((relationship_types.child_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (old.relationship_id)::bigint)))) AND (data_set_relationships_history.valid_until_timestamp IS NULL)));
CREATE RULE data_relationship_update AS
    ON UPDATE TO data_set_relationships_all
   WHERE ((new.del_id IS NULL) AND (old.del_id IS NULL)) DO ( UPDATE data_set_relationships_history SET valid_until_timestamp = now()
  WHERE ((((((data_set_relationships_history.main_data_id)::bigint = (old.data_id_parent)::bigint) AND ((data_set_relationships_history.data_id)::bigint = (old.data_id_child)::bigint)) AND ((data_set_relationships_history.relation_type)::text = ( SELECT upper((relationship_types.parent_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (old.relationship_id)::bigint)))) AND (data_set_relationships_history.valid_until_timestamp IS NULL)) OR (((((data_set_relationships_history.main_data_id)::bigint = (old.data_id_child)::bigint) AND ((data_set_relationships_history.data_id)::bigint = (old.data_id_parent)::bigint)) AND ((data_set_relationships_history.relation_type)::text = ( SELECT upper((relationship_types.child_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (old.relationship_id)::bigint)))) AND (data_set_relationships_history.valid_until_timestamp IS NULL)));
 INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, ordinal)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.data_id_parent, ( SELECT upper((relationship_types.parent_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (new.relationship_id)::bigint)), new.data_id_child, ( SELECT data_all.code
           FROM data_all
          WHERE ((data_all.id)::bigint = (new.data_id_child)::bigint)), new.pers_id_author, new.modification_timestamp, new.ordinal);
 INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp, ordinal)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.data_id_child, ( SELECT upper((relationship_types.child_label)::text) AS upper
           FROM relationship_types
          WHERE ((relationship_types.id)::bigint = (new.relationship_id)::bigint)), new.data_id_parent, ( SELECT data_all.code
           FROM data_all
          WHERE ((data_all.id)::bigint = (new.data_id_parent)::bigint)), new.pers_id_author, new.modification_timestamp, new.ordinal);
);
CREATE RULE data_set_properties_delete AS
    ON DELETE TO data_set_properties
   WHERE (((((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) OR (old.cvte_id IS NOT NULL)) OR (old.mate_prop_id IS NOT NULL)) AND (( SELECT data_all.del_id
           FROM data_all
          WHERE ((data_all.id)::bigint = (old.ds_id)::bigint)) IS NULL)) DO  INSERT INTO data_set_properties_history (id, ds_id, dstpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('data_set_property_id_seq'::regclass), old.ds_id, old.dstpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (controlled_vocabulary_terms t
      JOIN controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
     WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (materials m
      JOIN material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
     WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, now());
CREATE RULE data_set_properties_update AS
    ON UPDATE TO data_set_properties
   WHERE (((((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) AND ((old.value)::text <> (new.value)::text)) OR ((old.cvte_id IS NOT NULL) AND ((old.cvte_id)::bigint <> (new.cvte_id)::bigint))) OR ((old.mate_prop_id IS NOT NULL) AND ((old.mate_prop_id)::bigint <> (new.mate_prop_id)::bigint))) DO  INSERT INTO data_set_properties_history (id, ds_id, dstpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('data_set_property_id_seq'::regclass), old.ds_id, old.dstpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (controlled_vocabulary_terms t
      JOIN controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
     WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (materials m
      JOIN material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
     WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, now());
CREATE RULE data_set_relationships_delete AS
    ON DELETE TO data_set_relationships DO INSTEAD  DELETE FROM data_set_relationships_all
  WHERE ((((data_set_relationships_all.data_id_parent)::bigint = (old.data_id_parent)::bigint) AND ((data_set_relationships_all.data_id_child)::bigint = (old.data_id_child)::bigint)) AND ((data_set_relationships_all.relationship_id)::bigint = (old.relationship_id)::bigint));
CREATE RULE data_set_relationships_insert AS
    ON INSERT TO data_set_relationships DO INSTEAD  INSERT INTO data_set_relationships_all (data_id_parent, data_id_child, pers_id_author, relationship_id, ordinal, registration_timestamp, modification_timestamp)
  VALUES (new.data_id_parent, new.data_id_child, new.pers_id_author, new.relationship_id, new.ordinal, new.registration_timestamp, new.modification_timestamp);
CREATE RULE data_set_relationships_update AS
    ON UPDATE TO data_set_relationships DO INSTEAD  UPDATE data_set_relationships_all SET data_id_parent = new.data_id_parent, data_id_child = new.data_id_child, del_id = new.del_id, relationship_id = new.relationship_id, ordinal = new.ordinal, pers_id_author = new.pers_id_author, registration_timestamp = new.registration_timestamp, modification_timestamp = new.modification_timestamp
  WHERE ((((data_set_relationships_all.data_id_parent)::bigint = (new.data_id_parent)::bigint) AND ((data_set_relationships_all.data_id_child)::bigint = (new.data_id_child)::bigint)) AND ((data_set_relationships_all.relationship_id)::bigint = (new.relationship_id)::bigint));
CREATE RULE data_update AS
    ON UPDATE TO data DO INSTEAD  UPDATE data_all SET code = new.code, del_id = new.del_id, orig_del = new.orig_del, expe_id = new.expe_id, dast_id = new.dast_id, data_producer_code = new.data_producer_code, dsty_id = new.dsty_id, is_derived = new.is_derived, is_placeholder = new.is_placeholder, is_valid = new.is_valid, modification_timestamp = new.modification_timestamp, access_timestamp = new.access_timestamp, pers_id_registerer = new.pers_id_registerer, pers_id_modifier = new.pers_id_modifier, production_timestamp = new.production_timestamp, registration_timestamp = new.registration_timestamp, samp_id = new.samp_id, version = new.version
  WHERE ((data_all.id)::bigint = (new.id)::bigint);
CREATE RULE dataset_experiment_delete AS
    ON DELETE TO data_all
   WHERE ((old.expe_id IS NOT NULL) AND (old.samp_id IS NULL)) DO  UPDATE experiment_relationships_history SET valid_until_timestamp = now()
  WHERE ((((experiment_relationships_history.main_expe_id)::bigint = (old.expe_id)::bigint) AND ((experiment_relationships_history.data_id)::bigint = (old.id)::bigint)) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
CREATE RULE dataset_experiment_insert AS
    ON INSERT TO data_all
   WHERE ((new.expe_id IS NOT NULL) AND (new.samp_id IS NULL)) DO ( INSERT INTO experiment_relationships_history (id, main_expe_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('experiment_relationships_history_id_seq'::regclass), new.expe_id, 'OWNER'::text, new.id, new.code, new.pers_id_modifier, new.modification_timestamp);
 INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, expe_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.expe_id, ( SELECT experiments_all.perm_id
           FROM experiments_all
          WHERE ((experiments_all.id)::bigint = (new.expe_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);
CREATE RULE dataset_experiment_remove_update AS
    ON UPDATE TO data_all
   WHERE ((old.samp_id IS NULL) AND (new.samp_id IS NOT NULL)) DO ( UPDATE experiment_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((experiment_relationships_history.main_expe_id)::bigint = (old.expe_id)::bigint) AND ((experiment_relationships_history.data_id)::bigint = (old.id)::bigint)) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
 UPDATE data_set_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((data_set_relationships_history.main_data_id)::bigint = (old.id)::bigint) AND ((data_set_relationships_history.expe_id)::bigint = (old.expe_id)::bigint)) AND (data_set_relationships_history.valid_until_timestamp IS NULL));
);
CREATE RULE dataset_experiment_update AS
    ON UPDATE TO data_all
   WHERE ((((old.expe_id)::bigint <> (new.expe_id)::bigint) OR (old.samp_id IS NOT NULL)) AND (new.samp_id IS NULL)) DO ( UPDATE experiment_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((experiment_relationships_history.main_expe_id)::bigint = (old.expe_id)::bigint) AND ((experiment_relationships_history.data_id)::bigint = (old.id)::bigint)) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO experiment_relationships_history (id, main_expe_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('experiment_relationships_history_id_seq'::regclass), new.expe_id, 'OWNER'::text, new.id, new.code, new.pers_id_modifier, new.modification_timestamp);
 UPDATE data_set_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((data_set_relationships_history.main_data_id)::bigint = (old.id)::bigint) AND ((data_set_relationships_history.expe_id)::bigint = (old.expe_id)::bigint)) AND (data_set_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, expe_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.expe_id, ( SELECT experiments_all.perm_id
           FROM experiments_all
          WHERE ((experiments_all.id)::bigint = (new.expe_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);
CREATE RULE dataset_sample_delete AS
    ON DELETE TO data_all
   WHERE (old.samp_id IS NOT NULL) DO  UPDATE sample_relationships_history SET valid_until_timestamp = now()
  WHERE ((((sample_relationships_history.main_samp_id)::bigint = (old.samp_id)::bigint) AND ((sample_relationships_history.data_id)::bigint = (old.id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL));
CREATE RULE dataset_sample_insert AS
    ON INSERT TO data_all
   WHERE (new.samp_id IS NOT NULL) DO ( INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.samp_id, 'OWNER'::text, new.id, new.code, new.pers_id_modifier, new.modification_timestamp);
 INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.samp_id, ( SELECT samples_all.perm_id
           FROM samples_all
          WHERE ((samples_all.id)::bigint = (new.samp_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);
CREATE RULE dataset_sample_remove_update AS
    ON UPDATE TO data_all
   WHERE ((old.samp_id IS NOT NULL) AND (new.samp_id IS NULL)) DO ( UPDATE sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((sample_relationships_history.main_samp_id)::bigint = (old.samp_id)::bigint) AND ((sample_relationships_history.data_id)::bigint = (old.id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL));
 UPDATE data_set_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((data_set_relationships_history.main_data_id)::bigint = (old.id)::bigint) AND ((data_set_relationships_history.samp_id)::bigint = (old.samp_id)::bigint)) AND (data_set_relationships_history.valid_until_timestamp IS NULL));
);
CREATE RULE dataset_sample_update AS
    ON UPDATE TO data_all
   WHERE ((((old.samp_id)::bigint <> (new.samp_id)::bigint) OR (old.samp_id IS NULL)) AND (new.samp_id IS NOT NULL)) DO ( UPDATE sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((sample_relationships_history.main_samp_id)::bigint = (old.samp_id)::bigint) AND ((sample_relationships_history.data_id)::bigint = (old.id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, data_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.samp_id, 'OWNER'::text, new.id, new.code, new.pers_id_modifier, new.modification_timestamp);
 UPDATE data_set_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((data_set_relationships_history.main_data_id)::bigint = (old.id)::bigint) AND ((data_set_relationships_history.samp_id)::bigint = (old.samp_id)::bigint)) AND (data_set_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO data_set_relationships_history (id, main_data_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('data_set_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.samp_id, ( SELECT samples_all.perm_id
           FROM samples_all
          WHERE ((samples_all.id)::bigint = (new.samp_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);
CREATE RULE experiment_delete AS
    ON DELETE TO experiments DO INSTEAD  DELETE FROM experiments_all
  WHERE ((experiments_all.id)::bigint = (old.id)::bigint);
CREATE RULE experiment_insert AS
    ON INSERT TO experiments DO INSTEAD  INSERT INTO experiments_all (id, code, del_id, orig_del, exty_id, is_public, modification_timestamp, perm_id, pers_id_registerer, pers_id_modifier, proj_id, registration_timestamp, version)
  VALUES (new.id, new.code, new.del_id, new.orig_del, new.exty_id, new.is_public, new.modification_timestamp, new.perm_id, new.pers_id_registerer, new.pers_id_modifier, new.proj_id, new.registration_timestamp, new.version);
CREATE RULE experiment_project_delete AS
    ON DELETE TO experiments_all
   WHERE (old.proj_id IS NOT NULL) DO  UPDATE project_relationships_history SET valid_until_timestamp = now()
  WHERE ((((project_relationships_history.main_proj_id)::bigint = (old.proj_id)::bigint) AND ((project_relationships_history.expe_id)::bigint = (old.id)::bigint)) AND (project_relationships_history.valid_until_timestamp IS NULL));
CREATE RULE experiment_project_insert AS
    ON INSERT TO experiments_all
   WHERE (new.proj_id IS NOT NULL) DO ( INSERT INTO project_relationships_history (id, main_proj_id, relation_type, expe_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('project_relationships_history_id_seq'::regclass), new.proj_id, 'OWNER'::text, new.id, new.perm_id, new.pers_id_modifier, new.modification_timestamp);
 INSERT INTO experiment_relationships_history (id, main_expe_id, relation_type, proj_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('experiment_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.proj_id, ( SELECT projects.code
           FROM projects
          WHERE ((projects.id)::bigint = (new.proj_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);
CREATE RULE experiment_project_remove_update AS
    ON UPDATE TO experiments_all
   WHERE ((old.proj_id IS NOT NULL) AND (new.proj_id IS NULL)) DO ( UPDATE project_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((project_relationships_history.main_proj_id)::bigint = (old.proj_id)::bigint) AND ((project_relationships_history.expe_id)::bigint = (old.id)::bigint)) AND (project_relationships_history.valid_until_timestamp IS NULL));
 UPDATE experiment_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((experiment_relationships_history.main_expe_id)::bigint = (old.id)::bigint) AND ((experiment_relationships_history.proj_id)::bigint = (old.proj_id)::bigint)) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
);
CREATE RULE experiment_project_update AS
    ON UPDATE TO experiments_all
   WHERE ((((old.proj_id)::bigint <> (new.proj_id)::bigint) OR (old.proj_id IS NULL)) AND (new.proj_id IS NOT NULL)) DO ( UPDATE project_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((project_relationships_history.main_proj_id)::bigint = (old.proj_id)::bigint) AND ((project_relationships_history.expe_id)::bigint = (old.id)::bigint)) AND (project_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO project_relationships_history (id, main_proj_id, relation_type, expe_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('project_relationships_history_id_seq'::regclass), new.proj_id, 'OWNER'::text, new.id, new.perm_id, new.pers_id_modifier, new.modification_timestamp);
 UPDATE experiment_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((experiment_relationships_history.main_expe_id)::bigint = (old.id)::bigint) AND ((experiment_relationships_history.proj_id)::bigint = (old.proj_id)::bigint)) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO experiment_relationships_history (id, main_expe_id, relation_type, proj_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('experiment_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.proj_id, ( SELECT projects.code
           FROM projects
          WHERE ((projects.id)::bigint = (new.proj_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);
CREATE RULE experiment_properties_delete AS
    ON DELETE TO experiment_properties
   WHERE ((((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) OR (old.cvte_id IS NOT NULL)) OR (old.mate_prop_id IS NOT NULL)) DO  INSERT INTO experiment_properties_history (id, expe_id, etpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('experiment_property_id_seq'::regclass), old.expe_id, old.etpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (controlled_vocabulary_terms t
      JOIN controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
     WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (materials m
      JOIN material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
     WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, now());
CREATE RULE experiment_properties_update AS
    ON UPDATE TO experiment_properties
   WHERE (((((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) AND ((old.value)::text <> (new.value)::text)) OR ((old.cvte_id IS NOT NULL) AND ((old.cvte_id)::bigint <> (new.cvte_id)::bigint))) OR ((old.mate_prop_id IS NOT NULL) AND ((old.mate_prop_id)::bigint <> (new.mate_prop_id)::bigint))) DO  INSERT INTO experiment_properties_history (id, expe_id, etpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('experiment_property_id_seq'::regclass), old.expe_id, old.etpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (controlled_vocabulary_terms t
      JOIN controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
     WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (materials m
      JOIN material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
     WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, now());
CREATE RULE experiment_update AS
    ON UPDATE TO experiments DO INSTEAD  UPDATE experiments_all SET code = new.code, del_id = new.del_id, orig_del = new.orig_del, exty_id = new.exty_id, is_public = new.is_public, modification_timestamp = new.modification_timestamp, perm_id = new.perm_id, pers_id_registerer = new.pers_id_registerer, pers_id_modifier = new.pers_id_modifier, proj_id = new.proj_id, registration_timestamp = new.registration_timestamp, version = new.version
  WHERE ((experiments_all.id)::bigint = (new.id)::bigint);
CREATE RULE experiments_deleted_delete AS
    ON DELETE TO experiments_deleted DO INSTEAD  DELETE FROM experiments_all
  WHERE ((experiments_all.id)::bigint = (old.id)::bigint);
CREATE RULE experiments_deleted_update AS
    ON UPDATE TO experiments_deleted DO INSTEAD  UPDATE experiments_all SET del_id = new.del_id, orig_del = new.orig_del, modification_timestamp = new.modification_timestamp, version = new.version
  WHERE ((experiments_all.id)::bigint = (new.id)::bigint);
CREATE RULE material_properties_delete AS
    ON DELETE TO material_properties
   WHERE ((((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) OR (old.cvte_id IS NOT NULL)) OR (old.mate_prop_id IS NOT NULL)) DO  INSERT INTO material_properties_history (id, mate_id, mtpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('material_property_id_seq'::regclass), old.mate_id, old.mtpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (controlled_vocabulary_terms t
      JOIN controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
     WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (materials m
      JOIN material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
     WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, now());
CREATE RULE material_properties_update AS
    ON UPDATE TO material_properties
   WHERE (((((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) AND ((old.value)::text <> (new.value)::text)) OR ((old.cvte_id IS NOT NULL) AND ((old.cvte_id)::bigint <> (new.cvte_id)::bigint))) OR ((old.mate_prop_id IS NOT NULL) AND ((old.mate_prop_id)::bigint <> (new.mate_prop_id)::bigint))) DO  INSERT INTO material_properties_history (id, mate_id, mtpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('material_property_id_seq'::regclass), old.mate_id, old.mtpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (controlled_vocabulary_terms t
      JOIN controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
     WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (materials m
      JOIN material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
     WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, now());
CREATE RULE metaproject_assignments_delete AS
    ON DELETE TO metaproject_assignments DO INSTEAD  DELETE FROM metaproject_assignments_all
  WHERE ((metaproject_assignments_all.id)::bigint = (old.id)::bigint);
CREATE RULE metaproject_assignments_insert AS
    ON INSERT TO metaproject_assignments DO INSTEAD  INSERT INTO metaproject_assignments_all (id, mepr_id, expe_id, samp_id, data_id, mate_id, del_id, creation_date)
  VALUES (new.id, new.mepr_id, new.expe_id, new.samp_id, new.data_id, new.mate_id, new.del_id, new.creation_date);
CREATE RULE metaproject_assignments_update AS
    ON UPDATE TO metaproject_assignments DO INSTEAD  UPDATE metaproject_assignments_all SET id = new.id, mepr_id = new.mepr_id, expe_id = new.expe_id, samp_id = new.samp_id, data_id = new.data_id, mate_id = new.mate_id, del_id = new.del_id, creation_date = new.creation_date
  WHERE ((metaproject_assignments_all.id)::bigint = (new.id)::bigint);
CREATE RULE project_space_insert AS
    ON INSERT TO projects
   WHERE (new.space_id IS NOT NULL) DO  INSERT INTO project_relationships_history (id, main_proj_id, relation_type, space_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('project_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.space_id, ( SELECT spaces.code
           FROM spaces
          WHERE ((spaces.id)::bigint = (new.space_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
CREATE RULE project_space_remove_update AS
    ON UPDATE TO projects
   WHERE ((old.space_id IS NOT NULL) AND (new.space_id IS NULL)) DO  UPDATE project_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((project_relationships_history.main_proj_id)::bigint = (old.id)::bigint) AND ((project_relationships_history.space_id)::bigint = (old.space_id)::bigint)) AND (project_relationships_history.valid_until_timestamp IS NULL));
CREATE RULE project_space_update AS
    ON UPDATE TO projects
   WHERE ((((old.space_id)::bigint <> (new.space_id)::bigint) OR (old.space_id IS NULL)) AND (new.space_id IS NOT NULL)) DO ( UPDATE project_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((project_relationships_history.main_proj_id)::bigint = (old.id)::bigint) AND ((project_relationships_history.space_id)::bigint = (old.space_id)::bigint)) AND (project_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO project_relationships_history (id, main_proj_id, relation_type, space_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('project_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.space_id, ( SELECT spaces.code
           FROM spaces
          WHERE ((spaces.id)::bigint = (new.space_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);
CREATE RULE sample_container_delete AS
    ON DELETE TO samples_all
   WHERE (old.samp_id_part_of IS NOT NULL) DO  UPDATE sample_relationships_history SET valid_until_timestamp = now()
  WHERE (((((sample_relationships_history.main_samp_id)::bigint = (old.samp_id_part_of)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL)) AND ((sample_relationships_history.relation_type)::text = 'CONTAINER'::text));
CREATE RULE sample_container_insert AS
    ON INSERT TO samples_all
   WHERE (new.samp_id_part_of IS NOT NULL) DO ( INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.samp_id_part_of, 'CONTAINER'::text, new.id, new.perm_id, new.pers_id_modifier, new.modification_timestamp);
 INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.id, 'CONTAINED'::text, new.samp_id_part_of, ( SELECT samples_all.perm_id
           FROM samples_all
          WHERE ((samples_all.id)::bigint = (new.samp_id_part_of)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);
CREATE RULE sample_container_remove_update AS
    ON UPDATE TO samples_all
   WHERE ((old.samp_id_part_of IS NOT NULL) AND (new.samp_id_part_of IS NULL)) DO  UPDATE sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((((sample_relationships_history.main_samp_id)::bigint = (old.samp_id_part_of)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL)) AND ((sample_relationships_history.relation_type)::text = 'CONTAINER'::text)) OR (((((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.samp_id_part_of)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL)) AND ((sample_relationships_history.relation_type)::text = 'CONTAINED'::text)));
CREATE RULE sample_container_update AS
    ON UPDATE TO samples_all
   WHERE ((((old.samp_id_part_of)::bigint <> (new.samp_id_part_of)::bigint) OR (old.samp_id_part_of IS NULL)) AND (new.samp_id_part_of IS NOT NULL)) DO ( UPDATE sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((((sample_relationships_history.main_samp_id)::bigint = (old.samp_id_part_of)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL)) AND ((sample_relationships_history.relation_type)::text = 'CONTAINER'::text)) OR (((((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.samp_id_part_of)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL)) AND ((sample_relationships_history.relation_type)::text = 'CONTAINED'::text)));
 INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.samp_id_part_of, 'CONTAINER'::text, new.id, new.perm_id, new.pers_id_modifier, new.modification_timestamp);
 INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.id, 'CONTAINED'::text, new.samp_id_part_of, ( SELECT samples_all.perm_id
           FROM samples_all
          WHERE ((samples_all.id)::bigint = (new.samp_id_part_of)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);
CREATE RULE sample_experiment_delete AS
    ON DELETE TO samples_all
   WHERE (old.expe_id IS NOT NULL) DO  UPDATE experiment_relationships_history SET valid_until_timestamp = now()
  WHERE ((((experiment_relationships_history.main_expe_id)::bigint = (old.expe_id)::bigint) AND ((experiment_relationships_history.samp_id)::bigint = (old.id)::bigint)) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
CREATE RULE sample_experiment_insert AS
    ON INSERT TO samples_all
   WHERE (new.expe_id IS NOT NULL) DO ( INSERT INTO experiment_relationships_history (id, main_expe_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('experiment_relationships_history_id_seq'::regclass), new.expe_id, 'OWNER'::text, new.id, new.perm_id, new.pers_id_modifier, new.modification_timestamp);
 INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, expe_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.expe_id, ( SELECT experiments_all.perm_id
           FROM experiments_all
          WHERE ((experiments_all.id)::bigint = (new.expe_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);
CREATE RULE sample_experiment_remove_update AS
    ON UPDATE TO samples_all
   WHERE ((old.expe_id IS NOT NULL) AND (new.expe_id IS NULL)) DO ( UPDATE experiment_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((experiment_relationships_history.main_expe_id)::bigint = (old.expe_id)::bigint) AND ((experiment_relationships_history.samp_id)::bigint = (old.id)::bigint)) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
 UPDATE sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.expe_id)::bigint = (old.expe_id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL));
);
CREATE RULE sample_experiment_update AS
    ON UPDATE TO samples_all
   WHERE ((((old.expe_id)::bigint <> (new.expe_id)::bigint) OR (old.expe_id IS NULL)) AND (new.expe_id IS NOT NULL)) DO ( UPDATE experiment_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((experiment_relationships_history.main_expe_id)::bigint = (old.expe_id)::bigint) AND ((experiment_relationships_history.samp_id)::bigint = (old.id)::bigint)) AND (experiment_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO experiment_relationships_history (id, main_expe_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('experiment_relationships_history_id_seq'::regclass), new.expe_id, 'OWNER'::text, new.id, new.perm_id, new.pers_id_modifier, new.modification_timestamp);
 UPDATE sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.expe_id)::bigint = (old.expe_id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, expe_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.expe_id, ( SELECT experiments_all.perm_id
           FROM experiments_all
          WHERE ((experiments_all.id)::bigint = (new.expe_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);
CREATE RULE sample_insert AS
    ON INSERT TO samples DO INSTEAD  INSERT INTO samples_all (id, code, del_id, orig_del, expe_id, modification_timestamp, perm_id, pers_id_registerer, pers_id_modifier, registration_timestamp, samp_id_part_of, saty_id, space_id, version)
  VALUES (new.id, new.code, new.del_id, new.orig_del, new.expe_id, new.modification_timestamp, new.perm_id, new.pers_id_registerer, new.pers_id_modifier, new.registration_timestamp, new.samp_id_part_of, new.saty_id, new.space_id, new.version);
CREATE RULE sample_parent_child_delete AS
    ON DELETE TO sample_relationships_all
   WHERE (old.del_id IS NULL) DO  UPDATE sample_relationships_history SET valid_until_timestamp = now()
  WHERE (((((sample_relationships_history.main_samp_id)::bigint = (old.sample_id_parent)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.sample_id_child)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL)) OR ((((sample_relationships_history.main_samp_id)::bigint = (old.sample_id_child)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.sample_id_parent)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL)));
CREATE RULE sample_parent_child_insert AS
    ON INSERT TO sample_relationships_all
   WHERE (new.del_id IS NULL) DO ( INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.sample_id_parent, 'PARENT'::text, new.sample_id_child, ( SELECT samples_all.perm_id
           FROM samples_all
          WHERE ((samples_all.id)::bigint = (new.sample_id_child)::bigint)), new.pers_id_author, new.modification_timestamp);
 INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.sample_id_child, 'CHILD'::text, new.sample_id_parent, ( SELECT samples_all.perm_id
           FROM samples_all
          WHERE ((samples_all.id)::bigint = (new.sample_id_parent)::bigint)), new.pers_id_author, new.modification_timestamp);
);
CREATE RULE sample_parent_child_revert_update AS
    ON UPDATE TO sample_relationships_all
   WHERE ((new.del_id IS NULL) AND (old.del_id IS NOT NULL)) DO ( INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.sample_id_parent, 'PARENT'::text, new.sample_id_child, ( SELECT samples_all.perm_id
           FROM samples_all
          WHERE ((samples_all.id)::bigint = (new.sample_id_child)::bigint)), new.pers_id_author, new.modification_timestamp);
 INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, samp_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.sample_id_child, 'CHILD'::text, new.sample_id_parent, ( SELECT samples_all.perm_id
           FROM samples_all
          WHERE ((samples_all.id)::bigint = (new.sample_id_parent)::bigint)), new.pers_id_author, new.modification_timestamp);
);
CREATE RULE sample_parent_child_update AS
    ON UPDATE TO sample_relationships_all
   WHERE ((new.del_id IS NOT NULL) AND (old.del_id IS NULL)) DO  UPDATE sample_relationships_history SET valid_until_timestamp = now()
  WHERE (((((sample_relationships_history.main_samp_id)::bigint = (old.sample_id_parent)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.sample_id_child)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL)) OR ((((sample_relationships_history.main_samp_id)::bigint = (old.sample_id_child)::bigint) AND ((sample_relationships_history.samp_id)::bigint = (old.sample_id_parent)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL)));
CREATE RULE sample_properties_delete AS
    ON DELETE TO sample_properties
   WHERE (((((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) OR (old.cvte_id IS NOT NULL)) OR (old.mate_prop_id IS NOT NULL)) AND (( SELECT samples_all.del_id
           FROM samples_all
          WHERE ((samples_all.id)::bigint = (old.samp_id)::bigint)) IS NULL)) DO  INSERT INTO sample_properties_history (id, samp_id, stpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('sample_property_id_seq'::regclass), old.samp_id, old.stpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (controlled_vocabulary_terms t
      JOIN controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
     WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (materials m
      JOIN material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
     WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, now());
CREATE RULE sample_properties_update AS
    ON UPDATE TO sample_properties
   WHERE (((((old.value IS NOT NULL) AND (decode(replace("substring"((old.value)::text, 1, 1), '\'::text, '\\'::text), 'escape'::text) <> '\xefbfbd'::bytea)) AND ((old.value)::text <> (new.value)::text)) OR ((old.cvte_id IS NOT NULL) AND ((old.cvte_id)::bigint <> (new.cvte_id)::bigint))) OR ((old.mate_prop_id IS NOT NULL) AND ((old.mate_prop_id)::bigint <> (new.mate_prop_id)::bigint))) DO  INSERT INTO sample_properties_history (id, samp_id, stpt_id, value, vocabulary_term, material, pers_id_author, valid_from_timestamp, valid_until_timestamp)
  VALUES (nextval('sample_property_id_seq'::regclass), old.samp_id, old.stpt_id, old.value, ( SELECT ((((t.code)::text || ' ['::text) || (v.code)::text) || ']'::text)
           FROM (controlled_vocabulary_terms t
      JOIN controlled_vocabularies v ON (((t.covo_id)::bigint = (v.id)::bigint)))
     WHERE ((t.id)::bigint = (old.cvte_id)::bigint)), ( SELECT ((((m.code)::text || ' ['::text) || (mt.code)::text) || ']'::text)
           FROM (materials m
      JOIN material_types mt ON (((m.maty_id)::bigint = (mt.id)::bigint)))
     WHERE ((m.id)::bigint = (old.mate_prop_id)::bigint)), old.pers_id_author, old.modification_timestamp, now());
CREATE RULE sample_relationships_delete AS
    ON DELETE TO sample_relationships DO INSTEAD  DELETE FROM sample_relationships_all
  WHERE ((sample_relationships_all.id)::bigint = (old.id)::bigint);
CREATE RULE sample_relationships_insert AS
    ON INSERT TO sample_relationships DO INSTEAD  INSERT INTO sample_relationships_all (id, sample_id_parent, relationship_id, sample_id_child, pers_id_author, registration_timestamp, modification_timestamp)
  VALUES (new.id, new.sample_id_parent, new.relationship_id, new.sample_id_child, new.pers_id_author, new.registration_timestamp, new.modification_timestamp);
CREATE RULE sample_relationships_update AS
    ON UPDATE TO sample_relationships DO INSTEAD  UPDATE sample_relationships_all SET sample_id_parent = new.sample_id_parent, relationship_id = new.relationship_id, sample_id_child = new.sample_id_child, del_id = new.del_id, pers_id_author = new.pers_id_author, registration_timestamp = new.registration_timestamp, modification_timestamp = new.modification_timestamp
  WHERE ((sample_relationships_all.id)::bigint = (new.id)::bigint);
CREATE RULE sample_space_insert AS
    ON INSERT TO samples_all
   WHERE ((new.expe_id IS NULL) AND (new.space_id IS NOT NULL)) DO  INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, space_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.space_id, ( SELECT spaces.code
           FROM spaces
          WHERE ((spaces.id)::bigint = (new.space_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
CREATE RULE sample_space_remove_update AS
    ON UPDATE TO samples_all
   WHERE ((old.space_id IS NOT NULL) AND ((new.space_id IS NULL) OR ((old.expe_id IS NULL) AND (new.expe_id IS NOT NULL)))) DO  UPDATE sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.space_id)::bigint = (old.space_id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL));
CREATE RULE sample_space_update AS
    ON UPDATE TO samples_all
   WHERE ((((((old.space_id)::bigint <> (new.space_id)::bigint) OR (old.space_id IS NULL)) OR (old.expe_id IS NOT NULL)) AND (new.space_id IS NOT NULL)) AND (new.expe_id IS NULL)) DO ( UPDATE sample_relationships_history SET valid_until_timestamp = new.modification_timestamp
  WHERE ((((sample_relationships_history.main_samp_id)::bigint = (old.id)::bigint) AND ((sample_relationships_history.space_id)::bigint = (old.space_id)::bigint)) AND (sample_relationships_history.valid_until_timestamp IS NULL));
 INSERT INTO sample_relationships_history (id, main_samp_id, relation_type, space_id, entity_perm_id, pers_id_author, valid_from_timestamp)
  VALUES (nextval('sample_relationships_history_id_seq'::regclass), new.id, 'OWNED'::text, new.space_id, ( SELECT spaces.code
           FROM spaces
          WHERE ((spaces.id)::bigint = (new.space_id)::bigint)), new.pers_id_modifier, new.modification_timestamp);
);
CREATE RULE sample_update AS
    ON UPDATE TO samples DO INSTEAD  UPDATE samples_all SET code = new.code, del_id = new.del_id, orig_del = new.orig_del, expe_id = new.expe_id, modification_timestamp = new.modification_timestamp, perm_id = new.perm_id, pers_id_registerer = new.pers_id_registerer, pers_id_modifier = new.pers_id_modifier, registration_timestamp = new.registration_timestamp, samp_id_part_of = new.samp_id_part_of, saty_id = new.saty_id, space_id = new.space_id, version = new.version
  WHERE ((samples_all.id)::bigint = (new.id)::bigint);
CREATE CONSTRAINT TRIGGER check_created_or_modified_data_set_owner_is_alive AFTER INSERT OR UPDATE ON data_all DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE PROCEDURE check_created_or_modified_data_set_owner_is_alive();
CREATE CONSTRAINT TRIGGER check_created_or_modified_sample_owner_is_alive AFTER INSERT OR UPDATE ON samples_all DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE PROCEDURE check_created_or_modified_sample_owner_is_alive();
CREATE CONSTRAINT TRIGGER check_deletion_consistency_on_experiment_deletion AFTER UPDATE ON experiments_all DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE PROCEDURE check_deletion_consistency_on_experiment_deletion();
CREATE CONSTRAINT TRIGGER check_deletion_consistency_on_sample_deletion AFTER UPDATE ON samples_all DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE PROCEDURE check_deletion_consistency_on_sample_deletion();
CREATE TRIGGER controlled_vocabulary_check BEFORE INSERT OR UPDATE ON property_types FOR EACH ROW EXECUTE PROCEDURE controlled_vocabulary_check();
CREATE TRIGGER data_exp_or_sample_link_check BEFORE INSERT OR UPDATE ON data_all FOR EACH ROW EXECUTE PROCEDURE data_exp_or_sample_link_check();
CREATE TRIGGER data_set_property_with_material_data_type_check BEFORE INSERT OR UPDATE ON data_set_properties FOR EACH ROW EXECUTE PROCEDURE data_set_property_with_material_data_type_check();
CREATE TRIGGER experiment_property_with_material_data_type_check BEFORE INSERT OR UPDATE ON experiment_properties FOR EACH ROW EXECUTE PROCEDURE experiment_property_with_material_data_type_check();
CREATE TRIGGER external_data_storage_format_check BEFORE INSERT OR UPDATE ON external_data FOR EACH ROW EXECUTE PROCEDURE external_data_storage_format_check();
CREATE TRIGGER material_property_with_material_data_type_check BEFORE INSERT OR UPDATE ON material_properties FOR EACH ROW EXECUTE PROCEDURE material_property_with_material_data_type_check();
CREATE TRIGGER preserve_deletion_consistency_on_data_set_relationships BEFORE UPDATE ON data_set_relationships_all FOR EACH ROW EXECUTE PROCEDURE preserve_deletion_consistency_on_data_set_relationships();
CREATE TRIGGER preserve_deletion_consistency_on_sample_relationships BEFORE UPDATE ON sample_relationships_all FOR EACH ROW EXECUTE PROCEDURE preserve_deletion_consistency_on_sample_relationships();
CREATE TRIGGER sample_fill_code_unique_check BEFORE INSERT OR UPDATE ON samples_all FOR EACH ROW EXECUTE PROCEDURE sample_fill_code_unique_check();
CREATE TRIGGER sample_fill_subcode_unique_check BEFORE INSERT OR UPDATE ON samples_all FOR EACH ROW EXECUTE PROCEDURE sample_fill_subcode_unique_check();
CREATE TRIGGER sample_property_with_material_data_type_check BEFORE INSERT OR UPDATE ON sample_properties FOR EACH ROW EXECUTE PROCEDURE sample_property_with_material_data_type_check();
CREATE TRIGGER sample_type_fill_subcode_unique_check AFTER UPDATE ON sample_types FOR EACH ROW EXECUTE PROCEDURE sample_type_fill_subcode_unique_check();
ALTER TABLE ONLY authorization_groups
    ADD CONSTRAINT ag_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY authorization_group_persons
    ADD CONSTRAINT agp_ag_fk FOREIGN KEY (ag_id) REFERENCES authorization_groups(id);
ALTER TABLE ONLY authorization_group_persons
    ADD CONSTRAINT agp_pers_fk FOREIGN KEY (pers_id) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_cont_fk FOREIGN KEY (exac_id) REFERENCES attachment_contents(id);
ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments_all(id);
ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_proj_fk FOREIGN KEY (proj_id) REFERENCES projects(id);
ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_samp_fk FOREIGN KEY (samp_id) REFERENCES samples_all(id);
ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_covo_fk FOREIGN KEY (covo_id) REFERENCES controlled_vocabularies(id);
ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY data_all
    ADD CONSTRAINT data_dast_fk FOREIGN KEY (dast_id) REFERENCES data_stores(id);
ALTER TABLE ONLY data_all
    ADD CONSTRAINT data_del_fk FOREIGN KEY (del_id) REFERENCES deletions(id);
ALTER TABLE ONLY data_all
    ADD CONSTRAINT data_dsty_fk FOREIGN KEY (dsty_id) REFERENCES data_set_types(id);
ALTER TABLE ONLY data_all
    ADD CONSTRAINT data_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments_all(id);
ALTER TABLE ONLY data_all
    ADD CONSTRAINT data_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY data_all
    ADD CONSTRAINT data_pers_fk_mod FOREIGN KEY (pers_id_modifier) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY data_all
    ADD CONSTRAINT data_samp_fk FOREIGN KEY (samp_id) REFERENCES samples_all(id);
ALTER TABLE ONLY data_set_relationships_all
    ADD CONSTRAINT data_set_relationships_pers_fk FOREIGN KEY (pers_id_author) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY data_set_relationships_history
    ADD CONSTRAINT datarelh_data_fk FOREIGN KEY (data_id) REFERENCES data_all(id) ON DELETE SET NULL;
ALTER TABLE ONLY data_set_relationships_history
    ADD CONSTRAINT datarelh_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments_all(id) ON DELETE SET NULL;
ALTER TABLE ONLY data_set_relationships_history
    ADD CONSTRAINT datarelh_main_data_fk FOREIGN KEY (main_data_id) REFERENCES data_all(id) ON DELETE CASCADE;
ALTER TABLE ONLY data_set_relationships_history
    ADD CONSTRAINT datarelh_samp_fk FOREIGN KEY (samp_id) REFERENCES samples_all(id) ON DELETE SET NULL;
ALTER TABLE ONLY deletions
    ADD CONSTRAINT del_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);
ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_ds_fk FOREIGN KEY (ds_id) REFERENCES data_all(id);
ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_dstpt_fk FOREIGN KEY (dstpt_id) REFERENCES data_set_type_property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);
ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY data_set_properties_history
    ADD CONSTRAINT dsprh_ds_fk FOREIGN KEY (ds_id) REFERENCES data_all(id) ON DELETE CASCADE;
ALTER TABLE ONLY data_set_properties_history
    ADD CONSTRAINT dsprh_dstpt_fk FOREIGN KEY (dstpt_id) REFERENCES data_set_type_property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY data_set_relationships_all
    ADD CONSTRAINT dsre_data_fk_child FOREIGN KEY (data_id_child) REFERENCES data_all(id) ON DELETE CASCADE;
ALTER TABLE ONLY data_set_relationships_all
    ADD CONSTRAINT dsre_data_fk_parent FOREIGN KEY (data_id_parent) REFERENCES data_all(id) ON DELETE CASCADE;
ALTER TABLE ONLY data_set_relationships_all
    ADD CONSTRAINT dsre_data_fk_relationship FOREIGN KEY (relationship_id) REFERENCES relationship_types(id);
ALTER TABLE ONLY data_set_relationships_all
    ADD CONSTRAINT dsre_del_fk FOREIGN KEY (del_id) REFERENCES deletions(id);
ALTER TABLE ONLY data_store_service_data_set_types
    ADD CONSTRAINT dssdst_ds_fk FOREIGN KEY (data_store_service_id) REFERENCES data_store_services(id) ON DELETE CASCADE;
ALTER TABLE ONLY data_store_service_data_set_types
    ADD CONSTRAINT dssdst_dst_fk FOREIGN KEY (data_set_type_id) REFERENCES data_set_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY data_store_services
    ADD CONSTRAINT dsse_ds_fk FOREIGN KEY (data_store_id) REFERENCES data_stores(id) ON DELETE CASCADE;
ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_dsty_fk FOREIGN KEY (dsty_id) REFERENCES data_set_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_script_fk FOREIGN KEY (script_id) REFERENCES scripts(id);
ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT dsty_script_fk FOREIGN KEY (validation_script_id) REFERENCES scripts(id);
ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_exty_fk FOREIGN KEY (exty_id) REFERENCES experiment_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_script_fk FOREIGN KEY (script_id) REFERENCES scripts(id);
ALTER TABLE ONLY events
    ADD CONSTRAINT evnt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_cvte_fk FOREIGN KEY (cvte_id_stor_fmt) REFERENCES controlled_vocabulary_terms(id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_cvte_stored_on_fk FOREIGN KEY (cvte_id_store) REFERENCES controlled_vocabulary_terms(id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_data_fk FOREIGN KEY (data_id) REFERENCES data_all(id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_ffty_fk FOREIGN KEY (ffty_id) REFERENCES file_format_types(id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_loty_fk FOREIGN KEY (loty_id) REFERENCES locator_types(id);
ALTER TABLE ONLY experiments_all
    ADD CONSTRAINT expe_del_fk FOREIGN KEY (del_id) REFERENCES deletions(id);
ALTER TABLE ONLY experiments_all
    ADD CONSTRAINT expe_exty_fk FOREIGN KEY (exty_id) REFERENCES experiment_types(id);
ALTER TABLE ONLY experiments_all
    ADD CONSTRAINT expe_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY experiments_all
    ADD CONSTRAINT expe_pers_fk_mod FOREIGN KEY (pers_id_modifier) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY experiments_all
    ADD CONSTRAINT expe_proj_fk FOREIGN KEY (proj_id) REFERENCES projects(id);
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_etpt_fk FOREIGN KEY (etpt_id) REFERENCES experiment_type_property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments_all(id);
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY experiment_properties_history
    ADD CONSTRAINT exprh_etpt_fk FOREIGN KEY (etpt_id) REFERENCES experiment_type_property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY experiment_properties_history
    ADD CONSTRAINT exprh_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments_all(id) ON DELETE CASCADE;
ALTER TABLE ONLY experiment_relationships_history
    ADD CONSTRAINT exrelh_data_fk FOREIGN KEY (data_id) REFERENCES data_all(id) ON DELETE SET NULL;
ALTER TABLE ONLY experiment_relationships_history
    ADD CONSTRAINT exrelh_main_expe_fk FOREIGN KEY (main_expe_id) REFERENCES experiments_all(id) ON DELETE CASCADE;
ALTER TABLE ONLY experiment_relationships_history
    ADD CONSTRAINT exrelh_proj_fk FOREIGN KEY (proj_id) REFERENCES projects(id) ON DELETE SET NULL;
ALTER TABLE ONLY experiment_relationships_history
    ADD CONSTRAINT exrelh_samp_fk FOREIGN KEY (samp_id) REFERENCES samples_all(id) ON DELETE SET NULL;
ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_script_fk FOREIGN KEY (validation_script_id) REFERENCES scripts(id);
ALTER TABLE ONLY filters
    ADD CONSTRAINT filt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY grid_custom_columns
    ADD CONSTRAINT grid_custom_columns_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY link_data
    ADD CONSTRAINT lnda_data_fk FOREIGN KEY (data_id) REFERENCES data_all(id) ON DELETE CASCADE;
ALTER TABLE ONLY link_data
    ADD CONSTRAINT lnda_edms_fk FOREIGN KEY (edms_id) REFERENCES external_data_management_systems(id);
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_mate_fk FOREIGN KEY (mate_id) REFERENCES materials(id);
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_mtpt_fk FOREIGN KEY (mtpt_id) REFERENCES material_type_property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY material_properties_history
    ADD CONSTRAINT maprh_mate_fk FOREIGN KEY (mate_id) REFERENCES materials(id) ON DELETE CASCADE;
ALTER TABLE ONLY material_properties_history
    ADD CONSTRAINT maprh_mtpt_fk FOREIGN KEY (mtpt_id) REFERENCES material_type_property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_maty_fk FOREIGN KEY (maty_id) REFERENCES material_types(id);
ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_script_fk FOREIGN KEY (validation_script_id) REFERENCES scripts(id);
ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_data_id_fk FOREIGN KEY (data_id) REFERENCES data_all(id) ON DELETE CASCADE;
ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_del_id_fk FOREIGN KEY (del_id) REFERENCES deletions(id);
ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_expe_id_fk FOREIGN KEY (expe_id) REFERENCES experiments_all(id) ON DELETE CASCADE;
ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_mate_id_fk FOREIGN KEY (mate_id) REFERENCES materials(id) ON DELETE CASCADE;
ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_mepr_id_fk FOREIGN KEY (mepr_id) REFERENCES metaprojects(id) ON DELETE CASCADE;
ALTER TABLE ONLY metaproject_assignments_all
    ADD CONSTRAINT metaproject_assignments_all_samp_id_fk FOREIGN KEY (samp_id) REFERENCES samples_all(id) ON DELETE CASCADE;
ALTER TABLE ONLY metaprojects
    ADD CONSTRAINT metaprojects_owner_fk FOREIGN KEY (owner) REFERENCES persons(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_maty_fk FOREIGN KEY (maty_id) REFERENCES material_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_script_fk FOREIGN KEY (script_id) REFERENCES scripts(id);
ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id);
ALTER TABLE ONLY post_registration_dataset_queue
    ADD CONSTRAINT prdq_ds_fk FOREIGN KEY (ds_id) REFERENCES data_all(id) ON DELETE CASCADE;
ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pers_fk_leader FOREIGN KEY (pers_id_leader) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pers_fk_mod FOREIGN KEY (pers_id_modifier) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id);
ALTER TABLE ONLY project_relationships_history
    ADD CONSTRAINT prrelh_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments_all(id) ON DELETE SET NULL;
ALTER TABLE ONLY project_relationships_history
    ADD CONSTRAINT prrelh_main_proj_fk FOREIGN KEY (main_proj_id) REFERENCES projects(id) ON DELETE CASCADE;
ALTER TABLE ONLY project_relationships_history
    ADD CONSTRAINT prrelh_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id) ON DELETE SET NULL;
ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_covo_fk FOREIGN KEY (covo_id) REFERENCES controlled_vocabularies(id);
ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_daty_fk FOREIGN KEY (daty_id) REFERENCES data_types(id);
ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_maty_fk FOREIGN KEY (maty_prop_id) REFERENCES material_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY queries
    ADD CONSTRAINT quer_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_ag_fk_grantee FOREIGN KEY (ag_id_grantee) REFERENCES authorization_groups(id) ON DELETE CASCADE;
ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pers_fk_grantee FOREIGN KEY (pers_id_grantee) REFERENCES persons(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id) ON DELETE CASCADE;
ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_del_fk FOREIGN KEY (del_id) REFERENCES deletions(id);
ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments_all(id);
ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_pers_fk_mod FOREIGN KEY (pers_id_modifier) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_samp_fk_part_of FOREIGN KEY (samp_id_part_of) REFERENCES samples_all(id);
ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_saty_fk FOREIGN KEY (saty_id) REFERENCES sample_types(id);
ALTER TABLE ONLY samples_all
    ADD CONSTRAINT samp_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id);
ALTER TABLE ONLY sample_relationships_all
    ADD CONSTRAINT sample_relationships_pers_fk FOREIGN KEY (pers_id_author) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY sample_relationships_history
    ADD CONSTRAINT samprelh_data_fk FOREIGN KEY (data_id) REFERENCES data_all(id) ON DELETE SET NULL;
ALTER TABLE ONLY sample_relationships_history
    ADD CONSTRAINT samprelh_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments_all(id) ON DELETE SET NULL;
ALTER TABLE ONLY sample_relationships_history
    ADD CONSTRAINT samprelh_main_samp_fk FOREIGN KEY (main_samp_id) REFERENCES samples_all(id) ON DELETE CASCADE;
ALTER TABLE ONLY sample_relationships_history
    ADD CONSTRAINT samprelh_samp_fk FOREIGN KEY (samp_id) REFERENCES samples_all(id) ON DELETE SET NULL;
ALTER TABLE ONLY sample_relationships_history
    ADD CONSTRAINT samprelh_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id) ON DELETE SET NULL;
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_samp_fk FOREIGN KEY (samp_id) REFERENCES samples_all(id);
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_stpt_fk FOREIGN KEY (stpt_id) REFERENCES sample_type_property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY sample_properties_history
    ADD CONSTRAINT saprh_samp_fk FOREIGN KEY (samp_id) REFERENCES samples_all(id) ON DELETE CASCADE;
ALTER TABLE ONLY sample_properties_history
    ADD CONSTRAINT saprh_stpt_fk FOREIGN KEY (stpt_id) REFERENCES sample_type_property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY sample_relationships_all
    ADD CONSTRAINT sare_data_fk_child FOREIGN KEY (sample_id_child) REFERENCES samples_all(id) ON DELETE CASCADE;
ALTER TABLE ONLY sample_relationships_all
    ADD CONSTRAINT sare_data_fk_parent FOREIGN KEY (sample_id_parent) REFERENCES samples_all(id) ON DELETE CASCADE;
ALTER TABLE ONLY sample_relationships_all
    ADD CONSTRAINT sare_data_fk_relationship FOREIGN KEY (relationship_id) REFERENCES relationship_types(id);
ALTER TABLE ONLY sample_relationships_all
    ADD CONSTRAINT sare_del_fk FOREIGN KEY (del_id) REFERENCES deletions(id);
ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_script_fk FOREIGN KEY (validation_script_id) REFERENCES scripts(id);
ALTER TABLE ONLY scripts
    ADD CONSTRAINT scri_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY spaces
    ADD CONSTRAINT space_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_saty_fk FOREIGN KEY (saty_id) REFERENCES sample_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_script_fk FOREIGN KEY (script_id) REFERENCES scripts(id);
REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;

