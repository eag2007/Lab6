# Лабораторная №6 - Управление коллекцией маршрутов через сетевое приложение

## Описание
Клиент-серверное приложение для управления коллекцией объектов `Route`. Реализовано с использованием Java NIO, сериализации и Stream API.

## Стек технологий
- Java 17
- Gradle
- Java NIO (Selectors, SocketChannel)
- Сериализация объектов
- GZIP сжатие данных
- JLine для автодополнения команд

## Архитектура

### Клиент
```
client/
├── commands/          # Реализация команд
├── managers/          # Менеджеры (ввод/вывод, валидация, парсинг)
├── modules/           # Модули чтения/записи (ReadModule, WriteModule)
└── Client.java        # Точка входа
```

### Сервер
```
server/
├── commands/          # Обработчики команд
├── managers/          # Менеджеры (коллекции, парсинг, CSV)
├── modules/           # Модули чтения/записи (ReadModule, WriteModule)
└── Server.java        # Точка входа
```

### Общие пакеты
```
packet/
├── collection/        # Классы (Route, Coordinates, Location)
├── CommandPacket.java # Пакет команды
└── ResponsePacket.java # Пакет ответа
```

## Команды клиента
- `add` - добавить элемент
- `add_if_max` - добавить если больше максимального
- `average_of_distance` - среднее значение distance
- `clear` - очистить коллекцию
- `execute_script file_name` - выполнить скрипт
- `exit` - выход
- `filter_less_than_distance distance` - фильтр по distance
- `help` - справка
- `history` - последние 14 команд
- `info` - информация о коллекции
- `remove_all_by_distance distance` - удалить по distance
- `remove_by_id id` - удалить по id
- `remove_first` - удалить первый
- `show` - показать все
- `update id` - обновить элемент

## Команды сервера (консоль)
- `save` - сохранить коллекцию в CSV
- `exit` - завершить работу
- `help` - справка

## Особенности
- **Сжатие GZIP** для больших ответов (show)
- **Автосохранение** при завершении сервера
- **Цветной вывод** в консоли
- **Автодополнение** команд (Tab)
- **Stream API** для обработки коллекций
- **Многопоточность** (отдельный поток для команд сервера)

## Запуск

### Сервер
```bash
export PATHTOCOLLECTION="/path/to/file.csv"
gradle run --args="server"
```

### Клиент
```bash
java -jar client.jar
```

### Сервер
```bash
java -jar server.jar
```