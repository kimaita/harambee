CREATE TYPE "entities" AS ENUM ('individual', 'organisation');
CREATE TYPE "pledge_status" AS ENUM ('pending', 'partial', 'fulfilled');
CREATE TYPE "user_type" AS ENUM ('donor', 'recipient');

CREATE TABLE "deliveries" (
    "id" serial NOT NULL,
    "donor" integer NOT NULL,
    "pledge_item" integer NOT NULL,
    "quantity" integer NOT NULL,
    "delivery_date" timestamp with time zone NOT NULL,
    CONSTRAINT "pk_donations_id" PRIMARY KEY ("id")
);

CREATE TABLE "request_items" (
    "id" serial NOT NULL,
    "request_id" integer NOT NULL,
    "item_id" integer NOT NULL,
    "quantity" integer NOT NULL,
    "priority" integer,
    "date_needed" date,
    CONSTRAINT "pk_request_items_id" PRIMARY KEY ("id")
);

CREATE TABLE "disbursements" (
    "id" serial NOT NULL,
    "request_item" integer NOT NULL,
    "recipient" integer NOT NULL,
    "quantity" bigint NOT NULL,
    "date_given" bigint NOT NULL,
    CONSTRAINT "pk_table_4_id" PRIMARY KEY ("id")
);

CREATE TABLE "users" (
    "id" serial NOT NULL,
    "name" varchar NOT NULL,
    "email" varchar,
    "phone_number" varchar NOT NULL,
    "address" varchar,
    "role" user_type NOT NULL,
    "entity_type" entities NOT NULL,
    "created_at" timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "pk_table_8_id" PRIMARY KEY ("id")
);
-- Indexes
CREATE INDEX "users_index_2" ON "users" ("role");

CREATE TABLE "item_categories" (
    "id" serial NOT NULL,
    "name" varchar NOT NULL,
    "description" varchar,
    CONSTRAINT "pk_table_11_id" PRIMARY KEY ("id")
);

CREATE TABLE "pledge_items" (
    "id" serial NOT NULL,
    "pledge_id" integer NOT NULL,
    "item_id" integer NOT NULL,
    "quantity" integer NOT NULL,
    "delivery_date" timestamp with time zone,
    CONSTRAINT "pk_table_10_id" PRIMARY KEY ("id")
);

CREATE TABLE "pledges" (
    "id" serial NOT NULL,
    "user_id" serial NOT NULL,
    "description" varchar,
    "created_at" timestamp with time zone,
    "updated_at" timestamp with time zone,
    CONSTRAINT "pk_table_5_id" PRIMARY KEY ("id")
);

CREATE TABLE "inventory_items" (
    "id" serial NOT NULL,
    "name" varchar NOT NULL,
    "description" varchar,
    "quantity" integer NOT NULL DEFAULT 0,
    "units" varchar,
    "last_restock" timestamp with time zone,
    "category" integer,
    CONSTRAINT "pk_table_3_id" PRIMARY KEY ("id")
);
-- Indexes
CREATE INDEX "inventory_items_index_2" ON "inventory_items" ("name");

CREATE TABLE "requests" (
    "id" serial NOT NULL,
    "user_id" integer NOT NULL,
    "description" varchar,
    "created_at" timestamp with time zone NOT NULL,
    "updated_at" timestamp with time zone NOT NULL,
    "repeating" bigint,
    CONSTRAINT "pk_table_7_id" PRIMARY KEY ("id")
);
-- Indexes
CREATE INDEX "requests_index_2" ON "requests" ("user_id");

-- Foreign key constraints
-- Schema: public
ALTER TABLE "pledge_items" ADD CONSTRAINT "fk_pledge_items_item_id_inventory_items_id" FOREIGN KEY("item_id") REFERENCES "inventory_items"("id");
ALTER TABLE "inventory_items" ADD CONSTRAINT "fk_inventory_items_category_item_categories_id" FOREIGN KEY("category") REFERENCES "item_categories"("id");
ALTER TABLE "deliveries" ADD CONSTRAINT "fk_deliveries_pledge_item_pledge_items_id" FOREIGN KEY("pledge_item") REFERENCES "pledge_items"("id");
ALTER TABLE "pledge_items" ADD CONSTRAINT "fk_pledge_items_pledge_id_pledges_id" FOREIGN KEY("pledge_id") REFERENCES "pledges"("id");
ALTER TABLE "disbursements" ADD CONSTRAINT "fk_disbursements_request_item_request_items_id" FOREIGN KEY("request_item") REFERENCES "request_items"("id");
ALTER TABLE "request_items" ADD CONSTRAINT "fk_request_items_item_id_inventory_items_id" FOREIGN KEY("item_id") REFERENCES "inventory_items"("id");
ALTER TABLE "request_items" ADD CONSTRAINT "fk_request_items_request_id_requests_id" FOREIGN KEY("request_id") REFERENCES "requests"("id");
ALTER TABLE "requests" ADD CONSTRAINT "fk_requests_user_id_users_id" FOREIGN KEY("user_id") REFERENCES "users"("id");
ALTER TABLE "pledges" ADD CONSTRAINT "fk_pledges_user_id_users_id" FOREIGN KEY("user_id") REFERENCES "users"("id");
ALTER TABLE "disbursements" ADD CONSTRAINT "fk_disbursements_recipient_users_id" FOREIGN KEY("recipient") REFERENCES "users"("id");
ALTER TABLE "deliveries" ADD CONSTRAINT "fk_users_id_deliveries_donor" FOREIGN KEY("donor") REFERENCES "users"("id");
