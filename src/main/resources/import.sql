INSERT INTO users(name, email, password, last_name, first_name, phone, role) VALUES ('dev', 'test@mail.at', '$2a$10$IfrX7uEH8zGjHVHSPfTe6uHrQewsMVSAkhHTYaV63GiG07Eh0WV5a', 'Mustermann', 'Max', 0664000000, 'ADMIN');
INSERT INTO users(name, email, password, last_name, first_name, phone, role) VALUES ('frontend', 'frontend@mail.at', '$2a$10$nTFgFeGvGTja5rXJjqq/oupkbGozYi/JEnD9Y.qcQodbrDA07zIPa', 'frontend', 'frontend', 0664000000, 'ADMIN');
INSERT INTO competence_relationship_type(description, distance_val, name) VALUES ('Basic type SMALL', 0.9, 'small');
INSERT INTO competence_relationship_type(description, distance_val, name) VALUES ('Competences are synonym to each other', 1.0, 'isSynonym');
INSERT INTO competence_relationship_type(description, distance_val, name) VALUES ('Competences are closely related to each other', 0.9, 'isSimilar');
INSERT INTO competence_relationship_type(description, distance_val, name) VALUES ('Basic type MEDIUM', 0.6, 'medium');
INSERT INTO competence_relationship_type(description, distance_val, name) VALUES ('Basic type LARGE', 0.3, 'large');