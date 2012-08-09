# Users schema
 
# --- !Ups
 
CREATE TABLE page (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    pagekey varchar(255) NOT NULL,
    content TEXT NOT NULL,
    PRIMARY KEY (id)
);
 
# --- !Downs
 
DROP TABLE Page;
