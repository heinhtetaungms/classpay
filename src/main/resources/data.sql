-- Inserting Users
INSERT INTO users (username, email, password, country, is_verified) VALUES
('john_doe', 'john.doe@example.com', '$2a$10$ugZ7uS9B.sFAK4J0kuw6puAp3q5a5aIG2vc/aMlwNq/JCGSRbZeD6', 'Singapore',TRUE),
('jane_smith', 'jane.smith@example.com', '$2a$10$ugZ7uS9B.sFAK4J0kuw6puAp3q5a5aIG2vc/aMlwNq/JCGSRbZeD6', 'Singapore',FALSE);


INSERT INTO roles (name) VALUES ('ADMIN');
INSERT INTO roles (name) VALUES ('USER');

-- Insert sample Permissions
INSERT INTO permissions (name) VALUES ('MANAGE_PACKAGES');
INSERT INTO permissions (name) VALUES ('MANAGE_SCHEDULES');
INSERT INTO permissions (name) VALUES ('VIEW_ALL_BOOKINGS');
INSERT INTO permissions (name) VALUES ('MANAGE_WAITLIST');


-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2);

INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 1);
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 2);
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 3);
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 4);

-- Inserting Packages
INSERT INTO packages (package_name, total_credits, price, expiry_days, country) VALUES
('Basic Plan', 10, 99.99, 30, 'USA'),
('Premium Plan', 20, 199.99, 60, 'USA'),
('Standard Plan', 15, 149.99, 45, 'Canada');

-- Inserting User Packages
INSERT INTO user_packages (user_id, package_id, remaining_credits, status, expiration_date) VALUES
(1, 1, 10, 'active', CURRENT_TIMESTAMP + INTERVAL '30 days'),
(2, 2, 20, 'active', CURRENT_TIMESTAMP + INTERVAL '60 days');

-- Inserting Businesses
INSERT INTO business (business_name, country) VALUES
('Fitness Studio', 'USA'),
('Yoga Center', 'Canada');

-- Inserting Classes
INSERT INTO classes (class_name, country, required_credits, available_slots, class_date, business_id) VALUES
('Yoga Beginner', 'USA', 5, 20, CURRENT_TIMESTAMP + INTERVAL '1 hour', 1),
('Advanced Pilates', 'Canada', 10, 10, CURRENT_TIMESTAMP + INTERVAL '2 hours', 2);

-- Inserting Bookings
INSERT INTO bookings (user_id, class_id, user_package_id, booking_time, status) VALUES
(1, 1, 1, CURRENT_TIMESTAMP, 'booked'),
(2, 2, 2, CURRENT_TIMESTAMP, 'waitlisted');

-- Inserting Waitlists
INSERT INTO waitlists (user_id, class_id, waitlist_position, status) VALUES
(2, 1, 1, 'waitlisted');

-- Inserting Refunds
INSERT INTO refunds (user_id, user_package_id, amount_refunded, reason) VALUES
(1, 1, 49.99, 'Cancellation');

-- Inserting Payments
INSERT INTO payments (user_id, amount, status) VALUES
(1, 99.99, 'completed'),
(2, 149.99, 'failed');
