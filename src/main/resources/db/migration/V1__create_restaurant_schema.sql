-- Create schema
CREATE SCHEMA IF NOT EXISTS restaurant_service;

-- Create restaurant table
CREATE TABLE restaurant_service.restaurant (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    phone VARCHAR(30),
    email VARCHAR(150),
    status VARCHAR(20) NOT NULL,
    accepting_orders BOOLEAN NOT NULL DEFAULT FALSE,
    minimum_order_amount DECIMAL(10,2),
    delivery_fee DECIMAL(10,2),
    estimated_preparation_minutes INTEGER,
    average_rating DECIMAL(3,2),
    total_ratings INTEGER DEFAULT 0,
    currency VARCHAR(3),
    timezone VARCHAR(50),
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_restaurant_owner_id ON restaurant_service.restaurant(owner_id);
CREATE INDEX idx_restaurant_status ON restaurant_service.restaurant(status);
CREATE INDEX idx_restaurant_accepting_orders ON restaurant_service.restaurant(accepting_orders);

-- Create restaurant_address table
CREATE TABLE restaurant_service.restaurant_address (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL UNIQUE,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    province VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(2) NOT NULL,
    latitude DECIMAL(9,6),
    longitude DECIMAL(9,6),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (restaurant_id) REFERENCES restaurant_service.restaurant(id) ON DELETE CASCADE
);

CREATE INDEX idx_restaurant_address_city ON restaurant_service.restaurant_address(city);
CREATE INDEX idx_restaurant_address_postal_code ON restaurant_service.restaurant_address(postal_code);

-- Create cuisine table
CREATE TABLE restaurant_service.cuisine (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

ALTER TABLE restaurant_service.cuisine ADD CONSTRAINT uk_cuisine_name UNIQUE (name);

-- Create restaurant_cuisine join table
CREATE TABLE restaurant_service.restaurant_cuisine (
    restaurant_id UUID NOT NULL,
    cuisine_id UUID NOT NULL,
    PRIMARY KEY (restaurant_id, cuisine_id),
    FOREIGN KEY (restaurant_id) REFERENCES restaurant_service.restaurant(id) ON DELETE CASCADE,
    FOREIGN KEY (cuisine_id) REFERENCES restaurant_service.cuisine(id) ON DELETE CASCADE
);

CREATE INDEX idx_restaurant_cuisine_restaurant_id ON restaurant_service.restaurant_cuisine(restaurant_id);
CREATE INDEX idx_restaurant_cuisine_cuisine_id ON restaurant_service.restaurant_cuisine(cuisine_id);

-- Create business_hour table
CREATE TABLE restaurant_service.business_hour (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,
    open_time TIME,
    close_time TIME,
    closed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_business_hour_restaurant_day UNIQUE (restaurant_id, day_of_week)
);

CREATE INDEX idx_business_hour_restaurant_day ON restaurant_service.business_hour(restaurant_id, day_of_week);

-- Create menu_category table
CREATE TABLE restaurant_service.menu_category (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    display_order INTEGER,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (restaurant_id) REFERENCES restaurant_service.restaurant(id) ON DELETE CASCADE
);

CREATE INDEX idx_menu_category_restaurant_active ON restaurant_service.menu_category(restaurant_id, active);

-- Create menu_item table
CREATE TABLE restaurant_service.menu_item (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL,
    category_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    price DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    image_url VARCHAR(500),
    available BOOLEAN NOT NULL DEFAULT TRUE,
    vegetarian BOOLEAN NOT NULL DEFAULT FALSE,
    vegan BOOLEAN NOT NULL DEFAULT FALSE,
    gluten_free BOOLEAN NOT NULL DEFAULT FALSE,
    spicy_level INTEGER,
    preparation_minutes INTEGER,
    display_order INTEGER,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (restaurant_id) REFERENCES restaurant_service.restaurant(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES restaurant_service.menu_category(id) ON DELETE CASCADE,
    CONSTRAINT chk_spicy_level CHECK (spicy_level IS NULL OR (spicy_level >= 0 AND spicy_level <= 5)),
    CONSTRAINT chk_price_positive CHECK (price > 0)
);

CREATE INDEX idx_menu_item_restaurant_available ON restaurant_service.menu_item(restaurant_id, available);
CREATE INDEX idx_menu_item_category_available ON restaurant_service.menu_item(category_id, available);
CREATE INDEX idx_menu_item_name ON restaurant_service.menu_item(name);

-- Create delivery_setting table
CREATE TABLE restaurant_service.delivery_setting (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL UNIQUE,
    delivery_radius_km DECIMAL(5,2),
    minimum_order_amount DECIMAL(10,2),
    delivery_fee DECIMAL(10,2),
    free_delivery_threshold DECIMAL(10,2),
    estimated_delivery_minutes INTEGER,
    delivery_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    pickup_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_delivery_setting_restaurant UNIQUE (restaurant_id),
    FOREIGN KEY (restaurant_id) REFERENCES restaurant_service.restaurant(id) ON DELETE CASCADE
);

-- Create outbox_event table
CREATE TABLE restaurant_service.outbox_event (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    last_error VARCHAR(1000)
);

CREATE INDEX idx_outbox_event_status_created ON restaurant_service.outbox_event(status, created_at);

-- Create idempotency_record table
CREATE TABLE restaurant_service.idempotency_record (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    request_hash VARCHAR(255) NOT NULL,
    response_status INTEGER NOT NULL,
    response_body TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_idempotency_key UNIQUE (idempotency_key)
);

CREATE INDEX idx_idempotency_expires_at ON restaurant_service.idempotency_record(expires_at);
