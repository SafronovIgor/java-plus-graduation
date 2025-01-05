DROP TABLE IF EXISTS public.comp_event;
DROP TABLE IF EXISTS public.requests;
DROP TABLE IF EXISTS public.compilations;
DROP TABLE IF EXISTS public.events;
DROP TABLE IF EXISTS public.users;
DROP TABLE IF EXISTS public.categories;

DROP SEQUENCE IF EXISTS USER_ID_SEQ;
CREATE SEQUENCE IF NOT EXISTS USER_ID_SEQ START WITH 1;
DROP SEQUENCE IF EXISTS EVENT_ID_SEQ;
CREATE SEQUENCE IF NOT EXISTS EVENT_ID_SEQ START WITH 1;
DROP SEQUENCE IF EXISTS CAT_ID_SEQ;
CREATE SEQUENCE IF NOT EXISTS CAT_ID_SEQ START WITH 1;
DROP SEQUENCE IF EXISTS REQ_ID_SEQ;
CREATE SEQUENCE IF NOT EXISTS REQ_ID_SEQ START WITH 1;
DROP SEQUENCE IF EXISTS COMP_ID_SEQ;
CREATE SEQUENCE IF NOT EXISTS COMP_ID_SEQ START WITH 1;

CREATE TABLE IF NOT EXISTS public.USERS
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
    USERNAME character varying
(
    256
) NOT NULL,
    EMAIL character varying
(
    256
) UNIQUE NOT NULL,
    CONSTRAINT user_pkey PRIMARY KEY
(
    ID
),
    CONSTRAINT EMAIL_UNIQUE UNIQUE
(
    EMAIL
)
    );
CREATE TABLE IF NOT EXISTS public.CATEGORIES
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
    CATEGORY_NAME character varying
(
    64
) NOT NULL UNIQUE,
    CONSTRAINT cat_pkey PRIMARY KEY
(
    ID
),
    CONSTRAINT name_unique UNIQUE
(
    CATEGORY_NAME
)
    );
CREATE TABLE IF NOT EXISTS public.EVENTS
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
    ANNOTATION character varying
(
    2048
) NOT NULL,
    CATEGORY_ID bigint,
    CREATED_ON timestamp without time zone,
    PUBLISHED_ON timestamp
                         without time zone,
    EVENT_DATE timestamp
                         without time zone NOT NULL,
    DESCRIPTION character varying
(
    8192
) NOT NULL,
    PAID boolean NOT NULL,
    PARTICIPANT_LIMIT int NOT NULL,
    REQUEST_MODERATION boolean NOT NULL,
    TITLE character varying
(
    128
) NOT NULL,
    USER_ID bigint NOT NULL,
    LOCATION_LAT double precision NOT NULL,
    LOCATION_LON double precision NOT NULL,
    EVENT_STATE character varying
(
    64
) NOT NULL,
    CONSTRAINT event_pkey PRIMARY KEY
(
    ID
),
    CONSTRAINT event_user_fk FOREIGN KEY
(
    USER_ID
)
    REFERENCES public.USERS
(
    ID
)
                         ON UPDATE CASCADE
                         ON DELETE CASCADE,
    CONSTRAINT event_cat_fk FOREIGN KEY
(
    CATEGORY_ID
)
    REFERENCES public.CATEGORIES
(
    ID
)
                         ON UPDATE CASCADE
                         ON DELETE NO ACTION
    );
CREATE TABLE IF NOT EXISTS public.COMPILATIONS
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
    pinned boolean NOT NULL,
    title character varying
(
    64
) NOT NULL,
    event_id BIGINT,
    CONSTRAINT comp_pkey PRIMARY KEY
(
    ID
),
    CONSTRAINT comp_fk_event FOREIGN KEY
(
    event_id
)
    REFERENCES public.events
(
    id
)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    );
CREATE TABLE IF NOT EXISTS public.COMP_EVENT
(
    COMPILATION_ID
    bigint
    NOT
    NULL,
    event_id
    bigint
    NOT
    NULL,
    CONSTRAINT
    comp_event_pkey
    PRIMARY
    KEY
(
    COMPILATION_ID,
    event_id
),
    CONSTRAINT event_comp_join_fk FOREIGN KEY
(
    COMPILATION_ID
)
    REFERENCES public.compilations
(
    id
)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
    CONSTRAINT comp_event_join_fk FOREIGN KEY
(
    event_id
)
    REFERENCES public.events
(
    id
)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    );
CREATE TABLE IF NOT EXISTS public.REQUESTS
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
    created timestamp without time zone,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    status character varying
(
    64
),
    CONSTRAINT req_pkey PRIMARY KEY
(
    ID
),
    CONSTRAINT fk_user FOREIGN KEY
(
    user_id
)
    REFERENCES public.users
(
    id
)
                      ON UPDATE CASCADE
                      ON DELETE CASCADE,
    CONSTRAINT fk_event FOREIGN KEY
(
    event_id
)
    REFERENCES public.events
(
    id
)
                      ON UPDATE CASCADE
                      ON DELETE CASCADE
    );
CREATE TABLE IF NOT EXISTS public.LOCI
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
    name character varying
(
    128
),
    locus_type character varying
(
    32
),
    lat double precision,
    lon double precision,
    rad double precision,
    CONSTRAINT loc_pkey PRIMARY KEY
(
    ID
)
    );
CREATE
ALIAS IF NOT EXISTS distance FOR "ru.practicum.util.GeoUtils.haversineDistance";
