--
-- PostgreSQL database dump
--

-- Dumped from database version 9.0.4
-- Dumped by pg_dump version 9.0.4
-- Started on 2011-06-07 14:25:08 CEST

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

--
-- TOC entry 2273 (class 0 OID 428416)
-- Dependencies: 1707
-- Data for Name: controlled_vocabularies; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY controlled_vocabularies (id, code, description, registration_timestamp, pers_id_registerer, is_managed_internally, is_internal_namespace, dbin_id, modification_timestamp, is_chosen_from_list, source_uri) FROM stdin;
2	GROWTH_MEDIA	Growth media for B. Subtilis.	2011-06-01 14:21:40.40244+02	2	f	f	1	2011-06-01 14:21:40.415+02	t	\N
3	TEMPERATURE	Temperature the experiment was conducted under	2011-06-01 14:22:36.240178+02	2	f	f	1	2011-06-01 14:22:36.241+02	t	\N
4	CONTAINER	Container in which the experiment was performed.	2011-06-01 14:23:09.455946+02	2	f	f	1	2011-06-01 14:23:09.458+02	t	\N
5	TECHNOLOGY	Technologies used in BaSynthec	2011-06-01 14:32:54.940133+02	2	f	f	1	2011-06-01 14:32:54.941+02	t	\N
6	DEVICE	Devices used to produce measurements.	2011-06-01 14:33:26.888983+02	2	f	f	1	2011-06-01 14:33:26.89+02	t	\N
7	TIMEPOINT_TYPE	Timepoint Type	2011-06-01 14:59:35.528305+02	2	f	f	1	2011-06-01 14:59:35.529+02	t	\N
8	CELL_LOCATION	Cell Location	2011-06-01 14:59:51.638904+02	2	f	f	1	2011-06-01 14:59:51.639+02	t	\N
9	VALUE_TYPE	Value Type	2011-06-01 15:07:15.850051+02	2	f	f	1	2011-06-01 15:07:15.85+02	t	\N
11	SCALE	Scale	2011-06-01 15:08:55.973041+02	2	f	f	1	2011-06-01 15:08:55.973+02	t	\N
10	VALUE_UNIT	Value Unit	2011-06-01 15:07:40.656819+02	2	f	f	1	2011-06-07 11:06:21.669+02	t	\N
\.

select setval('controlled_vocabulary_id_seq', 10);

