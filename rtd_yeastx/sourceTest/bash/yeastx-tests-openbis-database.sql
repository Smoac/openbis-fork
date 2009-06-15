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
-- Name: authorization_role; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN authorization_role AS character varying(40)
	CONSTRAINT authorization_role_check CHECK (((VALUE)::text = ANY ((ARRAY['ADMIN'::character varying, 'USER'::character varying, 'OBSERVER'::character varying, 'ETL_SERVER'::character varying])::text[])));


--
-- Name: boolean_char; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN boolean_char AS boolean DEFAULT false;


--
-- Name: boolean_char_or_unknown; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN boolean_char_or_unknown AS character(1) DEFAULT 'U'::bpchar
	CONSTRAINT boolean_char_or_unknown_check CHECK ((VALUE = ANY (ARRAY['F'::bpchar, 'T'::bpchar, 'U'::bpchar])));


--
-- Name: code; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN code AS character varying(40);


--
-- Name: column_label; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN column_label AS character varying(40);


--
-- Name: description_1000; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN description_1000 AS character varying(1000);


--
-- Name: description_250; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN description_250 AS character varying(250);


--
-- Name: description_80; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN description_80 AS character varying(80);


--
-- Name: event_type; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN event_type AS character varying(40)
	CONSTRAINT event_type_check CHECK (((VALUE)::text = ANY ((ARRAY['DELETION'::character varying, 'INVALIDATION'::character varying, 'MOVEMENT'::character varying])::text[])));


--
-- Name: file; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN file AS bytea;


--
-- Name: file_name; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN file_name AS character varying(100);


--
-- Name: generic_value; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN generic_value AS character varying(1024);


--
-- Name: object_name; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN object_name AS character varying(50);


--
-- Name: real_value; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN real_value AS real;


--
-- Name: tech_id; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN tech_id AS bigint;


--
-- Name: time_stamp; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN time_stamp AS timestamp with time zone;


--
-- Name: time_stamp_dfl; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN time_stamp_dfl AS timestamp with time zone NOT NULL DEFAULT now();


--
-- Name: user_id; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN user_id AS character varying(50);


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
-- Name: data_set_property_with_material_data_type_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION data_set_property_with_material_data_type_check() RETURNS trigger
    AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from data_set_type_property_types dstpt, property_types pt 
			 where NEW.dstpt_id = dstpt.id AND dstpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
												 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
			end if;
   end if;
   RETURN NEW;
END;
$$
    LANGUAGE plpgsql;


--
-- Name: experiment_property_with_material_data_type_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION experiment_property_with_material_data_type_check() RETURNS trigger
    AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from experiment_type_property_types etpt, property_types pt 
			 where NEW.etpt_id = etpt.id AND etpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
												 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
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
-- Name: material_property_with_material_data_type_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION material_property_with_material_data_type_check() RETURNS trigger
    AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from material_type_property_types etpt, property_types pt 
			 where NEW.mtpt_id = etpt.id AND etpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
							 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
			end if;
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
    LOCK TABLE samples IN EXCLUSIVE MODE;
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
-- Name: sample_property_with_material_data_type_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION sample_property_with_material_data_type_check() RETURNS trigger
    AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from sample_type_property_types etpt, property_types pt 
			 where NEW.stpt_id = etpt.id AND etpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
												 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
			end if;
   end if;
   RETURN NEW;
END;
$$
    LANGUAGE plpgsql;


--
-- Name: attachment_content_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE attachment_content_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: attachment_content_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('attachment_content_id_seq', 1, false);


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: attachment_contents; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE attachment_contents (
    id tech_id NOT NULL,
    value file NOT NULL
);


--
-- Name: attachment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE attachment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: attachment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('attachment_id_seq', 1, false);


--
-- Name: attachments; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE attachments (
    id tech_id NOT NULL,
    expe_id tech_id,
    samp_id tech_id,
    proj_id tech_id,
    exac_id tech_id NOT NULL,
    file_name file_name NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    version integer NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    CONSTRAINT atta_arc_ck CHECK ((((((expe_id IS NOT NULL) AND (proj_id IS NULL)) AND (samp_id IS NULL)) OR (((expe_id IS NULL) AND (proj_id IS NOT NULL)) AND (samp_id IS NULL))) OR (((expe_id IS NULL) AND (proj_id IS NULL)) AND (samp_id IS NOT NULL))))
);


--
-- Name: code_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE code_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: code_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('code_seq', 59, true);


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
    dbin_id tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    is_chosen_from_list boolean_char DEFAULT true NOT NULL,
    source_uri character varying(250)
);


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

SELECT pg_catalog.setval('controlled_vocabulary_id_seq', 1, true);


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

SELECT pg_catalog.setval('cvte_id_seq', 2, true);


--
-- Name: data; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data (
    id tech_id NOT NULL,
    code code,
    dsty_id tech_id NOT NULL,
    dast_id tech_id NOT NULL,
    expe_id tech_id NOT NULL,
    data_producer_code code,
    production_timestamp time_stamp,
    samp_id tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    is_placeholder boolean_char DEFAULT false,
    is_valid boolean_char DEFAULT true,
    modification_timestamp time_stamp DEFAULT now(),
    is_derived boolean_char NOT NULL
);


--
-- Name: data_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: data_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_id_seq', 24, true);


