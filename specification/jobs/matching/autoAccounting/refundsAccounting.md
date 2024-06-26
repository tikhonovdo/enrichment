## Фича: автоматический учет компенсаций

По комментарию во входящем переводе автоматически рассчитывать сумму расходной транзакции во время финального матчинга в financepm.transaction.
Для этого потребуется работа в трех шагах: правки в _exportMatchingTransactionsStep_ и создание двух шагов:
- [_searchRefundStep_](#searchrefundstep) - новый шаг, расположен непосредственно перед _exportMatchingTransactionsStep_
- [_exportMatchingTransactionsStep_](#доработка-exportMatchingTransactionsStep) - существует, потребуется правка
- [_applyRefundStep_](#applyrefundstep) - новый шаг, расположен сразу после _exportMatchingTransactionsStep_

### _searchRefundStep_

###### Логика работы:
- для всех доходных транзакций в `matching.transaction` с непустым комментарием должна быть найдена такая расходная транзакция,
  - имя которой эквивалентно значению комментария исходной доходной транзакции и 
  - находится в диапазоне от начала прошлых суток с момента совершения транзакции и до конца суток с момента совершения транзакции.
- в случае нахождения такой записи, ID найденной расходной транзакции должен быть задан в поле `matching.transaction.refund_for_id` записи доходных транзакций.

Для шага _transferMatchingStep_ (в TransferMatchingStepReader) потребуется задать дополнительное условие: для переводов отбирать только те доходные транзакции, в которых нет комментария


### Доработка _exportMatchingTransactionsStep_

###### Логика работы:
- выбрать записи по существующей логике, кроме тех, у которых поле `matching.transaction.refund_for_id` != NULL


### _applyRefundStep_

###### Логика работы:
- Выбрать записи по скрипту
  ```
    SELECT SUM(sum) as sum, refund_for_id, id as source_id
    FROM matching.transaction 
    WHERE description is not null 
        AND length(description) > 0 
        AND refund_for_id IS NOT NULL 
        AND NOT validated
    GROUP BY refund_for_id, id;
    ```
- Для выбранных записей по значению поля `refund_for_id` найти `financepm.transaction` и
  для каждой найденной транзакции:
    - вычесть соответствующую сумму из выборки п.1.
    - в случае, если по окончанию предыдущего шага знак суммы выбранной записи изменился (т.е. фактически расходная операция стала доходной), 
      то следует выполнить следующие действия:
        1) задать для заданного `financepm.transaction` противоположное значение type: 1, если было 2 и 2, если было 1.
        2) задать категорию "Другое" в рамках заданного типа (id должны быть заданы, либо находиться по имени).
    - зафиксировать изменения в БД

##### Зачем вносить правки сразу в `financepm.transaction`, вместо `matching.transaction`?
Затем чтобы исходные данные претерпевали минимальные изменения - после подобных правок в `matching.transaction`
будет сложно провести трассировку того, как применялись изменения.
