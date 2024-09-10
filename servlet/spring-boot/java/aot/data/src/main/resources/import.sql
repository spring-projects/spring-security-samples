insert into users (id,email,password,first_name,last_name) values ('rob','rob@example.com','password','Rob','Winch');
insert into users (id,email,password,first_name,last_name) values ('luke','luke@example.com','password','Luke','Taylor');

insert into message (id,created,to_id,summary,text) values (100,'2014-07-10 10:00:00','rob','Hello Rob','This message is for Rob');
insert into message (id,created,to_id,summary,text) values (101,'2014-07-10 14:00:00','rob','How are you Rob?','This message is for Rob');
insert into message (id,created,to_id,summary,text) values (102,'2014-07-11 22:00:00','rob','Is this secure?','This message is for Rob');

insert into message (id,created,to_id,summary,text) values (110,'2014-07-12 10:00:00','luke','Hello Luke','This message is for Luke');
insert into message (id,created,to_id,summary,text) values (111,'2014-07-12 10:00:00','luke','Greetings Luke','This message is for Luke');
insert into message (id,created,to_id,summary,text) values (112,'2014-07-12 10:00:00','luke','Is this secure?','This message is for Luke');