--
-- Name: data_set_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_properties (
    id tech_id NOT NULL,
    ds_id tech_id NOT NULL,
    dstpt_id tech_id NOT NULL,
    value generic_value,
    cvte_id tech_id,
    mate_prop_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    CONSTRAINT dspr_ck CHECK ((((((value IS NOT NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NULL)) OR (((value IS NULL) AND (cvte_id IS NOT NULL)) AND (mate_prop_id IS NULL))) OR (((value IS NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NOT NULL))))
);


--
-- Name: data_set_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_set_property_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: data_set_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_set_property_id_seq', 3, true);


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
-- Name: data_set_relationships; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_relationships (
    data_id_parent tech_id NOT NULL,
    data_id_child tech_id NOT NULL
);


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

SELECT pg_catalog.setval('data_set_type_id_seq', 4, true);


--
-- Name: data_set_type_property_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_type_property_types (
    id tech_id NOT NULL,
    dsty_id tech_id NOT NULL,
    prty_id tech_id NOT NULL,
    is_mandatory boolean_char DEFAULT false NOT NULL,
    is_managed_internally boolean_char DEFAULT false NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL
);


--
-- Name: data_set_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_set_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80,
    dbin_id tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


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

SELECT pg_catalog.setval('data_store_id_seq', 3, true);


--
-- Name: data_stores; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_stores (
    id tech_id NOT NULL,
    dbin_id tech_id NOT NULL,
    code code NOT NULL,
    download_url character varying(1024) NOT NULL,
    remote_url character varying(250) NOT NULL,
    session_token character varying(50) NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


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

SELECT pg_catalog.setval('data_type_id_seq', 9, true);


--
-- Name: data_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE data_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80 NOT NULL
);


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
-- Name: database_instances; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE database_instances (
    id tech_id NOT NULL,
    code code NOT NULL,
    uuid code NOT NULL,
    is_original_source boolean_char DEFAULT false NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL
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
-- Name: dstpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE dstpt_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: dstpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('dstpt_id_seq', 3, true);


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

SELECT pg_catalog.setval('etpt_id_seq', 10, true);


--
-- Name: event_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE event_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('event_id_seq', 24, true);


--
-- Name: events; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE events (
    id tech_id NOT NULL,
    event_type event_type NOT NULL,
    description description_250,
    reason description_250,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    entity_type character varying(80) NOT NULL,
    identifier character varying(250) NOT NULL,
    CONSTRAINT evnt_et_enum_ck CHECK (((entity_type)::text = ANY ((ARRAY['ATTACHMENT'::character varying, 'DATASET'::character varying, 'EXPERIMENT'::character varying, 'MATERIAL'::character varying, 'PROJECT'::character varying, 'PROPERTY_TYPE'::character varying, 'SAMPLE'::character varying, 'VOCABULARY'::character varying])::text[])))
);


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

SELECT pg_catalog.setval('experiment_id_seq', 2, true);


--
-- Name: experiment_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiment_properties (
    id tech_id NOT NULL,
    expe_id tech_id NOT NULL,
    etpt_id tech_id NOT NULL,
    value generic_value,
    cvte_id tech_id,
    mate_prop_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    CONSTRAINT expr_ck CHECK ((((((value IS NOT NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NULL)) OR (((value IS NULL) AND (cvte_id IS NOT NULL)) AND (mate_prop_id IS NULL))) OR (((value IS NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NOT NULL))))
);


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

SELECT pg_catalog.setval('experiment_property_id_seq', 13, true);


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

SELECT pg_catalog.setval('experiment_type_id_seq', 1, true);


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
    dbin_id tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


--
-- Name: experiments; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiments (
    id tech_id NOT NULL,
    perm_id code NOT NULL,
    code code NOT NULL,
    exty_id tech_id NOT NULL,
    mate_id_study_object tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    proj_id tech_id NOT NULL,
    inva_id tech_id,
    is_public boolean_char DEFAULT false NOT NULL
);


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

SELECT pg_catalog.setval('file_format_type_id_seq', 6, true);


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
    pers_id_registerer tech_id NOT NULL
);


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
-- Name: invalidations; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE invalidations (
    id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    reason description_250
);


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
-- Name: locator_types; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE locator_types (
    id tech_id NOT NULL,
    code code NOT NULL,
    description description_80
);


--
-- Name: material_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE material_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: material_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('material_id_seq', 1, false);


--
-- Name: material_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE material_properties (
    id tech_id NOT NULL,
    mate_id tech_id NOT NULL,
    mtpt_id tech_id NOT NULL,
    value generic_value,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    pers_id_registerer tech_id NOT NULL,
    cvte_id tech_id,
    mate_prop_id tech_id,
    CONSTRAINT mapr_ck CHECK ((((((value IS NOT NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NULL)) OR (((value IS NULL) AND (cvte_id IS NOT NULL)) AND (mate_prop_id IS NULL))) OR (((value IS NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NOT NULL))))
);


--
-- Name: material_property_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE material_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: material_property_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('material_property_id_seq', 1, false);


--
-- Name: material_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE material_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: material_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('material_type_id_seq', 1, false);


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
    dbin_id tech_id NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


--
-- Name: materials; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE materials (
    id tech_id NOT NULL,
    code code NOT NULL,
    maty_id tech_id NOT NULL,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    dbin_id tech_id NOT NULL
);


--
-- Name: mtpt_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE mtpt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: mtpt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('mtpt_id_seq', 1, false);


--
-- Name: perm_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE perm_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: perm_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('perm_id_seq', 75, true);


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

SELECT pg_catalog.setval('person_id_seq', 6, true);


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
    pers_id_registerer tech_id,
    display_settings file
);


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

SELECT pg_catalog.setval('project_id_seq', 3, true);


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
    modification_timestamp time_stamp DEFAULT now()
);


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

SELECT pg_catalog.setval('property_type_id_seq', 12, true);


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
    dbin_id tech_id NOT NULL,
    maty_prop_id tech_id
);


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

SELECT pg_catalog.setval('role_assignment_id_seq', 5, true);


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

SELECT pg_catalog.setval('sample_id_seq', 28, true);


--
-- Name: sample_properties; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sample_properties (
    id tech_id NOT NULL,
    samp_id tech_id NOT NULL,
    stpt_id tech_id NOT NULL,
    value generic_value,
    cvte_id tech_id,
    mate_prop_id tech_id,
    pers_id_registerer tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    CONSTRAINT sapr_ck CHECK ((((((value IS NOT NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NULL)) OR (((value IS NULL) AND (cvte_id IS NOT NULL)) AND (mate_prop_id IS NULL))) OR (((value IS NULL) AND (cvte_id IS NULL)) AND (mate_prop_id IS NOT NULL))))
);


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

SELECT pg_catalog.setval('sample_property_id_seq', 65, true);


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

SELECT pg_catalog.setval('sample_type_id_seq', 1, true);


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
    part_of_depth integer DEFAULT 0 NOT NULL,
    modification_timestamp time_stamp DEFAULT now()
);


--
-- Name: samples; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE samples (
    id tech_id NOT NULL,
    perm_id code NOT NULL,
    code code NOT NULL,
    expe_id tech_id,
    samp_id_top tech_id,
    samp_id_generated_from tech_id,
    saty_id tech_id NOT NULL,
    registration_timestamp time_stamp_dfl DEFAULT now() NOT NULL,
    modification_timestamp time_stamp DEFAULT now(),
    pers_id_registerer tech_id NOT NULL,
    inva_id tech_id,
    samp_id_control_layout tech_id,
    dbin_id tech_id,
    grou_id tech_id,
    samp_id_part_of tech_id,
    CONSTRAINT samp_dbin_grou_arc_ck CHECK ((((dbin_id IS NOT NULL) AND (grou_id IS NULL)) OR ((dbin_id IS NULL) AND (grou_id IS NOT NULL))))
);


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

SELECT pg_catalog.setval('stpt_id_seq', 6, true);


--
-- Data for Name: attachment_contents; Type: TABLE DATA; Schema: public; Owner: -
--

COPY attachment_contents (id, value) FROM stdin;
\.


--
-- Data for Name: attachments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY attachments (id, expe_id, samp_id, proj_id, exac_id, file_name, registration_timestamp, version, pers_id_registerer) FROM stdin;
\.


--
-- Data for Name: controlled_vocabularies; Type: TABLE DATA; Schema: public; Owner: -
--

COPY controlled_vocabularies (id, code, description, registration_timestamp, pers_id_registerer, is_managed_internally, is_internal_namespace, dbin_id, modification_timestamp, is_chosen_from_list, source_uri) FROM stdin;
1	STORAGE_FORMAT	The on-disk storage format of a data set	2009-05-30 17:56:29.269825+02	1	t	t	1	2009-05-30 17:56:29.269825+02	t	\N
\.


--
-- Data for Name: controlled_vocabulary_terms; Type: TABLE DATA; Schema: public; Owner: -
--

COPY controlled_vocabulary_terms (id, code, registration_timestamp, covo_id, pers_id_registerer) FROM stdin;
1	PROPRIETARY	2009-05-30 17:56:29.269825+02	1	1
2	BDS_DIRECTORY	2009-05-30 17:56:29.269825+02	1	1
\.


--
-- Data for Name: data; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data (id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, is_placeholder, is_valid, modification_timestamp, is_derived) FROM stdin;
\.


--
-- Data for Name: data_set_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_properties (id, ds_id, dstpt_id, value, cvte_id, mate_prop_id, pers_id_registerer, registration_timestamp, modification_timestamp) FROM stdin;
\.


--
-- Data for Name: data_set_relationships; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_relationships (data_id_parent, data_id_child) FROM stdin;
\.


--
-- Data for Name: data_set_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp) FROM stdin;
1	4	12	f	f	2	2009-06-15 16:51:16.115705+02
2	3	12	f	f	2	2009-06-15 16:51:21.094701+02
3	2	12	f	f	2	2009-06-15 16:51:25.011779+02
\.


--
-- Data for Name: data_set_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_types (id, code, description, dbin_id, modification_timestamp) FROM stdin;
1	UNKNOWN	Unknown	1	2009-05-30 17:56:29.269825+02
2	MZXML	\N	1	2009-05-30 17:57:53.908369+02
3	FIAML	\N	1	2009-05-30 17:58:01.541292+02
4	EICML	\N	1	2009-05-30 17:58:13.470848+02
\.


--
-- Data for Name: data_stores; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_stores (id, dbin_id, code, download_url, remote_url, session_token, registration_timestamp, modification_timestamp) FROM stdin;
1	1	STANDARD				2009-05-30 17:56:29.269825+02	2009-05-30 17:56:29.269825+02
2	1	DSS1	https://imsb-us-openbis.ethz.ch:8444	https://82.130.89.130:8444	090604112509432-ED135F8C68EBE5B6CC4584BB49E929EC	2009-05-30 18:41:53.132181+02	2009-06-04 11:25:09.476+02
3	1	MY-DSS	https://localhost:8889	https://127.0.0.1:8889	090615164413756-630124C2DE3C53B60AEAB049AFCECD5B	2009-06-15 16:26:55.27492+02	2009-06-15 16:44:13.821+02
\.


--
-- Data for Name: data_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_types (id, code, description) FROM stdin;
1	VARCHAR	Short text
2	MULTILINE_VARCHAR	Long text
3	INTEGER	Integer number
4	REAL	Real number, i.e. an inexact, variable-precision numeric type
5	BOOLEAN	True or False
6	TIMESTAMP	Both date and time. Format: yyyy-mm-dd hh:mm:ss
7	CONTROLLEDVOCABULARY	Controlled Vocabulary
8	MATERIAL	Reference to a material
9	HYPERLINK	Address of a web page
\.


--
-- Data for Name: database_instances; Type: TABLE DATA; Schema: public; Owner: -
--

COPY database_instances (id, code, uuid, is_original_source, registration_timestamp) FROM stdin;
1	YEASTX	6760ECCE-2FE8-4D87-9C33-0B4199BC29BE	t	2009-05-30 17:56:29.269825+02
\.


--
-- Data for Name: database_version_logs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY database_version_logs (db_version, module_name, run_status, run_status_timestamp, module_code, run_exception) FROM stdin;
034	./sql/generic/034/schema-034.sql	SUCCESS	2009-05-30 17:56:29.178	-- D:\\\\DDL\\\\postgresql\\\\schema-023.sql\\012--\\012-- Generated for ANSI SQL92 on Fri Jul 04  15:13:22 2008 by Server Generator 10.1.2.6.18\\012------------------------------------------------------------------------------------\\012--\\012--  Post-Generation Modifications:\\012--\\012--  1. Changed domain FILE from BIT(32000) to BYTEA\\012--  2. Changed domain TECH_ID from NUMERIC(20) to BIGINT\\012--  3. Changed domain BOOLEAN_CHAR from CHAR(1) DEFAULT F to BOOLEAN DEFAULT FALSE\\012--  4. Removed the check constraints to handle boolean values in Oracle for the\\012--     tables MATERIAL_TYPE_PROPERTY_TYPES, EXPERIMENT_TYPE_PROPERTY_TYPES and\\012--     SAMPLE_TYPE_PROPERTY_TYPES (AVCON_%)\\012--  5. Added the ON DELETE CASCADE qualifier to the foreign keys MAPR_MTPT_FK,\\012--     EXPR_ETPT_FK and SAPR_STPT_FK\\012--  6. Add the check constraint directly on the domain BOOLEAN_CHAR_OR_UNKNOWN\\012--     CREATE DOMAIN BOOLEAN_CHAR_OR_UNKNOWN AS CHAR(1) CHECK (VALUE in ('F', 'T', 'U')) DEFAULT 'U';\\012--  7. Add the WITH TIMEZONE qualifier to the domain TIME_STAMP\\012--     CREATE DOMAIN TIME_STAMP AS TIMESTAMP WITH TIME ZONE;\\012--  8. Add the WITH TIMEZONE and NOT NULL qualifiers to the domain TIME_STAMP_DFL\\012--     CREATE DOMAIN TIME_STAMP_DFL AS TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;\\012--  9. Extend the domain EVENT_TYPE by adding the CHECK constraint\\012--     CREATE DOMAIN EVENT_TYPE AS VARCHAR(40) CHECK (VALUE in ('DELETION', 'INVALIDATION', 'MOVEMENT'));\\012--  10. Extend the domain AUTHORIZATION_ROLE by adding the CHECK constraint\\012--     CREATE DOMAIN AUTHORIZATION_ROLE as VARCHAR(40) CHECK (VALUE IN ('ADMIN', 'USER', 'OBSERVER', 'ETL_SERVER'));\\012--  11. Added the Sequence and Index sections\\012--  12. Added DATABASE_INSTANCES.GLOBAL_CODE column for UUID\\012--  13. DATABASE_INSTANCES.GLOBAL_CODE renamed to DATABASE_INSTANCES.UUID\\012--  14. OBSERVABLE_TYPES renamed to DATA_SET_TYPES\\012--  15. OBSERVABLE_TYPE_ID_SEQ renamed to DATA_SET_TYPE_ID_SEQ\\012--  16. DATA.OBTY_ID renamed to DATA.DSTY_ID;\\012--  17. some others - the source model should be updated to make these Post-Generation Modifications minimal \\012------------------------------------------------------------------------------------\\012\\012-- Creating domains\\012\\012CREATE DOMAIN AUTHORIZATION_ROLE AS VARCHAR(40) CHECK (VALUE IN ('ADMIN', 'USER', 'OBSERVER', 'ETL_SERVER'));\\012CREATE DOMAIN BOOLEAN_CHAR AS BOOLEAN DEFAULT FALSE;\\012CREATE DOMAIN BOOLEAN_CHAR_OR_UNKNOWN AS CHAR(1) CHECK (VALUE IN ('F', 'T', 'U')) DEFAULT 'U';\\012CREATE DOMAIN CODE AS VARCHAR(40);\\012CREATE DOMAIN COLUMN_LABEL AS VARCHAR(40);\\012CREATE DOMAIN DESCRIPTION_1000 AS VARCHAR(1000);\\012CREATE DOMAIN DESCRIPTION_250 AS VARCHAR(250);\\012CREATE DOMAIN DESCRIPTION_80 AS VARCHAR(80);\\012CREATE DOMAIN EVENT_TYPE AS VARCHAR(40) CHECK (VALUE IN ('DELETION', 'INVALIDATION', 'MOVEMENT'));\\012CREATE DOMAIN FILE AS BYTEA;\\012CREATE DOMAIN FILE_NAME AS VARCHAR(100);\\012CREATE DOMAIN GENERIC_VALUE AS VARCHAR(1024);\\012CREATE DOMAIN OBJECT_NAME AS VARCHAR(50);\\012CREATE DOMAIN REAL_VALUE AS REAL;\\012CREATE DOMAIN TECH_ID AS BIGINT;\\012CREATE DOMAIN TIME_STAMP AS TIMESTAMP WITH TIME ZONE;\\012CREATE DOMAIN TIME_STAMP_DFL AS TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;\\012CREATE DOMAIN USER_ID AS VARCHAR(50);\\012-- Creating tables\\012\\012CREATE TABLE CONTROLLED_VOCABULARIES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL,IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_INTERNAL_NAMESPACE BOOLEAN_CHAR NOT NULL DEFAULT 'F',DBIN_ID TECH_ID NOT NULL, MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP, IS_CHOSEN_FROM_LIST BOOLEAN_CHAR NOT NULL DEFAULT TRUE, SOURCE_URI CHARACTER VARYING(250));\\012CREATE TABLE CONTROLLED_VOCABULARY_TERMS (ID TECH_ID NOT NULL,CODE OBJECT_NAME NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,COVO_ID TECH_ID NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL);\\012CREATE TABLE DATA (ID TECH_ID NOT NULL,CODE CODE,DSTY_ID TECH_ID NOT NULL,DAST_ID TECH_ID NOT NULL,EXPE_ID TECH_ID NOT NULL,DATA_PRODUCER_CODE CODE,PRODUCTION_TIMESTAMP TIME_STAMP,SAMP_ID TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,IS_PLACEHOLDER BOOLEAN_CHAR DEFAULT 'F',IS_DELETED BOOLEAN_CHAR DEFAULT 'F',IS_VALID BOOLEAN_CHAR DEFAULT 'T', MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP, IS_DERIVED BOOLEAN_CHAR NOT NULL);\\012CREATE TABLE DATABASE_INSTANCES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,UUID CODE NOT NULL,IS_ORIGINAL_SOURCE BOOLEAN_CHAR NOT NULL DEFAULT 'F',REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE DATA_SET_RELATIONSHIPS (DATA_ID_PARENT TECH_ID NOT NULL,DATA_ID_CHILD TECH_ID NOT NULL);\\012CREATE TABLE DATA_STORES (ID TECH_ID NOT NULL,DBIN_ID TECH_ID NOT NULL,CODE CODE NOT NULL,DOWNLOAD_URL VARCHAR(1024) NOT NULL,REMOTE_URL VARCHAR(250) NOT NULL,SESSION_TOKEN VARCHAR(50) NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE DATA_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80 NOT NULL);\\012CREATE TABLE EVENTS (ID TECH_ID NOT NULL,EVENT_TYPE EVENT_TYPE NOT NULL,DESCRIPTION DESCRIPTION_250,DATA_ID TECH_ID,REASON DESCRIPTION_250,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE EXPERIMENTS (ID TECH_ID NOT NULL,PERM_ID CODE NOT NULL,CODE CODE NOT NULL,EXTY_ID TECH_ID NOT NULL,MATE_ID_STUDY_OBJECT TECH_ID,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP, PROJ_ID TECH_ID NOT NULL,INVA_ID TECH_ID,IS_PUBLIC BOOLEAN_CHAR NOT NULL DEFAULT 'F');\\012CREATE TABLE ATTACHMENTS (ID TECH_ID NOT NULL,EXPE_ID TECH_ID,SAMP_ID TECH_ID,PROJ_ID TECH_ID,EXAC_ID TECH_ID NOT NULL,FILE_NAME FILE_NAME NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,VERSION INTEGER NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL);\\012CREATE TABLE ATTACHMENT_CONTENTS (ID TECH_ID NOT NULL,VALUE FILE NOT NULL);\\012CREATE TABLE EXPERIMENT_PROPERTIES (ID TECH_ID NOT NULL,EXPE_ID TECH_ID NOT NULL,ETPT_ID TECH_ID NOT NULL,VALUE GENERIC_VALUE,CVTE_ID TECH_ID, MATE_PROP_ID TECH_ID, PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE EXPERIMENT_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL, MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE EXPERIMENT_TYPE_PROPERTY_TYPES (ID TECH_ID NOT NULL,EXTY_ID TECH_ID NOT NULL,PRTY_ID TECH_ID NOT NULL,IS_MANDATORY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE EXTERNAL_DATA (DATA_ID TECH_ID NOT NULL,LOCATION VARCHAR(1024) NOT NULL,FFTY_ID TECH_ID NOT NULL,LOTY_ID TECH_ID NOT NULL,CVTE_ID_STOR_FMT TECH_ID NOT NULL,IS_COMPLETE BOOLEAN_CHAR_OR_UNKNOWN NOT NULL DEFAULT 'U',CVTE_ID_STORE TECH_ID);\\012CREATE TABLE FILE_FORMAT_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL);\\012CREATE TABLE GROUPS (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DBIN_ID TECH_ID NOT NULL,GROU_ID_PARENT TECH_ID,PERS_ID_LEADER TECH_ID,DESCRIPTION DESCRIPTION_250,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL);\\012CREATE TABLE INVALIDATIONS (ID TECH_ID NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,REASON DESCRIPTION_250);\\012CREATE TABLE LOCATOR_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80);\\012CREATE TABLE MATERIALS (ID TECH_ID NOT NULL,CODE CODE NOT NULL,MATY_ID TECH_ID NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP, DBIN_ID TECH_ID NOT NULL);\\012CREATE TABLE MATERIAL_PROPERTIES (ID TECH_ID NOT NULL,MATE_ID TECH_ID NOT NULL,MTPT_ID TECH_ID NOT NULL,VALUE GENERIC_VALUE,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP, PERS_ID_REGISTERER TECH_ID NOT NULL,CVTE_ID TECH_ID, MATE_PROP_ID TECH_ID);\\012CREATE TABLE MATERIAL_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL, MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE MATERIAL_TYPE_PROPERTY_TYPES (ID TECH_ID NOT NULL,MATY_ID TECH_ID NOT NULL,PRTY_ID TECH_ID NOT NULL,IS_MANDATORY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL);\\012CREATE TABLE DATA_SET_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL, MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE PERSONS (ID TECH_ID NOT NULL,FIRST_NAME VARCHAR(30),LAST_NAME VARCHAR(30),USER_ID USER_ID NOT NULL,EMAIL OBJECT_NAME,DBIN_ID TECH_ID NOT NULL,GROU_ID TECH_ID,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID, DISPLAY_SETTINGS FILE);\\012CREATE TABLE PROJECTS (ID TECH_ID NOT NULL,CODE CODE NOT NULL,GROU_ID TECH_ID NOT NULL,PERS_ID_LEADER TECH_ID,DESCRIPTION DESCRIPTION_1000,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE PROPERTY_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80 NOT NULL,LABEL COLUMN_LABEL NOT NULL,DATY_ID TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL,COVO_ID TECH_ID,IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_INTERNAL_NAMESPACE BOOLEAN_CHAR NOT NULL DEFAULT 'F',DBIN_ID TECH_ID NOT NULL, MATY_PROP_ID TECH_ID);\\012CREATE TABLE ROLE_ASSIGNMENTS (ID TECH_ID NOT NULL,ROLE_CODE AUTHORIZATION_ROLE NOT NULL,GROU_ID TECH_ID,DBIN_ID TECH_ID,PERS_ID_GRANTEE TECH_ID NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE SAMPLES (ID TECH_ID NOT NULL,PERM_ID CODE NOT NULL,CODE CODE NOT NULL,EXPE_ID TECH_ID,SAMP_ID_TOP TECH_ID,SAMP_ID_GENERATED_FROM TECH_ID,SATY_ID TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP,MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP,PERS_ID_REGISTERER TECH_ID NOT NULL,INVA_ID TECH_ID,SAMP_ID_CONTROL_LAYOUT TECH_ID,DBIN_ID TECH_ID,GROU_ID TECH_ID,SAMP_ID_PART_OF TECH_ID);\\012CREATE TABLE SAMPLE_PROPERTIES (ID TECH_ID NOT NULL,SAMP_ID TECH_ID NOT NULL,STPT_ID TECH_ID NOT NULL,VALUE GENERIC_VALUE,CVTE_ID TECH_ID,MATE_PROP_ID TECH_ID,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE SAMPLE_TYPES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,DBIN_ID TECH_ID NOT NULL, IS_LISTABLE BOOLEAN_CHAR NOT NULL DEFAULT 'T', GENERATED_FROM_DEPTH INTEGER NOT NULL DEFAULT 0, PART_OF_DEPTH INTEGER NOT NULL DEFAULT 0, MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE SAMPLE_TYPE_PROPERTY_TYPES (ID TECH_ID NOT NULL,SATY_ID TECH_ID NOT NULL,PRTY_ID TECH_ID NOT NULL,IS_MANDATORY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP, IS_DISPLAYED BOOLEAN_CHAR NOT NULL DEFAULT 'T');\\012\\012CREATE TABLE DATA_SET_PROPERTIES (ID TECH_ID NOT NULL,DS_ID TECH_ID NOT NULL,DSTPT_ID TECH_ID NOT NULL,VALUE GENERIC_VALUE,CVTE_ID TECH_ID, MATE_PROP_ID TECH_ID, PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP);\\012CREATE TABLE DATA_SET_TYPE_PROPERTY_TYPES (ID TECH_ID NOT NULL,DSTY_ID TECH_ID NOT NULL,PRTY_ID TECH_ID NOT NULL,IS_MANDATORY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP);\\012\\012-- Creating sequences\\012\\012CREATE SEQUENCE CONTROLLED_VOCABULARY_ID_SEQ;\\012CREATE SEQUENCE CVTE_ID_SEQ;\\012CREATE SEQUENCE DATABASE_INSTANCE_ID_SEQ;\\012CREATE SEQUENCE DATA_ID_SEQ;\\012CREATE SEQUENCE DATA_SET_RELATIONSHIP_ID_SEQ;\\012CREATE SEQUENCE DATA_STORE_ID_SEQ;\\012CREATE SEQUENCE DATA_TYPE_ID_SEQ;\\012CREATE SEQUENCE ETPT_ID_SEQ;\\012CREATE SEQUENCE EVENT_ID_SEQ;\\012CREATE SEQUENCE ATTACHMENT_ID_SEQ;\\012CREATE SEQUENCE ATTACHMENT_CONTENT_ID_SEQ;\\012CREATE SEQUENCE EXPERIMENT_ID_SEQ;\\012CREATE SEQUENCE EXPERIMENT_PROPERTY_ID_SEQ;\\012CREATE SEQUENCE EXPERIMENT_TYPE_ID_SEQ;\\012CREATE SEQUENCE FILE_FORMAT_TYPE_ID_SEQ;\\012CREATE SEQUENCE GROUP_ID_SEQ;\\012CREATE SEQUENCE INVALIDATION_ID_SEQ;\\012CREATE SEQUENCE LOCATOR_TYPE_ID_SEQ;\\012CREATE SEQUENCE MATERIAL_ID_SEQ;\\012CREATE SEQUENCE MATERIAL_PROPERTY_ID_SEQ;\\012CREATE SEQUENCE MATERIAL_TYPE_ID_SEQ;\\012CREATE SEQUENCE MTPT_ID_SEQ;\\012CREATE SEQUENCE DATA_SET_TYPE_ID_SEQ;\\012CREATE SEQUENCE PERSON_ID_SEQ;\\012CREATE SEQUENCE PROJECT_ID_SEQ;\\012CREATE SEQUENCE PROPERTY_TYPE_ID_SEQ;\\012CREATE SEQUENCE ROLE_ASSIGNMENT_ID_SEQ;\\012CREATE SEQUENCE SAMPLE_ID_SEQ;\\012CREATE SEQUENCE SAMPLE_PROPERTY_ID_SEQ;\\012CREATE SEQUENCE SAMPLE_TYPE_ID_SEQ;\\012CREATE SEQUENCE STPT_ID_SEQ;\\012CREATE SEQUENCE DATA_SET_PROPERTY_ID_SEQ;\\012CREATE SEQUENCE DSTPT_ID_SEQ;\\012CREATE SEQUENCE CODE_SEQ;\\012CREATE SEQUENCE PERM_ID_SEQ;\\012\\012-- Creating primary key constraints\\012\\012ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_PK PRIMARY KEY(ID);\\012ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_PK PRIMARY KEY(ID);\\012ALTER TABLE DATA ADD CONSTRAINT DATA_PK PRIMARY KEY(ID);\\012ALTER TABLE DATABASE_INSTANCES ADD CONSTRAINT DBIN_PK PRIMARY KEY(ID);\\012ALTER TABLE DATA_STORES ADD CONSTRAINT DAST_PK PRIMARY KEY(ID);\\012ALTER TABLE DATA_TYPES ADD CONSTRAINT DATY_PK PRIMARY KEY(ID);\\012ALTER TABLE EVENTS ADD CONSTRAINT EVNT_PK PRIMARY KEY(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_PK PRIMARY KEY(ID);\\012ALTER TABLE ATTACHMENTS ADD CONSTRAINT ATTA_PK PRIMARY KEY(ID);\\012ALTER TABLE ATTACHMENT_CONTENTS ADD CONSTRAINT EXAC_PK PRIMARY KEY(ID);\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_PK PRIMARY KEY(ID);\\012ALTER TABLE EXPERIMENT_TYPES ADD CONSTRAINT EXTY_PK PRIMARY KEY(ID);\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_PK PRIMARY KEY(ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_PK PRIMARY KEY(DATA_ID);\\012ALTER TABLE FILE_FORMAT_TYPES ADD CONSTRAINT FFTY_PK PRIMARY KEY(ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_PK PRIMARY KEY(ID);\\012ALTER TABLE INVALIDATIONS ADD CONSTRAINT INVA_PK PRIMARY KEY(ID);\\012ALTER TABLE LOCATOR_TYPES ADD CONSTRAINT LOTY_PK PRIMARY KEY(ID);\\012ALTER TABLE MATERIALS ADD CONSTRAINT MATE_PK PRIMARY KEY(ID);\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_PK PRIMARY KEY(ID);\\012ALTER TABLE MATERIAL_TYPES ADD CONSTRAINT MATY_PK PRIMARY KEY(ID);\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_PK PRIMARY KEY(ID);\\012ALTER TABLE DATA_SET_TYPES ADD CONSTRAINT DSTY_PK PRIMARY KEY(ID);\\012ALTER TABLE PERSONS ADD CONSTRAINT PERS_PK PRIMARY KEY(ID);\\012ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_PK PRIMARY KEY(ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_PK PRIMARY KEY(ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_PK PRIMARY KEY(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_PK PRIMARY KEY(ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_PK PRIMARY KEY(ID);\\012ALTER TABLE SAMPLE_TYPES ADD CONSTRAINT SATY_PK PRIMARY KEY(ID);\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_PK PRIMARY KEY(ID);\\012ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES ADD CONSTRAINT DSTPT_PK PRIMARY KEY(ID);\\012ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_PK PRIMARY KEY(ID);\\012\\012-- Creating unique constraints\\012\\012ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_BK_UK UNIQUE(CODE,IS_INTERNAL_NAMESPACE,DBIN_ID);\\012ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_BK_UK UNIQUE(CODE,COVO_ID);\\012ALTER TABLE DATA ADD CONSTRAINT DATA_BK_UK UNIQUE(CODE);\\012ALTER TABLE DATABASE_INSTANCES ADD CONSTRAINT DBIN_BK_UK UNIQUE(CODE);\\012ALTER TABLE DATABASE_INSTANCES ADD CONSTRAINT DBIN_UUID_UK UNIQUE(UUID);\\012ALTER TABLE DATA_SET_RELATIONSHIPS ADD CONSTRAINT DSRE_BK_UK UNIQUE(DATA_ID_CHILD,DATA_ID_PARENT);\\012ALTER TABLE DATA_STORES ADD CONSTRAINT DAST_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE DATA_TYPES ADD CONSTRAINT DATY_BK_UK UNIQUE(CODE);\\012ALTER TABLE EVENTS ADD CONSTRAINT EVNT_BK_UK UNIQUE(EVENT_TYPE,DATA_ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_BK_UK UNIQUE(CODE,PROJ_ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_PI_UK UNIQUE(PERM_ID);\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_BK_UK UNIQUE(EXPE_ID,ETPT_ID);\\012ALTER TABLE EXPERIMENT_TYPES ADD CONSTRAINT EXTY_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_BK_UK UNIQUE(EXTY_ID,PRTY_ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_BK_UK UNIQUE(LOCATION,LOTY_ID);\\012ALTER TABLE FILE_FORMAT_TYPES ADD CONSTRAINT FFTY_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE LOCATOR_TYPES ADD CONSTRAINT LOTY_BK_UK UNIQUE(CODE);\\012ALTER TABLE MATERIALS ADD CONSTRAINT MATE_BK_UK UNIQUE(CODE,MATY_ID,DBIN_ID);\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_BK_UK UNIQUE(MATE_ID,MTPT_ID);\\012ALTER TABLE MATERIAL_TYPES ADD CONSTRAINT MATY_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_BK_UK UNIQUE(MATY_ID,PRTY_ID);\\012ALTER TABLE DATA_SET_TYPES ADD CONSTRAINT DSTY_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE PERSONS ADD CONSTRAINT PERS_BK_UK UNIQUE(DBIN_ID,USER_ID);\\012ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_BK_UK UNIQUE(CODE,GROU_ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_BK_UK UNIQUE(CODE,IS_INTERNAL_NAMESPACE,DBIN_ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_GROUP_BK_UK UNIQUE(PERS_ID_GRANTEE,ROLE_CODE,GROU_ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_INSTANCE_BK_UK UNIQUE(PERS_ID_GRANTEE,ROLE_CODE,DBIN_ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_PI_UK UNIQUE(PERM_ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_BK_UK UNIQUE(SAMP_ID,STPT_ID);\\012ALTER TABLE SAMPLE_TYPES ADD CONSTRAINT SATY_BK_UK UNIQUE(CODE,DBIN_ID);\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_BK_UK UNIQUE(SATY_ID,PRTY_ID);\\012ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES ADD CONSTRAINT DSTPT_BK_UK UNIQUE(DSTY_ID,PRTY_ID);\\012ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_BK_UK UNIQUE(DS_ID,DSTPT_ID);\\012-- NOTE: following uniqueness constraints for attachments work, because (null != null) in Postgres \\012ALTER TABLE ATTACHMENTS ADD CONSTRAINT ATTA_EXPE_BK_UK UNIQUE(EXPE_ID,FILE_NAME,VERSION);\\012ALTER TABLE ATTACHMENTS ADD CONSTRAINT ATTA_PROJ_BK_UK UNIQUE(PROJ_ID,FILE_NAME,VERSION);\\012ALTER TABLE ATTACHMENTS ADD CONSTRAINT ATTA_SAMP_BK_UK UNIQUE(SAMP_ID,FILE_NAME,VERSION);\\012\\012\\012-- Creating foreign key constraints\\012\\012ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_COVO_FK FOREIGN KEY (COVO_ID) REFERENCES CONTROLLED_VOCABULARIES(ID);\\012ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE DATA ADD CONSTRAINT DATA_DSTY_FK FOREIGN KEY (DSTY_ID) REFERENCES DATA_SET_TYPES(ID);\\012ALTER TABLE DATA ADD CONSTRAINT DATA_EXPE_FK FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS(ID);\\012ALTER TABLE DATA ADD CONSTRAINT DATA_SAMP_FK FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES(ID);\\012ALTER TABLE DATA ADD CONSTRAINT DATA_DAST_FK FOREIGN KEY (DAST_ID) REFERENCES DATA_STORES(ID);\\012ALTER TABLE DATA_SET_RELATIONSHIPS ADD CONSTRAINT DSRE_DATA_FK_CHILD FOREIGN KEY (DATA_ID_CHILD) REFERENCES DATA(ID);\\012ALTER TABLE DATA_SET_RELATIONSHIPS ADD CONSTRAINT DSRE_DATA_FK_PARENT FOREIGN KEY (DATA_ID_PARENT) REFERENCES DATA(ID);\\012ALTER TABLE DATA_STORES ADD CONSTRAINT DAST_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE EVENTS ADD CONSTRAINT EVNT_DATA_FK FOREIGN KEY (DATA_ID) REFERENCES DATA(ID);\\012ALTER TABLE EVENTS ADD CONSTRAINT EVNT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_EXTY_FK FOREIGN KEY (EXTY_ID) REFERENCES EXPERIMENT_TYPES(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_INVA_FK FOREIGN KEY (INVA_ID) REFERENCES INVALIDATIONS(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_MATE_FK FOREIGN KEY (MATE_ID_STUDY_OBJECT) REFERENCES MATERIALS(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_PROJ_FK FOREIGN KEY (PROJ_ID) REFERENCES PROJECTS(ID);\\012ALTER TABLE ATTACHMENTS ADD CONSTRAINT ATTA_EXPE_FK FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS(ID);\\012ALTER TABLE ATTACHMENTS ADD CONSTRAINT ATTA_PROJ_FK FOREIGN KEY (PROJ_ID) REFERENCES PROJECTS(ID);\\012ALTER TABLE ATTACHMENTS ADD CONSTRAINT ATTA_SAMP_FK FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES(ID);\\012ALTER TABLE ATTACHMENTS ADD CONSTRAINT ATTA_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE ATTACHMENTS ADD CONSTRAINT ATTA_CONT_FK FOREIGN KEY (EXAC_ID) REFERENCES ATTACHMENT_CONTENTS(ID);\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_ETPT_FK FOREIGN KEY (ETPT_ID) REFERENCES EXPERIMENT_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_EXPE_FK FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS(ID);\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_MAPR_FK FOREIGN KEY (MATE_PROP_ID) REFERENCES MATERIALS(ID);\\012ALTER TABLE EXPERIMENT_TYPES ADD CONSTRAINT EXTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_EXTY_FK FOREIGN KEY (EXTY_ID) REFERENCES EXPERIMENT_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_CVTE_FK FOREIGN KEY (CVTE_ID_STOR_FMT) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_CVTE_STORED_ON_FK FOREIGN KEY (CVTE_ID_STORE) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_DATA_FK FOREIGN KEY (DATA_ID) REFERENCES DATA(ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_FFTY_FK FOREIGN KEY (FFTY_ID) REFERENCES FILE_FORMAT_TYPES(ID);\\012ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_LOTY_FK FOREIGN KEY (LOTY_ID) REFERENCES LOCATOR_TYPES(ID);\\012ALTER TABLE FILE_FORMAT_TYPES ADD CONSTRAINT FFTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_GROU_FK FOREIGN KEY (GROU_ID_PARENT) REFERENCES GROUPS(ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_PERS_FK_LEADER FOREIGN KEY (PERS_ID_LEADER) REFERENCES PERSONS(ID);\\012ALTER TABLE GROUPS ADD CONSTRAINT GROU_PERS_FK_REGISTERER FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE INVALIDATIONS ADD CONSTRAINT INVA_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE MATERIALS ADD CONSTRAINT MATE_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE MATERIALS ADD CONSTRAINT MATE_MATY_FK FOREIGN KEY (MATY_ID) REFERENCES MATERIAL_TYPES(ID);\\012ALTER TABLE MATERIALS ADD CONSTRAINT MATE_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_MAPR_FK FOREIGN KEY (MATE_PROP_ID) REFERENCES MATERIALS(ID);\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_MATE_FK FOREIGN KEY (MATE_ID) REFERENCES MATERIALS(ID);\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_MTPT_FK FOREIGN KEY (MTPT_ID) REFERENCES MATERIAL_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE MATERIAL_TYPES ADD CONSTRAINT MATY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_MATY_FK FOREIGN KEY (MATY_ID) REFERENCES MATERIAL_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE DATA_SET_TYPES ADD CONSTRAINT DSTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE PERSONS ADD CONSTRAINT PERS_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE PERSONS ADD CONSTRAINT PERS_GROU_FK FOREIGN KEY (GROU_ID) REFERENCES GROUPS(ID);\\012ALTER TABLE PERSONS ADD CONSTRAINT PERS_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_GROU_FK FOREIGN KEY (GROU_ID) REFERENCES GROUPS(ID);\\012ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_PERS_FK_LEADER FOREIGN KEY (PERS_ID_LEADER) REFERENCES PERSONS(ID);\\012ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_PERS_FK_REGISTERER FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_COVO_FK FOREIGN KEY (COVO_ID) REFERENCES CONTROLLED_VOCABULARIES(ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_DATY_FK FOREIGN KEY (DATY_ID) REFERENCES DATA_TYPES(ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_MATY_FK FOREIGN KEY (MATY_PROP_ID) REFERENCES MATERIAL_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_GROU_FK FOREIGN KEY (GROU_ID) REFERENCES GROUPS(ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_PERS_FK_GRANTEE FOREIGN KEY (PERS_ID_GRANTEE) REFERENCES PERSONS(ID);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_PERS_FK_REGISTERER FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_GROU_FK FOREIGN KEY (GROU_ID) REFERENCES GROUPS(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_INVA_FK FOREIGN KEY (INVA_ID) REFERENCES INVALIDATIONS(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SAMP_FK_CONTROL_LAYOUT FOREIGN KEY (SAMP_ID_CONTROL_LAYOUT) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SAMP_FK_GENERATED_FROM FOREIGN KEY (SAMP_ID_GENERATED_FROM) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SAMP_FK_PART_OF FOREIGN KEY (SAMP_ID_PART_OF) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SAMP_FK_TOP FOREIGN KEY (SAMP_ID_TOP) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_EXPE_FK FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS(ID);\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SATY_FK FOREIGN KEY (SATY_ID) REFERENCES SAMPLE_TYPES(ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_MAPR_FK FOREIGN KEY (MATE_PROP_ID) REFERENCES MATERIALS(ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_SAMP_FK FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES(ID);\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_STPT_FK FOREIGN KEY (STPT_ID) REFERENCES SAMPLE_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE SAMPLE_TYPES ADD CONSTRAINT SATY_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_SATY_FK FOREIGN KEY (SATY_ID) REFERENCES SAMPLE_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES ADD CONSTRAINT DSTPT_DSTY_FK FOREIGN KEY (DSTY_ID) REFERENCES DATA_SET_TYPES(ID)  ON DELETE CASCADE;\\012ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES ADD CONSTRAINT DSTPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES ADD CONSTRAINT DSTPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);\\012ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_DSTPT_FK FOREIGN KEY (DSTPT_ID) REFERENCES DATA_SET_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;\\012ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_DS_FK FOREIGN KEY (DS_ID) REFERENCES DATA(ID);\\012ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);\\012ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_MAPR_FK FOREIGN KEY (MATE_PROP_ID) REFERENCES MATERIALS(ID);\\012\\012-- Creating check constraints\\012\\012ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_CK CHECK \\012\\011((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL) OR \\012\\011 (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL) OR\\012\\011 (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL)\\012\\011);\\012ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_DBIN_GROU_ARC_CK CHECK ((DBIN_ID IS NOT NULL AND GROU_ID IS NULL) OR (DBIN_ID IS NULL AND GROU_ID IS NOT NULL));\\012ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_DBIN_GROU_ARC_CK CHECK ((DBIN_ID IS NOT NULL AND GROU_ID IS NULL) OR (DBIN_ID IS NULL AND GROU_ID IS NOT NULL));\\012ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_CK CHECK \\012\\011((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL) OR \\012\\011 (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL) OR\\012\\011 (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL)\\012\\011);\\012ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_CK CHECK \\012\\011((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL) OR \\012\\011 (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL) OR\\012\\011 (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL)\\012\\011);\\012ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_CK CHECK \\012\\011((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL) OR \\012\\011 (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL) OR\\012\\011 (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL)\\012\\011);\\012ALTER TABLE ATTACHMENTS ADD CONSTRAINT ATTA_ARC_CK CHECK \\012\\011((EXPE_ID IS NOT NULL AND PROJ_ID IS NULL AND SAMP_ID IS NULL) OR \\012\\011 (EXPE_ID IS NULL AND PROJ_ID IS NOT NULL AND SAMP_ID IS NULL) OR\\012\\011 (EXPE_ID IS NULL AND PROJ_ID IS NULL AND SAMP_ID IS NOT NULL)\\012\\011);\\012\\012-- Creating indices\\012\\012CREATE INDEX COVO_PERS_FK_I ON CONTROLLED_VOCABULARIES (PERS_ID_REGISTERER);\\012CREATE INDEX CVTE_COVO_FK_I ON CONTROLLED_VOCABULARY_TERMS (COVO_ID);\\012CREATE INDEX CVTE_PERS_FK_I ON CONTROLLED_VOCABULARY_TERMS (PERS_ID_REGISTERER);\\012CREATE INDEX DATA_DSTY_FK_I ON DATA (DSTY_ID);\\012CREATE INDEX DATA_SAMP_FK_I ON DATA (SAMP_ID);\\012CREATE INDEX DATA_EXPE_FK_I ON DATA (EXPE_ID);\\012CREATE INDEX DAST_DBIN_FK_I ON DATA_STORES (DBIN_ID);\\012CREATE INDEX DSRE_DATA_FK_I_CHILD ON DATA_SET_RELATIONSHIPS (DATA_ID_CHILD);\\012CREATE INDEX DSRE_DATA_FK_I_PARENT ON DATA_SET_RELATIONSHIPS (DATA_ID_PARENT);\\012CREATE INDEX ETPT_EXTY_FK_I ON EXPERIMENT_TYPE_PROPERTY_TYPES (EXTY_ID);\\012CREATE INDEX ETPT_PERS_FK_I ON EXPERIMENT_TYPE_PROPERTY_TYPES (PERS_ID_REGISTERER);\\012CREATE INDEX ETPT_PRTY_FK_I ON EXPERIMENT_TYPE_PROPERTY_TYPES (PRTY_ID);\\012CREATE INDEX EVNT_DATA_FK_I ON EVENTS (DATA_ID);\\012CREATE INDEX EVNT_PERS_FK_I ON EVENTS (PERS_ID_REGISTERER);\\012CREATE INDEX ATTA_EXPE_FK_I ON ATTACHMENTS (EXPE_ID);\\012CREATE INDEX ATTA_SAMP_FK_I ON ATTACHMENTS (SAMP_ID);\\012CREATE INDEX ATTA_PROJ_FK_I ON ATTACHMENTS (PROJ_ID);\\012CREATE INDEX ATTA_PERS_FK_I ON ATTACHMENTS (PERS_ID_REGISTERER);\\012CREATE INDEX ATTA_EXAC_FK_I ON ATTACHMENTS (EXAC_ID);\\012CREATE INDEX EXDA_CVTE_FK_I ON EXTERNAL_DATA (CVTE_ID_STOR_FMT);\\012CREATE INDEX EXDA_CVTE_STORED_ON_FK_I ON EXTERNAL_DATA (CVTE_ID_STORE);\\012CREATE INDEX EXDA_FFTY_FK_I ON EXTERNAL_DATA (FFTY_ID);\\012CREATE INDEX EXDA_LOTY_FK_I ON EXTERNAL_DATA (LOTY_ID);\\012CREATE INDEX EXPE_EXTY_FK_I ON EXPERIMENTS (EXTY_ID);\\012CREATE INDEX EXPE_INVA_FK_I ON EXPERIMENTS (INVA_ID);\\012CREATE INDEX EXPE_MATE_FK_I ON EXPERIMENTS (MATE_ID_STUDY_OBJECT);\\012CREATE INDEX EXPE_PERS_FK_I ON EXPERIMENTS (PERS_ID_REGISTERER);\\012CREATE INDEX EXPE_PROJ_FK_I ON EXPERIMENTS (PROJ_ID);\\012CREATE INDEX EXPR_CVTE_FK_I ON EXPERIMENT_PROPERTIES (CVTE_ID);\\012CREATE INDEX EXPR_ETPT_FK_I ON EXPERIMENT_PROPERTIES (ETPT_ID);\\012CREATE INDEX EXPR_EXPE_FK_I ON EXPERIMENT_PROPERTIES (EXPE_ID);\\012CREATE INDEX EXPR_PERS_FK_I ON EXPERIMENT_PROPERTIES (PERS_ID_REGISTERER);\\012CREATE INDEX GROU_DBIN_FK_I ON GROUPS (DBIN_ID);\\012CREATE INDEX GROU_GROU_FK_I ON GROUPS (GROU_ID_PARENT);\\012CREATE INDEX GROU_PERS_FK_I ON GROUPS (PERS_ID_LEADER);\\012CREATE INDEX GROU_PERS_REGISTERED_BY_FK_I ON GROUPS (PERS_ID_REGISTERER);\\012CREATE INDEX INVA_PERS_FK_I ON INVALIDATIONS (PERS_ID_REGISTERER);\\012CREATE INDEX MAPR_CVTE_FK_I ON MATERIAL_PROPERTIES (CVTE_ID);\\012CREATE INDEX MAPR_MATE_FK_I ON MATERIAL_PROPERTIES (MATE_ID);\\012CREATE INDEX MAPR_MTPT_FK_I ON MATERIAL_PROPERTIES (MTPT_ID);\\012CREATE INDEX MAPR_PERS_FK_I ON MATERIAL_PROPERTIES (PERS_ID_REGISTERER);\\012CREATE INDEX MATE_MATY_FK_I ON MATERIALS (MATY_ID);\\012CREATE INDEX MATE_PERS_FK_I ON MATERIALS (PERS_ID_REGISTERER);\\012CREATE INDEX MTPT_MATY_FK_I ON MATERIAL_TYPE_PROPERTY_TYPES (MATY_ID);\\012CREATE INDEX MTPT_PERS_FK_I ON MATERIAL_TYPE_PROPERTY_TYPES (PERS_ID_REGISTERER);\\012CREATE INDEX MTPT_PRTY_FK_I ON MATERIAL_TYPE_PROPERTY_TYPES (PRTY_ID);\\012CREATE INDEX PERS_GROU_FK_I ON PERSONS (GROU_ID);\\012CREATE INDEX PROJ_GROU_FK_I ON PROJECTS (GROU_ID);\\012CREATE INDEX PROJ_PERS_FK_I_LEADER ON PROJECTS (PERS_ID_LEADER);\\012CREATE INDEX PROJ_PERS_FK_I_REGISTERER ON PROJECTS (PERS_ID_REGISTERER);\\012CREATE INDEX PRTY_COVO_FK_I ON PROPERTY_TYPES (COVO_ID);\\012CREATE INDEX PRTY_DATY_FK_I ON PROPERTY_TYPES (DATY_ID);\\012CREATE INDEX PRTY_PERS_FK_I ON PROPERTY_TYPES (PERS_ID_REGISTERER);\\012CREATE INDEX ROAS_DBIN_FK_I ON ROLE_ASSIGNMENTS (DBIN_ID);\\012CREATE INDEX ROAS_GROU_FK_I ON ROLE_ASSIGNMENTS (GROU_ID);\\012CREATE INDEX ROAS_PERS_FK_I_GRANTEE ON ROLE_ASSIGNMENTS (PERS_ID_GRANTEE);\\012CREATE INDEX ROAS_PERS_FK_I_REGISTERER ON ROLE_ASSIGNMENTS (PERS_ID_REGISTERER);\\012CREATE INDEX SAMP_INVA_FK_I ON SAMPLES (INVA_ID);\\012CREATE INDEX SAMP_PERS_FK_I ON SAMPLES (PERS_ID_REGISTERER);\\012CREATE INDEX SAMP_SAMP_FK_I_CONTROL_LAYOUT ON SAMPLES (SAMP_ID_CONTROL_LAYOUT);\\012CREATE INDEX SAMP_SAMP_FK_I_GENERATED_FROM ON SAMPLES (SAMP_ID_GENERATED_FROM);\\012CREATE INDEX SAMP_SAMP_FK_I_PART_OF ON SAMPLES (SAMP_ID_PART_OF);\\012CREATE INDEX SAMP_SAMP_FK_I_TOP ON SAMPLES (SAMP_ID_TOP);\\012CREATE INDEX SAMP_EXPE_FK_I ON SAMPLES (EXPE_ID);\\012CREATE INDEX SAMP_CODE_I ON SAMPLES (CODE);\\012CREATE INDEX SAMP_SATY_FK_I ON SAMPLES (SATY_ID);\\012CREATE INDEX SAPR_CVTE_FK_I ON SAMPLE_PROPERTIES (CVTE_ID);\\012CREATE INDEX SAPR_PERS_FK_I ON SAMPLE_PROPERTIES (PERS_ID_REGISTERER);\\012CREATE INDEX SAPR_SAMP_FK_I ON SAMPLE_PROPERTIES (SAMP_ID);\\012CREATE INDEX SAPR_STPT_FK_I ON SAMPLE_PROPERTIES (STPT_ID);\\012CREATE INDEX STPT_PERS_FK_I ON SAMPLE_TYPE_PROPERTY_TYPES (PERS_ID_REGISTERER);\\012CREATE INDEX STPT_PRTY_FK_I ON SAMPLE_TYPE_PROPERTY_TYPES (PRTY_ID);\\012CREATE INDEX STPT_SATY_FK_I ON SAMPLE_TYPE_PROPERTY_TYPES (SATY_ID);\\012CREATE INDEX DSPR_CVTE_FK_I ON DATA_SET_PROPERTIES (CVTE_ID);\\012CREATE INDEX DSPR_DSTPT_FK_I ON DATA_SET_PROPERTIES (DSTPT_ID);\\012CREATE INDEX DSPR_DS_FK_I ON DATA_SET_PROPERTIES (DS_ID);\\012CREATE INDEX DSPR_PERS_FK_I ON DATA_SET_PROPERTIES (PERS_ID_REGISTERER);\\012CREATE INDEX DSTPT_DSTY_FK_I ON DATA_SET_TYPE_PROPERTY_TYPES (DSTY_ID);\\012CREATE INDEX DSTPT_PERS_FK_I ON DATA_SET_TYPE_PROPERTY_TYPES (PERS_ID_REGISTERER);\\012CREATE INDEX DSTPT_PRTY_FK_I ON DATA_SET_TYPE_PROPERTY_TYPES (PRTY_ID);\\012\\012-- Granting SELECT privilege to group OPENBIS_READONLY\\012\\012GRANT SELECT ON SEQUENCE attachment_content_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE attachment_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE code_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE controlled_vocabulary_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE cvte_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE data_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE data_set_property_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE data_set_relationship_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE data_set_type_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE data_store_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE data_type_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE database_instance_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE dstpt_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE etpt_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE event_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE experiment_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE experiment_property_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE experiment_type_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE file_format_type_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE group_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE invalidation_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE locator_type_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE material_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE material_property_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE material_type_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE mtpt_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE perm_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE person_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE project_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE property_type_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE role_assignment_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE sample_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE sample_property_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE sample_type_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON SEQUENCE stpt_id_seq TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE attachment_contents TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE attachments TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE controlled_vocabularies TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE controlled_vocabulary_terms TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE data TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE data_set_properties TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE data_set_relationships TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE data_set_type_property_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE data_set_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE data_stores TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE data_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE database_instances TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE database_version_logs TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE events TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE experiment_properties TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE experiment_type_property_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE experiment_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE experiments TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE external_data TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE file_format_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE groups TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE invalidations TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE locator_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE material_properties TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE material_type_property_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE material_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE materials TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE persons TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE projects TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE property_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE role_assignments TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE sample_properties TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE sample_type_property_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE sample_types TO GROUP OPENBIS_READONLY;\\012GRANT SELECT ON TABLE samples TO GROUP OPENBIS_READONLY;\\012	\N
034	./sql/postgresql/034/function-034.sql	SUCCESS	2009-05-30 17:56:29.238	-- Creating Functions\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Create function RENAME_SEQUENCE() that is required for renaming the sequences belonging to tables\\012------------------------------------------------------------------------------------\\012CREATE FUNCTION RENAME_SEQUENCE(OLD_NAME VARCHAR, NEW_NAME VARCHAR) RETURNS INTEGER AS $$\\012DECLARE\\012  CURR_SEQ_VAL   INTEGER;\\012BEGIN\\012  SELECT INTO CURR_SEQ_VAL NEXTVAL(OLD_NAME);\\012  EXECUTE 'CREATE SEQUENCE ' || NEW_NAME || ' START WITH ' || CURR_SEQ_VAL;\\012  EXECUTE 'DROP SEQUENCE ' || OLD_NAME;\\012  RETURN CURR_SEQ_VAL;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Create trigger CONTROLLED_VOCABULARY_CHECK \\012------------------------------------------------------------------------------------\\012\\012CREATE OR REPLACE FUNCTION CONTROLLED_VOCABULARY_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   v_code  CODE;\\012BEGIN\\012\\012   select code into v_code from data_types where id = NEW.daty_id;\\012\\012   -- Check if the data is of type "CONTROLLEDVOCABULARY"\\012   if v_code = 'CONTROLLEDVOCABULARY' then\\012      if NEW.covo_id IS NULL then\\012         RAISE EXCEPTION 'Insert/Update of Property Type (Code: %) failed, as its Data Type is CONTROLLEDVOCABULARY, but it is not linked to a Controlled Vocabulary.', NEW.code;\\012      end if;\\012   end if;\\012\\012   RETURN NEW;\\012\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012CREATE TRIGGER CONTROLLED_VOCABULARY_CHECK BEFORE INSERT OR UPDATE ON PROPERTY_TYPES\\012    FOR EACH ROW EXECUTE PROCEDURE CONTROLLED_VOCABULARY_CHECK();\\012\\012\\012------------------------------------------------------------------------------------\\012--  Purpose:  Create trigger EXTERNAL_DATA_STORAGE_FORMAT_CHECK \\012------------------------------------------------------------------------------------\\012\\012CREATE OR REPLACE FUNCTION EXTERNAL_DATA_STORAGE_FORMAT_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   v_covo_code  CODE;\\012   data_code CODE;\\012BEGIN\\012\\012   select code into v_covo_code from controlled_vocabularies\\012      where is_internal_namespace = true and \\012         id = (select covo_id from controlled_vocabulary_terms where id = NEW.cvte_id_stor_fmt);\\012   -- Check if the data storage format is a term of the controlled vocabulary "STORAGE_FORMAT"\\012   if v_covo_code != 'STORAGE_FORMAT' then\\012      select code into data_code from data where id = NEW.data_id; \\012      RAISE EXCEPTION 'Insert/Update of Data (Code: %) failed, as its Storage Format is %, but is required to be STORAGE_FORMAT.', data_code, v_covo_code;\\012   end if;\\012\\012   RETURN NEW;\\012\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012CREATE TRIGGER EXTERNAL_DATA_STORAGE_FORMAT_CHECK BEFORE INSERT OR UPDATE ON EXTERNAL_DATA\\012    FOR EACH ROW EXECUTE PROCEDURE EXTERNAL_DATA_STORAGE_FORMAT_CHECK();\\012\\012   \\012------------------------------------------------------------------------------------\\012--  Purpose:  Create trigger SAMPLE_CODE_UNIQUENESS_CHECK \\012------------------------------------------------------------------------------------\\012\\012CREATE OR REPLACE FUNCTION SAMPLE_CODE_UNIQUENESS_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   counter  INTEGER;\\012BEGIN\\012    LOCK TABLE samples IN EXCLUSIVE MODE;\\012\\011IF (NEW.samp_id_part_of is NULL) THEN\\012\\011\\011IF (NEW.dbin_id is not NULL) THEN\\012\\011\\011\\011SELECT count(*) into counter FROM samples \\012\\011\\011\\011\\011where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and dbin_id = NEW.dbin_id;\\012\\011\\011\\011IF (counter > 0) THEN\\012\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code already exists.', NEW.code;\\012\\011\\011\\011END IF;\\012\\011\\011ELSIF (NEW.grou_id is not NULL) THEN\\012\\011\\011\\011SELECT count(*) into counter FROM samples \\012\\011\\011\\011\\011where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and grou_id = NEW.grou_id;\\012\\011\\011\\011IF (counter > 0) THEN\\012\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because group sample with the same code already exists.', NEW.code;\\012\\011\\011\\011END IF;\\012\\011\\011END IF;\\012        ELSE\\012\\011\\011IF (NEW.dbin_id is not NULL) THEN\\012\\011\\011\\011SELECT count(*) into counter FROM samples \\012\\011\\011\\011\\011where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and dbin_id = NEW.dbin_id;\\012\\011\\011\\011IF (counter > 0) THEN\\012\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code and being the part of the same parent already exists.', NEW.code;\\012\\011\\011\\011END IF;\\012\\011\\011ELSIF (NEW.grou_id is not NULL) THEN\\012\\011\\011\\011SELECT count(*) into counter FROM samples \\012\\011\\011\\011\\011where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and grou_id = NEW.grou_id;\\012\\011\\011\\011IF (counter > 0) THEN\\012\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because group sample with the same code and being the part of the same parent already exists.', NEW.code;\\012\\011\\011\\011END IF;\\012\\011\\011END IF;\\012        END IF;   \\012   RETURN NEW;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012CREATE TRIGGER SAMPLE_CODE_UNIQUENESS_CHECK BEFORE INSERT OR UPDATE ON SAMPLES\\012    FOR EACH ROW EXECUTE PROCEDURE SAMPLE_CODE_UNIQUENESS_CHECK();\\012    \\012------------------------------------------------------------------------------------\\012--  Purpose:  Create trigger MATERIAL/SAMPLE/EXPERIMENT/DATA_SET _PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK\\012--            It checks that if material property value is assigned to the entity,\\012--\\011\\011\\011\\011\\011\\011then the material type is equal to the one described by property type.\\012------------------------------------------------------------------------------------\\012\\012CREATE OR REPLACE FUNCTION MATERIAL_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   v_type_id  CODE;\\012   v_type_id_prop  CODE;\\012BEGIN\\012   if NEW.mate_prop_id IS NOT NULL then\\012\\011\\011\\011-- find material type id of the property type \\012\\011\\011\\011select pt.maty_prop_id into v_type_id_prop \\012\\011\\011\\011  from material_type_property_types etpt, property_types pt \\012\\011\\011\\011 where NEW.mtpt_id = etpt.id AND etpt.prty_id = pt.id;\\012\\011\\011\\012\\011\\011\\011if v_type_id_prop IS NOT NULL then\\012\\011\\011\\011\\011-- find material type id of the material which consists the entity's property value\\012\\011\\011\\011\\011select entity.maty_id into v_type_id \\012\\011\\011\\011\\011  from materials entity\\012\\011\\011\\011\\011 where NEW.mate_prop_id = entity.id;\\012\\011\\011\\011\\011if v_type_id != v_type_id_prop then\\012\\011\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', \\012\\011\\011\\011\\011\\011\\011\\011 NEW.mate_prop_id, v_type_id, v_type_id_prop;\\012\\011\\011\\011\\011end if;\\012\\011\\011\\011end if;\\012   end if;\\012   RETURN NEW;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012CREATE TRIGGER MATERIAL_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK BEFORE INSERT OR UPDATE ON material_properties\\012    FOR EACH ROW EXECUTE PROCEDURE MATERIAL_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK();\\012    \\012CREATE OR REPLACE FUNCTION SAMPLE_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   v_type_id  CODE;\\012   v_type_id_prop  CODE;\\012BEGIN\\012   if NEW.mate_prop_id IS NOT NULL then\\012\\011\\011\\011-- find material type id of the property type \\012\\011\\011\\011select pt.maty_prop_id into v_type_id_prop \\012\\011\\011\\011  from sample_type_property_types etpt, property_types pt \\012\\011\\011\\011 where NEW.stpt_id = etpt.id AND etpt.prty_id = pt.id;\\012\\011\\011\\012\\011\\011\\011if v_type_id_prop IS NOT NULL then\\012\\011\\011\\011\\011-- find material type id of the material which consists the entity's property value\\012\\011\\011\\011\\011select entity.maty_id into v_type_id \\012\\011\\011\\011\\011  from materials entity\\012\\011\\011\\011\\011 where NEW.mate_prop_id = entity.id;\\012\\011\\011\\011\\011if v_type_id != v_type_id_prop then\\012\\011\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', \\012\\011\\011\\011\\011\\011\\011\\011\\011\\011\\011\\011\\011 NEW.mate_prop_id, v_type_id, v_type_id_prop;\\012\\011\\011\\011\\011end if;\\012\\011\\011\\011end if;\\012   end if;\\012   RETURN NEW;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012CREATE TRIGGER SAMPLE_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK BEFORE INSERT OR UPDATE ON sample_properties\\012    FOR EACH ROW EXECUTE PROCEDURE SAMPLE_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK();\\012    \\012CREATE OR REPLACE FUNCTION EXPERIMENT_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   v_type_id  CODE;\\012   v_type_id_prop  CODE;\\012BEGIN\\012   if NEW.mate_prop_id IS NOT NULL then\\012\\011\\011\\011-- find material type id of the property type \\012\\011\\011\\011select pt.maty_prop_id into v_type_id_prop \\012\\011\\011\\011  from experiment_type_property_types etpt, property_types pt \\012\\011\\011\\011 where NEW.etpt_id = etpt.id AND etpt.prty_id = pt.id;\\012\\011\\011\\012\\011\\011\\011if v_type_id_prop IS NOT NULL then\\012\\011\\011\\011\\011-- find material type id of the material which consists the entity's property value\\012\\011\\011\\011\\011select entity.maty_id into v_type_id \\012\\011\\011\\011\\011  from materials entity\\012\\011\\011\\011\\011 where NEW.mate_prop_id = entity.id;\\012\\011\\011\\011\\011if v_type_id != v_type_id_prop then\\012\\011\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', \\012\\011\\011\\011\\011\\011\\011\\011\\011\\011\\011\\011\\011 NEW.mate_prop_id, v_type_id, v_type_id_prop;\\012\\011\\011\\011\\011end if;\\012\\011\\011\\011end if;\\012   end if;\\012   RETURN NEW;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012CREATE TRIGGER EXPERIMENT_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK BEFORE INSERT OR UPDATE ON experiment_properties\\012    FOR EACH ROW EXECUTE PROCEDURE EXPERIMENT_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK();\\012 \\012 -- data set\\012CREATE OR REPLACE FUNCTION DATA_SET_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$\\012DECLARE\\012   v_type_id  CODE;\\012   v_type_id_prop  CODE;\\012BEGIN\\012   if NEW.mate_prop_id IS NOT NULL then\\012\\011\\011\\011-- find material type id of the property type \\012\\011\\011\\011select pt.maty_prop_id into v_type_id_prop \\012\\011\\011\\011  from data_set_type_property_types dstpt, property_types pt \\012\\011\\011\\011 where NEW.dstpt_id = dstpt.id AND dstpt.prty_id = pt.id;\\012\\011\\011\\012\\011\\011\\011if v_type_id_prop IS NOT NULL then\\012\\011\\011\\011\\011-- find material type id of the material which consists the entity's property value\\012\\011\\011\\011\\011select entity.maty_id into v_type_id \\012\\011\\011\\011\\011  from materials entity\\012\\011\\011\\011\\011 where NEW.mate_prop_id = entity.id;\\012\\011\\011\\011\\011if v_type_id != v_type_id_prop then\\012\\011\\011\\011\\011\\011RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', \\012\\011\\011\\011\\011\\011\\011\\011\\011\\011\\011\\011\\011 NEW.mate_prop_id, v_type_id, v_type_id_prop;\\012\\011\\011\\011\\011end if;\\012\\011\\011\\011end if;\\012   end if;\\012   RETURN NEW;\\012END;\\012$$ LANGUAGE 'plpgsql';\\012\\012CREATE TRIGGER DATA_SET_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK BEFORE INSERT OR UPDATE ON data_set_properties\\012    FOR EACH ROW EXECUTE PROCEDURE DATA_SET_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK();   \\012	\N
034	./sql/generic/034/data-034.sql	SUCCESS	2009-05-30 17:56:29.295	----------------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table DATABASE_INSTANCES\\012----------------------------------------------------------------------------\\012\\012INSERT INTO database_instances(\\012              id\\012            , code\\012\\011    \\011, uuid\\012            , is_original_source)\\012    VALUES (  nextval('DATABASE_INSTANCE_ID_SEQ')\\012            , 'SYSTEM_DEFAULT'\\012\\011    \\011, 'SYSTEM_DEFAULT'\\012            , 'T');\\012\\012----------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table DATA_STORES\\012----------------------------------------------------------------------\\012\\012insert into data_stores\\012(id\\012,code\\012,download_url\\012,remote_url\\012,session_token\\012,dbin_id)\\012values\\012(nextval('DATA_STORE_ID_SEQ')\\012,'STANDARD'\\012,''\\012,''\\012,''\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012----------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table PERSONS\\012-----------------------------------------------------------------------\\012\\012insert into persons\\012(id\\012,first_name\\012,last_name\\012,user_id\\012,email\\012,dbin_id)\\012values\\012(nextval('PERSON_ID_SEQ')\\012,''\\012,'System User'\\012,'system'\\012,''\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT') );\\012\\012-----------------------------------------------------------------------------------\\012--  Purpose:  Create Controlled Vocabulary STORAGE_FORMAT\\012-----------------------------------------------------------------------------------\\012insert into controlled_vocabularies \\012       ( id\\012       , code\\012       , is_internal_namespace      \\012       , description\\012       , pers_id_registerer\\012       , is_managed_internally\\012       , dbin_id )\\012values  (nextval('CONTROLLED_VOCABULARY_ID_SEQ')\\012       , 'STORAGE_FORMAT'\\012       , true\\012       , 'The on-disk storage format of a data set'\\012       , (select id from persons where user_id ='system')\\012       , true\\012       ,(select id from database_instances where code = 'SYSTEM_DEFAULT'));\\012\\012\\012-----------------------------------------------------------------------------------\\012--  Purpose:  Create Controlled Vocabulary Terms for STORAGE_FORMAT\\012-----------------------------------------------------------------------------------\\012insert into controlled_vocabulary_terms \\012       ( id\\012       , code\\012       , covo_id \\012       , pers_id_registerer )\\012values  (nextval('CVTE_ID_SEQ')\\012       , 'PROPRIETARY'\\012       , (select id from controlled_vocabularies where code = 'STORAGE_FORMAT' and is_internal_namespace = true)\\012       , (select id from persons where user_id ='system'));\\012\\012insert into controlled_vocabulary_terms \\012       ( id\\012       , code\\012       , covo_id \\012       , pers_id_registerer )\\012values  (nextval('CVTE_ID_SEQ')\\012       , 'BDS_DIRECTORY'\\012       , (select id from controlled_vocabularies where code = 'STORAGE_FORMAT' and is_internal_namespace = true)\\012       , (select id from persons where user_id ='system'));\\012\\012------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table DATA_TYPES\\012------------------------------------------------------------------\\012\\012insert into data_types\\012(id\\012,code\\012,description)\\012values \\012(nextval('DATA_TYPE_ID_SEQ')\\012,'VARCHAR'\\012,'Short text'\\012);\\012\\012insert into data_types\\012(id\\012 ,code\\012 ,description)\\012 values \\012 (nextval('DATA_TYPE_ID_SEQ')\\012 ,'MULTILINE_VARCHAR'\\012 ,'Long text'\\012);\\012\\012insert into data_types\\012(id\\012,code\\012,description)\\012values \\012(nextval('DATA_TYPE_ID_SEQ')\\012,'INTEGER'\\012,'Integer number'\\012);\\012\\012insert into data_types\\012(id\\012,code\\012,description)\\012values \\012(nextval('DATA_TYPE_ID_SEQ')\\012,'REAL'\\012,'Real number, i.e. an inexact, variable-precision numeric type'\\012);\\012\\012insert into data_types\\012(id\\012,code\\012,description)\\012values \\012(nextval('DATA_TYPE_ID_SEQ')\\012,'BOOLEAN'\\012,'True or False'\\012);\\012\\012insert into data_types\\012(id\\012,code\\012,description)\\012values \\012(nextval('DATA_TYPE_ID_SEQ')\\012,'TIMESTAMP'\\012,'Both date and time. Format: yyyy-mm-dd hh:mm:ss'\\012);\\012\\012insert into data_types\\012(id\\012 ,code\\012 ,description)\\012 values \\012 (nextval('DATA_TYPE_ID_SEQ')\\012 ,'CONTROLLEDVOCABULARY'\\012 ,'Controlled Vocabulary'\\012);\\012\\012insert into data_types\\012(id\\012 ,code\\012 ,description)\\012 values \\012 (nextval('DATA_TYPE_ID_SEQ')\\012 ,'MATERIAL'\\012 ,'Reference to a material'\\012);\\012\\012insert into data_types\\012(id\\012 ,code\\012 ,description)\\012 values \\012 (nextval('DATA_TYPE_ID_SEQ')\\012 ,'HYPERLINK'\\012 ,'Address of a web page'\\012);\\012\\012----------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table PROPERTY_TYPES\\012-----------------------------------------------------------------------\\012\\012insert into property_types\\012(id\\012,code\\012,description\\012,label\\012,daty_id\\012,pers_id_registerer\\012,dbin_id)\\012values \\012(nextval('PROPERTY_TYPE_ID_SEQ')\\012,'DESCRIPTION'\\012,'A Description'\\012,'Description'\\012,(select id from data_types where code ='VARCHAR')\\012,(select id from persons where user_id ='system')\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012--------------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table DATA_SET_TYPES\\012--------------------------------------------------------------------------\\012\\012insert into data_set_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('DATA_SET_TYPE_ID_SEQ')\\012,'UNKNOWN'\\012,'Unknown'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012-------------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table FILE_FORMAT_TYPES\\012-------------------------------------------------------------------------\\012\\012insert into file_format_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('FILE_FORMAT_TYPE_ID_SEQ')\\012,'HDF5'\\012,'Hierarchical Data Format File, version 5'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into file_format_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('FILE_FORMAT_TYPE_ID_SEQ')\\012,'PROPRIETARY'\\012,'Proprietary Format File'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into file_format_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('FILE_FORMAT_TYPE_ID_SEQ')\\012,'SRF'\\012,'Sequence Read Format File'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into file_format_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('FILE_FORMAT_TYPE_ID_SEQ')\\012,'TIFF'\\012,'TIFF File'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into file_format_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('FILE_FORMAT_TYPE_ID_SEQ')\\012,'TSV'\\012,'Tab Separated Values File'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012insert into file_format_types\\012(id\\012,code\\012,description\\012,dbin_id)\\012values \\012(nextval('FILE_FORMAT_TYPE_ID_SEQ')\\012,'XML'\\012,'XML File'\\012,(select id from database_instances where code = 'SYSTEM_DEFAULT')\\012);\\012\\012---------------------------------------------------------------------\\012--  Purpose:  Insert an initial data set into the table LOCATOR_TYPES\\012---------------------------------------------------------------------\\012\\012insert into locator_types\\012(id\\012,code\\012,description)\\012values \\012(nextval('LOCATOR_TYPE_ID_SEQ')\\012,'RELATIVE_LOCATION'\\012,'Relative Location'\\012);\\012	\N
035	./source//sql/postgresql/migration/migration-034-035.sql	SUCCESS	2009-06-15 16:26:24.705	------------------------------------------------------------------------------------\\012-- Extend events table\\012------------------------------------------------------------------------------------\\012\\012ALTER TABLE events\\012\\011\\011ADD COLUMN entity_type VARCHAR(80);\\012ALTER TABLE events\\012\\011\\011ADD COLUMN identifier VARCHAR(250);\\012\\011\\011\\012-- Creating check constraints\\012\\012ALTER TABLE events ADD CONSTRAINT evnt_et_enum_ck CHECK \\012\\011\\011(entity_type IN ('ATTACHMENT', 'DATASET', 'EXPERIMENT', 'MATERIAL', 'PROJECT', 'PROPERTY_TYPE', 'SAMPLE', 'VOCABULARY')); \\012\\012-- simple migration of events (all old rows contain data set deletion information)\\012\\012UPDATE events SET \\012\\011\\011entity_type = 'DATASET',\\012\\011\\011identifier = data_id,\\012\\011\\011description = data_id;\\012\\012ALTER TABLE events\\012    DROP COLUMN data_id;\\012\\012-- remove deleted data sets with their properties and relations to other data sets\\012\\012CREATE VIEW deleted_data_ids AS\\012    SELECT id FROM data WHERE is_deleted = 'TRUE';\\012DELETE FROM data_set_relationships \\012\\011\\011WHERE data_id_parent IN (SELECT * FROM deleted_data_ids) \\012\\011\\011OR data_id_child IN (SELECT * FROM deleted_data_ids);\\012DELETE FROM data_set_properties \\012    WHERE ds_id IN (SELECT * FROM deleted_data_ids);\\012DELETE FROM external_data\\012\\011\\011WHERE data_id IN (SELECT * FROM deleted_data_ids);\\012DELETE FROM data WHERE is_deleted = 'TRUE';    \\012DROP VIEW deleted_data_ids;\\012\\012-- remove old columns\\012\\012ALTER TABLE data\\012\\011\\011DROP COLUMN is_deleted;\\012\\011\\011\\012-- add constraints\\012\\012ALTER TABLE events\\012    ALTER COLUMN entity_type SET NOT NULL;\\012ALTER TABLE events\\012    ALTER COLUMN identifier SET NOT NULL;\\012ALTER TABLE events\\012\\011\\011ADD CONSTRAINT evnt_bk_uk UNIQUE(EVENT_TYPE,ENTITY_TYPE,IDENTIFIER);\\012	\N
\.


--
-- Data for Name: events; Type: TABLE DATA; Schema: public; Owner: -
--

COPY events (id, event_type, description, reason, pers_id_registerer, registration_timestamp, entity_type, identifier) FROM stdin;
1	DELETION	20090615165213631-71	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090615165213631-71
2	DELETION	20090615165221798-73	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090615165221798-73
3	DELETION	20090615165218470-72	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090615165218470-72
4	DELETION	20090615165731854-74	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090615165731854-74
5	DELETION	20090615165736653-75	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090615165736653-75
6	DELETION	20090604125047480-43	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604125047480-43
7	DELETION	20090604125028993-39	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604125028993-39
8	DELETION	20090604125020288-37	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604125020288-37
9	DELETION	20090604125024647-38	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604125024647-38
10	DELETION	20090604125114563-49	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604125114563-49
11	DELETION	20090604125033513-40	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604125033513-40
12	DELETION	20090604125042962-42	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604125042962-42
13	DELETION	20090604125052463-44	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604125052463-44
14	DELETION	20090604125100966-46	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604125100966-46
15	DELETION	20090604125105577-47	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604125105577-47
16	DELETION	20090604125109831-48	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604125109831-48
17	DELETION	20090604125038003-41	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604125038003-41
18	DELETION	20090604125056669-45	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604125056669-45
19	DELETION	20090604125015687-36	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604125015687-36
20	DELETION	20090604125006019-34	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604125006019-34
21	DELETION	20090604125011220-35	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604125011220-35
22	DELETION	20090604125000982-33	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604125000982-33
23	DELETION	20090604124951585-31	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604124951585-31
24	DELETION	20090604124956349-32	no reason	2	2009-06-15 16:58:51.305513+02	DATASET	20090604124956349-32
\.


--
-- Data for Name: experiment_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_properties (id, expe_id, etpt_id, value, cvte_id, mate_prop_id, pers_id_registerer, registration_timestamp, modification_timestamp) FROM stdin;
5	1	10	Juliane Schulz	\N	\N	3	2009-06-01 15:17:43.944843+02	2009-06-01 15:17:44.076+02
6	1	7	Kinase KO screen 	\N	\N	3	2009-06-01 15:17:43.944843+02	2009-06-01 15:17:44.077+02
7	1	6	Kinase Screen	\N	\N	3	2009-06-01 15:17:43.944843+02	2009-06-01 15:17:44.077+02
8	1	9	glucose	\N	\N	3	2009-06-01 15:17:43.944843+02	2009-06-01 15:17:44.077+02
9	1	8	2009-02-19 00:00:00 GMT+01:00	\N	\N	3	2009-06-01 15:17:43.944843+02	2009-06-01 15:17:44.077+02
10	2	8	2009-02-17 00:00:00 GMT+01:00	\N	\N	3	2009-06-01 20:25:11.797362+02	2009-06-01 20:25:11.867+02
11	2	10	Jörg Büscher	\N	\N	3	2009-06-01 20:25:11.797362+02	2009-06-01 20:25:11.867+02
12	2	7	PPP mutants	\N	\N	3	2009-06-01 20:25:11.797362+02	2009-06-01 20:25:11.867+02
13	2	6	Praktikum Metabolic Networks 2009	\N	\N	3	2009-06-01 20:25:11.797362+02	2009-06-01 20:25:11.867+02
\.


--
-- Data for Name: experiment_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_type_property_types (id, exty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp) FROM stdin;
6	1	2	f	f	3	2009-06-01 14:26:23.783782+02
7	1	1	f	f	3	2009-06-01 14:26:36.628467+02
8	1	3	f	f	3	2009-06-01 14:26:48.940546+02
9	1	4	f	f	3	2009-06-01 14:26:58.31583+02
10	1	11	f	f	3	2009-06-01 14:51:49.319049+02
\.


--
-- Data for Name: experiment_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_types (id, code, description, dbin_id, modification_timestamp) FROM stdin;
1	GENERAL	general for all-omics 	1	2009-05-31 11:37:27.496595+02
\.


--
-- Data for Name: experiments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiments (id, perm_id, code, exty_id, mate_id_study_object, pers_id_registerer, registration_timestamp, modification_timestamp, proj_id, inva_id, is_public) FROM stdin;
1	20090601134726148-1	E1	1	\N	3	2009-06-01 13:47:26.087576+02	2009-06-01 15:17:44.112+02	1	\N	f
2	20090601202511852-2	E2	1	\N	3	2009-06-01 20:25:11.797362+02	2009-06-15 16:57:37.451+02	3	\N	f
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
1	HDF5	Hierarchical Data Format File, version 5	1
2	PROPRIETARY	Proprietary Format File	1
3	SRF	Sequence Read Format File	1
4	TIFF	TIFF File	1
5	TSV	Tab Separated Values File	1
6	XML	XML File	1
\.


--
-- Data for Name: groups; Type: TABLE DATA; Schema: public; Owner: -
--

COPY groups (id, code, dbin_id, grou_id_parent, pers_id_leader, description, registration_timestamp, pers_id_registerer) FROM stdin;
1	IMSB_ZAMBONI	1	\N	\N	\N	2009-05-30 17:58:57.704819+02	2
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
-- Data for Name: material_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_properties (id, mate_id, mtpt_id, value, registration_timestamp, modification_timestamp, pers_id_registerer, cvte_id, mate_prop_id) FROM stdin;
\.


--
-- Data for Name: material_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_type_property_types (id, maty_id, prty_id, is_mandatory, is_managed_internally, registration_timestamp, pers_id_registerer) FROM stdin;
\.


--
-- Data for Name: material_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_types (id, code, description, dbin_id, modification_timestamp) FROM stdin;
\.


--
-- Data for Name: materials; Type: TABLE DATA; Schema: public; Owner: -
--

COPY materials (id, code, maty_id, pers_id_registerer, registration_timestamp, modification_timestamp, dbin_id) FROM stdin;
\.


--
-- Data for Name: persons; Type: TABLE DATA; Schema: public; Owner: -
--

COPY persons (id, first_name, last_name, user_id, email, dbin_id, grou_id, registration_timestamp, pers_id_registerer, display_settings) FROM stdin;
1		System User	system		1	\N	2009-05-30 17:56:29.269825+02	\N	\N
5	Bernd	Rinn	brinn	bernd.rinn@bsse.ethz.ch	1	\N	2009-05-30 17:57:05.646423+02	2	\N
6	ETL Server	YeastX RTD Project	etlserver_yeastx	bernd.rinn@bsse.ethz.ch	1	\N	2009-05-30 17:57:17.063403+02	2	\N
4	Angela	Bauch	baucha	angela.bauch@bsse.ethz.ch	1	\N	2009-05-30 17:57:00.259618+02	2	\\254\\355\\000\\005sr\\000Ach.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\001L\\000\\016columnSettingst\\000\\017Ljava/util/Map;xpsr\\000\\027java.util.LinkedHashMap4\\300N\\\\\\020l\\300\\373\\002\\000\\001Z\\000\\013accessOrderxr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\002t\\000 experiment-details-grid-DATA_SETsr\\000\\023java.util.ArrayListx\\201\\322\\035\\231\\307a\\235\\003\\000\\001I\\000\\004sizexp\\000\\000\\000\\017w\\004\\000\\000\\000\\020sr\\000?ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\004Z\\000\\011hasFilterZ\\000\\006hiddenI\\000\\005widthL\\000\\010columnIDt\\000\\022Ljava/lang/String;xp\\001\\000\\000\\000\\000\\371t\\000\\004CODEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\013PARENT_CODEsq\\000~\\000\\011\\000\\000\\000\\000\\000\\310t\\000\\021SAMPLE_IDENTIFIERsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\013SAMPLE_TYPEsq\\000~\\000\\011\\000\\000\\000\\000\\000\\310t\\000\\021REGISTRATION_DATEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\012IS_INVALIDsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\013SOURCE_TYPEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\013IS_COMPLETEsq\\000~\\000\\011\\001\\001\\000\\000\\000\\226t\\000\\010LOCATIONsq\\000~\\000\\011\\001\\001\\000\\000\\000\\226t\\000\\020FILE_FORMAT_TYPEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\015DATA_SET_TYPEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\310t\\000\\017PRODUCTION_DATEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\022DATA_PRODUCER_CODEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\007PERM_IDsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\021SHOW_DETAILS_LINKxt\\000\\032property-type-browser-gridsq\\000~\\000\\007\\000\\000\\000\\013w\\004\\000\\000\\000\\020sq\\000~\\000\\011\\001\\000\\000\\000\\000\\271t\\000\\005LABELsq\\000~\\000\\011\\001\\000\\000\\000\\000\\310q\\000~\\000\\014sq\\000~\\000\\011\\001\\000\\000\\000\\000\\310t\\000\\011DATA_TYPEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\016DATA_TYPE_CODEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\012VOCABULARYsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\015MATERIAL_TYPEsq\\000~\\000\\011\\000\\000\\000\\000\\001\\207t\\000\\013DESCRIPTIONsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\014SAMPLE_TYPESsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\020EXPERIMENT_TYPESsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\016MATERIAL_TYPESsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\016DATA_SET_TYPESxx\\000
3	Nicola	Zamboni	nzamboni	zamboni@imsb.biol.ethz.ch	1	\N	2009-05-30 17:56:53.799928+02	2	\\254\\355\\000\\005sr\\000Ach.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\001L\\000\\016columnSettingst\\000\\017Ljava/util/Map;xpsr\\000\\027java.util.LinkedHashMap4\\300N\\\\\\020l\\300\\373\\002\\000\\001Z\\000\\013accessOrderxr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\006t\\000%property-type-assignment-browser-gridsr\\000\\023java.util.ArrayListx\\201\\322\\035\\231\\307a\\235\\003\\000\\001I\\000\\004sizexp\\000\\000\\000\\007w\\004\\000\\000\\000\\012sr\\000?ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\004Z\\000\\011hasFilterZ\\000\\006hiddenI\\000\\005widthL\\000\\010columnIDt\\000\\022Ljava/lang/String;xp\\001\\000\\000\\000\\000yt\\000\\022PROPERTY_TYPE_CODEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\005LABELsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\013DESCRIPTIONsq\\000~\\000\\011\\001\\000\\000\\000\\000\\310t\\000\\020ENTITY_TYPE_CODEsq\\000~\\000\\011\\001\\000\\000\\000\\000\\226t\\000\\013ENTITY_KINDsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\014IS_MANDATORYsq\\000~\\000\\011\\000\\000\\000\\000\\000\\310t\\000\\011DATA_TYPExt\\000&entity-browser-grid-EXPERIMENT-GENERALsq\\000~\\000\\007\\000\\000\\000\\017w\\004\\000\\000\\000\\020sq\\000~\\000\\011\\001\\000\\000\\000\\0008t\\000\\004CODEsq\\000~\\000\\011\\000\\000\\000\\000\\000\\321t\\000\\022property-USER-NAMEsq\\000~\\000\\011\\000\\000\\000\\000\\000\\320t\\000\\031property-USER-DESCRIPTIONsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\017EXPERIMENT_TYPEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\025EXPERIMENT_IDENTIFIERsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\021DATABASE_INSTANCEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\005GROUPsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\007PROJECTsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\013REGISTRATORsq\\000~\\000\\011\\000\\000\\000\\000\\000\\310t\\000\\021REGISTRATION_DATEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\012IS_INVALIDsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\007PERM_IDsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\021SHOW_DETAILS_LINKsq\\000~\\000\\011\\000\\001\\000\\000\\000xt\\000\\034property-USER-EXPERIMENTDATEsq\\000~\\000\\011\\000\\001\\000\\000\\000xt\\000\\022property-USER-TAGSxt\\000"entity-browser-grid-SAMPLE-GENERALsq\\000~\\000\\007\\000\\000\\000\\027w\\004\\000\\000\\000\\031sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000&sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000(sq\\000~\\000\\011\\001\\000\\000\\000\\000\\226q\\000~\\000\\034sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\021SAMPLE_IDENTIFIERsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\013SAMPLE_TYPEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\022IS_INSTANCE_SAMPLEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\0000sq\\000~\\000\\011\\001\\000\\000\\000\\000[t\\000\\012EXPERIMENTsq\\000~\\000\\011\\000\\001\\000\\000\\000\\310q\\000~\\000$sq\\000~\\000\\011\\001\\000\\000\\000\\000Tq\\000~\\000*sq\\000~\\000\\011\\000\\000\\000\\000\\000\\226q\\000~\\000,sq\\000~\\000\\011\\000\\000\\000\\000\\001,q\\000~\\000.sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\0002sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\0004sq\\000~\\000\\011\\000\\001\\000\\000\\000xt\\000\\032property-USER-SAMPLEAMOUNTsq\\000~\\000\\011\\000\\001\\000\\000\\000xt\\000\\030property-USER-SAMPLENAMEsq\\000~\\000\\011\\000\\001\\000\\000\\000xt\\000 property-USER-SAMPLEPERTURBATIONsq\\000~\\000\\011\\000\\001\\000\\000\\000xt\\000\\034property-USER-SAMPLEPOSITIONsq\\000~\\000\\011\\000\\001\\000\\000\\000xt\\000\\033property-USER-SAMPLESPECIESsq\\000~\\000\\011\\000\\001\\000\\000\\000xt\\000\\030property-USER-SAMPLETIMEsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\024generatedFromParent1sq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\024generatedFromParent2sq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\024generatedFromParent3xt\\000\\036experiment-details-grid-SAMPLEsq\\000~\\000\\007\\000\\000\\000\\024w\\004\\000\\000\\000\\031sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000&sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000(sq\\000~\\000\\011\\001\\000\\000\\000\\000\\226q\\000~\\000\\034sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000?sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000Asq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000Csq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\0000sq\\000~\\000\\011\\001\\000\\000\\000\\000\\226q\\000~\\000Fsq\\000~\\000\\011\\000\\001\\000\\000\\000\\310q\\000~\\000$sq\\000~\\000\\011\\001\\001\\000\\000\\000\\226q\\000~\\000*sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000,sq\\000~\\000\\011\\000\\001\\000\\000\\001,q\\000~\\000.sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\0002sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\0004sq\\000~\\000\\011\\000\\001\\000\\000\\000xq\\000~\\000Nsq\\000~\\000\\011\\000\\000\\000\\000\\000xq\\000~\\000Psq\\000~\\000\\011\\000\\000\\000\\000\\000xq\\000~\\000Rsq\\000~\\000\\011\\000\\001\\000\\000\\000xq\\000~\\000Tsq\\000~\\000\\011\\000\\000\\000\\000\\001\\021q\\000~\\000Vsq\\000~\\000\\011\\000\\001\\000\\000\\000xq\\000~\\000Xxt\\000 experiment-details-grid-DATA_SETsq\\000~\\000\\007\\000\\000\\000\\017w\\004\\000\\000\\000\\020sq\\000~\\000\\011\\001\\000\\000\\000\\000\\264q\\000~\\000\\034sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\013PARENT_CODEsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\015DATA_SET_TYPEsq\\000~\\000\\011\\000\\000\\000\\000\\000\\310q\\000~\\000?sq\\000~\\000\\011\\000\\000\\000\\000\\000\\226q\\000~\\000Asq\\000~\\000\\011\\000\\000\\000\\000\\000\\310q\\000~\\000.sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\0000sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\013SOURCE_TYPEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\013IS_COMPLETEsq\\000~\\000\\011\\001\\001\\000\\000\\000\\226t\\000\\010LOCATIONsq\\000~\\000\\011\\001\\001\\000\\000\\000\\226t\\000\\020FILE_FORMAT_TYPEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\310t\\000\\017PRODUCTION_DATEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\022DATA_PRODUCER_CODEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\0002sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\0004xt\\000$data-set-search-result-grid-DATA_SETsq\\000~\\000\\007\\000\\000\\000\\033w\\004\\000\\000\\000&sq\\000~\\000\\011\\001\\000\\000\\000\\000\\310q\\000~\\000\\034sq\\000~\\000\\011\\000\\000\\000\\000\\000\\310q\\000~\\000ysq\\000~\\000\\011\\001\\001\\000\\000\\000\\310q\\000~\\000\\205sq\\000~\\000\\011\\000\\000\\000\\000\\000xq\\000~\\000{sq\\000~\\000\\011\\001\\001\\000\\000\\000xt\\000\\011FILE_TYPEsq\\000~\\000\\011\\000\\000\\000\\000\\000dt\\000\\006SAMPLEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000?sq\\000~\\000\\011\\000\\001\\000\\000\\000dq\\000~\\000Asq\\000~\\000\\011\\000\\000\\000\\000\\000dq\\000~\\000(sq\\000~\\000\\011\\000\\000\\000\\000\\000dq\\000~\\000*sq\\000~\\000\\011\\000\\000\\000\\000\\000dq\\000~\\000Fsq\\000~\\000\\011\\000\\001\\000\\000\\000xq\\000~\\000"sq\\000~\\000\\011\\000\\001\\000\\000\\000\\310q\\000~\\000.sq\\000~\\000\\011\\000\\001\\000\\000\\000dq\\000~\\0000sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\0002sq\\000~\\000\\011\\000\\000\\000\\000\\000\\226q\\000~\\0004sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\034property-expUSER-DESCRIPTIONsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\037property-expUSER-EXPERIMENTDATEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\025property-expUSER-NAMEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\026property-expUSER-OWNERsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\025property-expUSER-TAGSsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000 property-sampleUSER-SAMPLEAMOUNTsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\036property-sampleUSER-SAMPLENAMEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000&property-sampleUSER-SAMPLEPERTURBATIONsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000"property-sampleUSER-SAMPLEPOSITIONsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000!property-sampleUSER-SAMPLESPECIESsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\036property-sampleUSER-SAMPLETIMExx\\000
2	Tomasz	Pylak	tpylak	tomasz.pylak@systemsx.ch	1	1	2009-05-30 17:56:43.024101+02	1	\\254\\355\\000\\005sr\\000Ach.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\001L\\000\\016columnSettingst\\000\\017Ljava/util/Map;xpsr\\000\\027java.util.LinkedHashMap4\\300N\\\\\\020l\\300\\373\\002\\000\\001Z\\000\\013accessOrderxr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\006t\\000\\022group-browser-gridsr\\000\\023java.util.ArrayListx\\201\\322\\035\\231\\307a\\235\\003\\000\\001I\\000\\004sizexp\\000\\000\\000\\005w\\004\\000\\000\\000\\012sr\\000?ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting\\000\\000\\000\\000\\000\\000\\000\\001\\002\\000\\004Z\\000\\011hasFilterZ\\000\\006hiddenI\\000\\005widthL\\000\\010columnIDt\\000\\022Ljava/lang/String;xp\\000\\000\\000\\000\\000\\024t\\000\\010expandersq\\000~\\000\\011\\000\\000\\000\\000\\000ut\\000\\004codesq\\000~\\000\\011\\000\\001\\000\\000\\000dt\\000\\006leadersq\\000~\\000\\011\\000\\000\\000\\000\\000dt\\000\\013registratorsq\\000~\\000\\011\\000\\000\\000\\000\\001,t\\000\\020registrationDatext\\000%property-type-assignment-browser-gridsq\\000~\\000\\007\\000\\000\\000\\007w\\004\\000\\000\\000\\012sq\\000~\\000\\011\\001\\000\\000\\000\\000\\310t\\000\\022PROPERTY_TYPE_CODEsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\005LABELsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\013DESCRIPTIONsq\\000~\\000\\011\\001\\000\\000\\000\\000\\310t\\000\\020ENTITY_TYPE_CODEsq\\000~\\000\\011\\001\\000\\000\\000\\000\\226t\\000\\013ENTITY_KINDsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\014IS_MANDATORYsq\\000~\\000\\011\\000\\000\\000\\000\\000\\310t\\000\\011DATA_TYPExt\\000\\032property-type-browser-gridsq\\000~\\000\\007\\000\\000\\000\\013w\\004\\000\\000\\000\\020sq\\000~\\000\\011\\001\\000\\000\\000\\000\\226q\\000~\\000\\032sq\\000~\\000\\011\\001\\000\\000\\000\\000\\265t\\000\\004CODEsq\\000~\\000\\011\\001\\000\\000\\000\\000\\310q\\000~\\000$sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\016DATA_TYPE_CODEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\012VOCABULARYsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\015MATERIAL_TYPEsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226q\\000~\\000\\034sq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\014SAMPLE_TYPESsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\020EXPERIMENT_TYPESsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\016MATERIAL_TYPESsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\016DATA_SET_TYPESxt\\000\\036experiment-details-grid-SAMPLEsq\\000~\\000\\007\\000\\000\\000\\024w\\004\\000\\000\\000\\031sq\\000~\\000\\011\\001\\000\\000\\000\\000xt\\000\\030property-USER-SAMPLENAMEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\021DATABASE_INSTANCEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\005GROUPsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226q\\000~\\000)sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\021SAMPLE_IDENTIFIERsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\013SAMPLE_TYPEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\022IS_INSTANCE_SAMPLEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\012IS_INVALIDsq\\000~\\000\\011\\001\\000\\000\\000\\000\\226t\\000\\012EXPERIMENTsq\\000~\\000\\011\\000\\001\\000\\000\\000\\310t\\000\\025EXPERIMENT_IDENTIFIERsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\007PROJECTsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\013REGISTRATORsq\\000~\\000\\011\\000\\000\\000\\000\\001,t\\000\\021REGISTRATION_DATEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\007PERM_IDsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\021SHOW_DETAILS_LINKsq\\000~\\000\\011\\000\\001\\000\\000\\000xt\\000\\032property-USER-SAMPLEAMOUNTsq\\000~\\000\\011\\000\\001\\000\\000\\000xt\\000 property-USER-SAMPLEPERTURBATIONsq\\000~\\000\\011\\000\\001\\000\\000\\000xt\\000\\034property-USER-SAMPLEPOSITIONsq\\000~\\000\\011\\000\\001\\000\\000\\000xt\\000\\033property-USER-SAMPLESPECIESsq\\000~\\000\\011\\000\\001\\000\\000\\000xt\\000\\030property-USER-SAMPLETIMExt\\000"entity-browser-grid-SAMPLE-GENERALsq\\000~\\000\\007\\000\\000\\000\\027w\\004\\000\\000\\000\\031sq\\000~\\000\\011\\001\\000\\000\\000\\000xq\\000~\\000=sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000?sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000Asq\\000~\\000\\011\\000\\000\\000\\000\\000\\226q\\000~\\000)sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000Dsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000Fsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000Hsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000Jsq\\000~\\000\\011\\001\\000\\000\\000\\000\\226q\\000~\\000Lsq\\000~\\000\\011\\000\\001\\000\\000\\000\\310q\\000~\\000Nsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226q\\000~\\000Psq\\000~\\000\\011\\000\\000\\000\\000\\000\\226q\\000~\\000Rsq\\000~\\000\\011\\000\\000\\000\\000\\001,q\\000~\\000Tsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000Vsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000Xsq\\000~\\000\\011\\000\\001\\000\\000\\000xq\\000~\\000Zsq\\000~\\000\\011\\000\\001\\000\\000\\000xq\\000~\\000\\\\sq\\000~\\000\\011\\000\\001\\000\\000\\000xq\\000~\\000^sq\\000~\\000\\011\\000\\001\\000\\000\\000xq\\000~\\000`sq\\000~\\000\\011\\000\\001\\000\\000\\000xq\\000~\\000bsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\024generatedFromParent1sq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\024generatedFromParent2sq\\000~\\000\\011\\000\\000\\000\\000\\000\\226t\\000\\024generatedFromParent3xt\\000$data-set-search-result-grid-DATA_SETsq\\000~\\000\\007\\000\\000\\000\\034w\\004\\000\\000\\000&sq\\000~\\000\\011\\001\\000\\000\\000\\000\\315q\\000~\\000)sq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\006PARENTsq\\000~\\000\\011\\001\\001\\000\\000\\000\\310t\\000\\010LOCATIONsq\\000~\\000\\011\\000\\000\\000\\000\\000xt\\000\\015DATA_SET_TYPEsq\\000~\\000\\011\\001\\000\\000\\000\\000xt\\000\\011FILE_TYPEsq\\000~\\000\\011\\000\\000\\000\\000\\000dt\\000\\006SAMPLEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000Dsq\\000~\\000\\011\\000\\000\\000\\000\\000dq\\000~\\000Fsq\\000~\\000\\011\\000\\000\\000\\000\\000dq\\000~\\000Asq\\000~\\000\\011\\000\\000\\000\\000\\000dq\\000~\\000Psq\\000~\\000\\011\\000\\000\\000\\000\\000dq\\000~\\000Lsq\\000~\\000\\011\\000\\000\\000\\000\\000xt\\000\\017EXPERIMENT_TYPEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\310q\\000~\\000Tsq\\000~\\000\\011\\000\\001\\000\\000\\000dq\\000~\\000Jsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226q\\000~\\000Vsq\\000~\\000\\011\\000\\000\\000\\000\\000\\226q\\000~\\000Xsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\037property-dsUSER-DATASETCOMMENTSsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\034property-expUSER-DESCRIPTIONsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\037property-expUSER-EXPERIMENTDATEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\025property-expUSER-NAMEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\026property-expUSER-OWNERsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\025property-expUSER-TAGSsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000 property-sampleUSER-SAMPLEAMOUNTsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\036property-sampleUSER-SAMPLENAMEsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000&property-sampleUSER-SAMPLEPERTURBATIONsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000"property-sampleUSER-SAMPLEPOSITIONsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000!property-sampleUSER-SAMPLESPECIESsq\\000~\\000\\011\\000\\001\\000\\000\\000\\226t\\000\\036property-sampleUSER-SAMPLETIMExx\\000
\.


--
-- Data for Name: projects; Type: TABLE DATA; Schema: public; Owner: -
--

COPY projects (id, code, grou_id, pers_id_leader, description, pers_id_registerer, registration_timestamp, modification_timestamp) FROM stdin;
1	FIA-TOF	1	\N	FIA-TOF	3	2009-06-01 13:46:00.215059+02	2009-06-01 13:47:26.168+02
2	YEAST	1	\N	General yeast datasets	3	2009-06-01 20:15:19.236894+02	2009-06-01 20:15:19.263+02
3	BACTERIA	1	\N	\N	3	2009-06-01 20:23:29.863317+02	2009-06-01 20:25:11.868+02
\.


--
-- Data for Name: property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) FROM stdin;
1	DESCRIPTION	A Description	Description	1	2009-05-30 17:56:29.269825+02	1	\N	f	f	1	\N
2	NAME	Sample name, should be unique in the experiment.	Name	1	2009-05-30 18:02:06.461267+02	2	\N	f	f	1	\N
3	EXPERIMENTDATE	Experiment Date	Experiment Date	6	2009-06-01 14:07:31.941222+02	3	\N	f	f	1	\N
4	TAGS	Tags	Tags	1	2009-06-01 14:07:54.719787+02	3	\N	f	f	1	\N
5	SAMPLENAME	Sample name	Name	1	2009-06-01 14:17:22.560685+02	3	\N	f	f	1	\N
6	SAMPLESPECIES	Species, Strain, Cell, Line	Species/Strain/Cell	1	2009-06-01 14:19:27.669845+02	3	\N	f	f	1	\N
7	SAMPLEPERTURBATION	Condition, Mutation, Inhibitor, ...	Perturbation	1	2009-06-01 14:20:44.911475+02	3	\N	f	f	1	\N
8	SAMPLETIME	Time point (min)	Time point (min)	4	2009-06-01 14:21:59.491022+02	3	\N	f	f	1	\N
9	SAMPLEPOSITION	Plate, Well	Position	1	2009-06-01 14:22:40.365013+02	3	\N	f	f	1	\N
10	SAMPLEAMOUNT	Amount, Optical density, Volume	Amount	1	2009-06-01 14:23:18.910691+02	3	\N	f	f	1	\N
11	OWNER	Owner	Owner	1	2009-06-01 14:51:34.338217+02	3	\N	f	f	1	\N
12	DATASETCOMMENTS	Comments	Comments	1	2009-06-01 21:04:40.38093+02	3	\N	f	f	1	\N
\.


--
-- Data for Name: role_assignments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY role_assignments (id, role_code, grou_id, dbin_id, pers_id_grantee, pers_id_registerer, registration_timestamp) FROM stdin;
4	ADMIN	\N	1	5	2	2009-05-30 18:03:41.368996+02
5	ETL_SERVER	\N	1	6	2	2009-05-30 18:03:50.924238+02
2	ADMIN	\N	1	3	2	2009-05-30 18:03:25.248494+02
3	ADMIN	\N	1	4	2	2009-05-30 18:03:33.585567+02
1	ADMIN	\N	1	2	1	2009-05-30 17:56:43.024101+02
\.


--
-- Data for Name: sample_properties; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_properties (id, samp_id, stpt_id, value, cvte_id, mate_prop_id, pers_id_registerer, registration_timestamp, modification_timestamp) FROM stdin;
1	1	2	Escherichia coli MG1655	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:36.953+02
2	1	3	wt	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:36.953+02
3	1	1	glucose 1	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:36.953+02
4	2	2	Escherichia coli MG1655	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:36.962+02
5	2	1	glucose 2	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:36.962+02
6	2	3	wt	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:36.963+02
7	3	2	Escherichia coli MG1655	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:36.971+02
8	3	3	wt	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:36.971+02
9	3	1	glucose 3	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:36.971+02
10	4	2	Escherichia coli MG1655	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:36.978+02
11	4	3	wt	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:36.979+02
12	4	1	malate 1	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:36.979+02
13	5	1	malate 2	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:36.987+02
14	5	2	Escherichia coli MG1655	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:36.987+02
15	5	3	wt	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:36.988+02
16	6	2	Escherichia coli MG1655	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:36.996+02
17	6	1	malate 3	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:36.996+02
18	6	3	wt	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:36.996+02
19	7	3	pgi	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.003+02
21	7	2	Escherichia coli MG1655	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.004+02
22	8	3	pgi	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.012+02
24	8	2	Escherichia coli MG1655	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.012+02
25	9	3	pgi	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.02+02
26	9	1	pgi 3	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.02+02
27	9	2	Escherichia coli MG1655	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.02+02
28	10	2	Escherichia coli MG1655	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.03+02
29	10	3	zwf	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.03+02
30	10	1	zwf 1	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.03+02
31	11	3	zwf	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.041+02
32	11	2	Escherichia coli MG1655	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.041+02
33	11	1	zwf 2	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.041+02
34	12	3	zwf	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.051+02
35	12	1	zwf 3	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.052+02
36	12	2	Escherichia coli MG1655	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.052+02
37	13	3	pntAB	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.062+02
38	13	2	Escherichia coli MG1655	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.062+02
39	13	1	pntAB 1	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.062+02
40	14	2	Escherichia coli MG1655	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.072+02
41	14	3	pntAB	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.073+02
42	14	1	pntAB 2	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.073+02
43	15	2	Escherichia coli MG1655	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.085+02
44	15	1	pntAB 3	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.085+02
45	15	3	pntAB	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.085+02
46	16	1	udhA 1	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.102+02
47	16	2	Escherichia coli MG1655	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.102+02
48	16	3	udhA	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.103+02
49	17	2	Escherichia coli MG1655	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.112+02
50	17	3	udhA	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.113+02
51	17	1	udhA 2	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.113+02
52	18	1	udhA 3	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.123+02
53	18	2	Escherichia coli MG1655	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.123+02
54	18	3	udhA	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.123+02
55	19	1	MM08	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.134+02
56	19	2	MM08	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.134+02
57	20	1	std 0.015	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.144+02
58	21	1	std 0.41	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.154+02
59	22	1	std 0.046	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.164+02
60	23	1	std 0.137	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.173+02
61	24	1	std 1.23	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.182+02
62	25	1	std 3.7	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.192+02
63	26	1	std 11.1	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.202+02
64	27	1	std 33.3	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.212+02
65	28	1	std 100	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-01 20:38:37.221+02
20	7	1	any	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-15 16:54:23.341+02
23	8	1	any	\N	\N	3	2009-06-01 20:38:36.710065+02	2009-06-15 16:54:36.455+02
\.


--
-- Data for Name: sample_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_type_property_types (id, saty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, is_displayed) FROM stdin;
1	1	5	f	f	3	2009-06-01 14:28:36.909152+02	f
2	1	6	f	f	3	2009-06-01 14:29:31.405341+02	f
3	1	7	f	f	3	2009-06-01 14:29:44.560458+02	f
4	1	8	f	f	3	2009-06-01 14:30:02.102428+02	f
5	1	9	f	f	3	2009-06-01 14:30:18.3007+02	f
6	1	10	f	f	3	2009-06-01 14:30:28.122336+02	f
\.


--
-- Data for Name: sample_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_types (id, code, description, dbin_id, is_listable, generated_from_depth, part_of_depth, modification_timestamp) FROM stdin;
1	GENERAL	\N	1	t	3	0	2009-06-01 14:15:38.971757+02
\.


--
-- Data for Name: samples; Type: TABLE DATA; Schema: public; Owner: -
--

COPY samples (id, perm_id, code, expe_id, samp_id_top, samp_id_generated_from, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, inva_id, samp_id_control_layout, dbin_id, grou_id, samp_id_part_of) FROM stdin;
20	20090601203836884-22	S50	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-01 20:46:02.598+02	3	\N	\N	\N	1	\N
21	20090601203836888-23	S51	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-01 20:46:14.833+02	3	\N	\N	\N	1	\N
22	20090601203836893-24	S52	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-01 20:46:28.863+02	3	\N	\N	\N	1	\N
23	20090601203836898-25	S53	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-01 20:46:41.746+02	3	\N	\N	\N	1	\N
24	20090601203836903-26	S54	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-01 20:46:53.497+02	3	\N	\N	\N	1	\N
25	20090601203836908-27	S55	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-01 20:47:11.43+02	3	\N	\N	\N	1	\N
26	20090601203836913-28	S56	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-01 20:47:31.009+02	3	\N	\N	\N	1	\N
27	20090601203836917-29	S57	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-01 20:47:46.741+02	3	\N	\N	\N	1	\N
28	20090601203836924-30	S58	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-01 20:47:59.406+02	3	\N	\N	\N	1	\N
3	20090601203836796-5	S33	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-04 12:50:04.621+02	3	\N	\N	\N	1	\N
4	20090601203836802-6	S34	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-04 12:50:09.885+02	3	\N	\N	\N	1	\N
5	20090601203836809-7	S35	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-04 12:50:14.339+02	3	\N	\N	\N	1	\N
6	20090601203836814-8	S36	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-04 12:50:19.028+02	3	\N	\N	\N	1	\N
9	20090601203836828-11	S39	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-04 12:50:32.285+02	3	\N	\N	\N	1	\N
13	20090601203836846-15	S43	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-04 12:50:36.762+02	3	\N	\N	\N	1	\N
14	20090601203836854-16	S44	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-04 12:50:41.746+02	3	\N	\N	\N	1	\N
15	20090601203836859-17	S45	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-04 12:50:46.272+02	3	\N	\N	\N	1	\N
19	20090601203836879-21	S49	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-04 12:50:50.978+02	3	\N	\N	\N	1	\N
16	20090601203836864-18	S46	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-04 12:50:55.44+02	3	\N	\N	\N	1	\N
17	20090601203836869-19	S47	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-04 12:50:59.685+02	3	\N	\N	\N	1	\N
18	20090601203836874-20	S48	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-04 12:51:04.321+02	3	\N	\N	\N	1	\N
10	20090601203836833-12	S40	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-04 12:51:08.626+02	3	\N	\N	\N	1	\N
11	20090601203836837-13	S41	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-04 12:51:13.361+02	3	\N	\N	\N	1	\N
12	20090601203836842-14	S42	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-04 12:51:17.604+02	3	\N	\N	\N	1	\N
1	20090601203836776-3	S31	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-15 16:52:15.037+02	3	\N	\N	\N	1	\N
7	20090601203836819-9	S37	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-15 16:54:23.34+02	3	\N	\N	\N	1	\N
8	20090601203836824-10	S38	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-15 16:54:36.454+02	3	\N	\N	\N	1	\N
2	20090601203836791-4	S32	2	\N	\N	1	2009-06-01 20:38:36.710065+02	2009-06-15 16:57:37.451+02	3	\N	\N	\N	1	\N
\.


--
-- Name: atta_expe_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_expe_bk_uk UNIQUE (expe_id, file_name, version);


--
-- Name: atta_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_pk PRIMARY KEY (id);


--
-- Name: atta_proj_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_proj_bk_uk UNIQUE (proj_id, file_name, version);


--
-- Name: atta_samp_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_samp_bk_uk UNIQUE (samp_id, file_name, version);


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
-- Name: dspr_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_bk_uk UNIQUE (ds_id, dstpt_id);


--
-- Name: dspr_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_pk PRIMARY KEY (id);


--
-- Name: dsre_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_relationships
    ADD CONSTRAINT dsre_bk_uk UNIQUE (data_id_child, data_id_parent);


--
-- Name: dstpt_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_bk_uk UNIQUE (dsty_id, prty_id);


--
-- Name: dstpt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_pk PRIMARY KEY (id);


--
-- Name: dsty_bk_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT dsty_bk_uk UNIQUE (code, dbin_id);


--
-- Name: dsty_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT dsty_pk PRIMARY KEY (id);


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
    ADD CONSTRAINT evnt_bk_uk UNIQUE (event_type, entity_type, identifier);


--
-- Name: evnt_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY events
    ADD CONSTRAINT evnt_pk PRIMARY KEY (id);


--
-- Name: exac_pk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY attachment_contents
    ADD CONSTRAINT exac_pk PRIMARY KEY (id);


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
-- Name: expe_pi_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_pi_uk UNIQUE (perm_id);


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
-- Name: samp_pi_uk; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_pi_uk UNIQUE (perm_id);


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
-- Name: atta_exac_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX atta_exac_fk_i ON attachments USING btree (exac_id);


--
-- Name: atta_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX atta_expe_fk_i ON attachments USING btree (expe_id);


--
-- Name: atta_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX atta_pers_fk_i ON attachments USING btree (pers_id_registerer);


--
-- Name: atta_proj_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX atta_proj_fk_i ON attachments USING btree (proj_id);


--
-- Name: atta_samp_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX atta_samp_fk_i ON attachments USING btree (samp_id);


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
-- Name: data_dsty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_dsty_fk_i ON data USING btree (dsty_id);


--
-- Name: data_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_expe_fk_i ON data USING btree (expe_id);


--
-- Name: data_samp_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX data_samp_fk_i ON data USING btree (samp_id);


--
-- Name: dspr_cvte_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dspr_cvte_fk_i ON data_set_properties USING btree (cvte_id);


--
-- Name: dspr_ds_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dspr_ds_fk_i ON data_set_properties USING btree (ds_id);


--
-- Name: dspr_dstpt_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dspr_dstpt_fk_i ON data_set_properties USING btree (dstpt_id);


--
-- Name: dspr_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dspr_pers_fk_i ON data_set_properties USING btree (pers_id_registerer);


--
-- Name: dsre_data_fk_i_child; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dsre_data_fk_i_child ON data_set_relationships USING btree (data_id_child);


--
-- Name: dsre_data_fk_i_parent; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dsre_data_fk_i_parent ON data_set_relationships USING btree (data_id_parent);


--
-- Name: dstpt_dsty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dstpt_dsty_fk_i ON data_set_type_property_types USING btree (dsty_id);


--
-- Name: dstpt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dstpt_pers_fk_i ON data_set_type_property_types USING btree (pers_id_registerer);


--
-- Name: dstpt_prty_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX dstpt_prty_fk_i ON data_set_type_property_types USING btree (prty_id);


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
-- Name: evnt_pers_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX evnt_pers_fk_i ON events USING btree (pers_id_registerer);


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
-- Name: samp_code_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_code_i ON samples USING btree (code);


--
-- Name: samp_expe_fk_i; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX samp_expe_fk_i ON samples USING btree (expe_id);


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
-- Name: data_set_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER data_set_property_with_material_data_type_check
    BEFORE INSERT OR UPDATE ON data_set_properties
    FOR EACH ROW
    EXECUTE PROCEDURE data_set_property_with_material_data_type_check();


--
-- Name: experiment_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER experiment_property_with_material_data_type_check
    BEFORE INSERT OR UPDATE ON experiment_properties
    FOR EACH ROW
    EXECUTE PROCEDURE experiment_property_with_material_data_type_check();


--
-- Name: external_data_storage_format_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER external_data_storage_format_check
    BEFORE INSERT OR UPDATE ON external_data
    FOR EACH ROW
    EXECUTE PROCEDURE external_data_storage_format_check();


--
-- Name: material_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER material_property_with_material_data_type_check
    BEFORE INSERT OR UPDATE ON material_properties
    FOR EACH ROW
    EXECUTE PROCEDURE material_property_with_material_data_type_check();


--
-- Name: sample_code_uniqueness_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_code_uniqueness_check
    BEFORE INSERT OR UPDATE ON samples
    FOR EACH ROW
    EXECUTE PROCEDURE sample_code_uniqueness_check();


--
-- Name: sample_property_with_material_data_type_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER sample_property_with_material_data_type_check
    BEFORE INSERT OR UPDATE ON sample_properties
    FOR EACH ROW
    EXECUTE PROCEDURE sample_property_with_material_data_type_check();


--
-- Name: atta_cont_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_cont_fk FOREIGN KEY (exac_id) REFERENCES attachment_contents(id);


--
-- Name: atta_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);


--
-- Name: atta_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: atta_proj_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_proj_fk FOREIGN KEY (proj_id) REFERENCES projects(id);


--
-- Name: atta_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);


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
-- Name: data_dast_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_dast_fk FOREIGN KEY (dast_id) REFERENCES data_stores(id);


--
-- Name: data_dsty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_dsty_fk FOREIGN KEY (dsty_id) REFERENCES data_set_types(id);


--
-- Name: data_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);


--
-- Name: data_samp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data
    ADD CONSTRAINT data_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);


--
-- Name: dspr_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: dspr_ds_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_ds_fk FOREIGN KEY (ds_id) REFERENCES data(id);


--
-- Name: dspr_dstpt_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_dstpt_fk FOREIGN KEY (dstpt_id) REFERENCES data_set_type_property_types(id) ON DELETE CASCADE;


--
-- Name: dspr_mapr_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);


--
-- Name: dspr_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


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
-- Name: dstpt_dsty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_dsty_fk FOREIGN KEY (dsty_id) REFERENCES data_set_types(id) ON DELETE CASCADE;


--
-- Name: dstpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: dstpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;


--
-- Name: dsty_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT dsty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: etpt_exty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_exty_fk FOREIGN KEY (exty_id) REFERENCES experiment_types(id) ON DELETE CASCADE;


--
-- Name: etpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: etpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;


--
-- Name: evnt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY events
    ADD CONSTRAINT evnt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


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
-- Name: expr_mapr_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);


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
-- Name: mapr_cvte_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);


--
-- Name: mapr_mapr_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);


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
    ADD CONSTRAINT mtpt_maty_fk FOREIGN KEY (maty_id) REFERENCES material_types(id) ON DELETE CASCADE;


--
-- Name: mtpt_pers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);


--
-- Name: mtpt_prty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;


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
-- Name: prty_maty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_maty_fk FOREIGN KEY (maty_prop_id) REFERENCES material_types(id) ON DELETE CASCADE;


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
-- Name: samp_dbin_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);


--
-- Name: samp_expe_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);


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
-- Name: sapr_mapr_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);


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
    ADD CONSTRAINT stpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;


--
-- Name: stpt_saty_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_saty_fk FOREIGN KEY (saty_id) REFERENCES sample_types(id) ON DELETE CASCADE;


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

