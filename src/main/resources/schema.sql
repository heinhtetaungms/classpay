DROP TABLE IF EXISTS users, packages, user_packages, classes, bookings, waitlists, refunds, payments, business CASCADE;


-- Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,                               
    username VARCHAR(255) NOT NULL UNIQUE,                     
    email VARCHAR(255) NOT NULL UNIQUE,                       
    password VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,        
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'System',
    updated_by VARCHAR(255) DEFAULT 'System',
    CONSTRAINT email_unique UNIQUE(email),
	CONSTRAINT chk_is_verified CHECK (is_verified IN (FALSE, TRUE))
);

CREATE TABLE IF NOT EXISTS roles (
     role_id SERIAL PRIMARY KEY,
     name VARCHAR(255) NOT NULL UNIQUE
);

-- Permissions Table
CREATE TABLE IF NOT EXISTS permissions (
    permission_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- User Roles Table (Many-to-Many between users and roles)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE
);

-- Role Permissions Table (Many-to-Many between roles and permissions)
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE
);

-- Index for quick email verification checks
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Index for role name
CREATE INDEX IF NOT EXISTS idx_roles_name ON roles(name);

-- Packages Table
CREATE TABLE IF NOT EXISTS packages (
    package_id SERIAL PRIMARY KEY,                            
    package_name VARCHAR(255) NOT NULL,                        
    total_credits INT NOT NULL,                               
    price DECIMAL(10, 2) NOT NULL,                            
    expiry_days INT NOT NULL,                                  
    country VARCHAR(100) NOT NULL,                           
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,         
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'System',
    updated_by VARCHAR(255) DEFAULT 'System',
	CONSTRAINT chk_expiry_days CHECK (expiry_days > 0)
);

-- User Packages Table (Mapping between users and packages)
CREATE TABLE IF NOT EXISTS user_packages (
    user_package_id SERIAL PRIMARY KEY,                      
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE, 
    package_id INT NOT NULL REFERENCES packages(package_id) ON DELETE CASCADE, 
    remaining_credits INT NOT NULL,                          
    status VARCHAR(50) DEFAULT 'active',                     
    expiration_date TIMESTAMPTZ,                               
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,         
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'System',
    updated_by VARCHAR(255) DEFAULT 'System',
	CONSTRAINT chk_status CHECK (status IN ('active', 'expired'))
);

-- Indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_user_packages_user_id ON user_packages(user_id);
CREATE INDEX IF NOT EXISTS idx_user_packages_package_id ON user_packages(package_id);

-- Business Table (For business entities managing the classes)
CREATE TABLE IF NOT EXISTS business (
    business_id SERIAL PRIMARY KEY,                          
    business_name VARCHAR(255) NOT NULL,                      
    country VARCHAR(255) NOT NULL,                            
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,        
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'System',
    updated_by VARCHAR(255) DEFAULT 'System'
);

-- Index for performance on business queries
CREATE INDEX IF NOT EXISTS idx_business_country ON business(country);

-- Classes Table (For available classes)
CREATE TABLE IF NOT EXISTS classes (
    class_id SERIAL PRIMARY KEY,                              
    class_name VARCHAR(255) NOT NULL,                          
    country VARCHAR(100) NOT NULL,                           
    required_credits INT NOT NULL,                          
    available_slots INT NOT NULL,                        
    class_date TIMESTAMPTZ NOT NULL,                         
    business_id INT NOT NULL REFERENCES business(business_id) ON DELETE CASCADE, 
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,         
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'System',
    updated_by VARCHAR(255) DEFAULT 'System',
	CONSTRAINT chk_required_credits CHECK (required_credits > 0),
	CONSTRAINT chk_available_slots CHECK (available_slots >= 0)
);

-- Index for performance on class dates and country
CREATE INDEX IF NOT EXISTS idx_classes_class_date ON classes(class_date);
CREATE INDEX IF NOT EXISTS idx_classes_country ON classes(country);

-- Bookings Table (User bookings for classes)
CREATE TABLE IF NOT EXISTS bookings (
    booking_id SERIAL PRIMARY KEY,                           
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    class_id INT NOT NULL REFERENCES classes(class_id) ON DELETE CASCADE,
    user_package_id INT NOT NULL REFERENCES user_packages(user_package_id) ON DELETE CASCADE, 
    booking_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,        
    cancellation_time TIMESTAMPTZ,                             
    status VARCHAR(50) DEFAULT 'booked',                      
    is_canceled BOOLEAN DEFAULT FALSE,                        
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,          
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'System',
    updated_by VARCHAR(255) DEFAULT 'System',
	CONSTRAINT chk_booking_status CHECK (status IN ('booked', 'canceled', 'waitlisted'))
);

-- Indexes for performance on booking queries
CREATE INDEX IF NOT EXISTS idx_bookings_user_id ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_class_id ON bookings(class_id);
CREATE INDEX IF NOT EXISTS idx_bookings_user_package_id ON bookings(user_package_id);

-- Waitlists Table (For handling waitlisted users)
CREATE TABLE IF NOT EXISTS waitlists (
    waitlist_id SERIAL PRIMARY KEY,                          
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    class_id INT NOT NULL REFERENCES classes(class_id) ON DELETE CASCADE,
    waitlist_position INT NOT NULL,                           
    status VARCHAR(50) DEFAULT 'waitlisted',                  
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,         
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'System',
    updated_by VARCHAR(255) DEFAULT 'System',
	CONSTRAINT chk_waitlist_status CHECK (status IN ('waitlisted', 'booked'))
);

-- Index for performance on waitlist queries
CREATE INDEX IF NOT EXISTS idx_waitlists_user_id ON waitlists(user_id);
CREATE INDEX IF NOT EXISTS idx_waitlists_class_id ON waitlists(class_id);

-- Refunds Table (For tracking user refunds)
CREATE TABLE IF NOT EXISTS refunds (
    refund_id SERIAL PRIMARY KEY,                            
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE, 
    user_package_id INT NOT NULL REFERENCES user_packages(user_package_id) ON DELETE CASCADE, 
    amount_refunded DECIMAL(10, 2) NOT NULL,                   
    refund_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,       
    reason VARCHAR(255),                                     
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,          
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'System',
    updated_by VARCHAR(255) DEFAULT 'System'
);

-- Index for performance on refund queries
CREATE INDEX IF NOT EXISTS idx_refunds_user_id ON refunds(user_id);
CREATE INDEX IF NOT EXISTS idx_refunds_user_package_id ON refunds(user_package_id);

-- Payments Table (Tracking user payments)
CREATE TABLE IF NOT EXISTS payments (
    payment_id SERIAL PRIMARY KEY,                           
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    amount DECIMAL(10, 2) NOT NULL,                           
    status VARCHAR(50) DEFAULT 'completed',                   
    payment_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,       
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,         
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) DEFAULT 'System',
    updated_by VARCHAR(255) DEFAULT 'System',
	CONSTRAINT chk_payment_status CHECK (status IN ('completed', 'failed'))
);

-- Index for performance on payment queries
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);


