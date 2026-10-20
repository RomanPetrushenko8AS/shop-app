## 🛠 Технологический стек

*  Java 21
* **Веб-сервер:** Embedded Jetty 11
* **REST API:** Jersey 3 (JAX-RS, Jakarta EE 10)
* **Сериализация JSON:** Jackson (`jersey-media-json-jackson`)
* **База данных:** PostgreSQL
* **ORM / Слой данных:** Hibernate 6 (JPA `EntityManager`), HikariCP (пул соединений)
* **Логирование:** SLF4J + Logback (с ротацией по 1 КБ на 2 файла)
* **Документация:**  Swagger UI

---
## 🗄 Структура базы данных

Схема состоит из трех связанных таблиц:
-- 1. Таблица товаров
CREATE TABLE IF NOT EXISTS product (
    id SERIAL PRIMARY KEY,
    name VARCHAR(300) NOT NULL,
    description TEXT,
    price NUMERIC(12, 2) NOT NULL,
    quantity INTEGER NOT NULL
);

-- 2. Таблица клиентов
CREATE TABLE IF NOT EXISTS client (
    client_id SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    surname VARCHAR(30) NOT NULL,
    email VARCHAR(50),
    phone VARCHAR(30) NOT NULL
);

-- 3. Таблица заказов
CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    client_id INTEGER NOT NULL REFERENCES client(client_id),
    product_id INTEGER NOT NULL REFERENCES product(id),
    quantity INTEGER NOT NULL,
    total_price NUMERIC(12, 2),
    status VARCHAR(20) DEFAULT 'New',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_client FOREIGN KEY (client_id) REFERENCES client(client_id),
    CONSTRAINT fk_orders_product FOREIGN KEY (product_id) REFERENCES product(id)
);