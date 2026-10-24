INSERT INTO category (name)
SELECT predefined.name
FROM (VALUES
    ('Education'),
    ('Entertainment'),
    ('Food'),
    ('Healthcare'),
    ('Housing'),
    ('Other'),
    ('Shopping'),
    ('Transport'),
    ('Travel'),
    ('Utilities'),
    ('Groceries'),
    ('Dining Out'),
    ('Fitness'),
    ('Personal Care'),
    ('Gifts'),
    ('Subscriptions'),
    ('Insurance'),
    ('Taxes'),
    ('Investments')
) AS predefined(name)
WHERE NOT EXISTS (
    SELECT 1 FROM category existing WHERE lower(existing.name) = lower(predefined.name)
);
