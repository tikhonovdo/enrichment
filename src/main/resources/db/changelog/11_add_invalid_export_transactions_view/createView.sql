create or replace view matching.invalid_export_transactions
            (date, type, sum, account_id, category_id, id, refund_for_id, event_id, name, description,
             draft_transaction_id, validated)
as
SELECT mt.date,
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
FROM matching.transaction mt
         LEFT JOIN financepm.category fc ON mt.category_id = fc.id
         LEFT JOIN financepm.account fa ON mt.account_id = fa.id
         LEFT JOIN financepm.type tr_t ON mt.type = tr_t.id
         LEFT JOIN financepm.type cat_t ON fc.type = cat_t.id
WHERE (mt.account_id IS NULL
           OR mt.category_id IS NOT NULL AND mt.event_id IS NOT NULL
           OR mt.category_id IS NULL AND mt.event_id IS NULL OR tr_t.id <> cat_t.id)
  AND NOT mt.validated AND mt.refund_for_id IS NULL;

comment on view matching.invalid_export_transactions is 'Транзакции, которые требуют валидации и не могут быть экспортированы в financepm.transaction на шаге exportMatchingTransactionsStep';

alter table matching.invalid_export_transactions
    owner to CURRENT_USER;

CREATE OR REPLACE RULE update_matching_transaction AS
    ON UPDATE TO matching.invalid_export_transactions
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
    ON INSERT TO matching.invalid_export_transactions
    DO INSTEAD
    INSERT INTO matching.transaction(draft_transaction_id, name, type, category_id, date, sum, account_id, description, event_id, validated, refund_for_id)
    VALUES (new.draft_transaction_id, new.name, new.type, new.category_id, new.date, new.sum, new.account_id, new.description, new.event_id, new.validated, new.refund_for_id);