--
-- PostgreSQL database dump
--

-- Started on 2010-10-13 15:14:06 CEST

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

--
-- TOC entry 2543 (class 0 OID 921953)
-- Dependencies: 1715 2532
-- Data for Name: experiment_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--

INSERT INTO experiment_types (id, code, description, dbin_id, modification_timestamp) VALUES (1, 'CINA_EXP_TYPE', 'Generic Experiment Type', 1, '2010-10-13 15:11:41.168821+02');


--
-- TOC entry 2548 (class 0 OID 921991)
-- Dependencies: 1720 2532
-- Data for Name: groups; Type: TABLE DATA; Schema: public; Owner: cramakri
--

INSERT INTO groups (id, code, dbin_id, description, registration_timestamp, pers_id_registerer) VALUES (1, 'SHARED', 1, 'The Shared CINA space', '2010-10-13 15:12:32.899527+02', 2);

--
-- TOC entry 2557 (class 0 OID 922057)
-- Dependencies: 1729 2548 2556 2556
-- Data for Name: projects; Type: TABLE DATA; Schema: public; Owner: cramakri
--

INSERT INTO projects (id, code, grou_id, pers_id_leader, description, pers_id_registerer, registration_timestamp, modification_timestamp) VALUES (1, 'GENERIC', 1, NULL, 'The generic CINA project', 2, '2010-10-13 15:12:48.765211+02', '2010-10-13 15:12:48.791+02');


--
-- TOC entry 2562 (class 0 OID 922097)
-- Dependencies: 1734 2532
-- Data for Name: sample_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--

INSERT INTO sample_types (id, code, description, dbin_id, is_listable, generated_from_depth, part_of_depth, modification_timestamp, is_auto_generated_code, generated_code_prefix, is_subcode_unique) VALUES (1, 'GRID_PREP', 'Grid biochemistry and preparation', 1, true, 1, 0, '2010-10-13 14:58:28.43514+02', false, 'EM-PREP', false);
INSERT INTO sample_types (id, code, description, dbin_id, is_listable, generated_from_depth, part_of_depth, modification_timestamp, is_auto_generated_code, generated_code_prefix, is_subcode_unique) VALUES (2, 'GRID_REPLICA', 'A replica of a grid preparation', 1, true, 1, 0, '2010-10-13 14:59:00.201139+02', false, 'REPLICA', false);


--
-- TOC entry 2555 (class 0 OID 922043)
-- Dependencies: 1727 2532
-- Data for Name: data_set_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--

-- This one is already in an initialized database
-- INSERT INTO data_set_types (id, code, description, dbin_id, modification_timestamp, main_ds_pattern, main_ds_path) VALUES (1, 'UNKNOWN', 'Unknown', 1, '2010-10-13 14:53:54.256819+02', NULL, NULL);
INSERT INTO data_set_types (id, code, description, dbin_id, modification_timestamp, main_ds_pattern, main_ds_path) VALUES (2, 'BUNDLE', 'Bundle Data Set', 1, '2010-10-13 14:59:22.13588+02', NULL, NULL);
INSERT INTO data_set_types (id, code, description, dbin_id, modification_timestamp, main_ds_pattern, main_ds_path) VALUES (3, 'RAW_IMAGES', 'Raw Images', 1, '2010-10-13 14:59:33.224555+02', NULL, NULL);
INSERT INTO data_set_types (id, code, description, dbin_id, modification_timestamp, main_ds_pattern, main_ds_path) VALUES (4, 'METADATA', 'Metadata Data Set', 1, '2010-10-13 14:59:50.325991+02', 'metadata.xml', 'original');
INSERT INTO data_set_types (id, code, description, dbin_id, modification_timestamp, main_ds_pattern, main_ds_path) VALUES (5, 'IMAGE', 'Annotated image', 1, '2010-10-13 15:00:17.209773+02', '*/Representations/StandardPreview.png', 'original');
INSERT INTO data_set_types (id, code, description, dbin_id, modification_timestamp, main_ds_pattern, main_ds_path) VALUES (6, 'ANALYSIS', 'Analysis', 1, '2010-10-13 15:00:27.668553+02', NULL, NULL);


--
-- TOC entry 2558 (class 0 OID 922065)
-- Dependencies: 1730 2529 2537 2532 2556 2553
-- Data for Name: property_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--

