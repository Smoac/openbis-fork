--
-- PostgreSQL database dump
--

-- Started on 2010-11-30 16:37:58 CET

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 519 (class 2612 OID 1366350)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: cramakri
--

-- CREATE PROCEDURAL LANGUAGE plpgsql;


-- ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO cramakri;

SET search_path = public, pg_catalog;

--
-- TOC entry 2592 (class 0 OID 1366728)
-- Dependencies: 1775
-- Data for Name: property_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--
-- 1	DESCRIPTION	A Description	Description	1	2010-11-23 15:35:26.619042+01	1	\N	f	f	1	\N	\N	\N

COPY property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) FROM stdin;
2	ANNOTATION	A user annotation of the image.	Annotation	2	2010-11-23 16:04:23.696762+01	2	\N	f	f	1	\N	\N	\N
3	COLORFLAG	Is the image a color image?	Is Color?	5	2010-11-23 16:05:14.828992+01	2	\N	f	f	1	\N	\N	\N
4	CREATION_DATE	Creation date of this sample.	Creation Date	6	2010-11-23 16:05:42.995123+01	2	\N	f	f	1	\N	\N	\N
5	DATA-TYPE	The data type.	Data Type	1	2010-11-23 16:07:14.282926+01	2	\N	f	f	1	\N	\N	\N
6	CREATOR_EMAIL	The creator of the sample.	Creator Email	1	2010-11-23 16:07:39.703739+01	2	\N	f	f	1	\N	\N	\N
7	DIMENSIONX	The X dimension of the image.	Dimension X	3	2010-11-23 16:08:15.426879+01	2	\N	f	f	1	\N	\N	\N
8	DIMENSIONY	The Y dimension of the image.	Dimension Y	3	2010-11-23 16:09:35.990968+01	2	\N	f	f	1	\N	\N	\N
9	DIMENSIONZ	The Z dimension of the image.	Dimension Z	3	2010-11-23 16:10:12.987695+01	2	\N	f	f	1	\N	\N	\N
11	MAX	The Max value.	Max	4	2010-11-23 16:13:56.551571+01	2	\N	f	f	1	\N	\N	\N
12	LAB_ID	ID for the Lab in the external contact database.	Lab ID	1	2010-11-23 16:14:37.163892+01	2	\N	f	f	1	\N	\N	\N
13	MICROSCOPE	Which microscope was used.	Microscope	1	2010-11-23 16:15:53.914806+01	2	\N	f	f	1	\N	\N	\N
14	MIN	The min value.	Min	4	2010-11-23 16:16:31.779754+01	2	\N	f	f	1	\N	\N	\N
15	MISC	Miscelleanous Property Data	Misc	2	2010-11-23 16:17:08.838941+01	2	\N	f	f	1	\N	\N	\N
16	OPERATOR	Operator of the measurement device.	Operator	1	2010-11-23 16:17:34.735427+01	2	\N	f	f	1	\N	\N	\N
17	RATING	Rating of data quality	Rating	3	2010-11-23 16:17:58.799796+01	2	\N	f	f	1	\N	\N	\N
18	SIZEX	The X size of the image	Size X	4	2010-11-23 16:18:39.352648+01	2	\N	f	f	1	\N	\N	\N
19	SIZEY	The Y size of the image.	Size Y	4	2010-11-23 16:19:00.625666+01	2	\N	f	f	1	\N	\N	\N
20	SIZEZ	The Z size of the image.	Size Z	4	2010-11-23 16:19:22.004994+01	2	\N	f	f	1	\N	\N	\N
21	STACKFLAG	Is the image a stack?	Is Stack?	5	2010-11-23 16:20:25.024599+01	2	\N	f	f	1	\N	\N	\N
\.

select setval('property_type_id_seq', 21);


--
-- TOC entry 2567 (class 0 OID 1366491)
-- Dependencies: 1726
-- Data for Name: data_set_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--
-- 1	UNKNOWN	Unknown	1	2010-11-23 15:35:26.619042+01	\N	\N

COPY data_set_types (id, code, description, dbin_id, modification_timestamp, main_ds_pattern, main_ds_path) FROM stdin;
2	IMAGE	\N	1	2010-11-23 16:35:48.274825+01	\N	\N
3	BUNDLE	\N	1	2010-11-23 16:39:16.522518+01	\N	\N
4	METADATA	\N	1	2010-11-23 16:39:23.890373+01	\N	\N
5	RAW_IMAGES	\N	1	2010-11-23 16:39:28.89983+01	\N	\N
\.

select setval('data_set_type_id_seq', 5);

