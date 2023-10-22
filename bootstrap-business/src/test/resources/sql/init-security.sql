INSERT INTO S_USER            ("LOGIN")                         values ('junit');
INSERT INTO S_ROLE            ("ID", "NAME")                    values (1, 'USER');
INSERT INTO S_ROLE_ASSIGNMENT ("ID", "user", "ROLE")            values (1,'junit',1);
INSERT INTO S_AUTHORIZATION   ("ID", "TYPE", "ROLE", "PATTERN") values (1,'API',1, '.*');