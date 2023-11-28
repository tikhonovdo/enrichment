### Спецификация процесса обогащения данных FinancePM

1. Загрузить исходный data-файл FinancePM
   1. разложить файл в таблицы из миграции `init-finance-pm.xml`
2. Загрузить транзакции от каждого Банка в виде XLS-файла 
   с перекладыванием контента в json формат в таблице <u>matching.draft_transaction</u>.
3. Выполнить [матчинг категорий](./matching/category_matching.md)
4. Выполнить матчинг валют для наполнения таблицы <u>matching.currency</u> полями:
   * `bank_currency_code`
   * `bank_id`
5. Выполнить [матчинг счетов](./matching/account_matching.md)
6. **_Вручную_** сматчить данные таблиц <u>matching.category</u>, <u>matching.currency</u> и 
<u>matching.account</u> с соответствующим id из таблиц <u>financepm.category</u>, <u>financepm.currency</u> и <u>financepm.account</u>. 
7. Выполнить [матчинг транзакций](./matching/transaction_matching.md)
8. Выполнить [матчинг переводов](./matching/transfer_matching.md)
9. **_Вручную_** провалидировать записи о переводах.
> В рамках целевого решения есть желание отказаться от такой валидации.
> На этапе разработки это необходимо для упрощения проверки стабильности матчинга.  
10. Процесс завершен. Далее следует сформировать файл формата FinancePM по данным из таблиц
    * financepm.currency
    * financepm.account
    * financepm.category
    * financepm.transaction
    * financepm.transfer
    * financepm.arrear
    * financepm.arrear_transaction