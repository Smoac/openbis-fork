--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

--
-- Name: boolean_char; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN boolean_char AS boolean DEFAULT false;


--
-- Name: code; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN code AS character varying(60);


--
-- Name: file_path; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN file_path AS character varying(1000);


--
-- Name: tech_id; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN tech_id AS bigint;


--
-- Name: time_stamp; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN time_stamp AS timestamp with time zone;


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: data_set_files; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_files (
    id bigint NOT NULL,
    dase_id tech_id NOT NULL,
    parent_id tech_id,
    relative_path file_path NOT NULL,
    file_name file_path NOT NULL,
    size_in_bytes bigint NOT NULL,
    checksum_crc32 integer,
    is_directory boolean_char NOT NULL,
    last_modified time_stamp DEFAULT now() NOT NULL
);


--
-- Name: data_set_files_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_set_files_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_set_files_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE data_set_files_id_seq OWNED BY data_set_files.id;


--
-- Name: data_sets; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_sets (
    id bigint NOT NULL,
    code code NOT NULL,
    location file_path NOT NULL
);


--
-- Name: data_sets_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_sets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_sets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE data_sets_id_seq OWNED BY data_sets.id;


--
-- Name: database_version_logs; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE database_version_logs (
    db_version character varying(4) NOT NULL,
    module_name character varying(250),
    run_status character varying(10),
    run_status_timestamp timestamp without time zone,
    module_code bytea,
    run_exception bytea
);


--
-- Name: events; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE events (
    last_seen_deletion_event_id tech_id NOT NULL
);


