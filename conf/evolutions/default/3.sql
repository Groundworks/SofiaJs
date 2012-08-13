# Users schema
 
# --- !Ups

ALTER TABLE page ADD location TEXT;
ALTER TABLE page ADD userid TEXT;

# --- !Downs
 
ALTER TABLE page DROP location;
ALTER TABLE page DROP userid;