--
-- TOC entry 2274 (class 0 OID 428427)
-- Dependencies: 1708 2273
-- Data for Name: controlled_vocabulary_terms; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY controlled_vocabulary_terms (id, code, registration_timestamp, covo_id, pers_id_registerer, label, description, ordinal, is_official) FROM stdin;
3	MALATE	2011-06-01 14:21:40.40244+02	2	2	\N	\N	1	t
4	GLUCOSE	2011-06-01 14:21:40.40244+02	2	2	\N	\N	2	t
5	20	2011-06-01 14:22:36.240178+02	3	2	\N	\N	1	t
6	30	2011-06-01 14:22:36.240178+02	3	2	\N	\N	2	t
7	40	2011-06-01 14:22:36.240178+02	3	2	\N	\N	3	t
8	FLASK	2011-06-01 14:23:09.455946+02	4	2	\N	\N	1	t
9	METABOLOMICS	2011-06-01 14:32:54.940133+02	5	2	\N	\N	1	t
10	PHYSIOLOGY	2011-06-01 14:32:54.940133+02	5	2	\N	\N	2	t
11	PROTEOMICS	2011-06-01 14:32:54.940133+02	5	2	\N	\N	3	t
12	TRANSCRIPTOMICS	2011-06-01 14:32:54.940133+02	5	2	\N	\N	4	t
13	ORBITRAP	2011-06-01 14:33:26.888983+02	6	2	\N	\N	1	t
14	EX	2011-06-01 14:59:35.528305+02	7	2	\N	\N	1	t
15	IN	2011-06-01 14:59:35.528305+02	7	2	\N	\N	2	t
16	SI	2011-06-01 14:59:35.528305+02	7	2	\N	\N	3	t
17	CE	2011-06-01 14:59:51.638904+02	8	2	\N	\N	1	t
18	ES	2011-06-01 14:59:51.638904+02	8	2	\N	\N	2	t
19	ME	2011-06-01 14:59:51.638904+02	8	2	\N	\N	3	t
20	CY	2011-06-01 14:59:51.638904+02	8	2	\N	\N	4	t
21	NC	2011-06-01 14:59:51.638904+02	8	2	\N	\N	5	t
22	VALUE	2011-06-01 15:07:15.850051+02	9	2	\N	\N	1	t
23	MEAN	2011-06-01 15:07:15.850051+02	9	2	\N	\N	2	t
24	MEDIAN	2011-06-01 15:07:15.850051+02	9	2	\N	\N	3	t
25	STD	2011-06-01 15:07:15.850051+02	9	2	\N	\N	4	t
26	VAR	2011-06-01 15:07:15.850051+02	9	2	\N	\N	5	t
27	ERROR	2011-06-01 15:07:15.850051+02	9	2	\N	\N	6	t
28	IQR	2011-06-01 15:07:15.850051+02	9	2	\N	\N	7	t
29	MM	2011-06-01 15:07:40.656819+02	10	2	\N	\N	1	t
30	UM	2011-06-01 15:07:40.656819+02	10	2	\N	\N	2	t
34	LIN	2011-06-01 15:08:55.973041+02	11	2	\N	\N	1	t
35	LOG2	2011-06-01 15:08:55.973041+02	11	2	\N	\N	2	t
36	LOG10	2011-06-01 15:08:55.973041+02	11	2	\N	\N	3	t
37	LN	2011-06-01 15:08:55.973041+02	11	2	\N	\N	4	t
31	RATIOT1	2011-06-01 15:07:40.656819+02	10	2	\N	\N	4	t
33	RATIOCS	2011-06-01 15:08:14.224243+02	10	2	\N	\N	5	t
38	PERCENT	2011-06-07 09:25:59.284385+02	10	2	\N	\N	3	t
39	AU	2011-06-07 09:26:07.523864+02	10	2	\N	\N	6	t
40	DIMENSIONLESS	2011-06-07 11:06:21.616952+02	10	2	\N	\N	7	t
\.

select setval('cvte_id_seq', 40);

--
-- TOC entry 2287 (class 0 OID 428609)
-- Dependencies: 1733
-- Data for Name: data_set_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY data_set_types (id, code, description, dbin_id, modification_timestamp, main_ds_pattern, main_ds_path, is_container) FROM stdin;
2	RAW	Raw data	1	2011-06-01 14:35:43.248544+02	\N	\N	f
5	TRANSCRIPTOMICS	Transcriptomics data	1	2011-06-07 09:43:30.916419+02	\N	\N	f
3	METABOLITE_INTENSITIES	Metabolite intensities	1	2011-06-01 15:09:15.822214+02	\N	\N	f
4	OD600	Growth profiles	1	2011-06-07 09:40:07.551324+02	\N	\N	f
6	PROTEIN_QUANTIFICATIONS	Protein quantifications	1	2011-06-07 09:44:55.591374+02	\N	\N	f
\.

select setval('data_set_type_id_seq', 6);

--
-- TOC entry 2285 (class 0 OID 428593)
-- Dependencies: 1731
-- Data for Name: material_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY material_types (id, code, description, dbin_id, modification_timestamp) FROM stdin;
1	STRAIN	Strain	1	2011-06-01 14:48:39.161056+02
\.

select setval('material_type_id_seq', 1);

