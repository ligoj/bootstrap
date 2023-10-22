INSERT INTO S_USER            ("LOGIN")                         values ('junit');
INSERT INTO S_USER            ("LOGIN")                         values ('session');
INSERT INTO S_ROLE            ("ID", "NAME")                    values (1000, 'USER');
INSERT INTO S_ROLE            ("ID", "NAME")                    values (1001, 'SESSION_ONLY');
INSERT INTO S_ROLE_ASSIGNMENT ("ID", "user", "ROLE")            values (1000,'junit',1000);
INSERT INTO S_ROLE_ASSIGNMENT ("ID", "user", "ROLE")            values (1001,'session',1001);
INSERT INTO S_AUTHORIZATION   ("ID", "TYPE", "ROLE", "PATTERN") values (1000,'API',1000, '(throw|test)/.*');
INSERT INTO S_AUTHORIZATION   ("ID", "TYPE", "ROLE", "PATTERN", "method") values (1001,'API',1001, 'session', 'GET');
