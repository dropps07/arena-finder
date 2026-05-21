INSERT INTO
    arenas (
        name,
        address,
        city,
        latitude,
        longitude,
        sport,
        open_time,
        close_time,
        price_per_hour,
        owner_id
    )
VALUES (
        'Champions Turf',
        'Lanka, Varanasi',
        'Varanasi',
        25.3176,
        82.9739,
        'FOOTBALL',
        '06:00',
        '22:00',
        500.0,
        1
    ) ON CONFLICT DO NOTHING;

INSERT INTO
    arenas (
        name,
        address,
        city,
        latitude,
        longitude,
        sport,
        open_time,
        close_time,
        price_per_hour,
        owner_id
    )
VALUES (
        'City Badminton Hall',
        'Sigra, Varanasi',
        'Varanasi',
        25.3200,
        82.9800,
        'BADMINTON',
        '07:00',
        '21:00',
        300.0,
        1
    ) ON CONFLICT DO NOTHING;

INSERT INTO
    arenas (
        name,
        address,
        city,
        latitude,
        longitude,
        sport,
        open_time,
        close_time,
        price_per_hour,
        owner_id
    )
VALUES (
        'Sports Arena Mumbai',
        'Andheri, Mumbai',
        'Mumbai',
        19.1136,
        72.8697,
        'BOTH',
        '05:00',
        '23:00',
        800.0,
        2
    ) ON CONFLICT DO NOTHING;