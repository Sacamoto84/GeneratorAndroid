# Глобальная шина событий и корневой снекбар

Дата: 2026-07-21
Статус: спека к реализации
Образец: репозиторий Sacamoto84/xvideos2 — `common/eventBus/{Event,EventBus}.kt`,
`common/snackbar/UiMessage.kt`, `screenRoot/{ScreenRoot,RootSnackbarHost}.kt`

## Проблема

Экран пресетов применяет пресет молча: `presetsVM.onClickPresetsRead()` заливает значения
в генератор, но пользователь не получает подтверждения и остаётся на том же экране. Кнопка
«Закрыть» в нижней панели пресетов подключена к пустому обработчику (`onClickBack = {}`).

Шире: в приложении нет единого способа показать пользователю результат действия. Там, где
сообщение всё же есть, используется `Toast` (сохранение скрипта), а слой `features` не может
сообщить об успехе, не таща за собой UI-зависимости.

## Решение

Шина событий уровня приложения плюс один снекбар в корне навигации. Любой слой шлёт событие,
корень его показывает — знание об UI не протекает в `features`.

### `common/eventbus/Event.kt`

```
sealed interface Event {
    data class ShowSnackBar(val message: UiMessage) : Event
    data class Log(val message: String) : Event
}
```

Список событий расширяемый: это точка, куда со временем переезжают уведомления о фоновых
операциях (импорт, сохранение, ошибки движка скриптов).

### `common/eventbus/EventBus.kt`

`object` с `MutableSharedFlow<Event>(replay = 0, extraBufferCapacity = 1024)` и своей
корутинной областью, чтобы `postEvent()` был обычным не-suspend вызовом и его можно было
звать откуда угодно, включая не-UI слои.

### `common/snackbar/UiMessage.kt`

```
sealed interface UiMessage { val text: String
    Info | Success | Error | Warning
}
```

Плюс `UiSnackbarVisuals : SnackbarVisuals`, который протаскивает тип сообщения до отрисовки,
и `SnackbarHostState.show(ui: UiMessage)`. Тип задаёт и длительность показа: ошибка висит
дольше остального.

### `common/snackbar/SnackBarEvent.kt`

Фасад `object SnackBar` с `info/success/error/warning`, чтобы вызов на месте выглядел как
`SnackBar.success("Пресет применён")`, а не как трёхэтажный
`EventBus.postEvent(Event.ShowSnackBar(UiMessage.Success(...)))`.

Там же `RenderSnackBarFilled(uiMsg)` — отрисовка одного снекбара: цвет фона и иконка по типу
сообщения (успех — зелёный с галкой, ошибка — красный, информация — синий, предупреждение —
оранжевый). Вынесена из хоста, поэтому её видно в `@Preview` без запуска приложения.

### `screens/root/RootSnackbarHost.kt`

Хост: достаёт тип сообщения из visuals и задаёт время показа — ошибка держится 5 секунд,
остальное 2 секунды. Рисует через `RenderSnackBarFilled`, своей копии оформления не имеет.

### `common/haptic/HapticKind.kt` и `HapticEvent.kt`

Виброотклик ходит через ту же шину: `Event.PerformHaptic(HapticKind)`.

`HapticKind` — свой enum на 13 значений (Confirm, Reject, ToggleOn, ToggleOff, LongPress,
TextHandleMove, ContextClick, KeyboardTap, VirtualKey, GestureEnd, GestureThresholdActivate,
SegmentTick, SegmentFrequentTick — весь набор `HapticFeedbackType` из compose-ui 1.11).
Свой, а не тип Compose, чтобы событие можно было послать из features, не таща туда UI.

Фасад `object Haptic` даёт по методу на вид: `Haptic.confirm()`, `Haptic.toggleOn()`,
`Haptic.segmentTick()`.

Перевод в `HapticFeedbackType` — `screens/root/HapticFeedbackMapping.kt`, то есть в UI-слое.

Снекбар сам сопровождается откликом по смыслу сообщения: успех — Confirm, ошибка — Reject,
остальное — ContextClick. Отдельно звать `Haptic.*` рядом со `SnackBar.*` не нужно.

Где отклик расставлен:

- Кнопки включения канала, CR, AM, FM — `Haptic.confirm()`. ToggleOn/ToggleOff на многих
  прошивках почти неразличимы на ощупь, Confirm ощущается чётче.
- Крутилки (`InfinitySlider`, `MainScreenTextBoxGuest`, а через него и
  `MainscreenTextBoxAndDropdownMenu`) — только GestureEnd по отпусканию. Щелчки во время
  вращения пробовали, но при частой отдаче отклик мешает: крутилка тут работает как
  бесконечный энкодер, шагов у неё нет.
- Регулятор громкости — SegmentTick на каждый процент, ровно как показывает индикатор.

Из-за пачек щелчков `EventBus.postEvent` сперва пробует `tryEmit` и заводит корутину только
если буфер переполнен.

### `screens/root/ScreenRoot.kt`

Корень UI: `Scaffold`, у которого `snackbarHost = RootSnackbarHost(...)`, а в контенте живёт
`Navigator(AppScreen.Home)`. Подписка на `EventBus.events` заводится один раз в
`LaunchedEffect(Unit)` и разбирает событие по типу: снекбар с откликом, отдельный
виброотклик, лог.

В отличие от образца, где `ScreenRoot` — это `Screen` (там ему нужен свой `ScreenModel`),
здесь достаточно `@Composable`: корню нечего хранить. `MainActivity` вместо `Navigator(...)`
вызывает `ScreenRoot()`.

## Исправление экрана пресетов

- `presetsVM.onClickPresetsRead()` после применения шлёт
  `Event.ShowSnackBar(UiMessage.Success("Пресет «имя» применён"))`.
- Клик по пресету закрывает экран — `navigator.pop()`; выбор сделан, дальше держать список
  незачем.
- Кнопка «Закрыть» получает тот же `pop()` вместо пустого обработчика.
- Удаление, переименование и создание пресета тоже отвечают снекбаром: иначе останется
  ровно та же немота, только в соседних диалогах.

## Проверка

- `:app:compileDebugKotlin` и существующие тесты — зелёные.
- Прогон на эмуляторе: применение пресета показывает зелёный снекбар и закрывает экран;
  кнопка «Закрыть» работает; удаление и переименование дают своё сообщение.
