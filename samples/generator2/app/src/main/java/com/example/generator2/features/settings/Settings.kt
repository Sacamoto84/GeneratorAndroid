package com.example.generator2.features.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.example.generator2.AppPath
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ### Настройки приложения
 *
 * Один файл `config.json` во внутреннем хранилище приложения, вся схема — [AppConfig].
 *
 * - запись атомарная, обрыв процесса не рвёт настройки;
 * - чтение и запись не блокируют UI-поток;
 * - [data] — поток, изменение настройки само доезжает до всех подписчиков;
 * - битый файл заменяется дефолтом, а не крашит старт;
 * - старые Satchel-файлы переносятся один раз, см. [SatchelMigration].
 */
@Singleton
class Settings @Inject constructor(
    @ApplicationContext context: Context,
    appPath: AppPath,
) {

    private val store: DataStore<AppConfig> = DataStoreFactory.create(
        serializer = AppConfigSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler { AppConfig() },
        migrations = listOf(SatchelMigration(appPath.config)),
        produceFile = { context.dataStoreFile(FILE_NAME) }
    )

    /**
     * Поток настроек. Первое значение приходит после чтения файла (и миграции,
     * если она нужна), дальше — на каждое изменение.
     */
    val data: Flow<AppConfig> = store.data

    /**
     * Текущие настройки без подписки.
     */
    suspend fun get(): AppConfig = data.first()

    /**
     * Изменение настройки: `settings.update { it.copy(language = "en") }`.
     * Запись сериализуется относительно других вызовов [update] и завершается
     * до возврата из функции.
     */
    suspend fun update(block: (AppConfig) -> AppConfig) {
        store.updateData(block)
    }

    /**
     * Синхронное чтение с блокировкой потока.
     *
     * Одна законная точка вызова — SplashScreen: язык нужен до первого кадра.
     * Везде остальное — [data] или [get].
     */
    fun blockingGet(): AppConfig = runBlocking { get() }
}
