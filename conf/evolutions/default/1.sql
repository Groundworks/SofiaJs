# Users schema
 
# --- !Ups
 
CREATE TABLE page (
    pagekey varchar(255) NOT NULL,
    content TEXT NOT NULL,
    PRIMARY KEY (id)
);
 
# --- !Downs
 
DROP TABLE Page;
