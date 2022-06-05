# Vezdekod 2022 backend
Нужна Java 11 (или выше, но тестировалось на 11)
## Сборка
``./gradlew buildAll``
В корневой папке репозитория появятся файлы:
- `shard.jar` - Сервер-голосовалка (ответ на задания за 10, 20, 30 баллов)
- `tester.jar` - CLI-инструмент для нагрузочного тестирования (задание за 40 баллов)
- `balancer.jar` - Шлюз-балансировщик (задания за 50 баллов)
## Запуск
### Запуск голосовалки (шарда)

``java -jar shard.jar --server.port=<port> --vk22.artists=<artists> --vk22.requestsPerPeriod=<rps> --vk22.ratePeriod=<period>``
- port - порт, на котором зпустится сервер
- artists - список исполнителей через запятую (у всех шардов должен быть одинаковым)
- rps - Максимум запросов на голосование за период
- period - Период сброса ограничения на количество запросов (в секундах)

Пример: ``java -jar shard.jar --server.port=8080 --vk22.artists='pf,ap' --vk22.requestsPerPeriod=7 --vk22.ratePeriod=60``

Шард может работать самомтоятельно, без балансировщика

### Запуск тестера
`java -jar tester.jar -n <requests> -c <clients> -a <artists> <address>`
- requests - Суммарное количество запросов
- clients - количество параллельно работающих клиентов
- address - адрес серврера голосовалки или лоад балансера

Пример: `java -jar tester.jar -n 5000 -c 50 -a pf,ap http://127.0.0.1:8080`


### Запуск балансировщика нагрузки
``java -jar balancer.jar --vk22.balancer.shards=<shards> --server.port=<port>``
- port - порт, на котором зпустится сервер
- shards - адреса шардов через запятую


Пример: ``java -jar balancer.jar --vk22.balancer.shards=http://127.0.0.1:8081,http://127.0.0.1:8083 --server.port=9000``

### Запущенный проект
3 шарда + балансировщи запущены на [http://217.71.129.139:4966/](http://217.71.129.139:4966/)