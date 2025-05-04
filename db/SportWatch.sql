-- Primary key no setea el user_id como serial
CREATE TABLE IF NOT EXISTS users (
	user_id serial primary key NOT NULL UNIQUE,
	username varchar(50) NOT NULL UNIQUE,
	password varchar(72) NOT NULL,
	email varchar(400) UNIQUE,
	created_at timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
	streamer_id int UNIQUE,
	follows int[],
	subscribed int[],
	notifications varchar(128)[]
	-- PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS streamer_details (
	streamer_id serial primary key NOT NULL UNIQUE,
	user_id int NOT NULL UNIQUE,
	is_live boolean NOT NULL DEFAULT false,
	sports varchar(30)[] NOT NULL,
	followers int[],
	subscribers int[]
	-- PRIMARY KEY (streamer_id)
);

CREATE TABLE IF NOT EXISTS streams (
	stream_id serial primary key NOT NULL UNIQUE,
	author_id int NOT NULL UNIQUE,
	metadata varchar(256),
	created_at timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
	file_route varchar(256) NOT NULL UNIQUE
	-- PRIMARY KEY (stream_id)
);

CREATE TABLE IF NOT EXISTS comments (
	comment_id serial primary key NOT NULL UNIQUE,
	author_id int NOT NULL UNIQUE,
	stream_id int NOT NULL UNIQUE,
	comment varchar(256) NOT NULL,
	created_at timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP
	-- PRIMARY KEY (comment_id)
);

ALTER TABLE users ADD CONSTRAINT Users_fk6 FOREIGN KEY (streamer_id) REFERENCES streamer_details(streamer_id);
ALTER TABLE streamer_details ADD CONSTRAINT Streamer_details_fk1 FOREIGN KEY (user_id) REFERENCES users(user_id);
ALTER TABLE streams ADD CONSTRAINT Streams_fk1 FOREIGN KEY (author_id) REFERENCES streamer_details(streamer_id);
ALTER TABLE comments ADD CONSTRAINT Comments_fk1 FOREIGN KEY (author_id) REFERENCES users(user_id);

ALTER TABLE comments ADD CONSTRAINT Comments_fk2 FOREIGN KEY (stream_id) REFERENCES streams(stream_id);
