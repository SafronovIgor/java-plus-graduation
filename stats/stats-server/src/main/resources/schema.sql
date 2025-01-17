DROP TABLE IF EXISTS public.HIT;

DROP SEQUENCE IF EXISTS HIT_ID_SEQ;
CREATE SEQUENCE IF NOT EXISTS HIT_ID_SEQ START WITH 1;

CREATE TABLE IF NOT EXISTS public.HIT
(
    ID
    bigint
    NOT
    NULL
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
(
    INCREMENT
    BY
    1
),
    APP character varying
(
    128
),
    URI character varying
(
    128
),
    IP character varying
(
    128
),
    CREATED timestamp without time zone,
    CONSTRAINT hit_pkey PRIMARY KEY
(
    ID
)
    );