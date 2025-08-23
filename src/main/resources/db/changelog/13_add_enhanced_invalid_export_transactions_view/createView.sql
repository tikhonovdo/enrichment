create or replace view matching.invalid_export_transactions_enhanced
            (invalid_tx, date, type, sum, account_id, category_id, id, refund_for_id, event_id, name, description,
             draft_transaction_id, validated)
as
SELECT id in (select id from matching.invalid_export_transactions) as invalid_tx,
       mt.date,
       mt.type,
       mt.sum,
       mt.account_id,
       mt.category_id,
       mt.id,
       mt.refund_for_id,
       mt.event_id,
       mt.name,
       mt.description,
       mt.draft_transaction_id,
       mt.validated
from matching.transaction mt
where id in (
    select distinct unnest(ids)
    from (WITH ordered_data AS (
        SELECT
            mt.id,
            ARRAY[
                (LAG(mt.id, -2) OVER (ORDER BY mt.date))::bigint,
                (LAG(mt.id, -1) OVER (ORDER BY mt.date))::bigint,
                mt.id,
                (LAG(mt.id, 1) OVER (ORDER BY mt.date))::bigint,
                (LAG(mt.id, 2) OVER (ORDER BY mt.date))::bigint
                ] as ids
        FROM matching.transaction mt
        order by date desc
    )
          SELECT id, ids
          FROM ordered_data
          where id in (select id from matching.invalid_export_transactions)) as tmp
);

comment on view matching.invalid_export_transactions_enhanced is
    'Расширенное представление транзакций, которые требуют валидации и не могут быть экспортированы в financepm.transaction на шаге exportMatchingTransactionsStep: добавлено отображение соседних операций, которые могут добавить контекста';

alter table matching.invalid_export_transactions_enhanced
    owner to CURRENT_USER;

CREATE OR REPLACE RULE update_matching_transaction AS
    ON UPDATE TO matching.invalid_export_transactions_enhanced
    DO INSTEAD
    UPDATE matching.transaction
    SET date = new.date,
        type = new.type,
        sum = new.sum,
        account_id = new.account_id,
        category_id = new.category_id,
        refund_for_id = new.refund_for_id,
        event_id = new.event_id,
        name = new.name,
        description = new.description,
        draft_transaction_id = new.draft_transaction_id,
        validated = new.validated
    WHERE id = old.id;

CREATE OR REPLACE RULE insert_matching_transaction AS
    ON INSERT TO matching.invalid_export_transactions_enhanced
    DO INSTEAD
    INSERT INTO matching.transaction(draft_transaction_id, name, type, category_id, date, sum, account_id, description, event_id, validated, refund_for_id)
    VALUES (new.draft_transaction_id, new.name, new.type, new.category_id, new.date, new.sum, new.account_id, new.description, new.event_id, new.validated, new.refund_for_id);