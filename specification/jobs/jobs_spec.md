### Спецификация процесса обогащения данных FinancePM

1. Загрузить исходный data-файл FinancePM
   1. разложить файл в таблицы из миграции `init-finance-pm.xml`
   2. выполнить `UPDATE transfer SET validated = true;`  
2. загрузить транзакции в формате банка в виде XLS (взять Apache POI) для каждого банка
   * *(возможно стоит перегнать данные в json при складывании в <u>draft_transaction</u> - 
   нужно подумать о более удобном формате)*
```
Для того чтобы различать данные из дата-файла и банковских выписок, в таблице _transaction_ 
следует создать поле `bank_id`: непустое значение разрешит обновление полей name и description
при совпадении по type, category_id, date, sum, account_id данными от соответствующего банка
в случае повторного вызова матчинга (не будет создаваться дубликат).
```
3. Прогнать [матчинг категорий](./matching/category_matching.md) для наполнения таблицы <u>category_matching</u> полями:
   * `bank_category_name`
   * `bank_id`
4. Прогнать матчинг валют для наполнения таблицы <u>currency_matching</u> полями:
   * `bank_currency_code`
   * `bank_id`
5. Прогнать [матчинг счетов](./matching/account_matching.md) для наполнения таблицы <u>account_matching</u> полями:
   * `bank_account_code`
   * `bank_id`
6. **_Вручную_** сматчить данные таблиц <u>category_matching</u>, <u>currency_matching</u> и 
<u>account_matching</u> с соответствующим id из таблиц <u>category</u>, <u>currency</u> и <u>account</u>. 
7. Выполнить [матчинг транзакций](./matching/transaction_matching.md)
8. Выполнить [матчинг переводов](./matching/transfer_matching.md)
9. **_Вручную_** провалидировать записи о переводах.
> В рамках целевого решения есть желание отказаться от такой валидации.
> На этапе разработки это необходимо для упрощения проверки стабильности матчинга.  
10. Процесс завершен. Далее следует сформировать файл формата FinancePM по данным из таблиц
    * currency
    * account
    * category
    * transaction
    * transfer
    * arrear
    * arrear_transaction