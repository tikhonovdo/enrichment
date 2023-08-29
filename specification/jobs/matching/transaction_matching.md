## transactionMatchingStep

Для всех записей из таблицы <u>draft_transaction</u>, у которых
* `category_matching.category_id != null`, для соответствующего `bank_id != null`,
* `currency_matching.currency_id != null`, для соответствующего `bank_id != null`,
* `account_matching.account_id != null`, для соответствующего `bank_id != null`

выполнить разбор и вставку результатов в таблицу <u>transaction</u>
в соответствии с правилами матчинга:

| `transaction` | `draft_transaction`                                                         |
|---------------|-----------------------------------------------------------------------------|
| name          | Описание операции                                                           |
| type          | Доход/расход (1 или 2 соответственно)                                       |
| category_id   | `category_matching.category_id`, соответствующий маппингу конкретного Банка |
| date          | Дата операции (допустимо с точностью до дня)                                |
| sum           | Сумма операции в валюте Счета                                               |
| account_id    | `account_matching.account_id`, соответствующий маппингу конкретного Банка   |
| description   | Комментарий к операции                                                      |
| event_id      | `null` (для новых записей)                                                  | 

Повторить процедуру для каждого Банка.

В случае невозможности сматчить транзацкии в соотвествии с данными из таблиц
- `account_matching`,
- `category_matching`,
- `currency_matching`, 
следует добавить информацию о таких транзакциях в таблицу `manual_transaction`, которая полностью повторяет 
структуру таблицы `transaction`, но ни одно поле в ней не является в ней обязательным.

После ручного внесения изменений, записи из `manual_transaction` попадают в `transaction` 
с отметкой о добавлении в последнюю. При