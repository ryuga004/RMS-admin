INSERT INTO roles (id, name, description) VALUES (1,'SUPER_ADMIN', 'Super Admin'), (2,'ADMIN', 'Admin'), (3, 'TENANT', 'Tenant') ON CONFLICT (name) DO NOTHING;

INSERT INTO category (name) VALUES ('Apartment'), ('House'), ('Villa'), ('Studio'), ('PG'), ('Commercial') ON CONFLICT (name) DO NOTHING;

INSERT INTO amenities (name) VALUES ('Wifi'), ('Parking'), ('Pool'), ('Gym'), ('Laundry'), ('Security') ON CONFLICT (name) DO NOTHING;

INSERT INTO notification_templates (title, template, placeholder_values) VALUES ('Welcome to the platform', 'Welcome to the platform', '[]') ON CONFLICT (title) DO NOTHING;