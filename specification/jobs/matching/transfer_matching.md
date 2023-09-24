## transferMatchingStep

1. Работа с наличными:
#### Тинькофф
```
    1. Находим все matching.draft_transaction, для которых category = 'Наличные'.
    2. Для таких записей создаем новые записи в _financepm.transaction_, для которых `transaction.type = 2`
    3. Для каждой созданной записи в _financepm.transaction_ создаем парную для неё запись, для которых `transaction.type = 1`
    4. Для каждой созданной выше записи задать _financepm.transaction.category_id_ = null
    5. Для каждой созданной выше записи задать _financepm.transaction.event_id_ = 1
```

2. Предпринимаем попытку выполнить матчинг транзакций из `matching.draft_transaction` 
как переводов по следующему правилу:
```
   1. Две транзакции совершены в рамках 5 секунд друг от друга.
   2. Абсолютные значения суммы данных двух транзакций равны.
   3. Одна из транзакций - доходная, а другая расходная.
   3. Валюты транзакций одинаковы.
   4. Счета не одинаковы.
```
При соблюдении всех правил выше, в `matching.draft_transfer` добавляется запись 
по представленному в таблице маппингу. Запись добавляется тогда, когда `matching.draft_transaction.id`
не фигурирует в столбце `draft_transaction_id_from` или `draft_transaction_id_to`.

| <u>`matching.draft_transfer`</u> | Значение                                              |
|----------------------------------|-------------------------------------------------------|
| id                               | seq_next_id                                           |
| name                             | значение по умолчанию: "Перевод"                      |
| draft_transaction_id_from        | `matching.draft_transaction.id` расходной транзакции  |
| draft_transaction_id_to          | `matching.draft_transaction.id` доходной транзакции   |
| validated                        | `false` - все такие переводы требуют ручной валидации |

3. После **ручной** валидации и задания `matching.draft_transfer.validated = true` при следующем
выполнении **transferMatchingStep** транзакции из `matching.draft_transfer`, для которых 
`matching.draft_transfer.validated = true` должны быть добавлены в `financepm.transaction` 
согласно маппингу, указанному [здесь](transaction_matching.md), а также задав для каждой 
созданной в `financepm.transaction` записи значений:
- `financepm.transaction.category_id = null`,
- `financepm.transaction.event_id = 1`

В рамках одной транзакции следует также перенести запись о переводе в `financepm.transfer` согласно маппингу

| <u>`financepm.transfer`</u> | Значение                                                                                                                                                      |
|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| id                          | seq_next_id                                                                                                                                                   |
| name                        | значение по умолчанию: "Перевод"                                                                                                                              |
| transaction_id_from         | `financepm.transaction.id` такой, что `financepm.draft_transaction_id = matching.draft_transfer.draft_transaction_id_from` И `financepm.transaction.type = 2` |
| transaction_id_to           | `financepm.transaction.id` такой, что `financepm.draft_transaction_id = matching.draft_transfer.draft_transaction_id_to`   И `financepm.transaction.type = 1` |


```
Возможно в Тинькофф сделают новый столбец "Комментарий" и тогда получится создать
правила матчинга переводов и компенсаций в рамках групповых трат (например в ресторанах 
или при совместной покупке билетов).
 
В Альфе такое поле уже есть, но я его не проверял - давно не пользовался их дебетовкой.
```