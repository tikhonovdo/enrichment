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
5. Прогнать матчинг счетов для наполнения таблицы <u>account_matching</u> полями:
   * `bank_account_code`
   * `bank_id`
6. **_Вручную_** сматчить данные таблиц <u>category_matching</u>, <u>currency_matching</u> и 
<u>account_matching</u> с соответствующим id из таблиц <u>category</u>, <u>currency</u> и <u>account</u>. 
7. Для всех записей из таблицы <u>draft_transaction</u>, у которых
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

8. Выполнить  [матчинг переводов](./matching/transfer_matching.md)
9. **Вручную** провалидировать записи о переводах.
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