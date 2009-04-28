--
-- PostgreSQL database dump
--

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: -
--

CREATE PROCEDURAL LANGUAGE plpgsql;


SET search_path = public, pg_catalog;

--
-- Name: boolean_char; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN boolean_char AS boolean DEFAULT false;


--
-- Name: code; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN code AS character varying(40);


--
-- Name: description_80; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN description_80 AS character varying(80);


--
-- Name: tech_id; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN tech_id AS bigint;


--
-- Name: time_stamp_dfl; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN time_stamp_dfl AS timestamp with time zone NOT NULL DEFAULT now();


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: controlled_vocabularies; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE controlled_vocabularies (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    is_internal_namespace boolean_char DEFAULT false NOT NULL,
    dbin_id tech_id NOT NULL
);


--
-- Name: object_name; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN object_name AS character varying(50);


--
-- Name: controlled_vocabulary_terms; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE controlled_vocabulary_terms (
    id tech_id NOT NULL,
    code object_name NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    covo_id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL
);


--
-- Name: time_stamp; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN time_stamp AS timestamp with time zone;


--
-- Name: data; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data (
    id tech_id NOT NULL,
    code code,
    dsty_id tech_id NOT NULL,
    proc_id_produced_by tech_id NOT NULL,
    data_producer_code code,
    production_timestamp time_stamp,
    samp_id_acquired_from tech_id,
    samp_id_derived_from tech_id,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    is_placeholder boolean_char DEFAULT false,
    is_deleted boolean_char DEFAULT false,
    is_valid boolean_char DEFAULT true,
    CONSTRAINT data_samp_arc_ck CHECK ((((samp_id_acquired_from IS NOT NULL) AND (samp_id_derived_from IS NULL)) OR ((samp_id_acquired_from IS NULL) AND (samp_id_derived_from IS NOT NULL))))
);


--
-- Name: data_set_relationships; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_relationships (
    data_id_parent tech_id NOT NULL,
    data_id_child tech_id NOT NULL
);


--
-- Name: data_set_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80,
    dbin_id tech_id NOT NULL
);


--
-- Name: data_stores; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_stores (
    id tech_id NOT NULL,
    dbin_id tech_id NOT NULL,
    code code NOT NULL,
    download_url character varying(1024) NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL
);


--
-- Name: data_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80 NOT NULL
);


--
-- Name: database_instances; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE database_instances (
    id tech_id NOT NULL,
    code code NOT NULL,
    uuid code NOT NULL,
    is_original_source boolean_char DEFAULT false NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    dast_id tech_id
);


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
-- Name: description_250; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN description_250 AS character varying(250);


--
-- Name: event_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN event_type AS character varying(40)
	CONSTRAINT event_type_check CHECK (((VALUE)::text = ANY ((ARRAY['DELETION'::character varying, 'INVALIDATION'::character varying, 'MOVEMENT'::character varying])::text[])));


--
-- Name: events; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE events (
    id tech_id NOT NULL,
    event_type event_type NOT NULL,
    description description_250,
    data_id tech_id,
    reason description_250,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL
);


--
-- Name: file; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN file AS bytea;


--
-- Name: experiment_attachment_contents; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiment_attachment_contents (
    id tech_id NOT NULL,
    value file NOT NULL
);


--
-- Name: file_name; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN file_name AS character varying(100);


--
-- Name: experiment_attachments; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiment_attachments (
    id tech_id NOT NULL,
    expe_id tech_id NOT NULL,
    exac_id tech_id NOT NULL,
    file_name file_name NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    version integer NOT NULL,
    pers_id_registerer tech_id NOT NULL
);


--
-- Name: generic_value; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN generic_value AS character varying(1024);


--
-- Name: experiment_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiment_properties (
    id tech_id NOT NULL,
    expe_id tech_id NOT NULL,
    etpt_id tech_id NOT NULL,
    value generic_value,
    cvte_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    CONSTRAINT expr_ck CHECK ((((value IS NOT NULL) AND (cvte_id IS NULL)) OR ((value IS NULL) AND (cvte_id IS NOT NULL))))
);


--
-- Name: experiment_type_property_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiment_type_property_types (
    id tech_id NOT NULL,
    exty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL
);


--
-- Name: experiment_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiment_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80,
    dbin_id tech_id NOT NULL
);


--
-- Name: experiments; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiments (
    id tech_id NOT NULL,
    code code NOT NULL,
    exty_id tech_id NOT NULL,
    mate_id_study_object tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    proj_id tech_id NOT NULL,
    inva_id tech_id,
    is_public boolean_char DEFAULT false NOT NULL,
    dast_id tech_id
);


--
-- Name: boolean_char_or_unknown; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN boolean_char_or_unknown AS character(1) DEFAULT 'U'::bpchar
	CONSTRAINT boolean_char_or_unknown_check CHECK ((VALUE = ANY (ARRAY['F'::bpchar, 'T'::bpchar, 'U'::bpchar])));


--
-- Name: external_data; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE external_data (
    data_id tech_id NOT NULL,
    location character varying(1024) NOT NULL,
    ffty_id tech_id NOT NULL,
    loty_id tech_id NOT NULL,
    cvte_id_stor_fmt tech_id NOT NULL,
    is_complete boolean_char_or_unknown DEFAULT 'U'::bpchar NOT NULL,
    cvte_id_store tech_id
);


--
-- Name: file_format_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE file_format_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80,
    dbin_id tech_id NOT NULL
);


--
-- Name: groups; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE groups (
    id tech_id NOT NULL,
    code code NOT NULL,
    dbin_id tech_id NOT NULL,
    grou_id_parent tech_id,
    pers_id_leader tech_id,
    description description_250,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    dast_id tech_id
);


--
-- Name: invalidations; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE invalidations (
    id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    reason description_250
);


--
-- Name: locator_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE locator_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80
);


--
-- Name: real_value; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN real_value AS real;


--
-- Name: material_batches; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE material_batches (
    id tech_id NOT NULL,
    code code NOT NULL,
    amount real_value,
    mate_id tech_id NOT NULL,
    proc_id tech_id,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL
);


--
-- Name: material_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE material_properties (
    id tech_id NOT NULL,
    mate_id tech_id NOT NULL,
    mtpt_id tech_id NOT NULL,
    value generic_value,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    cvte_id tech_id,
    CONSTRAINT mapr_ck CHECK ((((value IS NOT NULL) AND (cvte_id IS NULL)) OR ((value IS NULL) AND (cvte_id IS NOT NULL))))
);


--
-- Name: material_type_property_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE material_type_property_types (
    id tech_id NOT NULL,
    maty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL
);


--
-- Name: material_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE material_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80,
    dbin_id tech_id NOT NULL
);


--
-- Name: materials; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE materials (
    id tech_id NOT NULL,
    code code NOT NULL,
    maty_id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    mate_id_inhibitor_of tech_id,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    dbin_id tech_id NOT NULL
);


--
-- Name: user_id; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN user_id AS character varying(50);


--
-- Name: persons; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE persons (
    id tech_id NOT NULL,
    first_name character varying(30),
    last_name character varying(30),
    user_id user_id NOT NULL,
    email object_name,
    dbin_id tech_id NOT NULL,
    grou_id tech_id,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id
);


--
-- Name: procedure_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE procedure_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80 NOT NULL,
    is_data_acquisition boolean_char DEFAULT false NOT NULL,
    dbin_id tech_id NOT NULL
);


--
-- Name: procedures; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE procedures (
    id tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    expe_id tech_id NOT NULL,
    pcty_id tech_id NOT NULL
);


--
-- Name: description_1000; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN description_1000 AS character varying(1000);


--
-- Name: projects; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE projects (
    id tech_id NOT NULL,
    code code NOT NULL,
    grou_id tech_id NOT NULL,
    pers_id_leader tech_id,
    description description_1000,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    dast_id tech_id
);


--
-- Name: column_label; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN column_label AS character varying(40);


--
-- Name: property_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE property_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80 NOT NULL,
    label column_label NOT NULL,
    daty_id tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    covo_id tech_id,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    is_internal_namespace boolean_char DEFAULT false NOT NULL,
    dbin_id tech_id NOT NULL
);


--
-- Name: authorization_role; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN authorization_role AS character varying(40)
	CONSTRAINT authorization_role_check CHECK (((VALUE)::text = ANY ((ARRAY['ADMIN'::character varying, 'USER'::character varying, 'OBSERVER'::character varying, 'ETL_SERVER'::character varying])::text[])));


--
-- Name: role_assignments; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE role_assignments (
    id tech_id NOT NULL,
    role_code authorization_role NOT NULL,
    grou_id tech_id,
    dbin_id tech_id,
    pers_id_grantee tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    CONSTRAINT roas_dbin_grou_arc_ck CHECK ((((dbin_id IS NOT NULL) AND (grou_id IS NULL)) OR ((dbin_id IS NULL) AND (grou_id IS NOT NULL))))
);


--
-- Name: sample_inputs; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sample_inputs (
    samp_id tech_id NOT NULL,
    proc_id tech_id NOT NULL
);


--
-- Name: sample_material_batches; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sample_material_batches (
    samp_id tech_id NOT NULL,
    maba_id tech_id NOT NULL
);


--
-- Name: sample_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sample_properties (
    id tech_id NOT NULL,
    samp_id tech_id NOT NULL,
    stpt_id tech_id NOT NULL,
    value generic_value,
    cvte_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    CONSTRAINT sapr_ck CHECK ((((value IS NOT NULL) AND (cvte_id IS NULL)) OR ((value IS NULL) AND (cvte_id IS NOT NULL))))
);


--
-- Name: sample_type_property_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sample_type_property_types (
    id tech_id NOT NULL,
    saty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    is_displayed boolean_char DEFAULT true NOT NULL
);


--
-- Name: sample_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sample_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80,
    dbin_id tech_id NOT NULL,
    is_listable boolean_char DEFAULT true NOT NULL,
    generated_from_depth integer DEFAULT 0 NOT NULL,
    part_of_depth integer DEFAULT 0 NOT NULL
);


--
-- Name: samples; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE samples (
    id tech_id NOT NULL,
    code code NOT NULL,
    samp_id_top tech_id,
    samp_id_generated_from tech_id,
    saty_id tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    inva_id tech_id,
    samp_id_control_layout tech_id,
    dbin_id tech_id,
    grou_id tech_id,
    samp_id_part_of tech_id,
    CONSTRAINT samp_dbin_grou_arc_ck CHECK ((((dbin_id IS NOT NULL) AND (grou_id IS NULL)) OR ((dbin_id IS NULL) AND (grou_id IS NOT NULL))))
);


--
-- Name: controlled_vocabulary_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION controlled_vocabulary_check() RETURNS trigger
    AS $$
DECLARE
   v_code  CODE;
BEGIN

   select code into v_code from data_types where id = NEW.daty_id;

   -- Check if the data is of type "CONTROLLEDVOCABULARY"
   if v_code = 'CONTROLLEDVOCABULARY' then
      if NEW.covo_id IS NULL then
         RAISE EXCEPTION 'Insert/Update of Property Type (Code: %) failed, as its Data Type is CONTROLLEDVOCABULARY, but it is not linked to a Controlled Vocabulary.', NEW.code;
      end if;
   end if;

   RETURN NEW;

END;
$$
    LANGUAGE plpgsql;


--
-- Name: external_data_storage_format_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION external_data_storage_format_check() RETURNS trigger
    AS $$
DECLARE
   v_covo_code  CODE;
   data_code CODE;
BEGIN

   select code into v_covo_code from controlled_vocabularies
      where is_internal_namespace = true and 
         id = (select covo_id from controlled_vocabulary_terms where id = NEW.cvte_id_stor_fmt);
   -- Check if the data storage format is a term of the controlled vocabulary "STORAGE_FORMAT"
   if v_covo_code != 'STORAGE_FORMAT' then
      select code into data_code from data where id = NEW.data_id; 
      RAISE EXCEPTION 'Insert/Update of Data (Code: %) failed, as its Storage Format is %, but is required to be STORAGE_FORMAT.', data_code, v_covo_code;
   end if;

   RETURN NEW;

END;
$$
    LANGUAGE plpgsql;


