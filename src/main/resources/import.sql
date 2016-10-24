INSERT INTO role(name) VALUES('ADMIN');
INSERT INTO users(name, email, password, last_name, first_name, phone) VALUES ('frontend', 'frontend@mail.at', '$2a$10$nTFgFeGvGTja5rXJjqq/oupkbGozYi/JEnD9Y.qcQodbrDA07zIPa', 'frontend', 'frontend', 0664000000);
INSERT INTO mapping_role_user(user_id, role_id) VALUES (1, 1);