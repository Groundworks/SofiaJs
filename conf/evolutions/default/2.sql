# Users schema
 
# --- !Ups

CREATE TABLE pullrequest (
    id SERIAL,
    pagekey TEXT NOT NULL,
    PRIMARY KEY (pagekey)
);
 
# --- !Downs
 
DROP TABLE pullrequest;
