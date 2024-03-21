## Фича: автоматический учет переводов самому себе

Для непредставленных в импорте банков данные приходится продолжать вносить вручную. Чтобы уменьшить количество 
ручной работы для не представленных в импорте банков, можно по заранее заданной таблице находить транзакции 
по некоему паттерну (перечислить в таблице) для дополнения её другой транзакцией до создания перевода в требуемый счет.
Таким образом можно регистрировать как входящие, так и исходящие переводы в счетах, которые отсутствуют в импортах.

Для этого потребуется создание нового шага:
- [_transferPatternPreMatchingStep_](#transferPatternPreMatchingStep) - расположен сразу перед _transferMatchingStep_

Потребуется новая таблица `matching.transfer_pattern`:

| Поле               | Описание                                                                                            |
|--------------------|-----------------------------------------------------------------------------------------------------|
| source_name        | Точное значение соответствующего поля `name` транзакции в `matching.transaction`                    |
| source_description | Точное значение соответствующего поля `description` транзакции в `matching.transaction`             |
| source_type        | Тип операции транзакции в `matching.transaction` - если здесь доход, значит будет создана расходная |
| source_account_id  | значение поля `matching.account.account_id` исходной транзакции                                     |
| target_account_id  | id счета (`financepm.account.id`), для которого нужно создать дополняющую транзакцию                |

`matching.self_transfer.source_type` 
- если здесь указан тип "Доход", значит нужно создать расходную операцию для `target_account_id`.
- если здесь указан тип "Расход", значит нужно создать доходную операцию для `target_account_id`.


### _transferPatternPreMatchingStep_

###### Логика работы:
- найти в `matching.transactions` все записи, которые соответствуют перечисленным в `matching.transfer_pattern` 
  кортежам [source_name, source_description, source_type, source_account_id]
- для найденных записей проверить наличие комплементарной записи в `matching.transaction` (клон исходной записи, со следующими различиями):
  - `matching.transaction.account_id` = `target_account_id` 
  - `matching.transaction.type` = противоположный исходной
- если такой записи нет - создать её.

Для _transferMatchingStep_ потребуется изменить условие для выборки: выбирать записи с ненулевым описанием в случае, если они им явно проставлен event_id = 1.


#### Гипотеза

> _alfaCashTransferMatchingStep_ и _tinkoffCashTransferMatchingStep_ являются частными случаями _patternBasedTransfersMatchingStep_

В этом случае TinkoffRecordReader и AlfaRecordReader потребуют правки