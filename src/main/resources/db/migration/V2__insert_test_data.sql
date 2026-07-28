INSERT INTO airports (code, name, city)
VALUES
    ('SVO', 'Sheremetyevo International Airport', 'Moscow'),
    ('LED', 'Pulkovo Airport', 'Saint Petersburg'),
    ('VVO', 'Vladivostok International Airport', 'Vladivostok'),
    ('KHV', 'Khabarovsk Novy Airport', 'Khabarovsk'),
    ('SVX', 'Koltsovo International Airport', 'Ekaterinburg');

INSERT INTO aircrafts (model, capacity)
VALUES
    ('Boeing 737', 180),
    ('Airbus A320', 186),
    ('Sukhoi Superjet 100', 100);

INSERT INTO flights (
    flight_number,
    departure_airport_id,
    arrival_airport_id,
    departure_time,
    arrival_time,
    aircraft_id
)
VALUES
    ('SU101', (SELECT id FROM airports WHERE code = 'SVO'), (SELECT id FROM airports WHERE code = 'LED'), TIMESTAMP '2026-08-01 10:00:00', TIMESTAMP '2026-08-01 12:30:00', (SELECT id FROM aircrafts WHERE model = 'Boeing 737')),
    ('SU102', (SELECT id FROM airports WHERE code = 'LED'), (SELECT id FROM airports WHERE code = 'SVO'), TIMESTAMP '2026-08-02 09:00:00', TIMESTAMP '2026-08-02 11:30:00', (SELECT id FROM aircrafts WHERE model = 'Airbus A320')),
    ('SU103', (SELECT id FROM airports WHERE code = 'VVO'), (SELECT id FROM airports WHERE code = 'SVO'), TIMESTAMP '2026-08-03 08:00:00', TIMESTAMP '2026-08-03 09:00:00', (SELECT id FROM aircrafts WHERE model = 'Sukhoi Superjet 100')),
    ('SU104', (SELECT id FROM airports WHERE code = 'SVO'), (SELECT id FROM airports WHERE code = 'KHV'), TIMESTAMP '2026-08-04 13:00:00', TIMESTAMP '2026-08-04 14:30:00', (SELECT id FROM aircrafts WHERE model = 'Boeing 737')),
    ('SU105', (SELECT id FROM airports WHERE code = 'KHV'), (SELECT id FROM airports WHERE code = 'SVO'), TIMESTAMP '2026-08-05 15:00:00', TIMESTAMP '2026-08-05 16:30:00', (SELECT id FROM aircrafts WHERE model = 'Airbus A320')),
    ('SU106', (SELECT id FROM airports WHERE code = 'SVX'), (SELECT id FROM airports WHERE code = 'SVO'), TIMESTAMP '2026-08-06 07:30:00', TIMESTAMP '2026-08-06 09:30:00', (SELECT id FROM aircrafts WHERE model = 'Sukhoi Superjet 100')),
    ('SU107', (SELECT id FROM airports WHERE code = 'SVO'), (SELECT id FROM airports WHERE code = 'SVX'), TIMESTAMP '2026-08-07 17:00:00', TIMESTAMP '2026-08-07 19:00:00', (SELECT id FROM aircrafts WHERE model = 'Boeing 737')),
    ('SU108', (SELECT id FROM airports WHERE code = 'LED'), (SELECT id FROM airports WHERE code = 'VVO'), TIMESTAMP '2026-08-08 11:00:00', TIMESTAMP '2026-08-08 12:30:00', (SELECT id FROM aircrafts WHERE model = 'Airbus A320')),
    ('SU109', (SELECT id FROM airports WHERE code = 'VVO'), (SELECT id FROM airports WHERE code = 'LED'), TIMESTAMP '2026-08-09 12:00:00', TIMESTAMP '2026-08-09 13:30:00', (SELECT id FROM aircrafts WHERE model = 'Sukhoi Superjet 100')),
    ('SU110', (SELECT id FROM airports WHERE code = 'KHV'), (SELECT id FROM airports WHERE code = 'SVX'), TIMESTAMP '2026-08-10 14:00:00', TIMESTAMP '2026-08-10 16:00:00', (SELECT id FROM aircrafts WHERE model = 'Boeing 737'));

INSERT INTO passengers (first_name, last_name, passport_number, flight_id)
VALUES
    ('John', 'Smith', '4010000001', (SELECT id FROM flights WHERE flight_number = 'SU101')),
    ('Emily', 'Johnson', '4010000002', (SELECT id FROM flights WHERE flight_number = 'SU101')),
    ('Michael', 'Williams', '4010000003', (SELECT id FROM flights WHERE flight_number = 'SU102')),
    ('Sophia', 'Brown', '4010000004', (SELECT id FROM flights WHERE flight_number = 'SU102')),
    ('Daniel', 'Jones', '4010000005', (SELECT id FROM flights WHERE flight_number = 'SU103')),
    ('Olivia', 'Garcia', '4010000006', (SELECT id FROM flights WHERE flight_number = 'SU103')),
    ('James', 'Miller', '4010000007', (SELECT id FROM flights WHERE flight_number = 'SU104')),
    ('Ava', 'Davis', '4010000008', (SELECT id FROM flights WHERE flight_number = 'SU104')),
    ('Robert', 'Wilson', '4010000009', (SELECT id FROM flights WHERE flight_number = 'SU105')),
    ('Isabella', 'Moore', '4010000010', (SELECT id FROM flights WHERE flight_number = 'SU105')),
    ('William', 'Taylor', '4010000011', (SELECT id FROM flights WHERE flight_number = 'SU106')),
    ('Mia', 'Anderson', '4010000012', (SELECT id FROM flights WHERE flight_number = 'SU106')),
    ('David', 'Thomas', '4010000013', (SELECT id FROM flights WHERE flight_number = 'SU107')),
    ('Charlotte', 'Jackson', '4010000014', (SELECT id FROM flights WHERE flight_number = 'SU107')),
    ('Joseph', 'White', '4010000015', (SELECT id FROM flights WHERE flight_number = 'SU108')),
    ('Amelia', 'Harris', '4010000016', (SELECT id FROM flights WHERE flight_number = 'SU108')),
    ('Thomas', 'Martin', '4010000017', (SELECT id FROM flights WHERE flight_number = 'SU109')),
    ('Harper', 'Thompson', '4010000018', (SELECT id FROM flights WHERE flight_number = 'SU109')),
    ('Charles', 'Garcia', '4010000019', (SELECT id FROM flights WHERE flight_number = 'SU110')),
    ('Evelyn', 'Martinez', '4010000020', (SELECT id FROM flights WHERE flight_number = 'SU110'));
