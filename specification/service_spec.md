### Спецификация сервиса обогащения данных FinancePM

1. Загрузка данных FinancePM - `POST /data` - **ВЫПОЛНЕНО**
  - очистка данных таблиц
    * currency
    * currency_matching
    * category
    * category_matching
    * account
    * account_matching
    * transaction
    * transfer
    * arrear
    * arrear_transaction
  - парсинг записей в сущности
  - сохранение по таблицам
  - подготовка к следующим шагам (`UPDATE transfer SET validated = true WHERE bank_id IS NULL;`)


2. Загрузка данных от Банка - `POST /data?bankId=` - **ВЫПОЛНЕНО**
   - сохранение файла 


3. matchingJob - `POST /performMatching`
   * categoryMatchingStep - **ВЫПОЛНЕНО**
   * currencyMatchingStep - **ВЫПОЛНЕНО**
   * accountMatchingStep - **ВЫПОЛНЕНО**
   * transactionMatchingStep
   * transferMatchingStep

4. Выгрузка текущих данных - `GET /data`