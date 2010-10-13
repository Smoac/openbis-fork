--
-- PostgreSQL database dump
--

-- Started on 2010-08-18 10:13:10 CEST

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

--
-- TOC entry 2058 (class 0 OID 566004)
-- Dependencies: 1704
-- Data for Name: experiment_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--
INSERT INTO experiment_types (id, code, description, dbin_id, modification_timestamp) VALUES (1, 'CINA_EXP_TYPE', 'Generic Experiment Type', 1, '2010-03-18 08:37:38.173356+01');
-- Completed on 2010-08-18 10:19:08 CEST

--
-- TOC entry 2064 (class 0 OID 566149)
-- Dependencies: 1723
-- Data for Name: sample_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--
INSERT INTO sample_types (id, code, description, dbin_id, is_listable, generated_from_depth, part_of_depth, modification_timestamp, is_auto_generated_code, generated_code_prefix, is_subcode_unique) VALUES (1, 'ORIGINAL', 'The sample for an experiment', 1, true, 1, 0, '2010-03-18 10:09:48.073644+01', false, 'ORIGINAL', false);
INSERT INTO sample_types (id, code, description, dbin_id, is_listable, generated_from_depth, part_of_depth, modification_timestamp, is_auto_generated_code, generated_code_prefix, is_subcode_unique) VALUES (2, 'GRID_PREP', 'Grid biochemistry and preparation', 1, true, 1, 0, '2010-06-04 14:44:53.706263+02', false, 'EM-PREP', false);
INSERT INTO sample_types (id, code, description, dbin_id, is_listable, generated_from_depth, part_of_depth, modification_timestamp, is_auto_generated_code, generated_code_prefix, is_subcode_unique) VALUES (3, 'GRID_REPLICA', 'A replica of a grid template', 1, true, 1, 0, '2010-06-04 14:46:38.886039+02', false, 'REPLICA', false);
-- Completed on 2010-08-18 10:13:10 CEST

--
-- TOC entry 2058 (class 0 OID 566095)
-- Dependencies: 1716
-- Data for Name: data_set_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--
INSERT INTO data_set_types (id, code, description, dbin_id, modification_timestamp, main_ds_pattern, main_ds_path) VALUES (1, 'UNKNOWN', 'Unknown', 1, '2010-03-18 08:33:32.470674+01', NULL, NULL);
INSERT INTO data_set_types (id, code, description, dbin_id, modification_timestamp, main_ds_pattern, main_ds_path) VALUES (2, 'BUNDLE', 'Bundle Data Set', 1, '2010-06-04 15:40:58.882161+02', '.*.bundle', NULL);
INSERT INTO data_set_types (id, code, description, dbin_id, modification_timestamp, main_ds_pattern, main_ds_path) VALUES (3, 'RAW_IMAGES', 'Raw Images', 1, '2010-06-04 16:08:40.980157+02', NULL, NULL);
INSERT INTO data_set_types (id, code, description, dbin_id, modification_timestamp, main_ds_pattern, main_ds_path) VALUES (4, 'METADATA', 'Metadata', 1, '2010-06-04 16:08:40.980157+02', 'metadata.xml', 'original');
INSERT INTO data_set_types (id, code, description, dbin_id, modification_timestamp, main_ds_pattern, main_ds_path) VALUES (5, 'IMAGE', 'Image', 1, '2010-06-04 16:06:40.980157+02', '.*.png', 'original/Representations');
INSERT INTO data_set_types (id, code, description, dbin_id, modification_timestamp, main_ds_pattern, main_ds_path) VALUES (6, 'ANALYSIS', 'Analysis', 1, '2010-06-04 16:07:04.556718+02', NULL, NULL);
-- Completed on 2010-08-18 10:19:39 CEST