--
-- Name: rename_sequence(character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION rename_sequence(old_name character varying, new_name character varying) RETURNS integer
    AS $$
DECLARE
  CURR_SEQ_VAL   INTEGER;
BEGIN
  SELECT INTO CURR_SEQ_VAL NEXTVAL(OLD_NAME);
  EXECUTE 'CREATE SEQUENCE ' || NEW_NAME || ' START WITH ' || CURR_SEQ_VAL;
  EXECUTE 'DROP SEQUENCE ' || OLD_NAME;
  RETURN CURR_SEQ_VAL;
END;
$$
    LANGUAGE plpgsql;


--
-- Name: sample_code_uniqueness_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION sample_code_uniqueness_check() RETURNS trigger
    AS $$
DECLARE
   counter  INTEGER;
BEGIN
	IF (NEW.samp_id_part_of is NULL) THEN
		IF (NEW.dbin_id is not NULL) THEN
			SELECT count(*) into counter FROM samples 
				where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and dbin_id = NEW.dbin_id;
			IF (counter > 0) THEN
				RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code already exists.', NEW.code;
			END IF;
		ELSIF (NEW.grou_id is not NULL) THEN
			SELECT count(*) into counter FROM samples 
				where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and grou_id = NEW.grou_id;
			IF (counter > 0) THEN
				RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because group sample with the same code already exists.', NEW.code;
			END IF;
		END IF;
        ELSE
		IF (NEW.dbin_id is not NULL) THEN
			SELECT count(*) into counter FROM samples 
				where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and dbin_id = NEW.dbin_id;
			IF (counter > 0) THEN
				RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code and being the part of the same parent already exists.', NEW.code;
			END IF;
		ELSIF (NEW.grou_id is not NULL) THEN
			SELECT count(*) into counter FROM samples 
				where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and grou_id = NEW.grou_id;
			IF (counter > 0) THEN
				RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because group sample with the same code and being the part of the same parent already exists.', NEW.code;
			END IF;
		END IF;
        END IF;   
   RETURN NEW;
END;
$$
    LANGUAGE plpgsql;


--
-- Name: controlled_vocabulary_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE controlled_vocabulary_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: controlled_vocabulary_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('controlled_vocabulary_id_seq', 2, true);


--
-- Name: cvte_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE cvte_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: cvte_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('cvte_id_seq', 5, true);


--
-- Name: data_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: data_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_id_seq', 1, false);


--
-- Name: data_set_relationship_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_set_relationship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: data_set_relationship_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_set_relationship_id_seq', 1, false);


--
-- Name: data_set_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_set_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: data_set_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_set_type_id_seq', 3, true);


--
-- Name: data_store_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_store_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: data_store_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_store_id_seq', 1, true);


--
-- Name: data_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: data_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_type_id_seq', 6, true);


--
-- Name: database_instance_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE database_instance_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: database_instance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('database_instance_id_seq', 1, true);


--
-- Name: etpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE etpt_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: etpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('etpt_id_seq', 2, true);


--
-- Name: event_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('event_id_seq', 1, false);


--
-- Name: experiment_attachment_content_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE experiment_attachment_content_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: experiment_attachment_content_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('experiment_attachment_content_id_seq', 3, true);


--
-- Name: experiment_attachment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE experiment_attachment_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: experiment_attachment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('experiment_attachment_id_seq', 3, true);


--
-- Name: experiment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE experiment_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: experiment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('experiment_id_seq', 1, true);


--
-- Name: experiment_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE experiment_property_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: experiment_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('experiment_property_id_seq', 1, true);


--
-- Name: experiment_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE experiment_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: experiment_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('experiment_type_id_seq', 2, true);


--
-- Name: file_format_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE file_format_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: file_format_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('file_format_type_id_seq', 3, true);


--
-- Name: group_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE group_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('group_id_seq', 1, true);


--
-- Name: invalidation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE invalidation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: invalidation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('invalidation_id_seq', 1, false);


--
-- Name: locator_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE locator_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: locator_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('locator_type_id_seq', 1, true);


--
-- Name: material_batch_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE material_batch_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: material_batch_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('material_batch_id_seq', 6, true);


--
-- Name: material_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE material_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: material_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('material_id_seq', 19, true);


--
-- Name: material_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE material_property_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: material_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('material_property_id_seq', 17, true);


--
-- Name: material_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE material_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: material_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('material_type_id_seq', 7, true);


--
-- Name: mtpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE mtpt_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: mtpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('mtpt_id_seq', 9, true);


--
-- Name: person_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE person_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: person_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('person_id_seq', 4, true);


--
-- Name: procedure_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE procedure_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: procedure_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('procedure_id_seq', 1, true);


--
-- Name: procedure_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE procedure_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: procedure_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('procedure_type_id_seq', 3, true);


--
-- Name: project_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE project_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: project_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('project_id_seq', 1, true);


--
-- Name: property_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE property_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: property_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('property_type_id_seq', 6, true);


--
-- Name: role_assignment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE role_assignment_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: role_assignment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('role_assignment_id_seq', 6, true);


--
-- Name: sample_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sample_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: sample_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sample_id_seq', 17, true);


--
-- Name: sample_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sample_property_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: sample_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sample_property_id_seq', 3, true);


--
-- Name: sample_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sample_type_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: sample_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sample_type_id_seq', 6, true);


--
-- Name: stpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE stpt_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: stpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('stpt_id_seq', 2, true);


--
-- Data for Name: controlled_vocabularies; Type: TABLE DATA; Schema: public; Owner: -
--

COPY controlled_vocabularies (id, code, description, registration_timestamp, pers_id_registerer, is_managed_internally, is_internal_namespace, dbin_id) FROM stdin;
1	PLATE_GEOMETRY	The geometry or dimensions of a plate	2009-02-10 15:40:42.184979+01	1	t	t	1
2	STORAGE_FORMAT	The on-disk storage format of a data set	2009-02-10 15:40:42.184979+01	1	t	t	1
\.


--
-- Data for Name: controlled_vocabulary_terms; Type: TABLE DATA; Schema: public; Owner: -
--

COPY controlled_vocabulary_terms (id, code, registration_timestamp, covo_id, pers_id_registerer) FROM stdin;
1	96_WELLS_8X12	2009-02-10 15:40:42.184979+01	1	1
2	384_WELLS_16X24	2009-02-10 15:40:42.184979+01	1	1
3	1536_WELLS_32X48	2009-02-10 15:40:42.184979+01	1	1
4	PROPRIETARY	2009-02-10 15:40:42.184979+01	2	1
5	BDS_DIRECTORY	2009-02-10 15:40:42.184979+01	2	1
\.


--
-- Data for Name: data; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data (id, code, dsty_id, proc_id_produced_by, data_producer_code, production_timestamp, samp_id_acquired_from, samp_id_derived_from, registration_timestamp, is_placeholder, is_deleted, is_valid) FROM stdin;
\.


--
-- Data for Name: data_set_relationships; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_relationships (data_id_parent, data_id_child) FROM stdin;
\.


--
-- Data for Name: data_set_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_types (id, code, description, dbin_id) FROM stdin;
1	UNKNOWN	Unknown	1
2	HCS_IMAGE	High Content Screening Image	1
3	HCS_IMAGE_ANALYSIS_DATA	Data derived from analysis of HCS images	1
\.


--
-- Data for Name: data_stores; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_stores (id, dbin_id, code, download_url, registration_timestamp) FROM stdin;
1	1	STANDARD		2009-02-10 15:40:42.184979+01
\.


--
-- Data for Name: data_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_types (id, code, description) FROM stdin;
1	VARCHAR	Variable length character
2	INTEGER	Integer
3	REAL	Real number, i.e. an inexact, variable-precision numeric type
4	BOOLEAN	An enumerated type with values True and False
5	TIMESTAMP	Both date and time. Format: yyyy-mm-dd hh:mm:ss
6	CONTROLLEDVOCABULARY	Controlled Vocabulary
\.


--
-- Data for Name: database_instances; Type: TABLE DATA; Schema: public; Owner: -
--

COPY database_instances (id, code, uuid, is_original_source, registration_timestamp, dast_id) FROM stdin;
1	CISD	E96C8910-596A-409D-BDA4-BBD3FE6629A7	t	2009-02-10 15:40:42.184979+01	1
\.


--
-- Data for Name: database_version_logs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY database_version_logs (db_version, module_name, run_status, run_status_timestamp, module_code, run_exception) FROM stdin;
028	./sql/generic/028/schema-028.sql	SUCCESS	2009-02-10 15:40:42.141	-- D:\\\\DDL\\\\postgresql\\\\schema-023.sql\\012--\\012-- Generated for ANSI SQL92 on Fri Jul 04  15:13:22 2008 by Server Generator 10.1.2.6.18\\012------------------------------------------------------------------------------------\\012--\\012--  Post-Generation Modifications:\\012--\\012--  1. Changed domain FILE from BIT(32000) to BYTEA\\012--  2. Changed domain TECH_ID from NUMERIC(20) to BIGINT\\012--  3. Changed domain BOOLEAN_CHAR from CHAR(1) DEFAULT F to BOOLEAN DEFAULT FALSE\\012--  4. Removed the check constraints to handle boolean values in Oracle for the\\012--     tables MATERIAL_TYPE_PROPERTY_TYPES, EXPERIMENT_TYPE_PROPERTY_TYPES and\\012--     SAMPLE_TYPE_PROPERTY_TYPES (AVCON_%)\\012--  5. Added the ON DELETE CASCADE qualifier to the foreign keys MAPR_MTPT_FK,\\012--     EXPR_ETPT_FK and SAPR_STPT_FK\\012--  6. Add the check constraint directly on the domain BOOLEAN_CHAR_OR_UNKNOWN\\012--     CREATE DOMAIN BOOLEAN_CHAR_OR_UNKNOWN AS CHAR(1) CHECK (VALUE in ('F', 'T', 'U')) DEFAULT 'U';\\012--  7. Add the WITH TIMEZONE qualifier to the domain TIME_STAMP\\012--     CREATE DOMAIN TIME_STAMP AS TIMESTAMP WITH TIME ZONE;\\012--  8. Add the WITH TIMEZONE and NOT NULL qualifiers to the domain TIME_STAMP_DFL\\012--     CREATE DOMAIN TIME_STAMP_DFL AS TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;\\012--  9. Extend the domain EVENT_TYPE by adding the CHECK constraint\\012--     CREATE DOMAIN EVENT_TYPE AS VARCHAR(40) CHECK (VALUE in ('DELETION', 'INVALIDATION', 'MOVEMENT'));\\012--  10. Extend the domain AUTHORIZATION_ROLE by adding the CHECK constraint\\012--     CREATE DOMAIN AUTHORIZATION_ROLE as VARCHAR(40) CHECK (VALUE IN ('ADMIN', 'USER', 'OBSERVER', 'ETL_SERVER'));\\012--  11. Added the Sequence and Index sections\\012--  12. Added DATABASE_INSTANCES.GLOBAL_CODE column for UUID\\012--  13. DATABASE_INSTANCES.GLOBAL_CODE renamed to DATABASE_INSTANCES.UUID\\012--  14. OBSERVABLE_TYPES renamed to DATA_SET_TYPES\\012--  15. OBSERVABLE_TYPE_ID_SEQ renamed to DATA_SET_TYPE_ID_SEQ\\012--  16. DATA.OBTY_ID renamed to DATA.DSTY_ID;\\012--  17. some others - the source model should be updated to make these Post-Generation Modifications minimal \\012------------------------------------------------------------------------------------\\012\\012-- Creating domains\\012\\012CREATE DOMAIN AUTHORIZATION_ROLE AS VARCHAR(40) CHECK (VALUE IN ('ADMIN', 'USER', 'OBSERVER', 'ETL_SERVER'));\\012CREATE DOMAIN BOOLEAN_CHAR AS BOOLEAN DEFAULT FALSE;\\012CREATE DOMAIN BOOLEAN_CHAR_OR_UNKNOWN AS CHAR(1) CHECK (VALUE IN ('F', 'T', 'U')) DEFAULT 'U';\\012CREATE DOMAIN CODE AS VARCHAR(40);\\012CREATE DOMAIN COLUMN_LABEL AS VARCHAR(40);\\012CREATE DOMAIN DESCRIPTION_1000 AS VARCHAR(1000);\\012CREATE DOMAIN DESCRIPTION_250 AS VARCHAR(250);\\012CREATE DOMAIN DESCRIPTION_80 AS VARCHAR(80);\\012CREATE DOMAIN EVENT_TYPE AS VARCHAR(40) CHECK (VALUE IN ('DELETION', 'INVALIDATION', 'MOVEMENT'));\\012CREATE DOMAIN FILE AS BYTEA;\\012CREATE DOMAIN FILE_NAME AS VARCHAR(100);\\012CREATE DOMAIN GENERIC_VALUE AS VARCHAR(1024);\\012CREATE DOMAIN OBJECT_NAME AS VARCHAR(50);\\012CREATE DOMAIN REAL_VALUE AS REAL;\\012CREATE DOMAIN TECH_ID AS BIGINT;\\012CREATE DOMAIN TIME_STAMP AS TIMESTAMP WITH TIME ZONE;\\012CREATE DOMAIN TIME_STAMP_DFL AS TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;\\012CREATE DOMAIN USER_ID AS VARCHAR(50);\\012\\012-- Creating tables\\012\\012CREATE TABLE CONTROLLED_VOCABULARIES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL,IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_INTERNAL_NAMESPACE BOOLEAN_CHAR NOT NULL DEFAULT 'F',DBIN_ID TECH_ID NOT NULL);\\012CREATE TABLE CONTROLLED_VOCABULARY_TERMS (ID TECH_ID NOT NULL,CODE OBJECT_NAME NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,COVO_ID TECH_ID NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL);\\012CREATE TABLE DATA (ID TECH_ID NOT NULL,CODE CODE,DSTY_ID TECH_ID NOT NULL,PROC_ID_PRODUCED_BY TECH_ID NOT NULL,DATA_PRODUCER_CODE CODE,PRODUCTION_TIMESTAMP TIME_STAMP,SAMP_ID_ACQUIRED_FROM TECH_ID,SAMP_ID_DERIVED_FROM TECH_ID,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,IS_PLACEHOLDER BOOLEAN_CHAR DEFAULT 'F',IS_DELETED BOOLEAN_CHAR DEFAULT 'F',IS_VALID BOOLEAN_CHAR DEFAULT 'T');\\012CREATE TABLE DATABASE_INSTANCES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,UUID CODE NOT NULL,IS_ORIGINAL_SOURCE BOOLEAN_CHAR NOT NULL DEFAULT 'F',REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,DAST_ID TECH_ID);\\012CREATE TABLE DATA_SET_RELATIONSHIPS (DATA_ID_PARENT TECH_ID NOT NULL,DATA_ID_CHILD TECH_ID NOT NULL);\\012CREATE TABLE DATA_STORES (ID TECH_ID NOT NULL,DBIN_ID TECH_ID NOT NULL,CODE CODE NOT NULL,DOWNLOAD_URL VARCHAR(1024) NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE DATA_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80 NOT NULL);\\012CREATE TABLE EVENTS (ID TECH_ID NOT NULL,EVENT_TYPE EVENT_TYPE NOT NULL,DESCRIPTION DESCRIPTION_250,DATA_ID TECH_ID,REASON DESCRIPTION_250,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE EXPERIMENTS (ID TECH_ID NOT NULL,CODE CODE NOT NULL,EXTY_ID TECH_ID NOT NULL,MATE_ID_STUDY_OBJECT TECH_ID,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PROJ_ID TECH_ID NOT NULL,INVA_ID TECH_ID,IS_PUBLIC BOOLEAN_CHAR NOT NULL DEFAULT 'F',DAST_ID TECH_ID);\\012CREATE TABLE EXPERIMENT_ATTACHMENTS (ID TECH_ID NOT NULL,EXPE_ID TECH_ID NOT NULL,EXAC_ID TECH_ID NOT NULL,FILE_NAME FILE_NAME NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,VERSION INTEGER NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL);\\012CREATE TABLE EXPERIMENT_ATTACHMENT_CONTENTS (ID TECH_ID NOT NULL,VALUE FILE NOT NULL);\\012CREATE TABLE EXPERIMENT_PROPERTIES (ID TECH_ID NOT NULL,EXPE_ID TECH_ID NOT NULL,ETPT_ID TECH_ID NOT NULL,VALUE GENERIC_VALUE,CVTE_ID TECH_ID,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE EXPERIMENT_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL);\\012CREATE TABLE EXPERIMENT_TYPE_PROPERTY_TYPES (ID TECH_ID NOT NULL,EXTY_ID TECH_ID NOT NULL,PRTY_ID TECH_ID NOT NULL,IS_MANDATORY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE EXTERNAL_DATA (DATA_ID TECH_ID NOT NULL,LOCATION VARCHAR(1024) NOT NULL,FFTY_ID TECH_ID NOT NULL,LOTY_ID TECH_ID NOT NULL,CVTE_ID_STOR_FMT TECH_ID NOT NULL,IS_COMPLETE BOOLEAN_CHAR_OR_UNKNOWN NOT NULL DEFAULT 'U',CVTE_ID_STORE TECH_ID);\\012CREATE TABLE FILE_FORMAT_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL);\\012CREATE TABLE GROUPS (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DBIN_ID TECH_ID NOT NULL,GROU_ID_PARENT TECH_ID,PERS_ID_LEADER TECH_ID,DESCRIPTION DESCRIPTION_250,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL,DAST_ID TECH_ID);\\012CREATE TABLE INVALIDATIONS (ID TECH_ID NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,REASON DESCRIPTION_250);\\012CREATE TABLE LOCATOR_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80);\\012CREATE TABLE MATERIALS (ID TECH_ID NOT NULL,CODE CODE NOT NULL,MATY_ID TECH_ID NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL,MATE_ID_INHIBITOR_OF TECH_ID,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,DBIN_ID TECH_ID NOT NULL);\\012CREATE TABLE MATERIAL_BATCHES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,AMOUNT REAL_VALUE,MATE_ID TECH_ID NOT NULL,PROC_ID TECH_ID,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL);\\012CREATE TABLE MATERIAL_PROPERTIES (ID TECH_ID NOT NULL,MATE_ID TECH_ID NOT NULL,MTPT_ID TECH_ID NOT NULL,VALUE GENERIC_VALUE,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL,CVTE_ID TECH_ID);\\012CREATE TABLE MATERIAL_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL);\\012CREATE TABLE MATERIAL_TYPE_PROPERTY_TYPES (ID TECH_ID NOT NULL,MATY_ID TECH_ID NOT NULL,PRTY_ID TECH_ID NOT NULL,IS_MANDATORY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL);\\012CREATE TABLE DATA_SET_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL);\\012CREATE TABLE PERSONS (ID TECH_ID NOT NULL,FIRST_NAME VARCHAR(30),LAST_NAME VARCHAR(30),USER_ID USER_ID NOT NULL,EMAIL OBJECT_NAME,DBIN_ID TECH_ID NOT NULL,GROU_ID TECH_ID,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID);\\012CREATE TABLE PROCEDURES (ID TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,EXPE_ID TECH_ID NOT NULL,PCTY_ID TECH_ID NOT NULL);\\012CREATE TABLE PROCEDURE_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80 NOT NULL,IS_DATA_ACQUISITION BOOLEAN_CHAR NOT NULL DEFAULT 'F',DBIN_ID TECH_ID NOT NULL);\\012CREATE TABLE PROJECTS (ID TECH_ID NOT NULL,CODE CODE NOT NULL,GROU_ID TECH_ID NOT NULL,PERS_ID_LEADER TECH_ID,DESCRIPTION DESCRIPTION_1000,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,DAST_ID TECH_ID);\\012CREATE TABLE PROPERTY_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80 NOT NULL,LABEL COLUMN_LABEL NOT NULL,DATY_ID TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL,COVO_ID TECH_ID,IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_INTERNAL_NAMESPACE BOOLEAN_CHAR NOT NULL DEFAULT 'F',DBIN_ID TECH_ID NOT NULL);\\012CREATE TABLE ROLE_ASSIGNMENTS (ID TECH_ID NOT NULL,ROLE_CODE AUTHORIZATION_ROLE NOT NULL,GROU_ID TECH_ID,DBIN_ID TECH_ID,PERS_ID_GRANTEE TECH_ID NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE SAMPLES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,SAMP_ID_TOP TECH_ID,SAMP_ID_GENERATED_FROM TECH_ID,SATY_ID TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL,INVA_ID TECH_ID,SAMP_ID_CONTROL_LAYOUT TECH_ID,DBIN_ID TECH_ID,GROU_ID TECH_ID,SAMP_ID_PART_OF TECH_ID);\\012CREATE TABLE SAMPLE_INPUTS (SAMP_ID TECH_ID NOT NULL,PROC_ID TECH_ID NOT NULL);\\012CREATE TABLE SAMPLE_MATERIAL_BATCHES (SAMP_ID TECH_ID NOT NULL,MABA_ID TECH_ID NOT NULL);\\012CREATE TABLE SAMPLE_PROPERTIES (ID TECH_ID NOT NULL,SAMP_ID TECH_ID NOT NULL,STPT_ID TECH_ID NOT NULL,VALUE GENERIC_VALUE,CVTE_ID TECH_ID,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE SAMPLE_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL, IS_LISTABLE BOOLEAN_CHAR NOT NULL DEFAULT 'T', GENERATED_FROM_DEPTH INTEGER NOT NULL DEFAULT 0, PART_OF_DEPTH INTEGER NOT NULL DEFAULT 0);\\012CREATE TABLE SAMPLE_TYPE_PROPERTY_TYPES (ID TECH_ID NOT NULL,SATY_ID TECH_ID NOT NULL,PRTY_ID TECH_ID NOT NULL,IS_MANDATORY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP, IS_DISPLAYED BOOLEAN_CHAR NOT NULL DEFAULT 'T');\\012\\012-- Creating sequences\\012\\012CREATE SEQUENCE CONTROLLED_VOCABULARY_ID_SEQ;\\012CREATE SEQUENCE CVTE_ID_SEQ;\\012CREATE SEQUENCE DATABASE_INSTANCE_ID_SEQ;\\012CREATE SEQUENCE DATA_ID_SEQ;\\012CREATE SEQUENCE DATA_SET_RELATIONSHIP_ID_SEQ;\\012CREATE SEQUENCE DATA_STORE_ID_SEQ;\\012CREATE SEQUENCE DATA_TYPE_ID_SEQ;\\012CREATE SEQUENCE ETPT_ID_SEQ;\\012CREATE SEQUENCE EVENT_ID_SEQ;\\012CREATE SEQUENCE EXPERIMENT_ATTACHMENT_ID_SEQ;\\012CREATE SEQUENCE EXPERIMENT_ATTACHMENT_CONTENT_ID_SEQ;\\012CREATE SEQUENCE EXPERIMENT_ID_SEQ;\\012CREATE SEQUENCE EXPERIMENT_PROPERTY_ID_SEQ;\\012CREATE SEQUENCE EXPERIMENT_TYPE_ID_SEQ;\\012CREATE SEQUENCE FILE_FORMAT_TYPE_ID_SEQ;\\012CREATE SEQUENCE GROUP_ID_SEQ;\\012CREATE SEQUENCE INVALIDATION_ID_SEQ;\\012CREATE SEQUENCE LOCATOR_TYPE_ID_SEQ;\\012CREATE SEQUENCE MATERIAL_BATCH_ID_SEQ;\\012CREATE SEQUENCE MATERIAL_ID_SEQ;\\012CREATE SEQUENCE MATERIAL_PROPERTY_ID_SEQ;\\012CREATE SEQUENCE MATERIAL_TYPE_ID_SEQ;\\012CREATE SEQUENCE MTPT_ID_SEQ;\\012CREATE SEQUENCE DATA_SET_TYPE_ID_SEQ;\\012CREATE SEQUENCE PERSON_ID_SEQ;\\012CREATE SEQUENCE PROCEDURE_ID_SEQ;\\012CREATE SEQUENCE PROCEDURE_TYPE_ID_SEQ;\\012CREATE SEQUENCE PROJECT_ID_SEQ;\\012CREATE SEQUENCE PROPERTY_TYPE_ID_SEQ;\\012CREATE SEQUENCE ROLE_ASSIGNMENT_ID_SEQ;\\012CREATE SEQUENCE SAMPLE_ID_SEQ;\\012CREATE SEQUENCE SAMPLE_PROPERTY_ID_SEQ;\\012CREATE SEQUENCE SAMPLE_TYPE_ID_SEQ;\\012CREATE SEQUENCE STPT_ID_SEQ;\\012\\012-- Creating primary key constraints\\012\\012ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_PK PRIMARY KEY(ID);\\012ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_PK PRIMARY KEY(ID);\\012ALTER TABLE DATA ADD CONSTRAINT DATA_PK PRIMARY KEY(ID);\\012ALTER TABLE DATABASE_INSTANCES ADD CONSTRAINT DBIN_PK PRIMARY KEY(ID);\\012ALTER TABLE DATA_STORES ADD CONSTRAINT DAST_PK PRIMARY KEY(ID);\\012ALTER TABLE DATA_TYPES ADD CONSTRAINT DATY_PK PRIMARY KEY(ID);\\012ALTER TABLE EVENTS ADD CONSTRAINT EVNT_PK PRIMARY KEY(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_PK PRIMARY KEY(ID);\\012ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_PK PRIMARY KEY(ID);\\012ALTER TABLE EXPERIMENT_ATTACHMENT_CONTENTS ADD CONSTRAINT EXAC_PK PRIMARY KEY(ID);\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_PK PRIMARY KEY(ID);\\012ALTER TABLE EXPERIMENT_TYPES ADD CONSTRAINT EXTY_PK PRIMARY KEY(ID);\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_PK PRIMARY KEY(ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_PK PRIMARY KEY(DATA_ID);\\012ALTER TABLE FILE_FORMAT_TYPES ADD CONSTRAINT FFTY_PK PRIMARY KEY(ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_PK PRIMARY KEY(ID);\\012ALTER TABLE INVALIDATIONS ADD CONSTRAINT INVA_PK PRIMARY KEY(ID);\\012ALTER TABLE LOCATOR_TYPES ADD CONSTRAINT LOTY_PK PRIMARY KEY(ID);\\012ALTER TABLE MATERIALS ADD CONSTRAINT MATE_PK PRIMARY KEY(ID);\\012ALTER TABLE MATERIAL_BATCHES ADD CONSTRAINT MABA_PK PRIMARY KEY(ID);\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_PK PRIMARY KEY(ID);\\012ALTER TABLE MATERIAL_TYPES ADD CONSTRAINT MATY_PK PRIMARY KEY(ID);\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_PK PRIMARY KEY(ID);\\012ALTER TABLE DATA_SET_TYPES ADD CONSTRAINT OBTY_PK PRIMARY KEY(ID);\\012ALTER TABLE PERSONS ADD CONSTRAINT PERS_PK PRIMARY KEY(ID);\\012ALTER TABLE PROCEDURES ADD CONSTRAINT PROC_PK PRIMARY KEY(ID);\\012ALTER TABLE PROCEDURE_TYPES ADD CONSTRAINT PCTY_PK PRIMARY KEY(ID);\\012ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_PK PRIMARY KEY(ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_PK PRIMARY KEY(ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_PK PRIMARY KEY(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_PK PRIMARY KEY(ID);\\012ALTER TABLE SAMPLE_INPUTS ADD CONSTRAINT SAIN_PK PRIMARY KEY(PROC_ID,SAMP_ID);\\012ALTER TABLE SAMPLE_MATERIAL_BATCHES ADD CONSTRAINT SAMB_PK PRIMARY KEY(SAMP_ID,MABA_ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_PK PRIMARY KEY(ID);\\012ALTER TABLE SAMPLE_TYPES ADD CONSTRAINT SATY_PK PRIMARY KEY(ID);\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_PK PRIMARY KEY(ID);\\012\\012-- Creating unique constraints\\012\\012ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_BK_UK UNIQUE(CODE,IS_INTERNAL_NAMESPACE,DBIN_ID);\\012ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_BK_UK UNIQUE(CODE,COVO_ID);\\012ALTER TABLE DATA ADD CONSTRAINT DATA_BK_UK UNIQUE(CODE);\\012ALTER TABLE DATABASE_INSTANCES ADD CONSTRAINT DBIN_BK_UK UNIQUE(CODE);\\012ALTER TABLE DATABASE_INSTANCES ADD CONSTRAINT DBIN_UUID_UK UNIQUE(UUID);\\012ALTER TABLE DATA_SET_RELATIONSHIPS ADD CONSTRAINT DSRE_BK_UK UNIQUE(DATA_ID_CHILD,DATA_ID_PARENT);\\012ALTER TABLE DATA_STORES ADD CONSTRAINT DAST_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE DATA_TYPES ADD CONSTRAINT DATY_BK_UK UNIQUE(CODE);\\012ALTER TABLE EVENTS ADD CONSTRAINT EVNT_BK_UK UNIQUE(EVENT_TYPE,DATA_ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_BK_UK UNIQUE(CODE,PROJ_ID);\\012ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_BK_UK UNIQUE(EXPE_ID,FILE_NAME,VERSION);\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_BK_UK UNIQUE(EXPE_ID,ETPT_ID);\\012ALTER TABLE EXPERIMENT_TYPES ADD CONSTRAINT EXTY_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_BK_UK UNIQUE(EXTY_ID,PRTY_ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_BK_UK UNIQUE(LOCATION,LOTY_ID);\\012ALTER TABLE FILE_FORMAT_TYPES ADD CONSTRAINT FFTY_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE LOCATOR_TYPES ADD CONSTRAINT LOTY_BK_UK UNIQUE(CODE);\\012ALTER TABLE MATERIALS ADD CONSTRAINT MATE_BK_UK UNIQUE(CODE,MATY_ID,DBIN_ID);\\012ALTER TABLE MATERIAL_BATCHES ADD CONSTRAINT MABA_BK_UK UNIQUE(CODE,MATE_ID);\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_BK_UK UNIQUE(MATE_ID,MTPT_ID);\\012ALTER TABLE MATERIAL_TYPES ADD CONSTRAINT MATY_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_BK_UK UNIQUE(MATY_ID,PRTY_ID);\\012ALTER TABLE DATA_SET_TYPES ADD CONSTRAINT OBTY_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE PERSONS ADD CONSTRAINT PERS_BK_UK UNIQUE(DBIN_ID,USER_ID);\\012ALTER TABLE PROCEDURE_TYPES ADD CONSTRAINT PCTY_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_BK_UK UNIQUE(CODE,GROU_ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_BK_UK UNIQUE(CODE,IS_INTERNAL_NAMESPACE,DBIN_ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_GROUP_BK_UK UNIQUE(PERS_ID_GRANTEE,ROLE_CODE,GROU_ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_INSTANCE_BK_UK UNIQUE(PERS_ID_GRANTEE,ROLE_CODE,DBIN_ID);\\012ALTER TABLE SAMPLE_INPUTS ADD CONSTRAINT SAIN_BK_UK UNIQUE(SAMP_ID,PROC_ID);\\012ALTER TABLE SAMPLE_MATERIAL_BATCHES ADD CONSTRAINT SAMB_BK_UK UNIQUE(MABA_ID,SAMP_ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_BK_UK UNIQUE(SAMP_ID,STPT_ID);\\012ALTER TABLE SAMPLE_TYPES ADD CONSTRAINT SATY_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_BK_UK UNIQUE(SATY_ID,PRTY_ID);\\012\\012-- Creating foreign key constraints\\012\\012ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_COVO_FK FOREIGN KEY (COVO_ID) REFERENCES CONTROLLED_VOCABULARIES(ID);\\012ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE DATA ADD CONSTRAINT DATA_OBTY_FK FOREIGN KEY (DSTY_ID) REFERENCES DATA_SET_TYPES(ID);\\012ALTER TABLE DATA ADD CONSTRAINT DATA_PROC_PRODUCED_BY_FK FOREIGN KEY (PROC_ID_PRODUCED_BY) REFERENCES PROCEDURES(ID);\\012ALTER TABLE DATA ADD CONSTRAINT DATA_SAMP_FK_ACQUIRED_FROM FOREIGN KEY (SAMP_ID_ACQUIRED_FROM) REFERENCES SAMPLES(ID);\\012ALTER TABLE DATA ADD CONSTRAINT DATA_SAMP_FK_DERIVED_FROM FOREIGN KEY (SAMP_ID_DERIVED_FROM) REFERENCES SAMPLES(ID);\\012ALTER TABLE DATABASE_INSTANCES ADD CONSTRAINT DBIN_DAST_FK FOREIGN KEY (DAST_ID) REFERENCES DATA_STORES(ID);\\012ALTER TABLE DATA_SET_RELATIONSHIPS ADD CONSTRAINT DSRE_DATA_FK_CHILD FOREIGN KEY (DATA_ID_CHILD) REFERENCES DATA(ID);\\012ALTER TABLE DATA_SET_RELATIONSHIPS ADD CONSTRAINT DSRE_DATA_FK_PARENT FOREIGN KEY (DATA_ID_PARENT) REFERENCES DATA(ID);\\012ALTER TABLE DATA_STORES ADD CONSTRAINT DAST_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE EVENTS ADD CONSTRAINT EVNT_DATA_FK FOREIGN KEY (DATA_ID) REFERENCES DATA(ID);\\012ALTER TABLE EVENTS ADD CONSTRAINT EVNT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_EXTY_FK FOREIGN KEY (EXTY_ID) REFERENCES EXPERIMENT_TYPES(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_INVA_FK FOREIGN KEY (INVA_ID) REFERENCES INVALIDATIONS(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_MATE_FK FOREIGN KEY (MATE_ID_STUDY_OBJECT) REFERENCES MATERIALS(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_PROJ_FK FOREIGN KEY (PROJ_ID) REFERENCES PROJECTS(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_DAST_FK FOREIGN KEY (DAST_ID) REFERENCES DATA_STORES(ID);\\012ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_EXPE_FK FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS(ID);\\012ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_CONT_FK FOREIGN KEY (EXAC_ID) REFERENCES EXPERIMENT_ATTACHMENT_CONTENTS(ID);\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_ETPT_FK FOREIGN KEY (ETPT_ID) REFERENCES EXPERIMENT_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_EXPE_FK FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS(ID);\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE EXPERIMENT_TYPES ADD CONSTRAINT EXTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_EXTY_FK FOREIGN KEY (EXTY_ID) REFERENCES EXPERIMENT_TYPES(ID);\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_CVTE_FK FOREIGN KEY (CVTE_ID_STOR_FMT) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_CVTE_STORED_ON_FK FOREIGN KEY (CVTE_ID_STORE) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_DATA_FK FOREIGN KEY (DATA_ID) REFERENCES DATA(ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_FFTY_FK FOREIGN KEY (FFTY_ID) REFERENCES FILE_FORMAT_TYPES(ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_LOTY_FK FOREIGN KEY (LOTY_ID) REFERENCES LOCATOR_TYPES(ID);\\012ALTER TABLE FILE_FORMAT_TYPES ADD CONSTRAINT FFTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_GROU_FK FOREIGN KEY (GROU_ID_PARENT) REFERENCES GROUPS(ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_PERS_FK_LEADER FOREIGN KEY (PERS_ID_LEADER) REFERENCES PERSONS(ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_PERS_FK_REGISTERER FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_DAST_FK FOREIGN KEY (DAST_ID) REFERENCES DATA_STORES(ID);\\012ALTER TABLE INVALIDATIONS ADD CONSTRAINT INVA_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE MATERIALS ADD CONSTRAINT MATE_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE MATERIALS ADD CONSTRAINT MATE_MATE_FK FOREIGN KEY (MATE_ID_INHIBITOR_OF) REFERENCES MATERIALS(ID);\\012ALTER TABLE MATERIALS ADD CONSTRAINT MATE_MATY_FK FOREIGN KEY (MATY_ID) REFERENCES MATERIAL_TYPES(ID);\\012ALTER TABLE MATERIALS ADD CONSTRAINT MATE_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE MATERIAL_BATCHES ADD CONSTRAINT MABA_MATE_FK FOREIGN KEY (MATE_ID) REFERENCES MATERIALS(ID);\\012ALTER TABLE MATERIAL_BATCHES ADD CONSTRAINT MABA_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE MATERIAL_BATCHES ADD CONSTRAINT MABA_PROC_FK FOREIGN KEY (PROC_ID) REFERENCES PROCEDURES(ID);\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_MATE_FK FOREIGN KEY (MATE_ID) REFERENCES MATERIALS(ID);\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_MTPT_FK FOREIGN KEY (MTPT_ID) REFERENCES MATERIAL_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE MATERIAL_TYPES ADD CONSTRAINT MATY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_MATY_FK FOREIGN KEY (MATY_ID) REFERENCES MATERIAL_TYPES(ID);\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID);\\012ALTER TABLE DATA_SET_TYPES ADD CONSTRAINT OBTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE PERSONS ADD CONSTRAINT PERS_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE PERSONS ADD CONSTRAINT PERS_GROU_FK FOREIGN KEY (GROU_ID) REFERENCES GROUPS(ID);\\012ALTER TABLE PERSONS ADD CONSTRAINT PERS_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE PROCEDURES ADD CONSTRAINT PROC_EXPE_FK FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS(ID);\\012ALTER TABLE PROCEDURES ADD CONSTRAINT PROC_PCTY_FK FOREIGN KEY (PCTY_ID) REFERENCES PROCEDURE_TYPES(ID);\\012ALTER TABLE PROCEDURE_TYPES ADD CONSTRAINT PCTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_GROU_FK FOREIGN KEY (GROU_ID) REFERENCES GROUPS(ID);\\012ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_PERS_FK_LEADER FOREIGN KEY (PERS_ID_LEADER) REFERENCES PERSONS(ID);\\012ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_PERS_FK_REGISTERER FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_DAST_FK FOREIGN KEY (DAST_ID) REFERENCES DATA_STORES(ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_COVO_FK FOREIGN KEY (COVO_ID) REFERENCES CONTROLLED_VOCABULARIES(ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_DATY_FK FOREIGN KEY (DATY_ID) REFERENCES DATA_TYPES(ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_GROU_FK FOREIGN KEY (GROU_ID) REFERENCES GROUPS(ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_PERS_FK_GRANTEE FOREIGN KEY (PERS_ID_GRANTEE) REFERENCES PERSONS(ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_PERS_FK_REGISTERER FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_GROU_FK FOREIGN KEY (GROU_ID) REFERENCES GROUPS(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_INVA_FK FOREIGN KEY (INVA_ID) REFERENCES INVALIDATIONS(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SAMP_FK_CONTROL_LAYOUT FOREIGN KEY (SAMP_ID_CONTROL_LAYOUT) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SAMP_FK_GENERATED_FROM FOREIGN KEY (SAMP_ID_GENERATED_FROM) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SAMP_FK_PART_OF FOREIGN KEY (SAMP_ID_PART_OF) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SAMP_FK_TOP FOREIGN KEY (SAMP_ID_TOP) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SATY_FK FOREIGN KEY (SATY_ID) REFERENCES SAMPLE_TYPES(ID);\\012ALTER TABLE SAMPLE_INPUTS ADD CONSTRAINT SAIN_PROC_FK FOREIGN KEY (PROC_ID) REFERENCES PROCEDURES(ID);\\012ALTER TABLE SAMPLE_INPUTS ADD CONSTRAINT SAIN_SAMP_FK FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLE_MATERIAL_BATCHES ADD CONSTRAINT SAMB_MABA_FK FOREIGN KEY (MABA_ID) REFERENCES MATERIAL_BATCHES(ID);\\012ALTER TABLE SAMPLE_MATERIAL_BATCHES ADD CONSTRAINT SAMB_SAMP_FK FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_SAMP_FK FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_STPT_FK FOREIGN KEY (STPT_ID) REFERENCES SAMPLE_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE SAMPLE_TYPES ADD CONSTRAINT SATY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID);\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_SATY_FK FOREIGN KEY (SATY_ID) REFERENCES SAMPLE_TYPES(ID);\\012\\012-- Creating check constraints\\012\\012ALTER TABLE DATA ADD CONSTRAINT DATA_SAMP_ARC_CK CHECK ((SAMP_ID_ACQUIRED_FROM IS NOT NULL AND SAMP_ID_DERIVED_FROM IS NULL) OR (SAMP_ID_ACQUIRED_FROM IS NULL AND SAMP_ID_DERIVED_FROM IS NOT NULL));\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_CK CHECK ((VALUE IS NOT NULL AND CVTE_ID IS NULL) OR (VALUE IS NULL AND CVTE_ID IS NOT NULL));\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_CK CHECK ((VALUE IS NOT NULL AND CVTE_ID IS NULL) OR (VALUE IS NULL AND CVTE_ID IS NOT NULL));\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_DBIN_GROU_ARC_CK CHECK ((DBIN_ID IS NOT NULL AND GROU_ID IS NULL) OR (DBIN_ID IS NULL AND GROU_ID IS NOT NULL));\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_DBIN_GROU_ARC_CK CHECK ((DBIN_ID IS NOT NULL AND GROU_ID IS NULL) OR (DBIN_ID IS NULL AND GROU_ID IS NOT NULL));\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_CK CHECK ((VALUE IS NOT NULL AND CVTE_ID IS NULL) OR (VALUE IS NULL AND CVTE_ID IS NOT NULL));\\012\\012-- Creating indices\\012\\012CREATE INDEX COVO_PERS_FK_I ON CONTROLLED_VOCABULARIES (PERS_ID_REGISTERER);\\012CREATE INDEX CVTE_COVO_FK_I ON CONTROLLED_VOCABULARY_TERMS (COVO_ID);\\012CREATE INDEX CVTE_PERS_FK_I ON CONTROLLED_VOCABULARY_TERMS (PERS_ID_REGISTERER);\\012CREATE INDEX DATA_OBTY_FK_I ON DATA (DSTY_ID);\\012CREATE INDEX DATA_PROC_FK_I ON DATA (PROC_ID_PRODUCED_BY);\\012CREATE INDEX DATA_SAMP_FK_I_ACQUIRED_FROM ON DATA (SAMP_ID_ACQUIRED_FROM);\\012CREATE INDEX DATA_SAMP_FK_I_DERIVED_FROM ON DATA (SAMP_ID_DERIVED_FROM);\\012CREATE INDEX DAST_DBIN_FK_I ON DATA_STORES (DBIN_ID);\\012CREATE INDEX DSRE_DATA_FK_I_CHILD ON DATA_SET_RELATIONSHIPS (DATA_ID_CHILD);\\012CREATE INDEX DSRE_DATA_FK_I_PARENT ON DATA_SET_RELATIONSHIPS (DATA_ID_PARENT);\\012CREATE INDEX ETPT_EXTY_FK_I ON EXPERIMENT_TYPE_PROPERTY_TYPES (EXTY_ID);\\012CREATE INDEX ETPT_PERS_FK_I ON EXPERIMENT_TYPE_PROPERTY_TYPES (PERS_ID_REGISTERER);\\012CREATE INDEX ETPT_PRTY_FK_I ON EXPERIMENT_TYPE_PROPERTY_TYPES (PRTY_ID);\\012CREATE INDEX EVNT_DATA_FK_I ON EVENTS (DATA_ID);\\012CREATE INDEX EVNT_PERS_FK_I ON EVENTS (PERS_ID_REGISTERER);\\012CREATE INDEX EXAT_EXPE_FK_I ON EXPERIMENT_ATTACHMENTS (EXPE_ID);\\012CREATE INDEX EXAT_PERS_FK_I ON EXPERIMENT_ATTACHMENTS (PERS_ID_REGISTERER);\\012CREATE INDEX EXAT_EXAC_FK_I ON EXPERIMENT_ATTACHMENTS (EXAC_ID);\\012CREATE INDEX EXDA_CVTE_FK_I ON EXTERNAL_DATA (CVTE_ID_STOR_FMT);\\012CREATE INDEX EXDA_CVTE_STORED_ON_FK_I ON EXTERNAL_DATA (CVTE_ID_STORE);\\012CREATE INDEX EXDA_FFTY_FK_I ON EXTERNAL_DATA (FFTY_ID);\\012CREATE INDEX EXDA_LOTY_FK_I ON EXTERNAL_DATA (LOTY_ID);\\012CREATE INDEX EXPE_EXTY_FK_I ON EXPERIMENTS (EXTY_ID);\\012CREATE INDEX EXPE_INVA_FK_I ON EXPERIMENTS (INVA_ID);\\012CREATE INDEX EXPE_MATE_FK_I ON EXPERIMENTS (MATE_ID_STUDY_OBJECT);\\012CREATE INDEX EXPE_PERS_FK_I ON EXPERIMENTS (PERS_ID_REGISTERER);\\012CREATE INDEX EXPE_PROJ_FK_I ON EXPERIMENTS (PROJ_ID);\\012CREATE INDEX EXPR_CVTE_FK_I ON EXPERIMENT_PROPERTIES (CVTE_ID);\\012CREATE INDEX EXPR_ETPT_FK_I ON EXPERIMENT_PROPERTIES (ETPT_ID);\\012CREATE INDEX EXPR_EXPE_FK_I ON EXPERIMENT_PROPERTIES (EXPE_ID);\\012CREATE INDEX EXPR_PERS_FK_I ON EXPERIMENT_PROPERTIES (PERS_ID_REGISTERER);\\012CREATE INDEX GROU_DBIN_FK_I ON GROUPS (DBIN_ID);\\012CREATE INDEX GROU_GROU_FK_I ON GROUPS (GROU_ID_PARENT);\\012CREATE INDEX GROU_PERS_FK_I ON GROUPS (PERS_ID_LEADER);\\012CREATE INDEX GROU_PERS_REGISTERED_BY_FK_I ON GROUPS (PERS_ID_REGISTERER);\\012CREATE INDEX INVA_PERS_FK_I ON INVALIDATIONS (PERS_ID_REGISTERER);\\012CREATE INDEX MABA_MATE_FK_I ON MATERIAL_BATCHES (MATE_ID);\\012CREATE INDEX MABA_PERS_FK_I ON MATERIAL_BATCHES (PERS_ID_REGISTERER);\\012CREATE INDEX MABA_PROC_FK_I ON MATERIAL_BATCHES (PROC_ID);\\012CREATE INDEX MAPR_CVTE_FK_I ON MATERIAL_PROPERTIES (CVTE_ID);\\012CREATE INDEX MAPR_MATE_FK_I ON MATERIAL_PROPERTIES (MATE_ID);\\012CREATE INDEX MAPR_MTPT_FK_I ON MATERIAL_PROPERTIES (MTPT_ID);\\012CREATE INDEX MAPR_PERS_FK_I ON MATERIAL_PROPERTIES (PERS_ID_REGISTERER);\\012CREATE INDEX MATE_MATE_FK_I ON MATERIALS (MATE_ID_INHIBITOR_OF);\\012CREATE INDEX MATE_MATY_FK_I ON MATERIALS (MATY_ID);\\012CREATE INDEX MATE_PERS_FK_I ON MATERIALS (PERS_ID_REGISTERER);\\012CREATE INDEX MTPT_MATY_FK_I ON MATERIAL_TYPE_PROPERTY_TYPES (MATY_ID);\\012CREATE INDEX MTPT_PERS_FK_I ON MATERIAL_TYPE_PROPERTY_TYPES (PERS_ID_REGISTERER);\\012CREATE INDEX MTPT_PRTY_FK_I ON MATERIAL_TYPE_PROPERTY_TYPES (PRTY_ID);\\012CREATE INDEX PERS_GROU_FK_I ON PERSONS (GROU_ID);\\012CREATE INDEX PROC_EXPE_FK_I ON PROCEDURES (EXPE_ID);\\012CREATE INDEX PROC_PCTY_FK_I ON PROCEDURES (PCTY_ID);\\012CREATE INDEX PROJ_GROU_FK_I ON PROJECTS (GROU_ID);\\012CREATE INDEX PROJ_PERS_FK_I_LEADER ON PROJECTS (PERS_ID_LEADER);\\012CREATE INDEX PROJ_PERS_FK_I_REGISTERER ON PROJECTS (PERS_ID_REGISTERER);\\012CREATE INDEX PRTY_COVO_FK_I ON PROPERTY_TYPES (COVO_ID);\\012CREATE INDEX PRTY_DATY_FK_I ON PROPERTY_TYPES (DATY_ID);\\012CREATE INDEX PRTY_PERS_FK_I ON PROPERTY_TYPES (PERS_ID_REGISTERER);\\012CREATE INDEX ROAS_DBIN_FK_I ON ROLE_ASSIGNMENTS (DBIN_ID);\\012CREATE INDEX ROAS_GROU_FK_I ON ROLE_ASSIGNMENTS (GROU_ID);\\012CREATE INDEX ROAS_PERS_FK_I_GRANTEE ON ROLE_ASSIGNMENTS (PERS_ID_GRANTEE);\\012CREATE INDEX ROAS_PERS_FK_I_REGISTERER ON ROLE_ASSIGNMENTS (PERS_ID_REGISTERER);\\012CREATE INDEX SAIN_PROC_FK_I ON SAMPLE_INPUTS (PROC_ID);\\012CREATE INDEX SAIN_SAMP_FK_I ON SAMPLE_INPUTS (SAMP_ID);\\012CREATE INDEX SAMB_MABA_FK_I ON SAMPLE_MATERIAL_BATCHES (MABA_ID);\\012CREATE INDEX SAMB_SAMP_FK_I ON SAMPLE_MATERIAL_BATCHES (SAMP_ID);\\012CREATE INDEX SAMP_INVA_FK_I ON SAMPLES (INVA_ID);\\012CREATE INDEX SAMP_PERS_FK_I ON SAMPLES (PERS_ID_REGISTERER);\\012CREATE INDEX SAMP_SAMP_FK_I_CONTROL_LAYOUT ON SAMPLES (SAMP_ID_CONTROL_LAYOUT);\\012CREATE INDEX SAMP_SAMP_FK_I_GENERATED_FROM ON SAMPLES (SAMP_ID_GENERATED_FROM);\\012CREATE INDEX SAMP_SAMP_FK_I_PART_OF ON SAMPLES (SAMP_ID_PART_OF);\\012CREATE INDEX SAMP_SAMP_FK_I_TOP ON SAMPLES (SAMP_ID_TOP);\\012CREATE INDEX SAMP_CODE_I ON SAMPLES (CODE);\\012CREATE INDEX SAMP_SATY_FK_I ON SAMPLES (SATY_ID);\\012CREATE INDEX SAPR_CVTE_FK_I ON SAMPLE_PROPERTIES (CVTE_ID);\\012CREATE INDEX SAPR_PERS_FK_I ON SAMPLE_PROPERTIES (PERS_ID_REGISTERER);\\012CREATE INDEX SAPR_SAMP_FK_I ON SAMPLE_PROPERTIES (SAMP_ID);\\012CREATE INDEX SAPR_STPT_FK_I ON SAMPLE_PROPERTIES (STPT_ID);\\012CREATE INDEX STPT_PERS_FK_I ON SAMPLE_TYPE_PROPERTY_TYPES (PERS_ID_REGISTERER);\\012CREATE INDEX STPT_PRTY_FK_I ON SAMPLE_TYPE_PROPERTY_TYPES (PRTY_ID);\\012CREATE INDEX STPT_SATY_FK_I ON SAMPLE_TYPE_PROPERTY_TYPES (SATY_ID);\\012	\N
028	./sql/postgresql/028/function-028.sql	SUCCESS	2009-02-10 15:40:42.168	-- Creating Functions\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Create function RENAME_SEQUENCE() that is required for renaming the sequences belonging to tables\\012------------------------------------------------------------------------------------\\012CREATE FUNCTION RENAME_SEQUENCE(OLD_NAME VARCHAR, NEW_NAME VARCHAR) RETURNS INTEGER AS $$\\012DECLARE\\012  CURR_SEQ_VAL   INTEGER;\\012BEGIN\\012  SELECT INTO CURR_SEQ_VAL NEXTVAL(OLD_NAME);\\012  EXECUTE 'CREATE SEQUENCE ' || NEW_NAME || ' START WITH ' || CURR_SEQ_VAL;\\012  EXECUTE 'DROP SEQUENCE ' || OLD_NAME;\\012  RETURN CURR_SEQ_VAL;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Create trigger CONTROLLED_VOCABULARY_CHECK \\012------------------------------------------------------------------------------------\\012\\012CREATE OR REPLACE FUNCTION CONTROLLED_VOCABULARY_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   v_code  CODE;\\012BEGIN\\012\\012   select code into v_code from data_types where id = NEW.daty_id;\\012\\012   -- Check if the data is of type "CONTROLLEDVOCABULARY"\\012   if v_code = 'CONTROLLEDVOCABULARY' then\\012      if NEW.covo_id IS NULL then\\012         RAISE EXCEPTION 'Insert/Update of Property Type (Code: %) failed, as its Data Type is CONTROLLEDVOCABULARY, but it is not linked to a Controlled Vocabulary.', NEW.code;\\012      end if;\\012   end if;\\012\\012   RETURN NEW;\\012\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012CREATE TRIGGER CONTROLLED_VOCABULARY_CHECK BEFORE INSERT OR UPDATE ON PROPERTY_TYPES\\012    FOR EACH ROW EXECUTE PROCEDURE CONTROLLED_VOCABULARY_CHECK();\\012\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Create trigger EXTERNAL_DATA_STORAGE_FORMAT_CHECK \\012------------------------------------------------------------------------------------\\012\\012CREATE OR REPLACE FUNCTION EXTERNAL_DATA_STORAGE_FORMAT_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   v_covo_code  CODE;\\012   data_code CODE;\\012BEGIN\\012\\012   select code into v_covo_code from controlled_vocabularies\\012      where is_internal_namespace = true and \\012         id = (select covo_id from controlled_vocabulary_terms where id = NEW.cvte_id_stor_fmt);\\012   -- Check if the data storage format is a term of the controlled vocabulary "STORAGE_FORMAT"\\012   if v_covo_code != 'STORAGE_FORMAT' then\\012      select code into data_code from data where id = NEW.data_id; \\012      RAISE EXCEPTION 'Insert/Update of Data (Code: %) failed, as its Storage Format is %, but is required to be STORAGE_FORMAT.', data_code, v_covo_code;\\012   end if;\\012\\012   RETURN NEW;\\012\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012CREATE TRIGGER EXTERNAL_DATA_STORAGE_FORMAT_CHECK BEFORE INSERT OR UPDATE ON EXTERNAL_DATA\\012    FOR EACH ROW EXECUTE PROCEDURE EXTERNAL_DATA_STORAGE_FORMAT_CHECK();\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Create trigger SAMPLE_CODE_UNIQUENESS_CHECK \\012------------------------------------------------------------------------------------\\012\\012CREATE OR REPLACE FUNCTION SAMPLE_CODE_UNIQUENESS_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   counter  INTEGER;\\012BEGIN\\012\\011IF (NEW.samp_id_part_of is NULL) THEN\\012\\011\\011IF (NEW.dbin_id is not NULL) THEN\\012\\011\\011\\011SELECT count(*) into counter FROM samples \\012\\011\\011\\011\\011where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and dbin_id = NEW.dbin_id;\\012\\011\\011\\011IF (counter > 0) THEN\\012\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code already exists.', NEW.code;\\012\\011\\011\\011END IF;\\012\\011\\011ELSIF (NEW.grou_id is not NULL) THEN\\012\\011\\011\\011SELECT count(*) into counter FROM samples \\012\\011\\011\\011\\011where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and grou_id = NEW.grou_id;\\012\\011\\011\\011IF (counter > 0) THEN\\012\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because group sample with the same code already exists.', NEW.code;\\012\\011\\011\\011END IF;\\012\\011\\011END IF;\\012        ELSE\\012\\011\\011IF (NEW.dbin_id is not NULL) THEN\\012\\011\\011\\011SELECT count(*) into counter FROM samples \\012\\011\\011\\011\\011where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and dbin_id = NEW.dbin_id;\\012\\011\\011\\011IF (counter > 0) THEN\\012\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code and being the part of the same parent already exists.', NEW.code;\\012\\011\\011\\011END IF;\\012\\011\\011ELSIF (NEW.grou_id is not NULL) THEN\\012\\011\\011\\011SELECT count(*) into counter FROM samples \\012\\011\\011\\011\\011where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and grou_id = NEW.grou_id;\\012\\011\\011\\011IF (counter > 0) THEN\\012\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because group sample with the same code and being the part of the same parent already exists.', NEW.code;\\012\\011\\011\\011END IF;\\012\\011\\011END IF;\\012        END IF;   \\012   RETURN NEW;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012CREATE TRIGGER SAMPLE_CODE_UNIQUENESS_CHECK BEFORE INSERT OR UPDATE ON SAMPLES\\012    FOR EACH ROW EXECUTE PROCEDURE SAMPLE_CODE_UNIQUENESS_CHECK();\\012	\N
028	./sql/generic/028/data-028.sql	SUCCESS	2009-02-10 15:40:42.214	----------------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table DATABASE_INSTANCES\\012----------------------------------------------------------------------------\\012\\012INSERT INTO database_instances(\\012              id\\012            , code\\012\\011    \\011, uuid\\012            , is_original_source)\\012    VALUES (  nextval('DATABASE_INSTANCE_ID_SEQ')\\012            , 'SYSTEM_DEFAULT'\\012\\011    \\011, 'SYSTEM_DEFAULT'\\012            , 'T');\\012\\012----------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table DATA_STORES\\012----------------------------------------------------------------------\\012\\012insert into data_stores\\012(id\\012,code\\012,download_url\\012,dbin_id)\\012values\\012(nextval('DATA_STORE_ID_SEQ')\\012,'STANDARD'\\012,''\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012update database_instances set dast_id = (select id from data_stores where code = 'STANDARD');\\012\\012----------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table PERSONS\\012-----------------------------------------------------------------------\\012\\012insert into persons\\012(id\\012,first_name\\012,last_name\\012,user_id\\012,email\\012,dbin_id)\\012values\\012(nextval('PERSON_ID_SEQ')\\012,''\\012,'System User'\\012,'system'\\012,''\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT') );\\012\\012-----------------------------------------------------------------------------------\\012--  Purpose:  Create Controlled Vocabulary PLATE_GEOMETRY\\012-----------------------------------------------------------------------------------\\012insert into controlled_vocabularies\\012       ( id\\012       , code\\012       , is_internal_namespace\\012       , description\\012       , pers_id_registerer\\012       , is_managed_internally\\012       , dbin_id )\\012values  (nextval('CONTROLLED_VOCABULARY_ID_SEQ')\\012       , 'PLATE_GEOMETRY'\\012       , true\\012       , 'The geometry or dimensions of a plate'\\012       , (select id from persons where user_id ='system')\\012       , true\\012       ,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012\\012-----------------------------------------------------------------------------------\\012--  Purpose:  Create Controlled Vocabulary Terms for PLATE_GEOMETRY\\012-----------------------------------------------------------------------------------\\012insert into controlled_vocabulary_terms \\012       ( id\\012       , code\\012       , covo_id \\012       , pers_id_registerer)\\012values  (nextval('CVTE_ID_SEQ')\\012       , '96_WELLS_8X12'\\012       , (select id from controlled_vocabularies where code = 'PLATE_GEOMETRY' and is_internal_namespace = true)\\012       , (select id from persons where user_id ='system'));\\012\\012insert into controlled_vocabulary_terms \\012       ( id\\012       , code\\012       , covo_id \\012       , pers_id_registerer)\\012values  (nextval('CVTE_ID_SEQ')\\012       , '384_WELLS_16X24'\\012       , (select id from controlled_vocabularies where code = 'PLATE_GEOMETRY' and is_internal_namespace = true)\\012       , (select id from persons where user_id ='system'));\\012\\012insert into controlled_vocabulary_terms \\012       ( id\\012       , code\\012       , covo_id \\012       , pers_id_registerer)\\012values  (nextval('CVTE_ID_SEQ')\\012       , '1536_WELLS_32X48'\\012       , (select id from controlled_vocabularies where code = 'PLATE_GEOMETRY' and is_internal_namespace = true)\\012       , (select id from persons where user_id ='system'));\\012\\012-----------------------------------------------------------------------------------\\012--  Purpose:  Create Controlled Vocabulary STORAGE_FORMAT\\012-----------------------------------------------------------------------------------\\012insert into controlled_vocabularies \\012       ( id\\012       , code\\012       , is_internal_namespace      \\012       , description\\012       , pers_id_registerer\\012       , is_managed_internally\\012       , dbin_id )\\012values  (nextval('CONTROLLED_VOCABULARY_ID_SEQ')\\012       , 'STORAGE_FORMAT'\\012       , true\\012       , 'The on-disk storage format of a data set'\\012       , (select id from persons where user_id ='system')\\012       , true\\012       ,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012\\012-----------------------------------------------------------------------------------\\012--  Purpose:  Create Controlled Vocabulary Terms for STORAGE_FORMAT\\012-----------------------------------------------------------------------------------\\012insert into controlled_vocabulary_terms \\012       ( id\\012       , code\\012       , covo_id \\012       , pers_id_registerer )\\012values  (nextval('CVTE_ID_SEQ')\\012       , 'PROPRIETARY'\\012       , (select id from controlled_vocabularies where code = 'STORAGE_FORMAT' and is_internal_namespace = true)\\012       , (select id from persons where user_id ='system'));\\012\\012insert into controlled_vocabulary_terms \\012       ( id\\012       , code\\012       , covo_id \\012       , pers_id_registerer )\\012values  (nextval('CVTE_ID_SEQ')\\012       , 'BDS_DIRECTORY'\\012       , (select id from controlled_vocabularies where code = 'STORAGE_FORMAT' and is_internal_namespace = true)\\012       , (select id from persons where user_id ='system'));\\012\\012\\012-------------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table EXPERIMENT_TYPES\\012--------------------------------------------------------------------------\\012\\012insert into experiment_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('EXPERIMENT_TYPE_ID_SEQ')\\012,'SIRNA_HCS'\\012,'Small Interfering RNA High Content Screening'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012insert into experiment_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('EXPERIMENT_TYPE_ID_SEQ')\\012,'COMPOUND_HCS'\\012,'Compound High Content Screening'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012\\012----------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table MATERIAL_TYPES\\012-----------------------------------------------------------------------\\012\\012insert into material_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('MATERIAL_TYPE_ID_SEQ')\\012,'VIRUS'\\012,'Virus'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012insert into material_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('MATERIAL_TYPE_ID_SEQ')\\012,'CELL_LINE'\\012,'Cell Line or Cell Culture. The growing of cells under controlled conditions.'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012insert into material_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('MATERIAL_TYPE_ID_SEQ')\\012,'GENE'\\012,'Gene'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012insert into material_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('MATERIAL_TYPE_ID_SEQ')\\012,'OLIGO'\\012,'Oligo nucleotide'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012insert into material_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('MATERIAL_TYPE_ID_SEQ')\\012,'CONTROL'\\012,'Control of a control layout'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012insert into material_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('MATERIAL_TYPE_ID_SEQ')\\012,'BACTERIUM'\\012,'Bacterium'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012insert into material_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('MATERIAL_TYPE_ID_SEQ')\\012,'COMPOUND'\\012,'Compound'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012\\012------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table DATA_TYPES\\012------------------------------------------------------------------\\012\\012insert into data_types\\012(id\\012,code\\012,description)\\012values \\012(nextval('DATA_TYPE_ID_SEQ')\\012,'VARCHAR'\\012,'Variable length character'\\012);\\012\\012insert into data_types\\012(id\\012,code\\012,description)\\012values \\012(nextval('DATA_TYPE_ID_SEQ')\\012,'INTEGER'\\012,'Integer'\\012);\\012\\012insert into data_types\\012(id\\012,code\\012,description)\\012values \\012(nextval('DATA_TYPE_ID_SEQ')\\012,'REAL'\\012,'Real number, i.e. an inexact, variable-precision numeric type'\\012);\\012\\012insert into data_types\\012(id\\012,code\\012,description)\\012values \\012(nextval('DATA_TYPE_ID_SEQ')\\012,'BOOLEAN'\\012,'An enumerated type with values True and False'\\012);\\012\\012insert into data_types\\012(id\\012,code\\012,description)\\012values \\012(nextval('DATA_TYPE_ID_SEQ')\\012,'TIMESTAMP'\\012,'Both date and time. Format: yyyy-mm-dd hh:mm:ss'\\012);\\012\\012insert into data_types\\012(id\\012 ,code\\012 ,description)\\012 values \\012 (nextval('DATA_TYPE_ID_SEQ')\\012 ,'CONTROLLEDVOCABULARY'\\012 ,'Controlled Vocabulary'\\012);\\012\\012\\012\\012\\012\\012----------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table PROPERTY_TYPES\\012-----------------------------------------------------------------------\\012\\012insert into property_types\\012(id\\012,code\\012,description\\012,label\\012,daty_id\\012,pers_id_registerer\\012,dbin_id)\\012values \\012(nextval('PROPERTY_TYPE_ID_SEQ')\\012,'DESCRIPTION'\\012,'A Description'\\012,'Description'\\012,(select id from data_types where code ='VARCHAR')\\012,(select id from persons where user_id ='system')\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into property_types\\012(id\\012,code\\012,description\\012,label\\012,daty_id\\012,pers_id_registerer\\012,dbin_id)\\012values \\012(nextval('PROPERTY_TYPE_ID_SEQ')\\012,'GENE_SYMBOL'\\012,'Gene Symbol, e.g. BMP15'\\012,'Gene Symbol'\\012,(select id from data_types where code ='VARCHAR')\\012,(select id from persons where user_id ='system')\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into property_types\\012(id\\012,code\\012,description\\012,label\\012,daty_id\\012,pers_id_registerer\\012,dbin_id)\\012values \\012(nextval('PROPERTY_TYPE_ID_SEQ')\\012,'NUCLEOTIDE_SEQUENCE'\\012,'A sequence of nucleotides'\\012,'Nucleotide Sequence'\\012,(select id from data_types where code ='VARCHAR')\\012,(select id from persons where user_id ='system')\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into property_types\\012(id\\012,code\\012,description\\012,label\\012,daty_id\\012,pers_id_registerer\\012,dbin_id)\\012values \\012(nextval('PROPERTY_TYPE_ID_SEQ')\\012,'REFSEQ'\\012,'NCBI Reference Sequence code, applicable to sequences of type: DNA, RNA, protein'\\012,'RefSeq'\\012,(select id from data_types where code ='VARCHAR')\\012,(select id from persons where user_id ='system')\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into property_types\\012(id\\012,code\\012,description\\012,label\\012,daty_id\\012,pers_id_registerer\\012,dbin_id)\\012values \\012(nextval('PROPERTY_TYPE_ID_SEQ')\\012,'OFFSET'\\012,'Offset from the start of the sequence'\\012,'Offset'\\012,(select id from data_types where code ='INTEGER')\\012,(select id from persons where user_id ='system')\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012-----------------------------------------------------------------------------------\\012--  Purpose:  Create property type PLATE_GEOMETRY\\012-----------------------------------------------------------------------------------\\012insert into property_types\\012(id\\012,code\\012,is_internal_namespace\\012,description\\012,label\\012,daty_id\\012,covo_id\\012,pers_id_registerer\\012, is_managed_internally \\012,dbin_id)\\012values \\012(nextval('PROPERTY_TYPE_ID_SEQ')\\012,'PLATE_GEOMETRY'\\012,true\\012,'Plate Geometry'\\012,'Plate Geometry'\\012,(select id from data_types where code ='CONTROLLEDVOCABULARY')\\012,(select id from controlled_vocabularies where code ='PLATE_GEOMETRY' and is_internal_namespace = true)\\012,(select id from persons where user_id ='system')\\012,true\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012\\012----------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table SAMPLE_TYPES\\012----------------------------------------------------------------------\\012\\012insert into sample_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('SAMPLE_TYPE_ID_SEQ')\\012,'MASTER_PLATE'\\012,'Master Plate'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into sample_types\\012(id\\012,code\\012,description\\012,dbin_id\\012,generated_from_depth)\\012values \\012(nextval('SAMPLE_TYPE_ID_SEQ')\\012,'DILUTION_PLATE'\\012,'Dilution Plate'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012,1\\012);\\012\\012insert into sample_types\\012(id\\012,code\\012,description\\012,dbin_id\\012,generated_from_depth)\\012values \\012(nextval('SAMPLE_TYPE_ID_SEQ')\\012,'CELL_PLATE'\\012,'Cell Plate'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012,2\\012);\\012\\012insert into sample_types\\012(id\\012,code\\012,description\\012,dbin_id\\012,generated_from_depth)\\012values \\012(nextval('SAMPLE_TYPE_ID_SEQ')\\012,'REINFECT_PLATE'\\012,'Re-infection Plate'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012,3\\012);\\012\\012insert into sample_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('SAMPLE_TYPE_ID_SEQ')\\012,'CONTROL_LAYOUT'\\012,'Control layout'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into sample_types\\012(id\\012,code\\012,description\\012,dbin_id\\012,is_listable\\012,generated_from_depth\\012,part_of_depth)\\012values \\012(nextval('SAMPLE_TYPE_ID_SEQ')\\012,'WELL'\\012,'Plate Well'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012,'F'\\012,0\\012,1\\012);\\012\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table EXPERIMENT_TYPE_PROPERTY_TYPES\\012------------------------------------------------------------------------------------\\012\\012----\\012-- Note: we rely on DESCRIPTION to be present and internally_managed for all experiment types!\\012----\\012\\012   ----------------------------------\\012   --  Experiment Type SIRNA_HCS\\012   ----------------------------------\\012\\012insert into experiment_type_property_types\\012(   id\\012   ,exty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('ETPT_ID_SEQ')\\012   ,(select id from experiment_types where code = 'SIRNA_HCS')\\012   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)\\012   ,true\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012   ----------------------------------\\012   --  Experiment Type COMPOUND_HCS\\012   ----------------------------------\\012\\012insert into experiment_type_property_types\\012(   id\\012   ,exty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('ETPT_ID_SEQ')\\012   ,(select id from experiment_types where code = 'COMPOUND_HCS')\\012   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)\\012   ,true\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table SAMPLE_TYPE_PROPERTY_TYPES\\012------------------------------------------------------------------------------------\\012\\012   ---------------------------------\\012   --  Sample Type   MASTER_PLATE\\012   --  Property Type PLATE_GEOMETRY   \\012   ---------------------------------\\012\\012insert into sample_type_property_types\\012(   id\\012   ,saty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('STPT_ID_SEQ')\\012   ,(select id from sample_types where code = 'MASTER_PLATE')\\012   ,(select id from property_types where code = 'PLATE_GEOMETRY' and is_internal_namespace = true)\\012   ,true\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012   ---------------------------------\\012   --  Sample Type   CONTROL_LAYOUT\\012   --  Property Type PLATE_GEOMETRY   \\012   ---------------------------------\\012\\012insert into sample_type_property_types\\012(   id\\012   ,saty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('STPT_ID_SEQ')\\012   ,(select id from sample_types where code = 'CONTROL_LAYOUT')\\012   ,(select id from property_types where code = 'PLATE_GEOMETRY' and is_internal_namespace = true)\\012   ,true\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table MATERIAL_TYPE_PROPERTY_TYPES\\012------------------------------------------------------------------------------------\\012\\012----\\012-- Note: we rely on DESCRIPTION to be present and internally_managed for all material types!\\012----\\012\\012   -----------------------\\012   --  Material Type VIRUS\\012   -----------------------\\012\\012insert into material_type_property_types\\012(   id\\012   ,maty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('MTPT_ID_SEQ')\\012   ,(select id from material_types where code = 'VIRUS')\\012   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)\\012   ,true\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012   -----------------------\\012   --  Material Type BACTERIUM\\012   -----------------------\\012\\012insert into material_type_property_types\\012(   id\\012   ,maty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('MTPT_ID_SEQ')\\012   ,(select id from material_types where code = 'BACTERIUM')\\012   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)\\012   ,true\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012   -----------------------\\012   --  Material Type COMPOUND\\012   -----------------------\\012\\012insert into material_type_property_types\\012(   id\\012   ,maty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('MTPT_ID_SEQ')\\012   ,(select id from material_types where code = 'COMPOUND')\\012   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)\\012   ,true\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012   -----------------------\\012   --  Material Type GENE\\012   -----------------------\\012\\012insert into material_type_property_types\\012(   id\\012   ,maty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('MTPT_ID_SEQ')\\012   ,(select id from material_types where code = 'GENE')\\012   ,(select id from property_types where code = 'GENE_SYMBOL' and is_internal_namespace = false)\\012   ,true\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012insert into material_type_property_types\\012(   id\\012   ,maty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('MTPT_ID_SEQ')\\012   ,(select id from material_types where code = 'GENE')\\012   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)\\012   ,false\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012\\012   -----------------------\\012   --  Material Type OLIGO\\012   -----------------------\\012\\012insert into material_type_property_types\\012(   id\\012   ,maty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('MTPT_ID_SEQ')\\012   ,(select id from material_types where code = 'OLIGO')\\012   ,(select id from property_types where code = 'NUCLEOTIDE_SEQUENCE' and is_internal_namespace = false)\\012   ,true\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012insert into material_type_property_types\\012(   id\\012   ,maty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('MTPT_ID_SEQ')\\012   ,(select id from material_types where code = 'OLIGO')\\012   ,(select id from property_types where code = 'OFFSET' and is_internal_namespace = false)\\012   ,false\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012insert into material_type_property_types\\012(   id\\012   ,maty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('MTPT_ID_SEQ')\\012   ,(select id from material_types where code = 'OLIGO')\\012   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)\\012   ,false\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012   -------------------------\\012   --  Material Type CONTROL\\012   -------------------------\\012\\012insert into material_type_property_types\\012(   id\\012   ,maty_id\\012   ,prty_id\\012   ,is_mandatory\\012   ,is_managed_internally\\012   ,pers_id_registerer\\012   )\\012values \\012   (nextval('MTPT_ID_SEQ')\\012   ,(select id from material_types where code = 'CONTROL')\\012   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)\\012   ,false\\012   ,true\\012   ,(select id from persons where user_id ='system')\\012);\\012\\012\\012\\012----------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table PROCEDURE_TYPES\\012-----------------------------------------------------------------------\\012\\012insert into procedure_types\\012(id\\012,code\\012,description\\012,is_data_acquisition\\012,dbin_id)\\012values \\012(nextval('PROCEDURE_TYPE_ID_SEQ')\\012,'UNKNOWN'\\012,'Unknown'\\012,false\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into procedure_types\\012(id\\012,code\\012,description\\012,is_data_acquisition\\012,dbin_id)\\012values \\012(nextval('PROCEDURE_TYPE_ID_SEQ')\\012,'DATA_ACQUISITION'\\012,'Data Acquisition'\\012,true\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into procedure_types\\012(id\\012,code\\012,description\\012,is_data_acquisition\\012,dbin_id)\\012values \\012(nextval('PROCEDURE_TYPE_ID_SEQ')\\012,'IMAGE_ANALYSIS'\\012,'Image Analysis'\\012,false\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012--------------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table DATA_SET_TYPES\\012--------------------------------------------------------------------------\\012\\012insert into data_set_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('DATA_SET_TYPE_ID_SEQ')\\012,'UNKNOWN'\\012,'Unknown'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into data_set_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('DATA_SET_TYPE_ID_SEQ')\\012,'HCS_IMAGE'\\012,'High Content Screening Image'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into data_set_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('DATA_SET_TYPE_ID_SEQ')\\012,'HCS_IMAGE_ANALYSIS_DATA'\\012,'Data derived from analysis of HCS images'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012-------------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table FILE_FORMAT_TYPES\\012-------------------------------------------------------------------------\\012\\012insert into file_format_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('FILE_FORMAT_TYPE_ID_SEQ')\\012,'TIFF'\\012,'TIFF File'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into file_format_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('FILE_FORMAT_TYPE_ID_SEQ')\\012,'3VPROPRIETARY'\\012,'Data Analysis 3V proprietary format'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into file_format_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('FILE_FORMAT_TYPE_ID_SEQ')\\012,'PLKPROPRIETARY'\\012,'Data Analysis Pelkmans group proprietary format'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012---------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table LOCATOR_TYPES\\012---------------------------------------------------------------------\\012\\012insert into locator_types\\012(id\\012,code\\012,description)\\012values \\012(nextval('LOCATOR_TYPE_ID_SEQ')\\012,'RELATIVE_LOCATION'\\012,'Relative Location'\\012);\\012	\N
\.


--
-- Data for Name: events; Type: TABLE DATA; Schema: public; Owner: -
--

COPY events (id, event_type, description, data_id, reason, pers_id_registerer, registration_timestamp) FROM stdin;
\.


--
-- Data for Name: experiment_attachment_contents; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_attachment_contents (id, value) FROM stdin;
1	processing-dir
2	Processing parameters from file '/local0/home/ci/cruisecontrol-bin-2.8.1/projects/cisd/trunk/integration-tests/targets/playground/openBIS-client/testdata/register-experiments/processing-parameters.txt'.
3	answer=42
\.


--
-- Data for Name: experiment_attachments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_attachments (id, expe_id, exac_id, file_name, registration_timestamp, version, pers_id_registerer) FROM stdin;
1	1	1	$processing-path-for-DATA_ACQUISITION	2009-02-10 15:41:04.90922+01	1	2
2	1	2	$processing-description-for-DATA_ACQUISITION	2009-02-10 15:41:04.90922+01	1	2
3	1	3	$processing-parameters-for-DATA_ACQUISITION	2009-02-10 15:41:04.90922+01	1	2
\.


--
-- Data for Name: experiment_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_properties (id, expe_id, etpt_id, value, cvte_id, pers_id_registerer, registration_timestamp) FROM stdin;
1	1	1	A simple experiment	\N	2	2009-02-10 15:41:04.90922+01
\.


--
-- Data for Name: experiment_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_type_property_types (id, exty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp) FROM stdin;
1	1	1	t	t	1	2009-02-10 15:40:42.184979+01
2	2	1	t	t	1	2009-02-10 15:40:42.184979+01
\.


--
-- Data for Name: experiment_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_types (id, code, description, dbin_id) FROM stdin;
1	SIRNA_HCS	Small Interfering RNA High Content Screening	1
2	COMPOUND_HCS	Compound High Content Screening	1
\.


--
-- Data for Name: experiments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiments (id, code, exty_id, mate_id_study_object, pers_id_registerer, registration_timestamp, proj_id, inva_id, is_public, dast_id) FROM stdin;
1	EXP1	1	14	2	2009-02-10 15:41:04.90922+01	1	\N	f	\N
\.


--
-- Data for Name: external_data; Type: TABLE DATA; Schema: public; Owner: -
--

COPY external_data (data_id, location, ffty_id, loty_id, cvte_id_stor_fmt, is_complete, cvte_id_store) FROM stdin;
\.


--
-- Data for Name: file_format_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY file_format_types (id, code, description, dbin_id) FROM stdin;
1	TIFF	TIFF File	1
2	3VPROPRIETARY	Data Analysis 3V proprietary format	1
3	PLKPROPRIETARY	Data Analysis Pelkmans group proprietary format	1
\.


--
-- Data for Name: groups; Type: TABLE DATA; Schema: public; Owner: -
--

COPY groups (id, code, dbin_id, grou_id_parent, pers_id_leader, description, registration_timestamp, pers_id_registerer, dast_id) FROM stdin;
1	CISD	1	\N	\N	\N	2009-02-10 15:40:46.747442+01	2	\N
\.


--
-- Data for Name: invalidations; Type: TABLE DATA; Schema: public; Owner: -
--

COPY invalidations (id, pers_id_registerer, registration_timestamp, reason) FROM stdin;
\.


--
-- Data for Name: locator_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY locator_types (id, code, description) FROM stdin;
1	RELATIVE_LOCATION	Relative Location
\.


--
-- Data for Name: material_batches; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_batches (id, code, amount, mate_id, proc_id, registration_timestamp, pers_id_registerer) FROM stdin;
1	CODE1	\N	19	\N	2009-02-10 15:40:59.870171+01	2
2	CODE1	\N	18	\N	2009-02-10 15:40:59.870171+01	2
3	CODE2	\N	19	\N	2009-02-10 15:41:00.923687+01	2
4	CODE2	\N	18	\N	2009-02-10 15:41:00.923687+01	2
5	CL-3V	\N	19	\N	2009-02-10 15:41:02.898333+01	2
6	CL-3V	\N	18	\N	2009-02-10 15:41:02.898333+01	2
\.


--
-- Data for Name: material_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_properties (id, mate_id, mtpt_id, value, registration_timestamp, pers_id_registerer, cvte_id) FROM stdin;
1	1	1	Adenovirus 3	2009-02-10 15:40:57.725283+01	2	\N
2	2	1	Adenovirus 5	2009-02-10 15:40:57.725283+01	2	\N
3	3	1	Dengue Virus 1	2009-02-10 15:40:57.725283+01	2	\N
4	4	1	Echovirus 1	2009-02-10 15:40:57.725283+01	2	\N
5	5	1	Influenza A virus	2009-02-10 15:40:57.725283+01	2	\N
6	6	1	Human respiratory virus	2009-02-10 15:40:57.725283+01	2	\N
7	7	1	Herpes Simplex Virus 1	2009-02-10 15:40:57.725283+01	2	\N
8	8	1	Mouse Hepatitis Virus	2009-02-10 15:40:57.725283+01	2	\N
9	9	1	Rotavirus	2009-02-10 15:40:57.725283+01	2	\N
10	10	1	Rhesus Rotavirus	2009-02-10 15:40:57.725283+01	2	\N
11	11	1	Respiratory Syncytial Virus	2009-02-10 15:40:57.725283+01	2	\N
12	12	1	Rhinovirus	2009-02-10 15:40:57.725283+01	2	\N
13	13	1	Semliki Forest Virus	2009-02-10 15:40:57.725283+01	2	\N
14	14	1	Simian Virus 40	2009-02-10 15:40:57.725283+01	2	\N
15	15	1	Vesicular Stomatitis Virus	2009-02-10 15:40:57.725283+01	2	\N
16	16	1	Vaccinia Virus	2009-02-10 15:40:57.725283+01	2	\N
17	17	1	Yellow Fever Virus	2009-02-10 15:40:57.725283+01	2	\N
\.


--
-- Data for Name: material_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_type_property_types (id, maty_id, prty_id, is_mandatory, is_managed_internally, registration_timestamp, pers_id_registerer) FROM stdin;
1	1	1	t	t	2009-02-10 15:40:42.184979+01	1
2	6	1	t	t	2009-02-10 15:40:42.184979+01	1
3	7	1	t	t	2009-02-10 15:40:42.184979+01	1
4	3	2	t	t	2009-02-10 15:40:42.184979+01	1
5	3	1	f	t	2009-02-10 15:40:42.184979+01	1
6	4	3	t	t	2009-02-10 15:40:42.184979+01	1
7	4	5	f	t	2009-02-10 15:40:42.184979+01	1
8	4	1	f	t	2009-02-10 15:40:42.184979+01	1
9	5	1	f	t	2009-02-10 15:40:42.184979+01	1
\.


--
-- Data for Name: material_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_types (id, code, description, dbin_id) FROM stdin;
1	VIRUS	Virus	1
2	CELL_LINE	Cell Line or Cell Culture. The growing of cells under controlled conditions.	1
3	GENE	Gene	1
4	OLIGO	Oligo nucleotide	1
5	CONTROL	Control of a control layout	1
6	BACTERIUM	Bacterium	1
7	COMPOUND	Compound	1
\.


--
-- Data for Name: materials; Type: TABLE DATA; Schema: public; Owner: -
--

COPY materials (id, code, maty_id, pers_id_registerer, mate_id_inhibitor_of, registration_timestamp, dbin_id) FROM stdin;
1	AD3	1	2	\N	2009-02-10 15:40:57.725283+01	1
2	AD5	1	2	\N	2009-02-10 15:40:57.725283+01	1
3	DV	1	2	\N	2009-02-10 15:40:57.725283+01	1
4	EV1	1	2	\N	2009-02-10 15:40:57.725283+01	1
5	FLU	1	2	\N	2009-02-10 15:40:57.725283+01	1
6	HRV2	1	2	\N	2009-02-10 15:40:57.725283+01	1
7	HSV1	1	2	\N	2009-02-10 15:40:57.725283+01	1
8	MHV	1	2	\N	2009-02-10 15:40:57.725283+01	1
9	ROTAV	1	2	\N	2009-02-10 15:40:57.725283+01	1
10	RRV	1	2	\N	2009-02-10 15:40:57.725283+01	1
11	RSV	1	2	\N	2009-02-10 15:40:57.725283+01	1
12	RV	1	2	\N	2009-02-10 15:40:57.725283+01	1
13	SFV	1	2	\N	2009-02-10 15:40:57.725283+01	1
14	SV40	1	2	\N	2009-02-10 15:40:57.725283+01	1
15	VSV	1	2	\N	2009-02-10 15:40:57.725283+01	1
16	VV	1	2	\N	2009-02-10 15:40:57.725283+01	1
17	YFV	1	2	\N	2009-02-10 15:40:57.725283+01	1
18	GFP	5	2	\N	2009-02-10 15:40:58.86855+01	1
19	SCRAM	5	2	\N	2009-02-10 15:40:58.86855+01	1
\.


--
-- Data for Name: persons; Type: TABLE DATA; Schema: public; Owner: -
--

COPY persons (id, first_name, last_name, user_id, email, dbin_id, grou_id, registration_timestamp, pers_id_registerer) FROM stdin;
1		System User	system		1	\N	2009-02-10 15:40:42.184979+01	\N
2	John	Doe	test	franz-josef.elmer@systemsx.ch	1	1	2009-02-10 15:40:45.854539+01	\N
3	John	Etl1	etlserver1	franz-josef.elmer@systemsx.ch	1	1	2009-02-10 15:40:51.094944+01	\N
4	John	Etl2	etlserver2	franz-josef.elmer@systemsx.ch	1	1	2009-02-10 15:40:51.094944+01	\N
\.


--
-- Data for Name: procedure_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY procedure_types (id, code, description, is_data_acquisition, dbin_id) FROM stdin;
1	UNKNOWN	Unknown	f	1
2	DATA_ACQUISITION	Data Acquisition	t	1
3	IMAGE_ANALYSIS	Image Analysis	f	1
\.


--
-- Data for Name: procedures; Type: TABLE DATA; Schema: public; Owner: -
--

COPY procedures (id, registration_timestamp, expe_id, pcty_id) FROM stdin;
1	2009-02-10 15:41:04.90922+01	1	2
\.


--
-- Data for Name: projects; Type: TABLE DATA; Schema: public; Owner: -
--

COPY projects (id, code, grou_id, pers_id_leader, description, pers_id_registerer, registration_timestamp, dast_id) FROM stdin;
1	NEMO	1	\N	\N	2	2009-02-10 15:40:56.771896+01	\N
\.


--
-- Data for Name: property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id) FROM stdin;
1	DESCRIPTION	A Description	Description	1	2009-02-10 15:40:42.184979+01	1	\N	f	f	1
2	GENE_SYMBOL	Gene Symbol, e.g. BMP15	Gene Symbol	1	2009-02-10 15:40:42.184979+01	1	\N	f	f	1
3	NUCLEOTIDE_SEQUENCE	A sequence of nucleotides	Nucleotide Sequence	1	2009-02-10 15:40:42.184979+01	1	\N	f	f	1
4	REFSEQ	NCBI Reference Sequence code, applicable to sequences of type: DNA, RNA, protein	RefSeq	1	2009-02-10 15:40:42.184979+01	1	\N	f	f	1
5	OFFSET	Offset from the start of the sequence	Offset	2	2009-02-10 15:40:42.184979+01	1	\N	f	f	1
6	PLATE_GEOMETRY	Plate Geometry	Plate Geometry	6	2009-02-10 15:40:42.184979+01	1	1	t	t	1
\.


--
-- Data for Name: role_assignments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY role_assignments (id, role_code, grou_id, dbin_id, pers_id_grantee, pers_id_registerer, registration_timestamp) FROM stdin;
1	ADMIN	\N	1	2	1	2009-02-10 15:40:45.854539+01
2	ADMIN	1	\N	2	2	2009-02-10 15:40:53.51404+01
3	ETL_SERVER	\N	1	3	2	2009-02-10 15:40:53.51404+01
4	ETL_SERVER	1	\N	3	2	2009-02-10 15:40:53.51404+01
5	ETL_SERVER	\N	1	4	2	2009-02-10 15:40:53.51404+01
6	ETL_SERVER	1	\N	4	2	2009-02-10 15:40:53.51404+01
\.


--
-- Data for Name: sample_inputs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_inputs (samp_id, proc_id) FROM stdin;
14	1
16	1
17	1
\.


--
-- Data for Name: sample_material_batches; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_material_batches (samp_id, maba_id) FROM stdin;
2	1
3	2
5	3
6	4
12	5
13	6
\.


--
-- Data for Name: sample_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_properties (id, samp_id, stpt_id, value, cvte_id, pers_id_registerer, registration_timestamp) FROM stdin;
1	1	1	\N	2	2	2009-02-10 15:40:59.870171+01
2	4	1	\N	2	2	2009-02-10 15:41:00.923687+01
3	11	2	\N	2	2	2009-02-10 15:41:02.898333+01
\.


--
-- Data for Name: sample_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_type_property_types (id, saty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, is_displayed) FROM stdin;
1	1	6	t	t	1	2009-02-10 15:40:42.184979+01	t
2	5	6	t	t	1	2009-02-10 15:40:42.184979+01	t
\.


--
-- Data for Name: sample_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_types (id, code, description, dbin_id, is_listable, generated_from_depth, part_of_depth) FROM stdin;
1	MASTER_PLATE	Master Plate	1	t	0	0
2	DILUTION_PLATE	Dilution Plate	1	t	1	0
3	CELL_PLATE	Cell Plate	1	t	2	0
4	REINFECT_PLATE	Re-infection Plate	1	t	3	0
5	CONTROL_LAYOUT	Control layout	1	t	0	0
6	WELL	Plate Well	1	f	0	1
\.


--
-- Data for Name: samples; Type: TABLE DATA; Schema: public; Owner: -
--

COPY samples (id, code, samp_id_top, samp_id_generated_from, saty_id, registration_timestamp, pers_id_registerer, inva_id, samp_id_control_layout, dbin_id, grou_id, samp_id_part_of) FROM stdin;
1	MP001-1	\N	\N	1	2009-02-10 15:40:59.870171+01	2	\N	\N	\N	1	\N
2	A03	\N	\N	6	2009-02-10 15:40:59.870171+01	2	\N	\N	\N	1	1
3	A04	\N	\N	6	2009-02-10 15:40:59.870171+01	2	\N	\N	\N	1	1
4	MP002-1	\N	\N	1	2009-02-10 15:41:00.923687+01	2	\N	\N	\N	1	\N
5	A03	\N	\N	6	2009-02-10 15:41:00.923687+01	2	\N	\N	\N	1	4
6	A04	\N	\N	6	2009-02-10 15:41:00.923687+01	2	\N	\N	\N	1	4
7	3V-123	1	1	2	2009-02-10 15:41:01.89914+01	2	\N	\N	\N	1	\N
8	3V-124	1	1	2	2009-02-10 15:41:01.89914+01	2	\N	\N	\N	1	\N
9	3V-125	4	4	2	2009-02-10 15:41:01.89914+01	2	\N	\N	\N	1	\N
10	3V-126	4	4	2	2009-02-10 15:41:01.89914+01	2	\N	\N	\N	1	\N
11	CL-3V	\N	\N	5	2009-02-10 15:41:02.898333+01	2	\N	\N	\N	1	\N
12	A01	\N	\N	6	2009-02-10 15:41:02.898333+01	2	\N	\N	\N	1	11
13	A02	\N	\N	6	2009-02-10 15:41:02.898333+01	2	\N	\N	\N	1	11
14	3VCP1	1	7	3	2009-02-10 15:41:03.801771+01	2	\N	11	\N	1	\N
15	3VCP2	1	7	3	2009-02-10 15:41:03.801771+01	2	\N	11	\N	1	\N
16	3VCP3	4	9	3	2009-02-10 15:41:03.801771+01	2	\N	11	\N	1	\N
17	3VCP4	4	9	3	2009-02-10 15:41:03.801771+01	2	\N	11	\N	1	\N
\.


--
-- Name: covo_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_bk_uk UNIQUE (code, is_internal_namespace, dbin_id);


--
-- Name: covo_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_pk PRIMARY KEY (id);


--
-- Name: cvte_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_bk_uk UNIQUE (code, covo_id);


--
-- Name: cvte_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_pk PRIMARY KEY (id);


--
-- Name: dast_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_stores
    ADD CONSTRAINT dast_bk_uk UNIQUE (code, dbin_id);


--
-- Name: dast_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_stores
    ADD CONSTRAINT dast_pk PRIMARY KEY (id);


--
-- Name: data_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_bk_uk UNIQUE (code);


--
-- Name: data_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_pk PRIMARY KEY (id);


--
-- Name: daty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_types
    ADD CONSTRAINT daty_bk_uk UNIQUE (code);


--
-- Name: daty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_types
    ADD CONSTRAINT daty_pk PRIMARY KEY (id);


--
-- Name: dbin_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY database_instances
    ADD CONSTRAINT dbin_bk_uk UNIQUE (code);


--
-- Name: dbin_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY database_instances
    ADD CONSTRAINT dbin_pk PRIMARY KEY (id);


--
-- Name: dbin_uuid_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY database_instances
    ADD CONSTRAINT dbin_uuid_uk UNIQUE (uuid);


--
-- Name: dsre_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_relationships
    ADD CONSTRAINT dsre_bk_uk UNIQUE (data_id_child, data_id_parent);


--
-- Name: etpt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_bk_uk UNIQUE (exty_id, prty_id);


--
-- Name: etpt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_pk PRIMARY KEY (id);


--
-- Name: evnt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY events
    ADD CONSTRAINT evnt_bk_uk UNIQUE (event_type, data_id);


--
-- Name: evnt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY events
    ADD CONSTRAINT evnt_pk PRIMARY KEY (id);


--
-- Name: exac_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_attachment_contents
    ADD CONSTRAINT exac_pk PRIMARY KEY (id);


--
-- Name: exat_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_attachments
    ADD CONSTRAINT exat_bk_uk UNIQUE (expe_id, file_name, version);


--
-- Name: exat_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_attachments
    ADD CONSTRAINT exat_pk PRIMARY KEY (id);


--
-- Name: exda_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_bk_uk UNIQUE (location, loty_id);


--
-- Name: exda_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_pk PRIMARY KEY (data_id);


--
-- Name: expe_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_bk_uk UNIQUE (code, proj_id);


--
-- Name: expe_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_pk PRIMARY KEY (id);


--
-- Name: expr_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_bk_uk UNIQUE (expe_id, etpt_id);


--
-- Name: expr_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_pk PRIMARY KEY (id);


--
-- Name: exty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_bk_uk UNIQUE (code, dbin_id);


--
-- Name: exty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_pk PRIMARY KEY (id);


--
-- Name: ffty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY file_format_types
    ADD CONSTRAINT ffty_bk_uk UNIQUE (code, dbin_id);


--
-- Name: ffty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY file_format_types
    ADD CONSTRAINT ffty_pk PRIMARY KEY (id);


--
-- Name: grou_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_bk_uk UNIQUE (code, dbin_id);


--
-- Name: grou_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_pk PRIMARY KEY (id);


--
-- Name: inva_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY invalidations
    ADD CONSTRAINT inva_pk PRIMARY KEY (id);


--
-- Name: loty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY locator_types
    ADD CONSTRAINT loty_bk_uk UNIQUE (code);


--
-- Name: loty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY locator_types
    ADD CONSTRAINT loty_pk PRIMARY KEY (id);


--
-- Name: maba_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_batches
    ADD CONSTRAINT maba_bk_uk UNIQUE (code, mate_id);


--
-- Name: maba_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_batches
    ADD CONSTRAINT maba_pk PRIMARY KEY (id);


--
-- Name: mapr_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_bk_uk UNIQUE (mate_id, mtpt_id);


--
-- Name: mapr_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_pk PRIMARY KEY (id);


--
-- Name: mate_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_bk_uk UNIQUE (code, maty_id, dbin_id);


--
-- Name: mate_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_pk PRIMARY KEY (id);


--
-- Name: maty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_bk_uk UNIQUE (code, dbin_id);


--
-- Name: maty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_pk PRIMARY KEY (id);


--
-- Name: mtpt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_bk_uk UNIQUE (maty_id, prty_id);


--
-- Name: mtpt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_pk PRIMARY KEY (id);


--
-- Name: obty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT obty_bk_uk UNIQUE (code, dbin_id);


--
-- Name: obty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT obty_pk PRIMARY KEY (id);


--
-- Name: pcty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY procedure_types
    ADD CONSTRAINT pcty_bk_uk UNIQUE (code, dbin_id);


--
-- Name: pcty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY procedure_types
    ADD CONSTRAINT pcty_pk PRIMARY KEY (id);


--
-- Name: pers_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_bk_uk UNIQUE (dbin_id, user_id);


--
-- Name: pers_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_pk PRIMARY KEY (id);


--
-- Name: proc_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY procedures
    ADD CONSTRAINT proc_pk PRIMARY KEY (id);


--
-- Name: proj_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_bk_uk UNIQUE (code, grou_id);


--
-- Name: proj_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pk PRIMARY KEY (id);


--
-- Name: prty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_bk_uk UNIQUE (code, is_internal_namespace, dbin_id);


--
-- Name: prty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_pk PRIMARY KEY (id);


--
-- Name: roas_group_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_group_bk_uk UNIQUE (pers_id_grantee, role_code, grou_id);


--
-- Name: roas_instance_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_instance_bk_uk UNIQUE (pers_id_grantee, role_code, dbin_id);


--
-- Name: roas_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pk PRIMARY KEY (id);


--
-- Name: sain_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_inputs
    ADD CONSTRAINT sain_bk_uk UNIQUE (samp_id, proc_id);


--
-- Name: sain_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_inputs
    ADD CONSTRAINT sain_pk PRIMARY KEY (proc_id, samp_id);


--
-- Name: samb_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_material_batches
    ADD CONSTRAINT samb_bk_uk UNIQUE (maba_id, samp_id);


--
-- Name: samb_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_material_batches
    ADD CONSTRAINT samb_pk PRIMARY KEY (samp_id, maba_id);


--
-- Name: samp_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_pk PRIMARY KEY (id);


--
-- Name: sapr_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_bk_uk UNIQUE (samp_id, stpt_id);


--
-- Name: sapr_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_pk PRIMARY KEY (id);


--
-- Name: saty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_bk_uk UNIQUE (code, dbin_id);


--
-- Name: saty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_pk PRIMARY KEY (id);


--
-- Name: stpt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_bk_uk UNIQUE (saty_id, prty_id);


--
-- Name: stpt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_pk PRIMARY KEY (id);


--
-- Name: covo_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX covo_pers_fk_i ON controlled_vocabularies USING btree (pers_id_registerer);


--
-- Name: cvte_covo_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX cvte_covo_fk_i ON controlled_vocabulary_terms USING btree (covo_id);


--
-- Name: cvte_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX cvte_pers_fk_i ON controlled_vocabulary_terms USING btree (pers_id_registerer);


--
-- Name: dast_dbin_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dast_dbin_fk_i ON data_stores USING btree (dbin_id);


--
-- Name: data_obty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_obty_fk_i ON data USING btree (dsty_id);


--
-- Name: data_proc_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_proc_fk_i ON data USING btree (proc_id_produced_by);


--
-- Name: data_samp_fk_i_acquired_from; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_samp_fk_i_acquired_from ON data USING btree (samp_id_acquired_from);


--
-- Name: data_samp_fk_i_derived_from; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_samp_fk_i_derived_from ON data USING btree (samp_id_derived_from);


--
-- Name: dsre_data_fk_i_child; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dsre_data_fk_i_child ON data_set_relationships USING btree (data_id_child);


--
-- Name: dsre_data_fk_i_parent; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dsre_data_fk_i_parent ON data_set_relationships USING btree (data_id_parent);


--
-- Name: etpt_exty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX etpt_exty_fk_i ON experiment_type_property_types USING btree (exty_id);


--
-- Name: etpt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX etpt_pers_fk_i ON experiment_type_property_types USING btree (pers_id_registerer);


--
-- Name: etpt_prty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX etpt_prty_fk_i ON experiment_type_property_types USING btree (prty_id);


--
-- Name: evnt_data_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX evnt_data_fk_i ON events USING btree (data_id);


--
-- Name: evnt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX evnt_pers_fk_i ON events USING btree (pers_id_registerer);


--
-- Name: exat_exac_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exat_exac_fk_i ON experiment_attachments USING btree (exac_id);


--
-- Name: exat_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exat_expe_fk_i ON experiment_attachments USING btree (expe_id);


--
-- Name: exat_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exat_pers_fk_i ON experiment_attachments USING btree (pers_id_registerer);


--
-- Name: exda_cvte_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exda_cvte_fk_i ON external_data USING btree (cvte_id_stor_fmt);


--
-- Name: exda_cvte_stored_on_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exda_cvte_stored_on_fk_i ON external_data USING btree (cvte_id_store);


--
-- Name: exda_ffty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exda_ffty_fk_i ON external_data USING btree (ffty_id);


--
-- Name: exda_loty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX exda_loty_fk_i ON external_data USING btree (loty_id);


--
-- Name: expe_exty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_exty_fk_i ON experiments USING btree (exty_id);


--
-- Name: expe_inva_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_inva_fk_i ON experiments USING btree (inva_id);


--
-- Name: expe_mate_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_mate_fk_i ON experiments USING btree (mate_id_study_object);


--
-- Name: expe_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_pers_fk_i ON experiments USING btree (pers_id_registerer);


--
-- Name: expe_proj_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expe_proj_fk_i ON experiments USING btree (proj_id);


--
-- Name: expr_cvte_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expr_cvte_fk_i ON experiment_properties USING btree (cvte_id);


--
-- Name: expr_etpt_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expr_etpt_fk_i ON experiment_properties USING btree (etpt_id);


--
-- Name: expr_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expr_expe_fk_i ON experiment_properties USING btree (expe_id);


--
-- Name: expr_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX expr_pers_fk_i ON experiment_properties USING btree (pers_id_registerer);


--
-- Name: grou_dbin_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX grou_dbin_fk_i ON groups USING btree (dbin_id);


--
-- Name: grou_grou_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX grou_grou_fk_i ON groups USING btree (grou_id_parent);


--
-- Name: grou_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX grou_pers_fk_i ON groups USING btree (pers_id_leader);


--
-- Name: grou_pers_registered_by_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX grou_pers_registered_by_fk_i ON groups USING btree (pers_id_registerer);


--
-- Name: inva_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX inva_pers_fk_i ON invalidations USING btree (pers_id_registerer);


--
-- Name: maba_mate_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX maba_mate_fk_i ON material_batches USING btree (mate_id);


--
-- Name: maba_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX maba_pers_fk_i ON material_batches USING btree (pers_id_registerer);


--
-- Name: maba_proc_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX maba_proc_fk_i ON material_batches USING btree (proc_id);


--
-- Name: mapr_cvte_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mapr_cvte_fk_i ON material_properties USING btree (cvte_id);


--
-- Name: mapr_mate_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mapr_mate_fk_i ON material_properties USING btree (mate_id);


--
-- Name: mapr_mtpt_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mapr_mtpt_fk_i ON material_properties USING btree (mtpt_id);


--
-- Name: mapr_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mapr_pers_fk_i ON material_properties USING btree (pers_id_registerer);


--
-- Name: mate_mate_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mate_mate_fk_i ON materials USING btree (mate_id_inhibitor_of);


--
-- Name: mate_maty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mate_maty_fk_i ON materials USING btree (maty_id);


--
-- Name: mate_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mate_pers_fk_i ON materials USING btree (pers_id_registerer);


--
-- Name: mtpt_maty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mtpt_maty_fk_i ON material_type_property_types USING btree (maty_id);


--
-- Name: mtpt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mtpt_pers_fk_i ON material_type_property_types USING btree (pers_id_registerer);


--
-- Name: mtpt_prty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mtpt_prty_fk_i ON material_type_property_types USING btree (prty_id);


--
-- Name: pers_grou_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX pers_grou_fk_i ON persons USING btree (grou_id);


--
-- Name: proc_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX proc_expe_fk_i ON procedures USING btree (expe_id);


--
-- Name: proc_pcty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX proc_pcty_fk_i ON procedures USING btree (pcty_id);


--
-- Name: proj_grou_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX proj_grou_fk_i ON projects USING btree (grou_id);


--
-- Name: proj_pers_fk_i_leader; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX proj_pers_fk_i_leader ON projects USING btree (pers_id_leader);


--
-- Name: proj_pers_fk_i_registerer; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX proj_pers_fk_i_registerer ON projects USING btree (pers_id_registerer);


--
-- Name: prty_covo_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX prty_covo_fk_i ON property_types USING btree (covo_id);


--
-- Name: prty_daty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX prty_daty_fk_i ON property_types USING btree (daty_id);


--
-- Name: prty_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX prty_pers_fk_i ON property_types USING btree (pers_id_registerer);


--
-- Name: roas_dbin_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX roas_dbin_fk_i ON role_assignments USING btree (dbin_id);


--
-- Name: roas_grou_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX roas_grou_fk_i ON role_assignments USING btree (grou_id);


--
-- Name: roas_pers_fk_i_grantee; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX roas_pers_fk_i_grantee ON role_assignments USING btree (pers_id_grantee);


--
-- Name: roas_pers_fk_i_registerer; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX roas_pers_fk_i_registerer ON role_assignments USING btree (pers_id_registerer);


--
-- Name: sain_proc_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sain_proc_fk_i ON sample_inputs USING btree (proc_id);


--
-- Name: sain_samp_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sain_samp_fk_i ON sample_inputs USING btree (samp_id);


--
-- Name: samb_maba_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samb_maba_fk_i ON sample_material_batches USING btree (maba_id);


--
-- Name: samb_samp_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samb_samp_fk_i ON sample_material_batches USING btree (samp_id);


--
-- Name: samp_code_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_code_i ON samples USING btree (code);


--
-- Name: samp_inva_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_inva_fk_i ON samples USING btree (inva_id);


--
-- Name: samp_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_pers_fk_i ON samples USING btree (pers_id_registerer);


--
-- Name: samp_samp_fk_i_control_layout; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_samp_fk_i_control_layout ON samples USING btree (samp_id_control_layout);


--
-- Name: samp_samp_fk_i_generated_from; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_samp_fk_i_generated_from ON samples USING btree (samp_id_generated_from);


--
-- Name: samp_samp_fk_i_part_of; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_samp_fk_i_part_of ON samples USING btree (samp_id_part_of);


--
-- Name: samp_samp_fk_i_top; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_samp_fk_i_top ON samples USING btree (samp_id_top);


--
-- Name: samp_saty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_saty_fk_i ON samples USING btree (saty_id);


--
-- Name: sapr_cvte_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sapr_cvte_fk_i ON sample_properties USING btree (cvte_id);


--
-- Name: sapr_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sapr_pers_fk_i ON sample_properties USING btree (pers_id_registerer);


--
-- Name: sapr_samp_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sapr_samp_fk_i ON sample_properties USING btree (samp_id);


--
-- Name: sapr_stpt_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sapr_stpt_fk_i ON sample_properties USING btree (stpt_id);


--
-- Name: stpt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX stpt_pers_fk_i ON sample_type_property_types USING btree (pers_id_registerer);


--
-- Name: stpt_prty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX stpt_prty_fk_i ON sample_type_property_types USING btree (prty_id);


--
-- Name: stpt_saty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX stpt_saty_fk_i ON sample_type_property_types USING btree (saty_id);


--
-- Name: controlled_vocabulary_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER controlled_vocabulary_check
    BEFORE INSERT OR UPDATE ON property_types
    FOR EACH ROW
    EXECUTE PROCEDURE controlled_vocabulary_check();


--
-- Name: external_data_storage_format_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER external_data_storage_format_check
    BEFORE INSERT OR UPDATE ON external_data
    FOR EACH ROW
    EXECUTE PROCEDURE external_data_storage_format_check();


--
-- Name: sample_code_uniqueness_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_code_uniqueness_check
    BEFORE INSERT OR UPDATE ON samples
    FOR EACH ROW
    EXECUTE PROCEDURE sample_code_uniqueness_check();


--
-- Name: covo_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: covo_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: cvte_covo_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_covo_fk FOREIGN KEY (covo_id) REFERENCES controlled_vocabularies(id);


--
-- Name: cvte_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: dast_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_stores
    ADD CONSTRAINT dast_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: data_obty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_obty_fk FOREIGN KEY (dsty_id) REFERENCES data_set_types(id);


--
-- Name: data_proc_produced_by_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_proc_produced_by_fk FOREIGN KEY (proc_id_produced_by) REFERENCES procedures(id);


--
-- Name: data_samp_fk_acquired_from; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_samp_fk_acquired_from FOREIGN KEY (samp_id_acquired_from) REFERENCES samples(id);


--
-- Name: data_samp_fk_derived_from; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_samp_fk_derived_from FOREIGN KEY (samp_id_derived_from) REFERENCES samples(id);


--
-- Name: dbin_dast_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY database_instances
    ADD CONSTRAINT dbin_dast_fk FOREIGN KEY (dast_id) REFERENCES data_stores(id);


--
-- Name: dsre_data_fk_child; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_relationships
    ADD CONSTRAINT dsre_data_fk_child FOREIGN KEY (data_id_child) REFERENCES data(id);


--
-- Name: dsre_data_fk_parent; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_relationships
    ADD CONSTRAINT dsre_data_fk_parent FOREIGN KEY (data_id_parent) REFERENCES data(id);


--
-- Name: etpt_exty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_exty_fk FOREIGN KEY (exty_id) REFERENCES experiment_types(id);


--
-- Name: etpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: etpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id);


--
-- Name: evnt_data_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY events
    ADD CONSTRAINT evnt_data_fk FOREIGN KEY (data_id) REFERENCES data(id);


--
-- Name: evnt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY events
    ADD CONSTRAINT evnt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: exat_cont_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_attachments
    ADD CONSTRAINT exat_cont_fk FOREIGN KEY (exac_id) REFERENCES experiment_attachment_contents(id);


--
-- Name: exat_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_attachments
    ADD CONSTRAINT exat_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);


--
-- Name: exat_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_attachments
    ADD CONSTRAINT exat_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: exda_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_cvte_fk FOREIGN KEY (cvte_id_stor_fmt) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: exda_cvte_stored_on_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_cvte_stored_on_fk FOREIGN KEY (cvte_id_store) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: exda_data_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_data_fk FOREIGN KEY (data_id) REFERENCES data(id);


--
-- Name: exda_ffty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_ffty_fk FOREIGN KEY (ffty_id) REFERENCES file_format_types(id);


--
-- Name: exda_loty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_loty_fk FOREIGN KEY (loty_id) REFERENCES locator_types(id);


--
-- Name: expe_dast_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_dast_fk FOREIGN KEY (dast_id) REFERENCES data_stores(id);


--
-- Name: expe_exty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_exty_fk FOREIGN KEY (exty_id) REFERENCES experiment_types(id);


--
-- Name: expe_inva_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_inva_fk FOREIGN KEY (inva_id) REFERENCES invalidations(id);


--
-- Name: expe_mate_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_mate_fk FOREIGN KEY (mate_id_study_object) REFERENCES materials(id);


--
-- Name: expe_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: expe_proj_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_proj_fk FOREIGN KEY (proj_id) REFERENCES projects(id);


--
-- Name: expr_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: expr_etpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_etpt_fk FOREIGN KEY (etpt_id) REFERENCES experiment_type_property_types(id) ON DELETE CASCADE;


--
-- Name: expr_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);


--
-- Name: expr_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: exty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: ffty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY file_format_types
    ADD CONSTRAINT ffty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: grou_dast_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_dast_fk FOREIGN KEY (dast_id) REFERENCES data_stores(id);


--
-- Name: grou_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: grou_grou_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_grou_fk FOREIGN KEY (grou_id_parent) REFERENCES groups(id);


--
-- Name: grou_pers_fk_leader; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_pers_fk_leader FOREIGN KEY (pers_id_leader) REFERENCES persons(id);


--
-- Name: grou_pers_fk_registerer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: inva_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY invalidations
    ADD CONSTRAINT inva_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: maba_mate_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_batches
    ADD CONSTRAINT maba_mate_fk FOREIGN KEY (mate_id) REFERENCES materials(id);


--
-- Name: maba_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_batches
    ADD CONSTRAINT maba_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: maba_proc_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_batches
    ADD CONSTRAINT maba_proc_fk FOREIGN KEY (proc_id) REFERENCES procedures(id);


--
-- Name: mapr_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: mapr_mate_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_mate_fk FOREIGN KEY (mate_id) REFERENCES materials(id);


--
-- Name: mapr_mtpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_mtpt_fk FOREIGN KEY (mtpt_id) REFERENCES material_type_property_types(id) ON DELETE CASCADE;


--
-- Name: mapr_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: mate_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: mate_mate_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_mate_fk FOREIGN KEY (mate_id_inhibitor_of) REFERENCES materials(id);


--
-- Name: mate_maty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_maty_fk FOREIGN KEY (maty_id) REFERENCES material_types(id);


--
-- Name: mate_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: maty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: mtpt_maty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_maty_fk FOREIGN KEY (maty_id) REFERENCES material_types(id);


--
-- Name: mtpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: mtpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id);


--
-- Name: obty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT obty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: pcty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY procedure_types
    ADD CONSTRAINT pcty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: pers_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: pers_grou_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_grou_fk FOREIGN KEY (grou_id) REFERENCES groups(id);


--
-- Name: pers_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: proc_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY procedures
    ADD CONSTRAINT proc_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);


--
-- Name: proc_pcty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY procedures
    ADD CONSTRAINT proc_pcty_fk FOREIGN KEY (pcty_id) REFERENCES procedure_types(id);


--
-- Name: proj_dast_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_dast_fk FOREIGN KEY (dast_id) REFERENCES data_stores(id);


--
-- Name: proj_grou_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_grou_fk FOREIGN KEY (grou_id) REFERENCES groups(id);


--
-- Name: proj_pers_fk_leader; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pers_fk_leader FOREIGN KEY (pers_id_leader) REFERENCES persons(id);


--
-- Name: proj_pers_fk_registerer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: prty_covo_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_covo_fk FOREIGN KEY (covo_id) REFERENCES controlled_vocabularies(id);


--
-- Name: prty_daty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_daty_fk FOREIGN KEY (daty_id) REFERENCES data_types(id);


--
-- Name: prty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: prty_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: roas_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: roas_grou_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_grou_fk FOREIGN KEY (grou_id) REFERENCES groups(id);


--
-- Name: roas_pers_fk_grantee; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pers_fk_grantee FOREIGN KEY (pers_id_grantee) REFERENCES persons(id);


--
-- Name: roas_pers_fk_registerer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: sain_proc_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_inputs
    ADD CONSTRAINT sain_proc_fk FOREIGN KEY (proc_id) REFERENCES procedures(id);


--
-- Name: sain_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_inputs
    ADD CONSTRAINT sain_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);


--
-- Name: samb_maba_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_material_batches
    ADD CONSTRAINT samb_maba_fk FOREIGN KEY (maba_id) REFERENCES material_batches(id);


--
-- Name: samb_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_material_batches
    ADD CONSTRAINT samb_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);


--
-- Name: samp_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: samp_grou_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_grou_fk FOREIGN KEY (grou_id) REFERENCES groups(id);


--
-- Name: samp_inva_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_inva_fk FOREIGN KEY (inva_id) REFERENCES invalidations(id);


--
-- Name: samp_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: samp_samp_fk_control_layout; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_samp_fk_control_layout FOREIGN KEY (samp_id_control_layout) REFERENCES samples(id);


--
-- Name: samp_samp_fk_generated_from; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_samp_fk_generated_from FOREIGN KEY (samp_id_generated_from) REFERENCES samples(id);


--
-- Name: samp_samp_fk_part_of; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_samp_fk_part_of FOREIGN KEY (samp_id_part_of) REFERENCES samples(id);


--
-- Name: samp_samp_fk_top; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_samp_fk_top FOREIGN KEY (samp_id_top) REFERENCES samples(id);


--
-- Name: samp_saty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_saty_fk FOREIGN KEY (saty_id) REFERENCES sample_types(id);


--
-- Name: sapr_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: sapr_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: sapr_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);


--
-- Name: sapr_stpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_stpt_fk FOREIGN KEY (stpt_id) REFERENCES sample_type_property_types(id) ON DELETE CASCADE;


--
-- Name: saty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: stpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: stpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id);


--
-- Name: stpt_saty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_saty_fk FOREIGN KEY (saty_id) REFERENCES sample_types(id);


--
-- Name: public; Type: ACL; Schema: -; Owner: -
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

