## transactionMatchingStep

Для всех записей из таблицы <u>draft_transaction</u>, у которых
* `category_matching.category_id != null`, для соответствующего `bank_id != null`,
* `currency_matching.currency_id != null`, для соответствующего `bank_id != null`,
* `account_matching.account_id != null`, для соответствующего `bank_id != null`

выполнить разбор и вставку результатов в таблицу <u>transaction</u>
в соответствии с правилами матчинга:

| `transaction`      | `draft_transaction`                                                         |
|--------------------|-----------------------------------------------------------------------------|
| name               | Описание операции                                                           |
| type               | Доход/расход (1 или 2 соответственно)                                       |
| category_id        | `category_matching.category_id`, соответствующий маппингу конкретного Банка |
| date               | Дата операции (допустимо с точностью до дня)                                |
| sum                | Сумма операции в валюте Счета                                               |
| account_id         | `account_matching.account_id`, соответствующий маппингу конкретного Банка   |
| description        | Комментарий к операции                                                      |
| event_id | `null` (для новых записей)                                                  | 

Повторить процедуру для каждого Банка.
