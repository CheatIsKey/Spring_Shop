-- 목적:
-- 1) attachment.url 이 과거 @Lob -> PostgreSQL oid 였던 스키마를
--    안전하게 TEXT 로 전환 (이미 TEXT 면 무해)
-- 2) 제약/길이/인덱스 정리

DO $$
DECLARE
    col_type text;
BEGIN
    SELECT data_type
      INTO col_type
      FROM information_schema.columns
     WHERE table_schema = 'public'
       AND table_name  = 'attachment'
       AND column_name = 'url';

    -- 1) url 이 oid 타입이면: 새 컬럼으로 LO 내용 복사 후 교체
    IF col_type = 'oid' THEN
        -- 임시 새 컬럼
        ALTER TABLE attachment ADD COLUMN url_new text;

        -- Large Object(oid)의 바이트 → UTF8 문자열로 변환
        -- ※ url 컬럼에 "문자열 URL"을 LOB 로 저장했던 케이스를 가정
        UPDATE attachment
           SET url_new = convert_from(lo_get(url), 'UTF8');

        -- NOT NULL 제약(필요 시 빈 문자열 대체)
        UPDATE attachment SET url_new = '' WHERE url_new IS NULL;

        -- 교체
        ALTER TABLE attachment DROP COLUMN url;
        ALTER TABLE attachment RENAME COLUMN url_new TO url;

        -- (선택) 사용했던 Large Object 정리: 참조 끊긴 LO 제거
        --   주의: 운영에서 즉시 실행이 부담되면 생략 가능
        -- PERFORM lo_unlink(oid) ... 형태는 참조 추적이 필요해 생략

    -- 2) 이미 TEXT/CHARACTER VARYING 계열이면: 타입/제약만 통일
    ELSE
        ALTER TABLE attachment
            ALTER COLUMN url TYPE text USING url::text;
    END IF;
END $$;

-- 공통 제약 정리
ALTER TABLE attachment
    ALTER COLUMN url SET NOT NULL;

-- 기타 칼럼도 엔티티와 맞춤(이미 맞다면 무해)
ALTER TABLE attachment
    ALTER COLUMN file_name TYPE varchar(255),
    ALTER COLUMN file_name SET NOT NULL,
    ALTER COLUMN mime_type TYPE varchar(100),
    ALTER COLUMN mime_type SET NOT NULL,
    ALTER COLUMN size SET NOT NULL;

-- 티켓 조회 인덱스 보강 (중복 생성 방지)
CREATE INDEX IF NOT EXISTS idx_attachment_ticket ON attachment (ticket_id);
