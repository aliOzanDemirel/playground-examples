INSERT INTO clothing (id, description, brand, is_hot, is_closed_to_review) VALUES (1, 'Worst shirt ever', 'Some Brand A', false, true);
INSERT INTO clothing (id, description, brand, is_hot, is_closed_to_review) VALUES (2, 'Very cool shirt', 'Some Brand A', true, false);
INSERT INTO clothing (id, description, brand, is_hot, is_closed_to_review) VALUES (3, 'Easy to iron', 'Some Brand B', false, false);
INSERT INTO clothing (id, description, brand, is_hot, is_closed_to_review) VALUES (4, 'Softest shirt ever', 'Some Brand C', false, false);

INSERT INTO clothing_size(clothing_id, size) VALUES (1, 0);
INSERT INTO clothing_size(clothing_id, size) VALUES (1, 1);
INSERT INTO clothing_size(clothing_id, size) VALUES (2, 0);
INSERT INTO clothing_size(clothing_id, size) VALUES (3, 1);
INSERT INTO clothing_size(clothing_id, size) VALUES (3, 2);
INSERT INTO clothing_size(clothing_id, size) VALUES (4, 0);
INSERT INTO clothing_size(clothing_id, size) VALUES (4, 1);
INSERT INTO clothing_size(clothing_id, size) VALUES (4, 2);

INSERT INTO clothing_color(clothing_id, color_id) VALUES (1, 1);
INSERT INTO clothing_color(clothing_id, color_id) VALUES (1, 2);
INSERT INTO clothing_color(clothing_id, color_id) VALUES (1, 3);
INSERT INTO clothing_color(clothing_id, color_id) VALUES (2, 4);
INSERT INTO clothing_color(clothing_id, color_id) VALUES (2, 5);
INSERT INTO clothing_color(clothing_id, color_id) VALUES (3, 1);
INSERT INTO clothing_color(clothing_id, color_id) VALUES (3, 3);
INSERT INTO clothing_color(clothing_id, color_id) VALUES (3, 5);
INSERT INTO clothing_color(clothing_id, color_id) VALUES (4, 1);
INSERT INTO clothing_color(clothing_id, color_id) VALUES (4, 2);
INSERT INTO clothing_color(clothing_id, color_id) VALUES (4, 3);
INSERT INTO clothing_color(clothing_id, color_id) VALUES (4, 4);
INSERT INTO clothing_color(clothing_id, color_id) VALUES (4, 5);
