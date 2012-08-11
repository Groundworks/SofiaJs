# Users schema
 
# --- !Ups

ALTER TABLE page ADD hashkey VARCHAR(255);
 
# --- !Downs
 
ALTER TABLE page DROP hashkey;
