### Спецификация процесса обогащения данных FinancePM

1. Загрузить исходный data-файл FinancePM
   1. разложить файл в таблицы из миграции `init-finance-pm.xml`
   2. выполнить `UPDATE transfer SET validated = true;`  
2. Загрузить транзакции в формате Банка в виде XLS-файла для каждого Банка с перекладыванием контента в json формат
   в таблице <u>draft_transaction</u>.
```
Для того чтобы различать данные из дата-файла и банковских выписок, в таблице _transaction_ 
следует создать поле `bank_id`: непустое значение разрешит обновление полей name и description
при совпадении по type, category_id, date, sum, account_id данными от соответствующего банка
в случае повторного вызова матчинга (не будет создаваться дубликат).
```
3. Выполнить [матчинг категорий](./matching/category_matching.md)
4. Выполнить матчинг валют для наполнения таблицы <u>currency_matching</u> полями:
   * `bank_currency_code`
   * `bank_id`
5. Выполнить [матчинг счетов](./matching/account_matching.md)
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