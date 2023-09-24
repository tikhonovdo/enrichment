### Спецификация сервиса обогащения данных FinancePM

1. Загрузка данных FinancePM - `POST /upload` - **ВЫПОЛНЕНО**
  - очистка данных таблиц
    * financepm.currency
    * matching.currency
    * financepm.category
    * matching.category
    * financepm.account
    * matching.account
    * financepm.transaction
    * financepm.transfer
    * financepm.arrear
    * financepm.arrear_transaction
  - парсинг записей в сущности
  - сохранение по таблицам


2. Загрузка данных от Банка - `POST /upload` - **ВЫПОЛНЕНО**
    - [x] Тинькофф 


3. matchingJob - `POST /performMatching`
   - [x] categoryMatchingStep
   - [x] currencyMatchingStep
   - [x] accountMatchingStep
   - [ ] transactionMatchingStep
     - [ ] foo
   - [ ] transferMatchingStep

4. Выгрузка текущих данных - `GET /data`