--
-- TOC entry 2289 (class 0 OID 428632)
-- Dependencies: 1736 2273 2276 2285
-- Data for Name: property_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id, schema, transformation) FROM stdin;
2	MEDIUM	The growth medium for the experiment	Medium	7	2011-06-01 14:23:50.820065+02	2	2	f	f	1	\N	\N	\N
3	TEMPERATURE	The temperature under which the experiment was carried out.	Temperature	7	2011-06-01 14:24:18.789581+02	2	3	f	f	1	\N	\N	\N
4	CONTAINER	The type of container in which the experiment was carried out.	Container	7	2011-06-01 14:24:40.461839+02	2	4	f	f	1	\N	\N	\N
5	CONTAINER_VOLUME	The volume of the container.	Container Volume	1	2011-06-01 14:25:42.437944+02	2	\N	f	f	1	\N	\N	\N
6	MISC_GROWTH_CONDITIONS	Miscellaneous growth conditions.	Growth Conditions	2	2011-06-01 14:26:20.93953+02	2	\N	f	f	1	\N	\N	\N
7	SHAKER_RPM	The RPMs of the shaker used to prepare the experiment.	Shaker RPM	3	2011-06-01 14:26:51.368174+02	2	\N	f	f	1	\N	\N	\N
8	COMMENTS	Miscellaneous comments.	Comments	2	2011-06-01 14:27:17.239486+02	2	\N	f	f	1	\N	\N	\N
9	TECHNOLOGY	The technology used for a measurement.	Technology	7	2011-06-01 14:34:38.569805+02	2	5	f	f	1	\N	\N	\N
10	DEVICE	The device used for a measurement. 	Device	7	2011-06-01 14:35:02.06542+02	2	6	f	f	1	\N	\N	\N
11	OPERATOR	The operator of a device.	Operator	1	2011-06-01 14:35:20.308799+02	2	\N	f	f	1	\N	\N	\N
12	STRAIN_NAME	The name of the strain.	Name	1	2011-06-01 14:49:27.382572+02	2	\N	f	f	1	\N	\N	\N
13	STRAIN	The strain of B. Subtilis.	Strain	8	2011-06-01 14:52:49.555029+02	2	\N	f	f	1	1	\N	\N
14	TIMEPOINT_TYPE	Timepoint Type	Timepoint Type	7	2011-06-01 15:10:26.249761+02	2	7	f	f	1	\N	\N	\N
15	CELL_LOCATION	Cell Location	Cell Location	7	2011-06-01 15:10:41.349422+02	2	8	f	f	1	\N	\N	\N
16	VALUE_TYPE	Value Type	Value Type	7	2011-06-01 15:11:00.652368+02	2	9	f	f	1	\N	\N	\N
17	VALUE_UNIT	Value Unit	Value Unit	7	2011-06-01 15:11:20.926301+02	2	10	f	f	1	\N	\N	\N
18	SCALE	Scale	Scale	7	2011-06-01 15:11:36.989897+02	2	11	f	f	1	\N	\N	\N
\.

select setval('property_type_id_seq', 18);