--
-- TOC entry 2566 (class 0 OID 1366480)
-- Dependencies: 1725
-- Data for Name: data_set_type_property_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section, script_id) FROM stdin;
1	2	2	f	f	2	2010-11-23 16:41:20.476646+01	1	\N	\N
5	2	16	f	f	2	2010-11-23 16:42:42.31869+01	2	\N	\N
2	2	7	f	f	2	2010-11-23 16:41:55.598405+01	4	Dimension	\N
3	2	8	f	f	2	2010-11-23 16:42:03.75754+01	5	Dimension	\N
4	2	9	f	f	2	2010-11-23 16:42:13.6773+01	6	Dimension	\N
6	2	17	f	f	2	2010-11-23 16:42:52.425723+01	3	\N	\N
7	2	18	f	f	2	2010-11-23 16:43:21.420024+01	7	Size	\N
8	2	19	f	f	2	2010-11-23 16:43:36.39704+01	8	Size	\N
9	2	20	f	f	2	2010-11-23 16:43:46.571047+01	9	Size	\N
10	2	15	f	f	2	2010-11-23 16:44:48.993521+01	10	\N	\N
11	3	15	f	f	2	2010-11-23 16:45:08.535134+01	1	\N	\N
12	4	15	f	f	2	2010-11-23 16:45:17.44938+01	1	\N	\N
13	5	15	f	f	2	2010-11-23 16:45:25.349048+01	1	\N	\N
\.

--
-- TOC entry 2577 (class 0 OID 1366584)
-- Dependencies: 1746
-- Data for Name: experiment_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY experiment_types (id, code, description, dbin_id, modification_timestamp) FROM stdin;
1	CINA_EXP_TYPE	\N	1	2010-11-23 16:40:21.980445+01
\.

--
-- TOC entry 2576 (class 0 OID 1366573)
-- Dependencies: 1745
-- Data for Name: experiment_type_property_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY experiment_type_property_types (id, exty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section, script_id) FROM stdin;
1	1	1	f	f	2	2010-11-23 16:46:31.838797+01	1	\N	\N
\.


select setval('experiment_type_id_seq', 1);

--
-- TOC entry 2599 (class 0 OID 1366802)
-- Dependencies: 1789
-- Data for Name: sample_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY sample_types (id, code, description, dbin_id, is_listable, generated_from_depth, part_of_depth, modification_timestamp, is_auto_generated_code, generated_code_prefix, is_subcode_unique) FROM stdin;
1	GRID_REPLICA	\N	1	t	1	0	2010-11-23 16:39:53.07607+01	f	REPLICA	f
3	GRID_PREP	\N	1	t	1	0	2010-11-25 08:35:13.965219+01	f	GRID	f
\.

select setval('sample_type_id_seq', 3);

--
-- TOC entry 2598 (class 0 OID 1366790)
-- Dependencies: 1788
-- Data for Name: sample_type_property_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY sample_type_property_types (id, saty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, is_displayed, ordinal, section, script_id) FROM stdin;
1	1	1	f	f	2	2010-11-23 16:45:50.000057+01	t	1	\N	\N
3	1	6	f	f	2	2010-11-23 16:46:14.461392+01	t	2	\N	\N
\.


--
-- TOC entry 2583 (class 0 OID 1366638)
-- Dependencies: 1756
-- Data for Name: spaces; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY spaces (id, code, dbin_id, description, registration_timestamp, pers_id_registerer) FROM stdin;
1	SHARED	1	\N	2010-11-23 15:39:06.834484+01	2
\.

select setval('space_id_seq', 1);

--
-- TOC entry 2591 (class 0 OID 1366718)
-- Dependencies: 1773
-- Data for Name: projects; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY projects (id, code, space_id, pers_id_leader, description, pers_id_registerer, registration_timestamp, modification_timestamp) FROM stdin;
1	GENERIC	1	\N	\N	2	2010-11-23 15:39:17.549496+01	2010-11-25 08:31:33.998+01
\.

select setval('project_id_seq', 1);


--
-- TOC entry 2578 (class 0 OID 1366591)
-- Dependencies: 1747
-- Data for Name: experiments; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY experiments (id, perm_id, code, exty_id, mate_id_study_object, pers_id_registerer, registration_timestamp, modification_timestamp, proj_id, inva_id, is_public) FROM stdin;
1	20101125083133963-1	E1	1	\N	2	2010-11-25 08:31:33.818753+01	2010-11-25 10:38:05.38+01	1	\N	f
\.

select setval('experiment_id_seq', 1);

--
-- TOC entry 2575 (class 0 OID 1366560)
-- Dependencies: 1742
-- Data for Name: experiment_properties; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY experiment_properties (id, expe_id, etpt_id, value, cvte_id, mate_prop_id, pers_id_registerer, registration_timestamp, modification_timestamp) FROM stdin;
1	1	1	Generic Experiment	\N	\N	2	2010-11-25 08:31:33.818753+01	2010-11-25 08:31:33.997+01
\.

select setval('experiment_property_id_seq', 1);


--
-- PostgreSQL database dump complete
--

