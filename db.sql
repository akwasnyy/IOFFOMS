-- Tabela dla kategorii produktów
CREATE TABLE Categories (
    category_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela dla użytkowników
CREATE TABLE Users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NULL,
    last_name VARCHAR(50) NULL,
    email VARCHAR(100) UNIQUE NULL,
    role VARCHAR(20) CHECK (role IN ('admin', 'kitchen_staff', 'customer')) NOT NULL DEFAULT 'customer',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL
);

-- Tabela dla produktów w menu
CREATE TABLE Products (
    product_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    category_id INT NULL,
    image_url VARCHAR(255) NULL,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES Categories(category_id) ON DELETE SET NULL ON UPDATE CASCADE
);

-- Tabela dla zamówień
CREATE TABLE Orders (
    order_id SERIAL PRIMARY KEY,
    user_id INT NULL,
    order_status VARCHAR(20) CHECK (order_status IN ('pending_payment', 'received', 'in_preparation', 'ready_for_pickup', 'completed', 'cancelled')) NOT NULL DEFAULT 'received',
    order_type VARCHAR(20) CHECK (order_type IN ('dine_in', 'takeaway', 'delivery', 'mcdrive')) NULL,
    table_number VARCHAR(20) NULL,
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
    payment_status VARCHAR(20) CHECK (payment_status IN ('pending', 'paid', 'failed', 'refunded')) DEFAULT 'pending',
    payment_method VARCHAR(50) NULL,
    customer_notes TEXT NULL,
    kitchen_notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE SET NULL ON UPDATE CASCADE
);

-- Tabela łącząca produkty z zamówieniami
CREATE TABLE OrderItems (
    order_item_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    price_at_order DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Funkcja do aktualizacji updated_at
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggery do automatycznej aktualizacji pola updated_at
CREATE TRIGGER set_timestamp_categories
BEFORE UPDATE ON Categories
FOR EACH ROW
EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_products
BEFORE UPDATE ON Products
FOR EACH ROW
EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_users
BEFORE UPDATE ON Users
FOR EACH ROW
EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_orders
BEFORE UPDATE ON Orders
FOR EACH ROW
EXECUTE FUNCTION trigger_set_timestamp();

-- Indeksy
CREATE INDEX idx_products_category_id ON Products(category_id);
CREATE INDEX idx_products_is_available ON Products(is_available);
CREATE INDEX idx_orders_user_id ON Orders(user_id);
CREATE INDEX idx_orders_status ON Orders(order_status);
CREATE INDEX idx_orderitems_order_id ON OrderItems(order_id);
CREATE INDEX idx_orderitems_product_id ON OrderItems(product_id);

CREATE TYPE user_role_enum AS ENUM ('admin', 'kitchen_staff', 'customer');
CREATE TYPE order_status_enum AS ENUM ('pending_payment', 'received', 'in_preparation', 'ready_for_pickup', 'completed', 'cancelled');


-- Kategorie
INSERT INTO Categories (name, description) VALUES
('Burgery', 'Soczyste burgery wołowe i wegetariańskie'),
('Dodatki', 'Frytki, sałatki i inne dodatki'),
('Napoje', 'Zimne i gorące napoje'),
('Sałatki', 'Świeże i zdrowe sałatki'),
('Desery', 'Słodkie zakończenie posiłku');

-- Użytkownicy (pracownicy)
-- Pamiętaj, aby zastąpić 'YOUR_GENERATED_BCRYPT_HASH...' prawdziwymi hashami! <<<<<<<<<<<<-------------------------------------------------------------------------------------------------!!!!!!!!!!!!!!!!!!
INSERT INTO Users (username, password_hash, first_name, last_name, email, role) VALUES
('admin', '$2a$10$YOUR_GENERATED_BCRYPT_HASH_FOR_ADMIN', 'Adam', 'Nowak', 'admin@fastfood.com', 'admin'),
('kuchnia1', '$2a$10$YOUR_GENERATED_BCRYPT_HASH_FOR_KITCHEN', 'Ewa', 'Kowalska', 'kuchnia1@fastfood.com', 'kitchen_staff');

-- Produkty
INSERT INTO Products (name, description, price, category_id, image_url, is_available) VALUES
('Burger Classic', 'Klasyczny burger z wołowiną 100%, sałatą, pomidorem, cebulą i sosem.', 15.00, (SELECT category_id FROM Categories WHERE name='Burgery'), 'images/burger_classic.jpg', TRUE),
('Frytki Małe', 'Chrupiące frytki, porcja mała.', 6.00, (SELECT category_id FROM Categories WHERE name='Dodatki'), 'images/frytki.jpg', TRUE),
('Napój Cola', 'Oryginalna Coca-Cola 0.33l.', 5.00, (SELECT category_id FROM Categories WHERE name='Napoje'), 'images/cola.jpg', TRUE),
('Sałatka Vege', 'Mieszanka świeżych sałat z warzywami sezonowymi i sosem vinegret.', 12.50, (SELECT category_id FROM Categories WHERE name='Sałatki'), 'images/salatka_vege.jpg', TRUE),
('Cheeseburger', 'Burger z wołowiną i serem cheddar.', 17.00, (SELECT category_id FROM Categories WHERE name='Burgery'), 'images/cheeseburger.jpg', TRUE),
('Woda Niegazowana', 'Butelka wody niegazowanej 0.5l.', 4.00, (SELECT category_id FROM Categories WHERE name='Napoje'), 'images/woda.jpg', FALSE);

-- Przykładowe zamówienie 1
INSERT INTO Orders (user_id, order_status, order_type, table_number, total_amount, payment_status, payment_method, customer_notes) VALUES
(NULL, 'in_preparation', 'dine_in', '5', 35.00, 'pending', 'cash', 'Proszę bez cebuli w jednym burgerze.'); -- Poprawiona suma

INSERT INTO OrderItems (order_id, product_id, quantity, price_at_order, subtotal) VALUES
(1, (SELECT product_id FROM Products WHERE name='Burger Classic'), 2, 15.00, 30.00),
(1, (SELECT product_id FROM Products WHERE name='Napój Cola'), 1, 5.00, 5.00);

-- Przykładowe zamówienie 2
INSERT INTO Orders (user_id, order_status, order_type, total_amount, payment_status, payment_method) VALUES
(NULL, 'received', 'takeaway', 17.50, 'paid', 'blik'); -- Poprawiona suma

INSERT INTO OrderItems (order_id, product_id, quantity, price_at_order, subtotal) VALUES
(2, (SELECT product_id FROM Products WHERE name='Sałatka Vege'), 1, 12.50, 12.50),
(2, (SELECT product_id FROM Products WHERE name='Napój Cola'), 1, 5.00, 5.00);

-- Przykładowe zamówienie 3
INSERT INTO Orders (user_id, order_status, order_type, total_amount, payment_status, payment_method) VALUES
(NULL, 'ready_for_pickup', 'mcdrive', 28.00, 'paid', 'card_online');

INSERT INTO OrderItems (order_id, product_id, quantity, price_at_order, subtotal) VALUES
(3, (SELECT product_id FROM Products WHERE name='Cheeseburger'), 1, 17.00, 17.00),
(3, (SELECT product_id FROM Products WHERE name='Frytki Małe'), 1, 6.00, 6.00),
(3, (SELECT product_id FROM Products WHERE name='Napój Cola'), 1, 5.00, 5.00);
