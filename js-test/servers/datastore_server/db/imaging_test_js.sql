--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
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
-- Name: channel_color; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN channel_color AS character varying(20)
	CONSTRAINT channel_color_check CHECK (((VALUE)::text = ANY ((ARRAY['BLUE'::character varying, 'GREEN'::character varying, 'RED'::character varying, 'RED_GREEN'::character varying, 'RED_BLUE'::character varying, 'GREEN_BLUE'::character varying])::text[])));


--
-- Name: code; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN code AS character varying(40);


--
-- Name: color_component; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN color_component AS character varying(40)
	CONSTRAINT color_component_check CHECK (((VALUE)::text = ANY ((ARRAY['RED'::character varying, 'GREEN'::character varying, 'BLUE'::character varying])::text[])));


--
-- Name: description; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN description AS character varying(200);


--
-- Name: file_path; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN file_path AS character varying(1000);


--
-- Name: long_name; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN long_name AS text;


--
-- Name: tech_id; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN tech_id AS bigint;


--
-- Name: channel_stacks_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION channel_stacks_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   v_cont_id  CODE;
BEGIN

   select cont_id into v_cont_id from image_data_sets where id = NEW.ds_id;

   -- Check that if there is no spot than there is no dataset container as well
   if v_cont_id IS NULL then
      if NEW.spot_id IS NOT NULL then
         RAISE EXCEPTION 'Insert/Update of CHANNEL_STACKS failed, as the dataset container is not set, but spot is (spot id = %).',NEW.spot_id;
      end if;
	 else
      if NEW.spot_id IS NULL then
         RAISE EXCEPTION 'Insert/Update of CHANNEL_STACKS failed, as the dataset container is set (id = %), but spot is not set.',v_cont_id;
      end if; 
   end if;
   RETURN NEW;
END;
$$;


--
-- Name: delete_empty_acquired_images(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION delete_empty_acquired_images() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	delete from acquired_images where id = OLD.id;
	RETURN NEW;
END;
$$;


--
-- Name: delete_unused_images(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION delete_unused_images() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
   delete from images where id = OLD.img_id or id = OLD.thumbnail_id;
   RETURN NEW;
END;
$$;


--
-- Name: delete_unused_nulled_images(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION delete_unused_nulled_images() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	if NEW.img_id IS NULL then
		if OLD.img_id IS NOT NULL then
		  delete from images where id = OLD.img_id;
		end if;
	end if;
	if NEW.thumbnail_id IS NULL then
		if OLD.thumbnail_id IS NOT NULL then
		  delete from images where id = OLD.thumbnail_id;
		end if;
	end if;
	RETURN NEW;
END;
$$;


--
-- Name: image_transformations_default_check(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION image_transformations_default_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   v_is_default boolean;
BEGIN
   if NEW.is_default = 'T' then
	   select is_default into v_is_default from IMAGE_TRANSFORMATIONS 
	   	where is_default = 'T' 
	   			  and channel_id = NEW.channel_id
	   				and id != NEW.id;
	   if v_is_default is NOT NULL then
	      RAISE EXCEPTION 'Insert/Update of image transformation (Code: %) failed, as the new record has is_default set to true and there is already a default record defined.', NEW.code;
	   end if;
   end if;

   RETURN NEW;
END;
$$;


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: acquired_images; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE acquired_images (
    id bigint NOT NULL,
    img_id tech_id,
    thumbnail_id tech_id,
    image_transformer_factory bytea,
    channel_stack_id tech_id NOT NULL,
    channel_id tech_id NOT NULL
);


--
-- Name: acquired_images_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE acquired_images_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: acquired_images_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE acquired_images_id_seq OWNED BY acquired_images.id;


--
-- Name: acquired_images_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('acquired_images_id_seq', 9684, true);


--
-- Name: analysis_data_sets; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE analysis_data_sets (
    id bigint NOT NULL,
    perm_id code NOT NULL,
    cont_id tech_id
);


--
-- Name: analysis_data_sets_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE analysis_data_sets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: analysis_data_sets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE analysis_data_sets_id_seq OWNED BY analysis_data_sets.id;


--
-- Name: analysis_data_sets_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('analysis_data_sets_id_seq', 3, true);


--
-- Name: channel_stacks; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE channel_stacks (
    id bigint NOT NULL,
    x integer,
    y integer,
    z_in_m real,
    t_in_sec real,
    series_number integer,
    is_representative boolean_char DEFAULT false NOT NULL,
    ds_id tech_id NOT NULL,
    spot_id tech_id
);


--
-- Name: channel_stacks_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE channel_stacks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: channel_stacks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE channel_stacks_id_seq OWNED BY channel_stacks.id;


--
-- Name: channel_stacks_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('channel_stacks_id_seq', 4500, true);


--
-- Name: channels; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE channels (
    id bigint NOT NULL,
    code long_name NOT NULL,
    label long_name NOT NULL,
    description description,
    wavelength integer,
    red_cc integer NOT NULL,
    green_cc integer NOT NULL,
    blue_cc integer NOT NULL,
    ds_id tech_id,
    exp_id tech_id,
    CONSTRAINT channels_ds_exp_arc_ck CHECK ((((ds_id IS NOT NULL) AND (exp_id IS NULL)) OR ((ds_id IS NULL) AND (exp_id IS NOT NULL))))
);


--
-- Name: channels_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE channels_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: channels_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE channels_id_seq OWNED BY channels.id;


--
-- Name: channels_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('channels_id_seq', 17, true);


--
-- Name: containers; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE containers (
    id bigint NOT NULL,
    perm_id code NOT NULL,
    spots_width integer,
    spots_height integer,
    expe_id tech_id NOT NULL
);


--
-- Name: containers_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE containers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: containers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE containers_id_seq OWNED BY containers.id;


--
-- Name: containers_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('containers_id_seq', 2, true);


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
-- Name: experiments; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE experiments (
    id bigint NOT NULL,
    perm_id code NOT NULL,
    image_transformer_factory bytea
);


--
-- Name: experiments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE experiments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: experiments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE experiments_id_seq OWNED BY experiments.id;


--
-- Name: experiments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('experiments_id_seq', 2, true);


--
-- Name: feature_defs; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE feature_defs (
    id bigint NOT NULL,
    code long_name NOT NULL,
    label long_name NOT NULL,
    description description,
    ds_id tech_id NOT NULL
);


--
-- Name: feature_defs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE feature_defs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: feature_defs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE feature_defs_id_seq OWNED BY feature_defs.id;


--
-- Name: feature_defs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('feature_defs_id_seq', 12, true);


--
-- Name: feature_values; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE feature_values (
    id bigint NOT NULL,
    z_in_m real,
    t_in_sec real,
    "values" bytea NOT NULL,
    fd_id tech_id NOT NULL
);


--
-- Name: feature_values_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE feature_values_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: feature_values_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE feature_values_id_seq OWNED BY feature_values.id;


--
-- Name: feature_values_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('feature_values_id_seq', 12, true);


--
-- Name: feature_vocabulary_terms; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE feature_vocabulary_terms (
    id bigint NOT NULL,
    code long_name NOT NULL,
    sequence_number integer NOT NULL,
    fd_id tech_id NOT NULL
);


--
-- Name: feature_vocabulary_terms_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE feature_vocabulary_terms_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: feature_vocabulary_terms_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE feature_vocabulary_terms_id_seq OWNED BY feature_vocabulary_terms.id;


--
-- Name: feature_vocabulary_terms_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('feature_vocabulary_terms_id_seq', 6, true);


--
-- Name: image_data_sets; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE image_data_sets (
    id bigint NOT NULL,
    perm_id code NOT NULL,
    fields_width integer,
    fields_height integer,
    image_transformer_factory bytea,
    is_multidimensional boolean_char NOT NULL,
    image_library_name long_name,
    image_library_reader_name long_name,
    cont_id tech_id
);


--
-- Name: image_data_sets_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE image_data_sets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: image_data_sets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE image_data_sets_id_seq OWNED BY image_data_sets.id;


--
-- Name: image_data_sets_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('image_data_sets_id_seq', 11, true);


--
-- Name: image_transformations; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE image_transformations (
    id bigint NOT NULL,
    code long_name NOT NULL,
    label long_name NOT NULL,
    description character varying(1000),
    image_transformer_factory bytea NOT NULL,
    is_editable boolean_char NOT NULL,
    is_default boolean_char DEFAULT false NOT NULL,
    channel_id tech_id NOT NULL
);


--
-- Name: image_transformations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE image_transformations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: image_transformations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE image_transformations_id_seq OWNED BY image_transformations.id;


--
-- Name: image_transformations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('image_transformations_id_seq', 72, true);


--
-- Name: image_zoom_level_transformations; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE image_zoom_level_transformations (
    id bigint NOT NULL,
    zoom_level_id tech_id NOT NULL,
    channel_id tech_id NOT NULL,
    image_transformation_id tech_id NOT NULL
);


--
-- Name: image_zoom_level_transformations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE image_zoom_level_transformations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: image_zoom_level_transformations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE image_zoom_level_transformations_id_seq OWNED BY image_zoom_level_transformations.id;


--
-- Name: image_zoom_level_transformations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('image_zoom_level_transformations_id_seq', 15, true);


--
-- Name: image_zoom_levels; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE image_zoom_levels (
    id bigint NOT NULL,
    is_original boolean_char NOT NULL,
    container_dataset_id tech_id NOT NULL,
    physical_dataset_perm_id text NOT NULL,
    path file_path,
    width integer,
    height integer,
    color_depth integer,
    file_type character varying(20)
);


--
-- Name: image_zoom_levels_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE image_zoom_levels_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: image_zoom_levels_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE image_zoom_levels_id_seq OWNED BY image_zoom_levels.id;


--
-- Name: image_zoom_levels_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('image_zoom_levels_id_seq', 21, true);


--
-- Name: images; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE images (
    id bigint NOT NULL,
    path file_path NOT NULL,
    image_id code,
    color color_component
);


--
-- Name: images_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE images_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: images_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE images_id_seq OWNED BY images.id;


--
-- Name: images_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('images_id_seq', 17550, true);


--
-- Name: spots; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE spots (
    id bigint NOT NULL,
    x integer,
    y integer,
    cont_id tech_id NOT NULL
);


--
-- Name: spots_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE spots_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: spots_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE spots_id_seq OWNED BY spots.id;


--
-- Name: spots_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('spots_id_seq', 288, true);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE acquired_images ALTER COLUMN id SET DEFAULT nextval('acquired_images_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE analysis_data_sets ALTER COLUMN id SET DEFAULT nextval('analysis_data_sets_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE channel_stacks ALTER COLUMN id SET DEFAULT nextval('channel_stacks_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE channels ALTER COLUMN id SET DEFAULT nextval('channels_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE containers ALTER COLUMN id SET DEFAULT nextval('containers_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE experiments ALTER COLUMN id SET DEFAULT nextval('experiments_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE feature_defs ALTER COLUMN id SET DEFAULT nextval('feature_defs_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE feature_values ALTER COLUMN id SET DEFAULT nextval('feature_values_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE feature_vocabulary_terms ALTER COLUMN id SET DEFAULT nextval('feature_vocabulary_terms_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE image_data_sets ALTER COLUMN id SET DEFAULT nextval('image_data_sets_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE image_transformations ALTER COLUMN id SET DEFAULT nextval('image_transformations_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE image_zoom_level_transformations ALTER COLUMN id SET DEFAULT nextval('image_zoom_level_transformations_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE image_zoom_levels ALTER COLUMN id SET DEFAULT nextval('image_zoom_levels_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE images ALTER COLUMN id SET DEFAULT nextval('images_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE spots ALTER COLUMN id SET DEFAULT nextval('spots_id_seq'::regclass);


--
-- Data for Name: acquired_images; Type: TABLE DATA; Schema: public; Owner: -
--

COPY acquired_images (id, img_id, thumbnail_id, image_transformer_factory, channel_stack_id, channel_id) FROM stdin;
2593	5185	5186	\N	865	4
2594	5187	5188	\N	865	5
2595	5189	5190	\N	865	6
2596	5191	5192	\N	866	4
2597	5193	5194	\N	866	5
2598	5195	5196	\N	866	6
2599	5197	5198	\N	867	4
2600	5199	5200	\N	867	5
2601	5201	5202	\N	867	6
2602	5203	5204	\N	868	4
2603	5205	5206	\N	868	5
2604	5207	5208	\N	868	6
2605	5209	5210	\N	869	4
2606	5211	5212	\N	869	5
2607	5213	5214	\N	869	6
2608	5215	5216	\N	870	4
2609	5217	5218	\N	870	5
2610	5219	5220	\N	870	6
2611	5221	5222	\N	871	4
2612	5223	5224	\N	871	5
2613	5225	5226	\N	871	6
2614	5227	5228	\N	872	4
2615	5229	5230	\N	872	5
2616	5231	5232	\N	872	6
2617	5233	5234	\N	873	4
2618	5235	5236	\N	873	5
2619	5237	5238	\N	873	6
2620	5239	5240	\N	874	4
2621	5241	5242	\N	874	5
2622	5243	5244	\N	874	6
2623	5245	5246	\N	875	4
2624	5247	5248	\N	875	5
2625	5249	5250	\N	875	6
2626	5251	5252	\N	876	4
2627	5253	5254	\N	876	5
2628	5255	5256	\N	876	6
2629	5257	5258	\N	877	4
2630	5259	5260	\N	877	5
2631	5261	5262	\N	877	6
2632	5263	5264	\N	878	4
2633	5265	5266	\N	878	5
2634	5267	5268	\N	878	6
2635	5269	5270	\N	879	4
2636	5271	5272	\N	879	5
2637	5273	5274	\N	879	6
2638	5275	5276	\N	880	4
2639	5277	5278	\N	880	5
2640	5279	5280	\N	880	6
2641	5281	5282	\N	881	4
2642	5283	5284	\N	881	5
2643	5285	5286	\N	881	6
2644	5287	5288	\N	882	4
2645	5289	5290	\N	882	5
2646	5291	5292	\N	882	6
2647	5293	5294	\N	883	4
2648	5295	5296	\N	883	5
2649	5297	5298	\N	883	6
2650	5299	5300	\N	884	4
2651	5301	5302	\N	884	5
2652	5303	5304	\N	884	6
2653	5305	5306	\N	885	4
2654	5307	5308	\N	885	5
2655	5309	5310	\N	885	6
2656	5311	5312	\N	886	4
2657	5313	5314	\N	886	5
2658	5315	5316	\N	886	6
2659	5317	5318	\N	887	4
2660	5319	5320	\N	887	5
2661	5321	5322	\N	887	6
2662	5323	5324	\N	888	4
2663	5325	5326	\N	888	5
2664	5327	5328	\N	888	6
2665	5329	5330	\N	889	4
2666	5331	5332	\N	889	5
2667	5333	5334	\N	889	6
2668	5335	5336	\N	890	4
2669	5337	5338	\N	890	5
2670	5339	5340	\N	890	6
2671	5341	5342	\N	891	4
2672	5343	5344	\N	891	5
2673	5345	5346	\N	891	6
2674	5347	5348	\N	892	4
2675	5349	5350	\N	892	5
2676	5351	5352	\N	892	6
2677	5353	5354	\N	893	4
2678	5355	5356	\N	893	5
2679	5357	5358	\N	893	6
2680	5359	5360	\N	894	4
2681	5361	5362	\N	894	5
2682	5363	5364	\N	894	6
2683	5365	5366	\N	895	4
2684	5367	5368	\N	895	5
2685	5369	5370	\N	895	6
2686	5371	5372	\N	896	4
2687	5373	5374	\N	896	5
2688	5375	5376	\N	896	6
2689	5377	5378	\N	897	4
2690	5379	5380	\N	897	5
2691	5381	5382	\N	897	6
2692	5383	5384	\N	898	4
2693	5385	5386	\N	898	5
2694	5387	5388	\N	898	6
2695	5389	5390	\N	899	4
2696	5391	5392	\N	899	5
2697	5393	5394	\N	899	6
2698	5395	5396	\N	900	4
2699	5397	5398	\N	900	5
2700	5399	5400	\N	900	6
2701	5401	5402	\N	901	4
2702	5403	5404	\N	901	5
2703	5405	5406	\N	901	6
2704	5407	5408	\N	902	4
2705	5409	5410	\N	902	5
2706	5411	5412	\N	902	6
2707	5413	5414	\N	903	4
2708	5415	5416	\N	903	5
2709	5417	5418	\N	903	6
2710	5419	5420	\N	904	4
2711	5421	5422	\N	904	5
2712	5423	5424	\N	904	6
2713	5425	5426	\N	905	4
2714	5427	5428	\N	905	5
2715	5429	5430	\N	905	6
2716	5431	5432	\N	906	4
2717	5433	5434	\N	906	5
2718	5435	5436	\N	906	6
2719	5437	5438	\N	907	4
2720	5439	5440	\N	907	5
2721	5441	5442	\N	907	6
2722	5443	5444	\N	908	4
2723	5445	5446	\N	908	5
2724	5447	5448	\N	908	6
2725	5449	5450	\N	909	4
2726	5451	5452	\N	909	5
2727	5453	5454	\N	909	6
2728	5455	5456	\N	910	4
2729	5457	5458	\N	910	5
2730	5459	5460	\N	910	6
2731	5461	5462	\N	911	4
2732	5463	5464	\N	911	5
2733	5465	5466	\N	911	6
2734	5467	5468	\N	912	4
2735	5469	5470	\N	912	5
2736	5471	5472	\N	912	6
2737	5473	5474	\N	913	4
2738	5475	5476	\N	913	5
2739	5477	5478	\N	913	6
2740	5479	5480	\N	914	4
2741	5481	5482	\N	914	5
2742	5483	5484	\N	914	6
2743	5485	5486	\N	915	4
2744	5487	5488	\N	915	5
2745	5489	5490	\N	915	6
2746	5491	5492	\N	916	4
2747	5493	5494	\N	916	5
2748	5495	5496	\N	916	6
2749	5497	5498	\N	917	4
2750	5499	5500	\N	917	5
2751	5501	5502	\N	917	6
2752	5503	5504	\N	918	4
2753	5505	5506	\N	918	5
2754	5507	5508	\N	918	6
2755	5509	5510	\N	919	4
2756	5511	5512	\N	919	5
2757	5513	5514	\N	919	6
2758	5515	5516	\N	920	4
2759	5517	5518	\N	920	5
2760	5519	5520	\N	920	6
2761	5521	5522	\N	921	4
2762	5523	5524	\N	921	5
2763	5525	5526	\N	921	6
2764	5527	5528	\N	922	4
2765	5529	5530	\N	922	5
2766	5531	5532	\N	922	6
2767	5533	5534	\N	923	4
2768	5535	5536	\N	923	5
2769	5537	5538	\N	923	6
2770	5539	5540	\N	924	4
2771	5541	5542	\N	924	5
2772	5543	5544	\N	924	6
2773	5545	5546	\N	925	4
2774	5547	5548	\N	925	5
2775	5549	5550	\N	925	6
2776	5551	5552	\N	926	4
2777	5553	5554	\N	926	5
2778	5555	5556	\N	926	6
2779	5557	5558	\N	927	4
2780	5559	5560	\N	927	5
2781	5561	5562	\N	927	6
2782	5563	5564	\N	928	4
2783	5565	5566	\N	928	5
2784	5567	5568	\N	928	6
2785	5569	5570	\N	929	4
2786	5571	5572	\N	929	5
2787	5573	5574	\N	929	6
2788	5575	5576	\N	930	4
2789	5577	5578	\N	930	5
2790	5579	5580	\N	930	6
2791	5581	5582	\N	931	4
2792	5583	5584	\N	931	5
2793	5585	5586	\N	931	6
2794	5587	5588	\N	932	4
2795	5589	5590	\N	932	5
2796	5591	5592	\N	932	6
2797	5593	5594	\N	933	4
2798	5595	5596	\N	933	5
2799	5597	5598	\N	933	6
2800	5599	5600	\N	934	4
2801	5601	5602	\N	934	5
2802	5603	5604	\N	934	6
2803	5605	5606	\N	935	4
2804	5607	5608	\N	935	5
2805	5609	5610	\N	935	6
2806	5611	5612	\N	936	4
2807	5613	5614	\N	936	5
2808	5615	5616	\N	936	6
2809	5617	5618	\N	937	4
2810	5619	5620	\N	937	5
2811	5621	5622	\N	937	6
2812	5623	5624	\N	938	4
2813	5625	5626	\N	938	5
2814	5627	5628	\N	938	6
2815	5629	5630	\N	939	4
2816	5631	5632	\N	939	5
2817	5633	5634	\N	939	6
2818	5635	5636	\N	940	4
2819	5637	5638	\N	940	5
2820	5639	5640	\N	940	6
2821	5641	5642	\N	941	4
2822	5643	5644	\N	941	5
2823	5645	5646	\N	941	6
2824	5647	5648	\N	942	4
2825	5649	5650	\N	942	5
2826	5651	5652	\N	942	6
2827	5653	5654	\N	943	4
2828	5655	5656	\N	943	5
2829	5657	5658	\N	943	6
2830	5659	5660	\N	944	4
2831	5661	5662	\N	944	5
2832	5663	5664	\N	944	6
2833	5665	5666	\N	945	4
2834	5667	5668	\N	945	5
2835	5669	5670	\N	945	6
2836	5671	5672	\N	946	4
2837	5673	5674	\N	946	5
2838	5675	5676	\N	946	6
2839	5677	5678	\N	947	4
2840	5679	5680	\N	947	5
2841	5681	5682	\N	947	6
2842	5683	5684	\N	948	4
2843	5685	5686	\N	948	5
2844	5687	5688	\N	948	6
2845	5689	5690	\N	949	4
2846	5691	5692	\N	949	5
2847	5693	5694	\N	949	6
2848	5695	5696	\N	950	4
2849	5697	5698	\N	950	5
2850	5699	5700	\N	950	6
2851	5701	5702	\N	951	4
2852	5703	5704	\N	951	5
2853	5705	5706	\N	951	6
2854	5707	5708	\N	952	4
2855	5709	5710	\N	952	5
2856	5711	5712	\N	952	6
2857	5713	5714	\N	953	4
2858	5715	5716	\N	953	5
2859	5717	5718	\N	953	6
2860	5719	5720	\N	954	4
2861	5721	5722	\N	954	5
2862	5723	5724	\N	954	6
2863	5725	5726	\N	955	4
2864	5727	5728	\N	955	5
2865	5729	5730	\N	955	6
2866	5731	5732	\N	956	4
2867	5733	5734	\N	956	5
2868	5735	5736	\N	956	6
2869	5737	5738	\N	957	4
2870	5739	5740	\N	957	5
2871	5741	5742	\N	957	6
2872	5743	5744	\N	958	4
2873	5745	5746	\N	958	5
2874	5747	5748	\N	958	6
2875	5749	5750	\N	959	4
2876	5751	5752	\N	959	5
2877	5753	5754	\N	959	6
2878	5755	5756	\N	960	4
2879	5757	5758	\N	960	5
2880	5759	5760	\N	960	6
2881	5761	5762	\N	961	4
2882	5763	5764	\N	961	5
2883	5765	5766	\N	961	6
2884	5767	5768	\N	962	4
2885	5769	5770	\N	962	5
2886	5771	5772	\N	962	6
2887	5773	5774	\N	963	4
2888	5775	5776	\N	963	5
2889	5777	5778	\N	963	6
2890	5779	5780	\N	964	4
2891	5781	5782	\N	964	5
2892	5783	5784	\N	964	6
2893	5785	5786	\N	965	4
2894	5787	5788	\N	965	5
2895	5789	5790	\N	965	6
2896	5791	5792	\N	966	4
2897	5793	5794	\N	966	5
2898	5795	5796	\N	966	6
2899	5797	5798	\N	967	4
2900	5799	5800	\N	967	5
2901	5801	5802	\N	967	6
2902	5803	5804	\N	968	4
2903	5805	5806	\N	968	5
2904	5807	5808	\N	968	6
2905	5809	5810	\N	969	4
2906	5811	5812	\N	969	5
2907	5813	5814	\N	969	6
2908	5815	5816	\N	970	4
2909	5817	5818	\N	970	5
2910	5819	5820	\N	970	6
2911	5821	5822	\N	971	4
2912	5823	5824	\N	971	5
2913	5825	5826	\N	971	6
2914	5827	5828	\N	972	4
2915	5829	5830	\N	972	5
2916	5831	5832	\N	972	6
2917	5833	5834	\N	973	4
2918	5835	5836	\N	973	5
2919	5837	5838	\N	973	6
2920	5839	5840	\N	974	4
2921	5841	5842	\N	974	5
2922	5843	5844	\N	974	6
2923	5845	5846	\N	975	4
2924	5847	5848	\N	975	5
2925	5849	5850	\N	975	6
2926	5851	5852	\N	976	4
2927	5853	5854	\N	976	5
2928	5855	5856	\N	976	6
2929	5857	5858	\N	977	4
2930	5859	5860	\N	977	5
2931	5861	5862	\N	977	6
2932	5863	5864	\N	978	4
2933	5865	5866	\N	978	5
2934	5867	5868	\N	978	6
2935	5869	5870	\N	979	4
2936	5871	5872	\N	979	5
2937	5873	5874	\N	979	6
2938	5875	5876	\N	980	4
2939	5877	5878	\N	980	5
2940	5879	5880	\N	980	6
2941	5881	5882	\N	981	4
2942	5883	5884	\N	981	5
2943	5885	5886	\N	981	6
2944	5887	5888	\N	982	4
2945	5889	5890	\N	982	5
2946	5891	5892	\N	982	6
2947	5893	5894	\N	983	4
2948	5895	5896	\N	983	5
2949	5897	5898	\N	983	6
2950	5899	5900	\N	984	4
2951	5901	5902	\N	984	5
2952	5903	5904	\N	984	6
2953	5905	5906	\N	985	4
2954	5907	5908	\N	985	5
2955	5909	5910	\N	985	6
2956	5911	5912	\N	986	4
2957	5913	5914	\N	986	5
2958	5915	5916	\N	986	6
2959	5917	5918	\N	987	4
2960	5919	5920	\N	987	5
2961	5921	5922	\N	987	6
2962	5923	5924	\N	988	4
2963	5925	5926	\N	988	5
2964	5927	5928	\N	988	6
2965	5929	5930	\N	989	4
2966	5931	5932	\N	989	5
2967	5933	5934	\N	989	6
2968	5935	5936	\N	990	4
2969	5937	5938	\N	990	5
2970	5939	5940	\N	990	6
2971	5941	5942	\N	991	4
2972	5943	5944	\N	991	5
2973	5945	5946	\N	991	6
2974	5947	5948	\N	992	4
2975	5949	5950	\N	992	5
2976	5951	5952	\N	992	6
2977	5953	5954	\N	993	4
2978	5955	5956	\N	993	5
2979	5957	5958	\N	993	6
2980	5959	5960	\N	994	4
2981	5961	5962	\N	994	5
2982	5963	5964	\N	994	6
2983	5965	5966	\N	995	4
2984	5967	5968	\N	995	5
2985	5969	5970	\N	995	6
2986	5971	5972	\N	996	4
2987	5973	5974	\N	996	5
2988	5975	5976	\N	996	6
2989	5977	5978	\N	997	4
2990	5979	5980	\N	997	5
2991	5981	5982	\N	997	6
2992	5983	5984	\N	998	4
2993	5985	5986	\N	998	5
2994	5987	5988	\N	998	6
2995	5989	5990	\N	999	4
2996	5991	5992	\N	999	5
2997	5993	5994	\N	999	6
2998	5995	5996	\N	1000	4
2999	5997	5998	\N	1000	5
3000	5999	6000	\N	1000	6
3001	6001	6002	\N	1001	4
3002	6003	6004	\N	1001	5
3003	6005	6006	\N	1001	6
3004	6007	6008	\N	1002	4
3005	6009	6010	\N	1002	5
3006	6011	6012	\N	1002	6
3007	6013	6014	\N	1003	4
3008	6015	6016	\N	1003	5
3009	6017	6018	\N	1003	6
3010	6019	6020	\N	1004	4
3011	6021	6022	\N	1004	5
3012	6023	6024	\N	1004	6
3013	6025	6026	\N	1005	4
3014	6027	6028	\N	1005	5
3015	6029	6030	\N	1005	6
3016	6031	6032	\N	1006	4
3017	6033	6034	\N	1006	5
3018	6035	6036	\N	1006	6
3019	6037	6038	\N	1007	4
3020	6039	6040	\N	1007	5
3021	6041	6042	\N	1007	6
3022	6043	6044	\N	1008	4
3023	6045	6046	\N	1008	5
3024	6047	6048	\N	1008	6
3025	6049	6050	\N	1009	4
3026	6051	6052	\N	1009	5
3027	6053	6054	\N	1009	6
3028	6055	6056	\N	1010	4
3029	6057	6058	\N	1010	5
3030	6059	6060	\N	1010	6
3031	6061	6062	\N	1011	4
3032	6063	6064	\N	1011	5
3033	6065	6066	\N	1011	6
3034	6067	6068	\N	1012	4
3035	6069	6070	\N	1012	5
3036	6071	6072	\N	1012	6
3037	6073	6074	\N	1013	4
3038	6075	6076	\N	1013	5
3039	6077	6078	\N	1013	6
3040	6079	6080	\N	1014	4
3041	6081	6082	\N	1014	5
3042	6083	6084	\N	1014	6
3043	6085	6086	\N	1015	4
3044	6087	6088	\N	1015	5
3045	6089	6090	\N	1015	6
3046	6091	6092	\N	1016	4
3047	6093	6094	\N	1016	5
3048	6095	6096	\N	1016	6
3049	6097	6098	\N	1017	4
3050	6099	6100	\N	1017	5
3051	6101	6102	\N	1017	6
3052	6103	6104	\N	1018	4
3053	6105	6106	\N	1018	5
3054	6107	6108	\N	1018	6
3055	6109	6110	\N	1019	4
3056	6111	6112	\N	1019	5
3057	6113	6114	\N	1019	6
3058	6115	6116	\N	1020	4
3059	6117	6118	\N	1020	5
3060	6119	6120	\N	1020	6
3061	6121	6122	\N	1021	4
3062	6123	6124	\N	1021	5
3063	6125	6126	\N	1021	6
3064	6127	6128	\N	1022	4
3065	6129	6130	\N	1022	5
3066	6131	6132	\N	1022	6
3067	6133	6134	\N	1023	4
3068	6135	6136	\N	1023	5
3069	6137	6138	\N	1023	6
3070	6139	6140	\N	1024	4
3071	6141	6142	\N	1024	5
3072	6143	6144	\N	1024	6
3073	6145	6146	\N	1025	4
3074	6147	6148	\N	1025	5
3075	6149	6150	\N	1025	6
3076	6151	6152	\N	1026	4
3077	6153	6154	\N	1026	5
3078	6155	6156	\N	1026	6
3079	6157	6158	\N	1027	4
3080	6159	6160	\N	1027	5
3081	6161	6162	\N	1027	6
3082	6163	6164	\N	1028	4
3083	6165	6166	\N	1028	5
3084	6167	6168	\N	1028	6
3085	6169	6170	\N	1029	4
3086	6171	6172	\N	1029	5
3087	6173	6174	\N	1029	6
3088	6175	6176	\N	1030	4
3089	6177	6178	\N	1030	5
3090	6179	6180	\N	1030	6
3091	6181	6182	\N	1031	4
3092	6183	6184	\N	1031	5
3093	6185	6186	\N	1031	6
3094	6187	6188	\N	1032	4
3095	6189	6190	\N	1032	5
3096	6191	6192	\N	1032	6
3097	6193	6194	\N	1033	4
3098	6195	6196	\N	1033	5
3099	6197	6198	\N	1033	6
3100	6199	6200	\N	1034	4
3101	6201	6202	\N	1034	5
3102	6203	6204	\N	1034	6
3103	6205	6206	\N	1035	4
3104	6207	6208	\N	1035	5
3105	6209	6210	\N	1035	6
3106	6211	6212	\N	1036	4
3107	6213	6214	\N	1036	5
3108	6215	6216	\N	1036	6
3109	6217	6218	\N	1037	4
3110	6219	6220	\N	1037	5
3111	6221	6222	\N	1037	6
3112	6223	6224	\N	1038	4
3113	6225	6226	\N	1038	5
3114	6227	6228	\N	1038	6
3115	6229	6230	\N	1039	4
3116	6231	6232	\N	1039	5
3117	6233	6234	\N	1039	6
3118	6235	6236	\N	1040	4
3119	6237	6238	\N	1040	5
3120	6239	6240	\N	1040	6
3121	6241	6242	\N	1041	4
3122	6243	6244	\N	1041	5
3123	6245	6246	\N	1041	6
3124	6247	6248	\N	1042	4
3125	6249	6250	\N	1042	5
3126	6251	6252	\N	1042	6
3127	6253	6254	\N	1043	4
3128	6255	6256	\N	1043	5
3129	6257	6258	\N	1043	6
3130	6259	6260	\N	1044	4
3131	6261	6262	\N	1044	5
3132	6263	6264	\N	1044	6
3133	6265	6266	\N	1045	4
3134	6267	6268	\N	1045	5
3135	6269	6270	\N	1045	6
3136	6271	6272	\N	1046	4
3137	6273	6274	\N	1046	5
3138	6275	6276	\N	1046	6
3139	6277	6278	\N	1047	4
3140	6279	6280	\N	1047	5
3141	6281	6282	\N	1047	6
3142	6283	6284	\N	1048	4
3143	6285	6286	\N	1048	5
3144	6287	6288	\N	1048	6
3145	6289	6290	\N	1049	4
3146	6291	6292	\N	1049	5
3147	6293	6294	\N	1049	6
3148	6295	6296	\N	1050	4
3149	6297	6298	\N	1050	5
3150	6299	6300	\N	1050	6
3151	6301	6302	\N	1051	4
3152	6303	6304	\N	1051	5
3153	6305	6306	\N	1051	6
3154	6307	6308	\N	1052	4
3155	6309	6310	\N	1052	5
3156	6311	6312	\N	1052	6
3157	6313	6314	\N	1053	4
3158	6315	6316	\N	1053	5
3159	6317	6318	\N	1053	6
3160	6319	6320	\N	1054	4
3161	6321	6322	\N	1054	5
3162	6323	6324	\N	1054	6
3163	6325	6326	\N	1055	4
3164	6327	6328	\N	1055	5
3165	6329	6330	\N	1055	6
3166	6331	6332	\N	1056	4
3167	6333	6334	\N	1056	5
3168	6335	6336	\N	1056	6
3169	6337	6338	\N	1057	4
3170	6339	6340	\N	1057	5
3171	6341	6342	\N	1057	6
3172	6343	6344	\N	1058	4
3173	6345	6346	\N	1058	5
3174	6347	6348	\N	1058	6
3175	6349	6350	\N	1059	4
3176	6351	6352	\N	1059	5
3177	6353	6354	\N	1059	6
3178	6355	6356	\N	1060	4
3179	6357	6358	\N	1060	5
3180	6359	6360	\N	1060	6
3181	6361	6362	\N	1061	4
3182	6363	6364	\N	1061	5
3183	6365	6366	\N	1061	6
3184	6367	6368	\N	1062	4
3185	6369	6370	\N	1062	5
3186	6371	6372	\N	1062	6
3187	6373	6374	\N	1063	4
3188	6375	6376	\N	1063	5
3189	6377	6378	\N	1063	6
3190	6379	6380	\N	1064	4
3191	6381	6382	\N	1064	5
3192	6383	6384	\N	1064	6
3193	6385	6386	\N	1065	4
3194	6387	6388	\N	1065	5
3195	6389	6390	\N	1065	6
3196	6391	6392	\N	1066	4
3197	6393	6394	\N	1066	5
3198	6395	6396	\N	1066	6
3199	6397	6398	\N	1067	4
3200	6399	6400	\N	1067	5
3201	6401	6402	\N	1067	6
3202	6403	6404	\N	1068	4
3203	6405	6406	\N	1068	5
3204	6407	6408	\N	1068	6
3205	6409	6410	\N	1069	4
3206	6411	6412	\N	1069	5
3207	6413	6414	\N	1069	6
3208	6415	6416	\N	1070	4
3209	6417	6418	\N	1070	5
3210	6419	6420	\N	1070	6
3211	6421	6422	\N	1071	4
3212	6423	6424	\N	1071	5
3213	6425	6426	\N	1071	6
3214	6427	6428	\N	1072	4
3215	6429	6430	\N	1072	5
3216	6431	6432	\N	1072	6
3217	6433	6434	\N	1073	4
3218	6435	6436	\N	1073	5
3219	6437	6438	\N	1073	6
3220	6439	6440	\N	1074	4
3221	6441	6442	\N	1074	5
3222	6443	6444	\N	1074	6
3223	6445	6446	\N	1075	4
3224	6447	6448	\N	1075	5
3225	6449	6450	\N	1075	6
3226	6451	6452	\N	1076	4
3227	6453	6454	\N	1076	5
3228	6455	6456	\N	1076	6
3229	6457	6458	\N	1077	4
3230	6459	6460	\N	1077	5
3231	6461	6462	\N	1077	6
3232	6463	6464	\N	1078	4
3233	6465	6466	\N	1078	5
3234	6467	6468	\N	1078	6
3235	6469	6470	\N	1079	4
3236	6471	6472	\N	1079	5
3237	6473	6474	\N	1079	6
3238	6475	6476	\N	1080	4
3239	6477	6478	\N	1080	5
3240	6479	6480	\N	1080	6
3241	6481	6482	\N	1081	4
3242	6483	6484	\N	1081	5
3243	6485	6486	\N	1081	6
3244	6487	6488	\N	1082	4
3245	6489	6490	\N	1082	5
3246	6491	6492	\N	1082	6
3247	6493	6494	\N	1083	4
3248	6495	6496	\N	1083	5
3249	6497	6498	\N	1083	6
3250	6499	6500	\N	1084	4
3251	6501	6502	\N	1084	5
3252	6503	6504	\N	1084	6
3253	6505	6506	\N	1085	4
3254	6507	6508	\N	1085	5
3255	6509	6510	\N	1085	6
3256	6511	6512	\N	1086	4
3257	6513	6514	\N	1086	5
3258	6515	6516	\N	1086	6
3259	6517	6518	\N	1087	4
3260	6519	6520	\N	1087	5
3261	6521	6522	\N	1087	6
3262	6523	6524	\N	1088	4
3263	6525	6526	\N	1088	5
3264	6527	6528	\N	1088	6
3265	6529	6530	\N	1089	4
3266	6531	6532	\N	1089	5
3267	6533	6534	\N	1089	6
3268	6535	6536	\N	1090	4
3269	6537	6538	\N	1090	5
3270	6539	6540	\N	1090	6
3271	6541	6542	\N	1091	4
3272	6543	6544	\N	1091	5
3273	6545	6546	\N	1091	6
3274	6547	6548	\N	1092	4
3275	6549	6550	\N	1092	5
3276	6551	6552	\N	1092	6
3277	6553	6554	\N	1093	4
3278	6555	6556	\N	1093	5
3279	6557	6558	\N	1093	6
3280	6559	6560	\N	1094	4
3281	6561	6562	\N	1094	5
3282	6563	6564	\N	1094	6
3283	6565	6566	\N	1095	4
3284	6567	6568	\N	1095	5
3285	6569	6570	\N	1095	6
3286	6571	6572	\N	1096	4
3287	6573	6574	\N	1096	5
3288	6575	6576	\N	1096	6
3289	6577	6578	\N	1097	4
3290	6579	6580	\N	1097	5
3291	6581	6582	\N	1097	6
3292	6583	6584	\N	1098	4
3293	6585	6586	\N	1098	5
3294	6587	6588	\N	1098	6
3295	6589	6590	\N	1099	4
3296	6591	6592	\N	1099	5
3297	6593	6594	\N	1099	6
3298	6595	6596	\N	1100	4
3299	6597	6598	\N	1100	5
3300	6599	6600	\N	1100	6
3301	6601	6602	\N	1101	4
3302	6603	6604	\N	1101	5
3303	6605	6606	\N	1101	6
3304	6607	6608	\N	1102	4
3305	6609	6610	\N	1102	5
3306	6611	6612	\N	1102	6
3307	6613	6614	\N	1103	4
3308	6615	6616	\N	1103	5
3309	6617	6618	\N	1103	6
3310	6619	6620	\N	1104	4
3311	6621	6622	\N	1104	5
3312	6623	6624	\N	1104	6
3313	6625	6626	\N	1105	4
3314	6627	6628	\N	1105	5
3315	6629	6630	\N	1105	6
3316	6631	6632	\N	1106	4
3317	6633	6634	\N	1106	5
3318	6635	6636	\N	1106	6
3319	6637	6638	\N	1107	4
3320	6639	6640	\N	1107	5
3321	6641	6642	\N	1107	6
3322	6643	6644	\N	1108	4
3323	6645	6646	\N	1108	5
3324	6647	6648	\N	1108	6
3325	6649	6650	\N	1109	4
3326	6651	6652	\N	1109	5
3327	6653	6654	\N	1109	6
3328	6655	6656	\N	1110	4
3329	6657	6658	\N	1110	5
3330	6659	6660	\N	1110	6
3331	6661	6662	\N	1111	4
3332	6663	6664	\N	1111	5
3333	6665	6666	\N	1111	6
3334	6667	6668	\N	1112	4
3335	6669	6670	\N	1112	5
3336	6671	6672	\N	1112	6
3337	6673	6674	\N	1113	4
3338	6675	6676	\N	1113	5
3339	6677	6678	\N	1113	6
3340	6679	6680	\N	1114	4
3341	6681	6682	\N	1114	5
3342	6683	6684	\N	1114	6
3343	6685	6686	\N	1115	4
3344	6687	6688	\N	1115	5
3345	6689	6690	\N	1115	6
3346	6691	6692	\N	1116	4
3347	6693	6694	\N	1116	5
3348	6695	6696	\N	1116	6
3349	6697	6698	\N	1117	4
3350	6699	6700	\N	1117	5
3351	6701	6702	\N	1117	6
3352	6703	6704	\N	1118	4
3353	6705	6706	\N	1118	5
3354	6707	6708	\N	1118	6
3355	6709	6710	\N	1119	4
3356	6711	6712	\N	1119	5
3357	6713	6714	\N	1119	6
3358	6715	6716	\N	1120	4
3359	6717	6718	\N	1120	5
3360	6719	6720	\N	1120	6
3361	6721	6722	\N	1121	4
3362	6723	6724	\N	1121	5
3363	6725	6726	\N	1121	6
3364	6727	6728	\N	1122	4
3365	6729	6730	\N	1122	5
3366	6731	6732	\N	1122	6
3367	6733	6734	\N	1123	4
3368	6735	6736	\N	1123	5
3369	6737	6738	\N	1123	6
3370	6739	6740	\N	1124	4
3371	6741	6742	\N	1124	5
3372	6743	6744	\N	1124	6
3373	6745	6746	\N	1125	4
3374	6747	6748	\N	1125	5
3375	6749	6750	\N	1125	6
3376	6751	6752	\N	1126	4
3377	6753	6754	\N	1126	5
3378	6755	6756	\N	1126	6
3379	6757	6758	\N	1127	4
3380	6759	6760	\N	1127	5
3381	6761	6762	\N	1127	6
3382	6763	6764	\N	1128	4
3383	6765	6766	\N	1128	5
3384	6767	6768	\N	1128	6
3385	6769	6770	\N	1129	4
3386	6771	6772	\N	1129	5
3387	6773	6774	\N	1129	6
3388	6775	6776	\N	1130	4
3389	6777	6778	\N	1130	5
3390	6779	6780	\N	1130	6
3391	6781	6782	\N	1131	4
3392	6783	6784	\N	1131	5
3393	6785	6786	\N	1131	6
3394	6787	6788	\N	1132	4
3395	6789	6790	\N	1132	5
3396	6791	6792	\N	1132	6
3397	6793	6794	\N	1133	4
3398	6795	6796	\N	1133	5
3399	6797	6798	\N	1133	6
3400	6799	6800	\N	1134	4
3401	6801	6802	\N	1134	5
3402	6803	6804	\N	1134	6
3403	6805	6806	\N	1135	4
3404	6807	6808	\N	1135	5
3405	6809	6810	\N	1135	6
3406	6811	6812	\N	1136	4
3407	6813	6814	\N	1136	5
3408	6815	6816	\N	1136	6
3409	6817	6818	\N	1137	4
3410	6819	6820	\N	1137	5
3411	6821	6822	\N	1137	6
3412	6823	6824	\N	1138	4
3413	6825	6826	\N	1138	5
3414	6827	6828	\N	1138	6
3415	6829	6830	\N	1139	4
3416	6831	6832	\N	1139	5
3417	6833	6834	\N	1139	6
3418	6835	6836	\N	1140	4
3419	6837	6838	\N	1140	5
3420	6839	6840	\N	1140	6
3421	6841	6842	\N	1141	4
3422	6843	6844	\N	1141	5
3423	6845	6846	\N	1141	6
3424	6847	6848	\N	1142	4
3425	6849	6850	\N	1142	5
3426	6851	6852	\N	1142	6
3427	6853	6854	\N	1143	4
3428	6855	6856	\N	1143	5
3429	6857	6858	\N	1143	6
3430	6859	6860	\N	1144	4
3431	6861	6862	\N	1144	5
3432	6863	6864	\N	1144	6
3433	6865	6866	\N	1145	4
3434	6867	6868	\N	1145	5
3435	6869	6870	\N	1145	6
3436	6871	6872	\N	1146	4
3437	6873	6874	\N	1146	5
3438	6875	6876	\N	1146	6
3439	6877	6878	\N	1147	4
3440	6879	6880	\N	1147	5
3441	6881	6882	\N	1147	6
3442	6883	6884	\N	1148	4
3443	6885	6886	\N	1148	5
3444	6887	6888	\N	1148	6
3445	6889	6890	\N	1149	4
3446	6891	6892	\N	1149	5
3447	6893	6894	\N	1149	6
3448	6895	6896	\N	1150	4
3449	6897	6898	\N	1150	5
3450	6899	6900	\N	1150	6
3451	6901	6902	\N	1151	4
3452	6903	6904	\N	1151	5
3453	6905	6906	\N	1151	6
3454	6907	6908	\N	1152	4
3455	6909	6910	\N	1152	5
3456	6911	6912	\N	1152	6
3457	6913	6914	\N	1153	4
3458	6915	6916	\N	1153	5
3459	6917	6918	\N	1153	6
3460	6919	6920	\N	1154	4
3461	6921	6922	\N	1154	5
3462	6923	6924	\N	1154	6
3463	6925	6926	\N	1155	4
3464	6927	6928	\N	1155	5
3465	6929	6930	\N	1155	6
3466	6931	6932	\N	1156	4
3467	6933	6934	\N	1156	5
3468	6935	6936	\N	1156	6
3469	6937	6938	\N	1157	4
3470	6939	6940	\N	1157	5
3471	6941	6942	\N	1157	6
3472	6943	6944	\N	1158	4
3473	6945	6946	\N	1158	5
3474	6947	6948	\N	1158	6
3475	6949	6950	\N	1159	4
3476	6951	6952	\N	1159	5
3477	6953	6954	\N	1159	6
3478	6955	6956	\N	1160	4
3479	6957	6958	\N	1160	5
3480	6959	6960	\N	1160	6
3481	6961	6962	\N	1161	4
3482	6963	6964	\N	1161	5
3483	6965	6966	\N	1161	6
3484	6967	6968	\N	1162	4
3485	6969	6970	\N	1162	5
3486	6971	6972	\N	1162	6
3487	6973	6974	\N	1163	4
3488	6975	6976	\N	1163	5
3489	6977	6978	\N	1163	6
3490	6979	6980	\N	1164	4
3491	6981	6982	\N	1164	5
3492	6983	6984	\N	1164	6
3493	6985	6986	\N	1165	4
3494	6987	6988	\N	1165	5
3495	6989	6990	\N	1165	6
3496	6991	6992	\N	1166	4
3497	6993	6994	\N	1166	5
3498	6995	6996	\N	1166	6
3499	6997	6998	\N	1167	4
3500	6999	7000	\N	1167	5
3501	7001	7002	\N	1167	6
3502	7003	7004	\N	1168	4
3503	7005	7006	\N	1168	5
3504	7007	7008	\N	1168	6
3505	7009	7010	\N	1169	4
3506	7011	7012	\N	1169	5
3507	7013	7014	\N	1169	6
3508	7015	7016	\N	1170	4
3509	7017	7018	\N	1170	5
3510	7019	7020	\N	1170	6
3511	7021	7022	\N	1171	4
3512	7023	7024	\N	1171	5
3513	7025	7026	\N	1171	6
3514	7027	7028	\N	1172	4
3515	7029	7030	\N	1172	5
3516	7031	7032	\N	1172	6
3517	7033	7034	\N	1173	4
3518	7035	7036	\N	1173	5
3519	7037	7038	\N	1173	6
3520	7039	7040	\N	1174	4
3521	7041	7042	\N	1174	5
3522	7043	7044	\N	1174	6
3523	7045	7046	\N	1175	4
3524	7047	7048	\N	1175	5
3525	7049	7050	\N	1175	6
3526	7051	7052	\N	1176	4
3527	7053	7054	\N	1176	5
3528	7055	7056	\N	1176	6
3529	7057	7058	\N	1177	4
3530	7059	7060	\N	1177	5
3531	7061	7062	\N	1177	6
3532	7063	7064	\N	1178	4
3533	7065	7066	\N	1178	5
3534	7067	7068	\N	1178	6
3535	7069	7070	\N	1179	4
3536	7071	7072	\N	1179	5
3537	7073	7074	\N	1179	6
3538	7075	7076	\N	1180	4
3539	7077	7078	\N	1180	5
3540	7079	7080	\N	1180	6
3541	7081	7082	\N	1181	4
3542	7083	7084	\N	1181	5
3543	7085	7086	\N	1181	6
3544	7087	7088	\N	1182	4
3545	7089	7090	\N	1182	5
3546	7091	7092	\N	1182	6
3547	7093	7094	\N	1183	4
3548	7095	7096	\N	1183	5
3549	7097	7098	\N	1183	6
3550	7099	7100	\N	1184	4
3551	7101	7102	\N	1184	5
3552	7103	7104	\N	1184	6
3553	7105	7106	\N	1185	4
3554	7107	7108	\N	1185	5
3555	7109	7110	\N	1185	6
3556	7111	7112	\N	1186	4
3557	7113	7114	\N	1186	5
3558	7115	7116	\N	1186	6
3559	7117	7118	\N	1187	4
3560	7119	7120	\N	1187	5
3561	7121	7122	\N	1187	6
3562	7123	7124	\N	1188	4
3563	7125	7126	\N	1188	5
3564	7127	7128	\N	1188	6
3565	7129	7130	\N	1189	4
3566	7131	7132	\N	1189	5
3567	7133	7134	\N	1189	6
3568	7135	7136	\N	1190	4
3569	7137	7138	\N	1190	5
3570	7139	7140	\N	1190	6
3571	7141	7142	\N	1191	4
3572	7143	7144	\N	1191	5
3573	7145	7146	\N	1191	6
3574	7147	7148	\N	1192	4
3575	7149	7150	\N	1192	5
3576	7151	7152	\N	1192	6
3577	7153	7154	\N	1193	4
3578	7155	7156	\N	1193	5
3579	7157	7158	\N	1193	6
3580	7159	7160	\N	1194	4
3581	7161	7162	\N	1194	5
3582	7163	7164	\N	1194	6
3583	7165	7166	\N	1195	4
3584	7167	7168	\N	1195	5
3585	7169	7170	\N	1195	6
3586	7171	7172	\N	1196	4
3587	7173	7174	\N	1196	5
3588	7175	7176	\N	1196	6
3589	7177	7178	\N	1197	4
3590	7179	7180	\N	1197	5
3591	7181	7182	\N	1197	6
3592	7183	7184	\N	1198	4
3593	7185	7186	\N	1198	5
3594	7187	7188	\N	1198	6
3595	7189	7190	\N	1199	4
3596	7191	7192	\N	1199	5
3597	7193	7194	\N	1199	6
3598	7195	7196	\N	1200	4
3599	7197	7198	\N	1200	5
3600	7199	7200	\N	1200	6
3601	7201	7202	\N	1201	4
3602	7203	7204	\N	1201	5
3603	7205	7206	\N	1201	6
3604	7207	7208	\N	1202	4
3605	7209	7210	\N	1202	5
3606	7211	7212	\N	1202	6
3607	7213	7214	\N	1203	4
3608	7215	7216	\N	1203	5
3609	7217	7218	\N	1203	6
3610	7219	7220	\N	1204	4
3611	7221	7222	\N	1204	5
3612	7223	7224	\N	1204	6
3613	7225	7226	\N	1205	4
3614	7227	7228	\N	1205	5
3615	7229	7230	\N	1205	6
3616	7231	7232	\N	1206	4
3617	7233	7234	\N	1206	5
3618	7235	7236	\N	1206	6
3619	7237	7238	\N	1207	4
3620	7239	7240	\N	1207	5
3621	7241	7242	\N	1207	6
3622	7243	7244	\N	1208	4
3623	7245	7246	\N	1208	5
3624	7247	7248	\N	1208	6
3625	7249	7250	\N	1209	4
3626	7251	7252	\N	1209	5
3627	7253	7254	\N	1209	6
3628	7255	7256	\N	1210	4
3629	7257	7258	\N	1210	5
3630	7259	7260	\N	1210	6
3631	7261	7262	\N	1211	4
3632	7263	7264	\N	1211	5
3633	7265	7266	\N	1211	6
3634	7267	7268	\N	1212	4
3635	7269	7270	\N	1212	5
3636	7271	7272	\N	1212	6
3637	7273	7274	\N	1213	4
3638	7275	7276	\N	1213	5
3639	7277	7278	\N	1213	6
3640	7279	7280	\N	1214	4
3641	7281	7282	\N	1214	5
3642	7283	7284	\N	1214	6
3643	7285	7286	\N	1215	4
3644	7287	7288	\N	1215	5
3645	7289	7290	\N	1215	6
3646	7291	7292	\N	1216	4
3647	7293	7294	\N	1216	5
3648	7295	7296	\N	1216	6
3649	7297	7298	\N	1217	4
3650	7299	7300	\N	1217	5
3651	7301	7302	\N	1217	6
3652	7303	7304	\N	1218	4
3653	7305	7306	\N	1218	5
3654	7307	7308	\N	1218	6
3655	7309	7310	\N	1219	4
3656	7311	7312	\N	1219	5
3657	7313	7314	\N	1219	6
3658	7315	7316	\N	1220	4
3659	7317	7318	\N	1220	5
3660	7319	7320	\N	1220	6
3661	7321	7322	\N	1221	4
3662	7323	7324	\N	1221	5
3663	7325	7326	\N	1221	6
3664	7327	7328	\N	1222	4
3665	7329	7330	\N	1222	5
3666	7331	7332	\N	1222	6
3667	7333	7334	\N	1223	4
3668	7335	7336	\N	1223	5
3669	7337	7338	\N	1223	6
3670	7339	7340	\N	1224	4
3671	7341	7342	\N	1224	5
3672	7343	7344	\N	1224	6
3673	7345	7346	\N	1225	4
3674	7347	7348	\N	1225	5
3675	7349	7350	\N	1225	6
3676	7351	7352	\N	1226	4
3677	7353	7354	\N	1226	5
3678	7355	7356	\N	1226	6
3679	7357	7358	\N	1227	4
3680	7359	7360	\N	1227	5
3681	7361	7362	\N	1227	6
3682	7363	7364	\N	1228	4
3683	7365	7366	\N	1228	5
3684	7367	7368	\N	1228	6
3685	7369	7370	\N	1229	4
3686	7371	7372	\N	1229	5
3687	7373	7374	\N	1229	6
3688	7375	7376	\N	1230	4
3689	7377	7378	\N	1230	5
3690	7379	7380	\N	1230	6
3691	7381	7382	\N	1231	4
3692	7383	7384	\N	1231	5
3693	7385	7386	\N	1231	6
3694	7387	7388	\N	1232	4
3695	7389	7390	\N	1232	5
3696	7391	7392	\N	1232	6
3697	7393	7394	\N	1233	4
3698	7395	7396	\N	1233	5
3699	7397	7398	\N	1233	6
3700	7399	7400	\N	1234	4
3701	7401	7402	\N	1234	5
3702	7403	7404	\N	1234	6
3703	7405	7406	\N	1235	4
3704	7407	7408	\N	1235	5
3705	7409	7410	\N	1235	6
3706	7411	7412	\N	1236	4
3707	7413	7414	\N	1236	5
3708	7415	7416	\N	1236	6
3709	7417	7418	\N	1237	4
3710	7419	7420	\N	1237	5
3711	7421	7422	\N	1237	6
3712	7423	7424	\N	1238	4
3713	7425	7426	\N	1238	5
3714	7427	7428	\N	1238	6
3715	7429	7430	\N	1239	4
3716	7431	7432	\N	1239	5
3717	7433	7434	\N	1239	6
3718	7435	7436	\N	1240	4
3719	7437	7438	\N	1240	5
3720	7439	7440	\N	1240	6
3721	7441	7442	\N	1241	4
3722	7443	7444	\N	1241	5
3723	7445	7446	\N	1241	6
3724	7447	7448	\N	1242	4
3725	7449	7450	\N	1242	5
3726	7451	7452	\N	1242	6
3727	7453	7454	\N	1243	4
3728	7455	7456	\N	1243	5
3729	7457	7458	\N	1243	6
3730	7459	7460	\N	1244	4
3731	7461	7462	\N	1244	5
3732	7463	7464	\N	1244	6
3733	7465	7466	\N	1245	4
3734	7467	7468	\N	1245	5
3735	7469	7470	\N	1245	6
3736	7471	7472	\N	1246	4
3737	7473	7474	\N	1246	5
3738	7475	7476	\N	1246	6
3739	7477	7478	\N	1247	4
3740	7479	7480	\N	1247	5
3741	7481	7482	\N	1247	6
3742	7483	7484	\N	1248	4
3743	7485	7486	\N	1248	5
3744	7487	7488	\N	1248	6
3745	7489	7490	\N	1249	4
3746	7491	7492	\N	1249	5
3747	7493	7494	\N	1249	6
3748	7495	7496	\N	1250	4
3749	7497	7498	\N	1250	5
3750	7499	7500	\N	1250	6
3751	7501	7502	\N	1251	4
3752	7503	7504	\N	1251	5
3753	7505	7506	\N	1251	6
3754	7507	7508	\N	1252	4
3755	7509	7510	\N	1252	5
3756	7511	7512	\N	1252	6
3757	7513	7514	\N	1253	4
3758	7515	7516	\N	1253	5
3759	7517	7518	\N	1253	6
3760	7519	7520	\N	1254	4
3761	7521	7522	\N	1254	5
3762	7523	7524	\N	1254	6
3763	7525	7526	\N	1255	4
3764	7527	7528	\N	1255	5
3765	7529	7530	\N	1255	6
3766	7531	7532	\N	1256	4
3767	7533	7534	\N	1256	5
3768	7535	7536	\N	1256	6
3769	7537	7538	\N	1257	4
3770	7539	7540	\N	1257	5
3771	7541	7542	\N	1257	6
3772	7543	7544	\N	1258	4
3773	7545	7546	\N	1258	5
3774	7547	7548	\N	1258	6
3775	7549	7550	\N	1259	4
3776	7551	7552	\N	1259	5
3777	7553	7554	\N	1259	6
3778	7555	7556	\N	1260	4
3779	7557	7558	\N	1260	5
3780	7559	7560	\N	1260	6
3781	7561	7562	\N	1261	4
3782	7563	7564	\N	1261	5
3783	7565	7566	\N	1261	6
3784	7567	7568	\N	1262	4
3785	7569	7570	\N	1262	5
3786	7571	7572	\N	1262	6
3787	7573	7574	\N	1263	4
3788	7575	7576	\N	1263	5
3789	7577	7578	\N	1263	6
3790	7579	7580	\N	1264	4
3791	7581	7582	\N	1264	5
3792	7583	7584	\N	1264	6
3793	7585	7586	\N	1265	4
3794	7587	7588	\N	1265	5
3795	7589	7590	\N	1265	6
3796	7591	7592	\N	1266	4
3797	7593	7594	\N	1266	5
3798	7595	7596	\N	1266	6
3799	7597	7598	\N	1267	4
3800	7599	7600	\N	1267	5
3801	7601	7602	\N	1267	6
3802	7603	7604	\N	1268	4
3803	7605	7606	\N	1268	5
3804	7607	7608	\N	1268	6
3805	7609	7610	\N	1269	4
3806	7611	7612	\N	1269	5
3807	7613	7614	\N	1269	6
3808	7615	7616	\N	1270	4
3809	7617	7618	\N	1270	5
3810	7619	7620	\N	1270	6
3811	7621	7622	\N	1271	4
3812	7623	7624	\N	1271	5
3813	7625	7626	\N	1271	6
3814	7627	7628	\N	1272	4
3815	7629	7630	\N	1272	5
3816	7631	7632	\N	1272	6
3817	7633	7634	\N	1273	4
3818	7635	7636	\N	1273	5
3819	7637	7638	\N	1273	6
3820	7639	7640	\N	1274	4
3821	7641	7642	\N	1274	5
3822	7643	7644	\N	1274	6
3823	7645	7646	\N	1275	4
3824	7647	7648	\N	1275	5
3825	7649	7650	\N	1275	6
3826	7651	7652	\N	1276	4
3827	7653	7654	\N	1276	5
3828	7655	7656	\N	1276	6
3829	7657	7658	\N	1277	4
3830	7659	7660	\N	1277	5
3831	7661	7662	\N	1277	6
3832	7663	7664	\N	1278	4
3833	7665	7666	\N	1278	5
3834	7667	7668	\N	1278	6
3835	7669	7670	\N	1279	4
3836	7671	7672	\N	1279	5
3837	7673	7674	\N	1279	6
3838	7675	7676	\N	1280	4
3839	7677	7678	\N	1280	5
3840	7679	7680	\N	1280	6
3841	7681	7682	\N	1281	4
3842	7683	7684	\N	1281	5
3843	7685	7686	\N	1281	6
3844	7687	7688	\N	1282	4
3845	7689	7690	\N	1282	5
3846	7691	7692	\N	1282	6
3847	7693	7694	\N	1283	4
3848	7695	7696	\N	1283	5
3849	7697	7698	\N	1283	6
3850	7699	7700	\N	1284	4
3851	7701	7702	\N	1284	5
3852	7703	7704	\N	1284	6
3853	7705	7706	\N	1285	4
3854	7707	7708	\N	1285	5
3855	7709	7710	\N	1285	6
3856	7711	7712	\N	1286	4
3857	7713	7714	\N	1286	5
3858	7715	7716	\N	1286	6
3859	7717	7718	\N	1287	4
3860	7719	7720	\N	1287	5
3861	7721	7722	\N	1287	6
3862	7723	7724	\N	1288	4
3863	7725	7726	\N	1288	5
3864	7727	7728	\N	1288	6
3865	7729	7730	\N	1289	4
3866	7731	7732	\N	1289	5
3867	7733	7734	\N	1289	6
3868	7735	7736	\N	1290	4
3869	7737	7738	\N	1290	5
3870	7739	7740	\N	1290	6
3871	7741	7742	\N	1291	4
3872	7743	7744	\N	1291	5
3873	7745	7746	\N	1291	6
3874	7747	7748	\N	1292	4
3875	7749	7750	\N	1292	5
3876	7751	7752	\N	1292	6
3877	7753	7754	\N	1293	4
3878	7755	7756	\N	1293	5
3879	7757	7758	\N	1293	6
3880	7759	7760	\N	1294	4
3881	7761	7762	\N	1294	5
3882	7763	7764	\N	1294	6
3883	7765	7766	\N	1295	4
3884	7767	7768	\N	1295	5
3885	7769	7770	\N	1295	6
3886	7771	7772	\N	1296	4
3887	7773	7774	\N	1296	5
3888	7775	7776	\N	1296	6
3889	7777	7778	\N	1297	4
3890	7779	7780	\N	1297	5
3891	7781	7782	\N	1297	6
3892	7783	7784	\N	1298	4
3893	7785	7786	\N	1298	5
3894	7787	7788	\N	1298	6
3895	7789	7790	\N	1299	4
3896	7791	7792	\N	1299	5
3897	7793	7794	\N	1299	6
3898	7795	7796	\N	1300	4
3899	7797	7798	\N	1300	5
3900	7799	7800	\N	1300	6
3901	7801	7802	\N	1301	4
3902	7803	7804	\N	1301	5
3903	7805	7806	\N	1301	6
3904	7807	7808	\N	1302	4
3905	7809	7810	\N	1302	5
3906	7811	7812	\N	1302	6
3907	7813	7814	\N	1303	4
3908	7815	7816	\N	1303	5
3909	7817	7818	\N	1303	6
3910	7819	7820	\N	1304	4
3911	7821	7822	\N	1304	5
3912	7823	7824	\N	1304	6
3913	7825	7826	\N	1305	4
3914	7827	7828	\N	1305	5
3915	7829	7830	\N	1305	6
3916	7831	7832	\N	1306	4
3917	7833	7834	\N	1306	5
3918	7835	7836	\N	1306	6
3919	7837	7838	\N	1307	4
3920	7839	7840	\N	1307	5
3921	7841	7842	\N	1307	6
3922	7843	7844	\N	1308	4
3923	7845	7846	\N	1308	5
3924	7847	7848	\N	1308	6
3925	7849	7850	\N	1309	4
3926	7851	7852	\N	1309	5
3927	7853	7854	\N	1309	6
3928	7855	7856	\N	1310	4
3929	7857	7858	\N	1310	5
3930	7859	7860	\N	1310	6
3931	7861	7862	\N	1311	4
3932	7863	7864	\N	1311	5
3933	7865	7866	\N	1311	6
3934	7867	7868	\N	1312	4
3935	7869	7870	\N	1312	5
3936	7871	7872	\N	1312	6
3937	7873	7874	\N	1313	4
3938	7875	7876	\N	1313	5
3939	7877	7878	\N	1313	6
3940	7879	7880	\N	1314	4
3941	7881	7882	\N	1314	5
3942	7883	7884	\N	1314	6
3943	7885	7886	\N	1315	4
3944	7887	7888	\N	1315	5
3945	7889	7890	\N	1315	6
3946	7891	7892	\N	1316	4
3947	7893	7894	\N	1316	5
3948	7895	7896	\N	1316	6
3949	7897	7898	\N	1317	4
3950	7899	7900	\N	1317	5
3951	7901	7902	\N	1317	6
3952	7903	7904	\N	1318	4
3953	7905	7906	\N	1318	5
3954	7907	7908	\N	1318	6
3955	7909	7910	\N	1319	4
3956	7911	7912	\N	1319	5
3957	7913	7914	\N	1319	6
3958	7915	7916	\N	1320	4
3959	7917	7918	\N	1320	5
3960	7919	7920	\N	1320	6
3961	7921	7922	\N	1321	4
3962	7923	7924	\N	1321	5
3963	7925	7926	\N	1321	6
3964	7927	7928	\N	1322	4
3965	7929	7930	\N	1322	5
3966	7931	7932	\N	1322	6
3967	7933	7934	\N	1323	4
3968	7935	7936	\N	1323	5
3969	7937	7938	\N	1323	6
3970	7939	7940	\N	1324	4
3971	7941	7942	\N	1324	5
3972	7943	7944	\N	1324	6
3973	7945	7946	\N	1325	4
3974	7947	7948	\N	1325	5
3975	7949	7950	\N	1325	6
3976	7951	7952	\N	1326	4
3977	7953	7954	\N	1326	5
3978	7955	7956	\N	1326	6
3979	7957	7958	\N	1327	4
3980	7959	7960	\N	1327	5
3981	7961	7962	\N	1327	6
3982	7963	7964	\N	1328	4
3983	7965	7966	\N	1328	5
3984	7967	7968	\N	1328	6
3985	7969	7970	\N	1329	4
3986	7971	7972	\N	1329	5
3987	7973	7974	\N	1329	6
3988	7975	7976	\N	1330	4
3989	7977	7978	\N	1330	5
3990	7979	7980	\N	1330	6
3991	7981	7982	\N	1331	4
3992	7983	7984	\N	1331	5
3993	7985	7986	\N	1331	6
3994	7987	7988	\N	1332	4
3995	7989	7990	\N	1332	5
3996	7991	7992	\N	1332	6
3997	7993	7994	\N	1333	4
3998	7995	7996	\N	1333	5
3999	7997	7998	\N	1333	6
4000	7999	8000	\N	1334	4
4001	8001	8002	\N	1334	5
4002	8003	8004	\N	1334	6
4003	8005	8006	\N	1335	4
4004	8007	8008	\N	1335	5
4005	8009	8010	\N	1335	6
4006	8011	8012	\N	1336	4
4007	8013	8014	\N	1336	5
4008	8015	8016	\N	1336	6
4009	8017	8018	\N	1337	4
4010	8019	8020	\N	1337	5
4011	8021	8022	\N	1337	6
4012	8023	8024	\N	1338	4
4013	8025	8026	\N	1338	5
4014	8027	8028	\N	1338	6
4015	8029	8030	\N	1339	4
4016	8031	8032	\N	1339	5
4017	8033	8034	\N	1339	6
4018	8035	8036	\N	1340	4
4019	8037	8038	\N	1340	5
4020	8039	8040	\N	1340	6
4021	8041	8042	\N	1341	4
4022	8043	8044	\N	1341	5
4023	8045	8046	\N	1341	6
4024	8047	8048	\N	1342	4
4025	8049	8050	\N	1342	5
4026	8051	8052	\N	1342	6
4027	8053	8054	\N	1343	4
4028	8055	8056	\N	1343	5
4029	8057	8058	\N	1343	6
4030	8059	8060	\N	1344	4
4031	8061	8062	\N	1344	5
4032	8063	8064	\N	1344	6
4033	8065	8066	\N	1345	4
4034	8067	8068	\N	1345	5
4035	8069	8070	\N	1345	6
4036	8071	8072	\N	1346	4
4037	8073	8074	\N	1346	5
4038	8075	8076	\N	1346	6
4039	8077	8078	\N	1347	4
4040	8079	8080	\N	1347	5
4041	8081	8082	\N	1347	6
4042	8083	8084	\N	1348	4
4043	8085	8086	\N	1348	5
4044	8087	8088	\N	1348	6
4045	8089	8090	\N	1349	4
4046	8091	8092	\N	1349	5
4047	8093	8094	\N	1349	6
4048	8095	8096	\N	1350	4
4049	8097	8098	\N	1350	5
4050	8099	8100	\N	1350	6
4051	8101	8102	\N	1351	4
4052	8103	8104	\N	1351	5
4053	8105	8106	\N	1351	6
4054	8107	8108	\N	1352	4
4055	8109	8110	\N	1352	5
4056	8111	8112	\N	1352	6
4057	8113	8114	\N	1353	4
4058	8115	8116	\N	1353	5
4059	8117	8118	\N	1353	6
4060	8119	8120	\N	1354	4
4061	8121	8122	\N	1354	5
4062	8123	8124	\N	1354	6
4063	8125	8126	\N	1355	4
4064	8127	8128	\N	1355	5
4065	8129	8130	\N	1355	6
4066	8131	8132	\N	1356	4
4067	8133	8134	\N	1356	5
4068	8135	8136	\N	1356	6
4069	8137	8138	\N	1357	4
4070	8139	8140	\N	1357	5
4071	8141	8142	\N	1357	6
4072	8143	8144	\N	1358	4
4073	8145	8146	\N	1358	5
4074	8147	8148	\N	1358	6
4075	8149	8150	\N	1359	4
4076	8151	8152	\N	1359	5
4077	8153	8154	\N	1359	6
4078	8155	8156	\N	1360	4
4079	8157	8158	\N	1360	5
4080	8159	8160	\N	1360	6
4081	8161	8162	\N	1361	4
4082	8163	8164	\N	1361	5
4083	8165	8166	\N	1361	6
4084	8167	8168	\N	1362	4
4085	8169	8170	\N	1362	5
4086	8171	8172	\N	1362	6
4087	8173	8174	\N	1363	4
4088	8175	8176	\N	1363	5
4089	8177	8178	\N	1363	6
4090	8179	8180	\N	1364	4
4091	8181	8182	\N	1364	5
4092	8183	8184	\N	1364	6
4093	8185	8186	\N	1365	4
4094	8187	8188	\N	1365	5
4095	8189	8190	\N	1365	6
4096	8191	8192	\N	1366	4
4097	8193	8194	\N	1366	5
4098	8195	8196	\N	1366	6
4099	8197	8198	\N	1367	4
4100	8199	8200	\N	1367	5
4101	8201	8202	\N	1367	6
4102	8203	8204	\N	1368	4
4103	8205	8206	\N	1368	5
4104	8207	8208	\N	1368	6
4105	8209	8210	\N	1369	4
4106	8211	8212	\N	1369	5
4107	8213	8214	\N	1369	6
4108	8215	8216	\N	1370	4
4109	8217	8218	\N	1370	5
4110	8219	8220	\N	1370	6
4111	8221	8222	\N	1371	4
4112	8223	8224	\N	1371	5
4113	8225	8226	\N	1371	6
4114	8227	8228	\N	1372	4
4115	8229	8230	\N	1372	5
4116	8231	8232	\N	1372	6
4117	8233	8234	\N	1373	4
4118	8235	8236	\N	1373	5
4119	8237	8238	\N	1373	6
4120	8239	8240	\N	1374	4
4121	8241	8242	\N	1374	5
4122	8243	8244	\N	1374	6
4123	8245	8246	\N	1375	4
4124	8247	8248	\N	1375	5
4125	8249	8250	\N	1375	6
4126	8251	8252	\N	1376	4
4127	8253	8254	\N	1376	5
4128	8255	8256	\N	1376	6
4129	8257	8258	\N	1377	4
4130	8259	8260	\N	1377	5
4131	8261	8262	\N	1377	6
4132	8263	8264	\N	1378	4
4133	8265	8266	\N	1378	5
4134	8267	8268	\N	1378	6
4135	8269	8270	\N	1379	4
4136	8271	8272	\N	1379	5
4137	8273	8274	\N	1379	6
4138	8275	8276	\N	1380	4
4139	8277	8278	\N	1380	5
4140	8279	8280	\N	1380	6
4141	8281	8282	\N	1381	4
4142	8283	8284	\N	1381	5
4143	8285	8286	\N	1381	6
4144	8287	8288	\N	1382	4
4145	8289	8290	\N	1382	5
4146	8291	8292	\N	1382	6
4147	8293	8294	\N	1383	4
4148	8295	8296	\N	1383	5
4149	8297	8298	\N	1383	6
4150	8299	8300	\N	1384	4
4151	8301	8302	\N	1384	5
4152	8303	8304	\N	1384	6
4153	8305	8306	\N	1385	4
4154	8307	8308	\N	1385	5
4155	8309	8310	\N	1385	6
4156	8311	8312	\N	1386	4
4157	8313	8314	\N	1386	5
4158	8315	8316	\N	1386	6
4159	8317	8318	\N	1387	4
4160	8319	8320	\N	1387	5
4161	8321	8322	\N	1387	6
4162	8323	8324	\N	1388	4
4163	8325	8326	\N	1388	5
4164	8327	8328	\N	1388	6
4165	8329	8330	\N	1389	4
4166	8331	8332	\N	1389	5
4167	8333	8334	\N	1389	6
4168	8335	8336	\N	1390	4
4169	8337	8338	\N	1390	5
4170	8339	8340	\N	1390	6
4171	8341	8342	\N	1391	4
4172	8343	8344	\N	1391	5
4173	8345	8346	\N	1391	6
4174	8347	8348	\N	1392	4
4175	8349	8350	\N	1392	5
4176	8351	8352	\N	1392	6
4177	8353	8354	\N	1393	4
4178	8355	8356	\N	1393	5
4179	8357	8358	\N	1393	6
4180	8359	8360	\N	1394	4
4181	8361	8362	\N	1394	5
4182	8363	8364	\N	1394	6
4183	8365	8366	\N	1395	4
4184	8367	8368	\N	1395	5
4185	8369	8370	\N	1395	6
4186	8371	8372	\N	1396	4
4187	8373	8374	\N	1396	5
4188	8375	8376	\N	1396	6
4189	8377	8378	\N	1397	4
4190	8379	8380	\N	1397	5
4191	8381	8382	\N	1397	6
4192	8383	8384	\N	1398	4
4193	8385	8386	\N	1398	5
4194	8387	8388	\N	1398	6
4195	8389	8390	\N	1399	4
4196	8391	8392	\N	1399	5
4197	8393	8394	\N	1399	6
4198	8395	8396	\N	1400	4
4199	8397	8398	\N	1400	5
4200	8399	8400	\N	1400	6
4201	8401	8402	\N	1401	4
4202	8403	8404	\N	1401	5
4203	8405	8406	\N	1401	6
4204	8407	8408	\N	1402	4
4205	8409	8410	\N	1402	5
4206	8411	8412	\N	1402	6
4207	8413	8414	\N	1403	4
4208	8415	8416	\N	1403	5
4209	8417	8418	\N	1403	6
4210	8419	8420	\N	1404	4
4211	8421	8422	\N	1404	5
4212	8423	8424	\N	1404	6
4213	8425	8426	\N	1405	4
4214	8427	8428	\N	1405	5
4215	8429	8430	\N	1405	6
4216	8431	8432	\N	1406	4
4217	8433	8434	\N	1406	5
4218	8435	8436	\N	1406	6
4219	8437	8438	\N	1407	4
4220	8439	8440	\N	1407	5
4221	8441	8442	\N	1407	6
4222	8443	8444	\N	1408	4
4223	8445	8446	\N	1408	5
4224	8447	8448	\N	1408	6
4225	8449	8450	\N	1409	4
4226	8451	8452	\N	1409	5
4227	8453	8454	\N	1409	6
4228	8455	8456	\N	1410	4
4229	8457	8458	\N	1410	5
4230	8459	8460	\N	1410	6
4231	8461	8462	\N	1411	4
4232	8463	8464	\N	1411	5
4233	8465	8466	\N	1411	6
4234	8467	8468	\N	1412	4
4235	8469	8470	\N	1412	5
4236	8471	8472	\N	1412	6
4237	8473	8474	\N	1413	4
4238	8475	8476	\N	1413	5
4239	8477	8478	\N	1413	6
4240	8479	8480	\N	1414	4
4241	8481	8482	\N	1414	5
4242	8483	8484	\N	1414	6
4243	8485	8486	\N	1415	4
4244	8487	8488	\N	1415	5
4245	8489	8490	\N	1415	6
4246	8491	8492	\N	1416	4
4247	8493	8494	\N	1416	5
4248	8495	8496	\N	1416	6
4249	8497	8498	\N	1417	4
4250	8499	8500	\N	1417	5
4251	8501	8502	\N	1417	6
4252	8503	8504	\N	1418	4
4253	8505	8506	\N	1418	5
4254	8507	8508	\N	1418	6
4255	8509	8510	\N	1419	4
4256	8511	8512	\N	1419	5
4257	8513	8514	\N	1419	6
4258	8515	8516	\N	1420	4
4259	8517	8518	\N	1420	5
4260	8519	8520	\N	1420	6
4261	8521	8522	\N	1421	4
4262	8523	8524	\N	1421	5
4263	8525	8526	\N	1421	6
4264	8527	8528	\N	1422	4
4265	8529	8530	\N	1422	5
4266	8531	8532	\N	1422	6
4267	8533	8534	\N	1423	4
4268	8535	8536	\N	1423	5
4269	8537	8538	\N	1423	6
4270	8539	8540	\N	1424	4
4271	8541	8542	\N	1424	5
4272	8543	8544	\N	1424	6
4273	8545	8546	\N	1425	4
4274	8547	8548	\N	1425	5
4275	8549	8550	\N	1425	6
4276	8551	8552	\N	1426	4
4277	8553	8554	\N	1426	5
4278	8555	8556	\N	1426	6
4279	8557	8558	\N	1427	4
4280	8559	8560	\N	1427	5
4281	8561	8562	\N	1427	6
4282	8563	8564	\N	1428	4
4283	8565	8566	\N	1428	5
4284	8567	8568	\N	1428	6
4285	8569	8570	\N	1429	4
4286	8571	8572	\N	1429	5
4287	8573	8574	\N	1429	6
4288	8575	8576	\N	1430	4
4289	8577	8578	\N	1430	5
4290	8579	8580	\N	1430	6
4291	8581	8582	\N	1431	4
4292	8583	8584	\N	1431	5
4293	8585	8586	\N	1431	6
4294	8587	8588	\N	1432	4
4295	8589	8590	\N	1432	5
4296	8591	8592	\N	1432	6
4297	8593	8594	\N	1433	4
4298	8595	8596	\N	1433	5
4299	8597	8598	\N	1433	6
4300	8599	8600	\N	1434	4
4301	8601	8602	\N	1434	5
4302	8603	8604	\N	1434	6
4303	8605	8606	\N	1435	4
4304	8607	8608	\N	1435	5
4305	8609	8610	\N	1435	6
4306	8611	8612	\N	1436	4
4307	8613	8614	\N	1436	5
4308	8615	8616	\N	1436	6
4309	8617	8618	\N	1437	4
4310	8619	8620	\N	1437	5
4311	8621	8622	\N	1437	6
4312	8623	8624	\N	1438	4
4313	8625	8626	\N	1438	5
4314	8627	8628	\N	1438	6
4315	8629	8630	\N	1439	4
4316	8631	8632	\N	1439	5
4317	8633	8634	\N	1439	6
4318	8635	8636	\N	1440	4
4319	8637	8638	\N	1440	5
4320	8639	8640	\N	1440	6
4321	8641	8642	\N	1441	4
4322	8643	8644	\N	1441	5
4323	8645	8646	\N	1441	6
4324	8647	8648	\N	1442	4
4325	8649	8650	\N	1442	5
4326	8651	8652	\N	1442	6
4327	8653	8654	\N	1443	4
4328	8655	8656	\N	1443	5
4329	8657	8658	\N	1443	6
4330	8659	8660	\N	1444	4
4331	8661	8662	\N	1444	5
4332	8663	8664	\N	1444	6
4333	8665	8666	\N	1445	4
4334	8667	8668	\N	1445	5
4335	8669	8670	\N	1445	6
4336	8671	8672	\N	1446	4
4337	8673	8674	\N	1446	5
4338	8675	8676	\N	1446	6
4339	8677	8678	\N	1447	4
4340	8679	8680	\N	1447	5
4341	8681	8682	\N	1447	6
4342	8683	8684	\N	1448	4
4343	8685	8686	\N	1448	5
4344	8687	8688	\N	1448	6
4345	8689	8690	\N	1449	4
4346	8691	8692	\N	1449	5
4347	8693	8694	\N	1449	6
4348	8695	8696	\N	1450	4
4349	8697	8698	\N	1450	5
4350	8699	8700	\N	1450	6
4351	8701	8702	\N	1451	4
4352	8703	8704	\N	1451	5
4353	8705	8706	\N	1451	6
4354	8707	8708	\N	1452	4
4355	8709	8710	\N	1452	5
4356	8711	8712	\N	1452	6
4357	8713	8714	\N	1453	4
4358	8715	8716	\N	1453	5
4359	8717	8718	\N	1453	6
4360	8719	8720	\N	1454	4
4361	8721	8722	\N	1454	5
4362	8723	8724	\N	1454	6
4363	8725	8726	\N	1455	4
4364	8727	8728	\N	1455	5
4365	8729	8730	\N	1455	6
4366	8731	8732	\N	1456	4
4367	8733	8734	\N	1456	5
4368	8735	8736	\N	1456	6
4369	8737	8738	\N	1457	4
4370	8739	8740	\N	1457	5
4371	8741	8742	\N	1457	6
4372	8743	8744	\N	1458	4
4373	8745	8746	\N	1458	5
4374	8747	8748	\N	1458	6
4375	8749	8750	\N	1459	4
4376	8751	8752	\N	1459	5
4377	8753	8754	\N	1459	6
4378	8755	8756	\N	1460	4
4379	8757	8758	\N	1460	5
4380	8759	8760	\N	1460	6
4381	8761	8762	\N	1461	4
4382	8763	8764	\N	1461	5
4383	8765	8766	\N	1461	6
4384	8767	8768	\N	1462	4
4385	8769	8770	\N	1462	5
4386	8771	8772	\N	1462	6
4387	8773	8774	\N	1463	4
4388	8775	8776	\N	1463	5
4389	8777	8778	\N	1463	6
4390	8779	8780	\N	1464	4
4391	8781	8782	\N	1464	5
4392	8783	8784	\N	1464	6
4393	8785	8786	\N	1465	4
4394	8787	8788	\N	1465	5
4395	8789	8790	\N	1465	6
4396	8791	8792	\N	1466	4
4397	8793	8794	\N	1466	5
4398	8795	8796	\N	1466	6
4399	8797	8798	\N	1467	4
4400	8799	8800	\N	1467	5
4401	8801	8802	\N	1467	6
4402	8803	8804	\N	1468	4
4403	8805	8806	\N	1468	5
4404	8807	8808	\N	1468	6
4405	8809	8810	\N	1469	4
4406	8811	8812	\N	1469	5
4407	8813	8814	\N	1469	6
4408	8815	8816	\N	1470	4
4409	8817	8818	\N	1470	5
4410	8819	8820	\N	1470	6
4411	8821	8822	\N	1471	4
4412	8823	8824	\N	1471	5
4413	8825	8826	\N	1471	6
4414	8827	8828	\N	1472	4
4415	8829	8830	\N	1472	5
4416	8831	8832	\N	1472	6
4417	8833	8834	\N	1473	4
4418	8835	8836	\N	1473	5
4419	8837	8838	\N	1473	6
4420	8839	8840	\N	1474	4
4421	8841	8842	\N	1474	5
4422	8843	8844	\N	1474	6
4423	8845	8846	\N	1475	4
4424	8847	8848	\N	1475	5
4425	8849	8850	\N	1475	6
4426	8851	8852	\N	1476	4
4427	8853	8854	\N	1476	5
4428	8855	8856	\N	1476	6
4429	8857	8858	\N	1477	4
4430	8859	8860	\N	1477	5
4431	8861	8862	\N	1477	6
4432	8863	8864	\N	1478	4
4433	8865	8866	\N	1478	5
4434	8867	8868	\N	1478	6
4435	8869	8870	\N	1479	4
4436	8871	8872	\N	1479	5
4437	8873	8874	\N	1479	6
4438	8875	8876	\N	1480	4
4439	8877	8878	\N	1480	5
4440	8879	8880	\N	1480	6
4441	8881	8882	\N	1481	4
4442	8883	8884	\N	1481	5
4443	8885	8886	\N	1481	6
4444	8887	8888	\N	1482	4
4445	8889	8890	\N	1482	5
4446	8891	8892	\N	1482	6
4447	8893	8894	\N	1483	4
4448	8895	8896	\N	1483	5
4449	8897	8898	\N	1483	6
4450	8899	8900	\N	1484	4
4451	8901	8902	\N	1484	5
4452	8903	8904	\N	1484	6
4453	8905	8906	\N	1485	4
4454	8907	8908	\N	1485	5
4455	8909	8910	\N	1485	6
4456	8911	8912	\N	1486	4
4457	8913	8914	\N	1486	5
4458	8915	8916	\N	1486	6
4459	8917	8918	\N	1487	4
4460	8919	8920	\N	1487	5
4461	8921	8922	\N	1487	6
4462	8923	8924	\N	1488	4
4463	8925	8926	\N	1488	5
4464	8927	8928	\N	1488	6
4465	8929	8930	\N	1489	4
4466	8931	8932	\N	1489	5
4467	8933	8934	\N	1489	6
4468	8935	8936	\N	1490	4
4469	8937	8938	\N	1490	5
4470	8939	8940	\N	1490	6
4471	8941	8942	\N	1491	4
4472	8943	8944	\N	1491	5
4473	8945	8946	\N	1491	6
4474	8947	8948	\N	1492	4
4475	8949	8950	\N	1492	5
4476	8951	8952	\N	1492	6
4477	8953	8954	\N	1493	4
4478	8955	8956	\N	1493	5
4479	8957	8958	\N	1493	6
4480	8959	8960	\N	1494	4
4481	8961	8962	\N	1494	5
4482	8963	8964	\N	1494	6
4483	8965	8966	\N	1495	4
4484	8967	8968	\N	1495	5
4485	8969	8970	\N	1495	6
4486	8971	8972	\N	1496	4
4487	8973	8974	\N	1496	5
4488	8975	8976	\N	1496	6
4489	8977	8978	\N	1497	4
4490	8979	8980	\N	1497	5
4491	8981	8982	\N	1497	6
4492	8983	8984	\N	1498	4
4493	8985	8986	\N	1498	5
4494	8987	8988	\N	1498	6
4495	8989	8990	\N	1499	4
4496	8991	8992	\N	1499	5
4497	8993	8994	\N	1499	6
4498	8995	8996	\N	1500	4
4499	8997	8998	\N	1500	5
4500	8999	9000	\N	1500	6
4501	9001	9002	\N	1501	4
4502	9003	9004	\N	1501	5
4503	9005	9006	\N	1501	6
4504	9007	9008	\N	1502	4
4505	9009	9010	\N	1502	5
4506	9011	9012	\N	1502	6
4507	9013	9014	\N	1503	4
4508	9015	9016	\N	1503	5
4509	9017	9018	\N	1503	6
4510	9019	9020	\N	1504	4
4511	9021	9022	\N	1504	5
4512	9023	9024	\N	1504	6
4513	9025	9026	\N	1505	4
4514	9027	9028	\N	1505	5
4515	9029	9030	\N	1505	6
4516	9031	9032	\N	1506	4
4517	9033	9034	\N	1506	5
4518	9035	9036	\N	1506	6
4519	9037	9038	\N	1507	4
4520	9039	9040	\N	1507	5
4521	9041	9042	\N	1507	6
4522	9043	9044	\N	1508	4
4523	9045	9046	\N	1508	5
4524	9047	9048	\N	1508	6
4525	9049	9050	\N	1509	4
4526	9051	9052	\N	1509	5
4527	9053	9054	\N	1509	6
4528	9055	9056	\N	1510	4
4529	9057	9058	\N	1510	5
4530	9059	9060	\N	1510	6
4531	9061	9062	\N	1511	4
4532	9063	9064	\N	1511	5
4533	9065	9066	\N	1511	6
4534	9067	9068	\N	1512	4
4535	9069	9070	\N	1512	5
4536	9071	9072	\N	1512	6
4537	9073	9074	\N	1513	4
4538	9075	9076	\N	1513	5
4539	9077	9078	\N	1513	6
4540	9079	9080	\N	1514	4
4541	9081	9082	\N	1514	5
4542	9083	9084	\N	1514	6
4543	9085	9086	\N	1515	4
4544	9087	9088	\N	1515	5
4545	9089	9090	\N	1515	6
4546	9091	9092	\N	1516	4
4547	9093	9094	\N	1516	5
4548	9095	9096	\N	1516	6
4549	9097	9098	\N	1517	4
4550	9099	9100	\N	1517	5
4551	9101	9102	\N	1517	6
4552	9103	9104	\N	1518	4
4553	9105	9106	\N	1518	5
4554	9107	9108	\N	1518	6
4555	9109	9110	\N	1519	4
4556	9111	9112	\N	1519	5
4557	9113	9114	\N	1519	6
4558	9115	9116	\N	1520	4
4559	9117	9118	\N	1520	5
4560	9119	9120	\N	1520	6
4561	9121	9122	\N	1521	4
4562	9123	9124	\N	1521	5
4563	9125	9126	\N	1521	6
4564	9127	9128	\N	1522	4
4565	9129	9130	\N	1522	5
4566	9131	9132	\N	1522	6
4567	9133	9134	\N	1523	4
4568	9135	9136	\N	1523	5
4569	9137	9138	\N	1523	6
4570	9139	9140	\N	1524	4
4571	9141	9142	\N	1524	5
4572	9143	9144	\N	1524	6
4573	9145	9146	\N	1525	4
4574	9147	9148	\N	1525	5
4575	9149	9150	\N	1525	6
4576	9151	9152	\N	1526	4
4577	9153	9154	\N	1526	5
4578	9155	9156	\N	1526	6
4579	9157	9158	\N	1527	4
4580	9159	9160	\N	1527	5
4581	9161	9162	\N	1527	6
4582	9163	9164	\N	1528	4
4583	9165	9166	\N	1528	5
4584	9167	9168	\N	1528	6
4585	9169	9170	\N	1529	4
4586	9171	9172	\N	1529	5
4587	9173	9174	\N	1529	6
4588	9175	9176	\N	1530	4
4589	9177	9178	\N	1530	5
4590	9179	9180	\N	1530	6
4591	9181	9182	\N	1531	4
4592	9183	9184	\N	1531	5
4593	9185	9186	\N	1531	6
4594	9187	9188	\N	1532	4
4595	9189	9190	\N	1532	5
4596	9191	9192	\N	1532	6
4597	9193	9194	\N	1533	4
4598	9195	9196	\N	1533	5
4599	9197	9198	\N	1533	6
4600	9199	9200	\N	1534	4
4601	9201	9202	\N	1534	5
4602	9203	9204	\N	1534	6
4603	9205	9206	\N	1535	4
4604	9207	9208	\N	1535	5
4605	9209	9210	\N	1535	6
4606	9211	9212	\N	1536	4
4607	9213	9214	\N	1536	5
4608	9215	9216	\N	1536	6
4609	9217	9218	\N	1537	4
4610	9219	9220	\N	1537	5
4611	9221	9222	\N	1537	6
4612	9223	9224	\N	1538	4
4613	9225	9226	\N	1538	5
4614	9227	9228	\N	1538	6
4615	9229	9230	\N	1539	4
4616	9231	9232	\N	1539	5
4617	9233	9234	\N	1539	6
4618	9235	9236	\N	1540	4
4619	9237	9238	\N	1540	5
4620	9239	9240	\N	1540	6
4621	9241	9242	\N	1541	4
4622	9243	9244	\N	1541	5
4623	9245	9246	\N	1541	6
4624	9247	9248	\N	1542	4
4625	9249	9250	\N	1542	5
4626	9251	9252	\N	1542	6
4627	9253	9254	\N	1543	4
4628	9255	9256	\N	1543	5
4629	9257	9258	\N	1543	6
4630	9259	9260	\N	1544	4
4631	9261	9262	\N	1544	5
4632	9263	9264	\N	1544	6
4633	9265	9266	\N	1545	4
4634	9267	9268	\N	1545	5
4635	9269	9270	\N	1545	6
4636	9271	9272	\N	1546	4
4637	9273	9274	\N	1546	5
4638	9275	9276	\N	1546	6
4639	9277	9278	\N	1547	4
4640	9279	9280	\N	1547	5
4641	9281	9282	\N	1547	6
4642	9283	9284	\N	1548	4
4643	9285	9286	\N	1548	5
4644	9287	9288	\N	1548	6
4645	9289	9290	\N	1549	4
4646	9291	9292	\N	1549	5
4647	9293	9294	\N	1549	6
4648	9295	9296	\N	1550	4
4649	9297	9298	\N	1550	5
4650	9299	9300	\N	1550	6
4651	9301	9302	\N	1551	4
4652	9303	9304	\N	1551	5
4653	9305	9306	\N	1551	6
4654	9307	9308	\N	1552	4
4655	9309	9310	\N	1552	5
4656	9311	9312	\N	1552	6
4657	9313	9314	\N	1553	4
4658	9315	9316	\N	1553	5
4659	9317	9318	\N	1553	6
4660	9319	9320	\N	1554	4
4661	9321	9322	\N	1554	5
4662	9323	9324	\N	1554	6
4663	9325	9326	\N	1555	4
4664	9327	9328	\N	1555	5
4665	9329	9330	\N	1555	6
4666	9331	9332	\N	1556	4
4667	9333	9334	\N	1556	5
4668	9335	9336	\N	1556	6
4669	9337	9338	\N	1557	4
4670	9339	9340	\N	1557	5
4671	9341	9342	\N	1557	6
4672	9343	9344	\N	1558	4
4673	9345	9346	\N	1558	5
4674	9347	9348	\N	1558	6
4675	9349	9350	\N	1559	4
4676	9351	9352	\N	1559	5
4677	9353	9354	\N	1559	6
4678	9355	9356	\N	1560	4
4679	9357	9358	\N	1560	5
4680	9359	9360	\N	1560	6
4681	9361	9362	\N	1561	4
4682	9363	9364	\N	1561	5
4683	9365	9366	\N	1561	6
4684	9367	9368	\N	1562	4
4685	9369	9370	\N	1562	5
4686	9371	9372	\N	1562	6
4687	9373	9374	\N	1563	4
4688	9375	9376	\N	1563	5
4689	9377	9378	\N	1563	6
4690	9379	9380	\N	1564	4
4691	9381	9382	\N	1564	5
4692	9383	9384	\N	1564	6
4693	9385	9386	\N	1565	4
4694	9387	9388	\N	1565	5
4695	9389	9390	\N	1565	6
4696	9391	9392	\N	1566	4
4697	9393	9394	\N	1566	5
4698	9395	9396	\N	1566	6
4699	9397	9398	\N	1567	4
4700	9399	9400	\N	1567	5
4701	9401	9402	\N	1567	6
4702	9403	9404	\N	1568	4
4703	9405	9406	\N	1568	5
4704	9407	9408	\N	1568	6
4705	9409	9410	\N	1569	4
4706	9411	9412	\N	1569	5
4707	9413	9414	\N	1569	6
4708	9415	9416	\N	1570	4
4709	9417	9418	\N	1570	5
4710	9419	9420	\N	1570	6
4711	9421	9422	\N	1571	4
4712	9423	9424	\N	1571	5
4713	9425	9426	\N	1571	6
4714	9427	9428	\N	1572	4
4715	9429	9430	\N	1572	5
4716	9431	9432	\N	1572	6
4717	9433	9434	\N	1573	4
4718	9435	9436	\N	1573	5
4719	9437	9438	\N	1573	6
4720	9439	9440	\N	1574	4
4721	9441	9442	\N	1574	5
4722	9443	9444	\N	1574	6
4723	9445	9446	\N	1575	4
4724	9447	9448	\N	1575	5
4725	9449	9450	\N	1575	6
4726	9451	9452	\N	1576	4
4727	9453	9454	\N	1576	5
4728	9455	9456	\N	1576	6
4729	9457	9458	\N	1577	4
4730	9459	9460	\N	1577	5
4731	9461	9462	\N	1577	6
4732	9463	9464	\N	1578	4
4733	9465	9466	\N	1578	5
4734	9467	9468	\N	1578	6
4735	9469	9470	\N	1579	4
4736	9471	9472	\N	1579	5
4737	9473	9474	\N	1579	6
4738	9475	9476	\N	1580	4
4739	9477	9478	\N	1580	5
4740	9479	9480	\N	1580	6
4741	9481	9482	\N	1581	4
4742	9483	9484	\N	1581	5
4743	9485	9486	\N	1581	6
4744	9487	9488	\N	1582	4
4745	9489	9490	\N	1582	5
4746	9491	9492	\N	1582	6
4747	9493	9494	\N	1583	4
4748	9495	9496	\N	1583	5
4749	9497	9498	\N	1583	6
4750	9499	9500	\N	1584	4
4751	9501	9502	\N	1584	5
4752	9503	9504	\N	1584	6
4753	9505	9506	\N	1585	4
4754	9507	9508	\N	1585	5
4755	9509	9510	\N	1585	6
4756	9511	9512	\N	1586	4
4757	9513	9514	\N	1586	5
4758	9515	9516	\N	1586	6
4759	9517	9518	\N	1587	4
4760	9519	9520	\N	1587	5
4761	9521	9522	\N	1587	6
4762	9523	9524	\N	1588	4
4763	9525	9526	\N	1588	5
4764	9527	9528	\N	1588	6
4765	9529	9530	\N	1589	4
4766	9531	9532	\N	1589	5
4767	9533	9534	\N	1589	6
4768	9535	9536	\N	1590	4
4769	9537	9538	\N	1590	5
4770	9539	9540	\N	1590	6
4771	9541	9542	\N	1591	4
4772	9543	9544	\N	1591	5
4773	9545	9546	\N	1591	6
4774	9547	9548	\N	1592	4
4775	9549	9550	\N	1592	5
4776	9551	9552	\N	1592	6
4777	9553	9554	\N	1593	4
4778	9555	9556	\N	1593	5
4779	9557	9558	\N	1593	6
4780	9559	9560	\N	1594	4
4781	9561	9562	\N	1594	5
4782	9563	9564	\N	1594	6
4783	9565	9566	\N	1595	4
4784	9567	9568	\N	1595	5
4785	9569	9570	\N	1595	6
4786	9571	9572	\N	1596	4
4787	9573	9574	\N	1596	5
4788	9575	9576	\N	1596	6
4789	9577	9578	\N	1597	4
4790	9579	9580	\N	1597	5
4791	9581	9582	\N	1597	6
4792	9583	9584	\N	1598	4
4793	9585	9586	\N	1598	5
4794	9587	9588	\N	1598	6
4795	9589	9590	\N	1599	4
4796	9591	9592	\N	1599	5
4797	9593	9594	\N	1599	6
4798	9595	9596	\N	1600	4
4799	9597	9598	\N	1600	5
4800	9599	9600	\N	1600	6
4801	9601	9602	\N	1601	4
4802	9603	9604	\N	1601	5
4803	9605	9606	\N	1601	6
4804	9607	9608	\N	1602	4
4805	9609	9610	\N	1602	5
4806	9611	9612	\N	1602	6
4807	9613	9614	\N	1603	4
4808	9615	9616	\N	1603	5
4809	9617	9618	\N	1603	6
4810	9619	9620	\N	1604	4
4811	9621	9622	\N	1604	5
4812	9623	9624	\N	1604	6
4813	9625	9626	\N	1605	4
4814	9627	9628	\N	1605	5
4815	9629	9630	\N	1605	6
4816	9631	9632	\N	1606	4
4817	9633	9634	\N	1606	5
4818	9635	9636	\N	1606	6
4819	9637	9638	\N	1607	4
4820	9639	9640	\N	1607	5
4821	9641	9642	\N	1607	6
4822	9643	9644	\N	1608	4
4823	9645	9646	\N	1608	5
4824	9647	9648	\N	1608	6
4825	9649	9650	\N	1609	4
4826	9651	9652	\N	1609	5
4827	9653	9654	\N	1609	6
4828	9655	9656	\N	1610	4
4829	9657	9658	\N	1610	5
4830	9659	9660	\N	1610	6
4831	9661	9662	\N	1611	4
4832	9663	9664	\N	1611	5
4833	9665	9666	\N	1611	6
4834	9667	9668	\N	1612	4
4835	9669	9670	\N	1612	5
4836	9671	9672	\N	1612	6
4837	9673	9674	\N	1613	4
4838	9675	9676	\N	1613	5
4839	9677	9678	\N	1613	6
4840	9679	9680	\N	1614	4
4841	9681	9682	\N	1614	5
4842	9683	9684	\N	1614	6
4843	9685	9686	\N	1615	4
4844	9687	9688	\N	1615	5
4845	9689	9690	\N	1615	6
4846	9691	9692	\N	1616	4
4847	9693	9694	\N	1616	5
4848	9695	9696	\N	1616	6
4849	9697	9698	\N	1617	4
4850	9699	9700	\N	1617	5
4851	9701	9702	\N	1617	6
4852	9703	9704	\N	1618	4
4853	9705	9706	\N	1618	5
4854	9707	9708	\N	1618	6
4855	9709	9710	\N	1619	4
4856	9711	9712	\N	1619	5
4857	9713	9714	\N	1619	6
4858	9715	9716	\N	1620	4
4859	9717	9718	\N	1620	5
4860	9719	9720	\N	1620	6
4861	9721	9722	\N	1621	4
4862	9723	9724	\N	1621	5
4863	9725	9726	\N	1621	6
4864	9727	9728	\N	1622	4
4865	9729	9730	\N	1622	5
4866	9731	9732	\N	1622	6
4867	9733	9734	\N	1623	4
4868	9735	9736	\N	1623	5
4869	9737	9738	\N	1623	6
4870	9739	9740	\N	1624	4
4871	9741	9742	\N	1624	5
4872	9743	9744	\N	1624	6
4873	9745	9746	\N	1625	4
4874	9747	9748	\N	1625	5
4875	9749	9750	\N	1625	6
4876	9751	9752	\N	1626	4
4877	9753	9754	\N	1626	5
4878	9755	9756	\N	1626	6
4879	9757	9758	\N	1627	4
4880	9759	9760	\N	1627	5
4881	9761	9762	\N	1627	6
4882	9763	9764	\N	1628	4
4883	9765	9766	\N	1628	5
4884	9767	9768	\N	1628	6
4885	9769	9770	\N	1629	4
4886	9771	9772	\N	1629	5
4887	9773	9774	\N	1629	6
4888	9775	9776	\N	1630	4
4889	9777	9778	\N	1630	5
4890	9779	9780	\N	1630	6
4891	9781	9782	\N	1631	4
4892	9783	9784	\N	1631	5
4893	9785	9786	\N	1631	6
4894	9787	9788	\N	1632	4
4895	9789	9790	\N	1632	5
4896	9791	9792	\N	1632	6
4897	9793	9794	\N	1633	4
4898	9795	9796	\N	1633	5
4899	9797	9798	\N	1633	6
4900	9799	9800	\N	1634	4
4901	9801	9802	\N	1634	5
4902	9803	9804	\N	1634	6
4903	9805	9806	\N	1635	4
4904	9807	9808	\N	1635	5
4905	9809	9810	\N	1635	6
4906	9811	9812	\N	1636	4
4907	9813	9814	\N	1636	5
4908	9815	9816	\N	1636	6
4909	9817	9818	\N	1637	4
4910	9819	9820	\N	1637	5
4911	9821	9822	\N	1637	6
4912	9823	9824	\N	1638	4
4913	9825	9826	\N	1638	5
4914	9827	9828	\N	1638	6
4915	9829	9830	\N	1639	4
4916	9831	9832	\N	1639	5
4917	9833	9834	\N	1639	6
4918	9835	9836	\N	1640	4
4919	9837	9838	\N	1640	5
4920	9839	9840	\N	1640	6
4921	9841	9842	\N	1641	4
4922	9843	9844	\N	1641	5
4923	9845	9846	\N	1641	6
4924	9847	9848	\N	1642	4
4925	9849	9850	\N	1642	5
4926	9851	9852	\N	1642	6
4927	9853	9854	\N	1643	4
4928	9855	9856	\N	1643	5
4929	9857	9858	\N	1643	6
4930	9859	9860	\N	1644	4
4931	9861	9862	\N	1644	5
4932	9863	9864	\N	1644	6
4933	9865	9866	\N	1645	4
4934	9867	9868	\N	1645	5
4935	9869	9870	\N	1645	6
4936	9871	9872	\N	1646	4
4937	9873	9874	\N	1646	5
4938	9875	9876	\N	1646	6
4939	9877	9878	\N	1647	4
4940	9879	9880	\N	1647	5
4941	9881	9882	\N	1647	6
4942	9883	9884	\N	1648	4
4943	9885	9886	\N	1648	5
4944	9887	9888	\N	1648	6
4945	9889	9890	\N	1649	4
4946	9891	9892	\N	1649	5
4947	9893	9894	\N	1649	6
4948	9895	9896	\N	1650	4
4949	9897	9898	\N	1650	5
4950	9899	9900	\N	1650	6
4951	9901	9902	\N	1651	4
4952	9903	9904	\N	1651	5
4953	9905	9906	\N	1651	6
4954	9907	9908	\N	1652	4
4955	9909	9910	\N	1652	5
4956	9911	9912	\N	1652	6
4957	9913	9914	\N	1653	4
4958	9915	9916	\N	1653	5
4959	9917	9918	\N	1653	6
4960	9919	9920	\N	1654	4
4961	9921	9922	\N	1654	5
4962	9923	9924	\N	1654	6
4963	9925	9926	\N	1655	4
4964	9927	9928	\N	1655	5
4965	9929	9930	\N	1655	6
4966	9931	9932	\N	1656	4
4967	9933	9934	\N	1656	5
4968	9935	9936	\N	1656	6
4969	9937	9938	\N	1657	4
4970	9939	9940	\N	1657	5
4971	9941	9942	\N	1657	6
4972	9943	9944	\N	1658	4
4973	9945	9946	\N	1658	5
4974	9947	9948	\N	1658	6
4975	9949	9950	\N	1659	4
4976	9951	9952	\N	1659	5
4977	9953	9954	\N	1659	6
4978	9955	9956	\N	1660	4
4979	9957	9958	\N	1660	5
4980	9959	9960	\N	1660	6
4981	9961	9962	\N	1661	4
4982	9963	9964	\N	1661	5
4983	9965	9966	\N	1661	6
4984	9967	9968	\N	1662	4
4985	9969	9970	\N	1662	5
4986	9971	9972	\N	1662	6
4987	9973	9974	\N	1663	4
4988	9975	9976	\N	1663	5
4989	9977	9978	\N	1663	6
4990	9979	9980	\N	1664	4
4991	9981	9982	\N	1664	5
4992	9983	9984	\N	1664	6
4993	9985	9986	\N	1665	4
4994	9987	9988	\N	1665	5
4995	9989	9990	\N	1665	6
4996	9991	9992	\N	1666	4
4997	9993	9994	\N	1666	5
4998	9995	9996	\N	1666	6
4999	9997	9998	\N	1667	4
5000	9999	10000	\N	1667	5
5001	10001	10002	\N	1667	6
5002	10003	10004	\N	1668	4
5003	10005	10006	\N	1668	5
5004	10007	10008	\N	1668	6
5005	10009	10010	\N	1669	4
5006	10011	10012	\N	1669	5
5007	10013	10014	\N	1669	6
5008	10015	10016	\N	1670	4
5009	10017	10018	\N	1670	5
5010	10019	10020	\N	1670	6
5011	10021	10022	\N	1671	4
5012	10023	10024	\N	1671	5
5013	10025	10026	\N	1671	6
5014	10027	10028	\N	1672	4
5015	10029	10030	\N	1672	5
5016	10031	10032	\N	1672	6
5017	10033	10034	\N	1673	4
5018	10035	10036	\N	1673	5
5019	10037	10038	\N	1673	6
5020	10039	10040	\N	1674	4
5021	10041	10042	\N	1674	5
5022	10043	10044	\N	1674	6
5023	10045	10046	\N	1675	4
5024	10047	10048	\N	1675	5
5025	10049	10050	\N	1675	6
5026	10051	10052	\N	1676	4
5027	10053	10054	\N	1676	5
5028	10055	10056	\N	1676	6
5029	10057	10058	\N	1677	4
5030	10059	10060	\N	1677	5
5031	10061	10062	\N	1677	6
5032	10063	10064	\N	1678	4
5033	10065	10066	\N	1678	5
5034	10067	10068	\N	1678	6
5035	10069	10070	\N	1679	4
5036	10071	10072	\N	1679	5
5037	10073	10074	\N	1679	6
5038	10075	10076	\N	1680	4
5039	10077	10078	\N	1680	5
5040	10079	10080	\N	1680	6
5041	10081	10082	\N	1681	4
5042	10083	10084	\N	1681	5
5043	10085	10086	\N	1681	6
5044	10087	10088	\N	1682	4
5045	10089	10090	\N	1682	5
5046	10091	10092	\N	1682	6
5047	10093	10094	\N	1683	4
5048	10095	10096	\N	1683	5
5049	10097	10098	\N	1683	6
5050	10099	10100	\N	1684	4
5051	10101	10102	\N	1684	5
5052	10103	10104	\N	1684	6
5053	10105	10106	\N	1685	4
5054	10107	10108	\N	1685	5
5055	10109	10110	\N	1685	6
5056	10111	10112	\N	1686	4
5057	10113	10114	\N	1686	5
5058	10115	10116	\N	1686	6
5059	10117	10118	\N	1687	4
5060	10119	10120	\N	1687	5
5061	10121	10122	\N	1687	6
5062	10123	10124	\N	1688	4
5063	10125	10126	\N	1688	5
5064	10127	10128	\N	1688	6
5065	10129	10130	\N	1689	4
5066	10131	10132	\N	1689	5
5067	10133	10134	\N	1689	6
5068	10135	10136	\N	1690	4
5069	10137	10138	\N	1690	5
5070	10139	10140	\N	1690	6
5071	10141	10142	\N	1691	4
5072	10143	10144	\N	1691	5
5073	10145	10146	\N	1691	6
5074	10147	10148	\N	1692	4
5075	10149	10150	\N	1692	5
5076	10151	10152	\N	1692	6
5077	10153	10154	\N	1693	4
5078	10155	10156	\N	1693	5
5079	10157	10158	\N	1693	6
5080	10159	10160	\N	1694	4
5081	10161	10162	\N	1694	5
5082	10163	10164	\N	1694	6
5083	10165	10166	\N	1695	4
5084	10167	10168	\N	1695	5
5085	10169	10170	\N	1695	6
5086	10171	10172	\N	1696	4
5087	10173	10174	\N	1696	5
5088	10175	10176	\N	1696	6
5089	10177	10178	\N	1697	4
5090	10179	10180	\N	1697	5
5091	10181	10182	\N	1697	6
5092	10183	10184	\N	1698	4
5093	10185	10186	\N	1698	5
5094	10187	10188	\N	1698	6
5095	10189	10190	\N	1699	4
5096	10191	10192	\N	1699	5
5097	10193	10194	\N	1699	6
5098	10195	10196	\N	1700	4
5099	10197	10198	\N	1700	5
5100	10199	10200	\N	1700	6
5101	10201	10202	\N	1701	4
5102	10203	10204	\N	1701	5
5103	10205	10206	\N	1701	6
5104	10207	10208	\N	1702	4
5105	10209	10210	\N	1702	5
5106	10211	10212	\N	1702	6
5107	10213	10214	\N	1703	4
5108	10215	10216	\N	1703	5
5109	10217	10218	\N	1703	6
5110	10219	10220	\N	1704	4
5111	10221	10222	\N	1704	5
5112	10223	10224	\N	1704	6
5113	10225	10226	\N	1705	4
5114	10227	10228	\N	1705	5
5115	10229	10230	\N	1705	6
5116	10231	10232	\N	1706	4
5117	10233	10234	\N	1706	5
5118	10235	10236	\N	1706	6
5119	10237	10238	\N	1707	4
5120	10239	10240	\N	1707	5
5121	10241	10242	\N	1707	6
5122	10243	10244	\N	1708	4
5123	10245	10246	\N	1708	5
5124	10247	10248	\N	1708	6
5125	10249	10250	\N	1709	4
5126	10251	10252	\N	1709	5
5127	10253	10254	\N	1709	6
5128	10255	10256	\N	1710	4
5129	10257	10258	\N	1710	5
5130	10259	10260	\N	1710	6
5131	10261	10262	\N	1711	4
5132	10263	10264	\N	1711	5
5133	10265	10266	\N	1711	6
5134	10267	10268	\N	1712	4
5135	10269	10270	\N	1712	5
5136	10271	10272	\N	1712	6
5137	10273	10274	\N	1713	4
5138	10275	10276	\N	1713	5
5139	10277	10278	\N	1713	6
5140	10279	10280	\N	1714	4
5141	10281	10282	\N	1714	5
5142	10283	10284	\N	1714	6
5143	10285	10286	\N	1715	4
5144	10287	10288	\N	1715	5
5145	10289	10290	\N	1715	6
5146	10291	10292	\N	1716	4
5147	10293	10294	\N	1716	5
5148	10295	10296	\N	1716	6
5149	10297	10298	\N	1717	4
5150	10299	10300	\N	1717	5
5151	10301	10302	\N	1717	6
5152	10303	10304	\N	1718	4
5153	10305	10306	\N	1718	5
5154	10307	10308	\N	1718	6
5155	10309	10310	\N	1719	4
5156	10311	10312	\N	1719	5
5157	10313	10314	\N	1719	6
5158	10315	10316	\N	1720	4
5159	10317	10318	\N	1720	5
5160	10319	10320	\N	1720	6
5161	10321	10322	\N	1721	4
5162	10323	10324	\N	1721	5
5163	10325	10326	\N	1721	6
5164	10327	10328	\N	1722	4
5165	10329	10330	\N	1722	5
5166	10331	10332	\N	1722	6
5167	10333	10334	\N	1723	4
5168	10335	10336	\N	1723	5
5169	10337	10338	\N	1723	6
5170	10339	10340	\N	1724	4
5171	10341	10342	\N	1724	5
5172	10343	10344	\N	1724	6
5173	10345	10346	\N	1725	4
5174	10347	10348	\N	1725	5
5175	10349	10350	\N	1725	6
5176	10351	10352	\N	1726	4
5177	10353	10354	\N	1726	5
5178	10355	10356	\N	1726	6
5179	10357	10358	\N	1727	4
5180	10359	10360	\N	1727	5
5181	10361	10362	\N	1727	6
5182	10363	10364	\N	1728	4
5183	10365	10366	\N	1728	5
5184	10367	10368	\N	1728	6
5185	10369	10370	\N	1729	7
5186	10371	10372	\N	1729	8
5187	10373	10374	\N	1729	9
5188	10375	10376	\N	1730	7
5189	10377	10378	\N	1730	8
5190	10379	10380	\N	1730	9
5191	10381	10382	\N	1731	7
5192	10383	10384	\N	1731	8
5193	10385	10386	\N	1731	9
5194	10387	10388	\N	1732	7
5195	10389	10390	\N	1732	8
5196	10391	10392	\N	1732	9
5197	10393	10394	\N	1733	7
5198	10395	10396	\N	1733	8
5199	10397	10398	\N	1733	9
5200	10399	10400	\N	1734	7
5201	10401	10402	\N	1734	8
5202	10403	10404	\N	1734	9
5203	10405	10406	\N	1735	7
5204	10407	10408	\N	1735	8
5205	10409	10410	\N	1735	9
5206	10411	10412	\N	1736	7
5207	10413	10414	\N	1736	8
5208	10415	10416	\N	1736	9
5209	10417	10418	\N	1737	7
5210	10419	10420	\N	1737	8
5211	10421	10422	\N	1737	9
5212	10423	10424	\N	1738	7
5213	10425	10426	\N	1738	8
5214	10427	10428	\N	1738	9
5215	10429	10430	\N	1739	7
5216	10431	10432	\N	1739	8
5217	10433	10434	\N	1739	9
5218	10435	10436	\N	1740	7
5219	10437	10438	\N	1740	8
5220	10439	10440	\N	1740	9
5221	10441	10442	\N	1741	7
5222	10443	10444	\N	1741	8
5223	10445	10446	\N	1741	9
5224	10447	10448	\N	1742	7
5225	10449	10450	\N	1742	8
5226	10451	10452	\N	1742	9
5227	10453	10454	\N	1743	7
5228	10455	10456	\N	1743	8
5229	10457	10458	\N	1743	9
5230	10459	10460	\N	1744	7
5231	10461	10462	\N	1744	8
5232	10463	10464	\N	1744	9
5233	10465	10466	\N	1745	7
5234	10467	10468	\N	1745	8
5235	10469	10470	\N	1745	9
5236	10471	10472	\N	1746	7
5237	10473	10474	\N	1746	8
5238	10475	10476	\N	1746	9
5239	10477	10478	\N	1747	7
5240	10479	10480	\N	1747	8
5241	10481	10482	\N	1747	9
5242	10483	10484	\N	1748	7
5243	10485	10486	\N	1748	8
5244	10487	10488	\N	1748	9
5245	10489	10490	\N	1749	7
5246	10491	10492	\N	1749	8
5247	10493	10494	\N	1749	9
5248	10495	10496	\N	1750	7
5249	10497	10498	\N	1750	8
5250	10499	10500	\N	1750	9
5251	10501	10502	\N	1751	7
5252	10503	10504	\N	1751	8
5253	10505	10506	\N	1751	9
5254	10507	10508	\N	1752	7
5255	10509	10510	\N	1752	8
5256	10511	10512	\N	1752	9
5257	10513	10514	\N	1753	7
5258	10515	10516	\N	1753	8
5259	10517	10518	\N	1753	9
5260	10519	10520	\N	1754	7
5261	10521	10522	\N	1754	8
5262	10523	10524	\N	1754	9
5263	10525	10526	\N	1755	7
5264	10527	10528	\N	1755	8
5265	10529	10530	\N	1755	9
5266	10531	10532	\N	1756	7
5267	10533	10534	\N	1756	8
5268	10535	10536	\N	1756	9
5269	10537	10538	\N	1757	7
5270	10539	10540	\N	1757	8
5271	10541	10542	\N	1757	9
5272	10543	10544	\N	1758	7
5273	10545	10546	\N	1758	8
5274	10547	10548	\N	1758	9
5275	10549	10550	\N	1759	7
5276	10551	10552	\N	1759	8
5277	10553	10554	\N	1759	9
5278	10555	10556	\N	1760	7
5279	10557	10558	\N	1760	8
5280	10559	10560	\N	1760	9
5281	10561	10562	\N	1761	7
5282	10563	10564	\N	1761	8
5283	10565	10566	\N	1761	9
5284	10567	10568	\N	1762	7
5285	10569	10570	\N	1762	8
5286	10571	10572	\N	1762	9
5287	10573	10574	\N	1763	7
5288	10575	10576	\N	1763	8
5289	10577	10578	\N	1763	9
5290	10579	10580	\N	1764	7
5291	10581	10582	\N	1764	8
5292	10583	10584	\N	1764	9
5293	10585	10586	\N	1765	7
5294	10587	10588	\N	1765	8
5295	10589	10590	\N	1765	9
5296	10591	10592	\N	1766	7
5297	10593	10594	\N	1766	8
5298	10595	10596	\N	1766	9
5299	10597	10598	\N	1767	7
5300	10599	10600	\N	1767	8
5301	10601	10602	\N	1767	9
5302	10603	10604	\N	1768	7
5303	10605	10606	\N	1768	8
5304	10607	10608	\N	1768	9
5305	10609	10610	\N	1769	7
5306	10611	10612	\N	1769	8
5307	10613	10614	\N	1769	9
5308	10615	10616	\N	1770	7
5309	10617	10618	\N	1770	8
5310	10619	10620	\N	1770	9
5311	10621	10622	\N	1771	7
5312	10623	10624	\N	1771	8
5313	10625	10626	\N	1771	9
5314	10627	10628	\N	1772	7
5315	10629	10630	\N	1772	8
5316	10631	10632	\N	1772	9
5317	10633	10634	\N	1773	7
5318	10635	10636	\N	1773	8
5319	10637	10638	\N	1773	9
5320	10639	10640	\N	1774	7
5321	10641	10642	\N	1774	8
5322	10643	10644	\N	1774	9
5323	10645	10646	\N	1775	7
5324	10647	10648	\N	1775	8
5325	10649	10650	\N	1775	9
5326	10651	10652	\N	1776	7
5327	10653	10654	\N	1776	8
5328	10655	10656	\N	1776	9
5329	10657	10658	\N	1777	7
5330	10659	10660	\N	1777	8
5331	10661	10662	\N	1777	9
5332	10663	10664	\N	1778	7
5333	10665	10666	\N	1778	8
5334	10667	10668	\N	1778	9
5335	10669	10670	\N	1779	7
5336	10671	10672	\N	1779	8
5337	10673	10674	\N	1779	9
5338	10675	10676	\N	1780	7
5339	10677	10678	\N	1780	8
5340	10679	10680	\N	1780	9
5341	10681	10682	\N	1781	7
5342	10683	10684	\N	1781	8
5343	10685	10686	\N	1781	9
5344	10687	10688	\N	1782	7
5345	10689	10690	\N	1782	8
5346	10691	10692	\N	1782	9
5347	10693	10694	\N	1783	7
5348	10695	10696	\N	1783	8
5349	10697	10698	\N	1783	9
5350	10699	10700	\N	1784	7
5351	10701	10702	\N	1784	8
5352	10703	10704	\N	1784	9
5353	10705	10706	\N	1785	7
5354	10707	10708	\N	1785	8
5355	10709	10710	\N	1785	9
5356	10711	10712	\N	1786	7
5357	10713	10714	\N	1786	8
5358	10715	10716	\N	1786	9
5359	10717	10718	\N	1787	7
5360	10719	10720	\N	1787	8
5361	10721	10722	\N	1787	9
5362	10723	10724	\N	1788	7
5363	10725	10726	\N	1788	8
5364	10727	10728	\N	1788	9
5365	10729	10730	\N	1789	7
5366	10731	10732	\N	1789	8
5367	10733	10734	\N	1789	9
5368	10735	10736	\N	1790	7
5369	10737	10738	\N	1790	8
5370	10739	10740	\N	1790	9
5371	10741	10742	\N	1791	7
5372	10743	10744	\N	1791	8
5373	10745	10746	\N	1791	9
5374	10747	10748	\N	1792	7
5375	10749	10750	\N	1792	8
5376	10751	10752	\N	1792	9
5377	10753	10754	\N	1793	7
5378	10755	10756	\N	1793	8
5379	10757	10758	\N	1793	9
5380	10759	10760	\N	1794	7
5381	10761	10762	\N	1794	8
5382	10763	10764	\N	1794	9
5383	10765	10766	\N	1795	7
5384	10767	10768	\N	1795	8
5385	10769	10770	\N	1795	9
5386	10771	10772	\N	1796	7
5387	10773	10774	\N	1796	8
5388	10775	10776	\N	1796	9
5389	10777	10778	\N	1797	7
5390	10779	10780	\N	1797	8
5391	10781	10782	\N	1797	9
5392	10783	10784	\N	1798	7
5393	10785	10786	\N	1798	8
5394	10787	10788	\N	1798	9
5395	10789	10790	\N	1799	7
5396	10791	10792	\N	1799	8
5397	10793	10794	\N	1799	9
5398	10795	10796	\N	1800	7
5399	10797	10798	\N	1800	8
5400	10799	10800	\N	1800	9
5401	10801	10802	\N	1801	7
5402	10803	10804	\N	1801	8
5403	10805	10806	\N	1801	9
5404	10807	10808	\N	1802	7
5405	10809	10810	\N	1802	8
5406	10811	10812	\N	1802	9
5407	10813	10814	\N	1803	7
5408	10815	10816	\N	1803	8
5409	10817	10818	\N	1803	9
5410	10819	10820	\N	1804	7
5411	10821	10822	\N	1804	8
5412	10823	10824	\N	1804	9
5413	10825	10826	\N	1805	7
5414	10827	10828	\N	1805	8
5415	10829	10830	\N	1805	9
5416	10831	10832	\N	1806	7
5417	10833	10834	\N	1806	8
5418	10835	10836	\N	1806	9
5419	10837	10838	\N	1807	7
5420	10839	10840	\N	1807	8
5421	10841	10842	\N	1807	9
5422	10843	10844	\N	1808	7
5423	10845	10846	\N	1808	8
5424	10847	10848	\N	1808	9
5425	10849	10850	\N	1809	7
5426	10851	10852	\N	1809	8
5427	10853	10854	\N	1809	9
5428	10855	10856	\N	1810	7
5429	10857	10858	\N	1810	8
5430	10859	10860	\N	1810	9
5431	10861	10862	\N	1811	7
5432	10863	10864	\N	1811	8
5433	10865	10866	\N	1811	9
5434	10867	10868	\N	1812	7
5435	10869	10870	\N	1812	8
5436	10871	10872	\N	1812	9
5437	10873	10874	\N	1813	7
5438	10875	10876	\N	1813	8
5439	10877	10878	\N	1813	9
5440	10879	10880	\N	1814	7
5441	10881	10882	\N	1814	8
5442	10883	10884	\N	1814	9
5443	10885	10886	\N	1815	7
5444	10887	10888	\N	1815	8
5445	10889	10890	\N	1815	9
5446	10891	10892	\N	1816	7
5447	10893	10894	\N	1816	8
5448	10895	10896	\N	1816	9
5449	10897	10898	\N	1817	7
5450	10899	10900	\N	1817	8
5451	10901	10902	\N	1817	9
5452	10903	10904	\N	1818	7
5453	10905	10906	\N	1818	8
5454	10907	10908	\N	1818	9
5455	10909	10910	\N	1819	7
5456	10911	10912	\N	1819	8
5457	10913	10914	\N	1819	9
5458	10915	10916	\N	1820	7
5459	10917	10918	\N	1820	8
5460	10919	10920	\N	1820	9
5461	10921	10922	\N	1821	7
5462	10923	10924	\N	1821	8
5463	10925	10926	\N	1821	9
5464	10927	10928	\N	1822	7
5465	10929	10930	\N	1822	8
5466	10931	10932	\N	1822	9
5467	10933	10934	\N	1823	7
5468	10935	10936	\N	1823	8
5469	10937	10938	\N	1823	9
5470	10939	10940	\N	1824	7
5471	10941	10942	\N	1824	8
5472	10943	10944	\N	1824	9
5473	10945	10946	\N	1825	7
5474	10947	10948	\N	1825	8
5475	10949	10950	\N	1825	9
5476	10951	10952	\N	1826	7
5477	10953	10954	\N	1826	8
5478	10955	10956	\N	1826	9
5479	10957	10958	\N	1827	7
5480	10959	10960	\N	1827	8
5481	10961	10962	\N	1827	9
5482	10963	10964	\N	1828	7
5483	10965	10966	\N	1828	8
5484	10967	10968	\N	1828	9
5485	10969	10970	\N	1829	7
5486	10971	10972	\N	1829	8
5487	10973	10974	\N	1829	9
5488	10975	10976	\N	1830	7
5489	10977	10978	\N	1830	8
5490	10979	10980	\N	1830	9
5491	10981	10982	\N	1831	7
5492	10983	10984	\N	1831	8
5493	10985	10986	\N	1831	9
5494	10987	10988	\N	1832	7
5495	10989	10990	\N	1832	8
5496	10991	10992	\N	1832	9
5497	10993	10994	\N	1833	7
5498	10995	10996	\N	1833	8
5499	10997	10998	\N	1833	9
5500	10999	11000	\N	1834	7
5501	11001	11002	\N	1834	8
5502	11003	11004	\N	1834	9
5503	11005	11006	\N	1835	7
5504	11007	11008	\N	1835	8
5505	11009	11010	\N	1835	9
5506	11011	11012	\N	1836	7
5507	11013	11014	\N	1836	8
5508	11015	11016	\N	1836	9
5509	11017	11018	\N	1837	7
5510	11019	11020	\N	1837	8
5511	11021	11022	\N	1837	9
5512	11023	11024	\N	1838	7
5513	11025	11026	\N	1838	8
5514	11027	11028	\N	1838	9
5515	11029	11030	\N	1839	7
5516	11031	11032	\N	1839	8
5517	11033	11034	\N	1839	9
5518	11035	11036	\N	1840	7
5519	11037	11038	\N	1840	8
5520	11039	11040	\N	1840	9
5521	11041	11042	\N	1841	7
5522	11043	11044	\N	1841	8
5523	11045	11046	\N	1841	9
5524	11047	11048	\N	1842	7
5525	11049	11050	\N	1842	8
5526	11051	11052	\N	1842	9
5527	11053	11054	\N	1843	7
5528	11055	11056	\N	1843	8
5529	11057	11058	\N	1843	9
5530	11059	11060	\N	1844	7
5531	11061	11062	\N	1844	8
5532	11063	11064	\N	1844	9
5533	11065	11066	\N	1845	7
5534	11067	11068	\N	1845	8
5535	11069	11070	\N	1845	9
5536	11071	11072	\N	1846	7
5537	11073	11074	\N	1846	8
5538	11075	11076	\N	1846	9
5539	11077	11078	\N	1847	7
5540	11079	11080	\N	1847	8
5541	11081	11082	\N	1847	9
5542	11083	11084	\N	1848	7
5543	11085	11086	\N	1848	8
5544	11087	11088	\N	1848	9
5545	11089	11090	\N	1849	7
5546	11091	11092	\N	1849	8
5547	11093	11094	\N	1849	9
5548	11095	11096	\N	1850	7
5549	11097	11098	\N	1850	8
5550	11099	11100	\N	1850	9
5551	11101	11102	\N	1851	7
5552	11103	11104	\N	1851	8
5553	11105	11106	\N	1851	9
5554	11107	11108	\N	1852	7
5555	11109	11110	\N	1852	8
5556	11111	11112	\N	1852	9
5557	11113	11114	\N	1853	7
5558	11115	11116	\N	1853	8
5559	11117	11118	\N	1853	9
5560	11119	11120	\N	1854	7
5561	11121	11122	\N	1854	8
5562	11123	11124	\N	1854	9
5563	11125	11126	\N	1855	7
5564	11127	11128	\N	1855	8
5565	11129	11130	\N	1855	9
5566	11131	11132	\N	1856	7
5567	11133	11134	\N	1856	8
5568	11135	11136	\N	1856	9
5569	11137	11138	\N	1857	7
5570	11139	11140	\N	1857	8
5571	11141	11142	\N	1857	9
5572	11143	11144	\N	1858	7
5573	11145	11146	\N	1858	8
5574	11147	11148	\N	1858	9
5575	11149	11150	\N	1859	7
5576	11151	11152	\N	1859	8
5577	11153	11154	\N	1859	9
5578	11155	11156	\N	1860	7
5579	11157	11158	\N	1860	8
5580	11159	11160	\N	1860	9
5581	11161	11162	\N	1861	7
5582	11163	11164	\N	1861	8
5583	11165	11166	\N	1861	9
5584	11167	11168	\N	1862	7
5585	11169	11170	\N	1862	8
5586	11171	11172	\N	1862	9
5587	11173	11174	\N	1863	7
5588	11175	11176	\N	1863	8
5589	11177	11178	\N	1863	9
5590	11179	11180	\N	1864	7
5591	11181	11182	\N	1864	8
5592	11183	11184	\N	1864	9
5593	11185	11186	\N	1865	7
5594	11187	11188	\N	1865	8
5595	11189	11190	\N	1865	9
5596	11191	11192	\N	1866	7
5597	11193	11194	\N	1866	8
5598	11195	11196	\N	1866	9
5599	11197	11198	\N	1867	7
5600	11199	11200	\N	1867	8
5601	11201	11202	\N	1867	9
5602	11203	11204	\N	1868	7
5603	11205	11206	\N	1868	8
5604	11207	11208	\N	1868	9
5605	11209	11210	\N	1869	7
5606	11211	11212	\N	1869	8
5607	11213	11214	\N	1869	9
5608	11215	11216	\N	1870	7
5609	11217	11218	\N	1870	8
5610	11219	11220	\N	1870	9
5611	11221	11222	\N	1871	7
5612	11223	11224	\N	1871	8
5613	11225	11226	\N	1871	9
5614	11227	11228	\N	1872	7
5615	11229	11230	\N	1872	8
5616	11231	11232	\N	1872	9
5617	11233	11234	\N	1873	7
5618	11235	11236	\N	1873	8
5619	11237	11238	\N	1873	9
5620	11239	11240	\N	1874	7
5621	11241	11242	\N	1874	8
5622	11243	11244	\N	1874	9
5623	11245	11246	\N	1875	7
5624	11247	11248	\N	1875	8
5625	11249	11250	\N	1875	9
5626	11251	11252	\N	1876	7
5627	11253	11254	\N	1876	8
5628	11255	11256	\N	1876	9
5629	11257	11258	\N	1877	7
5630	11259	11260	\N	1877	8
5631	11261	11262	\N	1877	9
5632	11263	11264	\N	1878	7
5633	11265	11266	\N	1878	8
5634	11267	11268	\N	1878	9
5635	11269	11270	\N	1879	7
5636	11271	11272	\N	1879	8
5637	11273	11274	\N	1879	9
5638	11275	11276	\N	1880	7
5639	11277	11278	\N	1880	8
5640	11279	11280	\N	1880	9
5641	11281	11282	\N	1881	7
5642	11283	11284	\N	1881	8
5643	11285	11286	\N	1881	9
5644	11287	11288	\N	1882	7
5645	11289	11290	\N	1882	8
5646	11291	11292	\N	1882	9
5647	11293	11294	\N	1883	7
5648	11295	11296	\N	1883	8
5649	11297	11298	\N	1883	9
5650	11299	11300	\N	1884	7
5651	11301	11302	\N	1884	8
5652	11303	11304	\N	1884	9
5653	11305	11306	\N	1885	7
5654	11307	11308	\N	1885	8
5655	11309	11310	\N	1885	9
5656	11311	11312	\N	1886	7
5657	11313	11314	\N	1886	8
5658	11315	11316	\N	1886	9
5659	11317	11318	\N	1887	7
5660	11319	11320	\N	1887	8
5661	11321	11322	\N	1887	9
5662	11323	11324	\N	1888	7
5663	11325	11326	\N	1888	8
5664	11327	11328	\N	1888	9
5665	11329	11330	\N	1889	7
5666	11331	11332	\N	1889	8
5667	11333	11334	\N	1889	9
5668	11335	11336	\N	1890	7
5669	11337	11338	\N	1890	8
5670	11339	11340	\N	1890	9
5671	11341	11342	\N	1891	7
5672	11343	11344	\N	1891	8
5673	11345	11346	\N	1891	9
5674	11347	11348	\N	1892	7
5675	11349	11350	\N	1892	8
5676	11351	11352	\N	1892	9
5677	11353	11354	\N	1893	7
5678	11355	11356	\N	1893	8
5679	11357	11358	\N	1893	9
5680	11359	11360	\N	1894	7
5681	11361	11362	\N	1894	8
5682	11363	11364	\N	1894	9
5683	11365	11366	\N	1895	7
5684	11367	11368	\N	1895	8
5685	11369	11370	\N	1895	9
5686	11371	11372	\N	1896	7
5687	11373	11374	\N	1896	8
5688	11375	11376	\N	1896	9
5689	11377	11378	\N	1897	7
5690	11379	11380	\N	1897	8
5691	11381	11382	\N	1897	9
5692	11383	11384	\N	1898	7
5693	11385	11386	\N	1898	8
5694	11387	11388	\N	1898	9
5695	11389	11390	\N	1899	7
5696	11391	11392	\N	1899	8
5697	11393	11394	\N	1899	9
5698	11395	11396	\N	1900	7
5699	11397	11398	\N	1900	8
5700	11399	11400	\N	1900	9
5701	11401	11402	\N	1901	7
5702	11403	11404	\N	1901	8
5703	11405	11406	\N	1901	9
5704	11407	11408	\N	1902	7
5705	11409	11410	\N	1902	8
5706	11411	11412	\N	1902	9
5707	11413	11414	\N	1903	7
5708	11415	11416	\N	1903	8
5709	11417	11418	\N	1903	9
5710	11419	11420	\N	1904	7
5711	11421	11422	\N	1904	8
5712	11423	11424	\N	1904	9
5713	11425	11426	\N	1905	7
5714	11427	11428	\N	1905	8
5715	11429	11430	\N	1905	9
5716	11431	11432	\N	1906	7
5717	11433	11434	\N	1906	8
5718	11435	11436	\N	1906	9
5719	11437	11438	\N	1907	7
5720	11439	11440	\N	1907	8
5721	11441	11442	\N	1907	9
5722	11443	11444	\N	1908	7
5723	11445	11446	\N	1908	8
5724	11447	11448	\N	1908	9
5725	11449	11450	\N	1909	7
5726	11451	11452	\N	1909	8
5727	11453	11454	\N	1909	9
5728	11455	11456	\N	1910	7
5729	11457	11458	\N	1910	8
5730	11459	11460	\N	1910	9
5731	11461	11462	\N	1911	7
5732	11463	11464	\N	1911	8
5733	11465	11466	\N	1911	9
5734	11467	11468	\N	1912	7
5735	11469	11470	\N	1912	8
5736	11471	11472	\N	1912	9
5737	11473	11474	\N	1913	7
5738	11475	11476	\N	1913	8
5739	11477	11478	\N	1913	9
5740	11479	11480	\N	1914	7
5741	11481	11482	\N	1914	8
5742	11483	11484	\N	1914	9
5743	11485	11486	\N	1915	7
5744	11487	11488	\N	1915	8
5745	11489	11490	\N	1915	9
5746	11491	11492	\N	1916	7
5747	11493	11494	\N	1916	8
5748	11495	11496	\N	1916	9
5749	11497	11498	\N	1917	7
5750	11499	11500	\N	1917	8
5751	11501	11502	\N	1917	9
5752	11503	11504	\N	1918	7
5753	11505	11506	\N	1918	8
5754	11507	11508	\N	1918	9
5755	11509	11510	\N	1919	7
5756	11511	11512	\N	1919	8
5757	11513	11514	\N	1919	9
5758	11515	11516	\N	1920	7
5759	11517	11518	\N	1920	8
5760	11519	11520	\N	1920	9
5761	11521	11522	\N	1921	7
5762	11523	11524	\N	1921	8
5763	11525	11526	\N	1921	9
5764	11527	11528	\N	1922	7
5765	11529	11530	\N	1922	8
5766	11531	11532	\N	1922	9
5767	11533	11534	\N	1923	7
5768	11535	11536	\N	1923	8
5769	11537	11538	\N	1923	9
5770	11539	11540	\N	1924	7
5771	11541	11542	\N	1924	8
5772	11543	11544	\N	1924	9
5773	11545	11546	\N	1925	7
5774	11547	11548	\N	1925	8
5775	11549	11550	\N	1925	9
5776	11551	11552	\N	1926	7
5777	11553	11554	\N	1926	8
5778	11555	11556	\N	1926	9
5779	11557	11558	\N	1927	7
5780	11559	11560	\N	1927	8
5781	11561	11562	\N	1927	9
5782	11563	11564	\N	1928	7
5783	11565	11566	\N	1928	8
5784	11567	11568	\N	1928	9
5785	11569	11570	\N	1929	7
5786	11571	11572	\N	1929	8
5787	11573	11574	\N	1929	9
5788	11575	11576	\N	1930	7
5789	11577	11578	\N	1930	8
5790	11579	11580	\N	1930	9
5791	11581	11582	\N	1931	7
5792	11583	11584	\N	1931	8
5793	11585	11586	\N	1931	9
5794	11587	11588	\N	1932	7
5795	11589	11590	\N	1932	8
5796	11591	11592	\N	1932	9
5797	11593	11594	\N	1933	7
5798	11595	11596	\N	1933	8
5799	11597	11598	\N	1933	9
5800	11599	11600	\N	1934	7
5801	11601	11602	\N	1934	8
5802	11603	11604	\N	1934	9
5803	11605	11606	\N	1935	7
5804	11607	11608	\N	1935	8
5805	11609	11610	\N	1935	9
5806	11611	11612	\N	1936	7
5807	11613	11614	\N	1936	8
5808	11615	11616	\N	1936	9
5809	11617	11618	\N	1937	7
5810	11619	11620	\N	1937	8
5811	11621	11622	\N	1937	9
5812	11623	11624	\N	1938	7
5813	11625	11626	\N	1938	8
5814	11627	11628	\N	1938	9
5815	11629	11630	\N	1939	7
5816	11631	11632	\N	1939	8
5817	11633	11634	\N	1939	9
5818	11635	11636	\N	1940	7
5819	11637	11638	\N	1940	8
5820	11639	11640	\N	1940	9
5821	11641	11642	\N	1941	7
5822	11643	11644	\N	1941	8
5823	11645	11646	\N	1941	9
5824	11647	11648	\N	1942	7
5825	11649	11650	\N	1942	8
5826	11651	11652	\N	1942	9
5827	11653	11654	\N	1943	7
5828	11655	11656	\N	1943	8
5829	11657	11658	\N	1943	9
5830	11659	11660	\N	1944	7
5831	11661	11662	\N	1944	8
5832	11663	11664	\N	1944	9
5833	11665	11666	\N	1945	7
5834	11667	11668	\N	1945	8
5835	11669	11670	\N	1945	9
5836	11671	11672	\N	1946	7
5837	11673	11674	\N	1946	8
5838	11675	11676	\N	1946	9
5839	11677	11678	\N	1947	7
5840	11679	11680	\N	1947	8
5841	11681	11682	\N	1947	9
5842	11683	11684	\N	1948	7
5843	11685	11686	\N	1948	8
5844	11687	11688	\N	1948	9
5845	11689	11690	\N	1949	7
5846	11691	11692	\N	1949	8
5847	11693	11694	\N	1949	9
5848	11695	11696	\N	1950	7
5849	11697	11698	\N	1950	8
5850	11699	11700	\N	1950	9
5851	11701	11702	\N	1951	7
5852	11703	11704	\N	1951	8
5853	11705	11706	\N	1951	9
5854	11707	11708	\N	1952	7
5855	11709	11710	\N	1952	8
5856	11711	11712	\N	1952	9
5857	11713	11714	\N	1953	7
5858	11715	11716	\N	1953	8
5859	11717	11718	\N	1953	9
5860	11719	11720	\N	1954	7
5861	11721	11722	\N	1954	8
5862	11723	11724	\N	1954	9
5863	11725	11726	\N	1955	7
5864	11727	11728	\N	1955	8
5865	11729	11730	\N	1955	9
5866	11731	11732	\N	1956	7
5867	11733	11734	\N	1956	8
5868	11735	11736	\N	1956	9
5869	11737	11738	\N	1957	7
5870	11739	11740	\N	1957	8
5871	11741	11742	\N	1957	9
5872	11743	11744	\N	1958	7
5873	11745	11746	\N	1958	8
5874	11747	11748	\N	1958	9
5875	11749	11750	\N	1959	7
5876	11751	11752	\N	1959	8
5877	11753	11754	\N	1959	9
5878	11755	11756	\N	1960	7
5879	11757	11758	\N	1960	8
5880	11759	11760	\N	1960	9
5881	11761	11762	\N	1961	7
5882	11763	11764	\N	1961	8
5883	11765	11766	\N	1961	9
5884	11767	11768	\N	1962	7
5885	11769	11770	\N	1962	8
5886	11771	11772	\N	1962	9
5887	11773	11774	\N	1963	7
5888	11775	11776	\N	1963	8
5889	11777	11778	\N	1963	9
5890	11779	11780	\N	1964	7
5891	11781	11782	\N	1964	8
5892	11783	11784	\N	1964	9
5893	11785	11786	\N	1965	7
5894	11787	11788	\N	1965	8
5895	11789	11790	\N	1965	9
5896	11791	11792	\N	1966	7
5897	11793	11794	\N	1966	8
5898	11795	11796	\N	1966	9
5899	11797	11798	\N	1967	7
5900	11799	11800	\N	1967	8
5901	11801	11802	\N	1967	9
5902	11803	11804	\N	1968	7
5903	11805	11806	\N	1968	8
5904	11807	11808	\N	1968	9
5905	11809	11810	\N	1969	7
5906	11811	11812	\N	1969	8
5907	11813	11814	\N	1969	9
5908	11815	11816	\N	1970	7
5909	11817	11818	\N	1970	8
5910	11819	11820	\N	1970	9
5911	11821	11822	\N	1971	7
5912	11823	11824	\N	1971	8
5913	11825	11826	\N	1971	9
5914	11827	11828	\N	1972	7
5915	11829	11830	\N	1972	8
5916	11831	11832	\N	1972	9
5917	11833	11834	\N	1973	7
5918	11835	11836	\N	1973	8
5919	11837	11838	\N	1973	9
5920	11839	11840	\N	1974	7
5921	11841	11842	\N	1974	8
5922	11843	11844	\N	1974	9
5923	11845	11846	\N	1975	7
5924	11847	11848	\N	1975	8
5925	11849	11850	\N	1975	9
5926	11851	11852	\N	1976	7
5927	11853	11854	\N	1976	8
5928	11855	11856	\N	1976	9
5929	11857	11858	\N	1977	7
5930	11859	11860	\N	1977	8
5931	11861	11862	\N	1977	9
5932	11863	11864	\N	1978	7
5933	11865	11866	\N	1978	8
5934	11867	11868	\N	1978	9
5935	11869	11870	\N	1979	7
5936	11871	11872	\N	1979	8
5937	11873	11874	\N	1979	9
5938	11875	11876	\N	1980	7
5939	11877	11878	\N	1980	8
5940	11879	11880	\N	1980	9
5941	11881	11882	\N	1981	7
5942	11883	11884	\N	1981	8
5943	11885	11886	\N	1981	9
5944	11887	11888	\N	1982	7
5945	11889	11890	\N	1982	8
5946	11891	11892	\N	1982	9
5947	11893	11894	\N	1983	7
5948	11895	11896	\N	1983	8
5949	11897	11898	\N	1983	9
5950	11899	11900	\N	1984	7
5951	11901	11902	\N	1984	8
5952	11903	11904	\N	1984	9
5953	11905	11906	\N	1985	7
5954	11907	11908	\N	1985	8
5955	11909	11910	\N	1985	9
5956	11911	11912	\N	1986	7
5957	11913	11914	\N	1986	8
5958	11915	11916	\N	1986	9
5959	11917	11918	\N	1987	7
5960	11919	11920	\N	1987	8
5961	11921	11922	\N	1987	9
5962	11923	11924	\N	1988	7
5963	11925	11926	\N	1988	8
5964	11927	11928	\N	1988	9
5965	11929	11930	\N	1989	7
5966	11931	11932	\N	1989	8
5967	11933	11934	\N	1989	9
5968	11935	11936	\N	1990	7
5969	11937	11938	\N	1990	8
5970	11939	11940	\N	1990	9
5971	11941	11942	\N	1991	7
5972	11943	11944	\N	1991	8
5973	11945	11946	\N	1991	9
5974	11947	11948	\N	1992	7
5975	11949	11950	\N	1992	8
5976	11951	11952	\N	1992	9
5977	11953	11954	\N	1993	7
5978	11955	11956	\N	1993	8
5979	11957	11958	\N	1993	9
5980	11959	11960	\N	1994	7
5981	11961	11962	\N	1994	8
5982	11963	11964	\N	1994	9
5983	11965	11966	\N	1995	7
5984	11967	11968	\N	1995	8
5985	11969	11970	\N	1995	9
5986	11971	11972	\N	1996	7
5987	11973	11974	\N	1996	8
5988	11975	11976	\N	1996	9
5989	11977	11978	\N	1997	7
5990	11979	11980	\N	1997	8
5991	11981	11982	\N	1997	9
5992	11983	11984	\N	1998	7
5993	11985	11986	\N	1998	8
5994	11987	11988	\N	1998	9
5995	11989	11990	\N	1999	7
5996	11991	11992	\N	1999	8
5997	11993	11994	\N	1999	9
5998	11995	11996	\N	2000	7
5999	11997	11998	\N	2000	8
6000	11999	12000	\N	2000	9
6001	12001	12002	\N	2001	7
6002	12003	12004	\N	2001	8
6003	12005	12006	\N	2001	9
6004	12007	12008	\N	2002	7
6005	12009	12010	\N	2002	8
6006	12011	12012	\N	2002	9
6007	12013	12014	\N	2003	7
6008	12015	12016	\N	2003	8
6009	12017	12018	\N	2003	9
6010	12019	12020	\N	2004	7
6011	12021	12022	\N	2004	8
6012	12023	12024	\N	2004	9
6013	12025	12026	\N	2005	7
6014	12027	12028	\N	2005	8
6015	12029	12030	\N	2005	9
6016	12031	12032	\N	2006	7
6017	12033	12034	\N	2006	8
6018	12035	12036	\N	2006	9
6019	12037	12038	\N	2007	7
6020	12039	12040	\N	2007	8
6021	12041	12042	\N	2007	9
6022	12043	12044	\N	2008	7
6023	12045	12046	\N	2008	8
6024	12047	12048	\N	2008	9
6025	12049	12050	\N	2009	7
6026	12051	12052	\N	2009	8
6027	12053	12054	\N	2009	9
6028	12055	12056	\N	2010	7
6029	12057	12058	\N	2010	8
6030	12059	12060	\N	2010	9
6031	12061	12062	\N	2011	7
6032	12063	12064	\N	2011	8
6033	12065	12066	\N	2011	9
6034	12067	12068	\N	2012	7
6035	12069	12070	\N	2012	8
6036	12071	12072	\N	2012	9
6037	12073	12074	\N	2013	7
6038	12075	12076	\N	2013	8
6039	12077	12078	\N	2013	9
6040	12079	12080	\N	2014	7
6041	12081	12082	\N	2014	8
6042	12083	12084	\N	2014	9
6043	12085	12086	\N	2015	7
6044	12087	12088	\N	2015	8
6045	12089	12090	\N	2015	9
6046	12091	12092	\N	2016	7
6047	12093	12094	\N	2016	8
6048	12095	12096	\N	2016	9
6049	12097	12098	\N	2017	7
6050	12099	12100	\N	2017	8
6051	12101	12102	\N	2017	9
6052	12103	12104	\N	2018	7
6053	12105	12106	\N	2018	8
6054	12107	12108	\N	2018	9
6055	12109	12110	\N	2019	7
6056	12111	12112	\N	2019	8
6057	12113	12114	\N	2019	9
6058	12115	12116	\N	2020	7
6059	12117	12118	\N	2020	8
6060	12119	12120	\N	2020	9
6061	12121	12122	\N	2021	7
6062	12123	12124	\N	2021	8
6063	12125	12126	\N	2021	9
6064	12127	12128	\N	2022	7
6065	12129	12130	\N	2022	8
6066	12131	12132	\N	2022	9
6067	12133	12134	\N	2023	7
6068	12135	12136	\N	2023	8
6069	12137	12138	\N	2023	9
6070	12139	12140	\N	2024	7
6071	12141	12142	\N	2024	8
6072	12143	12144	\N	2024	9
6073	12145	12146	\N	2025	7
6074	12147	12148	\N	2025	8
6075	12149	12150	\N	2025	9
6076	12151	12152	\N	2026	7
6077	12153	12154	\N	2026	8
6078	12155	12156	\N	2026	9
6079	12157	12158	\N	2027	7
6080	12159	12160	\N	2027	8
6081	12161	12162	\N	2027	9
6082	12163	12164	\N	2028	7
6083	12165	12166	\N	2028	8
6084	12167	12168	\N	2028	9
6085	12169	12170	\N	2029	7
6086	12171	12172	\N	2029	8
6087	12173	12174	\N	2029	9
6088	12175	12176	\N	2030	7
6089	12177	12178	\N	2030	8
6090	12179	12180	\N	2030	9
6091	12181	12182	\N	2031	7
6092	12183	12184	\N	2031	8
6093	12185	12186	\N	2031	9
6094	12187	12188	\N	2032	7
6095	12189	12190	\N	2032	8
6096	12191	12192	\N	2032	9
6097	12193	12194	\N	2033	7
6098	12195	12196	\N	2033	8
6099	12197	12198	\N	2033	9
6100	12199	12200	\N	2034	7
6101	12201	12202	\N	2034	8
6102	12203	12204	\N	2034	9
6103	12205	12206	\N	2035	7
6104	12207	12208	\N	2035	8
6105	12209	12210	\N	2035	9
6106	12211	12212	\N	2036	7
6107	12213	12214	\N	2036	8
6108	12215	12216	\N	2036	9
6109	12217	12218	\N	2037	7
6110	12219	12220	\N	2037	8
6111	12221	12222	\N	2037	9
6112	12223	12224	\N	2038	7
6113	12225	12226	\N	2038	8
6114	12227	12228	\N	2038	9
6115	12229	12230	\N	2039	7
6116	12231	12232	\N	2039	8
6117	12233	12234	\N	2039	9
6118	12235	12236	\N	2040	7
6119	12237	12238	\N	2040	8
6120	12239	12240	\N	2040	9
6121	12241	12242	\N	2041	7
6122	12243	12244	\N	2041	8
6123	12245	12246	\N	2041	9
6124	12247	12248	\N	2042	7
6125	12249	12250	\N	2042	8
6126	12251	12252	\N	2042	9
6127	12253	12254	\N	2043	7
6128	12255	12256	\N	2043	8
6129	12257	12258	\N	2043	9
6130	12259	12260	\N	2044	7
6131	12261	12262	\N	2044	8
6132	12263	12264	\N	2044	9
6133	12265	12266	\N	2045	7
6134	12267	12268	\N	2045	8
6135	12269	12270	\N	2045	9
6136	12271	12272	\N	2046	7
6137	12273	12274	\N	2046	8
6138	12275	12276	\N	2046	9
6139	12277	12278	\N	2047	7
6140	12279	12280	\N	2047	8
6141	12281	12282	\N	2047	9
6142	12283	12284	\N	2048	7
6143	12285	12286	\N	2048	8
6144	12287	12288	\N	2048	9
6145	12289	12290	\N	2049	7
6146	12291	12292	\N	2049	8
6147	12293	12294	\N	2049	9
6148	12295	12296	\N	2050	7
6149	12297	12298	\N	2050	8
6150	12299	12300	\N	2050	9
6151	12301	12302	\N	2051	7
6152	12303	12304	\N	2051	8
6153	12305	12306	\N	2051	9
6154	12307	12308	\N	2052	7
6155	12309	12310	\N	2052	8
6156	12311	12312	\N	2052	9
6157	12313	12314	\N	2053	7
6158	12315	12316	\N	2053	8
6159	12317	12318	\N	2053	9
6160	12319	12320	\N	2054	7
6161	12321	12322	\N	2054	8
6162	12323	12324	\N	2054	9
6163	12325	12326	\N	2055	7
6164	12327	12328	\N	2055	8
6165	12329	12330	\N	2055	9
6166	12331	12332	\N	2056	7
6167	12333	12334	\N	2056	8
6168	12335	12336	\N	2056	9
6169	12337	12338	\N	2057	7
6170	12339	12340	\N	2057	8
6171	12341	12342	\N	2057	9
6172	12343	12344	\N	2058	7
6173	12345	12346	\N	2058	8
6174	12347	12348	\N	2058	9
6175	12349	12350	\N	2059	7
6176	12351	12352	\N	2059	8
6177	12353	12354	\N	2059	9
6178	12355	12356	\N	2060	7
6179	12357	12358	\N	2060	8
6180	12359	12360	\N	2060	9
6181	12361	12362	\N	2061	7
6182	12363	12364	\N	2061	8
6183	12365	12366	\N	2061	9
6184	12367	12368	\N	2062	7
6185	12369	12370	\N	2062	8
6186	12371	12372	\N	2062	9
6187	12373	12374	\N	2063	7
6188	12375	12376	\N	2063	8
6189	12377	12378	\N	2063	9
6190	12379	12380	\N	2064	7
6191	12381	12382	\N	2064	8
6192	12383	12384	\N	2064	9
6193	12385	12386	\N	2065	7
6194	12387	12388	\N	2065	8
6195	12389	12390	\N	2065	9
6196	12391	12392	\N	2066	7
6197	12393	12394	\N	2066	8
6198	12395	12396	\N	2066	9
6199	12397	12398	\N	2067	7
6200	12399	12400	\N	2067	8
6201	12401	12402	\N	2067	9
6202	12403	12404	\N	2068	7
6203	12405	12406	\N	2068	8
6204	12407	12408	\N	2068	9
6205	12409	12410	\N	2069	7
6206	12411	12412	\N	2069	8
6207	12413	12414	\N	2069	9
6208	12415	12416	\N	2070	7
6209	12417	12418	\N	2070	8
6210	12419	12420	\N	2070	9
6211	12421	12422	\N	2071	7
6212	12423	12424	\N	2071	8
6213	12425	12426	\N	2071	9
6214	12427	12428	\N	2072	7
6215	12429	12430	\N	2072	8
6216	12431	12432	\N	2072	9
6217	12433	12434	\N	2073	7
6218	12435	12436	\N	2073	8
6219	12437	12438	\N	2073	9
6220	12439	12440	\N	2074	7
6221	12441	12442	\N	2074	8
6222	12443	12444	\N	2074	9
6223	12445	12446	\N	2075	7
6224	12447	12448	\N	2075	8
6225	12449	12450	\N	2075	9
6226	12451	12452	\N	2076	7
6227	12453	12454	\N	2076	8
6228	12455	12456	\N	2076	9
6229	12457	12458	\N	2077	7
6230	12459	12460	\N	2077	8
6231	12461	12462	\N	2077	9
6232	12463	12464	\N	2078	7
6233	12465	12466	\N	2078	8
6234	12467	12468	\N	2078	9
6235	12469	12470	\N	2079	7
6236	12471	12472	\N	2079	8
6237	12473	12474	\N	2079	9
6238	12475	12476	\N	2080	7
6239	12477	12478	\N	2080	8
6240	12479	12480	\N	2080	9
6241	12481	12482	\N	2081	7
6242	12483	12484	\N	2081	8
6243	12485	12486	\N	2081	9
6244	12487	12488	\N	2082	7
6245	12489	12490	\N	2082	8
6246	12491	12492	\N	2082	9
6247	12493	12494	\N	2083	7
6248	12495	12496	\N	2083	8
6249	12497	12498	\N	2083	9
6250	12499	12500	\N	2084	7
6251	12501	12502	\N	2084	8
6252	12503	12504	\N	2084	9
6253	12505	12506	\N	2085	7
6254	12507	12508	\N	2085	8
6255	12509	12510	\N	2085	9
6256	12511	12512	\N	2086	7
6257	12513	12514	\N	2086	8
6258	12515	12516	\N	2086	9
6259	12517	12518	\N	2087	7
6260	12519	12520	\N	2087	8
6261	12521	12522	\N	2087	9
6262	12523	12524	\N	2088	7
6263	12525	12526	\N	2088	8
6264	12527	12528	\N	2088	9
6265	12529	12530	\N	2089	7
6266	12531	12532	\N	2089	8
6267	12533	12534	\N	2089	9
6268	12535	12536	\N	2090	7
6269	12537	12538	\N	2090	8
6270	12539	12540	\N	2090	9
6271	12541	12542	\N	2091	7
6272	12543	12544	\N	2091	8
6273	12545	12546	\N	2091	9
6274	12547	12548	\N	2092	7
6275	12549	12550	\N	2092	8
6276	12551	12552	\N	2092	9
6277	12553	12554	\N	2093	7
6278	12555	12556	\N	2093	8
6279	12557	12558	\N	2093	9
6280	12559	12560	\N	2094	7
6281	12561	12562	\N	2094	8
6282	12563	12564	\N	2094	9
6283	12565	12566	\N	2095	7
6284	12567	12568	\N	2095	8
6285	12569	12570	\N	2095	9
6286	12571	12572	\N	2096	7
6287	12573	12574	\N	2096	8
6288	12575	12576	\N	2096	9
6289	12577	12578	\N	2097	7
6290	12579	12580	\N	2097	8
6291	12581	12582	\N	2097	9
6292	12583	12584	\N	2098	7
6293	12585	12586	\N	2098	8
6294	12587	12588	\N	2098	9
6295	12589	12590	\N	2099	7
6296	12591	12592	\N	2099	8
6297	12593	12594	\N	2099	9
6298	12595	12596	\N	2100	7
6299	12597	12598	\N	2100	8
6300	12599	12600	\N	2100	9
6301	12601	12602	\N	2101	7
6302	12603	12604	\N	2101	8
6303	12605	12606	\N	2101	9
6304	12607	12608	\N	2102	7
6305	12609	12610	\N	2102	8
6306	12611	12612	\N	2102	9
6307	12613	12614	\N	2103	7
6308	12615	12616	\N	2103	8
6309	12617	12618	\N	2103	9
6310	12619	12620	\N	2104	7
6311	12621	12622	\N	2104	8
6312	12623	12624	\N	2104	9
6313	12625	12626	\N	2105	7
6314	12627	12628	\N	2105	8
6315	12629	12630	\N	2105	9
6316	12631	12632	\N	2106	7
6317	12633	12634	\N	2106	8
6318	12635	12636	\N	2106	9
6319	12637	12638	\N	2107	7
6320	12639	12640	\N	2107	8
6321	12641	12642	\N	2107	9
6322	12643	12644	\N	2108	7
6323	12645	12646	\N	2108	8
6324	12647	12648	\N	2108	9
6325	12649	12650	\N	2109	7
6326	12651	12652	\N	2109	8
6327	12653	12654	\N	2109	9
6328	12655	12656	\N	2110	7
6329	12657	12658	\N	2110	8
6330	12659	12660	\N	2110	9
6331	12661	12662	\N	2111	7
6332	12663	12664	\N	2111	8
6333	12665	12666	\N	2111	9
6334	12667	12668	\N	2112	7
6335	12669	12670	\N	2112	8
6336	12671	12672	\N	2112	9
6337	12673	12674	\N	2113	7
6338	12675	12676	\N	2113	8
6339	12677	12678	\N	2113	9
6340	12679	12680	\N	2114	7
6341	12681	12682	\N	2114	8
6342	12683	12684	\N	2114	9
6343	12685	12686	\N	2115	7
6344	12687	12688	\N	2115	8
6345	12689	12690	\N	2115	9
6346	12691	12692	\N	2116	7
6347	12693	12694	\N	2116	8
6348	12695	12696	\N	2116	9
6349	12697	12698	\N	2117	7
6350	12699	12700	\N	2117	8
6351	12701	12702	\N	2117	9
6352	12703	12704	\N	2118	7
6353	12705	12706	\N	2118	8
6354	12707	12708	\N	2118	9
6355	12709	12710	\N	2119	7
6356	12711	12712	\N	2119	8
6357	12713	12714	\N	2119	9
6358	12715	12716	\N	2120	7
6359	12717	12718	\N	2120	8
6360	12719	12720	\N	2120	9
6361	12721	12722	\N	2121	7
6362	12723	12724	\N	2121	8
6363	12725	12726	\N	2121	9
6364	12727	12728	\N	2122	7
6365	12729	12730	\N	2122	8
6366	12731	12732	\N	2122	9
6367	12733	12734	\N	2123	7
6368	12735	12736	\N	2123	8
6369	12737	12738	\N	2123	9
6370	12739	12740	\N	2124	7
6371	12741	12742	\N	2124	8
6372	12743	12744	\N	2124	9
6373	12745	12746	\N	2125	7
6374	12747	12748	\N	2125	8
6375	12749	12750	\N	2125	9
6376	12751	12752	\N	2126	7
6377	12753	12754	\N	2126	8
6378	12755	12756	\N	2126	9
6379	12757	12758	\N	2127	7
6380	12759	12760	\N	2127	8
6381	12761	12762	\N	2127	9
6382	12763	12764	\N	2128	7
6383	12765	12766	\N	2128	8
6384	12767	12768	\N	2128	9
6385	12769	12770	\N	2129	7
6386	12771	12772	\N	2129	8
6387	12773	12774	\N	2129	9
6388	12775	12776	\N	2130	7
6389	12777	12778	\N	2130	8
6390	12779	12780	\N	2130	9
6391	12781	12782	\N	2131	7
6392	12783	12784	\N	2131	8
6393	12785	12786	\N	2131	9
6394	12787	12788	\N	2132	7
6395	12789	12790	\N	2132	8
6396	12791	12792	\N	2132	9
6397	12793	12794	\N	2133	7
6398	12795	12796	\N	2133	8
6399	12797	12798	\N	2133	9
6400	12799	12800	\N	2134	7
6401	12801	12802	\N	2134	8
6402	12803	12804	\N	2134	9
6403	12805	12806	\N	2135	7
6404	12807	12808	\N	2135	8
6405	12809	12810	\N	2135	9
6406	12811	12812	\N	2136	7
6407	12813	12814	\N	2136	8
6408	12815	12816	\N	2136	9
6409	12817	12818	\N	2137	7
6410	12819	12820	\N	2137	8
6411	12821	12822	\N	2137	9
6412	12823	12824	\N	2138	7
6413	12825	12826	\N	2138	8
6414	12827	12828	\N	2138	9
6415	12829	12830	\N	2139	7
6416	12831	12832	\N	2139	8
6417	12833	12834	\N	2139	9
6418	12835	12836	\N	2140	7
6419	12837	12838	\N	2140	8
6420	12839	12840	\N	2140	9
6421	12841	12842	\N	2141	7
6422	12843	12844	\N	2141	8
6423	12845	12846	\N	2141	9
6424	12847	12848	\N	2142	7
6425	12849	12850	\N	2142	8
6426	12851	12852	\N	2142	9
6427	12853	12854	\N	2143	7
6428	12855	12856	\N	2143	8
6429	12857	12858	\N	2143	9
6430	12859	12860	\N	2144	7
6431	12861	12862	\N	2144	8
6432	12863	12864	\N	2144	9
6433	12865	12866	\N	2145	7
6434	12867	12868	\N	2145	8
6435	12869	12870	\N	2145	9
6436	12871	12872	\N	2146	7
6437	12873	12874	\N	2146	8
6438	12875	12876	\N	2146	9
6439	12877	12878	\N	2147	7
6440	12879	12880	\N	2147	8
6441	12881	12882	\N	2147	9
6442	12883	12884	\N	2148	7
6443	12885	12886	\N	2148	8
6444	12887	12888	\N	2148	9
6445	12889	12890	\N	2149	7
6446	12891	12892	\N	2149	8
6447	12893	12894	\N	2149	9
6448	12895	12896	\N	2150	7
6449	12897	12898	\N	2150	8
6450	12899	12900	\N	2150	9
6451	12901	12902	\N	2151	7
6452	12903	12904	\N	2151	8
6453	12905	12906	\N	2151	9
6454	12907	12908	\N	2152	7
6455	12909	12910	\N	2152	8
6456	12911	12912	\N	2152	9
6457	12913	12914	\N	2153	7
6458	12915	12916	\N	2153	8
6459	12917	12918	\N	2153	9
6460	12919	12920	\N	2154	7
6461	12921	12922	\N	2154	8
6462	12923	12924	\N	2154	9
6463	12925	12926	\N	2155	7
6464	12927	12928	\N	2155	8
6465	12929	12930	\N	2155	9
6466	12931	12932	\N	2156	7
6467	12933	12934	\N	2156	8
6468	12935	12936	\N	2156	9
6469	12937	12938	\N	2157	7
6470	12939	12940	\N	2157	8
6471	12941	12942	\N	2157	9
6472	12943	12944	\N	2158	7
6473	12945	12946	\N	2158	8
6474	12947	12948	\N	2158	9
6475	12949	12950	\N	2159	7
6476	12951	12952	\N	2159	8
6477	12953	12954	\N	2159	9
6478	12955	12956	\N	2160	7
6479	12957	12958	\N	2160	8
6480	12959	12960	\N	2160	9
6481	12961	12962	\N	2161	7
6482	12963	12964	\N	2161	8
6483	12965	12966	\N	2161	9
6484	12967	12968	\N	2162	7
6485	12969	12970	\N	2162	8
6486	12971	12972	\N	2162	9
6487	12973	12974	\N	2163	7
6488	12975	12976	\N	2163	8
6489	12977	12978	\N	2163	9
6490	12979	12980	\N	2164	7
6491	12981	12982	\N	2164	8
6492	12983	12984	\N	2164	9
6493	12985	12986	\N	2165	7
6494	12987	12988	\N	2165	8
6495	12989	12990	\N	2165	9
6496	12991	12992	\N	2166	7
6497	12993	12994	\N	2166	8
6498	12995	12996	\N	2166	9
6499	12997	12998	\N	2167	7
6500	12999	13000	\N	2167	8
6501	13001	13002	\N	2167	9
6502	13003	13004	\N	2168	7
6503	13005	13006	\N	2168	8
6504	13007	13008	\N	2168	9
6505	13009	13010	\N	2169	7
6506	13011	13012	\N	2169	8
6507	13013	13014	\N	2169	9
6508	13015	13016	\N	2170	7
6509	13017	13018	\N	2170	8
6510	13019	13020	\N	2170	9
6511	13021	13022	\N	2171	7
6512	13023	13024	\N	2171	8
6513	13025	13026	\N	2171	9
6514	13027	13028	\N	2172	7
6515	13029	13030	\N	2172	8
6516	13031	13032	\N	2172	9
6517	13033	13034	\N	2173	7
6518	13035	13036	\N	2173	8
6519	13037	13038	\N	2173	9
6520	13039	13040	\N	2174	7
6521	13041	13042	\N	2174	8
6522	13043	13044	\N	2174	9
6523	13045	13046	\N	2175	7
6524	13047	13048	\N	2175	8
6525	13049	13050	\N	2175	9
6526	13051	13052	\N	2176	7
6527	13053	13054	\N	2176	8
6528	13055	13056	\N	2176	9
6529	13057	13058	\N	2177	7
6530	13059	13060	\N	2177	8
6531	13061	13062	\N	2177	9
6532	13063	13064	\N	2178	7
6533	13065	13066	\N	2178	8
6534	13067	13068	\N	2178	9
6535	13069	13070	\N	2179	7
6536	13071	13072	\N	2179	8
6537	13073	13074	\N	2179	9
6538	13075	13076	\N	2180	7
6539	13077	13078	\N	2180	8
6540	13079	13080	\N	2180	9
6541	13081	13082	\N	2181	7
6542	13083	13084	\N	2181	8
6543	13085	13086	\N	2181	9
6544	13087	13088	\N	2182	7
6545	13089	13090	\N	2182	8
6546	13091	13092	\N	2182	9
6547	13093	13094	\N	2183	7
6548	13095	13096	\N	2183	8
6549	13097	13098	\N	2183	9
6550	13099	13100	\N	2184	7
6551	13101	13102	\N	2184	8
6552	13103	13104	\N	2184	9
6553	13105	13106	\N	2185	7
6554	13107	13108	\N	2185	8
6555	13109	13110	\N	2185	9
6556	13111	13112	\N	2186	7
6557	13113	13114	\N	2186	8
6558	13115	13116	\N	2186	9
6559	13117	13118	\N	2187	7
6560	13119	13120	\N	2187	8
6561	13121	13122	\N	2187	9
6562	13123	13124	\N	2188	7
6563	13125	13126	\N	2188	8
6564	13127	13128	\N	2188	9
6565	13129	13130	\N	2189	7
6566	13131	13132	\N	2189	8
6567	13133	13134	\N	2189	9
6568	13135	13136	\N	2190	7
6569	13137	13138	\N	2190	8
6570	13139	13140	\N	2190	9
6571	13141	13142	\N	2191	7
6572	13143	13144	\N	2191	8
6573	13145	13146	\N	2191	9
6574	13147	13148	\N	2192	7
6575	13149	13150	\N	2192	8
6576	13151	13152	\N	2192	9
6577	13153	13154	\N	2193	7
6578	13155	13156	\N	2193	8
6579	13157	13158	\N	2193	9
6580	13159	13160	\N	2194	7
6581	13161	13162	\N	2194	8
6582	13163	13164	\N	2194	9
6583	13165	13166	\N	2195	7
6584	13167	13168	\N	2195	8
6585	13169	13170	\N	2195	9
6586	13171	13172	\N	2196	7
6587	13173	13174	\N	2196	8
6588	13175	13176	\N	2196	9
6589	13177	13178	\N	2197	7
6590	13179	13180	\N	2197	8
6591	13181	13182	\N	2197	9
6592	13183	13184	\N	2198	7
6593	13185	13186	\N	2198	8
6594	13187	13188	\N	2198	9
6595	13189	13190	\N	2199	7
6596	13191	13192	\N	2199	8
6597	13193	13194	\N	2199	9
6598	13195	13196	\N	2200	7
6599	13197	13198	\N	2200	8
6600	13199	13200	\N	2200	9
6601	13201	13202	\N	2201	7
6602	13203	13204	\N	2201	8
6603	13205	13206	\N	2201	9
6604	13207	13208	\N	2202	7
6605	13209	13210	\N	2202	8
6606	13211	13212	\N	2202	9
6607	13213	13214	\N	2203	7
6608	13215	13216	\N	2203	8
6609	13217	13218	\N	2203	9
6610	13219	13220	\N	2204	7
6611	13221	13222	\N	2204	8
6612	13223	13224	\N	2204	9
6613	13225	13226	\N	2205	7
6614	13227	13228	\N	2205	8
6615	13229	13230	\N	2205	9
6616	13231	13232	\N	2206	7
6617	13233	13234	\N	2206	8
6618	13235	13236	\N	2206	9
6619	13237	13238	\N	2207	7
6620	13239	13240	\N	2207	8
6621	13241	13242	\N	2207	9
6622	13243	13244	\N	2208	7
6623	13245	13246	\N	2208	8
6624	13247	13248	\N	2208	9
6625	13249	13250	\N	2209	7
6626	13251	13252	\N	2209	8
6627	13253	13254	\N	2209	9
6628	13255	13256	\N	2210	7
6629	13257	13258	\N	2210	8
6630	13259	13260	\N	2210	9
6631	13261	13262	\N	2211	7
6632	13263	13264	\N	2211	8
6633	13265	13266	\N	2211	9
6634	13267	13268	\N	2212	7
6635	13269	13270	\N	2212	8
6636	13271	13272	\N	2212	9
6637	13273	13274	\N	2213	7
6638	13275	13276	\N	2213	8
6639	13277	13278	\N	2213	9
6640	13279	13280	\N	2214	7
6641	13281	13282	\N	2214	8
6642	13283	13284	\N	2214	9
6643	13285	13286	\N	2215	7
6644	13287	13288	\N	2215	8
6645	13289	13290	\N	2215	9
6646	13291	13292	\N	2216	7
6647	13293	13294	\N	2216	8
6648	13295	13296	\N	2216	9
6649	13297	13298	\N	2217	7
6650	13299	13300	\N	2217	8
6651	13301	13302	\N	2217	9
6652	13303	13304	\N	2218	7
6653	13305	13306	\N	2218	8
6654	13307	13308	\N	2218	9
6655	13309	13310	\N	2219	7
6656	13311	13312	\N	2219	8
6657	13313	13314	\N	2219	9
6658	13315	13316	\N	2220	7
6659	13317	13318	\N	2220	8
6660	13319	13320	\N	2220	9
6661	13321	13322	\N	2221	7
6662	13323	13324	\N	2221	8
6663	13325	13326	\N	2221	9
6664	13327	13328	\N	2222	7
6665	13329	13330	\N	2222	8
6666	13331	13332	\N	2222	9
6667	13333	13334	\N	2223	7
6668	13335	13336	\N	2223	8
6669	13337	13338	\N	2223	9
6670	13339	13340	\N	2224	7
6671	13341	13342	\N	2224	8
6672	13343	13344	\N	2224	9
6673	13345	13346	\N	2225	7
6674	13347	13348	\N	2225	8
6675	13349	13350	\N	2225	9
6676	13351	13352	\N	2226	7
6677	13353	13354	\N	2226	8
6678	13355	13356	\N	2226	9
6679	13357	13358	\N	2227	7
6680	13359	13360	\N	2227	8
6681	13361	13362	\N	2227	9
6682	13363	13364	\N	2228	7
6683	13365	13366	\N	2228	8
6684	13367	13368	\N	2228	9
6685	13369	13370	\N	2229	7
6686	13371	13372	\N	2229	8
6687	13373	13374	\N	2229	9
6688	13375	13376	\N	2230	7
6689	13377	13378	\N	2230	8
6690	13379	13380	\N	2230	9
6691	13381	13382	\N	2231	7
6692	13383	13384	\N	2231	8
6693	13385	13386	\N	2231	9
6694	13387	13388	\N	2232	7
6695	13389	13390	\N	2232	8
6696	13391	13392	\N	2232	9
6697	13393	13394	\N	2233	7
6698	13395	13396	\N	2233	8
6699	13397	13398	\N	2233	9
6700	13399	13400	\N	2234	7
6701	13401	13402	\N	2234	8
6702	13403	13404	\N	2234	9
6703	13405	13406	\N	2235	7
6704	13407	13408	\N	2235	8
6705	13409	13410	\N	2235	9
6706	13411	13412	\N	2236	7
6707	13413	13414	\N	2236	8
6708	13415	13416	\N	2236	9
6709	13417	13418	\N	2237	7
6710	13419	13420	\N	2237	8
6711	13421	13422	\N	2237	9
6712	13423	13424	\N	2238	7
6713	13425	13426	\N	2238	8
6714	13427	13428	\N	2238	9
6715	13429	13430	\N	2239	7
6716	13431	13432	\N	2239	8
6717	13433	13434	\N	2239	9
6718	13435	13436	\N	2240	7
6719	13437	13438	\N	2240	8
6720	13439	13440	\N	2240	9
6721	13441	13442	\N	2241	7
6722	13443	13444	\N	2241	8
6723	13445	13446	\N	2241	9
6724	13447	13448	\N	2242	7
6725	13449	13450	\N	2242	8
6726	13451	13452	\N	2242	9
6727	13453	13454	\N	2243	7
6728	13455	13456	\N	2243	8
6729	13457	13458	\N	2243	9
6730	13459	13460	\N	2244	7
6731	13461	13462	\N	2244	8
6732	13463	13464	\N	2244	9
6733	13465	13466	\N	2245	7
6734	13467	13468	\N	2245	8
6735	13469	13470	\N	2245	9
6736	13471	13472	\N	2246	7
6737	13473	13474	\N	2246	8
6738	13475	13476	\N	2246	9
6739	13477	13478	\N	2247	7
6740	13479	13480	\N	2247	8
6741	13481	13482	\N	2247	9
6742	13483	13484	\N	2248	7
6743	13485	13486	\N	2248	8
6744	13487	13488	\N	2248	9
6745	13489	13490	\N	2249	7
6746	13491	13492	\N	2249	8
6747	13493	13494	\N	2249	9
6748	13495	13496	\N	2250	7
6749	13497	13498	\N	2250	8
6750	13499	13500	\N	2250	9
6751	13501	13502	\N	2251	7
6752	13503	13504	\N	2251	8
6753	13505	13506	\N	2251	9
6754	13507	13508	\N	2252	7
6755	13509	13510	\N	2252	8
6756	13511	13512	\N	2252	9
6757	13513	13514	\N	2253	7
6758	13515	13516	\N	2253	8
6759	13517	13518	\N	2253	9
6760	13519	13520	\N	2254	7
6761	13521	13522	\N	2254	8
6762	13523	13524	\N	2254	9
6763	13525	13526	\N	2255	7
6764	13527	13528	\N	2255	8
6765	13529	13530	\N	2255	9
6766	13531	13532	\N	2256	7
6767	13533	13534	\N	2256	8
6768	13535	13536	\N	2256	9
6769	13537	13538	\N	2257	7
6770	13539	13540	\N	2257	8
6771	13541	13542	\N	2257	9
6772	13543	13544	\N	2258	7
6773	13545	13546	\N	2258	8
6774	13547	13548	\N	2258	9
6775	13549	13550	\N	2259	7
6776	13551	13552	\N	2259	8
6777	13553	13554	\N	2259	9
6778	13555	13556	\N	2260	7
6779	13557	13558	\N	2260	8
6780	13559	13560	\N	2260	9
6781	13561	13562	\N	2261	7
6782	13563	13564	\N	2261	8
6783	13565	13566	\N	2261	9
6784	13567	13568	\N	2262	7
6785	13569	13570	\N	2262	8
6786	13571	13572	\N	2262	9
6787	13573	13574	\N	2263	7
6788	13575	13576	\N	2263	8
6789	13577	13578	\N	2263	9
6790	13579	13580	\N	2264	7
6791	13581	13582	\N	2264	8
6792	13583	13584	\N	2264	9
6793	13585	13586	\N	2265	7
6794	13587	13588	\N	2265	8
6795	13589	13590	\N	2265	9
6796	13591	13592	\N	2266	7
6797	13593	13594	\N	2266	8
6798	13595	13596	\N	2266	9
6799	13597	13598	\N	2267	7
6800	13599	13600	\N	2267	8
6801	13601	13602	\N	2267	9
6802	13603	13604	\N	2268	7
6803	13605	13606	\N	2268	8
6804	13607	13608	\N	2268	9
6805	13609	13610	\N	2269	7
6806	13611	13612	\N	2269	8
6807	13613	13614	\N	2269	9
6808	13615	13616	\N	2270	7
6809	13617	13618	\N	2270	8
6810	13619	13620	\N	2270	9
6811	13621	13622	\N	2271	7
6812	13623	13624	\N	2271	8
6813	13625	13626	\N	2271	9
6814	13627	13628	\N	2272	7
6815	13629	13630	\N	2272	8
6816	13631	13632	\N	2272	9
6817	13633	13634	\N	2273	7
6818	13635	13636	\N	2273	8
6819	13637	13638	\N	2273	9
6820	13639	13640	\N	2274	7
6821	13641	13642	\N	2274	8
6822	13643	13644	\N	2274	9
6823	13645	13646	\N	2275	7
6824	13647	13648	\N	2275	8
6825	13649	13650	\N	2275	9
6826	13651	13652	\N	2276	7
6827	13653	13654	\N	2276	8
6828	13655	13656	\N	2276	9
6829	13657	13658	\N	2277	7
6830	13659	13660	\N	2277	8
6831	13661	13662	\N	2277	9
6832	13663	13664	\N	2278	7
6833	13665	13666	\N	2278	8
6834	13667	13668	\N	2278	9
6835	13669	13670	\N	2279	7
6836	13671	13672	\N	2279	8
6837	13673	13674	\N	2279	9
6838	13675	13676	\N	2280	7
6839	13677	13678	\N	2280	8
6840	13679	13680	\N	2280	9
6841	13681	13682	\N	2281	7
6842	13683	13684	\N	2281	8
6843	13685	13686	\N	2281	9
6844	13687	13688	\N	2282	7
6845	13689	13690	\N	2282	8
6846	13691	13692	\N	2282	9
6847	13693	13694	\N	2283	7
6848	13695	13696	\N	2283	8
6849	13697	13698	\N	2283	9
6850	13699	13700	\N	2284	7
6851	13701	13702	\N	2284	8
6852	13703	13704	\N	2284	9
6853	13705	13706	\N	2285	7
6854	13707	13708	\N	2285	8
6855	13709	13710	\N	2285	9
6856	13711	13712	\N	2286	7
6857	13713	13714	\N	2286	8
6858	13715	13716	\N	2286	9
6859	13717	13718	\N	2287	7
6860	13719	13720	\N	2287	8
6861	13721	13722	\N	2287	9
6862	13723	13724	\N	2288	7
6863	13725	13726	\N	2288	8
6864	13727	13728	\N	2288	9
6865	13729	13730	\N	2289	7
6866	13731	13732	\N	2289	8
6867	13733	13734	\N	2289	9
6868	13735	13736	\N	2290	7
6869	13737	13738	\N	2290	8
6870	13739	13740	\N	2290	9
6871	13741	13742	\N	2291	7
6872	13743	13744	\N	2291	8
6873	13745	13746	\N	2291	9
6874	13747	13748	\N	2292	7
6875	13749	13750	\N	2292	8
6876	13751	13752	\N	2292	9
6877	13753	13754	\N	2293	7
6878	13755	13756	\N	2293	8
6879	13757	13758	\N	2293	9
6880	13759	13760	\N	2294	7
6881	13761	13762	\N	2294	8
6882	13763	13764	\N	2294	9
6883	13765	13766	\N	2295	7
6884	13767	13768	\N	2295	8
6885	13769	13770	\N	2295	9
6886	13771	13772	\N	2296	7
6887	13773	13774	\N	2296	8
6888	13775	13776	\N	2296	9
6889	13777	13778	\N	2297	7
6890	13779	13780	\N	2297	8
6891	13781	13782	\N	2297	9
6892	13783	13784	\N	2298	7
6893	13785	13786	\N	2298	8
6894	13787	13788	\N	2298	9
6895	13789	13790	\N	2299	7
6896	13791	13792	\N	2299	8
6897	13793	13794	\N	2299	9
6898	13795	13796	\N	2300	7
6899	13797	13798	\N	2300	8
6900	13799	13800	\N	2300	9
6901	13801	13802	\N	2301	7
6902	13803	13804	\N	2301	8
6903	13805	13806	\N	2301	9
6904	13807	13808	\N	2302	7
6905	13809	13810	\N	2302	8
6906	13811	13812	\N	2302	9
6907	13813	13814	\N	2303	7
6908	13815	13816	\N	2303	8
6909	13817	13818	\N	2303	9
6910	13819	13820	\N	2304	7
6911	13821	13822	\N	2304	8
6912	13823	13824	\N	2304	9
6913	13825	13826	\N	2305	7
6914	13827	13828	\N	2305	8
6915	13829	13830	\N	2305	9
6916	13831	13832	\N	2306	7
6917	13833	13834	\N	2306	8
6918	13835	13836	\N	2306	9
6919	13837	13838	\N	2307	7
6920	13839	13840	\N	2307	8
6921	13841	13842	\N	2307	9
6922	13843	13844	\N	2308	7
6923	13845	13846	\N	2308	8
6924	13847	13848	\N	2308	9
6925	13849	13850	\N	2309	7
6926	13851	13852	\N	2309	8
6927	13853	13854	\N	2309	9
6928	13855	13856	\N	2310	7
6929	13857	13858	\N	2310	8
6930	13859	13860	\N	2310	9
6931	13861	13862	\N	2311	7
6932	13863	13864	\N	2311	8
6933	13865	13866	\N	2311	9
6934	13867	13868	\N	2312	7
6935	13869	13870	\N	2312	8
6936	13871	13872	\N	2312	9
6937	13873	13874	\N	2313	7
6938	13875	13876	\N	2313	8
6939	13877	13878	\N	2313	9
6940	13879	13880	\N	2314	7
6941	13881	13882	\N	2314	8
6942	13883	13884	\N	2314	9
6943	13885	13886	\N	2315	7
6944	13887	13888	\N	2315	8
6945	13889	13890	\N	2315	9
6946	13891	13892	\N	2316	7
6947	13893	13894	\N	2316	8
6948	13895	13896	\N	2316	9
6949	13897	13898	\N	2317	7
6950	13899	13900	\N	2317	8
6951	13901	13902	\N	2317	9
6952	13903	13904	\N	2318	7
6953	13905	13906	\N	2318	8
6954	13907	13908	\N	2318	9
6955	13909	13910	\N	2319	7
6956	13911	13912	\N	2319	8
6957	13913	13914	\N	2319	9
6958	13915	13916	\N	2320	7
6959	13917	13918	\N	2320	8
6960	13919	13920	\N	2320	9
6961	13921	13922	\N	2321	7
6962	13923	13924	\N	2321	8
6963	13925	13926	\N	2321	9
6964	13927	13928	\N	2322	7
6965	13929	13930	\N	2322	8
6966	13931	13932	\N	2322	9
6967	13933	13934	\N	2323	7
6968	13935	13936	\N	2323	8
6969	13937	13938	\N	2323	9
6970	13939	13940	\N	2324	7
6971	13941	13942	\N	2324	8
6972	13943	13944	\N	2324	9
6973	13945	13946	\N	2325	7
6974	13947	13948	\N	2325	8
6975	13949	13950	\N	2325	9
6976	13951	13952	\N	2326	7
6977	13953	13954	\N	2326	8
6978	13955	13956	\N	2326	9
6979	13957	13958	\N	2327	7
6980	13959	13960	\N	2327	8
6981	13961	13962	\N	2327	9
6982	13963	13964	\N	2328	7
6983	13965	13966	\N	2328	8
6984	13967	13968	\N	2328	9
6985	13969	13970	\N	2329	7
6986	13971	13972	\N	2329	8
6987	13973	13974	\N	2329	9
6988	13975	13976	\N	2330	7
6989	13977	13978	\N	2330	8
6990	13979	13980	\N	2330	9
6991	13981	13982	\N	2331	7
6992	13983	13984	\N	2331	8
6993	13985	13986	\N	2331	9
6994	13987	13988	\N	2332	7
6995	13989	13990	\N	2332	8
6996	13991	13992	\N	2332	9
6997	13993	13994	\N	2333	7
6998	13995	13996	\N	2333	8
6999	13997	13998	\N	2333	9
7000	13999	14000	\N	2334	7
7001	14001	14002	\N	2334	8
7002	14003	14004	\N	2334	9
7003	14005	14006	\N	2335	7
7004	14007	14008	\N	2335	8
7005	14009	14010	\N	2335	9
7006	14011	14012	\N	2336	7
7007	14013	14014	\N	2336	8
7008	14015	14016	\N	2336	9
7009	14017	14018	\N	2337	7
7010	14019	14020	\N	2337	8
7011	14021	14022	\N	2337	9
7012	14023	14024	\N	2338	7
7013	14025	14026	\N	2338	8
7014	14027	14028	\N	2338	9
7015	14029	14030	\N	2339	7
7016	14031	14032	\N	2339	8
7017	14033	14034	\N	2339	9
7018	14035	14036	\N	2340	7
7019	14037	14038	\N	2340	8
7020	14039	14040	\N	2340	9
7021	14041	14042	\N	2341	7
7022	14043	14044	\N	2341	8
7023	14045	14046	\N	2341	9
7024	14047	14048	\N	2342	7
7025	14049	14050	\N	2342	8
7026	14051	14052	\N	2342	9
7027	14053	14054	\N	2343	7
7028	14055	14056	\N	2343	8
7029	14057	14058	\N	2343	9
7030	14059	14060	\N	2344	7
7031	14061	14062	\N	2344	8
7032	14063	14064	\N	2344	9
7033	14065	14066	\N	2345	7
7034	14067	14068	\N	2345	8
7035	14069	14070	\N	2345	9
7036	14071	14072	\N	2346	7
7037	14073	14074	\N	2346	8
7038	14075	14076	\N	2346	9
7039	14077	14078	\N	2347	7
7040	14079	14080	\N	2347	8
7041	14081	14082	\N	2347	9
7042	14083	14084	\N	2348	7
7043	14085	14086	\N	2348	8
7044	14087	14088	\N	2348	9
7045	14089	14090	\N	2349	7
7046	14091	14092	\N	2349	8
7047	14093	14094	\N	2349	9
7048	14095	14096	\N	2350	7
7049	14097	14098	\N	2350	8
7050	14099	14100	\N	2350	9
7051	14101	14102	\N	2351	7
7052	14103	14104	\N	2351	8
7053	14105	14106	\N	2351	9
7054	14107	14108	\N	2352	7
7055	14109	14110	\N	2352	8
7056	14111	14112	\N	2352	9
7057	14113	14114	\N	2353	7
7058	14115	14116	\N	2353	8
7059	14117	14118	\N	2353	9
7060	14119	14120	\N	2354	7
7061	14121	14122	\N	2354	8
7062	14123	14124	\N	2354	9
7063	14125	14126	\N	2355	7
7064	14127	14128	\N	2355	8
7065	14129	14130	\N	2355	9
7066	14131	14132	\N	2356	7
7067	14133	14134	\N	2356	8
7068	14135	14136	\N	2356	9
7069	14137	14138	\N	2357	7
7070	14139	14140	\N	2357	8
7071	14141	14142	\N	2357	9
7072	14143	14144	\N	2358	7
7073	14145	14146	\N	2358	8
7074	14147	14148	\N	2358	9
7075	14149	14150	\N	2359	7
7076	14151	14152	\N	2359	8
7077	14153	14154	\N	2359	9
7078	14155	14156	\N	2360	7
7079	14157	14158	\N	2360	8
7080	14159	14160	\N	2360	9
7081	14161	14162	\N	2361	7
7082	14163	14164	\N	2361	8
7083	14165	14166	\N	2361	9
7084	14167	14168	\N	2362	7
7085	14169	14170	\N	2362	8
7086	14171	14172	\N	2362	9
7087	14173	14174	\N	2363	7
7088	14175	14176	\N	2363	8
7089	14177	14178	\N	2363	9
7090	14179	14180	\N	2364	7
7091	14181	14182	\N	2364	8
7092	14183	14184	\N	2364	9
7093	14185	14186	\N	2365	7
7094	14187	14188	\N	2365	8
7095	14189	14190	\N	2365	9
7096	14191	14192	\N	2366	7
7097	14193	14194	\N	2366	8
7098	14195	14196	\N	2366	9
7099	14197	14198	\N	2367	7
7100	14199	14200	\N	2367	8
7101	14201	14202	\N	2367	9
7102	14203	14204	\N	2368	7
7103	14205	14206	\N	2368	8
7104	14207	14208	\N	2368	9
7105	14209	14210	\N	2369	7
7106	14211	14212	\N	2369	8
7107	14213	14214	\N	2369	9
7108	14215	14216	\N	2370	7
7109	14217	14218	\N	2370	8
7110	14219	14220	\N	2370	9
7111	14221	14222	\N	2371	7
7112	14223	14224	\N	2371	8
7113	14225	14226	\N	2371	9
7114	14227	14228	\N	2372	7
7115	14229	14230	\N	2372	8
7116	14231	14232	\N	2372	9
7117	14233	14234	\N	2373	7
7118	14235	14236	\N	2373	8
7119	14237	14238	\N	2373	9
7120	14239	14240	\N	2374	7
7121	14241	14242	\N	2374	8
7122	14243	14244	\N	2374	9
7123	14245	14246	\N	2375	7
7124	14247	14248	\N	2375	8
7125	14249	14250	\N	2375	9
7126	14251	14252	\N	2376	7
7127	14253	14254	\N	2376	8
7128	14255	14256	\N	2376	9
7129	14257	14258	\N	2377	7
7130	14259	14260	\N	2377	8
7131	14261	14262	\N	2377	9
7132	14263	14264	\N	2378	7
7133	14265	14266	\N	2378	8
7134	14267	14268	\N	2378	9
7135	14269	14270	\N	2379	7
7136	14271	14272	\N	2379	8
7137	14273	14274	\N	2379	9
7138	14275	14276	\N	2380	7
7139	14277	14278	\N	2380	8
7140	14279	14280	\N	2380	9
7141	14281	14282	\N	2381	7
7142	14283	14284	\N	2381	8
7143	14285	14286	\N	2381	9
7144	14287	14288	\N	2382	7
7145	14289	14290	\N	2382	8
7146	14291	14292	\N	2382	9
7147	14293	14294	\N	2383	7
7148	14295	14296	\N	2383	8
7149	14297	14298	\N	2383	9
7150	14299	14300	\N	2384	7
7151	14301	14302	\N	2384	8
7152	14303	14304	\N	2384	9
7153	14305	14306	\N	2385	7
7154	14307	14308	\N	2385	8
7155	14309	14310	\N	2385	9
7156	14311	14312	\N	2386	7
7157	14313	14314	\N	2386	8
7158	14315	14316	\N	2386	9
7159	14317	14318	\N	2387	7
7160	14319	14320	\N	2387	8
7161	14321	14322	\N	2387	9
7162	14323	14324	\N	2388	7
7163	14325	14326	\N	2388	8
7164	14327	14328	\N	2388	9
7165	14329	14330	\N	2389	7
7166	14331	14332	\N	2389	8
7167	14333	14334	\N	2389	9
7168	14335	14336	\N	2390	7
7169	14337	14338	\N	2390	8
7170	14339	14340	\N	2390	9
7171	14341	14342	\N	2391	7
7172	14343	14344	\N	2391	8
7173	14345	14346	\N	2391	9
7174	14347	14348	\N	2392	7
7175	14349	14350	\N	2392	8
7176	14351	14352	\N	2392	9
7177	14353	14354	\N	2393	7
7178	14355	14356	\N	2393	8
7179	14357	14358	\N	2393	9
7180	14359	14360	\N	2394	7
7181	14361	14362	\N	2394	8
7182	14363	14364	\N	2394	9
7183	14365	14366	\N	2395	7
7184	14367	14368	\N	2395	8
7185	14369	14370	\N	2395	9
7186	14371	14372	\N	2396	7
7187	14373	14374	\N	2396	8
7188	14375	14376	\N	2396	9
7189	14377	14378	\N	2397	7
7190	14379	14380	\N	2397	8
7191	14381	14382	\N	2397	9
7192	14383	14384	\N	2398	7
7193	14385	14386	\N	2398	8
7194	14387	14388	\N	2398	9
7195	14389	14390	\N	2399	7
7196	14391	14392	\N	2399	8
7197	14393	14394	\N	2399	9
7198	14395	14396	\N	2400	7
7199	14397	14398	\N	2400	8
7200	14399	14400	\N	2400	9
7201	14401	14402	\N	2401	7
7202	14403	14404	\N	2401	8
7203	14405	14406	\N	2401	9
7204	14407	14408	\N	2402	7
7205	14409	14410	\N	2402	8
7206	14411	14412	\N	2402	9
7207	14413	14414	\N	2403	7
7208	14415	14416	\N	2403	8
7209	14417	14418	\N	2403	9
7210	14419	14420	\N	2404	7
7211	14421	14422	\N	2404	8
7212	14423	14424	\N	2404	9
7213	14425	14426	\N	2405	7
7214	14427	14428	\N	2405	8
7215	14429	14430	\N	2405	9
7216	14431	14432	\N	2406	7
7217	14433	14434	\N	2406	8
7218	14435	14436	\N	2406	9
7219	14437	14438	\N	2407	7
7220	14439	14440	\N	2407	8
7221	14441	14442	\N	2407	9
7222	14443	14444	\N	2408	7
7223	14445	14446	\N	2408	8
7224	14447	14448	\N	2408	9
7225	14449	14450	\N	2409	7
7226	14451	14452	\N	2409	8
7227	14453	14454	\N	2409	9
7228	14455	14456	\N	2410	7
7229	14457	14458	\N	2410	8
7230	14459	14460	\N	2410	9
7231	14461	14462	\N	2411	7
7232	14463	14464	\N	2411	8
7233	14465	14466	\N	2411	9
7234	14467	14468	\N	2412	7
7235	14469	14470	\N	2412	8
7236	14471	14472	\N	2412	9
7237	14473	14474	\N	2413	7
7238	14475	14476	\N	2413	8
7239	14477	14478	\N	2413	9
7240	14479	14480	\N	2414	7
7241	14481	14482	\N	2414	8
7242	14483	14484	\N	2414	9
7243	14485	14486	\N	2415	7
7244	14487	14488	\N	2415	8
7245	14489	14490	\N	2415	9
7246	14491	14492	\N	2416	7
7247	14493	14494	\N	2416	8
7248	14495	14496	\N	2416	9
7249	14497	14498	\N	2417	7
7250	14499	14500	\N	2417	8
7251	14501	14502	\N	2417	9
7252	14503	14504	\N	2418	7
7253	14505	14506	\N	2418	8
7254	14507	14508	\N	2418	9
7255	14509	14510	\N	2419	7
7256	14511	14512	\N	2419	8
7257	14513	14514	\N	2419	9
7258	14515	14516	\N	2420	7
7259	14517	14518	\N	2420	8
7260	14519	14520	\N	2420	9
7261	14521	14522	\N	2421	7
7262	14523	14524	\N	2421	8
7263	14525	14526	\N	2421	9
7264	14527	14528	\N	2422	7
7265	14529	14530	\N	2422	8
7266	14531	14532	\N	2422	9
7267	14533	14534	\N	2423	7
7268	14535	14536	\N	2423	8
7269	14537	14538	\N	2423	9
7270	14539	14540	\N	2424	7
7271	14541	14542	\N	2424	8
7272	14543	14544	\N	2424	9
7273	14545	14546	\N	2425	7
7274	14547	14548	\N	2425	8
7275	14549	14550	\N	2425	9
7276	14551	14552	\N	2426	7
7277	14553	14554	\N	2426	8
7278	14555	14556	\N	2426	9
7279	14557	14558	\N	2427	7
7280	14559	14560	\N	2427	8
7281	14561	14562	\N	2427	9
7282	14563	14564	\N	2428	7
7283	14565	14566	\N	2428	8
7284	14567	14568	\N	2428	9
7285	14569	14570	\N	2429	7
7286	14571	14572	\N	2429	8
7287	14573	14574	\N	2429	9
7288	14575	14576	\N	2430	7
7289	14577	14578	\N	2430	8
7290	14579	14580	\N	2430	9
7291	14581	14582	\N	2431	7
7292	14583	14584	\N	2431	8
7293	14585	14586	\N	2431	9
7294	14587	14588	\N	2432	7
7295	14589	14590	\N	2432	8
7296	14591	14592	\N	2432	9
7297	14593	14594	\N	2433	7
7298	14595	14596	\N	2433	8
7299	14597	14598	\N	2433	9
7300	14599	14600	\N	2434	7
7301	14601	14602	\N	2434	8
7302	14603	14604	\N	2434	9
7303	14605	14606	\N	2435	7
7304	14607	14608	\N	2435	8
7305	14609	14610	\N	2435	9
7306	14611	14612	\N	2436	7
7307	14613	14614	\N	2436	8
7308	14615	14616	\N	2436	9
7309	14617	14618	\N	2437	7
7310	14619	14620	\N	2437	8
7311	14621	14622	\N	2437	9
7312	14623	14624	\N	2438	7
7313	14625	14626	\N	2438	8
7314	14627	14628	\N	2438	9
7315	14629	14630	\N	2439	7
7316	14631	14632	\N	2439	8
7317	14633	14634	\N	2439	9
7318	14635	14636	\N	2440	7
7319	14637	14638	\N	2440	8
7320	14639	14640	\N	2440	9
7321	14641	14642	\N	2441	7
7322	14643	14644	\N	2441	8
7323	14645	14646	\N	2441	9
7324	14647	14648	\N	2442	7
7325	14649	14650	\N	2442	8
7326	14651	14652	\N	2442	9
7327	14653	14654	\N	2443	7
7328	14655	14656	\N	2443	8
7329	14657	14658	\N	2443	9
7330	14659	14660	\N	2444	7
7331	14661	14662	\N	2444	8
7332	14663	14664	\N	2444	9
7333	14665	14666	\N	2445	7
7334	14667	14668	\N	2445	8
7335	14669	14670	\N	2445	9
7336	14671	14672	\N	2446	7
7337	14673	14674	\N	2446	8
7338	14675	14676	\N	2446	9
7339	14677	14678	\N	2447	7
7340	14679	14680	\N	2447	8
7341	14681	14682	\N	2447	9
7342	14683	14684	\N	2448	7
7343	14685	14686	\N	2448	8
7344	14687	14688	\N	2448	9
7345	14689	14690	\N	2449	7
7346	14691	14692	\N	2449	8
7347	14693	14694	\N	2449	9
7348	14695	14696	\N	2450	7
7349	14697	14698	\N	2450	8
7350	14699	14700	\N	2450	9
7351	14701	14702	\N	2451	7
7352	14703	14704	\N	2451	8
7353	14705	14706	\N	2451	9
7354	14707	14708	\N	2452	7
7355	14709	14710	\N	2452	8
7356	14711	14712	\N	2452	9
7357	14713	14714	\N	2453	7
7358	14715	14716	\N	2453	8
7359	14717	14718	\N	2453	9
7360	14719	14720	\N	2454	7
7361	14721	14722	\N	2454	8
7362	14723	14724	\N	2454	9
7363	14725	14726	\N	2455	7
7364	14727	14728	\N	2455	8
7365	14729	14730	\N	2455	9
7366	14731	14732	\N	2456	7
7367	14733	14734	\N	2456	8
7368	14735	14736	\N	2456	9
7369	14737	14738	\N	2457	7
7370	14739	14740	\N	2457	8
7371	14741	14742	\N	2457	9
7372	14743	14744	\N	2458	7
7373	14745	14746	\N	2458	8
7374	14747	14748	\N	2458	9
7375	14749	14750	\N	2459	7
7376	14751	14752	\N	2459	8
7377	14753	14754	\N	2459	9
7378	14755	14756	\N	2460	7
7379	14757	14758	\N	2460	8
7380	14759	14760	\N	2460	9
7381	14761	14762	\N	2461	7
7382	14763	14764	\N	2461	8
7383	14765	14766	\N	2461	9
7384	14767	14768	\N	2462	7
7385	14769	14770	\N	2462	8
7386	14771	14772	\N	2462	9
7387	14773	14774	\N	2463	7
7388	14775	14776	\N	2463	8
7389	14777	14778	\N	2463	9
7390	14779	14780	\N	2464	7
7391	14781	14782	\N	2464	8
7392	14783	14784	\N	2464	9
7393	14785	14786	\N	2465	7
7394	14787	14788	\N	2465	8
7395	14789	14790	\N	2465	9
7396	14791	14792	\N	2466	7
7397	14793	14794	\N	2466	8
7398	14795	14796	\N	2466	9
7399	14797	14798	\N	2467	7
7400	14799	14800	\N	2467	8
7401	14801	14802	\N	2467	9
7402	14803	14804	\N	2468	7
7403	14805	14806	\N	2468	8
7404	14807	14808	\N	2468	9
7405	14809	14810	\N	2469	7
7406	14811	14812	\N	2469	8
7407	14813	14814	\N	2469	9
7408	14815	14816	\N	2470	7
7409	14817	14818	\N	2470	8
7410	14819	14820	\N	2470	9
7411	14821	14822	\N	2471	7
7412	14823	14824	\N	2471	8
7413	14825	14826	\N	2471	9
7414	14827	14828	\N	2472	7
7415	14829	14830	\N	2472	8
7416	14831	14832	\N	2472	9
7417	14833	14834	\N	2473	7
7418	14835	14836	\N	2473	8
7419	14837	14838	\N	2473	9
7420	14839	14840	\N	2474	7
7421	14841	14842	\N	2474	8
7422	14843	14844	\N	2474	9
7423	14845	14846	\N	2475	7
7424	14847	14848	\N	2475	8
7425	14849	14850	\N	2475	9
7426	14851	14852	\N	2476	7
7427	14853	14854	\N	2476	8
7428	14855	14856	\N	2476	9
7429	14857	14858	\N	2477	7
7430	14859	14860	\N	2477	8
7431	14861	14862	\N	2477	9
7432	14863	14864	\N	2478	7
7433	14865	14866	\N	2478	8
7434	14867	14868	\N	2478	9
7435	14869	14870	\N	2479	7
7436	14871	14872	\N	2479	8
7437	14873	14874	\N	2479	9
7438	14875	14876	\N	2480	7
7439	14877	14878	\N	2480	8
7440	14879	14880	\N	2480	9
7441	14881	14882	\N	2481	7
7442	14883	14884	\N	2481	8
7443	14885	14886	\N	2481	9
7444	14887	14888	\N	2482	7
7445	14889	14890	\N	2482	8
7446	14891	14892	\N	2482	9
7447	14893	14894	\N	2483	7
7448	14895	14896	\N	2483	8
7449	14897	14898	\N	2483	9
7450	14899	14900	\N	2484	7
7451	14901	14902	\N	2484	8
7452	14903	14904	\N	2484	9
7453	14905	14906	\N	2485	7
7454	14907	14908	\N	2485	8
7455	14909	14910	\N	2485	9
7456	14911	14912	\N	2486	7
7457	14913	14914	\N	2486	8
7458	14915	14916	\N	2486	9
7459	14917	14918	\N	2487	7
7460	14919	14920	\N	2487	8
7461	14921	14922	\N	2487	9
7462	14923	14924	\N	2488	7
7463	14925	14926	\N	2488	8
7464	14927	14928	\N	2488	9
7465	14929	14930	\N	2489	7
7466	14931	14932	\N	2489	8
7467	14933	14934	\N	2489	9
7468	14935	14936	\N	2490	7
7469	14937	14938	\N	2490	8
7470	14939	14940	\N	2490	9
7471	14941	14942	\N	2491	7
7472	14943	14944	\N	2491	8
7473	14945	14946	\N	2491	9
7474	14947	14948	\N	2492	7
7475	14949	14950	\N	2492	8
7476	14951	14952	\N	2492	9
7477	14953	14954	\N	2493	7
7478	14955	14956	\N	2493	8
7479	14957	14958	\N	2493	9
7480	14959	14960	\N	2494	7
7481	14961	14962	\N	2494	8
7482	14963	14964	\N	2494	9
7483	14965	14966	\N	2495	7
7484	14967	14968	\N	2495	8
7485	14969	14970	\N	2495	9
7486	14971	14972	\N	2496	7
7487	14973	14974	\N	2496	8
7488	14975	14976	\N	2496	9
7489	14977	14978	\N	2497	7
7490	14979	14980	\N	2497	8
7491	14981	14982	\N	2497	9
7492	14983	14984	\N	2498	7
7493	14985	14986	\N	2498	8
7494	14987	14988	\N	2498	9
7495	14989	14990	\N	2499	7
7496	14991	14992	\N	2499	8
7497	14993	14994	\N	2499	9
7498	14995	14996	\N	2500	7
7499	14997	14998	\N	2500	8
7500	14999	15000	\N	2500	9
7501	15001	15002	\N	2501	7
7502	15003	15004	\N	2501	8
7503	15005	15006	\N	2501	9
7504	15007	15008	\N	2502	7
7505	15009	15010	\N	2502	8
7506	15011	15012	\N	2502	9
7507	15013	15014	\N	2503	7
7508	15015	15016	\N	2503	8
7509	15017	15018	\N	2503	9
7510	15019	15020	\N	2504	7
7511	15021	15022	\N	2504	8
7512	15023	15024	\N	2504	9
7513	15025	15026	\N	2505	7
7514	15027	15028	\N	2505	8
7515	15029	15030	\N	2505	9
7516	15031	15032	\N	2506	7
7517	15033	15034	\N	2506	8
7518	15035	15036	\N	2506	9
7519	15037	15038	\N	2507	7
7520	15039	15040	\N	2507	8
7521	15041	15042	\N	2507	9
7522	15043	15044	\N	2508	7
7523	15045	15046	\N	2508	8
7524	15047	15048	\N	2508	9
7525	15049	15050	\N	2509	7
7526	15051	15052	\N	2509	8
7527	15053	15054	\N	2509	9
7528	15055	15056	\N	2510	7
7529	15057	15058	\N	2510	8
7530	15059	15060	\N	2510	9
7531	15061	15062	\N	2511	7
7532	15063	15064	\N	2511	8
7533	15065	15066	\N	2511	9
7534	15067	15068	\N	2512	7
7535	15069	15070	\N	2512	8
7536	15071	15072	\N	2512	9
7537	15073	15074	\N	2513	7
7538	15075	15076	\N	2513	8
7539	15077	15078	\N	2513	9
7540	15079	15080	\N	2514	7
7541	15081	15082	\N	2514	8
7542	15083	15084	\N	2514	9
7543	15085	15086	\N	2515	7
7544	15087	15088	\N	2515	8
7545	15089	15090	\N	2515	9
7546	15091	15092	\N	2516	7
7547	15093	15094	\N	2516	8
7548	15095	15096	\N	2516	9
7549	15097	15098	\N	2517	7
7550	15099	15100	\N	2517	8
7551	15101	15102	\N	2517	9
7552	15103	15104	\N	2518	7
7553	15105	15106	\N	2518	8
7554	15107	15108	\N	2518	9
7555	15109	15110	\N	2519	7
7556	15111	15112	\N	2519	8
7557	15113	15114	\N	2519	9
7558	15115	15116	\N	2520	7
7559	15117	15118	\N	2520	8
7560	15119	15120	\N	2520	9
7561	15121	15122	\N	2521	7
7562	15123	15124	\N	2521	8
7563	15125	15126	\N	2521	9
7564	15127	15128	\N	2522	7
7565	15129	15130	\N	2522	8
7566	15131	15132	\N	2522	9
7567	15133	15134	\N	2523	7
7568	15135	15136	\N	2523	8
7569	15137	15138	\N	2523	9
7570	15139	15140	\N	2524	7
7571	15141	15142	\N	2524	8
7572	15143	15144	\N	2524	9
7573	15145	15146	\N	2525	7
7574	15147	15148	\N	2525	8
7575	15149	15150	\N	2525	9
7576	15151	15152	\N	2526	7
7577	15153	15154	\N	2526	8
7578	15155	15156	\N	2526	9
7579	15157	15158	\N	2527	7
7580	15159	15160	\N	2527	8
7581	15161	15162	\N	2527	9
7582	15163	15164	\N	2528	7
7583	15165	15166	\N	2528	8
7584	15167	15168	\N	2528	9
7585	15169	15170	\N	2529	7
7586	15171	15172	\N	2529	8
7587	15173	15174	\N	2529	9
7588	15175	15176	\N	2530	7
7589	15177	15178	\N	2530	8
7590	15179	15180	\N	2530	9
7591	15181	15182	\N	2531	7
7592	15183	15184	\N	2531	8
7593	15185	15186	\N	2531	9
7594	15187	15188	\N	2532	7
7595	15189	15190	\N	2532	8
7596	15191	15192	\N	2532	9
7597	15193	15194	\N	2533	7
7598	15195	15196	\N	2533	8
7599	15197	15198	\N	2533	9
7600	15199	15200	\N	2534	7
7601	15201	15202	\N	2534	8
7602	15203	15204	\N	2534	9
7603	15205	15206	\N	2535	7
7604	15207	15208	\N	2535	8
7605	15209	15210	\N	2535	9
7606	15211	15212	\N	2536	7
7607	15213	15214	\N	2536	8
7608	15215	15216	\N	2536	9
7609	15217	15218	\N	2537	7
7610	15219	15220	\N	2537	8
7611	15221	15222	\N	2537	9
7612	15223	15224	\N	2538	7
7613	15225	15226	\N	2538	8
7614	15227	15228	\N	2538	9
7615	15229	15230	\N	2539	7
7616	15231	15232	\N	2539	8
7617	15233	15234	\N	2539	9
7618	15235	15236	\N	2540	7
7619	15237	15238	\N	2540	8
7620	15239	15240	\N	2540	9
7621	15241	15242	\N	2541	7
7622	15243	15244	\N	2541	8
7623	15245	15246	\N	2541	9
7624	15247	15248	\N	2542	7
7625	15249	15250	\N	2542	8
7626	15251	15252	\N	2542	9
7627	15253	15254	\N	2543	7
7628	15255	15256	\N	2543	8
7629	15257	15258	\N	2543	9
7630	15259	15260	\N	2544	7
7631	15261	15262	\N	2544	8
7632	15263	15264	\N	2544	9
7633	15265	15266	\N	2545	7
7634	15267	15268	\N	2545	8
7635	15269	15270	\N	2545	9
7636	15271	15272	\N	2546	7
7637	15273	15274	\N	2546	8
7638	15275	15276	\N	2546	9
7639	15277	15278	\N	2547	7
7640	15279	15280	\N	2547	8
7641	15281	15282	\N	2547	9
7642	15283	15284	\N	2548	7
7643	15285	15286	\N	2548	8
7644	15287	15288	\N	2548	9
7645	15289	15290	\N	2549	7
7646	15291	15292	\N	2549	8
7647	15293	15294	\N	2549	9
7648	15295	15296	\N	2550	7
7649	15297	15298	\N	2550	8
7650	15299	15300	\N	2550	9
7651	15301	15302	\N	2551	7
7652	15303	15304	\N	2551	8
7653	15305	15306	\N	2551	9
7654	15307	15308	\N	2552	7
7655	15309	15310	\N	2552	8
7656	15311	15312	\N	2552	9
7657	15313	15314	\N	2553	7
7658	15315	15316	\N	2553	8
7659	15317	15318	\N	2553	9
7660	15319	15320	\N	2554	7
7661	15321	15322	\N	2554	8
7662	15323	15324	\N	2554	9
7663	15325	15326	\N	2555	7
7664	15327	15328	\N	2555	8
7665	15329	15330	\N	2555	9
7666	15331	15332	\N	2556	7
7667	15333	15334	\N	2556	8
7668	15335	15336	\N	2556	9
7669	15337	15338	\N	2557	7
7670	15339	15340	\N	2557	8
7671	15341	15342	\N	2557	9
7672	15343	15344	\N	2558	7
7673	15345	15346	\N	2558	8
7674	15347	15348	\N	2558	9
7675	15349	15350	\N	2559	7
7676	15351	15352	\N	2559	8
7677	15353	15354	\N	2559	9
7678	15355	15356	\N	2560	7
7679	15357	15358	\N	2560	8
7680	15359	15360	\N	2560	9
7681	15361	15362	\N	2561	7
7682	15363	15364	\N	2561	8
7683	15365	15366	\N	2561	9
7684	15367	15368	\N	2562	7
7685	15369	15370	\N	2562	8
7686	15371	15372	\N	2562	9
7687	15373	15374	\N	2563	7
7688	15375	15376	\N	2563	8
7689	15377	15378	\N	2563	9
7690	15379	15380	\N	2564	7
7691	15381	15382	\N	2564	8
7692	15383	15384	\N	2564	9
7693	15385	15386	\N	2565	7
7694	15387	15388	\N	2565	8
7695	15389	15390	\N	2565	9
7696	15391	15392	\N	2566	7
7697	15393	15394	\N	2566	8
7698	15395	15396	\N	2566	9
7699	15397	15398	\N	2567	7
7700	15399	15400	\N	2567	8
7701	15401	15402	\N	2567	9
7702	15403	15404	\N	2568	7
7703	15405	15406	\N	2568	8
7704	15407	15408	\N	2568	9
7705	15409	15410	\N	2569	7
7706	15411	15412	\N	2569	8
7707	15413	15414	\N	2569	9
7708	15415	15416	\N	2570	7
7709	15417	15418	\N	2570	8
7710	15419	15420	\N	2570	9
7711	15421	15422	\N	2571	7
7712	15423	15424	\N	2571	8
7713	15425	15426	\N	2571	9
7714	15427	15428	\N	2572	7
7715	15429	15430	\N	2572	8
7716	15431	15432	\N	2572	9
7717	15433	15434	\N	2573	7
7718	15435	15436	\N	2573	8
7719	15437	15438	\N	2573	9
7720	15439	15440	\N	2574	7
7721	15441	15442	\N	2574	8
7722	15443	15444	\N	2574	9
7723	15445	15446	\N	2575	7
7724	15447	15448	\N	2575	8
7725	15449	15450	\N	2575	9
7726	15451	15452	\N	2576	7
7727	15453	15454	\N	2576	8
7728	15455	15456	\N	2576	9
7729	15457	15458	\N	2577	7
7730	15459	15460	\N	2577	8
7731	15461	15462	\N	2577	9
7732	15463	15464	\N	2578	7
7733	15465	15466	\N	2578	8
7734	15467	15468	\N	2578	9
7735	15469	15470	\N	2579	7
7736	15471	15472	\N	2579	8
7737	15473	15474	\N	2579	9
7738	15475	15476	\N	2580	7
7739	15477	15478	\N	2580	8
7740	15479	15480	\N	2580	9
7741	15481	15482	\N	2581	7
7742	15483	15484	\N	2581	8
7743	15485	15486	\N	2581	9
7744	15487	15488	\N	2582	7
7745	15489	15490	\N	2582	8
7746	15491	15492	\N	2582	9
7747	15493	15494	\N	2583	7
7748	15495	15496	\N	2583	8
7749	15497	15498	\N	2583	9
7750	15499	15500	\N	2584	7
7751	15501	15502	\N	2584	8
7752	15503	15504	\N	2584	9
7753	15505	15506	\N	2585	7
7754	15507	15508	\N	2585	8
7755	15509	15510	\N	2585	9
7756	15511	15512	\N	2586	7
7757	15513	15514	\N	2586	8
7758	15515	15516	\N	2586	9
7759	15517	15518	\N	2587	7
7760	15519	15520	\N	2587	8
7761	15521	15522	\N	2587	9
7762	15523	15524	\N	2588	7
7763	15525	15526	\N	2588	8
7764	15527	15528	\N	2588	9
7765	15529	15530	\N	2589	7
7766	15531	15532	\N	2589	8
7767	15533	15534	\N	2589	9
7768	15535	15536	\N	2590	7
7769	15537	15538	\N	2590	8
7770	15539	15540	\N	2590	9
7771	15541	15542	\N	2591	7
7772	15543	15544	\N	2591	8
7773	15545	15546	\N	2591	9
7774	15547	15548	\N	2592	7
7775	15549	15550	\N	2592	8
7776	15551	15552	\N	2592	9
8641	16417	\N	\N	3457	11
8642	16418	\N	\N	3458	11
8643	16419	\N	\N	3459	11
8644	16420	\N	\N	3460	11
8645	16421	\N	\N	3461	11
8646	16422	\N	\N	3462	11
8647	16423	\N	\N	3463	11
8648	16424	\N	\N	3464	11
8649	16425	\N	\N	3465	11
8650	16426	\N	\N	3466	11
8651	16427	\N	\N	3467	11
8652	16428	\N	\N	3468	11
8653	16429	\N	\N	3469	11
8654	16430	\N	\N	3470	11
8655	16431	\N	\N	3471	11
8656	16432	\N	\N	3472	11
8657	16433	\N	\N	3473	11
8658	16434	\N	\N	3474	11
8659	16435	\N	\N	3475	11
8660	16436	\N	\N	3476	11
8661	16437	\N	\N	3477	11
8662	16438	\N	\N	3478	11
8663	16439	\N	\N	3479	11
8664	16440	\N	\N	3480	11
8665	16441	\N	\N	3481	11
8666	16442	\N	\N	3482	11
8667	16443	\N	\N	3483	11
8668	16444	\N	\N	3484	11
8669	16445	\N	\N	3485	11
8670	16446	\N	\N	3486	11
8671	16447	\N	\N	3487	11
8672	16448	\N	\N	3488	11
8673	16449	\N	\N	3489	11
8674	16450	\N	\N	3490	11
8675	16451	\N	\N	3491	11
8676	16452	\N	\N	3492	11
8677	16453	\N	\N	3493	11
8678	16454	\N	\N	3494	11
8679	16455	\N	\N	3495	11
8680	16456	\N	\N	3496	11
8681	16457	\N	\N	3497	11
8682	16458	\N	\N	3498	11
8683	16459	\N	\N	3499	11
8684	16460	\N	\N	3500	11
8685	16461	\N	\N	3501	11
8686	16462	\N	\N	3502	11
8687	16463	\N	\N	3503	11
8688	16464	\N	\N	3504	11
8689	16465	\N	\N	3505	11
8690	16466	\N	\N	3506	11
8691	16467	\N	\N	3507	11
8692	16468	\N	\N	3508	11
8693	16469	\N	\N	3509	11
8694	16470	\N	\N	3510	11
8695	16471	\N	\N	3511	11
8696	16472	\N	\N	3512	11
8697	16473	\N	\N	3513	11
8698	16474	\N	\N	3514	11
8699	16475	\N	\N	3515	11
8700	16476	\N	\N	3516	11
8701	16477	\N	\N	3517	11
8702	16478	\N	\N	3518	11
8703	16479	\N	\N	3519	11
8704	16480	\N	\N	3520	11
8705	16481	\N	\N	3521	11
8706	16482	\N	\N	3522	11
8707	16483	\N	\N	3523	11
8708	16484	\N	\N	3524	11
8709	16485	\N	\N	3525	11
8710	16486	\N	\N	3526	11
8711	16487	\N	\N	3527	11
8712	16488	\N	\N	3528	11
8713	16489	\N	\N	3529	11
8714	16490	\N	\N	3530	11
8715	16491	\N	\N	3531	11
8716	16492	\N	\N	3532	11
8717	16493	\N	\N	3533	11
8718	16494	\N	\N	3534	11
8719	16495	\N	\N	3535	11
8720	16496	\N	\N	3536	11
8721	16497	\N	\N	3537	11
8722	16498	\N	\N	3538	11
8723	16499	\N	\N	3539	11
8724	16500	\N	\N	3540	11
8725	16501	\N	\N	3541	11
8726	16502	\N	\N	3542	11
8727	16503	\N	\N	3543	11
8728	16504	\N	\N	3544	11
8729	16505	\N	\N	3545	11
8730	16506	\N	\N	3546	11
8731	16507	\N	\N	3547	11
8732	16508	\N	\N	3548	11
8733	16509	\N	\N	3549	11
8734	16510	\N	\N	3550	11
8735	16511	\N	\N	3551	11
8736	16512	\N	\N	3552	11
8737	16513	\N	\N	3553	11
8738	16514	\N	\N	3554	11
8739	16515	\N	\N	3555	11
8740	16516	\N	\N	3556	11
8741	16517	\N	\N	3557	11
8742	16518	\N	\N	3558	11
8743	16519	\N	\N	3559	11
8744	16520	\N	\N	3560	11
8745	16521	\N	\N	3561	11
8746	16522	\N	\N	3562	11
8747	16523	\N	\N	3563	11
8748	16524	\N	\N	3564	11
8749	16525	\N	\N	3565	11
8750	16526	\N	\N	3566	11
8751	16527	\N	\N	3567	11
8752	16528	\N	\N	3568	11
8753	16529	\N	\N	3569	11
8754	16530	\N	\N	3570	11
8755	16531	\N	\N	3571	11
8756	16532	\N	\N	3572	11
8757	16533	\N	\N	3573	11
8758	16534	\N	\N	3574	11
8759	16535	\N	\N	3575	11
8760	16536	\N	\N	3576	11
8761	16537	\N	\N	3577	11
8762	16538	\N	\N	3578	11
8763	16539	\N	\N	3579	11
8764	16540	\N	\N	3580	11
8765	16541	\N	\N	3581	11
8766	16542	\N	\N	3582	11
8767	16543	\N	\N	3583	11
8768	16544	\N	\N	3584	11
8769	16545	\N	\N	3585	11
8770	16546	\N	\N	3586	11
8771	16547	\N	\N	3587	11
8772	16548	\N	\N	3588	11
8773	16549	\N	\N	3589	11
8774	16550	\N	\N	3590	11
8775	16551	\N	\N	3591	11
8776	16552	\N	\N	3592	11
8777	16553	\N	\N	3593	11
8778	16554	\N	\N	3594	11
8779	16555	\N	\N	3595	11
8780	16556	\N	\N	3596	11
8781	16557	\N	\N	3597	11
8782	16558	\N	\N	3598	11
8783	16559	\N	\N	3599	11
8784	16560	\N	\N	3600	11
8785	16561	\N	\N	3601	11
8786	16562	\N	\N	3602	11
8787	16563	\N	\N	3603	11
8788	16564	\N	\N	3604	11
8789	16565	\N	\N	3605	11
8790	16566	\N	\N	3606	11
8791	16567	\N	\N	3607	11
8792	16568	\N	\N	3608	11
8793	16569	\N	\N	3609	11
8794	16570	\N	\N	3610	11
8795	16571	\N	\N	3611	11
8796	16572	\N	\N	3612	11
8797	16573	\N	\N	3613	11
8798	16574	\N	\N	3614	11
8799	16575	\N	\N	3615	11
8800	16576	\N	\N	3616	11
8801	16577	\N	\N	3617	11
8802	16578	\N	\N	3618	11
8803	16579	\N	\N	3619	11
8804	16580	\N	\N	3620	11
8805	16581	\N	\N	3621	11
8806	16582	\N	\N	3622	11
8807	16583	\N	\N	3623	11
8808	16584	\N	\N	3624	11
8809	16585	\N	\N	3625	11
8810	16586	\N	\N	3626	11
8811	16587	\N	\N	3627	11
8812	16588	\N	\N	3628	11
8813	16589	\N	\N	3629	11
8814	16590	\N	\N	3630	11
8815	16591	\N	\N	3631	11
8816	16592	\N	\N	3632	11
8817	16593	\N	\N	3633	11
8818	16594	\N	\N	3634	11
8819	16595	\N	\N	3635	11
8820	16596	\N	\N	3636	11
8821	16597	\N	\N	3637	11
8822	16598	\N	\N	3638	11
8823	16599	\N	\N	3639	11
8824	16600	\N	\N	3640	11
8825	16601	\N	\N	3641	11
8826	16602	\N	\N	3642	11
8827	16603	\N	\N	3643	11
8828	16604	\N	\N	3644	11
8829	16605	\N	\N	3645	11
8830	16606	\N	\N	3646	11
8831	16607	\N	\N	3647	11
8832	16608	\N	\N	3648	11
8833	16609	\N	\N	3649	11
8834	16610	\N	\N	3650	11
8835	16611	\N	\N	3651	11
8836	16612	\N	\N	3652	11
8837	16613	\N	\N	3653	11
8838	16614	\N	\N	3654	11
8839	16615	\N	\N	3655	11
8840	16616	\N	\N	3656	11
8841	16617	\N	\N	3657	11
8842	16618	\N	\N	3658	11
8843	16619	\N	\N	3659	11
8844	16620	\N	\N	3660	11
8845	16621	\N	\N	3661	11
8846	16622	\N	\N	3662	11
8847	16623	\N	\N	3663	11
8848	16624	\N	\N	3664	11
8849	16625	\N	\N	3665	11
8850	16626	\N	\N	3666	11
8851	16627	\N	\N	3667	11
8852	16628	\N	\N	3668	11
8853	16629	\N	\N	3669	11
8854	16630	\N	\N	3670	11
8855	16631	\N	\N	3671	11
8856	16632	\N	\N	3672	11
8857	16633	\N	\N	3673	11
8858	16634	\N	\N	3674	11
8859	16635	\N	\N	3675	11
8860	16636	\N	\N	3676	11
8861	16637	\N	\N	3677	11
8862	16638	\N	\N	3678	11
8863	16639	\N	\N	3679	11
8864	16640	\N	\N	3680	11
8865	16641	\N	\N	3681	11
8866	16642	\N	\N	3682	11
8867	16643	\N	\N	3683	11
8868	16644	\N	\N	3684	11
8869	16645	\N	\N	3685	11
8870	16646	\N	\N	3686	11
8871	16647	\N	\N	3687	11
8872	16648	\N	\N	3688	11
8873	16649	\N	\N	3689	11
8874	16650	\N	\N	3690	11
8875	16651	\N	\N	3691	11
8876	16652	\N	\N	3692	11
8877	16653	\N	\N	3693	11
8878	16654	\N	\N	3694	11
8879	16655	\N	\N	3695	11
8880	16656	\N	\N	3696	11
8881	16657	\N	\N	3697	11
8882	16658	\N	\N	3698	11
8883	16659	\N	\N	3699	11
8884	16660	\N	\N	3700	11
8885	16661	\N	\N	3701	11
8886	16662	\N	\N	3702	11
8887	16663	\N	\N	3703	11
8888	16664	\N	\N	3704	11
8889	16665	\N	\N	3705	11
8890	16666	\N	\N	3706	11
8891	16667	\N	\N	3707	11
8892	16668	\N	\N	3708	11
8893	16669	\N	\N	3709	11
8894	16670	\N	\N	3710	11
8895	16671	\N	\N	3711	11
8896	16672	\N	\N	3712	11
8897	16673	\N	\N	3713	11
8898	16674	\N	\N	3714	11
8899	16675	\N	\N	3715	11
8900	16676	\N	\N	3716	11
8901	16677	\N	\N	3717	11
8902	16678	\N	\N	3718	11
8903	16679	\N	\N	3719	11
8904	16680	\N	\N	3720	11
8905	16681	\N	\N	3721	11
8906	16682	\N	\N	3722	11
8907	16683	\N	\N	3723	11
8908	16684	\N	\N	3724	11
8909	16685	\N	\N	3725	11
8910	16686	\N	\N	3726	11
8911	16687	\N	\N	3727	11
8912	16688	\N	\N	3728	11
8913	16689	\N	\N	3729	11
8914	16690	\N	\N	3730	11
8915	16691	\N	\N	3731	11
8916	16692	\N	\N	3732	11
8917	16693	\N	\N	3733	11
8918	16694	\N	\N	3734	11
8919	16695	\N	\N	3735	11
8920	16696	\N	\N	3736	11
8921	16697	\N	\N	3737	11
8922	16698	\N	\N	3738	11
8923	16699	\N	\N	3739	11
8924	16700	\N	\N	3740	11
8925	16701	\N	\N	3741	11
8926	16702	\N	\N	3742	11
8927	16703	\N	\N	3743	11
8928	16704	\N	\N	3744	11
8929	16705	\N	\N	3745	11
8930	16706	\N	\N	3746	11
8931	16707	\N	\N	3747	11
8932	16708	\N	\N	3748	11
8933	16709	\N	\N	3749	11
8934	16710	\N	\N	3750	11
8935	16711	\N	\N	3751	11
8936	16712	\N	\N	3752	11
8937	16713	\N	\N	3753	11
8938	16714	\N	\N	3754	11
8939	16715	\N	\N	3755	11
8940	16716	\N	\N	3756	11
8941	16717	\N	\N	3757	11
8942	16718	\N	\N	3758	11
8943	16719	\N	\N	3759	11
8944	16720	\N	\N	3760	11
8945	16721	\N	\N	3761	11
8946	16722	\N	\N	3762	11
8947	16723	\N	\N	3763	11
8948	16724	\N	\N	3764	11
8949	16725	\N	\N	3765	11
8950	16726	\N	\N	3766	11
8951	16727	\N	\N	3767	11
8952	16728	\N	\N	3768	11
8953	16729	\N	\N	3769	11
8954	16730	\N	\N	3770	11
8955	16731	\N	\N	3771	11
8956	16732	\N	\N	3772	11
8957	16733	\N	\N	3773	11
8958	16734	\N	\N	3774	11
8959	16735	\N	\N	3775	11
8960	16736	\N	\N	3776	11
8961	16737	\N	\N	3777	11
8962	16738	\N	\N	3778	11
8963	16739	\N	\N	3779	11
8964	16740	\N	\N	3780	11
8965	16741	\N	\N	3781	11
8966	16742	\N	\N	3782	11
8967	16743	\N	\N	3783	11
8968	16744	\N	\N	3784	11
8969	16745	\N	\N	3785	11
8970	16746	\N	\N	3786	11
8971	16747	\N	\N	3787	11
8972	16748	\N	\N	3788	11
8973	16749	\N	\N	3789	11
8974	16750	\N	\N	3790	11
8975	16751	\N	\N	3791	11
8976	16752	\N	\N	3792	11
8977	16753	\N	\N	3793	11
8978	16754	\N	\N	3794	11
8979	16755	\N	\N	3795	11
8980	16756	\N	\N	3796	11
8981	16757	\N	\N	3797	11
8982	16758	\N	\N	3798	11
8983	16759	\N	\N	3799	11
8984	16760	\N	\N	3800	11
8985	16761	\N	\N	3801	11
8986	16762	\N	\N	3802	11
8987	16763	\N	\N	3803	11
8988	16764	\N	\N	3804	11
8989	16765	\N	\N	3805	11
8990	16766	\N	\N	3806	11
8991	16767	\N	\N	3807	11
8992	16768	\N	\N	3808	11
8993	16769	\N	\N	3809	11
8994	16770	\N	\N	3810	11
8995	16771	\N	\N	3811	11
8996	16772	\N	\N	3812	11
8997	16773	\N	\N	3813	11
8998	16774	\N	\N	3814	11
8999	16775	\N	\N	3815	11
9000	16776	\N	\N	3816	11
9001	16777	\N	\N	3817	11
9002	16778	\N	\N	3818	11
9003	16779	\N	\N	3819	11
9004	16780	\N	\N	3820	11
9005	16781	\N	\N	3821	11
9006	16782	\N	\N	3822	11
9007	16783	\N	\N	3823	11
9008	16784	\N	\N	3824	11
9009	16785	\N	\N	3825	11
9010	16786	\N	\N	3826	11
9011	16787	\N	\N	3827	11
9012	16788	\N	\N	3828	11
9013	16789	\N	\N	3829	11
9014	16790	\N	\N	3830	11
9015	16791	\N	\N	3831	11
9016	16792	\N	\N	3832	11
9017	16793	\N	\N	3833	11
9018	16794	\N	\N	3834	11
9019	16795	\N	\N	3835	11
9020	16796	\N	\N	3836	11
9021	16797	\N	\N	3837	11
9022	16798	\N	\N	3838	11
9023	16799	\N	\N	3839	11
9024	16800	\N	\N	3840	11
9025	16801	\N	\N	3841	11
9026	16802	\N	\N	3842	11
9027	16803	\N	\N	3843	11
9028	16804	\N	\N	3844	11
9029	16805	\N	\N	3845	11
9030	16806	\N	\N	3846	11
9031	16807	\N	\N	3847	11
9032	16808	\N	\N	3848	11
9033	16809	\N	\N	3849	11
9034	16810	\N	\N	3850	11
9035	16811	\N	\N	3851	11
9036	16812	\N	\N	3852	11
9037	16813	\N	\N	3853	11
9038	16814	\N	\N	3854	11
9039	16815	\N	\N	3855	11
9040	16816	\N	\N	3856	11
9041	16817	\N	\N	3857	11
9042	16818	\N	\N	3858	11
9043	16819	\N	\N	3859	11
9044	16820	\N	\N	3860	11
9045	16821	\N	\N	3861	11
9046	16822	\N	\N	3862	11
9047	16823	\N	\N	3863	11
9048	16824	\N	\N	3864	11
9049	16825	\N	\N	3865	11
9050	16826	\N	\N	3866	11
9051	16827	\N	\N	3867	11
9052	16828	\N	\N	3868	11
9053	16829	\N	\N	3869	11
9054	16830	\N	\N	3870	11
9055	16831	\N	\N	3871	11
9056	16832	\N	\N	3872	11
9057	16833	\N	\N	3873	11
9058	16834	\N	\N	3874	11
9059	16835	\N	\N	3875	11
9060	16836	\N	\N	3876	11
9061	16837	\N	\N	3877	11
9062	16838	\N	\N	3878	11
9063	16839	\N	\N	3879	11
9064	16840	\N	\N	3880	11
9065	16841	\N	\N	3881	11
9066	16842	\N	\N	3882	11
9067	16843	\N	\N	3883	11
9068	16844	\N	\N	3884	11
9069	16845	\N	\N	3885	11
9070	16846	\N	\N	3886	11
9071	16847	\N	\N	3887	11
9072	16848	\N	\N	3888	11
9073	16849	\N	\N	3889	11
9074	16850	\N	\N	3890	11
9075	16851	\N	\N	3891	11
9076	16852	\N	\N	3892	11
9077	16853	\N	\N	3893	11
9078	16854	\N	\N	3894	11
9079	16855	\N	\N	3895	11
9080	16856	\N	\N	3896	11
9081	16857	\N	\N	3897	11
9082	16858	\N	\N	3898	11
9083	16859	\N	\N	3899	11
9084	16860	\N	\N	3900	11
9085	16861	\N	\N	3901	11
9086	16862	\N	\N	3902	11
9087	16863	\N	\N	3903	11
9088	16864	\N	\N	3904	11
9089	16865	\N	\N	3905	11
9090	16866	\N	\N	3906	11
9091	16867	\N	\N	3907	11
9092	16868	\N	\N	3908	11
9093	16869	\N	\N	3909	11
9094	16870	\N	\N	3910	11
9095	16871	\N	\N	3911	11
9096	16872	\N	\N	3912	11
9097	16873	\N	\N	3913	11
9098	16874	\N	\N	3914	11
9099	16875	\N	\N	3915	11
9100	16876	\N	\N	3916	11
9101	16877	\N	\N	3917	11
9102	16878	\N	\N	3918	11
9103	16879	\N	\N	3919	11
9104	16880	\N	\N	3920	11
9105	16881	\N	\N	3921	11
9106	16882	\N	\N	3922	11
9107	16883	\N	\N	3923	11
9108	16884	\N	\N	3924	11
9109	16885	\N	\N	3925	11
9110	16886	\N	\N	3926	11
9111	16887	\N	\N	3927	11
9112	16888	\N	\N	3928	11
9113	16889	\N	\N	3929	11
9114	16890	\N	\N	3930	11
9115	16891	\N	\N	3931	11
9116	16892	\N	\N	3932	11
9117	16893	\N	\N	3933	11
9118	16894	\N	\N	3934	11
9119	16895	\N	\N	3935	11
9120	16896	\N	\N	3936	11
9121	16897	\N	\N	3937	11
9122	16898	\N	\N	3938	11
9123	16899	\N	\N	3939	11
9124	16900	\N	\N	3940	11
9125	16901	\N	\N	3941	11
9126	16902	\N	\N	3942	11
9127	16903	\N	\N	3943	11
9128	16904	\N	\N	3944	11
9129	16905	\N	\N	3945	11
9130	16906	\N	\N	3946	11
9131	16907	\N	\N	3947	11
9132	16908	\N	\N	3948	11
9133	16909	\N	\N	3949	11
9134	16910	\N	\N	3950	11
9135	16911	\N	\N	3951	11
9136	16912	\N	\N	3952	11
9137	16913	\N	\N	3953	11
9138	16914	\N	\N	3954	11
9139	16915	\N	\N	3955	11
9140	16916	\N	\N	3956	11
9141	16917	\N	\N	3957	11
9142	16918	\N	\N	3958	11
9143	16919	\N	\N	3959	11
9144	16920	\N	\N	3960	11
9145	16921	\N	\N	3961	11
9146	16922	\N	\N	3962	11
9147	16923	\N	\N	3963	11
9148	16924	\N	\N	3964	11
9149	16925	\N	\N	3965	11
9150	16926	\N	\N	3966	11
9151	16927	\N	\N	3967	11
9152	16928	\N	\N	3968	11
9153	16929	\N	\N	3969	11
9154	16930	\N	\N	3970	11
9155	16931	\N	\N	3971	11
9156	16932	\N	\N	3972	11
9157	16933	\N	\N	3973	11
9158	16934	\N	\N	3974	11
9159	16935	\N	\N	3975	11
9160	16936	\N	\N	3976	11
9161	16937	\N	\N	3977	11
9162	16938	\N	\N	3978	11
9163	16939	\N	\N	3979	11
9164	16940	\N	\N	3980	11
9165	16941	\N	\N	3981	11
9166	16942	\N	\N	3982	11
9167	16943	\N	\N	3983	11
9168	16944	\N	\N	3984	11
9169	16945	\N	\N	3985	11
9170	16946	\N	\N	3986	11
9171	16947	\N	\N	3987	11
9172	16948	\N	\N	3988	11
9173	16949	\N	\N	3989	11
9174	16950	\N	\N	3990	11
9175	16951	\N	\N	3991	11
9176	16952	\N	\N	3992	11
9177	16953	\N	\N	3993	11
9178	16954	\N	\N	3994	11
9179	16955	\N	\N	3995	11
9180	16956	\N	\N	3996	11
9181	16957	\N	\N	3997	11
9182	16958	\N	\N	3998	11
9183	16959	\N	\N	3999	11
9184	16960	\N	\N	4000	11
9185	16961	\N	\N	4001	11
9186	16962	\N	\N	4002	11
9187	16963	\N	\N	4003	11
9188	16964	\N	\N	4004	11
9189	16965	\N	\N	4005	11
9190	16966	\N	\N	4006	11
9191	16967	\N	\N	4007	11
9192	16968	\N	\N	4008	11
9193	16969	\N	\N	4009	11
9194	16970	\N	\N	4010	11
9195	16971	\N	\N	4011	11
9196	16972	\N	\N	4012	11
9197	16973	\N	\N	4013	11
9198	16974	\N	\N	4014	11
9199	16975	\N	\N	4015	11
9200	16976	\N	\N	4016	11
9201	16977	\N	\N	4017	11
9202	16978	\N	\N	4018	11
9203	16979	\N	\N	4019	11
9204	16980	\N	\N	4020	11
9205	16981	\N	\N	4021	11
9206	16982	\N	\N	4022	11
9207	16983	\N	\N	4023	11
9208	16984	\N	\N	4024	11
9209	16985	\N	\N	4025	11
9210	16986	\N	\N	4026	11
9211	16987	\N	\N	4027	11
9212	16988	\N	\N	4028	11
9213	16989	\N	\N	4029	11
9214	16990	\N	\N	4030	11
9215	16991	\N	\N	4031	11
9216	16992	\N	\N	4032	11
9217	16993	\N	\N	4033	11
9218	16994	\N	\N	4034	11
9219	16995	\N	\N	4035	11
9220	16996	\N	\N	4036	11
9221	16997	\N	\N	4037	11
9222	16998	\N	\N	4038	11
9223	16999	\N	\N	4039	11
9224	17000	\N	\N	4040	11
9225	17001	\N	\N	4041	11
9226	17002	\N	\N	4042	11
9227	17003	\N	\N	4043	11
9228	17004	\N	\N	4044	11
9229	17005	\N	\N	4045	11
9230	17006	\N	\N	4046	11
9231	17007	\N	\N	4047	11
9232	17008	\N	\N	4048	11
9233	17009	\N	\N	4049	11
9234	17010	\N	\N	4050	11
9235	17011	\N	\N	4051	11
9236	17012	\N	\N	4052	11
9237	17013	\N	\N	4053	11
9238	17014	\N	\N	4054	11
9239	17015	\N	\N	4055	11
9240	17016	\N	\N	4056	11
9241	17017	\N	\N	4057	11
9242	17018	\N	\N	4058	11
9243	17019	\N	\N	4059	11
9244	17020	\N	\N	4060	11
9245	17021	\N	\N	4061	11
9246	17022	\N	\N	4062	11
9247	17023	\N	\N	4063	11
9248	17024	\N	\N	4064	11
9249	17025	\N	\N	4065	11
9250	17026	\N	\N	4066	11
9251	17027	\N	\N	4067	11
9252	17028	\N	\N	4068	11
9253	17029	\N	\N	4069	11
9254	17030	\N	\N	4070	11
9255	17031	\N	\N	4071	11
9256	17032	\N	\N	4072	11
9257	17033	\N	\N	4073	11
9258	17034	\N	\N	4074	11
9259	17035	\N	\N	4075	11
9260	17036	\N	\N	4076	11
9261	17037	\N	\N	4077	11
9262	17038	\N	\N	4078	11
9263	17039	\N	\N	4079	11
9264	17040	\N	\N	4080	11
9265	17041	\N	\N	4081	11
9266	17042	\N	\N	4082	11
9267	17043	\N	\N	4083	11
9268	17044	\N	\N	4084	11
9269	17045	\N	\N	4085	11
9270	17046	\N	\N	4086	11
9271	17047	\N	\N	4087	11
9272	17048	\N	\N	4088	11
9273	17049	\N	\N	4089	11
9274	17050	\N	\N	4090	11
9275	17051	\N	\N	4091	11
9276	17052	\N	\N	4092	11
9277	17053	\N	\N	4093	11
9278	17054	\N	\N	4094	11
9279	17055	\N	\N	4095	11
9280	17056	\N	\N	4096	11
9281	17057	\N	\N	4097	11
9282	17058	\N	\N	4098	11
9283	17059	\N	\N	4099	11
9284	17060	\N	\N	4100	11
9285	17061	\N	\N	4101	11
9286	17062	\N	\N	4102	11
9287	17063	\N	\N	4103	11
9288	17064	\N	\N	4104	11
9289	17065	\N	\N	4105	11
9290	17066	\N	\N	4106	11
9291	17067	\N	\N	4107	11
9292	17068	\N	\N	4108	11
9293	17069	\N	\N	4109	11
9294	17070	\N	\N	4110	11
9295	17071	\N	\N	4111	11
9296	17072	\N	\N	4112	11
9297	17073	\N	\N	4113	11
9298	17074	\N	\N	4114	11
9299	17075	\N	\N	4115	11
9300	17076	\N	\N	4116	11
9301	17077	\N	\N	4117	11
9302	17078	\N	\N	4118	11
9303	17079	\N	\N	4119	11
9304	17080	\N	\N	4120	11
9305	17081	\N	\N	4121	11
9306	17082	\N	\N	4122	11
9307	17083	\N	\N	4123	11
9308	17084	\N	\N	4124	11
9309	17085	\N	\N	4125	11
9310	17086	\N	\N	4126	11
9311	17087	\N	\N	4127	11
9312	17088	\N	\N	4128	11
9313	17089	\N	\N	4129	11
9314	17090	\N	\N	4130	11
9315	17091	\N	\N	4131	11
9316	17092	\N	\N	4132	11
9317	17093	\N	\N	4133	11
9318	17094	\N	\N	4134	11
9319	17095	\N	\N	4135	11
9320	17096	\N	\N	4136	11
9321	17097	\N	\N	4137	11
9322	17098	\N	\N	4138	11
9323	17099	\N	\N	4139	11
9324	17100	\N	\N	4140	11
9325	17101	\N	\N	4141	11
9326	17102	\N	\N	4142	11
9327	17103	\N	\N	4143	11
9328	17104	\N	\N	4144	11
9329	17105	\N	\N	4145	11
9330	17106	\N	\N	4146	11
9331	17107	\N	\N	4147	11
9332	17108	\N	\N	4148	11
9333	17109	\N	\N	4149	11
9334	17110	\N	\N	4150	11
9335	17111	\N	\N	4151	11
9336	17112	\N	\N	4152	11
9337	17113	\N	\N	4153	11
9338	17114	\N	\N	4154	11
9339	17115	\N	\N	4155	11
9340	17116	\N	\N	4156	11
9341	17117	\N	\N	4157	11
9342	17118	\N	\N	4158	11
9343	17119	\N	\N	4159	11
9344	17120	\N	\N	4160	11
9345	17121	\N	\N	4161	11
9346	17122	\N	\N	4162	11
9347	17123	\N	\N	4163	11
9348	17124	\N	\N	4164	11
9349	17125	\N	\N	4165	11
9350	17126	\N	\N	4166	11
9351	17127	\N	\N	4167	11
9352	17128	\N	\N	4168	11
9353	17129	\N	\N	4169	11
9354	17130	\N	\N	4170	11
9355	17131	\N	\N	4171	11
9356	17132	\N	\N	4172	11
9357	17133	\N	\N	4173	11
9358	17134	\N	\N	4174	11
9359	17135	\N	\N	4175	11
9360	17136	\N	\N	4176	11
9361	17137	\N	\N	4177	11
9362	17138	\N	\N	4178	11
9363	17139	\N	\N	4179	11
9364	17140	\N	\N	4180	11
9365	17141	\N	\N	4181	11
9366	17142	\N	\N	4182	11
9367	17143	\N	\N	4183	11
9368	17144	\N	\N	4184	11
9369	17145	\N	\N	4185	11
9370	17146	\N	\N	4186	11
9371	17147	\N	\N	4187	11
9372	17148	\N	\N	4188	11
9373	17149	\N	\N	4189	11
9374	17150	\N	\N	4190	11
9375	17151	\N	\N	4191	11
9376	17152	\N	\N	4192	11
9377	17153	\N	\N	4193	11
9378	17154	\N	\N	4194	11
9379	17155	\N	\N	4195	11
9380	17156	\N	\N	4196	11
9381	17157	\N	\N	4197	11
9382	17158	\N	\N	4198	11
9383	17159	\N	\N	4199	11
9384	17160	\N	\N	4200	11
9385	17161	\N	\N	4201	11
9386	17162	\N	\N	4202	11
9387	17163	\N	\N	4203	11
9388	17164	\N	\N	4204	11
9389	17165	\N	\N	4205	11
9390	17166	\N	\N	4206	11
9391	17167	\N	\N	4207	11
9392	17168	\N	\N	4208	11
9393	17169	\N	\N	4209	11
9394	17170	\N	\N	4210	11
9395	17171	\N	\N	4211	11
9396	17172	\N	\N	4212	11
9397	17173	\N	\N	4213	11
9398	17174	\N	\N	4214	11
9399	17175	\N	\N	4215	11
9400	17176	\N	\N	4216	11
9401	17177	\N	\N	4217	11
9402	17178	\N	\N	4218	11
9403	17179	\N	\N	4219	11
9404	17180	\N	\N	4220	11
9405	17181	\N	\N	4221	11
9406	17182	\N	\N	4222	11
9407	17183	\N	\N	4223	11
9408	17184	\N	\N	4224	11
9409	17185	\N	\N	4225	11
9410	17186	\N	\N	4226	11
9411	17187	\N	\N	4227	11
9412	17188	\N	\N	4228	11
9413	17189	\N	\N	4229	11
9414	17190	\N	\N	4230	11
9415	17191	\N	\N	4231	11
9416	17192	\N	\N	4232	11
9417	17193	\N	\N	4233	11
9418	17194	\N	\N	4234	11
9419	17195	\N	\N	4235	11
9420	17196	\N	\N	4236	11
9421	17197	\N	\N	4237	11
9422	17198	\N	\N	4238	11
9423	17199	\N	\N	4239	11
9424	17200	\N	\N	4240	11
9425	17201	\N	\N	4241	11
9426	17202	\N	\N	4242	11
9427	17203	\N	\N	4243	11
9428	17204	\N	\N	4244	11
9429	17205	\N	\N	4245	11
9430	17206	\N	\N	4246	11
9431	17207	\N	\N	4247	11
9432	17208	\N	\N	4248	11
9433	17209	\N	\N	4249	11
9434	17210	\N	\N	4250	11
9435	17211	\N	\N	4251	11
9436	17212	\N	\N	4252	11
9437	17213	\N	\N	4253	11
9438	17214	\N	\N	4254	11
9439	17215	\N	\N	4255	11
9440	17216	\N	\N	4256	11
9441	17217	\N	\N	4257	11
9442	17218	\N	\N	4258	11
9443	17219	\N	\N	4259	11
9444	17220	\N	\N	4260	11
9445	17221	\N	\N	4261	11
9446	17222	\N	\N	4262	11
9447	17223	\N	\N	4263	11
9448	17224	\N	\N	4264	11
9449	17225	\N	\N	4265	11
9450	17226	\N	\N	4266	11
9451	17227	\N	\N	4267	11
9452	17228	\N	\N	4268	11
9453	17229	\N	\N	4269	11
9454	17230	\N	\N	4270	11
9455	17231	\N	\N	4271	11
9456	17232	\N	\N	4272	11
9457	17233	\N	\N	4273	11
9458	17234	\N	\N	4274	11
9459	17235	\N	\N	4275	11
9460	17236	\N	\N	4276	11
9461	17237	\N	\N	4277	11
9462	17238	\N	\N	4278	11
9463	17239	\N	\N	4279	11
9464	17240	\N	\N	4280	11
9465	17241	\N	\N	4281	11
9466	17242	\N	\N	4282	11
9467	17243	\N	\N	4283	11
9468	17244	\N	\N	4284	11
9469	17245	\N	\N	4285	11
9470	17246	\N	\N	4286	11
9471	17247	\N	\N	4287	11
9472	17248	\N	\N	4288	11
9473	17249	\N	\N	4289	11
9474	17250	\N	\N	4290	11
9475	17251	\N	\N	4291	11
9476	17252	\N	\N	4292	11
9477	17253	\N	\N	4293	11
9478	17254	\N	\N	4294	11
9479	17255	\N	\N	4295	11
9480	17256	\N	\N	4296	11
9481	17257	\N	\N	4297	11
9482	17258	\N	\N	4298	11
9483	17259	\N	\N	4299	11
9484	17260	\N	\N	4300	11
9485	17261	\N	\N	4301	11
9486	17262	\N	\N	4302	11
9487	17263	\N	\N	4303	11
9488	17264	\N	\N	4304	11
9489	17265	\N	\N	4305	11
9490	17266	\N	\N	4306	11
9491	17267	\N	\N	4307	11
9492	17268	\N	\N	4308	11
9493	17269	\N	\N	4309	11
9494	17270	\N	\N	4310	11
9495	17271	\N	\N	4311	11
9496	17272	\N	\N	4312	11
9497	17273	\N	\N	4313	11
9498	17274	\N	\N	4314	11
9499	17275	\N	\N	4315	11
9500	17276	\N	\N	4316	11
9501	17277	\N	\N	4317	11
9502	17278	\N	\N	4318	11
9503	17279	\N	\N	4319	11
9504	17280	\N	\N	4320	11
\.


--
-- Data for Name: analysis_data_sets; Type: TABLE DATA; Schema: public; Owner: -
--

COPY analysis_data_sets (id, perm_id, cont_id) FROM stdin;
3	20130412153659945-390	1
\.


--
-- Data for Name: channel_stacks; Type: TABLE DATA; Schema: public; Owner: -
--

COPY channel_stacks (id, x, y, z_in_m, t_in_sec, series_number, is_representative, ds_id, spot_id) FROM stdin;
865	2	3	\N	\N	\N	f	2	110
866	2	2	\N	\N	\N	f	2	141
867	2	1	\N	\N	\N	f	2	172
868	2	3	\N	\N	\N	f	2	108
869	2	2	\N	\N	\N	f	2	139
870	2	1	\N	\N	\N	f	2	170
871	3	2	\N	\N	\N	f	2	115
872	2	1	\N	\N	\N	f	2	114
873	3	1	\N	\N	\N	f	2	146
874	2	3	\N	\N	\N	f	2	106
875	2	2	\N	\N	\N	f	2	137
876	2	1	\N	\N	\N	f	2	168
877	3	3	\N	\N	\N	f	2	166
878	3	2	\N	\N	\N	f	2	126
879	3	1	\N	\N	\N	f	2	157
880	3	2	\N	\N	\N	f	2	191
881	3	3	\N	\N	\N	f	2	160
882	1	1	\N	\N	\N	t	2	107
883	2	1	\N	\N	\N	f	2	105
884	2	3	\N	\N	\N	f	2	181
885	2	3	\N	\N	\N	f	2	136
886	2	2	\N	\N	\N	f	2	167
887	3	2	\N	\N	\N	f	2	108
888	3	1	\N	\N	\N	f	2	139
889	3	2	\N	\N	\N	f	2	183
890	3	3	\N	\N	\N	f	2	152
891	3	1	\N	\N	\N	f	2	142
892	3	2	\N	\N	\N	f	2	111
893	2	1	\N	\N	\N	f	2	97
894	3	3	\N	\N	\N	f	2	187
895	3	3	\N	\N	\N	f	2	135
896	3	2	\N	\N	\N	f	2	166
897	3	1	\N	\N	\N	f	2	104
898	1	1	\N	\N	\N	t	2	111
899	3	2	\N	\N	\N	f	2	107
900	3	1	\N	\N	\N	f	2	138
901	1	3	\N	\N	\N	f	2	177
902	1	3	\N	\N	\N	f	2	144
903	1	2	\N	\N	\N	f	2	175
904	2	1	\N	\N	\N	f	2	182
905	2	3	\N	\N	\N	f	2	120
906	2	2	\N	\N	\N	f	2	151
907	1	1	\N	\N	\N	t	2	115
908	1	3	\N	\N	\N	f	2	140
909	1	2	\N	\N	\N	f	2	171
910	1	1	\N	\N	\N	t	2	127
911	2	1	\N	\N	\N	f	2	134
912	2	2	\N	\N	\N	f	2	103
913	3	2	\N	\N	\N	f	2	127
914	3	1	\N	\N	\N	f	2	158
915	2	2	\N	\N	\N	f	2	185
916	2	3	\N	\N	\N	f	2	154
917	2	1	\N	\N	\N	f	2	189
918	1	3	\N	\N	\N	f	2	136
919	2	3	\N	\N	\N	f	2	127
920	1	2	\N	\N	\N	f	2	167
921	2	2	\N	\N	\N	f	2	158
922	2	3	\N	\N	\N	f	2	185
923	2	3	\N	\N	\N	f	2	179
924	1	3	\N	\N	\N	f	2	180
925	2	1	\N	\N	\N	f	2	186
926	2	3	\N	\N	\N	f	2	124
927	2	2	\N	\N	\N	f	2	155
928	1	2	\N	\N	\N	f	2	187
929	2	3	\N	\N	\N	f	2	182
930	1	3	\N	\N	\N	f	2	156
931	2	1	\N	\N	\N	f	2	142
932	2	2	\N	\N	\N	f	2	111
933	1	1	\N	\N	\N	t	2	183
934	1	3	\N	\N	\N	f	2	121
935	1	2	\N	\N	\N	f	2	152
936	3	2	\N	\N	\N	f	2	135
937	3	3	\N	\N	\N	f	2	104
938	3	1	\N	\N	\N	f	2	166
939	1	3	\N	\N	\N	f	2	148
940	1	2	\N	\N	\N	f	2	179
941	3	3	\N	\N	\N	f	2	169
942	3	3	\N	\N	\N	f	2	116
943	3	2	\N	\N	\N	f	2	147
944	3	1	\N	\N	\N	f	2	178
945	1	1	\N	\N	\N	t	2	103
946	1	1	\N	\N	\N	t	2	130
947	1	2	\N	\N	\N	f	2	99
948	2	1	\N	\N	\N	f	2	117
949	1	2	\N	\N	\N	f	2	124
950	1	1	\N	\N	\N	t	2	155
951	3	3	\N	\N	\N	f	2	175
952	1	3	\N	\N	\N	f	2	106
953	1	2	\N	\N	\N	f	2	137
954	1	1	\N	\N	\N	t	2	168
955	1	1	\N	\N	\N	t	2	186
956	1	3	\N	\N	\N	f	2	124
957	3	1	\N	\N	\N	f	2	119
958	1	2	\N	\N	\N	f	2	155
959	2	3	\N	\N	\N	f	2	162
960	3	2	\N	\N	\N	f	2	143
961	3	3	\N	\N	\N	f	2	112
962	3	1	\N	\N	\N	f	2	174
963	2	1	\N	\N	\N	f	2	113
964	3	3	\N	\N	\N	f	2	173
965	3	2	\N	\N	\N	f	2	185
966	3	3	\N	\N	\N	f	2	154
967	1	3	\N	\N	\N	f	2	128
968	1	2	\N	\N	\N	f	2	159
969	1	1	\N	\N	\N	t	2	190
970	2	1	\N	\N	\N	f	2	133
971	2	2	\N	\N	\N	f	2	102
972	3	1	\N	\N	\N	f	2	182
973	3	3	\N	\N	\N	f	2	120
974	3	2	\N	\N	\N	f	2	151
975	3	3	\N	\N	\N	f	2	167
976	1	1	\N	\N	\N	t	2	192
977	1	3	\N	\N	\N	f	2	130
978	1	2	\N	\N	\N	f	2	161
979	2	3	\N	\N	\N	f	2	133
980	2	2	\N	\N	\N	f	2	164
981	1	3	\N	\N	\N	f	2	116
982	1	2	\N	\N	\N	f	2	147
983	1	1	\N	\N	\N	t	2	178
984	2	2	\N	\N	\N	f	2	143
985	2	3	\N	\N	\N	f	2	112
986	2	1	\N	\N	\N	f	2	174
987	1	3	\N	\N	\N	f	2	174
988	3	1	\N	\N	\N	f	2	181
989	3	3	\N	\N	\N	f	2	119
990	3	2	\N	\N	\N	f	2	150
991	3	3	\N	\N	\N	f	2	184
992	3	1	\N	\N	\N	f	2	125
993	3	2	\N	\N	\N	f	2	134
994	3	3	\N	\N	\N	f	2	103
995	3	1	\N	\N	\N	f	2	165
996	2	3	\N	\N	\N	f	2	129
997	2	2	\N	\N	\N	f	2	160
998	2	1	\N	\N	\N	f	2	191
999	2	2	\N	\N	\N	f	2	184
1000	2	3	\N	\N	\N	f	2	153
1001	2	3	\N	\N	\N	f	2	115
1002	2	2	\N	\N	\N	f	2	146
1003	2	1	\N	\N	\N	f	2	177
1004	2	1	\N	\N	\N	f	2	122
1005	3	3	\N	\N	\N	f	2	139
1006	3	2	\N	\N	\N	f	2	170
1007	3	3	\N	\N	\N	f	2	133
1008	3	2	\N	\N	\N	f	2	164
1009	1	3	\N	\N	\N	f	2	165
1010	3	1	\N	\N	\N	f	2	102
1011	2	3	\N	\N	\N	f	2	145
1012	2	2	\N	\N	\N	f	2	176
1013	1	2	\N	\N	\N	f	2	128
1014	1	3	\N	\N	\N	f	2	97
1015	1	1	\N	\N	\N	t	2	159
1016	2	3	\N	\N	\N	f	2	138
1017	2	2	\N	\N	\N	f	2	169
1018	2	1	\N	\N	\N	f	2	183
1019	2	3	\N	\N	\N	f	2	121
1020	2	2	\N	\N	\N	f	2	152
1021	2	1	\N	\N	\N	f	2	101
1022	1	3	\N	\N	\N	f	2	191
1023	2	3	\N	\N	\N	f	2	147
1024	2	2	\N	\N	\N	f	2	178
1025	2	1	\N	\N	\N	f	2	130
1026	2	2	\N	\N	\N	f	2	99
1027	1	3	\N	\N	\N	f	2	168
1028	1	1	\N	\N	\N	t	2	106
1029	1	3	\N	\N	\N	f	2	109
1030	1	2	\N	\N	\N	f	2	140
1031	1	1	\N	\N	\N	t	2	171
1032	1	2	\N	\N	\N	f	2	122
1033	1	1	\N	\N	\N	t	2	153
1034	1	3	\N	\N	\N	f	2	172
1035	1	2	\N	\N	\N	f	2	114
1036	1	1	\N	\N	\N	t	2	145
1037	3	2	\N	\N	\N	f	2	187
1038	3	3	\N	\N	\N	f	2	156
1039	1	3	\N	\N	\N	f	2	189
1040	3	1	\N	\N	\N	f	2	118
1041	2	2	\N	\N	\N	f	2	107
1042	2	1	\N	\N	\N	f	2	138
1043	3	3	\N	\N	\N	f	2	148
1044	3	2	\N	\N	\N	f	2	179
1045	2	1	\N	\N	\N	f	2	99
1046	3	1	\N	\N	\N	f	2	98
1047	2	3	\N	\N	\N	f	2	141
1048	2	2	\N	\N	\N	f	2	172
1049	3	2	\N	\N	\N	f	2	117
1050	3	1	\N	\N	\N	f	2	148
1051	2	2	\N	\N	\N	f	2	123
1052	2	1	\N	\N	\N	f	2	154
1053	1	1	\N	\N	\N	t	2	136
1054	1	2	\N	\N	\N	f	2	116
1055	1	2	\N	\N	\N	f	2	105
1056	1	1	\N	\N	\N	t	2	147
1057	3	1	\N	\N	\N	f	2	123
1058	3	3	\N	\N	\N	f	2	110
1059	3	2	\N	\N	\N	f	2	141
1060	3	1	\N	\N	\N	f	2	172
1061	1	3	\N	\N	\N	f	2	160
1062	1	2	\N	\N	\N	f	2	191
1063	3	3	\N	\N	\N	f	2	171
1064	1	3	\N	\N	\N	f	2	142
1065	1	2	\N	\N	\N	f	2	173
1066	2	3	\N	\N	\N	f	2	189
1067	1	1	\N	\N	\N	t	2	98
1068	3	1	\N	\N	\N	f	2	110
1069	2	2	\N	\N	\N	f	2	192
1070	2	3	\N	\N	\N	f	2	161
1071	1	1	\N	\N	\N	t	2	143
1072	1	2	\N	\N	\N	f	2	112
1073	1	1	\N	\N	\N	t	2	124
1074	1	2	\N	\N	\N	f	2	119
1075	1	1	\N	\N	\N	t	2	150
1076	2	2	\N	\N	\N	f	2	116
1077	2	1	\N	\N	\N	f	2	147
1078	2	3	\N	\N	\N	f	2	166
1079	3	3	\N	\N	\N	f	2	140
1080	3	2	\N	\N	\N	f	2	171
1081	2	2	\N	\N	\N	f	2	120
1082	2	1	\N	\N	\N	f	2	151
1083	1	1	\N	\N	\N	t	2	100
1084	3	3	\N	\N	\N	f	2	163
1085	3	3	\N	\N	\N	f	2	136
1086	3	2	\N	\N	\N	f	2	167
1087	1	2	\N	\N	\N	f	2	120
1088	1	1	\N	\N	\N	t	2	151
1089	2	3	\N	\N	\N	f	2	190
1090	1	3	\N	\N	\N	f	2	159
1091	1	2	\N	\N	\N	f	2	190
1092	1	3	\N	\N	\N	f	2	184
1093	1	1	\N	\N	\N	t	2	119
1094	3	2	\N	\N	\N	f	2	136
1095	3	3	\N	\N	\N	f	2	105
1096	3	1	\N	\N	\N	f	2	167
1097	3	3	\N	\N	\N	f	2	190
1098	2	3	\N	\N	\N	f	2	131
1099	2	2	\N	\N	\N	f	2	162
1100	1	1	\N	\N	\N	t	2	120
1101	1	2	\N	\N	\N	f	2	107
1102	1	1	\N	\N	\N	t	2	138
1103	3	3	\N	\N	\N	f	2	107
1104	3	2	\N	\N	\N	f	2	138
1105	3	1	\N	\N	\N	f	2	169
1106	2	1	\N	\N	\N	f	2	181
1107	2	3	\N	\N	\N	f	2	119
1108	2	2	\N	\N	\N	f	2	150
1109	1	3	\N	\N	\N	f	2	139
1110	1	2	\N	\N	\N	f	2	170
1111	3	3	\N	\N	\N	f	2	170
1112	1	1	\N	\N	\N	t	2	112
1113	3	1	\N	\N	\N	f	2	121
1114	1	1	\N	\N	\N	t	2	101
1115	1	3	\N	\N	\N	f	2	163
1116	1	3	\N	\N	\N	f	2	166
1117	3	2	\N	\N	\N	f	2	120
1118	3	1	\N	\N	\N	f	2	151
1119	2	2	\N	\N	\N	f	2	114
1120	2	1	\N	\N	\N	f	2	145
1121	2	3	\N	\N	\N	f	2	143
1122	2	2	\N	\N	\N	f	2	174
1123	2	1	\N	\N	\N	f	2	185
1124	2	3	\N	\N	\N	f	2	123
1125	2	2	\N	\N	\N	f	2	154
1126	2	3	\N	\N	\N	f	2	174
1127	3	3	\N	\N	\N	f	2	186
1128	3	3	\N	\N	\N	f	2	149
1129	3	2	\N	\N	\N	f	2	180
1130	2	2	\N	\N	\N	f	2	182
1131	2	3	\N	\N	\N	f	2	151
1132	2	1	\N	\N	\N	f	2	187
1133	2	3	\N	\N	\N	f	2	125
1134	2	2	\N	\N	\N	f	2	156
1135	3	2	\N	\N	\N	f	2	132
1136	3	3	\N	\N	\N	f	2	101
1137	3	1	\N	\N	\N	f	2	163
1138	1	2	\N	\N	\N	f	2	144
1139	1	3	\N	\N	\N	f	2	113
1140	1	1	\N	\N	\N	t	2	175
1141	2	2	\N	\N	\N	f	2	144
1142	2	3	\N	\N	\N	f	2	113
1143	2	1	\N	\N	\N	f	2	175
1144	3	2	\N	\N	\N	f	2	122
1145	3	1	\N	\N	\N	f	2	153
1146	1	3	\N	\N	\N	f	2	175
1147	3	1	\N	\N	\N	f	2	130
1148	3	2	\N	\N	\N	f	2	99
1149	3	1	\N	\N	\N	f	2	128
1150	3	2	\N	\N	\N	f	2	97
1151	2	1	\N	\N	\N	f	2	131
1152	2	2	\N	\N	\N	f	2	100
1153	3	3	\N	\N	\N	f	2	162
1154	2	1	\N	\N	\N	f	2	124
1155	2	1	\N	\N	\N	f	2	128
1156	2	2	\N	\N	\N	f	2	97
1157	2	3	\N	\N	\N	f	2	116
1158	3	3	\N	\N	\N	f	2	109
1159	3	2	\N	\N	\N	f	2	140
1160	2	2	\N	\N	\N	f	2	147
1161	2	1	\N	\N	\N	f	2	178
1162	3	1	\N	\N	\N	f	2	171
1163	3	3	\N	\N	\N	f	2	141
1164	3	3	\N	\N	\N	f	2	178
1165	3	2	\N	\N	\N	f	2	172
1166	2	2	\N	\N	\N	f	2	106
1167	2	1	\N	\N	\N	f	2	137
1168	1	1	\N	\N	\N	t	2	181
1169	1	3	\N	\N	\N	f	2	119
1170	1	2	\N	\N	\N	f	2	150
1171	3	3	\N	\N	\N	f	2	176
1172	3	1	\N	\N	\N	f	2	114
1173	1	1	\N	\N	\N	t	2	126
1174	3	2	\N	\N	\N	f	2	124
1175	3	1	\N	\N	\N	f	2	155
1176	2	1	\N	\N	\N	f	2	103
1177	1	1	\N	\N	\N	t	2	132
1178	1	2	\N	\N	\N	f	2	101
1179	1	3	\N	\N	\N	f	2	183
1180	2	3	\N	\N	\N	f	2	191
1181	2	1	\N	\N	\N	f	2	143
1182	1	2	\N	\N	\N	f	2	125
1183	2	2	\N	\N	\N	f	2	112
1184	1	1	\N	\N	\N	t	2	156
1185	3	3	\N	\N	\N	f	2	145
1186	3	2	\N	\N	\N	f	2	176
1187	3	2	\N	\N	\N	f	2	184
1188	3	3	\N	\N	\N	f	2	153
1189	3	1	\N	\N	\N	f	2	192
1190	3	3	\N	\N	\N	f	2	130
1191	3	2	\N	\N	\N	f	2	161
1192	2	2	\N	\N	\N	f	2	135
1193	2	3	\N	\N	\N	f	2	104
1194	2	1	\N	\N	\N	f	2	166
1195	2	1	\N	\N	\N	f	2	126
1196	2	2	\N	\N	\N	f	2	110
1197	2	1	\N	\N	\N	f	2	141
1198	1	1	\N	\N	\N	t	2	117
1199	3	2	\N	\N	\N	f	2	182
1200	2	1	\N	\N	\N	f	2	129
1201	2	2	\N	\N	\N	f	2	98
1202	3	1	\N	\N	\N	f	2	113
1203	3	3	\N	\N	\N	f	2	151
1204	3	2	\N	\N	\N	f	2	123
1205	3	1	\N	\N	\N	f	2	154
1206	2	3	\N	\N	\N	f	2	149
1207	2	2	\N	\N	\N	f	2	180
1208	3	3	\N	\N	\N	f	2	128
1209	3	2	\N	\N	\N	f	2	159
1210	3	1	\N	\N	\N	f	2	190
1211	1	2	\N	\N	\N	f	2	130
1212	1	3	\N	\N	\N	f	2	99
1213	3	3	\N	\N	\N	f	2	147
1214	1	1	\N	\N	\N	t	2	161
1215	3	2	\N	\N	\N	f	2	178
1216	1	2	\N	\N	\N	f	2	109
1217	1	1	\N	\N	\N	t	2	140
1218	1	2	\N	\N	\N	f	2	182
1219	1	3	\N	\N	\N	f	2	151
1220	3	3	\N	\N	\N	f	2	144
1221	2	3	\N	\N	\N	f	2	142
1222	2	2	\N	\N	\N	f	2	173
1223	3	2	\N	\N	\N	f	2	175
1224	1	3	\N	\N	\N	f	2	192
1225	3	3	\N	\N	\N	f	2	137
1226	3	2	\N	\N	\N	f	2	168
1227	1	2	\N	\N	\N	f	2	185
1228	1	3	\N	\N	\N	f	2	154
1229	2	3	\N	\N	\N	f	2	175
1230	1	3	\N	\N	\N	f	2	145
1231	1	2	\N	\N	\N	f	2	176
1232	3	1	\N	\N	\N	f	2	109
1233	3	3	\N	\N	\N	f	2	182
1234	1	3	\N	\N	\N	f	2	108
1235	1	2	\N	\N	\N	f	2	139
1236	1	1	\N	\N	\N	t	2	170
1237	2	3	\N	\N	\N	f	2	132
1238	2	2	\N	\N	\N	f	2	163
1239	2	1	\N	\N	\N	f	2	98
1240	3	1	\N	\N	\N	f	2	105
1241	1	1	\N	\N	\N	t	2	144
1242	1	2	\N	\N	\N	f	2	113
1243	1	2	\N	\N	\N	f	2	132
1244	1	3	\N	\N	\N	f	2	101
1245	1	1	\N	\N	\N	t	2	163
1246	3	1	\N	\N	\N	f	2	122
1247	3	1	\N	\N	\N	f	2	111
1248	1	3	\N	\N	\N	f	2	131
1249	1	2	\N	\N	\N	f	2	162
1250	1	3	\N	\N	\N	f	2	186
1251	1	1	\N	\N	\N	t	2	99
1252	1	2	\N	\N	\N	f	2	188
1253	1	3	\N	\N	\N	f	2	157
1254	1	1	\N	\N	\N	t	2	135
1255	1	2	\N	\N	\N	f	2	133
1256	1	2	\N	\N	\N	f	2	104
1257	1	3	\N	\N	\N	f	2	102
1258	1	1	\N	\N	\N	t	2	164
1259	2	3	\N	\N	\N	f	2	170
1260	2	3	\N	\N	\N	f	2	172
1261	2	2	\N	\N	\N	f	2	118
1262	2	1	\N	\N	\N	f	2	149
1263	2	2	\N	\N	\N	f	2	186
1264	2	3	\N	\N	\N	f	2	155
1265	3	1	\N	\N	\N	f	2	183
1266	3	3	\N	\N	\N	f	2	121
1267	3	2	\N	\N	\N	f	2	152
1268	2	3	\N	\N	\N	f	2	146
1269	2	2	\N	\N	\N	f	2	177
1270	2	3	\N	\N	\N	f	2	187
1271	1	3	\N	\N	\N	f	2	149
1272	1	2	\N	\N	\N	f	2	180
1273	2	2	\N	\N	\N	f	2	131
1274	2	3	\N	\N	\N	f	2	100
1275	2	1	\N	\N	\N	f	2	162
1276	3	3	\N	\N	\N	f	2	174
1277	2	2	\N	\N	\N	f	2	126
1278	2	1	\N	\N	\N	f	2	157
1279	3	2	\N	\N	\N	f	2	131
1280	3	3	\N	\N	\N	f	2	100
1281	3	1	\N	\N	\N	f	2	162
1282	1	3	\N	\N	\N	f	2	147
1283	1	2	\N	\N	\N	f	2	178
1284	2	2	\N	\N	\N	f	2	127
1285	2	1	\N	\N	\N	f	2	158
1286	3	1	\N	\N	\N	f	2	107
1287	3	1	\N	\N	\N	f	2	186
1288	3	3	\N	\N	\N	f	2	124
1289	3	2	\N	\N	\N	f	2	155
1290	2	2	\N	\N	\N	f	2	130
1291	2	3	\N	\N	\N	f	2	99
1292	2	1	\N	\N	\N	f	2	161
1293	1	3	\N	\N	\N	f	2	115
1294	1	2	\N	\N	\N	f	2	146
1295	1	1	\N	\N	\N	t	2	177
1296	2	1	\N	\N	\N	f	2	110
1297	3	2	\N	\N	\N	f	2	106
1298	3	1	\N	\N	\N	f	2	137
1299	2	3	\N	\N	\N	f	2	178
1300	3	1	\N	\N	\N	f	2	134
1301	3	2	\N	\N	\N	f	2	103
1302	2	2	\N	\N	\N	f	2	189
1303	2	3	\N	\N	\N	f	2	158
1304	2	3	\N	\N	\N	f	2	188
1305	1	3	\N	\N	\N	f	2	164
1306	2	3	\N	\N	\N	f	2	107
1307	1	1	\N	\N	\N	t	2	105
1308	2	2	\N	\N	\N	f	2	138
1309	2	1	\N	\N	\N	f	2	169
1310	2	2	\N	\N	\N	f	2	133
1311	2	3	\N	\N	\N	f	2	102
1312	2	1	\N	\N	\N	f	2	164
1313	2	3	\N	\N	\N	f	2	140
1314	2	2	\N	\N	\N	f	2	171
1315	1	1	\N	\N	\N	t	2	109
1316	2	1	\N	\N	\N	f	2	107
1317	3	2	\N	\N	\N	f	2	109
1318	3	1	\N	\N	\N	f	2	140
1319	2	3	\N	\N	\N	f	2	167
1320	1	1	\N	\N	\N	t	2	128
1321	1	2	\N	\N	\N	f	2	97
1322	3	3	\N	\N	\N	f	2	114
1323	3	2	\N	\N	\N	f	2	145
1324	3	1	\N	\N	\N	f	2	176
1325	2	3	\N	\N	\N	f	2	134
1326	2	2	\N	\N	\N	f	2	165
1327	3	2	\N	\N	\N	f	2	128
1328	3	3	\N	\N	\N	f	2	97
1329	3	1	\N	\N	\N	f	2	159
1330	3	1	\N	\N	\N	f	2	132
1331	3	2	\N	\N	\N	f	2	101
1332	1	1	\N	\N	\N	t	2	108
1333	1	3	\N	\N	\N	f	2	146
1334	1	2	\N	\N	\N	f	2	177
1335	1	2	\N	\N	\N	f	2	189
1336	1	3	\N	\N	\N	f	2	158
1337	1	2	\N	\N	\N	f	2	115
1338	1	1	\N	\N	\N	t	2	146
1339	3	3	\N	\N	\N	f	2	132
1340	3	2	\N	\N	\N	f	2	163
1341	3	1	\N	\N	\N	f	2	101
1342	3	2	\N	\N	\N	f	2	129
1343	3	3	\N	\N	\N	f	2	98
1344	3	1	\N	\N	\N	f	2	160
1345	3	3	\N	\N	\N	f	2	189
1346	1	1	\N	\N	\N	t	2	121
1347	2	3	\N	\N	\N	f	2	114
1348	2	2	\N	\N	\N	f	2	145
1349	2	1	\N	\N	\N	f	2	176
1350	1	3	\N	\N	\N	f	2	138
1351	1	2	\N	\N	\N	f	2	169
1352	3	3	\N	\N	\N	f	2	188
1353	3	3	\N	\N	\N	f	2	118
1354	3	2	\N	\N	\N	f	2	149
1355	3	1	\N	\N	\N	f	2	180
1356	2	3	\N	\N	\N	f	2	128
1357	2	2	\N	\N	\N	f	2	159
1358	2	1	\N	\N	\N	f	2	190
1359	2	2	\N	\N	\N	f	2	142
1360	2	3	\N	\N	\N	f	2	111
1361	2	1	\N	\N	\N	f	2	173
1362	3	2	\N	\N	\N	f	2	125
1363	3	1	\N	\N	\N	f	2	156
1364	2	2	\N	\N	\N	f	2	136
1365	2	3	\N	\N	\N	f	2	105
1366	2	1	\N	\N	\N	f	2	167
1367	1	3	\N	\N	\N	f	2	178
1368	3	3	\N	\N	\N	f	2	191
1369	1	2	\N	\N	\N	f	2	106
1370	1	3	\N	\N	\N	f	2	118
1371	1	1	\N	\N	\N	t	2	137
1372	1	2	\N	\N	\N	f	2	149
1373	1	1	\N	\N	\N	t	2	180
1374	3	3	\N	\N	\N	f	2	117
1375	3	2	\N	\N	\N	f	2	148
1376	3	1	\N	\N	\N	f	2	179
1377	1	2	\N	\N	\N	f	2	186
1378	1	3	\N	\N	\N	f	2	155
1379	2	2	\N	\N	\N	f	2	124
1380	2	1	\N	\N	\N	f	2	155
1381	3	3	\N	\N	\N	f	2	181
1382	2	1	\N	\N	\N	f	2	121
1383	1	1	\N	\N	\N	t	2	123
1384	2	1	\N	\N	\N	f	2	192
1385	2	3	\N	\N	\N	f	2	130
1386	2	2	\N	\N	\N	f	2	161
1387	3	1	\N	\N	\N	f	2	120
1388	3	3	\N	\N	\N	f	2	172
1389	1	3	\N	\N	\N	f	2	143
1390	1	1	\N	\N	\N	t	2	131
1391	1	2	\N	\N	\N	f	2	100
1392	1	2	\N	\N	\N	f	2	174
1393	1	1	\N	\N	\N	t	2	133
1394	1	2	\N	\N	\N	f	2	102
1395	3	3	\N	\N	\N	f	2	179
1396	3	1	\N	\N	\N	f	2	124
1397	3	1	\N	\N	\N	f	2	188
1398	1	1	\N	\N	\N	t	2	184
1399	3	3	\N	\N	\N	f	2	126
1400	1	3	\N	\N	\N	f	2	122
1401	1	2	\N	\N	\N	f	2	153
1402	3	2	\N	\N	\N	f	2	157
1403	2	1	\N	\N	\N	f	2	119
1404	1	1	\N	\N	\N	t	2	129
1405	1	2	\N	\N	\N	f	2	98
1406	1	3	\N	\N	\N	f	2	170
1407	2	1	\N	\N	\N	f	2	127
1408	1	3	\N	\N	\N	f	2	114
1409	1	2	\N	\N	\N	f	2	145
1410	1	1	\N	\N	\N	t	2	176
1411	2	3	\N	\N	\N	f	2	184
1412	3	3	\N	\N	\N	f	2	185
1413	3	2	\N	\N	\N	f	2	189
1414	3	3	\N	\N	\N	f	2	158
1415	2	2	\N	\N	\N	f	2	129
1416	2	3	\N	\N	\N	f	2	98
1417	2	1	\N	\N	\N	f	2	160
1418	3	1	\N	\N	\N	f	2	100
1419	1	1	\N	\N	\N	t	2	182
1420	1	3	\N	\N	\N	f	2	120
1421	1	2	\N	\N	\N	f	2	151
1422	2	2	\N	\N	\N	f	2	188
1423	2	3	\N	\N	\N	f	2	157
1424	2	3	\N	\N	\N	f	2	176
1425	2	3	\N	\N	\N	f	2	139
1426	2	2	\N	\N	\N	f	2	170
1427	3	1	\N	\N	\N	f	2	117
1428	2	3	\N	\N	\N	f	2	186
1429	2	3	\N	\N	\N	f	2	144
1430	2	2	\N	\N	\N	f	2	175
1431	1	3	\N	\N	\N	f	2	110
1432	1	2	\N	\N	\N	f	2	141
1433	1	1	\N	\N	\N	t	2	172
1434	1	3	\N	\N	\N	f	2	162
1435	1	1	\N	\N	\N	t	2	114
1436	2	2	\N	\N	\N	f	2	121
1437	2	1	\N	\N	\N	f	2	152
1438	1	3	\N	\N	\N	f	2	117
1439	1	2	\N	\N	\N	f	2	148
1440	1	1	\N	\N	\N	t	2	179
1441	1	3	\N	\N	\N	f	2	185
1442	3	3	\N	\N	\N	f	2	134
1443	3	2	\N	\N	\N	f	2	165
1444	2	2	\N	\N	\N	f	2	191
1445	2	3	\N	\N	\N	f	2	160
1446	3	2	\N	\N	\N	f	2	142
1447	2	2	\N	\N	\N	f	2	115
1448	3	3	\N	\N	\N	f	2	111
1449	2	1	\N	\N	\N	f	2	146
1450	3	1	\N	\N	\N	f	2	173
1451	2	1	\N	\N	\N	f	2	132
1452	2	2	\N	\N	\N	f	2	101
1453	1	2	\N	\N	\N	f	2	134
1454	1	3	\N	\N	\N	f	2	103
1455	1	1	\N	\N	\N	t	2	165
1456	3	1	\N	\N	\N	f	2	126
1457	3	3	\N	\N	\N	f	2	165
1458	3	2	\N	\N	\N	f	2	192
1459	3	3	\N	\N	\N	f	2	161
1460	1	2	\N	\N	\N	f	2	142
1461	1	3	\N	\N	\N	f	2	111
1462	1	1	\N	\N	\N	t	2	173
1463	3	3	\N	\N	\N	f	2	142
1464	1	2	\N	\N	\N	f	2	121
1465	1	1	\N	\N	\N	t	2	152
1466	3	2	\N	\N	\N	f	2	173
1467	3	1	\N	\N	\N	f	2	116
1468	2	1	\N	\N	\N	f	2	115
1469	1	3	\N	\N	\N	f	2	134
1470	1	3	\N	\N	\N	f	2	107
1471	1	2	\N	\N	\N	f	2	138
1472	1	2	\N	\N	\N	f	2	165
1473	1	1	\N	\N	\N	t	2	169
1474	3	3	\N	\N	\N	f	2	138
1475	3	2	\N	\N	\N	f	2	169
1476	3	1	\N	\N	\N	f	2	103
1477	3	2	\N	\N	\N	f	2	116
1478	2	2	\N	\N	\N	f	2	119
1479	3	1	\N	\N	\N	f	2	147
1480	2	1	\N	\N	\N	f	2	150
1481	2	1	\N	\N	\N	f	2	123
1482	2	2	\N	\N	\N	f	2	117
1483	2	1	\N	\N	\N	f	2	148
1484	2	3	\N	\N	\N	f	2	169
1485	2	3	\N	\N	\N	f	2	137
1486	2	2	\N	\N	\N	f	2	168
1487	3	2	\N	\N	\N	f	2	110
1488	3	1	\N	\N	\N	f	2	141
1489	2	1	\N	\N	\N	f	2	100
1490	1	2	\N	\N	\N	f	2	136
1491	2	1	\N	\N	\N	f	2	102
1492	1	3	\N	\N	\N	f	2	105
1493	1	1	\N	\N	\N	t	2	167
1494	3	2	\N	\N	\N	f	2	186
1495	3	2	\N	\N	\N	f	2	118
1496	3	3	\N	\N	\N	f	2	155
1497	3	1	\N	\N	\N	f	2	149
1498	1	2	\N	\N	\N	f	2	183
1499	1	3	\N	\N	\N	f	2	152
1500	1	2	\N	\N	\N	f	2	181
1501	1	1	\N	\N	\N	t	2	97
1502	3	2	\N	\N	\N	f	2	114
1503	3	1	\N	\N	\N	f	2	145
1504	1	3	\N	\N	\N	f	2	150
1505	1	2	\N	\N	\N	f	2	118
1506	1	1	\N	\N	\N	t	2	149
1507	2	3	\N	\N	\N	f	2	183
1508	2	3	\N	\N	\N	f	2	163
1509	2	1	\N	\N	\N	f	2	109
1510	3	2	\N	\N	\N	f	2	144
1511	3	3	\N	\N	\N	f	2	113
1512	3	1	\N	\N	\N	f	2	175
1513	1	1	\N	\N	\N	t	2	188
1514	1	3	\N	\N	\N	f	2	126
1515	1	2	\N	\N	\N	f	2	157
1516	3	1	\N	\N	\N	f	2	133
1517	3	2	\N	\N	\N	f	2	102
1518	1	1	\N	\N	\N	t	2	104
1519	3	1	\N	\N	\N	f	2	97
1520	2	3	\N	\N	\N	f	2	159
1521	2	2	\N	\N	\N	f	2	190
1522	1	3	\N	\N	\N	f	2	188
1523	1	3	\N	\N	\N	f	2	132
1524	1	2	\N	\N	\N	f	2	163
1525	1	1	\N	\N	\N	t	2	118
1526	2	2	\N	\N	\N	f	2	134
1527	2	3	\N	\N	\N	f	2	103
1528	2	1	\N	\N	\N	f	2	165
1529	1	3	\N	\N	\N	f	2	133
1530	1	2	\N	\N	\N	f	2	164
1531	1	3	\N	\N	\N	f	2	187
1532	1	3	\N	\N	\N	f	2	135
1533	1	2	\N	\N	\N	f	2	166
1534	1	1	\N	\N	\N	t	2	110
1535	1	3	\N	\N	\N	f	2	167
1536	1	3	\N	\N	\N	f	2	141
1537	1	2	\N	\N	\N	f	2	172
1538	1	2	\N	\N	\N	f	2	143
1539	1	3	\N	\N	\N	f	2	112
1540	1	1	\N	\N	\N	t	2	174
1541	3	1	\N	\N	\N	f	2	187
1542	3	3	\N	\N	\N	f	2	125
1543	3	2	\N	\N	\N	f	2	156
1544	3	3	\N	\N	\N	f	2	177
1545	2	3	\N	\N	\N	f	2	118
1546	2	2	\N	\N	\N	f	2	149
1547	2	1	\N	\N	\N	f	2	180
1548	3	3	\N	\N	\N	f	2	183
1549	1	2	\N	\N	\N	f	2	184
1550	1	3	\N	\N	\N	f	2	153
1551	1	1	\N	\N	\N	t	2	102
1552	1	2	\N	\N	\N	f	2	135
1553	1	3	\N	\N	\N	f	2	104
1554	3	2	\N	\N	\N	f	2	119
1555	3	1	\N	\N	\N	f	2	150
1556	1	1	\N	\N	\N	t	2	166
1557	3	3	\N	\N	\N	f	2	115
1558	3	2	\N	\N	\N	f	2	146
1559	3	1	\N	\N	\N	f	2	177
1560	2	2	\N	\N	\N	f	2	122
1561	2	1	\N	\N	\N	f	2	153
1562	2	3	\N	\N	\N	f	2	180
1563	2	3	\N	\N	\N	f	2	177
1564	3	3	\N	\N	\N	f	2	143
1565	3	2	\N	\N	\N	f	2	174
1566	2	1	\N	\N	\N	f	2	108
1567	1	2	\N	\N	\N	f	2	108
1568	3	1	\N	\N	\N	f	2	112
1569	1	1	\N	\N	\N	t	2	139
1570	3	1	\N	\N	\N	f	2	115
1571	2	1	\N	\N	\N	f	2	144
1572	1	2	\N	\N	\N	f	2	126
1573	3	3	\N	\N	\N	f	2	106
1574	2	2	\N	\N	\N	f	2	113
1575	3	2	\N	\N	\N	f	2	137
1576	3	1	\N	\N	\N	f	2	168
1577	1	1	\N	\N	\N	t	2	157
1578	2	2	\N	\N	\N	f	2	187
1579	2	3	\N	\N	\N	f	2	156
1580	3	3	\N	\N	\N	f	2	108
1581	3	2	\N	\N	\N	f	2	139
1582	3	1	\N	\N	\N	f	2	170
1583	3	1	\N	\N	\N	f	2	135
1584	3	2	\N	\N	\N	f	2	104
1585	1	3	\N	\N	\N	f	2	181
1586	1	3	\N	\N	\N	f	2	169
1587	1	1	\N	\N	\N	t	2	116
1588	1	1	\N	\N	\N	t	2	187
1589	1	3	\N	\N	\N	f	2	125
1590	1	2	\N	\N	\N	f	2	156
1591	2	1	\N	\N	\N	f	2	184
1592	2	3	\N	\N	\N	f	2	122
1593	2	2	\N	\N	\N	f	2	153
1594	3	1	\N	\N	\N	f	2	127
1595	3	3	\N	\N	\N	f	2	164
1596	2	1	\N	\N	\N	f	2	125
1597	3	1	\N	\N	\N	f	2	185
1598	3	3	\N	\N	\N	f	2	123
1599	3	2	\N	\N	\N	f	2	154
1600	2	3	\N	\N	\N	f	2	117
1601	2	2	\N	\N	\N	f	2	148
1602	2	1	\N	\N	\N	f	2	179
1603	3	3	\N	\N	\N	f	2	168
1604	1	1	\N	\N	\N	t	2	189
1605	1	3	\N	\N	\N	f	2	127
1606	1	2	\N	\N	\N	f	2	158
1607	2	3	\N	\N	\N	f	2	192
1608	1	3	\N	\N	\N	f	2	182
1609	3	3	\N	\N	\N	f	2	146
1610	3	2	\N	\N	\N	f	2	177
1611	3	1	\N	\N	\N	f	2	129
1612	3	2	\N	\N	\N	f	2	98
1613	2	3	\N	\N	\N	f	2	165
1614	2	1	\N	\N	\N	f	2	106
1615	2	1	\N	\N	\N	f	2	118
1616	2	3	\N	\N	\N	f	2	109
1617	2	2	\N	\N	\N	f	2	140
1618	2	1	\N	\N	\N	f	2	171
1619	2	1	\N	\N	\N	f	2	116
1620	3	1	\N	\N	\N	f	2	184
1621	3	3	\N	\N	\N	f	2	122
1622	3	2	\N	\N	\N	f	2	153
1623	1	1	\N	\N	\N	t	2	134
1624	1	2	\N	\N	\N	f	2	103
1625	1	3	\N	\N	\N	f	2	129
1626	1	1	\N	\N	\N	t	2	122
1627	1	2	\N	\N	\N	f	2	160
1628	3	3	\N	\N	\N	f	2	180
1629	1	1	\N	\N	\N	t	2	191
1630	3	1	\N	\N	\N	f	2	143
1631	3	2	\N	\N	\N	f	2	112
1632	2	3	\N	\N	\N	f	2	135
1633	2	2	\N	\N	\N	f	2	166
1634	3	1	\N	\N	\N	f	2	131
1635	3	2	\N	\N	\N	f	2	100
1636	1	2	\N	\N	\N	f	2	110
1637	1	1	\N	\N	\N	t	2	141
1638	3	3	\N	\N	\N	f	2	192
1639	3	1	\N	\N	\N	f	2	144
1640	2	1	\N	\N	\N	f	2	120
1641	3	2	\N	\N	\N	f	2	113
1642	2	3	\N	\N	\N	f	2	148
1643	2	2	\N	\N	\N	f	2	179
1644	2	2	\N	\N	\N	f	2	181
1645	2	3	\N	\N	\N	f	2	150
1646	1	3	\N	\N	\N	f	2	179
1647	1	3	\N	\N	\N	f	2	190
1648	2	3	\N	\N	\N	f	2	171
1649	3	3	\N	\N	\N	f	2	131
1650	3	2	\N	\N	\N	f	2	162
1651	3	2	\N	\N	\N	f	2	133
1652	3	3	\N	\N	\N	f	2	102
1653	3	1	\N	\N	\N	f	2	164
1654	1	2	\N	\N	\N	f	2	131
1655	1	3	\N	\N	\N	f	2	100
1656	1	1	\N	\N	\N	t	2	162
1657	2	2	\N	\N	\N	f	2	183
1658	2	3	\N	\N	\N	f	2	152
1659	2	2	\N	\N	\N	f	2	108
1660	2	1	\N	\N	\N	f	2	139
1661	1	3	\N	\N	\N	f	2	137
1662	1	2	\N	\N	\N	f	2	168
1663	1	3	\N	\N	\N	f	2	171
1664	2	2	\N	\N	\N	f	2	109
1665	2	1	\N	\N	\N	f	2	140
1666	1	2	\N	\N	\N	f	2	127
1667	1	1	\N	\N	\N	t	2	158
1668	3	2	\N	\N	\N	f	2	188
1669	3	3	\N	\N	\N	f	2	157
1670	1	2	\N	\N	\N	f	2	129
1671	1	3	\N	\N	\N	f	2	98
1672	1	1	\N	\N	\N	t	2	160
1673	3	1	\N	\N	\N	f	2	189
1674	3	3	\N	\N	\N	f	2	127
1675	3	2	\N	\N	\N	f	2	158
1676	3	1	\N	\N	\N	f	2	108
1677	3	1	\N	\N	\N	f	2	106
1678	2	2	\N	\N	\N	f	2	125
1679	2	1	\N	\N	\N	f	2	156
1680	1	1	\N	\N	\N	t	2	142
1681	1	2	\N	\N	\N	f	2	111
1682	2	1	\N	\N	\N	f	2	136
1683	2	2	\N	\N	\N	f	2	105
1684	2	3	\N	\N	\N	f	2	173
1685	2	3	\N	\N	\N	f	2	168
1686	2	1	\N	\N	\N	f	2	135
1687	1	2	\N	\N	\N	f	2	117
1688	2	2	\N	\N	\N	f	2	104
1689	1	1	\N	\N	\N	t	2	148
1690	2	2	\N	\N	\N	f	2	132
1691	2	3	\N	\N	\N	f	2	101
1692	2	1	\N	\N	\N	f	2	163
1693	1	2	\N	\N	\N	f	2	192
1694	1	3	\N	\N	\N	f	2	161
1695	3	3	\N	\N	\N	f	2	129
1696	3	2	\N	\N	\N	f	2	160
1697	3	1	\N	\N	\N	f	2	191
1698	3	1	\N	\N	\N	f	2	99
1699	3	3	\N	\N	\N	f	2	159
1700	3	2	\N	\N	\N	f	2	190
1701	3	2	\N	\N	\N	f	2	130
1702	3	3	\N	\N	\N	f	2	99
1703	1	1	\N	\N	\N	t	2	113
1704	3	1	\N	\N	\N	f	2	161
1705	1	2	\N	\N	\N	f	2	123
1706	2	1	\N	\N	\N	f	2	111
1707	1	1	\N	\N	\N	t	2	154
1708	2	3	\N	\N	\N	f	2	164
1709	3	1	\N	\N	\N	f	2	136
1710	3	2	\N	\N	\N	f	2	105
1711	2	1	\N	\N	\N	f	2	112
1712	1	1	\N	\N	\N	t	2	185
1713	1	3	\N	\N	\N	f	2	123
1714	1	2	\N	\N	\N	f	2	154
1715	1	1	\N	\N	\N	t	2	125
1716	3	2	\N	\N	\N	f	2	181
1717	3	3	\N	\N	\N	f	2	150
1718	2	1	\N	\N	\N	f	2	104
1719	2	1	\N	\N	\N	f	2	188
1720	2	3	\N	\N	\N	f	2	126
1721	2	2	\N	\N	\N	f	2	157
1722	1	3	\N	\N	\N	f	2	173
1723	1	3	\N	\N	\N	f	2	176
1724	2	2	\N	\N	\N	f	2	128
1725	2	3	\N	\N	\N	f	2	97
1726	2	1	\N	\N	\N	f	2	159
1727	3	2	\N	\N	\N	f	2	121
1728	3	1	\N	\N	\N	f	2	152
1729	1	1	\N	\N	\N	t	3	231
1730	1	2	\N	\N	\N	f	3	200
1731	3	2	\N	\N	\N	f	3	229
1732	3	3	\N	\N	\N	f	3	198
1733	3	1	\N	\N	\N	f	3	260
1734	1	1	\N	\N	\N	t	3	227
1735	1	2	\N	\N	\N	f	3	196
1736	2	1	\N	\N	\N	f	3	221
1737	3	1	\N	\N	\N	f	3	285
1738	3	3	\N	\N	\N	f	3	223
1739	3	2	\N	\N	\N	f	3	254
1740	3	1	\N	\N	\N	f	3	201
1741	1	2	\N	\N	\N	f	3	223
1742	1	1	\N	\N	\N	t	3	254
1743	3	3	\N	\N	\N	f	3	204
1744	3	2	\N	\N	\N	f	3	235
1745	3	1	\N	\N	\N	f	3	266
1746	2	3	\N	\N	\N	f	3	228
1747	2	2	\N	\N	\N	f	3	259
1748	2	3	\N	\N	\N	f	3	260
1749	2	3	\N	\N	\N	f	3	211
1750	2	2	\N	\N	\N	f	3	242
1751	2	1	\N	\N	\N	f	3	273
1752	1	3	\N	\N	\N	f	3	204
1753	1	2	\N	\N	\N	f	3	235
1754	1	1	\N	\N	\N	t	3	266
1755	2	1	\N	\N	\N	f	3	194
1756	2	3	\N	\N	\N	f	3	255
1757	2	2	\N	\N	\N	f	3	286
1758	3	3	\N	\N	\N	f	3	210
1759	2	3	\N	\N	\N	f	3	234
1760	2	3	\N	\N	\N	f	3	236
1761	3	2	\N	\N	\N	f	3	241
1762	2	2	\N	\N	\N	f	3	265
1763	2	2	\N	\N	\N	f	3	267
1764	3	1	\N	\N	\N	f	3	272
1765	1	3	\N	\N	\N	f	3	230
1766	1	2	\N	\N	\N	f	3	261
1767	2	1	\N	\N	\N	f	3	200
1768	1	2	\N	\N	\N	f	3	217
1769	2	3	\N	\N	\N	f	3	232
1770	1	1	\N	\N	\N	t	3	248
1771	2	2	\N	\N	\N	f	3	263
1772	1	2	\N	\N	\N	f	3	203
1773	1	1	\N	\N	\N	t	3	234
1774	2	1	\N	\N	\N	f	3	196
1775	1	3	\N	\N	\N	f	3	284
1776	2	2	\N	\N	\N	f	3	282
1777	1	2	\N	\N	\N	f	3	215
1778	2	3	\N	\N	\N	f	3	251
1779	1	1	\N	\N	\N	t	3	246
1780	1	3	\N	\N	\N	f	3	275
1781	1	1	\N	\N	\N	t	3	224
1782	1	2	\N	\N	\N	f	3	193
1783	2	2	\N	\N	\N	f	3	224
1784	2	3	\N	\N	\N	f	3	193
1785	2	1	\N	\N	\N	f	3	255
1786	3	3	\N	\N	\N	f	3	261
1787	3	1	\N	\N	\N	f	3	230
1788	3	2	\N	\N	\N	f	3	199
1789	1	1	\N	\N	\N	t	3	199
1790	1	1	\N	\N	\N	t	3	238
1791	1	2	\N	\N	\N	f	3	207
1792	2	1	\N	\N	\N	f	3	213
1793	3	3	\N	\N	\N	f	3	278
1794	1	3	\N	\N	\N	f	3	224
1795	1	2	\N	\N	\N	f	3	255
1796	1	1	\N	\N	\N	t	3	286
1797	1	3	\N	\N	\N	f	3	232
1798	1	2	\N	\N	\N	f	3	263
1799	3	1	\N	\N	\N	f	3	194
1800	2	1	\N	\N	\N	f	3	197
1801	2	2	\N	\N	\N	f	3	279
1802	2	3	\N	\N	\N	f	3	248
1803	3	2	\N	\N	\N	f	3	223
1804	3	3	\N	\N	\N	f	3	243
1805	3	1	\N	\N	\N	f	3	254
1806	3	2	\N	\N	\N	f	3	274
1807	1	2	\N	\N	\N	f	3	232
1808	1	3	\N	\N	\N	f	3	201
1809	1	1	\N	\N	\N	t	3	263
1810	3	1	\N	\N	\N	f	3	198
1811	3	1	\N	\N	\N	f	3	195
1812	2	1	\N	\N	\N	f	3	281
1813	2	3	\N	\N	\N	f	3	219
1814	2	2	\N	\N	\N	f	3	250
1815	3	2	\N	\N	\N	f	3	215
1816	3	1	\N	\N	\N	f	3	246
1817	2	2	\N	\N	\N	f	3	288
1818	2	3	\N	\N	\N	f	3	257
1819	1	1	\N	\N	\N	t	3	218
1820	2	3	\N	\N	\N	f	3	280
1821	1	2	\N	\N	\N	f	3	285
1822	1	3	\N	\N	\N	f	3	254
1823	1	3	\N	\N	\N	f	3	256
1824	1	2	\N	\N	\N	f	3	287
1825	3	2	\N	\N	\N	f	3	222
1826	3	1	\N	\N	\N	f	3	253
1827	3	2	\N	\N	\N	f	3	283
1828	3	3	\N	\N	\N	f	3	252
1829	2	1	\N	\N	\N	f	3	223
1830	3	1	\N	\N	\N	f	3	207
1831	3	2	\N	\N	\N	f	3	282
1832	3	3	\N	\N	\N	f	3	251
1833	2	1	\N	\N	\N	f	3	206
1834	2	1	\N	\N	\N	f	3	204
1835	1	3	\N	\N	\N	f	3	240
1836	1	2	\N	\N	\N	f	3	271
1837	3	2	\N	\N	\N	f	3	280
1838	3	3	\N	\N	\N	f	3	249
1839	1	2	\N	\N	\N	f	3	278
1840	1	3	\N	\N	\N	f	3	247
1841	2	3	\N	\N	\N	f	3	264
1842	3	2	\N	\N	\N	f	3	238
1843	3	3	\N	\N	\N	f	3	207
1844	3	1	\N	\N	\N	f	3	269
1845	1	1	\N	\N	\N	t	3	195
1846	3	1	\N	\N	\N	f	3	218
1847	3	1	\N	\N	\N	f	3	216
1848	3	1	\N	\N	\N	f	3	226
1849	3	2	\N	\N	\N	f	3	195
1850	3	3	\N	\N	\N	f	3	255
1851	3	2	\N	\N	\N	f	3	286
1852	1	3	\N	\N	\N	f	3	260
1853	1	3	\N	\N	\N	f	3	236
1854	1	2	\N	\N	\N	f	3	267
1855	1	3	\N	\N	\N	f	3	263
1856	2	3	\N	\N	\N	f	3	288
1857	3	1	\N	\N	\N	f	3	222
1858	2	3	\N	\N	\N	f	3	204
1859	1	2	\N	\N	\N	f	3	211
1860	2	2	\N	\N	\N	f	3	235
1861	1	1	\N	\N	\N	t	3	242
1862	2	1	\N	\N	\N	f	3	266
1863	2	2	\N	\N	\N	f	3	238
1864	2	3	\N	\N	\N	f	3	207
1865	2	1	\N	\N	\N	f	3	269
1866	2	3	\N	\N	\N	f	3	284
1867	2	1	\N	\N	\N	f	3	278
1868	2	3	\N	\N	\N	f	3	216
1869	2	2	\N	\N	\N	f	3	247
1870	2	2	\N	\N	\N	f	3	205
1871	2	1	\N	\N	\N	f	3	236
1872	1	3	\N	\N	\N	f	3	243
1873	1	2	\N	\N	\N	f	3	274
1874	1	1	\N	\N	\N	t	3	282
1875	1	3	\N	\N	\N	f	3	220
1876	1	2	\N	\N	\N	f	3	251
1877	3	2	\N	\N	\N	f	3	288
1878	3	3	\N	\N	\N	f	3	257
1879	3	2	\N	\N	\N	f	3	203
1880	3	1	\N	\N	\N	f	3	234
1881	3	3	\N	\N	\N	f	3	259
1882	2	3	\N	\N	\N	f	3	268
1883	2	3	\N	\N	\N	f	3	213
1884	2	2	\N	\N	\N	f	3	244
1885	2	1	\N	\N	\N	f	3	275
1886	2	3	\N	\N	\N	f	3	285
1887	1	1	\N	\N	\N	t	3	278
1888	1	3	\N	\N	\N	f	3	216
1889	1	2	\N	\N	\N	f	3	247
1890	3	1	\N	\N	\N	f	3	228
1891	3	2	\N	\N	\N	f	3	197
1892	3	3	\N	\N	\N	f	3	287
1893	1	2	\N	\N	\N	f	3	219
1894	1	1	\N	\N	\N	t	3	250
1895	3	1	\N	\N	\N	f	3	225
1896	3	2	\N	\N	\N	f	3	194
1897	3	3	\N	\N	\N	f	3	231
1898	3	2	\N	\N	\N	f	3	262
1899	1	3	\N	\N	\N	f	3	279
1900	1	2	\N	\N	\N	f	3	238
1901	2	1	\N	\N	\N	f	3	238
1902	2	2	\N	\N	\N	f	3	207
1903	1	3	\N	\N	\N	f	3	207
1904	1	1	\N	\N	\N	t	3	269
1905	1	1	\N	\N	\N	t	3	215
1906	2	3	\N	\N	\N	f	3	263
1907	3	2	\N	\N	\N	f	3	231
1908	3	3	\N	\N	\N	f	3	200
1909	3	1	\N	\N	\N	f	3	262
1910	1	3	\N	\N	\N	f	3	277
1911	2	1	\N	\N	\N	f	3	220
1912	2	3	\N	\N	\N	f	3	265
1913	2	2	\N	\N	\N	f	3	217
1914	2	1	\N	\N	\N	f	3	248
1915	3	3	\N	\N	\N	f	3	212
1916	3	2	\N	\N	\N	f	3	243
1917	3	1	\N	\N	\N	f	3	274
1918	1	1	\N	\N	\N	t	3	201
1919	2	2	\N	\N	\N	f	3	213
1920	2	1	\N	\N	\N	f	3	244
1921	2	1	\N	\N	\N	f	3	240
1922	2	2	\N	\N	\N	f	3	209
1923	1	3	\N	\N	\N	f	3	228
1924	1	2	\N	\N	\N	f	3	259
1925	3	2	\N	\N	\N	f	3	278
1926	3	3	\N	\N	\N	f	3	247
1927	2	3	\N	\N	\N	f	3	271
1928	1	3	\N	\N	\N	f	3	283
1929	2	1	\N	\N	\N	f	3	282
1930	2	3	\N	\N	\N	f	3	220
1931	2	2	\N	\N	\N	f	3	251
1932	3	1	\N	\N	\N	f	3	210
1933	3	1	\N	\N	\N	f	3	278
1934	3	3	\N	\N	\N	f	3	216
1935	3	2	\N	\N	\N	f	3	247
1936	3	3	\N	\N	\N	f	3	275
1937	2	2	\N	\N	\N	f	3	210
1938	2	1	\N	\N	\N	f	3	241
1939	1	2	\N	\N	\N	f	3	218
1940	1	1	\N	\N	\N	t	3	249
1941	2	2	\N	\N	\N	f	3	216
1942	2	1	\N	\N	\N	f	3	247
1943	2	1	\N	\N	\N	f	3	209
1944	3	2	\N	\N	\N	f	3	240
1945	3	3	\N	\N	\N	f	3	209
1946	3	1	\N	\N	\N	f	3	271
1947	1	3	\N	\N	\N	f	3	268
1948	2	3	\N	\N	\N	f	3	259
1949	1	1	\N	\N	\N	t	3	200
1950	3	1	\N	\N	\N	f	3	209
1951	1	1	\N	\N	\N	t	3	228
1952	1	2	\N	\N	\N	f	3	197
1953	1	1	\N	\N	\N	t	3	220
1954	1	3	\N	\N	\N	f	3	272
1955	1	1	\N	\N	\N	t	3	207
1956	3	3	\N	\N	\N	f	3	258
1957	2	2	\N	\N	\N	f	3	231
1958	2	3	\N	\N	\N	f	3	200
1959	2	1	\N	\N	\N	f	3	262
1960	2	1	\N	\N	\N	f	3	232
1961	2	2	\N	\N	\N	f	3	201
1962	2	1	\N	\N	\N	f	3	195
1963	1	2	\N	\N	\N	f	3	227
1964	1	3	\N	\N	\N	f	3	196
1965	1	1	\N	\N	\N	t	3	258
1966	1	1	\N	\N	\N	t	3	216
1967	1	2	\N	\N	\N	f	3	279
1968	2	2	\N	\N	\N	f	3	229
1969	2	3	\N	\N	\N	f	3	198
1970	1	3	\N	\N	\N	f	3	248
1971	2	1	\N	\N	\N	f	3	260
1972	1	3	\N	\N	\N	f	3	244
1973	1	2	\N	\N	\N	f	3	275
1974	3	3	\N	\N	\N	f	3	232
1975	2	3	\N	\N	\N	f	3	245
1976	3	2	\N	\N	\N	f	3	263
1977	2	2	\N	\N	\N	f	3	276
1978	1	3	\N	\N	\N	f	3	203
1979	1	2	\N	\N	\N	f	3	234
1980	1	1	\N	\N	\N	t	3	265
1981	3	3	\N	\N	\N	f	3	241
1982	1	3	\N	\N	\N	f	3	264
1983	3	2	\N	\N	\N	f	3	272
1984	3	3	\N	\N	\N	f	3	234
1985	3	2	\N	\N	\N	f	3	265
1986	2	1	\N	\N	\N	f	3	279
1987	2	3	\N	\N	\N	f	3	217
1988	2	2	\N	\N	\N	f	3	248
1989	2	3	\N	\N	\N	f	3	240
1990	2	2	\N	\N	\N	f	3	271
1991	1	3	\N	\N	\N	f	3	265
1992	2	1	\N	\N	\N	f	3	203
1993	1	2	\N	\N	\N	f	3	224
1994	1	3	\N	\N	\N	f	3	193
1995	1	1	\N	\N	\N	t	3	255
1996	1	2	\N	\N	\N	f	3	206
1997	1	1	\N	\N	\N	t	3	237
1998	2	2	\N	\N	\N	f	3	239
1999	2	3	\N	\N	\N	f	3	208
2000	2	1	\N	\N	\N	f	3	270
2001	1	3	\N	\N	\N	f	3	245
2002	1	2	\N	\N	\N	f	3	276
2003	1	2	\N	\N	\N	f	3	282
2004	2	1	\N	\N	\N	f	3	215
2005	1	3	\N	\N	\N	f	3	251
2006	1	1	\N	\N	\N	t	3	281
2007	1	3	\N	\N	\N	f	3	219
2008	1	2	\N	\N	\N	f	3	250
2009	3	1	\N	\N	\N	f	3	280
2010	3	3	\N	\N	\N	f	3	218
2011	3	2	\N	\N	\N	f	3	249
2012	3	3	\N	\N	\N	f	3	238
2013	3	2	\N	\N	\N	f	3	269
2014	1	3	\N	\N	\N	f	3	281
2015	3	3	\N	\N	\N	f	3	267
2016	2	2	\N	\N	\N	f	3	232
2017	2	3	\N	\N	\N	f	3	201
2018	2	1	\N	\N	\N	f	3	263
2019	2	3	\N	\N	\N	f	3	269
2020	3	1	\N	\N	\N	f	3	219
2021	2	1	\N	\N	\N	f	3	207
2022	2	1	\N	\N	\N	f	3	211
2023	3	2	\N	\N	\N	f	3	277
2024	3	3	\N	\N	\N	f	3	246
2025	1	2	\N	\N	\N	f	3	226
2026	1	3	\N	\N	\N	f	3	195
2027	1	1	\N	\N	\N	t	3	257
2028	2	2	\N	\N	\N	f	3	284
2029	2	3	\N	\N	\N	f	3	253
2030	1	3	\N	\N	\N	f	3	235
2031	1	2	\N	\N	\N	f	3	266
2032	3	3	\N	\N	\N	f	3	264
2033	3	1	\N	\N	\N	f	3	284
2034	3	3	\N	\N	\N	f	3	222
2035	3	2	\N	\N	\N	f	3	253
2036	3	2	\N	\N	\N	f	3	224
2037	3	2	\N	\N	\N	f	3	226
2038	3	3	\N	\N	\N	f	3	195
2039	3	3	\N	\N	\N	f	3	193
2040	3	1	\N	\N	\N	f	3	255
2041	3	1	\N	\N	\N	f	3	257
2042	2	3	\N	\N	\N	f	3	261
2043	2	2	\N	\N	\N	f	3	222
2044	2	1	\N	\N	\N	f	3	253
2045	1	1	\N	\N	\N	t	3	240
2046	1	2	\N	\N	\N	f	3	209
2047	3	3	\N	\N	\N	f	3	262
2048	3	1	\N	\N	\N	f	3	239
2049	3	2	\N	\N	\N	f	3	208
2050	1	3	\N	\N	\N	f	3	205
2051	1	2	\N	\N	\N	f	3	236
2052	1	1	\N	\N	\N	t	3	267
2053	3	2	\N	\N	\N	f	3	214
2054	3	1	\N	\N	\N	f	3	245
2055	3	3	\N	\N	\N	f	3	283
2056	3	2	\N	\N	\N	f	3	212
2057	3	1	\N	\N	\N	f	3	243
2058	2	1	\N	\N	\N	f	3	212
2059	3	3	\N	\N	\N	f	3	270
2060	1	3	\N	\N	\N	f	3	280
2061	3	3	\N	\N	\N	f	3	282
2062	3	3	\N	\N	\N	f	3	229
2063	3	2	\N	\N	\N	f	3	260
2064	3	1	\N	\N	\N	f	3	215
2065	3	1	\N	\N	\N	f	3	227
2066	3	2	\N	\N	\N	f	3	196
2067	2	3	\N	\N	\N	f	3	227
2068	2	2	\N	\N	\N	f	3	258
2069	2	1	\N	\N	\N	f	3	230
2070	2	2	\N	\N	\N	f	3	199
2071	1	1	\N	\N	\N	t	3	203
2072	2	2	\N	\N	\N	f	3	220
2073	2	2	\N	\N	\N	f	3	227
2074	2	3	\N	\N	\N	f	3	196
2075	2	1	\N	\N	\N	f	3	251
2076	2	1	\N	\N	\N	f	3	258
2077	3	1	\N	\N	\N	f	3	211
2078	2	1	\N	\N	\N	f	3	224
2079	2	2	\N	\N	\N	f	3	193
2080	3	2	\N	\N	\N	f	3	211
2081	3	1	\N	\N	\N	f	3	242
2082	1	3	\N	\N	\N	f	3	274
2083	3	3	\N	\N	\N	f	3	266
2084	2	3	\N	\N	\N	f	3	244
2085	2	2	\N	\N	\N	f	3	275
2086	2	3	\N	\N	\N	f	3	272
2087	1	3	\N	\N	\N	f	3	212
2088	1	2	\N	\N	\N	f	3	243
2089	1	1	\N	\N	\N	t	3	274
2090	2	1	\N	\N	\N	f	3	283
2091	2	3	\N	\N	\N	f	3	221
2092	2	2	\N	\N	\N	f	3	252
2093	3	3	\N	\N	\N	f	3	285
2094	2	3	\N	\N	\N	f	3	278
2095	1	3	\N	\N	\N	f	3	231
2096	1	2	\N	\N	\N	f	3	262
2097	2	3	\N	\N	\N	f	3	274
2098	3	1	\N	\N	\N	f	3	199
2099	2	3	\N	\N	\N	f	3	225
2100	2	2	\N	\N	\N	f	3	256
2101	2	1	\N	\N	\N	f	3	287
2102	2	3	\N	\N	\N	f	3	276
2103	1	2	\N	\N	\N	f	3	280
2104	1	3	\N	\N	\N	f	3	249
2105	3	2	\N	\N	\N	f	3	232
2106	3	3	\N	\N	\N	f	3	201
2107	3	1	\N	\N	\N	f	3	263
2108	1	3	\N	\N	\N	f	3	239
2109	1	2	\N	\N	\N	f	3	270
2110	3	1	\N	\N	\N	f	3	288
2111	3	3	\N	\N	\N	f	3	226
2112	3	2	\N	\N	\N	f	3	257
2113	1	1	\N	\N	\N	t	3	212
2114	3	2	\N	\N	\N	f	3	281
2115	3	3	\N	\N	\N	f	3	250
2116	3	2	\N	\N	\N	f	3	205
2117	3	1	\N	\N	\N	f	3	236
2118	1	2	\N	\N	\N	f	3	230
2119	1	3	\N	\N	\N	f	3	199
2120	1	1	\N	\N	\N	t	3	261
2121	1	2	\N	\N	\N	f	3	222
2122	1	1	\N	\N	\N	t	3	253
2123	3	1	\N	\N	\N	f	3	282
2124	3	3	\N	\N	\N	f	3	220
2125	3	2	\N	\N	\N	f	3	251
2126	2	1	\N	\N	\N	f	3	277
2127	2	3	\N	\N	\N	f	3	215
2128	2	2	\N	\N	\N	f	3	246
2129	3	3	\N	\N	\N	f	3	276
2130	1	3	\N	\N	\N	f	3	288
2131	2	2	\N	\N	\N	f	3	283
2132	2	3	\N	\N	\N	f	3	252
2133	3	2	\N	\N	\N	f	3	220
2134	3	1	\N	\N	\N	f	3	251
2135	3	2	\N	\N	\N	f	3	217
2136	3	1	\N	\N	\N	f	3	248
2137	3	1	\N	\N	\N	f	3	213
2138	1	3	\N	\N	\N	f	3	233
2139	1	2	\N	\N	\N	f	3	264
2140	3	1	\N	\N	\N	f	3	202
2141	2	3	\N	\N	\N	f	3	224
2142	2	2	\N	\N	\N	f	3	255
2143	2	1	\N	\N	\N	f	3	286
2144	1	1	\N	\N	\N	t	3	226
2145	1	2	\N	\N	\N	f	3	195
2146	2	1	\N	\N	\N	f	3	227
2147	2	2	\N	\N	\N	f	3	196
2148	1	3	\N	\N	\N	f	3	237
2149	1	2	\N	\N	\N	f	3	268
2150	1	1	\N	\N	\N	t	3	193
2151	1	3	\N	\N	\N	f	3	210
2152	1	2	\N	\N	\N	f	3	241
2153	1	1	\N	\N	\N	t	3	272
2154	1	2	\N	\N	\N	f	3	216
2155	1	1	\N	\N	\N	t	3	247
2156	1	3	\N	\N	\N	f	3	285
2157	2	2	\N	\N	\N	f	3	214
2158	2	1	\N	\N	\N	f	3	245
2159	3	1	\N	\N	\N	f	3	283
2160	3	1	\N	\N	\N	f	3	240
2161	3	3	\N	\N	\N	f	3	221
2162	3	2	\N	\N	\N	f	3	209
2163	3	2	\N	\N	\N	f	3	252
2164	2	1	\N	\N	\N	f	3	216
2165	2	3	\N	\N	\N	f	3	235
2166	1	3	\N	\N	\N	f	3	261
2167	2	2	\N	\N	\N	f	3	266
2168	1	1	\N	\N	\N	t	3	217
2169	1	3	\N	\N	\N	f	3	255
2170	1	2	\N	\N	\N	f	3	286
2171	2	2	\N	\N	\N	f	3	285
2172	3	2	\N	\N	\N	f	3	216
2173	3	1	\N	\N	\N	f	3	247
2174	2	3	\N	\N	\N	f	3	254
2175	3	3	\N	\N	\N	f	3	269
2176	3	3	\N	\N	\N	f	3	206
2177	3	2	\N	\N	\N	f	3	237
2178	3	1	\N	\N	\N	f	3	268
2179	2	2	\N	\N	\N	f	3	240
2180	2	3	\N	\N	\N	f	3	209
2181	2	3	\N	\N	\N	f	3	242
2182	2	1	\N	\N	\N	f	3	271
2183	2	2	\N	\N	\N	f	3	273
2184	3	3	\N	\N	\N	f	3	227
2185	3	3	\N	\N	\N	f	3	214
2186	3	2	\N	\N	\N	f	3	245
2187	3	2	\N	\N	\N	f	3	258
2188	3	1	\N	\N	\N	f	3	276
2189	2	1	\N	\N	\N	f	3	239
2190	2	2	\N	\N	\N	f	3	208
2191	2	2	\N	\N	\N	f	3	212
2192	2	1	\N	\N	\N	f	3	243
2193	1	2	\N	\N	\N	f	3	210
2194	1	1	\N	\N	\N	t	3	241
2195	1	3	\N	\N	\N	f	3	227
2196	1	2	\N	\N	\N	f	3	258
2197	1	3	\N	\N	\N	f	3	262
2198	3	3	\N	\N	\N	f	3	235
2199	3	2	\N	\N	\N	f	3	266
2200	1	2	\N	\N	\N	f	3	284
2201	1	3	\N	\N	\N	f	3	253
2202	3	1	\N	\N	\N	f	3	193
2203	1	1	\N	\N	\N	t	3	209
2204	1	3	\N	\N	\N	f	3	282
2205	2	2	\N	\N	\N	f	3	281
2206	1	2	\N	\N	\N	f	3	288
2207	2	3	\N	\N	\N	f	3	250
2208	1	3	\N	\N	\N	f	3	257
2209	1	1	\N	\N	\N	t	3	221
2210	3	2	\N	\N	\N	f	3	219
2211	3	1	\N	\N	\N	f	3	250
2212	1	2	\N	\N	\N	f	3	225
2213	1	3	\N	\N	\N	f	3	194
2214	1	1	\N	\N	\N	t	3	256
2215	2	1	\N	\N	\N	f	3	218
2216	1	2	\N	\N	\N	f	3	213
2217	1	1	\N	\N	\N	t	3	244
2218	1	2	\N	\N	\N	f	3	229
2219	1	3	\N	\N	\N	f	3	198
2220	2	3	\N	\N	\N	f	3	206
2221	2	2	\N	\N	\N	f	3	237
2222	1	1	\N	\N	\N	t	3	260
2223	2	1	\N	\N	\N	f	3	268
2224	3	3	\N	\N	\N	f	3	233
2225	3	2	\N	\N	\N	f	3	264
2226	3	3	\N	\N	\N	f	3	230
2227	3	2	\N	\N	\N	f	3	261
2228	2	1	\N	\N	\N	f	3	214
2229	2	1	\N	\N	\N	f	3	198
2230	1	1	\N	\N	\N	t	3	205
2231	3	3	\N	\N	\N	f	3	245
2232	3	2	\N	\N	\N	f	3	276
2233	1	3	\N	\N	\N	f	3	269
2234	2	2	\N	\N	\N	f	3	228
2235	2	3	\N	\N	\N	f	3	197
2236	2	1	\N	\N	\N	f	3	259
2237	1	1	\N	\N	\N	t	3	202
2238	2	2	\N	\N	\N	f	3	211
2239	2	1	\N	\N	\N	f	3	242
2240	2	2	\N	\N	\N	f	3	287
2241	2	3	\N	\N	\N	f	3	256
2242	1	3	\N	\N	\N	f	3	259
2243	1	1	\N	\N	\N	t	3	197
2244	2	3	\N	\N	\N	f	3	238
2245	2	2	\N	\N	\N	f	3	269
2246	2	1	\N	\N	\N	f	3	219
2247	3	1	\N	\N	\N	f	3	205
2248	3	2	\N	\N	\N	f	3	287
2249	3	3	\N	\N	\N	f	3	256
2250	1	1	\N	\N	\N	t	3	196
2251	3	3	\N	\N	\N	f	3	237
2252	3	2	\N	\N	\N	f	3	268
2253	2	3	\N	\N	\N	f	3	267
2254	3	3	\N	\N	\N	f	3	265
2255	3	3	\N	\N	\N	f	3	279
2256	1	3	\N	\N	\N	f	3	229
2257	3	2	\N	\N	\N	f	3	221
2258	2	3	\N	\N	\N	f	3	210
2259	3	1	\N	\N	\N	f	3	252
2260	2	2	\N	\N	\N	f	3	241
2261	1	2	\N	\N	\N	f	3	260
2262	2	1	\N	\N	\N	f	3	272
2263	1	3	\N	\N	\N	f	3	206
2264	1	2	\N	\N	\N	f	3	237
2265	1	1	\N	\N	\N	t	3	268
2266	1	2	\N	\N	\N	f	3	239
2267	1	3	\N	\N	\N	f	3	208
2268	1	1	\N	\N	\N	t	3	270
2269	2	3	\N	\N	\N	f	3	241
2270	2	2	\N	\N	\N	f	3	272
2271	3	1	\N	\N	\N	f	3	197
2272	3	3	\N	\N	\N	f	3	225
2273	3	2	\N	\N	\N	f	3	256
2274	3	1	\N	\N	\N	f	3	287
2275	2	3	\N	\N	\N	f	3	233
2276	2	2	\N	\N	\N	f	3	264
2277	2	1	\N	\N	\N	f	3	199
2278	1	1	\N	\N	\N	t	3	285
2279	1	3	\N	\N	\N	f	3	223
2280	1	2	\N	\N	\N	f	3	254
2281	2	2	\N	\N	\N	f	3	278
2282	2	3	\N	\N	\N	f	3	247
2283	2	2	\N	\N	\N	f	3	204
2284	2	1	\N	\N	\N	f	3	235
2285	1	1	\N	\N	\N	t	3	284
2286	1	3	\N	\N	\N	f	3	222
2287	1	2	\N	\N	\N	f	3	253
2288	1	1	\N	\N	\N	t	3	232
2289	1	2	\N	\N	\N	f	3	201
2290	2	3	\N	\N	\N	f	3	287
2291	3	1	\N	\N	\N	f	3	231
2292	3	2	\N	\N	\N	f	3	200
2293	2	3	\N	\N	\N	f	3	275
2294	3	2	\N	\N	\N	f	3	213
2295	3	1	\N	\N	\N	f	3	244
2296	2	1	\N	\N	\N	f	3	222
2297	3	1	\N	\N	\N	f	3	232
2298	3	2	\N	\N	\N	f	3	201
2299	2	1	\N	\N	\N	f	3	228
2300	2	2	\N	\N	\N	f	3	197
2301	2	2	\N	\N	\N	f	3	225
2302	2	3	\N	\N	\N	f	3	194
2303	2	1	\N	\N	\N	f	3	256
2304	2	1	\N	\N	\N	f	3	226
2305	2	2	\N	\N	\N	f	3	195
2306	3	2	\N	\N	\N	f	3	284
2307	3	3	\N	\N	\N	f	3	253
2308	3	2	\N	\N	\N	f	3	225
2309	3	3	\N	\N	\N	f	3	194
2310	3	1	\N	\N	\N	f	3	256
2311	3	3	\N	\N	\N	f	3	274
2312	1	2	\N	\N	\N	f	3	220
2313	1	1	\N	\N	\N	t	3	251
2314	2	3	\N	\N	\N	f	3	281
2315	1	1	\N	\N	\N	t	3	288
2316	1	3	\N	\N	\N	f	3	226
2317	1	2	\N	\N	\N	f	3	257
2318	3	1	\N	\N	\N	f	3	238
2319	3	2	\N	\N	\N	f	3	207
2320	3	1	\N	\N	\N	f	3	212
2321	1	1	\N	\N	\N	t	3	211
2322	1	3	\N	\N	\N	f	3	202
2323	1	2	\N	\N	\N	f	3	233
2324	1	1	\N	\N	\N	t	3	264
2325	3	3	\N	\N	\N	f	3	239
2326	3	2	\N	\N	\N	f	3	270
2327	3	2	\N	\N	\N	f	3	228
2328	3	3	\N	\N	\N	f	3	197
2329	3	1	\N	\N	\N	f	3	259
2330	1	1	\N	\N	\N	t	3	230
2331	1	2	\N	\N	\N	f	3	199
2332	3	1	\N	\N	\N	f	3	224
2333	3	2	\N	\N	\N	f	3	193
2334	3	3	\N	\N	\N	f	3	271
2335	2	3	\N	\N	\N	f	3	214
2336	2	2	\N	\N	\N	f	3	245
2337	2	1	\N	\N	\N	f	3	276
2338	2	3	\N	\N	\N	f	3	212
2339	2	2	\N	\N	\N	f	3	243
2340	2	1	\N	\N	\N	f	3	274
2341	1	1	\N	\N	\N	t	3	214
2342	3	3	\N	\N	\N	f	3	277
2343	2	3	\N	\N	\N	f	3	266
2344	3	3	\N	\N	\N	f	3	273
2345	2	1	\N	\N	\N	f	3	288
2346	2	3	\N	\N	\N	f	3	226
2347	2	2	\N	\N	\N	f	3	257
2348	1	3	\N	\N	\N	f	3	278
2349	1	3	\N	\N	\N	f	3	241
2350	1	2	\N	\N	\N	f	3	272
2351	1	1	\N	\N	\N	t	3	223
2352	1	2	\N	\N	\N	f	3	204
2353	1	1	\N	\N	\N	t	3	235
2354	3	3	\N	\N	\N	f	3	281
2355	2	3	\N	\N	\N	f	3	283
2356	1	3	\N	\N	\N	f	3	276
2357	2	3	\N	\N	\N	f	3	229
2358	2	2	\N	\N	\N	f	3	260
2359	1	3	\N	\N	\N	f	3	214
2360	1	2	\N	\N	\N	f	3	245
2361	1	1	\N	\N	\N	t	3	276
2362	2	1	\N	\N	\N	f	3	231
2363	2	2	\N	\N	\N	f	3	200
2364	2	3	\N	\N	\N	f	3	237
2365	2	2	\N	\N	\N	f	3	268
2366	1	3	\N	\N	\N	f	3	213
2367	1	2	\N	\N	\N	f	3	244
2368	1	1	\N	\N	\N	t	3	275
2369	1	2	\N	\N	\N	f	3	214
2370	1	1	\N	\N	\N	t	3	245
2371	1	3	\N	\N	\N	f	3	238
2372	1	2	\N	\N	\N	f	3	269
2373	3	3	\N	\N	\N	f	3	202
2374	3	2	\N	\N	\N	f	3	233
2375	3	1	\N	\N	\N	f	3	264
2376	1	3	\N	\N	\N	f	3	267
2377	3	3	\N	\N	\N	f	3	203
2378	3	2	\N	\N	\N	f	3	234
2379	3	1	\N	\N	\N	f	3	265
2380	1	1	\N	\N	\N	t	3	283
2381	1	3	\N	\N	\N	f	3	221
2382	1	2	\N	\N	\N	f	3	252
2383	3	1	\N	\N	\N	f	3	277
2384	3	3	\N	\N	\N	f	3	215
2385	3	2	\N	\N	\N	f	3	246
2386	3	2	\N	\N	\N	f	3	210
2387	3	1	\N	\N	\N	f	3	241
2388	3	3	\N	\N	\N	f	3	224
2389	2	2	\N	\N	\N	f	3	223
2390	2	1	\N	\N	\N	f	3	254
2391	3	2	\N	\N	\N	f	3	255
2392	3	1	\N	\N	\N	f	3	286
2393	3	1	\N	\N	\N	f	3	203
2394	1	1	\N	\N	\N	t	3	210
2395	2	2	\N	\N	\N	f	3	219
2396	2	1	\N	\N	\N	f	3	250
2397	2	3	\N	\N	\N	f	3	262
2398	2	3	\N	\N	\N	f	3	231
2399	2	2	\N	\N	\N	f	3	262
2400	2	1	\N	\N	\N	f	3	193
2401	2	2	\N	\N	\N	f	3	280
2402	2	3	\N	\N	\N	f	3	249
2403	2	1	\N	\N	\N	f	3	201
2404	1	2	\N	\N	\N	f	3	205
2405	1	1	\N	\N	\N	t	3	236
2406	3	2	\N	\N	\N	f	3	218
2407	3	1	\N	\N	\N	f	3	249
2408	3	2	\N	\N	\N	f	3	227
2409	3	3	\N	\N	\N	f	3	196
2410	3	1	\N	\N	\N	f	3	258
2411	2	3	\N	\N	\N	f	3	239
2412	2	2	\N	\N	\N	f	3	270
2413	2	2	\N	\N	\N	f	3	277
2414	2	3	\N	\N	\N	f	3	246
2415	1	1	\N	\N	\N	t	3	206
2416	1	2	\N	\N	\N	f	3	202
2417	1	1	\N	\N	\N	t	3	208
2418	1	1	\N	\N	\N	t	3	233
2419	3	1	\N	\N	\N	f	3	279
2420	3	3	\N	\N	\N	f	3	217
2421	3	2	\N	\N	\N	f	3	248
2422	3	3	\N	\N	\N	f	3	240
2423	3	2	\N	\N	\N	f	3	271
2424	3	3	\N	\N	\N	f	3	244
2425	3	2	\N	\N	\N	f	3	275
2426	2	1	\N	\N	\N	f	3	208
2427	1	3	\N	\N	\N	f	3	258
2428	2	1	\N	\N	\N	f	3	205
2429	2	3	\N	\N	\N	f	3	282
2430	2	1	\N	\N	\N	f	3	217
2431	2	2	\N	\N	\N	f	3	203
2432	2	1	\N	\N	\N	f	3	234
2433	3	1	\N	\N	\N	f	3	200
2434	1	1	\N	\N	\N	t	3	198
2435	1	3	\N	\N	\N	f	3	270
2436	3	2	\N	\N	\N	f	3	279
2437	3	3	\N	\N	\N	f	3	248
2438	2	3	\N	\N	\N	f	3	243
2439	2	2	\N	\N	\N	f	3	274
2440	3	1	\N	\N	\N	f	3	196
2441	1	1	\N	\N	\N	t	3	213
2442	1	1	\N	\N	\N	t	3	229
2443	1	2	\N	\N	\N	f	3	198
2444	3	3	\N	\N	\N	f	3	280
2445	3	1	\N	\N	\N	f	3	206
2446	1	2	\N	\N	\N	f	3	281
2447	3	2	\N	\N	\N	f	3	230
2448	3	3	\N	\N	\N	f	3	199
2449	1	3	\N	\N	\N	f	3	250
2450	3	1	\N	\N	\N	f	3	261
2451	1	3	\N	\N	\N	f	3	242
2452	1	2	\N	\N	\N	f	3	273
2453	1	3	\N	\N	\N	f	3	266
2454	1	2	\N	\N	\N	f	3	277
2455	1	3	\N	\N	\N	f	3	246
2456	2	2	\N	\N	\N	f	3	226
2457	2	3	\N	\N	\N	f	3	195
2458	2	1	\N	\N	\N	f	3	257
2459	3	3	\N	\N	\N	f	3	272
2460	2	1	\N	\N	\N	f	3	202
2461	3	1	\N	\N	\N	f	3	281
2462	3	3	\N	\N	\N	f	3	219
2463	3	1	\N	\N	\N	f	3	204
2464	3	2	\N	\N	\N	f	3	250
2465	2	1	\N	\N	\N	f	3	284
2466	2	3	\N	\N	\N	f	3	222
2467	1	2	\N	\N	\N	f	3	221
2468	1	1	\N	\N	\N	t	3	252
2469	2	2	\N	\N	\N	f	3	253
2470	1	2	\N	\N	\N	f	3	228
2471	1	3	\N	\N	\N	f	3	197
2472	1	1	\N	\N	\N	t	3	259
2473	1	1	\N	\N	\N	t	3	194
2474	1	2	\N	\N	\N	f	3	240
2475	1	3	\N	\N	\N	f	3	209
2476	1	1	\N	\N	\N	t	3	271
2477	3	3	\N	\N	\N	f	3	260
2478	2	3	\N	\N	\N	f	3	279
2479	3	3	\N	\N	\N	f	3	242
2480	3	2	\N	\N	\N	f	3	273
2481	1	3	\N	\N	\N	f	3	211
2482	1	2	\N	\N	\N	f	3	242
2483	1	1	\N	\N	\N	t	3	273
2484	1	2	\N	\N	\N	f	3	212
2485	1	1	\N	\N	\N	t	3	243
2486	2	3	\N	\N	\N	f	3	230
2487	2	2	\N	\N	\N	f	3	261
2488	2	2	\N	\N	\N	f	3	202
2489	2	1	\N	\N	\N	f	3	233
2490	3	3	\N	\N	\N	f	3	268
2491	2	2	\N	\N	\N	f	3	230
2492	2	3	\N	\N	\N	f	3	199
2493	2	1	\N	\N	\N	f	3	261
2494	1	1	\N	\N	\N	t	3	279
2495	1	3	\N	\N	\N	f	3	217
2496	1	2	\N	\N	\N	f	3	248
2497	2	3	\N	\N	\N	f	3	205
2498	2	2	\N	\N	\N	f	3	236
2499	2	1	\N	\N	\N	f	3	267
2500	3	3	\N	\N	\N	f	3	228
2501	3	2	\N	\N	\N	f	3	259
2502	2	3	\N	\N	\N	f	3	258
2503	2	2	\N	\N	\N	f	3	206
2504	2	1	\N	\N	\N	f	3	237
2505	3	2	\N	\N	\N	f	3	239
2506	3	3	\N	\N	\N	f	3	208
2507	3	1	\N	\N	\N	f	3	270
2508	2	2	\N	\N	\N	f	3	218
2509	2	1	\N	\N	\N	f	3	249
2510	3	2	\N	\N	\N	f	3	285
2511	3	3	\N	\N	\N	f	3	254
2512	1	3	\N	\N	\N	f	3	225
2513	1	2	\N	\N	\N	f	3	256
2514	1	1	\N	\N	\N	t	3	287
2515	1	1	\N	\N	\N	t	3	222
2516	3	3	\N	\N	\N	f	3	211
2517	3	2	\N	\N	\N	f	3	242
2518	3	1	\N	\N	\N	f	3	273
2519	3	1	\N	\N	\N	f	3	208
2520	2	1	\N	\N	\N	f	3	225
2521	2	2	\N	\N	\N	f	3	194
2522	1	1	\N	\N	\N	t	3	277
2523	1	3	\N	\N	\N	f	3	215
2524	1	2	\N	\N	\N	f	3	246
2525	3	3	\N	\N	\N	f	3	236
2526	3	2	\N	\N	\N	f	3	267
2527	2	3	\N	\N	\N	f	3	203
2528	2	2	\N	\N	\N	f	3	234
2529	2	1	\N	\N	\N	f	3	265
2530	2	1	\N	\N	\N	f	3	229
2531	2	2	\N	\N	\N	f	3	198
2532	3	2	\N	\N	\N	f	3	204
2533	3	1	\N	\N	\N	f	3	235
2534	1	3	\N	\N	\N	f	3	271
2535	2	3	\N	\N	\N	f	3	286
2536	1	3	\N	\N	\N	f	3	234
2537	1	2	\N	\N	\N	f	3	265
2538	3	3	\N	\N	\N	f	3	263
2539	3	3	\N	\N	\N	f	3	213
2540	3	2	\N	\N	\N	f	3	244
2541	3	1	\N	\N	\N	f	3	275
2542	3	1	\N	\N	\N	f	3	220
2543	2	1	\N	\N	\N	f	3	210
2544	3	2	\N	\N	\N	f	3	206
2545	3	1	\N	\N	\N	f	3	237
2546	3	3	\N	\N	\N	f	3	286
2547	3	1	\N	\N	\N	f	3	223
2548	3	3	\N	\N	\N	f	3	288
2549	3	1	\N	\N	\N	f	3	229
2550	3	2	\N	\N	\N	f	3	198
2551	3	1	\N	\N	\N	f	3	221
2552	2	1	\N	\N	\N	f	3	285
2553	2	3	\N	\N	\N	f	3	223
2554	2	2	\N	\N	\N	f	3	254
2555	3	3	\N	\N	\N	f	3	284
2556	2	3	\N	\N	\N	f	3	277
2557	2	1	\N	\N	\N	f	3	280
2558	2	3	\N	\N	\N	f	3	218
2559	2	2	\N	\N	\N	f	3	249
2560	3	3	\N	\N	\N	f	3	205
2561	3	2	\N	\N	\N	f	3	236
2562	3	1	\N	\N	\N	f	3	267
2563	2	2	\N	\N	\N	f	3	221
2564	2	1	\N	\N	\N	f	3	252
2565	3	2	\N	\N	\N	f	3	202
2566	3	1	\N	\N	\N	f	3	233
2567	1	2	\N	\N	\N	f	3	231
2568	1	3	\N	\N	\N	f	3	200
2569	1	1	\N	\N	\N	t	3	262
2570	1	1	\N	\N	\N	t	3	280
2571	1	3	\N	\N	\N	f	3	218
2572	1	2	\N	\N	\N	f	3	249
2573	1	3	\N	\N	\N	f	3	286
2574	1	3	\N	\N	\N	f	3	273
2575	1	1	\N	\N	\N	t	3	239
2576	1	2	\N	\N	\N	f	3	208
2577	1	2	\N	\N	\N	f	3	283
2578	1	3	\N	\N	\N	f	3	252
2579	1	3	\N	\N	\N	f	3	287
2580	2	3	\N	\N	\N	f	3	202
2581	2	2	\N	\N	\N	f	3	233
2582	2	1	\N	\N	\N	f	3	264
2583	1	1	\N	\N	\N	t	3	204
2584	2	2	\N	\N	\N	f	3	215
2585	2	1	\N	\N	\N	f	3	246
2586	3	1	\N	\N	\N	f	3	217
2587	3	1	\N	\N	\N	f	3	214
2588	2	3	\N	\N	\N	f	3	270
2589	1	1	\N	\N	\N	t	3	225
2590	1	2	\N	\N	\N	f	3	194
2591	1	1	\N	\N	\N	t	3	219
2592	2	3	\N	\N	\N	f	3	273
3457	2	1	\N	\N	\N	f	5	124
3458	2	2	\N	\N	\N	f	5	189
3459	2	3	\N	\N	\N	f	5	158
3460	3	2	\N	\N	\N	f	5	185
3461	2	2	\N	\N	\N	f	5	182
3462	3	3	\N	\N	\N	f	5	154
3463	2	3	\N	\N	\N	f	5	151
3464	2	3	\N	\N	\N	f	5	170
3465	3	2	\N	\N	\N	f	5	106
3466	3	1	\N	\N	\N	f	5	137
3467	2	2	\N	\N	\N	f	5	120
3468	2	1	\N	\N	\N	f	5	151
3469	3	1	\N	\N	\N	f	5	109
3470	1	1	\N	\N	\N	t	5	104
3471	3	3	\N	\N	\N	f	5	129
3472	1	3	\N	\N	\N	f	5	139
3473	3	2	\N	\N	\N	f	5	160
3474	1	2	\N	\N	\N	f	5	170
3475	3	1	\N	\N	\N	f	5	191
3476	1	3	\N	\N	\N	f	5	114
3477	1	2	\N	\N	\N	f	5	145
3478	1	1	\N	\N	\N	t	5	176
3479	3	1	\N	\N	\N	f	5	184
3480	3	3	\N	\N	\N	f	5	122
3481	3	2	\N	\N	\N	f	5	153
3482	1	3	\N	\N	\N	f	5	132
3483	1	2	\N	\N	\N	f	5	163
3484	3	1	\N	\N	\N	f	5	102
3485	1	1	\N	\N	\N	t	5	121
3486	3	2	\N	\N	\N	f	5	126
3487	3	1	\N	\N	\N	f	5	157
3488	2	3	\N	\N	\N	f	5	128
3489	2	2	\N	\N	\N	f	5	159
3490	2	1	\N	\N	\N	f	5	190
3491	3	1	\N	\N	\N	f	5	119
3492	3	3	\N	\N	\N	f	5	171
3493	2	3	\N	\N	\N	f	5	138
3494	2	2	\N	\N	\N	f	5	169
3495	3	1	\N	\N	\N	f	5	101
3496	1	2	\N	\N	\N	f	5	142
3497	1	1	\N	\N	\N	t	5	105
3498	1	3	\N	\N	\N	f	5	111
3499	1	1	\N	\N	\N	t	5	173
3500	2	1	\N	\N	\N	f	5	136
3501	2	2	\N	\N	\N	f	5	105
3502	1	2	\N	\N	\N	f	5	119
3503	1	1	\N	\N	\N	t	5	150
3504	2	3	\N	\N	\N	f	5	184
3505	2	2	\N	\N	\N	f	5	185
3506	2	3	\N	\N	\N	f	5	154
3507	1	1	\N	\N	\N	t	5	101
3508	2	2	\N	\N	\N	f	5	117
3509	2	1	\N	\N	\N	f	5	148
3510	2	1	\N	\N	\N	f	5	122
3511	3	2	\N	\N	\N	f	5	181
3512	2	2	\N	\N	\N	f	5	136
3513	2	3	\N	\N	\N	f	5	105
3514	3	3	\N	\N	\N	f	5	150
3515	2	1	\N	\N	\N	f	5	167
3516	1	1	\N	\N	\N	t	5	125
3517	1	3	\N	\N	\N	f	5	149
3518	1	2	\N	\N	\N	f	5	180
3519	1	1	\N	\N	\N	t	5	118
3520	3	1	\N	\N	\N	f	5	142
3521	3	2	\N	\N	\N	f	5	111
3522	3	2	\N	\N	\N	f	5	129
3523	3	3	\N	\N	\N	f	5	98
3524	3	1	\N	\N	\N	f	5	160
3525	2	1	\N	\N	\N	f	5	99
3526	3	3	\N	\N	\N	f	5	142
3527	3	2	\N	\N	\N	f	5	173
3528	2	1	\N	\N	\N	f	5	181
3529	2	3	\N	\N	\N	f	5	119
3530	2	2	\N	\N	\N	f	5	150
3531	2	1	\N	\N	\N	f	5	183
3532	2	3	\N	\N	\N	f	5	121
3533	2	2	\N	\N	\N	f	5	152
3534	3	3	\N	\N	\N	f	5	178
3535	2	3	\N	\N	\N	f	5	109
3536	2	2	\N	\N	\N	f	5	140
3537	2	1	\N	\N	\N	f	5	171
3538	3	3	\N	\N	\N	f	5	166
3539	1	1	\N	\N	\N	t	5	182
3540	1	3	\N	\N	\N	f	5	120
3541	1	2	\N	\N	\N	f	5	151
3542	2	1	\N	\N	\N	f	5	120
3543	3	3	\N	\N	\N	f	5	163
3544	1	1	\N	\N	\N	t	5	124
3545	2	2	\N	\N	\N	f	5	144
3546	2	3	\N	\N	\N	f	5	113
3547	2	1	\N	\N	\N	f	5	175
3548	1	3	\N	\N	\N	f	5	187
3549	1	2	\N	\N	\N	f	5	184
3550	3	3	\N	\N	\N	f	5	114
3551	3	2	\N	\N	\N	f	5	145
3552	1	3	\N	\N	\N	f	5	153
3553	3	1	\N	\N	\N	f	5	176
3554	1	3	\N	\N	\N	f	5	162
3555	3	2	\N	\N	\N	f	5	125
3556	3	1	\N	\N	\N	f	5	156
3557	3	3	\N	\N	\N	f	5	190
3558	3	2	\N	\N	\N	f	5	183
3559	3	3	\N	\N	\N	f	5	152
3560	3	3	\N	\N	\N	f	5	148
3561	3	2	\N	\N	\N	f	5	179
3562	1	2	\N	\N	\N	f	5	143
3563	1	3	\N	\N	\N	f	5	112
3564	1	1	\N	\N	\N	t	5	174
3565	2	2	\N	\N	\N	f	5	188
3566	2	3	\N	\N	\N	f	5	157
3567	1	1	\N	\N	\N	t	5	133
3568	1	2	\N	\N	\N	f	5	102
3569	1	2	\N	\N	\N	f	5	133
3570	1	3	\N	\N	\N	f	5	102
3571	1	1	\N	\N	\N	t	5	164
3572	2	3	\N	\N	\N	f	5	137
3573	2	2	\N	\N	\N	f	5	168
3574	2	3	\N	\N	\N	f	5	134
3575	2	2	\N	\N	\N	f	5	165
3576	3	1	\N	\N	\N	f	5	187
3577	3	3	\N	\N	\N	f	5	125
3578	3	2	\N	\N	\N	f	5	156
3579	2	1	\N	\N	\N	f	5	143
3580	2	2	\N	\N	\N	f	5	112
3581	2	1	\N	\N	\N	f	5	134
3582	2	2	\N	\N	\N	f	5	103
3583	2	2	\N	\N	\N	f	5	110
3584	2	1	\N	\N	\N	f	5	141
3585	3	1	\N	\N	\N	f	5	136
3586	3	2	\N	\N	\N	f	5	105
3587	2	3	\N	\N	\N	f	5	168
3588	1	2	\N	\N	\N	f	5	107
3589	1	1	\N	\N	\N	t	5	138
3590	3	1	\N	\N	\N	f	5	144
3591	3	2	\N	\N	\N	f	5	113
3592	1	2	\N	\N	\N	f	5	126
3593	1	1	\N	\N	\N	t	5	157
3594	3	2	\N	\N	\N	f	5	132
3595	3	3	\N	\N	\N	f	5	101
3596	3	1	\N	\N	\N	f	5	163
3597	1	1	\N	\N	\N	t	5	134
3598	1	2	\N	\N	\N	f	5	103
3599	3	1	\N	\N	\N	f	5	189
3600	3	3	\N	\N	\N	f	5	127
3601	3	2	\N	\N	\N	f	5	158
3602	1	3	\N	\N	\N	f	5	181
3603	2	1	\N	\N	\N	f	5	107
3604	3	1	\N	\N	\N	f	5	132
3605	3	2	\N	\N	\N	f	5	101
3606	2	1	\N	\N	\N	f	5	104
3607	3	2	\N	\N	\N	f	5	130
3608	3	3	\N	\N	\N	f	5	99
3609	3	1	\N	\N	\N	f	5	161
3610	3	2	\N	\N	\N	f	5	120
3611	3	1	\N	\N	\N	f	5	151
3612	2	3	\N	\N	\N	f	5	189
3613	2	3	\N	\N	\N	f	5	191
3614	2	2	\N	\N	\N	f	5	126
3615	2	1	\N	\N	\N	f	5	157
3616	1	3	\N	\N	\N	f	5	169
3617	2	3	\N	\N	\N	f	5	187
3618	1	3	\N	\N	\N	f	5	129
3619	1	2	\N	\N	\N	f	5	160
3620	1	1	\N	\N	\N	t	5	191
3621	3	1	\N	\N	\N	f	5	104
3622	2	3	\N	\N	\N	f	5	133
3623	2	2	\N	\N	\N	f	5	164
3624	1	3	\N	\N	\N	f	5	143
3625	1	2	\N	\N	\N	f	5	174
3626	3	3	\N	\N	\N	f	5	186
3627	1	1	\N	\N	\N	t	5	97
3628	1	3	\N	\N	\N	f	5	166
3629	1	1	\N	\N	\N	t	5	136
3630	1	2	\N	\N	\N	f	5	105
3631	1	2	\N	\N	\N	f	5	135
3632	1	3	\N	\N	\N	f	5	104
3633	1	1	\N	\N	\N	t	5	166
3634	1	3	\N	\N	\N	f	5	185
3635	2	3	\N	\N	\N	f	5	163
3636	1	3	\N	\N	\N	f	5	183
3637	3	2	\N	\N	\N	f	5	188
3638	3	3	\N	\N	\N	f	5	157
3639	2	2	\N	\N	\N	f	5	184
3640	2	3	\N	\N	\N	f	5	153
3641	1	3	\N	\N	\N	f	5	178
3642	2	1	\N	\N	\N	f	5	128
3643	2	2	\N	\N	\N	f	5	97
3644	2	3	\N	\N	\N	f	5	107
3645	1	1	\N	\N	\N	t	5	120
3646	2	2	\N	\N	\N	f	5	138
3647	2	1	\N	\N	\N	f	5	169
3648	2	1	\N	\N	\N	f	5	116
3649	3	3	\N	\N	\N	f	5	173
3650	3	1	\N	\N	\N	f	5	116
3651	3	3	\N	\N	\N	f	5	115
3652	3	2	\N	\N	\N	f	5	146
3653	3	1	\N	\N	\N	f	5	177
3654	3	1	\N	\N	\N	f	5	181
3655	3	3	\N	\N	\N	f	5	119
3656	3	2	\N	\N	\N	f	5	150
3657	3	1	\N	\N	\N	f	5	113
3658	3	3	\N	\N	\N	f	5	134
3659	3	2	\N	\N	\N	f	5	165
3660	1	3	\N	\N	\N	f	5	116
3661	1	2	\N	\N	\N	f	5	147
3662	1	1	\N	\N	\N	t	5	178
3663	2	3	\N	\N	\N	f	5	181
3664	2	1	\N	\N	\N	f	5	108
3665	3	3	\N	\N	\N	f	5	118
3666	3	2	\N	\N	\N	f	5	149
3667	3	1	\N	\N	\N	f	5	180
3668	1	3	\N	\N	\N	f	5	108
3669	2	3	\N	\N	\N	f	5	114
3670	1	2	\N	\N	\N	f	5	139
3671	2	2	\N	\N	\N	f	5	145
3672	1	1	\N	\N	\N	t	5	170
3673	2	1	\N	\N	\N	f	5	176
3674	1	2	\N	\N	\N	f	5	188
3675	1	3	\N	\N	\N	f	5	157
3676	3	3	\N	\N	\N	f	5	180
3677	3	1	\N	\N	\N	f	5	128
3678	2	1	\N	\N	\N	f	5	125
3679	3	2	\N	\N	\N	f	5	97
3680	3	1	\N	\N	\N	f	5	110
3681	2	3	\N	\N	\N	f	5	110
3682	2	2	\N	\N	\N	f	5	141
3683	2	1	\N	\N	\N	f	5	172
3684	1	1	\N	\N	\N	t	5	132
3685	1	2	\N	\N	\N	f	5	101
3686	3	3	\N	\N	\N	f	5	139
3687	2	3	\N	\N	\N	f	5	146
3688	3	2	\N	\N	\N	f	5	170
3689	2	2	\N	\N	\N	f	5	177
3690	2	1	\N	\N	\N	f	5	111
3691	2	2	\N	\N	\N	f	5	131
3692	2	3	\N	\N	\N	f	5	100
3693	2	1	\N	\N	\N	f	5	162
3694	2	3	\N	\N	\N	f	5	165
3695	1	2	\N	\N	\N	f	5	108
3696	1	1	\N	\N	\N	t	5	139
3697	2	3	\N	\N	\N	f	5	173
3698	2	3	\N	\N	\N	f	5	175
3699	1	3	\N	\N	\N	f	5	131
3700	1	2	\N	\N	\N	f	5	162
3701	3	1	\N	\N	\N	f	5	106
3702	1	2	\N	\N	\N	f	5	182
3703	1	3	\N	\N	\N	f	5	151
3704	1	1	\N	\N	\N	t	5	130
3705	1	2	\N	\N	\N	f	5	99
3706	3	3	\N	\N	\N	f	5	177
3707	2	1	\N	\N	\N	f	5	135
3708	2	2	\N	\N	\N	f	5	104
3709	3	3	\N	\N	\N	f	5	131
3710	3	2	\N	\N	\N	f	5	162
3711	3	3	\N	\N	\N	f	5	143
3712	3	2	\N	\N	\N	f	5	174
3713	3	3	\N	\N	\N	f	5	133
3714	3	2	\N	\N	\N	f	5	164
3715	1	1	\N	\N	\N	t	5	115
3716	2	3	\N	\N	\N	f	5	185
3717	1	1	\N	\N	\N	t	5	113
3718	1	3	\N	\N	\N	f	5	174
3719	1	3	\N	\N	\N	f	5	115
3720	1	2	\N	\N	\N	f	5	146
3721	1	1	\N	\N	\N	t	5	177
3722	1	3	\N	\N	\N	f	5	135
3723	1	2	\N	\N	\N	f	5	166
3724	3	2	\N	\N	\N	f	5	109
3725	3	1	\N	\N	\N	f	5	140
3726	2	3	\N	\N	\N	f	5	116
3727	2	2	\N	\N	\N	f	5	147
3728	2	1	\N	\N	\N	f	5	178
3729	1	1	\N	\N	\N	t	5	183
3730	1	3	\N	\N	\N	f	5	121
3731	1	2	\N	\N	\N	f	5	152
3732	1	1	\N	\N	\N	t	5	185
3733	1	3	\N	\N	\N	f	5	123
3734	1	2	\N	\N	\N	f	5	154
3735	1	2	\N	\N	\N	f	5	114
3736	1	1	\N	\N	\N	t	5	145
3737	3	2	\N	\N	\N	f	5	116
3738	3	1	\N	\N	\N	f	5	147
3739	3	3	\N	\N	\N	f	5	147
3740	3	2	\N	\N	\N	f	5	178
3741	1	2	\N	\N	\N	f	5	106
3742	1	1	\N	\N	\N	t	5	137
3743	1	1	\N	\N	\N	t	5	122
3744	2	2	\N	\N	\N	f	5	135
3745	2	3	\N	\N	\N	f	5	104
3746	2	1	\N	\N	\N	f	5	166
3747	1	3	\N	\N	\N	f	5	107
3748	1	2	\N	\N	\N	f	5	138
3749	1	1	\N	\N	\N	t	5	169
3750	3	1	\N	\N	\N	f	5	126
3751	3	3	\N	\N	\N	f	5	189
3752	3	1	\N	\N	\N	f	5	186
3753	3	3	\N	\N	\N	f	5	124
3754	3	2	\N	\N	\N	f	5	155
3755	1	3	\N	\N	\N	f	5	145
3756	1	2	\N	\N	\N	f	5	176
3757	2	1	\N	\N	\N	f	5	185
3758	2	3	\N	\N	\N	f	5	123
3759	2	2	\N	\N	\N	f	5	154
3760	1	3	\N	\N	\N	f	5	170
3761	2	1	\N	\N	\N	f	5	192
3762	2	3	\N	\N	\N	f	5	130
3763	2	2	\N	\N	\N	f	5	161
3764	3	3	\N	\N	\N	f	5	106
3765	3	2	\N	\N	\N	f	5	137
3766	3	1	\N	\N	\N	f	5	168
3767	2	2	\N	\N	\N	f	5	106
3768	2	1	\N	\N	\N	f	5	137
3769	1	1	\N	\N	\N	t	5	129
3770	1	2	\N	\N	\N	f	5	98
3771	2	2	\N	\N	\N	f	5	129
3772	2	3	\N	\N	\N	f	5	98
3773	2	1	\N	\N	\N	f	5	160
3774	2	2	\N	\N	\N	f	5	133
3775	2	3	\N	\N	\N	f	5	102
3776	3	2	\N	\N	\N	f	5	118
3777	3	1	\N	\N	\N	f	5	149
3778	2	1	\N	\N	\N	f	5	164
3779	3	1	\N	\N	\N	f	5	131
3780	3	2	\N	\N	\N	f	5	100
3781	3	3	\N	\N	\N	f	5	182
3782	1	1	\N	\N	\N	t	5	109
3783	1	2	\N	\N	\N	f	5	185
3784	1	3	\N	\N	\N	f	5	154
3785	1	2	\N	\N	\N	f	5	134
3786	1	3	\N	\N	\N	f	5	103
3787	1	1	\N	\N	\N	t	5	165
3788	2	3	\N	\N	\N	f	5	192
3789	1	3	\N	\N	\N	f	5	140
3790	1	2	\N	\N	\N	f	5	171
3791	2	3	\N	\N	\N	f	5	179
3792	1	3	\N	\N	\N	f	5	168
3793	2	3	\N	\N	\N	f	5	144
3794	2	2	\N	\N	\N	f	5	175
3795	2	1	\N	\N	\N	f	5	97
3796	3	2	\N	\N	\N	f	5	114
3797	3	1	\N	\N	\N	f	5	145
3798	2	2	\N	\N	\N	f	5	115
3799	2	1	\N	\N	\N	f	5	146
3800	1	1	\N	\N	\N	t	5	189
3801	1	3	\N	\N	\N	f	5	127
3802	1	2	\N	\N	\N	f	5	158
3803	3	3	\N	\N	\N	f	5	183
3804	2	2	\N	\N	\N	f	5	121
3805	2	1	\N	\N	\N	f	5	152
3806	2	2	\N	\N	\N	f	5	124
3807	2	1	\N	\N	\N	f	5	155
3808	2	3	\N	\N	\N	f	5	148
3809	2	2	\N	\N	\N	f	5	179
3810	3	3	\N	\N	\N	f	5	187
3811	1	1	\N	\N	\N	t	5	106
3812	2	3	\N	\N	\N	f	5	118
3813	2	2	\N	\N	\N	f	5	149
3814	2	1	\N	\N	\N	f	5	180
3815	2	1	\N	\N	\N	f	5	123
3816	1	1	\N	\N	\N	t	5	126
3817	2	1	\N	\N	\N	f	5	118
3818	1	3	\N	\N	\N	f	5	106
3819	1	2	\N	\N	\N	f	5	137
3820	1	1	\N	\N	\N	t	5	168
3821	3	1	\N	\N	\N	f	5	124
3822	1	3	\N	\N	\N	f	5	180
3823	3	3	\N	\N	\N	f	5	138
3824	3	2	\N	\N	\N	f	5	169
3825	1	3	\N	\N	\N	f	5	142
3826	1	2	\N	\N	\N	f	5	173
3827	3	2	\N	\N	\N	f	5	121
3828	3	1	\N	\N	\N	f	5	152
3829	2	3	\N	\N	\N	f	5	142
3830	3	3	\N	\N	\N	f	5	145
3831	2	2	\N	\N	\N	f	5	173
3832	3	2	\N	\N	\N	f	5	176
3833	1	2	\N	\N	\N	f	5	122
3834	1	1	\N	\N	\N	t	5	153
3835	3	1	\N	\N	\N	f	5	117
3836	1	2	\N	\N	\N	f	5	118
3837	1	1	\N	\N	\N	t	5	149
3838	3	3	\N	\N	\N	f	5	191
3839	1	3	\N	\N	\N	f	5	186
3840	1	1	\N	\N	\N	t	5	181
3841	3	1	\N	\N	\N	f	5	143
3842	1	3	\N	\N	\N	f	5	119
3843	3	2	\N	\N	\N	f	5	112
3844	1	2	\N	\N	\N	f	5	150
3845	3	3	\N	\N	\N	f	5	110
3846	3	2	\N	\N	\N	f	5	141
3847	3	1	\N	\N	\N	f	5	172
3848	2	1	\N	\N	\N	f	5	127
3849	1	2	\N	\N	\N	f	5	131
3850	1	3	\N	\N	\N	f	5	100
3851	1	1	\N	\N	\N	t	5	162
3852	2	3	\N	\N	\N	f	5	162
3853	1	2	\N	\N	\N	f	5	110
3854	1	1	\N	\N	\N	t	5	141
3855	2	1	\N	\N	\N	f	5	187
3856	2	3	\N	\N	\N	f	5	125
3857	2	2	\N	\N	\N	f	5	156
3858	1	3	\N	\N	\N	f	5	182
3859	3	2	\N	\N	\N	f	5	135
3860	3	3	\N	\N	\N	f	5	104
3861	3	1	\N	\N	\N	f	5	166
3862	3	1	\N	\N	\N	f	5	97
3863	3	2	\N	\N	\N	f	5	144
3864	3	3	\N	\N	\N	f	5	113
3865	3	1	\N	\N	\N	f	5	175
3866	1	3	\N	\N	\N	f	5	177
3867	3	3	\N	\N	\N	f	5	168
3868	3	3	\N	\N	\N	f	5	108
3869	3	2	\N	\N	\N	f	5	139
3870	3	1	\N	\N	\N	f	5	170
3871	3	3	\N	\N	\N	f	5	175
3872	2	1	\N	\N	\N	f	5	132
3873	2	2	\N	\N	\N	f	5	101
3874	2	3	\N	\N	\N	f	5	180
3875	2	1	\N	\N	\N	f	5	189
3876	2	3	\N	\N	\N	f	5	127
3877	2	2	\N	\N	\N	f	5	158
3878	3	3	\N	\N	\N	f	5	159
3879	3	2	\N	\N	\N	f	5	190
3880	2	1	\N	\N	\N	f	5	131
3881	2	2	\N	\N	\N	f	5	100
3882	2	2	\N	\N	\N	f	5	108
3883	2	1	\N	\N	\N	f	5	139
3884	3	3	\N	\N	\N	f	5	162
3885	1	3	\N	\N	\N	f	5	189
3886	2	3	\N	\N	\N	f	5	176
3887	2	3	\N	\N	\N	f	5	139
3888	2	2	\N	\N	\N	f	5	170
3889	3	3	\N	\N	\N	f	5	185
3890	3	1	\N	\N	\N	f	5	111
3891	1	3	\N	\N	\N	f	5	163
3892	3	1	\N	\N	\N	f	5	188
3893	3	3	\N	\N	\N	f	5	126
3894	3	2	\N	\N	\N	f	5	157
3895	1	2	\N	\N	\N	f	5	192
3896	2	1	\N	\N	\N	f	5	112
3897	1	3	\N	\N	\N	f	5	161
3898	3	1	\N	\N	\N	f	5	99
3899	2	3	\N	\N	\N	f	5	174
3900	2	3	\N	\N	\N	f	5	167
3901	3	2	\N	\N	\N	f	5	182
3902	3	3	\N	\N	\N	f	5	151
3903	3	1	\N	\N	\N	f	5	103
3904	1	1	\N	\N	\N	t	5	102
3905	2	2	\N	\N	\N	f	5	127
3906	2	1	\N	\N	\N	f	5	158
3907	1	3	\N	\N	\N	f	5	133
3908	1	2	\N	\N	\N	f	5	164
3909	2	3	\N	\N	\N	f	5	136
3910	2	2	\N	\N	\N	f	5	167
3911	1	1	\N	\N	\N	t	5	103
3912	2	2	\N	\N	\N	f	5	142
3913	2	1	\N	\N	\N	f	5	98
3914	2	3	\N	\N	\N	f	5	111
3915	1	3	\N	\N	\N	f	5	171
3916	2	1	\N	\N	\N	f	5	173
3917	3	3	\N	\N	\N	f	5	188
3918	2	1	\N	\N	\N	f	5	106
3919	2	1	\N	\N	\N	f	5	119
3920	3	1	\N	\N	\N	f	5	120
3921	3	1	\N	\N	\N	f	5	192
3922	1	2	\N	\N	\N	f	5	125
3923	3	3	\N	\N	\N	f	5	130
3924	1	1	\N	\N	\N	t	5	156
3925	3	2	\N	\N	\N	f	5	161
3926	3	2	\N	\N	\N	f	5	123
3927	3	1	\N	\N	\N	f	5	154
3928	1	3	\N	\N	\N	f	5	175
3929	2	1	\N	\N	\N	f	5	121
3930	2	3	\N	\N	\N	f	5	188
3931	2	2	\N	\N	\N	f	5	130
3932	2	3	\N	\N	\N	f	5	99
3933	2	1	\N	\N	\N	f	5	161
3934	2	3	\N	\N	\N	f	5	183
3935	1	3	\N	\N	\N	f	5	159
3936	1	2	\N	\N	\N	f	5	190
3937	2	2	\N	\N	\N	f	5	191
3938	2	3	\N	\N	\N	f	5	160
3939	3	3	\N	\N	\N	f	5	132
3940	3	2	\N	\N	\N	f	5	163
3941	2	3	\N	\N	\N	f	5	115
3942	2	2	\N	\N	\N	f	5	146
3943	2	1	\N	\N	\N	f	5	177
3944	1	2	\N	\N	\N	f	5	187
3945	1	3	\N	\N	\N	f	5	156
3946	2	2	\N	\N	\N	f	5	143
3947	2	3	\N	\N	\N	f	5	112
3948	2	1	\N	\N	\N	f	5	174
3949	2	1	\N	\N	\N	f	5	126
3950	1	1	\N	\N	\N	t	5	123
3951	3	2	\N	\N	\N	f	5	110
3952	3	1	\N	\N	\N	f	5	141
3953	1	3	\N	\N	\N	f	5	179
3954	3	2	\N	\N	\N	f	5	108
3955	1	1	\N	\N	\N	t	5	99
3956	3	1	\N	\N	\N	f	5	139
3957	3	3	\N	\N	\N	f	5	117
3958	3	2	\N	\N	\N	f	5	148
3959	3	1	\N	\N	\N	f	5	179
3960	3	3	\N	\N	\N	f	5	128
3961	3	2	\N	\N	\N	f	5	159
3962	3	1	\N	\N	\N	f	5	190
3963	2	3	\N	\N	\N	f	5	186
3964	3	3	\N	\N	\N	f	5	135
3965	3	2	\N	\N	\N	f	5	166
3966	3	3	\N	\N	\N	f	5	144
3967	3	2	\N	\N	\N	f	5	175
3968	1	1	\N	\N	\N	t	5	98
3969	1	1	\N	\N	\N	t	5	111
3970	2	3	\N	\N	\N	f	5	131
3971	3	3	\N	\N	\N	f	5	116
3972	3	2	\N	\N	\N	f	5	147
3973	2	2	\N	\N	\N	f	5	162
3974	3	1	\N	\N	\N	f	5	178
3975	3	1	\N	\N	\N	f	5	115
3976	1	3	\N	\N	\N	f	5	110
3977	1	2	\N	\N	\N	f	5	141
3978	1	1	\N	\N	\N	t	5	172
3979	3	1	\N	\N	\N	f	5	129
3980	3	2	\N	\N	\N	f	5	98
3981	1	1	\N	\N	\N	t	5	186
3982	1	3	\N	\N	\N	f	5	124
3983	1	2	\N	\N	\N	f	5	155
3984	2	1	\N	\N	\N	f	5	129
3985	2	2	\N	\N	\N	f	5	98
3986	2	3	\N	\N	\N	f	5	106
3987	1	1	\N	\N	\N	t	5	119
3988	2	2	\N	\N	\N	f	5	137
3989	2	1	\N	\N	\N	f	5	168
3990	1	3	\N	\N	\N	f	5	136
3991	1	2	\N	\N	\N	f	5	167
3992	1	3	\N	\N	\N	f	5	117
3993	1	2	\N	\N	\N	f	5	148
3994	1	1	\N	\N	\N	t	5	179
3995	3	2	\N	\N	\N	f	5	187
3996	3	3	\N	\N	\N	f	5	156
3997	3	1	\N	\N	\N	f	5	105
3998	1	2	\N	\N	\N	f	5	117
3999	1	1	\N	\N	\N	t	5	148
4000	1	1	\N	\N	\N	t	5	127
4001	3	3	\N	\N	\N	f	5	179
4002	1	2	\N	\N	\N	f	5	120
4003	1	1	\N	\N	\N	t	5	151
4004	3	3	\N	\N	\N	f	5	176
4005	3	3	\N	\N	\N	f	5	192
4006	2	1	\N	\N	\N	f	5	100
4007	1	2	\N	\N	\N	f	5	129
4008	1	3	\N	\N	\N	f	5	98
4009	1	1	\N	\N	\N	t	5	160
4010	1	3	\N	\N	\N	f	5	165
4011	2	3	\N	\N	\N	f	5	171
4012	3	2	\N	\N	\N	f	5	127
4013	3	1	\N	\N	\N	f	5	158
4014	1	1	\N	\N	\N	t	5	188
4015	1	3	\N	\N	\N	f	5	126
4016	1	2	\N	\N	\N	f	5	157
4017	2	1	\N	\N	\N	f	5	102
4018	1	3	\N	\N	\N	f	5	191
4019	3	2	\N	\N	\N	f	5	107
4020	3	1	\N	\N	\N	f	5	138
4021	3	2	\N	\N	\N	f	5	189
4022	3	3	\N	\N	\N	f	5	158
4023	1	2	\N	\N	\N	f	5	181
4024	1	3	\N	\N	\N	f	5	150
4025	3	1	\N	\N	\N	f	5	134
4026	3	2	\N	\N	\N	f	5	103
4027	1	2	\N	\N	\N	f	5	121
4028	1	1	\N	\N	\N	t	5	152
4029	3	2	\N	\N	\N	f	5	128
4030	3	3	\N	\N	\N	f	5	97
4031	3	1	\N	\N	\N	f	5	159
4032	2	1	\N	\N	\N	f	5	142
4033	2	2	\N	\N	\N	f	5	111
4034	1	1	\N	\N	\N	t	5	135
4035	1	2	\N	\N	\N	f	5	104
4036	1	1	\N	\N	\N	t	5	144
4037	1	2	\N	\N	\N	f	5	113
4038	2	3	\N	\N	\N	f	5	129
4039	2	2	\N	\N	\N	f	5	160
4040	2	1	\N	\N	\N	f	5	191
4041	1	3	\N	\N	\N	f	5	138
4042	1	2	\N	\N	\N	f	5	169
4043	3	2	\N	\N	\N	f	5	133
4044	3	3	\N	\N	\N	f	5	102
4045	3	1	\N	\N	\N	f	5	164
4046	3	2	\N	\N	\N	f	5	124
4047	3	1	\N	\N	\N	f	5	155
4048	1	2	\N	\N	\N	f	5	123
4049	1	1	\N	\N	\N	t	5	154
4050	2	3	\N	\N	\N	f	5	143
4051	2	3	\N	\N	\N	f	5	141
4052	2	2	\N	\N	\N	f	5	172
4053	2	2	\N	\N	\N	f	5	174
4054	3	3	\N	\N	\N	f	5	164
4055	1	3	\N	\N	\N	f	5	144
4056	1	2	\N	\N	\N	f	5	175
4057	2	1	\N	\N	\N	f	5	144
4058	2	2	\N	\N	\N	f	5	113
4059	1	2	\N	\N	\N	f	5	127
4060	1	1	\N	\N	\N	t	5	158
4061	3	3	\N	\N	\N	f	5	170
4062	3	1	\N	\N	\N	f	5	135
4063	3	2	\N	\N	\N	f	5	104
4064	2	3	\N	\N	\N	f	5	135
4065	2	2	\N	\N	\N	f	5	166
4066	1	3	\N	\N	\N	f	5	147
4067	1	2	\N	\N	\N	f	5	178
4068	1	3	\N	\N	\N	f	5	184
4069	1	1	\N	\N	\N	t	5	192
4070	1	3	\N	\N	\N	f	5	130
4071	1	2	\N	\N	\N	f	5	161
4072	3	1	\N	\N	\N	f	5	114
4073	3	1	\N	\N	\N	f	5	108
4074	2	2	\N	\N	\N	f	5	119
4075	2	1	\N	\N	\N	f	5	150
4076	2	3	\N	\N	\N	f	5	159
4077	2	2	\N	\N	\N	f	5	190
4078	3	3	\N	\N	\N	f	5	149
4079	3	2	\N	\N	\N	f	5	180
4080	2	3	\N	\N	\N	f	5	169
4081	3	1	\N	\N	\N	f	5	122
4082	2	2	\N	\N	\N	f	5	181
4083	2	3	\N	\N	\N	f	5	150
4084	3	3	\N	\N	\N	f	5	172
4085	1	1	\N	\N	\N	t	5	117
4086	2	3	\N	\N	\N	f	5	190
4087	3	2	\N	\N	\N	f	5	142
4088	3	3	\N	\N	\N	f	5	111
4089	3	1	\N	\N	\N	f	5	173
4090	2	1	\N	\N	\N	f	5	114
4091	2	2	\N	\N	\N	f	5	186
4092	2	3	\N	\N	\N	f	5	155
4093	2	2	\N	\N	\N	f	5	125
4094	2	1	\N	\N	\N	f	5	156
4095	2	2	\N	\N	\N	f	5	107
4096	2	1	\N	\N	\N	f	5	138
4097	2	2	\N	\N	\N	f	5	128
4098	2	3	\N	\N	\N	f	5	97
4099	2	1	\N	\N	\N	f	5	159
4100	2	2	\N	\N	\N	f	5	187
4101	2	3	\N	\N	\N	f	5	156
4102	1	1	\N	\N	\N	t	5	142
4103	1	2	\N	\N	\N	f	5	111
4104	3	1	\N	\N	\N	f	5	112
4105	1	3	\N	\N	\N	f	5	160
4106	1	2	\N	\N	\N	f	5	191
4107	1	2	\N	\N	\N	f	5	136
4108	1	3	\N	\N	\N	f	5	105
4109	1	1	\N	\N	\N	t	5	167
4110	3	2	\N	\N	\N	f	5	134
4111	3	3	\N	\N	\N	f	5	103
4112	1	3	\N	\N	\N	f	5	164
4113	3	1	\N	\N	\N	f	5	165
4114	1	1	\N	\N	\N	t	5	108
4115	3	1	\N	\N	\N	f	5	183
4116	3	3	\N	\N	\N	f	5	121
4117	3	2	\N	\N	\N	f	5	152
4118	1	3	\N	\N	\N	f	5	172
4119	2	3	\N	\N	\N	f	5	147
4120	2	2	\N	\N	\N	f	5	178
4121	1	1	\N	\N	\N	t	5	112
4122	3	3	\N	\N	\N	f	5	146
4123	3	2	\N	\N	\N	f	5	177
4124	2	1	\N	\N	\N	f	5	186
4125	2	3	\N	\N	\N	f	5	124
4126	2	2	\N	\N	\N	f	5	155
4127	1	3	\N	\N	\N	f	5	128
4128	1	2	\N	\N	\N	f	5	159
4129	1	1	\N	\N	\N	t	5	190
4130	3	3	\N	\N	\N	f	5	137
4131	3	2	\N	\N	\N	f	5	168
4132	2	3	\N	\N	\N	f	5	178
4133	2	3	\N	\N	\N	f	5	172
4134	1	3	\N	\N	\N	f	5	148
4135	1	2	\N	\N	\N	f	5	179
4136	1	3	\N	\N	\N	f	5	167
4137	1	2	\N	\N	\N	f	5	128
4138	1	3	\N	\N	\N	f	5	97
4139	1	1	\N	\N	\N	t	5	159
4140	3	2	\N	\N	\N	f	5	191
4141	3	3	\N	\N	\N	f	5	160
4142	2	1	\N	\N	\N	f	5	117
4143	3	3	\N	\N	\N	f	5	109
4144	3	2	\N	\N	\N	f	5	140
4145	3	1	\N	\N	\N	f	5	171
4146	3	3	\N	\N	\N	f	5	174
4147	2	3	\N	\N	\N	f	5	117
4148	2	2	\N	\N	\N	f	5	148
4149	2	1	\N	\N	\N	f	5	179
4150	2	1	\N	\N	\N	f	5	182
4151	2	3	\N	\N	\N	f	5	120
4152	2	1	\N	\N	\N	f	5	110
4153	2	2	\N	\N	\N	f	5	151
4154	3	2	\N	\N	\N	f	5	136
4155	3	3	\N	\N	\N	f	5	105
4156	3	1	\N	\N	\N	f	5	167
4157	3	2	\N	\N	\N	f	5	143
4158	3	3	\N	\N	\N	f	5	112
4159	3	1	\N	\N	\N	f	5	174
4160	3	1	\N	\N	\N	f	5	100
4161	3	1	\N	\N	\N	f	5	98
4162	2	2	\N	\N	\N	f	5	123
4163	2	1	\N	\N	\N	f	5	154
4164	3	3	\N	\N	\N	f	5	140
4165	3	2	\N	\N	\N	f	5	171
4166	2	3	\N	\N	\N	f	5	140
4167	2	2	\N	\N	\N	f	5	171
4168	2	3	\N	\N	\N	f	5	177
4169	1	3	\N	\N	\N	f	5	190
4170	2	1	\N	\N	\N	f	5	109
4171	3	3	\N	\N	\N	f	5	141
4172	3	2	\N	\N	\N	f	5	172
4173	3	2	\N	\N	\N	f	5	184
4174	3	3	\N	\N	\N	f	5	153
4175	2	2	\N	\N	\N	f	5	116
4176	2	1	\N	\N	\N	f	5	147
4177	2	2	\N	\N	\N	f	5	109
4178	2	1	\N	\N	\N	f	5	140
4179	1	1	\N	\N	\N	t	5	116
4180	1	3	\N	\N	\N	f	5	173
4181	2	3	\N	\N	\N	f	5	149
4182	2	2	\N	\N	\N	f	5	180
4183	1	1	\N	\N	\N	t	5	187
4184	1	3	\N	\N	\N	f	5	125
4185	1	2	\N	\N	\N	f	5	156
4186	2	1	\N	\N	\N	f	5	184
4187	2	3	\N	\N	\N	f	5	122
4188	2	2	\N	\N	\N	f	5	153
4189	3	1	\N	\N	\N	f	5	118
4190	3	3	\N	\N	\N	f	5	167
4191	1	2	\N	\N	\N	f	5	183
4192	1	3	\N	\N	\N	f	5	152
4193	1	3	\N	\N	\N	f	5	109
4194	1	2	\N	\N	\N	f	5	140
4195	1	1	\N	\N	\N	t	5	171
4196	1	1	\N	\N	\N	t	5	184
4197	1	3	\N	\N	\N	f	5	122
4198	1	2	\N	\N	\N	f	5	153
4199	2	2	\N	\N	\N	f	5	132
4200	2	3	\N	\N	\N	f	5	101
4201	2	1	\N	\N	\N	f	5	163
4202	3	1	\N	\N	\N	f	5	130
4203	3	2	\N	\N	\N	f	5	99
4204	1	1	\N	\N	\N	t	5	110
4205	1	1	\N	\N	\N	t	5	114
4206	2	1	\N	\N	\N	f	5	188
4207	2	3	\N	\N	\N	f	5	126
4208	2	2	\N	\N	\N	f	5	157
4209	1	2	\N	\N	\N	f	5	144
4210	1	3	\N	\N	\N	f	5	113
4211	1	1	\N	\N	\N	t	5	175
4212	2	1	\N	\N	\N	f	5	101
4213	1	1	\N	\N	\N	t	5	100
4214	3	2	\N	\N	\N	f	5	186
4215	3	3	\N	\N	\N	f	5	155
4216	1	2	\N	\N	\N	f	5	130
4217	1	3	\N	\N	\N	f	5	99
4218	1	1	\N	\N	\N	t	5	161
4219	1	2	\N	\N	\N	f	5	132
4220	1	3	\N	\N	\N	f	5	101
4221	1	1	\N	\N	\N	t	5	163
4222	2	2	\N	\N	\N	f	5	118
4223	2	1	\N	\N	\N	f	5	149
4224	3	2	\N	\N	\N	f	5	131
4225	3	3	\N	\N	\N	f	5	100
4226	3	1	\N	\N	\N	f	5	162
4227	3	2	\N	\N	\N	f	5	122
4228	3	1	\N	\N	\N	f	5	153
4229	1	2	\N	\N	\N	f	5	109
4230	1	1	\N	\N	\N	t	5	140
4231	2	1	\N	\N	\N	f	5	105
4232	2	2	\N	\N	\N	f	5	122
4233	1	2	\N	\N	\N	f	5	115
4234	1	1	\N	\N	\N	t	5	146
4235	2	1	\N	\N	\N	f	5	153
4236	2	1	\N	\N	\N	f	5	103
4237	2	3	\N	\N	\N	f	5	132
4238	2	2	\N	\N	\N	f	5	163
4239	3	1	\N	\N	\N	f	5	107
4240	2	3	\N	\N	\N	f	5	145
4241	2	2	\N	\N	\N	f	5	176
4242	1	3	\N	\N	\N	f	5	176
4243	1	2	\N	\N	\N	f	5	186
4244	1	3	\N	\N	\N	f	5	155
4245	3	2	\N	\N	\N	f	5	119
4246	3	1	\N	\N	\N	f	5	150
4247	2	1	\N	\N	\N	f	5	115
4248	3	3	\N	\N	\N	f	5	184
4249	1	3	\N	\N	\N	f	5	137
4250	1	2	\N	\N	\N	f	5	168
4251	3	3	\N	\N	\N	f	5	181
4252	1	3	\N	\N	\N	f	5	146
4253	1	2	\N	\N	\N	f	5	177
4254	3	1	\N	\N	\N	f	5	133
4255	3	2	\N	\N	\N	f	5	102
4256	2	3	\N	\N	\N	f	5	182
4257	3	3	\N	\N	\N	f	5	169
4258	3	3	\N	\N	\N	f	5	136
4259	3	2	\N	\N	\N	f	5	167
4260	2	2	\N	\N	\N	f	5	134
4261	2	3	\N	\N	\N	f	5	103
4262	2	1	\N	\N	\N	f	5	165
4263	2	3	\N	\N	\N	f	5	164
4264	1	1	\N	\N	\N	t	5	143
4265	1	2	\N	\N	\N	f	5	112
4266	3	2	\N	\N	\N	f	5	117
4267	3	1	\N	\N	\N	f	5	148
4268	1	3	\N	\N	\N	f	5	118
4269	1	2	\N	\N	\N	f	5	149
4270	1	1	\N	\N	\N	t	5	180
4271	3	1	\N	\N	\N	f	5	121
4272	1	2	\N	\N	\N	f	5	116
4273	1	1	\N	\N	\N	t	5	147
4274	2	1	\N	\N	\N	f	5	113
4275	2	3	\N	\N	\N	f	5	166
4276	1	3	\N	\N	\N	f	5	188
4277	3	1	\N	\N	\N	f	5	185
4278	3	3	\N	\N	\N	f	5	123
4279	3	2	\N	\N	\N	f	5	154
4280	2	3	\N	\N	\N	f	5	108
4281	2	2	\N	\N	\N	f	5	139
4282	2	1	\N	\N	\N	f	5	170
4283	1	3	\N	\N	\N	f	5	192
4284	3	3	\N	\N	\N	f	5	165
4285	2	1	\N	\N	\N	f	5	130
4286	2	2	\N	\N	\N	f	5	99
4287	2	2	\N	\N	\N	f	5	183
4288	2	3	\N	\N	\N	f	5	152
4289	2	2	\N	\N	\N	f	5	192
4290	2	3	\N	\N	\N	f	5	161
4291	2	2	\N	\N	\N	f	5	114
4292	2	1	\N	\N	\N	f	5	145
4293	2	1	\N	\N	\N	f	5	133
4294	2	2	\N	\N	\N	f	5	102
4295	1	1	\N	\N	\N	t	5	107
4296	1	3	\N	\N	\N	f	5	141
4297	1	2	\N	\N	\N	f	5	172
4298	3	3	\N	\N	\N	f	5	107
4299	3	2	\N	\N	\N	f	5	138
4300	3	1	\N	\N	\N	f	5	169
4301	3	1	\N	\N	\N	f	5	127
4302	3	2	\N	\N	\N	f	5	192
4303	3	1	\N	\N	\N	f	5	123
4304	3	3	\N	\N	\N	f	5	161
4305	3	1	\N	\N	\N	f	5	182
4306	1	3	\N	\N	\N	f	5	134
4307	3	3	\N	\N	\N	f	5	120
4308	3	2	\N	\N	\N	f	5	151
4309	1	2	\N	\N	\N	f	5	165
4310	1	2	\N	\N	\N	f	5	124
4311	1	1	\N	\N	\N	t	5	155
4312	3	2	\N	\N	\N	f	5	115
4313	3	1	\N	\N	\N	f	5	146
4314	1	1	\N	\N	\N	t	5	128
4315	1	2	\N	\N	\N	f	5	97
4316	1	2	\N	\N	\N	f	5	189
4317	3	1	\N	\N	\N	f	5	125
4318	1	3	\N	\N	\N	f	5	158
4319	1	1	\N	\N	\N	t	5	131
4320	1	2	\N	\N	\N	f	5	100
\.


--
-- Data for Name: channels; Type: TABLE DATA; Schema: public; Owner: -
--

COPY channels (id, code, label, description, wavelength, red_cc, green_cc, blue_cc, ds_id, exp_id) FROM stdin;
4	DAPI	DAPI	\N	\N	0	0	255	2	\N
5	GFP	GFP	\N	\N	0	255	0	2	\N
6	CY5	Cy5	\N	\N	255	0	0	2	\N
7	DAPI	DAPI	\N	\N	0	0	255	3	\N
8	GFP	GFP	\N	\N	0	255	0	3	\N
9	CY5	Cy5	\N	\N	255	0	0	3	\N
11	OVERLAY-1	OVERLAY-1	\N	\N	0	0	255	5	\N
\.


--
-- Data for Name: containers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY containers (id, perm_id, spots_width, spots_height, expe_id) FROM stdin;
1	20130412140147735-20	12	8	1
2	20130412150557128-205	12	8	2
\.


--
-- Data for Name: database_version_logs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY database_version_logs (db_version, module_name, run_status, run_status_timestamp, module_code, run_exception) FROM stdin;
023	sql/imaging/postgresql/023/schema-023.sql	SUCCESS	2013-04-11 15:40:16.64	\\x0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f0a2f2a20446f6d61696e73202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202a2f0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f0a0a43524541544520444f4d41494e20544543485f494420415320424947494e543b0a0a43524541544520444f4d41494e20434f44452041532056415243484152283430293b0a0a43524541544520444f4d41494e204c4f4e475f4e414d4520415320544558543b0a0a43524541544520444f4d41494e204445534352495054494f4e204153205641524348415228323030293b0a0a43524541544520444f4d41494e2046494c455f5041544820617320564152434841522831303030293b0a0a43524541544520444f4d41494e20434f4c4f525f434f4d504f4e454e5420415320564152434841522834302920434845434b202856414c554520494e202827524544272c2027475245454e272c2027424c55452729293b0a0a43524541544520444f4d41494e204348414e4e454c5f434f4c4f5220415320564152434841522832302920434845434b202856414c554520494e202827424c5545272c2027475245454e272c2027524544272c20275245445f475245454e272c20275245445f424c5545272c2027475245454e5f424c55452729293b0a200a43524541544520444f4d41494e20424f4f4c45414e5f4348415220415320424f4f4c45414e2044454641554c542046414c53453b0a0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f0a2f2a205461626c657320202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202a2f0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f0a0a435245415445205441424c45204558504552494d454e545320280a202049442042494753455249414c204e4f54204e554c4c2c0a20205045524d5f494420434f4445204e4f54204e554c4c2c0a2020494d4147455f5452414e53464f524d45525f464143544f52592042595445412c0a0a20205052494d415259204b455920284944292c0a2020554e4951554520285045524d5f4944290a293b0a0a435245415445205441424c4520434f4e5441494e45525320280a202049442042494753455249414c204e4f54204e554c4c2c0a20205045524d5f494420434f4445204e4f54204e554c4c2c0a0a202053504f54535f574944544820494e54454745522c0a202053504f54535f48454947485420494e54454745522c0a20200a2020455850455f494420544543485f4944204e4f54204e554c4c2c0a0a20205052494d415259204b455920284944292c0a2020554e4951554520285045524d5f4944292c0a2020434f4e53545241494e5420464b5f53414d504c455f3120464f524549474e204b45592028455850455f494429205245464552454e434553204558504552494d454e54532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144450a293b0a0a43524541544520494e44455820434f4e5441494e4552535f455850455f494458204f4e20434f4e5441494e45525328455850455f4944293b0a0a435245415445205441424c452053504f545320280a202049442042494753455249414c204e4f54204e554c4c2c0a090a092d2d20706f736974696f6e20696e2074686520636f6e7461696e65722c206f6e652d62617365640a20205820494e54454745522c200a20205920494e54454745522c200a2020434f4e545f494420544543485f4944204e4f54204e554c4c2c0a20200a20205052494d415259204b455920284944292c0a2020434f4e53545241494e5420464b5f53504f545f3120464f524549474e204b45592028434f4e545f494429205245464552454e43455320434f4e5441494e4552532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144450a293b0a0a43524541544520494e4445582053504f54535f434f4e545f494458204f4e2053504f545328434f4e545f4944293b0a2d2d20616c6c6f777320746f2073656c656374206f6e652073706f74206f662074686520636f6e7461696e657220717569636b65720a43524541544520494e4445582053504f54535f434f4f5244535f494458204f4e2053504f545328434f4e545f49442c20582c2059293b0a0a435245415445205441424c4520414e414c595349535f444154415f5345545320280a202049442042494753455249414c204e4f54204e554c4c2c0a20205045524d5f494420434f4445204e4f54204e554c4c2c0a0a2020434f4e545f494420544543485f49442c0a20200a20205052494d415259204b455920284944292c0a2020554e4951554520285045524d5f4944292c0a2020434f4e53545241494e5420464b5f414e414c595349535f444154415f5345545f3120464f524549474e204b45592028434f4e545f494429205245464552454e43455320434f4e5441494e4552532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144450a293b0a0a43524541544520494e44455820414e414c595349535f444154415f534554535f434f4e545f494458204f4e20414e414c595349535f444154415f5345545328434f4e545f4944293b0a0a0a435245415445205441424c4520494d4147455f444154415f5345545320280a202049442042494753455249414c204e4f54204e554c4c2c0a20205045524d5f494420434f4445204e4f54204e554c4c2c0a0a20202d2d2d2d20696d6167652064617461736574207370656369666963206669656c6473202873686f756c64206265207265666163746f72656429200a094649454c44535f574944544820494e54454745522c0a094649454c44535f48454947485420494e54454745522c090a20202d2d207472616e73666f726d6174696f6e20666f72206d6572676564206368616e6e656c73206f6e207468652064617461736574206c6576656c2c206f7665727269646573206578706572696d656e74206c6576656c207472616e73666f726d6174696f6e0a2020494d4147455f5452414e53464f524d45525f464143544f52592042595445412c0a20202d2d206120726564756e64616e7420696e666f726d6174696f6e206966207468657265206172652074696d65706f696e74206f7220646570746820737461636b206461746120666f7220616e792073706f747320696e207468697320646174617365740a202049535f4d554c544944494d454e53494f4e414c20424f4f4c45414e5f43484152204e4f54204e554c4c2c0a0a20202d2d20576869636820696d616765206c6962726172792073686f756c64206265207573656420746f20726561642074686520696d6167653f200a20202d2d204966206e6f74207370656369666965642c20736f6d6520686575726973746963732061726520757365642c2062757420697420697320736c6f77657220616e6420646f6573206e6f7420747279207769746820616c6c2074686520617661696c61626c65206c69627261726965732e200a2020494d4147455f4c4942524152595f4e414d45204c4f4e475f4e414d452c0a20202d2d2057686963682072656164657220696e20746865206c6962726172792073686f756c6420626520757365643f2056616c6964206f6e6c7920696620746865206c696272617279204c4f4e475f4e414d45206973207370656369666965642e0a20202d2d2053686f756c6420626520737065636966696564207768656e206c696272617279204c4f4e475f4e414d45206973207370656369666965642e0a2020494d4147455f4c4942524152595f5245414445525f4e414d45204c4f4e475f4e414d452c0a20202d2d2d2d20454e4420696d6167652064617461736574207370656369666963206669656c64730a20200a2020434f4e545f494420544543485f49442c0a20200a20205052494d415259204b455920284944292c0a2020554e4951554520285045524d5f4944292c0a2020434f4e53545241494e5420464b5f494d4147455f444154415f5345545f3120464f524549474e204b45592028434f4e545f494429205245464552454e43455320434f4e5441494e4552532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144450a293b0a0a43524541544520494e44455820494d4147455f444154415f534554535f434f4e545f494458204f4e20494d4147455f444154415f5345545328434f4e545f4944293b0a0a435245415445205441424c45204348414e4e454c5320280a2020202049442042494753455249414c20204e4f54204e554c4c2c0a202020200a20202020434f4445204c4f4e475f4e414d45204e4f54204e554c4c2c0a202020204c4142454c204c4f4e475f4e414d45204e4f54204e554c4c2c0a202020204445534352495054494f4e204445534352495054494f4e2c0a20202020574156454c454e47544820494e54454745522c0a0a202020202d2d2052474220636f6c6f7220636f6d706f6e656e747320737065636966792074686520636f6c6f7220696e207768696368206368616e6e656c2073686f756c6420626520646973706c617965640a202020205245445f434320494e5445474552204e4f54204e554c4c2c0a20202020475245454e5f434320494e5445474552204e4f54204e554c4c2c0a20202020424c55455f434320494e5445474552204e4f54204e554c4c2c0a0a2020202044535f494420544543485f49442c0a202020204558505f494420544543485f49442c0a202020200a202020205052494d415259204b455920284944292c0a20202020434f4e53545241494e5420464b5f4348414e4e454c535f3120464f524549474e204b4559202844535f494429205245464552454e43455320494d4147455f444154415f534554532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144452c0a20202020434f4e53545241494e5420464b5f4348414e4e454c535f3220464f524549474e204b455920284558505f494429205245464552454e434553204558504552494d454e54532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144452c0a20202020434f4e53545241494e54204348414e4e454c535f44535f4558505f4152435f434b20434845434b20282844535f4944204953204e4f54204e554c4c20414e44204558505f4944204953204e554c4c29204f52202844535f4944204953204e554c4c20414e44204558505f4944204953204e4f54204e554c4c29292c0a202020200a20202020434f4e53545241494e54204348414e4e454c535f554b5f3120554e4951554528434f44452c2044535f4944292c0a20202020434f4e53545241494e54204348414e4e454c535f554b5f3220554e4951554528434f44452c204558505f4944290a293b0a0a43524541544520494e444558204348414e4e454c535f44535f494458204f4e204348414e4e454c532844535f4944293b0a0a435245415445205441424c4520494d4147455f5452414e53464f524d4154494f4e5320280a2020202049442042494753455249414c20204e4f54204e554c4c2c0a202020200a20202020434f4445204c4f4e475f4e414d45204e4f54204e554c4c2c0a202020204c4142454c204c4f4e475f4e414d45204e4f54204e554c4c2c0a202020204445534352495054494f4e206368617261637465722076617279696e672831303030292c0a20202020494d4147455f5452414e53464f524d45525f464143544f5259204259544541204e4f54204e554c4c2c0a202020200a202020202d2d20466f72206e6f772074686572652063616e206265206f6e6c79206f6e65207472616e73666f726d6174696f6e20666f722065616368206368616e6e656c207768696368206973206564697461626c6520627920496d616765205669657765722c0a202020202d2d20627574207768656e204755492077696c6c20737570706f7274206d6f7265207468656e207468697320636f6c756d6e2077696c6c206265636f6d65207265616c6c792075736566756c2e0a2020202049535f4544495441424c4520424f4f4c45414e5f43484152204e4f54204e554c4c2c0a202020200a202020202d2d205468652064656661756c742063686f69636520746f2070726573656e742074686520696d6167652e0a202020202d2d204966206e6f742070726573656e7420612027686172642d636f646564272064656661756c74207472616e73666f726d6174696f6e2077696c6c206265636f6d6520617661696c61626c652e200a2020202049535f44454641554c5420424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c0a202020200a202020204348414e4e454c5f494420544543485f4944204e4f54204e554c4c2c0a202020200a202020205052494d415259204b455920284944292c0a20202020434f4e53545241494e5420464b5f494d4147455f5452414e53464f524d4154494f4e535f4348414e4e454c20464f524549474e204b455920284348414e4e454c5f494429205245464552454e434553204348414e4e454c532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144452c0a202020200a20202020434f4e53545241494e5420494d4147455f5452414e53464f524d4154494f4e535f554b5f3120554e4951554528434f44452c204348414e4e454c5f4944290a293b0a0a43524541544520494e44455820494d4147455f5452414e53464f524d4154494f4e535f4348414e4e454c535f494458204f4e20494d4147455f5452414e53464f524d4154494f4e53284348414e4e454c5f4944293b0a0a435245415445205441424c4520494d4147455f5a4f4f4d5f4c4556454c5320280a202049442042494753455249414c204e4f54204e554c4c2c0a0a202049535f4f524947494e414c20424f4f4c45414e5f43484152204e4f54204e554c4c2c0a2020434f4e5441494e45525f444154415345545f494420544543485f4944204e4f54204e554c4c2c200a20200a20202d2d205065726d206964206f66207468652027706879736963616c27206461746173657420776869636820636f6e7461696e7320616c6c20696d6167657320776974682074686973207a6f6f6d2e0a20202d2d20506879736963616c20646174617365747320617265206e6f742073746f72656420696e2022696d6167655f646174615f7365747322207461626c652c20627574207765206e65656420746865207265666572656e636520746f207468656d200a20202d2d207768656e2077652064656c657465206f722061726368697665206f6e65207a6f6f6d206c6576656c2e200a2020504859534943414c5f444154415345545f5045524d5f49442054455854204e4f54204e554c4c2c0a20200a2020504154482046494c455f504154482c0a2020574944544820494e54454745522c0a202048454947485420494e54454745522c0a2020434f4c4f525f444550544820494e54454745522c0a202046494c455f545950452056415243484152283230292c0a20200a20205052494d415259204b455920284944292c0a2020434f4e53545241494e5420464b5f494d4147455f5a4f4f4d5f4c4556454c535f3120464f524549474e204b45592028434f4e5441494e45525f444154415345545f494429205245464552454e43455320494d4147455f444154415f534554532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144450a293b0a0a43524541544520494e44455820494d4147455f5a4f4f4d5f4c4556454c535f504859535f44535f494458204f4e20494d4147455f5a4f4f4d5f4c4556454c532028504859534943414c5f444154415345545f5045524d5f4944293b0a43524541544520494e44455820494d4147455f5a4f4f4d5f4c4556454c535f434f4e545f464b5f494458204f4e20494d4147455f5a4f4f4d5f4c4556454c532028434f4e5441494e45525f444154415345545f4944293b0a0a435245415445205441424c4520494d4147455f5a4f4f4d5f4c4556454c5f5452414e53464f524d4154494f4e5320280a0949442042494753455249414c204e4f54204e554c4c2c0a090a20205a4f4f4d5f4c4556454c5f494420544543485f4944204e4f54204e554c4c2c0a20204348414e4e454c5f494420544543485f4944204e4f54204e554c4c2c0a2020494d4147455f5452414e53464f524d4154494f4e5f494420544543485f4944204e4f54204e554c4c2c0a20200a20205052494d415259204b4559284944292c0a2020434f4e53545241494e5420464b5f494d4147455f5a4f4f4d5f4c4556454c5f5452414e53464f524d4154494f4e535f3120464f524549474e204b455920285a4f4f4d5f4c4556454c5f494429205245464552454e43455320494d4147455f5a4f4f4d5f4c4556454c532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144452c0a2020434f4e53545241494e5420464b5f494d4147455f5a4f4f4d5f4c4556454c5f5452414e53464f524d4154494f4e535f3220464f524549474e204b455920284348414e4e454c5f494429205245464552454e434553204348414e4e454c532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144450a293b0a0a43524541544520494e44455820494d4147455f5a4f4f4d5f4c4556454c5f5452414e53464f524d4154494f4e535f5a4c49445f494458204f4e20494d4147455f5a4f4f4d5f4c4556454c5f5452414e53464f524d4154494f4e53285a4f4f4d5f4c4556454c5f4944293b0a0a435245415445205441424c45204348414e4e454c5f535441434b5320280a2020202049442042494753455249414c20204e4f54204e554c4c2c0a09090a09092d2d207820616e64207920617265206b696e64206f6620612074776f2064696d656e73696f6e616c2073657175656e6365206e756d6265722c20736f6d65207573652063617365206d6179206f6e6c7920757365207820616e64206c65617665207920616c6f6e650a09095820494e54454745522c0a09095920494e54454745522c0a09092d2d20576520757365207468652066697865642064696d656e73696f6e206d657465727320686572652e0a09095a5f696e5f4d205245414c2c0a09092d2d20576520757365207468652066697865642064696d656e73696f6e207365636f6e647320686572652e0a0909545f696e5f534543205245414c2c0a09095345524945535f4e554d42455220494e54454745522c0a09090a09092d2d20466f7220616c6c206368616e6e656c20737461636b73206f6620612077656c6c202848435329206f7220696d616765206461746173657420286d6963726f73636f7079292074686572652073686f756c642062652065786163746c79200a09092d2d206f6e65207265636f726420776974682069735f726570726573656e746174697665203d20747275650a090969735f726570726573656e74617469766520424f4f4c45414e5f43484152204e4f54204e554c4c2044454641554c54202746272c0a202020200a2020202044535f494420544543485f4944094e4f54204e554c4c2c0a090953504f545f494420544543485f49442c0a0a202020205052494d415259204b455920284944292c0a20202020434f4e53545241494e5420464b5f4348414e4e454c5f535441434b535f3120464f524549474e204b4559202853504f545f494429205245464552454e4345532053504f54532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144452c0a20202020434f4e53545241494e5420464b5f4348414e4e454c5f535441434b535f3220464f524549474e204b4559202844535f494429205245464552454e43455320494d4147455f444154415f534554532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144450a293b0a0a43524541544520494e444558204348414e4e454c5f535441434b535f44535f494458204f4e204348414e4e454c5f535441434b532844535f4944293b0a43524541544520494e444558204348414e4e454c5f535441434b535f53504f545f494458204f4e204348414e4e454c5f535441434b532853504f545f4944293b0a43524541544520494e444558204348414e4e454c5f535441434b535f44494d5f494458204f4e204348414e4e454c5f535441434b5328582c20592c205a5f696e5f4d2c20545f696e5f534543293b0a0a435245415445205441424c4520494d4147455320280a2020202049442042494753455249414c20204e4f54204e554c4c2c0a2020200a20202020504154480946494c455f50415448204e4f54204e554c4c2c0a20202020494d4147455f494409434f44452c0a20202020434f4c4f5209434f4c4f525f434f4d504f4e454e542c0a202020200a202020205052494d415259204b455920284944290a293b0a0a435245415445205441424c452041435155495245445f494d4147455320280a2020202049442042494753455249414c20204e4f54204e554c4c2c0a2020200a0909494d475f494420544543485f49442c0a09095448554d424e41494c5f494420544543485f49442c0a0909494d4147455f5452414e53464f524d45525f464143544f52592042595445412c0a0a202020204348414e4e454c5f535441434b5f49442020544543485f4944204e4f54204e554c4c2c0a202020204348414e4e454c5f49442020544543485f4944204e4f54204e554c4c2c0a0a202020205052494d415259204b455920284944292c0a20202020434f4e53545241494e5420464b5f494d414745535f3120464f524549474e204b455920284348414e4e454c5f535441434b5f494429205245464552454e434553204348414e4e454c5f535441434b532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144452c0a20202020434f4e53545241494e5420464b5f494d414745535f3220464f524549474e204b455920284348414e4e454c5f494429205245464552454e434553204348414e4e454c532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144452c0a20202020434f4e53545241494e5420464b5f494d414745535f3320464f524549474e204b45592028494d475f494429205245464552454e43455320494d414745532028494429204f4e2044454c45544520534554204e554c4c204f4e2055504441544520434153434144452c0a20202020434f4e53545241494e5420464b5f494d414745535f3420464f524549474e204b455920285448554d424e41494c5f494429205245464552454e43455320494d414745532028494429204f4e2044454c45544520534554204e554c4c204f4e2055504441544520434153434144450a293b0a0a43524541544520494e44455820494d414745535f4348414e4e454c5f535441434b5f494458204f4e2041435155495245445f494d41474553284348414e4e454c5f535441434b5f4944293b0a43524541544520494e44455820494d414745535f4348414e4e454c5f494458204f4e2041435155495245445f494d41474553284348414e4e454c5f4944293b0a43524541544520494e44455820494d414745535f494d475f494458204f4e2041435155495245445f494d4147455328494d475f4944293b0a43524541544520494e44455820494d414745535f5448554d424e41494c5f494458204f4e2041435155495245445f494d41474553285448554d424e41494c5f4944293b0a0a435245415445205441424c45204556454e545320280a20204c4153545f5345454e5f44454c4554494f4e5f4556454e545f494420544543485f4944204e4f54204e554c4c0a293b0a0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f0a2f2a204645415455524520564543544f525320202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202a2f0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f200a0a435245415445205441424c4520464541545552455f4445465320280a2020202049442042494753455249414c20204e4f54204e554c4c2c0a202020200a20202020434f4445204c4f4e475f4e414d45204e4f54204e554c4c2c0a202020204c4142454c204c4f4e475f4e414d45204e4f54204e554c4c2c0a202020204445534352495054494f4e204445534352495054494f4e2c0a202020200a2020202044535f49442020544543485f4944204e4f54204e554c4c2c0a202020200a202020205052494d415259204b455920284944292c0a20202020434f4e53545241494e5420464b5f464541545552455f444546535f3120464f524549474e204b4559202844535f494429205245464552454e43455320414e414c595349535f444154415f534554532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144452c0a20202020434f4e53545241494e5420464541545552455f444546535f554b5f3120554e4951554528434f44452c2044535f4944290a293b0a0a43524541544520494e44455820464541545552455f444546535f44535f494458204f4e20464541545552455f444546532844535f4944293b0a0a435245415445205441424c4520464541545552455f564f434142554c4152595f5445524d5320280a0949442042494753455249414c20204e4f54204e554c4c2c0a0a2020434f4445204c4f4e475f4e414d45204e4f54204e554c4c2c0a202053455155454e43455f4e554d42455220494e5445474552204e4f54204e554c4c2c0a0946445f49442020544543485f4944204e4f54204e554c4c2c0a0a095052494d415259204b455920284944292c0a09434f4e53545241494e5420464b5f464541545552455f564f434142554c4152595f5445524d535f3120464f524549474e204b4559202846445f494429205245464552454e43455320464541545552455f444546532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144450a293b0a0a43524541544520494e44455820464541545552455f564f434142554c4152595f5445524d535f46445f494458204f4e20464541545552455f564f434142554c4152595f5445524d532846445f4944293b0a0a435245415445205441424c4520464541545552455f56414c55455320280a2020202049442042494753455249414c20204e4f54204e554c4c2c0a09090a09092d2d20776520757365207468652066697865642064696d656e73696f6e206d657465727320686572650a09095a5f696e5f4d205245414c2c0a09092d2d20776520757365207468652066697865642064696d656e73696f6e207365636f6e647320686572650a0909545f696e5f534543205245414c2c0a09092d2d2053657269616c697a6564203244206d617472697820776974682076616c75657320666f7220656163682073706f742e0a09092d2d20436f6e7461696e7320666c6f6174732077686963682063616e206265204e614e2e200a09092d2d204974206973206e65766572206120636173652074686174207468652077686f6c65206d617472697820636f6e7461696e73204e614e202d20696e2073756368206120636173652077652073617665206e6f7468696e672e0a09092d2d204966206665617475726520646566696e6974696f6e2068617320736f6d6520636f6e6e656374656420766f636162756c617279207465726d73207468656e20746865206d6174726978200a09092d2d2073746f72657320464541545552455f564f434142554c4152595f5445524d532e53455155454e43455f4e554d424552206f6620746865207465726d73202873686f756c64206265206361737465642066726f6d20666c6f617420746f20696e74292e0a09092d2d20496620746865207465726d206973206e756c6c2074686520466c6f61742e4e614e2069732073746f7265642e0a090956414c554553204259544541204e4f54204e554c4c2c0a09090a090946445f49442020544543485f4944204e4f54204e554c4c2c0a09090a09095052494d415259204b455920284944292c0a0909434f4e53545241494e5420464b5f464541545552455f56414c5545535f3120464f524549474e204b4559202846445f494429205245464552454e43455320464541545552455f444546532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144450a202020202d2d205468697320636f6e737461696e7420646f6573206e6f74206d616b6520616e792073656e73652e204c65617665206974206f757420666f72206e6f772e0a202020202d2d20434f4e53545241494e5420464541545552455f56414c5545535f554b5f3120554e49515545285a5f696e5f4d2c20545f696e5f534543290a293b0a0a43524541544520494e44455820464541545552455f56414c5545535f46445f494458204f4e20464541545552455f56414c5545532846445f4944293b0a43524541544520494e44455820464541545552455f56414c5545535f5a5f414e445f545f494458204f4e20464541545552455f56414c554553285a5f696e5f4d2c20545f696e5f534543293b0a0a0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f0a2f2a2046554e4354494f4e5320414e44205452494747455253202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202a2f0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f200a0a435245415445204f52205245504c4143452046554e4354494f4e2044454c4554455f554e555345445f494d4147455328292052455455524e5320747269676765722041532024240a424547494e0a20202064656c6574652066726f6d20696d61676573207768657265206964203d204f4c442e696d675f6964206f72206964203d204f4c442e7468756d626e61696c5f69643b0a20202052455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a435245415445205452494747455220554e555345445f494d414745532041465445522044454c455445204f4e2041435155495245445f494d414745530a20202020464f52204541434820524f5720455845435554452050524f4345445552452044454c4554455f554e555345445f494d4147455328293b0a0a435245415445204f52205245504c4143452046554e4354494f4e2044454c4554455f554e555345445f4e554c4c45445f494d4147455328292052455455524e5320747269676765722041532024240a424547494e0a096966204e45572e696d675f6964204953204e554c4c207468656e0a09096966204f4c442e696d675f6964204953204e4f54204e554c4c207468656e0a0909202064656c6574652066726f6d20696d61676573207768657265206964203d204f4c442e696d675f69643b0a0909656e642069663b0a09656e642069663b0a096966204e45572e7468756d626e61696c5f6964204953204e554c4c207468656e0a09096966204f4c442e7468756d626e61696c5f6964204953204e4f54204e554c4c207468656e0a0909202064656c6574652066726f6d20696d61676573207768657265206964203d204f4c442e7468756d626e61696c5f69643b0a0909656e642069663b0a09656e642069663b0a0952455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a435245415445205452494747455220554e555345445f4e554c4c45445f494d4147455320414654455220555044415445204f4e2041435155495245445f494d414745530a20202020464f52204541434820524f5720455845435554452050524f4345445552452044454c4554455f554e555345445f4e554c4c45445f494d4147455328293b0a0a435245415445204f52205245504c4143452046554e4354494f4e2044454c4554455f454d5054595f41435155495245445f494d4147455328292052455455524e5320747269676765722041532024240a424547494e0a0964656c6574652066726f6d2061637175697265645f696d61676573207768657265206964203d204f4c442e69643b0a0952455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a435245415445205452494747455220454d5054595f41435155495245445f494d41474553204245464f524520555044415445204f4e2041435155495245445f494d414745530a0909464f52204541434820524f570a09095748454e20284e45572e696d675f6964204953204e554c4c20414e44204e45572e7468756d626e61696c5f6964204953204e554c4c290a0909455845435554452050524f4345445552452044454c4554455f454d5054595f41435155495245445f494d4147455328293b0a0a0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a20204372656174652074726967676572204348414e4e454c5f535441434b535f434845434b20776869636820636865636b7320696620626f74682073706f745f696420616e6420646174617365742e636f6e745f6964200a2d2d20202020202020202020202061726520626f7468206e756c6c206f72206e6f74206e756c6c2e0a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a435245415445204f52205245504c4143452046554e4354494f4e204348414e4e454c5f535441434b535f434845434b28292052455455524e5320747269676765722041532024240a4445434c4152450a202020765f636f6e745f69642020434f44453b0a424547494e0a0a20202073656c65637420636f6e745f696420696e746f20765f636f6e745f69642066726f6d20696d6167655f646174615f73657473207768657265206964203d204e45572e64735f69643b0a0a2020202d2d20436865636b2074686174206966207468657265206973206e6f2073706f74207468616e207468657265206973206e6f206461746173657420636f6e7461696e65722061732077656c6c0a202020696620765f636f6e745f6964204953204e554c4c207468656e0a2020202020206966204e45572e73706f745f6964204953204e4f54204e554c4c207468656e0a202020202020202020524149534520455843455054494f4e2027496e736572742f557064617465206f66204348414e4e454c5f535441434b53206661696c65642c20617320746865206461746173657420636f6e7461696e6572206973206e6f74207365742c206275742073706f74206973202873706f74206964203d2025292e272c4e45572e73706f745f69643b0a202020202020656e642069663b0a0920656c73650a2020202020206966204e45572e73706f745f6964204953204e554c4c207468656e0a202020202020202020524149534520455843455054494f4e2027496e736572742f557064617465206f66204348414e4e454c5f535441434b53206661696c65642c20617320746865206461746173657420636f6e7461696e65722069732073657420286964203d2025292c206275742073706f74206973206e6f74207365742e272c765f636f6e745f69643b0a202020202020656e642069663b200a202020656e642069663b0a20202052455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a4352454154452054524947474552204348414e4e454c5f535441434b535f434845434b204245464f524520494e53455254204f5220555044415445204f4e204348414e4e454c5f535441434b530a20202020464f52204541434820524f5720455845435554452050524f434544555245204348414e4e454c5f535441434b535f434845434b28293b0a202020200a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a2d2d2020507572706f73653a2020437265617465207472696767657220494d4147455f5452414e53464f524d4154494f4e535f44454641554c545f434845434b20776869636820636865636b73200a2d2d2020202020202020202020206966206174206d6f7374206f6e65206368616e6e656c2773207472616e73666f726d6174696f6e2069732064656661756c740a2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d0a0a435245415445204f52205245504c4143452046554e4354494f4e20494d4147455f5452414e53464f524d4154494f4e535f44454641554c545f434845434b28292052455455524e5320747269676765722041532024240a4445434c4152450a202020765f69735f64656661756c7420626f6f6c65616e3b0a424547494e0a2020206966204e45572e69735f64656661756c74203d20275427207468656e0a0920202073656c6563742069735f64656661756c7420696e746f20765f69735f64656661756c742066726f6d20494d4147455f5452414e53464f524d4154494f4e53200a092020200977686572652069735f64656661756c74203d20275427200a092020200909092020616e64206368616e6e656c5f6964203d204e45572e6368616e6e656c5f69640a0920202009090909616e6420696420213d204e45572e69643b0a09202020696620765f69735f64656661756c74206973204e4f54204e554c4c207468656e0a09202020202020524149534520455843455054494f4e2027496e736572742f557064617465206f6620696d616765207472616e73666f726d6174696f6e2028436f64653a202529206661696c65642c20617320746865206e6577207265636f7264206861732069735f64656661756c742073657420746f207472756520616e6420746865726520697320616c726561647920612064656661756c74207265636f726420646566696e65642e272c204e45572e636f64653b0a09202020656e642069663b0a202020656e642069663b0a0a20202052455455524e204e45573b0a454e443b0a2424204c414e47554147452027706c706773716c273b0a0a435245415445205452494747455220494d4147455f5452414e53464f524d4154494f4e535f44454641554c545f434845434b204245464f524520494e53455254204f5220555044415445204f4e20494d4147455f5452414e53464f524d4154494f4e530a20202020464f52204541434820524f5720455845435554452050524f43454455524520494d4147455f5452414e53464f524d4154494f4e535f44454641554c545f434845434b28293b0a202020200a	\N
\.


--
-- Data for Name: events; Type: TABLE DATA; Schema: public; Owner: -
--

COPY events (last_seen_deletion_event_id) FROM stdin;
175
\.


--
-- Data for Name: experiments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiments (id, perm_id, image_transformer_factory) FROM stdin;
1	20130412105232616-2	\N
2	20130412150049446-204	\N
\.


--
-- Data for Name: feature_defs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY feature_defs (id, code, label, description, ds_id) FROM stdin;
9	ROW_NUMBER	row number	row number	3
10	COLUMN_NUMBER	column number	column number	3
11	TPU	TPU	TPU	3
12	STATE	STATE	STATE	3
\.


--
-- Data for Name: feature_values; Type: TABLE DATA; Schema: public; Owner: -
--

COPY feature_values (id, z_in_m, t_in_sec, "values", fd_id) FROM stdin;
9	0	0	\\x464c04020c00000008000000000000410000e0400000c0400000a0400000804000004040000000400000803f000000410000e0400000c0400000a0400000804000004040000000400000803f000000410000e0400000c0400000a0400000804000004040000000400000803f000000410000e0400000c0400000a0400000804000004040000000400000803f000000410000e0400000c0400000a0400000804000004040000000400000803f000000410000e0400000c0400000a0400000804000004040000000400000803f000000410000e0400000c0400000a0400000804000004040000000400000803f000000410000e0400000c0400000a0400000804000004040000000400000803f000000410000e0400000c0400000a0400000804000004040000000400000803f000000410000e0400000c0400000a0400000804000004040000000400000803f000000410000e0400000c0400000a0400000804000004040000000400000803f000000410000e0400000c0400000a0400000804000004040000000400000803f	9
10	0	0	\\x464c04020c000000080000000000803f0000803f0000803f0000803f0000803f0000803f0000803f0000803f0000004000000040000000400000004000000040000000400000004000000040000040400000404000004040000040400000404000004040000040400000404000008040000080400000804000008040000080400000804000008040000080400000a0400000a0400000a0400000a0400000a0400000a0400000a0400000a0400000c0400000c0400000c0400000c0400000c0400000c0400000c0400000c0400000e0400000e0400000e0400000e0400000e0400000e0400000e0400000e04000000041000000410000004100000041000000410000004100000041000000410000104100001041000010410000104100001041000010410000104100001041000020410000204100002041000020410000204100002041000020410000204100003041000030410000304100003041000030410000304100003041000030410000404100004041000040410000404100004041000040410000404100004041	10
11	0	0	\\x464c04020c00000008000000c315343f440a1b3fd7a7fd3e9760c93e3c46923e176e483e0d9df03d85ec733c4227313fde75193f2cd7f73ea080b53e00a0813e0cfc5d3e072ec23df245683b622f3d3fef911f3f02c6013fa162d03e46ad993ec1316f3ed44ae33ddc491dbc1325423f6d27163f9459073f2725cf3e53cba03e0fd26e3ebf823a3efde3043dfeef333fb1141e3f12a1de3efa5ec33ebf6aa13ea537303e1381e33da9c6dabd904a173f7f592c3f1c3fe83e150cad3ec0e0603e9223853e7d65c03d62576a3dcec73b3f0cf7133fa141c93ea2f6cc3e5d40523e4081673e8ec3353ed68b39bcdc6a2e3f6e39193f77a10a3f7c42ce3e4a357d3e42814a3e904410be022c133d5bd43e3f9fb00b3fb3bb013ffc32b43e13a9463ed851443ed5c65f3d8e6a6cbc20740c3fdfa8173f12d1093f1928a63e4094863ea92c3f3ed005133e90efc83d21e0043f7976cd3eef6aac3e44f08c3e5c00c63ec670333ea4da463d2ee96b3cb5701c3fa1e1223fa9c00f3f2732b63ed8cfe83e5e62f23ea36b553e5c0d9c3c	11
12	0	0	\\x464c04020c0000000800000000000000000000000000803f0000803f0000803f0000803f0000803f0000803f0000803f0000000000000000000000000000803f0000803f0000803f0000803f0000803f000000000000803f0000803f000000000000803f00000000000000000000803f0000803f000000000000803f0000803f000000000000803f00000000000000000000803f000000000000803f0000803f00000000000000000000803f000000000000803f0000803f0000803f000000000000803f000000000000803f0000803f0000803f0000803f0000803f0000803f0000803f0000803f000000000000803f00000000000000000000803f000000000000803f0000803f0000803f000000000000000000000000000000000000000000000000000000000000803f00000000000000000000803f000000000000803f000000000000803f0000803f00000000000000000000803f00000000000000000000803f000000000000803f000000000000803f00000000000000000000803f000000000000803f0000803f	12
\.


--
-- Data for Name: feature_vocabulary_terms; Type: TABLE DATA; Schema: public; Owner: -
--

COPY feature_vocabulary_terms (id, code, sequence_number, fd_id) FROM stdin;
5	UNSTABLE	0	12
6	STABLE	1	12
\.


--
-- Data for Name: image_data_sets; Type: TABLE DATA; Schema: public; Owner: -
--

COPY image_data_sets (id, perm_id, fields_width, fields_height, image_transformer_factory, is_multidimensional, image_library_name, image_library_reader_name, cont_id) FROM stdin;
2	20130412143121081-200	3	3	\N	f	\N	\N	1
3	20130412152038345-381	3	3	\N	f	\N	\N	2
5	20130412153119864-385	3	3	\N	f	\N	\N	1
\.


--
-- Data for Name: image_transformations; Type: TABLE DATA; Schema: public; Owner: -
--

COPY image_transformations (id, code, label, description, image_transformer_factory, is_editable, is_default, channel_id) FROM stdin;
25	_CONVERT_1	Edge detection	Transforms images with ImageMagic tool by calling: 'convert -edge 1 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400072d656467652031	f	f	4
26	_CONVERT_2	Radial Blur	Transforms images with ImageMagic tool by calling: 'convert -radial-blur 30 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74000f2d72616469616c2d626c7572203330	f	f	4
27	_CONVERT_3	Fuzzy	Transforms images with ImageMagic tool by calling: 'convert -blur 3x.7 -solarize 50% -level 50%,0 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400252d626c75722033782e37202d736f6c6172697a6520353025202d6c6576656c203530252c30	f	f	4
28	_CONVERT_4	3D 1	Transforms images with ImageMagic tool by calling: 'convert -shade 0x45 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74000b2d73686164652030783435	f	f	4
29	_CONVERT_5	3D 2	Transforms images with ImageMagic tool by calling: 'convert -shade 90x60 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74000c2d7368616465203930783630	f	f	4
30	_CONVERT_6	3D 3	Transforms images with ImageMagic tool by calling: 'convert -blur 0x3 -shade 120x45 -normalize ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400222d626c757220307833202d736861646520313230783435202d6e6f726d616c697a65	f	f	4
31	_CONVERT_7	Motion Blur	Transforms images with ImageMagic tool by calling: 'convert -motion-blur 0x12+45 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400142d6d6f74696f6e2d626c757220307831322b3435	f	f	4
32	_CONVERT_8	FFT	Transforms images with ImageMagic tool by calling: 'convert -fft -delete 1 -auto-level -evaluate log 100000 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74002f2d666674202d64656c6574652031202d6175746f2d6c6576656c202d6576616c75617465206c6f6720313030303030	f	f	4
33	_CONVERT_1	Edge detection	Transforms images with ImageMagic tool by calling: 'convert -edge 1 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400072d656467652031	f	f	5
34	_CONVERT_2	Radial Blur	Transforms images with ImageMagic tool by calling: 'convert -radial-blur 30 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74000f2d72616469616c2d626c7572203330	f	f	5
35	_CONVERT_3	Fuzzy	Transforms images with ImageMagic tool by calling: 'convert -blur 3x.7 -solarize 50% -level 50%,0 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400252d626c75722033782e37202d736f6c6172697a6520353025202d6c6576656c203530252c30	f	f	5
36	_CONVERT_4	3D 1	Transforms images with ImageMagic tool by calling: 'convert -shade 0x45 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74000b2d73686164652030783435	f	f	5
37	_CONVERT_5	3D 2	Transforms images with ImageMagic tool by calling: 'convert -shade 90x60 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74000c2d7368616465203930783630	f	f	5
38	_CONVERT_6	3D 3	Transforms images with ImageMagic tool by calling: 'convert -blur 0x3 -shade 120x45 -normalize ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400222d626c757220307833202d736861646520313230783435202d6e6f726d616c697a65	f	f	5
39	_CONVERT_7	Motion Blur	Transforms images with ImageMagic tool by calling: 'convert -motion-blur 0x12+45 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400142d6d6f74696f6e2d626c757220307831322b3435	f	f	5
40	_CONVERT_8	FFT	Transforms images with ImageMagic tool by calling: 'convert -fft -delete 1 -auto-level -evaluate log 100000 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74002f2d666674202d64656c6574652031202d6175746f2d6c6576656c202d6576616c75617465206c6f6720313030303030	f	f	5
41	_CONVERT_1	Edge detection	Transforms images with ImageMagic tool by calling: 'convert -edge 1 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400072d656467652031	f	f	6
42	_CONVERT_2	Radial Blur	Transforms images with ImageMagic tool by calling: 'convert -radial-blur 30 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74000f2d72616469616c2d626c7572203330	f	f	6
43	_CONVERT_3	Fuzzy	Transforms images with ImageMagic tool by calling: 'convert -blur 3x.7 -solarize 50% -level 50%,0 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400252d626c75722033782e37202d736f6c6172697a6520353025202d6c6576656c203530252c30	f	f	6
44	_CONVERT_4	3D 1	Transforms images with ImageMagic tool by calling: 'convert -shade 0x45 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74000b2d73686164652030783435	f	f	6
45	_CONVERT_5	3D 2	Transforms images with ImageMagic tool by calling: 'convert -shade 90x60 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74000c2d7368616465203930783630	f	f	6
46	_CONVERT_6	3D 3	Transforms images with ImageMagic tool by calling: 'convert -blur 0x3 -shade 120x45 -normalize ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400222d626c757220307833202d736861646520313230783435202d6e6f726d616c697a65	f	f	6
47	_CONVERT_7	Motion Blur	Transforms images with ImageMagic tool by calling: 'convert -motion-blur 0x12+45 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400142d6d6f74696f6e2d626c757220307831322b3435	f	f	6
48	_CONVERT_8	FFT	Transforms images with ImageMagic tool by calling: 'convert -fft -delete 1 -auto-level -evaluate log 100000 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74002f2d666674202d64656c6574652031202d6175746f2d6c6576656c202d6576616c75617465206c6f6720313030303030	f	f	6
49	_CONVERT_1	Edge detection	Transforms images with ImageMagic tool by calling: 'convert -edge 1 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400072d656467652031	f	f	7
50	_CONVERT_2	Radial Blur	Transforms images with ImageMagic tool by calling: 'convert -radial-blur 30 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74000f2d72616469616c2d626c7572203330	f	f	7
51	_CONVERT_3	Fuzzy	Transforms images with ImageMagic tool by calling: 'convert -blur 3x.7 -solarize 50% -level 50%,0 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400252d626c75722033782e37202d736f6c6172697a6520353025202d6c6576656c203530252c30	f	f	7
52	_CONVERT_4	3D 1	Transforms images with ImageMagic tool by calling: 'convert -shade 0x45 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74000b2d73686164652030783435	f	f	7
53	_CONVERT_5	3D 2	Transforms images with ImageMagic tool by calling: 'convert -shade 90x60 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74000c2d7368616465203930783630	f	f	7
54	_CONVERT_6	3D 3	Transforms images with ImageMagic tool by calling: 'convert -blur 0x3 -shade 120x45 -normalize ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400222d626c757220307833202d736861646520313230783435202d6e6f726d616c697a65	f	f	7
55	_CONVERT_7	Motion Blur	Transforms images with ImageMagic tool by calling: 'convert -motion-blur 0x12+45 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400142d6d6f74696f6e2d626c757220307831322b3435	f	f	7
56	_CONVERT_8	FFT	Transforms images with ImageMagic tool by calling: 'convert -fft -delete 1 -auto-level -evaluate log 100000 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74002f2d666674202d64656c6574652031202d6175746f2d6c6576656c202d6576616c75617465206c6f6720313030303030	f	f	7
57	_CONVERT_1	Edge detection	Transforms images with ImageMagic tool by calling: 'convert -edge 1 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400072d656467652031	f	f	8
58	_CONVERT_2	Radial Blur	Transforms images with ImageMagic tool by calling: 'convert -radial-blur 30 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74000f2d72616469616c2d626c7572203330	f	f	8
59	_CONVERT_3	Fuzzy	Transforms images with ImageMagic tool by calling: 'convert -blur 3x.7 -solarize 50% -level 50%,0 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400252d626c75722033782e37202d736f6c6172697a6520353025202d6c6576656c203530252c30	f	f	8
60	_CONVERT_4	3D 1	Transforms images with ImageMagic tool by calling: 'convert -shade 0x45 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74000b2d73686164652030783435	f	f	8
61	_CONVERT_5	3D 2	Transforms images with ImageMagic tool by calling: 'convert -shade 90x60 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74000c2d7368616465203930783630	f	f	8
62	_CONVERT_6	3D 3	Transforms images with ImageMagic tool by calling: 'convert -blur 0x3 -shade 120x45 -normalize ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400222d626c757220307833202d736861646520313230783435202d6e6f726d616c697a65	f	f	8
63	_CONVERT_7	Motion Blur	Transforms images with ImageMagic tool by calling: 'convert -motion-blur 0x12+45 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400142d6d6f74696f6e2d626c757220307831322b3435	f	f	8
64	_CONVERT_8	FFT	Transforms images with ImageMagic tool by calling: 'convert -fft -delete 1 -auto-level -evaluate log 100000 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74002f2d666674202d64656c6574652031202d6175746f2d6c6576656c202d6576616c75617465206c6f6720313030303030	f	f	8
65	_CONVERT_1	Edge detection	Transforms images with ImageMagic tool by calling: 'convert -edge 1 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400072d656467652031	f	f	9
66	_CONVERT_2	Radial Blur	Transforms images with ImageMagic tool by calling: 'convert -radial-blur 30 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74000f2d72616469616c2d626c7572203330	f	f	9
67	_CONVERT_3	Fuzzy	Transforms images with ImageMagic tool by calling: 'convert -blur 3x.7 -solarize 50% -level 50%,0 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400252d626c75722033782e37202d736f6c6172697a6520353025202d6c6576656c203530252c30	f	f	9
68	_CONVERT_4	3D 1	Transforms images with ImageMagic tool by calling: 'convert -shade 0x45 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74000b2d73686164652030783435	f	f	9
69	_CONVERT_5	3D 2	Transforms images with ImageMagic tool by calling: 'convert -shade 90x60 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74000c2d7368616465203930783630	f	f	9
70	_CONVERT_6	3D 3	Transforms images with ImageMagic tool by calling: 'convert -blur 0x3 -shade 120x45 -normalize ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400222d626c757220307833202d736861646520313230783435202d6e6f726d616c697a65	f	f	9
71	_CONVERT_7	Motion Blur	Transforms images with ImageMagic tool by calling: 'convert -motion-blur 0x12+45 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b7400142d6d6f74696f6e2d626c757220307831322b3435	f	f	9
72	_CONVERT_8	FFT	Transforms images with ImageMagic tool by calling: 'convert -fft -delete 1 -auto-level -evaluate log 100000 ...'	\\xaced00057372005b63682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727900000000000000010200024c000663686f6963657400684c63682f73797374656d73782f636973642f6f70656e6269732f6473732f65746c2f64746f2f6170692f7472616e73666f726d6174696f6e732f436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f6963653b4c0013636f6e76657274436c69417267756d656e74737400124c6a6176612f6c616e672f537472696e673b78707e72006663682e73797374656d73782e636973642e6f70656e6269732e6473732e65746c2e64746f2e6170692e7472616e73666f726d6174696f6e732e436f6e76657274546f6f6c496d6167655472616e73666f726d6572466163746f727924546f6f6c43686f69636500000000000000001200007872000e6a6176612e6c616e672e456e756d00000000000000001200007870740013454e464f5243455f494d4147454d414749434b74002f2d666674202d64656c6574652031202d6175746f2d6c6576656c202d6576616c75617465206c6f6720313030303030	f	f	9
\.


--
-- Data for Name: image_zoom_level_transformations; Type: TABLE DATA; Schema: public; Owner: -
--

COPY image_zoom_level_transformations (id, zoom_level_id, channel_id, image_transformation_id) FROM stdin;
7	7	5	34
8	7	6	42
9	7	4	26
10	8	5	34
11	8	6	42
12	8	4	26
13	10	8	58
14	10	9	66
15	10	7	50
\.


--
-- Data for Name: image_zoom_levels; Type: TABLE DATA; Schema: public; Owner: -
--

COPY image_zoom_levels (id, is_original, container_dataset_id, physical_dataset_perm_id, path, width, height, color_depth, file_type) FROM stdin;
5	t	2	20130412143119901-199		512	512	32	\N
6	f	2	20130412142942295-198	thumbnails_256x256.h5ar	256	256	32	png
7	f	2	20130412142543232-197	thumbnails_128x128__CONVERT_2.h5ar	128	128	32	jpg
8	f	2	20130412142205843-196	thumbnails_64x64__CONVERT_2.h5ar	64	64	32	jpg
9	t	3	20130412152036861-380		512	512	32	\N
10	f	3	20130412151710024-379	thumbnails_64x64__CONVERT_2.h5ar	64	64	32	jpg
12	t	5	20130412153118625-384		512	512	32	\N
\.


--
-- Data for Name: images; Type: TABLE DATA; Schema: public; Owner: -
--

COPY images (id, path, image_id, color) FROM stdin;
5185	original/PLATE-1/bPLATE_wB2_s8_cRGB.png	\N	BLUE
5186	wB2_d2-3_cDAPI	\N	\N
5187	original/PLATE-1/bPLATE_wB2_s8_cRGB.png	\N	GREEN
5188	wB2_d2-3_cGFP	\N	\N
5189	original/PLATE-1/bPLATE_wB2_s8_cRGB.png	\N	RED
5190	wB2_d2-3_cCy5	\N	\N
5191	original/PLATE-1/bPLATE_wD9_s5_cRGB.png	\N	BLUE
5192	wD9_d2-2_cDAPI	\N	\N
5193	original/PLATE-1/bPLATE_wD9_s5_cRGB.png	\N	GREEN
5194	wD9_d2-2_cGFP	\N	\N
5195	original/PLATE-1/bPLATE_wD9_s5_cRGB.png	\N	RED
5196	wD9_d2-2_cCy5	\N	\N
5197	original/PLATE-1/bPLATE_wG4_s2_cRGB.png	\N	BLUE
5198	wG4_d2-1_cDAPI	\N	\N
5199	original/PLATE-1/bPLATE_wG4_s2_cRGB.png	\N	GREEN
5200	wG4_d2-1_cGFP	\N	\N
5201	original/PLATE-1/bPLATE_wG4_s2_cRGB.png	\N	RED
5202	wG4_d2-1_cCy5	\N	\N
5203	original/PLATE-1/bPLATE_wA12_s8_cRGB.png	\N	BLUE
5204	wA12_d2-3_cDAPI	\N	\N
5205	original/PLATE-1/bPLATE_wA12_s8_cRGB.png	\N	GREEN
5206	wA12_d2-3_cGFP	\N	\N
5207	original/PLATE-1/bPLATE_wA12_s8_cRGB.png	\N	RED
5208	wA12_d2-3_cCy5	\N	\N
5209	original/PLATE-1/bPLATE_wD7_s5_cRGB.png	\N	BLUE
5210	wD7_d2-2_cDAPI	\N	\N
5211	original/PLATE-1/bPLATE_wD7_s5_cRGB.png	\N	GREEN
5212	wD7_d2-2_cGFP	\N	\N
5213	original/PLATE-1/bPLATE_wD7_s5_cRGB.png	\N	RED
5214	wD7_d2-2_cCy5	\N	\N
5215	original/PLATE-1/bPLATE_wG2_s2_cRGB.png	\N	BLUE
5216	wG2_d2-1_cDAPI	\N	\N
5217	original/PLATE-1/bPLATE_wG2_s2_cRGB.png	\N	GREEN
5218	wG2_d2-1_cGFP	\N	\N
5219	original/PLATE-1/bPLATE_wG2_s2_cRGB.png	\N	RED
5220	wG2_d2-1_cCy5	\N	\N
5221	original/PLATE-1/bPLATE_wB7_s6_cRGB.png	\N	BLUE
5222	wB7_d3-2_cDAPI	\N	\N
5223	original/PLATE-1/bPLATE_wB7_s6_cRGB.png	\N	GREEN
5224	wB7_d3-2_cGFP	\N	\N
5225	original/PLATE-1/bPLATE_wB7_s6_cRGB.png	\N	RED
5226	wB7_d3-2_cCy5	\N	\N
5227	original/PLATE-1/bPLATE_wB6_s2_cRGB.png	\N	BLUE
5228	wB6_d2-1_cDAPI	\N	\N
5229	original/PLATE-1/bPLATE_wB6_s2_cRGB.png	\N	GREEN
5230	wB6_d2-1_cGFP	\N	\N
5231	original/PLATE-1/bPLATE_wB6_s2_cRGB.png	\N	RED
5232	wB6_d2-1_cCy5	\N	\N
5233	original/PLATE-1/bPLATE_wE2_s3_cRGB.png	\N	BLUE
5234	wE2_d3-1_cDAPI	\N	\N
5235	original/PLATE-1/bPLATE_wE2_s3_cRGB.png	\N	GREEN
5236	wE2_d3-1_cGFP	\N	\N
5237	original/PLATE-1/bPLATE_wE2_s3_cRGB.png	\N	RED
5238	wE2_d3-1_cCy5	\N	\N
5239	original/PLATE-1/bPLATE_wA10_s8_cRGB.png	\N	BLUE
5240	wA10_d2-3_cDAPI	\N	\N
5241	original/PLATE-1/bPLATE_wA10_s8_cRGB.png	\N	GREEN
5242	wA10_d2-3_cGFP	\N	\N
5243	original/PLATE-1/bPLATE_wA10_s8_cRGB.png	\N	RED
5244	wA10_d2-3_cCy5	\N	\N
5245	original/PLATE-1/bPLATE_wD5_s5_cRGB.png	\N	BLUE
5246	wD5_d2-2_cDAPI	\N	\N
5247	original/PLATE-1/bPLATE_wD5_s5_cRGB.png	\N	GREEN
5248	wD5_d2-2_cGFP	\N	\N
5249	original/PLATE-1/bPLATE_wD5_s5_cRGB.png	\N	RED
5250	wD5_d2-2_cCy5	\N	\N
5251	original/PLATE-1/bPLATE_wF12_s2_cRGB.png	\N	BLUE
5252	wF12_d2-1_cDAPI	\N	\N
5253	original/PLATE-1/bPLATE_wF12_s2_cRGB.png	\N	GREEN
5254	wF12_d2-1_cGFP	\N	\N
5255	original/PLATE-1/bPLATE_wF12_s2_cRGB.png	\N	RED
5256	wF12_d2-1_cCy5	\N	\N
5257	original/PLATE-1/bPLATE_wF10_s9_cRGB.png	\N	BLUE
5258	wF10_d3-3_cDAPI	\N	\N
5259	original/PLATE-1/bPLATE_wF10_s9_cRGB.png	\N	GREEN
5260	wF10_d3-3_cGFP	\N	\N
5261	original/PLATE-1/bPLATE_wF10_s9_cRGB.png	\N	RED
5262	wF10_d3-3_cCy5	\N	\N
5263	original/PLATE-1/bPLATE_wC6_s6_cRGB.png	\N	BLUE
5264	wC6_d3-2_cDAPI	\N	\N
5265	original/PLATE-1/bPLATE_wC6_s6_cRGB.png	\N	GREEN
5266	wC6_d3-2_cGFP	\N	\N
5267	original/PLATE-1/bPLATE_wC6_s6_cRGB.png	\N	RED
5268	wC6_d3-2_cCy5	\N	\N
5269	original/PLATE-1/bPLATE_wF1_s3_cRGB.png	\N	BLUE
5270	wF1_d3-1_cDAPI	\N	\N
5271	original/PLATE-1/bPLATE_wF1_s3_cRGB.png	\N	GREEN
5272	wF1_d3-1_cGFP	\N	\N
5273	original/PLATE-1/bPLATE_wF1_s3_cRGB.png	\N	RED
5274	wF1_d3-1_cCy5	\N	\N
5275	original/PLATE-1/bPLATE_wH11_s6_cRGB.png	\N	BLUE
5276	wH11_d3-2_cDAPI	\N	\N
5277	original/PLATE-1/bPLATE_wH11_s6_cRGB.png	\N	GREEN
5278	wH11_d3-2_cGFP	\N	\N
5279	original/PLATE-1/bPLATE_wH11_s6_cRGB.png	\N	RED
5280	wH11_d3-2_cCy5	\N	\N
5281	original/PLATE-1/bPLATE_wF4_s9_cRGB.png	\N	BLUE
5282	wF4_d3-3_cDAPI	\N	\N
5283	original/PLATE-1/bPLATE_wF4_s9_cRGB.png	\N	GREEN
5284	wF4_d3-3_cGFP	\N	\N
5285	original/PLATE-1/bPLATE_wF4_s9_cRGB.png	\N	RED
5286	wF4_d3-3_cCy5	\N	\N
5287	original/PLATE-1/bPLATE_wA11_s1_cRGB.png	\N	BLUE
5288	wA11_d1-1_cDAPI	\N	\N
5289	original/PLATE-1/bPLATE_wA11_s1_cRGB.png	\N	GREEN
5290	wA11_d1-1_cGFP	\N	\N
5291	original/PLATE-1/bPLATE_wA11_s1_cRGB.png	\N	RED
5292	wA11_d1-1_cCy5	\N	\N
5293	original/PLATE-1/bPLATE_wA9_s2_cRGB.png	\N	BLUE
5294	wA9_d2-1_cDAPI	\N	\N
5295	original/PLATE-1/bPLATE_wA9_s2_cRGB.png	\N	GREEN
5296	wA9_d2-1_cGFP	\N	\N
5297	original/PLATE-1/bPLATE_wA9_s2_cRGB.png	\N	RED
5298	wA9_d2-1_cCy5	\N	\N
5299	original/PLATE-1/bPLATE_wH1_s8_cRGB.png	\N	BLUE
5300	wH1_d2-3_cDAPI	\N	\N
5301	original/PLATE-1/bPLATE_wH1_s8_cRGB.png	\N	GREEN
5302	wH1_d2-3_cGFP	\N	\N
5303	original/PLATE-1/bPLATE_wH1_s8_cRGB.png	\N	RED
5304	wH1_d2-3_cCy5	\N	\N
5305	original/PLATE-1/bPLATE_wD4_s8_cRGB.png	\N	BLUE
5306	wD4_d2-3_cDAPI	\N	\N
5307	original/PLATE-1/bPLATE_wD4_s8_cRGB.png	\N	GREEN
5308	wD4_d2-3_cGFP	\N	\N
5309	original/PLATE-1/bPLATE_wD4_s8_cRGB.png	\N	RED
5310	wD4_d2-3_cCy5	\N	\N
5311	original/PLATE-1/bPLATE_wF11_s5_cRGB.png	\N	BLUE
5312	wF11_d2-2_cDAPI	\N	\N
5313	original/PLATE-1/bPLATE_wF11_s5_cRGB.png	\N	GREEN
5314	wF11_d2-2_cGFP	\N	\N
5315	original/PLATE-1/bPLATE_wF11_s5_cRGB.png	\N	RED
5316	wF11_d2-2_cCy5	\N	\N
5317	original/PLATE-1/bPLATE_wA12_s6_cRGB.png	\N	BLUE
5318	wA12_d3-2_cDAPI	\N	\N
5319	original/PLATE-1/bPLATE_wA12_s6_cRGB.png	\N	GREEN
5320	wA12_d3-2_cGFP	\N	\N
5321	original/PLATE-1/bPLATE_wA12_s6_cRGB.png	\N	RED
5322	wA12_d3-2_cCy5	\N	\N
5323	original/PLATE-1/bPLATE_wD7_s3_cRGB.png	\N	BLUE
5324	wD7_d3-1_cDAPI	\N	\N
5325	original/PLATE-1/bPLATE_wD7_s3_cRGB.png	\N	GREEN
5326	wD7_d3-1_cGFP	\N	\N
5327	original/PLATE-1/bPLATE_wD7_s3_cRGB.png	\N	RED
5328	wD7_d3-1_cCy5	\N	\N
5329	original/PLATE-1/bPLATE_wH3_s6_cRGB.png	\N	BLUE
5330	wH3_d3-2_cDAPI	\N	\N
5331	original/PLATE-1/bPLATE_wH3_s6_cRGB.png	\N	GREEN
5332	wH3_d3-2_cGFP	\N	\N
5333	original/PLATE-1/bPLATE_wH3_s6_cRGB.png	\N	RED
5334	wH3_d3-2_cCy5	\N	\N
5335	original/PLATE-1/bPLATE_wE8_s9_cRGB.png	\N	BLUE
5336	wE8_d3-3_cDAPI	\N	\N
5337	original/PLATE-1/bPLATE_wE8_s9_cRGB.png	\N	GREEN
5338	wE8_d3-3_cGFP	\N	\N
5339	original/PLATE-1/bPLATE_wE8_s9_cRGB.png	\N	RED
5340	wE8_d3-3_cCy5	\N	\N
5341	original/PLATE-1/bPLATE_wD10_s3_cRGB.png	\N	BLUE
5342	wD10_d3-1_cDAPI	\N	\N
5343	original/PLATE-1/bPLATE_wD10_s3_cRGB.png	\N	GREEN
5344	wD10_d3-1_cGFP	\N	\N
5345	original/PLATE-1/bPLATE_wD10_s3_cRGB.png	\N	RED
5346	wD10_d3-1_cCy5	\N	\N
5347	original/PLATE-1/bPLATE_wB3_s6_cRGB.png	\N	BLUE
5348	wB3_d3-2_cDAPI	\N	\N
5349	original/PLATE-1/bPLATE_wB3_s6_cRGB.png	\N	GREEN
5350	wB3_d3-2_cGFP	\N	\N
5351	original/PLATE-1/bPLATE_wB3_s6_cRGB.png	\N	RED
5352	wB3_d3-2_cCy5	\N	\N
5353	original/PLATE-1/bPLATE_wA1_s2_cRGB.png	\N	BLUE
5354	wA1_d2-1_cDAPI	\N	\N
5355	original/PLATE-1/bPLATE_wA1_s2_cRGB.png	\N	GREEN
5356	wA1_d2-1_cGFP	\N	\N
5357	original/PLATE-1/bPLATE_wA1_s2_cRGB.png	\N	RED
5358	wA1_d2-1_cCy5	\N	\N
5359	original/PLATE-1/bPLATE_wH7_s9_cRGB.png	\N	BLUE
5360	wH7_d3-3_cDAPI	\N	\N
5361	original/PLATE-1/bPLATE_wH7_s9_cRGB.png	\N	GREEN
5362	wH7_d3-3_cGFP	\N	\N
5363	original/PLATE-1/bPLATE_wH7_s9_cRGB.png	\N	RED
5364	wH7_d3-3_cCy5	\N	\N
5365	original/PLATE-1/bPLATE_wD3_s9_cRGB.png	\N	BLUE
5366	wD3_d3-3_cDAPI	\N	\N
5367	original/PLATE-1/bPLATE_wD3_s9_cRGB.png	\N	GREEN
5368	wD3_d3-3_cGFP	\N	\N
5369	original/PLATE-1/bPLATE_wD3_s9_cRGB.png	\N	RED
5370	wD3_d3-3_cCy5	\N	\N
5371	original/PLATE-1/bPLATE_wF10_s6_cRGB.png	\N	BLUE
5372	wF10_d3-2_cDAPI	\N	\N
5373	original/PLATE-1/bPLATE_wF10_s6_cRGB.png	\N	GREEN
5374	wF10_d3-2_cGFP	\N	\N
5375	original/PLATE-1/bPLATE_wF10_s6_cRGB.png	\N	RED
5376	wF10_d3-2_cCy5	\N	\N
5377	original/PLATE-1/bPLATE_wA8_s3_cRGB.png	\N	BLUE
5378	wA8_d3-1_cDAPI	\N	\N
5379	original/PLATE-1/bPLATE_wA8_s3_cRGB.png	\N	GREEN
5380	wA8_d3-1_cGFP	\N	\N
5381	original/PLATE-1/bPLATE_wA8_s3_cRGB.png	\N	RED
5382	wA8_d3-1_cCy5	\N	\N
5383	original/PLATE-1/bPLATE_wB3_s1_cRGB.png	\N	BLUE
5384	wB3_d1-1_cDAPI	\N	\N
5385	original/PLATE-1/bPLATE_wB3_s1_cRGB.png	\N	GREEN
5386	wB3_d1-1_cGFP	\N	\N
5387	original/PLATE-1/bPLATE_wB3_s1_cRGB.png	\N	RED
5388	wB3_d1-1_cCy5	\N	\N
5389	original/PLATE-1/bPLATE_wA11_s6_cRGB.png	\N	BLUE
5390	wA11_d3-2_cDAPI	\N	\N
5391	original/PLATE-1/bPLATE_wA11_s6_cRGB.png	\N	GREEN
5392	wA11_d3-2_cGFP	\N	\N
5393	original/PLATE-1/bPLATE_wA11_s6_cRGB.png	\N	RED
5394	wA11_d3-2_cCy5	\N	\N
5395	original/PLATE-1/bPLATE_wD6_s3_cRGB.png	\N	BLUE
5396	wD6_d3-1_cDAPI	\N	\N
5397	original/PLATE-1/bPLATE_wD6_s3_cRGB.png	\N	GREEN
5398	wD6_d3-1_cGFP	\N	\N
5399	original/PLATE-1/bPLATE_wD6_s3_cRGB.png	\N	RED
5400	wD6_d3-1_cCy5	\N	\N
5401	original/PLATE-1/bPLATE_wG9_s7_cRGB.png	\N	BLUE
5402	wG9_d1-3_cDAPI	\N	\N
5403	original/PLATE-1/bPLATE_wG9_s7_cRGB.png	\N	GREEN
5404	wG9_d1-3_cGFP	\N	\N
5405	original/PLATE-1/bPLATE_wG9_s7_cRGB.png	\N	RED
5406	wG9_d1-3_cCy5	\N	\N
5407	original/PLATE-1/bPLATE_wD12_s7_cRGB.png	\N	BLUE
5408	wD12_d1-3_cDAPI	\N	\N
5409	original/PLATE-1/bPLATE_wD12_s7_cRGB.png	\N	GREEN
5410	wD12_d1-3_cGFP	\N	\N
5411	original/PLATE-1/bPLATE_wD12_s7_cRGB.png	\N	RED
5412	wD12_d1-3_cCy5	\N	\N
5413	original/PLATE-1/bPLATE_wG7_s4_cRGB.png	\N	BLUE
5414	wG7_d1-2_cDAPI	\N	\N
5415	original/PLATE-1/bPLATE_wG7_s4_cRGB.png	\N	GREEN
5416	wG7_d1-2_cGFP	\N	\N
5417	original/PLATE-1/bPLATE_wG7_s4_cRGB.png	\N	RED
5418	wG7_d1-2_cCy5	\N	\N
5419	original/PLATE-1/bPLATE_wH2_s2_cRGB.png	\N	BLUE
5420	wH2_d2-1_cDAPI	\N	\N
5421	original/PLATE-1/bPLATE_wH2_s2_cRGB.png	\N	GREEN
5422	wH2_d2-1_cGFP	\N	\N
5423	original/PLATE-1/bPLATE_wH2_s2_cRGB.png	\N	RED
5424	wH2_d2-1_cCy5	\N	\N
5425	original/PLATE-1/bPLATE_wB12_s8_cRGB.png	\N	BLUE
5426	wB12_d2-3_cDAPI	\N	\N
5427	original/PLATE-1/bPLATE_wB12_s8_cRGB.png	\N	GREEN
5428	wB12_d2-3_cGFP	\N	\N
5429	original/PLATE-1/bPLATE_wB12_s8_cRGB.png	\N	RED
5430	wB12_d2-3_cCy5	\N	\N
5431	original/PLATE-1/bPLATE_wE7_s5_cRGB.png	\N	BLUE
5432	wE7_d2-2_cDAPI	\N	\N
5433	original/PLATE-1/bPLATE_wE7_s5_cRGB.png	\N	GREEN
5434	wE7_d2-2_cGFP	\N	\N
5435	original/PLATE-1/bPLATE_wE7_s5_cRGB.png	\N	RED
5436	wE7_d2-2_cCy5	\N	\N
5437	original/PLATE-1/bPLATE_wB7_s1_cRGB.png	\N	BLUE
5438	wB7_d1-1_cDAPI	\N	\N
5439	original/PLATE-1/bPLATE_wB7_s1_cRGB.png	\N	GREEN
5440	wB7_d1-1_cGFP	\N	\N
5441	original/PLATE-1/bPLATE_wB7_s1_cRGB.png	\N	RED
5442	wB7_d1-1_cCy5	\N	\N
5443	original/PLATE-1/bPLATE_wD8_s7_cRGB.png	\N	BLUE
5444	wD8_d1-3_cDAPI	\N	\N
5445	original/PLATE-1/bPLATE_wD8_s7_cRGB.png	\N	GREEN
5446	wD8_d1-3_cGFP	\N	\N
5447	original/PLATE-1/bPLATE_wD8_s7_cRGB.png	\N	RED
5448	wD8_d1-3_cCy5	\N	\N
5449	original/PLATE-1/bPLATE_wG3_s4_cRGB.png	\N	BLUE
5450	wG3_d1-2_cDAPI	\N	\N
5451	original/PLATE-1/bPLATE_wG3_s4_cRGB.png	\N	GREEN
5452	wG3_d1-2_cGFP	\N	\N
5453	original/PLATE-1/bPLATE_wG3_s4_cRGB.png	\N	RED
5454	wG3_d1-2_cCy5	\N	\N
5455	original/PLATE-1/bPLATE_wC7_s1_cRGB.png	\N	BLUE
5456	wC7_d1-1_cDAPI	\N	\N
5457	original/PLATE-1/bPLATE_wC7_s1_cRGB.png	\N	GREEN
5458	wC7_d1-1_cGFP	\N	\N
5459	original/PLATE-1/bPLATE_wC7_s1_cRGB.png	\N	RED
5460	wC7_d1-1_cCy5	\N	\N
5461	original/PLATE-1/bPLATE_wD2_s2_cRGB.png	\N	BLUE
5462	wD2_d2-1_cDAPI	\N	\N
5463	original/PLATE-1/bPLATE_wD2_s2_cRGB.png	\N	GREEN
5464	wD2_d2-1_cGFP	\N	\N
5465	original/PLATE-1/bPLATE_wD2_s2_cRGB.png	\N	RED
5466	wD2_d2-1_cCy5	\N	\N
5467	original/PLATE-1/bPLATE_wA7_s5_cRGB.png	\N	BLUE
5468	wA7_d2-2_cDAPI	\N	\N
5469	original/PLATE-1/bPLATE_wA7_s5_cRGB.png	\N	GREEN
5470	wA7_d2-2_cGFP	\N	\N
5471	original/PLATE-1/bPLATE_wA7_s5_cRGB.png	\N	RED
5472	wA7_d2-2_cCy5	\N	\N
5473	original/PLATE-1/bPLATE_wC7_s6_cRGB.png	\N	BLUE
5474	wC7_d3-2_cDAPI	\N	\N
5475	original/PLATE-1/bPLATE_wC7_s6_cRGB.png	\N	GREEN
5476	wC7_d3-2_cGFP	\N	\N
5477	original/PLATE-1/bPLATE_wC7_s6_cRGB.png	\N	RED
5478	wC7_d3-2_cCy5	\N	\N
5479	original/PLATE-1/bPLATE_wF2_s3_cRGB.png	\N	BLUE
5480	wF2_d3-1_cDAPI	\N	\N
5481	original/PLATE-1/bPLATE_wF2_s3_cRGB.png	\N	GREEN
5482	wF2_d3-1_cGFP	\N	\N
5483	original/PLATE-1/bPLATE_wF2_s3_cRGB.png	\N	RED
5484	wF2_d3-1_cCy5	\N	\N
5485	original/PLATE-1/bPLATE_wH5_s5_cRGB.png	\N	BLUE
5486	wH5_d2-2_cDAPI	\N	\N
5487	original/PLATE-1/bPLATE_wH5_s5_cRGB.png	\N	GREEN
5488	wH5_d2-2_cGFP	\N	\N
5489	original/PLATE-1/bPLATE_wH5_s5_cRGB.png	\N	RED
5490	wH5_d2-2_cCy5	\N	\N
5491	original/PLATE-1/bPLATE_wE10_s8_cRGB.png	\N	BLUE
5492	wE10_d2-3_cDAPI	\N	\N
5493	original/PLATE-1/bPLATE_wE10_s8_cRGB.png	\N	GREEN
5494	wE10_d2-3_cGFP	\N	\N
5495	original/PLATE-1/bPLATE_wE10_s8_cRGB.png	\N	RED
5496	wE10_d2-3_cCy5	\N	\N
5497	original/PLATE-1/bPLATE_wH9_s2_cRGB.png	\N	BLUE
5498	wH9_d2-1_cDAPI	\N	\N
5499	original/PLATE-1/bPLATE_wH9_s2_cRGB.png	\N	GREEN
5500	wH9_d2-1_cGFP	\N	\N
5501	original/PLATE-1/bPLATE_wH9_s2_cRGB.png	\N	RED
5502	wH9_d2-1_cCy5	\N	\N
5503	original/PLATE-1/bPLATE_wD4_s7_cRGB.png	\N	BLUE
5504	wD4_d1-3_cDAPI	\N	\N
5505	original/PLATE-1/bPLATE_wD4_s7_cRGB.png	\N	GREEN
5506	wD4_d1-3_cGFP	\N	\N
5507	original/PLATE-1/bPLATE_wD4_s7_cRGB.png	\N	RED
5508	wD4_d1-3_cCy5	\N	\N
5509	original/PLATE-1/bPLATE_wC7_s8_cRGB.png	\N	BLUE
5510	wC7_d2-3_cDAPI	\N	\N
5511	original/PLATE-1/bPLATE_wC7_s8_cRGB.png	\N	GREEN
5512	wC7_d2-3_cGFP	\N	\N
5513	original/PLATE-1/bPLATE_wC7_s8_cRGB.png	\N	RED
5514	wC7_d2-3_cCy5	\N	\N
5515	original/PLATE-1/bPLATE_wF11_s4_cRGB.png	\N	BLUE
5516	wF11_d1-2_cDAPI	\N	\N
5517	original/PLATE-1/bPLATE_wF11_s4_cRGB.png	\N	GREEN
5518	wF11_d1-2_cGFP	\N	\N
5519	original/PLATE-1/bPLATE_wF11_s4_cRGB.png	\N	RED
5520	wF11_d1-2_cCy5	\N	\N
5521	original/PLATE-1/bPLATE_wF2_s5_cRGB.png	\N	BLUE
5522	wF2_d2-2_cDAPI	\N	\N
5523	original/PLATE-1/bPLATE_wF2_s5_cRGB.png	\N	GREEN
5524	wF2_d2-2_cGFP	\N	\N
5525	original/PLATE-1/bPLATE_wF2_s5_cRGB.png	\N	RED
5526	wF2_d2-2_cCy5	\N	\N
5527	original/PLATE-1/bPLATE_wH5_s8_cRGB.png	\N	BLUE
5528	wH5_d2-3_cDAPI	\N	\N
5529	original/PLATE-1/bPLATE_wH5_s8_cRGB.png	\N	GREEN
5530	wH5_d2-3_cGFP	\N	\N
5531	original/PLATE-1/bPLATE_wH5_s8_cRGB.png	\N	RED
5532	wH5_d2-3_cCy5	\N	\N
5533	original/PLATE-1/bPLATE_wG11_s8_cRGB.png	\N	BLUE
5534	wG11_d2-3_cDAPI	\N	\N
5535	original/PLATE-1/bPLATE_wG11_s8_cRGB.png	\N	GREEN
5536	wG11_d2-3_cGFP	\N	\N
5537	original/PLATE-1/bPLATE_wG11_s8_cRGB.png	\N	RED
5538	wG11_d2-3_cCy5	\N	\N
5539	original/PLATE-1/bPLATE_wG12_s7_cRGB.png	\N	BLUE
5540	wG12_d1-3_cDAPI	\N	\N
5541	original/PLATE-1/bPLATE_wG12_s7_cRGB.png	\N	GREEN
5542	wG12_d1-3_cGFP	\N	\N
5543	original/PLATE-1/bPLATE_wG12_s7_cRGB.png	\N	RED
5544	wG12_d1-3_cCy5	\N	\N
5545	original/PLATE-1/bPLATE_wH6_s2_cRGB.png	\N	BLUE
5546	wH6_d2-1_cDAPI	\N	\N
5547	original/PLATE-1/bPLATE_wH6_s2_cRGB.png	\N	GREEN
5548	wH6_d2-1_cGFP	\N	\N
5549	original/PLATE-1/bPLATE_wH6_s2_cRGB.png	\N	RED
5550	wH6_d2-1_cCy5	\N	\N
5551	original/PLATE-1/bPLATE_wC4_s8_cRGB.png	\N	BLUE
5552	wC4_d2-3_cDAPI	\N	\N
5553	original/PLATE-1/bPLATE_wC4_s8_cRGB.png	\N	GREEN
5554	wC4_d2-3_cGFP	\N	\N
5555	original/PLATE-1/bPLATE_wC4_s8_cRGB.png	\N	RED
5556	wC4_d2-3_cCy5	\N	\N
5557	original/PLATE-1/bPLATE_wE11_s5_cRGB.png	\N	BLUE
5558	wE11_d2-2_cDAPI	\N	\N
5559	original/PLATE-1/bPLATE_wE11_s5_cRGB.png	\N	GREEN
5560	wE11_d2-2_cGFP	\N	\N
5561	original/PLATE-1/bPLATE_wE11_s5_cRGB.png	\N	RED
5562	wE11_d2-2_cCy5	\N	\N
5563	original/PLATE-1/bPLATE_wH7_s4_cRGB.png	\N	BLUE
5564	wH7_d1-2_cDAPI	\N	\N
5565	original/PLATE-1/bPLATE_wH7_s4_cRGB.png	\N	GREEN
5566	wH7_d1-2_cGFP	\N	\N
5567	original/PLATE-1/bPLATE_wH7_s4_cRGB.png	\N	RED
5568	wH7_d1-2_cCy5	\N	\N
5569	original/PLATE-1/bPLATE_wH2_s8_cRGB.png	\N	BLUE
5570	wH2_d2-3_cDAPI	\N	\N
5571	original/PLATE-1/bPLATE_wH2_s8_cRGB.png	\N	GREEN
5572	wH2_d2-3_cGFP	\N	\N
5573	original/PLATE-1/bPLATE_wH2_s8_cRGB.png	\N	RED
5574	wH2_d2-3_cCy5	\N	\N
5575	original/PLATE-1/bPLATE_wE12_s7_cRGB.png	\N	BLUE
5576	wE12_d1-3_cDAPI	\N	\N
5577	original/PLATE-1/bPLATE_wE12_s7_cRGB.png	\N	GREEN
5578	wE12_d1-3_cGFP	\N	\N
5579	original/PLATE-1/bPLATE_wE12_s7_cRGB.png	\N	RED
5580	wE12_d1-3_cCy5	\N	\N
5581	original/PLATE-1/bPLATE_wD10_s2_cRGB.png	\N	BLUE
5582	wD10_d2-1_cDAPI	\N	\N
5583	original/PLATE-1/bPLATE_wD10_s2_cRGB.png	\N	GREEN
5584	wD10_d2-1_cGFP	\N	\N
5585	original/PLATE-1/bPLATE_wD10_s2_cRGB.png	\N	RED
5586	wD10_d2-1_cCy5	\N	\N
5587	original/PLATE-1/bPLATE_wB3_s5_cRGB.png	\N	BLUE
5588	wB3_d2-2_cDAPI	\N	\N
5589	original/PLATE-1/bPLATE_wB3_s5_cRGB.png	\N	GREEN
5590	wB3_d2-2_cGFP	\N	\N
5591	original/PLATE-1/bPLATE_wB3_s5_cRGB.png	\N	RED
5592	wB3_d2-2_cCy5	\N	\N
5593	original/PLATE-1/bPLATE_wH3_s1_cRGB.png	\N	BLUE
5594	wH3_d1-1_cDAPI	\N	\N
5595	original/PLATE-1/bPLATE_wH3_s1_cRGB.png	\N	GREEN
5596	wH3_d1-1_cGFP	\N	\N
5597	original/PLATE-1/bPLATE_wH3_s1_cRGB.png	\N	RED
5598	wH3_d1-1_cCy5	\N	\N
5599	original/PLATE-1/bPLATE_wC1_s7_cRGB.png	\N	BLUE
5600	wC1_d1-3_cDAPI	\N	\N
5601	original/PLATE-1/bPLATE_wC1_s7_cRGB.png	\N	GREEN
5602	wC1_d1-3_cGFP	\N	\N
5603	original/PLATE-1/bPLATE_wC1_s7_cRGB.png	\N	RED
5604	wC1_d1-3_cCy5	\N	\N
5605	original/PLATE-1/bPLATE_wE8_s4_cRGB.png	\N	BLUE
5606	wE8_d1-2_cDAPI	\N	\N
5607	original/PLATE-1/bPLATE_wE8_s4_cRGB.png	\N	GREEN
5608	wE8_d1-2_cGFP	\N	\N
5609	original/PLATE-1/bPLATE_wE8_s4_cRGB.png	\N	RED
5610	wE8_d1-2_cCy5	\N	\N
5611	original/PLATE-1/bPLATE_wD3_s6_cRGB.png	\N	BLUE
5612	wD3_d3-2_cDAPI	\N	\N
5613	original/PLATE-1/bPLATE_wD3_s6_cRGB.png	\N	GREEN
5614	wD3_d3-2_cGFP	\N	\N
5615	original/PLATE-1/bPLATE_wD3_s6_cRGB.png	\N	RED
5616	wD3_d3-2_cCy5	\N	\N
5617	original/PLATE-1/bPLATE_wA8_s9_cRGB.png	\N	BLUE
5618	wA8_d3-3_cDAPI	\N	\N
5619	original/PLATE-1/bPLATE_wA8_s9_cRGB.png	\N	GREEN
5620	wA8_d3-3_cGFP	\N	\N
5621	original/PLATE-1/bPLATE_wA8_s9_cRGB.png	\N	RED
5622	wA8_d3-3_cCy5	\N	\N
5623	original/PLATE-1/bPLATE_wF10_s3_cRGB.png	\N	BLUE
5624	wF10_d3-1_cDAPI	\N	\N
5625	original/PLATE-1/bPLATE_wF10_s3_cRGB.png	\N	GREEN
5626	wF10_d3-1_cGFP	\N	\N
5627	original/PLATE-1/bPLATE_wF10_s3_cRGB.png	\N	RED
5628	wF10_d3-1_cCy5	\N	\N
5629	original/PLATE-1/bPLATE_wE4_s7_cRGB.png	\N	BLUE
5630	wE4_d1-3_cDAPI	\N	\N
5631	original/PLATE-1/bPLATE_wE4_s7_cRGB.png	\N	GREEN
5632	wE4_d1-3_cGFP	\N	\N
5633	original/PLATE-1/bPLATE_wE4_s7_cRGB.png	\N	RED
5634	wE4_d1-3_cCy5	\N	\N
5635	original/PLATE-1/bPLATE_wG11_s4_cRGB.png	\N	BLUE
5636	wG11_d1-2_cDAPI	\N	\N
5637	original/PLATE-1/bPLATE_wG11_s4_cRGB.png	\N	GREEN
5638	wG11_d1-2_cGFP	\N	\N
5639	original/PLATE-1/bPLATE_wG11_s4_cRGB.png	\N	RED
5640	wG11_d1-2_cCy5	\N	\N
5641	original/PLATE-1/bPLATE_wG1_s9_cRGB.png	\N	BLUE
5642	wG1_d3-3_cDAPI	\N	\N
5643	original/PLATE-1/bPLATE_wG1_s9_cRGB.png	\N	GREEN
5644	wG1_d3-3_cGFP	\N	\N
5645	original/PLATE-1/bPLATE_wG1_s9_cRGB.png	\N	RED
5646	wG1_d3-3_cCy5	\N	\N
5647	original/PLATE-1/bPLATE_wB8_s9_cRGB.png	\N	BLUE
5648	wB8_d3-3_cDAPI	\N	\N
5649	original/PLATE-1/bPLATE_wB8_s9_cRGB.png	\N	GREEN
5650	wB8_d3-3_cGFP	\N	\N
5651	original/PLATE-1/bPLATE_wB8_s9_cRGB.png	\N	RED
5652	wB8_d3-3_cCy5	\N	\N
5653	original/PLATE-1/bPLATE_wE3_s6_cRGB.png	\N	BLUE
5654	wE3_d3-2_cDAPI	\N	\N
5655	original/PLATE-1/bPLATE_wE3_s6_cRGB.png	\N	GREEN
5656	wE3_d3-2_cGFP	\N	\N
5657	original/PLATE-1/bPLATE_wE3_s6_cRGB.png	\N	RED
5658	wE3_d3-2_cCy5	\N	\N
5659	original/PLATE-1/bPLATE_wG10_s3_cRGB.png	\N	BLUE
5660	wG10_d3-1_cDAPI	\N	\N
5661	original/PLATE-1/bPLATE_wG10_s3_cRGB.png	\N	GREEN
5662	wG10_d3-1_cGFP	\N	\N
5663	original/PLATE-1/bPLATE_wG10_s3_cRGB.png	\N	RED
5664	wG10_d3-1_cCy5	\N	\N
5665	original/PLATE-1/bPLATE_wA7_s1_cRGB.png	\N	BLUE
5666	wA7_d1-1_cDAPI	\N	\N
5667	original/PLATE-1/bPLATE_wA7_s1_cRGB.png	\N	GREEN
5668	wA7_d1-1_cGFP	\N	\N
5669	original/PLATE-1/bPLATE_wA7_s1_cRGB.png	\N	RED
5670	wA7_d1-1_cCy5	\N	\N
5671	original/PLATE-1/bPLATE_wC10_s1_cRGB.png	\N	BLUE
5672	wC10_d1-1_cDAPI	\N	\N
5673	original/PLATE-1/bPLATE_wC10_s1_cRGB.png	\N	GREEN
5674	wC10_d1-1_cGFP	\N	\N
5675	original/PLATE-1/bPLATE_wC10_s1_cRGB.png	\N	RED
5676	wC10_d1-1_cCy5	\N	\N
5677	original/PLATE-1/bPLATE_wA3_s4_cRGB.png	\N	BLUE
5678	wA3_d1-2_cDAPI	\N	\N
5679	original/PLATE-1/bPLATE_wA3_s4_cRGB.png	\N	GREEN
5680	wA3_d1-2_cGFP	\N	\N
5681	original/PLATE-1/bPLATE_wA3_s4_cRGB.png	\N	RED
5682	wA3_d1-2_cCy5	\N	\N
5683	original/PLATE-1/bPLATE_wB9_s2_cRGB.png	\N	BLUE
5684	wB9_d2-1_cDAPI	\N	\N
5685	original/PLATE-1/bPLATE_wB9_s2_cRGB.png	\N	GREEN
5686	wB9_d2-1_cGFP	\N	\N
5687	original/PLATE-1/bPLATE_wB9_s2_cRGB.png	\N	RED
5688	wB9_d2-1_cCy5	\N	\N
5689	original/PLATE-1/bPLATE_wC4_s4_cRGB.png	\N	BLUE
5690	wC4_d1-2_cDAPI	\N	\N
5691	original/PLATE-1/bPLATE_wC4_s4_cRGB.png	\N	GREEN
5692	wC4_d1-2_cGFP	\N	\N
5693	original/PLATE-1/bPLATE_wC4_s4_cRGB.png	\N	RED
5694	wC4_d1-2_cCy5	\N	\N
5695	original/PLATE-1/bPLATE_wE11_s1_cRGB.png	\N	BLUE
5696	wE11_d1-1_cDAPI	\N	\N
5697	original/PLATE-1/bPLATE_wE11_s1_cRGB.png	\N	GREEN
5698	wE11_d1-1_cGFP	\N	\N
5699	original/PLATE-1/bPLATE_wE11_s1_cRGB.png	\N	RED
5700	wE11_d1-1_cCy5	\N	\N
5701	original/PLATE-1/bPLATE_wG7_s9_cRGB.png	\N	BLUE
5702	wG7_d3-3_cDAPI	\N	\N
5703	original/PLATE-1/bPLATE_wG7_s9_cRGB.png	\N	GREEN
5704	wG7_d3-3_cGFP	\N	\N
5705	original/PLATE-1/bPLATE_wG7_s9_cRGB.png	\N	RED
5706	wG7_d3-3_cCy5	\N	\N
5707	original/PLATE-1/bPLATE_wA10_s7_cRGB.png	\N	BLUE
5708	wA10_d1-3_cDAPI	\N	\N
5709	original/PLATE-1/bPLATE_wA10_s7_cRGB.png	\N	GREEN
5710	wA10_d1-3_cGFP	\N	\N
5711	original/PLATE-1/bPLATE_wA10_s7_cRGB.png	\N	RED
5712	wA10_d1-3_cCy5	\N	\N
5713	original/PLATE-1/bPLATE_wD5_s4_cRGB.png	\N	BLUE
5714	wD5_d1-2_cDAPI	\N	\N
5715	original/PLATE-1/bPLATE_wD5_s4_cRGB.png	\N	GREEN
5716	wD5_d1-2_cGFP	\N	\N
5717	original/PLATE-1/bPLATE_wD5_s4_cRGB.png	\N	RED
5718	wD5_d1-2_cCy5	\N	\N
5719	original/PLATE-1/bPLATE_wF12_s1_cRGB.png	\N	BLUE
5720	wF12_d1-1_cDAPI	\N	\N
5721	original/PLATE-1/bPLATE_wF12_s1_cRGB.png	\N	GREEN
5722	wF12_d1-1_cGFP	\N	\N
5723	original/PLATE-1/bPLATE_wF12_s1_cRGB.png	\N	RED
5724	wF12_d1-1_cCy5	\N	\N
5725	original/PLATE-1/bPLATE_wH6_s1_cRGB.png	\N	BLUE
5726	wH6_d1-1_cDAPI	\N	\N
5727	original/PLATE-1/bPLATE_wH6_s1_cRGB.png	\N	GREEN
5728	wH6_d1-1_cGFP	\N	\N
5729	original/PLATE-1/bPLATE_wH6_s1_cRGB.png	\N	RED
5730	wH6_d1-1_cCy5	\N	\N
5731	original/PLATE-1/bPLATE_wC4_s7_cRGB.png	\N	BLUE
5732	wC4_d1-3_cDAPI	\N	\N
5733	original/PLATE-1/bPLATE_wC4_s7_cRGB.png	\N	GREEN
5734	wC4_d1-3_cGFP	\N	\N
5735	original/PLATE-1/bPLATE_wC4_s7_cRGB.png	\N	RED
5736	wC4_d1-3_cCy5	\N	\N
5737	original/PLATE-1/bPLATE_wB11_s3_cRGB.png	\N	BLUE
5738	wB11_d3-1_cDAPI	\N	\N
5739	original/PLATE-1/bPLATE_wB11_s3_cRGB.png	\N	GREEN
5740	wB11_d3-1_cGFP	\N	\N
5741	original/PLATE-1/bPLATE_wB11_s3_cRGB.png	\N	RED
5742	wB11_d3-1_cCy5	\N	\N
5743	original/PLATE-1/bPLATE_wE11_s4_cRGB.png	\N	BLUE
5744	wE11_d1-2_cDAPI	\N	\N
5745	original/PLATE-1/bPLATE_wE11_s4_cRGB.png	\N	GREEN
5746	wE11_d1-2_cGFP	\N	\N
5747	original/PLATE-1/bPLATE_wE11_s4_cRGB.png	\N	RED
5748	wE11_d1-2_cCy5	\N	\N
5749	original/PLATE-1/bPLATE_wF6_s8_cRGB.png	\N	BLUE
5750	wF6_d2-3_cDAPI	\N	\N
5751	original/PLATE-1/bPLATE_wF6_s8_cRGB.png	\N	GREEN
5752	wF6_d2-3_cGFP	\N	\N
5753	original/PLATE-1/bPLATE_wF6_s8_cRGB.png	\N	RED
5754	wF6_d2-3_cCy5	\N	\N
5755	original/PLATE-1/bPLATE_wD11_s6_cRGB.png	\N	BLUE
5756	wD11_d3-2_cDAPI	\N	\N
5757	original/PLATE-1/bPLATE_wD11_s6_cRGB.png	\N	GREEN
5758	wD11_d3-2_cGFP	\N	\N
5759	original/PLATE-1/bPLATE_wD11_s6_cRGB.png	\N	RED
5760	wD11_d3-2_cCy5	\N	\N
5761	original/PLATE-1/bPLATE_wB4_s9_cRGB.png	\N	BLUE
5762	wB4_d3-3_cDAPI	\N	\N
5763	original/PLATE-1/bPLATE_wB4_s9_cRGB.png	\N	GREEN
5764	wB4_d3-3_cGFP	\N	\N
5765	original/PLATE-1/bPLATE_wB4_s9_cRGB.png	\N	RED
5766	wB4_d3-3_cCy5	\N	\N
5767	original/PLATE-1/bPLATE_wG6_s3_cRGB.png	\N	BLUE
5768	wG6_d3-1_cDAPI	\N	\N
5769	original/PLATE-1/bPLATE_wG6_s3_cRGB.png	\N	GREEN
5770	wG6_d3-1_cGFP	\N	\N
5771	original/PLATE-1/bPLATE_wG6_s3_cRGB.png	\N	RED
5772	wG6_d3-1_cCy5	\N	\N
5773	original/PLATE-1/bPLATE_wB5_s2_cRGB.png	\N	BLUE
5774	wB5_d2-1_cDAPI	\N	\N
5775	original/PLATE-1/bPLATE_wB5_s2_cRGB.png	\N	GREEN
5776	wB5_d2-1_cGFP	\N	\N
5777	original/PLATE-1/bPLATE_wB5_s2_cRGB.png	\N	RED
5778	wB5_d2-1_cCy5	\N	\N
5779	original/PLATE-1/bPLATE_wG5_s9_cRGB.png	\N	BLUE
5780	wG5_d3-3_cDAPI	\N	\N
5781	original/PLATE-1/bPLATE_wG5_s9_cRGB.png	\N	GREEN
5782	wG5_d3-3_cGFP	\N	\N
5783	original/PLATE-1/bPLATE_wG5_s9_cRGB.png	\N	RED
5784	wG5_d3-3_cCy5	\N	\N
5785	original/PLATE-1/bPLATE_wH5_s6_cRGB.png	\N	BLUE
5786	wH5_d3-2_cDAPI	\N	\N
5787	original/PLATE-1/bPLATE_wH5_s6_cRGB.png	\N	GREEN
5788	wH5_d3-2_cGFP	\N	\N
5789	original/PLATE-1/bPLATE_wH5_s6_cRGB.png	\N	RED
5790	wH5_d3-2_cCy5	\N	\N
5791	original/PLATE-1/bPLATE_wE10_s9_cRGB.png	\N	BLUE
5792	wE10_d3-3_cDAPI	\N	\N
5793	original/PLATE-1/bPLATE_wE10_s9_cRGB.png	\N	GREEN
5794	wE10_d3-3_cGFP	\N	\N
5795	original/PLATE-1/bPLATE_wE10_s9_cRGB.png	\N	RED
5796	wE10_d3-3_cCy5	\N	\N
5797	original/PLATE-1/bPLATE_wC8_s7_cRGB.png	\N	BLUE
5798	wC8_d1-3_cDAPI	\N	\N
5799	original/PLATE-1/bPLATE_wC8_s7_cRGB.png	\N	GREEN
5800	wC8_d1-3_cGFP	\N	\N
5801	original/PLATE-1/bPLATE_wC8_s7_cRGB.png	\N	RED
5802	wC8_d1-3_cCy5	\N	\N
5803	original/PLATE-1/bPLATE_wF3_s4_cRGB.png	\N	BLUE
5804	wF3_d1-2_cDAPI	\N	\N
5805	original/PLATE-1/bPLATE_wF3_s4_cRGB.png	\N	GREEN
5806	wF3_d1-2_cGFP	\N	\N
5807	original/PLATE-1/bPLATE_wF3_s4_cRGB.png	\N	RED
5808	wF3_d1-2_cCy5	\N	\N
5809	original/PLATE-1/bPLATE_wH10_s1_cRGB.png	\N	BLUE
5810	wH10_d1-1_cDAPI	\N	\N
5811	original/PLATE-1/bPLATE_wH10_s1_cRGB.png	\N	GREEN
5812	wH10_d1-1_cGFP	\N	\N
5813	original/PLATE-1/bPLATE_wH10_s1_cRGB.png	\N	RED
5814	wH10_d1-1_cCy5	\N	\N
5815	original/PLATE-1/bPLATE_wD1_s2_cRGB.png	\N	BLUE
5816	wD1_d2-1_cDAPI	\N	\N
5817	original/PLATE-1/bPLATE_wD1_s2_cRGB.png	\N	GREEN
5818	wD1_d2-1_cGFP	\N	\N
5819	original/PLATE-1/bPLATE_wD1_s2_cRGB.png	\N	RED
5820	wD1_d2-1_cCy5	\N	\N
5821	original/PLATE-1/bPLATE_wA6_s5_cRGB.png	\N	BLUE
5822	wA6_d2-2_cDAPI	\N	\N
5823	original/PLATE-1/bPLATE_wA6_s5_cRGB.png	\N	GREEN
5824	wA6_d2-2_cGFP	\N	\N
5825	original/PLATE-1/bPLATE_wA6_s5_cRGB.png	\N	RED
5826	wA6_d2-2_cCy5	\N	\N
5827	original/PLATE-1/bPLATE_wH2_s3_cRGB.png	\N	BLUE
5828	wH2_d3-1_cDAPI	\N	\N
5829	original/PLATE-1/bPLATE_wH2_s3_cRGB.png	\N	GREEN
5830	wH2_d3-1_cGFP	\N	\N
5831	original/PLATE-1/bPLATE_wH2_s3_cRGB.png	\N	RED
5832	wH2_d3-1_cCy5	\N	\N
5833	original/PLATE-1/bPLATE_wB12_s9_cRGB.png	\N	BLUE
5834	wB12_d3-3_cDAPI	\N	\N
5835	original/PLATE-1/bPLATE_wB12_s9_cRGB.png	\N	GREEN
5836	wB12_d3-3_cGFP	\N	\N
5837	original/PLATE-1/bPLATE_wB12_s9_cRGB.png	\N	RED
5838	wB12_d3-3_cCy5	\N	\N
5839	original/PLATE-1/bPLATE_wE7_s6_cRGB.png	\N	BLUE
5840	wE7_d3-2_cDAPI	\N	\N
5841	original/PLATE-1/bPLATE_wE7_s6_cRGB.png	\N	GREEN
5842	wE7_d3-2_cGFP	\N	\N
5843	original/PLATE-1/bPLATE_wE7_s6_cRGB.png	\N	RED
5844	wE7_d3-2_cCy5	\N	\N
5845	original/PLATE-1/bPLATE_wF11_s9_cRGB.png	\N	BLUE
5846	wF11_d3-3_cDAPI	\N	\N
5847	original/PLATE-1/bPLATE_wF11_s9_cRGB.png	\N	GREEN
5848	wF11_d3-3_cGFP	\N	\N
5849	original/PLATE-1/bPLATE_wF11_s9_cRGB.png	\N	RED
5850	wF11_d3-3_cCy5	\N	\N
5851	original/PLATE-1/bPLATE_wH12_s1_cRGB.png	\N	BLUE
5852	wH12_d1-1_cDAPI	\N	\N
5853	original/PLATE-1/bPLATE_wH12_s1_cRGB.png	\N	GREEN
5854	wH12_d1-1_cGFP	\N	\N
5855	original/PLATE-1/bPLATE_wH12_s1_cRGB.png	\N	RED
5856	wH12_d1-1_cCy5	\N	\N
5857	original/PLATE-1/bPLATE_wC10_s7_cRGB.png	\N	BLUE
5858	wC10_d1-3_cDAPI	\N	\N
5859	original/PLATE-1/bPLATE_wC10_s7_cRGB.png	\N	GREEN
5860	wC10_d1-3_cGFP	\N	\N
5861	original/PLATE-1/bPLATE_wC10_s7_cRGB.png	\N	RED
5862	wC10_d1-3_cCy5	\N	\N
5863	original/PLATE-1/bPLATE_wF5_s4_cRGB.png	\N	BLUE
5864	wF5_d1-2_cDAPI	\N	\N
5865	original/PLATE-1/bPLATE_wF5_s4_cRGB.png	\N	GREEN
5866	wF5_d1-2_cGFP	\N	\N
5867	original/PLATE-1/bPLATE_wF5_s4_cRGB.png	\N	RED
5868	wF5_d1-2_cCy5	\N	\N
5869	original/PLATE-1/bPLATE_wD1_s8_cRGB.png	\N	BLUE
5870	wD1_d2-3_cDAPI	\N	\N
5871	original/PLATE-1/bPLATE_wD1_s8_cRGB.png	\N	GREEN
5872	wD1_d2-3_cGFP	\N	\N
5873	original/PLATE-1/bPLATE_wD1_s8_cRGB.png	\N	RED
5874	wD1_d2-3_cCy5	\N	\N
5875	original/PLATE-1/bPLATE_wF8_s5_cRGB.png	\N	BLUE
5876	wF8_d2-2_cDAPI	\N	\N
5877	original/PLATE-1/bPLATE_wF8_s5_cRGB.png	\N	GREEN
5878	wF8_d2-2_cGFP	\N	\N
5879	original/PLATE-1/bPLATE_wF8_s5_cRGB.png	\N	RED
5880	wF8_d2-2_cCy5	\N	\N
5881	original/PLATE-1/bPLATE_wB8_s7_cRGB.png	\N	BLUE
5882	wB8_d1-3_cDAPI	\N	\N
5883	original/PLATE-1/bPLATE_wB8_s7_cRGB.png	\N	GREEN
5884	wB8_d1-3_cGFP	\N	\N
5885	original/PLATE-1/bPLATE_wB8_s7_cRGB.png	\N	RED
5886	wB8_d1-3_cCy5	\N	\N
5887	original/PLATE-1/bPLATE_wE3_s4_cRGB.png	\N	BLUE
5888	wE3_d1-2_cDAPI	\N	\N
5889	original/PLATE-1/bPLATE_wE3_s4_cRGB.png	\N	GREEN
5890	wE3_d1-2_cGFP	\N	\N
5891	original/PLATE-1/bPLATE_wE3_s4_cRGB.png	\N	RED
5892	wE3_d1-2_cCy5	\N	\N
5893	original/PLATE-1/bPLATE_wG10_s1_cRGB.png	\N	BLUE
5894	wG10_d1-1_cDAPI	\N	\N
5895	original/PLATE-1/bPLATE_wG10_s1_cRGB.png	\N	GREEN
5896	wG10_d1-1_cGFP	\N	\N
5897	original/PLATE-1/bPLATE_wG10_s1_cRGB.png	\N	RED
5898	wG10_d1-1_cCy5	\N	\N
5899	original/PLATE-1/bPLATE_wD11_s5_cRGB.png	\N	BLUE
5900	wD11_d2-2_cDAPI	\N	\N
5901	original/PLATE-1/bPLATE_wD11_s5_cRGB.png	\N	GREEN
5902	wD11_d2-2_cGFP	\N	\N
5903	original/PLATE-1/bPLATE_wD11_s5_cRGB.png	\N	RED
5904	wD11_d2-2_cCy5	\N	\N
5905	original/PLATE-1/bPLATE_wB4_s8_cRGB.png	\N	BLUE
5906	wB4_d2-3_cDAPI	\N	\N
5907	original/PLATE-1/bPLATE_wB4_s8_cRGB.png	\N	GREEN
5908	wB4_d2-3_cGFP	\N	\N
5909	original/PLATE-1/bPLATE_wB4_s8_cRGB.png	\N	RED
5910	wB4_d2-3_cCy5	\N	\N
5911	original/PLATE-1/bPLATE_wG6_s2_cRGB.png	\N	BLUE
5912	wG6_d2-1_cDAPI	\N	\N
5913	original/PLATE-1/bPLATE_wG6_s2_cRGB.png	\N	GREEN
5914	wG6_d2-1_cGFP	\N	\N
5915	original/PLATE-1/bPLATE_wG6_s2_cRGB.png	\N	RED
5916	wG6_d2-1_cCy5	\N	\N
5917	original/PLATE-1/bPLATE_wG6_s7_cRGB.png	\N	BLUE
5918	wG6_d1-3_cDAPI	\N	\N
5919	original/PLATE-1/bPLATE_wG6_s7_cRGB.png	\N	GREEN
5920	wG6_d1-3_cGFP	\N	\N
5921	original/PLATE-1/bPLATE_wG6_s7_cRGB.png	\N	RED
5922	wG6_d1-3_cCy5	\N	\N
5923	original/PLATE-1/bPLATE_wH1_s3_cRGB.png	\N	BLUE
5924	wH1_d3-1_cDAPI	\N	\N
5925	original/PLATE-1/bPLATE_wH1_s3_cRGB.png	\N	GREEN
5926	wH1_d3-1_cGFP	\N	\N
5927	original/PLATE-1/bPLATE_wH1_s3_cRGB.png	\N	RED
5928	wH1_d3-1_cCy5	\N	\N
5929	original/PLATE-1/bPLATE_wB11_s9_cRGB.png	\N	BLUE
5930	wB11_d3-3_cDAPI	\N	\N
5931	original/PLATE-1/bPLATE_wB11_s9_cRGB.png	\N	GREEN
5932	wB11_d3-3_cGFP	\N	\N
5933	original/PLATE-1/bPLATE_wB11_s9_cRGB.png	\N	RED
5934	wB11_d3-3_cCy5	\N	\N
5935	original/PLATE-1/bPLATE_wE6_s6_cRGB.png	\N	BLUE
5936	wE6_d3-2_cDAPI	\N	\N
5937	original/PLATE-1/bPLATE_wE6_s6_cRGB.png	\N	GREEN
5938	wE6_d3-2_cGFP	\N	\N
5939	original/PLATE-1/bPLATE_wE6_s6_cRGB.png	\N	RED
5940	wE6_d3-2_cCy5	\N	\N
5941	original/PLATE-1/bPLATE_wH4_s9_cRGB.png	\N	BLUE
5942	wH4_d3-3_cDAPI	\N	\N
5943	original/PLATE-1/bPLATE_wH4_s9_cRGB.png	\N	GREEN
5944	wH4_d3-3_cGFP	\N	\N
5945	original/PLATE-1/bPLATE_wH4_s9_cRGB.png	\N	RED
5946	wH4_d3-3_cCy5	\N	\N
5947	original/PLATE-1/bPLATE_wC5_s3_cRGB.png	\N	BLUE
5948	wC5_d3-1_cDAPI	\N	\N
5949	original/PLATE-1/bPLATE_wC5_s3_cRGB.png	\N	GREEN
5950	wC5_d3-1_cGFP	\N	\N
5951	original/PLATE-1/bPLATE_wC5_s3_cRGB.png	\N	RED
5952	wC5_d3-1_cCy5	\N	\N
5953	original/PLATE-1/bPLATE_wD2_s6_cRGB.png	\N	BLUE
5954	wD2_d3-2_cDAPI	\N	\N
5955	original/PLATE-1/bPLATE_wD2_s6_cRGB.png	\N	GREEN
5956	wD2_d3-2_cGFP	\N	\N
5957	original/PLATE-1/bPLATE_wD2_s6_cRGB.png	\N	RED
5958	wD2_d3-2_cCy5	\N	\N
5959	original/PLATE-1/bPLATE_wA7_s9_cRGB.png	\N	BLUE
5960	wA7_d3-3_cDAPI	\N	\N
5961	original/PLATE-1/bPLATE_wA7_s9_cRGB.png	\N	GREEN
5962	wA7_d3-3_cGFP	\N	\N
5963	original/PLATE-1/bPLATE_wA7_s9_cRGB.png	\N	RED
5964	wA7_d3-3_cCy5	\N	\N
5965	original/PLATE-1/bPLATE_wF9_s3_cRGB.png	\N	BLUE
5966	wF9_d3-1_cDAPI	\N	\N
5967	original/PLATE-1/bPLATE_wF9_s3_cRGB.png	\N	GREEN
5968	wF9_d3-1_cGFP	\N	\N
5969	original/PLATE-1/bPLATE_wF9_s3_cRGB.png	\N	RED
5970	wF9_d3-1_cCy5	\N	\N
5971	original/PLATE-1/bPLATE_wC9_s8_cRGB.png	\N	BLUE
5972	wC9_d2-3_cDAPI	\N	\N
5973	original/PLATE-1/bPLATE_wC9_s8_cRGB.png	\N	GREEN
5974	wC9_d2-3_cGFP	\N	\N
5975	original/PLATE-1/bPLATE_wC9_s8_cRGB.png	\N	RED
5976	wC9_d2-3_cCy5	\N	\N
5977	original/PLATE-1/bPLATE_wF4_s5_cRGB.png	\N	BLUE
5978	wF4_d2-2_cDAPI	\N	\N
5979	original/PLATE-1/bPLATE_wF4_s5_cRGB.png	\N	GREEN
5980	wF4_d2-2_cGFP	\N	\N
5981	original/PLATE-1/bPLATE_wF4_s5_cRGB.png	\N	RED
5982	wF4_d2-2_cCy5	\N	\N
5983	original/PLATE-1/bPLATE_wH11_s2_cRGB.png	\N	BLUE
5984	wH11_d2-1_cDAPI	\N	\N
5985	original/PLATE-1/bPLATE_wH11_s2_cRGB.png	\N	GREEN
5986	wH11_d2-1_cGFP	\N	\N
5987	original/PLATE-1/bPLATE_wH11_s2_cRGB.png	\N	RED
5988	wH11_d2-1_cCy5	\N	\N
5989	original/PLATE-1/bPLATE_wH4_s5_cRGB.png	\N	BLUE
5990	wH4_d2-2_cDAPI	\N	\N
5991	original/PLATE-1/bPLATE_wH4_s5_cRGB.png	\N	GREEN
5992	wH4_d2-2_cGFP	\N	\N
5993	original/PLATE-1/bPLATE_wH4_s5_cRGB.png	\N	RED
5994	wH4_d2-2_cCy5	\N	\N
5995	original/PLATE-1/bPLATE_wE9_s8_cRGB.png	\N	BLUE
5996	wE9_d2-3_cDAPI	\N	\N
5997	original/PLATE-1/bPLATE_wE9_s8_cRGB.png	\N	GREEN
5998	wE9_d2-3_cGFP	\N	\N
5999	original/PLATE-1/bPLATE_wE9_s8_cRGB.png	\N	RED
6000	wE9_d2-3_cCy5	\N	\N
6001	original/PLATE-1/bPLATE_wB7_s8_cRGB.png	\N	BLUE
6002	wB7_d2-3_cDAPI	\N	\N
6003	original/PLATE-1/bPLATE_wB7_s8_cRGB.png	\N	GREEN
6004	wB7_d2-3_cGFP	\N	\N
6005	original/PLATE-1/bPLATE_wB7_s8_cRGB.png	\N	RED
6006	wB7_d2-3_cCy5	\N	\N
6007	original/PLATE-1/bPLATE_wE2_s5_cRGB.png	\N	BLUE
6008	wE2_d2-2_cDAPI	\N	\N
6009	original/PLATE-1/bPLATE_wE2_s5_cRGB.png	\N	GREEN
6010	wE2_d2-2_cGFP	\N	\N
6011	original/PLATE-1/bPLATE_wE2_s5_cRGB.png	\N	RED
6012	wE2_d2-2_cCy5	\N	\N
6013	original/PLATE-1/bPLATE_wG9_s2_cRGB.png	\N	BLUE
6014	wG9_d2-1_cDAPI	\N	\N
6015	original/PLATE-1/bPLATE_wG9_s2_cRGB.png	\N	GREEN
6016	wG9_d2-1_cGFP	\N	\N
6017	original/PLATE-1/bPLATE_wG9_s2_cRGB.png	\N	RED
6018	wG9_d2-1_cCy5	\N	\N
6019	original/PLATE-1/bPLATE_wC2_s2_cRGB.png	\N	BLUE
6020	wC2_d2-1_cDAPI	\N	\N
6021	original/PLATE-1/bPLATE_wC2_s2_cRGB.png	\N	GREEN
6022	wC2_d2-1_cGFP	\N	\N
6023	original/PLATE-1/bPLATE_wC2_s2_cRGB.png	\N	RED
6024	wC2_d2-1_cCy5	\N	\N
6025	original/PLATE-1/bPLATE_wD7_s9_cRGB.png	\N	BLUE
6026	wD7_d3-3_cDAPI	\N	\N
6027	original/PLATE-1/bPLATE_wD7_s9_cRGB.png	\N	GREEN
6028	wD7_d3-3_cGFP	\N	\N
6029	original/PLATE-1/bPLATE_wD7_s9_cRGB.png	\N	RED
6030	wD7_d3-3_cCy5	\N	\N
6031	original/PLATE-1/bPLATE_wG2_s6_cRGB.png	\N	BLUE
6032	wG2_d3-2_cDAPI	\N	\N
6033	original/PLATE-1/bPLATE_wG2_s6_cRGB.png	\N	GREEN
6034	wG2_d3-2_cGFP	\N	\N
6035	original/PLATE-1/bPLATE_wG2_s6_cRGB.png	\N	RED
6036	wG2_d3-2_cCy5	\N	\N
6037	original/PLATE-1/bPLATE_wD1_s9_cRGB.png	\N	BLUE
6038	wD1_d3-3_cDAPI	\N	\N
6039	original/PLATE-1/bPLATE_wD1_s9_cRGB.png	\N	GREEN
6040	wD1_d3-3_cGFP	\N	\N
6041	original/PLATE-1/bPLATE_wD1_s9_cRGB.png	\N	RED
6042	wD1_d3-3_cCy5	\N	\N
6043	original/PLATE-1/bPLATE_wF8_s6_cRGB.png	\N	BLUE
6044	wF8_d3-2_cDAPI	\N	\N
6045	original/PLATE-1/bPLATE_wF8_s6_cRGB.png	\N	GREEN
6046	wF8_d3-2_cGFP	\N	\N
6047	original/PLATE-1/bPLATE_wF8_s6_cRGB.png	\N	RED
6048	wF8_d3-2_cCy5	\N	\N
6049	original/PLATE-1/bPLATE_wF9_s7_cRGB.png	\N	BLUE
6050	wF9_d1-3_cDAPI	\N	\N
6051	original/PLATE-1/bPLATE_wF9_s7_cRGB.png	\N	GREEN
6052	wF9_d1-3_cGFP	\N	\N
6053	original/PLATE-1/bPLATE_wF9_s7_cRGB.png	\N	RED
6054	wF9_d1-3_cCy5	\N	\N
6055	original/PLATE-1/bPLATE_wA6_s3_cRGB.png	\N	BLUE
6056	wA6_d3-1_cDAPI	\N	\N
6057	original/PLATE-1/bPLATE_wA6_s3_cRGB.png	\N	GREEN
6058	wA6_d3-1_cGFP	\N	\N
6059	original/PLATE-1/bPLATE_wA6_s3_cRGB.png	\N	RED
6060	wA6_d3-1_cCy5	\N	\N
6061	original/PLATE-1/bPLATE_wE1_s8_cRGB.png	\N	BLUE
6062	wE1_d2-3_cDAPI	\N	\N
6063	original/PLATE-1/bPLATE_wE1_s8_cRGB.png	\N	GREEN
6064	wE1_d2-3_cGFP	\N	\N
6065	original/PLATE-1/bPLATE_wE1_s8_cRGB.png	\N	RED
6066	wE1_d2-3_cCy5	\N	\N
6067	original/PLATE-1/bPLATE_wG8_s5_cRGB.png	\N	BLUE
6068	wG8_d2-2_cDAPI	\N	\N
6069	original/PLATE-1/bPLATE_wG8_s5_cRGB.png	\N	GREEN
6070	wG8_d2-2_cGFP	\N	\N
6071	original/PLATE-1/bPLATE_wG8_s5_cRGB.png	\N	RED
6072	wG8_d2-2_cCy5	\N	\N
6073	original/PLATE-1/bPLATE_wC8_s4_cRGB.png	\N	BLUE
6074	wC8_d1-2_cDAPI	\N	\N
6075	original/PLATE-1/bPLATE_wC8_s4_cRGB.png	\N	GREEN
6076	wC8_d1-2_cGFP	\N	\N
6077	original/PLATE-1/bPLATE_wC8_s4_cRGB.png	\N	RED
6078	wC8_d1-2_cCy5	\N	\N
6079	original/PLATE-1/bPLATE_wA1_s7_cRGB.png	\N	BLUE
6080	wA1_d1-3_cDAPI	\N	\N
6081	original/PLATE-1/bPLATE_wA1_s7_cRGB.png	\N	GREEN
6082	wA1_d1-3_cGFP	\N	\N
6083	original/PLATE-1/bPLATE_wA1_s7_cRGB.png	\N	RED
6084	wA1_d1-3_cCy5	\N	\N
6085	original/PLATE-1/bPLATE_wF3_s1_cRGB.png	\N	BLUE
6086	wF3_d1-1_cDAPI	\N	\N
6087	original/PLATE-1/bPLATE_wF3_s1_cRGB.png	\N	GREEN
6088	wF3_d1-1_cGFP	\N	\N
6089	original/PLATE-1/bPLATE_wF3_s1_cRGB.png	\N	RED
6090	wF3_d1-1_cCy5	\N	\N
6091	original/PLATE-1/bPLATE_wD6_s8_cRGB.png	\N	BLUE
6092	wD6_d2-3_cDAPI	\N	\N
6093	original/PLATE-1/bPLATE_wD6_s8_cRGB.png	\N	GREEN
6094	wD6_d2-3_cGFP	\N	\N
6095	original/PLATE-1/bPLATE_wD6_s8_cRGB.png	\N	RED
6096	wD6_d2-3_cCy5	\N	\N
6097	original/PLATE-1/bPLATE_wG1_s5_cRGB.png	\N	BLUE
6098	wG1_d2-2_cDAPI	\N	\N
6099	original/PLATE-1/bPLATE_wG1_s5_cRGB.png	\N	GREEN
6100	wG1_d2-2_cGFP	\N	\N
6101	original/PLATE-1/bPLATE_wG1_s5_cRGB.png	\N	RED
6102	wG1_d2-2_cCy5	\N	\N
6103	original/PLATE-1/bPLATE_wH3_s2_cRGB.png	\N	BLUE
6104	wH3_d2-1_cDAPI	\N	\N
6105	original/PLATE-1/bPLATE_wH3_s2_cRGB.png	\N	GREEN
6106	wH3_d2-1_cGFP	\N	\N
6107	original/PLATE-1/bPLATE_wH3_s2_cRGB.png	\N	RED
6108	wH3_d2-1_cCy5	\N	\N
6109	original/PLATE-1/bPLATE_wC1_s8_cRGB.png	\N	BLUE
6110	wC1_d2-3_cDAPI	\N	\N
6111	original/PLATE-1/bPLATE_wC1_s8_cRGB.png	\N	GREEN
6112	wC1_d2-3_cGFP	\N	\N
6113	original/PLATE-1/bPLATE_wC1_s8_cRGB.png	\N	RED
6114	wC1_d2-3_cCy5	\N	\N
6115	original/PLATE-1/bPLATE_wE8_s5_cRGB.png	\N	BLUE
6116	wE8_d2-2_cDAPI	\N	\N
6117	original/PLATE-1/bPLATE_wE8_s5_cRGB.png	\N	GREEN
6118	wE8_d2-2_cGFP	\N	\N
6119	original/PLATE-1/bPLATE_wE8_s5_cRGB.png	\N	RED
6120	wE8_d2-2_cCy5	\N	\N
6121	original/PLATE-1/bPLATE_wA5_s2_cRGB.png	\N	BLUE
6122	wA5_d2-1_cDAPI	\N	\N
6123	original/PLATE-1/bPLATE_wA5_s2_cRGB.png	\N	GREEN
6124	wA5_d2-1_cGFP	\N	\N
6125	original/PLATE-1/bPLATE_wA5_s2_cRGB.png	\N	RED
6126	wA5_d2-1_cCy5	\N	\N
6127	original/PLATE-1/bPLATE_wH11_s7_cRGB.png	\N	BLUE
6128	wH11_d1-3_cDAPI	\N	\N
6129	original/PLATE-1/bPLATE_wH11_s7_cRGB.png	\N	GREEN
6130	wH11_d1-3_cGFP	\N	\N
6131	original/PLATE-1/bPLATE_wH11_s7_cRGB.png	\N	RED
6132	wH11_d1-3_cCy5	\N	\N
6133	original/PLATE-1/bPLATE_wE3_s8_cRGB.png	\N	BLUE
6134	wE3_d2-3_cDAPI	\N	\N
6135	original/PLATE-1/bPLATE_wE3_s8_cRGB.png	\N	GREEN
6136	wE3_d2-3_cGFP	\N	\N
6137	original/PLATE-1/bPLATE_wE3_s8_cRGB.png	\N	RED
6138	wE3_d2-3_cCy5	\N	\N
6139	original/PLATE-1/bPLATE_wG10_s5_cRGB.png	\N	BLUE
6140	wG10_d2-2_cDAPI	\N	\N
6141	original/PLATE-1/bPLATE_wG10_s5_cRGB.png	\N	GREEN
6142	wG10_d2-2_cGFP	\N	\N
6143	original/PLATE-1/bPLATE_wG10_s5_cRGB.png	\N	RED
6144	wG10_d2-2_cCy5	\N	\N
6145	original/PLATE-1/bPLATE_wC10_s2_cRGB.png	\N	BLUE
6146	wC10_d2-1_cDAPI	\N	\N
6147	original/PLATE-1/bPLATE_wC10_s2_cRGB.png	\N	GREEN
6148	wC10_d2-1_cGFP	\N	\N
6149	original/PLATE-1/bPLATE_wC10_s2_cRGB.png	\N	RED
6150	wC10_d2-1_cCy5	\N	\N
6151	original/PLATE-1/bPLATE_wA3_s5_cRGB.png	\N	BLUE
6152	wA3_d2-2_cDAPI	\N	\N
6153	original/PLATE-1/bPLATE_wA3_s5_cRGB.png	\N	GREEN
6154	wA3_d2-2_cGFP	\N	\N
6155	original/PLATE-1/bPLATE_wA3_s5_cRGB.png	\N	RED
6156	wA3_d2-2_cCy5	\N	\N
6157	original/PLATE-1/bPLATE_wF12_s7_cRGB.png	\N	BLUE
6158	wF12_d1-3_cDAPI	\N	\N
6159	original/PLATE-1/bPLATE_wF12_s7_cRGB.png	\N	GREEN
6160	wF12_d1-3_cGFP	\N	\N
6161	original/PLATE-1/bPLATE_wF12_s7_cRGB.png	\N	RED
6162	wF12_d1-3_cCy5	\N	\N
6163	original/PLATE-1/bPLATE_wA10_s1_cRGB.png	\N	BLUE
6164	wA10_d1-1_cDAPI	\N	\N
6165	original/PLATE-1/bPLATE_wA10_s1_cRGB.png	\N	GREEN
6166	wA10_d1-1_cGFP	\N	\N
6167	original/PLATE-1/bPLATE_wA10_s1_cRGB.png	\N	RED
6168	wA10_d1-1_cCy5	\N	\N
6169	original/PLATE-1/bPLATE_wB1_s7_cRGB.png	\N	BLUE
6170	wB1_d1-3_cDAPI	\N	\N
6171	original/PLATE-1/bPLATE_wB1_s7_cRGB.png	\N	GREEN
6172	wB1_d1-3_cGFP	\N	\N
6173	original/PLATE-1/bPLATE_wB1_s7_cRGB.png	\N	RED
6174	wB1_d1-3_cCy5	\N	\N
6175	original/PLATE-1/bPLATE_wD8_s4_cRGB.png	\N	BLUE
6176	wD8_d1-2_cDAPI	\N	\N
6177	original/PLATE-1/bPLATE_wD8_s4_cRGB.png	\N	GREEN
6178	wD8_d1-2_cGFP	\N	\N
6179	original/PLATE-1/bPLATE_wD8_s4_cRGB.png	\N	RED
6180	wD8_d1-2_cCy5	\N	\N
6181	original/PLATE-1/bPLATE_wG3_s1_cRGB.png	\N	BLUE
6182	wG3_d1-1_cDAPI	\N	\N
6183	original/PLATE-1/bPLATE_wG3_s1_cRGB.png	\N	GREEN
6184	wG3_d1-1_cGFP	\N	\N
6185	original/PLATE-1/bPLATE_wG3_s1_cRGB.png	\N	RED
6186	wG3_d1-1_cCy5	\N	\N
6187	original/PLATE-1/bPLATE_wC2_s4_cRGB.png	\N	BLUE
6188	wC2_d1-2_cDAPI	\N	\N
6189	original/PLATE-1/bPLATE_wC2_s4_cRGB.png	\N	GREEN
6190	wC2_d1-2_cGFP	\N	\N
6191	original/PLATE-1/bPLATE_wC2_s4_cRGB.png	\N	RED
6192	wC2_d1-2_cCy5	\N	\N
6193	original/PLATE-1/bPLATE_wE9_s1_cRGB.png	\N	BLUE
6194	wE9_d1-1_cDAPI	\N	\N
6195	original/PLATE-1/bPLATE_wE9_s1_cRGB.png	\N	GREEN
6196	wE9_d1-1_cGFP	\N	\N
6197	original/PLATE-1/bPLATE_wE9_s1_cRGB.png	\N	RED
6198	wE9_d1-1_cCy5	\N	\N
6199	original/PLATE-1/bPLATE_wG4_s7_cRGB.png	\N	BLUE
6200	wG4_d1-3_cDAPI	\N	\N
6201	original/PLATE-1/bPLATE_wG4_s7_cRGB.png	\N	GREEN
6202	wG4_d1-3_cGFP	\N	\N
6203	original/PLATE-1/bPLATE_wG4_s7_cRGB.png	\N	RED
6204	wG4_d1-3_cCy5	\N	\N
6205	original/PLATE-1/bPLATE_wB6_s4_cRGB.png	\N	BLUE
6206	wB6_d1-2_cDAPI	\N	\N
6207	original/PLATE-1/bPLATE_wB6_s4_cRGB.png	\N	GREEN
6208	wB6_d1-2_cGFP	\N	\N
6209	original/PLATE-1/bPLATE_wB6_s4_cRGB.png	\N	RED
6210	wB6_d1-2_cCy5	\N	\N
6211	original/PLATE-1/bPLATE_wE1_s1_cRGB.png	\N	BLUE
6212	wE1_d1-1_cDAPI	\N	\N
6213	original/PLATE-1/bPLATE_wE1_s1_cRGB.png	\N	GREEN
6214	wE1_d1-1_cGFP	\N	\N
6215	original/PLATE-1/bPLATE_wE1_s1_cRGB.png	\N	RED
6216	wE1_d1-1_cCy5	\N	\N
6217	original/PLATE-1/bPLATE_wH7_s6_cRGB.png	\N	BLUE
6218	wH7_d3-2_cDAPI	\N	\N
6219	original/PLATE-1/bPLATE_wH7_s6_cRGB.png	\N	GREEN
6220	wH7_d3-2_cGFP	\N	\N
6221	original/PLATE-1/bPLATE_wH7_s6_cRGB.png	\N	RED
6222	wH7_d3-2_cCy5	\N	\N
6223	original/PLATE-1/bPLATE_wE12_s9_cRGB.png	\N	BLUE
6224	wE12_d3-3_cDAPI	\N	\N
6225	original/PLATE-1/bPLATE_wE12_s9_cRGB.png	\N	GREEN
6226	wE12_d3-3_cGFP	\N	\N
6227	original/PLATE-1/bPLATE_wE12_s9_cRGB.png	\N	RED
6228	wE12_d3-3_cCy5	\N	\N
6229	original/PLATE-1/bPLATE_wH9_s7_cRGB.png	\N	BLUE
6230	wH9_d1-3_cDAPI	\N	\N
6231	original/PLATE-1/bPLATE_wH9_s7_cRGB.png	\N	GREEN
6232	wH9_d1-3_cGFP	\N	\N
6233	original/PLATE-1/bPLATE_wH9_s7_cRGB.png	\N	RED
6234	wH9_d1-3_cCy5	\N	\N
6235	original/PLATE-1/bPLATE_wB10_s3_cRGB.png	\N	BLUE
6236	wB10_d3-1_cDAPI	\N	\N
6237	original/PLATE-1/bPLATE_wB10_s3_cRGB.png	\N	GREEN
6238	wB10_d3-1_cGFP	\N	\N
6239	original/PLATE-1/bPLATE_wB10_s3_cRGB.png	\N	RED
6240	wB10_d3-1_cCy5	\N	\N
6241	original/PLATE-1/bPLATE_wA11_s5_cRGB.png	\N	BLUE
6242	wA11_d2-2_cDAPI	\N	\N
6243	original/PLATE-1/bPLATE_wA11_s5_cRGB.png	\N	GREEN
6244	wA11_d2-2_cGFP	\N	\N
6245	original/PLATE-1/bPLATE_wA11_s5_cRGB.png	\N	RED
6246	wA11_d2-2_cCy5	\N	\N
6247	original/PLATE-1/bPLATE_wD6_s2_cRGB.png	\N	BLUE
6248	wD6_d2-1_cDAPI	\N	\N
6249	original/PLATE-1/bPLATE_wD6_s2_cRGB.png	\N	GREEN
6250	wD6_d2-1_cGFP	\N	\N
6251	original/PLATE-1/bPLATE_wD6_s2_cRGB.png	\N	RED
6252	wD6_d2-1_cCy5	\N	\N
6253	original/PLATE-1/bPLATE_wE4_s9_cRGB.png	\N	BLUE
6254	wE4_d3-3_cDAPI	\N	\N
6255	original/PLATE-1/bPLATE_wE4_s9_cRGB.png	\N	GREEN
6256	wE4_d3-3_cGFP	\N	\N
6257	original/PLATE-1/bPLATE_wE4_s9_cRGB.png	\N	RED
6258	wE4_d3-3_cCy5	\N	\N
6259	original/PLATE-1/bPLATE_wG11_s6_cRGB.png	\N	BLUE
6260	wG11_d3-2_cDAPI	\N	\N
6261	original/PLATE-1/bPLATE_wG11_s6_cRGB.png	\N	GREEN
6262	wG11_d3-2_cGFP	\N	\N
6263	original/PLATE-1/bPLATE_wG11_s6_cRGB.png	\N	RED
6264	wG11_d3-2_cCy5	\N	\N
6265	original/PLATE-1/bPLATE_wA3_s2_cRGB.png	\N	BLUE
6266	wA3_d2-1_cDAPI	\N	\N
6267	original/PLATE-1/bPLATE_wA3_s2_cRGB.png	\N	GREEN
6268	wA3_d2-1_cGFP	\N	\N
6269	original/PLATE-1/bPLATE_wA3_s2_cRGB.png	\N	RED
6270	wA3_d2-1_cCy5	\N	\N
6271	original/PLATE-1/bPLATE_wA2_s3_cRGB.png	\N	BLUE
6272	wA2_d3-1_cDAPI	\N	\N
6273	original/PLATE-1/bPLATE_wA2_s3_cRGB.png	\N	GREEN
6274	wA2_d3-1_cGFP	\N	\N
6275	original/PLATE-1/bPLATE_wA2_s3_cRGB.png	\N	RED
6276	wA2_d3-1_cCy5	\N	\N
6277	original/PLATE-1/bPLATE_wD9_s8_cRGB.png	\N	BLUE
6278	wD9_d2-3_cDAPI	\N	\N
6279	original/PLATE-1/bPLATE_wD9_s8_cRGB.png	\N	GREEN
6280	wD9_d2-3_cGFP	\N	\N
6281	original/PLATE-1/bPLATE_wD9_s8_cRGB.png	\N	RED
6282	wD9_d2-3_cCy5	\N	\N
6283	original/PLATE-1/bPLATE_wG4_s5_cRGB.png	\N	BLUE
6284	wG4_d2-2_cDAPI	\N	\N
6285	original/PLATE-1/bPLATE_wG4_s5_cRGB.png	\N	GREEN
6286	wG4_d2-2_cGFP	\N	\N
6287	original/PLATE-1/bPLATE_wG4_s5_cRGB.png	\N	RED
6288	wG4_d2-2_cCy5	\N	\N
6289	original/PLATE-1/bPLATE_wB9_s6_cRGB.png	\N	BLUE
6290	wB9_d3-2_cDAPI	\N	\N
6291	original/PLATE-1/bPLATE_wB9_s6_cRGB.png	\N	GREEN
6292	wB9_d3-2_cGFP	\N	\N
6293	original/PLATE-1/bPLATE_wB9_s6_cRGB.png	\N	RED
6294	wB9_d3-2_cCy5	\N	\N
6295	original/PLATE-1/bPLATE_wE4_s3_cRGB.png	\N	BLUE
6296	wE4_d3-1_cDAPI	\N	\N
6297	original/PLATE-1/bPLATE_wE4_s3_cRGB.png	\N	GREEN
6298	wE4_d3-1_cGFP	\N	\N
6299	original/PLATE-1/bPLATE_wE4_s3_cRGB.png	\N	RED
6300	wE4_d3-1_cCy5	\N	\N
6301	original/PLATE-1/bPLATE_wC3_s5_cRGB.png	\N	BLUE
6302	wC3_d2-2_cDAPI	\N	\N
6303	original/PLATE-1/bPLATE_wC3_s5_cRGB.png	\N	GREEN
6304	wC3_d2-2_cGFP	\N	\N
6305	original/PLATE-1/bPLATE_wC3_s5_cRGB.png	\N	RED
6306	wC3_d2-2_cCy5	\N	\N
6307	original/PLATE-1/bPLATE_wE10_s2_cRGB.png	\N	BLUE
6308	wE10_d2-1_cDAPI	\N	\N
6309	original/PLATE-1/bPLATE_wE10_s2_cRGB.png	\N	GREEN
6310	wE10_d2-1_cGFP	\N	\N
6311	original/PLATE-1/bPLATE_wE10_s2_cRGB.png	\N	RED
6312	wE10_d2-1_cCy5	\N	\N
6313	original/PLATE-1/bPLATE_wD4_s1_cRGB.png	\N	BLUE
6314	wD4_d1-1_cDAPI	\N	\N
6315	original/PLATE-1/bPLATE_wD4_s1_cRGB.png	\N	GREEN
6316	wD4_d1-1_cGFP	\N	\N
6317	original/PLATE-1/bPLATE_wD4_s1_cRGB.png	\N	RED
6318	wD4_d1-1_cCy5	\N	\N
6319	original/PLATE-1/bPLATE_wB8_s4_cRGB.png	\N	BLUE
6320	wB8_d1-2_cDAPI	\N	\N
6321	original/PLATE-1/bPLATE_wB8_s4_cRGB.png	\N	GREEN
6322	wB8_d1-2_cGFP	\N	\N
6323	original/PLATE-1/bPLATE_wB8_s4_cRGB.png	\N	RED
6324	wB8_d1-2_cCy5	\N	\N
6325	original/PLATE-1/bPLATE_wA9_s4_cRGB.png	\N	BLUE
6326	wA9_d1-2_cDAPI	\N	\N
6327	original/PLATE-1/bPLATE_wA9_s4_cRGB.png	\N	GREEN
6328	wA9_d1-2_cGFP	\N	\N
6329	original/PLATE-1/bPLATE_wA9_s4_cRGB.png	\N	RED
6330	wA9_d1-2_cCy5	\N	\N
6331	original/PLATE-1/bPLATE_wE3_s1_cRGB.png	\N	BLUE
6332	wE3_d1-1_cDAPI	\N	\N
6333	original/PLATE-1/bPLATE_wE3_s1_cRGB.png	\N	GREEN
6334	wE3_d1-1_cGFP	\N	\N
6335	original/PLATE-1/bPLATE_wE3_s1_cRGB.png	\N	RED
6336	wE3_d1-1_cCy5	\N	\N
6337	original/PLATE-1/bPLATE_wC3_s3_cRGB.png	\N	BLUE
6338	wC3_d3-1_cDAPI	\N	\N
6339	original/PLATE-1/bPLATE_wC3_s3_cRGB.png	\N	GREEN
6340	wC3_d3-1_cGFP	\N	\N
6341	original/PLATE-1/bPLATE_wC3_s3_cRGB.png	\N	RED
6342	wC3_d3-1_cCy5	\N	\N
6343	original/PLATE-1/bPLATE_wB2_s9_cRGB.png	\N	BLUE
6344	wB2_d3-3_cDAPI	\N	\N
6345	original/PLATE-1/bPLATE_wB2_s9_cRGB.png	\N	GREEN
6346	wB2_d3-3_cGFP	\N	\N
6347	original/PLATE-1/bPLATE_wB2_s9_cRGB.png	\N	RED
6348	wB2_d3-3_cCy5	\N	\N
6349	original/PLATE-1/bPLATE_wD9_s6_cRGB.png	\N	BLUE
6350	wD9_d3-2_cDAPI	\N	\N
6351	original/PLATE-1/bPLATE_wD9_s6_cRGB.png	\N	GREEN
6352	wD9_d3-2_cGFP	\N	\N
6353	original/PLATE-1/bPLATE_wD9_s6_cRGB.png	\N	RED
6354	wD9_d3-2_cCy5	\N	\N
6355	original/PLATE-1/bPLATE_wG4_s3_cRGB.png	\N	BLUE
6356	wG4_d3-1_cDAPI	\N	\N
6357	original/PLATE-1/bPLATE_wG4_s3_cRGB.png	\N	GREEN
6358	wG4_d3-1_cGFP	\N	\N
6359	original/PLATE-1/bPLATE_wG4_s3_cRGB.png	\N	RED
6360	wG4_d3-1_cCy5	\N	\N
6361	original/PLATE-1/bPLATE_wF4_s7_cRGB.png	\N	BLUE
6362	wF4_d1-3_cDAPI	\N	\N
6363	original/PLATE-1/bPLATE_wF4_s7_cRGB.png	\N	GREEN
6364	wF4_d1-3_cGFP	\N	\N
6365	original/PLATE-1/bPLATE_wF4_s7_cRGB.png	\N	RED
6366	wF4_d1-3_cCy5	\N	\N
6367	original/PLATE-1/bPLATE_wH11_s4_cRGB.png	\N	BLUE
6368	wH11_d1-2_cDAPI	\N	\N
6369	original/PLATE-1/bPLATE_wH11_s4_cRGB.png	\N	GREEN
6370	wH11_d1-2_cGFP	\N	\N
6371	original/PLATE-1/bPLATE_wH11_s4_cRGB.png	\N	RED
6372	wH11_d1-2_cCy5	\N	\N
6373	original/PLATE-1/bPLATE_wG3_s9_cRGB.png	\N	BLUE
6374	wG3_d3-3_cDAPI	\N	\N
6375	original/PLATE-1/bPLATE_wG3_s9_cRGB.png	\N	GREEN
6376	wG3_d3-3_cGFP	\N	\N
6377	original/PLATE-1/bPLATE_wG3_s9_cRGB.png	\N	RED
6378	wG3_d3-3_cCy5	\N	\N
6379	original/PLATE-1/bPLATE_wD10_s7_cRGB.png	\N	BLUE
6380	wD10_d1-3_cDAPI	\N	\N
6381	original/PLATE-1/bPLATE_wD10_s7_cRGB.png	\N	GREEN
6382	wD10_d1-3_cGFP	\N	\N
6383	original/PLATE-1/bPLATE_wD10_s7_cRGB.png	\N	RED
6384	wD10_d1-3_cCy5	\N	\N
6385	original/PLATE-1/bPLATE_wG5_s4_cRGB.png	\N	BLUE
6386	wG5_d1-2_cDAPI	\N	\N
6387	original/PLATE-1/bPLATE_wG5_s4_cRGB.png	\N	GREEN
6388	wG5_d1-2_cGFP	\N	\N
6389	original/PLATE-1/bPLATE_wG5_s4_cRGB.png	\N	RED
6390	wG5_d1-2_cCy5	\N	\N
6391	original/PLATE-1/bPLATE_wH9_s8_cRGB.png	\N	BLUE
6392	wH9_d2-3_cDAPI	\N	\N
6393	original/PLATE-1/bPLATE_wH9_s8_cRGB.png	\N	GREEN
6394	wH9_d2-3_cGFP	\N	\N
6395	original/PLATE-1/bPLATE_wH9_s8_cRGB.png	\N	RED
6396	wH9_d2-3_cCy5	\N	\N
6397	original/PLATE-1/bPLATE_wA2_s1_cRGB.png	\N	BLUE
6398	wA2_d1-1_cDAPI	\N	\N
6399	original/PLATE-1/bPLATE_wA2_s1_cRGB.png	\N	GREEN
6400	wA2_d1-1_cGFP	\N	\N
6401	original/PLATE-1/bPLATE_wA2_s1_cRGB.png	\N	RED
6402	wA2_d1-1_cCy5	\N	\N
6403	original/PLATE-1/bPLATE_wB2_s3_cRGB.png	\N	BLUE
6404	wB2_d3-1_cDAPI	\N	\N
6405	original/PLATE-1/bPLATE_wB2_s3_cRGB.png	\N	GREEN
6406	wB2_d3-1_cGFP	\N	\N
6407	original/PLATE-1/bPLATE_wB2_s3_cRGB.png	\N	RED
6408	wB2_d3-1_cCy5	\N	\N
6409	original/PLATE-1/bPLATE_wH12_s5_cRGB.png	\N	BLUE
6410	wH12_d2-2_cDAPI	\N	\N
6411	original/PLATE-1/bPLATE_wH12_s5_cRGB.png	\N	GREEN
6412	wH12_d2-2_cGFP	\N	\N
6413	original/PLATE-1/bPLATE_wH12_s5_cRGB.png	\N	RED
6414	wH12_d2-2_cCy5	\N	\N
6415	original/PLATE-1/bPLATE_wF5_s8_cRGB.png	\N	BLUE
6416	wF5_d2-3_cDAPI	\N	\N
6417	original/PLATE-1/bPLATE_wF5_s8_cRGB.png	\N	GREEN
6418	wF5_d2-3_cGFP	\N	\N
6419	original/PLATE-1/bPLATE_wF5_s8_cRGB.png	\N	RED
6420	wF5_d2-3_cCy5	\N	\N
6421	original/PLATE-1/bPLATE_wD11_s1_cRGB.png	\N	BLUE
6422	wD11_d1-1_cDAPI	\N	\N
6423	original/PLATE-1/bPLATE_wD11_s1_cRGB.png	\N	GREEN
6424	wD11_d1-1_cGFP	\N	\N
6425	original/PLATE-1/bPLATE_wD11_s1_cRGB.png	\N	RED
6426	wD11_d1-1_cCy5	\N	\N
6427	original/PLATE-1/bPLATE_wB4_s4_cRGB.png	\N	BLUE
6428	wB4_d1-2_cDAPI	\N	\N
6429	original/PLATE-1/bPLATE_wB4_s4_cRGB.png	\N	GREEN
6430	wB4_d1-2_cGFP	\N	\N
6431	original/PLATE-1/bPLATE_wB4_s4_cRGB.png	\N	RED
6432	wB4_d1-2_cCy5	\N	\N
6433	original/PLATE-1/bPLATE_wC4_s1_cRGB.png	\N	BLUE
6434	wC4_d1-1_cDAPI	\N	\N
6435	original/PLATE-1/bPLATE_wC4_s1_cRGB.png	\N	GREEN
6436	wC4_d1-1_cGFP	\N	\N
6437	original/PLATE-1/bPLATE_wC4_s1_cRGB.png	\N	RED
6438	wC4_d1-1_cCy5	\N	\N
6439	original/PLATE-1/bPLATE_wB11_s4_cRGB.png	\N	BLUE
6440	wB11_d1-2_cDAPI	\N	\N
6441	original/PLATE-1/bPLATE_wB11_s4_cRGB.png	\N	GREEN
6442	wB11_d1-2_cGFP	\N	\N
6443	original/PLATE-1/bPLATE_wB11_s4_cRGB.png	\N	RED
6444	wB11_d1-2_cCy5	\N	\N
6445	original/PLATE-1/bPLATE_wE6_s1_cRGB.png	\N	BLUE
6446	wE6_d1-1_cDAPI	\N	\N
6447	original/PLATE-1/bPLATE_wE6_s1_cRGB.png	\N	GREEN
6448	wE6_d1-1_cGFP	\N	\N
6449	original/PLATE-1/bPLATE_wE6_s1_cRGB.png	\N	RED
6450	wE6_d1-1_cCy5	\N	\N
6451	original/PLATE-1/bPLATE_wB8_s5_cRGB.png	\N	BLUE
6452	wB8_d2-2_cDAPI	\N	\N
6453	original/PLATE-1/bPLATE_wB8_s5_cRGB.png	\N	GREEN
6454	wB8_d2-2_cGFP	\N	\N
6455	original/PLATE-1/bPLATE_wB8_s5_cRGB.png	\N	RED
6456	wB8_d2-2_cCy5	\N	\N
6457	original/PLATE-1/bPLATE_wE3_s2_cRGB.png	\N	BLUE
6458	wE3_d2-1_cDAPI	\N	\N
6459	original/PLATE-1/bPLATE_wE3_s2_cRGB.png	\N	GREEN
6460	wE3_d2-1_cGFP	\N	\N
6461	original/PLATE-1/bPLATE_wE3_s2_cRGB.png	\N	RED
6462	wE3_d2-1_cCy5	\N	\N
6463	original/PLATE-1/bPLATE_wF10_s8_cRGB.png	\N	BLUE
6464	wF10_d2-3_cDAPI	\N	\N
6465	original/PLATE-1/bPLATE_wF10_s8_cRGB.png	\N	GREEN
6466	wF10_d2-3_cGFP	\N	\N
6467	original/PLATE-1/bPLATE_wF10_s8_cRGB.png	\N	RED
6468	wF10_d2-3_cCy5	\N	\N
6469	original/PLATE-1/bPLATE_wD8_s9_cRGB.png	\N	BLUE
6470	wD8_d3-3_cDAPI	\N	\N
6471	original/PLATE-1/bPLATE_wD8_s9_cRGB.png	\N	GREEN
6472	wD8_d3-3_cGFP	\N	\N
6473	original/PLATE-1/bPLATE_wD8_s9_cRGB.png	\N	RED
6474	wD8_d3-3_cCy5	\N	\N
6475	original/PLATE-1/bPLATE_wG3_s6_cRGB.png	\N	BLUE
6476	wG3_d3-2_cDAPI	\N	\N
6477	original/PLATE-1/bPLATE_wG3_s6_cRGB.png	\N	GREEN
6478	wG3_d3-2_cGFP	\N	\N
6479	original/PLATE-1/bPLATE_wG3_s6_cRGB.png	\N	RED
6480	wG3_d3-2_cCy5	\N	\N
6481	original/PLATE-1/bPLATE_wB12_s5_cRGB.png	\N	BLUE
6482	wB12_d2-2_cDAPI	\N	\N
6483	original/PLATE-1/bPLATE_wB12_s5_cRGB.png	\N	GREEN
6484	wB12_d2-2_cGFP	\N	\N
6485	original/PLATE-1/bPLATE_wB12_s5_cRGB.png	\N	RED
6486	wB12_d2-2_cCy5	\N	\N
6487	original/PLATE-1/bPLATE_wE7_s2_cRGB.png	\N	BLUE
6488	wE7_d2-1_cDAPI	\N	\N
6489	original/PLATE-1/bPLATE_wE7_s2_cRGB.png	\N	GREEN
6490	wE7_d2-1_cGFP	\N	\N
6491	original/PLATE-1/bPLATE_wE7_s2_cRGB.png	\N	RED
6492	wE7_d2-1_cCy5	\N	\N
6493	original/PLATE-1/bPLATE_wA4_s1_cRGB.png	\N	BLUE
6494	wA4_d1-1_cDAPI	\N	\N
6495	original/PLATE-1/bPLATE_wA4_s1_cRGB.png	\N	GREEN
6496	wA4_d1-1_cGFP	\N	\N
6497	original/PLATE-1/bPLATE_wA4_s1_cRGB.png	\N	RED
6498	wA4_d1-1_cCy5	\N	\N
6499	original/PLATE-1/bPLATE_wF7_s9_cRGB.png	\N	BLUE
6500	wF7_d3-3_cDAPI	\N	\N
6501	original/PLATE-1/bPLATE_wF7_s9_cRGB.png	\N	GREEN
6502	wF7_d3-3_cGFP	\N	\N
6503	original/PLATE-1/bPLATE_wF7_s9_cRGB.png	\N	RED
6504	wF7_d3-3_cCy5	\N	\N
6505	original/PLATE-1/bPLATE_wD4_s9_cRGB.png	\N	BLUE
6506	wD4_d3-3_cDAPI	\N	\N
6507	original/PLATE-1/bPLATE_wD4_s9_cRGB.png	\N	GREEN
6508	wD4_d3-3_cGFP	\N	\N
6509	original/PLATE-1/bPLATE_wD4_s9_cRGB.png	\N	RED
6510	wD4_d3-3_cCy5	\N	\N
6511	original/PLATE-1/bPLATE_wF11_s6_cRGB.png	\N	BLUE
6512	wF11_d3-2_cDAPI	\N	\N
6513	original/PLATE-1/bPLATE_wF11_s6_cRGB.png	\N	GREEN
6514	wF11_d3-2_cGFP	\N	\N
6515	original/PLATE-1/bPLATE_wF11_s6_cRGB.png	\N	RED
6516	wF11_d3-2_cCy5	\N	\N
6517	original/PLATE-1/bPLATE_wB12_s4_cRGB.png	\N	BLUE
6518	wB12_d1-2_cDAPI	\N	\N
6519	original/PLATE-1/bPLATE_wB12_s4_cRGB.png	\N	GREEN
6520	wB12_d1-2_cGFP	\N	\N
6521	original/PLATE-1/bPLATE_wB12_s4_cRGB.png	\N	RED
6522	wB12_d1-2_cCy5	\N	\N
6523	original/PLATE-1/bPLATE_wE7_s1_cRGB.png	\N	BLUE
6524	wE7_d1-1_cDAPI	\N	\N
6525	original/PLATE-1/bPLATE_wE7_s1_cRGB.png	\N	GREEN
6526	wE7_d1-1_cGFP	\N	\N
6527	original/PLATE-1/bPLATE_wE7_s1_cRGB.png	\N	RED
6528	wE7_d1-1_cCy5	\N	\N
6529	original/PLATE-1/bPLATE_wH10_s8_cRGB.png	\N	BLUE
6530	wH10_d2-3_cDAPI	\N	\N
6531	original/PLATE-1/bPLATE_wH10_s8_cRGB.png	\N	GREEN
6532	wH10_d2-3_cGFP	\N	\N
6533	original/PLATE-1/bPLATE_wH10_s8_cRGB.png	\N	RED
6534	wH10_d2-3_cCy5	\N	\N
6535	original/PLATE-1/bPLATE_wF3_s7_cRGB.png	\N	BLUE
6536	wF3_d1-3_cDAPI	\N	\N
6537	original/PLATE-1/bPLATE_wF3_s7_cRGB.png	\N	GREEN
6538	wF3_d1-3_cGFP	\N	\N
6539	original/PLATE-1/bPLATE_wF3_s7_cRGB.png	\N	RED
6540	wF3_d1-3_cCy5	\N	\N
6541	original/PLATE-1/bPLATE_wH10_s4_cRGB.png	\N	BLUE
6542	wH10_d1-2_cDAPI	\N	\N
6543	original/PLATE-1/bPLATE_wH10_s4_cRGB.png	\N	GREEN
6544	wH10_d1-2_cGFP	\N	\N
6545	original/PLATE-1/bPLATE_wH10_s4_cRGB.png	\N	RED
6546	wH10_d1-2_cCy5	\N	\N
6547	original/PLATE-1/bPLATE_wH4_s7_cRGB.png	\N	BLUE
6548	wH4_d1-3_cDAPI	\N	\N
6549	original/PLATE-1/bPLATE_wH4_s7_cRGB.png	\N	GREEN
6550	wH4_d1-3_cGFP	\N	\N
6551	original/PLATE-1/bPLATE_wH4_s7_cRGB.png	\N	RED
6552	wH4_d1-3_cCy5	\N	\N
6553	original/PLATE-1/bPLATE_wB11_s1_cRGB.png	\N	BLUE
6554	wB11_d1-1_cDAPI	\N	\N
6555	original/PLATE-1/bPLATE_wB11_s1_cRGB.png	\N	GREEN
6556	wB11_d1-1_cGFP	\N	\N
6557	original/PLATE-1/bPLATE_wB11_s1_cRGB.png	\N	RED
6558	wB11_d1-1_cCy5	\N	\N
6559	original/PLATE-1/bPLATE_wD4_s6_cRGB.png	\N	BLUE
6560	wD4_d3-2_cDAPI	\N	\N
6561	original/PLATE-1/bPLATE_wD4_s6_cRGB.png	\N	GREEN
6562	wD4_d3-2_cGFP	\N	\N
6563	original/PLATE-1/bPLATE_wD4_s6_cRGB.png	\N	RED
6564	wD4_d3-2_cCy5	\N	\N
6565	original/PLATE-1/bPLATE_wA9_s9_cRGB.png	\N	BLUE
6566	wA9_d3-3_cDAPI	\N	\N
6567	original/PLATE-1/bPLATE_wA9_s9_cRGB.png	\N	GREEN
6568	wA9_d3-3_cGFP	\N	\N
6569	original/PLATE-1/bPLATE_wA9_s9_cRGB.png	\N	RED
6570	wA9_d3-3_cCy5	\N	\N
6571	original/PLATE-1/bPLATE_wF11_s3_cRGB.png	\N	BLUE
6572	wF11_d3-1_cDAPI	\N	\N
6573	original/PLATE-1/bPLATE_wF11_s3_cRGB.png	\N	GREEN
6574	wF11_d3-1_cGFP	\N	\N
6575	original/PLATE-1/bPLATE_wF11_s3_cRGB.png	\N	RED
6576	wF11_d3-1_cCy5	\N	\N
6577	original/PLATE-1/bPLATE_wH10_s9_cRGB.png	\N	BLUE
6578	wH10_d3-3_cDAPI	\N	\N
6579	original/PLATE-1/bPLATE_wH10_s9_cRGB.png	\N	GREEN
6580	wH10_d3-3_cGFP	\N	\N
6581	original/PLATE-1/bPLATE_wH10_s9_cRGB.png	\N	RED
6582	wH10_d3-3_cCy5	\N	\N
6583	original/PLATE-1/bPLATE_wC11_s8_cRGB.png	\N	BLUE
6584	wC11_d2-3_cDAPI	\N	\N
6585	original/PLATE-1/bPLATE_wC11_s8_cRGB.png	\N	GREEN
6586	wC11_d2-3_cGFP	\N	\N
6587	original/PLATE-1/bPLATE_wC11_s8_cRGB.png	\N	RED
6588	wC11_d2-3_cCy5	\N	\N
6589	original/PLATE-1/bPLATE_wF6_s5_cRGB.png	\N	BLUE
6590	wF6_d2-2_cDAPI	\N	\N
6591	original/PLATE-1/bPLATE_wF6_s5_cRGB.png	\N	GREEN
6592	wF6_d2-2_cGFP	\N	\N
6593	original/PLATE-1/bPLATE_wF6_s5_cRGB.png	\N	RED
6594	wF6_d2-2_cCy5	\N	\N
6595	original/PLATE-1/bPLATE_wB12_s1_cRGB.png	\N	BLUE
6596	wB12_d1-1_cDAPI	\N	\N
6597	original/PLATE-1/bPLATE_wB12_s1_cRGB.png	\N	GREEN
6598	wB12_d1-1_cGFP	\N	\N
6599	original/PLATE-1/bPLATE_wB12_s1_cRGB.png	\N	RED
6600	wB12_d1-1_cCy5	\N	\N
6601	original/PLATE-1/bPLATE_wA11_s4_cRGB.png	\N	BLUE
6602	wA11_d1-2_cDAPI	\N	\N
6603	original/PLATE-1/bPLATE_wA11_s4_cRGB.png	\N	GREEN
6604	wA11_d1-2_cGFP	\N	\N
6605	original/PLATE-1/bPLATE_wA11_s4_cRGB.png	\N	RED
6606	wA11_d1-2_cCy5	\N	\N
6607	original/PLATE-1/bPLATE_wD6_s1_cRGB.png	\N	BLUE
6608	wD6_d1-1_cDAPI	\N	\N
6609	original/PLATE-1/bPLATE_wD6_s1_cRGB.png	\N	GREEN
6610	wD6_d1-1_cGFP	\N	\N
6611	original/PLATE-1/bPLATE_wD6_s1_cRGB.png	\N	RED
6612	wD6_d1-1_cCy5	\N	\N
6613	original/PLATE-1/bPLATE_wA11_s9_cRGB.png	\N	BLUE
6614	wA11_d3-3_cDAPI	\N	\N
6615	original/PLATE-1/bPLATE_wA11_s9_cRGB.png	\N	GREEN
6616	wA11_d3-3_cGFP	\N	\N
6617	original/PLATE-1/bPLATE_wA11_s9_cRGB.png	\N	RED
6618	wA11_d3-3_cCy5	\N	\N
6619	original/PLATE-1/bPLATE_wD6_s6_cRGB.png	\N	BLUE
6620	wD6_d3-2_cDAPI	\N	\N
6621	original/PLATE-1/bPLATE_wD6_s6_cRGB.png	\N	GREEN
6622	wD6_d3-2_cGFP	\N	\N
6623	original/PLATE-1/bPLATE_wD6_s6_cRGB.png	\N	RED
6624	wD6_d3-2_cCy5	\N	\N
6625	original/PLATE-1/bPLATE_wG1_s3_cRGB.png	\N	BLUE
6626	wG1_d3-1_cDAPI	\N	\N
6627	original/PLATE-1/bPLATE_wG1_s3_cRGB.png	\N	GREEN
6628	wG1_d3-1_cGFP	\N	\N
6629	original/PLATE-1/bPLATE_wG1_s3_cRGB.png	\N	RED
6630	wG1_d3-1_cCy5	\N	\N
6631	original/PLATE-1/bPLATE_wH1_s2_cRGB.png	\N	BLUE
6632	wH1_d2-1_cDAPI	\N	\N
6633	original/PLATE-1/bPLATE_wH1_s2_cRGB.png	\N	GREEN
6634	wH1_d2-1_cGFP	\N	\N
6635	original/PLATE-1/bPLATE_wH1_s2_cRGB.png	\N	RED
6636	wH1_d2-1_cCy5	\N	\N
6637	original/PLATE-1/bPLATE_wB11_s8_cRGB.png	\N	BLUE
6638	wB11_d2-3_cDAPI	\N	\N
6639	original/PLATE-1/bPLATE_wB11_s8_cRGB.png	\N	GREEN
6640	wB11_d2-3_cGFP	\N	\N
6641	original/PLATE-1/bPLATE_wB11_s8_cRGB.png	\N	RED
6642	wB11_d2-3_cCy5	\N	\N
6643	original/PLATE-1/bPLATE_wE6_s5_cRGB.png	\N	BLUE
6644	wE6_d2-2_cDAPI	\N	\N
6645	original/PLATE-1/bPLATE_wE6_s5_cRGB.png	\N	GREEN
6646	wE6_d2-2_cGFP	\N	\N
6647	original/PLATE-1/bPLATE_wE6_s5_cRGB.png	\N	RED
6648	wE6_d2-2_cCy5	\N	\N
6649	original/PLATE-1/bPLATE_wD7_s7_cRGB.png	\N	BLUE
6650	wD7_d1-3_cDAPI	\N	\N
6651	original/PLATE-1/bPLATE_wD7_s7_cRGB.png	\N	GREEN
6652	wD7_d1-3_cGFP	\N	\N
6653	original/PLATE-1/bPLATE_wD7_s7_cRGB.png	\N	RED
6654	wD7_d1-3_cCy5	\N	\N
6655	original/PLATE-1/bPLATE_wG2_s4_cRGB.png	\N	BLUE
6656	wG2_d1-2_cDAPI	\N	\N
6657	original/PLATE-1/bPLATE_wG2_s4_cRGB.png	\N	GREEN
6658	wG2_d1-2_cGFP	\N	\N
6659	original/PLATE-1/bPLATE_wG2_s4_cRGB.png	\N	RED
6660	wG2_d1-2_cCy5	\N	\N
6661	original/PLATE-1/bPLATE_wG2_s9_cRGB.png	\N	BLUE
6662	wG2_d3-3_cDAPI	\N	\N
6663	original/PLATE-1/bPLATE_wG2_s9_cRGB.png	\N	GREEN
6664	wG2_d3-3_cGFP	\N	\N
6665	original/PLATE-1/bPLATE_wG2_s9_cRGB.png	\N	RED
6666	wG2_d3-3_cCy5	\N	\N
6667	original/PLATE-1/bPLATE_wB4_s1_cRGB.png	\N	BLUE
6668	wB4_d1-1_cDAPI	\N	\N
6669	original/PLATE-1/bPLATE_wB4_s1_cRGB.png	\N	GREEN
6670	wB4_d1-1_cGFP	\N	\N
6671	original/PLATE-1/bPLATE_wB4_s1_cRGB.png	\N	RED
6672	wB4_d1-1_cCy5	\N	\N
6673	original/PLATE-1/bPLATE_wC1_s3_cRGB.png	\N	BLUE
6674	wC1_d3-1_cDAPI	\N	\N
6675	original/PLATE-1/bPLATE_wC1_s3_cRGB.png	\N	GREEN
6676	wC1_d3-1_cGFP	\N	\N
6677	original/PLATE-1/bPLATE_wC1_s3_cRGB.png	\N	RED
6678	wC1_d3-1_cCy5	\N	\N
6679	original/PLATE-1/bPLATE_wA5_s1_cRGB.png	\N	BLUE
6680	wA5_d1-1_cDAPI	\N	\N
6681	original/PLATE-1/bPLATE_wA5_s1_cRGB.png	\N	GREEN
6682	wA5_d1-1_cGFP	\N	\N
6683	original/PLATE-1/bPLATE_wA5_s1_cRGB.png	\N	RED
6684	wA5_d1-1_cCy5	\N	\N
6685	original/PLATE-1/bPLATE_wF7_s7_cRGB.png	\N	BLUE
6686	wF7_d1-3_cDAPI	\N	\N
6687	original/PLATE-1/bPLATE_wF7_s7_cRGB.png	\N	GREEN
6688	wF7_d1-3_cGFP	\N	\N
6689	original/PLATE-1/bPLATE_wF7_s7_cRGB.png	\N	RED
6690	wF7_d1-3_cCy5	\N	\N
6691	original/PLATE-1/bPLATE_wF10_s7_cRGB.png	\N	BLUE
6692	wF10_d1-3_cDAPI	\N	\N
6693	original/PLATE-1/bPLATE_wF10_s7_cRGB.png	\N	GREEN
6694	wF10_d1-3_cGFP	\N	\N
6695	original/PLATE-1/bPLATE_wF10_s7_cRGB.png	\N	RED
6696	wF10_d1-3_cCy5	\N	\N
6697	original/PLATE-1/bPLATE_wB12_s6_cRGB.png	\N	BLUE
6698	wB12_d3-2_cDAPI	\N	\N
6699	original/PLATE-1/bPLATE_wB12_s6_cRGB.png	\N	GREEN
6700	wB12_d3-2_cGFP	\N	\N
6701	original/PLATE-1/bPLATE_wB12_s6_cRGB.png	\N	RED
6702	wB12_d3-2_cCy5	\N	\N
6703	original/PLATE-1/bPLATE_wE7_s3_cRGB.png	\N	BLUE
6704	wE7_d3-1_cDAPI	\N	\N
6705	original/PLATE-1/bPLATE_wE7_s3_cRGB.png	\N	GREEN
6706	wE7_d3-1_cGFP	\N	\N
6707	original/PLATE-1/bPLATE_wE7_s3_cRGB.png	\N	RED
6708	wE7_d3-1_cCy5	\N	\N
6709	original/PLATE-1/bPLATE_wB6_s5_cRGB.png	\N	BLUE
6710	wB6_d2-2_cDAPI	\N	\N
6711	original/PLATE-1/bPLATE_wB6_s5_cRGB.png	\N	GREEN
6712	wB6_d2-2_cGFP	\N	\N
6713	original/PLATE-1/bPLATE_wB6_s5_cRGB.png	\N	RED
6714	wB6_d2-2_cCy5	\N	\N
6715	original/PLATE-1/bPLATE_wE1_s2_cRGB.png	\N	BLUE
6716	wE1_d2-1_cDAPI	\N	\N
6717	original/PLATE-1/bPLATE_wE1_s2_cRGB.png	\N	GREEN
6718	wE1_d2-1_cGFP	\N	\N
6719	original/PLATE-1/bPLATE_wE1_s2_cRGB.png	\N	RED
6720	wE1_d2-1_cCy5	\N	\N
6721	original/PLATE-1/bPLATE_wD11_s8_cRGB.png	\N	BLUE
6722	wD11_d2-3_cDAPI	\N	\N
6723	original/PLATE-1/bPLATE_wD11_s8_cRGB.png	\N	GREEN
6724	wD11_d2-3_cGFP	\N	\N
6725	original/PLATE-1/bPLATE_wD11_s8_cRGB.png	\N	RED
6726	wD11_d2-3_cCy5	\N	\N
6727	original/PLATE-1/bPLATE_wG6_s5_cRGB.png	\N	BLUE
6728	wG6_d2-2_cDAPI	\N	\N
6729	original/PLATE-1/bPLATE_wG6_s5_cRGB.png	\N	GREEN
6730	wG6_d2-2_cGFP	\N	\N
6731	original/PLATE-1/bPLATE_wG6_s5_cRGB.png	\N	RED
6732	wG6_d2-2_cCy5	\N	\N
6733	original/PLATE-1/bPLATE_wH5_s2_cRGB.png	\N	BLUE
6734	wH5_d2-1_cDAPI	\N	\N
6735	original/PLATE-1/bPLATE_wH5_s2_cRGB.png	\N	GREEN
6736	wH5_d2-1_cGFP	\N	\N
6737	original/PLATE-1/bPLATE_wH5_s2_cRGB.png	\N	RED
6738	wH5_d2-1_cCy5	\N	\N
6739	original/PLATE-1/bPLATE_wC3_s8_cRGB.png	\N	BLUE
6740	wC3_d2-3_cDAPI	\N	\N
6741	original/PLATE-1/bPLATE_wC3_s8_cRGB.png	\N	GREEN
6742	wC3_d2-3_cGFP	\N	\N
6743	original/PLATE-1/bPLATE_wC3_s8_cRGB.png	\N	RED
6744	wC3_d2-3_cCy5	\N	\N
6745	original/PLATE-1/bPLATE_wE10_s5_cRGB.png	\N	BLUE
6746	wE10_d2-2_cDAPI	\N	\N
6747	original/PLATE-1/bPLATE_wE10_s5_cRGB.png	\N	GREEN
6748	wE10_d2-2_cGFP	\N	\N
6749	original/PLATE-1/bPLATE_wE10_s5_cRGB.png	\N	RED
6750	wE10_d2-2_cCy5	\N	\N
6751	original/PLATE-1/bPLATE_wG6_s8_cRGB.png	\N	BLUE
6752	wG6_d2-3_cDAPI	\N	\N
6753	original/PLATE-1/bPLATE_wG6_s8_cRGB.png	\N	GREEN
6754	wG6_d2-3_cGFP	\N	\N
6755	original/PLATE-1/bPLATE_wG6_s8_cRGB.png	\N	RED
6756	wG6_d2-3_cCy5	\N	\N
6757	original/PLATE-1/bPLATE_wH6_s9_cRGB.png	\N	BLUE
6758	wH6_d3-3_cDAPI	\N	\N
6759	original/PLATE-1/bPLATE_wH6_s9_cRGB.png	\N	GREEN
6760	wH6_d3-3_cGFP	\N	\N
6761	original/PLATE-1/bPLATE_wH6_s9_cRGB.png	\N	RED
6762	wH6_d3-3_cCy5	\N	\N
6763	original/PLATE-1/bPLATE_wE5_s9_cRGB.png	\N	BLUE
6764	wE5_d3-3_cDAPI	\N	\N
6765	original/PLATE-1/bPLATE_wE5_s9_cRGB.png	\N	GREEN
6766	wE5_d3-3_cGFP	\N	\N
6767	original/PLATE-1/bPLATE_wE5_s9_cRGB.png	\N	RED
6768	wE5_d3-3_cCy5	\N	\N
6769	original/PLATE-1/bPLATE_wG12_s6_cRGB.png	\N	BLUE
6770	wG12_d3-2_cDAPI	\N	\N
6771	original/PLATE-1/bPLATE_wG12_s6_cRGB.png	\N	GREEN
6772	wG12_d3-2_cGFP	\N	\N
6773	original/PLATE-1/bPLATE_wG12_s6_cRGB.png	\N	RED
6774	wG12_d3-2_cCy5	\N	\N
6775	original/PLATE-1/bPLATE_wH2_s5_cRGB.png	\N	BLUE
6776	wH2_d2-2_cDAPI	\N	\N
6777	original/PLATE-1/bPLATE_wH2_s5_cRGB.png	\N	GREEN
6778	wH2_d2-2_cGFP	\N	\N
6779	original/PLATE-1/bPLATE_wH2_s5_cRGB.png	\N	RED
6780	wH2_d2-2_cCy5	\N	\N
6781	original/PLATE-1/bPLATE_wE7_s8_cRGB.png	\N	BLUE
6782	wE7_d2-3_cDAPI	\N	\N
6783	original/PLATE-1/bPLATE_wE7_s8_cRGB.png	\N	GREEN
6784	wE7_d2-3_cGFP	\N	\N
6785	original/PLATE-1/bPLATE_wE7_s8_cRGB.png	\N	RED
6786	wE7_d2-3_cCy5	\N	\N
6787	original/PLATE-1/bPLATE_wH7_s2_cRGB.png	\N	BLUE
6788	wH7_d2-1_cDAPI	\N	\N
6789	original/PLATE-1/bPLATE_wH7_s2_cRGB.png	\N	GREEN
6790	wH7_d2-1_cGFP	\N	\N
6791	original/PLATE-1/bPLATE_wH7_s2_cRGB.png	\N	RED
6792	wH7_d2-1_cCy5	\N	\N
6793	original/PLATE-1/bPLATE_wC5_s8_cRGB.png	\N	BLUE
6794	wC5_d2-3_cDAPI	\N	\N
6795	original/PLATE-1/bPLATE_wC5_s8_cRGB.png	\N	GREEN
6796	wC5_d2-3_cGFP	\N	\N
6797	original/PLATE-1/bPLATE_wC5_s8_cRGB.png	\N	RED
6798	wC5_d2-3_cCy5	\N	\N
6799	original/PLATE-1/bPLATE_wE12_s5_cRGB.png	\N	BLUE
6800	wE12_d2-2_cDAPI	\N	\N
6801	original/PLATE-1/bPLATE_wE12_s5_cRGB.png	\N	GREEN
6802	wE12_d2-2_cGFP	\N	\N
6803	original/PLATE-1/bPLATE_wE12_s5_cRGB.png	\N	RED
6804	wE12_d2-2_cCy5	\N	\N
6805	original/PLATE-1/bPLATE_wC12_s6_cRGB.png	\N	BLUE
6806	wC12_d3-2_cDAPI	\N	\N
6807	original/PLATE-1/bPLATE_wC12_s6_cRGB.png	\N	GREEN
6808	wC12_d3-2_cGFP	\N	\N
6809	original/PLATE-1/bPLATE_wC12_s6_cRGB.png	\N	RED
6810	wC12_d3-2_cCy5	\N	\N
6811	original/PLATE-1/bPLATE_wA5_s9_cRGB.png	\N	BLUE
6812	wA5_d3-3_cDAPI	\N	\N
6813	original/PLATE-1/bPLATE_wA5_s9_cRGB.png	\N	GREEN
6814	wA5_d3-3_cGFP	\N	\N
6815	original/PLATE-1/bPLATE_wA5_s9_cRGB.png	\N	RED
6816	wA5_d3-3_cCy5	\N	\N
6817	original/PLATE-1/bPLATE_wF7_s3_cRGB.png	\N	BLUE
6818	wF7_d3-1_cDAPI	\N	\N
6819	original/PLATE-1/bPLATE_wF7_s3_cRGB.png	\N	GREEN
6820	wF7_d3-1_cGFP	\N	\N
6821	original/PLATE-1/bPLATE_wF7_s3_cRGB.png	\N	RED
6822	wF7_d3-1_cCy5	\N	\N
6823	original/PLATE-1/bPLATE_wD12_s4_cRGB.png	\N	BLUE
6824	wD12_d1-2_cDAPI	\N	\N
6825	original/PLATE-1/bPLATE_wD12_s4_cRGB.png	\N	GREEN
6826	wD12_d1-2_cGFP	\N	\N
6827	original/PLATE-1/bPLATE_wD12_s4_cRGB.png	\N	RED
6828	wD12_d1-2_cCy5	\N	\N
6829	original/PLATE-1/bPLATE_wB5_s7_cRGB.png	\N	BLUE
6830	wB5_d1-3_cDAPI	\N	\N
6831	original/PLATE-1/bPLATE_wB5_s7_cRGB.png	\N	GREEN
6832	wB5_d1-3_cGFP	\N	\N
6833	original/PLATE-1/bPLATE_wB5_s7_cRGB.png	\N	RED
6834	wB5_d1-3_cCy5	\N	\N
6835	original/PLATE-1/bPLATE_wG7_s1_cRGB.png	\N	BLUE
6836	wG7_d1-1_cDAPI	\N	\N
6837	original/PLATE-1/bPLATE_wG7_s1_cRGB.png	\N	GREEN
6838	wG7_d1-1_cGFP	\N	\N
6839	original/PLATE-1/bPLATE_wG7_s1_cRGB.png	\N	RED
6840	wG7_d1-1_cCy5	\N	\N
6841	original/PLATE-1/bPLATE_wD12_s5_cRGB.png	\N	BLUE
6842	wD12_d2-2_cDAPI	\N	\N
6843	original/PLATE-1/bPLATE_wD12_s5_cRGB.png	\N	GREEN
6844	wD12_d2-2_cGFP	\N	\N
6845	original/PLATE-1/bPLATE_wD12_s5_cRGB.png	\N	RED
6846	wD12_d2-2_cCy5	\N	\N
6847	original/PLATE-1/bPLATE_wB5_s8_cRGB.png	\N	BLUE
6848	wB5_d2-3_cDAPI	\N	\N
6849	original/PLATE-1/bPLATE_wB5_s8_cRGB.png	\N	GREEN
6850	wB5_d2-3_cGFP	\N	\N
6851	original/PLATE-1/bPLATE_wB5_s8_cRGB.png	\N	RED
6852	wB5_d2-3_cCy5	\N	\N
6853	original/PLATE-1/bPLATE_wG7_s2_cRGB.png	\N	BLUE
6854	wG7_d2-1_cDAPI	\N	\N
6855	original/PLATE-1/bPLATE_wG7_s2_cRGB.png	\N	GREEN
6856	wG7_d2-1_cGFP	\N	\N
6857	original/PLATE-1/bPLATE_wG7_s2_cRGB.png	\N	RED
6858	wG7_d2-1_cCy5	\N	\N
6859	original/PLATE-1/bPLATE_wC2_s6_cRGB.png	\N	BLUE
6860	wC2_d3-2_cDAPI	\N	\N
6861	original/PLATE-1/bPLATE_wC2_s6_cRGB.png	\N	GREEN
6862	wC2_d3-2_cGFP	\N	\N
6863	original/PLATE-1/bPLATE_wC2_s6_cRGB.png	\N	RED
6864	wC2_d3-2_cCy5	\N	\N
6865	original/PLATE-1/bPLATE_wE9_s3_cRGB.png	\N	BLUE
6866	wE9_d3-1_cDAPI	\N	\N
6867	original/PLATE-1/bPLATE_wE9_s3_cRGB.png	\N	GREEN
6868	wE9_d3-1_cGFP	\N	\N
6869	original/PLATE-1/bPLATE_wE9_s3_cRGB.png	\N	RED
6870	wE9_d3-1_cCy5	\N	\N
6871	original/PLATE-1/bPLATE_wG7_s7_cRGB.png	\N	BLUE
6872	wG7_d1-3_cDAPI	\N	\N
6873	original/PLATE-1/bPLATE_wG7_s7_cRGB.png	\N	GREEN
6874	wG7_d1-3_cGFP	\N	\N
6875	original/PLATE-1/bPLATE_wG7_s7_cRGB.png	\N	RED
6876	wG7_d1-3_cCy5	\N	\N
6877	original/PLATE-1/bPLATE_wC10_s3_cRGB.png	\N	BLUE
6878	wC10_d3-1_cDAPI	\N	\N
6879	original/PLATE-1/bPLATE_wC10_s3_cRGB.png	\N	GREEN
6880	wC10_d3-1_cGFP	\N	\N
6881	original/PLATE-1/bPLATE_wC10_s3_cRGB.png	\N	RED
6882	wC10_d3-1_cCy5	\N	\N
6883	original/PLATE-1/bPLATE_wA3_s6_cRGB.png	\N	BLUE
6884	wA3_d3-2_cDAPI	\N	\N
6885	original/PLATE-1/bPLATE_wA3_s6_cRGB.png	\N	GREEN
6886	wA3_d3-2_cGFP	\N	\N
6887	original/PLATE-1/bPLATE_wA3_s6_cRGB.png	\N	RED
6888	wA3_d3-2_cCy5	\N	\N
6889	original/PLATE-1/bPLATE_wC8_s3_cRGB.png	\N	BLUE
6890	wC8_d3-1_cDAPI	\N	\N
6891	original/PLATE-1/bPLATE_wC8_s3_cRGB.png	\N	GREEN
6892	wC8_d3-1_cGFP	\N	\N
6893	original/PLATE-1/bPLATE_wC8_s3_cRGB.png	\N	RED
6894	wC8_d3-1_cCy5	\N	\N
6895	original/PLATE-1/bPLATE_wA1_s6_cRGB.png	\N	BLUE
6896	wA1_d3-2_cDAPI	\N	\N
6897	original/PLATE-1/bPLATE_wA1_s6_cRGB.png	\N	GREEN
6898	wA1_d3-2_cGFP	\N	\N
6899	original/PLATE-1/bPLATE_wA1_s6_cRGB.png	\N	RED
6900	wA1_d3-2_cCy5	\N	\N
6901	original/PLATE-1/bPLATE_wC11_s2_cRGB.png	\N	BLUE
6902	wC11_d2-1_cDAPI	\N	\N
6903	original/PLATE-1/bPLATE_wC11_s2_cRGB.png	\N	GREEN
6904	wC11_d2-1_cGFP	\N	\N
6905	original/PLATE-1/bPLATE_wC11_s2_cRGB.png	\N	RED
6906	wC11_d2-1_cCy5	\N	\N
6907	original/PLATE-1/bPLATE_wA4_s5_cRGB.png	\N	BLUE
6908	wA4_d2-2_cDAPI	\N	\N
6909	original/PLATE-1/bPLATE_wA4_s5_cRGB.png	\N	GREEN
6910	wA4_d2-2_cGFP	\N	\N
6911	original/PLATE-1/bPLATE_wA4_s5_cRGB.png	\N	RED
6912	wA4_d2-2_cCy5	\N	\N
6913	original/PLATE-1/bPLATE_wF6_s9_cRGB.png	\N	BLUE
6914	wF6_d3-3_cDAPI	\N	\N
6915	original/PLATE-1/bPLATE_wF6_s9_cRGB.png	\N	GREEN
6916	wF6_d3-3_cGFP	\N	\N
6917	original/PLATE-1/bPLATE_wF6_s9_cRGB.png	\N	RED
6918	wF6_d3-3_cCy5	\N	\N
6919	original/PLATE-1/bPLATE_wC4_s2_cRGB.png	\N	BLUE
6920	wC4_d2-1_cDAPI	\N	\N
6921	original/PLATE-1/bPLATE_wC4_s2_cRGB.png	\N	GREEN
6922	wC4_d2-1_cGFP	\N	\N
6923	original/PLATE-1/bPLATE_wC4_s2_cRGB.png	\N	RED
6924	wC4_d2-1_cCy5	\N	\N
6925	original/PLATE-1/bPLATE_wC8_s2_cRGB.png	\N	BLUE
6926	wC8_d2-1_cDAPI	\N	\N
6927	original/PLATE-1/bPLATE_wC8_s2_cRGB.png	\N	GREEN
6928	wC8_d2-1_cGFP	\N	\N
6929	original/PLATE-1/bPLATE_wC8_s2_cRGB.png	\N	RED
6930	wC8_d2-1_cCy5	\N	\N
6931	original/PLATE-1/bPLATE_wA1_s5_cRGB.png	\N	BLUE
6932	wA1_d2-2_cDAPI	\N	\N
6933	original/PLATE-1/bPLATE_wA1_s5_cRGB.png	\N	GREEN
6934	wA1_d2-2_cGFP	\N	\N
6935	original/PLATE-1/bPLATE_wA1_s5_cRGB.png	\N	RED
6936	wA1_d2-2_cCy5	\N	\N
6937	original/PLATE-1/bPLATE_wB8_s8_cRGB.png	\N	BLUE
6938	wB8_d2-3_cDAPI	\N	\N
6939	original/PLATE-1/bPLATE_wB8_s8_cRGB.png	\N	GREEN
6940	wB8_d2-3_cGFP	\N	\N
6941	original/PLATE-1/bPLATE_wB8_s8_cRGB.png	\N	RED
6942	wB8_d2-3_cCy5	\N	\N
6943	original/PLATE-1/bPLATE_wB1_s9_cRGB.png	\N	BLUE
6944	wB1_d3-3_cDAPI	\N	\N
6945	original/PLATE-1/bPLATE_wB1_s9_cRGB.png	\N	GREEN
6946	wB1_d3-3_cGFP	\N	\N
6947	original/PLATE-1/bPLATE_wB1_s9_cRGB.png	\N	RED
6948	wB1_d3-3_cCy5	\N	\N
6949	original/PLATE-1/bPLATE_wD8_s6_cRGB.png	\N	BLUE
6950	wD8_d3-2_cDAPI	\N	\N
6951	original/PLATE-1/bPLATE_wD8_s6_cRGB.png	\N	GREEN
6952	wD8_d3-2_cGFP	\N	\N
6953	original/PLATE-1/bPLATE_wD8_s6_cRGB.png	\N	RED
6954	wD8_d3-2_cCy5	\N	\N
6955	original/PLATE-1/bPLATE_wE3_s5_cRGB.png	\N	BLUE
6956	wE3_d2-2_cDAPI	\N	\N
6957	original/PLATE-1/bPLATE_wE3_s5_cRGB.png	\N	GREEN
6958	wE3_d2-2_cGFP	\N	\N
6959	original/PLATE-1/bPLATE_wE3_s5_cRGB.png	\N	RED
6960	wE3_d2-2_cCy5	\N	\N
6961	original/PLATE-1/bPLATE_wG10_s2_cRGB.png	\N	BLUE
6962	wG10_d2-1_cDAPI	\N	\N
6963	original/PLATE-1/bPLATE_wG10_s2_cRGB.png	\N	GREEN
6964	wG10_d2-1_cGFP	\N	\N
6965	original/PLATE-1/bPLATE_wG10_s2_cRGB.png	\N	RED
6966	wG10_d2-1_cCy5	\N	\N
6967	original/PLATE-1/bPLATE_wG3_s3_cRGB.png	\N	BLUE
6968	wG3_d3-1_cDAPI	\N	\N
6969	original/PLATE-1/bPLATE_wG3_s3_cRGB.png	\N	GREEN
6970	wG3_d3-1_cGFP	\N	\N
6971	original/PLATE-1/bPLATE_wG3_s3_cRGB.png	\N	RED
6972	wG3_d3-1_cCy5	\N	\N
6973	original/PLATE-1/bPLATE_wD9_s9_cRGB.png	\N	BLUE
6974	wD9_d3-3_cDAPI	\N	\N
6975	original/PLATE-1/bPLATE_wD9_s9_cRGB.png	\N	GREEN
6976	wD9_d3-3_cGFP	\N	\N
6977	original/PLATE-1/bPLATE_wD9_s9_cRGB.png	\N	RED
6978	wD9_d3-3_cCy5	\N	\N
6979	original/PLATE-1/bPLATE_wG10_s9_cRGB.png	\N	BLUE
6980	wG10_d3-3_cDAPI	\N	\N
6981	original/PLATE-1/bPLATE_wG10_s9_cRGB.png	\N	GREEN
6982	wG10_d3-3_cGFP	\N	\N
6983	original/PLATE-1/bPLATE_wG10_s9_cRGB.png	\N	RED
6984	wG10_d3-3_cCy5	\N	\N
6985	original/PLATE-1/bPLATE_wG4_s6_cRGB.png	\N	BLUE
6986	wG4_d3-2_cDAPI	\N	\N
6987	original/PLATE-1/bPLATE_wG4_s6_cRGB.png	\N	GREEN
6988	wG4_d3-2_cGFP	\N	\N
6989	original/PLATE-1/bPLATE_wG4_s6_cRGB.png	\N	RED
6990	wG4_d3-2_cCy5	\N	\N
6991	original/PLATE-1/bPLATE_wA10_s5_cRGB.png	\N	BLUE
6992	wA10_d2-2_cDAPI	\N	\N
6993	original/PLATE-1/bPLATE_wA10_s5_cRGB.png	\N	GREEN
6994	wA10_d2-2_cGFP	\N	\N
6995	original/PLATE-1/bPLATE_wA10_s5_cRGB.png	\N	RED
6996	wA10_d2-2_cCy5	\N	\N
6997	original/PLATE-1/bPLATE_wD5_s2_cRGB.png	\N	BLUE
6998	wD5_d2-1_cDAPI	\N	\N
6999	original/PLATE-1/bPLATE_wD5_s2_cRGB.png	\N	GREEN
7000	wD5_d2-1_cGFP	\N	\N
7001	original/PLATE-1/bPLATE_wD5_s2_cRGB.png	\N	RED
7002	wD5_d2-1_cCy5	\N	\N
7003	original/PLATE-1/bPLATE_wH1_s1_cRGB.png	\N	BLUE
7004	wH1_d1-1_cDAPI	\N	\N
7005	original/PLATE-1/bPLATE_wH1_s1_cRGB.png	\N	GREEN
7006	wH1_d1-1_cGFP	\N	\N
7007	original/PLATE-1/bPLATE_wH1_s1_cRGB.png	\N	RED
7008	wH1_d1-1_cCy5	\N	\N
7009	original/PLATE-1/bPLATE_wB11_s7_cRGB.png	\N	BLUE
7010	wB11_d1-3_cDAPI	\N	\N
7011	original/PLATE-1/bPLATE_wB11_s7_cRGB.png	\N	GREEN
7012	wB11_d1-3_cGFP	\N	\N
7013	original/PLATE-1/bPLATE_wB11_s7_cRGB.png	\N	RED
7014	wB11_d1-3_cCy5	\N	\N
7015	original/PLATE-1/bPLATE_wE6_s4_cRGB.png	\N	BLUE
7016	wE6_d1-2_cDAPI	\N	\N
7017	original/PLATE-1/bPLATE_wE6_s4_cRGB.png	\N	GREEN
7018	wE6_d1-2_cGFP	\N	\N
7019	original/PLATE-1/bPLATE_wE6_s4_cRGB.png	\N	RED
7020	wE6_d1-2_cCy5	\N	\N
7021	original/PLATE-1/bPLATE_wG8_s9_cRGB.png	\N	BLUE
7022	wG8_d3-3_cDAPI	\N	\N
7023	original/PLATE-1/bPLATE_wG8_s9_cRGB.png	\N	GREEN
7024	wG8_d3-3_cGFP	\N	\N
7025	original/PLATE-1/bPLATE_wG8_s9_cRGB.png	\N	RED
7026	wG8_d3-3_cCy5	\N	\N
7027	original/PLATE-1/bPLATE_wB6_s3_cRGB.png	\N	BLUE
7028	wB6_d3-1_cDAPI	\N	\N
7029	original/PLATE-1/bPLATE_wB6_s3_cRGB.png	\N	GREEN
7030	wB6_d3-1_cGFP	\N	\N
7031	original/PLATE-1/bPLATE_wB6_s3_cRGB.png	\N	RED
7032	wB6_d3-1_cCy5	\N	\N
7033	original/PLATE-1/bPLATE_wC6_s1_cRGB.png	\N	BLUE
7034	wC6_d1-1_cDAPI	\N	\N
7035	original/PLATE-1/bPLATE_wC6_s1_cRGB.png	\N	GREEN
7036	wC6_d1-1_cGFP	\N	\N
7037	original/PLATE-1/bPLATE_wC6_s1_cRGB.png	\N	RED
7038	wC6_d1-1_cCy5	\N	\N
7039	original/PLATE-1/bPLATE_wC4_s6_cRGB.png	\N	BLUE
7040	wC4_d3-2_cDAPI	\N	\N
7041	original/PLATE-1/bPLATE_wC4_s6_cRGB.png	\N	GREEN
7042	wC4_d3-2_cGFP	\N	\N
7043	original/PLATE-1/bPLATE_wC4_s6_cRGB.png	\N	RED
7044	wC4_d3-2_cCy5	\N	\N
7045	original/PLATE-1/bPLATE_wE11_s3_cRGB.png	\N	BLUE
7046	wE11_d3-1_cDAPI	\N	\N
7047	original/PLATE-1/bPLATE_wE11_s3_cRGB.png	\N	GREEN
7048	wE11_d3-1_cGFP	\N	\N
7049	original/PLATE-1/bPLATE_wE11_s3_cRGB.png	\N	RED
7050	wE11_d3-1_cCy5	\N	\N
7051	original/PLATE-1/bPLATE_wA7_s2_cRGB.png	\N	BLUE
7052	wA7_d2-1_cDAPI	\N	\N
7053	original/PLATE-1/bPLATE_wA7_s2_cRGB.png	\N	GREEN
7054	wA7_d2-1_cGFP	\N	\N
7055	original/PLATE-1/bPLATE_wA7_s2_cRGB.png	\N	RED
7056	wA7_d2-1_cCy5	\N	\N
7057	original/PLATE-1/bPLATE_wC12_s1_cRGB.png	\N	BLUE
7058	wC12_d1-1_cDAPI	\N	\N
7059	original/PLATE-1/bPLATE_wC12_s1_cRGB.png	\N	GREEN
7060	wC12_d1-1_cGFP	\N	\N
7061	original/PLATE-1/bPLATE_wC12_s1_cRGB.png	\N	RED
7062	wC12_d1-1_cCy5	\N	\N
7063	original/PLATE-1/bPLATE_wA5_s4_cRGB.png	\N	BLUE
7064	wA5_d1-2_cDAPI	\N	\N
7065	original/PLATE-1/bPLATE_wA5_s4_cRGB.png	\N	GREEN
7066	wA5_d1-2_cGFP	\N	\N
7067	original/PLATE-1/bPLATE_wA5_s4_cRGB.png	\N	RED
7068	wA5_d1-2_cCy5	\N	\N
7069	original/PLATE-1/bPLATE_wH3_s7_cRGB.png	\N	BLUE
7070	wH3_d1-3_cDAPI	\N	\N
7071	original/PLATE-1/bPLATE_wH3_s7_cRGB.png	\N	GREEN
7072	wH3_d1-3_cGFP	\N	\N
7073	original/PLATE-1/bPLATE_wH3_s7_cRGB.png	\N	RED
7074	wH3_d1-3_cCy5	\N	\N
7075	original/PLATE-1/bPLATE_wH11_s8_cRGB.png	\N	BLUE
7076	wH11_d2-3_cDAPI	\N	\N
7077	original/PLATE-1/bPLATE_wH11_s8_cRGB.png	\N	GREEN
7078	wH11_d2-3_cGFP	\N	\N
7079	original/PLATE-1/bPLATE_wH11_s8_cRGB.png	\N	RED
7080	wH11_d2-3_cCy5	\N	\N
7081	original/PLATE-1/bPLATE_wD11_s2_cRGB.png	\N	BLUE
7082	wD11_d2-1_cDAPI	\N	\N
7083	original/PLATE-1/bPLATE_wD11_s2_cRGB.png	\N	GREEN
7084	wD11_d2-1_cGFP	\N	\N
7085	original/PLATE-1/bPLATE_wD11_s2_cRGB.png	\N	RED
7086	wD11_d2-1_cCy5	\N	\N
7087	original/PLATE-1/bPLATE_wC5_s4_cRGB.png	\N	BLUE
7088	wC5_d1-2_cDAPI	\N	\N
7089	original/PLATE-1/bPLATE_wC5_s4_cRGB.png	\N	GREEN
7090	wC5_d1-2_cGFP	\N	\N
7091	original/PLATE-1/bPLATE_wC5_s4_cRGB.png	\N	RED
7092	wC5_d1-2_cCy5	\N	\N
7093	original/PLATE-1/bPLATE_wB4_s5_cRGB.png	\N	BLUE
7094	wB4_d2-2_cDAPI	\N	\N
7095	original/PLATE-1/bPLATE_wB4_s5_cRGB.png	\N	GREEN
7096	wB4_d2-2_cGFP	\N	\N
7097	original/PLATE-1/bPLATE_wB4_s5_cRGB.png	\N	RED
7098	wB4_d2-2_cCy5	\N	\N
7099	original/PLATE-1/bPLATE_wE12_s1_cRGB.png	\N	BLUE
7100	wE12_d1-1_cDAPI	\N	\N
7101	original/PLATE-1/bPLATE_wE12_s1_cRGB.png	\N	GREEN
7102	wE12_d1-1_cGFP	\N	\N
7103	original/PLATE-1/bPLATE_wE12_s1_cRGB.png	\N	RED
7104	wE12_d1-1_cCy5	\N	\N
7105	original/PLATE-1/bPLATE_wE1_s9_cRGB.png	\N	BLUE
7106	wE1_d3-3_cDAPI	\N	\N
7107	original/PLATE-1/bPLATE_wE1_s9_cRGB.png	\N	GREEN
7108	wE1_d3-3_cGFP	\N	\N
7109	original/PLATE-1/bPLATE_wE1_s9_cRGB.png	\N	RED
7110	wE1_d3-3_cCy5	\N	\N
7111	original/PLATE-1/bPLATE_wG8_s6_cRGB.png	\N	BLUE
7112	wG8_d3-2_cDAPI	\N	\N
7113	original/PLATE-1/bPLATE_wG8_s6_cRGB.png	\N	GREEN
7114	wG8_d3-2_cGFP	\N	\N
7115	original/PLATE-1/bPLATE_wG8_s6_cRGB.png	\N	RED
7116	wG8_d3-2_cCy5	\N	\N
7117	original/PLATE-1/bPLATE_wH4_s6_cRGB.png	\N	BLUE
7118	wH4_d3-2_cDAPI	\N	\N
7119	original/PLATE-1/bPLATE_wH4_s6_cRGB.png	\N	GREEN
7120	wH4_d3-2_cGFP	\N	\N
7121	original/PLATE-1/bPLATE_wH4_s6_cRGB.png	\N	RED
7122	wH4_d3-2_cCy5	\N	\N
7123	original/PLATE-1/bPLATE_wE9_s9_cRGB.png	\N	BLUE
7124	wE9_d3-3_cDAPI	\N	\N
7125	original/PLATE-1/bPLATE_wE9_s9_cRGB.png	\N	GREEN
7126	wE9_d3-3_cGFP	\N	\N
7127	original/PLATE-1/bPLATE_wE9_s9_cRGB.png	\N	RED
7128	wE9_d3-3_cCy5	\N	\N
7129	original/PLATE-1/bPLATE_wH12_s3_cRGB.png	\N	BLUE
7130	wH12_d3-1_cDAPI	\N	\N
7131	original/PLATE-1/bPLATE_wH12_s3_cRGB.png	\N	GREEN
7132	wH12_d3-1_cGFP	\N	\N
7133	original/PLATE-1/bPLATE_wH12_s3_cRGB.png	\N	RED
7134	wH12_d3-1_cCy5	\N	\N
7135	original/PLATE-1/bPLATE_wC10_s9_cRGB.png	\N	BLUE
7136	wC10_d3-3_cDAPI	\N	\N
7137	original/PLATE-1/bPLATE_wC10_s9_cRGB.png	\N	GREEN
7138	wC10_d3-3_cGFP	\N	\N
7139	original/PLATE-1/bPLATE_wC10_s9_cRGB.png	\N	RED
7140	wC10_d3-3_cCy5	\N	\N
7141	original/PLATE-1/bPLATE_wF5_s6_cRGB.png	\N	BLUE
7142	wF5_d3-2_cDAPI	\N	\N
7143	original/PLATE-1/bPLATE_wF5_s6_cRGB.png	\N	GREEN
7144	wF5_d3-2_cGFP	\N	\N
7145	original/PLATE-1/bPLATE_wF5_s6_cRGB.png	\N	RED
7146	wF5_d3-2_cCy5	\N	\N
7147	original/PLATE-1/bPLATE_wD3_s5_cRGB.png	\N	BLUE
7148	wD3_d2-2_cDAPI	\N	\N
7149	original/PLATE-1/bPLATE_wD3_s5_cRGB.png	\N	GREEN
7150	wD3_d2-2_cGFP	\N	\N
7151	original/PLATE-1/bPLATE_wD3_s5_cRGB.png	\N	RED
7152	wD3_d2-2_cCy5	\N	\N
7153	original/PLATE-1/bPLATE_wA8_s8_cRGB.png	\N	BLUE
7154	wA8_d2-3_cDAPI	\N	\N
7155	original/PLATE-1/bPLATE_wA8_s8_cRGB.png	\N	GREEN
7156	wA8_d2-3_cGFP	\N	\N
7157	original/PLATE-1/bPLATE_wA8_s8_cRGB.png	\N	RED
7158	wA8_d2-3_cCy5	\N	\N
7159	original/PLATE-1/bPLATE_wF10_s2_cRGB.png	\N	BLUE
7160	wF10_d2-1_cDAPI	\N	\N
7161	original/PLATE-1/bPLATE_wF10_s2_cRGB.png	\N	GREEN
7162	wF10_d2-1_cGFP	\N	\N
7163	original/PLATE-1/bPLATE_wF10_s2_cRGB.png	\N	RED
7164	wF10_d2-1_cCy5	\N	\N
7165	original/PLATE-1/bPLATE_wC6_s2_cRGB.png	\N	BLUE
7166	wC6_d2-1_cDAPI	\N	\N
7167	original/PLATE-1/bPLATE_wC6_s2_cRGB.png	\N	GREEN
7168	wC6_d2-1_cGFP	\N	\N
7169	original/PLATE-1/bPLATE_wC6_s2_cRGB.png	\N	RED
7170	wC6_d2-1_cCy5	\N	\N
7171	original/PLATE-1/bPLATE_wB2_s5_cRGB.png	\N	BLUE
7172	wB2_d2-2_cDAPI	\N	\N
7173	original/PLATE-1/bPLATE_wB2_s5_cRGB.png	\N	GREEN
7174	wB2_d2-2_cGFP	\N	\N
7175	original/PLATE-1/bPLATE_wB2_s5_cRGB.png	\N	RED
7176	wB2_d2-2_cCy5	\N	\N
7177	original/PLATE-1/bPLATE_wD9_s2_cRGB.png	\N	BLUE
7178	wD9_d2-1_cDAPI	\N	\N
7179	original/PLATE-1/bPLATE_wD9_s2_cRGB.png	\N	GREEN
7180	wD9_d2-1_cGFP	\N	\N
7181	original/PLATE-1/bPLATE_wD9_s2_cRGB.png	\N	RED
7182	wD9_d2-1_cCy5	\N	\N
7183	original/PLATE-1/bPLATE_wB9_s1_cRGB.png	\N	BLUE
7184	wB9_d1-1_cDAPI	\N	\N
7185	original/PLATE-1/bPLATE_wB9_s1_cRGB.png	\N	GREEN
7186	wB9_d1-1_cGFP	\N	\N
7187	original/PLATE-1/bPLATE_wB9_s1_cRGB.png	\N	RED
7188	wB9_d1-1_cCy5	\N	\N
7189	original/PLATE-1/bPLATE_wH2_s6_cRGB.png	\N	BLUE
7190	wH2_d3-2_cDAPI	\N	\N
7191	original/PLATE-1/bPLATE_wH2_s6_cRGB.png	\N	GREEN
7192	wH2_d3-2_cGFP	\N	\N
7193	original/PLATE-1/bPLATE_wH2_s6_cRGB.png	\N	RED
7194	wH2_d3-2_cCy5	\N	\N
7195	original/PLATE-1/bPLATE_wC9_s2_cRGB.png	\N	BLUE
7196	wC9_d2-1_cDAPI	\N	\N
7197	original/PLATE-1/bPLATE_wC9_s2_cRGB.png	\N	GREEN
7198	wC9_d2-1_cGFP	\N	\N
7199	original/PLATE-1/bPLATE_wC9_s2_cRGB.png	\N	RED
7200	wC9_d2-1_cCy5	\N	\N
7201	original/PLATE-1/bPLATE_wA2_s5_cRGB.png	\N	BLUE
7202	wA2_d2-2_cDAPI	\N	\N
7203	original/PLATE-1/bPLATE_wA2_s5_cRGB.png	\N	GREEN
7204	wA2_d2-2_cGFP	\N	\N
7205	original/PLATE-1/bPLATE_wA2_s5_cRGB.png	\N	RED
7206	wA2_d2-2_cCy5	\N	\N
7207	original/PLATE-1/bPLATE_wB5_s3_cRGB.png	\N	BLUE
7208	wB5_d3-1_cDAPI	\N	\N
7209	original/PLATE-1/bPLATE_wB5_s3_cRGB.png	\N	GREEN
7210	wB5_d3-1_cGFP	\N	\N
7211	original/PLATE-1/bPLATE_wB5_s3_cRGB.png	\N	RED
7212	wB5_d3-1_cCy5	\N	\N
7213	original/PLATE-1/bPLATE_wE7_s9_cRGB.png	\N	BLUE
7214	wE7_d3-3_cDAPI	\N	\N
7215	original/PLATE-1/bPLATE_wE7_s9_cRGB.png	\N	GREEN
7216	wE7_d3-3_cGFP	\N	\N
7217	original/PLATE-1/bPLATE_wE7_s9_cRGB.png	\N	RED
7218	wE7_d3-3_cCy5	\N	\N
7219	original/PLATE-1/bPLATE_wC3_s6_cRGB.png	\N	BLUE
7220	wC3_d3-2_cDAPI	\N	\N
7221	original/PLATE-1/bPLATE_wC3_s6_cRGB.png	\N	GREEN
7222	wC3_d3-2_cGFP	\N	\N
7223	original/PLATE-1/bPLATE_wC3_s6_cRGB.png	\N	RED
7224	wC3_d3-2_cCy5	\N	\N
7225	original/PLATE-1/bPLATE_wE10_s3_cRGB.png	\N	BLUE
7226	wE10_d3-1_cDAPI	\N	\N
7227	original/PLATE-1/bPLATE_wE10_s3_cRGB.png	\N	GREEN
7228	wE10_d3-1_cGFP	\N	\N
7229	original/PLATE-1/bPLATE_wE10_s3_cRGB.png	\N	RED
7230	wE10_d3-1_cCy5	\N	\N
7231	original/PLATE-1/bPLATE_wE5_s8_cRGB.png	\N	BLUE
7232	wE5_d2-3_cDAPI	\N	\N
7233	original/PLATE-1/bPLATE_wE5_s8_cRGB.png	\N	GREEN
7234	wE5_d2-3_cGFP	\N	\N
7235	original/PLATE-1/bPLATE_wE5_s8_cRGB.png	\N	RED
7236	wE5_d2-3_cCy5	\N	\N
7237	original/PLATE-1/bPLATE_wG12_s5_cRGB.png	\N	BLUE
7238	wG12_d2-2_cDAPI	\N	\N
7239	original/PLATE-1/bPLATE_wG12_s5_cRGB.png	\N	GREEN
7240	wG12_d2-2_cGFP	\N	\N
7241	original/PLATE-1/bPLATE_wG12_s5_cRGB.png	\N	RED
7242	wG12_d2-2_cCy5	\N	\N
7243	original/PLATE-1/bPLATE_wC8_s9_cRGB.png	\N	BLUE
7244	wC8_d3-3_cDAPI	\N	\N
7245	original/PLATE-1/bPLATE_wC8_s9_cRGB.png	\N	GREEN
7246	wC8_d3-3_cGFP	\N	\N
7247	original/PLATE-1/bPLATE_wC8_s9_cRGB.png	\N	RED
7248	wC8_d3-3_cCy5	\N	\N
7249	original/PLATE-1/bPLATE_wF3_s6_cRGB.png	\N	BLUE
7250	wF3_d3-2_cDAPI	\N	\N
7251	original/PLATE-1/bPLATE_wF3_s6_cRGB.png	\N	GREEN
7252	wF3_d3-2_cGFP	\N	\N
7253	original/PLATE-1/bPLATE_wF3_s6_cRGB.png	\N	RED
7254	wF3_d3-2_cCy5	\N	\N
7255	original/PLATE-1/bPLATE_wH10_s3_cRGB.png	\N	BLUE
7256	wH10_d3-1_cDAPI	\N	\N
7257	original/PLATE-1/bPLATE_wH10_s3_cRGB.png	\N	GREEN
7258	wH10_d3-1_cGFP	\N	\N
7259	original/PLATE-1/bPLATE_wH10_s3_cRGB.png	\N	RED
7260	wH10_d3-1_cCy5	\N	\N
7261	original/PLATE-1/bPLATE_wC10_s4_cRGB.png	\N	BLUE
7262	wC10_d1-2_cDAPI	\N	\N
7263	original/PLATE-1/bPLATE_wC10_s4_cRGB.png	\N	GREEN
7264	wC10_d1-2_cGFP	\N	\N
7265	original/PLATE-1/bPLATE_wC10_s4_cRGB.png	\N	RED
7266	wC10_d1-2_cCy5	\N	\N
7267	original/PLATE-1/bPLATE_wA3_s7_cRGB.png	\N	BLUE
7268	wA3_d1-3_cDAPI	\N	\N
7269	original/PLATE-1/bPLATE_wA3_s7_cRGB.png	\N	GREEN
7270	wA3_d1-3_cGFP	\N	\N
7271	original/PLATE-1/bPLATE_wA3_s7_cRGB.png	\N	RED
7272	wA3_d1-3_cCy5	\N	\N
7273	original/PLATE-1/bPLATE_wE3_s9_cRGB.png	\N	BLUE
7274	wE3_d3-3_cDAPI	\N	\N
7275	original/PLATE-1/bPLATE_wE3_s9_cRGB.png	\N	GREEN
7276	wE3_d3-3_cGFP	\N	\N
7277	original/PLATE-1/bPLATE_wE3_s9_cRGB.png	\N	RED
7278	wE3_d3-3_cCy5	\N	\N
7279	original/PLATE-1/bPLATE_wF5_s1_cRGB.png	\N	BLUE
7280	wF5_d1-1_cDAPI	\N	\N
7281	original/PLATE-1/bPLATE_wF5_s1_cRGB.png	\N	GREEN
7282	wF5_d1-1_cGFP	\N	\N
7283	original/PLATE-1/bPLATE_wF5_s1_cRGB.png	\N	RED
7284	wF5_d1-1_cCy5	\N	\N
7285	original/PLATE-1/bPLATE_wG10_s6_cRGB.png	\N	BLUE
7286	wG10_d3-2_cDAPI	\N	\N
7287	original/PLATE-1/bPLATE_wG10_s6_cRGB.png	\N	GREEN
7288	wG10_d3-2_cGFP	\N	\N
7289	original/PLATE-1/bPLATE_wG10_s6_cRGB.png	\N	RED
7290	wG10_d3-2_cCy5	\N	\N
7291	original/PLATE-1/bPLATE_wB1_s4_cRGB.png	\N	BLUE
7292	wB1_d1-2_cDAPI	\N	\N
7293	original/PLATE-1/bPLATE_wB1_s4_cRGB.png	\N	GREEN
7294	wB1_d1-2_cGFP	\N	\N
7295	original/PLATE-1/bPLATE_wB1_s4_cRGB.png	\N	RED
7296	wB1_d1-2_cCy5	\N	\N
7297	original/PLATE-1/bPLATE_wD8_s1_cRGB.png	\N	BLUE
7298	wD8_d1-1_cDAPI	\N	\N
7299	original/PLATE-1/bPLATE_wD8_s1_cRGB.png	\N	GREEN
7300	wD8_d1-1_cGFP	\N	\N
7301	original/PLATE-1/bPLATE_wD8_s1_cRGB.png	\N	RED
7302	wD8_d1-1_cCy5	\N	\N
7303	original/PLATE-1/bPLATE_wH2_s4_cRGB.png	\N	BLUE
7304	wH2_d1-2_cDAPI	\N	\N
7305	original/PLATE-1/bPLATE_wH2_s4_cRGB.png	\N	GREEN
7306	wH2_d1-2_cGFP	\N	\N
7307	original/PLATE-1/bPLATE_wH2_s4_cRGB.png	\N	RED
7308	wH2_d1-2_cCy5	\N	\N
7309	original/PLATE-1/bPLATE_wE7_s7_cRGB.png	\N	BLUE
7310	wE7_d1-3_cDAPI	\N	\N
7311	original/PLATE-1/bPLATE_wE7_s7_cRGB.png	\N	GREEN
7312	wE7_d1-3_cGFP	\N	\N
7313	original/PLATE-1/bPLATE_wE7_s7_cRGB.png	\N	RED
7314	wE7_d1-3_cCy5	\N	\N
7315	original/PLATE-1/bPLATE_wD12_s9_cRGB.png	\N	BLUE
7316	wD12_d3-3_cDAPI	\N	\N
7317	original/PLATE-1/bPLATE_wD12_s9_cRGB.png	\N	GREEN
7318	wD12_d3-3_cGFP	\N	\N
7319	original/PLATE-1/bPLATE_wD12_s9_cRGB.png	\N	RED
7320	wD12_d3-3_cCy5	\N	\N
7321	original/PLATE-1/bPLATE_wD10_s8_cRGB.png	\N	BLUE
7322	wD10_d2-3_cDAPI	\N	\N
7323	original/PLATE-1/bPLATE_wD10_s8_cRGB.png	\N	GREEN
7324	wD10_d2-3_cGFP	\N	\N
7325	original/PLATE-1/bPLATE_wD10_s8_cRGB.png	\N	RED
7326	wD10_d2-3_cCy5	\N	\N
7327	original/PLATE-1/bPLATE_wG5_s5_cRGB.png	\N	BLUE
7328	wG5_d2-2_cDAPI	\N	\N
7329	original/PLATE-1/bPLATE_wG5_s5_cRGB.png	\N	GREEN
7330	wG5_d2-2_cGFP	\N	\N
7331	original/PLATE-1/bPLATE_wG5_s5_cRGB.png	\N	RED
7332	wG5_d2-2_cCy5	\N	\N
7333	original/PLATE-1/bPLATE_wG7_s6_cRGB.png	\N	BLUE
7334	wG7_d3-2_cDAPI	\N	\N
7335	original/PLATE-1/bPLATE_wG7_s6_cRGB.png	\N	GREEN
7336	wG7_d3-2_cGFP	\N	\N
7337	original/PLATE-1/bPLATE_wG7_s6_cRGB.png	\N	RED
7338	wG7_d3-2_cCy5	\N	\N
7339	original/PLATE-1/bPLATE_wH12_s7_cRGB.png	\N	BLUE
7340	wH12_d1-3_cDAPI	\N	\N
7341	original/PLATE-1/bPLATE_wH12_s7_cRGB.png	\N	GREEN
7342	wH12_d1-3_cGFP	\N	\N
7343	original/PLATE-1/bPLATE_wH12_s7_cRGB.png	\N	RED
7344	wH12_d1-3_cCy5	\N	\N
7345	original/PLATE-1/bPLATE_wD5_s9_cRGB.png	\N	BLUE
7346	wD5_d3-3_cDAPI	\N	\N
7347	original/PLATE-1/bPLATE_wD5_s9_cRGB.png	\N	GREEN
7348	wD5_d3-3_cGFP	\N	\N
7349	original/PLATE-1/bPLATE_wD5_s9_cRGB.png	\N	RED
7350	wD5_d3-3_cCy5	\N	\N
7351	original/PLATE-1/bPLATE_wF12_s6_cRGB.png	\N	BLUE
7352	wF12_d3-2_cDAPI	\N	\N
7353	original/PLATE-1/bPLATE_wF12_s6_cRGB.png	\N	GREEN
7354	wF12_d3-2_cGFP	\N	\N
7355	original/PLATE-1/bPLATE_wF12_s6_cRGB.png	\N	RED
7356	wF12_d3-2_cCy5	\N	\N
7357	original/PLATE-1/bPLATE_wH5_s4_cRGB.png	\N	BLUE
7358	wH5_d1-2_cDAPI	\N	\N
7359	original/PLATE-1/bPLATE_wH5_s4_cRGB.png	\N	GREEN
7360	wH5_d1-2_cGFP	\N	\N
7361	original/PLATE-1/bPLATE_wH5_s4_cRGB.png	\N	RED
7362	wH5_d1-2_cCy5	\N	\N
7363	original/PLATE-1/bPLATE_wE10_s7_cRGB.png	\N	BLUE
7364	wE10_d1-3_cDAPI	\N	\N
7365	original/PLATE-1/bPLATE_wE10_s7_cRGB.png	\N	GREEN
7366	wE10_d1-3_cGFP	\N	\N
7367	original/PLATE-1/bPLATE_wE10_s7_cRGB.png	\N	RED
7368	wE10_d1-3_cCy5	\N	\N
7369	original/PLATE-1/bPLATE_wG7_s8_cRGB.png	\N	BLUE
7370	wG7_d2-3_cDAPI	\N	\N
7371	original/PLATE-1/bPLATE_wG7_s8_cRGB.png	\N	GREEN
7372	wG7_d2-3_cGFP	\N	\N
7373	original/PLATE-1/bPLATE_wG7_s8_cRGB.png	\N	RED
7374	wG7_d2-3_cCy5	\N	\N
7375	original/PLATE-1/bPLATE_wE1_s7_cRGB.png	\N	BLUE
7376	wE1_d1-3_cDAPI	\N	\N
7377	original/PLATE-1/bPLATE_wE1_s7_cRGB.png	\N	GREEN
7378	wE1_d1-3_cGFP	\N	\N
7379	original/PLATE-1/bPLATE_wE1_s7_cRGB.png	\N	RED
7380	wE1_d1-3_cCy5	\N	\N
7381	original/PLATE-1/bPLATE_wG8_s4_cRGB.png	\N	BLUE
7382	wG8_d1-2_cDAPI	\N	\N
7383	original/PLATE-1/bPLATE_wG8_s4_cRGB.png	\N	GREEN
7384	wG8_d1-2_cGFP	\N	\N
7385	original/PLATE-1/bPLATE_wG8_s4_cRGB.png	\N	RED
7386	wG8_d1-2_cCy5	\N	\N
7387	original/PLATE-1/bPLATE_wB1_s3_cRGB.png	\N	BLUE
7388	wB1_d3-1_cDAPI	\N	\N
7389	original/PLATE-1/bPLATE_wB1_s3_cRGB.png	\N	GREEN
7390	wB1_d3-1_cGFP	\N	\N
7391	original/PLATE-1/bPLATE_wB1_s3_cRGB.png	\N	RED
7392	wB1_d3-1_cCy5	\N	\N
7393	original/PLATE-1/bPLATE_wH2_s9_cRGB.png	\N	BLUE
7394	wH2_d3-3_cDAPI	\N	\N
7395	original/PLATE-1/bPLATE_wH2_s9_cRGB.png	\N	GREEN
7396	wH2_d3-3_cGFP	\N	\N
7397	original/PLATE-1/bPLATE_wH2_s9_cRGB.png	\N	RED
7398	wH2_d3-3_cCy5	\N	\N
7399	original/PLATE-1/bPLATE_wA12_s7_cRGB.png	\N	BLUE
7400	wA12_d1-3_cDAPI	\N	\N
7401	original/PLATE-1/bPLATE_wA12_s7_cRGB.png	\N	GREEN
7402	wA12_d1-3_cGFP	\N	\N
7403	original/PLATE-1/bPLATE_wA12_s7_cRGB.png	\N	RED
7404	wA12_d1-3_cCy5	\N	\N
7405	original/PLATE-1/bPLATE_wD7_s4_cRGB.png	\N	BLUE
7406	wD7_d1-2_cDAPI	\N	\N
7407	original/PLATE-1/bPLATE_wD7_s4_cRGB.png	\N	GREEN
7408	wD7_d1-2_cGFP	\N	\N
7409	original/PLATE-1/bPLATE_wD7_s4_cRGB.png	\N	RED
7410	wD7_d1-2_cCy5	\N	\N
7411	original/PLATE-1/bPLATE_wG2_s1_cRGB.png	\N	BLUE
7412	wG2_d1-1_cDAPI	\N	\N
7413	original/PLATE-1/bPLATE_wG2_s1_cRGB.png	\N	GREEN
7414	wG2_d1-1_cGFP	\N	\N
7415	original/PLATE-1/bPLATE_wG2_s1_cRGB.png	\N	RED
7416	wG2_d1-1_cCy5	\N	\N
7417	original/PLATE-1/bPLATE_wC12_s8_cRGB.png	\N	BLUE
7418	wC12_d2-3_cDAPI	\N	\N
7419	original/PLATE-1/bPLATE_wC12_s8_cRGB.png	\N	GREEN
7420	wC12_d2-3_cGFP	\N	\N
7421	original/PLATE-1/bPLATE_wC12_s8_cRGB.png	\N	RED
7422	wC12_d2-3_cCy5	\N	\N
7423	original/PLATE-1/bPLATE_wF7_s5_cRGB.png	\N	BLUE
7424	wF7_d2-2_cDAPI	\N	\N
7425	original/PLATE-1/bPLATE_wF7_s5_cRGB.png	\N	GREEN
7426	wF7_d2-2_cGFP	\N	\N
7427	original/PLATE-1/bPLATE_wF7_s5_cRGB.png	\N	RED
7428	wF7_d2-2_cCy5	\N	\N
7429	original/PLATE-1/bPLATE_wA2_s2_cRGB.png	\N	BLUE
7430	wA2_d2-1_cDAPI	\N	\N
7431	original/PLATE-1/bPLATE_wA2_s2_cRGB.png	\N	GREEN
7432	wA2_d2-1_cGFP	\N	\N
7433	original/PLATE-1/bPLATE_wA2_s2_cRGB.png	\N	RED
7434	wA2_d2-1_cCy5	\N	\N
7435	original/PLATE-1/bPLATE_wA9_s3_cRGB.png	\N	BLUE
7436	wA9_d3-1_cDAPI	\N	\N
7437	original/PLATE-1/bPLATE_wA9_s3_cRGB.png	\N	GREEN
7438	wA9_d3-1_cGFP	\N	\N
7439	original/PLATE-1/bPLATE_wA9_s3_cRGB.png	\N	RED
7440	wA9_d3-1_cCy5	\N	\N
7441	original/PLATE-1/bPLATE_wD12_s1_cRGB.png	\N	BLUE
7442	wD12_d1-1_cDAPI	\N	\N
7443	original/PLATE-1/bPLATE_wD12_s1_cRGB.png	\N	GREEN
7444	wD12_d1-1_cGFP	\N	\N
7445	original/PLATE-1/bPLATE_wD12_s1_cRGB.png	\N	RED
7446	wD12_d1-1_cCy5	\N	\N
7447	original/PLATE-1/bPLATE_wB5_s4_cRGB.png	\N	BLUE
7448	wB5_d1-2_cDAPI	\N	\N
7449	original/PLATE-1/bPLATE_wB5_s4_cRGB.png	\N	GREEN
7450	wB5_d1-2_cGFP	\N	\N
7451	original/PLATE-1/bPLATE_wB5_s4_cRGB.png	\N	RED
7452	wB5_d1-2_cCy5	\N	\N
7453	original/PLATE-1/bPLATE_wC12_s4_cRGB.png	\N	BLUE
7454	wC12_d1-2_cDAPI	\N	\N
7455	original/PLATE-1/bPLATE_wC12_s4_cRGB.png	\N	GREEN
7456	wC12_d1-2_cGFP	\N	\N
7457	original/PLATE-1/bPLATE_wC12_s4_cRGB.png	\N	RED
7458	wC12_d1-2_cCy5	\N	\N
7459	original/PLATE-1/bPLATE_wA5_s7_cRGB.png	\N	BLUE
7460	wA5_d1-3_cDAPI	\N	\N
7461	original/PLATE-1/bPLATE_wA5_s7_cRGB.png	\N	GREEN
7462	wA5_d1-3_cGFP	\N	\N
7463	original/PLATE-1/bPLATE_wA5_s7_cRGB.png	\N	RED
7464	wA5_d1-3_cCy5	\N	\N
7465	original/PLATE-1/bPLATE_wF7_s1_cRGB.png	\N	BLUE
7466	wF7_d1-1_cDAPI	\N	\N
7467	original/PLATE-1/bPLATE_wF7_s1_cRGB.png	\N	GREEN
7468	wF7_d1-1_cGFP	\N	\N
7469	original/PLATE-1/bPLATE_wF7_s1_cRGB.png	\N	RED
7470	wF7_d1-1_cCy5	\N	\N
7471	original/PLATE-1/bPLATE_wC2_s3_cRGB.png	\N	BLUE
7472	wC2_d3-1_cDAPI	\N	\N
7473	original/PLATE-1/bPLATE_wC2_s3_cRGB.png	\N	GREEN
7474	wC2_d3-1_cGFP	\N	\N
7475	original/PLATE-1/bPLATE_wC2_s3_cRGB.png	\N	RED
7476	wC2_d3-1_cCy5	\N	\N
7477	original/PLATE-1/bPLATE_wB3_s3_cRGB.png	\N	BLUE
7478	wB3_d3-1_cDAPI	\N	\N
7479	original/PLATE-1/bPLATE_wB3_s3_cRGB.png	\N	GREEN
7480	wB3_d3-1_cGFP	\N	\N
7481	original/PLATE-1/bPLATE_wB3_s3_cRGB.png	\N	RED
7482	wB3_d3-1_cCy5	\N	\N
7483	original/PLATE-1/bPLATE_wC11_s7_cRGB.png	\N	BLUE
7484	wC11_d1-3_cDAPI	\N	\N
7485	original/PLATE-1/bPLATE_wC11_s7_cRGB.png	\N	GREEN
7486	wC11_d1-3_cGFP	\N	\N
7487	original/PLATE-1/bPLATE_wC11_s7_cRGB.png	\N	RED
7488	wC11_d1-3_cCy5	\N	\N
7489	original/PLATE-1/bPLATE_wF6_s4_cRGB.png	\N	BLUE
7490	wF6_d1-2_cDAPI	\N	\N
7491	original/PLATE-1/bPLATE_wF6_s4_cRGB.png	\N	GREEN
7492	wF6_d1-2_cGFP	\N	\N
7493	original/PLATE-1/bPLATE_wF6_s4_cRGB.png	\N	RED
7494	wF6_d1-2_cCy5	\N	\N
7495	original/PLATE-1/bPLATE_wH6_s7_cRGB.png	\N	BLUE
7496	wH6_d1-3_cDAPI	\N	\N
7497	original/PLATE-1/bPLATE_wH6_s7_cRGB.png	\N	GREEN
7498	wH6_d1-3_cGFP	\N	\N
7499	original/PLATE-1/bPLATE_wH6_s7_cRGB.png	\N	RED
7500	wH6_d1-3_cCy5	\N	\N
7501	original/PLATE-1/bPLATE_wA3_s1_cRGB.png	\N	BLUE
7502	wA3_d1-1_cDAPI	\N	\N
7503	original/PLATE-1/bPLATE_wA3_s1_cRGB.png	\N	GREEN
7504	wA3_d1-1_cGFP	\N	\N
7505	original/PLATE-1/bPLATE_wA3_s1_cRGB.png	\N	RED
7506	wA3_d1-1_cCy5	\N	\N
7507	original/PLATE-1/bPLATE_wH8_s4_cRGB.png	\N	BLUE
7508	wH8_d1-2_cDAPI	\N	\N
7509	original/PLATE-1/bPLATE_wH8_s4_cRGB.png	\N	GREEN
7510	wH8_d1-2_cGFP	\N	\N
7511	original/PLATE-1/bPLATE_wH8_s4_cRGB.png	\N	RED
7512	wH8_d1-2_cCy5	\N	\N
7513	original/PLATE-1/bPLATE_wF1_s7_cRGB.png	\N	BLUE
7514	wF1_d1-3_cDAPI	\N	\N
7515	original/PLATE-1/bPLATE_wF1_s7_cRGB.png	\N	GREEN
7516	wF1_d1-3_cGFP	\N	\N
7517	original/PLATE-1/bPLATE_wF1_s7_cRGB.png	\N	RED
7518	wF1_d1-3_cCy5	\N	\N
7519	original/PLATE-1/bPLATE_wD3_s1_cRGB.png	\N	BLUE
7520	wD3_d1-1_cDAPI	\N	\N
7521	original/PLATE-1/bPLATE_wD3_s1_cRGB.png	\N	GREEN
7522	wD3_d1-1_cGFP	\N	\N
7523	original/PLATE-1/bPLATE_wD3_s1_cRGB.png	\N	RED
7524	wD3_d1-1_cCy5	\N	\N
7525	original/PLATE-1/bPLATE_wD1_s4_cRGB.png	\N	BLUE
7526	wD1_d1-2_cDAPI	\N	\N
7527	original/PLATE-1/bPLATE_wD1_s4_cRGB.png	\N	GREEN
7528	wD1_d1-2_cGFP	\N	\N
7529	original/PLATE-1/bPLATE_wD1_s4_cRGB.png	\N	RED
7530	wD1_d1-2_cCy5	\N	\N
7531	original/PLATE-1/bPLATE_wA8_s4_cRGB.png	\N	BLUE
7532	wA8_d1-2_cDAPI	\N	\N
7533	original/PLATE-1/bPLATE_wA8_s4_cRGB.png	\N	GREEN
7534	wA8_d1-2_cGFP	\N	\N
7535	original/PLATE-1/bPLATE_wA8_s4_cRGB.png	\N	RED
7536	wA8_d1-2_cCy5	\N	\N
7537	original/PLATE-1/bPLATE_wA6_s7_cRGB.png	\N	BLUE
7538	wA6_d1-3_cDAPI	\N	\N
7539	original/PLATE-1/bPLATE_wA6_s7_cRGB.png	\N	GREEN
7540	wA6_d1-3_cGFP	\N	\N
7541	original/PLATE-1/bPLATE_wA6_s7_cRGB.png	\N	RED
7542	wA6_d1-3_cCy5	\N	\N
7543	original/PLATE-1/bPLATE_wF8_s1_cRGB.png	\N	BLUE
7544	wF8_d1-1_cDAPI	\N	\N
7545	original/PLATE-1/bPLATE_wF8_s1_cRGB.png	\N	GREEN
7546	wF8_d1-1_cGFP	\N	\N
7547	original/PLATE-1/bPLATE_wF8_s1_cRGB.png	\N	RED
7548	wF8_d1-1_cCy5	\N	\N
7549	original/PLATE-1/bPLATE_wG2_s8_cRGB.png	\N	BLUE
7550	wG2_d2-3_cDAPI	\N	\N
7551	original/PLATE-1/bPLATE_wG2_s8_cRGB.png	\N	GREEN
7552	wG2_d2-3_cGFP	\N	\N
7553	original/PLATE-1/bPLATE_wG2_s8_cRGB.png	\N	RED
7554	wG2_d2-3_cCy5	\N	\N
7555	original/PLATE-1/bPLATE_wG4_s8_cRGB.png	\N	BLUE
7556	wG4_d2-3_cDAPI	\N	\N
7557	original/PLATE-1/bPLATE_wG4_s8_cRGB.png	\N	GREEN
7558	wG4_d2-3_cGFP	\N	\N
7559	original/PLATE-1/bPLATE_wG4_s8_cRGB.png	\N	RED
7560	wG4_d2-3_cCy5	\N	\N
7561	original/PLATE-1/bPLATE_wB10_s5_cRGB.png	\N	BLUE
7562	wB10_d2-2_cDAPI	\N	\N
7563	original/PLATE-1/bPLATE_wB10_s5_cRGB.png	\N	GREEN
7564	wB10_d2-2_cGFP	\N	\N
7565	original/PLATE-1/bPLATE_wB10_s5_cRGB.png	\N	RED
7566	wB10_d2-2_cCy5	\N	\N
7567	original/PLATE-1/bPLATE_wE5_s2_cRGB.png	\N	BLUE
7568	wE5_d2-1_cDAPI	\N	\N
7569	original/PLATE-1/bPLATE_wE5_s2_cRGB.png	\N	GREEN
7570	wE5_d2-1_cGFP	\N	\N
7571	original/PLATE-1/bPLATE_wE5_s2_cRGB.png	\N	RED
7572	wE5_d2-1_cCy5	\N	\N
7573	original/PLATE-1/bPLATE_wH6_s5_cRGB.png	\N	BLUE
7574	wH6_d2-2_cDAPI	\N	\N
7575	original/PLATE-1/bPLATE_wH6_s5_cRGB.png	\N	GREEN
7576	wH6_d2-2_cGFP	\N	\N
7577	original/PLATE-1/bPLATE_wH6_s5_cRGB.png	\N	RED
7578	wH6_d2-2_cCy5	\N	\N
7579	original/PLATE-1/bPLATE_wE11_s8_cRGB.png	\N	BLUE
7580	wE11_d2-3_cDAPI	\N	\N
7581	original/PLATE-1/bPLATE_wE11_s8_cRGB.png	\N	GREEN
7582	wE11_d2-3_cGFP	\N	\N
7583	original/PLATE-1/bPLATE_wE11_s8_cRGB.png	\N	RED
7584	wE11_d2-3_cCy5	\N	\N
7585	original/PLATE-1/bPLATE_wH3_s3_cRGB.png	\N	BLUE
7586	wH3_d3-1_cDAPI	\N	\N
7587	original/PLATE-1/bPLATE_wH3_s3_cRGB.png	\N	GREEN
7588	wH3_d3-1_cGFP	\N	\N
7589	original/PLATE-1/bPLATE_wH3_s3_cRGB.png	\N	RED
7590	wH3_d3-1_cCy5	\N	\N
7591	original/PLATE-1/bPLATE_wC1_s9_cRGB.png	\N	BLUE
7592	wC1_d3-3_cDAPI	\N	\N
7593	original/PLATE-1/bPLATE_wC1_s9_cRGB.png	\N	GREEN
7594	wC1_d3-3_cGFP	\N	\N
7595	original/PLATE-1/bPLATE_wC1_s9_cRGB.png	\N	RED
7596	wC1_d3-3_cCy5	\N	\N
7597	original/PLATE-1/bPLATE_wE8_s6_cRGB.png	\N	BLUE
7598	wE8_d3-2_cDAPI	\N	\N
7599	original/PLATE-1/bPLATE_wE8_s6_cRGB.png	\N	GREEN
7600	wE8_d3-2_cGFP	\N	\N
7601	original/PLATE-1/bPLATE_wE8_s6_cRGB.png	\N	RED
7602	wE8_d3-2_cCy5	\N	\N
7603	original/PLATE-1/bPLATE_wE2_s8_cRGB.png	\N	BLUE
7604	wE2_d2-3_cDAPI	\N	\N
7605	original/PLATE-1/bPLATE_wE2_s8_cRGB.png	\N	GREEN
7606	wE2_d2-3_cGFP	\N	\N
7607	original/PLATE-1/bPLATE_wE2_s8_cRGB.png	\N	RED
7608	wE2_d2-3_cCy5	\N	\N
7609	original/PLATE-1/bPLATE_wG9_s5_cRGB.png	\N	BLUE
7610	wG9_d2-2_cDAPI	\N	\N
7611	original/PLATE-1/bPLATE_wG9_s5_cRGB.png	\N	GREEN
7612	wG9_d2-2_cGFP	\N	\N
7613	original/PLATE-1/bPLATE_wG9_s5_cRGB.png	\N	RED
7614	wG9_d2-2_cCy5	\N	\N
7615	original/PLATE-1/bPLATE_wH7_s8_cRGB.png	\N	BLUE
7616	wH7_d2-3_cDAPI	\N	\N
7617	original/PLATE-1/bPLATE_wH7_s8_cRGB.png	\N	GREEN
7618	wH7_d2-3_cGFP	\N	\N
7619	original/PLATE-1/bPLATE_wH7_s8_cRGB.png	\N	RED
7620	wH7_d2-3_cCy5	\N	\N
7621	original/PLATE-1/bPLATE_wE5_s7_cRGB.png	\N	BLUE
7622	wE5_d1-3_cDAPI	\N	\N
7623	original/PLATE-1/bPLATE_wE5_s7_cRGB.png	\N	GREEN
7624	wE5_d1-3_cGFP	\N	\N
7625	original/PLATE-1/bPLATE_wE5_s7_cRGB.png	\N	RED
7626	wE5_d1-3_cCy5	\N	\N
7627	original/PLATE-1/bPLATE_wG12_s4_cRGB.png	\N	BLUE
7628	wG12_d1-2_cDAPI	\N	\N
7629	original/PLATE-1/bPLATE_wG12_s4_cRGB.png	\N	GREEN
7630	wG12_d1-2_cGFP	\N	\N
7631	original/PLATE-1/bPLATE_wG12_s4_cRGB.png	\N	RED
7632	wG12_d1-2_cCy5	\N	\N
7633	original/PLATE-1/bPLATE_wC11_s5_cRGB.png	\N	BLUE
7634	wC11_d2-2_cDAPI	\N	\N
7635	original/PLATE-1/bPLATE_wC11_s5_cRGB.png	\N	GREEN
7636	wC11_d2-2_cGFP	\N	\N
7637	original/PLATE-1/bPLATE_wC11_s5_cRGB.png	\N	RED
7638	wC11_d2-2_cCy5	\N	\N
7639	original/PLATE-1/bPLATE_wA4_s8_cRGB.png	\N	BLUE
7640	wA4_d2-3_cDAPI	\N	\N
7641	original/PLATE-1/bPLATE_wA4_s8_cRGB.png	\N	GREEN
7642	wA4_d2-3_cGFP	\N	\N
7643	original/PLATE-1/bPLATE_wA4_s8_cRGB.png	\N	RED
7644	wA4_d2-3_cCy5	\N	\N
7645	original/PLATE-1/bPLATE_wF6_s2_cRGB.png	\N	BLUE
7646	wF6_d2-1_cDAPI	\N	\N
7647	original/PLATE-1/bPLATE_wF6_s2_cRGB.png	\N	GREEN
7648	wF6_d2-1_cGFP	\N	\N
7649	original/PLATE-1/bPLATE_wF6_s2_cRGB.png	\N	RED
7650	wF6_d2-1_cCy5	\N	\N
7651	original/PLATE-1/bPLATE_wG6_s9_cRGB.png	\N	BLUE
7652	wG6_d3-3_cDAPI	\N	\N
7653	original/PLATE-1/bPLATE_wG6_s9_cRGB.png	\N	GREEN
7654	wG6_d3-3_cGFP	\N	\N
7655	original/PLATE-1/bPLATE_wG6_s9_cRGB.png	\N	RED
7656	wG6_d3-3_cCy5	\N	\N
7657	original/PLATE-1/bPLATE_wC6_s5_cRGB.png	\N	BLUE
7658	wC6_d2-2_cDAPI	\N	\N
7659	original/PLATE-1/bPLATE_wC6_s5_cRGB.png	\N	GREEN
7660	wC6_d2-2_cGFP	\N	\N
7661	original/PLATE-1/bPLATE_wC6_s5_cRGB.png	\N	RED
7662	wC6_d2-2_cCy5	\N	\N
7663	original/PLATE-1/bPLATE_wF1_s2_cRGB.png	\N	BLUE
7664	wF1_d2-1_cDAPI	\N	\N
7665	original/PLATE-1/bPLATE_wF1_s2_cRGB.png	\N	GREEN
7666	wF1_d2-1_cGFP	\N	\N
7667	original/PLATE-1/bPLATE_wF1_s2_cRGB.png	\N	RED
7668	wF1_d2-1_cCy5	\N	\N
7669	original/PLATE-1/bPLATE_wC11_s6_cRGB.png	\N	BLUE
7670	wC11_d3-2_cDAPI	\N	\N
7671	original/PLATE-1/bPLATE_wC11_s6_cRGB.png	\N	GREEN
7672	wC11_d3-2_cGFP	\N	\N
7673	original/PLATE-1/bPLATE_wC11_s6_cRGB.png	\N	RED
7674	wC11_d3-2_cCy5	\N	\N
7675	original/PLATE-1/bPLATE_wA4_s9_cRGB.png	\N	BLUE
7676	wA4_d3-3_cDAPI	\N	\N
7677	original/PLATE-1/bPLATE_wA4_s9_cRGB.png	\N	GREEN
7678	wA4_d3-3_cGFP	\N	\N
7679	original/PLATE-1/bPLATE_wA4_s9_cRGB.png	\N	RED
7680	wA4_d3-3_cCy5	\N	\N
7681	original/PLATE-1/bPLATE_wF6_s3_cRGB.png	\N	BLUE
7682	wF6_d3-1_cDAPI	\N	\N
7683	original/PLATE-1/bPLATE_wF6_s3_cRGB.png	\N	GREEN
7684	wF6_d3-1_cGFP	\N	\N
7685	original/PLATE-1/bPLATE_wF6_s3_cRGB.png	\N	RED
7686	wF6_d3-1_cCy5	\N	\N
7687	original/PLATE-1/bPLATE_wE3_s7_cRGB.png	\N	BLUE
7688	wE3_d1-3_cDAPI	\N	\N
7689	original/PLATE-1/bPLATE_wE3_s7_cRGB.png	\N	GREEN
7690	wE3_d1-3_cGFP	\N	\N
7691	original/PLATE-1/bPLATE_wE3_s7_cRGB.png	\N	RED
7692	wE3_d1-3_cCy5	\N	\N
7693	original/PLATE-1/bPLATE_wG10_s4_cRGB.png	\N	BLUE
7694	wG10_d1-2_cDAPI	\N	\N
7695	original/PLATE-1/bPLATE_wG10_s4_cRGB.png	\N	GREEN
7696	wG10_d1-2_cGFP	\N	\N
7697	original/PLATE-1/bPLATE_wG10_s4_cRGB.png	\N	RED
7698	wG10_d1-2_cCy5	\N	\N
7699	original/PLATE-1/bPLATE_wC7_s5_cRGB.png	\N	BLUE
7700	wC7_d2-2_cDAPI	\N	\N
7701	original/PLATE-1/bPLATE_wC7_s5_cRGB.png	\N	GREEN
7702	wC7_d2-2_cGFP	\N	\N
7703	original/PLATE-1/bPLATE_wC7_s5_cRGB.png	\N	RED
7704	wC7_d2-2_cCy5	\N	\N
7705	original/PLATE-1/bPLATE_wF2_s2_cRGB.png	\N	BLUE
7706	wF2_d2-1_cDAPI	\N	\N
7707	original/PLATE-1/bPLATE_wF2_s2_cRGB.png	\N	GREEN
7708	wF2_d2-1_cGFP	\N	\N
7709	original/PLATE-1/bPLATE_wF2_s2_cRGB.png	\N	RED
7710	wF2_d2-1_cCy5	\N	\N
7711	original/PLATE-1/bPLATE_wA11_s3_cRGB.png	\N	BLUE
7712	wA11_d3-1_cDAPI	\N	\N
7713	original/PLATE-1/bPLATE_wA11_s3_cRGB.png	\N	GREEN
7714	wA11_d3-1_cGFP	\N	\N
7715	original/PLATE-1/bPLATE_wA11_s3_cRGB.png	\N	RED
7716	wA11_d3-1_cCy5	\N	\N
7717	original/PLATE-1/bPLATE_wH6_s3_cRGB.png	\N	BLUE
7718	wH6_d3-1_cDAPI	\N	\N
7719	original/PLATE-1/bPLATE_wH6_s3_cRGB.png	\N	GREEN
7720	wH6_d3-1_cGFP	\N	\N
7721	original/PLATE-1/bPLATE_wH6_s3_cRGB.png	\N	RED
7722	wH6_d3-1_cCy5	\N	\N
7723	original/PLATE-1/bPLATE_wC4_s9_cRGB.png	\N	BLUE
7724	wC4_d3-3_cDAPI	\N	\N
7725	original/PLATE-1/bPLATE_wC4_s9_cRGB.png	\N	GREEN
7726	wC4_d3-3_cGFP	\N	\N
7727	original/PLATE-1/bPLATE_wC4_s9_cRGB.png	\N	RED
7728	wC4_d3-3_cCy5	\N	\N
7729	original/PLATE-1/bPLATE_wE11_s6_cRGB.png	\N	BLUE
7730	wE11_d3-2_cDAPI	\N	\N
7731	original/PLATE-1/bPLATE_wE11_s6_cRGB.png	\N	GREEN
7732	wE11_d3-2_cGFP	\N	\N
7733	original/PLATE-1/bPLATE_wE11_s6_cRGB.png	\N	RED
7734	wE11_d3-2_cCy5	\N	\N
7735	original/PLATE-1/bPLATE_wC10_s5_cRGB.png	\N	BLUE
7736	wC10_d2-2_cDAPI	\N	\N
7737	original/PLATE-1/bPLATE_wC10_s5_cRGB.png	\N	GREEN
7738	wC10_d2-2_cGFP	\N	\N
7739	original/PLATE-1/bPLATE_wC10_s5_cRGB.png	\N	RED
7740	wC10_d2-2_cCy5	\N	\N
7741	original/PLATE-1/bPLATE_wA3_s8_cRGB.png	\N	BLUE
7742	wA3_d2-3_cDAPI	\N	\N
7743	original/PLATE-1/bPLATE_wA3_s8_cRGB.png	\N	GREEN
7744	wA3_d2-3_cGFP	\N	\N
7745	original/PLATE-1/bPLATE_wA3_s8_cRGB.png	\N	RED
7746	wA3_d2-3_cCy5	\N	\N
7747	original/PLATE-1/bPLATE_wF5_s2_cRGB.png	\N	BLUE
7748	wF5_d2-1_cDAPI	\N	\N
7749	original/PLATE-1/bPLATE_wF5_s2_cRGB.png	\N	GREEN
7750	wF5_d2-1_cGFP	\N	\N
7751	original/PLATE-1/bPLATE_wF5_s2_cRGB.png	\N	RED
7752	wF5_d2-1_cCy5	\N	\N
7753	original/PLATE-1/bPLATE_wB7_s7_cRGB.png	\N	BLUE
7754	wB7_d1-3_cDAPI	\N	\N
7755	original/PLATE-1/bPLATE_wB7_s7_cRGB.png	\N	GREEN
7756	wB7_d1-3_cGFP	\N	\N
7757	original/PLATE-1/bPLATE_wB7_s7_cRGB.png	\N	RED
7758	wB7_d1-3_cCy5	\N	\N
7759	original/PLATE-1/bPLATE_wE2_s4_cRGB.png	\N	BLUE
7760	wE2_d1-2_cDAPI	\N	\N
7761	original/PLATE-1/bPLATE_wE2_s4_cRGB.png	\N	GREEN
7762	wE2_d1-2_cGFP	\N	\N
7763	original/PLATE-1/bPLATE_wE2_s4_cRGB.png	\N	RED
7764	wE2_d1-2_cCy5	\N	\N
7765	original/PLATE-1/bPLATE_wG9_s1_cRGB.png	\N	BLUE
7766	wG9_d1-1_cDAPI	\N	\N
7767	original/PLATE-1/bPLATE_wG9_s1_cRGB.png	\N	GREEN
7768	wG9_d1-1_cGFP	\N	\N
7769	original/PLATE-1/bPLATE_wG9_s1_cRGB.png	\N	RED
7770	wG9_d1-1_cCy5	\N	\N
7771	original/PLATE-1/bPLATE_wB2_s2_cRGB.png	\N	BLUE
7772	wB2_d2-1_cDAPI	\N	\N
7773	original/PLATE-1/bPLATE_wB2_s2_cRGB.png	\N	GREEN
7774	wB2_d2-1_cGFP	\N	\N
7775	original/PLATE-1/bPLATE_wB2_s2_cRGB.png	\N	RED
7776	wB2_d2-1_cCy5	\N	\N
7777	original/PLATE-1/bPLATE_wA10_s6_cRGB.png	\N	BLUE
7778	wA10_d3-2_cDAPI	\N	\N
7779	original/PLATE-1/bPLATE_wA10_s6_cRGB.png	\N	GREEN
7780	wA10_d3-2_cGFP	\N	\N
7781	original/PLATE-1/bPLATE_wA10_s6_cRGB.png	\N	RED
7782	wA10_d3-2_cCy5	\N	\N
7783	original/PLATE-1/bPLATE_wD5_s3_cRGB.png	\N	BLUE
7784	wD5_d3-1_cDAPI	\N	\N
7785	original/PLATE-1/bPLATE_wD5_s3_cRGB.png	\N	GREEN
7786	wD5_d3-1_cGFP	\N	\N
7787	original/PLATE-1/bPLATE_wD5_s3_cRGB.png	\N	RED
7788	wD5_d3-1_cCy5	\N	\N
7789	original/PLATE-1/bPLATE_wG10_s8_cRGB.png	\N	BLUE
7790	wG10_d2-3_cDAPI	\N	\N
7791	original/PLATE-1/bPLATE_wG10_s8_cRGB.png	\N	GREEN
7792	wG10_d2-3_cGFP	\N	\N
7793	original/PLATE-1/bPLATE_wG10_s8_cRGB.png	\N	RED
7794	wG10_d2-3_cCy5	\N	\N
7795	original/PLATE-1/bPLATE_wD2_s3_cRGB.png	\N	BLUE
7796	wD2_d3-1_cDAPI	\N	\N
7797	original/PLATE-1/bPLATE_wD2_s3_cRGB.png	\N	GREEN
7798	wD2_d3-1_cGFP	\N	\N
7799	original/PLATE-1/bPLATE_wD2_s3_cRGB.png	\N	RED
7800	wD2_d3-1_cCy5	\N	\N
7801	original/PLATE-1/bPLATE_wA7_s6_cRGB.png	\N	BLUE
7802	wA7_d3-2_cDAPI	\N	\N
7803	original/PLATE-1/bPLATE_wA7_s6_cRGB.png	\N	GREEN
7804	wA7_d3-2_cGFP	\N	\N
7805	original/PLATE-1/bPLATE_wA7_s6_cRGB.png	\N	RED
7806	wA7_d3-2_cCy5	\N	\N
7807	original/PLATE-1/bPLATE_wH9_s5_cRGB.png	\N	BLUE
7808	wH9_d2-2_cDAPI	\N	\N
7809	original/PLATE-1/bPLATE_wH9_s5_cRGB.png	\N	GREEN
7810	wH9_d2-2_cGFP	\N	\N
7811	original/PLATE-1/bPLATE_wH9_s5_cRGB.png	\N	RED
7812	wH9_d2-2_cCy5	\N	\N
7813	original/PLATE-1/bPLATE_wF2_s8_cRGB.png	\N	BLUE
7814	wF2_d2-3_cDAPI	\N	\N
7815	original/PLATE-1/bPLATE_wF2_s8_cRGB.png	\N	GREEN
7816	wF2_d2-3_cGFP	\N	\N
7817	original/PLATE-1/bPLATE_wF2_s8_cRGB.png	\N	RED
7818	wF2_d2-3_cCy5	\N	\N
7819	original/PLATE-1/bPLATE_wH8_s8_cRGB.png	\N	BLUE
7820	wH8_d2-3_cDAPI	\N	\N
7821	original/PLATE-1/bPLATE_wH8_s8_cRGB.png	\N	GREEN
7822	wH8_d2-3_cGFP	\N	\N
7823	original/PLATE-1/bPLATE_wH8_s8_cRGB.png	\N	RED
7824	wH8_d2-3_cCy5	\N	\N
7825	original/PLATE-1/bPLATE_wF8_s7_cRGB.png	\N	BLUE
7826	wF8_d1-3_cDAPI	\N	\N
7827	original/PLATE-1/bPLATE_wF8_s7_cRGB.png	\N	GREEN
7828	wF8_d1-3_cGFP	\N	\N
7829	original/PLATE-1/bPLATE_wF8_s7_cRGB.png	\N	RED
7830	wF8_d1-3_cCy5	\N	\N
7831	original/PLATE-1/bPLATE_wA11_s8_cRGB.png	\N	BLUE
7832	wA11_d2-3_cDAPI	\N	\N
7833	original/PLATE-1/bPLATE_wA11_s8_cRGB.png	\N	GREEN
7834	wA11_d2-3_cGFP	\N	\N
7835	original/PLATE-1/bPLATE_wA11_s8_cRGB.png	\N	RED
7836	wA11_d2-3_cCy5	\N	\N
7837	original/PLATE-1/bPLATE_wA9_s1_cRGB.png	\N	BLUE
7838	wA9_d1-1_cDAPI	\N	\N
7839	original/PLATE-1/bPLATE_wA9_s1_cRGB.png	\N	GREEN
7840	wA9_d1-1_cGFP	\N	\N
7841	original/PLATE-1/bPLATE_wA9_s1_cRGB.png	\N	RED
7842	wA9_d1-1_cCy5	\N	\N
7843	original/PLATE-1/bPLATE_wD6_s5_cRGB.png	\N	BLUE
7844	wD6_d2-2_cDAPI	\N	\N
7845	original/PLATE-1/bPLATE_wD6_s5_cRGB.png	\N	GREEN
7846	wD6_d2-2_cGFP	\N	\N
7847	original/PLATE-1/bPLATE_wD6_s5_cRGB.png	\N	RED
7848	wD6_d2-2_cCy5	\N	\N
7849	original/PLATE-1/bPLATE_wG1_s2_cRGB.png	\N	BLUE
7850	wG1_d2-1_cDAPI	\N	\N
7851	original/PLATE-1/bPLATE_wG1_s2_cRGB.png	\N	GREEN
7852	wG1_d2-1_cGFP	\N	\N
7853	original/PLATE-1/bPLATE_wG1_s2_cRGB.png	\N	RED
7854	wG1_d2-1_cCy5	\N	\N
7855	original/PLATE-1/bPLATE_wD1_s5_cRGB.png	\N	BLUE
7856	wD1_d2-2_cDAPI	\N	\N
7857	original/PLATE-1/bPLATE_wD1_s5_cRGB.png	\N	GREEN
7858	wD1_d2-2_cGFP	\N	\N
7859	original/PLATE-1/bPLATE_wD1_s5_cRGB.png	\N	RED
7860	wD1_d2-2_cCy5	\N	\N
7861	original/PLATE-1/bPLATE_wA6_s8_cRGB.png	\N	BLUE
7862	wA6_d2-3_cDAPI	\N	\N
7863	original/PLATE-1/bPLATE_wA6_s8_cRGB.png	\N	GREEN
7864	wA6_d2-3_cGFP	\N	\N
7865	original/PLATE-1/bPLATE_wA6_s8_cRGB.png	\N	RED
7866	wA6_d2-3_cCy5	\N	\N
7867	original/PLATE-1/bPLATE_wF8_s2_cRGB.png	\N	BLUE
7868	wF8_d2-1_cDAPI	\N	\N
7869	original/PLATE-1/bPLATE_wF8_s2_cRGB.png	\N	GREEN
7870	wF8_d2-1_cGFP	\N	\N
7871	original/PLATE-1/bPLATE_wF8_s2_cRGB.png	\N	RED
7872	wF8_d2-1_cCy5	\N	\N
7873	original/PLATE-1/bPLATE_wD8_s8_cRGB.png	\N	BLUE
7874	wD8_d2-3_cDAPI	\N	\N
7875	original/PLATE-1/bPLATE_wD8_s8_cRGB.png	\N	GREEN
7876	wD8_d2-3_cGFP	\N	\N
7877	original/PLATE-1/bPLATE_wD8_s8_cRGB.png	\N	RED
7878	wD8_d2-3_cCy5	\N	\N
7879	original/PLATE-1/bPLATE_wG3_s5_cRGB.png	\N	BLUE
7880	wG3_d2-2_cDAPI	\N	\N
7881	original/PLATE-1/bPLATE_wG3_s5_cRGB.png	\N	GREEN
7882	wG3_d2-2_cGFP	\N	\N
7883	original/PLATE-1/bPLATE_wG3_s5_cRGB.png	\N	RED
7884	wG3_d2-2_cCy5	\N	\N
7885	original/PLATE-1/bPLATE_wB1_s1_cRGB.png	\N	BLUE
7886	wB1_d1-1_cDAPI	\N	\N
7887	original/PLATE-1/bPLATE_wB1_s1_cRGB.png	\N	GREEN
7888	wB1_d1-1_cGFP	\N	\N
7889	original/PLATE-1/bPLATE_wB1_s1_cRGB.png	\N	RED
7890	wB1_d1-1_cCy5	\N	\N
7891	original/PLATE-1/bPLATE_wA11_s2_cRGB.png	\N	BLUE
7892	wA11_d2-1_cDAPI	\N	\N
7893	original/PLATE-1/bPLATE_wA11_s2_cRGB.png	\N	GREEN
7894	wA11_d2-1_cGFP	\N	\N
7895	original/PLATE-1/bPLATE_wA11_s2_cRGB.png	\N	RED
7896	wA11_d2-1_cCy5	\N	\N
7897	original/PLATE-1/bPLATE_wB1_s6_cRGB.png	\N	BLUE
7898	wB1_d3-2_cDAPI	\N	\N
7899	original/PLATE-1/bPLATE_wB1_s6_cRGB.png	\N	GREEN
7900	wB1_d3-2_cGFP	\N	\N
7901	original/PLATE-1/bPLATE_wB1_s6_cRGB.png	\N	RED
7902	wB1_d3-2_cCy5	\N	\N
7903	original/PLATE-1/bPLATE_wD8_s3_cRGB.png	\N	BLUE
7904	wD8_d3-1_cDAPI	\N	\N
7905	original/PLATE-1/bPLATE_wD8_s3_cRGB.png	\N	GREEN
7906	wD8_d3-1_cGFP	\N	\N
7907	original/PLATE-1/bPLATE_wD8_s3_cRGB.png	\N	RED
7908	wD8_d3-1_cCy5	\N	\N
7909	original/PLATE-1/bPLATE_wF11_s8_cRGB.png	\N	BLUE
7910	wF11_d2-3_cDAPI	\N	\N
7911	original/PLATE-1/bPLATE_wF11_s8_cRGB.png	\N	GREEN
7912	wF11_d2-3_cGFP	\N	\N
7913	original/PLATE-1/bPLATE_wF11_s8_cRGB.png	\N	RED
7914	wF11_d2-3_cCy5	\N	\N
7915	original/PLATE-1/bPLATE_wC8_s1_cRGB.png	\N	BLUE
7916	wC8_d1-1_cDAPI	\N	\N
7917	original/PLATE-1/bPLATE_wC8_s1_cRGB.png	\N	GREEN
7918	wC8_d1-1_cGFP	\N	\N
7919	original/PLATE-1/bPLATE_wC8_s1_cRGB.png	\N	RED
7920	wC8_d1-1_cCy5	\N	\N
7921	original/PLATE-1/bPLATE_wA1_s4_cRGB.png	\N	BLUE
7922	wA1_d1-2_cDAPI	\N	\N
7923	original/PLATE-1/bPLATE_wA1_s4_cRGB.png	\N	GREEN
7924	wA1_d1-2_cGFP	\N	\N
7925	original/PLATE-1/bPLATE_wA1_s4_cRGB.png	\N	RED
7926	wA1_d1-2_cCy5	\N	\N
7927	original/PLATE-1/bPLATE_wB6_s9_cRGB.png	\N	BLUE
7928	wB6_d3-3_cDAPI	\N	\N
7929	original/PLATE-1/bPLATE_wB6_s9_cRGB.png	\N	GREEN
7930	wB6_d3-3_cGFP	\N	\N
7931	original/PLATE-1/bPLATE_wB6_s9_cRGB.png	\N	RED
7932	wB6_d3-3_cCy5	\N	\N
7933	original/PLATE-1/bPLATE_wE1_s6_cRGB.png	\N	BLUE
7934	wE1_d3-2_cDAPI	\N	\N
7935	original/PLATE-1/bPLATE_wE1_s6_cRGB.png	\N	GREEN
7936	wE1_d3-2_cGFP	\N	\N
7937	original/PLATE-1/bPLATE_wE1_s6_cRGB.png	\N	RED
7938	wE1_d3-2_cCy5	\N	\N
7939	original/PLATE-1/bPLATE_wG8_s3_cRGB.png	\N	BLUE
7940	wG8_d3-1_cDAPI	\N	\N
7941	original/PLATE-1/bPLATE_wG8_s3_cRGB.png	\N	GREEN
7942	wG8_d3-1_cGFP	\N	\N
7943	original/PLATE-1/bPLATE_wG8_s3_cRGB.png	\N	RED
7944	wG8_d3-1_cCy5	\N	\N
7945	original/PLATE-1/bPLATE_wD2_s8_cRGB.png	\N	BLUE
7946	wD2_d2-3_cDAPI	\N	\N
7947	original/PLATE-1/bPLATE_wD2_s8_cRGB.png	\N	GREEN
7948	wD2_d2-3_cGFP	\N	\N
7949	original/PLATE-1/bPLATE_wD2_s8_cRGB.png	\N	RED
7950	wD2_d2-3_cCy5	\N	\N
7951	original/PLATE-1/bPLATE_wF9_s5_cRGB.png	\N	BLUE
7952	wF9_d2-2_cDAPI	\N	\N
7953	original/PLATE-1/bPLATE_wF9_s5_cRGB.png	\N	GREEN
7954	wF9_d2-2_cGFP	\N	\N
7955	original/PLATE-1/bPLATE_wF9_s5_cRGB.png	\N	RED
7956	wF9_d2-2_cCy5	\N	\N
7957	original/PLATE-1/bPLATE_wC8_s6_cRGB.png	\N	BLUE
7958	wC8_d3-2_cDAPI	\N	\N
7959	original/PLATE-1/bPLATE_wC8_s6_cRGB.png	\N	GREEN
7960	wC8_d3-2_cGFP	\N	\N
7961	original/PLATE-1/bPLATE_wC8_s6_cRGB.png	\N	RED
7962	wC8_d3-2_cCy5	\N	\N
7963	original/PLATE-1/bPLATE_wA1_s9_cRGB.png	\N	BLUE
7964	wA1_d3-3_cDAPI	\N	\N
7965	original/PLATE-1/bPLATE_wA1_s9_cRGB.png	\N	GREEN
7966	wA1_d3-3_cGFP	\N	\N
7967	original/PLATE-1/bPLATE_wA1_s9_cRGB.png	\N	RED
7968	wA1_d3-3_cCy5	\N	\N
7969	original/PLATE-1/bPLATE_wF3_s3_cRGB.png	\N	BLUE
7970	wF3_d3-1_cDAPI	\N	\N
7971	original/PLATE-1/bPLATE_wF3_s3_cRGB.png	\N	GREEN
7972	wF3_d3-1_cGFP	\N	\N
7973	original/PLATE-1/bPLATE_wF3_s3_cRGB.png	\N	RED
7974	wF3_d3-1_cCy5	\N	\N
7975	original/PLATE-1/bPLATE_wC12_s3_cRGB.png	\N	BLUE
7976	wC12_d3-1_cDAPI	\N	\N
7977	original/PLATE-1/bPLATE_wC12_s3_cRGB.png	\N	GREEN
7978	wC12_d3-1_cGFP	\N	\N
7979	original/PLATE-1/bPLATE_wC12_s3_cRGB.png	\N	RED
7980	wC12_d3-1_cCy5	\N	\N
7981	original/PLATE-1/bPLATE_wA5_s6_cRGB.png	\N	BLUE
7982	wA5_d3-2_cDAPI	\N	\N
7983	original/PLATE-1/bPLATE_wA5_s6_cRGB.png	\N	GREEN
7984	wA5_d3-2_cGFP	\N	\N
7985	original/PLATE-1/bPLATE_wA5_s6_cRGB.png	\N	RED
7986	wA5_d3-2_cCy5	\N	\N
7987	original/PLATE-1/bPLATE_wA12_s1_cRGB.png	\N	BLUE
7988	wA12_d1-1_cDAPI	\N	\N
7989	original/PLATE-1/bPLATE_wA12_s1_cRGB.png	\N	GREEN
7990	wA12_d1-1_cGFP	\N	\N
7991	original/PLATE-1/bPLATE_wA12_s1_cRGB.png	\N	RED
7992	wA12_d1-1_cCy5	\N	\N
7993	original/PLATE-1/bPLATE_wE2_s7_cRGB.png	\N	BLUE
7994	wE2_d1-3_cDAPI	\N	\N
7995	original/PLATE-1/bPLATE_wE2_s7_cRGB.png	\N	GREEN
7996	wE2_d1-3_cGFP	\N	\N
7997	original/PLATE-1/bPLATE_wE2_s7_cRGB.png	\N	RED
7998	wE2_d1-3_cCy5	\N	\N
7999	original/PLATE-1/bPLATE_wG9_s4_cRGB.png	\N	BLUE
8000	wG9_d1-2_cDAPI	\N	\N
8001	original/PLATE-1/bPLATE_wG9_s4_cRGB.png	\N	GREEN
8002	wG9_d1-2_cGFP	\N	\N
8003	original/PLATE-1/bPLATE_wG9_s4_cRGB.png	\N	RED
8004	wG9_d1-2_cCy5	\N	\N
8005	original/PLATE-1/bPLATE_wH9_s4_cRGB.png	\N	BLUE
8006	wH9_d1-2_cDAPI	\N	\N
8007	original/PLATE-1/bPLATE_wH9_s4_cRGB.png	\N	GREEN
8008	wH9_d1-2_cGFP	\N	\N
8009	original/PLATE-1/bPLATE_wH9_s4_cRGB.png	\N	RED
8010	wH9_d1-2_cCy5	\N	\N
8011	original/PLATE-1/bPLATE_wF2_s7_cRGB.png	\N	BLUE
8012	wF2_d1-3_cDAPI	\N	\N
8013	original/PLATE-1/bPLATE_wF2_s7_cRGB.png	\N	GREEN
8014	wF2_d1-3_cGFP	\N	\N
8015	original/PLATE-1/bPLATE_wF2_s7_cRGB.png	\N	RED
8016	wF2_d1-3_cCy5	\N	\N
8017	original/PLATE-1/bPLATE_wB7_s4_cRGB.png	\N	BLUE
8018	wB7_d1-2_cDAPI	\N	\N
8019	original/PLATE-1/bPLATE_wB7_s4_cRGB.png	\N	GREEN
8020	wB7_d1-2_cGFP	\N	\N
8021	original/PLATE-1/bPLATE_wB7_s4_cRGB.png	\N	RED
8022	wB7_d1-2_cCy5	\N	\N
8023	original/PLATE-1/bPLATE_wE2_s1_cRGB.png	\N	BLUE
8024	wE2_d1-1_cDAPI	\N	\N
8025	original/PLATE-1/bPLATE_wE2_s1_cRGB.png	\N	GREEN
8026	wE2_d1-1_cGFP	\N	\N
8027	original/PLATE-1/bPLATE_wE2_s1_cRGB.png	\N	RED
8028	wE2_d1-1_cCy5	\N	\N
8029	original/PLATE-1/bPLATE_wC12_s9_cRGB.png	\N	BLUE
8030	wC12_d3-3_cDAPI	\N	\N
8031	original/PLATE-1/bPLATE_wC12_s9_cRGB.png	\N	GREEN
8032	wC12_d3-3_cGFP	\N	\N
8033	original/PLATE-1/bPLATE_wC12_s9_cRGB.png	\N	RED
8034	wC12_d3-3_cCy5	\N	\N
8035	original/PLATE-1/bPLATE_wF7_s6_cRGB.png	\N	BLUE
8036	wF7_d3-2_cDAPI	\N	\N
8037	original/PLATE-1/bPLATE_wF7_s6_cRGB.png	\N	GREEN
8038	wF7_d3-2_cGFP	\N	\N
8039	original/PLATE-1/bPLATE_wF7_s6_cRGB.png	\N	RED
8040	wF7_d3-2_cCy5	\N	\N
8041	original/PLATE-1/bPLATE_wA5_s3_cRGB.png	\N	BLUE
8042	wA5_d3-1_cDAPI	\N	\N
8043	original/PLATE-1/bPLATE_wA5_s3_cRGB.png	\N	GREEN
8044	wA5_d3-1_cGFP	\N	\N
8045	original/PLATE-1/bPLATE_wA5_s3_cRGB.png	\N	RED
8046	wA5_d3-1_cCy5	\N	\N
8047	original/PLATE-1/bPLATE_wC9_s6_cRGB.png	\N	BLUE
8048	wC9_d3-2_cDAPI	\N	\N
8049	original/PLATE-1/bPLATE_wC9_s6_cRGB.png	\N	GREEN
8050	wC9_d3-2_cGFP	\N	\N
8051	original/PLATE-1/bPLATE_wC9_s6_cRGB.png	\N	RED
8052	wC9_d3-2_cCy5	\N	\N
8053	original/PLATE-1/bPLATE_wA2_s9_cRGB.png	\N	BLUE
8054	wA2_d3-3_cDAPI	\N	\N
8055	original/PLATE-1/bPLATE_wA2_s9_cRGB.png	\N	GREEN
8056	wA2_d3-3_cGFP	\N	\N
8057	original/PLATE-1/bPLATE_wA2_s9_cRGB.png	\N	RED
8058	wA2_d3-3_cCy5	\N	\N
8059	original/PLATE-1/bPLATE_wF4_s3_cRGB.png	\N	BLUE
8060	wF4_d3-1_cDAPI	\N	\N
8061	original/PLATE-1/bPLATE_wF4_s3_cRGB.png	\N	GREEN
8062	wF4_d3-1_cGFP	\N	\N
8063	original/PLATE-1/bPLATE_wF4_s3_cRGB.png	\N	RED
8064	wF4_d3-1_cCy5	\N	\N
8065	original/PLATE-1/bPLATE_wH9_s9_cRGB.png	\N	BLUE
8066	wH9_d3-3_cDAPI	\N	\N
8067	original/PLATE-1/bPLATE_wH9_s9_cRGB.png	\N	GREEN
8068	wH9_d3-3_cGFP	\N	\N
8069	original/PLATE-1/bPLATE_wH9_s9_cRGB.png	\N	RED
8070	wH9_d3-3_cCy5	\N	\N
8071	original/PLATE-1/bPLATE_wC1_s1_cRGB.png	\N	BLUE
8072	wC1_d1-1_cDAPI	\N	\N
8073	original/PLATE-1/bPLATE_wC1_s1_cRGB.png	\N	GREEN
8074	wC1_d1-1_cGFP	\N	\N
8075	original/PLATE-1/bPLATE_wC1_s1_cRGB.png	\N	RED
8076	wC1_d1-1_cCy5	\N	\N
8077	original/PLATE-1/bPLATE_wB6_s8_cRGB.png	\N	BLUE
8078	wB6_d2-3_cDAPI	\N	\N
8079	original/PLATE-1/bPLATE_wB6_s8_cRGB.png	\N	GREEN
8080	wB6_d2-3_cGFP	\N	\N
8081	original/PLATE-1/bPLATE_wB6_s8_cRGB.png	\N	RED
8082	wB6_d2-3_cCy5	\N	\N
8083	original/PLATE-1/bPLATE_wE1_s5_cRGB.png	\N	BLUE
8084	wE1_d2-2_cDAPI	\N	\N
8085	original/PLATE-1/bPLATE_wE1_s5_cRGB.png	\N	GREEN
8086	wE1_d2-2_cGFP	\N	\N
8087	original/PLATE-1/bPLATE_wE1_s5_cRGB.png	\N	RED
8088	wE1_d2-2_cCy5	\N	\N
8089	original/PLATE-1/bPLATE_wG8_s2_cRGB.png	\N	BLUE
8090	wG8_d2-1_cDAPI	\N	\N
8091	original/PLATE-1/bPLATE_wG8_s2_cRGB.png	\N	GREEN
8092	wG8_d2-1_cGFP	\N	\N
8093	original/PLATE-1/bPLATE_wG8_s2_cRGB.png	\N	RED
8094	wG8_d2-1_cCy5	\N	\N
8095	original/PLATE-1/bPLATE_wD6_s7_cRGB.png	\N	BLUE
8096	wD6_d1-3_cDAPI	\N	\N
8097	original/PLATE-1/bPLATE_wD6_s7_cRGB.png	\N	GREEN
8098	wD6_d1-3_cGFP	\N	\N
8099	original/PLATE-1/bPLATE_wD6_s7_cRGB.png	\N	RED
8100	wD6_d1-3_cCy5	\N	\N
8101	original/PLATE-1/bPLATE_wG1_s4_cRGB.png	\N	BLUE
8102	wG1_d1-2_cDAPI	\N	\N
8103	original/PLATE-1/bPLATE_wG1_s4_cRGB.png	\N	GREEN
8104	wG1_d1-2_cGFP	\N	\N
8105	original/PLATE-1/bPLATE_wG1_s4_cRGB.png	\N	RED
8106	wG1_d1-2_cCy5	\N	\N
8107	original/PLATE-1/bPLATE_wH8_s9_cRGB.png	\N	BLUE
8108	wH8_d3-3_cDAPI	\N	\N
8109	original/PLATE-1/bPLATE_wH8_s9_cRGB.png	\N	GREEN
8110	wH8_d3-3_cGFP	\N	\N
8111	original/PLATE-1/bPLATE_wH8_s9_cRGB.png	\N	RED
8112	wH8_d3-3_cCy5	\N	\N
8113	original/PLATE-1/bPLATE_wB10_s9_cRGB.png	\N	BLUE
8114	wB10_d3-3_cDAPI	\N	\N
8115	original/PLATE-1/bPLATE_wB10_s9_cRGB.png	\N	GREEN
8116	wB10_d3-3_cGFP	\N	\N
8117	original/PLATE-1/bPLATE_wB10_s9_cRGB.png	\N	RED
8118	wB10_d3-3_cCy5	\N	\N
8119	original/PLATE-1/bPLATE_wE5_s6_cRGB.png	\N	BLUE
8120	wE5_d3-2_cDAPI	\N	\N
8121	original/PLATE-1/bPLATE_wE5_s6_cRGB.png	\N	GREEN
8122	wE5_d3-2_cGFP	\N	\N
8123	original/PLATE-1/bPLATE_wE5_s6_cRGB.png	\N	RED
8124	wE5_d3-2_cCy5	\N	\N
8125	original/PLATE-1/bPLATE_wG12_s3_cRGB.png	\N	BLUE
8126	wG12_d3-1_cDAPI	\N	\N
8127	original/PLATE-1/bPLATE_wG12_s3_cRGB.png	\N	GREEN
8128	wG12_d3-1_cGFP	\N	\N
8129	original/PLATE-1/bPLATE_wG12_s3_cRGB.png	\N	RED
8130	wG12_d3-1_cCy5	\N	\N
8131	original/PLATE-1/bPLATE_wC8_s8_cRGB.png	\N	BLUE
8132	wC8_d2-3_cDAPI	\N	\N
8133	original/PLATE-1/bPLATE_wC8_s8_cRGB.png	\N	GREEN
8134	wC8_d2-3_cGFP	\N	\N
8135	original/PLATE-1/bPLATE_wC8_s8_cRGB.png	\N	RED
8136	wC8_d2-3_cCy5	\N	\N
8137	original/PLATE-1/bPLATE_wF3_s5_cRGB.png	\N	BLUE
8138	wF3_d2-2_cDAPI	\N	\N
8139	original/PLATE-1/bPLATE_wF3_s5_cRGB.png	\N	GREEN
8140	wF3_d2-2_cGFP	\N	\N
8141	original/PLATE-1/bPLATE_wF3_s5_cRGB.png	\N	RED
8142	wF3_d2-2_cCy5	\N	\N
8143	original/PLATE-1/bPLATE_wH10_s2_cRGB.png	\N	BLUE
8144	wH10_d2-1_cDAPI	\N	\N
8145	original/PLATE-1/bPLATE_wH10_s2_cRGB.png	\N	GREEN
8146	wH10_d2-1_cGFP	\N	\N
8147	original/PLATE-1/bPLATE_wH10_s2_cRGB.png	\N	RED
8148	wH10_d2-1_cCy5	\N	\N
8149	original/PLATE-1/bPLATE_wD10_s5_cRGB.png	\N	BLUE
8150	wD10_d2-2_cDAPI	\N	\N
8151	original/PLATE-1/bPLATE_wD10_s5_cRGB.png	\N	GREEN
8152	wD10_d2-2_cGFP	\N	\N
8153	original/PLATE-1/bPLATE_wD10_s5_cRGB.png	\N	RED
8154	wD10_d2-2_cCy5	\N	\N
8155	original/PLATE-1/bPLATE_wB3_s8_cRGB.png	\N	BLUE
8156	wB3_d2-3_cDAPI	\N	\N
8157	original/PLATE-1/bPLATE_wB3_s8_cRGB.png	\N	GREEN
8158	wB3_d2-3_cGFP	\N	\N
8159	original/PLATE-1/bPLATE_wB3_s8_cRGB.png	\N	RED
8160	wB3_d2-3_cCy5	\N	\N
8161	original/PLATE-1/bPLATE_wG5_s2_cRGB.png	\N	BLUE
8162	wG5_d2-1_cDAPI	\N	\N
8163	original/PLATE-1/bPLATE_wG5_s2_cRGB.png	\N	GREEN
8164	wG5_d2-1_cGFP	\N	\N
8165	original/PLATE-1/bPLATE_wG5_s2_cRGB.png	\N	RED
8166	wG5_d2-1_cCy5	\N	\N
8167	original/PLATE-1/bPLATE_wC5_s6_cRGB.png	\N	BLUE
8168	wC5_d3-2_cDAPI	\N	\N
8169	original/PLATE-1/bPLATE_wC5_s6_cRGB.png	\N	GREEN
8170	wC5_d3-2_cGFP	\N	\N
8171	original/PLATE-1/bPLATE_wC5_s6_cRGB.png	\N	RED
8172	wC5_d3-2_cCy5	\N	\N
8173	original/PLATE-1/bPLATE_wE12_s3_cRGB.png	\N	BLUE
8174	wE12_d3-1_cDAPI	\N	\N
8175	original/PLATE-1/bPLATE_wE12_s3_cRGB.png	\N	GREEN
8176	wE12_d3-1_cGFP	\N	\N
8177	original/PLATE-1/bPLATE_wE12_s3_cRGB.png	\N	RED
8178	wE12_d3-1_cCy5	\N	\N
8179	original/PLATE-1/bPLATE_wD4_s5_cRGB.png	\N	BLUE
8180	wD4_d2-2_cDAPI	\N	\N
8181	original/PLATE-1/bPLATE_wD4_s5_cRGB.png	\N	GREEN
8182	wD4_d2-2_cGFP	\N	\N
8183	original/PLATE-1/bPLATE_wD4_s5_cRGB.png	\N	RED
8184	wD4_d2-2_cCy5	\N	\N
8185	original/PLATE-1/bPLATE_wA9_s8_cRGB.png	\N	BLUE
8186	wA9_d2-3_cDAPI	\N	\N
8187	original/PLATE-1/bPLATE_wA9_s8_cRGB.png	\N	GREEN
8188	wA9_d2-3_cGFP	\N	\N
8189	original/PLATE-1/bPLATE_wA9_s8_cRGB.png	\N	RED
8190	wA9_d2-3_cCy5	\N	\N
8191	original/PLATE-1/bPLATE_wF11_s2_cRGB.png	\N	BLUE
8192	wF11_d2-1_cDAPI	\N	\N
8193	original/PLATE-1/bPLATE_wF11_s2_cRGB.png	\N	GREEN
8194	wF11_d2-1_cGFP	\N	\N
8195	original/PLATE-1/bPLATE_wF11_s2_cRGB.png	\N	RED
8196	wF11_d2-1_cCy5	\N	\N
8197	original/PLATE-1/bPLATE_wG10_s7_cRGB.png	\N	BLUE
8198	wG10_d1-3_cDAPI	\N	\N
8199	original/PLATE-1/bPLATE_wG10_s7_cRGB.png	\N	GREEN
8200	wG10_d1-3_cGFP	\N	\N
8201	original/PLATE-1/bPLATE_wG10_s7_cRGB.png	\N	RED
8202	wG10_d1-3_cCy5	\N	\N
8203	original/PLATE-1/bPLATE_wH11_s9_cRGB.png	\N	BLUE
8204	wH11_d3-3_cDAPI	\N	\N
8205	original/PLATE-1/bPLATE_wH11_s9_cRGB.png	\N	GREEN
8206	wH11_d3-3_cGFP	\N	\N
8207	original/PLATE-1/bPLATE_wH11_s9_cRGB.png	\N	RED
8208	wH11_d3-3_cCy5	\N	\N
8209	original/PLATE-1/bPLATE_wA10_s4_cRGB.png	\N	BLUE
8210	wA10_d1-2_cDAPI	\N	\N
8211	original/PLATE-1/bPLATE_wA10_s4_cRGB.png	\N	GREEN
8212	wA10_d1-2_cGFP	\N	\N
8213	original/PLATE-1/bPLATE_wA10_s4_cRGB.png	\N	RED
8214	wA10_d1-2_cCy5	\N	\N
8215	original/PLATE-1/bPLATE_wB10_s7_cRGB.png	\N	BLUE
8216	wB10_d1-3_cDAPI	\N	\N
8217	original/PLATE-1/bPLATE_wB10_s7_cRGB.png	\N	GREEN
8218	wB10_d1-3_cGFP	\N	\N
8219	original/PLATE-1/bPLATE_wB10_s7_cRGB.png	\N	RED
8220	wB10_d1-3_cCy5	\N	\N
8221	original/PLATE-1/bPLATE_wD5_s1_cRGB.png	\N	BLUE
8222	wD5_d1-1_cDAPI	\N	\N
8223	original/PLATE-1/bPLATE_wD5_s1_cRGB.png	\N	GREEN
8224	wD5_d1-1_cGFP	\N	\N
8225	original/PLATE-1/bPLATE_wD5_s1_cRGB.png	\N	RED
8226	wD5_d1-1_cCy5	\N	\N
8227	original/PLATE-1/bPLATE_wE5_s4_cRGB.png	\N	BLUE
8228	wE5_d1-2_cDAPI	\N	\N
8229	original/PLATE-1/bPLATE_wE5_s4_cRGB.png	\N	GREEN
8230	wE5_d1-2_cGFP	\N	\N
8231	original/PLATE-1/bPLATE_wE5_s4_cRGB.png	\N	RED
8232	wE5_d1-2_cCy5	\N	\N
8233	original/PLATE-1/bPLATE_wG12_s1_cRGB.png	\N	BLUE
8234	wG12_d1-1_cDAPI	\N	\N
8235	original/PLATE-1/bPLATE_wG12_s1_cRGB.png	\N	GREEN
8236	wG12_d1-1_cGFP	\N	\N
8237	original/PLATE-1/bPLATE_wG12_s1_cRGB.png	\N	RED
8238	wG12_d1-1_cCy5	\N	\N
8239	original/PLATE-1/bPLATE_wB9_s9_cRGB.png	\N	BLUE
8240	wB9_d3-3_cDAPI	\N	\N
8241	original/PLATE-1/bPLATE_wB9_s9_cRGB.png	\N	GREEN
8242	wB9_d3-3_cGFP	\N	\N
8243	original/PLATE-1/bPLATE_wB9_s9_cRGB.png	\N	RED
8244	wB9_d3-3_cCy5	\N	\N
8245	original/PLATE-1/bPLATE_wE4_s6_cRGB.png	\N	BLUE
8246	wE4_d3-2_cDAPI	\N	\N
8247	original/PLATE-1/bPLATE_wE4_s6_cRGB.png	\N	GREEN
8248	wE4_d3-2_cGFP	\N	\N
8249	original/PLATE-1/bPLATE_wE4_s6_cRGB.png	\N	RED
8250	wE4_d3-2_cCy5	\N	\N
8251	original/PLATE-1/bPLATE_wG11_s3_cRGB.png	\N	BLUE
8252	wG11_d3-1_cDAPI	\N	\N
8253	original/PLATE-1/bPLATE_wG11_s3_cRGB.png	\N	GREEN
8254	wG11_d3-1_cGFP	\N	\N
8255	original/PLATE-1/bPLATE_wG11_s3_cRGB.png	\N	RED
8256	wG11_d3-1_cCy5	\N	\N
8257	original/PLATE-1/bPLATE_wH6_s4_cRGB.png	\N	BLUE
8258	wH6_d1-2_cDAPI	\N	\N
8259	original/PLATE-1/bPLATE_wH6_s4_cRGB.png	\N	GREEN
8260	wH6_d1-2_cGFP	\N	\N
8261	original/PLATE-1/bPLATE_wH6_s4_cRGB.png	\N	RED
8262	wH6_d1-2_cCy5	\N	\N
8263	original/PLATE-1/bPLATE_wE11_s7_cRGB.png	\N	BLUE
8264	wE11_d1-3_cDAPI	\N	\N
8265	original/PLATE-1/bPLATE_wE11_s7_cRGB.png	\N	GREEN
8266	wE11_d1-3_cGFP	\N	\N
8267	original/PLATE-1/bPLATE_wE11_s7_cRGB.png	\N	RED
8268	wE11_d1-3_cCy5	\N	\N
8269	original/PLATE-1/bPLATE_wC4_s5_cRGB.png	\N	BLUE
8270	wC4_d2-2_cDAPI	\N	\N
8271	original/PLATE-1/bPLATE_wC4_s5_cRGB.png	\N	GREEN
8272	wC4_d2-2_cGFP	\N	\N
8273	original/PLATE-1/bPLATE_wC4_s5_cRGB.png	\N	RED
8274	wC4_d2-2_cCy5	\N	\N
8275	original/PLATE-1/bPLATE_wE11_s2_cRGB.png	\N	BLUE
8276	wE11_d2-1_cDAPI	\N	\N
8277	original/PLATE-1/bPLATE_wE11_s2_cRGB.png	\N	GREEN
8278	wE11_d2-1_cGFP	\N	\N
8279	original/PLATE-1/bPLATE_wE11_s2_cRGB.png	\N	RED
8280	wE11_d2-1_cCy5	\N	\N
8281	original/PLATE-1/bPLATE_wH1_s9_cRGB.png	\N	BLUE
8282	wH1_d3-3_cDAPI	\N	\N
8283	original/PLATE-1/bPLATE_wH1_s9_cRGB.png	\N	GREEN
8284	wH1_d3-3_cGFP	\N	\N
8285	original/PLATE-1/bPLATE_wH1_s9_cRGB.png	\N	RED
8286	wH1_d3-3_cCy5	\N	\N
8287	original/PLATE-1/bPLATE_wC1_s2_cRGB.png	\N	BLUE
8288	wC1_d2-1_cDAPI	\N	\N
8289	original/PLATE-1/bPLATE_wC1_s2_cRGB.png	\N	GREEN
8290	wC1_d2-1_cGFP	\N	\N
8291	original/PLATE-1/bPLATE_wC1_s2_cRGB.png	\N	RED
8292	wC1_d2-1_cCy5	\N	\N
8293	original/PLATE-1/bPLATE_wC3_s1_cRGB.png	\N	BLUE
8294	wC3_d1-1_cDAPI	\N	\N
8295	original/PLATE-1/bPLATE_wC3_s1_cRGB.png	\N	GREEN
8296	wC3_d1-1_cGFP	\N	\N
8297	original/PLATE-1/bPLATE_wC3_s1_cRGB.png	\N	RED
8298	wC3_d1-1_cCy5	\N	\N
8299	original/PLATE-1/bPLATE_wH12_s2_cRGB.png	\N	BLUE
8300	wH12_d2-1_cDAPI	\N	\N
8301	original/PLATE-1/bPLATE_wH12_s2_cRGB.png	\N	GREEN
8302	wH12_d2-1_cGFP	\N	\N
8303	original/PLATE-1/bPLATE_wH12_s2_cRGB.png	\N	RED
8304	wH12_d2-1_cCy5	\N	\N
8305	original/PLATE-1/bPLATE_wC10_s8_cRGB.png	\N	BLUE
8306	wC10_d2-3_cDAPI	\N	\N
8307	original/PLATE-1/bPLATE_wC10_s8_cRGB.png	\N	GREEN
8308	wC10_d2-3_cGFP	\N	\N
8309	original/PLATE-1/bPLATE_wC10_s8_cRGB.png	\N	RED
8310	wC10_d2-3_cCy5	\N	\N
8311	original/PLATE-1/bPLATE_wF5_s5_cRGB.png	\N	BLUE
8312	wF5_d2-2_cDAPI	\N	\N
8313	original/PLATE-1/bPLATE_wF5_s5_cRGB.png	\N	GREEN
8314	wF5_d2-2_cGFP	\N	\N
8315	original/PLATE-1/bPLATE_wF5_s5_cRGB.png	\N	RED
8316	wF5_d2-2_cCy5	\N	\N
8317	original/PLATE-1/bPLATE_wB12_s3_cRGB.png	\N	BLUE
8318	wB12_d3-1_cDAPI	\N	\N
8319	original/PLATE-1/bPLATE_wB12_s3_cRGB.png	\N	GREEN
8320	wB12_d3-1_cGFP	\N	\N
8321	original/PLATE-1/bPLATE_wB12_s3_cRGB.png	\N	RED
8322	wB12_d3-1_cCy5	\N	\N
8323	original/PLATE-1/bPLATE_wG4_s9_cRGB.png	\N	BLUE
8324	wG4_d3-3_cDAPI	\N	\N
8325	original/PLATE-1/bPLATE_wG4_s9_cRGB.png	\N	GREEN
8326	wG4_d3-3_cGFP	\N	\N
8327	original/PLATE-1/bPLATE_wG4_s9_cRGB.png	\N	RED
8328	wG4_d3-3_cCy5	\N	\N
8329	original/PLATE-1/bPLATE_wD11_s7_cRGB.png	\N	BLUE
8330	wD11_d1-3_cDAPI	\N	\N
8331	original/PLATE-1/bPLATE_wD11_s7_cRGB.png	\N	GREEN
8332	wD11_d1-3_cGFP	\N	\N
8333	original/PLATE-1/bPLATE_wD11_s7_cRGB.png	\N	RED
8334	wD11_d1-3_cCy5	\N	\N
8335	original/PLATE-1/bPLATE_wC11_s1_cRGB.png	\N	BLUE
8336	wC11_d1-1_cDAPI	\N	\N
8337	original/PLATE-1/bPLATE_wC11_s1_cRGB.png	\N	GREEN
8338	wC11_d1-1_cGFP	\N	\N
8339	original/PLATE-1/bPLATE_wC11_s1_cRGB.png	\N	RED
8340	wC11_d1-1_cCy5	\N	\N
8341	original/PLATE-1/bPLATE_wA4_s4_cRGB.png	\N	BLUE
8342	wA4_d1-2_cDAPI	\N	\N
8343	original/PLATE-1/bPLATE_wA4_s4_cRGB.png	\N	GREEN
8344	wA4_d1-2_cGFP	\N	\N
8345	original/PLATE-1/bPLATE_wA4_s4_cRGB.png	\N	RED
8346	wA4_d1-2_cCy5	\N	\N
8347	original/PLATE-1/bPLATE_wG6_s4_cRGB.png	\N	BLUE
8348	wG6_d1-2_cDAPI	\N	\N
8349	original/PLATE-1/bPLATE_wG6_s4_cRGB.png	\N	GREEN
8350	wG6_d1-2_cGFP	\N	\N
8351	original/PLATE-1/bPLATE_wG6_s4_cRGB.png	\N	RED
8352	wG6_d1-2_cCy5	\N	\N
8353	original/PLATE-1/bPLATE_wD1_s1_cRGB.png	\N	BLUE
8354	wD1_d1-1_cDAPI	\N	\N
8355	original/PLATE-1/bPLATE_wD1_s1_cRGB.png	\N	GREEN
8356	wD1_d1-1_cGFP	\N	\N
8357	original/PLATE-1/bPLATE_wD1_s1_cRGB.png	\N	RED
8358	wD1_d1-1_cCy5	\N	\N
8359	original/PLATE-1/bPLATE_wA6_s4_cRGB.png	\N	BLUE
8360	wA6_d1-2_cDAPI	\N	\N
8361	original/PLATE-1/bPLATE_wA6_s4_cRGB.png	\N	GREEN
8362	wA6_d1-2_cGFP	\N	\N
8363	original/PLATE-1/bPLATE_wA6_s4_cRGB.png	\N	RED
8364	wA6_d1-2_cCy5	\N	\N
8365	original/PLATE-1/bPLATE_wG11_s9_cRGB.png	\N	BLUE
8366	wG11_d3-3_cDAPI	\N	\N
8367	original/PLATE-1/bPLATE_wG11_s9_cRGB.png	\N	GREEN
8368	wG11_d3-3_cGFP	\N	\N
8369	original/PLATE-1/bPLATE_wG11_s9_cRGB.png	\N	RED
8370	wG11_d3-3_cCy5	\N	\N
8371	original/PLATE-1/bPLATE_wC4_s3_cRGB.png	\N	BLUE
8372	wC4_d3-1_cDAPI	\N	\N
8373	original/PLATE-1/bPLATE_wC4_s3_cRGB.png	\N	GREEN
8374	wC4_d3-1_cGFP	\N	\N
8375	original/PLATE-1/bPLATE_wC4_s3_cRGB.png	\N	RED
8376	wC4_d3-1_cCy5	\N	\N
8377	original/PLATE-1/bPLATE_wH8_s3_cRGB.png	\N	BLUE
8378	wH8_d3-1_cDAPI	\N	\N
8379	original/PLATE-1/bPLATE_wH8_s3_cRGB.png	\N	GREEN
8380	wH8_d3-1_cGFP	\N	\N
8381	original/PLATE-1/bPLATE_wH8_s3_cRGB.png	\N	RED
8382	wH8_d3-1_cCy5	\N	\N
8383	original/PLATE-1/bPLATE_wH4_s1_cRGB.png	\N	BLUE
8384	wH4_d1-1_cDAPI	\N	\N
8385	original/PLATE-1/bPLATE_wH4_s1_cRGB.png	\N	GREEN
8386	wH4_d1-1_cGFP	\N	\N
8387	original/PLATE-1/bPLATE_wH4_s1_cRGB.png	\N	RED
8388	wH4_d1-1_cCy5	\N	\N
8389	original/PLATE-1/bPLATE_wC6_s9_cRGB.png	\N	BLUE
8390	wC6_d3-3_cDAPI	\N	\N
8391	original/PLATE-1/bPLATE_wC6_s9_cRGB.png	\N	GREEN
8392	wC6_d3-3_cGFP	\N	\N
8393	original/PLATE-1/bPLATE_wC6_s9_cRGB.png	\N	RED
8394	wC6_d3-3_cCy5	\N	\N
8395	original/PLATE-1/bPLATE_wC2_s7_cRGB.png	\N	BLUE
8396	wC2_d1-3_cDAPI	\N	\N
8397	original/PLATE-1/bPLATE_wC2_s7_cRGB.png	\N	GREEN
8398	wC2_d1-3_cGFP	\N	\N
8399	original/PLATE-1/bPLATE_wC2_s7_cRGB.png	\N	RED
8400	wC2_d1-3_cCy5	\N	\N
8401	original/PLATE-1/bPLATE_wE9_s4_cRGB.png	\N	BLUE
8402	wE9_d1-2_cDAPI	\N	\N
8403	original/PLATE-1/bPLATE_wE9_s4_cRGB.png	\N	GREEN
8404	wE9_d1-2_cGFP	\N	\N
8405	original/PLATE-1/bPLATE_wE9_s4_cRGB.png	\N	RED
8406	wE9_d1-2_cCy5	\N	\N
8407	original/PLATE-1/bPLATE_wF1_s6_cRGB.png	\N	BLUE
8408	wF1_d3-2_cDAPI	\N	\N
8409	original/PLATE-1/bPLATE_wF1_s6_cRGB.png	\N	GREEN
8410	wF1_d3-2_cGFP	\N	\N
8411	original/PLATE-1/bPLATE_wF1_s6_cRGB.png	\N	RED
8412	wF1_d3-2_cCy5	\N	\N
8413	original/PLATE-1/bPLATE_wB11_s2_cRGB.png	\N	BLUE
8414	wB11_d2-1_cDAPI	\N	\N
8415	original/PLATE-1/bPLATE_wB11_s2_cRGB.png	\N	GREEN
8416	wB11_d2-1_cGFP	\N	\N
8417	original/PLATE-1/bPLATE_wB11_s2_cRGB.png	\N	RED
8418	wB11_d2-1_cCy5	\N	\N
8419	original/PLATE-1/bPLATE_wC9_s1_cRGB.png	\N	BLUE
8420	wC9_d1-1_cDAPI	\N	\N
8421	original/PLATE-1/bPLATE_wC9_s1_cRGB.png	\N	GREEN
8422	wC9_d1-1_cGFP	\N	\N
8423	original/PLATE-1/bPLATE_wC9_s1_cRGB.png	\N	RED
8424	wC9_d1-1_cCy5	\N	\N
8425	original/PLATE-1/bPLATE_wA2_s4_cRGB.png	\N	BLUE
8426	wA2_d1-2_cDAPI	\N	\N
8427	original/PLATE-1/bPLATE_wA2_s4_cRGB.png	\N	GREEN
8428	wA2_d1-2_cGFP	\N	\N
8429	original/PLATE-1/bPLATE_wA2_s4_cRGB.png	\N	RED
8430	wA2_d1-2_cCy5	\N	\N
8431	original/PLATE-1/bPLATE_wG2_s7_cRGB.png	\N	BLUE
8432	wG2_d1-3_cDAPI	\N	\N
8433	original/PLATE-1/bPLATE_wG2_s7_cRGB.png	\N	GREEN
8434	wG2_d1-3_cGFP	\N	\N
8435	original/PLATE-1/bPLATE_wG2_s7_cRGB.png	\N	RED
8436	wG2_d1-3_cCy5	\N	\N
8437	original/PLATE-1/bPLATE_wC7_s2_cRGB.png	\N	BLUE
8438	wC7_d2-1_cDAPI	\N	\N
8439	original/PLATE-1/bPLATE_wC7_s2_cRGB.png	\N	GREEN
8440	wC7_d2-1_cGFP	\N	\N
8441	original/PLATE-1/bPLATE_wC7_s2_cRGB.png	\N	RED
8442	wC7_d2-1_cCy5	\N	\N
8443	original/PLATE-1/bPLATE_wB6_s7_cRGB.png	\N	BLUE
8444	wB6_d1-3_cDAPI	\N	\N
8445	original/PLATE-1/bPLATE_wB6_s7_cRGB.png	\N	GREEN
8446	wB6_d1-3_cGFP	\N	\N
8447	original/PLATE-1/bPLATE_wB6_s7_cRGB.png	\N	RED
8448	wB6_d1-3_cCy5	\N	\N
8449	original/PLATE-1/bPLATE_wE1_s4_cRGB.png	\N	BLUE
8450	wE1_d1-2_cDAPI	\N	\N
8451	original/PLATE-1/bPLATE_wE1_s4_cRGB.png	\N	GREEN
8452	wE1_d1-2_cGFP	\N	\N
8453	original/PLATE-1/bPLATE_wE1_s4_cRGB.png	\N	RED
8454	wE1_d1-2_cCy5	\N	\N
8455	original/PLATE-1/bPLATE_wG8_s1_cRGB.png	\N	BLUE
8456	wG8_d1-1_cDAPI	\N	\N
8457	original/PLATE-1/bPLATE_wG8_s1_cRGB.png	\N	GREEN
8458	wG8_d1-1_cGFP	\N	\N
8459	original/PLATE-1/bPLATE_wG8_s1_cRGB.png	\N	RED
8460	wG8_d1-1_cCy5	\N	\N
8461	original/PLATE-1/bPLATE_wH4_s8_cRGB.png	\N	BLUE
8462	wH4_d2-3_cDAPI	\N	\N
8463	original/PLATE-1/bPLATE_wH4_s8_cRGB.png	\N	GREEN
8464	wH4_d2-3_cGFP	\N	\N
8465	original/PLATE-1/bPLATE_wH4_s8_cRGB.png	\N	RED
8466	wH4_d2-3_cCy5	\N	\N
8467	original/PLATE-1/bPLATE_wH5_s9_cRGB.png	\N	BLUE
8468	wH5_d3-3_cDAPI	\N	\N
8469	original/PLATE-1/bPLATE_wH5_s9_cRGB.png	\N	GREEN
8470	wH5_d3-3_cGFP	\N	\N
8471	original/PLATE-1/bPLATE_wH5_s9_cRGB.png	\N	RED
8472	wH5_d3-3_cCy5	\N	\N
8473	original/PLATE-1/bPLATE_wH9_s6_cRGB.png	\N	BLUE
8474	wH9_d3-2_cDAPI	\N	\N
8475	original/PLATE-1/bPLATE_wH9_s6_cRGB.png	\N	GREEN
8476	wH9_d3-2_cGFP	\N	\N
8477	original/PLATE-1/bPLATE_wH9_s6_cRGB.png	\N	RED
8478	wH9_d3-2_cCy5	\N	\N
8479	original/PLATE-1/bPLATE_wF2_s9_cRGB.png	\N	BLUE
8480	wF2_d3-3_cDAPI	\N	\N
8481	original/PLATE-1/bPLATE_wF2_s9_cRGB.png	\N	GREEN
8482	wF2_d3-3_cGFP	\N	\N
8483	original/PLATE-1/bPLATE_wF2_s9_cRGB.png	\N	RED
8484	wF2_d3-3_cCy5	\N	\N
8485	original/PLATE-1/bPLATE_wC9_s5_cRGB.png	\N	BLUE
8486	wC9_d2-2_cDAPI	\N	\N
8487	original/PLATE-1/bPLATE_wC9_s5_cRGB.png	\N	GREEN
8488	wC9_d2-2_cGFP	\N	\N
8489	original/PLATE-1/bPLATE_wC9_s5_cRGB.png	\N	RED
8490	wC9_d2-2_cCy5	\N	\N
8491	original/PLATE-1/bPLATE_wA2_s8_cRGB.png	\N	BLUE
8492	wA2_d2-3_cDAPI	\N	\N
8493	original/PLATE-1/bPLATE_wA2_s8_cRGB.png	\N	GREEN
8494	wA2_d2-3_cGFP	\N	\N
8495	original/PLATE-1/bPLATE_wA2_s8_cRGB.png	\N	RED
8496	wA2_d2-3_cCy5	\N	\N
8497	original/PLATE-1/bPLATE_wF4_s2_cRGB.png	\N	BLUE
8498	wF4_d2-1_cDAPI	\N	\N
8499	original/PLATE-1/bPLATE_wF4_s2_cRGB.png	\N	GREEN
8500	wF4_d2-1_cGFP	\N	\N
8501	original/PLATE-1/bPLATE_wF4_s2_cRGB.png	\N	RED
8502	wF4_d2-1_cCy5	\N	\N
8503	original/PLATE-1/bPLATE_wA4_s3_cRGB.png	\N	BLUE
8504	wA4_d3-1_cDAPI	\N	\N
8505	original/PLATE-1/bPLATE_wA4_s3_cRGB.png	\N	GREEN
8506	wA4_d3-1_cGFP	\N	\N
8507	original/PLATE-1/bPLATE_wA4_s3_cRGB.png	\N	RED
8508	wA4_d3-1_cCy5	\N	\N
8509	original/PLATE-1/bPLATE_wH2_s1_cRGB.png	\N	BLUE
8510	wH2_d1-1_cDAPI	\N	\N
8511	original/PLATE-1/bPLATE_wH2_s1_cRGB.png	\N	GREEN
8512	wH2_d1-1_cGFP	\N	\N
8513	original/PLATE-1/bPLATE_wH2_s1_cRGB.png	\N	RED
8514	wH2_d1-1_cCy5	\N	\N
8515	original/PLATE-1/bPLATE_wB12_s7_cRGB.png	\N	BLUE
8516	wB12_d1-3_cDAPI	\N	\N
8517	original/PLATE-1/bPLATE_wB12_s7_cRGB.png	\N	GREEN
8518	wB12_d1-3_cGFP	\N	\N
8519	original/PLATE-1/bPLATE_wB12_s7_cRGB.png	\N	RED
8520	wB12_d1-3_cCy5	\N	\N
8521	original/PLATE-1/bPLATE_wE7_s4_cRGB.png	\N	BLUE
8522	wE7_d1-2_cDAPI	\N	\N
8523	original/PLATE-1/bPLATE_wE7_s4_cRGB.png	\N	GREEN
8524	wE7_d1-2_cGFP	\N	\N
8525	original/PLATE-1/bPLATE_wE7_s4_cRGB.png	\N	RED
8526	wE7_d1-2_cCy5	\N	\N
8527	original/PLATE-1/bPLATE_wH8_s5_cRGB.png	\N	BLUE
8528	wH8_d2-2_cDAPI	\N	\N
8529	original/PLATE-1/bPLATE_wH8_s5_cRGB.png	\N	GREEN
8530	wH8_d2-2_cGFP	\N	\N
8531	original/PLATE-1/bPLATE_wH8_s5_cRGB.png	\N	RED
8532	wH8_d2-2_cCy5	\N	\N
8533	original/PLATE-1/bPLATE_wF1_s8_cRGB.png	\N	BLUE
8534	wF1_d2-3_cDAPI	\N	\N
8535	original/PLATE-1/bPLATE_wF1_s8_cRGB.png	\N	GREEN
8536	wF1_d2-3_cGFP	\N	\N
8537	original/PLATE-1/bPLATE_wF1_s8_cRGB.png	\N	RED
8538	wF1_d2-3_cCy5	\N	\N
8539	original/PLATE-1/bPLATE_wG8_s8_cRGB.png	\N	BLUE
8540	wG8_d2-3_cDAPI	\N	\N
8541	original/PLATE-1/bPLATE_wG8_s8_cRGB.png	\N	GREEN
8542	wG8_d2-3_cGFP	\N	\N
8543	original/PLATE-1/bPLATE_wG8_s8_cRGB.png	\N	RED
8544	wG8_d2-3_cCy5	\N	\N
8545	original/PLATE-1/bPLATE_wD7_s8_cRGB.png	\N	BLUE
8546	wD7_d2-3_cDAPI	\N	\N
8547	original/PLATE-1/bPLATE_wD7_s8_cRGB.png	\N	GREEN
8548	wD7_d2-3_cGFP	\N	\N
8549	original/PLATE-1/bPLATE_wD7_s8_cRGB.png	\N	RED
8550	wD7_d2-3_cCy5	\N	\N
8551	original/PLATE-1/bPLATE_wG2_s5_cRGB.png	\N	BLUE
8552	wG2_d2-2_cDAPI	\N	\N
8553	original/PLATE-1/bPLATE_wG2_s5_cRGB.png	\N	GREEN
8554	wG2_d2-2_cGFP	\N	\N
8555	original/PLATE-1/bPLATE_wG2_s5_cRGB.png	\N	RED
8556	wG2_d2-2_cCy5	\N	\N
8557	original/PLATE-1/bPLATE_wB9_s3_cRGB.png	\N	BLUE
8558	wB9_d3-1_cDAPI	\N	\N
8559	original/PLATE-1/bPLATE_wB9_s3_cRGB.png	\N	GREEN
8560	wB9_d3-1_cGFP	\N	\N
8561	original/PLATE-1/bPLATE_wB9_s3_cRGB.png	\N	RED
8562	wB9_d3-1_cCy5	\N	\N
8563	original/PLATE-1/bPLATE_wH6_s8_cRGB.png	\N	BLUE
8564	wH6_d2-3_cDAPI	\N	\N
8565	original/PLATE-1/bPLATE_wH6_s8_cRGB.png	\N	GREEN
8566	wH6_d2-3_cGFP	\N	\N
8567	original/PLATE-1/bPLATE_wH6_s8_cRGB.png	\N	RED
8568	wH6_d2-3_cCy5	\N	\N
8569	original/PLATE-1/bPLATE_wD12_s8_cRGB.png	\N	BLUE
8570	wD12_d2-3_cDAPI	\N	\N
8571	original/PLATE-1/bPLATE_wD12_s8_cRGB.png	\N	GREEN
8572	wD12_d2-3_cGFP	\N	\N
8573	original/PLATE-1/bPLATE_wD12_s8_cRGB.png	\N	RED
8574	wD12_d2-3_cCy5	\N	\N
8575	original/PLATE-1/bPLATE_wG7_s5_cRGB.png	\N	BLUE
8576	wG7_d2-2_cDAPI	\N	\N
8577	original/PLATE-1/bPLATE_wG7_s5_cRGB.png	\N	GREEN
8578	wG7_d2-2_cGFP	\N	\N
8579	original/PLATE-1/bPLATE_wG7_s5_cRGB.png	\N	RED
8580	wG7_d2-2_cCy5	\N	\N
8581	original/PLATE-1/bPLATE_wB2_s7_cRGB.png	\N	BLUE
8582	wB2_d1-3_cDAPI	\N	\N
8583	original/PLATE-1/bPLATE_wB2_s7_cRGB.png	\N	GREEN
8584	wB2_d1-3_cGFP	\N	\N
8585	original/PLATE-1/bPLATE_wB2_s7_cRGB.png	\N	RED
8586	wB2_d1-3_cCy5	\N	\N
8587	original/PLATE-1/bPLATE_wD9_s4_cRGB.png	\N	BLUE
8588	wD9_d1-2_cDAPI	\N	\N
8589	original/PLATE-1/bPLATE_wD9_s4_cRGB.png	\N	GREEN
8590	wD9_d1-2_cGFP	\N	\N
8591	original/PLATE-1/bPLATE_wD9_s4_cRGB.png	\N	RED
8592	wD9_d1-2_cCy5	\N	\N
8593	original/PLATE-1/bPLATE_wG4_s1_cRGB.png	\N	BLUE
8594	wG4_d1-1_cDAPI	\N	\N
8595	original/PLATE-1/bPLATE_wG4_s1_cRGB.png	\N	GREEN
8596	wG4_d1-1_cGFP	\N	\N
8597	original/PLATE-1/bPLATE_wG4_s1_cRGB.png	\N	RED
8598	wG4_d1-1_cCy5	\N	\N
8599	original/PLATE-1/bPLATE_wF6_s7_cRGB.png	\N	BLUE
8600	wF6_d1-3_cDAPI	\N	\N
8601	original/PLATE-1/bPLATE_wF6_s7_cRGB.png	\N	GREEN
8602	wF6_d1-3_cGFP	\N	\N
8603	original/PLATE-1/bPLATE_wF6_s7_cRGB.png	\N	RED
8604	wF6_d1-3_cCy5	\N	\N
8605	original/PLATE-1/bPLATE_wB6_s1_cRGB.png	\N	BLUE
8606	wB6_d1-1_cDAPI	\N	\N
8607	original/PLATE-1/bPLATE_wB6_s1_cRGB.png	\N	GREEN
8608	wB6_d1-1_cGFP	\N	\N
8609	original/PLATE-1/bPLATE_wB6_s1_cRGB.png	\N	RED
8610	wB6_d1-1_cCy5	\N	\N
8611	original/PLATE-1/bPLATE_wC1_s5_cRGB.png	\N	BLUE
8612	wC1_d2-2_cDAPI	\N	\N
8613	original/PLATE-1/bPLATE_wC1_s5_cRGB.png	\N	GREEN
8614	wC1_d2-2_cGFP	\N	\N
8615	original/PLATE-1/bPLATE_wC1_s5_cRGB.png	\N	RED
8616	wC1_d2-2_cCy5	\N	\N
8617	original/PLATE-1/bPLATE_wE8_s2_cRGB.png	\N	BLUE
8618	wE8_d2-1_cDAPI	\N	\N
8619	original/PLATE-1/bPLATE_wE8_s2_cRGB.png	\N	GREEN
8620	wE8_d2-1_cGFP	\N	\N
8621	original/PLATE-1/bPLATE_wE8_s2_cRGB.png	\N	RED
8622	wE8_d2-1_cCy5	\N	\N
8623	original/PLATE-1/bPLATE_wB9_s7_cRGB.png	\N	BLUE
8624	wB9_d1-3_cDAPI	\N	\N
8625	original/PLATE-1/bPLATE_wB9_s7_cRGB.png	\N	GREEN
8626	wB9_d1-3_cGFP	\N	\N
8627	original/PLATE-1/bPLATE_wB9_s7_cRGB.png	\N	RED
8628	wB9_d1-3_cCy5	\N	\N
8629	original/PLATE-1/bPLATE_wE4_s4_cRGB.png	\N	BLUE
8630	wE4_d1-2_cDAPI	\N	\N
8631	original/PLATE-1/bPLATE_wE4_s4_cRGB.png	\N	GREEN
8632	wE4_d1-2_cGFP	\N	\N
8633	original/PLATE-1/bPLATE_wE4_s4_cRGB.png	\N	RED
8634	wE4_d1-2_cCy5	\N	\N
8635	original/PLATE-1/bPLATE_wG11_s1_cRGB.png	\N	BLUE
8636	wG11_d1-1_cDAPI	\N	\N
8637	original/PLATE-1/bPLATE_wG11_s1_cRGB.png	\N	GREEN
8638	wG11_d1-1_cGFP	\N	\N
8639	original/PLATE-1/bPLATE_wG11_s1_cRGB.png	\N	RED
8640	wG11_d1-1_cCy5	\N	\N
8641	original/PLATE-1/bPLATE_wH5_s7_cRGB.png	\N	BLUE
8642	wH5_d1-3_cDAPI	\N	\N
8643	original/PLATE-1/bPLATE_wH5_s7_cRGB.png	\N	GREEN
8644	wH5_d1-3_cGFP	\N	\N
8645	original/PLATE-1/bPLATE_wH5_s7_cRGB.png	\N	RED
8646	wH5_d1-3_cCy5	\N	\N
8647	original/PLATE-1/bPLATE_wD2_s9_cRGB.png	\N	BLUE
8648	wD2_d3-3_cDAPI	\N	\N
8649	original/PLATE-1/bPLATE_wD2_s9_cRGB.png	\N	GREEN
8650	wD2_d3-3_cGFP	\N	\N
8651	original/PLATE-1/bPLATE_wD2_s9_cRGB.png	\N	RED
8652	wD2_d3-3_cCy5	\N	\N
8653	original/PLATE-1/bPLATE_wF9_s6_cRGB.png	\N	BLUE
8654	wF9_d3-2_cDAPI	\N	\N
8655	original/PLATE-1/bPLATE_wF9_s6_cRGB.png	\N	GREEN
8656	wF9_d3-2_cGFP	\N	\N
8657	original/PLATE-1/bPLATE_wF9_s6_cRGB.png	\N	RED
8658	wF9_d3-2_cCy5	\N	\N
8659	original/PLATE-1/bPLATE_wH11_s5_cRGB.png	\N	BLUE
8660	wH11_d2-2_cDAPI	\N	\N
8661	original/PLATE-1/bPLATE_wH11_s5_cRGB.png	\N	GREEN
8662	wH11_d2-2_cGFP	\N	\N
8663	original/PLATE-1/bPLATE_wH11_s5_cRGB.png	\N	RED
8664	wH11_d2-2_cCy5	\N	\N
8665	original/PLATE-1/bPLATE_wF4_s8_cRGB.png	\N	BLUE
8666	wF4_d2-3_cDAPI	\N	\N
8667	original/PLATE-1/bPLATE_wF4_s8_cRGB.png	\N	GREEN
8668	wF4_d2-3_cGFP	\N	\N
8669	original/PLATE-1/bPLATE_wF4_s8_cRGB.png	\N	RED
8670	wF4_d2-3_cCy5	\N	\N
8671	original/PLATE-1/bPLATE_wD10_s6_cRGB.png	\N	BLUE
8672	wD10_d3-2_cDAPI	\N	\N
8673	original/PLATE-1/bPLATE_wD10_s6_cRGB.png	\N	GREEN
8674	wD10_d3-2_cGFP	\N	\N
8675	original/PLATE-1/bPLATE_wD10_s6_cRGB.png	\N	RED
8676	wD10_d3-2_cCy5	\N	\N
8677	original/PLATE-1/bPLATE_wB7_s5_cRGB.png	\N	BLUE
8678	wB7_d2-2_cDAPI	\N	\N
8679	original/PLATE-1/bPLATE_wB7_s5_cRGB.png	\N	GREEN
8680	wB7_d2-2_cGFP	\N	\N
8681	original/PLATE-1/bPLATE_wB7_s5_cRGB.png	\N	RED
8682	wB7_d2-2_cCy5	\N	\N
8683	original/PLATE-1/bPLATE_wB3_s9_cRGB.png	\N	BLUE
8684	wB3_d3-3_cDAPI	\N	\N
8685	original/PLATE-1/bPLATE_wB3_s9_cRGB.png	\N	GREEN
8686	wB3_d3-3_cGFP	\N	\N
8687	original/PLATE-1/bPLATE_wB3_s9_cRGB.png	\N	RED
8688	wB3_d3-3_cCy5	\N	\N
8689	original/PLATE-1/bPLATE_wE2_s2_cRGB.png	\N	BLUE
8690	wE2_d2-1_cDAPI	\N	\N
8691	original/PLATE-1/bPLATE_wE2_s2_cRGB.png	\N	GREEN
8692	wE2_d2-1_cGFP	\N	\N
8693	original/PLATE-1/bPLATE_wE2_s2_cRGB.png	\N	RED
8694	wE2_d2-1_cCy5	\N	\N
8695	original/PLATE-1/bPLATE_wG5_s3_cRGB.png	\N	BLUE
8696	wG5_d3-1_cDAPI	\N	\N
8697	original/PLATE-1/bPLATE_wG5_s3_cRGB.png	\N	GREEN
8698	wG5_d3-1_cGFP	\N	\N
8699	original/PLATE-1/bPLATE_wG5_s3_cRGB.png	\N	RED
8700	wG5_d3-1_cCy5	\N	\N
8701	original/PLATE-1/bPLATE_wC12_s2_cRGB.png	\N	BLUE
8702	wC12_d2-1_cDAPI	\N	\N
8703	original/PLATE-1/bPLATE_wC12_s2_cRGB.png	\N	GREEN
8704	wC12_d2-1_cGFP	\N	\N
8705	original/PLATE-1/bPLATE_wC12_s2_cRGB.png	\N	RED
8706	wC12_d2-1_cCy5	\N	\N
8707	original/PLATE-1/bPLATE_wA5_s5_cRGB.png	\N	BLUE
8708	wA5_d2-2_cDAPI	\N	\N
8709	original/PLATE-1/bPLATE_wA5_s5_cRGB.png	\N	GREEN
8710	wA5_d2-2_cGFP	\N	\N
8711	original/PLATE-1/bPLATE_wA5_s5_cRGB.png	\N	RED
8712	wA5_d2-2_cCy5	\N	\N
8713	original/PLATE-1/bPLATE_wD2_s4_cRGB.png	\N	BLUE
8714	wD2_d1-2_cDAPI	\N	\N
8715	original/PLATE-1/bPLATE_wD2_s4_cRGB.png	\N	GREEN
8716	wD2_d1-2_cGFP	\N	\N
8717	original/PLATE-1/bPLATE_wD2_s4_cRGB.png	\N	RED
8718	wD2_d1-2_cCy5	\N	\N
8719	original/PLATE-1/bPLATE_wA7_s7_cRGB.png	\N	BLUE
8720	wA7_d1-3_cDAPI	\N	\N
8721	original/PLATE-1/bPLATE_wA7_s7_cRGB.png	\N	GREEN
8722	wA7_d1-3_cGFP	\N	\N
8723	original/PLATE-1/bPLATE_wA7_s7_cRGB.png	\N	RED
8724	wA7_d1-3_cCy5	\N	\N
8725	original/PLATE-1/bPLATE_wF9_s1_cRGB.png	\N	BLUE
8726	wF9_d1-1_cDAPI	\N	\N
8727	original/PLATE-1/bPLATE_wF9_s1_cRGB.png	\N	GREEN
8728	wF9_d1-1_cGFP	\N	\N
8729	original/PLATE-1/bPLATE_wF9_s1_cRGB.png	\N	RED
8730	wF9_d1-1_cCy5	\N	\N
8731	original/PLATE-1/bPLATE_wC6_s3_cRGB.png	\N	BLUE
8732	wC6_d3-1_cDAPI	\N	\N
8733	original/PLATE-1/bPLATE_wC6_s3_cRGB.png	\N	GREEN
8734	wC6_d3-1_cGFP	\N	\N
8735	original/PLATE-1/bPLATE_wC6_s3_cRGB.png	\N	RED
8736	wC6_d3-1_cCy5	\N	\N
8737	original/PLATE-1/bPLATE_wF9_s9_cRGB.png	\N	BLUE
8738	wF9_d3-3_cDAPI	\N	\N
8739	original/PLATE-1/bPLATE_wF9_s9_cRGB.png	\N	GREEN
8740	wF9_d3-3_cGFP	\N	\N
8741	original/PLATE-1/bPLATE_wF9_s9_cRGB.png	\N	RED
8742	wF9_d3-3_cCy5	\N	\N
8743	original/PLATE-1/bPLATE_wH12_s6_cRGB.png	\N	BLUE
8744	wH12_d3-2_cDAPI	\N	\N
8745	original/PLATE-1/bPLATE_wH12_s6_cRGB.png	\N	GREEN
8746	wH12_d3-2_cGFP	\N	\N
8747	original/PLATE-1/bPLATE_wH12_s6_cRGB.png	\N	RED
8748	wH12_d3-2_cCy5	\N	\N
8749	original/PLATE-1/bPLATE_wF5_s9_cRGB.png	\N	BLUE
8750	wF5_d3-3_cDAPI	\N	\N
8751	original/PLATE-1/bPLATE_wF5_s9_cRGB.png	\N	GREEN
8752	wF5_d3-3_cGFP	\N	\N
8753	original/PLATE-1/bPLATE_wF5_s9_cRGB.png	\N	RED
8754	wF5_d3-3_cCy5	\N	\N
8755	original/PLATE-1/bPLATE_wD10_s4_cRGB.png	\N	BLUE
8756	wD10_d1-2_cDAPI	\N	\N
8757	original/PLATE-1/bPLATE_wD10_s4_cRGB.png	\N	GREEN
8758	wD10_d1-2_cGFP	\N	\N
8759	original/PLATE-1/bPLATE_wD10_s4_cRGB.png	\N	RED
8760	wD10_d1-2_cCy5	\N	\N
8761	original/PLATE-1/bPLATE_wB3_s7_cRGB.png	\N	BLUE
8762	wB3_d1-3_cDAPI	\N	\N
8763	original/PLATE-1/bPLATE_wB3_s7_cRGB.png	\N	GREEN
8764	wB3_d1-3_cGFP	\N	\N
8765	original/PLATE-1/bPLATE_wB3_s7_cRGB.png	\N	RED
8766	wB3_d1-3_cCy5	\N	\N
8767	original/PLATE-1/bPLATE_wG5_s1_cRGB.png	\N	BLUE
8768	wG5_d1-1_cDAPI	\N	\N
8769	original/PLATE-1/bPLATE_wG5_s1_cRGB.png	\N	GREEN
8770	wG5_d1-1_cGFP	\N	\N
8771	original/PLATE-1/bPLATE_wG5_s1_cRGB.png	\N	RED
8772	wG5_d1-1_cCy5	\N	\N
8773	original/PLATE-1/bPLATE_wD10_s9_cRGB.png	\N	BLUE
8774	wD10_d3-3_cDAPI	\N	\N
8775	original/PLATE-1/bPLATE_wD10_s9_cRGB.png	\N	GREEN
8776	wD10_d3-3_cGFP	\N	\N
8777	original/PLATE-1/bPLATE_wD10_s9_cRGB.png	\N	RED
8778	wD10_d3-3_cCy5	\N	\N
8779	original/PLATE-1/bPLATE_wC1_s4_cRGB.png	\N	BLUE
8780	wC1_d1-2_cDAPI	\N	\N
8781	original/PLATE-1/bPLATE_wC1_s4_cRGB.png	\N	GREEN
8782	wC1_d1-2_cGFP	\N	\N
8783	original/PLATE-1/bPLATE_wC1_s4_cRGB.png	\N	RED
8784	wC1_d1-2_cCy5	\N	\N
8785	original/PLATE-1/bPLATE_wE8_s1_cRGB.png	\N	BLUE
8786	wE8_d1-1_cDAPI	\N	\N
8787	original/PLATE-1/bPLATE_wE8_s1_cRGB.png	\N	GREEN
8788	wE8_d1-1_cGFP	\N	\N
8789	original/PLATE-1/bPLATE_wE8_s1_cRGB.png	\N	RED
8790	wE8_d1-1_cCy5	\N	\N
8791	original/PLATE-1/bPLATE_wG5_s6_cRGB.png	\N	BLUE
8792	wG5_d3-2_cDAPI	\N	\N
8793	original/PLATE-1/bPLATE_wG5_s6_cRGB.png	\N	GREEN
8794	wG5_d3-2_cGFP	\N	\N
8795	original/PLATE-1/bPLATE_wG5_s6_cRGB.png	\N	RED
8796	wG5_d3-2_cCy5	\N	\N
8797	original/PLATE-1/bPLATE_wB8_s3_cRGB.png	\N	BLUE
8798	wB8_d3-1_cDAPI	\N	\N
8799	original/PLATE-1/bPLATE_wB8_s3_cRGB.png	\N	GREEN
8800	wB8_d3-1_cGFP	\N	\N
8801	original/PLATE-1/bPLATE_wB8_s3_cRGB.png	\N	RED
8802	wB8_d3-1_cCy5	\N	\N
8803	original/PLATE-1/bPLATE_wB7_s2_cRGB.png	\N	BLUE
8804	wB7_d2-1_cDAPI	\N	\N
8805	original/PLATE-1/bPLATE_wB7_s2_cRGB.png	\N	GREEN
8806	wB7_d2-1_cGFP	\N	\N
8807	original/PLATE-1/bPLATE_wB7_s2_cRGB.png	\N	RED
8808	wB7_d2-1_cCy5	\N	\N
8809	original/PLATE-1/bPLATE_wD2_s7_cRGB.png	\N	BLUE
8810	wD2_d1-3_cDAPI	\N	\N
8811	original/PLATE-1/bPLATE_wD2_s7_cRGB.png	\N	GREEN
8812	wD2_d1-3_cGFP	\N	\N
8813	original/PLATE-1/bPLATE_wD2_s7_cRGB.png	\N	RED
8814	wD2_d1-3_cCy5	\N	\N
8815	original/PLATE-1/bPLATE_wA11_s7_cRGB.png	\N	BLUE
8816	wA11_d1-3_cDAPI	\N	\N
8817	original/PLATE-1/bPLATE_wA11_s7_cRGB.png	\N	GREEN
8818	wA11_d1-3_cGFP	\N	\N
8819	original/PLATE-1/bPLATE_wA11_s7_cRGB.png	\N	RED
8820	wA11_d1-3_cCy5	\N	\N
8821	original/PLATE-1/bPLATE_wD6_s4_cRGB.png	\N	BLUE
8822	wD6_d1-2_cDAPI	\N	\N
8823	original/PLATE-1/bPLATE_wD6_s4_cRGB.png	\N	GREEN
8824	wD6_d1-2_cGFP	\N	\N
8825	original/PLATE-1/bPLATE_wD6_s4_cRGB.png	\N	RED
8826	wD6_d1-2_cCy5	\N	\N
8827	original/PLATE-1/bPLATE_wF9_s4_cRGB.png	\N	BLUE
8828	wF9_d1-2_cDAPI	\N	\N
8829	original/PLATE-1/bPLATE_wF9_s4_cRGB.png	\N	GREEN
8830	wF9_d1-2_cGFP	\N	\N
8831	original/PLATE-1/bPLATE_wF9_s4_cRGB.png	\N	RED
8832	wF9_d1-2_cCy5	\N	\N
8833	original/PLATE-1/bPLATE_wG1_s1_cRGB.png	\N	BLUE
8834	wG1_d1-1_cDAPI	\N	\N
8835	original/PLATE-1/bPLATE_wG1_s1_cRGB.png	\N	GREEN
8836	wG1_d1-1_cGFP	\N	\N
8837	original/PLATE-1/bPLATE_wG1_s1_cRGB.png	\N	RED
8838	wG1_d1-1_cCy5	\N	\N
8839	original/PLATE-1/bPLATE_wD6_s9_cRGB.png	\N	BLUE
8840	wD6_d3-3_cDAPI	\N	\N
8841	original/PLATE-1/bPLATE_wD6_s9_cRGB.png	\N	GREEN
8842	wD6_d3-3_cGFP	\N	\N
8843	original/PLATE-1/bPLATE_wD6_s9_cRGB.png	\N	RED
8844	wD6_d3-3_cCy5	\N	\N
8845	original/PLATE-1/bPLATE_wG1_s6_cRGB.png	\N	BLUE
8846	wG1_d3-2_cDAPI	\N	\N
8847	original/PLATE-1/bPLATE_wG1_s6_cRGB.png	\N	GREEN
8848	wG1_d3-2_cGFP	\N	\N
8849	original/PLATE-1/bPLATE_wG1_s6_cRGB.png	\N	RED
8850	wG1_d3-2_cCy5	\N	\N
8851	original/PLATE-1/bPLATE_wA7_s3_cRGB.png	\N	BLUE
8852	wA7_d3-1_cDAPI	\N	\N
8853	original/PLATE-1/bPLATE_wA7_s3_cRGB.png	\N	GREEN
8854	wA7_d3-1_cGFP	\N	\N
8855	original/PLATE-1/bPLATE_wA7_s3_cRGB.png	\N	RED
8856	wA7_d3-1_cCy5	\N	\N
8857	original/PLATE-1/bPLATE_wB8_s6_cRGB.png	\N	BLUE
8858	wB8_d3-2_cDAPI	\N	\N
8859	original/PLATE-1/bPLATE_wB8_s6_cRGB.png	\N	GREEN
8860	wB8_d3-2_cGFP	\N	\N
8861	original/PLATE-1/bPLATE_wB8_s6_cRGB.png	\N	RED
8862	wB8_d3-2_cCy5	\N	\N
8863	original/PLATE-1/bPLATE_wB11_s5_cRGB.png	\N	BLUE
8864	wB11_d2-2_cDAPI	\N	\N
8865	original/PLATE-1/bPLATE_wB11_s5_cRGB.png	\N	GREEN
8866	wB11_d2-2_cGFP	\N	\N
8867	original/PLATE-1/bPLATE_wB11_s5_cRGB.png	\N	RED
8868	wB11_d2-2_cCy5	\N	\N
8869	original/PLATE-1/bPLATE_wE3_s3_cRGB.png	\N	BLUE
8870	wE3_d3-1_cDAPI	\N	\N
8871	original/PLATE-1/bPLATE_wE3_s3_cRGB.png	\N	GREEN
8872	wE3_d3-1_cGFP	\N	\N
8873	original/PLATE-1/bPLATE_wE3_s3_cRGB.png	\N	RED
8874	wE3_d3-1_cCy5	\N	\N
8875	original/PLATE-1/bPLATE_wE6_s2_cRGB.png	\N	BLUE
8876	wE6_d2-1_cDAPI	\N	\N
8877	original/PLATE-1/bPLATE_wE6_s2_cRGB.png	\N	GREEN
8878	wE6_d2-1_cGFP	\N	\N
8879	original/PLATE-1/bPLATE_wE6_s2_cRGB.png	\N	RED
8880	wE6_d2-1_cCy5	\N	\N
8881	original/PLATE-1/bPLATE_wC3_s2_cRGB.png	\N	BLUE
8882	wC3_d2-1_cDAPI	\N	\N
8883	original/PLATE-1/bPLATE_wC3_s2_cRGB.png	\N	GREEN
8884	wC3_d2-1_cGFP	\N	\N
8885	original/PLATE-1/bPLATE_wC3_s2_cRGB.png	\N	RED
8886	wC3_d2-1_cCy5	\N	\N
8887	original/PLATE-1/bPLATE_wB9_s5_cRGB.png	\N	BLUE
8888	wB9_d2-2_cDAPI	\N	\N
8889	original/PLATE-1/bPLATE_wB9_s5_cRGB.png	\N	GREEN
8890	wB9_d2-2_cGFP	\N	\N
8891	original/PLATE-1/bPLATE_wB9_s5_cRGB.png	\N	RED
8892	wB9_d2-2_cCy5	\N	\N
8893	original/PLATE-1/bPLATE_wE4_s2_cRGB.png	\N	BLUE
8894	wE4_d2-1_cDAPI	\N	\N
8895	original/PLATE-1/bPLATE_wE4_s2_cRGB.png	\N	GREEN
8896	wE4_d2-1_cGFP	\N	\N
8897	original/PLATE-1/bPLATE_wE4_s2_cRGB.png	\N	RED
8898	wE4_d2-1_cCy5	\N	\N
8899	original/PLATE-1/bPLATE_wG1_s8_cRGB.png	\N	BLUE
8900	wG1_d2-3_cDAPI	\N	\N
8901	original/PLATE-1/bPLATE_wG1_s8_cRGB.png	\N	GREEN
8902	wG1_d2-3_cGFP	\N	\N
8903	original/PLATE-1/bPLATE_wG1_s8_cRGB.png	\N	RED
8904	wG1_d2-3_cCy5	\N	\N
8905	original/PLATE-1/bPLATE_wD5_s8_cRGB.png	\N	BLUE
8906	wD5_d2-3_cDAPI	\N	\N
8907	original/PLATE-1/bPLATE_wD5_s8_cRGB.png	\N	GREEN
8908	wD5_d2-3_cGFP	\N	\N
8909	original/PLATE-1/bPLATE_wD5_s8_cRGB.png	\N	RED
8910	wD5_d2-3_cCy5	\N	\N
8911	original/PLATE-1/bPLATE_wF12_s5_cRGB.png	\N	BLUE
8912	wF12_d2-2_cDAPI	\N	\N
8913	original/PLATE-1/bPLATE_wF12_s5_cRGB.png	\N	GREEN
8914	wF12_d2-2_cGFP	\N	\N
8915	original/PLATE-1/bPLATE_wF12_s5_cRGB.png	\N	RED
8916	wF12_d2-2_cCy5	\N	\N
8917	original/PLATE-1/bPLATE_wB2_s6_cRGB.png	\N	BLUE
8918	wB2_d3-2_cDAPI	\N	\N
8919	original/PLATE-1/bPLATE_wB2_s6_cRGB.png	\N	GREEN
8920	wB2_d3-2_cGFP	\N	\N
8921	original/PLATE-1/bPLATE_wB2_s6_cRGB.png	\N	RED
8922	wB2_d3-2_cCy5	\N	\N
8923	original/PLATE-1/bPLATE_wD9_s3_cRGB.png	\N	BLUE
8924	wD9_d3-1_cDAPI	\N	\N
8925	original/PLATE-1/bPLATE_wD9_s3_cRGB.png	\N	GREEN
8926	wD9_d3-1_cGFP	\N	\N
8927	original/PLATE-1/bPLATE_wD9_s3_cRGB.png	\N	RED
8928	wD9_d3-1_cCy5	\N	\N
8929	original/PLATE-1/bPLATE_wA4_s2_cRGB.png	\N	BLUE
8930	wA4_d2-1_cDAPI	\N	\N
8931	original/PLATE-1/bPLATE_wA4_s2_cRGB.png	\N	GREEN
8932	wA4_d2-1_cGFP	\N	\N
8933	original/PLATE-1/bPLATE_wA4_s2_cRGB.png	\N	RED
8934	wA4_d2-1_cCy5	\N	\N
8935	original/PLATE-1/bPLATE_wD4_s4_cRGB.png	\N	BLUE
8936	wD4_d1-2_cDAPI	\N	\N
8937	original/PLATE-1/bPLATE_wD4_s4_cRGB.png	\N	GREEN
8938	wD4_d1-2_cGFP	\N	\N
8939	original/PLATE-1/bPLATE_wD4_s4_cRGB.png	\N	RED
8940	wD4_d1-2_cCy5	\N	\N
8941	original/PLATE-1/bPLATE_wA6_s2_cRGB.png	\N	BLUE
8942	wA6_d2-1_cDAPI	\N	\N
8943	original/PLATE-1/bPLATE_wA6_s2_cRGB.png	\N	GREEN
8944	wA6_d2-1_cGFP	\N	\N
8945	original/PLATE-1/bPLATE_wA6_s2_cRGB.png	\N	RED
8946	wA6_d2-1_cCy5	\N	\N
8947	original/PLATE-1/bPLATE_wA9_s7_cRGB.png	\N	BLUE
8948	wA9_d1-3_cDAPI	\N	\N
8949	original/PLATE-1/bPLATE_wA9_s7_cRGB.png	\N	GREEN
8950	wA9_d1-3_cGFP	\N	\N
8951	original/PLATE-1/bPLATE_wA9_s7_cRGB.png	\N	RED
8952	wA9_d1-3_cCy5	\N	\N
8953	original/PLATE-1/bPLATE_wF11_s1_cRGB.png	\N	BLUE
8954	wF11_d1-1_cDAPI	\N	\N
8955	original/PLATE-1/bPLATE_wF11_s1_cRGB.png	\N	GREEN
8956	wF11_d1-1_cGFP	\N	\N
8957	original/PLATE-1/bPLATE_wF11_s1_cRGB.png	\N	RED
8958	wF11_d1-1_cCy5	\N	\N
8959	original/PLATE-1/bPLATE_wH6_s6_cRGB.png	\N	BLUE
8960	wH6_d3-2_cDAPI	\N	\N
8961	original/PLATE-1/bPLATE_wH6_s6_cRGB.png	\N	GREEN
8962	wH6_d3-2_cGFP	\N	\N
8963	original/PLATE-1/bPLATE_wH6_s6_cRGB.png	\N	RED
8964	wH6_d3-2_cCy5	\N	\N
8965	original/PLATE-1/bPLATE_wB10_s6_cRGB.png	\N	BLUE
8966	wB10_d3-2_cDAPI	\N	\N
8967	original/PLATE-1/bPLATE_wB10_s6_cRGB.png	\N	GREEN
8968	wB10_d3-2_cGFP	\N	\N
8969	original/PLATE-1/bPLATE_wB10_s6_cRGB.png	\N	RED
8970	wB10_d3-2_cCy5	\N	\N
8971	original/PLATE-1/bPLATE_wE11_s9_cRGB.png	\N	BLUE
8972	wE11_d3-3_cDAPI	\N	\N
8973	original/PLATE-1/bPLATE_wE11_s9_cRGB.png	\N	GREEN
8974	wE11_d3-3_cGFP	\N	\N
8975	original/PLATE-1/bPLATE_wE11_s9_cRGB.png	\N	RED
8976	wE11_d3-3_cCy5	\N	\N
8977	original/PLATE-1/bPLATE_wE5_s3_cRGB.png	\N	BLUE
8978	wE5_d3-1_cDAPI	\N	\N
8979	original/PLATE-1/bPLATE_wE5_s3_cRGB.png	\N	GREEN
8980	wE5_d3-1_cGFP	\N	\N
8981	original/PLATE-1/bPLATE_wE5_s3_cRGB.png	\N	RED
8982	wE5_d3-1_cCy5	\N	\N
8983	original/PLATE-1/bPLATE_wH3_s4_cRGB.png	\N	BLUE
8984	wH3_d1-2_cDAPI	\N	\N
8985	original/PLATE-1/bPLATE_wH3_s4_cRGB.png	\N	GREEN
8986	wH3_d1-2_cGFP	\N	\N
8987	original/PLATE-1/bPLATE_wH3_s4_cRGB.png	\N	RED
8988	wH3_d1-2_cCy5	\N	\N
8989	original/PLATE-1/bPLATE_wE8_s7_cRGB.png	\N	BLUE
8990	wE8_d1-3_cDAPI	\N	\N
8991	original/PLATE-1/bPLATE_wE8_s7_cRGB.png	\N	GREEN
8992	wE8_d1-3_cGFP	\N	\N
8993	original/PLATE-1/bPLATE_wE8_s7_cRGB.png	\N	RED
8994	wE8_d1-3_cCy5	\N	\N
8995	original/PLATE-1/bPLATE_wH1_s4_cRGB.png	\N	BLUE
8996	wH1_d1-2_cDAPI	\N	\N
8997	original/PLATE-1/bPLATE_wH1_s4_cRGB.png	\N	GREEN
8998	wH1_d1-2_cGFP	\N	\N
8999	original/PLATE-1/bPLATE_wH1_s4_cRGB.png	\N	RED
9000	wH1_d1-2_cCy5	\N	\N
9001	original/PLATE-1/bPLATE_wA1_s1_cRGB.png	\N	BLUE
9002	wA1_d1-1_cDAPI	\N	\N
9003	original/PLATE-1/bPLATE_wA1_s1_cRGB.png	\N	GREEN
9004	wA1_d1-1_cGFP	\N	\N
9005	original/PLATE-1/bPLATE_wA1_s1_cRGB.png	\N	RED
9006	wA1_d1-1_cCy5	\N	\N
9007	original/PLATE-1/bPLATE_wB6_s6_cRGB.png	\N	BLUE
9008	wB6_d3-2_cDAPI	\N	\N
9009	original/PLATE-1/bPLATE_wB6_s6_cRGB.png	\N	GREEN
9010	wB6_d3-2_cGFP	\N	\N
9011	original/PLATE-1/bPLATE_wB6_s6_cRGB.png	\N	RED
9012	wB6_d3-2_cCy5	\N	\N
9013	original/PLATE-1/bPLATE_wE1_s3_cRGB.png	\N	BLUE
9014	wE1_d3-1_cDAPI	\N	\N
9015	original/PLATE-1/bPLATE_wE1_s3_cRGB.png	\N	GREEN
9016	wE1_d3-1_cGFP	\N	\N
9017	original/PLATE-1/bPLATE_wE1_s3_cRGB.png	\N	RED
9018	wE1_d3-1_cCy5	\N	\N
9019	original/PLATE-1/bPLATE_wE6_s7_cRGB.png	\N	BLUE
9020	wE6_d1-3_cDAPI	\N	\N
9021	original/PLATE-1/bPLATE_wE6_s7_cRGB.png	\N	GREEN
9022	wE6_d1-3_cGFP	\N	\N
9023	original/PLATE-1/bPLATE_wE6_s7_cRGB.png	\N	RED
9024	wE6_d1-3_cCy5	\N	\N
9025	original/PLATE-1/bPLATE_wB10_s4_cRGB.png	\N	BLUE
9026	wB10_d1-2_cDAPI	\N	\N
9027	original/PLATE-1/bPLATE_wB10_s4_cRGB.png	\N	GREEN
9028	wB10_d1-2_cGFP	\N	\N
9029	original/PLATE-1/bPLATE_wB10_s4_cRGB.png	\N	RED
9030	wB10_d1-2_cCy5	\N	\N
9031	original/PLATE-1/bPLATE_wE5_s1_cRGB.png	\N	BLUE
9032	wE5_d1-1_cDAPI	\N	\N
9033	original/PLATE-1/bPLATE_wE5_s1_cRGB.png	\N	GREEN
9034	wE5_d1-1_cGFP	\N	\N
9035	original/PLATE-1/bPLATE_wE5_s1_cRGB.png	\N	RED
9036	wE5_d1-1_cCy5	\N	\N
9037	original/PLATE-1/bPLATE_wH3_s8_cRGB.png	\N	BLUE
9038	wH3_d2-3_cDAPI	\N	\N
9039	original/PLATE-1/bPLATE_wH3_s8_cRGB.png	\N	GREEN
9040	wH3_d2-3_cGFP	\N	\N
9041	original/PLATE-1/bPLATE_wH3_s8_cRGB.png	\N	RED
9042	wH3_d2-3_cCy5	\N	\N
9043	original/PLATE-1/bPLATE_wF7_s8_cRGB.png	\N	BLUE
9044	wF7_d2-3_cDAPI	\N	\N
9045	original/PLATE-1/bPLATE_wF7_s8_cRGB.png	\N	GREEN
9046	wF7_d2-3_cGFP	\N	\N
9047	original/PLATE-1/bPLATE_wF7_s8_cRGB.png	\N	RED
9048	wF7_d2-3_cCy5	\N	\N
9049	original/PLATE-1/bPLATE_wB1_s2_cRGB.png	\N	BLUE
9050	wB1_d2-1_cDAPI	\N	\N
9051	original/PLATE-1/bPLATE_wB1_s2_cRGB.png	\N	GREEN
9052	wB1_d2-1_cGFP	\N	\N
9053	original/PLATE-1/bPLATE_wB1_s2_cRGB.png	\N	RED
9054	wB1_d2-1_cCy5	\N	\N
9055	original/PLATE-1/bPLATE_wD12_s6_cRGB.png	\N	BLUE
9056	wD12_d3-2_cDAPI	\N	\N
9057	original/PLATE-1/bPLATE_wD12_s6_cRGB.png	\N	GREEN
9058	wD12_d3-2_cGFP	\N	\N
9059	original/PLATE-1/bPLATE_wD12_s6_cRGB.png	\N	RED
9060	wD12_d3-2_cCy5	\N	\N
9061	original/PLATE-1/bPLATE_wB5_s9_cRGB.png	\N	BLUE
9062	wB5_d3-3_cDAPI	\N	\N
9063	original/PLATE-1/bPLATE_wB5_s9_cRGB.png	\N	GREEN
9064	wB5_d3-3_cGFP	\N	\N
9065	original/PLATE-1/bPLATE_wB5_s9_cRGB.png	\N	RED
9066	wB5_d3-3_cCy5	\N	\N
9067	original/PLATE-1/bPLATE_wG7_s3_cRGB.png	\N	BLUE
9068	wG7_d3-1_cDAPI	\N	\N
9069	original/PLATE-1/bPLATE_wG7_s3_cRGB.png	\N	GREEN
9070	wG7_d3-1_cGFP	\N	\N
9071	original/PLATE-1/bPLATE_wG7_s3_cRGB.png	\N	RED
9072	wG7_d3-1_cCy5	\N	\N
9073	original/PLATE-1/bPLATE_wH8_s1_cRGB.png	\N	BLUE
9074	wH8_d1-1_cDAPI	\N	\N
9075	original/PLATE-1/bPLATE_wH8_s1_cRGB.png	\N	GREEN
9076	wH8_d1-1_cGFP	\N	\N
9077	original/PLATE-1/bPLATE_wH8_s1_cRGB.png	\N	RED
9078	wH8_d1-1_cCy5	\N	\N
9079	original/PLATE-1/bPLATE_wC6_s7_cRGB.png	\N	BLUE
9080	wC6_d1-3_cDAPI	\N	\N
9081	original/PLATE-1/bPLATE_wC6_s7_cRGB.png	\N	GREEN
9082	wC6_d1-3_cGFP	\N	\N
9083	original/PLATE-1/bPLATE_wC6_s7_cRGB.png	\N	RED
9084	wC6_d1-3_cCy5	\N	\N
9085	original/PLATE-1/bPLATE_wF1_s4_cRGB.png	\N	BLUE
9086	wF1_d1-2_cDAPI	\N	\N
9087	original/PLATE-1/bPLATE_wF1_s4_cRGB.png	\N	GREEN
9088	wF1_d1-2_cGFP	\N	\N
9089	original/PLATE-1/bPLATE_wF1_s4_cRGB.png	\N	RED
9090	wF1_d1-2_cCy5	\N	\N
9091	original/PLATE-1/bPLATE_wD1_s3_cRGB.png	\N	BLUE
9092	wD1_d3-1_cDAPI	\N	\N
9093	original/PLATE-1/bPLATE_wD1_s3_cRGB.png	\N	GREEN
9094	wD1_d3-1_cGFP	\N	\N
9095	original/PLATE-1/bPLATE_wD1_s3_cRGB.png	\N	RED
9096	wD1_d3-1_cCy5	\N	\N
9097	original/PLATE-1/bPLATE_wA6_s6_cRGB.png	\N	BLUE
9098	wA6_d3-2_cDAPI	\N	\N
9099	original/PLATE-1/bPLATE_wA6_s6_cRGB.png	\N	GREEN
9100	wA6_d3-2_cGFP	\N	\N
9101	original/PLATE-1/bPLATE_wA6_s6_cRGB.png	\N	RED
9102	wA6_d3-2_cCy5	\N	\N
9103	original/PLATE-1/bPLATE_wA8_s1_cRGB.png	\N	BLUE
9104	wA8_d1-1_cDAPI	\N	\N
9105	original/PLATE-1/bPLATE_wA8_s1_cRGB.png	\N	GREEN
9106	wA8_d1-1_cGFP	\N	\N
9107	original/PLATE-1/bPLATE_wA8_s1_cRGB.png	\N	RED
9108	wA8_d1-1_cCy5	\N	\N
9109	original/PLATE-1/bPLATE_wA1_s3_cRGB.png	\N	BLUE
9110	wA1_d3-1_cDAPI	\N	\N
9111	original/PLATE-1/bPLATE_wA1_s3_cRGB.png	\N	GREEN
9112	wA1_d3-1_cGFP	\N	\N
9113	original/PLATE-1/bPLATE_wA1_s3_cRGB.png	\N	RED
9114	wA1_d3-1_cCy5	\N	\N
9115	original/PLATE-1/bPLATE_wF3_s8_cRGB.png	\N	BLUE
9116	wF3_d2-3_cDAPI	\N	\N
9117	original/PLATE-1/bPLATE_wF3_s8_cRGB.png	\N	GREEN
9118	wF3_d2-3_cGFP	\N	\N
9119	original/PLATE-1/bPLATE_wF3_s8_cRGB.png	\N	RED
9120	wF3_d2-3_cCy5	\N	\N
9121	original/PLATE-1/bPLATE_wH10_s5_cRGB.png	\N	BLUE
9122	wH10_d2-2_cDAPI	\N	\N
9123	original/PLATE-1/bPLATE_wH10_s5_cRGB.png	\N	GREEN
9124	wH10_d2-2_cGFP	\N	\N
9125	original/PLATE-1/bPLATE_wH10_s5_cRGB.png	\N	RED
9126	wH10_d2-2_cCy5	\N	\N
9127	original/PLATE-1/bPLATE_wH8_s7_cRGB.png	\N	BLUE
9128	wH8_d1-3_cDAPI	\N	\N
9129	original/PLATE-1/bPLATE_wH8_s7_cRGB.png	\N	GREEN
9130	wH8_d1-3_cGFP	\N	\N
9131	original/PLATE-1/bPLATE_wH8_s7_cRGB.png	\N	RED
9132	wH8_d1-3_cCy5	\N	\N
9133	original/PLATE-1/bPLATE_wC12_s7_cRGB.png	\N	BLUE
9134	wC12_d1-3_cDAPI	\N	\N
9135	original/PLATE-1/bPLATE_wC12_s7_cRGB.png	\N	GREEN
9136	wC12_d1-3_cGFP	\N	\N
9137	original/PLATE-1/bPLATE_wC12_s7_cRGB.png	\N	RED
9138	wC12_d1-3_cCy5	\N	\N
9139	original/PLATE-1/bPLATE_wF7_s4_cRGB.png	\N	BLUE
9140	wF7_d1-2_cDAPI	\N	\N
9141	original/PLATE-1/bPLATE_wF7_s4_cRGB.png	\N	GREEN
9142	wF7_d1-2_cGFP	\N	\N
9143	original/PLATE-1/bPLATE_wF7_s4_cRGB.png	\N	RED
9144	wF7_d1-2_cCy5	\N	\N
9145	original/PLATE-1/bPLATE_wB10_s1_cRGB.png	\N	BLUE
9146	wB10_d1-1_cDAPI	\N	\N
9147	original/PLATE-1/bPLATE_wB10_s1_cRGB.png	\N	GREEN
9148	wB10_d1-1_cGFP	\N	\N
9149	original/PLATE-1/bPLATE_wB10_s1_cRGB.png	\N	RED
9150	wB10_d1-1_cCy5	\N	\N
9151	original/PLATE-1/bPLATE_wD2_s5_cRGB.png	\N	BLUE
9152	wD2_d2-2_cDAPI	\N	\N
9153	original/PLATE-1/bPLATE_wD2_s5_cRGB.png	\N	GREEN
9154	wD2_d2-2_cGFP	\N	\N
9155	original/PLATE-1/bPLATE_wD2_s5_cRGB.png	\N	RED
9156	wD2_d2-2_cCy5	\N	\N
9157	original/PLATE-1/bPLATE_wA7_s8_cRGB.png	\N	BLUE
9158	wA7_d2-3_cDAPI	\N	\N
9159	original/PLATE-1/bPLATE_wA7_s8_cRGB.png	\N	GREEN
9160	wA7_d2-3_cGFP	\N	\N
9161	original/PLATE-1/bPLATE_wA7_s8_cRGB.png	\N	RED
9162	wA7_d2-3_cCy5	\N	\N
9163	original/PLATE-1/bPLATE_wF9_s2_cRGB.png	\N	BLUE
9164	wF9_d2-1_cDAPI	\N	\N
9165	original/PLATE-1/bPLATE_wF9_s2_cRGB.png	\N	GREEN
9166	wF9_d2-1_cGFP	\N	\N
9167	original/PLATE-1/bPLATE_wF9_s2_cRGB.png	\N	RED
9168	wF9_d2-1_cCy5	\N	\N
9169	original/PLATE-1/bPLATE_wD1_s7_cRGB.png	\N	BLUE
9170	wD1_d1-3_cDAPI	\N	\N
9171	original/PLATE-1/bPLATE_wD1_s7_cRGB.png	\N	GREEN
9172	wD1_d1-3_cGFP	\N	\N
9173	original/PLATE-1/bPLATE_wD1_s7_cRGB.png	\N	RED
9174	wD1_d1-3_cCy5	\N	\N
9175	original/PLATE-1/bPLATE_wF8_s4_cRGB.png	\N	BLUE
9176	wF8_d1-2_cDAPI	\N	\N
9177	original/PLATE-1/bPLATE_wF8_s4_cRGB.png	\N	GREEN
9178	wF8_d1-2_cGFP	\N	\N
9179	original/PLATE-1/bPLATE_wF8_s4_cRGB.png	\N	RED
9180	wF8_d1-2_cCy5	\N	\N
9181	original/PLATE-1/bPLATE_wH7_s7_cRGB.png	\N	BLUE
9182	wH7_d1-3_cDAPI	\N	\N
9183	original/PLATE-1/bPLATE_wH7_s7_cRGB.png	\N	GREEN
9184	wH7_d1-3_cGFP	\N	\N
9185	original/PLATE-1/bPLATE_wH7_s7_cRGB.png	\N	RED
9186	wH7_d1-3_cCy5	\N	\N
9187	original/PLATE-1/bPLATE_wD3_s7_cRGB.png	\N	BLUE
9188	wD3_d1-3_cDAPI	\N	\N
9189	original/PLATE-1/bPLATE_wD3_s7_cRGB.png	\N	GREEN
9190	wD3_d1-3_cGFP	\N	\N
9191	original/PLATE-1/bPLATE_wD3_s7_cRGB.png	\N	RED
9192	wD3_d1-3_cCy5	\N	\N
9193	original/PLATE-1/bPLATE_wF10_s4_cRGB.png	\N	BLUE
9194	wF10_d1-2_cDAPI	\N	\N
9195	original/PLATE-1/bPLATE_wF10_s4_cRGB.png	\N	GREEN
9196	wF10_d1-2_cGFP	\N	\N
9197	original/PLATE-1/bPLATE_wF10_s4_cRGB.png	\N	RED
9198	wF10_d1-2_cCy5	\N	\N
9199	original/PLATE-1/bPLATE_wB2_s1_cRGB.png	\N	BLUE
9200	wB2_d1-1_cDAPI	\N	\N
9201	original/PLATE-1/bPLATE_wB2_s1_cRGB.png	\N	GREEN
9202	wB2_d1-1_cGFP	\N	\N
9203	original/PLATE-1/bPLATE_wB2_s1_cRGB.png	\N	RED
9204	wB2_d1-1_cCy5	\N	\N
9205	original/PLATE-1/bPLATE_wF11_s7_cRGB.png	\N	BLUE
9206	wF11_d1-3_cDAPI	\N	\N
9207	original/PLATE-1/bPLATE_wF11_s7_cRGB.png	\N	GREEN
9208	wF11_d1-3_cGFP	\N	\N
9209	original/PLATE-1/bPLATE_wF11_s7_cRGB.png	\N	RED
9210	wF11_d1-3_cCy5	\N	\N
9211	original/PLATE-1/bPLATE_wD9_s7_cRGB.png	\N	BLUE
9212	wD9_d1-3_cDAPI	\N	\N
9213	original/PLATE-1/bPLATE_wD9_s7_cRGB.png	\N	GREEN
9214	wD9_d1-3_cGFP	\N	\N
9215	original/PLATE-1/bPLATE_wD9_s7_cRGB.png	\N	RED
9216	wD9_d1-3_cCy5	\N	\N
9217	original/PLATE-1/bPLATE_wG4_s4_cRGB.png	\N	BLUE
9218	wG4_d1-2_cDAPI	\N	\N
9219	original/PLATE-1/bPLATE_wG4_s4_cRGB.png	\N	GREEN
9220	wG4_d1-2_cGFP	\N	\N
9221	original/PLATE-1/bPLATE_wG4_s4_cRGB.png	\N	RED
9222	wG4_d1-2_cCy5	\N	\N
9223	original/PLATE-1/bPLATE_wD11_s4_cRGB.png	\N	BLUE
9224	wD11_d1-2_cDAPI	\N	\N
9225	original/PLATE-1/bPLATE_wD11_s4_cRGB.png	\N	GREEN
9226	wD11_d1-2_cGFP	\N	\N
9227	original/PLATE-1/bPLATE_wD11_s4_cRGB.png	\N	RED
9228	wD11_d1-2_cCy5	\N	\N
9229	original/PLATE-1/bPLATE_wB4_s7_cRGB.png	\N	BLUE
9230	wB4_d1-3_cDAPI	\N	\N
9231	original/PLATE-1/bPLATE_wB4_s7_cRGB.png	\N	GREEN
9232	wB4_d1-3_cGFP	\N	\N
9233	original/PLATE-1/bPLATE_wB4_s7_cRGB.png	\N	RED
9234	wB4_d1-3_cCy5	\N	\N
9235	original/PLATE-1/bPLATE_wG6_s1_cRGB.png	\N	BLUE
9236	wG6_d1-1_cDAPI	\N	\N
9237	original/PLATE-1/bPLATE_wG6_s1_cRGB.png	\N	GREEN
9238	wG6_d1-1_cGFP	\N	\N
9239	original/PLATE-1/bPLATE_wG6_s1_cRGB.png	\N	RED
9240	wG6_d1-1_cCy5	\N	\N
9241	original/PLATE-1/bPLATE_wH7_s3_cRGB.png	\N	BLUE
9242	wH7_d3-1_cDAPI	\N	\N
9243	original/PLATE-1/bPLATE_wH7_s3_cRGB.png	\N	GREEN
9244	wH7_d3-1_cGFP	\N	\N
9245	original/PLATE-1/bPLATE_wH7_s3_cRGB.png	\N	RED
9246	wH7_d3-1_cCy5	\N	\N
9247	original/PLATE-1/bPLATE_wC5_s9_cRGB.png	\N	BLUE
9248	wC5_d3-3_cDAPI	\N	\N
9249	original/PLATE-1/bPLATE_wC5_s9_cRGB.png	\N	GREEN
9250	wC5_d3-3_cGFP	\N	\N
9251	original/PLATE-1/bPLATE_wC5_s9_cRGB.png	\N	RED
9252	wC5_d3-3_cCy5	\N	\N
9253	original/PLATE-1/bPLATE_wE12_s6_cRGB.png	\N	BLUE
9254	wE12_d3-2_cDAPI	\N	\N
9255	original/PLATE-1/bPLATE_wE12_s6_cRGB.png	\N	GREEN
9256	wE12_d3-2_cGFP	\N	\N
9257	original/PLATE-1/bPLATE_wE12_s6_cRGB.png	\N	RED
9258	wE12_d3-2_cCy5	\N	\N
9259	original/PLATE-1/bPLATE_wG9_s9_cRGB.png	\N	BLUE
9260	wG9_d3-3_cDAPI	\N	\N
9261	original/PLATE-1/bPLATE_wG9_s9_cRGB.png	\N	GREEN
9262	wG9_d3-3_cGFP	\N	\N
9263	original/PLATE-1/bPLATE_wG9_s9_cRGB.png	\N	RED
9264	wG9_d3-3_cCy5	\N	\N
9265	original/PLATE-1/bPLATE_wB10_s8_cRGB.png	\N	BLUE
9266	wB10_d2-3_cDAPI	\N	\N
9267	original/PLATE-1/bPLATE_wB10_s8_cRGB.png	\N	GREEN
9268	wB10_d2-3_cGFP	\N	\N
9269	original/PLATE-1/bPLATE_wB10_s8_cRGB.png	\N	RED
9270	wB10_d2-3_cCy5	\N	\N
9271	original/PLATE-1/bPLATE_wE5_s5_cRGB.png	\N	BLUE
9272	wE5_d2-2_cDAPI	\N	\N
9273	original/PLATE-1/bPLATE_wE5_s5_cRGB.png	\N	GREEN
9274	wE5_d2-2_cGFP	\N	\N
9275	original/PLATE-1/bPLATE_wE5_s5_cRGB.png	\N	RED
9276	wE5_d2-2_cCy5	\N	\N
9277	original/PLATE-1/bPLATE_wG12_s2_cRGB.png	\N	BLUE
9278	wG12_d2-1_cDAPI	\N	\N
9279	original/PLATE-1/bPLATE_wG12_s2_cRGB.png	\N	GREEN
9280	wG12_d2-1_cGFP	\N	\N
9281	original/PLATE-1/bPLATE_wG12_s2_cRGB.png	\N	RED
9282	wG12_d2-1_cCy5	\N	\N
9283	original/PLATE-1/bPLATE_wH3_s9_cRGB.png	\N	BLUE
9284	wH3_d3-3_cDAPI	\N	\N
9285	original/PLATE-1/bPLATE_wH3_s9_cRGB.png	\N	GREEN
9286	wH3_d3-3_cGFP	\N	\N
9287	original/PLATE-1/bPLATE_wH3_s9_cRGB.png	\N	RED
9288	wH3_d3-3_cCy5	\N	\N
9289	original/PLATE-1/bPLATE_wH4_s4_cRGB.png	\N	BLUE
9290	wH4_d1-2_cDAPI	\N	\N
9291	original/PLATE-1/bPLATE_wH4_s4_cRGB.png	\N	GREEN
9292	wH4_d1-2_cGFP	\N	\N
9293	original/PLATE-1/bPLATE_wH4_s4_cRGB.png	\N	RED
9294	wH4_d1-2_cCy5	\N	\N
9295	original/PLATE-1/bPLATE_wE9_s7_cRGB.png	\N	BLUE
9296	wE9_d1-3_cDAPI	\N	\N
9297	original/PLATE-1/bPLATE_wE9_s7_cRGB.png	\N	GREEN
9298	wE9_d1-3_cGFP	\N	\N
9299	original/PLATE-1/bPLATE_wE9_s7_cRGB.png	\N	RED
9300	wE9_d1-3_cCy5	\N	\N
9301	original/PLATE-1/bPLATE_wA6_s1_cRGB.png	\N	BLUE
9302	wA6_d1-1_cDAPI	\N	\N
9303	original/PLATE-1/bPLATE_wA6_s1_cRGB.png	\N	GREEN
9304	wA6_d1-1_cGFP	\N	\N
9305	original/PLATE-1/bPLATE_wA6_s1_cRGB.png	\N	RED
9306	wA6_d1-1_cCy5	\N	\N
9307	original/PLATE-1/bPLATE_wD3_s4_cRGB.png	\N	BLUE
9308	wD3_d1-2_cDAPI	\N	\N
9309	original/PLATE-1/bPLATE_wD3_s4_cRGB.png	\N	GREEN
9310	wD3_d1-2_cGFP	\N	\N
9311	original/PLATE-1/bPLATE_wD3_s4_cRGB.png	\N	RED
9312	wD3_d1-2_cCy5	\N	\N
9313	original/PLATE-1/bPLATE_wA8_s7_cRGB.png	\N	BLUE
9314	wA8_d1-3_cDAPI	\N	\N
9315	original/PLATE-1/bPLATE_wA8_s7_cRGB.png	\N	GREEN
9316	wA8_d1-3_cGFP	\N	\N
9317	original/PLATE-1/bPLATE_wA8_s7_cRGB.png	\N	RED
9318	wA8_d1-3_cCy5	\N	\N
9319	original/PLATE-1/bPLATE_wB11_s6_cRGB.png	\N	BLUE
9320	wB11_d3-2_cDAPI	\N	\N
9321	original/PLATE-1/bPLATE_wB11_s6_cRGB.png	\N	GREEN
9322	wB11_d3-2_cGFP	\N	\N
9323	original/PLATE-1/bPLATE_wB11_s6_cRGB.png	\N	RED
9324	wB11_d3-2_cCy5	\N	\N
9325	original/PLATE-1/bPLATE_wE6_s3_cRGB.png	\N	BLUE
9326	wE6_d3-1_cDAPI	\N	\N
9327	original/PLATE-1/bPLATE_wE6_s3_cRGB.png	\N	GREEN
9328	wE6_d3-1_cGFP	\N	\N
9329	original/PLATE-1/bPLATE_wE6_s3_cRGB.png	\N	RED
9330	wE6_d3-1_cCy5	\N	\N
9331	original/PLATE-1/bPLATE_wF10_s1_cRGB.png	\N	BLUE
9332	wF10_d1-1_cDAPI	\N	\N
9333	original/PLATE-1/bPLATE_wF10_s1_cRGB.png	\N	GREEN
9334	wF10_d1-1_cGFP	\N	\N
9335	original/PLATE-1/bPLATE_wF10_s1_cRGB.png	\N	RED
9336	wF10_d1-1_cCy5	\N	\N
9337	original/PLATE-1/bPLATE_wB7_s9_cRGB.png	\N	BLUE
9338	wB7_d3-3_cDAPI	\N	\N
9339	original/PLATE-1/bPLATE_wB7_s9_cRGB.png	\N	GREEN
9340	wB7_d3-3_cGFP	\N	\N
9341	original/PLATE-1/bPLATE_wB7_s9_cRGB.png	\N	RED
9342	wB7_d3-3_cCy5	\N	\N
9343	original/PLATE-1/bPLATE_wE2_s6_cRGB.png	\N	BLUE
9344	wE2_d3-2_cDAPI	\N	\N
9345	original/PLATE-1/bPLATE_wE2_s6_cRGB.png	\N	GREEN
9346	wE2_d3-2_cGFP	\N	\N
9347	original/PLATE-1/bPLATE_wE2_s6_cRGB.png	\N	RED
9348	wE2_d3-2_cCy5	\N	\N
9349	original/PLATE-1/bPLATE_wG9_s3_cRGB.png	\N	BLUE
9350	wG9_d3-1_cDAPI	\N	\N
9351	original/PLATE-1/bPLATE_wG9_s3_cRGB.png	\N	GREEN
9352	wG9_d3-1_cGFP	\N	\N
9353	original/PLATE-1/bPLATE_wG9_s3_cRGB.png	\N	RED
9354	wG9_d3-1_cCy5	\N	\N
9355	original/PLATE-1/bPLATE_wC2_s5_cRGB.png	\N	BLUE
9356	wC2_d2-2_cDAPI	\N	\N
9357	original/PLATE-1/bPLATE_wC2_s5_cRGB.png	\N	GREEN
9358	wC2_d2-2_cGFP	\N	\N
9359	original/PLATE-1/bPLATE_wC2_s5_cRGB.png	\N	RED
9360	wC2_d2-2_cCy5	\N	\N
9361	original/PLATE-1/bPLATE_wE9_s2_cRGB.png	\N	BLUE
9362	wE9_d2-1_cDAPI	\N	\N
9363	original/PLATE-1/bPLATE_wE9_s2_cRGB.png	\N	GREEN
9364	wE9_d2-1_cGFP	\N	\N
9365	original/PLATE-1/bPLATE_wE9_s2_cRGB.png	\N	RED
9366	wE9_d2-1_cCy5	\N	\N
9367	original/PLATE-1/bPLATE_wG12_s8_cRGB.png	\N	BLUE
9368	wG12_d2-3_cDAPI	\N	\N
9369	original/PLATE-1/bPLATE_wG12_s8_cRGB.png	\N	GREEN
9370	wG12_d2-3_cGFP	\N	\N
9371	original/PLATE-1/bPLATE_wG12_s8_cRGB.png	\N	RED
9372	wG12_d2-3_cCy5	\N	\N
9373	original/PLATE-1/bPLATE_wG9_s8_cRGB.png	\N	BLUE
9374	wG9_d2-3_cDAPI	\N	\N
9375	original/PLATE-1/bPLATE_wG9_s8_cRGB.png	\N	GREEN
9376	wG9_d2-3_cGFP	\N	\N
9377	original/PLATE-1/bPLATE_wG9_s8_cRGB.png	\N	RED
9378	wG9_d2-3_cCy5	\N	\N
9379	original/PLATE-1/bPLATE_wD11_s9_cRGB.png	\N	BLUE
9380	wD11_d3-3_cDAPI	\N	\N
9381	original/PLATE-1/bPLATE_wD11_s9_cRGB.png	\N	GREEN
9382	wD11_d3-3_cGFP	\N	\N
9383	original/PLATE-1/bPLATE_wD11_s9_cRGB.png	\N	RED
9384	wD11_d3-3_cCy5	\N	\N
9385	original/PLATE-1/bPLATE_wG6_s6_cRGB.png	\N	BLUE
9386	wG6_d3-2_cDAPI	\N	\N
9387	original/PLATE-1/bPLATE_wG6_s6_cRGB.png	\N	GREEN
9388	wG6_d3-2_cGFP	\N	\N
9389	original/PLATE-1/bPLATE_wG6_s6_cRGB.png	\N	RED
9390	wG6_d3-2_cCy5	\N	\N
9391	original/PLATE-1/bPLATE_wA12_s2_cRGB.png	\N	BLUE
9392	wA12_d2-1_cDAPI	\N	\N
9393	original/PLATE-1/bPLATE_wA12_s2_cRGB.png	\N	GREEN
9394	wA12_d2-1_cGFP	\N	\N
9395	original/PLATE-1/bPLATE_wA12_s2_cRGB.png	\N	RED
9396	wA12_d2-1_cCy5	\N	\N
9397	original/PLATE-1/bPLATE_wA12_s4_cRGB.png	\N	BLUE
9398	wA12_d1-2_cDAPI	\N	\N
9399	original/PLATE-1/bPLATE_wA12_s4_cRGB.png	\N	GREEN
9400	wA12_d1-2_cGFP	\N	\N
9401	original/PLATE-1/bPLATE_wA12_s4_cRGB.png	\N	RED
9402	wA12_d1-2_cCy5	\N	\N
9403	original/PLATE-1/bPLATE_wB4_s3_cRGB.png	\N	BLUE
9404	wB4_d3-1_cDAPI	\N	\N
9405	original/PLATE-1/bPLATE_wB4_s3_cRGB.png	\N	GREEN
9406	wB4_d3-1_cGFP	\N	\N
9407	original/PLATE-1/bPLATE_wB4_s3_cRGB.png	\N	RED
9408	wB4_d3-1_cCy5	\N	\N
9409	original/PLATE-1/bPLATE_wD7_s1_cRGB.png	\N	BLUE
9410	wD7_d1-1_cDAPI	\N	\N
9411	original/PLATE-1/bPLATE_wD7_s1_cRGB.png	\N	GREEN
9412	wD7_d1-1_cGFP	\N	\N
9413	original/PLATE-1/bPLATE_wD7_s1_cRGB.png	\N	RED
9414	wD7_d1-1_cCy5	\N	\N
9415	original/PLATE-1/bPLATE_wB7_s3_cRGB.png	\N	BLUE
9416	wB7_d3-1_cDAPI	\N	\N
9417	original/PLATE-1/bPLATE_wB7_s3_cRGB.png	\N	GREEN
9418	wB7_d3-1_cGFP	\N	\N
9419	original/PLATE-1/bPLATE_wB7_s3_cRGB.png	\N	RED
9420	wB7_d3-1_cCy5	\N	\N
9421	original/PLATE-1/bPLATE_wD12_s2_cRGB.png	\N	BLUE
9422	wD12_d2-1_cDAPI	\N	\N
9423	original/PLATE-1/bPLATE_wD12_s2_cRGB.png	\N	GREEN
9424	wD12_d2-1_cGFP	\N	\N
9425	original/PLATE-1/bPLATE_wD12_s2_cRGB.png	\N	RED
9426	wD12_d2-1_cCy5	\N	\N
9427	original/PLATE-1/bPLATE_wC6_s4_cRGB.png	\N	BLUE
9428	wC6_d1-2_cDAPI	\N	\N
9429	original/PLATE-1/bPLATE_wC6_s4_cRGB.png	\N	GREEN
9430	wC6_d1-2_cGFP	\N	\N
9431	original/PLATE-1/bPLATE_wC6_s4_cRGB.png	\N	RED
9432	wC6_d1-2_cCy5	\N	\N
9433	original/PLATE-1/bPLATE_wA10_s9_cRGB.png	\N	BLUE
9434	wA10_d3-3_cDAPI	\N	\N
9435	original/PLATE-1/bPLATE_wA10_s9_cRGB.png	\N	GREEN
9436	wA10_d3-3_cGFP	\N	\N
9437	original/PLATE-1/bPLATE_wA10_s9_cRGB.png	\N	RED
9438	wA10_d3-3_cCy5	\N	\N
9439	original/PLATE-1/bPLATE_wB5_s5_cRGB.png	\N	BLUE
9440	wB5_d2-2_cDAPI	\N	\N
9441	original/PLATE-1/bPLATE_wB5_s5_cRGB.png	\N	GREEN
9442	wB5_d2-2_cGFP	\N	\N
9443	original/PLATE-1/bPLATE_wB5_s5_cRGB.png	\N	RED
9444	wB5_d2-2_cCy5	\N	\N
9445	original/PLATE-1/bPLATE_wD5_s6_cRGB.png	\N	BLUE
9446	wD5_d3-2_cDAPI	\N	\N
9447	original/PLATE-1/bPLATE_wD5_s6_cRGB.png	\N	GREEN
9448	wD5_d3-2_cGFP	\N	\N
9449	original/PLATE-1/bPLATE_wD5_s6_cRGB.png	\N	RED
9450	wD5_d3-2_cCy5	\N	\N
9451	original/PLATE-1/bPLATE_wF12_s3_cRGB.png	\N	BLUE
9452	wF12_d3-1_cDAPI	\N	\N
9453	original/PLATE-1/bPLATE_wF12_s3_cRGB.png	\N	GREEN
9454	wF12_d3-1_cGFP	\N	\N
9455	original/PLATE-1/bPLATE_wF12_s3_cRGB.png	\N	RED
9456	wF12_d3-1_cCy5	\N	\N
9457	original/PLATE-1/bPLATE_wF1_s1_cRGB.png	\N	BLUE
9458	wF1_d1-1_cDAPI	\N	\N
9459	original/PLATE-1/bPLATE_wF1_s1_cRGB.png	\N	GREEN
9460	wF1_d1-1_cGFP	\N	\N
9461	original/PLATE-1/bPLATE_wF1_s1_cRGB.png	\N	RED
9462	wF1_d1-1_cCy5	\N	\N
9463	original/PLATE-1/bPLATE_wH7_s5_cRGB.png	\N	BLUE
9464	wH7_d2-2_cDAPI	\N	\N
9465	original/PLATE-1/bPLATE_wH7_s5_cRGB.png	\N	GREEN
9466	wH7_d2-2_cGFP	\N	\N
9467	original/PLATE-1/bPLATE_wH7_s5_cRGB.png	\N	RED
9468	wH7_d2-2_cCy5	\N	\N
9469	original/PLATE-1/bPLATE_wE12_s8_cRGB.png	\N	BLUE
9470	wE12_d2-3_cDAPI	\N	\N
9471	original/PLATE-1/bPLATE_wE12_s8_cRGB.png	\N	GREEN
9472	wE12_d2-3_cGFP	\N	\N
9473	original/PLATE-1/bPLATE_wE12_s8_cRGB.png	\N	RED
9474	wE12_d2-3_cCy5	\N	\N
9475	original/PLATE-1/bPLATE_wA12_s9_cRGB.png	\N	BLUE
9476	wA12_d3-3_cDAPI	\N	\N
9477	original/PLATE-1/bPLATE_wA12_s9_cRGB.png	\N	GREEN
9478	wA12_d3-3_cGFP	\N	\N
9479	original/PLATE-1/bPLATE_wA12_s9_cRGB.png	\N	RED
9480	wA12_d3-3_cCy5	\N	\N
9481	original/PLATE-1/bPLATE_wD7_s6_cRGB.png	\N	BLUE
9482	wD7_d3-2_cDAPI	\N	\N
9483	original/PLATE-1/bPLATE_wD7_s6_cRGB.png	\N	GREEN
9484	wD7_d3-2_cGFP	\N	\N
9485	original/PLATE-1/bPLATE_wD7_s6_cRGB.png	\N	RED
9486	wD7_d3-2_cCy5	\N	\N
9487	original/PLATE-1/bPLATE_wG2_s3_cRGB.png	\N	BLUE
9488	wG2_d3-1_cDAPI	\N	\N
9489	original/PLATE-1/bPLATE_wG2_s3_cRGB.png	\N	GREEN
9490	wG2_d3-1_cGFP	\N	\N
9491	original/PLATE-1/bPLATE_wG2_s3_cRGB.png	\N	RED
9492	wG2_d3-1_cCy5	\N	\N
9493	original/PLATE-1/bPLATE_wD3_s3_cRGB.png	\N	BLUE
9494	wD3_d3-1_cDAPI	\N	\N
9495	original/PLATE-1/bPLATE_wD3_s3_cRGB.png	\N	GREEN
9496	wD3_d3-1_cGFP	\N	\N
9497	original/PLATE-1/bPLATE_wD3_s3_cRGB.png	\N	RED
9498	wD3_d3-1_cCy5	\N	\N
9499	original/PLATE-1/bPLATE_wA8_s6_cRGB.png	\N	BLUE
9500	wA8_d3-2_cDAPI	\N	\N
9501	original/PLATE-1/bPLATE_wA8_s6_cRGB.png	\N	GREEN
9502	wA8_d3-2_cGFP	\N	\N
9503	original/PLATE-1/bPLATE_wA8_s6_cRGB.png	\N	RED
9504	wA8_d3-2_cCy5	\N	\N
9505	original/PLATE-1/bPLATE_wH1_s7_cRGB.png	\N	BLUE
9506	wH1_d1-3_cDAPI	\N	\N
9507	original/PLATE-1/bPLATE_wH1_s7_cRGB.png	\N	GREEN
9508	wH1_d1-3_cGFP	\N	\N
9509	original/PLATE-1/bPLATE_wH1_s7_cRGB.png	\N	RED
9510	wH1_d1-3_cCy5	\N	\N
9511	original/PLATE-1/bPLATE_wG1_s7_cRGB.png	\N	BLUE
9512	wG1_d1-3_cDAPI	\N	\N
9513	original/PLATE-1/bPLATE_wG1_s7_cRGB.png	\N	GREEN
9514	wG1_d1-3_cGFP	\N	\N
9515	original/PLATE-1/bPLATE_wG1_s7_cRGB.png	\N	RED
9516	wG1_d1-3_cCy5	\N	\N
9517	original/PLATE-1/bPLATE_wB8_s1_cRGB.png	\N	BLUE
9518	wB8_d1-1_cDAPI	\N	\N
9519	original/PLATE-1/bPLATE_wB8_s1_cRGB.png	\N	GREEN
9520	wB8_d1-1_cGFP	\N	\N
9521	original/PLATE-1/bPLATE_wB8_s1_cRGB.png	\N	RED
9522	wB8_d1-1_cCy5	\N	\N
9523	original/PLATE-1/bPLATE_wH7_s1_cRGB.png	\N	BLUE
9524	wH7_d1-1_cDAPI	\N	\N
9525	original/PLATE-1/bPLATE_wH7_s1_cRGB.png	\N	GREEN
9526	wH7_d1-1_cGFP	\N	\N
9527	original/PLATE-1/bPLATE_wH7_s1_cRGB.png	\N	RED
9528	wH7_d1-1_cCy5	\N	\N
9529	original/PLATE-1/bPLATE_wC5_s7_cRGB.png	\N	BLUE
9530	wC5_d1-3_cDAPI	\N	\N
9531	original/PLATE-1/bPLATE_wC5_s7_cRGB.png	\N	GREEN
9532	wC5_d1-3_cGFP	\N	\N
9533	original/PLATE-1/bPLATE_wC5_s7_cRGB.png	\N	RED
9534	wC5_d1-3_cCy5	\N	\N
9535	original/PLATE-1/bPLATE_wE12_s4_cRGB.png	\N	BLUE
9536	wE12_d1-2_cDAPI	\N	\N
9537	original/PLATE-1/bPLATE_wE12_s4_cRGB.png	\N	GREEN
9538	wE12_d1-2_cGFP	\N	\N
9539	original/PLATE-1/bPLATE_wE12_s4_cRGB.png	\N	RED
9540	wE12_d1-2_cCy5	\N	\N
9541	original/PLATE-1/bPLATE_wH4_s2_cRGB.png	\N	BLUE
9542	wH4_d2-1_cDAPI	\N	\N
9543	original/PLATE-1/bPLATE_wH4_s2_cRGB.png	\N	GREEN
9544	wH4_d2-1_cGFP	\N	\N
9545	original/PLATE-1/bPLATE_wH4_s2_cRGB.png	\N	RED
9546	wH4_d2-1_cCy5	\N	\N
9547	original/PLATE-1/bPLATE_wC2_s8_cRGB.png	\N	BLUE
9548	wC2_d2-3_cDAPI	\N	\N
9549	original/PLATE-1/bPLATE_wC2_s8_cRGB.png	\N	GREEN
9550	wC2_d2-3_cGFP	\N	\N
9551	original/PLATE-1/bPLATE_wC2_s8_cRGB.png	\N	RED
9552	wC2_d2-3_cCy5	\N	\N
9553	original/PLATE-1/bPLATE_wE9_s5_cRGB.png	\N	BLUE
9554	wE9_d2-2_cDAPI	\N	\N
9555	original/PLATE-1/bPLATE_wE9_s5_cRGB.png	\N	GREEN
9556	wE9_d2-2_cGFP	\N	\N
9557	original/PLATE-1/bPLATE_wE9_s5_cRGB.png	\N	RED
9558	wE9_d2-2_cCy5	\N	\N
9559	original/PLATE-1/bPLATE_wC7_s3_cRGB.png	\N	BLUE
9560	wC7_d3-1_cDAPI	\N	\N
9561	original/PLATE-1/bPLATE_wC7_s3_cRGB.png	\N	GREEN
9562	wC7_d3-1_cGFP	\N	\N
9563	original/PLATE-1/bPLATE_wC7_s3_cRGB.png	\N	RED
9564	wC7_d3-1_cCy5	\N	\N
9565	original/PLATE-1/bPLATE_wF8_s9_cRGB.png	\N	BLUE
9566	wF8_d3-3_cDAPI	\N	\N
9567	original/PLATE-1/bPLATE_wF8_s9_cRGB.png	\N	GREEN
9568	wF8_d3-3_cGFP	\N	\N
9569	original/PLATE-1/bPLATE_wF8_s9_cRGB.png	\N	RED
9570	wF8_d3-3_cCy5	\N	\N
9571	original/PLATE-1/bPLATE_wC5_s2_cRGB.png	\N	BLUE
9572	wC5_d2-1_cDAPI	\N	\N
9573	original/PLATE-1/bPLATE_wC5_s2_cRGB.png	\N	GREEN
9574	wC5_d2-1_cGFP	\N	\N
9575	original/PLATE-1/bPLATE_wC5_s2_cRGB.png	\N	RED
9576	wC5_d2-1_cCy5	\N	\N
9577	original/PLATE-1/bPLATE_wH5_s3_cRGB.png	\N	BLUE
9578	wH5_d3-1_cDAPI	\N	\N
9579	original/PLATE-1/bPLATE_wH5_s3_cRGB.png	\N	GREEN
9580	wH5_d3-1_cGFP	\N	\N
9581	original/PLATE-1/bPLATE_wH5_s3_cRGB.png	\N	RED
9582	wH5_d3-1_cCy5	\N	\N
9583	original/PLATE-1/bPLATE_wC3_s9_cRGB.png	\N	BLUE
9584	wC3_d3-3_cDAPI	\N	\N
9585	original/PLATE-1/bPLATE_wC3_s9_cRGB.png	\N	GREEN
9586	wC3_d3-3_cGFP	\N	\N
9587	original/PLATE-1/bPLATE_wC3_s9_cRGB.png	\N	RED
9588	wC3_d3-3_cCy5	\N	\N
9589	original/PLATE-1/bPLATE_wE10_s6_cRGB.png	\N	BLUE
9590	wE10_d3-2_cDAPI	\N	\N
9591	original/PLATE-1/bPLATE_wE10_s6_cRGB.png	\N	GREEN
9592	wE10_d3-2_cGFP	\N	\N
9593	original/PLATE-1/bPLATE_wE10_s6_cRGB.png	\N	RED
9594	wE10_d3-2_cCy5	\N	\N
9595	original/PLATE-1/bPLATE_wB9_s8_cRGB.png	\N	BLUE
9596	wB9_d2-3_cDAPI	\N	\N
9597	original/PLATE-1/bPLATE_wB9_s8_cRGB.png	\N	GREEN
9598	wB9_d2-3_cGFP	\N	\N
9599	original/PLATE-1/bPLATE_wB9_s8_cRGB.png	\N	RED
9600	wB9_d2-3_cCy5	\N	\N
9601	original/PLATE-1/bPLATE_wE4_s5_cRGB.png	\N	BLUE
9602	wE4_d2-2_cDAPI	\N	\N
9603	original/PLATE-1/bPLATE_wE4_s5_cRGB.png	\N	GREEN
9604	wE4_d2-2_cGFP	\N	\N
9605	original/PLATE-1/bPLATE_wE4_s5_cRGB.png	\N	RED
9606	wE4_d2-2_cCy5	\N	\N
9607	original/PLATE-1/bPLATE_wG11_s2_cRGB.png	\N	BLUE
9608	wG11_d2-1_cDAPI	\N	\N
9609	original/PLATE-1/bPLATE_wG11_s2_cRGB.png	\N	GREEN
9610	wG11_d2-1_cGFP	\N	\N
9611	original/PLATE-1/bPLATE_wG11_s2_cRGB.png	\N	RED
9612	wG11_d2-1_cCy5	\N	\N
9613	original/PLATE-1/bPLATE_wF12_s9_cRGB.png	\N	BLUE
9614	wF12_d3-3_cDAPI	\N	\N
9615	original/PLATE-1/bPLATE_wF12_s9_cRGB.png	\N	GREEN
9616	wF12_d3-3_cGFP	\N	\N
9617	original/PLATE-1/bPLATE_wF12_s9_cRGB.png	\N	RED
9618	wF12_d3-3_cCy5	\N	\N
9619	original/PLATE-1/bPLATE_wH9_s1_cRGB.png	\N	BLUE
9620	wH9_d1-1_cDAPI	\N	\N
9621	original/PLATE-1/bPLATE_wH9_s1_cRGB.png	\N	GREEN
9622	wH9_d1-1_cGFP	\N	\N
9623	original/PLATE-1/bPLATE_wH9_s1_cRGB.png	\N	RED
9624	wH9_d1-1_cCy5	\N	\N
9625	original/PLATE-1/bPLATE_wC7_s7_cRGB.png	\N	BLUE
9626	wC7_d1-3_cDAPI	\N	\N
9627	original/PLATE-1/bPLATE_wC7_s7_cRGB.png	\N	GREEN
9628	wC7_d1-3_cGFP	\N	\N
9629	original/PLATE-1/bPLATE_wC7_s7_cRGB.png	\N	RED
9630	wC7_d1-3_cCy5	\N	\N
9631	original/PLATE-1/bPLATE_wF2_s4_cRGB.png	\N	BLUE
9632	wF2_d1-2_cDAPI	\N	\N
9633	original/PLATE-1/bPLATE_wF2_s4_cRGB.png	\N	GREEN
9634	wF2_d1-2_cGFP	\N	\N
9635	original/PLATE-1/bPLATE_wF2_s4_cRGB.png	\N	RED
9636	wF2_d1-2_cCy5	\N	\N
9637	original/PLATE-1/bPLATE_wH12_s8_cRGB.png	\N	BLUE
9638	wH12_d2-3_cDAPI	\N	\N
9639	original/PLATE-1/bPLATE_wH12_s8_cRGB.png	\N	GREEN
9640	wH12_d2-3_cGFP	\N	\N
9641	original/PLATE-1/bPLATE_wH12_s8_cRGB.png	\N	RED
9642	wH12_d2-3_cCy5	\N	\N
9643	original/PLATE-1/bPLATE_wH2_s7_cRGB.png	\N	BLUE
9644	wH2_d1-3_cDAPI	\N	\N
9645	original/PLATE-1/bPLATE_wH2_s7_cRGB.png	\N	GREEN
9646	wH2_d1-3_cGFP	\N	\N
9647	original/PLATE-1/bPLATE_wH2_s7_cRGB.png	\N	RED
9648	wH2_d1-3_cCy5	\N	\N
9649	original/PLATE-1/bPLATE_wE2_s9_cRGB.png	\N	BLUE
9650	wE2_d3-3_cDAPI	\N	\N
9651	original/PLATE-1/bPLATE_wE2_s9_cRGB.png	\N	GREEN
9652	wE2_d3-3_cGFP	\N	\N
9653	original/PLATE-1/bPLATE_wE2_s9_cRGB.png	\N	RED
9654	wE2_d3-3_cCy5	\N	\N
9655	original/PLATE-1/bPLATE_wG9_s6_cRGB.png	\N	BLUE
9656	wG9_d3-2_cDAPI	\N	\N
9657	original/PLATE-1/bPLATE_wG9_s6_cRGB.png	\N	GREEN
9658	wG9_d3-2_cGFP	\N	\N
9659	original/PLATE-1/bPLATE_wG9_s6_cRGB.png	\N	RED
9660	wG9_d3-2_cCy5	\N	\N
9661	original/PLATE-1/bPLATE_wC9_s3_cRGB.png	\N	BLUE
9662	wC9_d3-1_cDAPI	\N	\N
9663	original/PLATE-1/bPLATE_wC9_s3_cRGB.png	\N	GREEN
9664	wC9_d3-1_cGFP	\N	\N
9665	original/PLATE-1/bPLATE_wC9_s3_cRGB.png	\N	RED
9666	wC9_d3-1_cCy5	\N	\N
9667	original/PLATE-1/bPLATE_wA2_s6_cRGB.png	\N	BLUE
9668	wA2_d3-2_cDAPI	\N	\N
9669	original/PLATE-1/bPLATE_wA2_s6_cRGB.png	\N	GREEN
9670	wA2_d3-2_cGFP	\N	\N
9671	original/PLATE-1/bPLATE_wA2_s6_cRGB.png	\N	RED
9672	wA2_d3-2_cCy5	\N	\N
9673	original/PLATE-1/bPLATE_wF9_s8_cRGB.png	\N	BLUE
9674	wF9_d2-3_cDAPI	\N	\N
9675	original/PLATE-1/bPLATE_wF9_s8_cRGB.png	\N	GREEN
9676	wF9_d2-3_cGFP	\N	\N
9677	original/PLATE-1/bPLATE_wF9_s8_cRGB.png	\N	RED
9678	wF9_d2-3_cCy5	\N	\N
9679	original/PLATE-1/bPLATE_wA10_s2_cRGB.png	\N	BLUE
9680	wA10_d2-1_cDAPI	\N	\N
9681	original/PLATE-1/bPLATE_wA10_s2_cRGB.png	\N	GREEN
9682	wA10_d2-1_cGFP	\N	\N
9683	original/PLATE-1/bPLATE_wA10_s2_cRGB.png	\N	RED
9684	wA10_d2-1_cCy5	\N	\N
9685	original/PLATE-1/bPLATE_wB10_s2_cRGB.png	\N	BLUE
9686	wB10_d2-1_cDAPI	\N	\N
9687	original/PLATE-1/bPLATE_wB10_s2_cRGB.png	\N	GREEN
9688	wB10_d2-1_cGFP	\N	\N
9689	original/PLATE-1/bPLATE_wB10_s2_cRGB.png	\N	RED
9690	wB10_d2-1_cCy5	\N	\N
9691	original/PLATE-1/bPLATE_wB1_s8_cRGB.png	\N	BLUE
9692	wB1_d2-3_cDAPI	\N	\N
9693	original/PLATE-1/bPLATE_wB1_s8_cRGB.png	\N	GREEN
9694	wB1_d2-3_cGFP	\N	\N
9695	original/PLATE-1/bPLATE_wB1_s8_cRGB.png	\N	RED
9696	wB1_d2-3_cCy5	\N	\N
9697	original/PLATE-1/bPLATE_wD8_s5_cRGB.png	\N	BLUE
9698	wD8_d2-2_cDAPI	\N	\N
9699	original/PLATE-1/bPLATE_wD8_s5_cRGB.png	\N	GREEN
9700	wD8_d2-2_cGFP	\N	\N
9701	original/PLATE-1/bPLATE_wD8_s5_cRGB.png	\N	RED
9702	wD8_d2-2_cCy5	\N	\N
9703	original/PLATE-1/bPLATE_wG3_s2_cRGB.png	\N	BLUE
9704	wG3_d2-1_cDAPI	\N	\N
9705	original/PLATE-1/bPLATE_wG3_s2_cRGB.png	\N	GREEN
9706	wG3_d2-1_cGFP	\N	\N
9707	original/PLATE-1/bPLATE_wG3_s2_cRGB.png	\N	RED
9708	wG3_d2-1_cCy5	\N	\N
9709	original/PLATE-1/bPLATE_wB8_s2_cRGB.png	\N	BLUE
9710	wB8_d2-1_cDAPI	\N	\N
9711	original/PLATE-1/bPLATE_wB8_s2_cRGB.png	\N	GREEN
9712	wB8_d2-1_cGFP	\N	\N
9713	original/PLATE-1/bPLATE_wB8_s2_cRGB.png	\N	RED
9714	wB8_d2-1_cCy5	\N	\N
9715	original/PLATE-1/bPLATE_wH4_s3_cRGB.png	\N	BLUE
9716	wH4_d3-1_cDAPI	\N	\N
9717	original/PLATE-1/bPLATE_wH4_s3_cRGB.png	\N	GREEN
9718	wH4_d3-1_cGFP	\N	\N
9719	original/PLATE-1/bPLATE_wH4_s3_cRGB.png	\N	RED
9720	wH4_d3-1_cCy5	\N	\N
9721	original/PLATE-1/bPLATE_wC2_s9_cRGB.png	\N	BLUE
9722	wC2_d3-3_cDAPI	\N	\N
9723	original/PLATE-1/bPLATE_wC2_s9_cRGB.png	\N	GREEN
9724	wC2_d3-3_cGFP	\N	\N
9725	original/PLATE-1/bPLATE_wC2_s9_cRGB.png	\N	RED
9726	wC2_d3-3_cCy5	\N	\N
9727	original/PLATE-1/bPLATE_wE9_s6_cRGB.png	\N	BLUE
9728	wE9_d3-2_cDAPI	\N	\N
9729	original/PLATE-1/bPLATE_wE9_s6_cRGB.png	\N	GREEN
9730	wE9_d3-2_cGFP	\N	\N
9731	original/PLATE-1/bPLATE_wE9_s6_cRGB.png	\N	RED
9732	wE9_d3-2_cCy5	\N	\N
9733	original/PLATE-1/bPLATE_wD2_s1_cRGB.png	\N	BLUE
9734	wD2_d1-1_cDAPI	\N	\N
9735	original/PLATE-1/bPLATE_wD2_s1_cRGB.png	\N	GREEN
9736	wD2_d1-1_cGFP	\N	\N
9737	original/PLATE-1/bPLATE_wD2_s1_cRGB.png	\N	RED
9738	wD2_d1-1_cCy5	\N	\N
9739	original/PLATE-1/bPLATE_wA7_s4_cRGB.png	\N	BLUE
9740	wA7_d1-2_cDAPI	\N	\N
9741	original/PLATE-1/bPLATE_wA7_s4_cRGB.png	\N	GREEN
9742	wA7_d1-2_cGFP	\N	\N
9743	original/PLATE-1/bPLATE_wA7_s4_cRGB.png	\N	RED
9744	wA7_d1-2_cCy5	\N	\N
9745	original/PLATE-1/bPLATE_wC9_s7_cRGB.png	\N	BLUE
9746	wC9_d1-3_cDAPI	\N	\N
9747	original/PLATE-1/bPLATE_wC9_s7_cRGB.png	\N	GREEN
9748	wC9_d1-3_cGFP	\N	\N
9749	original/PLATE-1/bPLATE_wC9_s7_cRGB.png	\N	RED
9750	wC9_d1-3_cCy5	\N	\N
9751	original/PLATE-1/bPLATE_wC2_s1_cRGB.png	\N	BLUE
9752	wC2_d1-1_cDAPI	\N	\N
9753	original/PLATE-1/bPLATE_wC2_s1_cRGB.png	\N	GREEN
9754	wC2_d1-1_cGFP	\N	\N
9755	original/PLATE-1/bPLATE_wC2_s1_cRGB.png	\N	RED
9756	wC2_d1-1_cCy5	\N	\N
9757	original/PLATE-1/bPLATE_wF4_s4_cRGB.png	\N	BLUE
9758	wF4_d1-2_cDAPI	\N	\N
9759	original/PLATE-1/bPLATE_wF4_s4_cRGB.png	\N	GREEN
9760	wF4_d1-2_cGFP	\N	\N
9761	original/PLATE-1/bPLATE_wF4_s4_cRGB.png	\N	RED
9762	wF4_d1-2_cCy5	\N	\N
9763	original/PLATE-1/bPLATE_wG12_s9_cRGB.png	\N	BLUE
9764	wG12_d3-3_cDAPI	\N	\N
9765	original/PLATE-1/bPLATE_wG12_s9_cRGB.png	\N	GREEN
9766	wG12_d3-3_cGFP	\N	\N
9767	original/PLATE-1/bPLATE_wG12_s9_cRGB.png	\N	RED
9768	wG12_d3-3_cCy5	\N	\N
9769	original/PLATE-1/bPLATE_wH11_s1_cRGB.png	\N	BLUE
9770	wH11_d1-1_cDAPI	\N	\N
9771	original/PLATE-1/bPLATE_wH11_s1_cRGB.png	\N	GREEN
9772	wH11_d1-1_cGFP	\N	\N
9773	original/PLATE-1/bPLATE_wH11_s1_cRGB.png	\N	RED
9774	wH11_d1-1_cCy5	\N	\N
9775	original/PLATE-1/bPLATE_wD11_s3_cRGB.png	\N	BLUE
9776	wD11_d3-1_cDAPI	\N	\N
9777	original/PLATE-1/bPLATE_wD11_s3_cRGB.png	\N	GREEN
9778	wD11_d3-1_cGFP	\N	\N
9779	original/PLATE-1/bPLATE_wD11_s3_cRGB.png	\N	RED
9780	wD11_d3-1_cCy5	\N	\N
9781	original/PLATE-1/bPLATE_wB4_s6_cRGB.png	\N	BLUE
9782	wB4_d3-2_cDAPI	\N	\N
9783	original/PLATE-1/bPLATE_wB4_s6_cRGB.png	\N	GREEN
9784	wB4_d3-2_cGFP	\N	\N
9785	original/PLATE-1/bPLATE_wB4_s6_cRGB.png	\N	RED
9786	wB4_d3-2_cCy5	\N	\N
9787	original/PLATE-1/bPLATE_wD3_s8_cRGB.png	\N	BLUE
9788	wD3_d2-3_cDAPI	\N	\N
9789	original/PLATE-1/bPLATE_wD3_s8_cRGB.png	\N	GREEN
9790	wD3_d2-3_cGFP	\N	\N
9791	original/PLATE-1/bPLATE_wD3_s8_cRGB.png	\N	RED
9792	wD3_d2-3_cCy5	\N	\N
9793	original/PLATE-1/bPLATE_wF10_s5_cRGB.png	\N	BLUE
9794	wF10_d2-2_cDAPI	\N	\N
9795	original/PLATE-1/bPLATE_wF10_s5_cRGB.png	\N	GREEN
9796	wF10_d2-2_cGFP	\N	\N
9797	original/PLATE-1/bPLATE_wF10_s5_cRGB.png	\N	RED
9798	wF10_d2-2_cCy5	\N	\N
9799	original/PLATE-1/bPLATE_wC11_s3_cRGB.png	\N	BLUE
9800	wC11_d3-1_cDAPI	\N	\N
9801	original/PLATE-1/bPLATE_wC11_s3_cRGB.png	\N	GREEN
9802	wC11_d3-1_cGFP	\N	\N
9803	original/PLATE-1/bPLATE_wC11_s3_cRGB.png	\N	RED
9804	wC11_d3-1_cCy5	\N	\N
9805	original/PLATE-1/bPLATE_wA4_s6_cRGB.png	\N	BLUE
9806	wA4_d3-2_cDAPI	\N	\N
9807	original/PLATE-1/bPLATE_wA4_s6_cRGB.png	\N	GREEN
9808	wA4_d3-2_cGFP	\N	\N
9809	original/PLATE-1/bPLATE_wA4_s6_cRGB.png	\N	RED
9810	wA4_d3-2_cCy5	\N	\N
9811	original/PLATE-1/bPLATE_wB2_s4_cRGB.png	\N	BLUE
9812	wB2_d1-2_cDAPI	\N	\N
9813	original/PLATE-1/bPLATE_wB2_s4_cRGB.png	\N	GREEN
9814	wB2_d1-2_cGFP	\N	\N
9815	original/PLATE-1/bPLATE_wB2_s4_cRGB.png	\N	RED
9816	wB2_d1-2_cCy5	\N	\N
9817	original/PLATE-1/bPLATE_wD9_s1_cRGB.png	\N	BLUE
9818	wD9_d1-1_cDAPI	\N	\N
9819	original/PLATE-1/bPLATE_wD9_s1_cRGB.png	\N	GREEN
9820	wD9_d1-1_cGFP	\N	\N
9821	original/PLATE-1/bPLATE_wD9_s1_cRGB.png	\N	RED
9822	wD9_d1-1_cCy5	\N	\N
9823	original/PLATE-1/bPLATE_wH12_s9_cRGB.png	\N	BLUE
9824	wH12_d3-3_cDAPI	\N	\N
9825	original/PLATE-1/bPLATE_wH12_s9_cRGB.png	\N	GREEN
9826	wH12_d3-3_cGFP	\N	\N
9827	original/PLATE-1/bPLATE_wH12_s9_cRGB.png	\N	RED
9828	wH12_d3-3_cCy5	\N	\N
9829	original/PLATE-1/bPLATE_wD12_s3_cRGB.png	\N	BLUE
9830	wD12_d3-1_cDAPI	\N	\N
9831	original/PLATE-1/bPLATE_wD12_s3_cRGB.png	\N	GREEN
9832	wD12_d3-1_cGFP	\N	\N
9833	original/PLATE-1/bPLATE_wD12_s3_cRGB.png	\N	RED
9834	wD12_d3-1_cCy5	\N	\N
9835	original/PLATE-1/bPLATE_wB12_s2_cRGB.png	\N	BLUE
9836	wB12_d2-1_cDAPI	\N	\N
9837	original/PLATE-1/bPLATE_wB12_s2_cRGB.png	\N	GREEN
9838	wB12_d2-1_cGFP	\N	\N
9839	original/PLATE-1/bPLATE_wB12_s2_cRGB.png	\N	RED
9840	wB12_d2-1_cCy5	\N	\N
9841	original/PLATE-1/bPLATE_wB5_s6_cRGB.png	\N	BLUE
9842	wB5_d3-2_cDAPI	\N	\N
9843	original/PLATE-1/bPLATE_wB5_s6_cRGB.png	\N	GREEN
9844	wB5_d3-2_cGFP	\N	\N
9845	original/PLATE-1/bPLATE_wB5_s6_cRGB.png	\N	RED
9846	wB5_d3-2_cCy5	\N	\N
9847	original/PLATE-1/bPLATE_wE4_s8_cRGB.png	\N	BLUE
9848	wE4_d2-3_cDAPI	\N	\N
9849	original/PLATE-1/bPLATE_wE4_s8_cRGB.png	\N	GREEN
9850	wE4_d2-3_cGFP	\N	\N
9851	original/PLATE-1/bPLATE_wE4_s8_cRGB.png	\N	RED
9852	wE4_d2-3_cCy5	\N	\N
9853	original/PLATE-1/bPLATE_wG11_s5_cRGB.png	\N	BLUE
9854	wG11_d2-2_cDAPI	\N	\N
9855	original/PLATE-1/bPLATE_wG11_s5_cRGB.png	\N	GREEN
9856	wG11_d2-2_cGFP	\N	\N
9857	original/PLATE-1/bPLATE_wG11_s5_cRGB.png	\N	RED
9858	wG11_d2-2_cCy5	\N	\N
9859	original/PLATE-1/bPLATE_wH1_s5_cRGB.png	\N	BLUE
9860	wH1_d2-2_cDAPI	\N	\N
9861	original/PLATE-1/bPLATE_wH1_s5_cRGB.png	\N	GREEN
9862	wH1_d2-2_cGFP	\N	\N
9863	original/PLATE-1/bPLATE_wH1_s5_cRGB.png	\N	RED
9864	wH1_d2-2_cCy5	\N	\N
9865	original/PLATE-1/bPLATE_wE6_s8_cRGB.png	\N	BLUE
9866	wE6_d2-3_cDAPI	\N	\N
9867	original/PLATE-1/bPLATE_wE6_s8_cRGB.png	\N	GREEN
9868	wE6_d2-3_cGFP	\N	\N
9869	original/PLATE-1/bPLATE_wE6_s8_cRGB.png	\N	RED
9870	wE6_d2-3_cCy5	\N	\N
9871	original/PLATE-1/bPLATE_wG11_s7_cRGB.png	\N	BLUE
9872	wG11_d1-3_cDAPI	\N	\N
9873	original/PLATE-1/bPLATE_wG11_s7_cRGB.png	\N	GREEN
9874	wG11_d1-3_cGFP	\N	\N
9875	original/PLATE-1/bPLATE_wG11_s7_cRGB.png	\N	RED
9876	wG11_d1-3_cCy5	\N	\N
9877	original/PLATE-1/bPLATE_wH10_s7_cRGB.png	\N	BLUE
9878	wH10_d1-3_cDAPI	\N	\N
9879	original/PLATE-1/bPLATE_wH10_s7_cRGB.png	\N	GREEN
9880	wH10_d1-3_cGFP	\N	\N
9881	original/PLATE-1/bPLATE_wH10_s7_cRGB.png	\N	RED
9882	wH10_d1-3_cCy5	\N	\N
9883	original/PLATE-1/bPLATE_wG3_s8_cRGB.png	\N	BLUE
9884	wG3_d2-3_cDAPI	\N	\N
9885	original/PLATE-1/bPLATE_wG3_s8_cRGB.png	\N	GREEN
9886	wG3_d2-3_cGFP	\N	\N
9887	original/PLATE-1/bPLATE_wG3_s8_cRGB.png	\N	RED
9888	wG3_d2-3_cCy5	\N	\N
9889	original/PLATE-1/bPLATE_wC11_s9_cRGB.png	\N	BLUE
9890	wC11_d3-3_cDAPI	\N	\N
9891	original/PLATE-1/bPLATE_wC11_s9_cRGB.png	\N	GREEN
9892	wC11_d3-3_cGFP	\N	\N
9893	original/PLATE-1/bPLATE_wC11_s9_cRGB.png	\N	RED
9894	wC11_d3-3_cCy5	\N	\N
9895	original/PLATE-1/bPLATE_wF6_s6_cRGB.png	\N	BLUE
9896	wF6_d3-2_cDAPI	\N	\N
9897	original/PLATE-1/bPLATE_wF6_s6_cRGB.png	\N	GREEN
9898	wF6_d3-2_cGFP	\N	\N
9899	original/PLATE-1/bPLATE_wF6_s6_cRGB.png	\N	RED
9900	wF6_d3-2_cCy5	\N	\N
9901	original/PLATE-1/bPLATE_wD1_s6_cRGB.png	\N	BLUE
9902	wD1_d3-2_cDAPI	\N	\N
9903	original/PLATE-1/bPLATE_wD1_s6_cRGB.png	\N	GREEN
9904	wD1_d3-2_cGFP	\N	\N
9905	original/PLATE-1/bPLATE_wD1_s6_cRGB.png	\N	RED
9906	wD1_d3-2_cCy5	\N	\N
9907	original/PLATE-1/bPLATE_wA6_s9_cRGB.png	\N	BLUE
9908	wA6_d3-3_cDAPI	\N	\N
9909	original/PLATE-1/bPLATE_wA6_s9_cRGB.png	\N	GREEN
9910	wA6_d3-3_cGFP	\N	\N
9911	original/PLATE-1/bPLATE_wA6_s9_cRGB.png	\N	RED
9912	wA6_d3-3_cCy5	\N	\N
9913	original/PLATE-1/bPLATE_wF8_s3_cRGB.png	\N	BLUE
9914	wF8_d3-1_cDAPI	\N	\N
9915	original/PLATE-1/bPLATE_wF8_s3_cRGB.png	\N	GREEN
9916	wF8_d3-1_cGFP	\N	\N
9917	original/PLATE-1/bPLATE_wF8_s3_cRGB.png	\N	RED
9918	wF8_d3-1_cCy5	\N	\N
9919	original/PLATE-1/bPLATE_wC11_s4_cRGB.png	\N	BLUE
9920	wC11_d1-2_cDAPI	\N	\N
9921	original/PLATE-1/bPLATE_wC11_s4_cRGB.png	\N	GREEN
9922	wC11_d1-2_cGFP	\N	\N
9923	original/PLATE-1/bPLATE_wC11_s4_cRGB.png	\N	RED
9924	wC11_d1-2_cCy5	\N	\N
9925	original/PLATE-1/bPLATE_wA4_s7_cRGB.png	\N	BLUE
9926	wA4_d1-3_cDAPI	\N	\N
9927	original/PLATE-1/bPLATE_wA4_s7_cRGB.png	\N	GREEN
9928	wA4_d1-3_cGFP	\N	\N
9929	original/PLATE-1/bPLATE_wA4_s7_cRGB.png	\N	RED
9930	wA4_d1-3_cCy5	\N	\N
9931	original/PLATE-1/bPLATE_wF6_s1_cRGB.png	\N	BLUE
9932	wF6_d1-1_cDAPI	\N	\N
9933	original/PLATE-1/bPLATE_wF6_s1_cRGB.png	\N	GREEN
9934	wF6_d1-1_cGFP	\N	\N
9935	original/PLATE-1/bPLATE_wF6_s1_cRGB.png	\N	RED
9936	wF6_d1-1_cCy5	\N	\N
9937	original/PLATE-1/bPLATE_wH3_s5_cRGB.png	\N	BLUE
9938	wH3_d2-2_cDAPI	\N	\N
9939	original/PLATE-1/bPLATE_wH3_s5_cRGB.png	\N	GREEN
9940	wH3_d2-2_cGFP	\N	\N
9941	original/PLATE-1/bPLATE_wH3_s5_cRGB.png	\N	RED
9942	wH3_d2-2_cCy5	\N	\N
9943	original/PLATE-1/bPLATE_wE8_s8_cRGB.png	\N	BLUE
9944	wE8_d2-3_cDAPI	\N	\N
9945	original/PLATE-1/bPLATE_wE8_s8_cRGB.png	\N	GREEN
9946	wE8_d2-3_cGFP	\N	\N
9947	original/PLATE-1/bPLATE_wE8_s8_cRGB.png	\N	RED
9948	wE8_d2-3_cCy5	\N	\N
9949	original/PLATE-1/bPLATE_wA12_s5_cRGB.png	\N	BLUE
9950	wA12_d2-2_cDAPI	\N	\N
9951	original/PLATE-1/bPLATE_wA12_s5_cRGB.png	\N	GREEN
9952	wA12_d2-2_cGFP	\N	\N
9953	original/PLATE-1/bPLATE_wA12_s5_cRGB.png	\N	RED
9954	wA12_d2-2_cCy5	\N	\N
9955	original/PLATE-1/bPLATE_wD7_s2_cRGB.png	\N	BLUE
9956	wD7_d2-1_cDAPI	\N	\N
9957	original/PLATE-1/bPLATE_wD7_s2_cRGB.png	\N	GREEN
9958	wD7_d2-1_cGFP	\N	\N
9959	original/PLATE-1/bPLATE_wD7_s2_cRGB.png	\N	RED
9960	wD7_d2-1_cCy5	\N	\N
9961	original/PLATE-1/bPLATE_wD5_s7_cRGB.png	\N	BLUE
9962	wD5_d1-3_cDAPI	\N	\N
9963	original/PLATE-1/bPLATE_wD5_s7_cRGB.png	\N	GREEN
9964	wD5_d1-3_cGFP	\N	\N
9965	original/PLATE-1/bPLATE_wD5_s7_cRGB.png	\N	RED
9966	wD5_d1-3_cCy5	\N	\N
9967	original/PLATE-1/bPLATE_wF12_s4_cRGB.png	\N	BLUE
9968	wF12_d1-2_cDAPI	\N	\N
9969	original/PLATE-1/bPLATE_wF12_s4_cRGB.png	\N	GREEN
9970	wF12_d1-2_cGFP	\N	\N
9971	original/PLATE-1/bPLATE_wF12_s4_cRGB.png	\N	RED
9972	wF12_d1-2_cCy5	\N	\N
9973	original/PLATE-1/bPLATE_wG3_s7_cRGB.png	\N	BLUE
9974	wG3_d1-3_cDAPI	\N	\N
9975	original/PLATE-1/bPLATE_wG3_s7_cRGB.png	\N	GREEN
9976	wG3_d1-3_cGFP	\N	\N
9977	original/PLATE-1/bPLATE_wG3_s7_cRGB.png	\N	RED
9978	wG3_d1-3_cCy5	\N	\N
9979	original/PLATE-1/bPLATE_wB1_s5_cRGB.png	\N	BLUE
9980	wB1_d2-2_cDAPI	\N	\N
9981	original/PLATE-1/bPLATE_wB1_s5_cRGB.png	\N	GREEN
9982	wB1_d2-2_cGFP	\N	\N
9983	original/PLATE-1/bPLATE_wB1_s5_cRGB.png	\N	RED
9984	wB1_d2-2_cCy5	\N	\N
9985	original/PLATE-1/bPLATE_wD8_s2_cRGB.png	\N	BLUE
9986	wD8_d2-1_cDAPI	\N	\N
9987	original/PLATE-1/bPLATE_wD8_s2_cRGB.png	\N	GREEN
9988	wD8_d2-1_cGFP	\N	\N
9989	original/PLATE-1/bPLATE_wD8_s2_cRGB.png	\N	RED
9990	wD8_d2-1_cCy5	\N	\N
9991	original/PLATE-1/bPLATE_wC7_s4_cRGB.png	\N	BLUE
9992	wC7_d1-2_cDAPI	\N	\N
9993	original/PLATE-1/bPLATE_wC7_s4_cRGB.png	\N	GREEN
9994	wC7_d1-2_cGFP	\N	\N
9995	original/PLATE-1/bPLATE_wC7_s4_cRGB.png	\N	RED
9996	wC7_d1-2_cCy5	\N	\N
9997	original/PLATE-1/bPLATE_wF2_s1_cRGB.png	\N	BLUE
9998	wF2_d1-1_cDAPI	\N	\N
9999	original/PLATE-1/bPLATE_wF2_s1_cRGB.png	\N	GREEN
10000	wF2_d1-1_cGFP	\N	\N
10001	original/PLATE-1/bPLATE_wF2_s1_cRGB.png	\N	RED
10002	wF2_d1-1_cCy5	\N	\N
10003	original/PLATE-1/bPLATE_wH8_s6_cRGB.png	\N	BLUE
10004	wH8_d3-2_cDAPI	\N	\N
10005	original/PLATE-1/bPLATE_wH8_s6_cRGB.png	\N	GREEN
10006	wH8_d3-2_cGFP	\N	\N
10007	original/PLATE-1/bPLATE_wH8_s6_cRGB.png	\N	RED
10008	wH8_d3-2_cCy5	\N	\N
10009	original/PLATE-1/bPLATE_wF1_s9_cRGB.png	\N	BLUE
10010	wF1_d3-3_cDAPI	\N	\N
10011	original/PLATE-1/bPLATE_wF1_s9_cRGB.png	\N	GREEN
10012	wF1_d3-3_cGFP	\N	\N
10013	original/PLATE-1/bPLATE_wF1_s9_cRGB.png	\N	RED
10014	wF1_d3-3_cCy5	\N	\N
10015	original/PLATE-1/bPLATE_wC9_s4_cRGB.png	\N	BLUE
10016	wC9_d1-2_cDAPI	\N	\N
10017	original/PLATE-1/bPLATE_wC9_s4_cRGB.png	\N	GREEN
10018	wC9_d1-2_cGFP	\N	\N
10019	original/PLATE-1/bPLATE_wC9_s4_cRGB.png	\N	RED
10020	wC9_d1-2_cCy5	\N	\N
10021	original/PLATE-1/bPLATE_wA2_s7_cRGB.png	\N	BLUE
10022	wA2_d1-3_cDAPI	\N	\N
10023	original/PLATE-1/bPLATE_wA2_s7_cRGB.png	\N	GREEN
10024	wA2_d1-3_cGFP	\N	\N
10025	original/PLATE-1/bPLATE_wA2_s7_cRGB.png	\N	RED
10026	wA2_d1-3_cCy5	\N	\N
10027	original/PLATE-1/bPLATE_wF4_s1_cRGB.png	\N	BLUE
10028	wF4_d1-1_cDAPI	\N	\N
10029	original/PLATE-1/bPLATE_wF4_s1_cRGB.png	\N	GREEN
10030	wF4_d1-1_cGFP	\N	\N
10031	original/PLATE-1/bPLATE_wF4_s1_cRGB.png	\N	RED
10032	wF4_d1-1_cCy5	\N	\N
10033	original/PLATE-1/bPLATE_wH9_s3_cRGB.png	\N	BLUE
10034	wH9_d3-1_cDAPI	\N	\N
10035	original/PLATE-1/bPLATE_wH9_s3_cRGB.png	\N	GREEN
10036	wH9_d3-1_cGFP	\N	\N
10037	original/PLATE-1/bPLATE_wH9_s3_cRGB.png	\N	RED
10038	wH9_d3-1_cCy5	\N	\N
10039	original/PLATE-1/bPLATE_wC7_s9_cRGB.png	\N	BLUE
10040	wC7_d3-3_cDAPI	\N	\N
10041	original/PLATE-1/bPLATE_wC7_s9_cRGB.png	\N	GREEN
10042	wC7_d3-3_cGFP	\N	\N
10043	original/PLATE-1/bPLATE_wC7_s9_cRGB.png	\N	RED
10044	wC7_d3-3_cCy5	\N	\N
10045	original/PLATE-1/bPLATE_wF2_s6_cRGB.png	\N	BLUE
10046	wF2_d3-2_cDAPI	\N	\N
10047	original/PLATE-1/bPLATE_wF2_s6_cRGB.png	\N	GREEN
10048	wF2_d3-2_cGFP	\N	\N
10049	original/PLATE-1/bPLATE_wF2_s6_cRGB.png	\N	RED
10050	wF2_d3-2_cCy5	\N	\N
10051	original/PLATE-1/bPLATE_wA12_s3_cRGB.png	\N	BLUE
10052	wA12_d3-1_cDAPI	\N	\N
10053	original/PLATE-1/bPLATE_wA12_s3_cRGB.png	\N	GREEN
10054	wA12_d3-1_cGFP	\N	\N
10055	original/PLATE-1/bPLATE_wA12_s3_cRGB.png	\N	RED
10056	wA12_d3-1_cCy5	\N	\N
10057	original/PLATE-1/bPLATE_wA10_s3_cRGB.png	\N	BLUE
10058	wA10_d3-1_cDAPI	\N	\N
10059	original/PLATE-1/bPLATE_wA10_s3_cRGB.png	\N	GREEN
10060	wA10_d3-1_cGFP	\N	\N
10061	original/PLATE-1/bPLATE_wA10_s3_cRGB.png	\N	RED
10062	wA10_d3-1_cCy5	\N	\N
10063	original/PLATE-1/bPLATE_wC5_s5_cRGB.png	\N	BLUE
10064	wC5_d2-2_cDAPI	\N	\N
10065	original/PLATE-1/bPLATE_wC5_s5_cRGB.png	\N	GREEN
10066	wC5_d2-2_cGFP	\N	\N
10067	original/PLATE-1/bPLATE_wC5_s5_cRGB.png	\N	RED
10068	wC5_d2-2_cCy5	\N	\N
10069	original/PLATE-1/bPLATE_wE12_s2_cRGB.png	\N	BLUE
10070	wE12_d2-1_cDAPI	\N	\N
10071	original/PLATE-1/bPLATE_wE12_s2_cRGB.png	\N	GREEN
10072	wE12_d2-1_cGFP	\N	\N
10073	original/PLATE-1/bPLATE_wE12_s2_cRGB.png	\N	RED
10074	wE12_d2-1_cCy5	\N	\N
10075	original/PLATE-1/bPLATE_wD10_s1_cRGB.png	\N	BLUE
10076	wD10_d1-1_cDAPI	\N	\N
10077	original/PLATE-1/bPLATE_wD10_s1_cRGB.png	\N	GREEN
10078	wD10_d1-1_cGFP	\N	\N
10079	original/PLATE-1/bPLATE_wD10_s1_cRGB.png	\N	RED
10080	wD10_d1-1_cCy5	\N	\N
10081	original/PLATE-1/bPLATE_wB3_s4_cRGB.png	\N	BLUE
10082	wB3_d1-2_cDAPI	\N	\N
10083	original/PLATE-1/bPLATE_wB3_s4_cRGB.png	\N	GREEN
10084	wB3_d1-2_cGFP	\N	\N
10085	original/PLATE-1/bPLATE_wB3_s4_cRGB.png	\N	RED
10086	wB3_d1-2_cCy5	\N	\N
10087	original/PLATE-1/bPLATE_wD4_s2_cRGB.png	\N	BLUE
10088	wD4_d2-1_cDAPI	\N	\N
10089	original/PLATE-1/bPLATE_wD4_s2_cRGB.png	\N	GREEN
10090	wD4_d2-1_cGFP	\N	\N
10091	original/PLATE-1/bPLATE_wD4_s2_cRGB.png	\N	RED
10092	wD4_d2-1_cCy5	\N	\N
10093	original/PLATE-1/bPLATE_wA9_s5_cRGB.png	\N	BLUE
10094	wA9_d2-2_cDAPI	\N	\N
10095	original/PLATE-1/bPLATE_wA9_s5_cRGB.png	\N	GREEN
10096	wA9_d2-2_cGFP	\N	\N
10097	original/PLATE-1/bPLATE_wA9_s5_cRGB.png	\N	RED
10098	wA9_d2-2_cCy5	\N	\N
10099	original/PLATE-1/bPLATE_wG5_s8_cRGB.png	\N	BLUE
10100	wG5_d2-3_cDAPI	\N	\N
10101	original/PLATE-1/bPLATE_wG5_s8_cRGB.png	\N	GREEN
10102	wG5_d2-3_cGFP	\N	\N
10103	original/PLATE-1/bPLATE_wG5_s8_cRGB.png	\N	RED
10104	wG5_d2-3_cCy5	\N	\N
10105	original/PLATE-1/bPLATE_wF12_s8_cRGB.png	\N	BLUE
10106	wF12_d2-3_cDAPI	\N	\N
10107	original/PLATE-1/bPLATE_wF12_s8_cRGB.png	\N	GREEN
10108	wF12_d2-3_cGFP	\N	\N
10109	original/PLATE-1/bPLATE_wF12_s8_cRGB.png	\N	RED
10110	wF12_d2-3_cCy5	\N	\N
10111	original/PLATE-1/bPLATE_wD3_s2_cRGB.png	\N	BLUE
10112	wD3_d2-1_cDAPI	\N	\N
10113	original/PLATE-1/bPLATE_wD3_s2_cRGB.png	\N	GREEN
10114	wD3_d2-1_cGFP	\N	\N
10115	original/PLATE-1/bPLATE_wD3_s2_cRGB.png	\N	RED
10116	wD3_d2-1_cCy5	\N	\N
10117	original/PLATE-1/bPLATE_wB9_s4_cRGB.png	\N	BLUE
10118	wB9_d1-2_cDAPI	\N	\N
10119	original/PLATE-1/bPLATE_wB9_s4_cRGB.png	\N	GREEN
10120	wB9_d1-2_cGFP	\N	\N
10121	original/PLATE-1/bPLATE_wB9_s4_cRGB.png	\N	RED
10122	wB9_d1-2_cCy5	\N	\N
10123	original/PLATE-1/bPLATE_wA8_s5_cRGB.png	\N	BLUE
10124	wA8_d2-2_cDAPI	\N	\N
10125	original/PLATE-1/bPLATE_wA8_s5_cRGB.png	\N	GREEN
10126	wA8_d2-2_cGFP	\N	\N
10127	original/PLATE-1/bPLATE_wA8_s5_cRGB.png	\N	RED
10128	wA8_d2-2_cCy5	\N	\N
10129	original/PLATE-1/bPLATE_wE4_s1_cRGB.png	\N	BLUE
10130	wE4_d1-1_cDAPI	\N	\N
10131	original/PLATE-1/bPLATE_wE4_s1_cRGB.png	\N	GREEN
10132	wE4_d1-1_cGFP	\N	\N
10133	original/PLATE-1/bPLATE_wE4_s1_cRGB.png	\N	RED
10134	wE4_d1-1_cCy5	\N	\N
10135	original/PLATE-1/bPLATE_wC12_s5_cRGB.png	\N	BLUE
10136	wC12_d2-2_cDAPI	\N	\N
10137	original/PLATE-1/bPLATE_wC12_s5_cRGB.png	\N	GREEN
10138	wC12_d2-2_cGFP	\N	\N
10139	original/PLATE-1/bPLATE_wC12_s5_cRGB.png	\N	RED
10140	wC12_d2-2_cCy5	\N	\N
10141	original/PLATE-1/bPLATE_wA5_s8_cRGB.png	\N	BLUE
10142	wA5_d2-3_cDAPI	\N	\N
10143	original/PLATE-1/bPLATE_wA5_s8_cRGB.png	\N	GREEN
10144	wA5_d2-3_cGFP	\N	\N
10145	original/PLATE-1/bPLATE_wA5_s8_cRGB.png	\N	RED
10146	wA5_d2-3_cCy5	\N	\N
10147	original/PLATE-1/bPLATE_wF7_s2_cRGB.png	\N	BLUE
10148	wF7_d2-1_cDAPI	\N	\N
10149	original/PLATE-1/bPLATE_wF7_s2_cRGB.png	\N	GREEN
10150	wF7_d2-1_cGFP	\N	\N
10151	original/PLATE-1/bPLATE_wF7_s2_cRGB.png	\N	RED
10152	wF7_d2-1_cCy5	\N	\N
10153	original/PLATE-1/bPLATE_wH12_s4_cRGB.png	\N	BLUE
10154	wH12_d1-2_cDAPI	\N	\N
10155	original/PLATE-1/bPLATE_wH12_s4_cRGB.png	\N	GREEN
10156	wH12_d1-2_cGFP	\N	\N
10157	original/PLATE-1/bPLATE_wH12_s4_cRGB.png	\N	RED
10158	wH12_d1-2_cCy5	\N	\N
10159	original/PLATE-1/bPLATE_wF5_s7_cRGB.png	\N	BLUE
10160	wF5_d1-3_cDAPI	\N	\N
10161	original/PLATE-1/bPLATE_wF5_s7_cRGB.png	\N	GREEN
10162	wF5_d1-3_cGFP	\N	\N
10163	original/PLATE-1/bPLATE_wF5_s7_cRGB.png	\N	RED
10164	wF5_d1-3_cCy5	\N	\N
10165	original/PLATE-1/bPLATE_wC9_s9_cRGB.png	\N	BLUE
10166	wC9_d3-3_cDAPI	\N	\N
10167	original/PLATE-1/bPLATE_wC9_s9_cRGB.png	\N	GREEN
10168	wC9_d3-3_cGFP	\N	\N
10169	original/PLATE-1/bPLATE_wC9_s9_cRGB.png	\N	RED
10170	wC9_d3-3_cCy5	\N	\N
10171	original/PLATE-1/bPLATE_wF4_s6_cRGB.png	\N	BLUE
10172	wF4_d3-2_cDAPI	\N	\N
10173	original/PLATE-1/bPLATE_wF4_s6_cRGB.png	\N	GREEN
10174	wF4_d3-2_cGFP	\N	\N
10175	original/PLATE-1/bPLATE_wF4_s6_cRGB.png	\N	RED
10176	wF4_d3-2_cCy5	\N	\N
10177	original/PLATE-1/bPLATE_wH11_s3_cRGB.png	\N	BLUE
10178	wH11_d3-1_cDAPI	\N	\N
10179	original/PLATE-1/bPLATE_wH11_s3_cRGB.png	\N	GREEN
10180	wH11_d3-1_cGFP	\N	\N
10181	original/PLATE-1/bPLATE_wH11_s3_cRGB.png	\N	RED
10182	wH11_d3-1_cCy5	\N	\N
10183	original/PLATE-1/bPLATE_wA3_s3_cRGB.png	\N	BLUE
10184	wA3_d3-1_cDAPI	\N	\N
10185	original/PLATE-1/bPLATE_wA3_s3_cRGB.png	\N	GREEN
10186	wA3_d3-1_cGFP	\N	\N
10187	original/PLATE-1/bPLATE_wA3_s3_cRGB.png	\N	RED
10188	wA3_d3-1_cCy5	\N	\N
10189	original/PLATE-1/bPLATE_wF3_s9_cRGB.png	\N	BLUE
10190	wF3_d3-3_cDAPI	\N	\N
10191	original/PLATE-1/bPLATE_wF3_s9_cRGB.png	\N	GREEN
10192	wF3_d3-3_cGFP	\N	\N
10193	original/PLATE-1/bPLATE_wF3_s9_cRGB.png	\N	RED
10194	wF3_d3-3_cCy5	\N	\N
10195	original/PLATE-1/bPLATE_wH10_s6_cRGB.png	\N	BLUE
10196	wH10_d3-2_cDAPI	\N	\N
10197	original/PLATE-1/bPLATE_wH10_s6_cRGB.png	\N	GREEN
10198	wH10_d3-2_cGFP	\N	\N
10199	original/PLATE-1/bPLATE_wH10_s6_cRGB.png	\N	RED
10200	wH10_d3-2_cCy5	\N	\N
10201	original/PLATE-1/bPLATE_wC10_s6_cRGB.png	\N	BLUE
10202	wC10_d3-2_cDAPI	\N	\N
10203	original/PLATE-1/bPLATE_wC10_s6_cRGB.png	\N	GREEN
10204	wC10_d3-2_cGFP	\N	\N
10205	original/PLATE-1/bPLATE_wC10_s6_cRGB.png	\N	RED
10206	wC10_d3-2_cCy5	\N	\N
10207	original/PLATE-1/bPLATE_wA3_s9_cRGB.png	\N	BLUE
10208	wA3_d3-3_cDAPI	\N	\N
10209	original/PLATE-1/bPLATE_wA3_s9_cRGB.png	\N	GREEN
10210	wA3_d3-3_cGFP	\N	\N
10211	original/PLATE-1/bPLATE_wA3_s9_cRGB.png	\N	RED
10212	wA3_d3-3_cCy5	\N	\N
10213	original/PLATE-1/bPLATE_wB5_s1_cRGB.png	\N	BLUE
10214	wB5_d1-1_cDAPI	\N	\N
10215	original/PLATE-1/bPLATE_wB5_s1_cRGB.png	\N	GREEN
10216	wB5_d1-1_cGFP	\N	\N
10217	original/PLATE-1/bPLATE_wB5_s1_cRGB.png	\N	RED
10218	wB5_d1-1_cCy5	\N	\N
10219	original/PLATE-1/bPLATE_wF5_s3_cRGB.png	\N	BLUE
10220	wF5_d3-1_cDAPI	\N	\N
10221	original/PLATE-1/bPLATE_wF5_s3_cRGB.png	\N	GREEN
10222	wF5_d3-1_cGFP	\N	\N
10223	original/PLATE-1/bPLATE_wF5_s3_cRGB.png	\N	RED
10224	wF5_d3-1_cCy5	\N	\N
10225	original/PLATE-1/bPLATE_wC3_s4_cRGB.png	\N	BLUE
10226	wC3_d1-2_cDAPI	\N	\N
10227	original/PLATE-1/bPLATE_wC3_s4_cRGB.png	\N	GREEN
10228	wC3_d1-2_cGFP	\N	\N
10229	original/PLATE-1/bPLATE_wC3_s4_cRGB.png	\N	RED
10230	wC3_d1-2_cCy5	\N	\N
10231	original/PLATE-1/bPLATE_wB3_s2_cRGB.png	\N	BLUE
10232	wB3_d2-1_cDAPI	\N	\N
10233	original/PLATE-1/bPLATE_wB3_s2_cRGB.png	\N	GREEN
10234	wB3_d2-1_cGFP	\N	\N
10235	original/PLATE-1/bPLATE_wB3_s2_cRGB.png	\N	RED
10236	wB3_d2-1_cCy5	\N	\N
10237	original/PLATE-1/bPLATE_wE10_s1_cRGB.png	\N	BLUE
10238	wE10_d1-1_cDAPI	\N	\N
10239	original/PLATE-1/bPLATE_wE10_s1_cRGB.png	\N	GREEN
10240	wE10_d1-1_cGFP	\N	\N
10241	original/PLATE-1/bPLATE_wE10_s1_cRGB.png	\N	RED
10242	wE10_d1-1_cCy5	\N	\N
10243	original/PLATE-1/bPLATE_wF8_s8_cRGB.png	\N	BLUE
10244	wF8_d2-3_cDAPI	\N	\N
10245	original/PLATE-1/bPLATE_wF8_s8_cRGB.png	\N	GREEN
10246	wF8_d2-3_cGFP	\N	\N
10247	original/PLATE-1/bPLATE_wF8_s8_cRGB.png	\N	RED
10248	wF8_d2-3_cCy5	\N	\N
10249	original/PLATE-1/bPLATE_wD4_s3_cRGB.png	\N	BLUE
10250	wD4_d3-1_cDAPI	\N	\N
10251	original/PLATE-1/bPLATE_wD4_s3_cRGB.png	\N	GREEN
10252	wD4_d3-1_cGFP	\N	\N
10253	original/PLATE-1/bPLATE_wD4_s3_cRGB.png	\N	RED
10254	wD4_d3-1_cCy5	\N	\N
10255	original/PLATE-1/bPLATE_wA9_s6_cRGB.png	\N	BLUE
10256	wA9_d3-2_cDAPI	\N	\N
10257	original/PLATE-1/bPLATE_wA9_s6_cRGB.png	\N	GREEN
10258	wA9_d3-2_cGFP	\N	\N
10259	original/PLATE-1/bPLATE_wA9_s6_cRGB.png	\N	RED
10260	wA9_d3-2_cCy5	\N	\N
10261	original/PLATE-1/bPLATE_wB4_s2_cRGB.png	\N	BLUE
10262	wB4_d2-1_cDAPI	\N	\N
10263	original/PLATE-1/bPLATE_wB4_s2_cRGB.png	\N	GREEN
10264	wB4_d2-1_cGFP	\N	\N
10265	original/PLATE-1/bPLATE_wB4_s2_cRGB.png	\N	RED
10266	wB4_d2-1_cCy5	\N	\N
10267	original/PLATE-1/bPLATE_wH5_s1_cRGB.png	\N	BLUE
10268	wH5_d1-1_cDAPI	\N	\N
10269	original/PLATE-1/bPLATE_wH5_s1_cRGB.png	\N	GREEN
10270	wH5_d1-1_cGFP	\N	\N
10271	original/PLATE-1/bPLATE_wH5_s1_cRGB.png	\N	RED
10272	wH5_d1-1_cCy5	\N	\N
10273	original/PLATE-1/bPLATE_wC3_s7_cRGB.png	\N	BLUE
10274	wC3_d1-3_cDAPI	\N	\N
10275	original/PLATE-1/bPLATE_wC3_s7_cRGB.png	\N	GREEN
10276	wC3_d1-3_cGFP	\N	\N
10277	original/PLATE-1/bPLATE_wC3_s7_cRGB.png	\N	RED
10278	wC3_d1-3_cCy5	\N	\N
10279	original/PLATE-1/bPLATE_wE10_s4_cRGB.png	\N	BLUE
10280	wE10_d1-2_cDAPI	\N	\N
10281	original/PLATE-1/bPLATE_wE10_s4_cRGB.png	\N	GREEN
10282	wE10_d1-2_cGFP	\N	\N
10283	original/PLATE-1/bPLATE_wE10_s4_cRGB.png	\N	RED
10284	wE10_d1-2_cCy5	\N	\N
10285	original/PLATE-1/bPLATE_wC5_s1_cRGB.png	\N	BLUE
10286	wC5_d1-1_cDAPI	\N	\N
10287	original/PLATE-1/bPLATE_wC5_s1_cRGB.png	\N	GREEN
10288	wC5_d1-1_cGFP	\N	\N
10289	original/PLATE-1/bPLATE_wC5_s1_cRGB.png	\N	RED
10290	wC5_d1-1_cCy5	\N	\N
10291	original/PLATE-1/bPLATE_wH1_s6_cRGB.png	\N	BLUE
10292	wH1_d3-2_cDAPI	\N	\N
10293	original/PLATE-1/bPLATE_wH1_s6_cRGB.png	\N	GREEN
10294	wH1_d3-2_cGFP	\N	\N
10295	original/PLATE-1/bPLATE_wH1_s6_cRGB.png	\N	RED
10296	wH1_d3-2_cCy5	\N	\N
10297	original/PLATE-1/bPLATE_wE6_s9_cRGB.png	\N	BLUE
10298	wE6_d3-3_cDAPI	\N	\N
10299	original/PLATE-1/bPLATE_wE6_s9_cRGB.png	\N	GREEN
10300	wE6_d3-3_cGFP	\N	\N
10301	original/PLATE-1/bPLATE_wE6_s9_cRGB.png	\N	RED
10302	wE6_d3-3_cCy5	\N	\N
10303	original/PLATE-1/bPLATE_wA8_s2_cRGB.png	\N	BLUE
10304	wA8_d2-1_cDAPI	\N	\N
10305	original/PLATE-1/bPLATE_wA8_s2_cRGB.png	\N	GREEN
10306	wA8_d2-1_cGFP	\N	\N
10307	original/PLATE-1/bPLATE_wA8_s2_cRGB.png	\N	RED
10308	wA8_d2-1_cCy5	\N	\N
10309	original/PLATE-1/bPLATE_wH8_s2_cRGB.png	\N	BLUE
10310	wH8_d2-1_cDAPI	\N	\N
10311	original/PLATE-1/bPLATE_wH8_s2_cRGB.png	\N	GREEN
10312	wH8_d2-1_cGFP	\N	\N
10313	original/PLATE-1/bPLATE_wH8_s2_cRGB.png	\N	RED
10314	wH8_d2-1_cCy5	\N	\N
10315	original/PLATE-1/bPLATE_wC6_s8_cRGB.png	\N	BLUE
10316	wC6_d2-3_cDAPI	\N	\N
10317	original/PLATE-1/bPLATE_wC6_s8_cRGB.png	\N	GREEN
10318	wC6_d2-3_cGFP	\N	\N
10319	original/PLATE-1/bPLATE_wC6_s8_cRGB.png	\N	RED
10320	wC6_d2-3_cCy5	\N	\N
10321	original/PLATE-1/bPLATE_wF1_s5_cRGB.png	\N	BLUE
10322	wF1_d2-2_cDAPI	\N	\N
10323	original/PLATE-1/bPLATE_wF1_s5_cRGB.png	\N	GREEN
10324	wF1_d2-2_cGFP	\N	\N
10325	original/PLATE-1/bPLATE_wF1_s5_cRGB.png	\N	RED
10326	wF1_d2-2_cCy5	\N	\N
10327	original/PLATE-1/bPLATE_wG5_s7_cRGB.png	\N	BLUE
10328	wG5_d1-3_cDAPI	\N	\N
10329	original/PLATE-1/bPLATE_wG5_s7_cRGB.png	\N	GREEN
10330	wG5_d1-3_cGFP	\N	\N
10331	original/PLATE-1/bPLATE_wG5_s7_cRGB.png	\N	RED
10332	wG5_d1-3_cCy5	\N	\N
10333	original/PLATE-1/bPLATE_wG8_s7_cRGB.png	\N	BLUE
10334	wG8_d1-3_cDAPI	\N	\N
10335	original/PLATE-1/bPLATE_wG8_s7_cRGB.png	\N	GREEN
10336	wG8_d1-3_cGFP	\N	\N
10337	original/PLATE-1/bPLATE_wG8_s7_cRGB.png	\N	RED
10338	wG8_d1-3_cCy5	\N	\N
10339	original/PLATE-1/bPLATE_wC8_s5_cRGB.png	\N	BLUE
10340	wC8_d2-2_cDAPI	\N	\N
10341	original/PLATE-1/bPLATE_wC8_s5_cRGB.png	\N	GREEN
10342	wC8_d2-2_cGFP	\N	\N
10343	original/PLATE-1/bPLATE_wC8_s5_cRGB.png	\N	RED
10344	wC8_d2-2_cCy5	\N	\N
10345	original/PLATE-1/bPLATE_wA1_s8_cRGB.png	\N	BLUE
10346	wA1_d2-3_cDAPI	\N	\N
10347	original/PLATE-1/bPLATE_wA1_s8_cRGB.png	\N	GREEN
10348	wA1_d2-3_cGFP	\N	\N
10349	original/PLATE-1/bPLATE_wA1_s8_cRGB.png	\N	RED
10350	wA1_d2-3_cCy5	\N	\N
10351	original/PLATE-1/bPLATE_wF3_s2_cRGB.png	\N	BLUE
10352	wF3_d2-1_cDAPI	\N	\N
10353	original/PLATE-1/bPLATE_wF3_s2_cRGB.png	\N	GREEN
10354	wF3_d2-1_cGFP	\N	\N
10355	original/PLATE-1/bPLATE_wF3_s2_cRGB.png	\N	RED
10356	wF3_d2-1_cCy5	\N	\N
10357	original/PLATE-1/bPLATE_wC1_s6_cRGB.png	\N	BLUE
10358	wC1_d3-2_cDAPI	\N	\N
10359	original/PLATE-1/bPLATE_wC1_s6_cRGB.png	\N	GREEN
10360	wC1_d3-2_cGFP	\N	\N
10361	original/PLATE-1/bPLATE_wC1_s6_cRGB.png	\N	RED
10362	wC1_d3-2_cCy5	\N	\N
10363	original/PLATE-1/bPLATE_wE8_s3_cRGB.png	\N	BLUE
10364	wE8_d3-1_cDAPI	\N	\N
10365	original/PLATE-1/bPLATE_wE8_s3_cRGB.png	\N	GREEN
10366	wE8_d3-1_cGFP	\N	\N
10367	original/PLATE-1/bPLATE_wE8_s3_cRGB.png	\N	RED
10368	wE8_d3-1_cCy5	\N	\N
10369	original/PLATE-1A/bPLATE_wD3_s1_cRGB.png	\N	BLUE
10370	wD3_d1-1_cDAPI	\N	\N
10371	original/PLATE-1A/bPLATE_wD3_s1_cRGB.png	\N	GREEN
10372	wD3_d1-1_cGFP	\N	\N
10373	original/PLATE-1A/bPLATE_wD3_s1_cRGB.png	\N	RED
10374	wD3_d1-1_cCy5	\N	\N
10375	original/PLATE-1A/bPLATE_wA8_s4_cRGB.png	\N	BLUE
10376	wA8_d1-2_cDAPI	\N	\N
10377	original/PLATE-1A/bPLATE_wA8_s4_cRGB.png	\N	GREEN
10378	wA8_d1-2_cGFP	\N	\N
10379	original/PLATE-1A/bPLATE_wA8_s4_cRGB.png	\N	RED
10380	wA8_d1-2_cCy5	\N	\N
10381	original/PLATE-1A/bPLATE_wD1_s6_cRGB.png	\N	BLUE
10382	wD1_d3-2_cDAPI	\N	\N
10383	original/PLATE-1A/bPLATE_wD1_s6_cRGB.png	\N	GREEN
10384	wD1_d3-2_cGFP	\N	\N
10385	original/PLATE-1A/bPLATE_wD1_s6_cRGB.png	\N	RED
10386	wD1_d3-2_cCy5	\N	\N
10387	original/PLATE-1A/bPLATE_wA6_s9_cRGB.png	\N	BLUE
10388	wA6_d3-3_cDAPI	\N	\N
10389	original/PLATE-1A/bPLATE_wA6_s9_cRGB.png	\N	GREEN
10390	wA6_d3-3_cGFP	\N	\N
10391	original/PLATE-1A/bPLATE_wA6_s9_cRGB.png	\N	RED
10392	wA6_d3-3_cCy5	\N	\N
10393	original/PLATE-1A/bPLATE_wF8_s3_cRGB.png	\N	BLUE
10394	wF8_d3-1_cDAPI	\N	\N
10395	original/PLATE-1A/bPLATE_wF8_s3_cRGB.png	\N	GREEN
10396	wF8_d3-1_cGFP	\N	\N
10397	original/PLATE-1A/bPLATE_wF8_s3_cRGB.png	\N	RED
10398	wF8_d3-1_cCy5	\N	\N
10399	original/PLATE-1A/bPLATE_wC11_s1_cRGB.png	\N	BLUE
10400	wC11_d1-1_cDAPI	\N	\N
10401	original/PLATE-1A/bPLATE_wC11_s1_cRGB.png	\N	GREEN
10402	wC11_d1-1_cGFP	\N	\N
10403	original/PLATE-1A/bPLATE_wC11_s1_cRGB.png	\N	RED
10404	wC11_d1-1_cCy5	\N	\N
10405	original/PLATE-1A/bPLATE_wA4_s4_cRGB.png	\N	BLUE
10406	wA4_d1-2_cDAPI	\N	\N
10407	original/PLATE-1A/bPLATE_wA4_s4_cRGB.png	\N	GREEN
10408	wA4_d1-2_cGFP	\N	\N
10409	original/PLATE-1A/bPLATE_wA4_s4_cRGB.png	\N	RED
10410	wA4_d1-2_cCy5	\N	\N
10411	original/PLATE-1A/bPLATE_wC5_s2_cRGB.png	\N	BLUE
10412	wC5_d2-1_cDAPI	\N	\N
10413	original/PLATE-1A/bPLATE_wC5_s2_cRGB.png	\N	GREEN
10414	wC5_d2-1_cGFP	\N	\N
10415	original/PLATE-1A/bPLATE_wC5_s2_cRGB.png	\N	RED
10416	wC5_d2-1_cCy5	\N	\N
10417	original/PLATE-1A/bPLATE_wH9_s3_cRGB.png	\N	BLUE
10418	wH9_d3-1_cDAPI	\N	\N
10419	original/PLATE-1A/bPLATE_wH9_s3_cRGB.png	\N	GREEN
10420	wH9_d3-1_cGFP	\N	\N
10421	original/PLATE-1A/bPLATE_wH9_s3_cRGB.png	\N	RED
10422	wH9_d3-1_cCy5	\N	\N
10423	original/PLATE-1A/bPLATE_wC7_s9_cRGB.png	\N	BLUE
10424	wC7_d3-3_cDAPI	\N	\N
10425	original/PLATE-1A/bPLATE_wC7_s9_cRGB.png	\N	GREEN
10426	wC7_d3-3_cGFP	\N	\N
10427	original/PLATE-1A/bPLATE_wC7_s9_cRGB.png	\N	RED
10428	wC7_d3-3_cCy5	\N	\N
10429	original/PLATE-1A/bPLATE_wF2_s6_cRGB.png	\N	BLUE
10430	wF2_d3-2_cDAPI	\N	\N
10431	original/PLATE-1A/bPLATE_wF2_s6_cRGB.png	\N	GREEN
10432	wF2_d3-2_cGFP	\N	\N
10433	original/PLATE-1A/bPLATE_wF2_s6_cRGB.png	\N	RED
10434	wF2_d3-2_cCy5	\N	\N
10435	original/PLATE-1A/bPLATE_wA9_s3_cRGB.png	\N	BLUE
10436	wA9_d3-1_cDAPI	\N	\N
10437	original/PLATE-1A/bPLATE_wA9_s3_cRGB.png	\N	GREEN
10438	wA9_d3-1_cGFP	\N	\N
10439	original/PLATE-1A/bPLATE_wA9_s3_cRGB.png	\N	RED
10440	wA9_d3-1_cCy5	\N	\N
10441	original/PLATE-1A/bPLATE_wC7_s4_cRGB.png	\N	BLUE
10442	wC7_d1-2_cDAPI	\N	\N
10443	original/PLATE-1A/bPLATE_wC7_s4_cRGB.png	\N	GREEN
10444	wC7_d1-2_cGFP	\N	\N
10445	original/PLATE-1A/bPLATE_wC7_s4_cRGB.png	\N	RED
10446	wC7_d1-2_cCy5	\N	\N
10447	original/PLATE-1A/bPLATE_wF2_s1_cRGB.png	\N	BLUE
10448	wF2_d1-1_cDAPI	\N	\N
10449	original/PLATE-1A/bPLATE_wF2_s1_cRGB.png	\N	GREEN
10450	wF2_d1-1_cGFP	\N	\N
10451	original/PLATE-1A/bPLATE_wF2_s1_cRGB.png	\N	RED
10452	wF2_d1-1_cCy5	\N	\N
10453	original/PLATE-1A/bPLATE_wA12_s9_cRGB.png	\N	BLUE
10454	wA12_d3-3_cDAPI	\N	\N
10455	original/PLATE-1A/bPLATE_wA12_s9_cRGB.png	\N	GREEN
10456	wA12_d3-3_cGFP	\N	\N
10457	original/PLATE-1A/bPLATE_wA12_s9_cRGB.png	\N	RED
10458	wA12_d3-3_cCy5	\N	\N
10459	original/PLATE-1A/bPLATE_wD7_s6_cRGB.png	\N	BLUE
10460	wD7_d3-2_cDAPI	\N	\N
10461	original/PLATE-1A/bPLATE_wD7_s6_cRGB.png	\N	GREEN
10462	wD7_d3-2_cGFP	\N	\N
10463	original/PLATE-1A/bPLATE_wD7_s6_cRGB.png	\N	RED
10464	wD7_d3-2_cCy5	\N	\N
10465	original/PLATE-1A/bPLATE_wG2_s3_cRGB.png	\N	BLUE
10466	wG2_d3-1_cDAPI	\N	\N
10467	original/PLATE-1A/bPLATE_wG2_s3_cRGB.png	\N	GREEN
10468	wG2_d3-1_cGFP	\N	\N
10469	original/PLATE-1A/bPLATE_wG2_s3_cRGB.png	\N	RED
10470	wG2_d3-1_cCy5	\N	\N
10471	original/PLATE-1A/bPLATE_wC12_s8_cRGB.png	\N	BLUE
10472	wC12_d2-3_cDAPI	\N	\N
10473	original/PLATE-1A/bPLATE_wC12_s8_cRGB.png	\N	GREEN
10474	wC12_d2-3_cGFP	\N	\N
10475	original/PLATE-1A/bPLATE_wC12_s8_cRGB.png	\N	RED
10476	wC12_d2-3_cCy5	\N	\N
10477	original/PLATE-1A/bPLATE_wF7_s5_cRGB.png	\N	BLUE
10478	wF7_d2-2_cDAPI	\N	\N
10479	original/PLATE-1A/bPLATE_wF7_s5_cRGB.png	\N	GREEN
10480	wF7_d2-2_cGFP	\N	\N
10481	original/PLATE-1A/bPLATE_wF7_s5_cRGB.png	\N	RED
10482	wF7_d2-2_cCy5	\N	\N
10483	original/PLATE-1A/bPLATE_wF8_s8_cRGB.png	\N	BLUE
10484	wF8_d2-3_cDAPI	\N	\N
10485	original/PLATE-1A/bPLATE_wF8_s8_cRGB.png	\N	GREEN
10486	wF8_d2-3_cGFP	\N	\N
10487	original/PLATE-1A/bPLATE_wF8_s8_cRGB.png	\N	RED
10488	wF8_d2-3_cCy5	\N	\N
10489	original/PLATE-1A/bPLATE_wB7_s8_cRGB.png	\N	BLUE
10490	wB7_d2-3_cDAPI	\N	\N
10491	original/PLATE-1A/bPLATE_wB7_s8_cRGB.png	\N	GREEN
10492	wB7_d2-3_cGFP	\N	\N
10493	original/PLATE-1A/bPLATE_wB7_s8_cRGB.png	\N	RED
10494	wB7_d2-3_cCy5	\N	\N
10495	original/PLATE-1A/bPLATE_wE2_s5_cRGB.png	\N	BLUE
10496	wE2_d2-2_cDAPI	\N	\N
10497	original/PLATE-1A/bPLATE_wE2_s5_cRGB.png	\N	GREEN
10498	wE2_d2-2_cGFP	\N	\N
10499	original/PLATE-1A/bPLATE_wE2_s5_cRGB.png	\N	RED
10500	wE2_d2-2_cCy5	\N	\N
10501	original/PLATE-1A/bPLATE_wG9_s2_cRGB.png	\N	BLUE
10502	wG9_d2-1_cDAPI	\N	\N
10503	original/PLATE-1A/bPLATE_wG9_s2_cRGB.png	\N	GREEN
10504	wG9_d2-1_cGFP	\N	\N
10505	original/PLATE-1A/bPLATE_wG9_s2_cRGB.png	\N	RED
10506	wG9_d2-1_cCy5	\N	\N
10507	original/PLATE-1A/bPLATE_wA12_s7_cRGB.png	\N	BLUE
10508	wA12_d1-3_cDAPI	\N	\N
10509	original/PLATE-1A/bPLATE_wA12_s7_cRGB.png	\N	GREEN
10510	wA12_d1-3_cGFP	\N	\N
10511	original/PLATE-1A/bPLATE_wA12_s7_cRGB.png	\N	RED
10512	wA12_d1-3_cCy5	\N	\N
10513	original/PLATE-1A/bPLATE_wD7_s4_cRGB.png	\N	BLUE
10514	wD7_d1-2_cDAPI	\N	\N
10515	original/PLATE-1A/bPLATE_wD7_s4_cRGB.png	\N	GREEN
10516	wD7_d1-2_cGFP	\N	\N
10517	original/PLATE-1A/bPLATE_wD7_s4_cRGB.png	\N	RED
10518	wD7_d1-2_cCy5	\N	\N
10519	original/PLATE-1A/bPLATE_wG2_s1_cRGB.png	\N	BLUE
10520	wG2_d1-1_cDAPI	\N	\N
10521	original/PLATE-1A/bPLATE_wG2_s1_cRGB.png	\N	GREEN
10522	wG2_d1-1_cGFP	\N	\N
10523	original/PLATE-1A/bPLATE_wG2_s1_cRGB.png	\N	RED
10524	wG2_d1-1_cCy5	\N	\N
10525	original/PLATE-1A/bPLATE_wA2_s2_cRGB.png	\N	BLUE
10526	wA2_d2-1_cDAPI	\N	\N
10527	original/PLATE-1A/bPLATE_wA2_s2_cRGB.png	\N	GREEN
10528	wA2_d2-1_cGFP	\N	\N
10529	original/PLATE-1A/bPLATE_wA2_s2_cRGB.png	\N	RED
10530	wA2_d2-1_cCy5	\N	\N
10531	original/PLATE-1A/bPLATE_wF3_s8_cRGB.png	\N	BLUE
10532	wF3_d2-3_cDAPI	\N	\N
10533	original/PLATE-1A/bPLATE_wF3_s8_cRGB.png	\N	GREEN
10534	wF3_d2-3_cGFP	\N	\N
10535	original/PLATE-1A/bPLATE_wF3_s8_cRGB.png	\N	RED
10536	wF3_d2-3_cCy5	\N	\N
10537	original/PLATE-1A/bPLATE_wH10_s5_cRGB.png	\N	BLUE
10538	wH10_d2-2_cDAPI	\N	\N
10539	original/PLATE-1A/bPLATE_wH10_s5_cRGB.png	\N	GREEN
10540	wH10_d2-2_cGFP	\N	\N
10541	original/PLATE-1A/bPLATE_wH10_s5_cRGB.png	\N	RED
10542	wH10_d2-2_cCy5	\N	\N
10543	original/PLATE-1A/bPLATE_wB6_s9_cRGB.png	\N	BLUE
10544	wB6_d3-3_cDAPI	\N	\N
10545	original/PLATE-1A/bPLATE_wB6_s9_cRGB.png	\N	GREEN
10546	wB6_d3-3_cGFP	\N	\N
10547	original/PLATE-1A/bPLATE_wB6_s9_cRGB.png	\N	RED
10548	wB6_d3-3_cCy5	\N	\N
10549	original/PLATE-1A/bPLATE_wD6_s8_cRGB.png	\N	BLUE
10550	wD6_d2-3_cDAPI	\N	\N
10551	original/PLATE-1A/bPLATE_wD6_s8_cRGB.png	\N	GREEN
10552	wD6_d2-3_cGFP	\N	\N
10553	original/PLATE-1A/bPLATE_wD6_s8_cRGB.png	\N	RED
10554	wD6_d2-3_cCy5	\N	\N
10555	original/PLATE-1A/bPLATE_wD8_s8_cRGB.png	\N	BLUE
10556	wD8_d2-3_cDAPI	\N	\N
10557	original/PLATE-1A/bPLATE_wD8_s8_cRGB.png	\N	GREEN
10558	wD8_d2-3_cGFP	\N	\N
10559	original/PLATE-1A/bPLATE_wD8_s8_cRGB.png	\N	RED
10560	wD8_d2-3_cCy5	\N	\N
10561	original/PLATE-1A/bPLATE_wE1_s6_cRGB.png	\N	BLUE
10562	wE1_d3-2_cDAPI	\N	\N
10563	original/PLATE-1A/bPLATE_wE1_s6_cRGB.png	\N	GREEN
10564	wE1_d3-2_cGFP	\N	\N
10565	original/PLATE-1A/bPLATE_wE1_s6_cRGB.png	\N	RED
10566	wE1_d3-2_cCy5	\N	\N
10567	original/PLATE-1A/bPLATE_wG1_s5_cRGB.png	\N	BLUE
10568	wG1_d2-2_cDAPI	\N	\N
10569	original/PLATE-1A/bPLATE_wG1_s5_cRGB.png	\N	GREEN
10570	wG1_d2-2_cGFP	\N	\N
10571	original/PLATE-1A/bPLATE_wG1_s5_cRGB.png	\N	RED
10572	wG1_d2-2_cCy5	\N	\N
10573	original/PLATE-1A/bPLATE_wG3_s5_cRGB.png	\N	BLUE
10574	wG3_d2-2_cDAPI	\N	\N
10575	original/PLATE-1A/bPLATE_wG3_s5_cRGB.png	\N	GREEN
10576	wG3_d2-2_cGFP	\N	\N
10577	original/PLATE-1A/bPLATE_wG3_s5_cRGB.png	\N	RED
10578	wG3_d2-2_cCy5	\N	\N
10579	original/PLATE-1A/bPLATE_wG8_s3_cRGB.png	\N	BLUE
10580	wG8_d3-1_cDAPI	\N	\N
10581	original/PLATE-1A/bPLATE_wG8_s3_cRGB.png	\N	GREEN
10582	wG8_d3-1_cGFP	\N	\N
10583	original/PLATE-1A/bPLATE_wG8_s3_cRGB.png	\N	RED
10584	wG8_d3-1_cCy5	\N	\N
10585	original/PLATE-1A/bPLATE_wD2_s7_cRGB.png	\N	BLUE
10586	wD2_d1-3_cDAPI	\N	\N
10587	original/PLATE-1A/bPLATE_wD2_s7_cRGB.png	\N	GREEN
10588	wD2_d1-3_cGFP	\N	\N
10589	original/PLATE-1A/bPLATE_wD2_s7_cRGB.png	\N	RED
10590	wD2_d1-3_cCy5	\N	\N
10591	original/PLATE-1A/bPLATE_wF9_s4_cRGB.png	\N	BLUE
10592	wF9_d1-2_cDAPI	\N	\N
10593	original/PLATE-1A/bPLATE_wF9_s4_cRGB.png	\N	GREEN
10594	wF9_d1-2_cGFP	\N	\N
10595	original/PLATE-1A/bPLATE_wF9_s4_cRGB.png	\N	RED
10596	wF9_d1-2_cCy5	\N	\N
10597	original/PLATE-1A/bPLATE_wA8_s2_cRGB.png	\N	BLUE
10598	wA8_d2-1_cDAPI	\N	\N
10599	original/PLATE-1A/bPLATE_wA8_s2_cRGB.png	\N	GREEN
10600	wA8_d2-1_cGFP	\N	\N
10601	original/PLATE-1A/bPLATE_wA8_s2_cRGB.png	\N	RED
10602	wA8_d2-1_cCy5	\N	\N
10603	original/PLATE-1A/bPLATE_wC1_s4_cRGB.png	\N	BLUE
10604	wC1_d1-2_cDAPI	\N	\N
10605	original/PLATE-1A/bPLATE_wC1_s4_cRGB.png	\N	GREEN
10606	wC1_d1-2_cGFP	\N	\N
10607	original/PLATE-1A/bPLATE_wC1_s4_cRGB.png	\N	RED
10608	wC1_d1-2_cCy5	\N	\N
10609	original/PLATE-1A/bPLATE_wD4_s8_cRGB.png	\N	BLUE
10610	wD4_d2-3_cDAPI	\N	\N
10611	original/PLATE-1A/bPLATE_wD4_s8_cRGB.png	\N	GREEN
10612	wD4_d2-3_cGFP	\N	\N
10613	original/PLATE-1A/bPLATE_wD4_s8_cRGB.png	\N	RED
10614	wD4_d2-3_cCy5	\N	\N
10615	original/PLATE-1A/bPLATE_wE8_s1_cRGB.png	\N	BLUE
10616	wE8_d1-1_cDAPI	\N	\N
10617	original/PLATE-1A/bPLATE_wE8_s1_cRGB.png	\N	GREEN
10618	wE8_d1-1_cGFP	\N	\N
10619	original/PLATE-1A/bPLATE_wE8_s1_cRGB.png	\N	RED
10620	wE8_d1-1_cCy5	\N	\N
10621	original/PLATE-1A/bPLATE_wF11_s5_cRGB.png	\N	BLUE
10622	wF11_d2-2_cDAPI	\N	\N
10623	original/PLATE-1A/bPLATE_wF11_s5_cRGB.png	\N	GREEN
10624	wF11_d2-2_cGFP	\N	\N
10625	original/PLATE-1A/bPLATE_wF11_s5_cRGB.png	\N	RED
10626	wF11_d2-2_cCy5	\N	\N
10627	original/PLATE-1A/bPLATE_wA11_s4_cRGB.png	\N	BLUE
10628	wA11_d1-2_cDAPI	\N	\N
10629	original/PLATE-1A/bPLATE_wA11_s4_cRGB.png	\N	GREEN
10630	wA11_d1-2_cGFP	\N	\N
10631	original/PLATE-1A/bPLATE_wA11_s4_cRGB.png	\N	RED
10632	wA11_d1-2_cCy5	\N	\N
10633	original/PLATE-1A/bPLATE_wD6_s1_cRGB.png	\N	BLUE
10634	wD6_d1-1_cDAPI	\N	\N
10635	original/PLATE-1A/bPLATE_wD6_s1_cRGB.png	\N	GREEN
10636	wD6_d1-1_cGFP	\N	\N
10637	original/PLATE-1A/bPLATE_wD6_s1_cRGB.png	\N	RED
10638	wD6_d1-1_cCy5	\N	\N
10639	original/PLATE-1A/bPLATE_wA4_s2_cRGB.png	\N	BLUE
10640	wA4_d2-1_cDAPI	\N	\N
10641	original/PLATE-1A/bPLATE_wA4_s2_cRGB.png	\N	GREEN
10642	wA4_d2-1_cGFP	\N	\N
10643	original/PLATE-1A/bPLATE_wA4_s2_cRGB.png	\N	RED
10644	wA4_d2-1_cCy5	\N	\N
10645	original/PLATE-1A/bPLATE_wH8_s7_cRGB.png	\N	BLUE
10646	wH8_d1-3_cDAPI	\N	\N
10647	original/PLATE-1A/bPLATE_wH8_s7_cRGB.png	\N	GREEN
10648	wH8_d1-3_cGFP	\N	\N
10649	original/PLATE-1A/bPLATE_wH8_s7_cRGB.png	\N	RED
10650	wH8_d1-3_cCy5	\N	\N
10651	original/PLATE-1A/bPLATE_wH6_s5_cRGB.png	\N	BLUE
10652	wH6_d2-2_cDAPI	\N	\N
10653	original/PLATE-1A/bPLATE_wH6_s5_cRGB.png	\N	GREEN
10654	wH6_d2-2_cGFP	\N	\N
10655	original/PLATE-1A/bPLATE_wH6_s5_cRGB.png	\N	RED
10656	wH6_d2-2_cCy5	\N	\N
10657	original/PLATE-1A/bPLATE_wB11_s4_cRGB.png	\N	BLUE
10658	wB11_d1-2_cDAPI	\N	\N
10659	original/PLATE-1A/bPLATE_wB11_s4_cRGB.png	\N	GREEN
10660	wB11_d1-2_cGFP	\N	\N
10661	original/PLATE-1A/bPLATE_wB11_s4_cRGB.png	\N	RED
10662	wB11_d1-2_cCy5	\N	\N
10663	original/PLATE-1A/bPLATE_wE11_s8_cRGB.png	\N	BLUE
10664	wE11_d2-3_cDAPI	\N	\N
10665	original/PLATE-1A/bPLATE_wE11_s8_cRGB.png	\N	GREEN
10666	wE11_d2-3_cGFP	\N	\N
10667	original/PLATE-1A/bPLATE_wE11_s8_cRGB.png	\N	RED
10668	wE11_d2-3_cCy5	\N	\N
10669	original/PLATE-1A/bPLATE_wE6_s1_cRGB.png	\N	BLUE
10670	wE6_d1-1_cDAPI	\N	\N
10671	original/PLATE-1A/bPLATE_wE6_s1_cRGB.png	\N	GREEN
10672	wE6_d1-1_cGFP	\N	\N
10673	original/PLATE-1A/bPLATE_wE6_s1_cRGB.png	\N	RED
10674	wE6_d1-1_cCy5	\N	\N
10675	original/PLATE-1A/bPLATE_wG11_s7_cRGB.png	\N	BLUE
10676	wG11_d1-3_cDAPI	\N	\N
10677	original/PLATE-1A/bPLATE_wG11_s7_cRGB.png	\N	GREEN
10678	wG11_d1-3_cGFP	\N	\N
10679	original/PLATE-1A/bPLATE_wG11_s7_cRGB.png	\N	RED
10680	wG11_d1-3_cCy5	\N	\N
10681	original/PLATE-1A/bPLATE_wC8_s1_cRGB.png	\N	BLUE
10682	wC8_d1-1_cDAPI	\N	\N
10683	original/PLATE-1A/bPLATE_wC8_s1_cRGB.png	\N	GREEN
10684	wC8_d1-1_cGFP	\N	\N
10685	original/PLATE-1A/bPLATE_wC8_s1_cRGB.png	\N	RED
10686	wC8_d1-1_cCy5	\N	\N
10687	original/PLATE-1A/bPLATE_wA1_s4_cRGB.png	\N	BLUE
10688	wA1_d1-2_cDAPI	\N	\N
10689	original/PLATE-1A/bPLATE_wA1_s4_cRGB.png	\N	GREEN
10690	wA1_d1-2_cGFP	\N	\N
10691	original/PLATE-1A/bPLATE_wA1_s4_cRGB.png	\N	RED
10692	wA1_d1-2_cCy5	\N	\N
10693	original/PLATE-1A/bPLATE_wC8_s5_cRGB.png	\N	BLUE
10694	wC8_d2-2_cDAPI	\N	\N
10695	original/PLATE-1A/bPLATE_wC8_s5_cRGB.png	\N	GREEN
10696	wC8_d2-2_cGFP	\N	\N
10697	original/PLATE-1A/bPLATE_wC8_s5_cRGB.png	\N	RED
10698	wC8_d2-2_cCy5	\N	\N
10699	original/PLATE-1A/bPLATE_wA1_s8_cRGB.png	\N	BLUE
10700	wA1_d2-3_cDAPI	\N	\N
10701	original/PLATE-1A/bPLATE_wA1_s8_cRGB.png	\N	GREEN
10702	wA1_d2-3_cGFP	\N	\N
10703	original/PLATE-1A/bPLATE_wA1_s8_cRGB.png	\N	RED
10704	wA1_d2-3_cCy5	\N	\N
10705	original/PLATE-1A/bPLATE_wF3_s2_cRGB.png	\N	BLUE
10706	wF3_d2-1_cDAPI	\N	\N
10707	original/PLATE-1A/bPLATE_wF3_s2_cRGB.png	\N	GREEN
10708	wF3_d2-1_cGFP	\N	\N
10709	original/PLATE-1A/bPLATE_wF3_s2_cRGB.png	\N	RED
10710	wF3_d2-1_cCy5	\N	\N
10711	original/PLATE-1A/bPLATE_wF9_s9_cRGB.png	\N	BLUE
10712	wF9_d3-3_cDAPI	\N	\N
10713	original/PLATE-1A/bPLATE_wF9_s9_cRGB.png	\N	GREEN
10714	wF9_d3-3_cGFP	\N	\N
10715	original/PLATE-1A/bPLATE_wF9_s9_cRGB.png	\N	RED
10716	wF9_d3-3_cCy5	\N	\N
10717	original/PLATE-1A/bPLATE_wD2_s3_cRGB.png	\N	BLUE
10718	wD2_d3-1_cDAPI	\N	\N
10719	original/PLATE-1A/bPLATE_wD2_s3_cRGB.png	\N	GREEN
10720	wD2_d3-1_cGFP	\N	\N
10721	original/PLATE-1A/bPLATE_wD2_s3_cRGB.png	\N	RED
10722	wD2_d3-1_cCy5	\N	\N
10723	original/PLATE-1A/bPLATE_wA7_s6_cRGB.png	\N	BLUE
10724	wA7_d3-2_cDAPI	\N	\N
10725	original/PLATE-1A/bPLATE_wA7_s6_cRGB.png	\N	GREEN
10726	wA7_d3-2_cGFP	\N	\N
10727	original/PLATE-1A/bPLATE_wA7_s6_cRGB.png	\N	RED
10728	wA7_d3-2_cCy5	\N	\N
10729	original/PLATE-1A/bPLATE_wA7_s1_cRGB.png	\N	BLUE
10730	wA7_d1-1_cDAPI	\N	\N
10731	original/PLATE-1A/bPLATE_wA7_s1_cRGB.png	\N	GREEN
10732	wA7_d1-1_cGFP	\N	\N
10733	original/PLATE-1A/bPLATE_wA7_s1_cRGB.png	\N	RED
10734	wA7_d1-1_cCy5	\N	\N
10735	original/PLATE-1A/bPLATE_wD10_s1_cRGB.png	\N	BLUE
10736	wD10_d1-1_cDAPI	\N	\N
10737	original/PLATE-1A/bPLATE_wD10_s1_cRGB.png	\N	GREEN
10738	wD10_d1-1_cGFP	\N	\N
10739	original/PLATE-1A/bPLATE_wD10_s1_cRGB.png	\N	RED
10740	wD10_d1-1_cCy5	\N	\N
10741	original/PLATE-1A/bPLATE_wB3_s4_cRGB.png	\N	BLUE
10742	wB3_d1-2_cDAPI	\N	\N
10743	original/PLATE-1A/bPLATE_wB3_s4_cRGB.png	\N	GREEN
10744	wB3_d1-2_cGFP	\N	\N
10745	original/PLATE-1A/bPLATE_wB3_s4_cRGB.png	\N	RED
10746	wB3_d1-2_cCy5	\N	\N
10747	original/PLATE-1A/bPLATE_wB9_s2_cRGB.png	\N	BLUE
10748	wB9_d2-1_cDAPI	\N	\N
10749	original/PLATE-1A/bPLATE_wB9_s2_cRGB.png	\N	GREEN
10750	wB9_d2-1_cGFP	\N	\N
10751	original/PLATE-1A/bPLATE_wB9_s2_cRGB.png	\N	RED
10752	wB9_d2-1_cCy5	\N	\N
10753	original/PLATE-1A/bPLATE_wH2_s9_cRGB.png	\N	BLUE
10754	wH2_d3-3_cDAPI	\N	\N
10755	original/PLATE-1A/bPLATE_wH2_s9_cRGB.png	\N	GREEN
10756	wH2_d3-3_cGFP	\N	\N
10757	original/PLATE-1A/bPLATE_wH2_s9_cRGB.png	\N	RED
10758	wH2_d3-3_cCy5	\N	\N
10759	original/PLATE-1A/bPLATE_wC8_s7_cRGB.png	\N	BLUE
10760	wC8_d1-3_cDAPI	\N	\N
10761	original/PLATE-1A/bPLATE_wC8_s7_cRGB.png	\N	GREEN
10762	wC8_d1-3_cGFP	\N	\N
10763	original/PLATE-1A/bPLATE_wC8_s7_cRGB.png	\N	RED
10764	wC8_d1-3_cCy5	\N	\N
10765	original/PLATE-1A/bPLATE_wF3_s4_cRGB.png	\N	BLUE
10766	wF3_d1-2_cDAPI	\N	\N
10767	original/PLATE-1A/bPLATE_wF3_s4_cRGB.png	\N	GREEN
10768	wF3_d1-2_cGFP	\N	\N
10769	original/PLATE-1A/bPLATE_wF3_s4_cRGB.png	\N	RED
10770	wF3_d1-2_cCy5	\N	\N
10771	original/PLATE-1A/bPLATE_wH10_s1_cRGB.png	\N	BLUE
10772	wH10_d1-1_cDAPI	\N	\N
10773	original/PLATE-1A/bPLATE_wH10_s1_cRGB.png	\N	GREEN
10774	wH10_d1-1_cGFP	\N	\N
10775	original/PLATE-1A/bPLATE_wH10_s1_cRGB.png	\N	RED
10776	wH10_d1-1_cCy5	\N	\N
10777	original/PLATE-1A/bPLATE_wD4_s7_cRGB.png	\N	BLUE
10778	wD4_d1-3_cDAPI	\N	\N
10779	original/PLATE-1A/bPLATE_wD4_s7_cRGB.png	\N	GREEN
10780	wD4_d1-3_cGFP	\N	\N
10781	original/PLATE-1A/bPLATE_wD4_s7_cRGB.png	\N	RED
10782	wD4_d1-3_cCy5	\N	\N
10783	original/PLATE-1A/bPLATE_wF11_s4_cRGB.png	\N	BLUE
10784	wF11_d1-2_cDAPI	\N	\N
10785	original/PLATE-1A/bPLATE_wF11_s4_cRGB.png	\N	GREEN
10786	wF11_d1-2_cGFP	\N	\N
10787	original/PLATE-1A/bPLATE_wF11_s4_cRGB.png	\N	RED
10788	wF11_d1-2_cCy5	\N	\N
10789	original/PLATE-1A/bPLATE_wA2_s3_cRGB.png	\N	BLUE
10790	wA2_d3-1_cDAPI	\N	\N
10791	original/PLATE-1A/bPLATE_wA2_s3_cRGB.png	\N	GREEN
10792	wA2_d3-1_cGFP	\N	\N
10793	original/PLATE-1A/bPLATE_wA2_s3_cRGB.png	\N	RED
10794	wA2_d3-1_cCy5	\N	\N
10795	original/PLATE-1A/bPLATE_wA5_s2_cRGB.png	\N	BLUE
10796	wA5_d2-1_cDAPI	\N	\N
10797	original/PLATE-1A/bPLATE_wA5_s2_cRGB.png	\N	GREEN
10798	wA5_d2-1_cGFP	\N	\N
10799	original/PLATE-1A/bPLATE_wA5_s2_cRGB.png	\N	RED
10800	wA5_d2-1_cCy5	\N	\N
10801	original/PLATE-1A/bPLATE_wH3_s5_cRGB.png	\N	BLUE
10802	wH3_d2-2_cDAPI	\N	\N
10803	original/PLATE-1A/bPLATE_wH3_s5_cRGB.png	\N	GREEN
10804	wH3_d2-2_cGFP	\N	\N
10805	original/PLATE-1A/bPLATE_wH3_s5_cRGB.png	\N	RED
10806	wH3_d2-2_cCy5	\N	\N
10807	original/PLATE-1A/bPLATE_wE8_s8_cRGB.png	\N	BLUE
10808	wE8_d2-3_cDAPI	\N	\N
10809	original/PLATE-1A/bPLATE_wE8_s8_cRGB.png	\N	GREEN
10810	wE8_d2-3_cGFP	\N	\N
10811	original/PLATE-1A/bPLATE_wE8_s8_cRGB.png	\N	RED
10812	wE8_d2-3_cCy5	\N	\N
10813	original/PLATE-1A/bPLATE_wC7_s6_cRGB.png	\N	BLUE
10814	wC7_d3-2_cDAPI	\N	\N
10815	original/PLATE-1A/bPLATE_wC7_s6_cRGB.png	\N	GREEN
10816	wC7_d3-2_cGFP	\N	\N
10817	original/PLATE-1A/bPLATE_wC7_s6_cRGB.png	\N	RED
10818	wC7_d3-2_cCy5	\N	\N
10819	original/PLATE-1A/bPLATE_wE3_s9_cRGB.png	\N	BLUE
10820	wE3_d3-3_cDAPI	\N	\N
10821	original/PLATE-1A/bPLATE_wE3_s9_cRGB.png	\N	GREEN
10822	wE3_d3-3_cGFP	\N	\N
10823	original/PLATE-1A/bPLATE_wE3_s9_cRGB.png	\N	RED
10824	wE3_d3-3_cCy5	\N	\N
10825	original/PLATE-1A/bPLATE_wF2_s3_cRGB.png	\N	BLUE
10826	wF2_d3-1_cDAPI	\N	\N
10827	original/PLATE-1A/bPLATE_wF2_s3_cRGB.png	\N	GREEN
10828	wF2_d3-1_cGFP	\N	\N
10829	original/PLATE-1A/bPLATE_wF2_s3_cRGB.png	\N	RED
10830	wF2_d3-1_cCy5	\N	\N
10831	original/PLATE-1A/bPLATE_wG10_s6_cRGB.png	\N	BLUE
10832	wG10_d3-2_cDAPI	\N	\N
10833	original/PLATE-1A/bPLATE_wG10_s6_cRGB.png	\N	GREEN
10834	wG10_d3-2_cGFP	\N	\N
10835	original/PLATE-1A/bPLATE_wG10_s6_cRGB.png	\N	RED
10836	wG10_d3-2_cCy5	\N	\N
10837	original/PLATE-1A/bPLATE_wD4_s4_cRGB.png	\N	BLUE
10838	wD4_d1-2_cDAPI	\N	\N
10839	original/PLATE-1A/bPLATE_wD4_s4_cRGB.png	\N	GREEN
10840	wD4_d1-2_cGFP	\N	\N
10841	original/PLATE-1A/bPLATE_wD4_s4_cRGB.png	\N	RED
10842	wD4_d1-2_cCy5	\N	\N
10843	original/PLATE-1A/bPLATE_wA9_s7_cRGB.png	\N	BLUE
10844	wA9_d1-3_cDAPI	\N	\N
10845	original/PLATE-1A/bPLATE_wA9_s7_cRGB.png	\N	GREEN
10846	wA9_d1-3_cGFP	\N	\N
10847	original/PLATE-1A/bPLATE_wA9_s7_cRGB.png	\N	RED
10848	wA9_d1-3_cCy5	\N	\N
10849	original/PLATE-1A/bPLATE_wF11_s1_cRGB.png	\N	BLUE
10850	wF11_d1-1_cDAPI	\N	\N
10851	original/PLATE-1A/bPLATE_wF11_s1_cRGB.png	\N	GREEN
10852	wF11_d1-1_cGFP	\N	\N
10853	original/PLATE-1A/bPLATE_wF11_s1_cRGB.png	\N	RED
10854	wF11_d1-1_cCy5	\N	\N
10855	original/PLATE-1A/bPLATE_wA6_s3_cRGB.png	\N	BLUE
10856	wA6_d3-1_cDAPI	\N	\N
10857	original/PLATE-1A/bPLATE_wA6_s3_cRGB.png	\N	GREEN
10858	wA6_d3-1_cGFP	\N	\N
10859	original/PLATE-1A/bPLATE_wA6_s3_cRGB.png	\N	RED
10860	wA6_d3-1_cCy5	\N	\N
10861	original/PLATE-1A/bPLATE_wA3_s3_cRGB.png	\N	BLUE
10862	wA3_d3-1_cDAPI	\N	\N
10863	original/PLATE-1A/bPLATE_wA3_s3_cRGB.png	\N	GREEN
10864	wA3_d3-1_cGFP	\N	\N
10865	original/PLATE-1A/bPLATE_wA3_s3_cRGB.png	\N	RED
10866	wA3_d3-1_cCy5	\N	\N
10867	original/PLATE-1A/bPLATE_wH5_s2_cRGB.png	\N	BLUE
10868	wH5_d2-1_cDAPI	\N	\N
10869	original/PLATE-1A/bPLATE_wH5_s2_cRGB.png	\N	GREEN
10870	wH5_d2-1_cGFP	\N	\N
10871	original/PLATE-1A/bPLATE_wH5_s2_cRGB.png	\N	RED
10872	wH5_d2-1_cCy5	\N	\N
10873	original/PLATE-1A/bPLATE_wC3_s8_cRGB.png	\N	BLUE
10874	wC3_d2-3_cDAPI	\N	\N
10875	original/PLATE-1A/bPLATE_wC3_s8_cRGB.png	\N	GREEN
10876	wC3_d2-3_cGFP	\N	\N
10877	original/PLATE-1A/bPLATE_wC3_s8_cRGB.png	\N	RED
10878	wC3_d2-3_cCy5	\N	\N
10879	original/PLATE-1A/bPLATE_wE10_s5_cRGB.png	\N	BLUE
10880	wE10_d2-2_cDAPI	\N	\N
10881	original/PLATE-1A/bPLATE_wE10_s5_cRGB.png	\N	GREEN
10882	wE10_d2-2_cGFP	\N	\N
10883	original/PLATE-1A/bPLATE_wE10_s5_cRGB.png	\N	RED
10884	wE10_d2-2_cCy5	\N	\N
10885	original/PLATE-1A/bPLATE_wB11_s6_cRGB.png	\N	BLUE
10886	wB11_d3-2_cDAPI	\N	\N
10887	original/PLATE-1A/bPLATE_wB11_s6_cRGB.png	\N	GREEN
10888	wB11_d3-2_cGFP	\N	\N
10889	original/PLATE-1A/bPLATE_wB11_s6_cRGB.png	\N	RED
10890	wB11_d3-2_cCy5	\N	\N
10891	original/PLATE-1A/bPLATE_wE6_s3_cRGB.png	\N	BLUE
10892	wE6_d3-1_cDAPI	\N	\N
10893	original/PLATE-1A/bPLATE_wE6_s3_cRGB.png	\N	GREEN
10894	wE6_d3-1_cGFP	\N	\N
10895	original/PLATE-1A/bPLATE_wE6_s3_cRGB.png	\N	RED
10896	wE6_d3-1_cCy5	\N	\N
10897	original/PLATE-1A/bPLATE_wH12_s5_cRGB.png	\N	BLUE
10898	wH12_d2-2_cDAPI	\N	\N
10899	original/PLATE-1A/bPLATE_wH12_s5_cRGB.png	\N	GREEN
10900	wH12_d2-2_cGFP	\N	\N
10901	original/PLATE-1A/bPLATE_wH12_s5_cRGB.png	\N	RED
10902	wH12_d2-2_cCy5	\N	\N
10903	original/PLATE-1A/bPLATE_wF5_s8_cRGB.png	\N	BLUE
10904	wF5_d2-3_cDAPI	\N	\N
10905	original/PLATE-1A/bPLATE_wF5_s8_cRGB.png	\N	GREEN
10906	wF5_d2-3_cGFP	\N	\N
10907	original/PLATE-1A/bPLATE_wF5_s8_cRGB.png	\N	RED
10908	wF5_d2-3_cCy5	\N	\N
10909	original/PLATE-1A/bPLATE_wC2_s1_cRGB.png	\N	BLUE
10910	wC2_d1-1_cDAPI	\N	\N
10911	original/PLATE-1A/bPLATE_wC2_s1_cRGB.png	\N	GREEN
10912	wC2_d1-1_cGFP	\N	\N
10913	original/PLATE-1A/bPLATE_wC2_s1_cRGB.png	\N	RED
10914	wC2_d1-1_cCy5	\N	\N
10915	original/PLATE-1A/bPLATE_wH4_s8_cRGB.png	\N	BLUE
10916	wH4_d2-3_cDAPI	\N	\N
10917	original/PLATE-1A/bPLATE_wH4_s8_cRGB.png	\N	GREEN
10918	wH4_d2-3_cGFP	\N	\N
10919	original/PLATE-1A/bPLATE_wH4_s8_cRGB.png	\N	RED
10920	wH4_d2-3_cCy5	\N	\N
10921	original/PLATE-1A/bPLATE_wH9_s4_cRGB.png	\N	BLUE
10922	wH9_d1-2_cDAPI	\N	\N
10923	original/PLATE-1A/bPLATE_wH9_s4_cRGB.png	\N	GREEN
10924	wH9_d1-2_cGFP	\N	\N
10925	original/PLATE-1A/bPLATE_wH9_s4_cRGB.png	\N	RED
10926	wH9_d1-2_cCy5	\N	\N
10927	original/PLATE-1A/bPLATE_wF2_s7_cRGB.png	\N	BLUE
10928	wF2_d1-3_cDAPI	\N	\N
10929	original/PLATE-1A/bPLATE_wF2_s7_cRGB.png	\N	GREEN
10930	wF2_d1-3_cGFP	\N	\N
10931	original/PLATE-1A/bPLATE_wF2_s7_cRGB.png	\N	RED
10932	wF2_d1-3_cCy5	\N	\N
10933	original/PLATE-1A/bPLATE_wF4_s7_cRGB.png	\N	BLUE
10934	wF4_d1-3_cDAPI	\N	\N
10935	original/PLATE-1A/bPLATE_wF4_s7_cRGB.png	\N	GREEN
10936	wF4_d1-3_cGFP	\N	\N
10937	original/PLATE-1A/bPLATE_wF4_s7_cRGB.png	\N	RED
10938	wF4_d1-3_cCy5	\N	\N
10939	original/PLATE-1A/bPLATE_wH11_s4_cRGB.png	\N	BLUE
10940	wH11_d1-2_cDAPI	\N	\N
10941	original/PLATE-1A/bPLATE_wH11_s4_cRGB.png	\N	GREEN
10942	wH11_d1-2_cGFP	\N	\N
10943	original/PLATE-1A/bPLATE_wH11_s4_cRGB.png	\N	RED
10944	wH11_d1-2_cCy5	\N	\N
10945	original/PLATE-1A/bPLATE_wC6_s6_cRGB.png	\N	BLUE
10946	wC6_d3-2_cDAPI	\N	\N
10947	original/PLATE-1A/bPLATE_wC6_s6_cRGB.png	\N	GREEN
10948	wC6_d3-2_cGFP	\N	\N
10949	original/PLATE-1A/bPLATE_wC6_s6_cRGB.png	\N	RED
10950	wC6_d3-2_cCy5	\N	\N
10951	original/PLATE-1A/bPLATE_wF1_s3_cRGB.png	\N	BLUE
10952	wF1_d3-1_cDAPI	\N	\N
10953	original/PLATE-1A/bPLATE_wF1_s3_cRGB.png	\N	GREEN
10954	wF1_d3-1_cGFP	\N	\N
10955	original/PLATE-1A/bPLATE_wF1_s3_cRGB.png	\N	RED
10956	wF1_d3-1_cCy5	\N	\N
10957	original/PLATE-1A/bPLATE_wH7_s6_cRGB.png	\N	BLUE
10958	wH7_d3-2_cDAPI	\N	\N
10959	original/PLATE-1A/bPLATE_wH7_s6_cRGB.png	\N	GREEN
10960	wH7_d3-2_cGFP	\N	\N
10961	original/PLATE-1A/bPLATE_wH7_s6_cRGB.png	\N	RED
10962	wH7_d3-2_cCy5	\N	\N
10963	original/PLATE-1A/bPLATE_wE12_s9_cRGB.png	\N	BLUE
10964	wE12_d3-3_cDAPI	\N	\N
10965	original/PLATE-1A/bPLATE_wE12_s9_cRGB.png	\N	GREEN
10966	wE12_d3-3_cGFP	\N	\N
10967	original/PLATE-1A/bPLATE_wE12_s9_cRGB.png	\N	RED
10968	wE12_d3-3_cCy5	\N	\N
10969	original/PLATE-1A/bPLATE_wC7_s2_cRGB.png	\N	BLUE
10970	wC7_d2-1_cDAPI	\N	\N
10971	original/PLATE-1A/bPLATE_wC7_s2_cRGB.png	\N	GREEN
10972	wC7_d2-1_cGFP	\N	\N
10973	original/PLATE-1A/bPLATE_wC7_s2_cRGB.png	\N	RED
10974	wC7_d2-1_cCy5	\N	\N
10975	original/PLATE-1A/bPLATE_wB3_s3_cRGB.png	\N	BLUE
10976	wB3_d3-1_cDAPI	\N	\N
10977	original/PLATE-1A/bPLATE_wB3_s3_cRGB.png	\N	GREEN
10978	wB3_d3-1_cGFP	\N	\N
10979	original/PLATE-1A/bPLATE_wB3_s3_cRGB.png	\N	RED
10980	wB3_d3-1_cCy5	\N	\N
10981	original/PLATE-1A/bPLATE_wH6_s6_cRGB.png	\N	BLUE
10982	wH6_d3-2_cDAPI	\N	\N
10983	original/PLATE-1A/bPLATE_wH6_s6_cRGB.png	\N	GREEN
10984	wH6_d3-2_cGFP	\N	\N
10985	original/PLATE-1A/bPLATE_wH6_s6_cRGB.png	\N	RED
10986	wH6_d3-2_cCy5	\N	\N
10987	original/PLATE-1A/bPLATE_wE11_s9_cRGB.png	\N	BLUE
10988	wE11_d3-3_cDAPI	\N	\N
10989	original/PLATE-1A/bPLATE_wE11_s9_cRGB.png	\N	GREEN
10990	wE11_d3-3_cGFP	\N	\N
10991	original/PLATE-1A/bPLATE_wE11_s9_cRGB.png	\N	RED
10992	wE11_d3-3_cCy5	\N	\N
10993	original/PLATE-1A/bPLATE_wB2_s2_cRGB.png	\N	BLUE
10994	wB2_d2-1_cDAPI	\N	\N
10995	original/PLATE-1A/bPLATE_wB2_s2_cRGB.png	\N	GREEN
10996	wB2_d2-1_cGFP	\N	\N
10997	original/PLATE-1A/bPLATE_wB2_s2_cRGB.png	\N	RED
10998	wB2_d2-1_cCy5	\N	\N
10999	original/PLATE-1A/bPLATE_wA12_s2_cRGB.png	\N	BLUE
11000	wA12_d2-1_cDAPI	\N	\N
11001	original/PLATE-1A/bPLATE_wA12_s2_cRGB.png	\N	GREEN
11002	wA12_d2-1_cGFP	\N	\N
11003	original/PLATE-1A/bPLATE_wA12_s2_cRGB.png	\N	RED
11004	wA12_d2-1_cCy5	\N	\N
11005	original/PLATE-1A/bPLATE_wD12_s7_cRGB.png	\N	BLUE
11006	wD12_d1-3_cDAPI	\N	\N
11007	original/PLATE-1A/bPLATE_wD12_s7_cRGB.png	\N	GREEN
11008	wD12_d1-3_cGFP	\N	\N
11009	original/PLATE-1A/bPLATE_wD12_s7_cRGB.png	\N	RED
11010	wD12_d1-3_cCy5	\N	\N
11011	original/PLATE-1A/bPLATE_wG7_s4_cRGB.png	\N	BLUE
11012	wG7_d1-2_cDAPI	\N	\N
11013	original/PLATE-1A/bPLATE_wG7_s4_cRGB.png	\N	GREEN
11014	wG7_d1-2_cGFP	\N	\N
11015	original/PLATE-1A/bPLATE_wG7_s4_cRGB.png	\N	RED
11016	wG7_d1-2_cCy5	\N	\N
11017	original/PLATE-1A/bPLATE_wH4_s6_cRGB.png	\N	BLUE
11018	wH4_d3-2_cDAPI	\N	\N
11019	original/PLATE-1A/bPLATE_wH4_s6_cRGB.png	\N	GREEN
11020	wH4_d3-2_cGFP	\N	\N
11021	original/PLATE-1A/bPLATE_wH4_s6_cRGB.png	\N	RED
11022	wH4_d3-2_cCy5	\N	\N
11023	original/PLATE-1A/bPLATE_wE9_s9_cRGB.png	\N	BLUE
11024	wE9_d3-3_cDAPI	\N	\N
11025	original/PLATE-1A/bPLATE_wE9_s9_cRGB.png	\N	GREEN
11026	wE9_d3-3_cGFP	\N	\N
11027	original/PLATE-1A/bPLATE_wE9_s9_cRGB.png	\N	RED
11028	wE9_d3-3_cCy5	\N	\N
11029	original/PLATE-1A/bPLATE_wH2_s4_cRGB.png	\N	BLUE
11030	wH2_d1-2_cDAPI	\N	\N
11031	original/PLATE-1A/bPLATE_wH2_s4_cRGB.png	\N	GREEN
11032	wH2_d1-2_cGFP	\N	\N
11033	original/PLATE-1A/bPLATE_wH2_s4_cRGB.png	\N	RED
11034	wH2_d1-2_cCy5	\N	\N
11035	original/PLATE-1A/bPLATE_wE7_s7_cRGB.png	\N	BLUE
11036	wE7_d1-3_cDAPI	\N	\N
11037	original/PLATE-1A/bPLATE_wE7_s7_cRGB.png	\N	GREEN
11038	wE7_d1-3_cGFP	\N	\N
11039	original/PLATE-1A/bPLATE_wE7_s7_cRGB.png	\N	RED
11040	wE7_d1-3_cCy5	\N	\N
11041	original/PLATE-1A/bPLATE_wF12_s8_cRGB.png	\N	BLUE
11042	wF12_d2-3_cDAPI	\N	\N
11043	original/PLATE-1A/bPLATE_wF12_s8_cRGB.png	\N	GREEN
11044	wF12_d2-3_cGFP	\N	\N
11045	original/PLATE-1A/bPLATE_wF12_s8_cRGB.png	\N	RED
11046	wF12_d2-3_cCy5	\N	\N
11047	original/PLATE-1A/bPLATE_wD10_s6_cRGB.png	\N	BLUE
11048	wD10_d3-2_cDAPI	\N	\N
11049	original/PLATE-1A/bPLATE_wD10_s6_cRGB.png	\N	GREEN
11050	wD10_d3-2_cGFP	\N	\N
11051	original/PLATE-1A/bPLATE_wD10_s6_cRGB.png	\N	RED
11052	wD10_d3-2_cCy5	\N	\N
11053	original/PLATE-1A/bPLATE_wB3_s9_cRGB.png	\N	BLUE
11054	wB3_d3-3_cDAPI	\N	\N
11055	original/PLATE-1A/bPLATE_wB3_s9_cRGB.png	\N	GREEN
11056	wB3_d3-3_cGFP	\N	\N
11057	original/PLATE-1A/bPLATE_wB3_s9_cRGB.png	\N	RED
11058	wB3_d3-3_cCy5	\N	\N
11059	original/PLATE-1A/bPLATE_wG5_s3_cRGB.png	\N	BLUE
11060	wG5_d3-1_cDAPI	\N	\N
11061	original/PLATE-1A/bPLATE_wG5_s3_cRGB.png	\N	GREEN
11062	wG5_d3-1_cGFP	\N	\N
11063	original/PLATE-1A/bPLATE_wG5_s3_cRGB.png	\N	RED
11064	wG5_d3-1_cCy5	\N	\N
11065	original/PLATE-1A/bPLATE_wA3_s1_cRGB.png	\N	BLUE
11066	wA3_d1-1_cDAPI	\N	\N
11067	original/PLATE-1A/bPLATE_wA3_s1_cRGB.png	\N	GREEN
11068	wA3_d1-1_cGFP	\N	\N
11069	original/PLATE-1A/bPLATE_wA3_s1_cRGB.png	\N	RED
11070	wA3_d1-1_cCy5	\N	\N
11071	original/PLATE-1A/bPLATE_wC2_s3_cRGB.png	\N	BLUE
11072	wC2_d3-1_cDAPI	\N	\N
11073	original/PLATE-1A/bPLATE_wC2_s3_cRGB.png	\N	GREEN
11074	wC2_d3-1_cGFP	\N	\N
11075	original/PLATE-1A/bPLATE_wC2_s3_cRGB.png	\N	RED
11076	wC2_d3-1_cCy5	\N	\N
11077	original/PLATE-1A/bPLATE_wB12_s3_cRGB.png	\N	BLUE
11078	wB12_d3-1_cDAPI	\N	\N
11079	original/PLATE-1A/bPLATE_wB12_s3_cRGB.png	\N	GREEN
11080	wB12_d3-1_cGFP	\N	\N
11081	original/PLATE-1A/bPLATE_wB12_s3_cRGB.png	\N	RED
11082	wB12_d3-1_cCy5	\N	\N
11083	original/PLATE-1A/bPLATE_wC10_s3_cRGB.png	\N	BLUE
11084	wC10_d3-1_cDAPI	\N	\N
11085	original/PLATE-1A/bPLATE_wC10_s3_cRGB.png	\N	GREEN
11086	wC10_d3-1_cGFP	\N	\N
11087	original/PLATE-1A/bPLATE_wC10_s3_cRGB.png	\N	RED
11088	wC10_d3-1_cCy5	\N	\N
11089	original/PLATE-1A/bPLATE_wA3_s6_cRGB.png	\N	BLUE
11090	wA3_d3-2_cDAPI	\N	\N
11091	original/PLATE-1A/bPLATE_wA3_s6_cRGB.png	\N	GREEN
11092	wA3_d3-2_cGFP	\N	\N
11093	original/PLATE-1A/bPLATE_wA3_s6_cRGB.png	\N	RED
11094	wA3_d3-2_cCy5	\N	\N
11095	original/PLATE-1A/bPLATE_wF3_s9_cRGB.png	\N	BLUE
11096	wF3_d3-3_cDAPI	\N	\N
11097	original/PLATE-1A/bPLATE_wF3_s9_cRGB.png	\N	GREEN
11098	wF3_d3-3_cGFP	\N	\N
11099	original/PLATE-1A/bPLATE_wF3_s9_cRGB.png	\N	RED
11100	wF3_d3-3_cCy5	\N	\N
11101	original/PLATE-1A/bPLATE_wH10_s6_cRGB.png	\N	BLUE
11102	wH10_d3-2_cDAPI	\N	\N
11103	original/PLATE-1A/bPLATE_wH10_s6_cRGB.png	\N	GREEN
11104	wH10_d3-2_cGFP	\N	\N
11105	original/PLATE-1A/bPLATE_wH10_s6_cRGB.png	\N	RED
11106	wH10_d3-2_cCy5	\N	\N
11107	original/PLATE-1A/bPLATE_wF8_s7_cRGB.png	\N	BLUE
11108	wF8_d1-3_cDAPI	\N	\N
11109	original/PLATE-1A/bPLATE_wF8_s7_cRGB.png	\N	GREEN
11110	wF8_d1-3_cGFP	\N	\N
11111	original/PLATE-1A/bPLATE_wF8_s7_cRGB.png	\N	RED
11112	wF8_d1-3_cCy5	\N	\N
11113	original/PLATE-1A/bPLATE_wD8_s7_cRGB.png	\N	BLUE
11114	wD8_d1-3_cDAPI	\N	\N
11115	original/PLATE-1A/bPLATE_wD8_s7_cRGB.png	\N	GREEN
11116	wD8_d1-3_cGFP	\N	\N
11117	original/PLATE-1A/bPLATE_wD8_s7_cRGB.png	\N	RED
11118	wD8_d1-3_cCy5	\N	\N
11119	original/PLATE-1A/bPLATE_wG3_s4_cRGB.png	\N	BLUE
11120	wG3_d1-2_cDAPI	\N	\N
11121	original/PLATE-1A/bPLATE_wG3_s4_cRGB.png	\N	GREEN
11122	wG3_d1-2_cGFP	\N	\N
11123	original/PLATE-1A/bPLATE_wG3_s4_cRGB.png	\N	RED
11124	wG3_d1-2_cCy5	\N	\N
11125	original/PLATE-1A/bPLATE_wF11_s7_cRGB.png	\N	BLUE
11126	wF11_d1-3_cDAPI	\N	\N
11127	original/PLATE-1A/bPLATE_wF11_s7_cRGB.png	\N	GREEN
11128	wF11_d1-3_cGFP	\N	\N
11129	original/PLATE-1A/bPLATE_wF11_s7_cRGB.png	\N	RED
11130	wF11_d1-3_cCy5	\N	\N
11131	original/PLATE-1A/bPLATE_wH12_s8_cRGB.png	\N	BLUE
11132	wH12_d2-3_cDAPI	\N	\N
11133	original/PLATE-1A/bPLATE_wH12_s8_cRGB.png	\N	GREEN
11134	wH12_d2-3_cGFP	\N	\N
11135	original/PLATE-1A/bPLATE_wH12_s8_cRGB.png	\N	RED
11136	wH12_d2-3_cCy5	\N	\N
11137	original/PLATE-1A/bPLATE_wC6_s3_cRGB.png	\N	BLUE
11138	wC6_d3-1_cDAPI	\N	\N
11139	original/PLATE-1A/bPLATE_wC6_s3_cRGB.png	\N	GREEN
11140	wC6_d3-1_cGFP	\N	\N
11141	original/PLATE-1A/bPLATE_wC6_s3_cRGB.png	\N	RED
11142	wC6_d3-1_cCy5	\N	\N
11143	original/PLATE-1A/bPLATE_wA12_s8_cRGB.png	\N	BLUE
11144	wA12_d2-3_cDAPI	\N	\N
11145	original/PLATE-1A/bPLATE_wA12_s8_cRGB.png	\N	GREEN
11146	wA12_d2-3_cGFP	\N	\N
11147	original/PLATE-1A/bPLATE_wA12_s8_cRGB.png	\N	RED
11148	wA12_d2-3_cCy5	\N	\N
11149	original/PLATE-1A/bPLATE_wB7_s4_cRGB.png	\N	BLUE
11150	wB7_d1-2_cDAPI	\N	\N
11151	original/PLATE-1A/bPLATE_wB7_s4_cRGB.png	\N	GREEN
11152	wB7_d1-2_cGFP	\N	\N
11153	original/PLATE-1A/bPLATE_wB7_s4_cRGB.png	\N	RED
11154	wB7_d1-2_cCy5	\N	\N
11155	original/PLATE-1A/bPLATE_wD7_s5_cRGB.png	\N	BLUE
11156	wD7_d2-2_cDAPI	\N	\N
11157	original/PLATE-1A/bPLATE_wD7_s5_cRGB.png	\N	GREEN
11158	wD7_d2-2_cGFP	\N	\N
11159	original/PLATE-1A/bPLATE_wD7_s5_cRGB.png	\N	RED
11160	wD7_d2-2_cCy5	\N	\N
11161	original/PLATE-1A/bPLATE_wE2_s1_cRGB.png	\N	BLUE
11162	wE2_d1-1_cDAPI	\N	\N
11163	original/PLATE-1A/bPLATE_wE2_s1_cRGB.png	\N	GREEN
11164	wE2_d1-1_cGFP	\N	\N
11165	original/PLATE-1A/bPLATE_wE2_s1_cRGB.png	\N	RED
11166	wE2_d1-1_cCy5	\N	\N
11167	original/PLATE-1A/bPLATE_wG2_s2_cRGB.png	\N	BLUE
11168	wG2_d2-1_cDAPI	\N	\N
11169	original/PLATE-1A/bPLATE_wG2_s2_cRGB.png	\N	GREEN
11170	wG2_d2-1_cGFP	\N	\N
11171	original/PLATE-1A/bPLATE_wG2_s2_cRGB.png	\N	RED
11172	wG2_d2-1_cCy5	\N	\N
11173	original/PLATE-1A/bPLATE_wD10_s5_cRGB.png	\N	BLUE
11174	wD10_d2-2_cDAPI	\N	\N
11175	original/PLATE-1A/bPLATE_wD10_s5_cRGB.png	\N	GREEN
11176	wD10_d2-2_cGFP	\N	\N
11177	original/PLATE-1A/bPLATE_wD10_s5_cRGB.png	\N	RED
11178	wD10_d2-2_cCy5	\N	\N
11179	original/PLATE-1A/bPLATE_wB3_s8_cRGB.png	\N	BLUE
11180	wB3_d2-3_cDAPI	\N	\N
11181	original/PLATE-1A/bPLATE_wB3_s8_cRGB.png	\N	GREEN
11182	wB3_d2-3_cGFP	\N	\N
11183	original/PLATE-1A/bPLATE_wB3_s8_cRGB.png	\N	RED
11184	wB3_d2-3_cCy5	\N	\N
11185	original/PLATE-1A/bPLATE_wG5_s2_cRGB.png	\N	BLUE
11186	wG5_d2-1_cDAPI	\N	\N
11187	original/PLATE-1A/bPLATE_wG5_s2_cRGB.png	\N	GREEN
11188	wG5_d2-1_cGFP	\N	\N
11189	original/PLATE-1A/bPLATE_wG5_s2_cRGB.png	\N	RED
11190	wG5_d2-1_cCy5	\N	\N
11191	original/PLATE-1A/bPLATE_wH8_s8_cRGB.png	\N	BLUE
11192	wH8_d2-3_cDAPI	\N	\N
11193	original/PLATE-1A/bPLATE_wH8_s8_cRGB.png	\N	GREEN
11194	wH8_d2-3_cGFP	\N	\N
11195	original/PLATE-1A/bPLATE_wH8_s8_cRGB.png	\N	RED
11196	wH8_d2-3_cCy5	\N	\N
11197	original/PLATE-1A/bPLATE_wH2_s2_cRGB.png	\N	BLUE
11198	wH2_d2-1_cDAPI	\N	\N
11199	original/PLATE-1A/bPLATE_wH2_s2_cRGB.png	\N	GREEN
11200	wH2_d2-1_cGFP	\N	\N
11201	original/PLATE-1A/bPLATE_wH2_s2_cRGB.png	\N	RED
11202	wH2_d2-1_cCy5	\N	\N
11203	original/PLATE-1A/bPLATE_wB12_s8_cRGB.png	\N	BLUE
11204	wB12_d2-3_cDAPI	\N	\N
11205	original/PLATE-1A/bPLATE_wB12_s8_cRGB.png	\N	GREEN
11206	wB12_d2-3_cGFP	\N	\N
11207	original/PLATE-1A/bPLATE_wB12_s8_cRGB.png	\N	RED
11208	wB12_d2-3_cCy5	\N	\N
11209	original/PLATE-1A/bPLATE_wE7_s5_cRGB.png	\N	BLUE
11210	wE7_d2-2_cDAPI	\N	\N
11211	original/PLATE-1A/bPLATE_wE7_s5_cRGB.png	\N	GREEN
11212	wE7_d2-2_cGFP	\N	\N
11213	original/PLATE-1A/bPLATE_wE7_s5_cRGB.png	\N	RED
11214	wE7_d2-2_cCy5	\N	\N
11215	original/PLATE-1A/bPLATE_wB1_s5_cRGB.png	\N	BLUE
11216	wB1_d2-2_cDAPI	\N	\N
11217	original/PLATE-1A/bPLATE_wB1_s5_cRGB.png	\N	GREEN
11218	wB1_d2-2_cGFP	\N	\N
11219	original/PLATE-1A/bPLATE_wB1_s5_cRGB.png	\N	RED
11220	wB1_d2-2_cCy5	\N	\N
11221	original/PLATE-1A/bPLATE_wD8_s2_cRGB.png	\N	BLUE
11222	wD8_d2-1_cDAPI	\N	\N
11223	original/PLATE-1A/bPLATE_wD8_s2_cRGB.png	\N	GREEN
11224	wD8_d2-1_cGFP	\N	\N
11225	original/PLATE-1A/bPLATE_wD8_s2_cRGB.png	\N	RED
11226	wD8_d2-1_cCy5	\N	\N
11227	original/PLATE-1A/bPLATE_wE3_s7_cRGB.png	\N	BLUE
11228	wE3_d1-3_cDAPI	\N	\N
11229	original/PLATE-1A/bPLATE_wE3_s7_cRGB.png	\N	GREEN
11230	wE3_d1-3_cGFP	\N	\N
11231	original/PLATE-1A/bPLATE_wE3_s7_cRGB.png	\N	RED
11232	wE3_d1-3_cCy5	\N	\N
11233	original/PLATE-1A/bPLATE_wG10_s4_cRGB.png	\N	BLUE
11234	wG10_d1-2_cDAPI	\N	\N
11235	original/PLATE-1A/bPLATE_wG10_s4_cRGB.png	\N	GREEN
11236	wG10_d1-2_cGFP	\N	\N
11237	original/PLATE-1A/bPLATE_wG10_s4_cRGB.png	\N	RED
11238	wG10_d1-2_cCy5	\N	\N
11239	original/PLATE-1A/bPLATE_wH6_s1_cRGB.png	\N	BLUE
11240	wH6_d1-1_cDAPI	\N	\N
11241	original/PLATE-1A/bPLATE_wH6_s1_cRGB.png	\N	GREEN
11242	wH6_d1-1_cGFP	\N	\N
11243	original/PLATE-1A/bPLATE_wH6_s1_cRGB.png	\N	RED
11244	wH6_d1-1_cCy5	\N	\N
11245	original/PLATE-1A/bPLATE_wC4_s7_cRGB.png	\N	BLUE
11246	wC4_d1-3_cDAPI	\N	\N
11247	original/PLATE-1A/bPLATE_wC4_s7_cRGB.png	\N	GREEN
11248	wC4_d1-3_cGFP	\N	\N
11249	original/PLATE-1A/bPLATE_wC4_s7_cRGB.png	\N	RED
11250	wC4_d1-3_cCy5	\N	\N
11251	original/PLATE-1A/bPLATE_wE11_s4_cRGB.png	\N	BLUE
11252	wE11_d1-2_cDAPI	\N	\N
11253	original/PLATE-1A/bPLATE_wE11_s4_cRGB.png	\N	GREEN
11254	wE11_d1-2_cGFP	\N	\N
11255	original/PLATE-1A/bPLATE_wE11_s4_cRGB.png	\N	RED
11256	wE11_d1-2_cCy5	\N	\N
11257	original/PLATE-1A/bPLATE_wH12_s6_cRGB.png	\N	BLUE
11258	wH12_d3-2_cDAPI	\N	\N
11259	original/PLATE-1A/bPLATE_wH12_s6_cRGB.png	\N	GREEN
11260	wH12_d3-2_cGFP	\N	\N
11261	original/PLATE-1A/bPLATE_wH12_s6_cRGB.png	\N	RED
11262	wH12_d3-2_cCy5	\N	\N
11263	original/PLATE-1A/bPLATE_wF5_s9_cRGB.png	\N	BLUE
11264	wF5_d3-3_cDAPI	\N	\N
11265	original/PLATE-1A/bPLATE_wF5_s9_cRGB.png	\N	GREEN
11266	wF5_d3-3_cGFP	\N	\N
11267	original/PLATE-1A/bPLATE_wF5_s9_cRGB.png	\N	RED
11268	wF5_d3-3_cCy5	\N	\N
11269	original/PLATE-1A/bPLATE_wA11_s6_cRGB.png	\N	BLUE
11270	wA11_d3-2_cDAPI	\N	\N
11271	original/PLATE-1A/bPLATE_wA11_s6_cRGB.png	\N	GREEN
11272	wA11_d3-2_cGFP	\N	\N
11273	original/PLATE-1A/bPLATE_wA11_s6_cRGB.png	\N	RED
11274	wA11_d3-2_cCy5	\N	\N
11275	original/PLATE-1A/bPLATE_wD6_s3_cRGB.png	\N	BLUE
11276	wD6_d3-1_cDAPI	\N	\N
11277	original/PLATE-1A/bPLATE_wD6_s3_cRGB.png	\N	GREEN
11278	wD6_d3-1_cGFP	\N	\N
11279	original/PLATE-1A/bPLATE_wD6_s3_cRGB.png	\N	RED
11280	wD6_d3-1_cCy5	\N	\N
11281	original/PLATE-1A/bPLATE_wF7_s9_cRGB.png	\N	BLUE
11282	wF7_d3-3_cDAPI	\N	\N
11283	original/PLATE-1A/bPLATE_wF7_s9_cRGB.png	\N	GREEN
11284	wF7_d3-3_cGFP	\N	\N
11285	original/PLATE-1A/bPLATE_wF7_s9_cRGB.png	\N	RED
11286	wF7_d3-3_cCy5	\N	\N
11287	original/PLATE-1A/bPLATE_wG4_s8_cRGB.png	\N	BLUE
11288	wG4_d2-3_cDAPI	\N	\N
11289	original/PLATE-1A/bPLATE_wG4_s8_cRGB.png	\N	GREEN
11290	wG4_d2-3_cGFP	\N	\N
11291	original/PLATE-1A/bPLATE_wG4_s8_cRGB.png	\N	RED
11292	wG4_d2-3_cCy5	\N	\N
11293	original/PLATE-1A/bPLATE_wB9_s8_cRGB.png	\N	BLUE
11294	wB9_d2-3_cDAPI	\N	\N
11295	original/PLATE-1A/bPLATE_wB9_s8_cRGB.png	\N	GREEN
11296	wB9_d2-3_cGFP	\N	\N
11297	original/PLATE-1A/bPLATE_wB9_s8_cRGB.png	\N	RED
11298	wB9_d2-3_cCy5	\N	\N
11299	original/PLATE-1A/bPLATE_wE4_s5_cRGB.png	\N	BLUE
11300	wE4_d2-2_cDAPI	\N	\N
11301	original/PLATE-1A/bPLATE_wE4_s5_cRGB.png	\N	GREEN
11302	wE4_d2-2_cGFP	\N	\N
11303	original/PLATE-1A/bPLATE_wE4_s5_cRGB.png	\N	RED
11304	wE4_d2-2_cCy5	\N	\N
11305	original/PLATE-1A/bPLATE_wG11_s2_cRGB.png	\N	BLUE
11306	wG11_d2-1_cDAPI	\N	\N
11307	original/PLATE-1A/bPLATE_wG11_s2_cRGB.png	\N	GREEN
11308	wG11_d2-1_cGFP	\N	\N
11309	original/PLATE-1A/bPLATE_wG11_s2_cRGB.png	\N	RED
11310	wG11_d2-1_cCy5	\N	\N
11311	original/PLATE-1A/bPLATE_wH9_s8_cRGB.png	\N	BLUE
11312	wH9_d2-3_cDAPI	\N	\N
11313	original/PLATE-1A/bPLATE_wH9_s8_cRGB.png	\N	GREEN
11314	wH9_d2-3_cGFP	\N	\N
11315	original/PLATE-1A/bPLATE_wH9_s8_cRGB.png	\N	RED
11316	wH9_d2-3_cCy5	\N	\N
11317	original/PLATE-1A/bPLATE_wH2_s1_cRGB.png	\N	BLUE
11318	wH2_d1-1_cDAPI	\N	\N
11319	original/PLATE-1A/bPLATE_wH2_s1_cRGB.png	\N	GREEN
11320	wH2_d1-1_cGFP	\N	\N
11321	original/PLATE-1A/bPLATE_wH2_s1_cRGB.png	\N	RED
11322	wH2_d1-1_cCy5	\N	\N
11323	original/PLATE-1A/bPLATE_wB12_s7_cRGB.png	\N	BLUE
11324	wB12_d1-3_cDAPI	\N	\N
11325	original/PLATE-1A/bPLATE_wB12_s7_cRGB.png	\N	GREEN
11326	wB12_d1-3_cGFP	\N	\N
11327	original/PLATE-1A/bPLATE_wB12_s7_cRGB.png	\N	RED
11328	wB12_d1-3_cCy5	\N	\N
11329	original/PLATE-1A/bPLATE_wE7_s4_cRGB.png	\N	BLUE
11330	wE7_d1-2_cDAPI	\N	\N
11331	original/PLATE-1A/bPLATE_wE7_s4_cRGB.png	\N	GREEN
11332	wE7_d1-2_cGFP	\N	\N
11333	original/PLATE-1A/bPLATE_wE7_s4_cRGB.png	\N	RED
11334	wE7_d1-2_cCy5	\N	\N
11335	original/PLATE-1A/bPLATE_wC12_s3_cRGB.png	\N	BLUE
11336	wC12_d3-1_cDAPI	\N	\N
11337	original/PLATE-1A/bPLATE_wC12_s3_cRGB.png	\N	GREEN
11338	wC12_d3-1_cGFP	\N	\N
11339	original/PLATE-1A/bPLATE_wC12_s3_cRGB.png	\N	RED
11340	wC12_d3-1_cCy5	\N	\N
11341	original/PLATE-1A/bPLATE_wA5_s6_cRGB.png	\N	BLUE
11342	wA5_d3-2_cDAPI	\N	\N
11343	original/PLATE-1A/bPLATE_wA5_s6_cRGB.png	\N	GREEN
11344	wA5_d3-2_cGFP	\N	\N
11345	original/PLATE-1A/bPLATE_wA5_s6_cRGB.png	\N	RED
11346	wA5_d3-2_cCy5	\N	\N
11347	original/PLATE-1A/bPLATE_wH11_s9_cRGB.png	\N	BLUE
11348	wH11_d3-3_cDAPI	\N	\N
11349	original/PLATE-1A/bPLATE_wH11_s9_cRGB.png	\N	GREEN
11350	wH11_d3-3_cGFP	\N	\N
11351	original/PLATE-1A/bPLATE_wH11_s9_cRGB.png	\N	RED
11352	wH11_d3-3_cCy5	\N	\N
11353	original/PLATE-1A/bPLATE_wC3_s4_cRGB.png	\N	BLUE
11354	wC3_d1-2_cDAPI	\N	\N
11355	original/PLATE-1A/bPLATE_wC3_s4_cRGB.png	\N	GREEN
11356	wC3_d1-2_cGFP	\N	\N
11357	original/PLATE-1A/bPLATE_wC3_s4_cRGB.png	\N	RED
11358	wC3_d1-2_cCy5	\N	\N
11359	original/PLATE-1A/bPLATE_wE10_s1_cRGB.png	\N	BLUE
11360	wE10_d1-1_cDAPI	\N	\N
11361	original/PLATE-1A/bPLATE_wE10_s1_cRGB.png	\N	GREEN
11362	wE10_d1-1_cGFP	\N	\N
11363	original/PLATE-1A/bPLATE_wE10_s1_cRGB.png	\N	RED
11364	wE10_d1-1_cCy5	\N	\N
11365	original/PLATE-1A/bPLATE_wC9_s3_cRGB.png	\N	BLUE
11366	wC9_d3-1_cDAPI	\N	\N
11367	original/PLATE-1A/bPLATE_wC9_s3_cRGB.png	\N	GREEN
11368	wC9_d3-1_cGFP	\N	\N
11369	original/PLATE-1A/bPLATE_wC9_s3_cRGB.png	\N	RED
11370	wC9_d3-1_cCy5	\N	\N
11371	original/PLATE-1A/bPLATE_wA2_s6_cRGB.png	\N	BLUE
11372	wA2_d3-2_cDAPI	\N	\N
11373	original/PLATE-1A/bPLATE_wA2_s6_cRGB.png	\N	GREEN
11374	wA2_d3-2_cGFP	\N	\N
11375	original/PLATE-1A/bPLATE_wA2_s6_cRGB.png	\N	RED
11376	wA2_d3-2_cCy5	\N	\N
11377	original/PLATE-1A/bPLATE_wD3_s9_cRGB.png	\N	BLUE
11378	wD3_d3-3_cDAPI	\N	\N
11379	original/PLATE-1A/bPLATE_wD3_s9_cRGB.png	\N	GREEN
11380	wD3_d3-3_cGFP	\N	\N
11381	original/PLATE-1A/bPLATE_wD3_s9_cRGB.png	\N	RED
11382	wD3_d3-3_cCy5	\N	\N
11383	original/PLATE-1A/bPLATE_wF10_s6_cRGB.png	\N	BLUE
11384	wF10_d3-2_cDAPI	\N	\N
11385	original/PLATE-1A/bPLATE_wF10_s6_cRGB.png	\N	GREEN
11386	wF10_d3-2_cGFP	\N	\N
11387	original/PLATE-1A/bPLATE_wF10_s6_cRGB.png	\N	RED
11388	wF10_d3-2_cCy5	\N	\N
11389	original/PLATE-1A/bPLATE_wH3_s7_cRGB.png	\N	BLUE
11390	wH3_d1-3_cDAPI	\N	\N
11391	original/PLATE-1A/bPLATE_wH3_s7_cRGB.png	\N	GREEN
11392	wH3_d1-3_cGFP	\N	\N
11393	original/PLATE-1A/bPLATE_wH3_s7_cRGB.png	\N	RED
11394	wH3_d1-3_cCy5	\N	\N
11395	original/PLATE-1A/bPLATE_wD10_s4_cRGB.png	\N	BLUE
11396	wD10_d1-2_cDAPI	\N	\N
11397	original/PLATE-1A/bPLATE_wD10_s4_cRGB.png	\N	GREEN
11398	wD10_d1-2_cGFP	\N	\N
11399	original/PLATE-1A/bPLATE_wD10_s4_cRGB.png	\N	RED
11400	wD10_d1-2_cCy5	\N	\N
11401	original/PLATE-1A/bPLATE_wD10_s2_cRGB.png	\N	BLUE
11402	wD10_d2-1_cDAPI	\N	\N
11403	original/PLATE-1A/bPLATE_wD10_s2_cRGB.png	\N	GREEN
11404	wD10_d2-1_cGFP	\N	\N
11405	original/PLATE-1A/bPLATE_wD10_s2_cRGB.png	\N	RED
11406	wD10_d2-1_cCy5	\N	\N
11407	original/PLATE-1A/bPLATE_wB3_s5_cRGB.png	\N	BLUE
11408	wB3_d2-2_cDAPI	\N	\N
11409	original/PLATE-1A/bPLATE_wB3_s5_cRGB.png	\N	GREEN
11410	wB3_d2-2_cGFP	\N	\N
11411	original/PLATE-1A/bPLATE_wB3_s5_cRGB.png	\N	RED
11412	wB3_d2-2_cCy5	\N	\N
11413	original/PLATE-1A/bPLATE_wB3_s7_cRGB.png	\N	BLUE
11414	wB3_d1-3_cDAPI	\N	\N
11415	original/PLATE-1A/bPLATE_wB3_s7_cRGB.png	\N	GREEN
11416	wB3_d1-3_cGFP	\N	\N
11417	original/PLATE-1A/bPLATE_wB3_s7_cRGB.png	\N	RED
11418	wB3_d1-3_cCy5	\N	\N
11419	original/PLATE-1A/bPLATE_wG5_s1_cRGB.png	\N	BLUE
11420	wG5_d1-1_cDAPI	\N	\N
11421	original/PLATE-1A/bPLATE_wG5_s1_cRGB.png	\N	GREEN
11422	wG5_d1-1_cGFP	\N	\N
11423	original/PLATE-1A/bPLATE_wG5_s1_cRGB.png	\N	RED
11424	wG5_d1-1_cCy5	\N	\N
11425	original/PLATE-1A/bPLATE_wB11_s1_cRGB.png	\N	BLUE
11426	wB11_d1-1_cDAPI	\N	\N
11427	original/PLATE-1A/bPLATE_wB11_s1_cRGB.png	\N	GREEN
11428	wB11_d1-1_cGFP	\N	\N
11429	original/PLATE-1A/bPLATE_wB11_s1_cRGB.png	\N	RED
11430	wB11_d1-1_cCy5	\N	\N
11431	original/PLATE-1A/bPLATE_wF11_s8_cRGB.png	\N	BLUE
11432	wF11_d2-3_cDAPI	\N	\N
11433	original/PLATE-1A/bPLATE_wF11_s8_cRGB.png	\N	GREEN
11434	wF11_d2-3_cGFP	\N	\N
11435	original/PLATE-1A/bPLATE_wF11_s8_cRGB.png	\N	RED
11436	wF11_d2-3_cCy5	\N	\N
11437	original/PLATE-1A/bPLATE_wD3_s6_cRGB.png	\N	BLUE
11438	wD3_d3-2_cDAPI	\N	\N
11439	original/PLATE-1A/bPLATE_wD3_s6_cRGB.png	\N	GREEN
11440	wD3_d3-2_cGFP	\N	\N
11441	original/PLATE-1A/bPLATE_wD3_s6_cRGB.png	\N	RED
11442	wD3_d3-2_cCy5	\N	\N
11443	original/PLATE-1A/bPLATE_wA8_s9_cRGB.png	\N	BLUE
11444	wA8_d3-3_cDAPI	\N	\N
11445	original/PLATE-1A/bPLATE_wA8_s9_cRGB.png	\N	GREEN
11446	wA8_d3-3_cGFP	\N	\N
11447	original/PLATE-1A/bPLATE_wA8_s9_cRGB.png	\N	RED
11448	wA8_d3-3_cCy5	\N	\N
11449	original/PLATE-1A/bPLATE_wF10_s3_cRGB.png	\N	BLUE
11450	wF10_d3-1_cDAPI	\N	\N
11451	original/PLATE-1A/bPLATE_wF10_s3_cRGB.png	\N	GREEN
11452	wF10_d3-1_cGFP	\N	\N
11453	original/PLATE-1A/bPLATE_wF10_s3_cRGB.png	\N	RED
11454	wF10_d3-1_cCy5	\N	\N
11455	original/PLATE-1A/bPLATE_wH1_s7_cRGB.png	\N	BLUE
11456	wH1_d1-3_cDAPI	\N	\N
11457	original/PLATE-1A/bPLATE_wH1_s7_cRGB.png	\N	GREEN
11458	wH1_d1-3_cGFP	\N	\N
11459	original/PLATE-1A/bPLATE_wH1_s7_cRGB.png	\N	RED
11460	wH1_d1-3_cCy5	\N	\N
11461	original/PLATE-1A/bPLATE_wC4_s2_cRGB.png	\N	BLUE
11462	wC4_d2-1_cDAPI	\N	\N
11463	original/PLATE-1A/bPLATE_wC4_s2_cRGB.png	\N	GREEN
11464	wC4_d2-1_cGFP	\N	\N
11465	original/PLATE-1A/bPLATE_wC4_s2_cRGB.png	\N	RED
11466	wC4_d2-1_cCy5	\N	\N
11467	original/PLATE-1A/bPLATE_wG1_s8_cRGB.png	\N	BLUE
11468	wG1_d2-3_cDAPI	\N	\N
11469	original/PLATE-1A/bPLATE_wG1_s8_cRGB.png	\N	GREEN
11470	wG1_d2-3_cGFP	\N	\N
11471	original/PLATE-1A/bPLATE_wG1_s8_cRGB.png	\N	RED
11472	wG1_d2-3_cCy5	\N	\N
11473	original/PLATE-1A/bPLATE_wC1_s5_cRGB.png	\N	BLUE
11474	wC1_d2-2_cDAPI	\N	\N
11475	original/PLATE-1A/bPLATE_wC1_s5_cRGB.png	\N	GREEN
11476	wC1_d2-2_cGFP	\N	\N
11477	original/PLATE-1A/bPLATE_wC1_s5_cRGB.png	\N	RED
11478	wC1_d2-2_cCy5	\N	\N
11479	original/PLATE-1A/bPLATE_wE8_s2_cRGB.png	\N	BLUE
11480	wE8_d2-1_cDAPI	\N	\N
11481	original/PLATE-1A/bPLATE_wE8_s2_cRGB.png	\N	GREEN
11482	wE8_d2-1_cGFP	\N	\N
11483	original/PLATE-1A/bPLATE_wE8_s2_cRGB.png	\N	RED
11484	wE8_d2-1_cCy5	\N	\N
11485	original/PLATE-1A/bPLATE_wB8_s9_cRGB.png	\N	BLUE
11486	wB8_d3-3_cDAPI	\N	\N
11487	original/PLATE-1A/bPLATE_wB8_s9_cRGB.png	\N	GREEN
11488	wB8_d3-3_cGFP	\N	\N
11489	original/PLATE-1A/bPLATE_wB8_s9_cRGB.png	\N	RED
11490	wB8_d3-3_cCy5	\N	\N
11491	original/PLATE-1A/bPLATE_wE3_s6_cRGB.png	\N	BLUE
11492	wE3_d3-2_cDAPI	\N	\N
11493	original/PLATE-1A/bPLATE_wE3_s6_cRGB.png	\N	GREEN
11494	wE3_d3-2_cGFP	\N	\N
11495	original/PLATE-1A/bPLATE_wE3_s6_cRGB.png	\N	RED
11496	wE3_d3-2_cCy5	\N	\N
11497	original/PLATE-1A/bPLATE_wG10_s3_cRGB.png	\N	BLUE
11498	wG10_d3-1_cDAPI	\N	\N
11499	original/PLATE-1A/bPLATE_wG10_s3_cRGB.png	\N	GREEN
11500	wG10_d3-1_cGFP	\N	\N
11501	original/PLATE-1A/bPLATE_wG10_s3_cRGB.png	\N	RED
11502	wG10_d3-1_cCy5	\N	\N
11503	original/PLATE-1A/bPLATE_wA9_s1_cRGB.png	\N	BLUE
11504	wA9_d1-1_cDAPI	\N	\N
11505	original/PLATE-1A/bPLATE_wA9_s1_cRGB.png	\N	GREEN
11506	wA9_d1-1_cGFP	\N	\N
11507	original/PLATE-1A/bPLATE_wA9_s1_cRGB.png	\N	RED
11508	wA9_d1-1_cCy5	\N	\N
11509	original/PLATE-1A/bPLATE_wB9_s5_cRGB.png	\N	BLUE
11510	wB9_d2-2_cDAPI	\N	\N
11511	original/PLATE-1A/bPLATE_wB9_s5_cRGB.png	\N	GREEN
11512	wB9_d2-2_cGFP	\N	\N
11513	original/PLATE-1A/bPLATE_wB9_s5_cRGB.png	\N	RED
11514	wB9_d2-2_cCy5	\N	\N
11515	original/PLATE-1A/bPLATE_wE4_s2_cRGB.png	\N	BLUE
11516	wE4_d2-1_cDAPI	\N	\N
11517	original/PLATE-1A/bPLATE_wE4_s2_cRGB.png	\N	GREEN
11518	wE4_d2-1_cGFP	\N	\N
11519	original/PLATE-1A/bPLATE_wE4_s2_cRGB.png	\N	RED
11520	wE4_d2-1_cCy5	\N	\N
11521	original/PLATE-1A/bPLATE_wD12_s2_cRGB.png	\N	BLUE
11522	wD12_d2-1_cDAPI	\N	\N
11523	original/PLATE-1A/bPLATE_wD12_s2_cRGB.png	\N	GREEN
11524	wD12_d2-1_cGFP	\N	\N
11525	original/PLATE-1A/bPLATE_wD12_s2_cRGB.png	\N	RED
11526	wD12_d2-1_cCy5	\N	\N
11527	original/PLATE-1A/bPLATE_wB5_s5_cRGB.png	\N	BLUE
11528	wB5_d2-2_cDAPI	\N	\N
11529	original/PLATE-1A/bPLATE_wB5_s5_cRGB.png	\N	GREEN
11530	wB5_d2-2_cGFP	\N	\N
11531	original/PLATE-1A/bPLATE_wB5_s5_cRGB.png	\N	RED
11532	wB5_d2-2_cCy5	\N	\N
11533	original/PLATE-1A/bPLATE_wC12_s7_cRGB.png	\N	BLUE
11534	wC12_d1-3_cDAPI	\N	\N
11535	original/PLATE-1A/bPLATE_wC12_s7_cRGB.png	\N	GREEN
11536	wC12_d1-3_cGFP	\N	\N
11537	original/PLATE-1A/bPLATE_wC12_s7_cRGB.png	\N	RED
11538	wC12_d1-3_cCy5	\N	\N
11539	original/PLATE-1A/bPLATE_wF7_s4_cRGB.png	\N	BLUE
11540	wF7_d1-2_cDAPI	\N	\N
11541	original/PLATE-1A/bPLATE_wF7_s4_cRGB.png	\N	GREEN
11542	wF7_d1-2_cGFP	\N	\N
11543	original/PLATE-1A/bPLATE_wF7_s4_cRGB.png	\N	RED
11544	wF7_d1-2_cCy5	\N	\N
11545	original/PLATE-1A/bPLATE_wH2_s6_cRGB.png	\N	BLUE
11546	wH2_d3-2_cDAPI	\N	\N
11547	original/PLATE-1A/bPLATE_wH2_s6_cRGB.png	\N	GREEN
11548	wH2_d3-2_cGFP	\N	\N
11549	original/PLATE-1A/bPLATE_wH2_s6_cRGB.png	\N	RED
11550	wH2_d3-2_cCy5	\N	\N
11551	original/PLATE-1A/bPLATE_wE7_s9_cRGB.png	\N	BLUE
11552	wE7_d3-3_cDAPI	\N	\N
11553	original/PLATE-1A/bPLATE_wE7_s9_cRGB.png	\N	GREEN
11554	wE7_d3-3_cGFP	\N	\N
11555	original/PLATE-1A/bPLATE_wE7_s9_cRGB.png	\N	RED
11556	wE7_d3-3_cCy5	\N	\N
11557	original/PLATE-1A/bPLATE_wG7_s8_cRGB.png	\N	BLUE
11558	wG7_d2-3_cDAPI	\N	\N
11559	original/PLATE-1A/bPLATE_wG7_s8_cRGB.png	\N	GREEN
11560	wG7_d2-3_cGFP	\N	\N
11561	original/PLATE-1A/bPLATE_wG7_s8_cRGB.png	\N	RED
11562	wG7_d2-3_cCy5	\N	\N
11563	original/PLATE-1A/bPLATE_wH7_s7_cRGB.png	\N	BLUE
11564	wH7_d1-3_cDAPI	\N	\N
11565	original/PLATE-1A/bPLATE_wH7_s7_cRGB.png	\N	GREEN
11566	wH7_d1-3_cGFP	\N	\N
11567	original/PLATE-1A/bPLATE_wH7_s7_cRGB.png	\N	RED
11568	wH7_d1-3_cCy5	\N	\N
11569	original/PLATE-1A/bPLATE_wH6_s2_cRGB.png	\N	BLUE
11570	wH6_d2-1_cDAPI	\N	\N
11571	original/PLATE-1A/bPLATE_wH6_s2_cRGB.png	\N	GREEN
11572	wH6_d2-1_cGFP	\N	\N
11573	original/PLATE-1A/bPLATE_wH6_s2_cRGB.png	\N	RED
11574	wH6_d2-1_cCy5	\N	\N
11575	original/PLATE-1A/bPLATE_wC4_s8_cRGB.png	\N	BLUE
11576	wC4_d2-3_cDAPI	\N	\N
11577	original/PLATE-1A/bPLATE_wC4_s8_cRGB.png	\N	GREEN
11578	wC4_d2-3_cGFP	\N	\N
11579	original/PLATE-1A/bPLATE_wC4_s8_cRGB.png	\N	RED
11580	wC4_d2-3_cCy5	\N	\N
11581	original/PLATE-1A/bPLATE_wE11_s5_cRGB.png	\N	BLUE
11582	wE11_d2-2_cDAPI	\N	\N
11583	original/PLATE-1A/bPLATE_wE11_s5_cRGB.png	\N	GREEN
11584	wE11_d2-2_cGFP	\N	\N
11585	original/PLATE-1A/bPLATE_wE11_s5_cRGB.png	\N	RED
11586	wE11_d2-2_cCy5	\N	\N
11587	original/PLATE-1A/bPLATE_wB6_s3_cRGB.png	\N	BLUE
11588	wB6_d3-1_cDAPI	\N	\N
11589	original/PLATE-1A/bPLATE_wB6_s3_cRGB.png	\N	GREEN
11590	wB6_d3-1_cGFP	\N	\N
11591	original/PLATE-1A/bPLATE_wB6_s3_cRGB.png	\N	RED
11592	wB6_d3-1_cCy5	\N	\N
11593	original/PLATE-1A/bPLATE_wH2_s3_cRGB.png	\N	BLUE
11594	wH2_d3-1_cDAPI	\N	\N
11595	original/PLATE-1A/bPLATE_wH2_s3_cRGB.png	\N	GREEN
11596	wH2_d3-1_cGFP	\N	\N
11597	original/PLATE-1A/bPLATE_wH2_s3_cRGB.png	\N	RED
11598	wH2_d3-1_cCy5	\N	\N
11599	original/PLATE-1A/bPLATE_wB12_s9_cRGB.png	\N	BLUE
11600	wB12_d3-3_cDAPI	\N	\N
11601	original/PLATE-1A/bPLATE_wB12_s9_cRGB.png	\N	GREEN
11602	wB12_d3-3_cGFP	\N	\N
11603	original/PLATE-1A/bPLATE_wB12_s9_cRGB.png	\N	RED
11604	wB12_d3-3_cCy5	\N	\N
11605	original/PLATE-1A/bPLATE_wE7_s6_cRGB.png	\N	BLUE
11606	wE7_d3-2_cDAPI	\N	\N
11607	original/PLATE-1A/bPLATE_wE7_s6_cRGB.png	\N	GREEN
11608	wE7_d3-2_cGFP	\N	\N
11609	original/PLATE-1A/bPLATE_wE7_s6_cRGB.png	\N	RED
11610	wE7_d3-2_cCy5	\N	\N
11611	original/PLATE-1A/bPLATE_wG11_s9_cRGB.png	\N	BLUE
11612	wG11_d3-3_cDAPI	\N	\N
11613	original/PLATE-1A/bPLATE_wG11_s9_cRGB.png	\N	GREEN
11614	wG11_d3-3_cGFP	\N	\N
11615	original/PLATE-1A/bPLATE_wG11_s9_cRGB.png	\N	RED
11616	wG11_d3-3_cCy5	\N	\N
11617	original/PLATE-1A/bPLATE_wB6_s5_cRGB.png	\N	BLUE
11618	wB6_d2-2_cDAPI	\N	\N
11619	original/PLATE-1A/bPLATE_wB6_s5_cRGB.png	\N	GREEN
11620	wB6_d2-2_cGFP	\N	\N
11621	original/PLATE-1A/bPLATE_wB6_s5_cRGB.png	\N	RED
11622	wB6_d2-2_cCy5	\N	\N
11623	original/PLATE-1A/bPLATE_wE1_s2_cRGB.png	\N	BLUE
11624	wE1_d2-1_cDAPI	\N	\N
11625	original/PLATE-1A/bPLATE_wE1_s2_cRGB.png	\N	GREEN
11626	wE1_d2-1_cGFP	\N	\N
11627	original/PLATE-1A/bPLATE_wE1_s2_cRGB.png	\N	RED
11628	wE1_d2-1_cCy5	\N	\N
11629	original/PLATE-1A/bPLATE_wC2_s4_cRGB.png	\N	BLUE
11630	wC2_d1-2_cDAPI	\N	\N
11631	original/PLATE-1A/bPLATE_wC2_s4_cRGB.png	\N	GREEN
11632	wC2_d1-2_cGFP	\N	\N
11633	original/PLATE-1A/bPLATE_wC2_s4_cRGB.png	\N	RED
11634	wC2_d1-2_cCy5	\N	\N
11635	original/PLATE-1A/bPLATE_wE9_s1_cRGB.png	\N	BLUE
11636	wE9_d1-1_cDAPI	\N	\N
11637	original/PLATE-1A/bPLATE_wE9_s1_cRGB.png	\N	GREEN
11638	wE9_d1-1_cGFP	\N	\N
11639	original/PLATE-1A/bPLATE_wE9_s1_cRGB.png	\N	RED
11640	wE9_d1-1_cCy5	\N	\N
11641	original/PLATE-1A/bPLATE_wB12_s5_cRGB.png	\N	BLUE
11642	wB12_d2-2_cDAPI	\N	\N
11643	original/PLATE-1A/bPLATE_wB12_s5_cRGB.png	\N	GREEN
11644	wB12_d2-2_cGFP	\N	\N
11645	original/PLATE-1A/bPLATE_wB12_s5_cRGB.png	\N	RED
11646	wB12_d2-2_cCy5	\N	\N
11647	original/PLATE-1A/bPLATE_wE7_s2_cRGB.png	\N	BLUE
11648	wE7_d2-1_cDAPI	\N	\N
11649	original/PLATE-1A/bPLATE_wE7_s2_cRGB.png	\N	GREEN
11650	wE7_d2-1_cGFP	\N	\N
11651	original/PLATE-1A/bPLATE_wE7_s2_cRGB.png	\N	RED
11652	wE7_d2-1_cCy5	\N	\N
11653	original/PLATE-1A/bPLATE_wB5_s2_cRGB.png	\N	BLUE
11654	wB5_d2-1_cDAPI	\N	\N
11655	original/PLATE-1A/bPLATE_wB5_s2_cRGB.png	\N	GREEN
11656	wB5_d2-1_cGFP	\N	\N
11657	original/PLATE-1A/bPLATE_wB5_s2_cRGB.png	\N	RED
11658	wB5_d2-1_cCy5	\N	\N
11659	original/PLATE-1A/bPLATE_wD12_s6_cRGB.png	\N	BLUE
11660	wD12_d3-2_cDAPI	\N	\N
11661	original/PLATE-1A/bPLATE_wD12_s6_cRGB.png	\N	GREEN
11662	wD12_d3-2_cGFP	\N	\N
11663	original/PLATE-1A/bPLATE_wD12_s6_cRGB.png	\N	RED
11664	wD12_d3-2_cCy5	\N	\N
11665	original/PLATE-1A/bPLATE_wB5_s9_cRGB.png	\N	BLUE
11666	wB5_d3-3_cDAPI	\N	\N
11667	original/PLATE-1A/bPLATE_wB5_s9_cRGB.png	\N	GREEN
11668	wB5_d3-3_cGFP	\N	\N
11669	original/PLATE-1A/bPLATE_wB5_s9_cRGB.png	\N	RED
11670	wB5_d3-3_cCy5	\N	\N
11671	original/PLATE-1A/bPLATE_wG7_s3_cRGB.png	\N	BLUE
11672	wG7_d3-1_cDAPI	\N	\N
11673	original/PLATE-1A/bPLATE_wG7_s3_cRGB.png	\N	GREEN
11674	wG7_d3-1_cGFP	\N	\N
11675	original/PLATE-1A/bPLATE_wG7_s3_cRGB.png	\N	RED
11676	wG7_d3-1_cCy5	\N	\N
11677	original/PLATE-1A/bPLATE_wG4_s7_cRGB.png	\N	BLUE
11678	wG4_d1-3_cDAPI	\N	\N
11679	original/PLATE-1A/bPLATE_wG4_s7_cRGB.png	\N	GREEN
11680	wG4_d1-3_cGFP	\N	\N
11681	original/PLATE-1A/bPLATE_wG4_s7_cRGB.png	\N	RED
11682	wG4_d1-3_cCy5	\N	\N
11683	original/PLATE-1A/bPLATE_wF7_s8_cRGB.png	\N	BLUE
11684	wF7_d2-3_cDAPI	\N	\N
11685	original/PLATE-1A/bPLATE_wF7_s8_cRGB.png	\N	GREEN
11686	wF7_d2-3_cGFP	\N	\N
11687	original/PLATE-1A/bPLATE_wF7_s8_cRGB.png	\N	RED
11688	wF7_d2-3_cCy5	\N	\N
11689	original/PLATE-1A/bPLATE_wA8_s1_cRGB.png	\N	BLUE
11690	wA8_d1-1_cDAPI	\N	\N
11691	original/PLATE-1A/bPLATE_wA8_s1_cRGB.png	\N	GREEN
11692	wA8_d1-1_cGFP	\N	\N
11693	original/PLATE-1A/bPLATE_wA8_s1_cRGB.png	\N	RED
11694	wA8_d1-1_cCy5	\N	\N
11695	original/PLATE-1A/bPLATE_wB5_s3_cRGB.png	\N	BLUE
11696	wB5_d3-1_cDAPI	\N	\N
11697	original/PLATE-1A/bPLATE_wB5_s3_cRGB.png	\N	GREEN
11698	wB5_d3-1_cGFP	\N	\N
11699	original/PLATE-1A/bPLATE_wB5_s3_cRGB.png	\N	RED
11700	wB5_d3-1_cCy5	\N	\N
11701	original/PLATE-1A/bPLATE_wC12_s1_cRGB.png	\N	BLUE
11702	wC12_d1-1_cDAPI	\N	\N
11703	original/PLATE-1A/bPLATE_wC12_s1_cRGB.png	\N	GREEN
11704	wC12_d1-1_cGFP	\N	\N
11705	original/PLATE-1A/bPLATE_wC12_s1_cRGB.png	\N	RED
11706	wC12_d1-1_cCy5	\N	\N
11707	original/PLATE-1A/bPLATE_wA5_s4_cRGB.png	\N	BLUE
11708	wA5_d1-2_cDAPI	\N	\N
11709	original/PLATE-1A/bPLATE_wA5_s4_cRGB.png	\N	GREEN
11710	wA5_d1-2_cGFP	\N	\N
11711	original/PLATE-1A/bPLATE_wA5_s4_cRGB.png	\N	RED
11712	wA5_d1-2_cCy5	\N	\N
11713	original/PLATE-1A/bPLATE_wC4_s1_cRGB.png	\N	BLUE
11714	wC4_d1-1_cDAPI	\N	\N
11715	original/PLATE-1A/bPLATE_wC4_s1_cRGB.png	\N	GREEN
11716	wC4_d1-1_cGFP	\N	\N
11717	original/PLATE-1A/bPLATE_wC4_s1_cRGB.png	\N	RED
11718	wC4_d1-1_cCy5	\N	\N
11719	original/PLATE-1A/bPLATE_wG8_s7_cRGB.png	\N	BLUE
11720	wG8_d1-3_cDAPI	\N	\N
11721	original/PLATE-1A/bPLATE_wG8_s7_cRGB.png	\N	GREEN
11722	wG8_d1-3_cGFP	\N	\N
11723	original/PLATE-1A/bPLATE_wG8_s7_cRGB.png	\N	RED
11724	wG8_d1-3_cCy5	\N	\N
11725	original/PLATE-1A/bPLATE_wB3_s1_cRGB.png	\N	BLUE
11726	wB3_d1-1_cDAPI	\N	\N
11727	original/PLATE-1A/bPLATE_wB3_s1_cRGB.png	\N	GREEN
11728	wB3_d1-1_cGFP	\N	\N
11729	original/PLATE-1A/bPLATE_wB3_s1_cRGB.png	\N	RED
11730	wB3_d1-1_cCy5	\N	\N
11731	original/PLATE-1A/bPLATE_wF6_s9_cRGB.png	\N	BLUE
11732	wF6_d3-3_cDAPI	\N	\N
11733	original/PLATE-1A/bPLATE_wF6_s9_cRGB.png	\N	GREEN
11734	wF6_d3-3_cGFP	\N	\N
11735	original/PLATE-1A/bPLATE_wF6_s9_cRGB.png	\N	RED
11736	wF6_d3-3_cCy5	\N	\N
11737	original/PLATE-1A/bPLATE_wD3_s5_cRGB.png	\N	BLUE
11738	wD3_d2-2_cDAPI	\N	\N
11739	original/PLATE-1A/bPLATE_wD3_s5_cRGB.png	\N	GREEN
11740	wD3_d2-2_cGFP	\N	\N
11741	original/PLATE-1A/bPLATE_wD3_s5_cRGB.png	\N	RED
11742	wD3_d2-2_cCy5	\N	\N
11743	original/PLATE-1A/bPLATE_wA8_s8_cRGB.png	\N	BLUE
11744	wA8_d2-3_cDAPI	\N	\N
11745	original/PLATE-1A/bPLATE_wA8_s8_cRGB.png	\N	GREEN
11746	wA8_d2-3_cGFP	\N	\N
11747	original/PLATE-1A/bPLATE_wA8_s8_cRGB.png	\N	RED
11748	wA8_d2-3_cCy5	\N	\N
11749	original/PLATE-1A/bPLATE_wF10_s2_cRGB.png	\N	BLUE
11750	wF10_d2-1_cDAPI	\N	\N
11751	original/PLATE-1A/bPLATE_wF10_s2_cRGB.png	\N	GREEN
11752	wF10_d2-1_cGFP	\N	\N
11753	original/PLATE-1A/bPLATE_wF10_s2_cRGB.png	\N	RED
11754	wF10_d2-1_cCy5	\N	\N
11755	original/PLATE-1A/bPLATE_wD4_s2_cRGB.png	\N	BLUE
11756	wD4_d2-1_cDAPI	\N	\N
11757	original/PLATE-1A/bPLATE_wD4_s2_cRGB.png	\N	GREEN
11758	wD4_d2-1_cGFP	\N	\N
11759	original/PLATE-1A/bPLATE_wD4_s2_cRGB.png	\N	RED
11760	wD4_d2-1_cCy5	\N	\N
11761	original/PLATE-1A/bPLATE_wA9_s5_cRGB.png	\N	BLUE
11762	wA9_d2-2_cDAPI	\N	\N
11763	original/PLATE-1A/bPLATE_wA9_s5_cRGB.png	\N	GREEN
11764	wA9_d2-2_cGFP	\N	\N
11765	original/PLATE-1A/bPLATE_wA9_s5_cRGB.png	\N	RED
11766	wA9_d2-2_cCy5	\N	\N
11767	original/PLATE-1A/bPLATE_wA3_s2_cRGB.png	\N	BLUE
11768	wA3_d2-1_cDAPI	\N	\N
11769	original/PLATE-1A/bPLATE_wA3_s2_cRGB.png	\N	GREEN
11770	wA3_d2-1_cGFP	\N	\N
11771	original/PLATE-1A/bPLATE_wA3_s2_cRGB.png	\N	RED
11772	wA3_d2-1_cCy5	\N	\N
11773	original/PLATE-1A/bPLATE_wC11_s4_cRGB.png	\N	BLUE
11774	wC11_d1-2_cDAPI	\N	\N
11775	original/PLATE-1A/bPLATE_wC11_s4_cRGB.png	\N	GREEN
11776	wC11_d1-2_cGFP	\N	\N
11777	original/PLATE-1A/bPLATE_wC11_s4_cRGB.png	\N	RED
11778	wC11_d1-2_cCy5	\N	\N
11779	original/PLATE-1A/bPLATE_wA4_s7_cRGB.png	\N	BLUE
11780	wA4_d1-3_cDAPI	\N	\N
11781	original/PLATE-1A/bPLATE_wA4_s7_cRGB.png	\N	GREEN
11782	wA4_d1-3_cGFP	\N	\N
11783	original/PLATE-1A/bPLATE_wA4_s7_cRGB.png	\N	RED
11784	wA4_d1-3_cCy5	\N	\N
11785	original/PLATE-1A/bPLATE_wF6_s1_cRGB.png	\N	BLUE
11786	wF6_d1-1_cDAPI	\N	\N
11787	original/PLATE-1A/bPLATE_wF6_s1_cRGB.png	\N	GREEN
11788	wF6_d1-1_cGFP	\N	\N
11789	original/PLATE-1A/bPLATE_wF6_s1_cRGB.png	\N	RED
11790	wF6_d1-1_cCy5	\N	\N
11791	original/PLATE-1A/bPLATE_wB12_s1_cRGB.png	\N	BLUE
11792	wB12_d1-1_cDAPI	\N	\N
11793	original/PLATE-1A/bPLATE_wB12_s1_cRGB.png	\N	GREEN
11794	wB12_d1-1_cGFP	\N	\N
11795	original/PLATE-1A/bPLATE_wB12_s1_cRGB.png	\N	RED
11796	wB12_d1-1_cCy5	\N	\N
11797	original/PLATE-1A/bPLATE_wH3_s4_cRGB.png	\N	BLUE
11798	wH3_d1-2_cDAPI	\N	\N
11799	original/PLATE-1A/bPLATE_wH3_s4_cRGB.png	\N	GREEN
11800	wH3_d1-2_cGFP	\N	\N
11801	original/PLATE-1A/bPLATE_wH3_s4_cRGB.png	\N	RED
11802	wH3_d1-2_cCy5	\N	\N
11803	original/PLATE-1A/bPLATE_wD1_s5_cRGB.png	\N	BLUE
11804	wD1_d2-2_cDAPI	\N	\N
11805	original/PLATE-1A/bPLATE_wD1_s5_cRGB.png	\N	GREEN
11806	wD1_d2-2_cGFP	\N	\N
11807	original/PLATE-1A/bPLATE_wD1_s5_cRGB.png	\N	RED
11808	wD1_d2-2_cCy5	\N	\N
11809	original/PLATE-1A/bPLATE_wA6_s8_cRGB.png	\N	BLUE
11810	wA6_d2-3_cDAPI	\N	\N
11811	original/PLATE-1A/bPLATE_wA6_s8_cRGB.png	\N	GREEN
11812	wA6_d2-3_cGFP	\N	\N
11813	original/PLATE-1A/bPLATE_wA6_s8_cRGB.png	\N	RED
11814	wA6_d2-3_cCy5	\N	\N
11815	original/PLATE-1A/bPLATE_wE8_s7_cRGB.png	\N	BLUE
11816	wE8_d1-3_cDAPI	\N	\N
11817	original/PLATE-1A/bPLATE_wE8_s7_cRGB.png	\N	GREEN
11818	wE8_d1-3_cGFP	\N	\N
11819	original/PLATE-1A/bPLATE_wE8_s7_cRGB.png	\N	RED
11820	wE8_d1-3_cCy5	\N	\N
11821	original/PLATE-1A/bPLATE_wF8_s2_cRGB.png	\N	BLUE
11822	wF8_d2-1_cDAPI	\N	\N
11823	original/PLATE-1A/bPLATE_wF8_s2_cRGB.png	\N	GREEN
11824	wF8_d2-1_cGFP	\N	\N
11825	original/PLATE-1A/bPLATE_wF8_s2_cRGB.png	\N	RED
11826	wF8_d2-1_cCy5	\N	\N
11827	original/PLATE-1A/bPLATE_wE4_s7_cRGB.png	\N	BLUE
11828	wE4_d1-3_cDAPI	\N	\N
11829	original/PLATE-1A/bPLATE_wE4_s7_cRGB.png	\N	GREEN
11830	wE4_d1-3_cGFP	\N	\N
11831	original/PLATE-1A/bPLATE_wE4_s7_cRGB.png	\N	RED
11832	wE4_d1-3_cCy5	\N	\N
11833	original/PLATE-1A/bPLATE_wG11_s4_cRGB.png	\N	BLUE
11834	wG11_d1-2_cDAPI	\N	\N
11835	original/PLATE-1A/bPLATE_wG11_s4_cRGB.png	\N	GREEN
11836	wG11_d1-2_cGFP	\N	\N
11837	original/PLATE-1A/bPLATE_wG11_s4_cRGB.png	\N	RED
11838	wG11_d1-2_cCy5	\N	\N
11839	original/PLATE-1A/bPLATE_wD4_s9_cRGB.png	\N	BLUE
11840	wD4_d3-3_cDAPI	\N	\N
11841	original/PLATE-1A/bPLATE_wD4_s9_cRGB.png	\N	GREEN
11842	wD4_d3-3_cGFP	\N	\N
11843	original/PLATE-1A/bPLATE_wD4_s9_cRGB.png	\N	RED
11844	wD4_d3-3_cCy5	\N	\N
11845	original/PLATE-1A/bPLATE_wE5_s8_cRGB.png	\N	BLUE
11846	wE5_d2-3_cDAPI	\N	\N
11847	original/PLATE-1A/bPLATE_wE5_s8_cRGB.png	\N	GREEN
11848	wE5_d2-3_cGFP	\N	\N
11849	original/PLATE-1A/bPLATE_wE5_s8_cRGB.png	\N	RED
11850	wE5_d2-3_cCy5	\N	\N
11851	original/PLATE-1A/bPLATE_wF11_s6_cRGB.png	\N	BLUE
11852	wF11_d3-2_cDAPI	\N	\N
11853	original/PLATE-1A/bPLATE_wF11_s6_cRGB.png	\N	GREEN
11854	wF11_d3-2_cGFP	\N	\N
11855	original/PLATE-1A/bPLATE_wF11_s6_cRGB.png	\N	RED
11856	wF11_d3-2_cCy5	\N	\N
11857	original/PLATE-1A/bPLATE_wG12_s5_cRGB.png	\N	BLUE
11858	wG12_d2-2_cDAPI	\N	\N
11859	original/PLATE-1A/bPLATE_wG12_s5_cRGB.png	\N	GREEN
11860	wG12_d2-2_cGFP	\N	\N
11861	original/PLATE-1A/bPLATE_wG12_s5_cRGB.png	\N	RED
11862	wG12_d2-2_cCy5	\N	\N
11863	original/PLATE-1A/bPLATE_wA11_s7_cRGB.png	\N	BLUE
11864	wA11_d1-3_cDAPI	\N	\N
11865	original/PLATE-1A/bPLATE_wA11_s7_cRGB.png	\N	GREEN
11866	wA11_d1-3_cGFP	\N	\N
11867	original/PLATE-1A/bPLATE_wA11_s7_cRGB.png	\N	RED
11868	wA11_d1-3_cCy5	\N	\N
11869	original/PLATE-1A/bPLATE_wD6_s4_cRGB.png	\N	BLUE
11870	wD6_d1-2_cDAPI	\N	\N
11871	original/PLATE-1A/bPLATE_wD6_s4_cRGB.png	\N	GREEN
11872	wD6_d1-2_cGFP	\N	\N
11873	original/PLATE-1A/bPLATE_wD6_s4_cRGB.png	\N	RED
11874	wD6_d1-2_cCy5	\N	\N
11875	original/PLATE-1A/bPLATE_wG1_s1_cRGB.png	\N	BLUE
11876	wG1_d1-1_cDAPI	\N	\N
11877	original/PLATE-1A/bPLATE_wG1_s1_cRGB.png	\N	GREEN
11878	wG1_d1-1_cGFP	\N	\N
11879	original/PLATE-1A/bPLATE_wG1_s1_cRGB.png	\N	RED
11880	wG1_d1-1_cCy5	\N	\N
11881	original/PLATE-1A/bPLATE_wE1_s9_cRGB.png	\N	BLUE
11882	wE1_d3-3_cDAPI	\N	\N
11883	original/PLATE-1A/bPLATE_wE1_s9_cRGB.png	\N	GREEN
11884	wE1_d3-3_cGFP	\N	\N
11885	original/PLATE-1A/bPLATE_wE1_s9_cRGB.png	\N	RED
11886	wE1_d3-3_cCy5	\N	\N
11887	original/PLATE-1A/bPLATE_wF12_s7_cRGB.png	\N	BLUE
11888	wF12_d1-3_cDAPI	\N	\N
11889	original/PLATE-1A/bPLATE_wF12_s7_cRGB.png	\N	GREEN
11890	wF12_d1-3_cGFP	\N	\N
11891	original/PLATE-1A/bPLATE_wF12_s7_cRGB.png	\N	RED
11892	wF12_d1-3_cCy5	\N	\N
11893	original/PLATE-1A/bPLATE_wG8_s6_cRGB.png	\N	BLUE
11894	wG8_d3-2_cDAPI	\N	\N
11895	original/PLATE-1A/bPLATE_wG8_s6_cRGB.png	\N	GREEN
11896	wG8_d3-2_cGFP	\N	\N
11897	original/PLATE-1A/bPLATE_wG8_s6_cRGB.png	\N	RED
11898	wG8_d3-2_cCy5	\N	\N
11899	original/PLATE-1A/bPLATE_wD6_s9_cRGB.png	\N	BLUE
11900	wD6_d3-3_cDAPI	\N	\N
11901	original/PLATE-1A/bPLATE_wD6_s9_cRGB.png	\N	GREEN
11902	wD6_d3-3_cGFP	\N	\N
11903	original/PLATE-1A/bPLATE_wD6_s9_cRGB.png	\N	RED
11904	wD6_d3-3_cCy5	\N	\N
11905	original/PLATE-1A/bPLATE_wG1_s6_cRGB.png	\N	BLUE
11906	wG1_d3-2_cDAPI	\N	\N
11907	original/PLATE-1A/bPLATE_wG1_s6_cRGB.png	\N	GREEN
11908	wG1_d3-2_cGFP	\N	\N
11909	original/PLATE-1A/bPLATE_wG1_s6_cRGB.png	\N	RED
11910	wG1_d3-2_cCy5	\N	\N
11911	original/PLATE-1A/bPLATE_wH3_s2_cRGB.png	\N	BLUE
11912	wH3_d2-1_cDAPI	\N	\N
11913	original/PLATE-1A/bPLATE_wH3_s2_cRGB.png	\N	GREEN
11914	wH3_d2-1_cGFP	\N	\N
11915	original/PLATE-1A/bPLATE_wH3_s2_cRGB.png	\N	RED
11916	wH3_d2-1_cCy5	\N	\N
11917	original/PLATE-1A/bPLATE_wC1_s8_cRGB.png	\N	BLUE
11918	wC1_d2-3_cDAPI	\N	\N
11919	original/PLATE-1A/bPLATE_wC1_s8_cRGB.png	\N	GREEN
11920	wC1_d2-3_cGFP	\N	\N
11921	original/PLATE-1A/bPLATE_wC1_s8_cRGB.png	\N	RED
11922	wC1_d2-3_cCy5	\N	\N
11923	original/PLATE-1A/bPLATE_wE8_s5_cRGB.png	\N	BLUE
11924	wE8_d2-2_cDAPI	\N	\N
11925	original/PLATE-1A/bPLATE_wE8_s5_cRGB.png	\N	GREEN
11926	wE8_d2-2_cGFP	\N	\N
11927	original/PLATE-1A/bPLATE_wE8_s5_cRGB.png	\N	RED
11928	wE8_d2-2_cCy5	\N	\N
11929	original/PLATE-1A/bPLATE_wD12_s8_cRGB.png	\N	BLUE
11930	wD12_d2-3_cDAPI	\N	\N
11931	original/PLATE-1A/bPLATE_wD12_s8_cRGB.png	\N	GREEN
11932	wD12_d2-3_cGFP	\N	\N
11933	original/PLATE-1A/bPLATE_wD12_s8_cRGB.png	\N	RED
11934	wD12_d2-3_cCy5	\N	\N
11935	original/PLATE-1A/bPLATE_wG7_s5_cRGB.png	\N	BLUE
11936	wG7_d2-2_cDAPI	\N	\N
11937	original/PLATE-1A/bPLATE_wG7_s5_cRGB.png	\N	GREEN
11938	wG7_d2-2_cGFP	\N	\N
11939	original/PLATE-1A/bPLATE_wG7_s5_cRGB.png	\N	RED
11940	wG7_d2-2_cCy5	\N	\N
11941	original/PLATE-1A/bPLATE_wG1_s7_cRGB.png	\N	BLUE
11942	wG1_d1-3_cDAPI	\N	\N
11943	original/PLATE-1A/bPLATE_wG1_s7_cRGB.png	\N	GREEN
11944	wG1_d1-3_cGFP	\N	\N
11945	original/PLATE-1A/bPLATE_wG1_s7_cRGB.png	\N	RED
11946	wG1_d1-3_cCy5	\N	\N
11947	original/PLATE-1A/bPLATE_wA11_s2_cRGB.png	\N	BLUE
11948	wA11_d2-1_cDAPI	\N	\N
11949	original/PLATE-1A/bPLATE_wA11_s2_cRGB.png	\N	GREEN
11950	wA11_d2-1_cGFP	\N	\N
11951	original/PLATE-1A/bPLATE_wA11_s2_cRGB.png	\N	RED
11952	wA11_d2-1_cCy5	\N	\N
11953	original/PLATE-1A/bPLATE_wC8_s4_cRGB.png	\N	BLUE
11954	wC8_d1-2_cDAPI	\N	\N
11955	original/PLATE-1A/bPLATE_wC8_s4_cRGB.png	\N	GREEN
11956	wC8_d1-2_cGFP	\N	\N
11957	original/PLATE-1A/bPLATE_wC8_s4_cRGB.png	\N	RED
11958	wC8_d1-2_cCy5	\N	\N
11959	original/PLATE-1A/bPLATE_wA1_s7_cRGB.png	\N	BLUE
11960	wA1_d1-3_cDAPI	\N	\N
11961	original/PLATE-1A/bPLATE_wA1_s7_cRGB.png	\N	GREEN
11962	wA1_d1-3_cGFP	\N	\N
11963	original/PLATE-1A/bPLATE_wA1_s7_cRGB.png	\N	RED
11964	wA1_d1-3_cCy5	\N	\N
11965	original/PLATE-1A/bPLATE_wF3_s1_cRGB.png	\N	BLUE
11966	wF3_d1-1_cDAPI	\N	\N
11967	original/PLATE-1A/bPLATE_wF3_s1_cRGB.png	\N	GREEN
11968	wF3_d1-1_cGFP	\N	\N
11969	original/PLATE-1A/bPLATE_wF3_s1_cRGB.png	\N	RED
11970	wF3_d1-1_cCy5	\N	\N
11971	original/PLATE-1A/bPLATE_wB2_s4_cRGB.png	\N	BLUE
11972	wB2_d1-2_cDAPI	\N	\N
11973	original/PLATE-1A/bPLATE_wB2_s4_cRGB.png	\N	GREEN
11974	wB2_d1-2_cGFP	\N	\N
11975	original/PLATE-1A/bPLATE_wB2_s4_cRGB.png	\N	RED
11976	wB2_d1-2_cCy5	\N	\N
11977	original/PLATE-1A/bPLATE_wD9_s1_cRGB.png	\N	BLUE
11978	wD9_d1-1_cDAPI	\N	\N
11979	original/PLATE-1A/bPLATE_wD9_s1_cRGB.png	\N	GREEN
11980	wD9_d1-1_cGFP	\N	\N
11981	original/PLATE-1A/bPLATE_wD9_s1_cRGB.png	\N	RED
11982	wD9_d1-1_cCy5	\N	\N
11983	original/PLATE-1A/bPLATE_wD11_s5_cRGB.png	\N	BLUE
11984	wD11_d2-2_cDAPI	\N	\N
11985	original/PLATE-1A/bPLATE_wD11_s5_cRGB.png	\N	GREEN
11986	wD11_d2-2_cGFP	\N	\N
11987	original/PLATE-1A/bPLATE_wD11_s5_cRGB.png	\N	RED
11988	wD11_d2-2_cCy5	\N	\N
11989	original/PLATE-1A/bPLATE_wB4_s8_cRGB.png	\N	BLUE
11990	wB4_d2-3_cDAPI	\N	\N
11991	original/PLATE-1A/bPLATE_wB4_s8_cRGB.png	\N	GREEN
11992	wB4_d2-3_cGFP	\N	\N
11993	original/PLATE-1A/bPLATE_wB4_s8_cRGB.png	\N	RED
11994	wB4_d2-3_cCy5	\N	\N
11995	original/PLATE-1A/bPLATE_wG6_s2_cRGB.png	\N	BLUE
11996	wG6_d2-1_cDAPI	\N	\N
11997	original/PLATE-1A/bPLATE_wG6_s2_cRGB.png	\N	GREEN
11998	wG6_d2-1_cGFP	\N	\N
11999	original/PLATE-1A/bPLATE_wG6_s2_cRGB.png	\N	RED
12000	wG6_d2-1_cCy5	\N	\N
12001	original/PLATE-1A/bPLATE_wE5_s7_cRGB.png	\N	BLUE
12002	wE5_d1-3_cDAPI	\N	\N
12003	original/PLATE-1A/bPLATE_wE5_s7_cRGB.png	\N	GREEN
12004	wE5_d1-3_cGFP	\N	\N
12005	original/PLATE-1A/bPLATE_wE5_s7_cRGB.png	\N	RED
12006	wE5_d1-3_cCy5	\N	\N
12007	original/PLATE-1A/bPLATE_wG12_s4_cRGB.png	\N	BLUE
12008	wG12_d1-2_cDAPI	\N	\N
12009	original/PLATE-1A/bPLATE_wG12_s4_cRGB.png	\N	GREEN
12010	wG12_d1-2_cGFP	\N	\N
12011	original/PLATE-1A/bPLATE_wG12_s4_cRGB.png	\N	RED
12012	wG12_d1-2_cCy5	\N	\N
12013	original/PLATE-1A/bPLATE_wH6_s4_cRGB.png	\N	BLUE
12014	wH6_d1-2_cDAPI	\N	\N
12015	original/PLATE-1A/bPLATE_wH6_s4_cRGB.png	\N	GREEN
12016	wH6_d1-2_cGFP	\N	\N
12017	original/PLATE-1A/bPLATE_wH6_s4_cRGB.png	\N	RED
12018	wH6_d1-2_cCy5	\N	\N
12019	original/PLATE-1A/bPLATE_wB11_s2_cRGB.png	\N	BLUE
12020	wB11_d2-1_cDAPI	\N	\N
12021	original/PLATE-1A/bPLATE_wB11_s2_cRGB.png	\N	GREEN
12022	wB11_d2-1_cGFP	\N	\N
12023	original/PLATE-1A/bPLATE_wB11_s2_cRGB.png	\N	RED
12024	wB11_d2-1_cCy5	\N	\N
12025	original/PLATE-1A/bPLATE_wE11_s7_cRGB.png	\N	BLUE
12026	wE11_d1-3_cDAPI	\N	\N
12027	original/PLATE-1A/bPLATE_wE11_s7_cRGB.png	\N	GREEN
12028	wE11_d1-3_cGFP	\N	\N
12029	original/PLATE-1A/bPLATE_wE11_s7_cRGB.png	\N	RED
12030	wE11_d1-3_cCy5	\N	\N
12031	original/PLATE-1A/bPLATE_wH5_s1_cRGB.png	\N	BLUE
12032	wH5_d1-1_cDAPI	\N	\N
12033	original/PLATE-1A/bPLATE_wH5_s1_cRGB.png	\N	GREEN
12034	wH5_d1-1_cGFP	\N	\N
12035	original/PLATE-1A/bPLATE_wH5_s1_cRGB.png	\N	RED
12036	wH5_d1-1_cCy5	\N	\N
12037	original/PLATE-1A/bPLATE_wC3_s7_cRGB.png	\N	BLUE
12038	wC3_d1-3_cDAPI	\N	\N
12039	original/PLATE-1A/bPLATE_wC3_s7_cRGB.png	\N	GREEN
12040	wC3_d1-3_cGFP	\N	\N
12041	original/PLATE-1A/bPLATE_wC3_s7_cRGB.png	\N	RED
12042	wC3_d1-3_cCy5	\N	\N
12043	original/PLATE-1A/bPLATE_wE10_s4_cRGB.png	\N	BLUE
12044	wE10_d1-2_cDAPI	\N	\N
12045	original/PLATE-1A/bPLATE_wE10_s4_cRGB.png	\N	GREEN
12046	wE10_d1-2_cGFP	\N	\N
12047	original/PLATE-1A/bPLATE_wE10_s4_cRGB.png	\N	RED
12048	wE10_d1-2_cCy5	\N	\N
12049	original/PLATE-1A/bPLATE_wH4_s3_cRGB.png	\N	BLUE
12050	wH4_d3-1_cDAPI	\N	\N
12051	original/PLATE-1A/bPLATE_wH4_s3_cRGB.png	\N	GREEN
12052	wH4_d3-1_cGFP	\N	\N
12053	original/PLATE-1A/bPLATE_wH4_s3_cRGB.png	\N	RED
12054	wH4_d3-1_cCy5	\N	\N
12055	original/PLATE-1A/bPLATE_wC2_s9_cRGB.png	\N	BLUE
12056	wC2_d3-3_cDAPI	\N	\N
12057	original/PLATE-1A/bPLATE_wC2_s9_cRGB.png	\N	GREEN
12058	wC2_d3-3_cGFP	\N	\N
12059	original/PLATE-1A/bPLATE_wC2_s9_cRGB.png	\N	RED
12060	wC2_d3-3_cCy5	\N	\N
12061	original/PLATE-1A/bPLATE_wE9_s6_cRGB.png	\N	BLUE
12062	wE9_d3-2_cDAPI	\N	\N
12063	original/PLATE-1A/bPLATE_wE9_s6_cRGB.png	\N	GREEN
12064	wE9_d3-2_cGFP	\N	\N
12065	original/PLATE-1A/bPLATE_wE9_s6_cRGB.png	\N	RED
12066	wE9_d3-2_cCy5	\N	\N
12067	original/PLATE-1A/bPLATE_wD10_s9_cRGB.png	\N	BLUE
12068	wD10_d3-3_cDAPI	\N	\N
12069	original/PLATE-1A/bPLATE_wD10_s9_cRGB.png	\N	GREEN
12070	wD10_d3-3_cGFP	\N	\N
12071	original/PLATE-1A/bPLATE_wD10_s9_cRGB.png	\N	RED
12072	wD10_d3-3_cCy5	\N	\N
12073	original/PLATE-1A/bPLATE_wG5_s6_cRGB.png	\N	BLUE
12074	wG5_d3-2_cDAPI	\N	\N
12075	original/PLATE-1A/bPLATE_wG5_s6_cRGB.png	\N	GREEN
12076	wG5_d3-2_cGFP	\N	\N
12077	original/PLATE-1A/bPLATE_wG5_s6_cRGB.png	\N	RED
12078	wG5_d3-2_cCy5	\N	\N
12079	original/PLATE-1A/bPLATE_wH5_s7_cRGB.png	\N	BLUE
12080	wH5_d1-3_cDAPI	\N	\N
12081	original/PLATE-1A/bPLATE_wH5_s7_cRGB.png	\N	GREEN
12082	wH5_d1-3_cGFP	\N	\N
12083	original/PLATE-1A/bPLATE_wH5_s7_cRGB.png	\N	RED
12084	wH5_d1-3_cCy5	\N	\N
12085	original/PLATE-1A/bPLATE_wG3_s9_cRGB.png	\N	BLUE
12086	wG3_d3-3_cDAPI	\N	\N
12087	original/PLATE-1A/bPLATE_wG3_s9_cRGB.png	\N	GREEN
12088	wG3_d3-3_cGFP	\N	\N
12089	original/PLATE-1A/bPLATE_wG3_s9_cRGB.png	\N	RED
12090	wG3_d3-3_cCy5	\N	\N
12091	original/PLATE-1A/bPLATE_wD4_s5_cRGB.png	\N	BLUE
12092	wD4_d2-2_cDAPI	\N	\N
12093	original/PLATE-1A/bPLATE_wD4_s5_cRGB.png	\N	GREEN
12094	wD4_d2-2_cGFP	\N	\N
12095	original/PLATE-1A/bPLATE_wD4_s5_cRGB.png	\N	RED
12096	wD4_d2-2_cCy5	\N	\N
12097	original/PLATE-1A/bPLATE_wA9_s8_cRGB.png	\N	BLUE
12098	wA9_d2-3_cDAPI	\N	\N
12099	original/PLATE-1A/bPLATE_wA9_s8_cRGB.png	\N	GREEN
12100	wA9_d2-3_cGFP	\N	\N
12101	original/PLATE-1A/bPLATE_wA9_s8_cRGB.png	\N	RED
12102	wA9_d2-3_cCy5	\N	\N
12103	original/PLATE-1A/bPLATE_wF11_s2_cRGB.png	\N	BLUE
12104	wF11_d2-1_cDAPI	\N	\N
12105	original/PLATE-1A/bPLATE_wF11_s2_cRGB.png	\N	GREEN
12106	wF11_d2-1_cGFP	\N	\N
12107	original/PLATE-1A/bPLATE_wF11_s2_cRGB.png	\N	RED
12108	wF11_d2-1_cCy5	\N	\N
12109	original/PLATE-1A/bPLATE_wG5_s8_cRGB.png	\N	BLUE
12110	wG5_d2-3_cDAPI	\N	\N
12111	original/PLATE-1A/bPLATE_wG5_s8_cRGB.png	\N	GREEN
12112	wG5_d2-3_cGFP	\N	\N
12113	original/PLATE-1A/bPLATE_wG5_s8_cRGB.png	\N	RED
12114	wG5_d2-3_cCy5	\N	\N
12115	original/PLATE-1A/bPLATE_wC3_s3_cRGB.png	\N	BLUE
12116	wC3_d3-1_cDAPI	\N	\N
12117	original/PLATE-1A/bPLATE_wC3_s3_cRGB.png	\N	GREEN
12118	wC3_d3-1_cGFP	\N	\N
12119	original/PLATE-1A/bPLATE_wC3_s3_cRGB.png	\N	RED
12120	wC3_d3-1_cCy5	\N	\N
12121	original/PLATE-1A/bPLATE_wB3_s2_cRGB.png	\N	BLUE
12122	wB3_d2-1_cDAPI	\N	\N
12123	original/PLATE-1A/bPLATE_wB3_s2_cRGB.png	\N	GREEN
12124	wB3_d2-1_cGFP	\N	\N
12125	original/PLATE-1A/bPLATE_wB3_s2_cRGB.png	\N	RED
12126	wB3_d2-1_cCy5	\N	\N
12127	original/PLATE-1A/bPLATE_wB7_s2_cRGB.png	\N	BLUE
12128	wB7_d2-1_cDAPI	\N	\N
12129	original/PLATE-1A/bPLATE_wB7_s2_cRGB.png	\N	GREEN
12130	wB7_d2-1_cGFP	\N	\N
12131	original/PLATE-1A/bPLATE_wB7_s2_cRGB.png	\N	RED
12132	wB7_d2-1_cCy5	\N	\N
12133	original/PLATE-1A/bPLATE_wH1_s6_cRGB.png	\N	BLUE
12134	wH1_d3-2_cDAPI	\N	\N
12135	original/PLATE-1A/bPLATE_wH1_s6_cRGB.png	\N	GREEN
12136	wH1_d3-2_cGFP	\N	\N
12137	original/PLATE-1A/bPLATE_wH1_s6_cRGB.png	\N	RED
12138	wH1_d3-2_cCy5	\N	\N
12139	original/PLATE-1A/bPLATE_wE6_s9_cRGB.png	\N	BLUE
12140	wE6_d3-3_cDAPI	\N	\N
12141	original/PLATE-1A/bPLATE_wE6_s9_cRGB.png	\N	GREEN
12142	wE6_d3-3_cGFP	\N	\N
12143	original/PLATE-1A/bPLATE_wE6_s9_cRGB.png	\N	RED
12144	wE6_d3-3_cCy5	\N	\N
12145	original/PLATE-1A/bPLATE_wC10_s4_cRGB.png	\N	BLUE
12146	wC10_d1-2_cDAPI	\N	\N
12147	original/PLATE-1A/bPLATE_wC10_s4_cRGB.png	\N	GREEN
12148	wC10_d1-2_cGFP	\N	\N
12149	original/PLATE-1A/bPLATE_wC10_s4_cRGB.png	\N	RED
12150	wC10_d1-2_cCy5	\N	\N
12151	original/PLATE-1A/bPLATE_wA3_s7_cRGB.png	\N	BLUE
12152	wA3_d1-3_cDAPI	\N	\N
12153	original/PLATE-1A/bPLATE_wA3_s7_cRGB.png	\N	GREEN
12154	wA3_d1-3_cGFP	\N	\N
12155	original/PLATE-1A/bPLATE_wA3_s7_cRGB.png	\N	RED
12156	wA3_d1-3_cCy5	\N	\N
12157	original/PLATE-1A/bPLATE_wF5_s1_cRGB.png	\N	BLUE
12158	wF5_d1-1_cDAPI	\N	\N
12159	original/PLATE-1A/bPLATE_wF5_s1_cRGB.png	\N	GREEN
12160	wF5_d1-1_cGFP	\N	\N
12161	original/PLATE-1A/bPLATE_wF5_s1_cRGB.png	\N	RED
12162	wF5_d1-1_cCy5	\N	\N
12163	original/PLATE-1A/bPLATE_wH8_s5_cRGB.png	\N	BLUE
12164	wH8_d2-2_cDAPI	\N	\N
12165	original/PLATE-1A/bPLATE_wH8_s5_cRGB.png	\N	GREEN
12166	wH8_d2-2_cGFP	\N	\N
12167	original/PLATE-1A/bPLATE_wH8_s5_cRGB.png	\N	RED
12168	wH8_d2-2_cCy5	\N	\N
12169	original/PLATE-1A/bPLATE_wF1_s8_cRGB.png	\N	BLUE
12170	wF1_d2-3_cDAPI	\N	\N
12171	original/PLATE-1A/bPLATE_wF1_s8_cRGB.png	\N	GREEN
12172	wF1_d2-3_cGFP	\N	\N
12173	original/PLATE-1A/bPLATE_wF1_s8_cRGB.png	\N	RED
12174	wF1_d2-3_cCy5	\N	\N
12175	original/PLATE-1A/bPLATE_wD7_s7_cRGB.png	\N	BLUE
12176	wD7_d1-3_cDAPI	\N	\N
12177	original/PLATE-1A/bPLATE_wD7_s7_cRGB.png	\N	GREEN
12178	wD7_d1-3_cGFP	\N	\N
12179	original/PLATE-1A/bPLATE_wD7_s7_cRGB.png	\N	RED
12180	wD7_d1-3_cCy5	\N	\N
12181	original/PLATE-1A/bPLATE_wG2_s4_cRGB.png	\N	BLUE
12182	wG2_d1-2_cDAPI	\N	\N
12183	original/PLATE-1A/bPLATE_wG2_s4_cRGB.png	\N	GREEN
12184	wG2_d1-2_cGFP	\N	\N
12185	original/PLATE-1A/bPLATE_wG2_s4_cRGB.png	\N	RED
12186	wG2_d1-2_cCy5	\N	\N
12187	original/PLATE-1A/bPLATE_wF12_s9_cRGB.png	\N	BLUE
12188	wF12_d3-3_cDAPI	\N	\N
12189	original/PLATE-1A/bPLATE_wF12_s9_cRGB.png	\N	GREEN
12190	wF12_d3-3_cGFP	\N	\N
12191	original/PLATE-1A/bPLATE_wF12_s9_cRGB.png	\N	RED
12192	wF12_d3-3_cCy5	\N	\N
12193	original/PLATE-1A/bPLATE_wH8_s3_cRGB.png	\N	BLUE
12194	wH8_d3-1_cDAPI	\N	\N
12195	original/PLATE-1A/bPLATE_wH8_s3_cRGB.png	\N	GREEN
12196	wH8_d3-1_cGFP	\N	\N
12197	original/PLATE-1A/bPLATE_wH8_s3_cRGB.png	\N	RED
12198	wH8_d3-1_cCy5	\N	\N
12199	original/PLATE-1A/bPLATE_wC6_s9_cRGB.png	\N	BLUE
12200	wC6_d3-3_cDAPI	\N	\N
12201	original/PLATE-1A/bPLATE_wC6_s9_cRGB.png	\N	GREEN
12202	wC6_d3-3_cGFP	\N	\N
12203	original/PLATE-1A/bPLATE_wC6_s9_cRGB.png	\N	RED
12204	wC6_d3-3_cCy5	\N	\N
12205	original/PLATE-1A/bPLATE_wF1_s6_cRGB.png	\N	BLUE
12206	wF1_d3-2_cDAPI	\N	\N
12207	original/PLATE-1A/bPLATE_wF1_s6_cRGB.png	\N	GREEN
12208	wF1_d3-2_cGFP	\N	\N
12209	original/PLATE-1A/bPLATE_wF1_s6_cRGB.png	\N	RED
12210	wF1_d3-2_cCy5	\N	\N
12211	original/PLATE-1A/bPLATE_wC8_s6_cRGB.png	\N	BLUE
12212	wC8_d3-2_cDAPI	\N	\N
12213	original/PLATE-1A/bPLATE_wC8_s6_cRGB.png	\N	GREEN
12214	wC8_d3-2_cGFP	\N	\N
12215	original/PLATE-1A/bPLATE_wC8_s6_cRGB.png	\N	RED
12216	wC8_d3-2_cCy5	\N	\N
12217	original/PLATE-1A/bPLATE_wC10_s6_cRGB.png	\N	BLUE
12218	wC10_d3-2_cDAPI	\N	\N
12219	original/PLATE-1A/bPLATE_wC10_s6_cRGB.png	\N	GREEN
12220	wC10_d3-2_cGFP	\N	\N
12221	original/PLATE-1A/bPLATE_wC10_s6_cRGB.png	\N	RED
12222	wC10_d3-2_cCy5	\N	\N
12223	original/PLATE-1A/bPLATE_wA3_s9_cRGB.png	\N	BLUE
12224	wA3_d3-3_cDAPI	\N	\N
12225	original/PLATE-1A/bPLATE_wA3_s9_cRGB.png	\N	GREEN
12226	wA3_d3-3_cGFP	\N	\N
12227	original/PLATE-1A/bPLATE_wA3_s9_cRGB.png	\N	RED
12228	wA3_d3-3_cCy5	\N	\N
12229	original/PLATE-1A/bPLATE_wA1_s9_cRGB.png	\N	BLUE
12230	wA1_d3-3_cDAPI	\N	\N
12231	original/PLATE-1A/bPLATE_wA1_s9_cRGB.png	\N	GREEN
12232	wA1_d3-3_cGFP	\N	\N
12233	original/PLATE-1A/bPLATE_wA1_s9_cRGB.png	\N	RED
12234	wA1_d3-3_cCy5	\N	\N
12235	original/PLATE-1A/bPLATE_wF3_s3_cRGB.png	\N	BLUE
12236	wF3_d3-1_cDAPI	\N	\N
12237	original/PLATE-1A/bPLATE_wF3_s3_cRGB.png	\N	GREEN
12238	wF3_d3-1_cGFP	\N	\N
12239	original/PLATE-1A/bPLATE_wF3_s3_cRGB.png	\N	RED
12240	wF3_d3-1_cCy5	\N	\N
12241	original/PLATE-1A/bPLATE_wF5_s3_cRGB.png	\N	BLUE
12242	wF5_d3-1_cDAPI	\N	\N
12243	original/PLATE-1A/bPLATE_wF5_s3_cRGB.png	\N	GREEN
12244	wF5_d3-1_cGFP	\N	\N
12245	original/PLATE-1A/bPLATE_wF5_s3_cRGB.png	\N	RED
12246	wF5_d3-1_cCy5	\N	\N
12247	original/PLATE-1A/bPLATE_wF9_s8_cRGB.png	\N	BLUE
12248	wF9_d2-3_cDAPI	\N	\N
12249	original/PLATE-1A/bPLATE_wF9_s8_cRGB.png	\N	GREEN
12250	wF9_d2-3_cGFP	\N	\N
12251	original/PLATE-1A/bPLATE_wF9_s8_cRGB.png	\N	RED
12252	wF9_d2-3_cCy5	\N	\N
12253	original/PLATE-1A/bPLATE_wC6_s5_cRGB.png	\N	BLUE
12254	wC6_d2-2_cDAPI	\N	\N
12255	original/PLATE-1A/bPLATE_wC6_s5_cRGB.png	\N	GREEN
12256	wC6_d2-2_cGFP	\N	\N
12257	original/PLATE-1A/bPLATE_wC6_s5_cRGB.png	\N	RED
12258	wC6_d2-2_cCy5	\N	\N
12259	original/PLATE-1A/bPLATE_wF1_s2_cRGB.png	\N	BLUE
12260	wF1_d2-1_cDAPI	\N	\N
12261	original/PLATE-1A/bPLATE_wF1_s2_cRGB.png	\N	GREEN
12262	wF1_d2-1_cGFP	\N	\N
12263	original/PLATE-1A/bPLATE_wF1_s2_cRGB.png	\N	RED
12264	wF1_d2-1_cCy5	\N	\N
12265	original/PLATE-1A/bPLATE_wD12_s1_cRGB.png	\N	BLUE
12266	wD12_d1-1_cDAPI	\N	\N
12267	original/PLATE-1A/bPLATE_wD12_s1_cRGB.png	\N	GREEN
12268	wD12_d1-1_cGFP	\N	\N
12269	original/PLATE-1A/bPLATE_wD12_s1_cRGB.png	\N	RED
12270	wD12_d1-1_cCy5	\N	\N
12271	original/PLATE-1A/bPLATE_wB5_s4_cRGB.png	\N	BLUE
12272	wB5_d1-2_cDAPI	\N	\N
12273	original/PLATE-1A/bPLATE_wB5_s4_cRGB.png	\N	GREEN
12274	wB5_d1-2_cGFP	\N	\N
12275	original/PLATE-1A/bPLATE_wB5_s4_cRGB.png	\N	RED
12276	wB5_d1-2_cCy5	\N	\N
12277	original/PLATE-1A/bPLATE_wF10_s9_cRGB.png	\N	BLUE
12278	wF10_d3-3_cDAPI	\N	\N
12279	original/PLATE-1A/bPLATE_wF10_s9_cRGB.png	\N	GREEN
12280	wF10_d3-3_cGFP	\N	\N
12281	original/PLATE-1A/bPLATE_wF10_s9_cRGB.png	\N	RED
12282	wF10_d3-3_cCy5	\N	\N
12283	original/PLATE-1A/bPLATE_wD11_s3_cRGB.png	\N	BLUE
12284	wD11_d3-1_cDAPI	\N	\N
12285	original/PLATE-1A/bPLATE_wD11_s3_cRGB.png	\N	GREEN
12286	wD11_d3-1_cGFP	\N	\N
12287	original/PLATE-1A/bPLATE_wD11_s3_cRGB.png	\N	RED
12288	wD11_d3-1_cCy5	\N	\N
12289	original/PLATE-1A/bPLATE_wB4_s6_cRGB.png	\N	BLUE
12290	wB4_d3-2_cDAPI	\N	\N
12291	original/PLATE-1A/bPLATE_wB4_s6_cRGB.png	\N	GREEN
12292	wB4_d3-2_cGFP	\N	\N
12293	original/PLATE-1A/bPLATE_wB4_s6_cRGB.png	\N	RED
12294	wB4_d3-2_cCy5	\N	\N
12295	original/PLATE-1A/bPLATE_wB1_s7_cRGB.png	\N	BLUE
12296	wB1_d1-3_cDAPI	\N	\N
12297	original/PLATE-1A/bPLATE_wB1_s7_cRGB.png	\N	GREEN
12298	wB1_d1-3_cGFP	\N	\N
12299	original/PLATE-1A/bPLATE_wB1_s7_cRGB.png	\N	RED
12300	wB1_d1-3_cCy5	\N	\N
12301	original/PLATE-1A/bPLATE_wD8_s4_cRGB.png	\N	BLUE
12302	wD8_d1-2_cDAPI	\N	\N
12303	original/PLATE-1A/bPLATE_wD8_s4_cRGB.png	\N	GREEN
12304	wD8_d1-2_cGFP	\N	\N
12305	original/PLATE-1A/bPLATE_wD8_s4_cRGB.png	\N	RED
12306	wD8_d1-2_cCy5	\N	\N
12307	original/PLATE-1A/bPLATE_wG3_s1_cRGB.png	\N	BLUE
12308	wG3_d1-1_cDAPI	\N	\N
12309	original/PLATE-1A/bPLATE_wG3_s1_cRGB.png	\N	GREEN
12310	wG3_d1-1_cGFP	\N	\N
12311	original/PLATE-1A/bPLATE_wG3_s1_cRGB.png	\N	RED
12312	wG3_d1-1_cCy5	\N	\N
12313	original/PLATE-1A/bPLATE_wB10_s6_cRGB.png	\N	BLUE
12314	wB10_d3-2_cDAPI	\N	\N
12315	original/PLATE-1A/bPLATE_wB10_s6_cRGB.png	\N	GREEN
12316	wB10_d3-2_cGFP	\N	\N
12317	original/PLATE-1A/bPLATE_wB10_s6_cRGB.png	\N	RED
12318	wB10_d3-2_cCy5	\N	\N
12319	original/PLATE-1A/bPLATE_wE5_s3_cRGB.png	\N	BLUE
12320	wE5_d3-1_cDAPI	\N	\N
12321	original/PLATE-1A/bPLATE_wE5_s3_cRGB.png	\N	GREEN
12322	wE5_d3-1_cGFP	\N	\N
12323	original/PLATE-1A/bPLATE_wE5_s3_cRGB.png	\N	RED
12324	wE5_d3-1_cCy5	\N	\N
12325	original/PLATE-1A/bPLATE_wH7_s9_cRGB.png	\N	BLUE
12326	wH7_d3-3_cDAPI	\N	\N
12327	original/PLATE-1A/bPLATE_wH7_s9_cRGB.png	\N	GREEN
12328	wH7_d3-3_cGFP	\N	\N
12329	original/PLATE-1A/bPLATE_wH7_s9_cRGB.png	\N	RED
12330	wH7_d3-3_cCy5	\N	\N
12331	original/PLATE-1A/bPLATE_wB8_s6_cRGB.png	\N	BLUE
12332	wB8_d3-2_cDAPI	\N	\N
12333	original/PLATE-1A/bPLATE_wB8_s6_cRGB.png	\N	GREEN
12334	wB8_d3-2_cGFP	\N	\N
12335	original/PLATE-1A/bPLATE_wB8_s6_cRGB.png	\N	RED
12336	wB8_d3-2_cCy5	\N	\N
12337	original/PLATE-1A/bPLATE_wE3_s3_cRGB.png	\N	BLUE
12338	wE3_d3-1_cDAPI	\N	\N
12339	original/PLATE-1A/bPLATE_wE3_s3_cRGB.png	\N	GREEN
12340	wE3_d3-1_cGFP	\N	\N
12341	original/PLATE-1A/bPLATE_wE3_s3_cRGB.png	\N	RED
12342	wE3_d3-1_cCy5	\N	\N
12343	original/PLATE-1A/bPLATE_wB8_s2_cRGB.png	\N	BLUE
12344	wB8_d2-1_cDAPI	\N	\N
12345	original/PLATE-1A/bPLATE_wB8_s2_cRGB.png	\N	GREEN
12346	wB8_d2-1_cGFP	\N	\N
12347	original/PLATE-1A/bPLATE_wB8_s2_cRGB.png	\N	RED
12348	wB8_d2-1_cCy5	\N	\N
12349	original/PLATE-1A/bPLATE_wG6_s9_cRGB.png	\N	BLUE
12350	wG6_d3-3_cDAPI	\N	\N
12351	original/PLATE-1A/bPLATE_wG6_s9_cRGB.png	\N	GREEN
12352	wG6_d3-3_cGFP	\N	\N
12353	original/PLATE-1A/bPLATE_wG6_s9_cRGB.png	\N	RED
12354	wG6_d3-3_cCy5	\N	\N
12355	original/PLATE-1A/bPLATE_wH4_s7_cRGB.png	\N	BLUE
12356	wH4_d1-3_cDAPI	\N	\N
12357	original/PLATE-1A/bPLATE_wH4_s7_cRGB.png	\N	GREEN
12358	wH4_d1-3_cGFP	\N	\N
12359	original/PLATE-1A/bPLATE_wH4_s7_cRGB.png	\N	RED
12360	wH4_d1-3_cCy5	\N	\N
12361	original/PLATE-1A/bPLATE_wH6_s9_cRGB.png	\N	BLUE
12362	wH6_d3-3_cDAPI	\N	\N
12363	original/PLATE-1A/bPLATE_wH6_s9_cRGB.png	\N	GREEN
12364	wH6_d3-3_cGFP	\N	\N
12365	original/PLATE-1A/bPLATE_wH6_s9_cRGB.png	\N	RED
12366	wH6_d3-3_cCy5	\N	\N
12367	original/PLATE-1A/bPLATE_wD1_s9_cRGB.png	\N	BLUE
12368	wD1_d3-3_cDAPI	\N	\N
12369	original/PLATE-1A/bPLATE_wD1_s9_cRGB.png	\N	GREEN
12370	wD1_d3-3_cGFP	\N	\N
12371	original/PLATE-1A/bPLATE_wD1_s9_cRGB.png	\N	RED
12372	wD1_d3-3_cCy5	\N	\N
12373	original/PLATE-1A/bPLATE_wF8_s6_cRGB.png	\N	BLUE
12374	wF8_d3-2_cDAPI	\N	\N
12375	original/PLATE-1A/bPLATE_wF8_s6_cRGB.png	\N	GREEN
12376	wF8_d3-2_cGFP	\N	\N
12377	original/PLATE-1A/bPLATE_wF8_s6_cRGB.png	\N	RED
12378	wF8_d3-2_cCy5	\N	\N
12379	original/PLATE-1A/bPLATE_wB11_s3_cRGB.png	\N	BLUE
12380	wB11_d3-1_cDAPI	\N	\N
12381	original/PLATE-1A/bPLATE_wB11_s3_cRGB.png	\N	GREEN
12382	wB11_d3-1_cGFP	\N	\N
12383	original/PLATE-1A/bPLATE_wB11_s3_cRGB.png	\N	RED
12384	wB11_d3-1_cCy5	\N	\N
12385	original/PLATE-1A/bPLATE_wC11_s3_cRGB.png	\N	BLUE
12386	wC11_d3-1_cDAPI	\N	\N
12387	original/PLATE-1A/bPLATE_wC11_s3_cRGB.png	\N	GREEN
12388	wC11_d3-1_cGFP	\N	\N
12389	original/PLATE-1A/bPLATE_wC11_s3_cRGB.png	\N	RED
12390	wC11_d3-1_cCy5	\N	\N
12391	original/PLATE-1A/bPLATE_wA4_s6_cRGB.png	\N	BLUE
12392	wA4_d3-2_cDAPI	\N	\N
12393	original/PLATE-1A/bPLATE_wA4_s6_cRGB.png	\N	GREEN
12394	wA4_d3-2_cGFP	\N	\N
12395	original/PLATE-1A/bPLATE_wA4_s6_cRGB.png	\N	RED
12396	wA4_d3-2_cCy5	\N	\N
12397	original/PLATE-1A/bPLATE_wC11_s8_cRGB.png	\N	BLUE
12398	wC11_d2-3_cDAPI	\N	\N
12399	original/PLATE-1A/bPLATE_wC11_s8_cRGB.png	\N	GREEN
12400	wC11_d2-3_cGFP	\N	\N
12401	original/PLATE-1A/bPLATE_wC11_s8_cRGB.png	\N	RED
12402	wC11_d2-3_cCy5	\N	\N
12403	original/PLATE-1A/bPLATE_wF6_s5_cRGB.png	\N	BLUE
12404	wF6_d2-2_cDAPI	\N	\N
12405	original/PLATE-1A/bPLATE_wF6_s5_cRGB.png	\N	GREEN
12406	wF6_d2-2_cGFP	\N	\N
12407	original/PLATE-1A/bPLATE_wF6_s5_cRGB.png	\N	RED
12408	wF6_d2-2_cCy5	\N	\N
12409	original/PLATE-1A/bPLATE_wD2_s2_cRGB.png	\N	BLUE
12410	wD2_d2-1_cDAPI	\N	\N
12411	original/PLATE-1A/bPLATE_wD2_s2_cRGB.png	\N	GREEN
12412	wD2_d2-1_cGFP	\N	\N
12413	original/PLATE-1A/bPLATE_wD2_s2_cRGB.png	\N	RED
12414	wD2_d2-1_cCy5	\N	\N
12415	original/PLATE-1A/bPLATE_wA7_s5_cRGB.png	\N	BLUE
12416	wA7_d2-2_cDAPI	\N	\N
12417	original/PLATE-1A/bPLATE_wA7_s5_cRGB.png	\N	GREEN
12418	wA7_d2-2_cGFP	\N	\N
12419	original/PLATE-1A/bPLATE_wA7_s5_cRGB.png	\N	RED
12420	wA7_d2-2_cCy5	\N	\N
12421	original/PLATE-1A/bPLATE_wA11_s1_cRGB.png	\N	BLUE
12422	wA11_d1-1_cDAPI	\N	\N
12423	original/PLATE-1A/bPLATE_wA11_s1_cRGB.png	\N	GREEN
12424	wA11_d1-1_cGFP	\N	\N
12425	original/PLATE-1A/bPLATE_wA11_s1_cRGB.png	\N	RED
12426	wA11_d1-1_cCy5	\N	\N
12427	original/PLATE-1A/bPLATE_wC4_s5_cRGB.png	\N	BLUE
12428	wC4_d2-2_cDAPI	\N	\N
12429	original/PLATE-1A/bPLATE_wC4_s5_cRGB.png	\N	GREEN
12430	wC4_d2-2_cGFP	\N	\N
12431	original/PLATE-1A/bPLATE_wC4_s5_cRGB.png	\N	RED
12432	wC4_d2-2_cCy5	\N	\N
12433	original/PLATE-1A/bPLATE_wC11_s5_cRGB.png	\N	BLUE
12434	wC11_d2-2_cDAPI	\N	\N
12435	original/PLATE-1A/bPLATE_wC11_s5_cRGB.png	\N	GREEN
12436	wC11_d2-2_cGFP	\N	\N
12437	original/PLATE-1A/bPLATE_wC11_s5_cRGB.png	\N	RED
12438	wC11_d2-2_cCy5	\N	\N
12439	original/PLATE-1A/bPLATE_wA4_s8_cRGB.png	\N	BLUE
12440	wA4_d2-3_cDAPI	\N	\N
12441	original/PLATE-1A/bPLATE_wA4_s8_cRGB.png	\N	GREEN
12442	wA4_d2-3_cGFP	\N	\N
12443	original/PLATE-1A/bPLATE_wA4_s8_cRGB.png	\N	RED
12444	wA4_d2-3_cCy5	\N	\N
12445	original/PLATE-1A/bPLATE_wE11_s2_cRGB.png	\N	BLUE
12446	wE11_d2-1_cDAPI	\N	\N
12447	original/PLATE-1A/bPLATE_wE11_s2_cRGB.png	\N	GREEN
12448	wE11_d2-1_cGFP	\N	\N
12449	original/PLATE-1A/bPLATE_wE11_s2_cRGB.png	\N	RED
12450	wE11_d2-1_cCy5	\N	\N
12451	original/PLATE-1A/bPLATE_wF6_s2_cRGB.png	\N	BLUE
12452	wF6_d2-1_cDAPI	\N	\N
12453	original/PLATE-1A/bPLATE_wF6_s2_cRGB.png	\N	GREEN
12454	wF6_d2-1_cGFP	\N	\N
12455	original/PLATE-1A/bPLATE_wF6_s2_cRGB.png	\N	RED
12456	wF6_d2-1_cCy5	\N	\N
12457	original/PLATE-1A/bPLATE_wB7_s3_cRGB.png	\N	BLUE
12458	wB7_d3-1_cDAPI	\N	\N
12459	original/PLATE-1A/bPLATE_wB7_s3_cRGB.png	\N	GREEN
12460	wB7_d3-1_cGFP	\N	\N
12461	original/PLATE-1A/bPLATE_wB7_s3_cRGB.png	\N	RED
12462	wB7_d3-1_cCy5	\N	\N
12463	original/PLATE-1A/bPLATE_wC8_s2_cRGB.png	\N	BLUE
12464	wC8_d2-1_cDAPI	\N	\N
12465	original/PLATE-1A/bPLATE_wC8_s2_cRGB.png	\N	GREEN
12466	wC8_d2-1_cGFP	\N	\N
12467	original/PLATE-1A/bPLATE_wC8_s2_cRGB.png	\N	RED
12468	wC8_d2-1_cCy5	\N	\N
12469	original/PLATE-1A/bPLATE_wA1_s5_cRGB.png	\N	BLUE
12470	wA1_d2-2_cDAPI	\N	\N
12471	original/PLATE-1A/bPLATE_wA1_s5_cRGB.png	\N	GREEN
12472	wA1_d2-2_cGFP	\N	\N
12473	original/PLATE-1A/bPLATE_wA1_s5_cRGB.png	\N	RED
12474	wA1_d2-2_cCy5	\N	\N
12475	original/PLATE-1A/bPLATE_wB7_s6_cRGB.png	\N	BLUE
12476	wB7_d3-2_cDAPI	\N	\N
12477	original/PLATE-1A/bPLATE_wB7_s6_cRGB.png	\N	GREEN
12478	wB7_d3-2_cGFP	\N	\N
12479	original/PLATE-1A/bPLATE_wB7_s6_cRGB.png	\N	RED
12480	wB7_d3-2_cCy5	\N	\N
12481	original/PLATE-1A/bPLATE_wE2_s3_cRGB.png	\N	BLUE
12482	wE2_d3-1_cDAPI	\N	\N
12483	original/PLATE-1A/bPLATE_wE2_s3_cRGB.png	\N	GREEN
12484	wE2_d3-1_cGFP	\N	\N
12485	original/PLATE-1A/bPLATE_wE2_s3_cRGB.png	\N	RED
12486	wE2_d3-1_cCy5	\N	\N
12487	original/PLATE-1A/bPLATE_wG10_s7_cRGB.png	\N	BLUE
12488	wG10_d1-3_cDAPI	\N	\N
12489	original/PLATE-1A/bPLATE_wG10_s7_cRGB.png	\N	GREEN
12490	wG10_d1-3_cGFP	\N	\N
12491	original/PLATE-1A/bPLATE_wG10_s7_cRGB.png	\N	RED
12492	wG10_d1-3_cCy5	\N	\N
12493	original/PLATE-1A/bPLATE_wG2_s9_cRGB.png	\N	BLUE
12494	wG2_d3-3_cDAPI	\N	\N
12495	original/PLATE-1A/bPLATE_wG2_s9_cRGB.png	\N	GREEN
12496	wG2_d3-3_cGFP	\N	\N
12497	original/PLATE-1A/bPLATE_wG2_s9_cRGB.png	\N	RED
12498	wG2_d3-3_cCy5	\N	\N
12499	original/PLATE-1A/bPLATE_wE4_s8_cRGB.png	\N	BLUE
12500	wE4_d2-3_cDAPI	\N	\N
12501	original/PLATE-1A/bPLATE_wE4_s8_cRGB.png	\N	GREEN
12502	wE4_d2-3_cGFP	\N	\N
12503	original/PLATE-1A/bPLATE_wE4_s8_cRGB.png	\N	RED
12504	wE4_d2-3_cCy5	\N	\N
12505	original/PLATE-1A/bPLATE_wG11_s5_cRGB.png	\N	BLUE
12506	wG11_d2-2_cDAPI	\N	\N
12507	original/PLATE-1A/bPLATE_wG11_s5_cRGB.png	\N	GREEN
12508	wG11_d2-2_cGFP	\N	\N
12509	original/PLATE-1A/bPLATE_wG11_s5_cRGB.png	\N	RED
12510	wG11_d2-2_cCy5	\N	\N
12511	original/PLATE-1A/bPLATE_wG8_s8_cRGB.png	\N	BLUE
12512	wG8_d2-3_cDAPI	\N	\N
12513	original/PLATE-1A/bPLATE_wG8_s8_cRGB.png	\N	GREEN
12514	wG8_d2-3_cGFP	\N	\N
12515	original/PLATE-1A/bPLATE_wG8_s8_cRGB.png	\N	RED
12516	wG8_d2-3_cCy5	\N	\N
12517	original/PLATE-1A/bPLATE_wB8_s7_cRGB.png	\N	BLUE
12518	wB8_d1-3_cDAPI	\N	\N
12519	original/PLATE-1A/bPLATE_wB8_s7_cRGB.png	\N	GREEN
12520	wB8_d1-3_cGFP	\N	\N
12521	original/PLATE-1A/bPLATE_wB8_s7_cRGB.png	\N	RED
12522	wB8_d1-3_cCy5	\N	\N
12523	original/PLATE-1A/bPLATE_wE3_s4_cRGB.png	\N	BLUE
12524	wE3_d1-2_cDAPI	\N	\N
12525	original/PLATE-1A/bPLATE_wE3_s4_cRGB.png	\N	GREEN
12526	wE3_d1-2_cGFP	\N	\N
12527	original/PLATE-1A/bPLATE_wE3_s4_cRGB.png	\N	RED
12528	wE3_d1-2_cCy5	\N	\N
12529	original/PLATE-1A/bPLATE_wG10_s1_cRGB.png	\N	BLUE
12530	wG10_d1-1_cDAPI	\N	\N
12531	original/PLATE-1A/bPLATE_wG10_s1_cRGB.png	\N	GREEN
12532	wG10_d1-1_cGFP	\N	\N
12533	original/PLATE-1A/bPLATE_wG10_s1_cRGB.png	\N	RED
12534	wG10_d1-1_cCy5	\N	\N
12535	original/PLATE-1A/bPLATE_wH7_s2_cRGB.png	\N	BLUE
12536	wH7_d2-1_cDAPI	\N	\N
12537	original/PLATE-1A/bPLATE_wH7_s2_cRGB.png	\N	GREEN
12538	wH7_d2-1_cGFP	\N	\N
12539	original/PLATE-1A/bPLATE_wH7_s2_cRGB.png	\N	RED
12540	wH7_d2-1_cCy5	\N	\N
12541	original/PLATE-1A/bPLATE_wC5_s8_cRGB.png	\N	BLUE
12542	wC5_d2-3_cDAPI	\N	\N
12543	original/PLATE-1A/bPLATE_wC5_s8_cRGB.png	\N	GREEN
12544	wC5_d2-3_cGFP	\N	\N
12545	original/PLATE-1A/bPLATE_wC5_s8_cRGB.png	\N	RED
12546	wC5_d2-3_cCy5	\N	\N
12547	original/PLATE-1A/bPLATE_wE12_s5_cRGB.png	\N	BLUE
12548	wE12_d2-2_cDAPI	\N	\N
12549	original/PLATE-1A/bPLATE_wE12_s5_cRGB.png	\N	GREEN
12550	wE12_d2-2_cGFP	\N	\N
12551	original/PLATE-1A/bPLATE_wE12_s5_cRGB.png	\N	RED
12552	wE12_d2-2_cCy5	\N	\N
12553	original/PLATE-1A/bPLATE_wH9_s9_cRGB.png	\N	BLUE
12554	wH9_d3-3_cDAPI	\N	\N
12555	original/PLATE-1A/bPLATE_wH9_s9_cRGB.png	\N	GREEN
12556	wH9_d3-3_cGFP	\N	\N
12557	original/PLATE-1A/bPLATE_wH9_s9_cRGB.png	\N	RED
12558	wH9_d3-3_cCy5	\N	\N
12559	original/PLATE-1A/bPLATE_wH2_s8_cRGB.png	\N	BLUE
12560	wH2_d2-3_cDAPI	\N	\N
12561	original/PLATE-1A/bPLATE_wH2_s8_cRGB.png	\N	GREEN
12562	wH2_d2-3_cGFP	\N	\N
12563	original/PLATE-1A/bPLATE_wH2_s8_cRGB.png	\N	RED
12564	wH2_d2-3_cCy5	\N	\N
12565	original/PLATE-1A/bPLATE_wD3_s7_cRGB.png	\N	BLUE
12566	wD3_d1-3_cDAPI	\N	\N
12567	original/PLATE-1A/bPLATE_wD3_s7_cRGB.png	\N	GREEN
12568	wD3_d1-3_cGFP	\N	\N
12569	original/PLATE-1A/bPLATE_wD3_s7_cRGB.png	\N	RED
12570	wD3_d1-3_cCy5	\N	\N
12571	original/PLATE-1A/bPLATE_wF10_s4_cRGB.png	\N	BLUE
12572	wF10_d1-2_cDAPI	\N	\N
12573	original/PLATE-1A/bPLATE_wF10_s4_cRGB.png	\N	GREEN
12574	wF10_d1-2_cGFP	\N	\N
12575	original/PLATE-1A/bPLATE_wF10_s4_cRGB.png	\N	RED
12576	wF10_d1-2_cCy5	\N	\N
12577	original/PLATE-1A/bPLATE_wG10_s8_cRGB.png	\N	BLUE
12578	wG10_d2-3_cDAPI	\N	\N
12579	original/PLATE-1A/bPLATE_wG10_s8_cRGB.png	\N	GREEN
12580	wG10_d2-3_cGFP	\N	\N
12581	original/PLATE-1A/bPLATE_wG10_s8_cRGB.png	\N	RED
12582	wG10_d2-3_cCy5	\N	\N
12583	original/PLATE-1A/bPLATE_wA7_s3_cRGB.png	\N	BLUE
12584	wA7_d3-1_cDAPI	\N	\N
12585	original/PLATE-1A/bPLATE_wA7_s3_cRGB.png	\N	GREEN
12586	wA7_d3-1_cGFP	\N	\N
12587	original/PLATE-1A/bPLATE_wA7_s3_cRGB.png	\N	RED
12588	wA7_d3-1_cCy5	\N	\N
12589	original/PLATE-1A/bPLATE_wC9_s8_cRGB.png	\N	BLUE
12590	wC9_d2-3_cDAPI	\N	\N
12591	original/PLATE-1A/bPLATE_wC9_s8_cRGB.png	\N	GREEN
12592	wC9_d2-3_cGFP	\N	\N
12593	original/PLATE-1A/bPLATE_wC9_s8_cRGB.png	\N	RED
12594	wC9_d2-3_cCy5	\N	\N
12595	original/PLATE-1A/bPLATE_wF4_s5_cRGB.png	\N	BLUE
12596	wF4_d2-2_cDAPI	\N	\N
12597	original/PLATE-1A/bPLATE_wF4_s5_cRGB.png	\N	GREEN
12598	wF4_d2-2_cGFP	\N	\N
12599	original/PLATE-1A/bPLATE_wF4_s5_cRGB.png	\N	RED
12600	wF4_d2-2_cCy5	\N	\N
12601	original/PLATE-1A/bPLATE_wH11_s2_cRGB.png	\N	BLUE
12602	wH11_d2-1_cDAPI	\N	\N
12603	original/PLATE-1A/bPLATE_wH11_s2_cRGB.png	\N	GREEN
12604	wH11_d2-1_cGFP	\N	\N
12605	original/PLATE-1A/bPLATE_wH11_s2_cRGB.png	\N	RED
12606	wH11_d2-1_cCy5	\N	\N
12607	original/PLATE-1A/bPLATE_wG12_s8_cRGB.png	\N	BLUE
12608	wG12_d2-3_cDAPI	\N	\N
12609	original/PLATE-1A/bPLATE_wG12_s8_cRGB.png	\N	GREEN
12610	wG12_d2-3_cGFP	\N	\N
12611	original/PLATE-1A/bPLATE_wG12_s8_cRGB.png	\N	RED
12612	wG12_d2-3_cCy5	\N	\N
12613	original/PLATE-1A/bPLATE_wH4_s4_cRGB.png	\N	BLUE
12614	wH4_d1-2_cDAPI	\N	\N
12615	original/PLATE-1A/bPLATE_wH4_s4_cRGB.png	\N	GREEN
12616	wH4_d1-2_cGFP	\N	\N
12617	original/PLATE-1A/bPLATE_wH4_s4_cRGB.png	\N	RED
12618	wH4_d1-2_cCy5	\N	\N
12619	original/PLATE-1A/bPLATE_wE9_s7_cRGB.png	\N	BLUE
12620	wE9_d1-3_cDAPI	\N	\N
12621	original/PLATE-1A/bPLATE_wE9_s7_cRGB.png	\N	GREEN
12622	wE9_d1-3_cGFP	\N	\N
12623	original/PLATE-1A/bPLATE_wE9_s7_cRGB.png	\N	RED
12624	wE9_d1-3_cCy5	\N	\N
12625	original/PLATE-1A/bPLATE_wD4_s6_cRGB.png	\N	BLUE
12626	wD4_d3-2_cDAPI	\N	\N
12627	original/PLATE-1A/bPLATE_wD4_s6_cRGB.png	\N	GREEN
12628	wD4_d3-2_cGFP	\N	\N
12629	original/PLATE-1A/bPLATE_wD4_s6_cRGB.png	\N	RED
12630	wD4_d3-2_cCy5	\N	\N
12631	original/PLATE-1A/bPLATE_wA9_s9_cRGB.png	\N	BLUE
12632	wA9_d3-3_cDAPI	\N	\N
12633	original/PLATE-1A/bPLATE_wA9_s9_cRGB.png	\N	GREEN
12634	wA9_d3-3_cGFP	\N	\N
12635	original/PLATE-1A/bPLATE_wA9_s9_cRGB.png	\N	RED
12636	wA9_d3-3_cCy5	\N	\N
12637	original/PLATE-1A/bPLATE_wF11_s3_cRGB.png	\N	BLUE
12638	wF11_d3-1_cDAPI	\N	\N
12639	original/PLATE-1A/bPLATE_wF11_s3_cRGB.png	\N	GREEN
12640	wF11_d3-1_cGFP	\N	\N
12641	original/PLATE-1A/bPLATE_wF11_s3_cRGB.png	\N	RED
12642	wF11_d3-1_cCy5	\N	\N
12643	original/PLATE-1A/bPLATE_wD11_s7_cRGB.png	\N	BLUE
12644	wD11_d1-3_cDAPI	\N	\N
12645	original/PLATE-1A/bPLATE_wD11_s7_cRGB.png	\N	GREEN
12646	wD11_d1-3_cGFP	\N	\N
12647	original/PLATE-1A/bPLATE_wD11_s7_cRGB.png	\N	RED
12648	wD11_d1-3_cCy5	\N	\N
12649	original/PLATE-1A/bPLATE_wG6_s4_cRGB.png	\N	BLUE
12650	wG6_d1-2_cDAPI	\N	\N
12651	original/PLATE-1A/bPLATE_wG6_s4_cRGB.png	\N	GREEN
12652	wG6_d1-2_cGFP	\N	\N
12653	original/PLATE-1A/bPLATE_wG6_s4_cRGB.png	\N	RED
12654	wG6_d1-2_cCy5	\N	\N
12655	original/PLATE-1A/bPLATE_wH12_s3_cRGB.png	\N	BLUE
12656	wH12_d3-1_cDAPI	\N	\N
12657	original/PLATE-1A/bPLATE_wH12_s3_cRGB.png	\N	GREEN
12658	wH12_d3-1_cGFP	\N	\N
12659	original/PLATE-1A/bPLATE_wH12_s3_cRGB.png	\N	RED
12660	wH12_d3-1_cCy5	\N	\N
12661	original/PLATE-1A/bPLATE_wC10_s9_cRGB.png	\N	BLUE
12662	wC10_d3-3_cDAPI	\N	\N
12663	original/PLATE-1A/bPLATE_wC10_s9_cRGB.png	\N	GREEN
12664	wC10_d3-3_cGFP	\N	\N
12665	original/PLATE-1A/bPLATE_wC10_s9_cRGB.png	\N	RED
12666	wC10_d3-3_cCy5	\N	\N
12667	original/PLATE-1A/bPLATE_wF5_s6_cRGB.png	\N	BLUE
12668	wF5_d3-2_cDAPI	\N	\N
12669	original/PLATE-1A/bPLATE_wF5_s6_cRGB.png	\N	GREEN
12670	wF5_d3-2_cGFP	\N	\N
12671	original/PLATE-1A/bPLATE_wF5_s6_cRGB.png	\N	RED
12672	wF5_d3-2_cCy5	\N	\N
12673	original/PLATE-1A/bPLATE_wB8_s1_cRGB.png	\N	BLUE
12674	wB8_d1-1_cDAPI	\N	\N
12675	original/PLATE-1A/bPLATE_wB8_s1_cRGB.png	\N	GREEN
12676	wB8_d1-1_cGFP	\N	\N
12677	original/PLATE-1A/bPLATE_wB8_s1_cRGB.png	\N	RED
12678	wB8_d1-1_cCy5	\N	\N
12679	original/PLATE-1A/bPLATE_wH5_s6_cRGB.png	\N	BLUE
12680	wH5_d3-2_cDAPI	\N	\N
12681	original/PLATE-1A/bPLATE_wH5_s6_cRGB.png	\N	GREEN
12682	wH5_d3-2_cGFP	\N	\N
12683	original/PLATE-1A/bPLATE_wH5_s6_cRGB.png	\N	RED
12684	wH5_d3-2_cCy5	\N	\N
12685	original/PLATE-1A/bPLATE_wE10_s9_cRGB.png	\N	BLUE
12686	wE10_d3-3_cDAPI	\N	\N
12687	original/PLATE-1A/bPLATE_wE10_s9_cRGB.png	\N	GREEN
12688	wE10_d3-3_cGFP	\N	\N
12689	original/PLATE-1A/bPLATE_wE10_s9_cRGB.png	\N	RED
12690	wE10_d3-3_cCy5	\N	\N
12691	original/PLATE-1A/bPLATE_wB1_s6_cRGB.png	\N	BLUE
12692	wB1_d3-2_cDAPI	\N	\N
12693	original/PLATE-1A/bPLATE_wB1_s6_cRGB.png	\N	GREEN
12694	wB1_d3-2_cGFP	\N	\N
12695	original/PLATE-1A/bPLATE_wB1_s6_cRGB.png	\N	RED
12696	wB1_d3-2_cCy5	\N	\N
12697	original/PLATE-1A/bPLATE_wD8_s3_cRGB.png	\N	BLUE
12698	wD8_d3-1_cDAPI	\N	\N
12699	original/PLATE-1A/bPLATE_wD8_s3_cRGB.png	\N	GREEN
12700	wD8_d3-1_cGFP	\N	\N
12701	original/PLATE-1A/bPLATE_wD8_s3_cRGB.png	\N	RED
12702	wD8_d3-1_cCy5	\N	\N
12703	original/PLATE-1A/bPLATE_wD2_s4_cRGB.png	\N	BLUE
12704	wD2_d1-2_cDAPI	\N	\N
12705	original/PLATE-1A/bPLATE_wD2_s4_cRGB.png	\N	GREEN
12706	wD2_d1-2_cGFP	\N	\N
12707	original/PLATE-1A/bPLATE_wD2_s4_cRGB.png	\N	RED
12708	wD2_d1-2_cCy5	\N	\N
12709	original/PLATE-1A/bPLATE_wA7_s7_cRGB.png	\N	BLUE
12710	wA7_d1-3_cDAPI	\N	\N
12711	original/PLATE-1A/bPLATE_wA7_s7_cRGB.png	\N	GREEN
12712	wA7_d1-3_cGFP	\N	\N
12713	original/PLATE-1A/bPLATE_wA7_s7_cRGB.png	\N	RED
12714	wA7_d1-3_cCy5	\N	\N
12715	original/PLATE-1A/bPLATE_wF9_s1_cRGB.png	\N	BLUE
12716	wF9_d1-1_cDAPI	\N	\N
12717	original/PLATE-1A/bPLATE_wF9_s1_cRGB.png	\N	GREEN
12718	wF9_d1-1_cGFP	\N	\N
12719	original/PLATE-1A/bPLATE_wF9_s1_cRGB.png	\N	RED
12720	wF9_d1-1_cCy5	\N	\N
12721	original/PLATE-1A/bPLATE_wC6_s4_cRGB.png	\N	BLUE
12722	wC6_d1-2_cDAPI	\N	\N
12723	original/PLATE-1A/bPLATE_wC6_s4_cRGB.png	\N	GREEN
12724	wC6_d1-2_cGFP	\N	\N
12725	original/PLATE-1A/bPLATE_wC6_s4_cRGB.png	\N	RED
12726	wC6_d1-2_cCy5	\N	\N
12727	original/PLATE-1A/bPLATE_wF1_s1_cRGB.png	\N	BLUE
12728	wF1_d1-1_cDAPI	\N	\N
12729	original/PLATE-1A/bPLATE_wF1_s1_cRGB.png	\N	GREEN
12730	wF1_d1-1_cGFP	\N	\N
12731	original/PLATE-1A/bPLATE_wF1_s1_cRGB.png	\N	RED
12732	wF1_d1-1_cCy5	\N	\N
12733	original/PLATE-1A/bPLATE_wH6_s3_cRGB.png	\N	BLUE
12734	wH6_d3-1_cDAPI	\N	\N
12735	original/PLATE-1A/bPLATE_wH6_s3_cRGB.png	\N	GREEN
12736	wH6_d3-1_cGFP	\N	\N
12737	original/PLATE-1A/bPLATE_wH6_s3_cRGB.png	\N	RED
12738	wH6_d3-1_cCy5	\N	\N
12739	original/PLATE-1A/bPLATE_wC4_s9_cRGB.png	\N	BLUE
12740	wC4_d3-3_cDAPI	\N	\N
12741	original/PLATE-1A/bPLATE_wC4_s9_cRGB.png	\N	GREEN
12742	wC4_d3-3_cGFP	\N	\N
12743	original/PLATE-1A/bPLATE_wC4_s9_cRGB.png	\N	RED
12744	wC4_d3-3_cCy5	\N	\N
12745	original/PLATE-1A/bPLATE_wE11_s6_cRGB.png	\N	BLUE
12746	wE11_d3-2_cDAPI	\N	\N
12747	original/PLATE-1A/bPLATE_wE11_s6_cRGB.png	\N	GREEN
12748	wE11_d3-2_cGFP	\N	\N
12749	original/PLATE-1A/bPLATE_wE11_s6_cRGB.png	\N	RED
12750	wE11_d3-2_cCy5	\N	\N
12751	original/PLATE-1A/bPLATE_wH1_s2_cRGB.png	\N	BLUE
12752	wH1_d2-1_cDAPI	\N	\N
12753	original/PLATE-1A/bPLATE_wH1_s2_cRGB.png	\N	GREEN
12754	wH1_d2-1_cGFP	\N	\N
12755	original/PLATE-1A/bPLATE_wH1_s2_cRGB.png	\N	RED
12756	wH1_d2-1_cCy5	\N	\N
12757	original/PLATE-1A/bPLATE_wB11_s8_cRGB.png	\N	BLUE
12758	wB11_d2-3_cDAPI	\N	\N
12759	original/PLATE-1A/bPLATE_wB11_s8_cRGB.png	\N	GREEN
12760	wB11_d2-3_cGFP	\N	\N
12761	original/PLATE-1A/bPLATE_wB11_s8_cRGB.png	\N	RED
12762	wB11_d2-3_cCy5	\N	\N
12763	original/PLATE-1A/bPLATE_wE6_s5_cRGB.png	\N	BLUE
12764	wE6_d2-2_cDAPI	\N	\N
12765	original/PLATE-1A/bPLATE_wE6_s5_cRGB.png	\N	GREEN
12766	wE6_d2-2_cGFP	\N	\N
12767	original/PLATE-1A/bPLATE_wE6_s5_cRGB.png	\N	RED
12768	wE6_d2-2_cCy5	\N	\N
12769	original/PLATE-1A/bPLATE_wG12_s9_cRGB.png	\N	BLUE
12770	wG12_d3-3_cDAPI	\N	\N
12771	original/PLATE-1A/bPLATE_wG12_s9_cRGB.png	\N	GREEN
12772	wG12_d3-3_cGFP	\N	\N
12773	original/PLATE-1A/bPLATE_wG12_s9_cRGB.png	\N	RED
12774	wG12_d3-3_cCy5	\N	\N
12775	original/PLATE-1A/bPLATE_wH12_s7_cRGB.png	\N	BLUE
12776	wH12_d1-3_cDAPI	\N	\N
12777	original/PLATE-1A/bPLATE_wH12_s7_cRGB.png	\N	GREEN
12778	wH12_d1-3_cGFP	\N	\N
12779	original/PLATE-1A/bPLATE_wH12_s7_cRGB.png	\N	RED
12780	wH12_d1-3_cCy5	\N	\N
12781	original/PLATE-1A/bPLATE_wH7_s5_cRGB.png	\N	BLUE
12782	wH7_d2-2_cDAPI	\N	\N
12783	original/PLATE-1A/bPLATE_wH7_s5_cRGB.png	\N	GREEN
12784	wH7_d2-2_cGFP	\N	\N
12785	original/PLATE-1A/bPLATE_wH7_s5_cRGB.png	\N	RED
12786	wH7_d2-2_cCy5	\N	\N
12787	original/PLATE-1A/bPLATE_wE12_s8_cRGB.png	\N	BLUE
12788	wE12_d2-3_cDAPI	\N	\N
12789	original/PLATE-1A/bPLATE_wE12_s8_cRGB.png	\N	GREEN
12790	wE12_d2-3_cGFP	\N	\N
12791	original/PLATE-1A/bPLATE_wE12_s8_cRGB.png	\N	RED
12792	wE12_d2-3_cCy5	\N	\N
12793	original/PLATE-1A/bPLATE_wC4_s6_cRGB.png	\N	BLUE
12794	wC4_d3-2_cDAPI	\N	\N
12795	original/PLATE-1A/bPLATE_wC4_s6_cRGB.png	\N	GREEN
12796	wC4_d3-2_cGFP	\N	\N
12797	original/PLATE-1A/bPLATE_wC4_s6_cRGB.png	\N	RED
12798	wC4_d3-2_cCy5	\N	\N
12799	original/PLATE-1A/bPLATE_wE11_s3_cRGB.png	\N	BLUE
12800	wE11_d3-1_cDAPI	\N	\N
12801	original/PLATE-1A/bPLATE_wE11_s3_cRGB.png	\N	GREEN
12802	wE11_d3-1_cGFP	\N	\N
12803	original/PLATE-1A/bPLATE_wE11_s3_cRGB.png	\N	RED
12804	wE11_d3-1_cCy5	\N	\N
12805	original/PLATE-1A/bPLATE_wC1_s6_cRGB.png	\N	BLUE
12806	wC1_d3-2_cDAPI	\N	\N
12807	original/PLATE-1A/bPLATE_wC1_s6_cRGB.png	\N	GREEN
12808	wC1_d3-2_cGFP	\N	\N
12809	original/PLATE-1A/bPLATE_wC1_s6_cRGB.png	\N	RED
12810	wC1_d3-2_cCy5	\N	\N
12811	original/PLATE-1A/bPLATE_wE8_s3_cRGB.png	\N	BLUE
12812	wE8_d3-1_cDAPI	\N	\N
12813	original/PLATE-1A/bPLATE_wE8_s3_cRGB.png	\N	GREEN
12814	wE8_d3-1_cGFP	\N	\N
12815	original/PLATE-1A/bPLATE_wE8_s3_cRGB.png	\N	RED
12816	wE8_d3-1_cCy5	\N	\N
12817	original/PLATE-1A/bPLATE_wB9_s3_cRGB.png	\N	BLUE
12818	wB9_d3-1_cDAPI	\N	\N
12819	original/PLATE-1A/bPLATE_wB9_s3_cRGB.png	\N	GREEN
12820	wB9_d3-1_cGFP	\N	\N
12821	original/PLATE-1A/bPLATE_wB9_s3_cRGB.png	\N	RED
12822	wB9_d3-1_cCy5	\N	\N
12823	original/PLATE-1A/bPLATE_wD5_s7_cRGB.png	\N	BLUE
12824	wD5_d1-3_cDAPI	\N	\N
12825	original/PLATE-1A/bPLATE_wD5_s7_cRGB.png	\N	GREEN
12826	wD5_d1-3_cGFP	\N	\N
12827	original/PLATE-1A/bPLATE_wD5_s7_cRGB.png	\N	RED
12828	wD5_d1-3_cCy5	\N	\N
12829	original/PLATE-1A/bPLATE_wF12_s4_cRGB.png	\N	BLUE
12830	wF12_d1-2_cDAPI	\N	\N
12831	original/PLATE-1A/bPLATE_wF12_s4_cRGB.png	\N	GREEN
12832	wF12_d1-2_cGFP	\N	\N
12833	original/PLATE-1A/bPLATE_wF12_s4_cRGB.png	\N	RED
12834	wF12_d1-2_cCy5	\N	\N
12835	original/PLATE-1A/bPLATE_wA10_s3_cRGB.png	\N	BLUE
12836	wA10_d3-1_cDAPI	\N	\N
12837	original/PLATE-1A/bPLATE_wA10_s3_cRGB.png	\N	GREEN
12838	wA10_d3-1_cGFP	\N	\N
12839	original/PLATE-1A/bPLATE_wA10_s3_cRGB.png	\N	RED
12840	wA10_d3-1_cCy5	\N	\N
12841	original/PLATE-1A/bPLATE_wC8_s8_cRGB.png	\N	BLUE
12842	wC8_d2-3_cDAPI	\N	\N
12843	original/PLATE-1A/bPLATE_wC8_s8_cRGB.png	\N	GREEN
12844	wC8_d2-3_cGFP	\N	\N
12845	original/PLATE-1A/bPLATE_wC8_s8_cRGB.png	\N	RED
12846	wC8_d2-3_cCy5	\N	\N
12847	original/PLATE-1A/bPLATE_wF3_s5_cRGB.png	\N	BLUE
12848	wF3_d2-2_cDAPI	\N	\N
12849	original/PLATE-1A/bPLATE_wF3_s5_cRGB.png	\N	GREEN
12850	wF3_d2-2_cGFP	\N	\N
12851	original/PLATE-1A/bPLATE_wF3_s5_cRGB.png	\N	RED
12852	wF3_d2-2_cCy5	\N	\N
12853	original/PLATE-1A/bPLATE_wH10_s2_cRGB.png	\N	BLUE
12854	wH10_d2-1_cDAPI	\N	\N
12855	original/PLATE-1A/bPLATE_wH10_s2_cRGB.png	\N	GREEN
12856	wH10_d2-1_cGFP	\N	\N
12857	original/PLATE-1A/bPLATE_wH10_s2_cRGB.png	\N	RED
12858	wH10_d2-1_cCy5	\N	\N
12859	original/PLATE-1A/bPLATE_wC10_s1_cRGB.png	\N	BLUE
12860	wC10_d1-1_cDAPI	\N	\N
12861	original/PLATE-1A/bPLATE_wC10_s1_cRGB.png	\N	GREEN
12862	wC10_d1-1_cGFP	\N	\N
12863	original/PLATE-1A/bPLATE_wC10_s1_cRGB.png	\N	RED
12864	wC10_d1-1_cCy5	\N	\N
12865	original/PLATE-1A/bPLATE_wA3_s4_cRGB.png	\N	BLUE
12866	wA3_d1-2_cDAPI	\N	\N
12867	original/PLATE-1A/bPLATE_wA3_s4_cRGB.png	\N	GREEN
12868	wA3_d1-2_cGFP	\N	\N
12869	original/PLATE-1A/bPLATE_wA3_s4_cRGB.png	\N	RED
12870	wA3_d1-2_cCy5	\N	\N
12871	original/PLATE-1A/bPLATE_wC11_s2_cRGB.png	\N	BLUE
12872	wC11_d2-1_cDAPI	\N	\N
12873	original/PLATE-1A/bPLATE_wC11_s2_cRGB.png	\N	GREEN
12874	wC11_d2-1_cGFP	\N	\N
12875	original/PLATE-1A/bPLATE_wC11_s2_cRGB.png	\N	RED
12876	wC11_d2-1_cCy5	\N	\N
12877	original/PLATE-1A/bPLATE_wA4_s5_cRGB.png	\N	BLUE
12878	wA4_d2-2_cDAPI	\N	\N
12879	original/PLATE-1A/bPLATE_wA4_s5_cRGB.png	\N	GREEN
12880	wA4_d2-2_cGFP	\N	\N
12881	original/PLATE-1A/bPLATE_wA4_s5_cRGB.png	\N	RED
12882	wA4_d2-2_cCy5	\N	\N
12883	original/PLATE-1A/bPLATE_wD9_s7_cRGB.png	\N	BLUE
12884	wD9_d1-3_cDAPI	\N	\N
12885	original/PLATE-1A/bPLATE_wD9_s7_cRGB.png	\N	GREEN
12886	wD9_d1-3_cGFP	\N	\N
12887	original/PLATE-1A/bPLATE_wD9_s7_cRGB.png	\N	RED
12888	wD9_d1-3_cCy5	\N	\N
12889	original/PLATE-1A/bPLATE_wG4_s4_cRGB.png	\N	BLUE
12890	wG4_d1-2_cDAPI	\N	\N
12891	original/PLATE-1A/bPLATE_wG4_s4_cRGB.png	\N	GREEN
12892	wG4_d1-2_cGFP	\N	\N
12893	original/PLATE-1A/bPLATE_wG4_s4_cRGB.png	\N	RED
12894	wG4_d1-2_cCy5	\N	\N
12895	original/PLATE-1A/bPLATE_wA1_s1_cRGB.png	\N	BLUE
12896	wA1_d1-1_cDAPI	\N	\N
12897	original/PLATE-1A/bPLATE_wA1_s1_cRGB.png	\N	GREEN
12898	wA1_d1-1_cGFP	\N	\N
12899	original/PLATE-1A/bPLATE_wA1_s1_cRGB.png	\N	RED
12900	wA1_d1-1_cCy5	\N	\N
12901	original/PLATE-1A/bPLATE_wB6_s7_cRGB.png	\N	BLUE
12902	wB6_d1-3_cDAPI	\N	\N
12903	original/PLATE-1A/bPLATE_wB6_s7_cRGB.png	\N	GREEN
12904	wB6_d1-3_cGFP	\N	\N
12905	original/PLATE-1A/bPLATE_wB6_s7_cRGB.png	\N	RED
12906	wB6_d1-3_cCy5	\N	\N
12907	original/PLATE-1A/bPLATE_wE1_s4_cRGB.png	\N	BLUE
12908	wE1_d1-2_cDAPI	\N	\N
12909	original/PLATE-1A/bPLATE_wE1_s4_cRGB.png	\N	GREEN
12910	wE1_d1-2_cGFP	\N	\N
12911	original/PLATE-1A/bPLATE_wE1_s4_cRGB.png	\N	RED
12912	wE1_d1-2_cCy5	\N	\N
12913	original/PLATE-1A/bPLATE_wG8_s1_cRGB.png	\N	BLUE
12914	wG8_d1-1_cDAPI	\N	\N
12915	original/PLATE-1A/bPLATE_wG8_s1_cRGB.png	\N	GREEN
12916	wG8_d1-1_cGFP	\N	\N
12917	original/PLATE-1A/bPLATE_wG8_s1_cRGB.png	\N	RED
12918	wG8_d1-1_cCy5	\N	\N
12919	original/PLATE-1A/bPLATE_wB12_s4_cRGB.png	\N	BLUE
12920	wB12_d1-2_cDAPI	\N	\N
12921	original/PLATE-1A/bPLATE_wB12_s4_cRGB.png	\N	GREEN
12922	wB12_d1-2_cGFP	\N	\N
12923	original/PLATE-1A/bPLATE_wB12_s4_cRGB.png	\N	RED
12924	wB12_d1-2_cCy5	\N	\N
12925	original/PLATE-1A/bPLATE_wE7_s1_cRGB.png	\N	BLUE
12926	wE7_d1-1_cDAPI	\N	\N
12927	original/PLATE-1A/bPLATE_wE7_s1_cRGB.png	\N	GREEN
12928	wE7_d1-1_cGFP	\N	\N
12929	original/PLATE-1A/bPLATE_wE7_s1_cRGB.png	\N	RED
12930	wE7_d1-1_cCy5	\N	\N
12931	original/PLATE-1A/bPLATE_wH9_s7_cRGB.png	\N	BLUE
12932	wH9_d1-3_cDAPI	\N	\N
12933	original/PLATE-1A/bPLATE_wH9_s7_cRGB.png	\N	GREEN
12934	wH9_d1-3_cGFP	\N	\N
12935	original/PLATE-1A/bPLATE_wH9_s7_cRGB.png	\N	RED
12936	wH9_d1-3_cCy5	\N	\N
12937	original/PLATE-1A/bPLATE_wB10_s5_cRGB.png	\N	BLUE
12938	wB10_d2-2_cDAPI	\N	\N
12939	original/PLATE-1A/bPLATE_wB10_s5_cRGB.png	\N	GREEN
12940	wB10_d2-2_cGFP	\N	\N
12941	original/PLATE-1A/bPLATE_wB10_s5_cRGB.png	\N	RED
12942	wB10_d2-2_cCy5	\N	\N
12943	original/PLATE-1A/bPLATE_wE5_s2_cRGB.png	\N	BLUE
12944	wE5_d2-1_cDAPI	\N	\N
12945	original/PLATE-1A/bPLATE_wE5_s2_cRGB.png	\N	GREEN
12946	wE5_d2-1_cGFP	\N	\N
12947	original/PLATE-1A/bPLATE_wE5_s2_cRGB.png	\N	RED
12948	wE5_d2-1_cCy5	\N	\N
12949	original/PLATE-1A/bPLATE_wH7_s3_cRGB.png	\N	BLUE
12950	wH7_d3-1_cDAPI	\N	\N
12951	original/PLATE-1A/bPLATE_wH7_s3_cRGB.png	\N	GREEN
12952	wH7_d3-1_cGFP	\N	\N
12953	original/PLATE-1A/bPLATE_wH7_s3_cRGB.png	\N	RED
12954	wH7_d3-1_cCy5	\N	\N
12955	original/PLATE-1A/bPLATE_wD12_s3_cRGB.png	\N	BLUE
12956	wD12_d3-1_cDAPI	\N	\N
12957	original/PLATE-1A/bPLATE_wD12_s3_cRGB.png	\N	GREEN
12958	wD12_d3-1_cGFP	\N	\N
12959	original/PLATE-1A/bPLATE_wD12_s3_cRGB.png	\N	RED
12960	wD12_d3-1_cCy5	\N	\N
12961	original/PLATE-1A/bPLATE_wC5_s9_cRGB.png	\N	BLUE
12962	wC5_d3-3_cDAPI	\N	\N
12963	original/PLATE-1A/bPLATE_wC5_s9_cRGB.png	\N	GREEN
12964	wC5_d3-3_cGFP	\N	\N
12965	original/PLATE-1A/bPLATE_wC5_s9_cRGB.png	\N	RED
12966	wC5_d3-3_cCy5	\N	\N
12967	original/PLATE-1A/bPLATE_wB5_s6_cRGB.png	\N	BLUE
12968	wB5_d3-2_cDAPI	\N	\N
12969	original/PLATE-1A/bPLATE_wB5_s6_cRGB.png	\N	GREEN
12970	wB5_d3-2_cGFP	\N	\N
12971	original/PLATE-1A/bPLATE_wB5_s6_cRGB.png	\N	RED
12972	wB5_d3-2_cCy5	\N	\N
12973	original/PLATE-1A/bPLATE_wE12_s6_cRGB.png	\N	BLUE
12974	wE12_d3-2_cDAPI	\N	\N
12975	original/PLATE-1A/bPLATE_wE12_s6_cRGB.png	\N	GREEN
12976	wE12_d3-2_cGFP	\N	\N
12977	original/PLATE-1A/bPLATE_wE12_s6_cRGB.png	\N	RED
12978	wE12_d3-2_cCy5	\N	\N
12979	original/PLATE-1A/bPLATE_wB12_s2_cRGB.png	\N	BLUE
12980	wB12_d2-1_cDAPI	\N	\N
12981	original/PLATE-1A/bPLATE_wB12_s2_cRGB.png	\N	GREEN
12982	wB12_d2-1_cGFP	\N	\N
12983	original/PLATE-1A/bPLATE_wB12_s2_cRGB.png	\N	RED
12984	wB12_d2-1_cCy5	\N	\N
12985	original/PLATE-1A/bPLATE_wD7_s8_cRGB.png	\N	BLUE
12986	wD7_d2-3_cDAPI	\N	\N
12987	original/PLATE-1A/bPLATE_wD7_s8_cRGB.png	\N	GREEN
12988	wD7_d2-3_cGFP	\N	\N
12989	original/PLATE-1A/bPLATE_wD7_s8_cRGB.png	\N	RED
12990	wD7_d2-3_cCy5	\N	\N
12991	original/PLATE-1A/bPLATE_wF9_s7_cRGB.png	\N	BLUE
12992	wF9_d1-3_cDAPI	\N	\N
12993	original/PLATE-1A/bPLATE_wF9_s7_cRGB.png	\N	GREEN
12994	wF9_d1-3_cGFP	\N	\N
12995	original/PLATE-1A/bPLATE_wF9_s7_cRGB.png	\N	RED
12996	wF9_d1-3_cCy5	\N	\N
12997	original/PLATE-1A/bPLATE_wG2_s5_cRGB.png	\N	BLUE
12998	wG2_d2-2_cDAPI	\N	\N
12999	original/PLATE-1A/bPLATE_wG2_s5_cRGB.png	\N	GREEN
13000	wG2_d2-2_cGFP	\N	\N
13001	original/PLATE-1A/bPLATE_wG2_s5_cRGB.png	\N	RED
13002	wG2_d2-2_cCy5	\N	\N
13003	original/PLATE-1A/bPLATE_wC1_s1_cRGB.png	\N	BLUE
13004	wC1_d1-1_cDAPI	\N	\N
13005	original/PLATE-1A/bPLATE_wC1_s1_cRGB.png	\N	GREEN
13006	wC1_d1-1_cGFP	\N	\N
13007	original/PLATE-1A/bPLATE_wC1_s1_cRGB.png	\N	RED
13008	wC1_d1-1_cCy5	\N	\N
13009	original/PLATE-1A/bPLATE_wF3_s7_cRGB.png	\N	BLUE
13010	wF3_d1-3_cDAPI	\N	\N
13011	original/PLATE-1A/bPLATE_wF3_s7_cRGB.png	\N	GREEN
13012	wF3_d1-3_cGFP	\N	\N
13013	original/PLATE-1A/bPLATE_wF3_s7_cRGB.png	\N	RED
13014	wF3_d1-3_cCy5	\N	\N
13015	original/PLATE-1A/bPLATE_wH10_s4_cRGB.png	\N	BLUE
13016	wH10_d1-2_cDAPI	\N	\N
13017	original/PLATE-1A/bPLATE_wH10_s4_cRGB.png	\N	GREEN
13018	wH10_d1-2_cGFP	\N	\N
13019	original/PLATE-1A/bPLATE_wH10_s4_cRGB.png	\N	RED
13020	wH10_d1-2_cCy5	\N	\N
13021	original/PLATE-1A/bPLATE_wH9_s5_cRGB.png	\N	BLUE
13022	wH9_d2-2_cDAPI	\N	\N
13023	original/PLATE-1A/bPLATE_wH9_s5_cRGB.png	\N	GREEN
13024	wH9_d2-2_cGFP	\N	\N
13025	original/PLATE-1A/bPLATE_wH9_s5_cRGB.png	\N	RED
13026	wH9_d2-2_cCy5	\N	\N
13027	original/PLATE-1A/bPLATE_wB12_s6_cRGB.png	\N	BLUE
13028	wB12_d3-2_cDAPI	\N	\N
13029	original/PLATE-1A/bPLATE_wB12_s6_cRGB.png	\N	GREEN
13030	wB12_d3-2_cGFP	\N	\N
13031	original/PLATE-1A/bPLATE_wB12_s6_cRGB.png	\N	RED
13032	wB12_d3-2_cCy5	\N	\N
13033	original/PLATE-1A/bPLATE_wE7_s3_cRGB.png	\N	BLUE
13034	wE7_d3-1_cDAPI	\N	\N
13035	original/PLATE-1A/bPLATE_wE7_s3_cRGB.png	\N	GREEN
13036	wE7_d3-1_cGFP	\N	\N
13037	original/PLATE-1A/bPLATE_wE7_s3_cRGB.png	\N	RED
13038	wE7_d3-1_cCy5	\N	\N
13039	original/PLATE-1A/bPLATE_wF2_s8_cRGB.png	\N	BLUE
13040	wF2_d2-3_cDAPI	\N	\N
13041	original/PLATE-1A/bPLATE_wF2_s8_cRGB.png	\N	GREEN
13042	wF2_d2-3_cGFP	\N	\N
13043	original/PLATE-1A/bPLATE_wF2_s8_cRGB.png	\N	RED
13044	wF2_d2-3_cCy5	\N	\N
13045	original/PLATE-1A/bPLATE_wG5_s9_cRGB.png	\N	BLUE
13046	wG5_d3-3_cDAPI	\N	\N
13047	original/PLATE-1A/bPLATE_wG5_s9_cRGB.png	\N	GREEN
13048	wG5_d3-3_cGFP	\N	\N
13049	original/PLATE-1A/bPLATE_wG5_s9_cRGB.png	\N	RED
13050	wG5_d3-3_cCy5	\N	\N
13051	original/PLATE-1A/bPLATE_wB2_s9_cRGB.png	\N	BLUE
13052	wB2_d3-3_cDAPI	\N	\N
13053	original/PLATE-1A/bPLATE_wB2_s9_cRGB.png	\N	GREEN
13054	wB2_d3-3_cGFP	\N	\N
13055	original/PLATE-1A/bPLATE_wB2_s9_cRGB.png	\N	RED
13056	wB2_d3-3_cCy5	\N	\N
13057	original/PLATE-1A/bPLATE_wD9_s6_cRGB.png	\N	BLUE
13058	wD9_d3-2_cDAPI	\N	\N
13059	original/PLATE-1A/bPLATE_wD9_s6_cRGB.png	\N	GREEN
13060	wD9_d3-2_cGFP	\N	\N
13061	original/PLATE-1A/bPLATE_wD9_s6_cRGB.png	\N	RED
13062	wD9_d3-2_cCy5	\N	\N
13063	original/PLATE-1A/bPLATE_wG4_s3_cRGB.png	\N	BLUE
13064	wG4_d3-1_cDAPI	\N	\N
13065	original/PLATE-1A/bPLATE_wG4_s3_cRGB.png	\N	GREEN
13066	wG4_d3-1_cGFP	\N	\N
13067	original/PLATE-1A/bPLATE_wG4_s3_cRGB.png	\N	RED
13068	wG4_d3-1_cCy5	\N	\N
13069	original/PLATE-1A/bPLATE_wD12_s5_cRGB.png	\N	BLUE
13070	wD12_d2-2_cDAPI	\N	\N
13071	original/PLATE-1A/bPLATE_wD12_s5_cRGB.png	\N	GREEN
13072	wD12_d2-2_cGFP	\N	\N
13073	original/PLATE-1A/bPLATE_wD12_s5_cRGB.png	\N	RED
13074	wD12_d2-2_cCy5	\N	\N
13075	original/PLATE-1A/bPLATE_wB5_s8_cRGB.png	\N	BLUE
13076	wB5_d2-3_cDAPI	\N	\N
13077	original/PLATE-1A/bPLATE_wB5_s8_cRGB.png	\N	GREEN
13078	wB5_d2-3_cGFP	\N	\N
13079	original/PLATE-1A/bPLATE_wB5_s8_cRGB.png	\N	RED
13080	wB5_d2-3_cCy5	\N	\N
13081	original/PLATE-1A/bPLATE_wE2_s8_cRGB.png	\N	BLUE
13082	wE2_d2-3_cDAPI	\N	\N
13083	original/PLATE-1A/bPLATE_wE2_s8_cRGB.png	\N	GREEN
13084	wE2_d2-3_cGFP	\N	\N
13085	original/PLATE-1A/bPLATE_wE2_s8_cRGB.png	\N	RED
13086	wE2_d2-3_cCy5	\N	\N
13087	original/PLATE-1A/bPLATE_wG7_s2_cRGB.png	\N	BLUE
13088	wG7_d2-1_cDAPI	\N	\N
13089	original/PLATE-1A/bPLATE_wG7_s2_cRGB.png	\N	GREEN
13090	wG7_d2-1_cGFP	\N	\N
13091	original/PLATE-1A/bPLATE_wG7_s2_cRGB.png	\N	RED
13092	wG7_d2-1_cCy5	\N	\N
13093	original/PLATE-1A/bPLATE_wG9_s5_cRGB.png	\N	BLUE
13094	wG9_d2-2_cDAPI	\N	\N
13095	original/PLATE-1A/bPLATE_wG9_s5_cRGB.png	\N	GREEN
13096	wG9_d2-2_cGFP	\N	\N
13097	original/PLATE-1A/bPLATE_wG9_s5_cRGB.png	\N	RED
13098	wG9_d2-2_cCy5	\N	\N
13099	original/PLATE-1A/bPLATE_wC11_s9_cRGB.png	\N	BLUE
13100	wC11_d3-3_cDAPI	\N	\N
13101	original/PLATE-1A/bPLATE_wC11_s9_cRGB.png	\N	GREEN
13102	wC11_d3-3_cGFP	\N	\N
13103	original/PLATE-1A/bPLATE_wC11_s9_cRGB.png	\N	RED
13104	wC11_d3-3_cCy5	\N	\N
13105	original/PLATE-1A/bPLATE_wB10_s9_cRGB.png	\N	BLUE
13106	wB10_d3-3_cDAPI	\N	\N
13107	original/PLATE-1A/bPLATE_wB10_s9_cRGB.png	\N	GREEN
13108	wB10_d3-3_cGFP	\N	\N
13109	original/PLATE-1A/bPLATE_wB10_s9_cRGB.png	\N	RED
13110	wB10_d3-3_cCy5	\N	\N
13111	original/PLATE-1A/bPLATE_wE5_s6_cRGB.png	\N	BLUE
13112	wE5_d3-2_cDAPI	\N	\N
13113	original/PLATE-1A/bPLATE_wE5_s6_cRGB.png	\N	GREEN
13114	wE5_d3-2_cGFP	\N	\N
13115	original/PLATE-1A/bPLATE_wE5_s6_cRGB.png	\N	RED
13116	wE5_d3-2_cCy5	\N	\N
13117	original/PLATE-1A/bPLATE_wF6_s6_cRGB.png	\N	BLUE
13118	wF6_d3-2_cDAPI	\N	\N
13119	original/PLATE-1A/bPLATE_wF6_s6_cRGB.png	\N	GREEN
13120	wF6_d3-2_cGFP	\N	\N
13121	original/PLATE-1A/bPLATE_wF6_s6_cRGB.png	\N	RED
13122	wF6_d3-2_cCy5	\N	\N
13123	original/PLATE-1A/bPLATE_wG12_s3_cRGB.png	\N	BLUE
13124	wG12_d3-1_cDAPI	\N	\N
13125	original/PLATE-1A/bPLATE_wG12_s3_cRGB.png	\N	GREEN
13126	wG12_d3-1_cGFP	\N	\N
13127	original/PLATE-1A/bPLATE_wG12_s3_cRGB.png	\N	RED
13128	wG12_d3-1_cCy5	\N	\N
13129	original/PLATE-1A/bPLATE_wD11_s2_cRGB.png	\N	BLUE
13130	wD11_d2-1_cDAPI	\N	\N
13131	original/PLATE-1A/bPLATE_wD11_s2_cRGB.png	\N	GREEN
13132	wD11_d2-1_cGFP	\N	\N
13133	original/PLATE-1A/bPLATE_wD11_s2_cRGB.png	\N	RED
13134	wD11_d2-1_cCy5	\N	\N
13135	original/PLATE-1A/bPLATE_wB4_s5_cRGB.png	\N	BLUE
13136	wB4_d2-2_cDAPI	\N	\N
13137	original/PLATE-1A/bPLATE_wB4_s5_cRGB.png	\N	GREEN
13138	wB4_d2-2_cGFP	\N	\N
13139	original/PLATE-1A/bPLATE_wB4_s5_cRGB.png	\N	RED
13140	wB4_d2-2_cCy5	\N	\N
13141	original/PLATE-1A/bPLATE_wB8_s5_cRGB.png	\N	BLUE
13142	wB8_d2-2_cDAPI	\N	\N
13143	original/PLATE-1A/bPLATE_wB8_s5_cRGB.png	\N	GREEN
13144	wB8_d2-2_cGFP	\N	\N
13145	original/PLATE-1A/bPLATE_wB8_s5_cRGB.png	\N	RED
13146	wB8_d2-2_cCy5	\N	\N
13147	original/PLATE-1A/bPLATE_wE3_s2_cRGB.png	\N	BLUE
13148	wE3_d2-1_cDAPI	\N	\N
13149	original/PLATE-1A/bPLATE_wE3_s2_cRGB.png	\N	GREEN
13150	wE3_d2-1_cGFP	\N	\N
13151	original/PLATE-1A/bPLATE_wE3_s2_cRGB.png	\N	RED
13152	wE3_d2-1_cCy5	\N	\N
13153	original/PLATE-1A/bPLATE_wB6_s4_cRGB.png	\N	BLUE
13154	wB6_d1-2_cDAPI	\N	\N
13155	original/PLATE-1A/bPLATE_wB6_s4_cRGB.png	\N	GREEN
13156	wB6_d1-2_cGFP	\N	\N
13157	original/PLATE-1A/bPLATE_wB6_s4_cRGB.png	\N	RED
13158	wB6_d1-2_cCy5	\N	\N
13159	original/PLATE-1A/bPLATE_wE1_s1_cRGB.png	\N	BLUE
13160	wE1_d1-1_cDAPI	\N	\N
13161	original/PLATE-1A/bPLATE_wE1_s1_cRGB.png	\N	GREEN
13162	wE1_d1-1_cGFP	\N	\N
13163	original/PLATE-1A/bPLATE_wE1_s1_cRGB.png	\N	RED
13164	wE1_d1-1_cCy5	\N	\N
13165	original/PLATE-1A/bPLATE_wC11_s7_cRGB.png	\N	BLUE
13166	wC11_d1-3_cDAPI	\N	\N
13167	original/PLATE-1A/bPLATE_wC11_s7_cRGB.png	\N	GREEN
13168	wC11_d1-3_cGFP	\N	\N
13169	original/PLATE-1A/bPLATE_wC11_s7_cRGB.png	\N	RED
13170	wC11_d1-3_cCy5	\N	\N
13171	original/PLATE-1A/bPLATE_wF6_s4_cRGB.png	\N	BLUE
13172	wF6_d1-2_cDAPI	\N	\N
13173	original/PLATE-1A/bPLATE_wF6_s4_cRGB.png	\N	GREEN
13174	wF6_d1-2_cGFP	\N	\N
13175	original/PLATE-1A/bPLATE_wF6_s4_cRGB.png	\N	RED
13176	wF6_d1-2_cCy5	\N	\N
13177	original/PLATE-1A/bPLATE_wF10_s7_cRGB.png	\N	BLUE
13178	wF10_d1-3_cDAPI	\N	\N
13179	original/PLATE-1A/bPLATE_wF10_s7_cRGB.png	\N	GREEN
13180	wF10_d1-3_cGFP	\N	\N
13181	original/PLATE-1A/bPLATE_wF10_s7_cRGB.png	\N	RED
13182	wF10_d1-3_cCy5	\N	\N
13183	original/PLATE-1A/bPLATE_wD7_s9_cRGB.png	\N	BLUE
13184	wD7_d3-3_cDAPI	\N	\N
13185	original/PLATE-1A/bPLATE_wD7_s9_cRGB.png	\N	GREEN
13186	wD7_d3-3_cGFP	\N	\N
13187	original/PLATE-1A/bPLATE_wD7_s9_cRGB.png	\N	RED
13188	wD7_d3-3_cCy5	\N	\N
13189	original/PLATE-1A/bPLATE_wG2_s6_cRGB.png	\N	BLUE
13190	wG2_d3-2_cDAPI	\N	\N
13191	original/PLATE-1A/bPLATE_wG2_s6_cRGB.png	\N	GREEN
13192	wG2_d3-2_cGFP	\N	\N
13193	original/PLATE-1A/bPLATE_wG2_s6_cRGB.png	\N	RED
13194	wG2_d3-2_cCy5	\N	\N
13195	original/PLATE-1A/bPLATE_wH8_s4_cRGB.png	\N	BLUE
13196	wH8_d1-2_cDAPI	\N	\N
13197	original/PLATE-1A/bPLATE_wH8_s4_cRGB.png	\N	GREEN
13198	wH8_d1-2_cGFP	\N	\N
13199	original/PLATE-1A/bPLATE_wH8_s4_cRGB.png	\N	RED
13200	wH8_d1-2_cCy5	\N	\N
13201	original/PLATE-1A/bPLATE_wF1_s7_cRGB.png	\N	BLUE
13202	wF1_d1-3_cDAPI	\N	\N
13203	original/PLATE-1A/bPLATE_wF1_s7_cRGB.png	\N	GREEN
13204	wF1_d1-3_cGFP	\N	\N
13205	original/PLATE-1A/bPLATE_wF1_s7_cRGB.png	\N	RED
13206	wF1_d1-3_cCy5	\N	\N
13207	original/PLATE-1A/bPLATE_wA1_s3_cRGB.png	\N	BLUE
13208	wA1_d3-1_cDAPI	\N	\N
13209	original/PLATE-1A/bPLATE_wA1_s3_cRGB.png	\N	GREEN
13210	wA1_d3-1_cGFP	\N	\N
13211	original/PLATE-1A/bPLATE_wA1_s3_cRGB.png	\N	RED
13212	wA1_d3-1_cCy5	\N	\N
13213	original/PLATE-1A/bPLATE_wB5_s1_cRGB.png	\N	BLUE
13214	wB5_d1-1_cDAPI	\N	\N
13215	original/PLATE-1A/bPLATE_wB5_s1_cRGB.png	\N	GREEN
13216	wB5_d1-1_cGFP	\N	\N
13217	original/PLATE-1A/bPLATE_wB5_s1_cRGB.png	\N	RED
13218	wB5_d1-1_cCy5	\N	\N
13219	original/PLATE-1A/bPLATE_wH6_s7_cRGB.png	\N	BLUE
13220	wH6_d1-3_cDAPI	\N	\N
13221	original/PLATE-1A/bPLATE_wH6_s7_cRGB.png	\N	GREEN
13222	wH6_d1-3_cGFP	\N	\N
13223	original/PLATE-1A/bPLATE_wH6_s7_cRGB.png	\N	RED
13224	wH6_d1-3_cCy5	\N	\N
13225	original/PLATE-1A/bPLATE_wH5_s5_cRGB.png	\N	BLUE
13226	wH5_d2-2_cDAPI	\N	\N
13227	original/PLATE-1A/bPLATE_wH5_s5_cRGB.png	\N	GREEN
13228	wH5_d2-2_cGFP	\N	\N
13229	original/PLATE-1A/bPLATE_wH5_s5_cRGB.png	\N	RED
13230	wH5_d2-2_cCy5	\N	\N
13231	original/PLATE-1A/bPLATE_wH12_s4_cRGB.png	\N	BLUE
13232	wH12_d1-2_cDAPI	\N	\N
13233	original/PLATE-1A/bPLATE_wH12_s4_cRGB.png	\N	GREEN
13234	wH12_d1-2_cGFP	\N	\N
13235	original/PLATE-1A/bPLATE_wH12_s4_cRGB.png	\N	RED
13236	wH12_d1-2_cCy5	\N	\N
13237	original/PLATE-1A/bPLATE_wE10_s8_cRGB.png	\N	BLUE
13238	wE10_d2-3_cDAPI	\N	\N
13239	original/PLATE-1A/bPLATE_wE10_s8_cRGB.png	\N	GREEN
13240	wE10_d2-3_cGFP	\N	\N
13241	original/PLATE-1A/bPLATE_wE10_s8_cRGB.png	\N	RED
13242	wE10_d2-3_cCy5	\N	\N
13243	original/PLATE-1A/bPLATE_wF5_s7_cRGB.png	\N	BLUE
13244	wF5_d1-3_cDAPI	\N	\N
13245	original/PLATE-1A/bPLATE_wF5_s7_cRGB.png	\N	GREEN
13246	wF5_d1-3_cGFP	\N	\N
13247	original/PLATE-1A/bPLATE_wF5_s7_cRGB.png	\N	RED
13248	wF5_d1-3_cCy5	\N	\N
13249	original/PLATE-1A/bPLATE_wC5_s1_cRGB.png	\N	BLUE
13250	wC5_d1-1_cDAPI	\N	\N
13251	original/PLATE-1A/bPLATE_wC5_s1_cRGB.png	\N	GREEN
13252	wC5_d1-1_cGFP	\N	\N
13253	original/PLATE-1A/bPLATE_wC5_s1_cRGB.png	\N	RED
13254	wC5_d1-1_cCy5	\N	\N
13255	original/PLATE-1A/bPLATE_wC3_s6_cRGB.png	\N	BLUE
13256	wC3_d3-2_cDAPI	\N	\N
13257	original/PLATE-1A/bPLATE_wC3_s6_cRGB.png	\N	GREEN
13258	wC3_d3-2_cGFP	\N	\N
13259	original/PLATE-1A/bPLATE_wC3_s6_cRGB.png	\N	RED
13260	wC3_d3-2_cCy5	\N	\N
13261	original/PLATE-1A/bPLATE_wE10_s3_cRGB.png	\N	BLUE
13262	wE10_d3-1_cDAPI	\N	\N
13263	original/PLATE-1A/bPLATE_wE10_s3_cRGB.png	\N	GREEN
13264	wE10_d3-1_cGFP	\N	\N
13265	original/PLATE-1A/bPLATE_wE10_s3_cRGB.png	\N	RED
13266	wE10_d3-1_cCy5	\N	\N
13267	original/PLATE-1A/bPLATE_wC9_s4_cRGB.png	\N	BLUE
13268	wC9_d1-2_cDAPI	\N	\N
13269	original/PLATE-1A/bPLATE_wC9_s4_cRGB.png	\N	GREEN
13270	wC9_d1-2_cGFP	\N	\N
13271	original/PLATE-1A/bPLATE_wC9_s4_cRGB.png	\N	RED
13272	wC9_d1-2_cCy5	\N	\N
13273	original/PLATE-1A/bPLATE_wA2_s7_cRGB.png	\N	BLUE
13274	wA2_d1-3_cDAPI	\N	\N
13275	original/PLATE-1A/bPLATE_wA2_s7_cRGB.png	\N	GREEN
13276	wA2_d1-3_cGFP	\N	\N
13277	original/PLATE-1A/bPLATE_wA2_s7_cRGB.png	\N	RED
13278	wA2_d1-3_cCy5	\N	\N
13279	original/PLATE-1A/bPLATE_wF4_s1_cRGB.png	\N	BLUE
13280	wF4_d1-1_cDAPI	\N	\N
13281	original/PLATE-1A/bPLATE_wF4_s1_cRGB.png	\N	GREEN
13282	wF4_d1-1_cGFP	\N	\N
13283	original/PLATE-1A/bPLATE_wF4_s1_cRGB.png	\N	RED
13284	wF4_d1-1_cCy5	\N	\N
13285	original/PLATE-1A/bPLATE_wC2_s2_cRGB.png	\N	BLUE
13286	wC2_d2-1_cDAPI	\N	\N
13287	original/PLATE-1A/bPLATE_wC2_s2_cRGB.png	\N	GREEN
13288	wC2_d2-1_cGFP	\N	\N
13289	original/PLATE-1A/bPLATE_wC2_s2_cRGB.png	\N	RED
13290	wC2_d2-1_cCy5	\N	\N
13291	original/PLATE-1A/bPLATE_wB9_s4_cRGB.png	\N	BLUE
13292	wB9_d1-2_cDAPI	\N	\N
13293	original/PLATE-1A/bPLATE_wB9_s4_cRGB.png	\N	GREEN
13294	wB9_d1-2_cGFP	\N	\N
13295	original/PLATE-1A/bPLATE_wB9_s4_cRGB.png	\N	RED
13296	wB9_d1-2_cCy5	\N	\N
13297	original/PLATE-1A/bPLATE_wE4_s1_cRGB.png	\N	BLUE
13298	wE4_d1-1_cDAPI	\N	\N
13299	original/PLATE-1A/bPLATE_wE4_s1_cRGB.png	\N	GREEN
13300	wE4_d1-1_cGFP	\N	\N
13301	original/PLATE-1A/bPLATE_wE4_s1_cRGB.png	\N	RED
13302	wE4_d1-1_cCy5	\N	\N
13303	original/PLATE-1A/bPLATE_wD1_s4_cRGB.png	\N	BLUE
13304	wD1_d1-2_cDAPI	\N	\N
13305	original/PLATE-1A/bPLATE_wD1_s4_cRGB.png	\N	GREEN
13306	wD1_d1-2_cGFP	\N	\N
13307	original/PLATE-1A/bPLATE_wD1_s4_cRGB.png	\N	RED
13308	wD1_d1-2_cCy5	\N	\N
13309	original/PLATE-1A/bPLATE_wA6_s7_cRGB.png	\N	BLUE
13310	wA6_d1-3_cDAPI	\N	\N
13311	original/PLATE-1A/bPLATE_wA6_s7_cRGB.png	\N	GREEN
13312	wA6_d1-3_cGFP	\N	\N
13313	original/PLATE-1A/bPLATE_wA6_s7_cRGB.png	\N	RED
13314	wA6_d1-3_cCy5	\N	\N
13315	original/PLATE-1A/bPLATE_wB2_s8_cRGB.png	\N	BLUE
13316	wB2_d2-3_cDAPI	\N	\N
13317	original/PLATE-1A/bPLATE_wB2_s8_cRGB.png	\N	GREEN
13318	wB2_d2-3_cGFP	\N	\N
13319	original/PLATE-1A/bPLATE_wB2_s8_cRGB.png	\N	RED
13320	wB2_d2-3_cCy5	\N	\N
13321	original/PLATE-1A/bPLATE_wD9_s5_cRGB.png	\N	BLUE
13322	wD9_d2-2_cDAPI	\N	\N
13323	original/PLATE-1A/bPLATE_wD9_s5_cRGB.png	\N	GREEN
13324	wD9_d2-2_cGFP	\N	\N
13325	original/PLATE-1A/bPLATE_wD9_s5_cRGB.png	\N	RED
13326	wD9_d2-2_cCy5	\N	\N
13327	original/PLATE-1A/bPLATE_wF8_s1_cRGB.png	\N	BLUE
13328	wF8_d1-1_cDAPI	\N	\N
13329	original/PLATE-1A/bPLATE_wF8_s1_cRGB.png	\N	GREEN
13330	wF8_d1-1_cGFP	\N	\N
13331	original/PLATE-1A/bPLATE_wF8_s1_cRGB.png	\N	RED
13332	wF8_d1-1_cCy5	\N	\N
13333	original/PLATE-1A/bPLATE_wG4_s2_cRGB.png	\N	BLUE
13334	wG4_d2-1_cDAPI	\N	\N
13335	original/PLATE-1A/bPLATE_wG4_s2_cRGB.png	\N	GREEN
13336	wG4_d2-1_cGFP	\N	\N
13337	original/PLATE-1A/bPLATE_wG4_s2_cRGB.png	\N	RED
13338	wG4_d2-1_cCy5	\N	\N
13339	original/PLATE-1A/bPLATE_wD5_s9_cRGB.png	\N	BLUE
13340	wD5_d3-3_cDAPI	\N	\N
13341	original/PLATE-1A/bPLATE_wD5_s9_cRGB.png	\N	GREEN
13342	wD5_d3-3_cGFP	\N	\N
13343	original/PLATE-1A/bPLATE_wD5_s9_cRGB.png	\N	RED
13344	wD5_d3-3_cCy5	\N	\N
13345	original/PLATE-1A/bPLATE_wF12_s6_cRGB.png	\N	BLUE
13346	wF12_d3-2_cDAPI	\N	\N
13347	original/PLATE-1A/bPLATE_wF12_s6_cRGB.png	\N	GREEN
13348	wF12_d3-2_cGFP	\N	\N
13349	original/PLATE-1A/bPLATE_wF12_s6_cRGB.png	\N	RED
13350	wF12_d3-2_cCy5	\N	\N
13351	original/PLATE-1A/bPLATE_wD2_s9_cRGB.png	\N	BLUE
13352	wD2_d3-3_cDAPI	\N	\N
13353	original/PLATE-1A/bPLATE_wD2_s9_cRGB.png	\N	GREEN
13354	wD2_d3-3_cGFP	\N	\N
13355	original/PLATE-1A/bPLATE_wD2_s9_cRGB.png	\N	RED
13356	wD2_d3-3_cCy5	\N	\N
13357	original/PLATE-1A/bPLATE_wF9_s6_cRGB.png	\N	BLUE
13358	wF9_d3-2_cDAPI	\N	\N
13359	original/PLATE-1A/bPLATE_wF9_s6_cRGB.png	\N	GREEN
13360	wF9_d3-2_cGFP	\N	\N
13361	original/PLATE-1A/bPLATE_wF9_s6_cRGB.png	\N	RED
13362	wF9_d3-2_cCy5	\N	\N
13363	original/PLATE-1A/bPLATE_wB10_s2_cRGB.png	\N	BLUE
13364	wB10_d2-1_cDAPI	\N	\N
13365	original/PLATE-1A/bPLATE_wB10_s2_cRGB.png	\N	GREEN
13366	wB10_d2-1_cGFP	\N	\N
13367	original/PLATE-1A/bPLATE_wB10_s2_cRGB.png	\N	RED
13368	wB10_d2-1_cCy5	\N	\N
13369	original/PLATE-1A/bPLATE_wA6_s2_cRGB.png	\N	BLUE
13370	wA6_d2-1_cDAPI	\N	\N
13371	original/PLATE-1A/bPLATE_wA6_s2_cRGB.png	\N	GREEN
13372	wA6_d2-1_cGFP	\N	\N
13373	original/PLATE-1A/bPLATE_wA6_s2_cRGB.png	\N	RED
13374	wA6_d2-1_cCy5	\N	\N
13375	original/PLATE-1A/bPLATE_wB1_s1_cRGB.png	\N	BLUE
13376	wB1_d1-1_cDAPI	\N	\N
13377	original/PLATE-1A/bPLATE_wB1_s1_cRGB.png	\N	GREEN
13378	wB1_d1-1_cGFP	\N	\N
13379	original/PLATE-1A/bPLATE_wB1_s1_cRGB.png	\N	RED
13380	wB1_d1-1_cCy5	\N	\N
13381	original/PLATE-1A/bPLATE_wE5_s9_cRGB.png	\N	BLUE
13382	wE5_d3-3_cDAPI	\N	\N
13383	original/PLATE-1A/bPLATE_wE5_s9_cRGB.png	\N	GREEN
13384	wE5_d3-3_cGFP	\N	\N
13385	original/PLATE-1A/bPLATE_wE5_s9_cRGB.png	\N	RED
13386	wE5_d3-3_cCy5	\N	\N
13387	original/PLATE-1A/bPLATE_wG12_s6_cRGB.png	\N	BLUE
13388	wG12_d3-2_cDAPI	\N	\N
13389	original/PLATE-1A/bPLATE_wG12_s6_cRGB.png	\N	GREEN
13390	wG12_d3-2_cGFP	\N	\N
13391	original/PLATE-1A/bPLATE_wG12_s6_cRGB.png	\N	RED
13392	wG12_d3-2_cCy5	\N	\N
13393	original/PLATE-1A/bPLATE_wG5_s7_cRGB.png	\N	BLUE
13394	wG5_d1-3_cDAPI	\N	\N
13395	original/PLATE-1A/bPLATE_wG5_s7_cRGB.png	\N	GREEN
13396	wG5_d1-3_cGFP	\N	\N
13397	original/PLATE-1A/bPLATE_wG5_s7_cRGB.png	\N	RED
13398	wG5_d1-3_cCy5	\N	\N
13399	original/PLATE-1A/bPLATE_wC12_s5_cRGB.png	\N	BLUE
13400	wC12_d2-2_cDAPI	\N	\N
13401	original/PLATE-1A/bPLATE_wC12_s5_cRGB.png	\N	GREEN
13402	wC12_d2-2_cGFP	\N	\N
13403	original/PLATE-1A/bPLATE_wC12_s5_cRGB.png	\N	RED
13404	wC12_d2-2_cCy5	\N	\N
13405	original/PLATE-1A/bPLATE_wA5_s8_cRGB.png	\N	BLUE
13406	wA5_d2-3_cDAPI	\N	\N
13407	original/PLATE-1A/bPLATE_wA5_s8_cRGB.png	\N	GREEN
13408	wA5_d2-3_cGFP	\N	\N
13409	original/PLATE-1A/bPLATE_wA5_s8_cRGB.png	\N	RED
13410	wA5_d2-3_cCy5	\N	\N
13411	original/PLATE-1A/bPLATE_wF7_s2_cRGB.png	\N	BLUE
13412	wF7_d2-1_cDAPI	\N	\N
13413	original/PLATE-1A/bPLATE_wF7_s2_cRGB.png	\N	GREEN
13414	wF7_d2-1_cGFP	\N	\N
13415	original/PLATE-1A/bPLATE_wF7_s2_cRGB.png	\N	RED
13416	wF7_d2-1_cCy5	\N	\N
13417	original/PLATE-1A/bPLATE_wA10_s1_cRGB.png	\N	BLUE
13418	wA10_d1-1_cDAPI	\N	\N
13419	original/PLATE-1A/bPLATE_wA10_s1_cRGB.png	\N	GREEN
13420	wA10_d1-1_cGFP	\N	\N
13421	original/PLATE-1A/bPLATE_wA10_s1_cRGB.png	\N	RED
13422	wA10_d1-1_cCy5	\N	\N
13423	original/PLATE-1A/bPLATE_wB7_s5_cRGB.png	\N	BLUE
13424	wB7_d2-2_cDAPI	\N	\N
13425	original/PLATE-1A/bPLATE_wB7_s5_cRGB.png	\N	GREEN
13426	wB7_d2-2_cGFP	\N	\N
13427	original/PLATE-1A/bPLATE_wB7_s5_cRGB.png	\N	RED
13428	wB7_d2-2_cCy5	\N	\N
13429	original/PLATE-1A/bPLATE_wE2_s2_cRGB.png	\N	BLUE
13430	wE2_d2-1_cDAPI	\N	\N
13431	original/PLATE-1A/bPLATE_wE2_s2_cRGB.png	\N	GREEN
13432	wE2_d2-1_cGFP	\N	\N
13433	original/PLATE-1A/bPLATE_wE2_s2_cRGB.png	\N	RED
13434	wE2_d2-1_cCy5	\N	\N
13435	original/PLATE-1A/bPLATE_wH11_s5_cRGB.png	\N	BLUE
13436	wH11_d2-2_cDAPI	\N	\N
13437	original/PLATE-1A/bPLATE_wH11_s5_cRGB.png	\N	GREEN
13438	wH11_d2-2_cGFP	\N	\N
13439	original/PLATE-1A/bPLATE_wH11_s5_cRGB.png	\N	RED
13440	wH11_d2-2_cCy5	\N	\N
13441	original/PLATE-1A/bPLATE_wF4_s8_cRGB.png	\N	BLUE
13442	wF4_d2-3_cDAPI	\N	\N
13443	original/PLATE-1A/bPLATE_wF4_s8_cRGB.png	\N	GREEN
13444	wF4_d2-3_cGFP	\N	\N
13445	original/PLATE-1A/bPLATE_wF4_s8_cRGB.png	\N	RED
13446	wF4_d2-3_cCy5	\N	\N
13447	original/PLATE-1A/bPLATE_wF7_s7_cRGB.png	\N	BLUE
13448	wF7_d1-3_cDAPI	\N	\N
13449	original/PLATE-1A/bPLATE_wF7_s7_cRGB.png	\N	GREEN
13450	wF7_d1-3_cGFP	\N	\N
13451	original/PLATE-1A/bPLATE_wF7_s7_cRGB.png	\N	RED
13452	wF7_d1-3_cCy5	\N	\N
13453	original/PLATE-1A/bPLATE_wA5_s1_cRGB.png	\N	BLUE
13454	wA5_d1-1_cDAPI	\N	\N
13455	original/PLATE-1A/bPLATE_wA5_s1_cRGB.png	\N	GREEN
13456	wA5_d1-1_cGFP	\N	\N
13457	original/PLATE-1A/bPLATE_wA5_s1_cRGB.png	\N	RED
13458	wA5_d1-1_cCy5	\N	\N
13459	original/PLATE-1A/bPLATE_wD10_s8_cRGB.png	\N	BLUE
13460	wD10_d2-3_cDAPI	\N	\N
13461	original/PLATE-1A/bPLATE_wD10_s8_cRGB.png	\N	GREEN
13462	wD10_d2-3_cGFP	\N	\N
13463	original/PLATE-1A/bPLATE_wD10_s8_cRGB.png	\N	RED
13464	wD10_d2-3_cCy5	\N	\N
13465	original/PLATE-1A/bPLATE_wG5_s5_cRGB.png	\N	BLUE
13466	wG5_d2-2_cDAPI	\N	\N
13467	original/PLATE-1A/bPLATE_wG5_s5_cRGB.png	\N	GREEN
13468	wG5_d2-2_cGFP	\N	\N
13469	original/PLATE-1A/bPLATE_wG5_s5_cRGB.png	\N	RED
13470	wG5_d2-2_cCy5	\N	\N
13471	original/PLATE-1A/bPLATE_wC3_s2_cRGB.png	\N	BLUE
13472	wC3_d2-1_cDAPI	\N	\N
13473	original/PLATE-1A/bPLATE_wC3_s2_cRGB.png	\N	GREEN
13474	wC3_d2-1_cGFP	\N	\N
13475	original/PLATE-1A/bPLATE_wC3_s2_cRGB.png	\N	RED
13476	wC3_d2-1_cCy5	\N	\N
13477	original/PLATE-1A/bPLATE_wB1_s3_cRGB.png	\N	BLUE
13478	wB1_d3-1_cDAPI	\N	\N
13479	original/PLATE-1A/bPLATE_wB1_s3_cRGB.png	\N	GREEN
13480	wB1_d3-1_cGFP	\N	\N
13481	original/PLATE-1A/bPLATE_wB1_s3_cRGB.png	\N	RED
13482	wB1_d3-1_cCy5	\N	\N
13483	original/PLATE-1A/bPLATE_wH11_s6_cRGB.png	\N	BLUE
13484	wH11_d3-2_cDAPI	\N	\N
13485	original/PLATE-1A/bPLATE_wH11_s6_cRGB.png	\N	GREEN
13486	wH11_d3-2_cGFP	\N	\N
13487	original/PLATE-1A/bPLATE_wH11_s6_cRGB.png	\N	RED
13488	wH11_d3-2_cCy5	\N	\N
13489	original/PLATE-1A/bPLATE_wF4_s9_cRGB.png	\N	BLUE
13490	wF4_d3-3_cDAPI	\N	\N
13491	original/PLATE-1A/bPLATE_wF4_s9_cRGB.png	\N	GREEN
13492	wF4_d3-3_cGFP	\N	\N
13493	original/PLATE-1A/bPLATE_wF4_s9_cRGB.png	\N	RED
13494	wF4_d3-3_cCy5	\N	\N
13495	original/PLATE-1A/bPLATE_wA4_s1_cRGB.png	\N	BLUE
13496	wA4_d1-1_cDAPI	\N	\N
13497	original/PLATE-1A/bPLATE_wA4_s1_cRGB.png	\N	GREEN
13498	wA4_d1-1_cGFP	\N	\N
13499	original/PLATE-1A/bPLATE_wA4_s1_cRGB.png	\N	RED
13500	wA4_d1-1_cCy5	\N	\N
13501	original/PLATE-1A/bPLATE_wD9_s9_cRGB.png	\N	BLUE
13502	wD9_d3-3_cDAPI	\N	\N
13503	original/PLATE-1A/bPLATE_wD9_s9_cRGB.png	\N	GREEN
13504	wD9_d3-3_cGFP	\N	\N
13505	original/PLATE-1A/bPLATE_wD9_s9_cRGB.png	\N	RED
13506	wD9_d3-3_cCy5	\N	\N
13507	original/PLATE-1A/bPLATE_wG4_s6_cRGB.png	\N	BLUE
13508	wG4_d3-2_cDAPI	\N	\N
13509	original/PLATE-1A/bPLATE_wG4_s6_cRGB.png	\N	GREEN
13510	wG4_d3-2_cGFP	\N	\N
13511	original/PLATE-1A/bPLATE_wG4_s6_cRGB.png	\N	RED
13512	wG4_d3-2_cCy5	\N	\N
13513	original/PLATE-1A/bPLATE_wG3_s8_cRGB.png	\N	BLUE
13514	wG3_d2-3_cDAPI	\N	\N
13515	original/PLATE-1A/bPLATE_wG3_s8_cRGB.png	\N	GREEN
13516	wG3_d2-3_cGFP	\N	\N
13517	original/PLATE-1A/bPLATE_wG3_s8_cRGB.png	\N	RED
13518	wG3_d2-3_cCy5	\N	\N
13519	original/PLATE-1A/bPLATE_wG1_s9_cRGB.png	\N	BLUE
13520	wG1_d3-3_cDAPI	\N	\N
13521	original/PLATE-1A/bPLATE_wG1_s9_cRGB.png	\N	GREEN
13522	wG1_d3-3_cGFP	\N	\N
13523	original/PLATE-1A/bPLATE_wG1_s9_cRGB.png	\N	RED
13524	wG1_d3-3_cCy5	\N	\N
13525	original/PLATE-1A/bPLATE_wH3_s9_cRGB.png	\N	BLUE
13526	wH3_d3-3_cDAPI	\N	\N
13527	original/PLATE-1A/bPLATE_wH3_s9_cRGB.png	\N	GREEN
13528	wH3_d3-3_cGFP	\N	\N
13529	original/PLATE-1A/bPLATE_wH3_s9_cRGB.png	\N	RED
13530	wH3_d3-3_cCy5	\N	\N
13531	original/PLATE-1A/bPLATE_wD1_s7_cRGB.png	\N	BLUE
13532	wD1_d1-3_cDAPI	\N	\N
13533	original/PLATE-1A/bPLATE_wD1_s7_cRGB.png	\N	GREEN
13534	wD1_d1-3_cGFP	\N	\N
13535	original/PLATE-1A/bPLATE_wD1_s7_cRGB.png	\N	RED
13536	wD1_d1-3_cCy5	\N	\N
13537	original/PLATE-1A/bPLATE_wC5_s6_cRGB.png	\N	BLUE
13538	wC5_d3-2_cDAPI	\N	\N
13539	original/PLATE-1A/bPLATE_wC5_s6_cRGB.png	\N	GREEN
13540	wC5_d3-2_cGFP	\N	\N
13541	original/PLATE-1A/bPLATE_wC5_s6_cRGB.png	\N	RED
13542	wC5_d3-2_cCy5	\N	\N
13543	original/PLATE-1A/bPLATE_wB6_s8_cRGB.png	\N	BLUE
13544	wB6_d2-3_cDAPI	\N	\N
13545	original/PLATE-1A/bPLATE_wB6_s8_cRGB.png	\N	GREEN
13546	wB6_d2-3_cGFP	\N	\N
13547	original/PLATE-1A/bPLATE_wB6_s8_cRGB.png	\N	RED
13548	wB6_d2-3_cCy5	\N	\N
13549	original/PLATE-1A/bPLATE_wE12_s3_cRGB.png	\N	BLUE
13550	wE12_d3-1_cDAPI	\N	\N
13551	original/PLATE-1A/bPLATE_wE12_s3_cRGB.png	\N	GREEN
13552	wE12_d3-1_cGFP	\N	\N
13553	original/PLATE-1A/bPLATE_wE12_s3_cRGB.png	\N	RED
13554	wE12_d3-1_cCy5	\N	\N
13555	original/PLATE-1A/bPLATE_wE1_s5_cRGB.png	\N	BLUE
13556	wE1_d2-2_cDAPI	\N	\N
13557	original/PLATE-1A/bPLATE_wE1_s5_cRGB.png	\N	GREEN
13558	wE1_d2-2_cGFP	\N	\N
13559	original/PLATE-1A/bPLATE_wE1_s5_cRGB.png	\N	RED
13560	wE1_d2-2_cCy5	\N	\N
13561	original/PLATE-1A/bPLATE_wF8_s4_cRGB.png	\N	BLUE
13562	wF8_d1-2_cDAPI	\N	\N
13563	original/PLATE-1A/bPLATE_wF8_s4_cRGB.png	\N	GREEN
13564	wF8_d1-2_cGFP	\N	\N
13565	original/PLATE-1A/bPLATE_wF8_s4_cRGB.png	\N	RED
13566	wF8_d1-2_cCy5	\N	\N
13567	original/PLATE-1A/bPLATE_wG8_s2_cRGB.png	\N	BLUE
13568	wG8_d2-1_cDAPI	\N	\N
13569	original/PLATE-1A/bPLATE_wG8_s2_cRGB.png	\N	GREEN
13570	wG8_d2-1_cGFP	\N	\N
13571	original/PLATE-1A/bPLATE_wG8_s2_cRGB.png	\N	RED
13572	wG8_d2-1_cCy5	\N	\N
13573	original/PLATE-1A/bPLATE_wB2_s7_cRGB.png	\N	BLUE
13574	wB2_d1-3_cDAPI	\N	\N
13575	original/PLATE-1A/bPLATE_wB2_s7_cRGB.png	\N	GREEN
13576	wB2_d1-3_cGFP	\N	\N
13577	original/PLATE-1A/bPLATE_wB2_s7_cRGB.png	\N	RED
13578	wB2_d1-3_cCy5	\N	\N
13579	original/PLATE-1A/bPLATE_wD9_s4_cRGB.png	\N	BLUE
13580	wD9_d1-2_cDAPI	\N	\N
13581	original/PLATE-1A/bPLATE_wD9_s4_cRGB.png	\N	GREEN
13582	wD9_d1-2_cGFP	\N	\N
13583	original/PLATE-1A/bPLATE_wD9_s4_cRGB.png	\N	RED
13584	wD9_d1-2_cCy5	\N	\N
13585	original/PLATE-1A/bPLATE_wG4_s1_cRGB.png	\N	BLUE
13586	wG4_d1-1_cDAPI	\N	\N
13587	original/PLATE-1A/bPLATE_wG4_s1_cRGB.png	\N	GREEN
13588	wG4_d1-1_cGFP	\N	\N
13589	original/PLATE-1A/bPLATE_wG4_s1_cRGB.png	\N	RED
13590	wG4_d1-1_cCy5	\N	\N
13591	original/PLATE-1A/bPLATE_wD11_s4_cRGB.png	\N	BLUE
13592	wD11_d1-2_cDAPI	\N	\N
13593	original/PLATE-1A/bPLATE_wD11_s4_cRGB.png	\N	GREEN
13594	wD11_d1-2_cGFP	\N	\N
13595	original/PLATE-1A/bPLATE_wD11_s4_cRGB.png	\N	RED
13596	wD11_d1-2_cCy5	\N	\N
13597	original/PLATE-1A/bPLATE_wB4_s7_cRGB.png	\N	BLUE
13598	wB4_d1-3_cDAPI	\N	\N
13599	original/PLATE-1A/bPLATE_wB4_s7_cRGB.png	\N	GREEN
13600	wB4_d1-3_cGFP	\N	\N
13601	original/PLATE-1A/bPLATE_wB4_s7_cRGB.png	\N	RED
13602	wB4_d1-3_cCy5	\N	\N
13603	original/PLATE-1A/bPLATE_wG6_s1_cRGB.png	\N	BLUE
13604	wG6_d1-1_cDAPI	\N	\N
13605	original/PLATE-1A/bPLATE_wG6_s1_cRGB.png	\N	GREEN
13606	wG6_d1-1_cGFP	\N	\N
13607	original/PLATE-1A/bPLATE_wG6_s1_cRGB.png	\N	RED
13608	wG6_d1-1_cCy5	\N	\N
13609	original/PLATE-1A/bPLATE_wE1_s8_cRGB.png	\N	BLUE
13610	wE1_d2-3_cDAPI	\N	\N
13611	original/PLATE-1A/bPLATE_wE1_s8_cRGB.png	\N	GREEN
13612	wE1_d2-3_cGFP	\N	\N
13613	original/PLATE-1A/bPLATE_wE1_s8_cRGB.png	\N	RED
13614	wE1_d2-3_cCy5	\N	\N
13615	original/PLATE-1A/bPLATE_wG8_s5_cRGB.png	\N	BLUE
13616	wG8_d2-2_cDAPI	\N	\N
13617	original/PLATE-1A/bPLATE_wG8_s5_cRGB.png	\N	GREEN
13618	wG8_d2-2_cGFP	\N	\N
13619	original/PLATE-1A/bPLATE_wG8_s5_cRGB.png	\N	RED
13620	wG8_d2-2_cCy5	\N	\N
13621	original/PLATE-1A/bPLATE_wA5_s3_cRGB.png	\N	BLUE
13622	wA5_d3-1_cDAPI	\N	\N
13623	original/PLATE-1A/bPLATE_wA5_s3_cRGB.png	\N	GREEN
13624	wA5_d3-1_cGFP	\N	\N
13625	original/PLATE-1A/bPLATE_wA5_s3_cRGB.png	\N	RED
13626	wA5_d3-1_cCy5	\N	\N
13627	original/PLATE-1A/bPLATE_wC9_s9_cRGB.png	\N	BLUE
13628	wC9_d3-3_cDAPI	\N	\N
13629	original/PLATE-1A/bPLATE_wC9_s9_cRGB.png	\N	GREEN
13630	wC9_d3-3_cGFP	\N	\N
13631	original/PLATE-1A/bPLATE_wC9_s9_cRGB.png	\N	RED
13632	wC9_d3-3_cCy5	\N	\N
13633	original/PLATE-1A/bPLATE_wF4_s6_cRGB.png	\N	BLUE
13634	wF4_d3-2_cDAPI	\N	\N
13635	original/PLATE-1A/bPLATE_wF4_s6_cRGB.png	\N	GREEN
13636	wF4_d3-2_cGFP	\N	\N
13637	original/PLATE-1A/bPLATE_wF4_s6_cRGB.png	\N	RED
13638	wF4_d3-2_cCy5	\N	\N
13639	original/PLATE-1A/bPLATE_wH11_s3_cRGB.png	\N	BLUE
13640	wH11_d3-1_cDAPI	\N	\N
13641	original/PLATE-1A/bPLATE_wH11_s3_cRGB.png	\N	GREEN
13642	wH11_d3-1_cGFP	\N	\N
13643	original/PLATE-1A/bPLATE_wH11_s3_cRGB.png	\N	RED
13644	wH11_d3-1_cCy5	\N	\N
13645	original/PLATE-1A/bPLATE_wD5_s8_cRGB.png	\N	BLUE
13646	wD5_d2-3_cDAPI	\N	\N
13647	original/PLATE-1A/bPLATE_wD5_s8_cRGB.png	\N	GREEN
13648	wD5_d2-3_cGFP	\N	\N
13649	original/PLATE-1A/bPLATE_wD5_s8_cRGB.png	\N	RED
13650	wD5_d2-3_cCy5	\N	\N
13651	original/PLATE-1A/bPLATE_wF12_s5_cRGB.png	\N	BLUE
13652	wF12_d2-2_cDAPI	\N	\N
13653	original/PLATE-1A/bPLATE_wF12_s5_cRGB.png	\N	GREEN
13654	wF12_d2-2_cGFP	\N	\N
13655	original/PLATE-1A/bPLATE_wF12_s5_cRGB.png	\N	RED
13656	wF12_d2-2_cCy5	\N	\N
13657	original/PLATE-1A/bPLATE_wA7_s2_cRGB.png	\N	BLUE
13658	wA7_d2-1_cDAPI	\N	\N
13659	original/PLATE-1A/bPLATE_wA7_s2_cRGB.png	\N	GREEN
13660	wA7_d2-1_cGFP	\N	\N
13661	original/PLATE-1A/bPLATE_wA7_s2_cRGB.png	\N	RED
13662	wA7_d2-1_cCy5	\N	\N
13663	original/PLATE-1A/bPLATE_wH9_s1_cRGB.png	\N	BLUE
13664	wH9_d1-1_cDAPI	\N	\N
13665	original/PLATE-1A/bPLATE_wH9_s1_cRGB.png	\N	GREEN
13666	wH9_d1-1_cGFP	\N	\N
13667	original/PLATE-1A/bPLATE_wH9_s1_cRGB.png	\N	RED
13668	wH9_d1-1_cCy5	\N	\N
13669	original/PLATE-1A/bPLATE_wC7_s7_cRGB.png	\N	BLUE
13670	wC7_d1-3_cDAPI	\N	\N
13671	original/PLATE-1A/bPLATE_wC7_s7_cRGB.png	\N	GREEN
13672	wC7_d1-3_cGFP	\N	\N
13673	original/PLATE-1A/bPLATE_wC7_s7_cRGB.png	\N	RED
13674	wC7_d1-3_cCy5	\N	\N
13675	original/PLATE-1A/bPLATE_wF2_s4_cRGB.png	\N	BLUE
13676	wF2_d1-2_cDAPI	\N	\N
13677	original/PLATE-1A/bPLATE_wF2_s4_cRGB.png	\N	GREEN
13678	wF2_d1-2_cGFP	\N	\N
13679	original/PLATE-1A/bPLATE_wF2_s4_cRGB.png	\N	RED
13680	wF2_d1-2_cCy5	\N	\N
13681	original/PLATE-1A/bPLATE_wH2_s5_cRGB.png	\N	BLUE
13682	wH2_d2-2_cDAPI	\N	\N
13683	original/PLATE-1A/bPLATE_wH2_s5_cRGB.png	\N	GREEN
13684	wH2_d2-2_cGFP	\N	\N
13685	original/PLATE-1A/bPLATE_wH2_s5_cRGB.png	\N	RED
13686	wH2_d2-2_cCy5	\N	\N
13687	original/PLATE-1A/bPLATE_wE7_s8_cRGB.png	\N	BLUE
13688	wE7_d2-3_cDAPI	\N	\N
13689	original/PLATE-1A/bPLATE_wE7_s8_cRGB.png	\N	GREEN
13690	wE7_d2-3_cGFP	\N	\N
13691	original/PLATE-1A/bPLATE_wE7_s8_cRGB.png	\N	RED
13692	wE7_d2-3_cCy5	\N	\N
13693	original/PLATE-1A/bPLATE_wA12_s5_cRGB.png	\N	BLUE
13694	wA12_d2-2_cDAPI	\N	\N
13695	original/PLATE-1A/bPLATE_wA12_s5_cRGB.png	\N	GREEN
13696	wA12_d2-2_cGFP	\N	\N
13697	original/PLATE-1A/bPLATE_wA12_s5_cRGB.png	\N	RED
13698	wA12_d2-2_cCy5	\N	\N
13699	original/PLATE-1A/bPLATE_wD7_s2_cRGB.png	\N	BLUE
13700	wD7_d2-1_cDAPI	\N	\N
13701	original/PLATE-1A/bPLATE_wD7_s2_cRGB.png	\N	GREEN
13702	wD7_d2-1_cGFP	\N	\N
13703	original/PLATE-1A/bPLATE_wD7_s2_cRGB.png	\N	RED
13704	wD7_d2-1_cCy5	\N	\N
13705	original/PLATE-1A/bPLATE_wH8_s1_cRGB.png	\N	BLUE
13706	wH8_d1-1_cDAPI	\N	\N
13707	original/PLATE-1A/bPLATE_wH8_s1_cRGB.png	\N	GREEN
13708	wH8_d1-1_cGFP	\N	\N
13709	original/PLATE-1A/bPLATE_wH8_s1_cRGB.png	\N	RED
13710	wH8_d1-1_cCy5	\N	\N
13711	original/PLATE-1A/bPLATE_wC6_s7_cRGB.png	\N	BLUE
13712	wC6_d1-3_cDAPI	\N	\N
13713	original/PLATE-1A/bPLATE_wC6_s7_cRGB.png	\N	GREEN
13714	wC6_d1-3_cGFP	\N	\N
13715	original/PLATE-1A/bPLATE_wC6_s7_cRGB.png	\N	RED
13716	wC6_d1-3_cCy5	\N	\N
13717	original/PLATE-1A/bPLATE_wF1_s4_cRGB.png	\N	BLUE
13718	wF1_d1-2_cDAPI	\N	\N
13719	original/PLATE-1A/bPLATE_wF1_s4_cRGB.png	\N	GREEN
13720	wF1_d1-2_cGFP	\N	\N
13721	original/PLATE-1A/bPLATE_wF1_s4_cRGB.png	\N	RED
13722	wF1_d1-2_cCy5	\N	\N
13723	original/PLATE-1A/bPLATE_wD4_s1_cRGB.png	\N	BLUE
13724	wD4_d1-1_cDAPI	\N	\N
13725	original/PLATE-1A/bPLATE_wD4_s1_cRGB.png	\N	GREEN
13726	wD4_d1-1_cGFP	\N	\N
13727	original/PLATE-1A/bPLATE_wD4_s1_cRGB.png	\N	RED
13728	wD4_d1-1_cCy5	\N	\N
13729	original/PLATE-1A/bPLATE_wA9_s4_cRGB.png	\N	BLUE
13730	wA9_d1-2_cDAPI	\N	\N
13731	original/PLATE-1A/bPLATE_wA9_s4_cRGB.png	\N	GREEN
13732	wA9_d1-2_cGFP	\N	\N
13733	original/PLATE-1A/bPLATE_wA9_s4_cRGB.png	\N	RED
13734	wA9_d1-2_cCy5	\N	\N
13735	original/PLATE-1A/bPLATE_wH11_s8_cRGB.png	\N	BLUE
13736	wH11_d2-3_cDAPI	\N	\N
13737	original/PLATE-1A/bPLATE_wH11_s8_cRGB.png	\N	GREEN
13738	wH11_d2-3_cGFP	\N	\N
13739	original/PLATE-1A/bPLATE_wH11_s8_cRGB.png	\N	RED
13740	wH11_d2-3_cCy5	\N	\N
13741	original/PLATE-1A/bPLATE_wD3_s3_cRGB.png	\N	BLUE
13742	wD3_d3-1_cDAPI	\N	\N
13743	original/PLATE-1A/bPLATE_wD3_s3_cRGB.png	\N	GREEN
13744	wD3_d3-1_cGFP	\N	\N
13745	original/PLATE-1A/bPLATE_wD3_s3_cRGB.png	\N	RED
13746	wD3_d3-1_cCy5	\N	\N
13747	original/PLATE-1A/bPLATE_wA8_s6_cRGB.png	\N	BLUE
13748	wA8_d3-2_cDAPI	\N	\N
13749	original/PLATE-1A/bPLATE_wA8_s6_cRGB.png	\N	GREEN
13750	wA8_d3-2_cGFP	\N	\N
13751	original/PLATE-1A/bPLATE_wA8_s6_cRGB.png	\N	RED
13752	wA8_d3-2_cCy5	\N	\N
13753	original/PLATE-1A/bPLATE_wG11_s8_cRGB.png	\N	BLUE
13754	wG11_d2-3_cDAPI	\N	\N
13755	original/PLATE-1A/bPLATE_wG11_s8_cRGB.png	\N	GREEN
13756	wG11_d2-3_cGFP	\N	\N
13757	original/PLATE-1A/bPLATE_wG11_s8_cRGB.png	\N	RED
13758	wG11_d2-3_cCy5	\N	\N
13759	original/PLATE-1A/bPLATE_wB9_s6_cRGB.png	\N	BLUE
13760	wB9_d3-2_cDAPI	\N	\N
13761	original/PLATE-1A/bPLATE_wB9_s6_cRGB.png	\N	GREEN
13762	wB9_d3-2_cGFP	\N	\N
13763	original/PLATE-1A/bPLATE_wB9_s6_cRGB.png	\N	RED
13764	wB9_d3-2_cCy5	\N	\N
13765	original/PLATE-1A/bPLATE_wE4_s3_cRGB.png	\N	BLUE
13766	wE4_d3-1_cDAPI	\N	\N
13767	original/PLATE-1A/bPLATE_wE4_s3_cRGB.png	\N	GREEN
13768	wE4_d3-1_cGFP	\N	\N
13769	original/PLATE-1A/bPLATE_wE4_s3_cRGB.png	\N	RED
13770	wE4_d3-1_cCy5	\N	\N
13771	original/PLATE-1A/bPLATE_wC6_s2_cRGB.png	\N	BLUE
13772	wC6_d2-1_cDAPI	\N	\N
13773	original/PLATE-1A/bPLATE_wC6_s2_cRGB.png	\N	GREEN
13774	wC6_d2-1_cGFP	\N	\N
13775	original/PLATE-1A/bPLATE_wC6_s2_cRGB.png	\N	RED
13776	wC6_d2-1_cCy5	\N	\N
13777	original/PLATE-1A/bPLATE_wD4_s3_cRGB.png	\N	BLUE
13778	wD4_d3-1_cDAPI	\N	\N
13779	original/PLATE-1A/bPLATE_wD4_s3_cRGB.png	\N	GREEN
13780	wD4_d3-1_cGFP	\N	\N
13781	original/PLATE-1A/bPLATE_wD4_s3_cRGB.png	\N	RED
13782	wD4_d3-1_cCy5	\N	\N
13783	original/PLATE-1A/bPLATE_wA9_s6_cRGB.png	\N	BLUE
13784	wA9_d3-2_cDAPI	\N	\N
13785	original/PLATE-1A/bPLATE_wA9_s6_cRGB.png	\N	GREEN
13786	wA9_d3-2_cGFP	\N	\N
13787	original/PLATE-1A/bPLATE_wA9_s6_cRGB.png	\N	RED
13788	wA9_d3-2_cCy5	\N	\N
13789	original/PLATE-1A/bPLATE_wC12_s2_cRGB.png	\N	BLUE
13790	wC12_d2-1_cDAPI	\N	\N
13791	original/PLATE-1A/bPLATE_wC12_s2_cRGB.png	\N	GREEN
13792	wC12_d2-1_cGFP	\N	\N
13793	original/PLATE-1A/bPLATE_wC12_s2_cRGB.png	\N	RED
13794	wC12_d2-1_cCy5	\N	\N
13795	original/PLATE-1A/bPLATE_wA5_s5_cRGB.png	\N	BLUE
13796	wA5_d2-2_cDAPI	\N	\N
13797	original/PLATE-1A/bPLATE_wA5_s5_cRGB.png	\N	GREEN
13798	wA5_d2-2_cGFP	\N	\N
13799	original/PLATE-1A/bPLATE_wA5_s5_cRGB.png	\N	RED
13800	wA5_d2-2_cCy5	\N	\N
13801	original/PLATE-1A/bPLATE_wC9_s5_cRGB.png	\N	BLUE
13802	wC9_d2-2_cDAPI	\N	\N
13803	original/PLATE-1A/bPLATE_wC9_s5_cRGB.png	\N	GREEN
13804	wC9_d2-2_cGFP	\N	\N
13805	original/PLATE-1A/bPLATE_wC9_s5_cRGB.png	\N	RED
13806	wC9_d2-2_cCy5	\N	\N
13807	original/PLATE-1A/bPLATE_wA2_s8_cRGB.png	\N	BLUE
13808	wA2_d2-3_cDAPI	\N	\N
13809	original/PLATE-1A/bPLATE_wA2_s8_cRGB.png	\N	GREEN
13810	wA2_d2-3_cGFP	\N	\N
13811	original/PLATE-1A/bPLATE_wA2_s8_cRGB.png	\N	RED
13812	wA2_d2-3_cCy5	\N	\N
13813	original/PLATE-1A/bPLATE_wF4_s2_cRGB.png	\N	BLUE
13814	wF4_d2-1_cDAPI	\N	\N
13815	original/PLATE-1A/bPLATE_wF4_s2_cRGB.png	\N	GREEN
13816	wF4_d2-1_cGFP	\N	\N
13817	original/PLATE-1A/bPLATE_wF4_s2_cRGB.png	\N	RED
13818	wF4_d2-1_cCy5	\N	\N
13819	original/PLATE-1A/bPLATE_wC10_s2_cRGB.png	\N	BLUE
13820	wC10_d2-1_cDAPI	\N	\N
13821	original/PLATE-1A/bPLATE_wC10_s2_cRGB.png	\N	GREEN
13822	wC10_d2-1_cGFP	\N	\N
13823	original/PLATE-1A/bPLATE_wC10_s2_cRGB.png	\N	RED
13824	wC10_d2-1_cCy5	\N	\N
13825	original/PLATE-1A/bPLATE_wA3_s5_cRGB.png	\N	BLUE
13826	wA3_d2-2_cDAPI	\N	\N
13827	original/PLATE-1A/bPLATE_wA3_s5_cRGB.png	\N	GREEN
13828	wA3_d2-2_cGFP	\N	\N
13829	original/PLATE-1A/bPLATE_wA3_s5_cRGB.png	\N	RED
13830	wA3_d2-2_cCy5	\N	\N
13831	original/PLATE-1A/bPLATE_wH8_s6_cRGB.png	\N	BLUE
13832	wH8_d3-2_cDAPI	\N	\N
13833	original/PLATE-1A/bPLATE_wH8_s6_cRGB.png	\N	GREEN
13834	wH8_d3-2_cGFP	\N	\N
13835	original/PLATE-1A/bPLATE_wH8_s6_cRGB.png	\N	RED
13836	wH8_d3-2_cCy5	\N	\N
13837	original/PLATE-1A/bPLATE_wF1_s9_cRGB.png	\N	BLUE
13838	wF1_d3-3_cDAPI	\N	\N
13839	original/PLATE-1A/bPLATE_wF1_s9_cRGB.png	\N	GREEN
13840	wF1_d3-3_cGFP	\N	\N
13841	original/PLATE-1A/bPLATE_wF1_s9_cRGB.png	\N	RED
13842	wF1_d3-3_cCy5	\N	\N
13843	original/PLATE-1A/bPLATE_wC9_s6_cRGB.png	\N	BLUE
13844	wC9_d3-2_cDAPI	\N	\N
13845	original/PLATE-1A/bPLATE_wC9_s6_cRGB.png	\N	GREEN
13846	wC9_d3-2_cGFP	\N	\N
13847	original/PLATE-1A/bPLATE_wC9_s6_cRGB.png	\N	RED
13848	wC9_d3-2_cCy5	\N	\N
13849	original/PLATE-1A/bPLATE_wA2_s9_cRGB.png	\N	BLUE
13850	wA2_d3-3_cDAPI	\N	\N
13851	original/PLATE-1A/bPLATE_wA2_s9_cRGB.png	\N	GREEN
13852	wA2_d3-3_cGFP	\N	\N
13853	original/PLATE-1A/bPLATE_wA2_s9_cRGB.png	\N	RED
13854	wA2_d3-3_cCy5	\N	\N
13855	original/PLATE-1A/bPLATE_wF4_s3_cRGB.png	\N	BLUE
13856	wF4_d3-1_cDAPI	\N	\N
13857	original/PLATE-1A/bPLATE_wF4_s3_cRGB.png	\N	GREEN
13858	wF4_d3-1_cGFP	\N	\N
13859	original/PLATE-1A/bPLATE_wF4_s3_cRGB.png	\N	RED
13860	wF4_d3-1_cCy5	\N	\N
13861	original/PLATE-1A/bPLATE_wG10_s9_cRGB.png	\N	BLUE
13862	wG10_d3-3_cDAPI	\N	\N
13863	original/PLATE-1A/bPLATE_wG10_s9_cRGB.png	\N	GREEN
13864	wG10_d3-3_cGFP	\N	\N
13865	original/PLATE-1A/bPLATE_wG10_s9_cRGB.png	\N	RED
13866	wG10_d3-3_cCy5	\N	\N
13867	original/PLATE-1A/bPLATE_wC4_s4_cRGB.png	\N	BLUE
13868	wC4_d1-2_cDAPI	\N	\N
13869	original/PLATE-1A/bPLATE_wC4_s4_cRGB.png	\N	GREEN
13870	wC4_d1-2_cGFP	\N	\N
13871	original/PLATE-1A/bPLATE_wC4_s4_cRGB.png	\N	RED
13872	wC4_d1-2_cCy5	\N	\N
13873	original/PLATE-1A/bPLATE_wE11_s1_cRGB.png	\N	BLUE
13874	wE11_d1-1_cDAPI	\N	\N
13875	original/PLATE-1A/bPLATE_wE11_s1_cRGB.png	\N	GREEN
13876	wE11_d1-1_cGFP	\N	\N
13877	original/PLATE-1A/bPLATE_wE11_s1_cRGB.png	\N	RED
13878	wE11_d1-1_cCy5	\N	\N
13879	original/PLATE-1A/bPLATE_wH5_s8_cRGB.png	\N	BLUE
13880	wH5_d2-3_cDAPI	\N	\N
13881	original/PLATE-1A/bPLATE_wH5_s8_cRGB.png	\N	GREEN
13882	wH5_d2-3_cGFP	\N	\N
13883	original/PLATE-1A/bPLATE_wH5_s8_cRGB.png	\N	RED
13884	wH5_d2-3_cCy5	\N	\N
13885	original/PLATE-1A/bPLATE_wH12_s1_cRGB.png	\N	BLUE
13886	wH12_d1-1_cDAPI	\N	\N
13887	original/PLATE-1A/bPLATE_wH12_s1_cRGB.png	\N	GREEN
13888	wH12_d1-1_cGFP	\N	\N
13889	original/PLATE-1A/bPLATE_wH12_s1_cRGB.png	\N	RED
13890	wH12_d1-1_cCy5	\N	\N
13891	original/PLATE-1A/bPLATE_wC10_s7_cRGB.png	\N	BLUE
13892	wC10_d1-3_cDAPI	\N	\N
13893	original/PLATE-1A/bPLATE_wC10_s7_cRGB.png	\N	GREEN
13894	wC10_d1-3_cGFP	\N	\N
13895	original/PLATE-1A/bPLATE_wC10_s7_cRGB.png	\N	RED
13896	wC10_d1-3_cCy5	\N	\N
13897	original/PLATE-1A/bPLATE_wF5_s4_cRGB.png	\N	BLUE
13898	wF5_d1-2_cDAPI	\N	\N
13899	original/PLATE-1A/bPLATE_wF5_s4_cRGB.png	\N	GREEN
13900	wF5_d1-2_cGFP	\N	\N
13901	original/PLATE-1A/bPLATE_wF5_s4_cRGB.png	\N	RED
13902	wF5_d1-2_cCy5	\N	\N
13903	original/PLATE-1A/bPLATE_wD10_s3_cRGB.png	\N	BLUE
13904	wD10_d3-1_cDAPI	\N	\N
13905	original/PLATE-1A/bPLATE_wD10_s3_cRGB.png	\N	GREEN
13906	wD10_d3-1_cGFP	\N	\N
13907	original/PLATE-1A/bPLATE_wD10_s3_cRGB.png	\N	RED
13908	wD10_d3-1_cCy5	\N	\N
13909	original/PLATE-1A/bPLATE_wB3_s6_cRGB.png	\N	BLUE
13910	wB3_d3-2_cDAPI	\N	\N
13911	original/PLATE-1A/bPLATE_wB3_s6_cRGB.png	\N	GREEN
13912	wB3_d3-2_cGFP	\N	\N
13913	original/PLATE-1A/bPLATE_wB3_s6_cRGB.png	\N	RED
13914	wB3_d3-2_cCy5	\N	\N
13915	original/PLATE-1A/bPLATE_wB8_s3_cRGB.png	\N	BLUE
13916	wB8_d3-1_cDAPI	\N	\N
13917	original/PLATE-1A/bPLATE_wB8_s3_cRGB.png	\N	GREEN
13918	wB8_d3-1_cGFP	\N	\N
13919	original/PLATE-1A/bPLATE_wB8_s3_cRGB.png	\N	RED
13920	wB8_d3-1_cCy5	\N	\N
13921	original/PLATE-1A/bPLATE_wB7_s1_cRGB.png	\N	BLUE
13922	wB7_d1-1_cDAPI	\N	\N
13923	original/PLATE-1A/bPLATE_wB7_s1_cRGB.png	\N	GREEN
13924	wB7_d1-1_cGFP	\N	\N
13925	original/PLATE-1A/bPLATE_wB7_s1_cRGB.png	\N	RED
13926	wB7_d1-1_cCy5	\N	\N
13927	original/PLATE-1A/bPLATE_wA10_s7_cRGB.png	\N	BLUE
13928	wA10_d1-3_cDAPI	\N	\N
13929	original/PLATE-1A/bPLATE_wA10_s7_cRGB.png	\N	GREEN
13930	wA10_d1-3_cGFP	\N	\N
13931	original/PLATE-1A/bPLATE_wA10_s7_cRGB.png	\N	RED
13932	wA10_d1-3_cCy5	\N	\N
13933	original/PLATE-1A/bPLATE_wD5_s4_cRGB.png	\N	BLUE
13934	wD5_d1-2_cDAPI	\N	\N
13935	original/PLATE-1A/bPLATE_wD5_s4_cRGB.png	\N	GREEN
13936	wD5_d1-2_cGFP	\N	\N
13937	original/PLATE-1A/bPLATE_wD5_s4_cRGB.png	\N	RED
13938	wD5_d1-2_cCy5	\N	\N
13939	original/PLATE-1A/bPLATE_wF12_s1_cRGB.png	\N	BLUE
13940	wF12_d1-1_cDAPI	\N	\N
13941	original/PLATE-1A/bPLATE_wF12_s1_cRGB.png	\N	GREEN
13942	wF12_d1-1_cGFP	\N	\N
13943	original/PLATE-1A/bPLATE_wF12_s1_cRGB.png	\N	RED
13944	wF12_d1-1_cCy5	\N	\N
13945	original/PLATE-1A/bPLATE_wD11_s9_cRGB.png	\N	BLUE
13946	wD11_d3-3_cDAPI	\N	\N
13947	original/PLATE-1A/bPLATE_wD11_s9_cRGB.png	\N	GREEN
13948	wD11_d3-3_cGFP	\N	\N
13949	original/PLATE-1A/bPLATE_wD11_s9_cRGB.png	\N	RED
13950	wD11_d3-3_cCy5	\N	\N
13951	original/PLATE-1A/bPLATE_wG6_s6_cRGB.png	\N	BLUE
13952	wG6_d3-2_cDAPI	\N	\N
13953	original/PLATE-1A/bPLATE_wG6_s6_cRGB.png	\N	GREEN
13954	wG6_d3-2_cGFP	\N	\N
13955	original/PLATE-1A/bPLATE_wG6_s6_cRGB.png	\N	RED
13956	wG6_d3-2_cCy5	\N	\N
13957	original/PLATE-1A/bPLATE_wC12_s6_cRGB.png	\N	BLUE
13958	wC12_d3-2_cDAPI	\N	\N
13959	original/PLATE-1A/bPLATE_wC12_s6_cRGB.png	\N	GREEN
13960	wC12_d3-2_cGFP	\N	\N
13961	original/PLATE-1A/bPLATE_wC12_s6_cRGB.png	\N	RED
13962	wC12_d3-2_cCy5	\N	\N
13963	original/PLATE-1A/bPLATE_wA5_s9_cRGB.png	\N	BLUE
13964	wA5_d3-3_cDAPI	\N	\N
13965	original/PLATE-1A/bPLATE_wA5_s9_cRGB.png	\N	GREEN
13966	wA5_d3-3_cGFP	\N	\N
13967	original/PLATE-1A/bPLATE_wA5_s9_cRGB.png	\N	RED
13968	wA5_d3-3_cCy5	\N	\N
13969	original/PLATE-1A/bPLATE_wF7_s3_cRGB.png	\N	BLUE
13970	wF7_d3-1_cDAPI	\N	\N
13971	original/PLATE-1A/bPLATE_wF7_s3_cRGB.png	\N	GREEN
13972	wF7_d3-1_cGFP	\N	\N
13973	original/PLATE-1A/bPLATE_wF7_s3_cRGB.png	\N	RED
13974	wF7_d3-1_cCy5	\N	\N
13975	original/PLATE-1A/bPLATE_wD2_s1_cRGB.png	\N	BLUE
13976	wD2_d1-1_cDAPI	\N	\N
13977	original/PLATE-1A/bPLATE_wD2_s1_cRGB.png	\N	GREEN
13978	wD2_d1-1_cGFP	\N	\N
13979	original/PLATE-1A/bPLATE_wD2_s1_cRGB.png	\N	RED
13980	wD2_d1-1_cCy5	\N	\N
13981	original/PLATE-1A/bPLATE_wA7_s4_cRGB.png	\N	BLUE
13982	wA7_d1-2_cDAPI	\N	\N
13983	original/PLATE-1A/bPLATE_wA7_s4_cRGB.png	\N	GREEN
13984	wA7_d1-2_cGFP	\N	\N
13985	original/PLATE-1A/bPLATE_wA7_s4_cRGB.png	\N	RED
13986	wA7_d1-2_cCy5	\N	\N
13987	original/PLATE-1A/bPLATE_wC8_s3_cRGB.png	\N	BLUE
13988	wC8_d3-1_cDAPI	\N	\N
13989	original/PLATE-1A/bPLATE_wC8_s3_cRGB.png	\N	GREEN
13990	wC8_d3-1_cGFP	\N	\N
13991	original/PLATE-1A/bPLATE_wC8_s3_cRGB.png	\N	RED
13992	wC8_d3-1_cCy5	\N	\N
13993	original/PLATE-1A/bPLATE_wA1_s6_cRGB.png	\N	BLUE
13994	wA1_d3-2_cDAPI	\N	\N
13995	original/PLATE-1A/bPLATE_wA1_s6_cRGB.png	\N	GREEN
13996	wA1_d3-2_cGFP	\N	\N
13997	original/PLATE-1A/bPLATE_wA1_s6_cRGB.png	\N	RED
13998	wA1_d3-2_cCy5	\N	\N
13999	original/PLATE-1A/bPLATE_wG7_s9_cRGB.png	\N	BLUE
14000	wG7_d3-3_cDAPI	\N	\N
14001	original/PLATE-1A/bPLATE_wG7_s9_cRGB.png	\N	GREEN
14002	wG7_d3-3_cGFP	\N	\N
14003	original/PLATE-1A/bPLATE_wG7_s9_cRGB.png	\N	RED
14004	wG7_d3-3_cCy5	\N	\N
14005	original/PLATE-1A/bPLATE_wB10_s8_cRGB.png	\N	BLUE
14006	wB10_d2-3_cDAPI	\N	\N
14007	original/PLATE-1A/bPLATE_wB10_s8_cRGB.png	\N	GREEN
14008	wB10_d2-3_cGFP	\N	\N
14009	original/PLATE-1A/bPLATE_wB10_s8_cRGB.png	\N	RED
14010	wB10_d2-3_cCy5	\N	\N
14011	original/PLATE-1A/bPLATE_wE5_s5_cRGB.png	\N	BLUE
14012	wE5_d2-2_cDAPI	\N	\N
14013	original/PLATE-1A/bPLATE_wE5_s5_cRGB.png	\N	GREEN
14014	wE5_d2-2_cGFP	\N	\N
14015	original/PLATE-1A/bPLATE_wE5_s5_cRGB.png	\N	RED
14016	wE5_d2-2_cCy5	\N	\N
14017	original/PLATE-1A/bPLATE_wG12_s2_cRGB.png	\N	BLUE
14018	wG12_d2-1_cDAPI	\N	\N
14019	original/PLATE-1A/bPLATE_wG12_s2_cRGB.png	\N	GREEN
14020	wG12_d2-1_cGFP	\N	\N
14021	original/PLATE-1A/bPLATE_wG12_s2_cRGB.png	\N	RED
14022	wG12_d2-1_cCy5	\N	\N
14023	original/PLATE-1A/bPLATE_wB8_s8_cRGB.png	\N	BLUE
14024	wB8_d2-3_cDAPI	\N	\N
14025	original/PLATE-1A/bPLATE_wB8_s8_cRGB.png	\N	GREEN
14026	wB8_d2-3_cGFP	\N	\N
14027	original/PLATE-1A/bPLATE_wB8_s8_cRGB.png	\N	RED
14028	wB8_d2-3_cCy5	\N	\N
14029	original/PLATE-1A/bPLATE_wE3_s5_cRGB.png	\N	BLUE
14030	wE3_d2-2_cDAPI	\N	\N
14031	original/PLATE-1A/bPLATE_wE3_s5_cRGB.png	\N	GREEN
14032	wE3_d2-2_cGFP	\N	\N
14033	original/PLATE-1A/bPLATE_wE3_s5_cRGB.png	\N	RED
14034	wE3_d2-2_cCy5	\N	\N
14035	original/PLATE-1A/bPLATE_wG10_s2_cRGB.png	\N	BLUE
14036	wG10_d2-1_cDAPI	\N	\N
14037	original/PLATE-1A/bPLATE_wG10_s2_cRGB.png	\N	GREEN
14038	wG10_d2-1_cGFP	\N	\N
14039	original/PLATE-1A/bPLATE_wG10_s2_cRGB.png	\N	RED
14040	wG10_d2-1_cCy5	\N	\N
14041	original/PLATE-1A/bPLATE_wB10_s1_cRGB.png	\N	BLUE
14042	wB10_d1-1_cDAPI	\N	\N
14043	original/PLATE-1A/bPLATE_wB10_s1_cRGB.png	\N	GREEN
14044	wB10_d1-1_cGFP	\N	\N
14045	original/PLATE-1A/bPLATE_wB10_s1_cRGB.png	\N	RED
14046	wB10_d1-1_cCy5	\N	\N
14047	original/PLATE-1A/bPLATE_wH1_s9_cRGB.png	\N	BLUE
14048	wH1_d3-3_cDAPI	\N	\N
14049	original/PLATE-1A/bPLATE_wH1_s9_cRGB.png	\N	GREEN
14050	wH1_d3-3_cGFP	\N	\N
14051	original/PLATE-1A/bPLATE_wH1_s9_cRGB.png	\N	RED
14052	wH1_d3-3_cCy5	\N	\N
14053	original/PLATE-1A/bPLATE_wG2_s8_cRGB.png	\N	BLUE
14054	wG2_d2-3_cDAPI	\N	\N
14055	original/PLATE-1A/bPLATE_wG2_s8_cRGB.png	\N	GREEN
14056	wG2_d2-3_cGFP	\N	\N
14057	original/PLATE-1A/bPLATE_wG2_s8_cRGB.png	\N	RED
14058	wG2_d2-3_cCy5	\N	\N
14059	original/PLATE-1A/bPLATE_wG9_s9_cRGB.png	\N	BLUE
14060	wG9_d3-3_cDAPI	\N	\N
14061	original/PLATE-1A/bPLATE_wG9_s9_cRGB.png	\N	GREEN
14062	wG9_d3-3_cGFP	\N	\N
14063	original/PLATE-1A/bPLATE_wG9_s9_cRGB.png	\N	RED
14064	wG9_d3-3_cCy5	\N	\N
14065	original/PLATE-1A/bPLATE_wH12_s2_cRGB.png	\N	BLUE
14066	wH12_d2-1_cDAPI	\N	\N
14067	original/PLATE-1A/bPLATE_wH12_s2_cRGB.png	\N	GREEN
14068	wH12_d2-1_cGFP	\N	\N
14069	original/PLATE-1A/bPLATE_wH12_s2_cRGB.png	\N	RED
14070	wH12_d2-1_cCy5	\N	\N
14071	original/PLATE-1A/bPLATE_wC10_s8_cRGB.png	\N	BLUE
14072	wC10_d2-3_cDAPI	\N	\N
14073	original/PLATE-1A/bPLATE_wC10_s8_cRGB.png	\N	GREEN
14074	wC10_d2-3_cGFP	\N	\N
14075	original/PLATE-1A/bPLATE_wC10_s8_cRGB.png	\N	RED
14076	wC10_d2-3_cCy5	\N	\N
14077	original/PLATE-1A/bPLATE_wF5_s5_cRGB.png	\N	BLUE
14078	wF5_d2-2_cDAPI	\N	\N
14079	original/PLATE-1A/bPLATE_wF5_s5_cRGB.png	\N	GREEN
14080	wF5_d2-2_cGFP	\N	\N
14081	original/PLATE-1A/bPLATE_wF5_s5_cRGB.png	\N	RED
14082	wF5_d2-2_cCy5	\N	\N
14083	original/PLATE-1A/bPLATE_wH2_s7_cRGB.png	\N	BLUE
14084	wH2_d1-3_cDAPI	\N	\N
14085	original/PLATE-1A/bPLATE_wH2_s7_cRGB.png	\N	GREEN
14086	wH2_d1-3_cGFP	\N	\N
14087	original/PLATE-1A/bPLATE_wH2_s7_cRGB.png	\N	RED
14088	wH2_d1-3_cCy5	\N	\N
14089	original/PLATE-1A/bPLATE_wE1_s7_cRGB.png	\N	BLUE
14090	wE1_d1-3_cDAPI	\N	\N
14091	original/PLATE-1A/bPLATE_wE1_s7_cRGB.png	\N	GREEN
14092	wE1_d1-3_cGFP	\N	\N
14093	original/PLATE-1A/bPLATE_wE1_s7_cRGB.png	\N	RED
14094	wE1_d1-3_cCy5	\N	\N
14095	original/PLATE-1A/bPLATE_wG8_s4_cRGB.png	\N	BLUE
14096	wG8_d1-2_cDAPI	\N	\N
14097	original/PLATE-1A/bPLATE_wG8_s4_cRGB.png	\N	GREEN
14098	wG8_d1-2_cGFP	\N	\N
14099	original/PLATE-1A/bPLATE_wG8_s4_cRGB.png	\N	RED
14100	wG8_d1-2_cCy5	\N	\N
14101	original/PLATE-1A/bPLATE_wC7_s1_cRGB.png	\N	BLUE
14102	wC7_d1-1_cDAPI	\N	\N
14103	original/PLATE-1A/bPLATE_wC7_s1_cRGB.png	\N	GREEN
14104	wC7_d1-1_cGFP	\N	\N
14105	original/PLATE-1A/bPLATE_wC7_s1_cRGB.png	\N	RED
14106	wC7_d1-1_cCy5	\N	\N
14107	original/PLATE-1A/bPLATE_wA12_s4_cRGB.png	\N	BLUE
14108	wA12_d1-2_cDAPI	\N	\N
14109	original/PLATE-1A/bPLATE_wA12_s4_cRGB.png	\N	GREEN
14110	wA12_d1-2_cGFP	\N	\N
14111	original/PLATE-1A/bPLATE_wA12_s4_cRGB.png	\N	RED
14112	wA12_d1-2_cCy5	\N	\N
14113	original/PLATE-1A/bPLATE_wD7_s1_cRGB.png	\N	BLUE
14114	wD7_d1-1_cDAPI	\N	\N
14115	original/PLATE-1A/bPLATE_wD7_s1_cRGB.png	\N	GREEN
14116	wD7_d1-1_cGFP	\N	\N
14117	original/PLATE-1A/bPLATE_wD7_s1_cRGB.png	\N	RED
14118	wD7_d1-1_cCy5	\N	\N
14119	original/PLATE-1A/bPLATE_wH5_s9_cRGB.png	\N	BLUE
14120	wH5_d3-3_cDAPI	\N	\N
14121	original/PLATE-1A/bPLATE_wH5_s9_cRGB.png	\N	GREEN
14122	wH5_d3-3_cGFP	\N	\N
14123	original/PLATE-1A/bPLATE_wH5_s9_cRGB.png	\N	RED
14124	wH5_d3-3_cCy5	\N	\N
14125	original/PLATE-1A/bPLATE_wH7_s8_cRGB.png	\N	BLUE
14126	wH7_d2-3_cDAPI	\N	\N
14127	original/PLATE-1A/bPLATE_wH7_s8_cRGB.png	\N	GREEN
14128	wH7_d2-3_cGFP	\N	\N
14129	original/PLATE-1A/bPLATE_wH7_s8_cRGB.png	\N	RED
14130	wH7_d2-3_cCy5	\N	\N
14131	original/PLATE-1A/bPLATE_wG12_s7_cRGB.png	\N	BLUE
14132	wG12_d1-3_cDAPI	\N	\N
14133	original/PLATE-1A/bPLATE_wG12_s7_cRGB.png	\N	GREEN
14134	wG12_d1-3_cGFP	\N	\N
14135	original/PLATE-1A/bPLATE_wG12_s7_cRGB.png	\N	RED
14136	wG12_d1-3_cCy5	\N	\N
14137	original/PLATE-1A/bPLATE_wD1_s8_cRGB.png	\N	BLUE
14138	wD1_d2-3_cDAPI	\N	\N
14139	original/PLATE-1A/bPLATE_wD1_s8_cRGB.png	\N	GREEN
14140	wD1_d2-3_cGFP	\N	\N
14141	original/PLATE-1A/bPLATE_wD1_s8_cRGB.png	\N	RED
14142	wD1_d2-3_cCy5	\N	\N
14143	original/PLATE-1A/bPLATE_wF8_s5_cRGB.png	\N	BLUE
14144	wF8_d2-2_cDAPI	\N	\N
14145	original/PLATE-1A/bPLATE_wF8_s5_cRGB.png	\N	GREEN
14146	wF8_d2-2_cGFP	\N	\N
14147	original/PLATE-1A/bPLATE_wF8_s5_cRGB.png	\N	RED
14148	wF8_d2-2_cCy5	\N	\N
14149	original/PLATE-1A/bPLATE_wB10_s7_cRGB.png	\N	BLUE
14150	wB10_d1-3_cDAPI	\N	\N
14151	original/PLATE-1A/bPLATE_wB10_s7_cRGB.png	\N	GREEN
14152	wB10_d1-3_cGFP	\N	\N
14153	original/PLATE-1A/bPLATE_wB10_s7_cRGB.png	\N	RED
14154	wB10_d1-3_cCy5	\N	\N
14155	original/PLATE-1A/bPLATE_wE5_s4_cRGB.png	\N	BLUE
14156	wE5_d1-2_cDAPI	\N	\N
14157	original/PLATE-1A/bPLATE_wE5_s4_cRGB.png	\N	GREEN
14158	wE5_d1-2_cGFP	\N	\N
14159	original/PLATE-1A/bPLATE_wE5_s4_cRGB.png	\N	RED
14160	wE5_d1-2_cCy5	\N	\N
14161	original/PLATE-1A/bPLATE_wG12_s1_cRGB.png	\N	BLUE
14162	wG12_d1-1_cDAPI	\N	\N
14163	original/PLATE-1A/bPLATE_wG12_s1_cRGB.png	\N	GREEN
14164	wG12_d1-1_cGFP	\N	\N
14165	original/PLATE-1A/bPLATE_wG12_s1_cRGB.png	\N	RED
14166	wG12_d1-1_cCy5	\N	\N
14167	original/PLATE-1A/bPLATE_wD3_s2_cRGB.png	\N	BLUE
14168	wD3_d2-1_cDAPI	\N	\N
14169	original/PLATE-1A/bPLATE_wD3_s2_cRGB.png	\N	GREEN
14170	wD3_d2-1_cGFP	\N	\N
14171	original/PLATE-1A/bPLATE_wD3_s2_cRGB.png	\N	RED
14172	wD3_d2-1_cCy5	\N	\N
14173	original/PLATE-1A/bPLATE_wA8_s5_cRGB.png	\N	BLUE
14174	wA8_d2-2_cDAPI	\N	\N
14175	original/PLATE-1A/bPLATE_wA8_s5_cRGB.png	\N	GREEN
14176	wA8_d2-2_cGFP	\N	\N
14177	original/PLATE-1A/bPLATE_wA8_s5_cRGB.png	\N	RED
14178	wA8_d2-2_cCy5	\N	\N
14179	original/PLATE-1A/bPLATE_wD9_s8_cRGB.png	\N	BLUE
14180	wD9_d2-3_cDAPI	\N	\N
14181	original/PLATE-1A/bPLATE_wD9_s8_cRGB.png	\N	GREEN
14182	wD9_d2-3_cGFP	\N	\N
14183	original/PLATE-1A/bPLATE_wD9_s8_cRGB.png	\N	RED
14184	wD9_d2-3_cCy5	\N	\N
14185	original/PLATE-1A/bPLATE_wG4_s5_cRGB.png	\N	BLUE
14186	wG4_d2-2_cDAPI	\N	\N
14187	original/PLATE-1A/bPLATE_wG4_s5_cRGB.png	\N	GREEN
14188	wG4_d2-2_cGFP	\N	\N
14189	original/PLATE-1A/bPLATE_wG4_s5_cRGB.png	\N	RED
14190	wG4_d2-2_cCy5	\N	\N
14191	original/PLATE-1A/bPLATE_wB9_s7_cRGB.png	\N	BLUE
14192	wB9_d1-3_cDAPI	\N	\N
14193	original/PLATE-1A/bPLATE_wB9_s7_cRGB.png	\N	GREEN
14194	wB9_d1-3_cGFP	\N	\N
14195	original/PLATE-1A/bPLATE_wB9_s7_cRGB.png	\N	RED
14196	wB9_d1-3_cCy5	\N	\N
14197	original/PLATE-1A/bPLATE_wE4_s4_cRGB.png	\N	BLUE
14198	wE4_d1-2_cDAPI	\N	\N
14199	original/PLATE-1A/bPLATE_wE4_s4_cRGB.png	\N	GREEN
14200	wE4_d1-2_cGFP	\N	\N
14201	original/PLATE-1A/bPLATE_wE4_s4_cRGB.png	\N	RED
14202	wE4_d1-2_cCy5	\N	\N
14203	original/PLATE-1A/bPLATE_wG11_s1_cRGB.png	\N	BLUE
14204	wG11_d1-1_cDAPI	\N	\N
14205	original/PLATE-1A/bPLATE_wG11_s1_cRGB.png	\N	GREEN
14206	wG11_d1-1_cGFP	\N	\N
14207	original/PLATE-1A/bPLATE_wG11_s1_cRGB.png	\N	RED
14208	wG11_d1-1_cCy5	\N	\N
14209	original/PLATE-1A/bPLATE_wB10_s4_cRGB.png	\N	BLUE
14210	wB10_d1-2_cDAPI	\N	\N
14211	original/PLATE-1A/bPLATE_wB10_s4_cRGB.png	\N	GREEN
14212	wB10_d1-2_cGFP	\N	\N
14213	original/PLATE-1A/bPLATE_wB10_s4_cRGB.png	\N	RED
14214	wB10_d1-2_cCy5	\N	\N
14215	original/PLATE-1A/bPLATE_wE5_s1_cRGB.png	\N	BLUE
14216	wE5_d1-1_cDAPI	\N	\N
14217	original/PLATE-1A/bPLATE_wE5_s1_cRGB.png	\N	GREEN
14218	wE5_d1-1_cGFP	\N	\N
14219	original/PLATE-1A/bPLATE_wE5_s1_cRGB.png	\N	RED
14220	wE5_d1-1_cCy5	\N	\N
14221	original/PLATE-1A/bPLATE_wD10_s7_cRGB.png	\N	BLUE
14222	wD10_d1-3_cDAPI	\N	\N
14223	original/PLATE-1A/bPLATE_wD10_s7_cRGB.png	\N	GREEN
14224	wD10_d1-3_cGFP	\N	\N
14225	original/PLATE-1A/bPLATE_wD10_s7_cRGB.png	\N	RED
14226	wD10_d1-3_cCy5	\N	\N
14227	original/PLATE-1A/bPLATE_wG5_s4_cRGB.png	\N	BLUE
14228	wG5_d1-2_cDAPI	\N	\N
14229	original/PLATE-1A/bPLATE_wG5_s4_cRGB.png	\N	GREEN
14230	wG5_d1-2_cGFP	\N	\N
14231	original/PLATE-1A/bPLATE_wG5_s4_cRGB.png	\N	RED
14232	wG5_d1-2_cCy5	\N	\N
14233	original/PLATE-1A/bPLATE_wA10_s9_cRGB.png	\N	BLUE
14234	wA10_d3-3_cDAPI	\N	\N
14235	original/PLATE-1A/bPLATE_wA10_s9_cRGB.png	\N	GREEN
14236	wA10_d3-3_cGFP	\N	\N
14237	original/PLATE-1A/bPLATE_wA10_s9_cRGB.png	\N	RED
14238	wA10_d3-3_cCy5	\N	\N
14239	original/PLATE-1A/bPLATE_wD5_s6_cRGB.png	\N	BLUE
14240	wD5_d3-2_cDAPI	\N	\N
14241	original/PLATE-1A/bPLATE_wD5_s6_cRGB.png	\N	GREEN
14242	wD5_d3-2_cGFP	\N	\N
14243	original/PLATE-1A/bPLATE_wD5_s6_cRGB.png	\N	RED
14244	wD5_d3-2_cCy5	\N	\N
14245	original/PLATE-1A/bPLATE_wF12_s3_cRGB.png	\N	BLUE
14246	wF12_d3-1_cDAPI	\N	\N
14247	original/PLATE-1A/bPLATE_wF12_s3_cRGB.png	\N	GREEN
14248	wF12_d3-1_cGFP	\N	\N
14249	original/PLATE-1A/bPLATE_wF12_s3_cRGB.png	\N	RED
14250	wF12_d3-1_cCy5	\N	\N
14251	original/PLATE-1A/bPLATE_wG3_s7_cRGB.png	\N	BLUE
14252	wG3_d1-3_cDAPI	\N	\N
14253	original/PLATE-1A/bPLATE_wG3_s7_cRGB.png	\N	GREEN
14254	wG3_d1-3_cGFP	\N	\N
14255	original/PLATE-1A/bPLATE_wG3_s7_cRGB.png	\N	RED
14256	wG3_d1-3_cCy5	\N	\N
14257	original/PLATE-1A/bPLATE_wA11_s9_cRGB.png	\N	BLUE
14258	wA11_d3-3_cDAPI	\N	\N
14259	original/PLATE-1A/bPLATE_wA11_s9_cRGB.png	\N	GREEN
14260	wA11_d3-3_cGFP	\N	\N
14261	original/PLATE-1A/bPLATE_wA11_s9_cRGB.png	\N	RED
14262	wA11_d3-3_cCy5	\N	\N
14263	original/PLATE-1A/bPLATE_wD6_s6_cRGB.png	\N	BLUE
14264	wD6_d3-2_cDAPI	\N	\N
14265	original/PLATE-1A/bPLATE_wD6_s6_cRGB.png	\N	GREEN
14266	wD6_d3-2_cGFP	\N	\N
14267	original/PLATE-1A/bPLATE_wD6_s6_cRGB.png	\N	RED
14268	wD6_d3-2_cCy5	\N	\N
14269	original/PLATE-1A/bPLATE_wG1_s3_cRGB.png	\N	BLUE
14270	wG1_d3-1_cDAPI	\N	\N
14271	original/PLATE-1A/bPLATE_wG1_s3_cRGB.png	\N	GREEN
14272	wG1_d3-1_cGFP	\N	\N
14273	original/PLATE-1A/bPLATE_wG1_s3_cRGB.png	\N	RED
14274	wG1_d3-1_cCy5	\N	\N
14275	original/PLATE-1A/bPLATE_wH7_s1_cRGB.png	\N	BLUE
14276	wH7_d1-1_cDAPI	\N	\N
14277	original/PLATE-1A/bPLATE_wH7_s1_cRGB.png	\N	GREEN
14278	wH7_d1-1_cGFP	\N	\N
14279	original/PLATE-1A/bPLATE_wH7_s1_cRGB.png	\N	RED
14280	wH7_d1-1_cCy5	\N	\N
14281	original/PLATE-1A/bPLATE_wC5_s7_cRGB.png	\N	BLUE
14282	wC5_d1-3_cDAPI	\N	\N
14283	original/PLATE-1A/bPLATE_wC5_s7_cRGB.png	\N	GREEN
14284	wC5_d1-3_cGFP	\N	\N
14285	original/PLATE-1A/bPLATE_wC5_s7_cRGB.png	\N	RED
14286	wC5_d1-3_cCy5	\N	\N
14287	original/PLATE-1A/bPLATE_wE12_s4_cRGB.png	\N	BLUE
14288	wE12_d1-2_cDAPI	\N	\N
14289	original/PLATE-1A/bPLATE_wE12_s4_cRGB.png	\N	GREEN
14290	wE12_d1-2_cGFP	\N	\N
14291	original/PLATE-1A/bPLATE_wE12_s4_cRGB.png	\N	RED
14292	wE12_d1-2_cCy5	\N	\N
14293	original/PLATE-1A/bPLATE_wH1_s3_cRGB.png	\N	BLUE
14294	wH1_d3-1_cDAPI	\N	\N
14295	original/PLATE-1A/bPLATE_wH1_s3_cRGB.png	\N	GREEN
14296	wH1_d3-1_cGFP	\N	\N
14297	original/PLATE-1A/bPLATE_wH1_s3_cRGB.png	\N	RED
14298	wH1_d3-1_cCy5	\N	\N
14299	original/PLATE-1A/bPLATE_wB11_s9_cRGB.png	\N	BLUE
14300	wB11_d3-3_cDAPI	\N	\N
14301	original/PLATE-1A/bPLATE_wB11_s9_cRGB.png	\N	GREEN
14302	wB11_d3-3_cGFP	\N	\N
14303	original/PLATE-1A/bPLATE_wB11_s9_cRGB.png	\N	RED
14304	wB11_d3-3_cCy5	\N	\N
14305	original/PLATE-1A/bPLATE_wE6_s6_cRGB.png	\N	BLUE
14306	wE6_d3-2_cDAPI	\N	\N
14307	original/PLATE-1A/bPLATE_wE6_s6_cRGB.png	\N	GREEN
14308	wE6_d3-2_cGFP	\N	\N
14309	original/PLATE-1A/bPLATE_wE6_s6_cRGB.png	\N	RED
14310	wE6_d3-2_cCy5	\N	\N
14311	original/PLATE-1A/bPLATE_wB6_s6_cRGB.png	\N	BLUE
14312	wB6_d3-2_cDAPI	\N	\N
14313	original/PLATE-1A/bPLATE_wB6_s6_cRGB.png	\N	GREEN
14314	wB6_d3-2_cGFP	\N	\N
14315	original/PLATE-1A/bPLATE_wB6_s6_cRGB.png	\N	RED
14316	wB6_d3-2_cCy5	\N	\N
14317	original/PLATE-1A/bPLATE_wE1_s3_cRGB.png	\N	BLUE
14318	wE1_d3-1_cDAPI	\N	\N
14319	original/PLATE-1A/bPLATE_wE1_s3_cRGB.png	\N	GREEN
14320	wE1_d3-1_cGFP	\N	\N
14321	original/PLATE-1A/bPLATE_wE1_s3_cRGB.png	\N	RED
14322	wE1_d3-1_cCy5	\N	\N
14323	original/PLATE-1A/bPLATE_wC8_s9_cRGB.png	\N	BLUE
14324	wC8_d3-3_cDAPI	\N	\N
14325	original/PLATE-1A/bPLATE_wC8_s9_cRGB.png	\N	GREEN
14326	wC8_d3-3_cGFP	\N	\N
14327	original/PLATE-1A/bPLATE_wC8_s9_cRGB.png	\N	RED
14328	wC8_d3-3_cCy5	\N	\N
14329	original/PLATE-1A/bPLATE_wC7_s5_cRGB.png	\N	BLUE
14330	wC7_d2-2_cDAPI	\N	\N
14331	original/PLATE-1A/bPLATE_wC7_s5_cRGB.png	\N	GREEN
14332	wC7_d2-2_cGFP	\N	\N
14333	original/PLATE-1A/bPLATE_wC7_s5_cRGB.png	\N	RED
14334	wC7_d2-2_cCy5	\N	\N
14335	original/PLATE-1A/bPLATE_wF2_s2_cRGB.png	\N	BLUE
14336	wF2_d2-1_cDAPI	\N	\N
14337	original/PLATE-1A/bPLATE_wF2_s2_cRGB.png	\N	GREEN
14338	wF2_d2-1_cGFP	\N	\N
14339	original/PLATE-1A/bPLATE_wF2_s2_cRGB.png	\N	RED
14340	wF2_d2-1_cCy5	\N	\N
14341	original/PLATE-1A/bPLATE_wF3_s6_cRGB.png	\N	BLUE
14342	wF3_d3-2_cDAPI	\N	\N
14343	original/PLATE-1A/bPLATE_wF3_s6_cRGB.png	\N	GREEN
14344	wF3_d3-2_cGFP	\N	\N
14345	original/PLATE-1A/bPLATE_wF3_s6_cRGB.png	\N	RED
14346	wF3_d3-2_cCy5	\N	\N
14347	original/PLATE-1A/bPLATE_wH10_s3_cRGB.png	\N	BLUE
14348	wH10_d3-1_cDAPI	\N	\N
14349	original/PLATE-1A/bPLATE_wH10_s3_cRGB.png	\N	GREEN
14350	wH10_d3-1_cGFP	\N	\N
14351	original/PLATE-1A/bPLATE_wH10_s3_cRGB.png	\N	RED
14352	wH10_d3-1_cCy5	\N	\N
14353	original/PLATE-1A/bPLATE_wA11_s3_cRGB.png	\N	BLUE
14354	wA11_d3-1_cDAPI	\N	\N
14355	original/PLATE-1A/bPLATE_wA11_s3_cRGB.png	\N	GREEN
14356	wA11_d3-1_cGFP	\N	\N
14357	original/PLATE-1A/bPLATE_wA11_s3_cRGB.png	\N	RED
14358	wA11_d3-1_cCy5	\N	\N
14359	original/PLATE-1A/bPLATE_wB6_s1_cRGB.png	\N	BLUE
14360	wB6_d1-1_cDAPI	\N	\N
14361	original/PLATE-1A/bPLATE_wB6_s1_cRGB.png	\N	GREEN
14362	wB6_d1-1_cGFP	\N	\N
14363	original/PLATE-1A/bPLATE_wB6_s1_cRGB.png	\N	RED
14364	wB6_d1-1_cCy5	\N	\N
14365	original/PLATE-1A/bPLATE_wC3_s5_cRGB.png	\N	BLUE
14366	wC3_d2-2_cDAPI	\N	\N
14367	original/PLATE-1A/bPLATE_wC3_s5_cRGB.png	\N	GREEN
14368	wC3_d2-2_cGFP	\N	\N
14369	original/PLATE-1A/bPLATE_wC3_s5_cRGB.png	\N	RED
14370	wC3_d2-2_cCy5	\N	\N
14371	original/PLATE-1A/bPLATE_wE10_s2_cRGB.png	\N	BLUE
14372	wE10_d2-1_cDAPI	\N	\N
14373	original/PLATE-1A/bPLATE_wE10_s2_cRGB.png	\N	GREEN
14374	wE10_d2-1_cGFP	\N	\N
14375	original/PLATE-1A/bPLATE_wE10_s2_cRGB.png	\N	RED
14376	wE10_d2-1_cCy5	\N	\N
14377	original/PLATE-1A/bPLATE_wF10_s8_cRGB.png	\N	BLUE
14378	wF10_d2-3_cDAPI	\N	\N
14379	original/PLATE-1A/bPLATE_wF10_s8_cRGB.png	\N	GREEN
14380	wF10_d2-3_cGFP	\N	\N
14381	original/PLATE-1A/bPLATE_wF10_s8_cRGB.png	\N	RED
14382	wF10_d2-3_cCy5	\N	\N
14383	original/PLATE-1A/bPLATE_wD3_s8_cRGB.png	\N	BLUE
14384	wD3_d2-3_cDAPI	\N	\N
14385	original/PLATE-1A/bPLATE_wD3_s8_cRGB.png	\N	GREEN
14386	wD3_d2-3_cGFP	\N	\N
14387	original/PLATE-1A/bPLATE_wD3_s8_cRGB.png	\N	RED
14388	wD3_d2-3_cCy5	\N	\N
14389	original/PLATE-1A/bPLATE_wF10_s5_cRGB.png	\N	BLUE
14390	wF10_d2-2_cDAPI	\N	\N
14391	original/PLATE-1A/bPLATE_wF10_s5_cRGB.png	\N	GREEN
14392	wF10_d2-2_cGFP	\N	\N
14393	original/PLATE-1A/bPLATE_wF10_s5_cRGB.png	\N	RED
14394	wF10_d2-2_cCy5	\N	\N
14395	original/PLATE-1A/bPLATE_wA1_s2_cRGB.png	\N	BLUE
14396	wA1_d2-1_cDAPI	\N	\N
14397	original/PLATE-1A/bPLATE_wA1_s2_cRGB.png	\N	GREEN
14398	wA1_d2-1_cGFP	\N	\N
14399	original/PLATE-1A/bPLATE_wA1_s2_cRGB.png	\N	RED
14400	wA1_d2-1_cCy5	\N	\N
14401	original/PLATE-1A/bPLATE_wH4_s5_cRGB.png	\N	BLUE
14402	wH4_d2-2_cDAPI	\N	\N
14403	original/PLATE-1A/bPLATE_wH4_s5_cRGB.png	\N	GREEN
14404	wH4_d2-2_cGFP	\N	\N
14405	original/PLATE-1A/bPLATE_wH4_s5_cRGB.png	\N	RED
14406	wH4_d2-2_cCy5	\N	\N
14407	original/PLATE-1A/bPLATE_wE9_s8_cRGB.png	\N	BLUE
14408	wE9_d2-3_cDAPI	\N	\N
14409	original/PLATE-1A/bPLATE_wE9_s8_cRGB.png	\N	GREEN
14410	wE9_d2-3_cGFP	\N	\N
14411	original/PLATE-1A/bPLATE_wE9_s8_cRGB.png	\N	RED
14412	wE9_d2-3_cCy5	\N	\N
14413	original/PLATE-1A/bPLATE_wA9_s2_cRGB.png	\N	BLUE
14414	wA9_d2-1_cDAPI	\N	\N
14415	original/PLATE-1A/bPLATE_wA9_s2_cRGB.png	\N	GREEN
14416	wA9_d2-1_cGFP	\N	\N
14417	original/PLATE-1A/bPLATE_wA9_s2_cRGB.png	\N	RED
14418	wA9_d2-1_cCy5	\N	\N
14419	original/PLATE-1A/bPLATE_wB1_s4_cRGB.png	\N	BLUE
14420	wB1_d1-2_cDAPI	\N	\N
14421	original/PLATE-1A/bPLATE_wB1_s4_cRGB.png	\N	GREEN
14422	wB1_d1-2_cGFP	\N	\N
14423	original/PLATE-1A/bPLATE_wB1_s4_cRGB.png	\N	RED
14424	wB1_d1-2_cCy5	\N	\N
14425	original/PLATE-1A/bPLATE_wD8_s1_cRGB.png	\N	BLUE
14426	wD8_d1-1_cDAPI	\N	\N
14427	original/PLATE-1A/bPLATE_wD8_s1_cRGB.png	\N	GREEN
14428	wD8_d1-1_cGFP	\N	\N
14429	original/PLATE-1A/bPLATE_wD8_s1_cRGB.png	\N	RED
14430	wD8_d1-1_cCy5	\N	\N
14431	original/PLATE-1A/bPLATE_wC2_s6_cRGB.png	\N	BLUE
14432	wC2_d3-2_cDAPI	\N	\N
14433	original/PLATE-1A/bPLATE_wC2_s6_cRGB.png	\N	GREEN
14434	wC2_d3-2_cGFP	\N	\N
14435	original/PLATE-1A/bPLATE_wC2_s6_cRGB.png	\N	RED
14436	wC2_d3-2_cCy5	\N	\N
14437	original/PLATE-1A/bPLATE_wE9_s3_cRGB.png	\N	BLUE
14438	wE9_d3-1_cDAPI	\N	\N
14439	original/PLATE-1A/bPLATE_wE9_s3_cRGB.png	\N	GREEN
14440	wE9_d3-1_cGFP	\N	\N
14441	original/PLATE-1A/bPLATE_wE9_s3_cRGB.png	\N	RED
14442	wE9_d3-1_cCy5	\N	\N
14443	original/PLATE-1A/bPLATE_wC11_s6_cRGB.png	\N	BLUE
14444	wC11_d3-2_cDAPI	\N	\N
14445	original/PLATE-1A/bPLATE_wC11_s6_cRGB.png	\N	GREEN
14446	wC11_d3-2_cGFP	\N	\N
14447	original/PLATE-1A/bPLATE_wC11_s6_cRGB.png	\N	RED
14448	wC11_d3-2_cCy5	\N	\N
14449	original/PLATE-1A/bPLATE_wA4_s9_cRGB.png	\N	BLUE
14450	wA4_d3-3_cDAPI	\N	\N
14451	original/PLATE-1A/bPLATE_wA4_s9_cRGB.png	\N	GREEN
14452	wA4_d3-3_cGFP	\N	\N
14453	original/PLATE-1A/bPLATE_wA4_s9_cRGB.png	\N	RED
14454	wA4_d3-3_cCy5	\N	\N
14455	original/PLATE-1A/bPLATE_wF6_s3_cRGB.png	\N	BLUE
14456	wF6_d3-1_cDAPI	\N	\N
14457	original/PLATE-1A/bPLATE_wF6_s3_cRGB.png	\N	GREEN
14458	wF6_d3-1_cGFP	\N	\N
14459	original/PLATE-1A/bPLATE_wF6_s3_cRGB.png	\N	RED
14460	wF6_d3-1_cCy5	\N	\N
14461	original/PLATE-1A/bPLATE_wD11_s8_cRGB.png	\N	BLUE
14462	wD11_d2-3_cDAPI	\N	\N
14463	original/PLATE-1A/bPLATE_wD11_s8_cRGB.png	\N	GREEN
14464	wD11_d2-3_cGFP	\N	\N
14465	original/PLATE-1A/bPLATE_wD11_s8_cRGB.png	\N	RED
14466	wD11_d2-3_cCy5	\N	\N
14467	original/PLATE-1A/bPLATE_wG6_s5_cRGB.png	\N	BLUE
14468	wG6_d2-2_cDAPI	\N	\N
14469	original/PLATE-1A/bPLATE_wG6_s5_cRGB.png	\N	GREEN
14470	wG6_d2-2_cGFP	\N	\N
14471	original/PLATE-1A/bPLATE_wG6_s5_cRGB.png	\N	RED
14472	wG6_d2-2_cCy5	\N	\N
14473	original/PLATE-1A/bPLATE_wH1_s5_cRGB.png	\N	BLUE
14474	wH1_d2-2_cDAPI	\N	\N
14475	original/PLATE-1A/bPLATE_wH1_s5_cRGB.png	\N	GREEN
14476	wH1_d2-2_cGFP	\N	\N
14477	original/PLATE-1A/bPLATE_wH1_s5_cRGB.png	\N	RED
14478	wH1_d2-2_cCy5	\N	\N
14479	original/PLATE-1A/bPLATE_wE6_s8_cRGB.png	\N	BLUE
14480	wE6_d2-3_cDAPI	\N	\N
14481	original/PLATE-1A/bPLATE_wE6_s8_cRGB.png	\N	GREEN
14482	wE6_d2-3_cGFP	\N	\N
14483	original/PLATE-1A/bPLATE_wE6_s8_cRGB.png	\N	RED
14484	wE6_d2-3_cCy5	\N	\N
14485	original/PLATE-1A/bPLATE_wB2_s1_cRGB.png	\N	BLUE
14486	wB2_d1-1_cDAPI	\N	\N
14487	original/PLATE-1A/bPLATE_wB2_s1_cRGB.png	\N	GREEN
14488	wB2_d1-1_cGFP	\N	\N
14489	original/PLATE-1A/bPLATE_wB2_s1_cRGB.png	\N	RED
14490	wB2_d1-1_cCy5	\N	\N
14491	original/PLATE-1A/bPLATE_wA10_s4_cRGB.png	\N	BLUE
14492	wA10_d1-2_cDAPI	\N	\N
14493	original/PLATE-1A/bPLATE_wA10_s4_cRGB.png	\N	GREEN
14494	wA10_d1-2_cGFP	\N	\N
14495	original/PLATE-1A/bPLATE_wA10_s4_cRGB.png	\N	RED
14496	wA10_d1-2_cCy5	\N	\N
14497	original/PLATE-1A/bPLATE_wB4_s1_cRGB.png	\N	BLUE
14498	wB4_d1-1_cDAPI	\N	\N
14499	original/PLATE-1A/bPLATE_wB4_s1_cRGB.png	\N	GREEN
14500	wB4_d1-1_cGFP	\N	\N
14501	original/PLATE-1A/bPLATE_wB4_s1_cRGB.png	\N	RED
14502	wB4_d1-1_cCy5	\N	\N
14503	original/PLATE-1A/bPLATE_wD5_s1_cRGB.png	\N	BLUE
14504	wD5_d1-1_cDAPI	\N	\N
14505	original/PLATE-1A/bPLATE_wD5_s1_cRGB.png	\N	GREEN
14506	wD5_d1-1_cGFP	\N	\N
14507	original/PLATE-1A/bPLATE_wD5_s1_cRGB.png	\N	RED
14508	wD5_d1-1_cCy5	\N	\N
14509	original/PLATE-1A/bPLATE_wH3_s3_cRGB.png	\N	BLUE
14510	wH3_d3-1_cDAPI	\N	\N
14511	original/PLATE-1A/bPLATE_wH3_s3_cRGB.png	\N	GREEN
14512	wH3_d3-1_cGFP	\N	\N
14513	original/PLATE-1A/bPLATE_wH3_s3_cRGB.png	\N	RED
14514	wH3_d3-1_cCy5	\N	\N
14515	original/PLATE-1A/bPLATE_wC1_s9_cRGB.png	\N	BLUE
14516	wC1_d3-3_cDAPI	\N	\N
14517	original/PLATE-1A/bPLATE_wC1_s9_cRGB.png	\N	GREEN
14518	wC1_d3-3_cGFP	\N	\N
14519	original/PLATE-1A/bPLATE_wC1_s9_cRGB.png	\N	RED
14520	wC1_d3-3_cCy5	\N	\N
14521	original/PLATE-1A/bPLATE_wE8_s6_cRGB.png	\N	BLUE
14522	wE8_d3-2_cDAPI	\N	\N
14523	original/PLATE-1A/bPLATE_wE8_s6_cRGB.png	\N	GREEN
14524	wE8_d3-2_cGFP	\N	\N
14525	original/PLATE-1A/bPLATE_wE8_s6_cRGB.png	\N	RED
14526	wE8_d3-2_cCy5	\N	\N
14527	original/PLATE-1A/bPLATE_wD12_s9_cRGB.png	\N	BLUE
14528	wD12_d3-3_cDAPI	\N	\N
14529	original/PLATE-1A/bPLATE_wD12_s9_cRGB.png	\N	GREEN
14530	wD12_d3-3_cGFP	\N	\N
14531	original/PLATE-1A/bPLATE_wD12_s9_cRGB.png	\N	RED
14532	wD12_d3-3_cCy5	\N	\N
14533	original/PLATE-1A/bPLATE_wG7_s6_cRGB.png	\N	BLUE
14534	wG7_d3-2_cDAPI	\N	\N
14535	original/PLATE-1A/bPLATE_wG7_s6_cRGB.png	\N	GREEN
14536	wG7_d3-2_cGFP	\N	\N
14537	original/PLATE-1A/bPLATE_wG7_s6_cRGB.png	\N	RED
14538	wG7_d3-2_cCy5	\N	\N
14539	original/PLATE-1A/bPLATE_wE4_s9_cRGB.png	\N	BLUE
14540	wE4_d3-3_cDAPI	\N	\N
14541	original/PLATE-1A/bPLATE_wE4_s9_cRGB.png	\N	GREEN
14542	wE4_d3-3_cGFP	\N	\N
14543	original/PLATE-1A/bPLATE_wE4_s9_cRGB.png	\N	RED
14544	wE4_d3-3_cCy5	\N	\N
14545	original/PLATE-1A/bPLATE_wG11_s6_cRGB.png	\N	BLUE
14546	wG11_d3-2_cDAPI	\N	\N
14547	original/PLATE-1A/bPLATE_wG11_s6_cRGB.png	\N	GREEN
14548	wG11_d3-2_cGFP	\N	\N
14549	original/PLATE-1A/bPLATE_wG11_s6_cRGB.png	\N	RED
14550	wG11_d3-2_cCy5	\N	\N
14551	original/PLATE-1A/bPLATE_wB4_s2_cRGB.png	\N	BLUE
14552	wB4_d2-1_cDAPI	\N	\N
14553	original/PLATE-1A/bPLATE_wB4_s2_cRGB.png	\N	GREEN
14554	wB4_d2-1_cGFP	\N	\N
14555	original/PLATE-1A/bPLATE_wB4_s2_cRGB.png	\N	RED
14556	wB4_d2-1_cCy5	\N	\N
14557	original/PLATE-1A/bPLATE_wF6_s7_cRGB.png	\N	BLUE
14558	wF6_d1-3_cDAPI	\N	\N
14559	original/PLATE-1A/bPLATE_wF6_s7_cRGB.png	\N	GREEN
14560	wF6_d1-3_cGFP	\N	\N
14561	original/PLATE-1A/bPLATE_wF6_s7_cRGB.png	\N	RED
14562	wF6_d1-3_cCy5	\N	\N
14563	original/PLATE-1A/bPLATE_wB1_s2_cRGB.png	\N	BLUE
14564	wB1_d2-1_cDAPI	\N	\N
14565	original/PLATE-1A/bPLATE_wB1_s2_cRGB.png	\N	GREEN
14566	wB1_d2-1_cGFP	\N	\N
14567	original/PLATE-1A/bPLATE_wB1_s2_cRGB.png	\N	RED
14568	wB1_d2-1_cCy5	\N	\N
14569	original/PLATE-1A/bPLATE_wH6_s8_cRGB.png	\N	BLUE
14570	wH6_d2-3_cDAPI	\N	\N
14571	original/PLATE-1A/bPLATE_wH6_s8_cRGB.png	\N	GREEN
14572	wH6_d2-3_cGFP	\N	\N
14573	original/PLATE-1A/bPLATE_wH6_s8_cRGB.png	\N	RED
14574	wH6_d2-3_cCy5	\N	\N
14575	original/PLATE-1A/bPLATE_wC1_s2_cRGB.png	\N	BLUE
14576	wC1_d2-1_cDAPI	\N	\N
14577	original/PLATE-1A/bPLATE_wC1_s2_cRGB.png	\N	GREEN
14578	wC1_d2-1_cGFP	\N	\N
14579	original/PLATE-1A/bPLATE_wC1_s2_cRGB.png	\N	RED
14580	wC1_d2-1_cCy5	\N	\N
14581	original/PLATE-1A/bPLATE_wA11_s5_cRGB.png	\N	BLUE
14582	wA11_d2-2_cDAPI	\N	\N
14583	original/PLATE-1A/bPLATE_wA11_s5_cRGB.png	\N	GREEN
14584	wA11_d2-2_cGFP	\N	\N
14585	original/PLATE-1A/bPLATE_wA11_s5_cRGB.png	\N	RED
14586	wA11_d2-2_cCy5	\N	\N
14587	original/PLATE-1A/bPLATE_wD6_s2_cRGB.png	\N	BLUE
14588	wD6_d2-1_cDAPI	\N	\N
14589	original/PLATE-1A/bPLATE_wD6_s2_cRGB.png	\N	GREEN
14590	wD6_d2-1_cGFP	\N	\N
14591	original/PLATE-1A/bPLATE_wD6_s2_cRGB.png	\N	RED
14592	wD6_d2-1_cCy5	\N	\N
14593	original/PLATE-1A/bPLATE_wA8_s3_cRGB.png	\N	BLUE
14594	wA8_d3-1_cDAPI	\N	\N
14595	original/PLATE-1A/bPLATE_wA8_s3_cRGB.png	\N	GREEN
14596	wA8_d3-1_cGFP	\N	\N
14597	original/PLATE-1A/bPLATE_wA8_s3_cRGB.png	\N	RED
14598	wA8_d3-1_cCy5	\N	\N
14599	original/PLATE-1A/bPLATE_wA6_s1_cRGB.png	\N	BLUE
14600	wA6_d1-1_cDAPI	\N	\N
14601	original/PLATE-1A/bPLATE_wA6_s1_cRGB.png	\N	GREEN
14602	wA6_d1-1_cGFP	\N	\N
14603	original/PLATE-1A/bPLATE_wA6_s1_cRGB.png	\N	RED
14604	wA6_d1-1_cCy5	\N	\N
14605	original/PLATE-1A/bPLATE_wG6_s7_cRGB.png	\N	BLUE
14606	wG6_d1-3_cDAPI	\N	\N
14607	original/PLATE-1A/bPLATE_wG6_s7_cRGB.png	\N	GREEN
14608	wG6_d1-3_cGFP	\N	\N
14609	original/PLATE-1A/bPLATE_wG6_s7_cRGB.png	\N	RED
14610	wG6_d1-3_cCy5	\N	\N
14611	original/PLATE-1A/bPLATE_wH3_s6_cRGB.png	\N	BLUE
14612	wH3_d3-2_cDAPI	\N	\N
14613	original/PLATE-1A/bPLATE_wH3_s6_cRGB.png	\N	GREEN
14614	wH3_d3-2_cGFP	\N	\N
14615	original/PLATE-1A/bPLATE_wH3_s6_cRGB.png	\N	RED
14616	wH3_d3-2_cCy5	\N	\N
14617	original/PLATE-1A/bPLATE_wE8_s9_cRGB.png	\N	BLUE
14618	wE8_d3-3_cDAPI	\N	\N
14619	original/PLATE-1A/bPLATE_wE8_s9_cRGB.png	\N	GREEN
14620	wE8_d3-3_cGFP	\N	\N
14621	original/PLATE-1A/bPLATE_wE8_s9_cRGB.png	\N	RED
14622	wE8_d3-3_cCy5	\N	\N
14623	original/PLATE-1A/bPLATE_wE3_s8_cRGB.png	\N	BLUE
14624	wE3_d2-3_cDAPI	\N	\N
14625	original/PLATE-1A/bPLATE_wE3_s8_cRGB.png	\N	GREEN
14626	wE3_d2-3_cGFP	\N	\N
14627	original/PLATE-1A/bPLATE_wE3_s8_cRGB.png	\N	RED
14628	wE3_d2-3_cCy5	\N	\N
14629	original/PLATE-1A/bPLATE_wG10_s5_cRGB.png	\N	BLUE
14630	wG10_d2-2_cDAPI	\N	\N
14631	original/PLATE-1A/bPLATE_wG10_s5_cRGB.png	\N	GREEN
14632	wG10_d2-2_cGFP	\N	\N
14633	original/PLATE-1A/bPLATE_wG10_s5_cRGB.png	\N	RED
14634	wG10_d2-2_cCy5	\N	\N
14635	original/PLATE-1A/bPLATE_wA4_s3_cRGB.png	\N	BLUE
14636	wA4_d3-1_cDAPI	\N	\N
14637	original/PLATE-1A/bPLATE_wA4_s3_cRGB.png	\N	GREEN
14638	wA4_d3-1_cGFP	\N	\N
14639	original/PLATE-1A/bPLATE_wA4_s3_cRGB.png	\N	RED
14640	wA4_d3-1_cCy5	\N	\N
14641	original/PLATE-1A/bPLATE_wB9_s1_cRGB.png	\N	BLUE
14642	wB9_d1-1_cDAPI	\N	\N
14643	original/PLATE-1A/bPLATE_wB9_s1_cRGB.png	\N	GREEN
14644	wB9_d1-1_cGFP	\N	\N
14645	original/PLATE-1A/bPLATE_wB9_s1_cRGB.png	\N	RED
14646	wB9_d1-1_cCy5	\N	\N
14647	original/PLATE-1A/bPLATE_wD1_s1_cRGB.png	\N	BLUE
14648	wD1_d1-1_cDAPI	\N	\N
14649	original/PLATE-1A/bPLATE_wD1_s1_cRGB.png	\N	GREEN
14650	wD1_d1-1_cGFP	\N	\N
14651	original/PLATE-1A/bPLATE_wD1_s1_cRGB.png	\N	RED
14652	wD1_d1-1_cCy5	\N	\N
14653	original/PLATE-1A/bPLATE_wA6_s4_cRGB.png	\N	BLUE
14654	wA6_d1-2_cDAPI	\N	\N
14655	original/PLATE-1A/bPLATE_wA6_s4_cRGB.png	\N	GREEN
14656	wA6_d1-2_cGFP	\N	\N
14657	original/PLATE-1A/bPLATE_wA6_s4_cRGB.png	\N	RED
14658	wA6_d1-2_cCy5	\N	\N
14659	original/PLATE-1A/bPLATE_wH4_s9_cRGB.png	\N	BLUE
14660	wH4_d3-3_cDAPI	\N	\N
14661	original/PLATE-1A/bPLATE_wH4_s9_cRGB.png	\N	GREEN
14662	wH4_d3-3_cGFP	\N	\N
14663	original/PLATE-1A/bPLATE_wH4_s9_cRGB.png	\N	RED
14664	wH4_d3-3_cCy5	\N	\N
14665	original/PLATE-1A/bPLATE_wB2_s3_cRGB.png	\N	BLUE
14666	wB2_d3-1_cDAPI	\N	\N
14667	original/PLATE-1A/bPLATE_wB2_s3_cRGB.png	\N	GREEN
14668	wB2_d3-1_cGFP	\N	\N
14669	original/PLATE-1A/bPLATE_wB2_s3_cRGB.png	\N	RED
14670	wB2_d3-1_cCy5	\N	\N
14671	original/PLATE-1A/bPLATE_wH5_s4_cRGB.png	\N	BLUE
14672	wH5_d1-2_cDAPI	\N	\N
14673	original/PLATE-1A/bPLATE_wH5_s4_cRGB.png	\N	GREEN
14674	wH5_d1-2_cGFP	\N	\N
14675	original/PLATE-1A/bPLATE_wH5_s4_cRGB.png	\N	RED
14676	wH5_d1-2_cCy5	\N	\N
14677	original/PLATE-1A/bPLATE_wD2_s6_cRGB.png	\N	BLUE
14678	wD2_d3-2_cDAPI	\N	\N
14679	original/PLATE-1A/bPLATE_wD2_s6_cRGB.png	\N	GREEN
14680	wD2_d3-2_cGFP	\N	\N
14681	original/PLATE-1A/bPLATE_wD2_s6_cRGB.png	\N	RED
14682	wD2_d3-2_cCy5	\N	\N
14683	original/PLATE-1A/bPLATE_wA7_s9_cRGB.png	\N	BLUE
14684	wA7_d3-3_cDAPI	\N	\N
14685	original/PLATE-1A/bPLATE_wA7_s9_cRGB.png	\N	GREEN
14686	wA7_d3-3_cGFP	\N	\N
14687	original/PLATE-1A/bPLATE_wA7_s9_cRGB.png	\N	RED
14688	wA7_d3-3_cCy5	\N	\N
14689	original/PLATE-1A/bPLATE_wE10_s7_cRGB.png	\N	BLUE
14690	wE10_d1-3_cDAPI	\N	\N
14691	original/PLATE-1A/bPLATE_wE10_s7_cRGB.png	\N	GREEN
14692	wE10_d1-3_cGFP	\N	\N
14693	original/PLATE-1A/bPLATE_wE10_s7_cRGB.png	\N	RED
14694	wE10_d1-3_cCy5	\N	\N
14695	original/PLATE-1A/bPLATE_wF9_s3_cRGB.png	\N	BLUE
14696	wF9_d3-1_cDAPI	\N	\N
14697	original/PLATE-1A/bPLATE_wF9_s3_cRGB.png	\N	GREEN
14698	wF9_d3-1_cGFP	\N	\N
14699	original/PLATE-1A/bPLATE_wF9_s3_cRGB.png	\N	RED
14700	wF9_d3-1_cCy5	\N	\N
14701	original/PLATE-1A/bPLATE_wE2_s7_cRGB.png	\N	BLUE
14702	wE2_d1-3_cDAPI	\N	\N
14703	original/PLATE-1A/bPLATE_wE2_s7_cRGB.png	\N	GREEN
14704	wE2_d1-3_cGFP	\N	\N
14705	original/PLATE-1A/bPLATE_wE2_s7_cRGB.png	\N	RED
14706	wE2_d1-3_cCy5	\N	\N
14707	original/PLATE-1A/bPLATE_wG9_s4_cRGB.png	\N	BLUE
14708	wG9_d1-2_cDAPI	\N	\N
14709	original/PLATE-1A/bPLATE_wG9_s4_cRGB.png	\N	GREEN
14710	wG9_d1-2_cGFP	\N	\N
14711	original/PLATE-1A/bPLATE_wG9_s4_cRGB.png	\N	RED
14712	wG9_d1-2_cCy5	\N	\N
14713	original/PLATE-1A/bPLATE_wG2_s7_cRGB.png	\N	BLUE
14714	wG2_d1-3_cDAPI	\N	\N
14715	original/PLATE-1A/bPLATE_wG2_s7_cRGB.png	\N	GREEN
14716	wG2_d1-3_cGFP	\N	\N
14717	original/PLATE-1A/bPLATE_wG2_s7_cRGB.png	\N	RED
14718	wG2_d1-3_cCy5	\N	\N
14719	original/PLATE-1A/bPLATE_wH1_s4_cRGB.png	\N	BLUE
14720	wH1_d1-2_cDAPI	\N	\N
14721	original/PLATE-1A/bPLATE_wH1_s4_cRGB.png	\N	GREEN
14722	wH1_d1-2_cGFP	\N	\N
14723	original/PLATE-1A/bPLATE_wH1_s4_cRGB.png	\N	RED
14724	wH1_d1-2_cCy5	\N	\N
14725	original/PLATE-1A/bPLATE_wE6_s7_cRGB.png	\N	BLUE
14726	wE6_d1-3_cDAPI	\N	\N
14727	original/PLATE-1A/bPLATE_wE6_s7_cRGB.png	\N	GREEN
14728	wE6_d1-3_cGFP	\N	\N
14729	original/PLATE-1A/bPLATE_wE6_s7_cRGB.png	\N	RED
14730	wE6_d1-3_cCy5	\N	\N
14731	original/PLATE-1A/bPLATE_wC10_s5_cRGB.png	\N	BLUE
14732	wC10_d2-2_cDAPI	\N	\N
14733	original/PLATE-1A/bPLATE_wC10_s5_cRGB.png	\N	GREEN
14734	wC10_d2-2_cGFP	\N	\N
14735	original/PLATE-1A/bPLATE_wC10_s5_cRGB.png	\N	RED
14736	wC10_d2-2_cCy5	\N	\N
14737	original/PLATE-1A/bPLATE_wA3_s8_cRGB.png	\N	BLUE
14738	wA3_d2-3_cDAPI	\N	\N
14739	original/PLATE-1A/bPLATE_wA3_s8_cRGB.png	\N	GREEN
14740	wA3_d2-3_cGFP	\N	\N
14741	original/PLATE-1A/bPLATE_wA3_s8_cRGB.png	\N	RED
14742	wA3_d2-3_cCy5	\N	\N
14743	original/PLATE-1A/bPLATE_wF5_s2_cRGB.png	\N	BLUE
14744	wF5_d2-1_cDAPI	\N	\N
14745	original/PLATE-1A/bPLATE_wF5_s2_cRGB.png	\N	GREEN
14746	wF5_d2-1_cGFP	\N	\N
14747	original/PLATE-1A/bPLATE_wF5_s2_cRGB.png	\N	RED
14748	wF5_d2-1_cCy5	\N	\N
14749	original/PLATE-1A/bPLATE_wG8_s9_cRGB.png	\N	BLUE
14750	wG8_d3-3_cDAPI	\N	\N
14751	original/PLATE-1A/bPLATE_wG8_s9_cRGB.png	\N	GREEN
14752	wG8_d3-3_cGFP	\N	\N
14753	original/PLATE-1A/bPLATE_wG8_s9_cRGB.png	\N	RED
14754	wG8_d3-3_cCy5	\N	\N
14755	original/PLATE-1A/bPLATE_wA10_s2_cRGB.png	\N	BLUE
14756	wA10_d2-1_cDAPI	\N	\N
14757	original/PLATE-1A/bPLATE_wA10_s2_cRGB.png	\N	GREEN
14758	wA10_d2-1_cGFP	\N	\N
14759	original/PLATE-1A/bPLATE_wA10_s2_cRGB.png	\N	RED
14760	wA10_d2-1_cCy5	\N	\N
14761	original/PLATE-1A/bPLATE_wH5_s3_cRGB.png	\N	BLUE
14762	wH5_d3-1_cDAPI	\N	\N
14763	original/PLATE-1A/bPLATE_wH5_s3_cRGB.png	\N	GREEN
14764	wH5_d3-1_cGFP	\N	\N
14765	original/PLATE-1A/bPLATE_wH5_s3_cRGB.png	\N	RED
14766	wH5_d3-1_cCy5	\N	\N
14767	original/PLATE-1A/bPLATE_wC3_s9_cRGB.png	\N	BLUE
14768	wC3_d3-3_cDAPI	\N	\N
14769	original/PLATE-1A/bPLATE_wC3_s9_cRGB.png	\N	GREEN
14770	wC3_d3-3_cGFP	\N	\N
14771	original/PLATE-1A/bPLATE_wC3_s9_cRGB.png	\N	RED
14772	wC3_d3-3_cCy5	\N	\N
14773	original/PLATE-1A/bPLATE_wA12_s3_cRGB.png	\N	BLUE
14774	wA12_d3-1_cDAPI	\N	\N
14775	original/PLATE-1A/bPLATE_wA12_s3_cRGB.png	\N	GREEN
14776	wA12_d3-1_cGFP	\N	\N
14777	original/PLATE-1A/bPLATE_wA12_s3_cRGB.png	\N	RED
14778	wA12_d3-1_cCy5	\N	\N
14779	original/PLATE-1A/bPLATE_wE10_s6_cRGB.png	\N	BLUE
14780	wE10_d3-2_cDAPI	\N	\N
14781	original/PLATE-1A/bPLATE_wE10_s6_cRGB.png	\N	GREEN
14782	wE10_d3-2_cGFP	\N	\N
14783	original/PLATE-1A/bPLATE_wE10_s6_cRGB.png	\N	RED
14784	wE10_d3-2_cCy5	\N	\N
14785	original/PLATE-1A/bPLATE_wH8_s2_cRGB.png	\N	BLUE
14786	wH8_d2-1_cDAPI	\N	\N
14787	original/PLATE-1A/bPLATE_wH8_s2_cRGB.png	\N	GREEN
14788	wH8_d2-1_cGFP	\N	\N
14789	original/PLATE-1A/bPLATE_wH8_s2_cRGB.png	\N	RED
14790	wH8_d2-1_cCy5	\N	\N
14791	original/PLATE-1A/bPLATE_wC6_s8_cRGB.png	\N	BLUE
14792	wC6_d2-3_cDAPI	\N	\N
14793	original/PLATE-1A/bPLATE_wC6_s8_cRGB.png	\N	GREEN
14794	wC6_d2-3_cGFP	\N	\N
14795	original/PLATE-1A/bPLATE_wC6_s8_cRGB.png	\N	RED
14796	wC6_d2-3_cCy5	\N	\N
14797	original/PLATE-1A/bPLATE_wC5_s4_cRGB.png	\N	BLUE
14798	wC5_d1-2_cDAPI	\N	\N
14799	original/PLATE-1A/bPLATE_wC5_s4_cRGB.png	\N	GREEN
14800	wC5_d1-2_cGFP	\N	\N
14801	original/PLATE-1A/bPLATE_wC5_s4_cRGB.png	\N	RED
14802	wC5_d1-2_cCy5	\N	\N
14803	original/PLATE-1A/bPLATE_wE12_s1_cRGB.png	\N	BLUE
14804	wE12_d1-1_cDAPI	\N	\N
14805	original/PLATE-1A/bPLATE_wE12_s1_cRGB.png	\N	GREEN
14806	wE12_d1-1_cGFP	\N	\N
14807	original/PLATE-1A/bPLATE_wE12_s1_cRGB.png	\N	RED
14808	wE12_d1-1_cCy5	\N	\N
14809	original/PLATE-1A/bPLATE_wF1_s5_cRGB.png	\N	BLUE
14810	wF1_d2-2_cDAPI	\N	\N
14811	original/PLATE-1A/bPLATE_wF1_s5_cRGB.png	\N	GREEN
14812	wF1_d2-2_cGFP	\N	\N
14813	original/PLATE-1A/bPLATE_wF1_s5_cRGB.png	\N	RED
14814	wF1_d2-2_cCy5	\N	\N
14815	original/PLATE-1A/bPLATE_wC12_s4_cRGB.png	\N	BLUE
14816	wC12_d1-2_cDAPI	\N	\N
14817	original/PLATE-1A/bPLATE_wC12_s4_cRGB.png	\N	GREEN
14818	wC12_d1-2_cGFP	\N	\N
14819	original/PLATE-1A/bPLATE_wC12_s4_cRGB.png	\N	RED
14820	wC12_d1-2_cCy5	\N	\N
14821	original/PLATE-1A/bPLATE_wA5_s7_cRGB.png	\N	BLUE
14822	wA5_d1-3_cDAPI	\N	\N
14823	original/PLATE-1A/bPLATE_wA5_s7_cRGB.png	\N	GREEN
14824	wA5_d1-3_cGFP	\N	\N
14825	original/PLATE-1A/bPLATE_wA5_s7_cRGB.png	\N	RED
14826	wA5_d1-3_cCy5	\N	\N
14827	original/PLATE-1A/bPLATE_wF7_s1_cRGB.png	\N	BLUE
14828	wF7_d1-1_cDAPI	\N	\N
14829	original/PLATE-1A/bPLATE_wF7_s1_cRGB.png	\N	GREEN
14830	wF7_d1-1_cGFP	\N	\N
14831	original/PLATE-1A/bPLATE_wF7_s1_cRGB.png	\N	RED
14832	wF7_d1-1_cCy5	\N	\N
14833	original/PLATE-1A/bPLATE_wA2_s1_cRGB.png	\N	BLUE
14834	wA2_d1-1_cDAPI	\N	\N
14835	original/PLATE-1A/bPLATE_wA2_s1_cRGB.png	\N	GREEN
14836	wA2_d1-1_cGFP	\N	\N
14837	original/PLATE-1A/bPLATE_wA2_s1_cRGB.png	\N	RED
14838	wA2_d1-1_cCy5	\N	\N
14839	original/PLATE-1A/bPLATE_wD12_s4_cRGB.png	\N	BLUE
14840	wD12_d1-2_cDAPI	\N	\N
14841	original/PLATE-1A/bPLATE_wD12_s4_cRGB.png	\N	GREEN
14842	wD12_d1-2_cGFP	\N	\N
14843	original/PLATE-1A/bPLATE_wD12_s4_cRGB.png	\N	RED
14844	wD12_d1-2_cCy5	\N	\N
14845	original/PLATE-1A/bPLATE_wB5_s7_cRGB.png	\N	BLUE
14846	wB5_d1-3_cDAPI	\N	\N
14847	original/PLATE-1A/bPLATE_wB5_s7_cRGB.png	\N	GREEN
14848	wB5_d1-3_cGFP	\N	\N
14849	original/PLATE-1A/bPLATE_wB5_s7_cRGB.png	\N	RED
14850	wB5_d1-3_cCy5	\N	\N
14851	original/PLATE-1A/bPLATE_wG7_s1_cRGB.png	\N	BLUE
14852	wG7_d1-1_cDAPI	\N	\N
14853	original/PLATE-1A/bPLATE_wG7_s1_cRGB.png	\N	GREEN
14854	wG7_d1-1_cGFP	\N	\N
14855	original/PLATE-1A/bPLATE_wG7_s1_cRGB.png	\N	RED
14856	wG7_d1-1_cCy5	\N	\N
14857	original/PLATE-1A/bPLATE_wF8_s9_cRGB.png	\N	BLUE
14858	wF8_d3-3_cDAPI	\N	\N
14859	original/PLATE-1A/bPLATE_wF8_s9_cRGB.png	\N	GREEN
14860	wF8_d3-3_cGFP	\N	\N
14861	original/PLATE-1A/bPLATE_wF8_s9_cRGB.png	\N	RED
14862	wF8_d3-3_cCy5	\N	\N
14863	original/PLATE-1A/bPLATE_wH3_s8_cRGB.png	\N	BLUE
14864	wH3_d2-3_cDAPI	\N	\N
14865	original/PLATE-1A/bPLATE_wH3_s8_cRGB.png	\N	GREEN
14866	wH3_d2-3_cGFP	\N	\N
14867	original/PLATE-1A/bPLATE_wH3_s8_cRGB.png	\N	RED
14868	wH3_d2-3_cCy5	\N	\N
14869	original/PLATE-1A/bPLATE_wE2_s9_cRGB.png	\N	BLUE
14870	wE2_d3-3_cDAPI	\N	\N
14871	original/PLATE-1A/bPLATE_wE2_s9_cRGB.png	\N	GREEN
14872	wE2_d3-3_cGFP	\N	\N
14873	original/PLATE-1A/bPLATE_wE2_s9_cRGB.png	\N	RED
14874	wE2_d3-3_cCy5	\N	\N
14875	original/PLATE-1A/bPLATE_wG9_s6_cRGB.png	\N	BLUE
14876	wG9_d3-2_cDAPI	\N	\N
14877	original/PLATE-1A/bPLATE_wG9_s6_cRGB.png	\N	GREEN
14878	wG9_d3-2_cGFP	\N	\N
14879	original/PLATE-1A/bPLATE_wG9_s6_cRGB.png	\N	RED
14880	wG9_d3-2_cCy5	\N	\N
14881	original/PLATE-1A/bPLATE_wB7_s7_cRGB.png	\N	BLUE
14882	wB7_d1-3_cDAPI	\N	\N
14883	original/PLATE-1A/bPLATE_wB7_s7_cRGB.png	\N	GREEN
14884	wB7_d1-3_cGFP	\N	\N
14885	original/PLATE-1A/bPLATE_wB7_s7_cRGB.png	\N	RED
14886	wB7_d1-3_cCy5	\N	\N
14887	original/PLATE-1A/bPLATE_wE2_s4_cRGB.png	\N	BLUE
14888	wE2_d1-2_cDAPI	\N	\N
14889	original/PLATE-1A/bPLATE_wE2_s4_cRGB.png	\N	GREEN
14890	wE2_d1-2_cGFP	\N	\N
14891	original/PLATE-1A/bPLATE_wE2_s4_cRGB.png	\N	RED
14892	wE2_d1-2_cCy5	\N	\N
14893	original/PLATE-1A/bPLATE_wG9_s1_cRGB.png	\N	BLUE
14894	wG9_d1-1_cDAPI	\N	\N
14895	original/PLATE-1A/bPLATE_wG9_s1_cRGB.png	\N	GREEN
14896	wG9_d1-1_cGFP	\N	\N
14897	original/PLATE-1A/bPLATE_wG9_s1_cRGB.png	\N	RED
14898	wG9_d1-1_cCy5	\N	\N
14899	original/PLATE-1A/bPLATE_wB8_s4_cRGB.png	\N	BLUE
14900	wB8_d1-2_cDAPI	\N	\N
14901	original/PLATE-1A/bPLATE_wB8_s4_cRGB.png	\N	GREEN
14902	wB8_d1-2_cGFP	\N	\N
14903	original/PLATE-1A/bPLATE_wB8_s4_cRGB.png	\N	RED
14904	wB8_d1-2_cCy5	\N	\N
14905	original/PLATE-1A/bPLATE_wE3_s1_cRGB.png	\N	BLUE
14906	wE3_d1-1_cDAPI	\N	\N
14907	original/PLATE-1A/bPLATE_wE3_s1_cRGB.png	\N	GREEN
14908	wE3_d1-1_cGFP	\N	\N
14909	original/PLATE-1A/bPLATE_wE3_s1_cRGB.png	\N	RED
14910	wE3_d1-1_cCy5	\N	\N
14911	original/PLATE-1A/bPLATE_wD2_s8_cRGB.png	\N	BLUE
14912	wD2_d2-3_cDAPI	\N	\N
14913	original/PLATE-1A/bPLATE_wD2_s8_cRGB.png	\N	GREEN
14914	wD2_d2-3_cGFP	\N	\N
14915	original/PLATE-1A/bPLATE_wD2_s8_cRGB.png	\N	RED
14916	wD2_d2-3_cCy5	\N	\N
14917	original/PLATE-1A/bPLATE_wF9_s5_cRGB.png	\N	BLUE
14918	wF9_d2-2_cDAPI	\N	\N
14919	original/PLATE-1A/bPLATE_wF9_s5_cRGB.png	\N	GREEN
14920	wF9_d2-2_cGFP	\N	\N
14921	original/PLATE-1A/bPLATE_wF9_s5_cRGB.png	\N	RED
14922	wF9_d2-2_cCy5	\N	\N
14923	original/PLATE-1A/bPLATE_wA10_s5_cRGB.png	\N	BLUE
14924	wA10_d2-2_cDAPI	\N	\N
14925	original/PLATE-1A/bPLATE_wA10_s5_cRGB.png	\N	GREEN
14926	wA10_d2-2_cGFP	\N	\N
14927	original/PLATE-1A/bPLATE_wA10_s5_cRGB.png	\N	RED
14928	wA10_d2-2_cCy5	\N	\N
14929	original/PLATE-1A/bPLATE_wD5_s2_cRGB.png	\N	BLUE
14930	wD5_d2-1_cDAPI	\N	\N
14931	original/PLATE-1A/bPLATE_wD5_s2_cRGB.png	\N	GREEN
14932	wD5_d2-1_cGFP	\N	\N
14933	original/PLATE-1A/bPLATE_wD5_s2_cRGB.png	\N	RED
14934	wD5_d2-1_cCy5	\N	\N
14935	original/PLATE-1A/bPLATE_wG4_s9_cRGB.png	\N	BLUE
14936	wG4_d3-3_cDAPI	\N	\N
14937	original/PLATE-1A/bPLATE_wG4_s9_cRGB.png	\N	GREEN
14938	wG4_d3-3_cGFP	\N	\N
14939	original/PLATE-1A/bPLATE_wG4_s9_cRGB.png	\N	RED
14940	wG4_d3-3_cCy5	\N	\N
14941	original/PLATE-1A/bPLATE_wD2_s5_cRGB.png	\N	BLUE
14942	wD2_d2-2_cDAPI	\N	\N
14943	original/PLATE-1A/bPLATE_wD2_s5_cRGB.png	\N	GREEN
14944	wD2_d2-2_cGFP	\N	\N
14945	original/PLATE-1A/bPLATE_wD2_s5_cRGB.png	\N	RED
14946	wD2_d2-2_cCy5	\N	\N
14947	original/PLATE-1A/bPLATE_wA7_s8_cRGB.png	\N	BLUE
14948	wA7_d2-3_cDAPI	\N	\N
14949	original/PLATE-1A/bPLATE_wA7_s8_cRGB.png	\N	GREEN
14950	wA7_d2-3_cGFP	\N	\N
14951	original/PLATE-1A/bPLATE_wA7_s8_cRGB.png	\N	RED
14952	wA7_d2-3_cCy5	\N	\N
14953	original/PLATE-1A/bPLATE_wF9_s2_cRGB.png	\N	BLUE
14954	wF9_d2-1_cDAPI	\N	\N
14955	original/PLATE-1A/bPLATE_wF9_s2_cRGB.png	\N	GREEN
14956	wF9_d2-1_cGFP	\N	\N
14957	original/PLATE-1A/bPLATE_wF9_s2_cRGB.png	\N	RED
14958	wF9_d2-1_cCy5	\N	\N
14959	original/PLATE-1A/bPLATE_wH3_s1_cRGB.png	\N	BLUE
14960	wH3_d1-1_cDAPI	\N	\N
14961	original/PLATE-1A/bPLATE_wH3_s1_cRGB.png	\N	GREEN
14962	wH3_d1-1_cGFP	\N	\N
14963	original/PLATE-1A/bPLATE_wH3_s1_cRGB.png	\N	RED
14964	wH3_d1-1_cCy5	\N	\N
14965	original/PLATE-1A/bPLATE_wC1_s7_cRGB.png	\N	BLUE
14966	wC1_d1-3_cDAPI	\N	\N
14967	original/PLATE-1A/bPLATE_wC1_s7_cRGB.png	\N	GREEN
14968	wC1_d1-3_cGFP	\N	\N
14969	original/PLATE-1A/bPLATE_wC1_s7_cRGB.png	\N	RED
14970	wC1_d1-3_cCy5	\N	\N
14971	original/PLATE-1A/bPLATE_wE8_s4_cRGB.png	\N	BLUE
14972	wE8_d1-2_cDAPI	\N	\N
14973	original/PLATE-1A/bPLATE_wE8_s4_cRGB.png	\N	GREEN
14974	wE8_d1-2_cGFP	\N	\N
14975	original/PLATE-1A/bPLATE_wE8_s4_cRGB.png	\N	RED
14976	wE8_d1-2_cCy5	\N	\N
14977	original/PLATE-1A/bPLATE_wB1_s8_cRGB.png	\N	BLUE
14978	wB1_d2-3_cDAPI	\N	\N
14979	original/PLATE-1A/bPLATE_wB1_s8_cRGB.png	\N	GREEN
14980	wB1_d2-3_cGFP	\N	\N
14981	original/PLATE-1A/bPLATE_wB1_s8_cRGB.png	\N	RED
14982	wB1_d2-3_cCy5	\N	\N
14983	original/PLATE-1A/bPLATE_wD8_s5_cRGB.png	\N	BLUE
14984	wD8_d2-2_cDAPI	\N	\N
14985	original/PLATE-1A/bPLATE_wD8_s5_cRGB.png	\N	GREEN
14986	wD8_d2-2_cGFP	\N	\N
14987	original/PLATE-1A/bPLATE_wD8_s5_cRGB.png	\N	RED
14988	wD8_d2-2_cCy5	\N	\N
14989	original/PLATE-1A/bPLATE_wG3_s2_cRGB.png	\N	BLUE
14990	wG3_d2-1_cDAPI	\N	\N
14991	original/PLATE-1A/bPLATE_wG3_s2_cRGB.png	\N	GREEN
14992	wG3_d2-1_cGFP	\N	\N
14993	original/PLATE-1A/bPLATE_wG3_s2_cRGB.png	\N	RED
14994	wG3_d2-1_cCy5	\N	\N
14995	original/PLATE-1A/bPLATE_wC12_s9_cRGB.png	\N	BLUE
14996	wC12_d3-3_cDAPI	\N	\N
14997	original/PLATE-1A/bPLATE_wC12_s9_cRGB.png	\N	GREEN
14998	wC12_d3-3_cGFP	\N	\N
14999	original/PLATE-1A/bPLATE_wC12_s9_cRGB.png	\N	RED
15000	wC12_d3-3_cCy5	\N	\N
15001	original/PLATE-1A/bPLATE_wF7_s6_cRGB.png	\N	BLUE
15002	wF7_d3-2_cDAPI	\N	\N
15003	original/PLATE-1A/bPLATE_wF7_s6_cRGB.png	\N	GREEN
15004	wF7_d3-2_cGFP	\N	\N
15005	original/PLATE-1A/bPLATE_wF7_s6_cRGB.png	\N	RED
15006	wF7_d3-2_cCy5	\N	\N
15007	original/PLATE-1A/bPLATE_wF6_s8_cRGB.png	\N	BLUE
15008	wF6_d2-3_cDAPI	\N	\N
15009	original/PLATE-1A/bPLATE_wF6_s8_cRGB.png	\N	GREEN
15010	wF6_d2-3_cGFP	\N	\N
15011	original/PLATE-1A/bPLATE_wF6_s8_cRGB.png	\N	RED
15012	wF6_d2-3_cCy5	\N	\N
15013	original/PLATE-1A/bPLATE_wB2_s5_cRGB.png	\N	BLUE
15014	wB2_d2-2_cDAPI	\N	\N
15015	original/PLATE-1A/bPLATE_wB2_s5_cRGB.png	\N	GREEN
15016	wB2_d2-2_cGFP	\N	\N
15017	original/PLATE-1A/bPLATE_wB2_s5_cRGB.png	\N	RED
15018	wB2_d2-2_cCy5	\N	\N
15019	original/PLATE-1A/bPLATE_wD9_s2_cRGB.png	\N	BLUE
15020	wD9_d2-1_cDAPI	\N	\N
15021	original/PLATE-1A/bPLATE_wD9_s2_cRGB.png	\N	GREEN
15022	wD9_d2-1_cGFP	\N	\N
15023	original/PLATE-1A/bPLATE_wD9_s2_cRGB.png	\N	RED
15024	wD9_d2-1_cCy5	\N	\N
15025	original/PLATE-1A/bPLATE_wD11_s6_cRGB.png	\N	BLUE
15026	wD11_d3-2_cDAPI	\N	\N
15027	original/PLATE-1A/bPLATE_wD11_s6_cRGB.png	\N	GREEN
15028	wD11_d3-2_cGFP	\N	\N
15029	original/PLATE-1A/bPLATE_wD11_s6_cRGB.png	\N	RED
15030	wD11_d3-2_cCy5	\N	\N
15031	original/PLATE-1A/bPLATE_wB4_s9_cRGB.png	\N	BLUE
15032	wB4_d3-3_cDAPI	\N	\N
15033	original/PLATE-1A/bPLATE_wB4_s9_cRGB.png	\N	GREEN
15034	wB4_d3-3_cGFP	\N	\N
15035	original/PLATE-1A/bPLATE_wB4_s9_cRGB.png	\N	RED
15036	wB4_d3-3_cCy5	\N	\N
15037	original/PLATE-1A/bPLATE_wG6_s3_cRGB.png	\N	BLUE
15038	wG6_d3-1_cDAPI	\N	\N
15039	original/PLATE-1A/bPLATE_wG6_s3_cRGB.png	\N	GREEN
15040	wG6_d3-1_cGFP	\N	\N
15041	original/PLATE-1A/bPLATE_wG6_s3_cRGB.png	\N	RED
15042	wG6_d3-1_cCy5	\N	\N
15043	original/PLATE-1A/bPLATE_wC2_s5_cRGB.png	\N	BLUE
15044	wC2_d2-2_cDAPI	\N	\N
15045	original/PLATE-1A/bPLATE_wC2_s5_cRGB.png	\N	GREEN
15046	wC2_d2-2_cGFP	\N	\N
15047	original/PLATE-1A/bPLATE_wC2_s5_cRGB.png	\N	RED
15048	wC2_d2-2_cCy5	\N	\N
15049	original/PLATE-1A/bPLATE_wE9_s2_cRGB.png	\N	BLUE
15050	wE9_d2-1_cDAPI	\N	\N
15051	original/PLATE-1A/bPLATE_wE9_s2_cRGB.png	\N	GREEN
15052	wE9_d2-1_cGFP	\N	\N
15053	original/PLATE-1A/bPLATE_wE9_s2_cRGB.png	\N	RED
15054	wE9_d2-1_cCy5	\N	\N
15055	original/PLATE-1A/bPLATE_wH9_s6_cRGB.png	\N	BLUE
15056	wH9_d3-2_cDAPI	\N	\N
15057	original/PLATE-1A/bPLATE_wH9_s6_cRGB.png	\N	GREEN
15058	wH9_d3-2_cGFP	\N	\N
15059	original/PLATE-1A/bPLATE_wH9_s6_cRGB.png	\N	RED
15060	wH9_d3-2_cCy5	\N	\N
15061	original/PLATE-1A/bPLATE_wF2_s9_cRGB.png	\N	BLUE
15062	wF2_d3-3_cDAPI	\N	\N
15063	original/PLATE-1A/bPLATE_wF2_s9_cRGB.png	\N	GREEN
15064	wF2_d3-3_cGFP	\N	\N
15065	original/PLATE-1A/bPLATE_wF2_s9_cRGB.png	\N	RED
15066	wF2_d3-3_cCy5	\N	\N
15067	original/PLATE-1A/bPLATE_wC9_s7_cRGB.png	\N	BLUE
15068	wC9_d1-3_cDAPI	\N	\N
15069	original/PLATE-1A/bPLATE_wC9_s7_cRGB.png	\N	GREEN
15070	wC9_d1-3_cGFP	\N	\N
15071	original/PLATE-1A/bPLATE_wC9_s7_cRGB.png	\N	RED
15072	wC9_d1-3_cCy5	\N	\N
15073	original/PLATE-1A/bPLATE_wF4_s4_cRGB.png	\N	BLUE
15074	wF4_d1-2_cDAPI	\N	\N
15075	original/PLATE-1A/bPLATE_wF4_s4_cRGB.png	\N	GREEN
15076	wF4_d1-2_cGFP	\N	\N
15077	original/PLATE-1A/bPLATE_wF4_s4_cRGB.png	\N	RED
15078	wF4_d1-2_cCy5	\N	\N
15079	original/PLATE-1A/bPLATE_wH11_s1_cRGB.png	\N	BLUE
15080	wH11_d1-1_cDAPI	\N	\N
15081	original/PLATE-1A/bPLATE_wH11_s1_cRGB.png	\N	GREEN
15082	wH11_d1-1_cGFP	\N	\N
15083	original/PLATE-1A/bPLATE_wH11_s1_cRGB.png	\N	RED
15084	wH11_d1-1_cCy5	\N	\N
15085	original/PLATE-1A/bPLATE_wC6_s1_cRGB.png	\N	BLUE
15086	wC6_d1-1_cDAPI	\N	\N
15087	original/PLATE-1A/bPLATE_wC6_s1_cRGB.png	\N	GREEN
15088	wC6_d1-1_cGFP	\N	\N
15089	original/PLATE-1A/bPLATE_wC6_s1_cRGB.png	\N	RED
15090	wC6_d1-1_cCy5	\N	\N
15091	original/PLATE-1A/bPLATE_wB7_s9_cRGB.png	\N	BLUE
15092	wB7_d3-3_cDAPI	\N	\N
15093	original/PLATE-1A/bPLATE_wB7_s9_cRGB.png	\N	GREEN
15094	wB7_d3-3_cGFP	\N	\N
15095	original/PLATE-1A/bPLATE_wB7_s9_cRGB.png	\N	RED
15096	wB7_d3-3_cCy5	\N	\N
15097	original/PLATE-1A/bPLATE_wE2_s6_cRGB.png	\N	BLUE
15098	wE2_d3-2_cDAPI	\N	\N
15099	original/PLATE-1A/bPLATE_wE2_s6_cRGB.png	\N	GREEN
15100	wE2_d3-2_cGFP	\N	\N
15101	original/PLATE-1A/bPLATE_wE2_s6_cRGB.png	\N	RED
15102	wE2_d3-2_cCy5	\N	\N
15103	original/PLATE-1A/bPLATE_wG9_s3_cRGB.png	\N	BLUE
15104	wG9_d3-1_cDAPI	\N	\N
15105	original/PLATE-1A/bPLATE_wG9_s3_cRGB.png	\N	GREEN
15106	wG9_d3-1_cGFP	\N	\N
15107	original/PLATE-1A/bPLATE_wG9_s3_cRGB.png	\N	RED
15108	wG9_d3-1_cCy5	\N	\N
15109	original/PLATE-1A/bPLATE_wB4_s3_cRGB.png	\N	BLUE
15110	wB4_d3-1_cDAPI	\N	\N
15111	original/PLATE-1A/bPLATE_wB4_s3_cRGB.png	\N	GREEN
15112	wB4_d3-1_cGFP	\N	\N
15113	original/PLATE-1A/bPLATE_wB4_s3_cRGB.png	\N	RED
15114	wB4_d3-1_cCy5	\N	\N
15115	original/PLATE-1A/bPLATE_wC9_s2_cRGB.png	\N	BLUE
15116	wC9_d2-1_cDAPI	\N	\N
15117	original/PLATE-1A/bPLATE_wC9_s2_cRGB.png	\N	GREEN
15118	wC9_d2-1_cGFP	\N	\N
15119	original/PLATE-1A/bPLATE_wC9_s2_cRGB.png	\N	RED
15120	wC9_d2-1_cCy5	\N	\N
15121	original/PLATE-1A/bPLATE_wA2_s5_cRGB.png	\N	BLUE
15122	wA2_d2-2_cDAPI	\N	\N
15123	original/PLATE-1A/bPLATE_wA2_s5_cRGB.png	\N	GREEN
15124	wA2_d2-2_cGFP	\N	\N
15125	original/PLATE-1A/bPLATE_wA2_s5_cRGB.png	\N	RED
15126	wA2_d2-2_cCy5	\N	\N
15127	original/PLATE-1A/bPLATE_wH1_s1_cRGB.png	\N	BLUE
15128	wH1_d1-1_cDAPI	\N	\N
15129	original/PLATE-1A/bPLATE_wH1_s1_cRGB.png	\N	GREEN
15130	wH1_d1-1_cGFP	\N	\N
15131	original/PLATE-1A/bPLATE_wH1_s1_cRGB.png	\N	RED
15132	wH1_d1-1_cCy5	\N	\N
15133	original/PLATE-1A/bPLATE_wB11_s7_cRGB.png	\N	BLUE
15134	wB11_d1-3_cDAPI	\N	\N
15135	original/PLATE-1A/bPLATE_wB11_s7_cRGB.png	\N	GREEN
15136	wB11_d1-3_cGFP	\N	\N
15137	original/PLATE-1A/bPLATE_wB11_s7_cRGB.png	\N	RED
15138	wB11_d1-3_cCy5	\N	\N
15139	original/PLATE-1A/bPLATE_wE6_s4_cRGB.png	\N	BLUE
15140	wE6_d1-2_cDAPI	\N	\N
15141	original/PLATE-1A/bPLATE_wE6_s4_cRGB.png	\N	GREEN
15142	wE6_d1-2_cGFP	\N	\N
15143	original/PLATE-1A/bPLATE_wE6_s4_cRGB.png	\N	RED
15144	wE6_d1-2_cCy5	\N	\N
15145	original/PLATE-1A/bPLATE_wD8_s9_cRGB.png	\N	BLUE
15146	wD8_d3-3_cDAPI	\N	\N
15147	original/PLATE-1A/bPLATE_wD8_s9_cRGB.png	\N	GREEN
15148	wD8_d3-3_cGFP	\N	\N
15149	original/PLATE-1A/bPLATE_wD8_s9_cRGB.png	\N	RED
15150	wD8_d3-3_cCy5	\N	\N
15151	original/PLATE-1A/bPLATE_wG3_s6_cRGB.png	\N	BLUE
15152	wG3_d3-2_cDAPI	\N	\N
15153	original/PLATE-1A/bPLATE_wG3_s6_cRGB.png	\N	GREEN
15154	wG3_d3-2_cGFP	\N	\N
15155	original/PLATE-1A/bPLATE_wG3_s6_cRGB.png	\N	RED
15156	wG3_d3-2_cCy5	\N	\N
15157	original/PLATE-1A/bPLATE_wA11_s8_cRGB.png	\N	BLUE
15158	wA11_d2-3_cDAPI	\N	\N
15159	original/PLATE-1A/bPLATE_wA11_s8_cRGB.png	\N	GREEN
15160	wA11_d2-3_cGFP	\N	\N
15161	original/PLATE-1A/bPLATE_wA11_s8_cRGB.png	\N	RED
15162	wA11_d2-3_cCy5	\N	\N
15163	original/PLATE-1A/bPLATE_wD6_s5_cRGB.png	\N	BLUE
15164	wD6_d2-2_cDAPI	\N	\N
15165	original/PLATE-1A/bPLATE_wD6_s5_cRGB.png	\N	GREEN
15166	wD6_d2-2_cGFP	\N	\N
15167	original/PLATE-1A/bPLATE_wD6_s5_cRGB.png	\N	RED
15168	wD6_d2-2_cCy5	\N	\N
15169	original/PLATE-1A/bPLATE_wG1_s2_cRGB.png	\N	BLUE
15170	wG1_d2-1_cDAPI	\N	\N
15171	original/PLATE-1A/bPLATE_wG1_s2_cRGB.png	\N	GREEN
15172	wG1_d2-1_cGFP	\N	\N
15173	original/PLATE-1A/bPLATE_wG1_s2_cRGB.png	\N	RED
15174	wG1_d2-1_cCy5	\N	\N
15175	original/PLATE-1A/bPLATE_wD1_s2_cRGB.png	\N	BLUE
15176	wD1_d2-1_cDAPI	\N	\N
15177	original/PLATE-1A/bPLATE_wD1_s2_cRGB.png	\N	GREEN
15178	wD1_d2-1_cGFP	\N	\N
15179	original/PLATE-1A/bPLATE_wD1_s2_cRGB.png	\N	RED
15180	wD1_d2-1_cCy5	\N	\N
15181	original/PLATE-1A/bPLATE_wA6_s5_cRGB.png	\N	BLUE
15182	wA6_d2-2_cDAPI	\N	\N
15183	original/PLATE-1A/bPLATE_wA6_s5_cRGB.png	\N	GREEN
15184	wA6_d2-2_cGFP	\N	\N
15185	original/PLATE-1A/bPLATE_wA6_s5_cRGB.png	\N	RED
15186	wA6_d2-2_cCy5	\N	\N
15187	original/PLATE-1A/bPLATE_wA12_s6_cRGB.png	\N	BLUE
15188	wA12_d3-2_cDAPI	\N	\N
15189	original/PLATE-1A/bPLATE_wA12_s6_cRGB.png	\N	GREEN
15190	wA12_d3-2_cGFP	\N	\N
15191	original/PLATE-1A/bPLATE_wA12_s6_cRGB.png	\N	RED
15192	wA12_d3-2_cCy5	\N	\N
15193	original/PLATE-1A/bPLATE_wD7_s3_cRGB.png	\N	BLUE
15194	wD7_d3-1_cDAPI	\N	\N
15195	original/PLATE-1A/bPLATE_wD7_s3_cRGB.png	\N	GREEN
15196	wD7_d3-1_cGFP	\N	\N
15197	original/PLATE-1A/bPLATE_wD7_s3_cRGB.png	\N	RED
15198	wD7_d3-1_cCy5	\N	\N
15199	original/PLATE-1A/bPLATE_wG7_s7_cRGB.png	\N	BLUE
15200	wG7_d1-3_cDAPI	\N	\N
15201	original/PLATE-1A/bPLATE_wG7_s7_cRGB.png	\N	GREEN
15202	wG7_d1-3_cGFP	\N	\N
15203	original/PLATE-1A/bPLATE_wG7_s7_cRGB.png	\N	RED
15204	wG7_d1-3_cCy5	\N	\N
15205	original/PLATE-1A/bPLATE_wH10_s8_cRGB.png	\N	BLUE
15206	wH10_d2-3_cDAPI	\N	\N
15207	original/PLATE-1A/bPLATE_wH10_s8_cRGB.png	\N	GREEN
15208	wH10_d2-3_cGFP	\N	\N
15209	original/PLATE-1A/bPLATE_wH10_s8_cRGB.png	\N	RED
15210	wH10_d2-3_cCy5	\N	\N
15211	original/PLATE-1A/bPLATE_wD6_s7_cRGB.png	\N	BLUE
15212	wD6_d1-3_cDAPI	\N	\N
15213	original/PLATE-1A/bPLATE_wD6_s7_cRGB.png	\N	GREEN
15214	wD6_d1-3_cGFP	\N	\N
15215	original/PLATE-1A/bPLATE_wD6_s7_cRGB.png	\N	RED
15216	wD6_d1-3_cCy5	\N	\N
15217	original/PLATE-1A/bPLATE_wG1_s4_cRGB.png	\N	BLUE
15218	wG1_d1-2_cDAPI	\N	\N
15219	original/PLATE-1A/bPLATE_wG1_s4_cRGB.png	\N	GREEN
15220	wG1_d1-2_cGFP	\N	\N
15221	original/PLATE-1A/bPLATE_wG1_s4_cRGB.png	\N	RED
15222	wG1_d1-2_cCy5	\N	\N
15223	original/PLATE-1A/bPLATE_wF11_s9_cRGB.png	\N	BLUE
15224	wF11_d3-3_cDAPI	\N	\N
15225	original/PLATE-1A/bPLATE_wF11_s9_cRGB.png	\N	GREEN
15226	wF11_d3-3_cGFP	\N	\N
15227	original/PLATE-1A/bPLATE_wF11_s9_cRGB.png	\N	RED
15228	wF11_d3-3_cCy5	\N	\N
15229	original/PLATE-1A/bPLATE_wB9_s9_cRGB.png	\N	BLUE
15230	wB9_d3-3_cDAPI	\N	\N
15231	original/PLATE-1A/bPLATE_wB9_s9_cRGB.png	\N	GREEN
15232	wB9_d3-3_cGFP	\N	\N
15233	original/PLATE-1A/bPLATE_wB9_s9_cRGB.png	\N	RED
15234	wB9_d3-3_cCy5	\N	\N
15235	original/PLATE-1A/bPLATE_wE4_s6_cRGB.png	\N	BLUE
15236	wE4_d3-2_cDAPI	\N	\N
15237	original/PLATE-1A/bPLATE_wE4_s6_cRGB.png	\N	GREEN
15238	wE4_d3-2_cGFP	\N	\N
15239	original/PLATE-1A/bPLATE_wE4_s6_cRGB.png	\N	RED
15240	wE4_d3-2_cCy5	\N	\N
15241	original/PLATE-1A/bPLATE_wG11_s3_cRGB.png	\N	BLUE
15242	wG11_d3-1_cDAPI	\N	\N
15243	original/PLATE-1A/bPLATE_wG11_s3_cRGB.png	\N	GREEN
15244	wG11_d3-1_cGFP	\N	\N
15245	original/PLATE-1A/bPLATE_wG11_s3_cRGB.png	\N	RED
15246	wG11_d3-1_cCy5	\N	\N
15247	original/PLATE-1A/bPLATE_wC4_s3_cRGB.png	\N	BLUE
15248	wC4_d3-1_cDAPI	\N	\N
15249	original/PLATE-1A/bPLATE_wC4_s3_cRGB.png	\N	GREEN
15250	wC4_d3-1_cGFP	\N	\N
15251	original/PLATE-1A/bPLATE_wC4_s3_cRGB.png	\N	RED
15252	wC4_d3-1_cCy5	\N	\N
15253	original/PLATE-1A/bPLATE_wB6_s2_cRGB.png	\N	BLUE
15254	wB6_d2-1_cDAPI	\N	\N
15255	original/PLATE-1A/bPLATE_wB6_s2_cRGB.png	\N	GREEN
15256	wB6_d2-1_cGFP	\N	\N
15257	original/PLATE-1A/bPLATE_wB6_s2_cRGB.png	\N	RED
15258	wB6_d2-1_cCy5	\N	\N
15259	original/PLATE-1A/bPLATE_wB2_s6_cRGB.png	\N	BLUE
15260	wB2_d3-2_cDAPI	\N	\N
15261	original/PLATE-1A/bPLATE_wB2_s6_cRGB.png	\N	GREEN
15262	wB2_d3-2_cGFP	\N	\N
15263	original/PLATE-1A/bPLATE_wB2_s6_cRGB.png	\N	RED
15264	wB2_d3-2_cCy5	\N	\N
15265	original/PLATE-1A/bPLATE_wD9_s3_cRGB.png	\N	BLUE
15266	wD9_d3-1_cDAPI	\N	\N
15267	original/PLATE-1A/bPLATE_wD9_s3_cRGB.png	\N	GREEN
15268	wD9_d3-1_cGFP	\N	\N
15269	original/PLATE-1A/bPLATE_wD9_s3_cRGB.png	\N	RED
15270	wD9_d3-1_cCy5	\N	\N
15271	original/PLATE-1A/bPLATE_wH10_s9_cRGB.png	\N	BLUE
15272	wH10_d3-3_cDAPI	\N	\N
15273	original/PLATE-1A/bPLATE_wH10_s9_cRGB.png	\N	GREEN
15274	wH10_d3-3_cGFP	\N	\N
15275	original/PLATE-1A/bPLATE_wH10_s9_cRGB.png	\N	RED
15276	wH10_d3-3_cCy5	\N	\N
15277	original/PLATE-1A/bPLATE_wC7_s3_cRGB.png	\N	BLUE
15278	wC7_d3-1_cDAPI	\N	\N
15279	original/PLATE-1A/bPLATE_wC7_s3_cRGB.png	\N	GREEN
15280	wC7_d3-1_cGFP	\N	\N
15281	original/PLATE-1A/bPLATE_wC7_s3_cRGB.png	\N	RED
15282	wC7_d3-1_cCy5	\N	\N
15283	original/PLATE-1A/bPLATE_wH12_s9_cRGB.png	\N	BLUE
15284	wH12_d3-3_cDAPI	\N	\N
15285	original/PLATE-1A/bPLATE_wH12_s9_cRGB.png	\N	GREEN
15286	wH12_d3-3_cGFP	\N	\N
15287	original/PLATE-1A/bPLATE_wH12_s9_cRGB.png	\N	RED
15288	wH12_d3-3_cCy5	\N	\N
15289	original/PLATE-1A/bPLATE_wD1_s3_cRGB.png	\N	BLUE
15290	wD1_d3-1_cDAPI	\N	\N
15291	original/PLATE-1A/bPLATE_wD1_s3_cRGB.png	\N	GREEN
15292	wD1_d3-1_cGFP	\N	\N
15293	original/PLATE-1A/bPLATE_wD1_s3_cRGB.png	\N	RED
15294	wD1_d3-1_cCy5	\N	\N
15295	original/PLATE-1A/bPLATE_wA6_s6_cRGB.png	\N	BLUE
15296	wA6_d3-2_cDAPI	\N	\N
15297	original/PLATE-1A/bPLATE_wA6_s6_cRGB.png	\N	GREEN
15298	wA6_d3-2_cGFP	\N	\N
15299	original/PLATE-1A/bPLATE_wA6_s6_cRGB.png	\N	RED
15300	wA6_d3-2_cCy5	\N	\N
15301	original/PLATE-1A/bPLATE_wC5_s3_cRGB.png	\N	BLUE
15302	wC5_d3-1_cDAPI	\N	\N
15303	original/PLATE-1A/bPLATE_wC5_s3_cRGB.png	\N	GREEN
15304	wC5_d3-1_cGFP	\N	\N
15305	original/PLATE-1A/bPLATE_wC5_s3_cRGB.png	\N	RED
15306	wC5_d3-1_cCy5	\N	\N
15307	original/PLATE-1A/bPLATE_wH9_s2_cRGB.png	\N	BLUE
15308	wH9_d2-1_cDAPI	\N	\N
15309	original/PLATE-1A/bPLATE_wH9_s2_cRGB.png	\N	GREEN
15310	wH9_d2-1_cGFP	\N	\N
15311	original/PLATE-1A/bPLATE_wH9_s2_cRGB.png	\N	RED
15312	wH9_d2-1_cCy5	\N	\N
15313	original/PLATE-1A/bPLATE_wC7_s8_cRGB.png	\N	BLUE
15314	wC7_d2-3_cDAPI	\N	\N
15315	original/PLATE-1A/bPLATE_wC7_s8_cRGB.png	\N	GREEN
15316	wC7_d2-3_cGFP	\N	\N
15317	original/PLATE-1A/bPLATE_wC7_s8_cRGB.png	\N	RED
15318	wC7_d2-3_cCy5	\N	\N
15319	original/PLATE-1A/bPLATE_wF2_s5_cRGB.png	\N	BLUE
15320	wF2_d2-2_cDAPI	\N	\N
15321	original/PLATE-1A/bPLATE_wF2_s5_cRGB.png	\N	GREEN
15322	wF2_d2-2_cGFP	\N	\N
15323	original/PLATE-1A/bPLATE_wF2_s5_cRGB.png	\N	RED
15324	wF2_d2-2_cCy5	\N	\N
15325	original/PLATE-1A/bPLATE_wH8_s9_cRGB.png	\N	BLUE
15326	wH8_d3-3_cDAPI	\N	\N
15327	original/PLATE-1A/bPLATE_wH8_s9_cRGB.png	\N	GREEN
15328	wH8_d3-3_cGFP	\N	\N
15329	original/PLATE-1A/bPLATE_wH8_s9_cRGB.png	\N	RED
15330	wH8_d3-3_cCy5	\N	\N
15331	original/PLATE-1A/bPLATE_wH1_s8_cRGB.png	\N	BLUE
15332	wH1_d2-3_cDAPI	\N	\N
15333	original/PLATE-1A/bPLATE_wH1_s8_cRGB.png	\N	GREEN
15334	wH1_d2-3_cGFP	\N	\N
15335	original/PLATE-1A/bPLATE_wH1_s8_cRGB.png	\N	RED
15336	wH1_d2-3_cCy5	\N	\N
15337	original/PLATE-1A/bPLATE_wH4_s2_cRGB.png	\N	BLUE
15338	wH4_d2-1_cDAPI	\N	\N
15339	original/PLATE-1A/bPLATE_wH4_s2_cRGB.png	\N	GREEN
15340	wH4_d2-1_cGFP	\N	\N
15341	original/PLATE-1A/bPLATE_wH4_s2_cRGB.png	\N	RED
15342	wH4_d2-1_cCy5	\N	\N
15343	original/PLATE-1A/bPLATE_wC2_s8_cRGB.png	\N	BLUE
15344	wC2_d2-3_cDAPI	\N	\N
15345	original/PLATE-1A/bPLATE_wC2_s8_cRGB.png	\N	GREEN
15346	wC2_d2-3_cGFP	\N	\N
15347	original/PLATE-1A/bPLATE_wC2_s8_cRGB.png	\N	RED
15348	wC2_d2-3_cCy5	\N	\N
15349	original/PLATE-1A/bPLATE_wE9_s5_cRGB.png	\N	BLUE
15350	wE9_d2-2_cDAPI	\N	\N
15351	original/PLATE-1A/bPLATE_wE9_s5_cRGB.png	\N	GREEN
15352	wE9_d2-2_cGFP	\N	\N
15353	original/PLATE-1A/bPLATE_wE9_s5_cRGB.png	\N	RED
15354	wE9_d2-2_cCy5	\N	\N
15355	original/PLATE-1A/bPLATE_wB1_s9_cRGB.png	\N	BLUE
15356	wB1_d3-3_cDAPI	\N	\N
15357	original/PLATE-1A/bPLATE_wB1_s9_cRGB.png	\N	GREEN
15358	wB1_d3-3_cGFP	\N	\N
15359	original/PLATE-1A/bPLATE_wB1_s9_cRGB.png	\N	RED
15360	wB1_d3-3_cCy5	\N	\N
15361	original/PLATE-1A/bPLATE_wD8_s6_cRGB.png	\N	BLUE
15362	wD8_d3-2_cDAPI	\N	\N
15363	original/PLATE-1A/bPLATE_wD8_s6_cRGB.png	\N	GREEN
15364	wD8_d3-2_cGFP	\N	\N
15365	original/PLATE-1A/bPLATE_wD8_s6_cRGB.png	\N	RED
15366	wD8_d3-2_cCy5	\N	\N
15367	original/PLATE-1A/bPLATE_wG3_s3_cRGB.png	\N	BLUE
15368	wG3_d3-1_cDAPI	\N	\N
15369	original/PLATE-1A/bPLATE_wG3_s3_cRGB.png	\N	GREEN
15370	wG3_d3-1_cGFP	\N	\N
15371	original/PLATE-1A/bPLATE_wG3_s3_cRGB.png	\N	RED
15372	wG3_d3-1_cCy5	\N	\N
15373	original/PLATE-1A/bPLATE_wC5_s5_cRGB.png	\N	BLUE
15374	wC5_d2-2_cDAPI	\N	\N
15375	original/PLATE-1A/bPLATE_wC5_s5_cRGB.png	\N	GREEN
15376	wC5_d2-2_cGFP	\N	\N
15377	original/PLATE-1A/bPLATE_wC5_s5_cRGB.png	\N	RED
15378	wC5_d2-2_cCy5	\N	\N
15379	original/PLATE-1A/bPLATE_wE12_s2_cRGB.png	\N	BLUE
15380	wE12_d2-1_cDAPI	\N	\N
15381	original/PLATE-1A/bPLATE_wE12_s2_cRGB.png	\N	GREEN
15382	wE12_d2-1_cGFP	\N	\N
15383	original/PLATE-1A/bPLATE_wE12_s2_cRGB.png	\N	RED
15384	wE12_d2-1_cCy5	\N	\N
15385	original/PLATE-1A/bPLATE_wA10_s6_cRGB.png	\N	BLUE
15386	wA10_d3-2_cDAPI	\N	\N
15387	original/PLATE-1A/bPLATE_wA10_s6_cRGB.png	\N	GREEN
15388	wA10_d3-2_cGFP	\N	\N
15389	original/PLATE-1A/bPLATE_wA10_s6_cRGB.png	\N	RED
15390	wA10_d3-2_cCy5	\N	\N
15391	original/PLATE-1A/bPLATE_wD5_s3_cRGB.png	\N	BLUE
15392	wD5_d3-1_cDAPI	\N	\N
15393	original/PLATE-1A/bPLATE_wD5_s3_cRGB.png	\N	GREEN
15394	wD5_d3-1_cGFP	\N	\N
15395	original/PLATE-1A/bPLATE_wD5_s3_cRGB.png	\N	RED
15396	wD5_d3-1_cCy5	\N	\N
15397	original/PLATE-1A/bPLATE_wD3_s4_cRGB.png	\N	BLUE
15398	wD3_d1-2_cDAPI	\N	\N
15399	original/PLATE-1A/bPLATE_wD3_s4_cRGB.png	\N	GREEN
15400	wD3_d1-2_cGFP	\N	\N
15401	original/PLATE-1A/bPLATE_wD3_s4_cRGB.png	\N	RED
15402	wD3_d1-2_cCy5	\N	\N
15403	original/PLATE-1A/bPLATE_wA8_s7_cRGB.png	\N	BLUE
15404	wA8_d1-3_cDAPI	\N	\N
15405	original/PLATE-1A/bPLATE_wA8_s7_cRGB.png	\N	GREEN
15406	wA8_d1-3_cGFP	\N	\N
15407	original/PLATE-1A/bPLATE_wA8_s7_cRGB.png	\N	RED
15408	wA8_d1-3_cCy5	\N	\N
15409	original/PLATE-1A/bPLATE_wF10_s1_cRGB.png	\N	BLUE
15410	wF10_d1-1_cDAPI	\N	\N
15411	original/PLATE-1A/bPLATE_wF10_s1_cRGB.png	\N	GREEN
15412	wF10_d1-1_cGFP	\N	\N
15413	original/PLATE-1A/bPLATE_wF10_s1_cRGB.png	\N	RED
15414	wF10_d1-1_cCy5	\N	\N
15415	original/PLATE-1A/bPLATE_wH4_s1_cRGB.png	\N	BLUE
15416	wH4_d1-1_cDAPI	\N	\N
15417	original/PLATE-1A/bPLATE_wH4_s1_cRGB.png	\N	GREEN
15418	wH4_d1-1_cGFP	\N	\N
15419	original/PLATE-1A/bPLATE_wH4_s1_cRGB.png	\N	RED
15420	wH4_d1-1_cCy5	\N	\N
15421	original/PLATE-1A/bPLATE_wC2_s7_cRGB.png	\N	BLUE
15422	wC2_d1-3_cDAPI	\N	\N
15423	original/PLATE-1A/bPLATE_wC2_s7_cRGB.png	\N	GREEN
15424	wC2_d1-3_cGFP	\N	\N
15425	original/PLATE-1A/bPLATE_wC2_s7_cRGB.png	\N	RED
15426	wC2_d1-3_cCy5	\N	\N
15427	original/PLATE-1A/bPLATE_wE9_s4_cRGB.png	\N	BLUE
15428	wE9_d1-2_cDAPI	\N	\N
15429	original/PLATE-1A/bPLATE_wE9_s4_cRGB.png	\N	GREEN
15430	wE9_d1-2_cGFP	\N	\N
15431	original/PLATE-1A/bPLATE_wE9_s4_cRGB.png	\N	RED
15432	wE9_d1-2_cCy5	\N	\N
15433	original/PLATE-1A/bPLATE_wH10_s7_cRGB.png	\N	BLUE
15434	wH10_d1-3_cDAPI	\N	\N
15435	original/PLATE-1A/bPLATE_wH10_s7_cRGB.png	\N	GREEN
15436	wH10_d1-3_cGFP	\N	\N
15437	original/PLATE-1A/bPLATE_wH10_s7_cRGB.png	\N	RED
15438	wH10_d1-3_cCy5	\N	\N
15439	original/PLATE-1A/bPLATE_wG9_s7_cRGB.png	\N	BLUE
15440	wG9_d1-3_cDAPI	\N	\N
15441	original/PLATE-1A/bPLATE_wG9_s7_cRGB.png	\N	GREEN
15442	wG9_d1-3_cGFP	\N	\N
15443	original/PLATE-1A/bPLATE_wG9_s7_cRGB.png	\N	RED
15444	wG9_d1-3_cCy5	\N	\N
15445	original/PLATE-1A/bPLATE_wD11_s1_cRGB.png	\N	BLUE
15446	wD11_d1-1_cDAPI	\N	\N
15447	original/PLATE-1A/bPLATE_wD11_s1_cRGB.png	\N	GREEN
15448	wD11_d1-1_cGFP	\N	\N
15449	original/PLATE-1A/bPLATE_wD11_s1_cRGB.png	\N	RED
15450	wD11_d1-1_cCy5	\N	\N
15451	original/PLATE-1A/bPLATE_wB4_s4_cRGB.png	\N	BLUE
15452	wB4_d1-2_cDAPI	\N	\N
15453	original/PLATE-1A/bPLATE_wB4_s4_cRGB.png	\N	GREEN
15454	wB4_d1-2_cGFP	\N	\N
15455	original/PLATE-1A/bPLATE_wB4_s4_cRGB.png	\N	RED
15456	wB4_d1-2_cCy5	\N	\N
15457	original/PLATE-1A/bPLATE_wH7_s4_cRGB.png	\N	BLUE
15458	wH7_d1-2_cDAPI	\N	\N
15459	original/PLATE-1A/bPLATE_wH7_s4_cRGB.png	\N	GREEN
15460	wH7_d1-2_cGFP	\N	\N
15461	original/PLATE-1A/bPLATE_wH7_s4_cRGB.png	\N	RED
15462	wH7_d1-2_cCy5	\N	\N
15463	original/PLATE-1A/bPLATE_wE12_s7_cRGB.png	\N	BLUE
15464	wE12_d1-3_cDAPI	\N	\N
15465	original/PLATE-1A/bPLATE_wE12_s7_cRGB.png	\N	GREEN
15466	wE12_d1-3_cGFP	\N	\N
15467	original/PLATE-1A/bPLATE_wE12_s7_cRGB.png	\N	RED
15468	wE12_d1-3_cCy5	\N	\N
15469	original/PLATE-1A/bPLATE_wH11_s7_cRGB.png	\N	BLUE
15470	wH11_d1-3_cDAPI	\N	\N
15471	original/PLATE-1A/bPLATE_wH11_s7_cRGB.png	\N	GREEN
15472	wH11_d1-3_cGFP	\N	\N
15473	original/PLATE-1A/bPLATE_wH11_s7_cRGB.png	\N	RED
15474	wH11_d1-3_cCy5	\N	\N
15475	original/PLATE-1A/bPLATE_wA10_s8_cRGB.png	\N	BLUE
15476	wA10_d2-3_cDAPI	\N	\N
15477	original/PLATE-1A/bPLATE_wA10_s8_cRGB.png	\N	GREEN
15478	wA10_d2-3_cGFP	\N	\N
15479	original/PLATE-1A/bPLATE_wA10_s8_cRGB.png	\N	RED
15480	wA10_d2-3_cCy5	\N	\N
15481	original/PLATE-1A/bPLATE_wD5_s5_cRGB.png	\N	BLUE
15482	wD5_d2-2_cDAPI	\N	\N
15483	original/PLATE-1A/bPLATE_wD5_s5_cRGB.png	\N	GREEN
15484	wD5_d2-2_cGFP	\N	\N
15485	original/PLATE-1A/bPLATE_wD5_s5_cRGB.png	\N	RED
15486	wD5_d2-2_cCy5	\N	\N
15487	original/PLATE-1A/bPLATE_wF12_s2_cRGB.png	\N	BLUE
15488	wF12_d2-1_cDAPI	\N	\N
15489	original/PLATE-1A/bPLATE_wF12_s2_cRGB.png	\N	GREEN
15490	wF12_d2-1_cGFP	\N	\N
15491	original/PLATE-1A/bPLATE_wF12_s2_cRGB.png	\N	RED
15492	wF12_d2-1_cCy5	\N	\N
15493	original/PLATE-1A/bPLATE_wA12_s1_cRGB.png	\N	BLUE
15494	wA12_d1-1_cDAPI	\N	\N
15495	original/PLATE-1A/bPLATE_wA12_s1_cRGB.png	\N	GREEN
15496	wA12_d1-1_cGFP	\N	\N
15497	original/PLATE-1A/bPLATE_wA12_s1_cRGB.png	\N	RED
15498	wA12_d1-1_cCy5	\N	\N
15499	original/PLATE-1A/bPLATE_wB11_s5_cRGB.png	\N	BLUE
15500	wB11_d2-2_cDAPI	\N	\N
15501	original/PLATE-1A/bPLATE_wB11_s5_cRGB.png	\N	GREEN
15502	wB11_d2-2_cGFP	\N	\N
15503	original/PLATE-1A/bPLATE_wB11_s5_cRGB.png	\N	RED
15504	wB11_d2-2_cCy5	\N	\N
15505	original/PLATE-1A/bPLATE_wE6_s2_cRGB.png	\N	BLUE
15506	wE6_d2-1_cDAPI	\N	\N
15507	original/PLATE-1A/bPLATE_wE6_s2_cRGB.png	\N	GREEN
15508	wE6_d2-1_cGFP	\N	\N
15509	original/PLATE-1A/bPLATE_wE6_s2_cRGB.png	\N	RED
15510	wE6_d2-1_cCy5	\N	\N
15511	original/PLATE-1A/bPLATE_wC1_s3_cRGB.png	\N	BLUE
15512	wC1_d3-1_cDAPI	\N	\N
15513	original/PLATE-1A/bPLATE_wC1_s3_cRGB.png	\N	GREEN
15514	wC1_d3-1_cGFP	\N	\N
15515	original/PLATE-1A/bPLATE_wC1_s3_cRGB.png	\N	RED
15516	wC1_d3-1_cCy5	\N	\N
15517	original/PLATE-1A/bPLATE_wB10_s3_cRGB.png	\N	BLUE
15518	wB10_d3-1_cDAPI	\N	\N
15519	original/PLATE-1A/bPLATE_wB10_s3_cRGB.png	\N	GREEN
15520	wB10_d3-1_cGFP	\N	\N
15521	original/PLATE-1A/bPLATE_wB10_s3_cRGB.png	\N	RED
15522	wB10_d3-1_cCy5	\N	\N
15523	original/PLATE-1A/bPLATE_wG6_s8_cRGB.png	\N	BLUE
15524	wG6_d2-3_cDAPI	\N	\N
15525	original/PLATE-1A/bPLATE_wG6_s8_cRGB.png	\N	GREEN
15526	wG6_d2-3_cGFP	\N	\N
15527	original/PLATE-1A/bPLATE_wG6_s8_cRGB.png	\N	RED
15528	wG6_d2-3_cCy5	\N	\N
15529	original/PLATE-1A/bPLATE_wC9_s1_cRGB.png	\N	BLUE
15530	wC9_d1-1_cDAPI	\N	\N
15531	original/PLATE-1A/bPLATE_wC9_s1_cRGB.png	\N	GREEN
15532	wC9_d1-1_cGFP	\N	\N
15533	original/PLATE-1A/bPLATE_wC9_s1_cRGB.png	\N	RED
15534	wC9_d1-1_cCy5	\N	\N
15535	original/PLATE-1A/bPLATE_wA2_s4_cRGB.png	\N	BLUE
15536	wA2_d1-2_cDAPI	\N	\N
15537	original/PLATE-1A/bPLATE_wA2_s4_cRGB.png	\N	GREEN
15538	wA2_d1-2_cGFP	\N	\N
15539	original/PLATE-1A/bPLATE_wA2_s4_cRGB.png	\N	RED
15540	wA2_d1-2_cCy5	\N	\N
15541	original/PLATE-1A/bPLATE_wC3_s1_cRGB.png	\N	BLUE
15542	wC3_d1-1_cDAPI	\N	\N
15543	original/PLATE-1A/bPLATE_wC3_s1_cRGB.png	\N	GREEN
15544	wC3_d1-1_cGFP	\N	\N
15545	original/PLATE-1A/bPLATE_wC3_s1_cRGB.png	\N	RED
15546	wC3_d1-1_cCy5	\N	\N
15547	original/PLATE-1A/bPLATE_wG9_s8_cRGB.png	\N	BLUE
15548	wG9_d2-3_cDAPI	\N	\N
15549	original/PLATE-1A/bPLATE_wG9_s8_cRGB.png	\N	GREEN
15550	wG9_d2-3_cGFP	\N	\N
15551	original/PLATE-1A/bPLATE_wG9_s8_cRGB.png	\N	RED
15552	wG9_d2-3_cCy5	\N	\N
16417	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC4_s2.png	\N	\N
16418	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH9_s5.png	\N	\N
16419	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF2_s8.png	\N	\N
16420	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH5_s6.png	\N	\N
16421	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH2_s5.png	\N	\N
16422	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE10_s9.png	\N	\N
16423	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE7_s8.png	\N	\N
16424	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG2_s8.png	\N	\N
16425	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA10_s6.png	\N	\N
16426	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD5_s3.png	\N	\N
16427	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB12_s5.png	\N	\N
16428	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE7_s2.png	\N	\N
16429	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB1_s3.png	\N	\N
16430	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA8_s1.png	\N	\N
16431	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC9_s9.png	\N	\N
16432	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD7_s7.png	\N	\N
16433	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF4_s6.png	\N	\N
16434	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG2_s4.png	\N	\N
16435	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH11_s3.png	\N	\N
16436	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB6_s7.png	\N	\N
16437	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE1_s4.png	\N	\N
16438	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG8_s1.png	\N	\N
16439	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH4_s3.png	\N	\N
16440	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC2_s9.png	\N	\N
16441	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE9_s6.png	\N	\N
16442	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC12_s7.png	\N	\N
16443	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF7_s4.png	\N	\N
16444	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA6_s3.png	\N	\N
16445	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC1_s1.png	\N	\N
16446	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC6_s6.png	\N	\N
16447	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF1_s3.png	\N	\N
16448	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC8_s8.png	\N	\N
16449	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF3_s5.png	\N	\N
16450	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH10_s2.png	\N	\N
16451	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB11_s3.png	\N	\N
16452	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG3_s9.png	\N	\N
16453	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD6_s8.png	\N	\N
16454	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG1_s5.png	\N	\N
16455	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA5_s3.png	\N	\N
16456	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD10_s4.png	\N	\N
16457	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA9_s1.png	\N	\N
16458	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB3_s7.png	\N	\N
16459	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG5_s1.png	\N	\N
16460	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD4_s2.png	\N	\N
16461	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA9_s5.png	\N	\N
16462	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB11_s4.png	\N	\N
16463	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE6_s1.png	\N	\N
16464	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH4_s8.png	\N	\N
16465	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH5_s5.png	\N	\N
16466	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE10_s8.png	\N	\N
16467	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA5_s1.png	\N	\N
16468	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB9_s5.png	\N	\N
16469	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE4_s2.png	\N	\N
16470	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC2_s2.png	\N	\N
16471	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH1_s6.png	\N	\N
16472	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD4_s5.png	\N	\N
16473	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA9_s8.png	\N	\N
16474	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE6_s9.png	\N	\N
16475	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF11_s2.png	\N	\N
16476	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC5_s1.png	\N	\N
16477	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE5_s7.png	\N	\N
16478	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG12_s4.png	\N	\N
16479	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB10_s1.png	\N	\N
16480	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD10_s3.png	\N	\N
16481	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB3_s6.png	\N	\N
16482	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC9_s6.png	\N	\N
16483	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA2_s9.png	\N	\N
16484	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF4_s3.png	\N	\N
16485	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA3_s2.png	\N	\N
16486	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD10_s9.png	\N	\N
16487	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG5_s6.png	\N	\N
16488	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH1_s2.png	\N	\N
16489	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB11_s8.png	\N	\N
16490	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE6_s5.png	\N	\N
16491	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH3_s2.png	\N	\N
16492	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC1_s8.png	\N	\N
16493	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE8_s5.png	\N	\N
16494	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG10_s9.png	\N	\N
16495	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB1_s8.png	\N	\N
16496	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD8_s5.png	\N	\N
16497	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG3_s2.png	\N	\N
16498	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF10_s9.png	\N	\N
16499	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH2_s1.png	\N	\N
16500	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB12_s7.png	\N	\N
16501	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE7_s4.png	\N	\N
16502	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB12_s2.png	\N	\N
16503	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF7_s9.png	\N	\N
16504	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC4_s1.png	\N	\N
16505	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD12_s5.png	\N	\N
16506	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB5_s8.png	\N	\N
16507	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG7_s2.png	\N	\N
16508	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH7_s7.png	\N	\N
16509	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH4_s4.png	\N	\N
16510	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB6_s9.png	\N	\N
16511	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE1_s6.png	\N	\N
16512	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE9_s7.png	\N	\N
16513	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG8_s3.png	\N	\N
16514	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF6_s7.png	\N	\N
16515	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC5_s6.png	\N	\N
16516	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE12_s3.png	\N	\N
16517	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH10_s9.png	\N	\N
16518	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH3_s6.png	\N	\N
16519	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE8_s9.png	\N	\N
16520	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE4_s9.png	\N	\N
16521	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG11_s6.png	\N	\N
16522	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD11_s4.png	\N	\N
16523	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB4_s7.png	\N	\N
16524	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG6_s1.png	\N	\N
16525	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH8_s5.png	\N	\N
16526	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF1_s8.png	\N	\N
16527	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD1_s1.png	\N	\N
16528	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA6_s4.png	\N	\N
16529	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD1_s4.png	\N	\N
16530	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA6_s7.png	\N	\N
16531	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF8_s1.png	\N	\N
16532	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD5_s8.png	\N	\N
16533	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF12_s5.png	\N	\N
16534	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD2_s8.png	\N	\N
16535	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF9_s5.png	\N	\N
16536	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH7_s3.png	\N	\N
16537	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC5_s9.png	\N	\N
16538	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE12_s6.png	\N	\N
16539	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD11_s2.png	\N	\N
16540	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB4_s5.png	\N	\N
16541	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD2_s2.png	\N	\N
16542	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA7_s5.png	\N	\N
16543	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB2_s5.png	\N	\N
16544	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD9_s2.png	\N	\N
16545	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD4_s3.png	\N	\N
16546	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA9_s6.png	\N	\N
16547	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF12_s8.png	\N	\N
16548	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA11_s4.png	\N	\N
16549	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD6_s1.png	\N	\N
16550	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD12_s3.png	\N	\N
16551	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB5_s6.png	\N	\N
16552	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC6_s4.png	\N	\N
16553	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF1_s1.png	\N	\N
16554	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC12_s6.png	\N	\N
16555	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA5_s9.png	\N	\N
16556	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF7_s3.png	\N	\N
16557	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD2_s1.png	\N	\N
16558	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA7_s4.png	\N	\N
16559	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH9_s3.png	\N	\N
16560	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC7_s9.png	\N	\N
16561	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF2_s6.png	\N	\N
16562	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH1_s7.png	\N	\N
16563	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA11_s2.png	\N	\N
16564	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC12_s3.png	\N	\N
16565	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA5_s6.png	\N	\N
16566	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA8_s2.png	\N	\N
16567	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC10_s6.png	\N	\N
16568	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA3_s9.png	\N	\N
16569	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF5_s3.png	\N	\N
16570	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB12_s6.png	\N	\N
16571	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE7_s3.png	\N	\N
16572	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH9_s8.png	\N	\N
16573	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH11_s8.png	\N	\N
16574	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC6_s5.png	\N	\N
16575	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF1_s2.png	\N	\N
16576	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG1_s7.png	\N	\N
16577	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH7_s8.png	\N	\N
16578	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC9_s7.png	\N	\N
16579	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF4_s4.png	\N	\N
16580	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH11_s1.png	\N	\N
16581	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA8_s3.png	\N	\N
16582	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD1_s8.png	\N	\N
16583	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF8_s5.png	\N	\N
16584	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD11_s7.png	\N	\N
16585	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG6_s4.png	\N	\N
16586	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH6_s9.png	\N	\N
16587	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA1_s1.png	\N	\N
16588	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF10_s7.png	\N	\N
16589	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD4_s1.png	\N	\N
16590	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA9_s4.png	\N	\N
16591	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD3_s4.png	\N	\N
16592	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA8_s7.png	\N	\N
16593	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF10_s1.png	\N	\N
16594	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH5_s7.png	\N	\N
16595	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF7_s8.png	\N	\N
16596	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH3_s7.png	\N	\N
16597	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH8_s6.png	\N	\N
16598	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF1_s9.png	\N	\N
16599	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH4_s5.png	\N	\N
16600	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE9_s8.png	\N	\N
16601	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG10_s7.png	\N	\N
16602	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC8_s2.png	\N	\N
16603	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA1_s5.png	\N	\N
16604	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA11_s8.png	\N	\N
16605	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB12_s1.png	\N	\N
16606	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD6_s5.png	\N	\N
16607	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG1_s2.png	\N	\N
16608	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB8_s2.png	\N	\N
16609	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG5_s9.png	\N	\N
16610	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB8_s3.png	\N	\N
16611	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB7_s9.png	\N	\N
16612	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE2_s6.png	\N	\N
16613	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG9_s3.png	\N	\N
16614	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH1_s3.png	\N	\N
16615	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB11_s9.png	\N	\N
16616	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE6_s6.png	\N	\N
16617	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB5_s3.png	\N	\N
16618	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD2_s9.png	\N	\N
16619	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF9_s6.png	\N	\N
16620	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB8_s7.png	\N	\N
16621	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE3_s4.png	\N	\N
16622	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG10_s1.png	\N	\N
16623	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH1_s8.png	\N	\N
16624	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA12_s2.png	\N	\N
16625	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB10_s9.png	\N	\N
16626	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE5_s6.png	\N	\N
16627	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG12_s3.png	\N	\N
16628	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA12_s7.png	\N	\N
16629	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB6_s8.png	\N	\N
16630	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD7_s4.png	\N	\N
16631	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE1_s5.png	\N	\N
16632	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG2_s1.png	\N	\N
16633	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG8_s2.png	\N	\N
16634	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH8_s4.png	\N	\N
16635	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF1_s7.png	\N	\N
16636	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG12_s9.png	\N	\N
16637	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC8_s3.png	\N	\N
16638	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC5_s2.png	\N	\N
16639	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA1_s6.png	\N	\N
16640	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB2_s3.png	\N	\N
16641	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB2_s8.png	\N	\N
16642	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD9_s5.png	\N	\N
16643	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG4_s2.png	\N	\N
16644	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC12_s1.png	\N	\N
16645	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA5_s4.png	\N	\N
16646	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD7_s9.png	\N	\N
16647	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE2_s8.png	\N	\N
16648	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG2_s6.png	\N	\N
16649	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG9_s5.png	\N	\N
16650	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB3_s2.png	\N	\N
16651	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC11_s5.png	\N	\N
16652	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA4_s8.png	\N	\N
16653	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF6_s2.png	\N	\N
16654	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF9_s8.png	\N	\N
16655	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA12_s4.png	\N	\N
16656	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD7_s1.png	\N	\N
16657	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG5_s8.png	\N	\N
16658	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG7_s8.png	\N	\N
16659	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC11_s7.png	\N	\N
16660	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF6_s4.png	\N	\N
16661	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA10_s3.png	\N	\N
16662	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH2_s4.png	\N	\N
16663	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE7_s7.png	\N	\N
16664	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC10_s1.png	\N	\N
16665	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA3_s4.png	\N	\N
16666	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG9_s9.png	\N	\N
16667	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD3_s2.png	\N	\N
16668	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA8_s5.png	\N	\N
16669	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC11_s9.png	\N	\N
16670	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF6_s6.png	\N	\N
16671	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD11_s9.png	\N	\N
16672	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG6_s6.png	\N	\N
16673	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD1_s9.png	\N	\N
16674	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF8_s6.png	\N	\N
16675	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB7_s1.png	\N	\N
16676	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH5_s8.png	\N	\N
16677	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB5_s1.png	\N	\N
16678	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG6_s7.png	\N	\N
16679	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB7_s7.png	\N	\N
16680	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE2_s4.png	\N	\N
16681	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG9_s1.png	\N	\N
16682	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD3_s7.png	\N	\N
16683	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF10_s4.png	\N	\N
16684	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB1_s6.png	\N	\N
16685	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD8_s3.png	\N	\N
16686	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB8_s8.png	\N	\N
16687	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE3_s5.png	\N	\N
16688	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG10_s2.png	\N	\N
16689	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH3_s1.png	\N	\N
16690	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC1_s7.png	\N	\N
16691	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE8_s4.png	\N	\N
16692	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH5_s1.png	\N	\N
16693	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC3_s7.png	\N	\N
16694	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE10_s4.png	\N	\N
16695	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB6_s4.png	\N	\N
16696	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE1_s1.png	\N	\N
16697	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB8_s6.png	\N	\N
16698	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE3_s3.png	\N	\N
16699	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE3_s9.png	\N	\N
16700	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG10_s6.png	\N	\N
16701	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA10_s4.png	\N	\N
16702	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD5_s1.png	\N	\N
16703	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC2_s1.png	\N	\N
16704	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD3_s5.png	\N	\N
16705	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA8_s8.png	\N	\N
16706	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF10_s2.png	\N	\N
16707	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA11_s7.png	\N	\N
16708	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD6_s4.png	\N	\N
16709	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG1_s1.png	\N	\N
16710	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC6_s3.png	\N	\N
16711	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH9_s9.png	\N	\N
16712	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH6_s3.png	\N	\N
16713	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC4_s9.png	\N	\N
16714	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE11_s6.png	\N	\N
16715	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE1_s7.png	\N	\N
16716	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG8_s4.png	\N	\N
16717	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH5_s2.png	\N	\N
16718	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC3_s8.png	\N	\N
16719	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE10_s5.png	\N	\N
16720	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG2_s7.png	\N	\N
16721	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH12_s2.png	\N	\N
16722	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC10_s8.png	\N	\N
16723	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF5_s5.png	\N	\N
16724	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA10_s9.png	\N	\N
16725	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD5_s6.png	\N	\N
16726	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF12_s3.png	\N	\N
16727	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA10_s5.png	\N	\N
16728	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD5_s2.png	\N	\N
16729	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC9_s1.png	\N	\N
16730	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA2_s4.png	\N	\N
16731	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC9_s5.png	\N	\N
16732	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA2_s8.png	\N	\N
16733	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF4_s2.png	\N	\N
16734	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD1_s5.png	\N	\N
16735	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA6_s8.png	\N	\N
16736	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB10_s6.png	\N	\N
16737	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE5_s3.png	\N	\N
16738	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF8_s2.png	\N	\N
16739	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC11_s3.png	\N	\N
16740	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA4_s6.png	\N	\N
16741	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH2_s9.png	\N	\N
16742	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB1_s1.png	\N	\N
16743	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH5_s4.png	\N	\N
16744	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE10_s7.png	\N	\N
16745	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD2_s4.png	\N	\N
16746	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA7_s7.png	\N	\N
16747	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF9_s1.png	\N	\N
16748	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH12_s8.png	\N	\N
16749	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD8_s7.png	\N	\N
16750	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG3_s4.png	\N	\N
16751	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG11_s8.png	\N	\N
16752	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF12_s7.png	\N	\N
16753	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD12_s8.png	\N	\N
16754	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG7_s5.png	\N	\N
16755	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA1_s2.png	\N	\N
16756	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB6_s6.png	\N	\N
16757	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE1_s3.png	\N	\N
16758	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB7_s5.png	\N	\N
16759	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE2_s2.png	\N	\N
16760	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH9_s1.png	\N	\N
16761	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC7_s7.png	\N	\N
16762	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF2_s4.png	\N	\N
16763	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH3_s9.png	\N	\N
16764	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC1_s5.png	\N	\N
16765	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE8_s2.png	\N	\N
16766	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC4_s5.png	\N	\N
16767	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE11_s2.png	\N	\N
16768	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE4_s8.png	\N	\N
16769	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG11_s5.png	\N	\N
16770	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH7_s9.png	\N	\N
16771	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA10_s1.png	\N	\N
16772	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB10_s8.png	\N	\N
16773	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE5_s5.png	\N	\N
16774	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG12_s2.png	\N	\N
16775	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC3_s2.png	\N	\N
16776	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC6_s1.png	\N	\N
16777	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB10_s2.png	\N	\N
16778	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA10_s7.png	\N	\N
16779	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD5_s4.png	\N	\N
16780	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF12_s1.png	\N	\N
16781	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC4_s3.png	\N	\N
16782	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG12_s7.png	\N	\N
16783	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD6_s9.png	\N	\N
16784	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG1_s6.png	\N	\N
16785	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD10_s7.png	\N	\N
16786	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG5_s4.png	\N	\N
16787	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC1_s6.png	\N	\N
16788	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE8_s3.png	\N	\N
16789	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD10_s8.png	\N	\N
16790	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE1_s9.png	\N	\N
16791	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG5_s5.png	\N	\N
16792	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG8_s6.png	\N	\N
16793	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC2_s4.png	\N	\N
16794	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE9_s1.png	\N	\N
16795	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB9_s3.png	\N	\N
16796	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB10_s4.png	\N	\N
16797	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE5_s1.png	\N	\N
16798	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH11_s9.png	\N	\N
16799	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH6_s7.png	\N	\N
16800	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH1_s1.png	\N	\N
16801	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD11_s3.png	\N	\N
16802	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB11_s7.png	\N	\N
16803	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB4_s6.png	\N	\N
16804	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE6_s4.png	\N	\N
16805	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB2_s9.png	\N	\N
16806	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD9_s6.png	\N	\N
16807	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG4_s3.png	\N	\N
16808	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC7_s2.png	\N	\N
16809	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC11_s4.png	\N	\N
16810	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA4_s7.png	\N	\N
16811	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF6_s1.png	\N	\N
16812	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF6_s8.png	\N	\N
16813	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB2_s4.png	\N	\N
16814	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD9_s1.png	\N	\N
16815	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH7_s2.png	\N	\N
16816	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC5_s8.png	\N	\N
16817	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE12_s5.png	\N	\N
16818	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH2_s7.png	\N	\N
16819	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD3_s6.png	\N	\N
16820	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA8_s9.png	\N	\N
16821	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF10_s3.png	\N	\N
16822	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA1_s3.png	\N	\N
16823	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD12_s6.png	\N	\N
16824	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB5_s9.png	\N	\N
16825	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG7_s3.png	\N	\N
16826	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG9_s7.png	\N	\N
16827	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF12_s9.png	\N	\N
16828	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA12_s9.png	\N	\N
16829	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD7_s6.png	\N	\N
16830	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG2_s3.png	\N	\N
16831	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG7_s9.png	\N	\N
16832	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC12_s2.png	\N	\N
16833	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA5_s5.png	\N	\N
16834	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG12_s8.png	\N	\N
16835	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH9_s2.png	\N	\N
16836	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC7_s8.png	\N	\N
16837	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF2_s5.png	\N	\N
16838	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF3_s9.png	\N	\N
16839	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH10_s6.png	\N	\N
16840	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC11_s2.png	\N	\N
16841	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA4_s5.png	\N	\N
16842	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA12_s5.png	\N	\N
16843	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD7_s2.png	\N	\N
16844	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF6_s9.png	\N	\N
16845	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH9_s7.png	\N	\N
16846	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG8_s8.png	\N	\N
16847	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD7_s8.png	\N	\N
16848	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG2_s5.png	\N	\N
16849	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH5_s9.png	\N	\N
16850	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB3_s3.png	\N	\N
16851	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF7_s7.png	\N	\N
16852	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH8_s3.png	\N	\N
16853	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC6_s9.png	\N	\N
16854	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF1_s6.png	\N	\N
16855	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH12_s4.png	\N	\N
16856	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB4_s2.png	\N	\N
16857	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF5_s7.png	\N	\N
16858	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA3_s3.png	\N	\N
16859	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG6_s8.png	\N	\N
16860	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF11_s8.png	\N	\N
16861	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH2_s6.png	\N	\N
16862	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE7_s9.png	\N	\N
16863	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA7_s3.png	\N	\N
16864	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA6_s1.png	\N	\N
16865	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC7_s5.png	\N	\N
16866	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF2_s2.png	\N	\N
16867	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD1_s7.png	\N	\N
16868	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF8_s4.png	\N	\N
16869	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD4_s8.png	\N	\N
16870	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF11_s5.png	\N	\N
16871	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA7_s1.png	\N	\N
16872	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD10_s5.png	\N	\N
16873	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA2_s2.png	\N	\N
16874	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB3_s8.png	\N	\N
16875	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG3_s7.png	\N	\N
16876	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG5_s2.png	\N	\N
16877	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH8_s9.png	\N	\N
16878	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA10_s2.png	\N	\N
16879	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB11_s2.png	\N	\N
16880	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB12_s3.png	\N	\N
16881	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH12_s3.png	\N	\N
16882	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC5_s4.png	\N	\N
16883	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC10_s9.png	\N	\N
16884	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE12_s1.png	\N	\N
16885	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF5_s6.png	\N	\N
16886	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC3_s6.png	\N	\N
16887	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE10_s3.png	\N	\N
16888	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG7_s7.png	\N	\N
16889	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC1_s2.png	\N	\N
16890	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH8_s8.png	\N	\N
16891	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC10_s5.png	\N	\N
16892	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA3_s8.png	\N	\N
16893	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF5_s2.png	\N	\N
16894	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH3_s8.png	\N	\N
16895	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF3_s7.png	\N	\N
16896	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH10_s4.png	\N	\N
16897	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH11_s5.png	\N	\N
16898	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF4_s8.png	\N	\N
16899	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC12_s9.png	\N	\N
16900	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF7_s6.png	\N	\N
16901	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB7_s8.png	\N	\N
16902	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE2_s5.png	\N	\N
16903	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG9_s2.png	\N	\N
16904	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH7_s4.png	\N	\N
16905	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE12_s7.png	\N	\N
16906	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD11_s5.png	\N	\N
16907	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB4_s8.png	\N	\N
16908	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG6_s2.png	\N	\N
16909	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC6_s2.png	\N	\N
16910	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC3_s1.png	\N	\N
16911	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB2_s6.png	\N	\N
16912	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD9_s3.png	\N	\N
16913	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG11_s7.png	\N	\N
16914	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA12_s6.png	\N	\N
16915	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA3_s1.png	\N	\N
16916	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD7_s3.png	\N	\N
16917	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB9_s9.png	\N	\N
16918	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE4_s6.png	\N	\N
16919	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG11_s3.png	\N	\N
16920	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC8_s9.png	\N	\N
16921	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF3_s6.png	\N	\N
16922	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH10_s3.png	\N	\N
16923	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH6_s8.png	\N	\N
16924	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD3_s9.png	\N	\N
16925	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF10_s6.png	\N	\N
16926	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD12_s9.png	\N	\N
16927	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG7_s6.png	\N	\N
16928	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA2_s1.png	\N	\N
16929	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB3_s1.png	\N	\N
16930	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC11_s8.png	\N	\N
16931	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB8_s9.png	\N	\N
16932	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE3_s6.png	\N	\N
16933	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF6_s5.png	\N	\N
16934	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG10_s3.png	\N	\N
16935	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB7_s3.png	\N	\N
16936	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB2_s7.png	\N	\N
16937	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD9_s4.png	\N	\N
16938	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG4_s1.png	\N	\N
16939	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC9_s3.png	\N	\N
16940	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA2_s6.png	\N	\N
16941	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH6_s1.png	\N	\N
16942	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC4_s7.png	\N	\N
16943	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE11_s4.png	\N	\N
16944	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC9_s2.png	\N	\N
16945	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA2_s5.png	\N	\N
16946	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA10_s8.png	\N	\N
16947	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB11_s1.png	\N	\N
16948	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD5_s5.png	\N	\N
16949	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF12_s2.png	\N	\N
16950	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD4_s7.png	\N	\N
16951	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF11_s4.png	\N	\N
16952	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB9_s7.png	\N	\N
16953	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE4_s4.png	\N	\N
16954	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG11_s1.png	\N	\N
16955	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH7_s6.png	\N	\N
16956	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE12_s9.png	\N	\N
16957	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA9_s3.png	\N	\N
16958	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB9_s4.png	\N	\N
16959	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE4_s1.png	\N	\N
16960	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC7_s1.png	\N	\N
16961	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG11_s9.png	\N	\N
16962	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB12_s4.png	\N	\N
16963	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE7_s1.png	\N	\N
16964	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG8_s9.png	\N	\N
16965	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH12_s9.png	\N	\N
16966	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA4_s2.png	\N	\N
16967	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC9_s4.png	\N	\N
16968	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA2_s7.png	\N	\N
16969	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF4_s1.png	\N	\N
16970	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF9_s7.png	\N	\N
16971	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG3_s8.png	\N	\N
16972	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC7_s6.png	\N	\N
16973	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF2_s3.png	\N	\N
16974	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH8_s1.png	\N	\N
16975	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC6_s7.png	\N	\N
16976	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF1_s4.png	\N	\N
16977	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA6_s2.png	\N	\N
16978	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH11_s7.png	\N	\N
16979	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA11_s6.png	\N	\N
16980	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD6_s3.png	\N	\N
16981	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH9_s6.png	\N	\N
16982	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF2_s9.png	\N	\N
16983	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH1_s4.png	\N	\N
16984	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE6_s7.png	\N	\N
16985	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD2_s3.png	\N	\N
16986	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA7_s6.png	\N	\N
16987	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC1_s4.png	\N	\N
16988	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE8_s1.png	\N	\N
16989	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC8_s6.png	\N	\N
16990	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA1_s9.png	\N	\N
16991	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF3_s3.png	\N	\N
16992	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD10_s2.png	\N	\N
16993	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB3_s5.png	\N	\N
16994	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD3_s1.png	\N	\N
16995	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA8_s4.png	\N	\N
16996	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD12_s1.png	\N	\N
16997	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB5_s4.png	\N	\N
16998	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC9_s8.png	\N	\N
16999	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF4_s5.png	\N	\N
17000	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH11_s2.png	\N	\N
17001	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD6_s7.png	\N	\N
17002	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG1_s4.png	\N	\N
17003	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD1_s6.png	\N	\N
17004	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA6_s9.png	\N	\N
17005	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF8_s3.png	\N	\N
17006	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC4_s6.png	\N	\N
17007	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE11_s3.png	\N	\N
17008	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC3_s4.png	\N	\N
17009	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE10_s1.png	\N	\N
17010	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD11_s8.png	\N	\N
17011	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD9_s8.png	\N	\N
17012	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG4_s5.png	\N	\N
17013	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG6_s5.png	\N	\N
17014	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF8_s9.png	\N	\N
17015	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD12_s7.png	\N	\N
17016	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG7_s4.png	\N	\N
17017	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD12_s2.png	\N	\N
17018	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB5_s5.png	\N	\N
17019	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC7_s4.png	\N	\N
17020	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF2_s1.png	\N	\N
17021	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG2_s9.png	\N	\N
17022	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD3_s3.png	\N	\N
17023	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA8_s6.png	\N	\N
17024	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD3_s8.png	\N	\N
17025	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF10_s5.png	\N	\N
17026	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE3_s7.png	\N	\N
17027	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG10_s4.png	\N	\N
17028	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH4_s7.png	\N	\N
17029	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH12_s1.png	\N	\N
17030	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC10_s7.png	\N	\N
17031	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF5_s4.png	\N	\N
17032	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB6_s3.png	\N	\N
17033	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA12_s3.png	\N	\N
17034	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB11_s5.png	\N	\N
17035	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE6_s2.png	\N	\N
17036	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF3_s8.png	\N	\N
17037	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH10_s5.png	\N	\N
17038	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE5_s9.png	\N	\N
17039	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG12_s6.png	\N	\N
17040	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG1_s8.png	\N	\N
17041	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC2_s3.png	\N	\N
17042	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH1_s5.png	\N	\N
17043	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE6_s8.png	\N	\N
17044	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG4_s9.png	\N	\N
17045	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB9_s1.png	\N	\N
17046	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH10_s8.png	\N	\N
17047	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD10_s6.png	\N	\N
17048	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB3_s9.png	\N	\N
17049	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG5_s3.png	\N	\N
17050	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB6_s2.png	\N	\N
17051	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH6_s5.png	\N	\N
17052	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE11_s8.png	\N	\N
17053	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC5_s5.png	\N	\N
17054	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE12_s2.png	\N	\N
17055	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA11_s5.png	\N	\N
17056	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD6_s2.png	\N	\N
17057	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC8_s5.png	\N	\N
17058	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA1_s8.png	\N	\N
17059	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF3_s2.png	\N	\N
17060	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH7_s5.png	\N	\N
17061	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE12_s8.png	\N	\N
17062	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD10_s1.png	\N	\N
17063	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB3_s4.png	\N	\N
17064	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB4_s3.png	\N	\N
17065	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF4_s7.png	\N	\N
17066	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH11_s4.png	\N	\N
17067	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD4_s4.png	\N	\N
17068	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA9_s7.png	\N	\N
17069	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF11_s1.png	\N	\N
17070	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD2_s6.png	\N	\N
17071	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA7_s9.png	\N	\N
17072	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF8_s7.png	\N	\N
17073	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF9_s3.png	\N	\N
17074	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA12_s1.png	\N	\N
17075	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH3_s3.png	\N	\N
17076	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC1_s9.png	\N	\N
17077	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE8_s6.png	\N	\N
17078	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG4_s7.png	\N	\N
17079	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE3_s8.png	\N	\N
17080	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG10_s5.png	\N	\N
17081	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB4_s1.png	\N	\N
17082	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE2_s9.png	\N	\N
17083	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG9_s6.png	\N	\N
17084	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH6_s2.png	\N	\N
17085	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC4_s8.png	\N	\N
17086	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE11_s5.png	\N	\N
17087	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC8_s7.png	\N	\N
17088	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF3_s4.png	\N	\N
17089	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH10_s1.png	\N	\N
17090	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD5_s9.png	\N	\N
17091	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF12_s6.png	\N	\N
17092	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG10_s8.png	\N	\N
17093	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG4_s8.png	\N	\N
17094	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE4_s7.png	\N	\N
17095	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG11_s4.png	\N	\N
17096	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF11_s7.png	\N	\N
17097	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC8_s4.png	\N	\N
17098	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA1_s7.png	\N	\N
17099	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF3_s1.png	\N	\N
17100	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH11_s6.png	\N	\N
17101	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF4_s9.png	\N	\N
17102	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB9_s2.png	\N	\N
17103	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB1_s9.png	\N	\N
17104	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD8_s6.png	\N	\N
17105	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG3_s3.png	\N	\N
17106	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG6_s9.png	\N	\N
17107	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB9_s8.png	\N	\N
17108	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE4_s5.png	\N	\N
17109	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG11_s2.png	\N	\N
17110	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH2_s2.png	\N	\N
17111	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB12_s8.png	\N	\N
17112	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB2_s2.png	\N	\N
17113	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE7_s5.png	\N	\N
17114	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD4_s6.png	\N	\N
17115	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA9_s9.png	\N	\N
17116	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF11_s3.png	\N	\N
17117	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD11_s6.png	\N	\N
17118	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB4_s9.png	\N	\N
17119	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG6_s3.png	\N	\N
17120	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA4_s3.png	\N	\N
17121	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA2_s3.png	\N	\N
17122	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC3_s5.png	\N	\N
17123	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE10_s2.png	\N	\N
17124	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD8_s9.png	\N	\N
17125	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG3_s6.png	\N	\N
17126	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD8_s8.png	\N	\N
17127	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG3_s5.png	\N	\N
17128	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG9_s8.png	\N	\N
17129	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH10_s7.png	\N	\N
17130	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB1_s2.png	\N	\N
17131	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD9_s9.png	\N	\N
17132	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG4_s6.png	\N	\N
17133	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH4_s6.png	\N	\N
17134	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE9_s9.png	\N	\N
17135	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB8_s5.png	\N	\N
17136	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE3_s2.png	\N	\N
17137	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB1_s5.png	\N	\N
17138	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD8_s2.png	\N	\N
17139	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB8_s1.png	\N	\N
17140	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG5_s7.png	\N	\N
17141	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE5_s8.png	\N	\N
17142	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG12_s5.png	\N	\N
17143	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH7_s1.png	\N	\N
17144	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC5_s7.png	\N	\N
17145	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE12_s4.png	\N	\N
17146	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH4_s2.png	\N	\N
17147	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC2_s8.png	\N	\N
17148	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE9_s5.png	\N	\N
17149	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB10_s3.png	\N	\N
17150	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF11_s9.png	\N	\N
17151	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH3_s4.png	\N	\N
17152	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE8_s7.png	\N	\N
17153	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB1_s7.png	\N	\N
17154	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD8_s4.png	\N	\N
17155	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG3_s1.png	\N	\N
17156	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH4_s1.png	\N	\N
17157	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC2_s7.png	\N	\N
17158	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE9_s4.png	\N	\N
17159	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC12_s5.png	\N	\N
17160	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA5_s8.png	\N	\N
17161	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF7_s2.png	\N	\N
17162	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC10_s3.png	\N	\N
17163	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA3_s6.png	\N	\N
17164	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB2_s1.png	\N	\N
17165	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB6_s1.png	\N	\N
17166	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH8_s2.png	\N	\N
17167	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC6_s8.png	\N	\N
17168	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF1_s5.png	\N	\N
17169	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD12_s4.png	\N	\N
17170	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB5_s7.png	\N	\N
17171	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG7_s1.png	\N	\N
17172	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA5_s2.png	\N	\N
17173	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA4_s1.png	\N	\N
17174	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH6_s6.png	\N	\N
17175	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE11_s9.png	\N	\N
17176	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC10_s4.png	\N	\N
17177	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA3_s7.png	\N	\N
17178	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF5_s1.png	\N	\N
17179	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC12_s4.png	\N	\N
17180	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA5_s7.png	\N	\N
17181	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF7_s1.png	\N	\N
17182	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB10_s5.png	\N	\N
17183	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE5_s2.png	\N	\N
17184	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC11_s6.png	\N	\N
17185	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA4_s9.png	\N	\N
17186	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF6_s3.png	\N	\N
17187	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC2_s6.png	\N	\N
17188	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE9_s3.png	\N	\N
17189	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB1_s4.png	\N	\N
17190	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD8_s1.png	\N	\N
17191	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA9_s2.png	\N	\N
17192	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC2_s5.png	\N	\N
17193	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB7_s4.png	\N	\N
17194	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE2_s1.png	\N	\N
17195	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE9_s2.png	\N	\N
17196	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA7_s2.png	\N	\N
17197	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC12_s8.png	\N	\N
17198	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF7_s5.png	\N	\N
17199	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA11_s3.png	\N	\N
17200	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE1_s8.png	\N	\N
17201	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG8_s5.png	\N	\N
17202	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG8_s7.png	\N	\N
17203	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH6_s4.png	\N	\N
17204	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE11_s7.png	\N	\N
17205	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB11_s6.png	\N	\N
17206	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE6_s3.png	\N	\N
17207	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB7_s2.png	\N	\N
17208	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH4_s9.png	\N	\N
17209	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD5_s7.png	\N	\N
17210	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF12_s4.png	\N	\N
17211	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH1_s9.png	\N	\N
17212	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE2_s7.png	\N	\N
17213	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG9_s4.png	\N	\N
17214	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD1_s3.png	\N	\N
17215	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA6_s6.png	\N	\N
17216	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH2_s8.png	\N	\N
17217	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG1_s9.png	\N	\N
17218	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD4_s9.png	\N	\N
17219	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF11_s6.png	\N	\N
17220	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD2_s5.png	\N	\N
17221	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA7_s8.png	\N	\N
17222	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF9_s2.png	\N	\N
17223	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF8_s8.png	\N	\N
17224	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD11_s1.png	\N	\N
17225	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB4_s4.png	\N	\N
17226	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB9_s6.png	\N	\N
17227	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE4_s3.png	\N	\N
17228	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB10_s7.png	\N	\N
17229	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE5_s4.png	\N	\N
17230	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG12_s1.png	\N	\N
17231	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC1_s3.png	\N	\N
17232	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB8_s4.png	\N	\N
17233	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE3_s1.png	\N	\N
17234	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB5_s2.png	\N	\N
17235	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF10_s8.png	\N	\N
17236	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH8_s7.png	\N	\N
17237	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH5_s3.png	\N	\N
17238	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC3_s9.png	\N	\N
17239	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE10_s6.png	\N	\N
17240	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA12_s8.png	\N	\N
17241	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD7_s5.png	\N	\N
17242	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG2_s2.png	\N	\N
17243	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH12_s7.png	\N	\N
17244	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF9_s9.png	\N	\N
17245	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC10_s2.png	\N	\N
17246	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA3_s5.png	\N	\N
17247	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH3_s5.png	\N	\N
17248	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE8_s8.png	\N	\N
17249	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH12_s5.png	\N	\N
17250	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF5_s8.png	\N	\N
17251	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB6_s5.png	\N	\N
17252	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE1_s2.png	\N	\N
17253	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD1_s2.png	\N	\N
17254	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA6_s5.png	\N	\N
17255	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA11_s1.png	\N	\N
17256	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD9_s7.png	\N	\N
17257	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG4_s4.png	\N	\N
17258	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA11_s9.png	\N	\N
17259	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD6_s6.png	\N	\N
17260	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wG1_s3.png	\N	\N
17261	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC7_s3.png	\N	\N
17262	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH12_s6.png	\N	\N
17263	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC3_s3.png	\N	\N
17264	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF5_s9.png	\N	\N
17265	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH2_s3.png	\N	\N
17266	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wD2_s7.png	\N	\N
17267	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB12_s9.png	\N	\N
17268	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE7_s6.png	\N	\N
17269	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF9_s4.png	\N	\N
17270	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC4_s4.png	\N	\N
17271	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE11_s1.png	\N	\N
17272	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wB7_s6.png	\N	\N
17273	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wE2_s3.png	\N	\N
17274	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC8_s1.png	\N	\N
17275	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA1_s4.png	\N	\N
17276	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wH9_s4.png	\N	\N
17277	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC5_s3.png	\N	\N
17278	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wF2_s7.png	\N	\N
17279	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wC11_s1.png	\N	\N
17280	original/PLATE-1.OVERLAY-1/cOVERLAY-1_wA4_s4.png	\N	\N
\.


--
-- Data for Name: spots; Type: TABLE DATA; Schema: public; Owner: -
--

COPY spots (id, x, y, cont_id) FROM stdin;
97	1	1	1
98	2	1	1
99	3	1	1
100	4	1	1
101	5	1	1
102	6	1	1
103	7	1	1
104	8	1	1
105	9	1	1
106	10	1	1
107	11	1	1
108	12	1	1
109	1	2	1
110	2	2	1
111	3	2	1
112	4	2	1
113	5	2	1
114	6	2	1
115	7	2	1
116	8	2	1
117	9	2	1
118	10	2	1
119	11	2	1
120	12	2	1
121	1	3	1
122	2	3	1
123	3	3	1
124	4	3	1
125	5	3	1
126	6	3	1
127	7	3	1
128	8	3	1
129	9	3	1
130	10	3	1
131	11	3	1
132	12	3	1
133	1	4	1
134	2	4	1
135	3	4	1
136	4	4	1
137	5	4	1
138	6	4	1
139	7	4	1
140	8	4	1
141	9	4	1
142	10	4	1
143	11	4	1
144	12	4	1
145	1	5	1
146	2	5	1
147	3	5	1
148	4	5	1
149	5	5	1
150	6	5	1
151	7	5	1
152	8	5	1
153	9	5	1
154	10	5	1
155	11	5	1
156	12	5	1
157	1	6	1
158	2	6	1
159	3	6	1
160	4	6	1
161	5	6	1
162	6	6	1
163	7	6	1
164	8	6	1
165	9	6	1
166	10	6	1
167	11	6	1
168	12	6	1
169	1	7	1
170	2	7	1
171	3	7	1
172	4	7	1
173	5	7	1
174	6	7	1
175	7	7	1
176	8	7	1
177	9	7	1
178	10	7	1
179	11	7	1
180	12	7	1
181	1	8	1
182	2	8	1
183	3	8	1
184	4	8	1
185	5	8	1
186	6	8	1
187	7	8	1
188	8	8	1
189	9	8	1
190	10	8	1
191	11	8	1
192	12	8	1
193	1	1	2
194	2	1	2
195	3	1	2
196	4	1	2
197	5	1	2
198	6	1	2
199	7	1	2
200	8	1	2
201	9	1	2
202	10	1	2
203	11	1	2
204	12	1	2
205	1	2	2
206	2	2	2
207	3	2	2
208	4	2	2
209	5	2	2
210	6	2	2
211	7	2	2
212	8	2	2
213	9	2	2
214	10	2	2
215	11	2	2
216	12	2	2
217	1	3	2
218	2	3	2
219	3	3	2
220	4	3	2
221	5	3	2
222	6	3	2
223	7	3	2
224	8	3	2
225	9	3	2
226	10	3	2
227	11	3	2
228	12	3	2
229	1	4	2
230	2	4	2
231	3	4	2
232	4	4	2
233	5	4	2
234	6	4	2
235	7	4	2
236	8	4	2
237	9	4	2
238	10	4	2
239	11	4	2
240	12	4	2
241	1	5	2
242	2	5	2
243	3	5	2
244	4	5	2
245	5	5	2
246	6	5	2
247	7	5	2
248	8	5	2
249	9	5	2
250	10	5	2
251	11	5	2
252	12	5	2
253	1	6	2
254	2	6	2
255	3	6	2
256	4	6	2
257	5	6	2
258	6	6	2
259	7	6	2
260	8	6	2
261	9	6	2
262	10	6	2
263	11	6	2
264	12	6	2
265	1	7	2
266	2	7	2
267	3	7	2
268	4	7	2
269	5	7	2
270	6	7	2
271	7	7	2
272	8	7	2
273	9	7	2
274	10	7	2
275	11	7	2
276	12	7	2
277	1	8	2
278	2	8	2
279	3	8	2
280	4	8	2
281	5	8	2
282	6	8	2
283	7	8	2
284	8	8	2
285	9	8	2
286	10	8	2
287	11	8	2
288	12	8	2
\.


--
-- Name: acquired_images_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY acquired_images
    ADD CONSTRAINT acquired_images_pkey PRIMARY KEY (id);


--
-- Name: analysis_data_sets_perm_id_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY analysis_data_sets
    ADD CONSTRAINT analysis_data_sets_perm_id_key UNIQUE (perm_id);


--
-- Name: analysis_data_sets_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY analysis_data_sets
    ADD CONSTRAINT analysis_data_sets_pkey PRIMARY KEY (id);


--
-- Name: channel_stacks_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY channel_stacks
    ADD CONSTRAINT channel_stacks_pkey PRIMARY KEY (id);


--
-- Name: channels_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY channels
    ADD CONSTRAINT channels_pkey PRIMARY KEY (id);


--
-- Name: channels_uk_1; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY channels
    ADD CONSTRAINT channels_uk_1 UNIQUE (code, ds_id);


--
-- Name: channels_uk_2; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY channels
    ADD CONSTRAINT channels_uk_2 UNIQUE (code, exp_id);


--
-- Name: containers_perm_id_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY containers
    ADD CONSTRAINT containers_perm_id_key UNIQUE (perm_id);


--
-- Name: containers_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY containers
    ADD CONSTRAINT containers_pkey PRIMARY KEY (id);


--
-- Name: experiments_perm_id_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT experiments_perm_id_key UNIQUE (perm_id);


--
-- Name: experiments_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY experiments
    ADD CONSTRAINT experiments_pkey PRIMARY KEY (id);


--
-- Name: feature_defs_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY feature_defs
    ADD CONSTRAINT feature_defs_pkey PRIMARY KEY (id);


--
-- Name: feature_defs_uk_1; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY feature_defs
    ADD CONSTRAINT feature_defs_uk_1 UNIQUE (code, ds_id);


--
-- Name: feature_values_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY feature_values
    ADD CONSTRAINT feature_values_pkey PRIMARY KEY (id);


--
-- Name: feature_vocabulary_terms_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY feature_vocabulary_terms
    ADD CONSTRAINT feature_vocabulary_terms_pkey PRIMARY KEY (id);


--
-- Name: image_data_sets_perm_id_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY image_data_sets
    ADD CONSTRAINT image_data_sets_perm_id_key UNIQUE (perm_id);


--
-- Name: image_data_sets_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY image_data_sets
    ADD CONSTRAINT image_data_sets_pkey PRIMARY KEY (id);


--
-- Name: image_transformations_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY image_transformations
    ADD CONSTRAINT image_transformations_pkey PRIMARY KEY (id);


--
-- Name: image_transformations_uk_1; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY image_transformations
    ADD CONSTRAINT image_transformations_uk_1 UNIQUE (code, channel_id);


--
-- Name: image_zoom_level_transformations_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY image_zoom_level_transformations
    ADD CONSTRAINT image_zoom_level_transformations_pkey PRIMARY KEY (id);


--
-- Name: image_zoom_levels_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY image_zoom_levels
    ADD CONSTRAINT image_zoom_levels_pkey PRIMARY KEY (id);


--
-- Name: images_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY images
    ADD CONSTRAINT images_pkey PRIMARY KEY (id);


--
-- Name: spots_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY spots
    ADD CONSTRAINT spots_pkey PRIMARY KEY (id);


--
-- Name: analysis_data_sets_cont_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX analysis_data_sets_cont_idx ON analysis_data_sets USING btree (cont_id);


--
-- Name: channel_stacks_dim_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX channel_stacks_dim_idx ON channel_stacks USING btree (x, y, z_in_m, t_in_sec);


--
-- Name: channel_stacks_ds_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX channel_stacks_ds_idx ON channel_stacks USING btree (ds_id);


--
-- Name: channel_stacks_spot_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX channel_stacks_spot_idx ON channel_stacks USING btree (spot_id);


--
-- Name: channels_ds_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX channels_ds_idx ON channels USING btree (ds_id);


--
-- Name: containers_expe_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX containers_expe_idx ON containers USING btree (expe_id);


--
-- Name: feature_defs_ds_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX feature_defs_ds_idx ON feature_defs USING btree (ds_id);


--
-- Name: feature_values_fd_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX feature_values_fd_idx ON feature_values USING btree (fd_id);


--
-- Name: feature_values_z_and_t_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX feature_values_z_and_t_idx ON feature_values USING btree (z_in_m, t_in_sec);


--
-- Name: feature_vocabulary_terms_fd_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX feature_vocabulary_terms_fd_idx ON feature_vocabulary_terms USING btree (fd_id);


--
-- Name: image_data_sets_cont_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX image_data_sets_cont_idx ON image_data_sets USING btree (cont_id);


--
-- Name: image_transformations_channels_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX image_transformations_channels_idx ON image_transformations USING btree (channel_id);


--
-- Name: image_zoom_level_transformations_zlid_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX image_zoom_level_transformations_zlid_idx ON image_zoom_level_transformations USING btree (zoom_level_id);


--
-- Name: image_zoom_levels_cont_fk_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX image_zoom_levels_cont_fk_idx ON image_zoom_levels USING btree (container_dataset_id);


--
-- Name: image_zoom_levels_phys_ds_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX image_zoom_levels_phys_ds_idx ON image_zoom_levels USING btree (physical_dataset_perm_id);


--
-- Name: images_channel_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX images_channel_idx ON acquired_images USING btree (channel_id);


--
-- Name: images_channel_stack_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX images_channel_stack_idx ON acquired_images USING btree (channel_stack_id);


--
-- Name: images_img_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX images_img_idx ON acquired_images USING btree (img_id);


--
-- Name: images_thumbnail_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX images_thumbnail_idx ON acquired_images USING btree (thumbnail_id);


--
-- Name: spots_cont_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX spots_cont_idx ON spots USING btree (cont_id);


--
-- Name: spots_coords_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX spots_coords_idx ON spots USING btree (cont_id, x, y);


--
-- Name: channel_stacks_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER channel_stacks_check BEFORE INSERT OR UPDATE ON channel_stacks FOR EACH ROW EXECUTE PROCEDURE channel_stacks_check();


--
-- Name: empty_acquired_images; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER empty_acquired_images BEFORE UPDATE ON acquired_images FOR EACH ROW WHEN (((new.img_id IS NULL) AND (new.thumbnail_id IS NULL))) EXECUTE PROCEDURE delete_empty_acquired_images();


--
-- Name: image_transformations_default_check; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER image_transformations_default_check BEFORE INSERT OR UPDATE ON image_transformations FOR EACH ROW EXECUTE PROCEDURE image_transformations_default_check();


--
-- Name: unused_images; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER unused_images AFTER DELETE ON acquired_images FOR EACH ROW EXECUTE PROCEDURE delete_unused_images();


--
-- Name: unused_nulled_images; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER unused_nulled_images AFTER UPDATE ON acquired_images FOR EACH ROW EXECUTE PROCEDURE delete_unused_nulled_images();


--
-- Name: fk_analysis_data_set_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY analysis_data_sets
    ADD CONSTRAINT fk_analysis_data_set_1 FOREIGN KEY (cont_id) REFERENCES containers(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_channel_stacks_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY channel_stacks
    ADD CONSTRAINT fk_channel_stacks_1 FOREIGN KEY (spot_id) REFERENCES spots(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_channel_stacks_2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY channel_stacks
    ADD CONSTRAINT fk_channel_stacks_2 FOREIGN KEY (ds_id) REFERENCES image_data_sets(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_channels_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY channels
    ADD CONSTRAINT fk_channels_1 FOREIGN KEY (ds_id) REFERENCES image_data_sets(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_channels_2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY channels
    ADD CONSTRAINT fk_channels_2 FOREIGN KEY (exp_id) REFERENCES experiments(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_feature_defs_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY feature_defs
    ADD CONSTRAINT fk_feature_defs_1 FOREIGN KEY (ds_id) REFERENCES analysis_data_sets(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_feature_values_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY feature_values
    ADD CONSTRAINT fk_feature_values_1 FOREIGN KEY (fd_id) REFERENCES feature_defs(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_feature_vocabulary_terms_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY feature_vocabulary_terms
    ADD CONSTRAINT fk_feature_vocabulary_terms_1 FOREIGN KEY (fd_id) REFERENCES feature_defs(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_image_data_set_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY image_data_sets
    ADD CONSTRAINT fk_image_data_set_1 FOREIGN KEY (cont_id) REFERENCES containers(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_image_transformations_channel; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY image_transformations
    ADD CONSTRAINT fk_image_transformations_channel FOREIGN KEY (channel_id) REFERENCES channels(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_image_zoom_level_transformations_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY image_zoom_level_transformations
    ADD CONSTRAINT fk_image_zoom_level_transformations_1 FOREIGN KEY (zoom_level_id) REFERENCES image_zoom_levels(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_image_zoom_level_transformations_2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY image_zoom_level_transformations
    ADD CONSTRAINT fk_image_zoom_level_transformations_2 FOREIGN KEY (channel_id) REFERENCES channels(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_image_zoom_levels_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY image_zoom_levels
    ADD CONSTRAINT fk_image_zoom_levels_1 FOREIGN KEY (container_dataset_id) REFERENCES image_data_sets(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_images_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY acquired_images
    ADD CONSTRAINT fk_images_1 FOREIGN KEY (channel_stack_id) REFERENCES channel_stacks(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_images_2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY acquired_images
    ADD CONSTRAINT fk_images_2 FOREIGN KEY (channel_id) REFERENCES channels(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_images_3; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY acquired_images
    ADD CONSTRAINT fk_images_3 FOREIGN KEY (img_id) REFERENCES images(id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: fk_images_4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY acquired_images
    ADD CONSTRAINT fk_images_4 FOREIGN KEY (thumbnail_id) REFERENCES images(id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: fk_sample_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY containers
    ADD CONSTRAINT fk_sample_1 FOREIGN KEY (expe_id) REFERENCES experiments(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_spot_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY spots
    ADD CONSTRAINT fk_spot_1 FOREIGN KEY (cont_id) REFERENCES containers(id) ON UPDATE CASCADE ON DELETE CASCADE;


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

