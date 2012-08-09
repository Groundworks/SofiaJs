# Users schema
 
# --- !Ups

set ignorecase true;

CREATE TABLE page (
    pagekey varchar(255) NOT NULL,
    content TEXT NOT NULL,
    constraint pk_page PRIMARY KEY (pagekey)
);
 
# --- !Downs
 
DROP TABLE Page;