--
-- Name: last_feeding_event; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE last_feeding_event (
    registration_timestamp time_stamp NOT NULL
);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_files ALTER COLUMN id SET DEFAULT nextval('data_set_files_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_sets ALTER COLUMN id SET DEFAULT nextval('data_sets_id_seq'::regclass);


--
-- Data for Name: data_set_files; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_files (id, dase_id, parent_id, relative_path, file_name, size_in_bytes, checksum_crc32, is_directory, last_modified) FROM stdin;
1	1	\N		20161010125004966-3	9594702	\N	t	2016-10-10 12:50:07+02
2	1	1	original	original	9594702	\N	t	2016-10-10 12:50:06+02
3	1	2	original/PLATE1	PLATE1	9594702	\N	t	2016-10-10 12:49:52+02
4	1	3	original/PLATE1/PLATE1_A01_01_Cy3.jpg	PLATE1_A01_01_Cy3.jpg	507355	366121855	f	2016-10-10 12:49:52+02
5	1	3	original/PLATE1/PLATE1_A01_01_DAPI.jpg	PLATE1_A01_01_DAPI.jpg	577505	-1399612785	f	2016-10-10 12:49:52+02
6	1	3	original/PLATE1/PLATE1_A01_01_GFP.jpg	PLATE1_A01_01_GFP.jpg	493561	-164232993	f	2016-10-10 12:49:52+02
7	1	3	original/PLATE1/PLATE1_A01_02_Cy3.jpg	PLATE1_A01_02_Cy3.jpg	649100	-1426775737	f	2016-10-10 12:49:52+02
8	1	3	original/PLATE1/PLATE1_A01_02_DAPI.jpg	PLATE1_A01_02_DAPI.jpg	539855	901875234	f	2016-10-10 12:49:52+02
9	1	3	original/PLATE1/PLATE1_A01_02_GFP.jpg	PLATE1_A01_02_GFP.jpg	497419	-1926412963	f	2016-10-10 12:49:52+02
10	1	3	original/PLATE1/PLATE1_A01_03_Cy3.jpg	PLATE1_A01_03_Cy3.jpg	598547	1489054275	f	2016-10-10 12:49:52+02
11	1	3	original/PLATE1/PLATE1_A01_03_DAPI.jpg	PLATE1_A01_03_DAPI.jpg	589729	506116745	f	2016-10-10 12:49:52+02
12	1	3	original/PLATE1/PLATE1_A01_03_GFP.jpg	PLATE1_A01_03_GFP.jpg	531811	-2108281375	f	2016-10-10 12:49:52+02
13	1	3	original/PLATE1/PLATE1_A01_04_Cy3.jpg	PLATE1_A01_04_Cy3.jpg	578310	1780216652	f	2016-10-10 12:49:52+02
14	1	3	original/PLATE1/PLATE1_A01_04_DAPI.jpg	PLATE1_A01_04_DAPI.jpg	395130	-1886920854	f	2016-10-10 12:49:52+02
15	1	3	original/PLATE1/PLATE1_A01_04_GFP.jpg	PLATE1_A01_04_GFP.jpg	397440	-1351125929	f	2016-10-10 12:49:52+02
16	1	3	original/PLATE1/PLATE1_A01_05_Cy3.jpg	PLATE1_A01_05_Cy3.jpg	735177	-1329505217	f	2016-10-10 12:49:52+02
17	1	3	original/PLATE1/PLATE1_A01_05_DAPI.jpg	PLATE1_A01_05_DAPI.jpg	521540	1694618329	f	2016-10-10 12:49:52+02
18	1	3	original/PLATE1/PLATE1_A01_05_GFP.jpg	PLATE1_A01_05_GFP.jpg	524730	1446396610	f	2016-10-10 12:49:52+02
19	1	3	original/PLATE1/PLATE1_A01_06_Cy3.jpg	PLATE1_A01_06_Cy3.jpg	529625	240482783	f	2016-10-10 12:49:52+02
20	1	3	original/PLATE1/PLATE1_A01_06_DAPI.jpg	PLATE1_A01_06_DAPI.jpg	501639	-1994343582	f	2016-10-10 12:49:52+02
21	1	3	original/PLATE1/PLATE1_A01_06_GFP.jpg	PLATE1_A01_06_GFP.jpg	426229	-1437750793	f	2016-10-10 12:49:52+02
22	2	\N		20161010125005326-4	734360	\N	t	2016-10-10 12:50:07+02
23	2	22	thumbnails.h5ar	thumbnails.h5ar	734360	\N	t	2016-10-10 12:50:06+02
24	2	23	thumbnails.h5ar/wA1_d1-1_cCy3.png	wA1_d1-1_cCy3.png	36424	-513922207	f	2016-10-10 12:50:05+02
25	2	23	thumbnails.h5ar/wA1_d1-1_cDAPI.png	wA1_d1-1_cDAPI.png	43322	1851277413	f	2016-10-10 12:50:05+02
26	2	23	thumbnails.h5ar/wA1_d1-1_cGFP.png	wA1_d1-1_cGFP.png	40505	-1621268612	f	2016-10-10 12:50:05+02
27	2	23	thumbnails.h5ar/wA1_d1-2_cCy3.png	wA1_d1-2_cCy3.png	43096	878106913	f	2016-10-10 12:50:05+02
28	2	23	thumbnails.h5ar/wA1_d1-2_cDAPI.png	wA1_d1-2_cDAPI.png	32444	1998248885	f	2016-10-10 12:50:05+02
29	2	23	thumbnails.h5ar/wA1_d1-2_cGFP.png	wA1_d1-2_cGFP.png	32961	-438235316	f	2016-10-10 12:50:05+02
30	2	23	thumbnails.h5ar/wA1_d2-1_cCy3.png	wA1_d2-1_cCy3.png	48062	1007909444	f	2016-10-10 12:50:05+02
31	2	23	thumbnails.h5ar/wA1_d2-1_cDAPI.png	wA1_d2-1_cDAPI.png	41913	-486424631	f	2016-10-10 12:50:05+02
32	2	23	thumbnails.h5ar/wA1_d2-1_cGFP.png	wA1_d2-1_cGFP.png	40327	-1836668011	f	2016-10-10 12:50:05+02
33	2	23	thumbnails.h5ar/wA1_d2-2_cCy3.png	wA1_d2-2_cCy3.png	51564	1321494909	f	2016-10-10 12:50:05+02
34	2	23	thumbnails.h5ar/wA1_d2-2_cDAPI.png	wA1_d2-2_cDAPI.png	42019	-1839845276	f	2016-10-10 12:50:05+02
35	2	23	thumbnails.h5ar/wA1_d2-2_cGFP.png	wA1_d2-2_cGFP.png	41219	-1038144621	f	2016-10-10 12:50:06+02
36	2	23	thumbnails.h5ar/wA1_d3-1_cCy3.png	wA1_d3-1_cCy3.png	43449	2066939482	f	2016-10-10 12:50:05+02
37	2	23	thumbnails.h5ar/wA1_d3-1_cDAPI.png	wA1_d3-1_cDAPI.png	45041	-861647438	f	2016-10-10 12:50:05+02
38	2	23	thumbnails.h5ar/wA1_d3-1_cGFP.png	wA1_d3-1_cGFP.png	42168	44148349	f	2016-10-10 12:50:05+02
39	2	23	thumbnails.h5ar/wA1_d3-2_cCy3.png	wA1_d3-2_cCy3.png	39980	-1641314331	f	2016-10-10 12:50:05+02
40	2	23	thumbnails.h5ar/wA1_d3-2_cDAPI.png	wA1_d3-2_cDAPI.png	37051	950970110	f	2016-10-10 12:50:06+02
41	2	23	thumbnails.h5ar/wA1_d3-2_cGFP.png	wA1_d3-2_cGFP.png	32815	-1151781810	f	2016-10-10 12:50:06+02
42	3	\N		20161010125005326-5	2581465	\N	t	2016-10-10 12:50:07+02
43	3	42	thumbnails_512x512.h5ar	thumbnails_512x512.h5ar	2581465	\N	t	2016-10-10 12:50:06+02
44	3	43	thumbnails_512x512.h5ar/wA1_d1-1_cCy3.png	wA1_d1-1_cCy3.png	133440	103584626	f	2016-10-10 12:50:06+02
45	3	43	thumbnails_512x512.h5ar/wA1_d1-1_cDAPI.png	wA1_d1-1_cDAPI.png	150424	1359824571	f	2016-10-10 12:50:06+02
46	3	43	thumbnails_512x512.h5ar/wA1_d1-1_cGFP.png	wA1_d1-1_cGFP.png	137116	-1831156898	f	2016-10-10 12:50:06+02
47	3	43	thumbnails_512x512.h5ar/wA1_d1-2_cCy3.png	wA1_d1-2_cCy3.png	154519	-1890654503	f	2016-10-10 12:50:06+02
48	3	43	thumbnails_512x512.h5ar/wA1_d1-2_cDAPI.png	wA1_d1-2_cDAPI.png	111974	-607623778	f	2016-10-10 12:50:06+02
49	3	43	thumbnails_512x512.h5ar/wA1_d1-2_cGFP.png	wA1_d1-2_cGFP.png	113287	-1446998283	f	2016-10-10 12:50:06+02
50	3	43	thumbnails_512x512.h5ar/wA1_d2-1_cCy3.png	wA1_d2-1_cCy3.png	173522	-1074257112	f	2016-10-10 12:50:06+02
51	3	43	thumbnails_512x512.h5ar/wA1_d2-1_cDAPI.png	wA1_d2-1_cDAPI.png	143921	-1592111482	f	2016-10-10 12:50:06+02
52	3	43	thumbnails_512x512.h5ar/wA1_d2-1_cGFP.png	wA1_d2-1_cGFP.png	136674	1453238004	f	2016-10-10 12:50:06+02
53	3	43	thumbnails_512x512.h5ar/wA1_d2-2_cCy3.png	wA1_d2-2_cCy3.png	190868	2056469078	f	2016-10-10 12:50:06+02
54	3	43	thumbnails_512x512.h5ar/wA1_d2-2_cDAPI.png	wA1_d2-2_cDAPI.png	143894	1195575114	f	2016-10-10 12:50:06+02
55	3	43	thumbnails_512x512.h5ar/wA1_d2-2_cGFP.png	wA1_d2-2_cGFP.png	141265	1710530257	f	2016-10-10 12:50:06+02
56	3	43	thumbnails_512x512.h5ar/wA1_d3-1_cCy3.png	wA1_d3-1_cCy3.png	159723	-1421448094	f	2016-10-10 12:50:06+02
57	3	43	thumbnails_512x512.h5ar/wA1_d3-1_cDAPI.png	wA1_d3-1_cDAPI.png	154711	62386562	f	2016-10-10 12:50:06+02
58	3	43	thumbnails_512x512.h5ar/wA1_d3-1_cGFP.png	wA1_d3-1_cGFP.png	142816	-471758384	f	2016-10-10 12:50:06+02
59	3	43	thumbnails_512x512.h5ar/wA1_d3-2_cCy3.png	wA1_d3-2_cCy3.png	144543	-1850300595	f	2016-10-10 12:50:06+02
60	3	43	thumbnails_512x512.h5ar/wA1_d3-2_cDAPI.png	wA1_d3-2_cDAPI.png	132031	1374644977	f	2016-10-10 12:50:06+02
61	3	43	thumbnails_512x512.h5ar/wA1_d3-2_cGFP.png	wA1_d3-2_cGFP.png	116737	-910094929	f	2016-10-10 12:50:06+02
\.


--
-- Name: data_set_files_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_set_files_id_seq', 61, true);


--
-- Data for Name: data_sets; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_sets (id, code, location) FROM stdin;
1	20161010125004966-3	FCCA0C6A-E191-413B-9CD5-B0B63877F4DA/0e/17/fa/20161010125004966-3
2	20161010125005326-4	FCCA0C6A-E191-413B-9CD5-B0B63877F4DA/db/11/31/20161010125005326-4
3	20161010125005326-5	FCCA0C6A-E191-413B-9CD5-B0B63877F4DA/eb/7d/3a/20161010125005326-5
\.


--
-- Name: data_sets_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_sets_id_seq', 3, true);


--
-- Data for Name: database_version_logs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY database_version_logs (db_version, module_name, run_status, run_status_timestamp, module_code, run_exception) FROM stdin;
008	datastore_server/sql/postgresql/008/schema-008.sql	SUCCESS	2016-10-10 12:39:37.687	\\x0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f0a2f2a20446f6d61696e73202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202a2f0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f0a0a43524541544520444f4d41494e20544543485f494420415320424947494e543b0a0a43524541544520444f4d41494e20434f44452041532056415243484152283630293b0a0a43524541544520444f4d41494e2046494c455f5041544820415320564152434841522831303030293b0a0a43524541544520444f4d41494e20424f4f4c45414e5f4348415220415320424f4f4c45414e2044454641554c542046414c53453b0a0a43524541544520444f4d41494e2054494d455f5354414d502041532054494d455354414d5020574954482054494d45205a4f4e453b0a0a0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f0a2f2a205461626c657320202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202a2f0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f0a0a435245415445205441424c4520444154415f5345545320280a202049442042494753455249414c204e4f54204e554c4c2c0a2020434f444520434f4445204e4f54204e554c4c2c0a20204c4f434154494f4e2046494c455f50415448204e4f54204e554c4c2c0a0a20205052494d415259204b455920284944292c0a2020554e495155452028434f4445290a293b0a0a43524541544520494e44455820444154415f534554535f434f44455f494458204f4e20444154415f534554532028434f4445293b0a0a435245415445205441424c4520444154415f5345545f46494c455320280a202049442042494753455249414c204e4f54204e554c4c2c0a2020444153455f494420544543485f4944204e4f54204e554c4c2c0a2020504152454e545f494420544543485f49442c0a202052454c41544956455f504154482046494c455f50415448204e4f54204e554c4c2c0a202046494c455f4e414d452046494c455f50415448204e4f54204e554c4c2c0a202053495a455f494e5f425954455320424947494e54204e4f54204e554c4c2c0a2020434845434b53554d5f435243333220494e54454745522c0a202049535f4449524543544f525920424f4f4c45414e5f43484152204e4f54204e554c4c2c0a20204c4153545f4d4f4449464945442054494d455f5354414d50204e4f54204e554c4c2044454641554c54204e4f5728292c0a0a20205052494d415259204b455920284944292c0a2020434f4e53545241494e5420464b5f444154415f5345545f46494c45535f444154415f5345545320464f524549474e204b45592028444153455f494429205245464552454e43455320444154415f534554532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144450a293b0a0a43524541544520494e44455820444154415f5345545f46494c45535f444153455f49445f494458204f4e20444154415f5345545f46494c45532028444153455f4944293b0a43524541544520494e44455820444154415f5345545f46494c45535f444153455f49445f504152454e545f49445f494458204f4e20444154415f5345545f46494c45532028444153455f49442c20504152454e545f4944293b0a43524541544520494e44455820444154415f5345545f46494c45535f444153455f49445f52454c41544956455f504154485f494458204f4e20444154415f5345545f46494c45532028444153455f49442c2052454c41544956455f50415448293b0a43524541544520494e44455820444154415f5345545f46494c45535f444153455f49445f46494c455f4e414d455f494458204f4e20444154415f5345545f46494c45532028444153455f49442c2046494c455f4e414d45293b0a0a435245415445205441424c45204556454e545320280a20204c4153545f5345454e5f44454c4554494f4e5f4556454e545f494420544543485f4944204e4f54204e554c4c0a293b0a0a435245415445205441424c45204c4153545f46454544494e475f4556454e5420280a2020524547495354524154494f4e5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c0a293b0a0a	\N
\.


--
-- Data for Name: events; Type: TABLE DATA; Schema: public; Owner: -
--

COPY events (last_seen_deletion_event_id) FROM stdin;
\.


--
-- Data for Name: last_feeding_event; Type: TABLE DATA; Schema: public; Owner: -
--

COPY last_feeding_event (registration_timestamp) FROM stdin;
\.


--
-- Name: data_set_files_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_files
    ADD CONSTRAINT data_set_files_pkey PRIMARY KEY (id);


--
-- Name: data_sets_code_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_sets
    ADD CONSTRAINT data_sets_code_key UNIQUE (code);


--
-- Name: data_sets_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_sets
    ADD CONSTRAINT data_sets_pkey PRIMARY KEY (id);


--
-- Name: data_set_files_dase_id_file_name_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_set_files_dase_id_file_name_idx ON data_set_files USING btree (dase_id, file_name);


--
-- Name: data_set_files_dase_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_set_files_dase_id_idx ON data_set_files USING btree (dase_id);


--
-- Name: data_set_files_dase_id_parent_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_set_files_dase_id_parent_id_idx ON data_set_files USING btree (dase_id, parent_id);


--
-- Name: data_set_files_dase_id_relative_path_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_set_files_dase_id_relative_path_idx ON data_set_files USING btree (dase_id, relative_path);


--
-- Name: data_sets_code_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_sets_code_idx ON data_sets USING btree (code);


--
-- Name: fk_data_set_files_data_sets; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_files
    ADD CONSTRAINT fk_data_set_files_data_sets FOREIGN KEY (dase_id) REFERENCES data_sets(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: public; Type: ACL; Schema: -; Owner: -
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM gakin;
GRANT ALL ON SCHEMA public TO gakin;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

