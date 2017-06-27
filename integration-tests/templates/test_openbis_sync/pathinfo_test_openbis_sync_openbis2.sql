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
1	1	\N		20161010132817957-3	4598260	\N	t	2016-10-10 13:28:17+02
2	1	1	original	original	4598260	\N	t	2016-10-10 13:28:17+02
3	1	2	original/test+prot	test+prot	4598260	\N	t	2016-10-10 13:28:04+02
4	1	3	original/test+prot/example.prot.xml	example.prot.xml	4598260	-309280870	f	2016-10-10 13:28:04+02
5	1	3	original/test+prot/search.properties	search.properties	0	0	f	2016-10-10 13:28:04+02
6	2	\N		20161010133046010-5	4598260	\N	t	2016-10-10 13:30:46+02
7	2	6	original	original	4598260	\N	t	2016-10-10 13:30:46+02
8	2	7	original/test+prot	test+prot	4598260	\N	t	2016-10-10 13:30:35+02
9	2	8	original/test+prot/example.prot.xml	example.prot.xml	4598260	-309280870	f	2016-10-10 13:30:35+02
10	2	8	original/test+prot/search.properties	search.properties	0	0	f	2016-10-10 13:30:35+02
\.


--
-- Name: data_set_files_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_set_files_id_seq', 10, true);


--
-- Data for Name: data_sets; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_sets (id, code, location) FROM stdin;
1	20161010132817957-3	C4311C47-19F4-4175-A3B4-2F5848B1AB73/51/88/65/20161010132817957-3
2	20161010133046010-5	C4311C47-19F4-4175-A3B4-2F5848B1AB73/17/a7/0b/20161010133046010-5
\.


--
-- Name: data_sets_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_sets_id_seq', 2, true);


--
-- Data for Name: database_version_logs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY database_version_logs (db_version, module_name, run_status, run_status_timestamp, module_code, run_exception) FROM stdin;
008	datastore_server/sql/postgresql/008/schema-008.sql	SUCCESS	2016-10-10 13:25:32.48	\\x0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f0a2f2a20446f6d61696e73202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202a2f0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f0a0a43524541544520444f4d41494e20544543485f494420415320424947494e543b0a0a43524541544520444f4d41494e20434f44452041532056415243484152283630293b0a0a43524541544520444f4d41494e2046494c455f5041544820415320564152434841522831303030293b0a0a43524541544520444f4d41494e20424f4f4c45414e5f4348415220415320424f4f4c45414e2044454641554c542046414c53453b0a0a43524541544520444f4d41494e2054494d455f5354414d502041532054494d455354414d5020574954482054494d45205a4f4e453b0a0a0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f0a2f2a205461626c657320202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202a2f0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f0a0a435245415445205441424c4520444154415f5345545320280a202049442042494753455249414c204e4f54204e554c4c2c0a2020434f444520434f4445204e4f54204e554c4c2c0a20204c4f434154494f4e2046494c455f50415448204e4f54204e554c4c2c0a0a20205052494d415259204b455920284944292c0a2020554e495155452028434f4445290a293b0a0a43524541544520494e44455820444154415f534554535f434f44455f494458204f4e20444154415f534554532028434f4445293b0a0a435245415445205441424c4520444154415f5345545f46494c455320280a202049442042494753455249414c204e4f54204e554c4c2c0a2020444153455f494420544543485f4944204e4f54204e554c4c2c0a2020504152454e545f494420544543485f49442c0a202052454c41544956455f504154482046494c455f50415448204e4f54204e554c4c2c0a202046494c455f4e414d452046494c455f50415448204e4f54204e554c4c2c0a202053495a455f494e5f425954455320424947494e54204e4f54204e554c4c2c0a2020434845434b53554d5f435243333220494e54454745522c0a202049535f4449524543544f525920424f4f4c45414e5f43484152204e4f54204e554c4c2c0a20204c4153545f4d4f4449464945442054494d455f5354414d50204e4f54204e554c4c2044454641554c54204e4f5728292c0a0a20205052494d415259204b455920284944292c0a2020434f4e53545241494e5420464b5f444154415f5345545f46494c45535f444154415f5345545320464f524549474e204b45592028444153455f494429205245464552454e43455320444154415f534554532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144450a293b0a0a43524541544520494e44455820444154415f5345545f46494c45535f444153455f49445f494458204f4e20444154415f5345545f46494c45532028444153455f4944293b0a43524541544520494e44455820444154415f5345545f46494c45535f444153455f49445f504152454e545f49445f494458204f4e20444154415f5345545f46494c45532028444153455f49442c20504152454e545f4944293b0a43524541544520494e44455820444154415f5345545f46494c45535f444153455f49445f52454c41544956455f504154485f494458204f4e20444154415f5345545f46494c45532028444153455f49442c2052454c41544956455f50415448293b0a43524541544520494e44455820444154415f5345545f46494c45535f444153455f49445f46494c455f4e414d455f494458204f4e20444154415f5345545f46494c45532028444153455f49442c2046494c455f4e414d45293b0a0a435245415445205441424c45204556454e545320280a20204c4153545f5345454e5f44454c4554494f4e5f4556454e545f494420544543485f4944204e4f54204e554c4c0a293b0a0a435245415445205441424c45204c4153545f46454544494e475f4556454e5420280a2020524547495354524154494f4e5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c0a293b0a0a	\N
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
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