--
-- TOC entry 2290 (class 0 OID 428695)
-- Dependencies: 1743 2287 2289
-- Data for Name: data_set_type_property_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section, script_id) FROM stdin;
1	2	9	t	f	2	2011-06-01 14:44:56.126417+02	2	\N	\N
2	2	10	f	f	2	2011-06-01 14:45:10.671022+02	3	\N	\N
3	2	11	f	f	2	2011-06-01 14:45:20.412582+02	4	\N	\N
4	2	13	t	f	2	2011-06-01 14:55:14.000965+02	1	\N	\N
5	3	13	f	f	2	2011-06-01 15:09:45.526152+02	1	\N	\N
6	3	14	f	f	2	2011-06-01 15:11:51.457314+02	2	\N	\N
7	3	15	f	f	2	2011-06-01 15:12:06.094358+02	3	\N	\N
8	3	16	f	f	2	2011-06-01 15:12:17.388726+02	4	\N	\N
9	3	17	f	f	2	2011-06-01 15:12:25.79652+02	5	\N	\N
10	3	18	f	f	2	2011-06-01 15:12:35.809392+02	6	\N	\N
11	4	13	f	f	2	2011-06-07 09:41:19.577666+02	1	\N	\N
12	4	14	f	f	2	2011-06-07 09:41:39.189056+02	2	\N	\N
13	4	15	f	f	2	2011-06-07 09:42:00.464051+02	3	\N	\N
14	4	16	f	f	2	2011-06-07 09:42:14.906524+02	4	\N	\N
15	4	17	f	f	2	2011-06-07 09:42:30.293596+02	5	\N	\N
16	4	18	f	f	2	2011-06-07 09:42:36.606913+02	6	\N	\N
17	5	13	f	f	2	2011-06-07 09:45:57.381638+02	1	\N	\N
18	5	14	f	f	2	2011-06-07 09:46:07.477336+02	2	\N	\N
19	5	15	f	f	2	2011-06-07 09:46:23.775868+02	3	\N	\N
20	5	16	f	f	2	2011-06-07 09:46:34.150697+02	4	\N	\N
21	5	17	f	f	2	2011-06-07 09:46:42.908173+02	5	\N	\N
22	5	18	f	f	2	2011-06-07 09:46:49.641018+02	6	\N	\N
23	6	13	f	f	2	2011-06-07 10:26:20.876194+02	1	\N	\N
24	6	14	f	f	2	2011-06-07 10:26:37.439939+02	2	\N	\N
25	6	15	f	f	2	2011-06-07 10:27:03.19151+02	3	\N	\N
27	6	17	f	f	2	2011-06-07 10:27:23.191867+02	5	\N	\N
28	6	18	f	f	2	2011-06-07 10:27:33.149813+02	6	\N	\N
\.

select setval('material_type_id_seq', 1);

--
-- TOC entry 2279 (class 0 OID 428518)
-- Dependencies: 1721
-- Data for Name: experiment_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY experiment_types (id, code, description, dbin_id, modification_timestamp) FROM stdin;
1	BASYNTHEC	The BaSynthec experiment type.	1	2011-06-01 14:08:24.183425+02
\.


select setval('experiment_type_id_seq', 1);

--
-- TOC entry 2280 (class 0 OID 428525)
-- Dependencies: 1722 2279 2289
-- Data for Name: experiment_type_property_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY experiment_type_property_types (id, exty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section, script_id) FROM stdin;
1	1	1	f	f	2	2011-06-01 14:11:11.407487+02	1	\N	\N
2	1	2	f	f	2	2011-06-01 14:30:06.812876+02	2	\N	\N
3	1	3	f	f	2	2011-06-01 14:30:12.302431+02	3	\N	\N
4	1	4	f	f	2	2011-06-01 14:30:23.277699+02	4	\N	\N
5	1	5	f	f	2	2011-06-01 14:30:28.637025+02	5	\N	\N
6	1	6	f	f	2	2011-06-01 14:30:43.373831+02	6	\N	\N
7	1	7	f	f	2	2011-06-01 14:30:54.766288+02	7	\N	\N
8	1	8	f	f	2	2011-06-01 14:31:02.906052+02	8	\N	\N
\.


--
-- TOC entry 2283 (class 0 OID 428577)
-- Dependencies: 1729 2285
-- Data for Name: materials; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY materials (id, code, maty_id, pers_id_registerer, registration_timestamp, modification_timestamp, dbin_id) FROM stdin;
1	STRAIN1	1	2	2011-06-01 14:51:19.288314+02	2011-06-01 14:51:19.647+02	1
\.

select setval('material_id_seq', 1);

--
-- TOC entry 2282 (class 0 OID 428557)
-- Dependencies: 1726
-- Data for Name: spaces; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY spaces (id, code, dbin_id, description, registration_timestamp, pers_id_registerer) FROM stdin;
1	SHARED	1	The shared space for BaSynthec projects.	2011-06-01 14:08:52.698088+02	2
2	TEST	1	The space for playing around.	2011-06-07 10:49:39.809463+02	2
\.