-- This one is already in an initialized database
-- INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (1, 'DESCRIPTION', 'A Description', 'Description', 1, '2010-03-18 08:33:32.470674+01', 1, NULL, false, false, 1, NULL, NULL, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (2, 'LAB_ID', 'ID for the Lab in the external contact database.', 'Lab ID', 1, '2010-03-18 10:11:21.361947+01', 2, NULL, false, false, 1, NULL, NULL, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (3, 'CREATION_DATE', 'Creation date of this sample.', 'Creation Date', 6, '2010-03-23 17:42:30.258666+01', 2, NULL, false, false, 1, NULL, NULL, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (5, 'RATING', 'Rating of data quality', 'Rating', 3, '2010-06-10 09:46:29.093055+02', 2, NULL, false, false, 1, NULL, NULL, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (6, 'OPERATOR', 'Operator of the measurement device', 'Operator', 1, '2010-06-10 12:25:40.888407+02', 2, NULL, false, false, 1, NULL, NULL, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (7, 'MICROSCOPE', 'Which microscope was used ', 'Microscope', 1, '2010-08-17 17:38:41.944393+02', 2, NULL, false, false, 1, NULL, NULL, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (8, 'SIZEX', 'The X size of the image', 'Size X', 4, '2010-08-17 17:39:02.211289+02', 2, NULL, false, false, 1, NULL, NULL, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (9, 'SIZEY', 'The Y size of the image.', 'Size Y', 4, '2010-08-17 17:39:19.720074+02', 2, NULL, false, false, 1, NULL, NULL, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (10, 'SIZEZ', 'The Z size of the image.', 'Size Z', 4, '2010-08-17 17:39:39.614258+02', 2, NULL, false, false, 1, NULL, NULL, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (11, 'DIMENSIONX', 'The X dimension of the image.', 'Dimension X', 3, '2010-08-17 17:39:59.787031+02', 2, NULL, false, false, 1, NULL, NULL, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (12, 'DIMENSIONY', 'The Y dimension of the image.', 'Dimension Y', 3, '2010-08-17 17:40:15.518858+02', 2, NULL, false, false, 1, NULL, NULL, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (13, 'DIMENSIONZ', 'The Z dimension of the image.', 'Dimension Z', 3, '2010-08-17 17:40:31.43621+02', 2, NULL, false, false, 1, NULL, NULL, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (14, 'MIN', 'The min value.', 'Min', 4, '2010-08-17 17:40:50.414389+02', 2, NULL, false, false, 1, NULL, NULL, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (15, 'MAX', 'The Max value.', 'Max', 4, '2010-08-17 17:41:05.048801+02', 2, NULL, false, false, 1, NULL, NULL, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (16, 'ANNOTATION', 'A user annotation of the image.', 'Annotation', 2, '2010-08-17 17:41:27.830224+02', 2, NULL, false, false, 1, NULL, NULL, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (17, 'STACKFLAG', 'Is the image a stack?', 'Is Stack?', 5, '2010-08-17 17:41:54.744874+02', 2, NULL, false, false, 1, NULL, NULL, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (18, 'COLORFLAG', 'Is the image a color image?', 'Is Color?', 5, '2010-08-17 17:42:10.974678+02', 2, NULL, false, false, 1, NULL, NULL, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (19, 'DATA-TYPE', 'The data type.', 'Data Type', 1, '2010-08-17 17:42:50.359753+02', 2, NULL, false, false, 1, NULL, NULL, NULL);
INSERT INTO property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) VALUES (4, 'MISC', 'Miscelleanous Property Data', 'Misc', 2, '2010-06-10 09:30:45.922177+02', 2, NULL, false, false, 1, NULL, NULL, NULL);


--
-- TOC entry 2565 (class 0 OID 922128)
-- Dependencies: 1737 2555 2556 2558
-- Data for Name: data_set_type_property_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--

INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (1, 2, 4, false, false, 2, '2010-10-13 15:07:47.636403+02', 1, NULL);
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (2, 4, 4, false, false, 2, '2010-10-13 15:08:00.643786+02', 1, NULL);
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (3, 3, 4, false, false, 2, '2010-10-13 15:08:07.525629+02', 1, NULL);
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (4, 5, 16, false, false, 2, '2010-10-13 15:08:52.265351+02', 2, NULL);
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (6, 5, 6, false, false, 2, '2010-10-13 15:09:17.423274+02', 1, NULL);
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (5, 5, 19, false, false, 2, '2010-10-13 15:09:00.528773+02', 5, NULL);
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (7, 5, 11, false, false, 2, '2010-10-13 15:09:53.064636+02', 6, 'Dimension');
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (8, 5, 12, false, false, 2, '2010-10-13 15:10:00.509684+02', 7, 'Dimension');
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (9, 5, 13, false, false, 2, '2010-10-13 15:10:09.240504+02', 8, 'Dimension');
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (10, 5, 5, false, false, 2, '2010-10-13 15:10:25.056054+02', 3, NULL);
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (11, 5, 8, false, false, 2, '2010-10-13 15:10:41.89441+02', 9, 'Size');
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (12, 5, 9, false, false, 2, '2010-10-13 15:11:00.984381+02', 10, 'Size');
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (13, 5, 10, false, false, 2, '2010-10-13 15:11:10.22946+02', 11, 'Size');
INSERT INTO data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section) VALUES (14, 5, 4, false, false, 2, '2010-10-13 15:23:02.242689+02', 12, NULL);


-- Completed on 2010-10-13 15:14:07 CEST

--
-- PostgreSQL database dump complete
--

