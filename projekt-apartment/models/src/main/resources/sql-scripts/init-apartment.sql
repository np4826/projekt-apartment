--
-- PostgreSQL database dump
--

-- Dumped from database version 10.0
-- Dumped by pg_dump version 10.0

-- Started on 2017-11-08 15:17:35

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET search_path = public, pg_catalog;

--
-- TOC entry 2846 (class 0 OID 16394)
-- Dependencies: 196
-- Data for Name: apartment; Type: TABLE DATA; Schema: public; Owner: dbuser
--

COPY apartment (id, address, adults, airconditioning, bathrooms, bedrooms, beds, breakfast, children, description, doorman, dryer, elevator, events, fireplace, gym, hangers, heating, hottub, infants, iron, kitchen, parking, pets, pool, published, shampoo, smoking, submitted, title, tv, type, user_id, washer, wheelchair, wifi) FROM stdin;
1	Street address 13	2	t	1	1	2	f	0	Example apartment 1	f	f	f	f	f	f	f	t	f	0	f	t	t	f	f	t	f	f	2017-10-31 20:00:00	Example 1	t	Full apartment	1	f	f	t
2	Street address 5	2	t	1	2	4	f	2	Example apartment 2	f	f	f	f	f	f	f	t	f	1	f	t	f	f	f	f	f	f	2017-10-31 20:15:00	Example 2	f	Full apartment	1	f	f	t
\.


-- Completed on 2017-11-08 15:17:35

--
-- PostgreSQL database dump complete
--