--
-- TOC entry 2068 (class 0 OID 566117)
-- Dependencies: 1719
-- Data for Name: property_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (1, 'DESCRIPTION', 'A Description', 'Description', 1, '2010-03-18 08:33:32.470674+01', 1, NULL, false, false, 1, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (2, 'LAB_ID', 'ID for the Lab in the external contact database.', 'Lab ID', 1, '2010-03-18 10:11:21.361947+01', 2, NULL, false, false, 1, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (3, 'CREATION_DATE', 'Creation date of this sample.', 'Creation Date', 6, '2010-03-23 17:42:30.258666+01', 2, NULL, false, false, 1, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (5, 'RATING', 'Rating of data quality', 'Rating', 3, '2010-06-10 09:46:29.093055+02', 2, NULL, false, false, 1, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (6, 'OPERATOR', 'Operator of the measurement device', 'Operator', 1, '2010-06-10 12:25:40.888407+02', 2, NULL, false, false, 1, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (4, 'MISC', 'Miscelleanous Property Data', 'All', 2, '2010-06-10 09:30:45.922177+02', 2, NULL, false, false, 1, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (7, 'MICROSCOPE', 'Which microscope was used ', 'Microscope', 1, '2010-08-17 17:38:41.944393+02', 2, NULL, false, false, 1, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (8, 'SIZEX', 'The X size of the image', 'Size X', 4, '2010-08-17 17:39:02.211289+02', 2, NULL, false, false, 1, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (9, 'SIZEY', 'The Y size of the image.', 'Size Y', 4, '2010-08-17 17:39:19.720074+02', 2, NULL, false, false, 1, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (10, 'SIZEZ', 'The Z size of the image.', 'Size Z', 4, '2010-08-17 17:39:39.614258+02', 2, NULL, false, false, 1, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (11, 'DIMENSIONX', 'The X dimension of the image.', 'Dimension X', 3, '2010-08-17 17:39:59.787031+02', 2, NULL, false, false, 1, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (12, 'DIMENSIONY', 'The Y dimension of the image.', 'Dimension Y', 3, '2010-08-17 17:40:15.518858+02', 2, NULL, false, false, 1, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (13, 'DIMENSIONZ', 'The Z dimension of the image.', 'Dimension Z', 3, '2010-08-17 17:40:31.43621+02', 2, NULL, false, false, 1, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (14, 'MIN', 'The min value.', 'Min', 4, '2010-08-17 17:40:50.414389+02', 2, NULL, false, false, 1, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (15, 'MAX', 'The Max value.', 'Max', 4, '2010-08-17 17:41:05.048801+02', 2, NULL, false, false, 1, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (16, 'ANNOTATION', 'A user annotation of the image.', 'Annotation', 2, '2010-08-17 17:41:27.830224+02', 2, NULL, false, false, 1, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (17, 'STACKFLAG', 'Is the image a stack?', 'Is Stack?', 5, '2010-08-17 17:41:54.744874+02', 2, NULL, false, false, 1, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (18, 'COLORFLAG', 'Is the image a color image?', 'Is Color?', 5, '2010-08-17 17:42:10.974678+02', 2, NULL, false, false, 1, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) VALUES (19, 'DATA-TYPE', 'The data type.', 'Data Type', 1, '2010-08-17 17:42:50.359753+02', 2, NULL, false, false, 1, NULL);
-- Completed on 2010-08-18 11:27:57 CEST

--
-- TOC entry 2066 (class 0 OID 566161)
-- Dependencies: 1724
-- Data for Name: sample_type_property_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--
INSERT INTO sample_type_property_types (id, saty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, is_displayed, ordinal, section) VALUES (1, 1, 3, false, false, 2, '2010-03-23 17:42:50.965274+01', true, 1, NULL);
-- Completed on 2010-08-18 14:34:43 CEST

--
-- TOC entry 2065 (class 0 OID 566179)
-- Dependencies: 1726
-- Data for Name: data_set_type_property_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (2, 4, 5, false, false, 2, '2010-06-10 09:54:14.563261+02', 2, NULL);
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (3, 4, 6, false, false, 2, '2010-06-10 12:26:44.840772+02', 1, NULL);
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (4, 3, 1, false, false, 2, '2010-06-10 18:09:13.483141+02', 1, NULL);
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (5, 4, 7, false, false, 2, '2010-08-17 17:45:53.236047+02', 3, NULL);
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (6, 4, 8, false, false, 2, '2010-08-17 17:54:43.847962+02', 4, 'Size');
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (7, 4, 9, false, false, 2, '2010-08-17 17:55:06.438111+02', 5, 'Size');
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (8, 4, 10, false, false, 2, '2010-08-17 17:55:50.719033+02', 6, 'Size');
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (9, 4, 11, false, false, 2, '2010-08-17 17:56:18.897185+02', 7, 'Dimension');
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (10, 4, 12, false, false, 2, '2010-08-17 17:56:35.218133+02', 8, 'Dimension');
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (11, 4, 13, false, false, 2, '2010-08-17 17:56:56.027473+02', 9, 'Dimension');
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (12, 4, 14, false, false, 2, '2010-08-17 17:57:17.950636+02', 10, 'Limits');
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (13, 4, 15, false, false, 2, '2010-08-17 17:57:32.894346+02', 11, 'Limits');
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (14, 4, 16, false, false, 2, '2010-08-17 17:58:43.547061+02', 12, NULL);
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (15, 4, 17, false, false, 2, '2010-08-17 17:59:03.438808+02', 13, NULL);
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (16, 4, 18, false, false, 2, '2010-08-17 17:59:25.551212+02', 14, NULL);
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (1, 4, 4, false, false, 2, '2010-06-10 09:31:17.382233+02', 16, NULL);
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (17, 4, 19, false, false, 2, '2010-08-17 17:59:37.910066+02', 15, NULL);
-- Completed on 2010-08-18 11:27:13 CEST

--
-- PostgreSQL database dump complete
--

