CREATE TABLE public.todos (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    title VARCHAR(255),
    completed BOOLEAN,
    version INTEGER
);
