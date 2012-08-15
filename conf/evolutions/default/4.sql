# Users schema
 
# --- !Ups

ALTER  TABLE page ADD version TEXT;
CREATE TABLE head (
    id SERIAL,
    location TEXT NOT NULL,
    version  TEXT NOT NULL,
    clientid TEXT NOT NULL,
    PRIMARY KEY (id)
);
 
# --- !Downs
 
ALTER TABLE page DROP version;
DROP  TABLE head;
