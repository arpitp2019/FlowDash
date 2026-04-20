ALTER TABLE decision_message ADD COLUMN updated_at timestamp(6) not null DEFAULT now();
