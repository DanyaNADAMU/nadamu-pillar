# Выпуск релизов и CI/CD автоматизация

В проекте **Pillars of Fortune** (`nadamu-pillar`) настроен полный цикл непрерывной интеграции (CI) и автоматического выпуска релизов (CD) с помощью **GitHub Actions**.

---

## 1. Архитектура CI/CD

В каталоге `.github/workflows/` настроены два основных рабочих процесса:

```
                      ┌─────────────────────────────────┐
                      │          GitHub Events          │
                      └────────────────┬────────────────┘
                                       │
            ┌──────────────────────────┴──────────────────────────┐
            ▼                                                     ▼
   Push / PR в `master`                                  Push тега `v*` или
            │                                            `workflow_dispatch`
            ▼                                                     │
┌─────────────────────────┐                                       ▼
│   CI (.github/ci.yml)   │                           ┌─────────────────────────┐
├─────────────────────────┤                           │ Release (.github/       │
│ • Checkout              │                           │          release.yml)   │
│ • Java 21 (Corretto)    │                           ├─────────────────────────┤
│ • ./gradlew check test  │                           │ • Checkout (full depth) │
│ • ./gradlew build       │                           │ • Определение версии    │
└─────────────────────────┘                           │ • Java 21 (Corretto)    │
                                                      │ • ./gradlew check test  │
                                                      │ • ./gradlew build       │
                                                      │   -Pversion=${VERSION}  │
                                                      │ • GitHub Release        │
                                                      │ • Загрузка .jar         │
                                                      └─────────────────────────┘
```

---

## 2. Динамическое версионирование в Gradle

В файле [`build.gradle.kts`](file:///workspace/nadamu-pillar/build.gradle.kts) реализовано динамическое разрешение версии:

```kotlin
group = "space.nadamu"
version = project.findProperty("pluginVersion")?.toString()
    ?: project.findProperty("version")?.toString()?.takeIf { it != "unspecified" }
    ?: "1.0-SNAPSHOT"
```

### Как это работает:
- **Локальная разработка**: если флаги не переданы, версия по умолчанию — `1.0-SNAPSHOT`.
- **Сборка релиза**: при передаче `-Pversion=1.0.0` (или `-PpluginVersion=1.0.0`) версия проекта, имя собранного файла (`NadamuPillar-1.0.0.jar`) и запись `version: 1.0.0` в сгенерированном `plugin.yml` получают точный номер релиза.

---

## 3. Как выпустить новый релиз

Существует два равноценных способа выпустить новую версию:

### Способ 1: Через Git-тег (Рекомендуемый)

1. Убедитесь, что все изменения закоммичены и отправлены в ветку `master`:
   ```bash
   git status
   git push origin master
   ```
2. Создайте аннотированный Git-тег с версией (префикс `v` обязателен):
   ```bash
   git tag -a v1.0.1 -m "Release v1.0.1: описание ключевых изменений"
   ```
3. Отправьте тег в удаленный репозиторий GitHub:
   ```bash
   git push origin v1.0.1
   ```
4. GitHub Actions автоматически:
   - Извлечёт имя тега `v1.0.1` и вычислит версию `1.0.1`.
   - Запустит верификацию (`check`, `test`).
   - Соберёт JAR плагина: `NadamuPillar-1.0.1.jar`.
   - Сгенерирует Release Notes на основе коммитов и PR.
   - Опубликует релиз на странице [Releases](https://github.com/DanyaNADAMU/nadamu-pillar/releases) и прикрепит `.jar` файл.

### Способ 2: Вручную через интерфейс GitHub Actions

1. Перейдите во вкладку **Actions** в репозитории: `https://github.com/DanyaNADAMU/nadamu-pillar/actions`.
2. В левом меню выберите workflow **Release**.
3. Нажмите кнопку **Run workflow**.
4. В поле `Tag name to release` введите версию, например `v1.0.1`.
5. Нажмите зелёную кнопку **Run workflow**. Пайплайн соберёт релиз и опубликует его.

---

## 4. Конфигурация рабочих процессов

### CI (`.github/workflows/ci.yml`)
- **Триггеры**: каждый push в ветку `master` и открытие/обновление Pull Request в `master`.
- **Окружение**: `ubuntu-latest`, Amazon Corretto Java 21, официальный экшен `gradle/actions/setup-gradle@v4` с кэшированием зависимостей.
- **Шаги**:
  ```bash
  ./gradlew check test build
  ```

### Release (`.github/workflows/release.yml`)
- **Триггеры**: push тегов `v*` или `workflow_dispatch`.
- **Права (Permissions)**: `contents: write` (для создания релизов и загрузки артефактов).
- **Экшен релиза**: `softprops/action-gh-release@v2`.
- **Прикрепляемые файлы**: `build/libs/*.jar`.
- **Release Notes**: генерируются автоматически (`generate_release_notes: true`).