select setval('space_id_seq', 1);

--
-- TOC entry 2288 (class 0 OID 428624)
-- Dependencies: 1735 2282
-- Data for Name: projects; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY projects (id, code, space_id, pers_id_leader, description, pers_id_registerer, registration_timestamp, modification_timestamp) FROM stdin;
1	SHARED	1	\N	The shared project for BaSynthec.	2	2011-06-01 14:09:22.816134+02	2011-06-06 23:43:41.031+02
2	TEST	2	\N	The project for playing around.	2	2011-06-07 10:49:54.113257+02	2011-06-07 10:50:06.356+02
\.

select setval('project_id_seq', 1);

--
-- TOC entry 2277 (class 0 OID 428488)
-- Dependencies: 1717 2279 2283 2288
-- Data for Name: experiments; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY experiments (id, perm_id, code, exty_id, mate_id_study_object, pers_id_registerer, registration_timestamp, modification_timestamp, proj_id, inva_id, is_public) FROM stdin;
16	20110606225040828-60	TEST	1	\N	2	2011-06-06 22:50:40.504299+02	2011-06-06 23:45:46.52+02	1	\N	f
15	20110606154239424-58	SHARED	1	\N	2	2011-06-06 15:42:39.39547+02	2011-06-07 09:30:59.375+02	1	\N	f
27	20110607105006352-102	TEST	1	\N	2	2011-06-07 10:50:06.193353+02	2011-06-07 13:52:22.216+02	2	\N	f
\.

select setval('experiment_id_seq', 27);

--
-- TOC entry 2278 (class 0 OID 428510)
-- Dependencies: 1720 2274 2280 2277 2283
-- Data for Name: experiment_properties; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY experiment_properties (id, expe_id, etpt_id, value, cvte_id, mate_prop_id, pers_id_registerer, registration_timestamp, modification_timestamp) FROM stdin;
15	15	3	\N	5	\N	2	2011-06-06 15:42:39.39547+02	2011-06-06 15:42:39.425+02
16	15	4	\N	8	\N	2	2011-06-06 15:42:39.39547+02	2011-06-06 15:42:39.426+02
17	15	2	\N	3	\N	2	2011-06-06 15:42:39.39547+02	2011-06-06 15:42:39.426+02
18	16	4	\N	8	\N	2	2011-06-06 22:50:40.504299+02	2011-06-06 22:50:40.852+02
29	27	4	\N	8	\N	2	2011-06-07 10:50:06.193353+02	2011-06-07 10:50:06.355+02
\.

select setval('experiment_property_id_seq', 27);


--
-- TOC entry 2286 (class 0 OID 428600)
-- Dependencies: 1732 2285 2289
-- Data for Name: material_type_property_types; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY material_type_property_types (id, maty_id, prty_id, is_mandatory, is_managed_internally, registration_timestamp, pers_id_registerer, ordinal, section, script_id) FROM stdin;
1	1	12	t	f	2011-06-01 14:50:16.872011+02	2	1	\N	\N
\.


--
-- TOC entry 2284 (class 0 OID 428585)
-- Dependencies: 1730 2274 2283 2283 2286
-- Data for Name: material_properties; Type: TABLE DATA; Schema: public; Owner: cramakri
--

COPY material_properties (id, mate_id, mtpt_id, value, registration_timestamp, modification_timestamp, pers_id_registerer, cvte_id, mate_prop_id) FROM stdin;
1	1	1	Strain 1	2011-06-01 14:51:19.288314+02	2011-06-01 14:51:19.65+02	2	\N	\N
\.

select setval('material_property_id_seq', 27);

-- Completed on 2011-06-07 14:25:08 CEST

--
-- PostgreSQL database dump complete
--

