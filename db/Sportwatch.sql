CREATE TABLE IF NOT EXISTS "users" (
	"user_id" serial NOT NULL UNIQUE,
	"username" varchar(50) NOT NULL UNIQUE,
	"password" varchar(72) NOT NULL,
	"email" varchar(400) UNIQUE,
	"created_at" timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
	"streamer_id" int UNIQUE,
	"notifications" varchar(128)[],
	PRIMARY KEY ("user_id")
);

CREATE TABLE IF NOT EXISTS "streamer_details" (
	"streamer_id" int NOT NULL UNIQUE,
	"is_live" boolean NOT NULL DEFAULT false,
	"sport_type" varchar(30) NOT NULL,
);

CREATE TABLE IF NOT EXISTS "streams" (
	"stream_id" serial NOT NULL UNIQUE,
	"author_id" int NOT NULL UNIQUE,
	"title" varchar(32),
	"arn" varchar(2048),
	"object_key" varchar(160) UNIQUE,
	"category" varchar(30) NOT NULL,
	"desc" varchar(512),
	"created_at" timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
 	"thumbnail_obj_key" varchar(160) UNIQUE,
	PRIMARY KEY ("stream_id")
);

CREATE TABLE IF NOT EXISTS "comments" (
	"comment_id" serial NOT NULL UNIQUE,
	"author_id" int NOT NULL UNIQUE,
	"stream_id" int NOT NULL UNIQUE,
	"comment" varchar(512) NOT NULL,
	"created_at" timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY ("comment_id")
);

CREATE TABLE IF NOT EXISTS "suscribers_streamers" (
	"suscriber_id" int NOT NULL,
	"streamer_id" int NOT NULL,
	PRIMARY KEY ("suscriber_id")
);

CREATE TABLE IF NOT EXISTS "followers_streamers" (
	"follower_id" int NOT NULL,
	"streamer_id" int NOT NULL,
	PRIMARY KEY ("follower_id")
);

ALTER TABLE "users" ADD CONSTRAINT "users_fk5" FOREIGN KEY ("streamer_id") REFERENCES "streamer_details"("streamer_id");
ALTER TABLE "streamer_details" ADD CONSTRAINT "streamer_details_fk0" FOREIGN KEY ("streamer_id") REFERENCES "users"("user_id");
ALTER TABLE "streams" ADD CONSTRAINT "streams_fk1" FOREIGN KEY ("author_id") REFERENCES "streamer_details"("streamer_id");

ALTER TABLE "comments" ADD CONSTRAINT "comments_fk1" FOREIGN KEY ("author_id") REFERENCES "users"("user_id");
ALTER TABLE "comments" ADD CONSTRAINT "comments_fk2" FOREIGN KEY ("stream_id") REFERENCES "streams"("stream_id");

ALTER TABLE "suscribers_streamers" ADD CONSTRAINT "suscribers_streamers_fk0" FOREIGN KEY ("suscriber_id") REFERENCES "users"("user_id");
ALTER TABLE "suscribers_streamers" ADD CONSTRAINT "suscribers_streamers_fk1" FOREIGN KEY ("streamer_id") REFERENCES "users"("user_id");

ALTER TABLE "followers_streamers" ADD CONSTRAINT "followers_streamers_fk0" FOREIGN KEY ("follower_id") REFERENCES "users"("user_id");
ALTER TABLE "followers_streamers" ADD CONSTRAINT "followers_streamers_fk1" FOREIGN KEY ("streamer_id") REFERENCES "users"("user_id");
