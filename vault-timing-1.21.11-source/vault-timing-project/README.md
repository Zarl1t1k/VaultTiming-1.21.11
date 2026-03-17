# Vault Timing — сборка для Minecraft 1.21.11

## Что нужно установить

- **JDK 21**: https://adoptium.net/temurin/releases/?version=21
- **Gradle** (не обязательно — скачается автоматически через wrapper)

## Как собрать

### Шаг 1 — Получи gradle-wrapper.jar

Файл `gradle/wrapper/gradle-wrapper.jar` нельзя распространять в архиве.
Скачай его вручную одной командой в папке проекта:

**Windows (PowerShell):**
```powershell
Invoke-WebRequest -Uri "https://github.com/gradle/gradle/raw/v8.8.0/gradle/wrapper/gradle-wrapper.jar" -OutFile "gradle\wrapper\gradle-wrapper.jar"
```

**Linux / macOS:**
```bash
curl -L "https://github.com/gradle/gradle/raw/v8.8.0/gradle/wrapper/gradle-wrapper.jar" -o gradle/wrapper/gradle-wrapper.jar
```

### Шаг 2 — Собери мод

**Windows:**
```cmd
gradlew.bat build
```

**Linux / macOS:**
```bash
chmod +x gradlew
./gradlew build
```

### Шаг 3 — Готовый мод

После успешной сборки файл будет по пути:
```
build/libs/vault-timing-2.0.2.jar
```

Скопируй его в папку `.minecraft/mods/`

## Что было исправлено

В Minecraft 1.21.11 метод `tryUnlock` у `VaultBlockEntity.Server` получил
новый параметр `Hand hand`. Из-за этого старый мод крашился с ошибкой:

```
NoSuchMethodError: method_56757(... PlayerEntity, ItemStack)
```

В этой версии мода сигнатура метода обновлена до:

```java
tryUnlock(ServerWorld, BlockPos, BlockState, VaultConfig,
          VaultServerData, VaultSharedData, PlayerEntity, ItemStack, Hand)
```
