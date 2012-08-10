# Users schema
 
# --- !Ups

CREATE TABLE pullrequest (
    id SERIAL,
    fromKey TEXT NOT NULL,
    tokey TEXT NOT NULL,
    PRIMARY KEY (id)
);
 
# --- !Downs
 
DROP TABLE pullrequest;
