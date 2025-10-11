# Hibernate-Postgres-Service
Учебный проект, разработанный для понимания принципов работы Hibernate и его взаимодействия с БД (В данном случае с PostgreSQL)
## Необходимые компоненты
+ Java 11+
+ Maven 3.6+
+ PosgreSQL 12+
## Установка
``
mvn install
``

## Запуск
Перед запуском убедиться, что создана необходимая БД с логином и паролем, как в файле hibernate.cfg.xml. Либо прописать свои настройки

``
mvn exec:java -Dexec.mainClass="com.user.service.Main"
``