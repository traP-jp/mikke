CREATE TABLE file_info
(
    id           UUID PRIMARY KEY,
    file_name    VARCHAR(255) NOT NULL,
    file_size    BIGINT       NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    uploader_id  UUID         NOT NULL,
    upload_date  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);