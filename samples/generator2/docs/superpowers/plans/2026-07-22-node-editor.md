# Редактор графа нод — план реализации

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Экран, где сценарий управления генератором набирается нодами, компилируется в строки существующего языка скриптов и исполняется существующим движком `Script`.

**Architecture:** Граф — чистая модель данных (`NodeGraph`). Валидатор и компилятор — чистые функции без Android, поэтому тестируются обычным JUnit. Компилятор выдаёт список строк плюс карту «строка → нода»; `NodeRunner` грузит строки в собственный экземпляр `Script` и по `script.pc` через карту подсвечивает активную ноду. Интерфейс на Compose: холст с трансформацией, карточки нод фиксированного размера, связи создаются в два тапа.

**Tech Stack:** Kotlin, Jetpack Compose (Material 3), Voyager (навигация + ScreenModel), Hilt, Gson, kotlinx.coroutines, JUnit 4.

**Спека:** `docs/superpowers/specs/2026-07-22-node-editor-design.md`

---

## Структура файлов

**Новое — ядро (без Android, тестируется JUnit):**

| Файл | Ответственность |
|---|---|
| `features/nodes/model/NodeGraph.kt` | `NodeId`, `NodeGraph`, `GraphNode`, `NodeBody`, `Port`, `GraphEdge`, `RegOp`, `StepParams`, `ChannelParams` + операции над графом |
| `features/nodes/model/GraphDto.kt` | DTO под Gson и мапперы в домен и обратно |
| `features/nodes/NodeGraphValidator.kt` | `Issue`, `Severity`, `validate()` |
| `features/nodes/NodeGraphCompiler.kt` | `CompileResult`, `compile()`, эмиссия строк |
| `features/nodes/NodeGraphUtils.kt` | файлы `.ng`: список, сохранить, прочитать, удалить, переименовать |

**Новое — рантайм и DI:**

| Файл | Ответственность |
|---|---|
| `features/nodes/GeneratorArbiter.kt` | кто владеет генератором: `NONE`, `SCRIPT`, `NODES` |
| `features/nodes/NodeRunner.kt` | свой `Script`, пуск/пауза/стоп, `pc → NodeId` |
| `features/nodes/NodesModule.kt` | Hilt-провайдеры |

**Новое — интерфейс:**

| Файл | Ответственность |
|---|---|
| `screens/nodes/ScreenNodes.kt` | каркас: топбар, холст, панель, шторка, диалоги |
| `screens/nodes/vm/VMNodes.kt` | граф в памяти, выделение, режим связи, файловые операции |
| `screens/nodes/canvas/NodeCanvas.kt` | слой трансформации, размещение карточек, жесты |
| `screens/nodes/canvas/NodeCard.kt` | карточка одной ноды |
| `screens/nodes/canvas/EdgeLayer.kt` | кривые Безье под карточками |
| `screens/nodes/top/NodesTopBar.kt` | имя, START/PAUSE/STOP, меню файлов, `{ }` |
| `screens/nodes/bottom/NodeActionBar.kt` | панель действий выделенного |
| `screens/nodes/bottom/RunSheet.kt` | шторка «регистры + консоль» |
| `screens/nodes/dialog/StepDialog.kt` | форма 22 полей |
| `screens/nodes/dialog/RegisterDialog.kt` | `LOAD`/`PLUS`/`MINUS`, регистр, операнд |
| `screens/nodes/dialog/ConditionDialog.kt` | регистр, сравнение, операнд |
| `screens/nodes/dialog/NodePickerSheet.kt` | выбор типа ноды при «+» |
| `screens/nodes/dialog/DialogOpenGraph.kt` | список сохранённых графов |
| `screens/nodes/bottom/IssuesSheet.kt` | список ошибок и предупреждений |
| `screens/nodes/dialog/OperandField.kt` | поле значения с чипом `123` ⇄ `F` |
| `screens/nodes/ui/GeneratedScriptView.kt` | текст скрипта только для чтения, полноэкранный диалог |

**Правки существующего:**

| Файл | Правка |
|---|---|
| `features/script/ScriptCommand.kt` | наружу выносятся `looksLikeRegister`, `registerIndexOrNull`, `parseOperand`, `Operand.toToken()` |
| `AppPath.kt` | папка `Nodes` |
| `Navigation.kt` | экран `Nodes` |
| `screens/mainscreen4/bottom/BottomAppBarComponent.kt` | кнопка входа |
| `screens/scripting/dialog/DialogSaveAs.kt`, `DialogDeleteRename.kt` | переезд в `screens/common/dialog/` |
| `screens/scripting/vm/vmscripting.kt` | `bStartClick()` с захватом арбитра |
| `screens/scripting/bottom/BottomAppBarScript.kt:67` | вызов `vm.bStartClick()` |
| `di/ScreenModelModule.kt` | регистрация `VMNodes` |

---

## Порядок работы

Задача 1 — спайк: проверяет единственное место, где дизайн полагается на поведение Compose, а не на наш код. Если он не подтвердится, меняется весь подход к холсту, поэтому он идёт до всего остального.

Задачи 2–12 — ядро и рантайм, всё покрыто автотестами. Задачи 13–24 — интерфейс, проверяется руками.

---

### Task 1: Спайк — hit-testing внутри `graphicsLayer`

Проверяем: если положить кликабельные composable внутрь слоя с `graphicsLayer(scaleX, scaleY, translationX, translationY)`, доходят ли до них касания по правильным координатам после зума и панорамы.

**Files:**
- Create: `app/src/main/java/com/example/generator2/screens/nodes/SpikeCanvas.kt`

- [ ] **Step 1: Написать спайк-экран**

```kotlin
package com.example.generator2.screens.nodes

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Одноразовый спайк: проверяем, что касания доходят до карточек внутри
 * трансформированного слоя после зума и панорамы. Удаляется в Task 15.
 */
@Composable
fun SpikeCanvas() {

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var lastTapped by remember { mutableStateOf("нет") }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF202020))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.4f, 2.5f)
                    offset += pan
                }
            }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                )
        ) {
            listOf("A" to 40, "B" to 160, "C" to 280).forEach { (name, top) ->
                Box(
                    Modifier
                        .offset { IntOffset(60, top) }
                        .size(168.dp, 72.dp)
                        .background(Color(0xFF3A7BD5))
                        .clickable { lastTapped = name }
                ) {
                    Text(name, color = Color.White)
                }
            }
        }

        Text(
            "нажата: $lastTapped   зум: ${(scale * 100).roundToInt()}%",
            color = Color.White,
        )
    }
}
```

- [ ] **Step 2: Показать спайк вместо главного экрана**

В `screens/root/ScreenRoot.kt` временно заменить `Navigator(AppScreen.Home)` на `SpikeCanvas()`.

- [ ] **Step 3: Собрать и проверить руками**

Run: `./gradlew :app:installDebug`

Проверить на устройстве:
1. Тап по карточке — подпись «нажата» меняется на её букву.
2. Отдалить щипком до ~50%, тапнуть по каждой карточке — подпись меняется правильно, промахов нет.
3. Приблизить до ~200%, сдвинуть холст пальцем, тапнуть — подпись меняется правильно.

**Если все три пункта прошли** — дизайн подтверждён, идём дальше.

**Если касания приходят не в ту карточку или не приходят вовсе** — остановиться и сообщить. Запасной план из спеки: рисовать всё одним `Canvas` и делать hit-test вручную обратной трансформацией; это меняет задачи 15–17, и план надо переписать до их начала.

- [ ] **Step 4: Вернуть `ScreenRoot` как было**

В `ScreenRoot.kt` вернуть `Navigator(AppScreen.Home)`. Файл `SpikeCanvas.kt` пока оставить — он удаляется в Task 15.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/generator2/screens/nodes/SpikeCanvas.kt
git commit -m "chore(nodes): спайк холста — проверка касаний внутри graphicsLayer"
```

---

### Task 2: Вынести разбор операнда из `parseCommand`

Формат `.ng` хранит операнд той же строкой, что уходит в скрипт (`"1000.0"` или `"F1"`). Чтобы файл и язык скрипта не разъехались, разбор должен быть общим.

**Files:**
- Modify: `app/src/main/java/com/example/generator2/features/script/ScriptCommand.kt:112-141`
- Test: `app/src/test/java/com/example/generator2/features/script/ScriptCommandTest.kt`

- [ ] **Step 1: Написать падающие тесты**

Дописать в конец `ScriptCommandTest.kt`, перед закрывающей скобкой класса:

```kotlin
    //╭─ Операнд наружу ──────────────────────────────────────────────────────╮

    @Test
    fun `parseOperand читает регистр и константу`() {
        assertEquals(Operand.Reg(1), parseOperand("F1"))
        assertEquals(Operand.Reg(1), parseOperand("R1"))
        assertEquals(Operand.Const(50f), parseOperand("50"))
        assertEquals(Operand.Const(1000f), parseOperand("1000.0"))
    }

    @Test
    fun `parseOperand возвращает null на мусоре`() {
        assertNull(parseOperand("Fl"))
        assertNull(parseOperand("хрень"))
        assertNull(parseOperand(""))
    }

    @Test
    fun `parseOperand не берёт регистр вне диапазона`() {
        assertNull(parseOperand("F10"))
        assertNull(parseOperand("F99"))
    }

    @Test
    fun `toToken и parseOperand обратны друг другу`() {
        listOf(Operand.Reg(0), Operand.Reg(9), Operand.Const(0f), Operand.Const(1234.5f))
            .forEach { assertEquals(it, parseOperand(it.toToken())) }
    }

    @Test
    fun `регистр вне диапазона по-прежнему ошибка разбора команды`() {
        val e = assertThrows(ScriptException::class.java) { parseCommand("LOAD F10 5", 7) }
        assertEquals(7, e.line)
    }
```

Добавить импорты в начало файла тестов:

```kotlin
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
```

- [ ] **Step 2: Запустить и убедиться, что падает**

Run: `./gradlew :app:testDebugUnitTest --tests "*ScriptCommandTest*"`
Expected: FAIL — `Unresolved reference: parseOperand`, `Unresolved reference: toToken`

- [ ] **Step 3: Добавить публичные функции**

В `ScriptCommand.kt` после объявления `class ScriptException` (строка 18) вставить:

```kotlin
/**
 * Токен похож на регистр: префикс F или R, дальше целое число.
 * Диапазон не проверяется — этим занимается вызывающий.
 */
fun looksLikeRegister(token: String): Boolean {
    val prefix = token.firstOrNull() ?: return false
    if (prefix != 'F' && prefix != 'R') return false
    return token.drop(1).toIntOrNull() != null
}

/**
 * Индекс регистра из токена F1 или R1.
 * null — токен не регистр либо номер вне F0..F[REGISTER_COUNT]-1.
 */
fun registerIndexOrNull(token: String): Int? {
    if (!looksLikeRegister(token)) return null
    val index = token.drop(1).toIntOrNull() ?: return null
    return if (index in 0 until REGISTER_COUNT) index else null
}

/**
 * Операнд из токена: "F1" -> Reg(1), "50" -> Const(50f).
 * null, если токен ни регистр, ни число.
 */
fun parseOperand(token: String): Operand? {
    registerIndexOrNull(token)?.let { return Operand.Reg(it) }
    return token.toFloatOrNull()?.let { Operand.Const(it) }
}

/**
 * Обратно в токен скрипта. Const(50f) -> "50.0", Reg(1) -> "F1".
 */
fun Operand.toToken(): String = when (this) {
    is Operand.Const -> value.toString()
    is Operand.Reg -> "F$index"
}
```

- [ ] **Step 4: Переписать локальные хелперы через них**

В `parseCommand` заменить локальную `registerIndex` (строки 124–132) на:

```kotlin
    //F1 R1 -> индекс регистра, иначе null
    fun registerIndex(token: String): Int? {
        if (!looksLikeRegister(token)) return null
        return registerIndexOrNull(token)
            ?: fail("регистр $token вне диапазона F0..F${REGISTER_COUNT - 1}")
    }
```

и локальную `operand` (строки 137–141) на:

```kotlin
    fun operand(token: String): Operand =
        parseOperand(token) ?: fail("не число и не регистр: $token")
```

- [ ] **Step 5: Запустить тесты**

Run: `./gradlew :app:testDebugUnitTest --tests "*ScriptCommandTest*"`
Expected: PASS, все тесты включая старые

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/generator2/features/script/ScriptCommand.kt app/src/test/java/com/example/generator2/features/script/ScriptCommandTest.kt
git commit -m "refactor(script): вынести разбор операнда из parseCommand наружу"
```

---

### Task 3: Модель графа

**Files:**
- Create: `app/src/main/java/com/example/generator2/features/nodes/model/NodeGraph.kt`
- Test: `app/src/test/java/com/example/generator2/features/nodes/NodeGraphTest.kt`

- [ ] **Step 1: Написать падающий тест**

```kotlin
package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.*
import com.example.generator2.features.script.Operand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NodeGraphTest {

    @Test
    fun `новый граф это Старт со стрелкой в Стоп`() {
        val g = newGraph()
        assertEquals(2, g.nodes.size)
        assertTrue(g.startNode()!!.body is NodeBody.Start)
        val stop = g.target(g.startNode()!!.id, Port.OUT)
        assertTrue(g.node(stop!!)!!.body is NodeBody.Stop)
    }

    @Test
    fun `следующий id больше максимального`() {
        assertEquals(NodeId(3), newGraph().nextId())
    }

    @Test
    fun `id не переиспользуется после удаления`() {
        val g = newGraph()
        val afterDelete = g.withoutNode(NodeId(2))
        assertEquals(NodeId(3), afterDelete.nextId())
    }

    @Test
    fun `новая связь из занятого порта заменяет старую`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, emptyStep()))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))

        assertEquals(1, g.edges.count { it.from == NodeId(1) && it.port == Port.OUT })
        assertEquals(NodeId(3), g.target(NodeId(1), Port.OUT))
    }

    @Test
    fun `удаление ноды уносит все её рёбра`() {
        val g = newGraph().withoutNode(NodeId(2))
        assertTrue(g.edges.isEmpty())
        assertNull(g.target(NodeId(1), Port.OUT))
    }

    @Test
    fun `порты зависят от типа ноды`() {
        assertEquals(listOf(Port.OUT), NodeBody.Start.ports())
        assertEquals(listOf(Port.OUT), emptyStep().ports())
        assertEquals(listOf(Port.YES, Port.NO), condition().ports())
        assertEquals(emptyList<Port>(), NodeBody.Stop.ports())
    }

    private fun emptyStep() = NodeBody.Step(StepParams(ChannelParams(), ChannelParams()), 0L)

    private fun condition() =
        NodeBody.Condition(1, com.example.generator2.features.script.CompareOp.LESS, Operand.Const(5f))
}
```

- [ ] **Step 2: Запустить и убедиться, что падает**

Run: `./gradlew :app:testDebugUnitTest --tests "*NodeGraphTest*"`
Expected: FAIL — `Unresolved reference: model`

- [ ] **Step 3: Написать модель**

```kotlin
package com.example.generator2.features.nodes.model

import com.example.generator2.features.script.CompareOp
import com.example.generator2.features.script.Operand

/** Идентификатор ноды. Не переиспользуется: новый всегда больше всех бывших. */
@JvmInline
value class NodeId(val value: Int)

/** Выход ноды. У Старта, Шага и Регистра один, у Условия два, у Стопа ни одного. */
enum class Port { OUT, YES, NO }

/** Операция ноды Регистр. Ложится на LOAD, PLUS, MINUS языка скрипта. */
enum class RegOp { LOAD, PLUS, MINUS }

data class GraphEdge(val from: NodeId, val port: Port, val to: NodeId)

/**
 * Параметры одного канала. null означает снятую галочку: поле не трогаем.
 *
 * carrierEnabled ложится на "CH1 CR ON|OFF" — в движке это включение
 * всего канала (gen.liveData.ch1_EN), а не только несущей.
 */
data class ChannelParams(
    val carrierEnabled: Boolean? = null,
    val carrierFr: Operand? = null,
    val carrierMod: String? = null,
    val amEnabled: Boolean? = null,
    val amFr: Operand? = null,
    val amMod: String? = null,
    val fmEnabled: Boolean? = null,
    val fmBase: Operand? = null,
    val fmDev: Operand? = null,
    val fmFr: Operand? = null,
    val fmMod: String? = null,
) {
    /** Сколько галочек отмечено — для счётчика в заголовке группы */
    val checkedCount: Int
        get() = listOf(
            carrierEnabled, carrierFr, carrierMod,
            amEnabled, amFr, amMod,
            fmEnabled, fmBase, fmDev, fmFr, fmMod,
        ).count { it != null }
}

data class StepParams(val ch1: ChannelParams, val ch2: ChannelParams) {
    val checkedCount: Int get() = ch1.checkedCount + ch2.checkedCount
}

sealed interface NodeBody {
    data object Start : NodeBody
    data object Stop : NodeBody
    data class Step(val params: StepParams, val delayMs: Long) : NodeBody
    data class Register(val op: RegOp, val dst: Int, val src: Operand) : NodeBody
    data class Condition(val left: Int, val op: CompareOp, val right: Operand) : NodeBody
}

/** Выходы ноды в порядке обхода компилятором */
fun NodeBody.ports(): List<Port> = when (this) {
    is NodeBody.Start -> listOf(Port.OUT)
    is NodeBody.Step -> listOf(Port.OUT)
    is NodeBody.Register -> listOf(Port.OUT)
    is NodeBody.Condition -> listOf(Port.YES, Port.NO)
    is NodeBody.Stop -> emptyList()
}

data class GraphNode(
    val id: NodeId,
    val title: String,
    val x: Float,
    val y: Float,
    val body: NodeBody,
)

data class NodeGraph(
    val nodes: List<GraphNode> = emptyList(),
    val edges: List<GraphEdge> = emptyList(),
    /**
     * Максимальный когда-либо выданный id. Удаление ноды его не опускает,
     * поэтому освободившийся номер второй раз не выдаётся и новая нода
     * не наследует связи мёртвой.
     */
    val lastId: Int = nodes.maxOfOrNull { it.id.value } ?: 0,
)

//╭─ Чтение ──────────────────────────────────────────────────────────────╮

fun NodeGraph.node(id: NodeId): GraphNode? = nodes.firstOrNull { it.id == id }

fun NodeGraph.startNode(): GraphNode? = nodes.firstOrNull { it.body is NodeBody.Start }

fun NodeGraph.target(from: NodeId, port: Port): NodeId? =
    edges.firstOrNull { it.from == from && it.port == port }?.to

/**
 * Следующий свободный id. Берётся от счётчика, а не от текущего списка нод:
 * иначе удаление ноды с максимальным номером тут же освобождало бы его,
 * и новая нода наследовала бы связи мёртвой.
 */
fun NodeGraph.nextId(): NodeId = NodeId(lastId + 1)

//╭─ Правка ──────────────────────────────────────────────────────────────╮

/**
 * Вставка или правка ноды. Счётчик берёт максимум, а не увеличивается:
 * правка существующей ноды (сдвиг, смена параметров) не должна его двигать.
 */
fun NodeGraph.withNode(node: GraphNode): NodeGraph = copy(
    nodes = nodes.filterNot { it.id == node.id } + node,
    lastId = maxOf(lastId, node.id.value),
)

/** Один порт — одна связь: новая молча заменяет прежнюю */
fun NodeGraph.withEdge(from: NodeId, port: Port, to: NodeId): NodeGraph =
    copy(edges = edges.filterNot { it.from == from && it.port == port } + GraphEdge(from, port, to))

fun NodeGraph.withoutEdge(from: NodeId, port: Port): NodeGraph =
    copy(edges = edges.filterNot { it.from == from && it.port == port })

/** Удаление ноды уносит и входящие, и исходящие рёбра */
fun NodeGraph.withoutNode(id: NodeId): NodeGraph = copy(
    nodes = nodes.filterNot { it.id == id },
    edges = edges.filterNot { it.from == id || it.to == id },
)

/** Новый граф: Старт со стрелкой в Стоп — сразу валиден, можно жать «Пуск» */
fun newGraph(): NodeGraph = NodeGraph(
    nodes = listOf(
        GraphNode(NodeId(1), "Старт", 60f, 80f, NodeBody.Start),
        GraphNode(NodeId(2), "Стоп", 60f, 300f, NodeBody.Stop),
    ),
    edges = listOf(GraphEdge(NodeId(1), Port.OUT, NodeId(2))),
)
```

- [ ] **Step 4: Запустить тесты**

Run: `./gradlew :app:testDebugUnitTest --tests "*NodeGraphTest*"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/generator2/features/nodes/model/NodeGraph.kt app/src/test/java/com/example/generator2/features/nodes/NodeGraphTest.kt
git commit -m "feat(nodes): модель графа и операции над ней"
```

---

### Task 4: Формат `.ng` — DTO и мапперы

Gson не умеет sealed-классы и `value class`, а ещё создаёт объекты в обход конструктора, поэтому значения по умолчанию не применяются. Отсюда правило: **все поля DTO nullable**, а недостающее обязательное поле превращается в понятную ошибку в маппере.

**Files:**
- Create: `app/src/main/java/com/example/generator2/features/nodes/model/GraphDto.kt`
- Test: `app/src/test/java/com/example/generator2/features/nodes/GraphDtoTest.kt`

- [ ] **Step 1: Написать падающий тест**

```kotlin
package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.*
import com.example.generator2.features.script.CompareOp
import com.example.generator2.features.script.Operand
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class GraphDtoTest {

    private val gson = Gson()

    private val sample = NodeGraph(
        nodes = listOf(
            GraphNode(NodeId(1), "Старт", 40f, 120f, NodeBody.Start),
            GraphNode(
                NodeId(2), "Разгон", 220f, 100f,
                NodeBody.Step(
                    StepParams(
                        ch1 = ChannelParams(
                            carrierEnabled = true,
                            carrierFr = Operand.Reg(1),
                            amMod = "02_HWave",
                        ),
                        ch2 = ChannelParams(),
                    ),
                    delayMs = 100L,
                ),
            ),
            GraphNode(
                NodeId(3), "шаг +50", 220f, 240f,
                NodeBody.Register(RegOp.PLUS, 1, Operand.Const(50f)),
            ),
            GraphNode(
                NodeId(4), "до 5 кГц", 220f, 350f,
                NodeBody.Condition(1, CompareOp.LESS, Operand.Const(5000f)),
            ),
            GraphNode(NodeId(5), "Стоп", 40f, 460f, NodeBody.Stop),
        ),
        edges = listOf(
            GraphEdge(NodeId(1), Port.OUT, NodeId(2)),
            GraphEdge(NodeId(2), Port.OUT, NodeId(3)),
            GraphEdge(NodeId(3), Port.OUT, NodeId(4)),
            GraphEdge(NodeId(4), Port.YES, NodeId(2)),
            GraphEdge(NodeId(4), Port.NO, NodeId(5)),
        ),
    )

    @Test
    fun `граф переживает поездку через json`() {
        val json = gson.toJson(sample.toDto())
        val back = gson.fromJson(json, GraphDto::class.java).toDomain()
        assertEquals(sample, back)
    }

    @Test
    fun `счётчик id переживает поездку даже когда больше максимального номера`() {
        //После удаления ноды lastId выше любого живого id — он обязан
        //сохраниться, иначе удалённый номер выдастся заново после перезагрузки
        val afterDelete = sample.withoutNode(NodeId(5))
        val back = gson.fromJson(gson.toJson(afterDelete.toDto()), GraphDto::class.java).toDomain()
        assertEquals(5, back.lastId)
        assertEquals(NodeId(6), back.nextId())
    }

    @Test
    fun `снятая галочка не попадает в json`() {
        val json = gson.toJson(sample.toDto())
        assertFalse(json.contains("fmDev"))
    }

    @Test
    fun `отсутствующий ключ читается как снятая галочка`() {
        val json = "{\"version\":1," +
            "\"nodes\":[{\"id\":1,\"type\":\"STEP\",\"title\":\"x\",\"x\":0,\"y\":0,\"delayMs\":0}]," +
            "\"edges\":[]}"
        val node = gson.fromJson(json, GraphDto::class.java).toDomain().node(NodeId(1))!!
        val step = node.body as NodeBody.Step
        assertNull(step.params.ch1.carrierFr)
        assertEquals(0, step.params.checkedCount)
    }

    @Test
    fun `неизвестный тип ноды это ошибка формата`() {
        val json = "{\"version\":1,\"nodes\":[{\"id\":1,\"type\":\"ТАНЕЦ\",\"x\":0,\"y\":0}],\"edges\":[]}"
        assertThrows(GraphFormatException::class.java) {
            gson.fromJson(json, GraphDto::class.java).toDomain()
        }
    }

    @Test
    fun `версия новее нашей это ошибка формата`() {
        val json = "{\"version\":99,\"nodes\":[],\"edges\":[]}"
        assertThrows(GraphFormatException::class.java) {
            gson.fromJson(json, GraphDto::class.java).toDomain()
        }
    }

    @Test
    fun `нечитаемый операнд это ошибка формата`() {
        val json = "{\"version\":1,\"nodes\":[{\"id\":1,\"type\":\"REGISTER\",\"x\":0,\"y\":0," +
            "\"regOp\":\"PLUS\",\"regDst\":1,\"regSrc\":\"хрень\"}],\"edges\":[]}"
        assertThrows(GraphFormatException::class.java) {
            gson.fromJson(json, GraphDto::class.java).toDomain()
        }
    }
}
```

- [ ] **Step 2: Запустить и убедиться, что падает**

Run: `./gradlew :app:testDebugUnitTest --tests "*GraphDtoTest*"`
Expected: FAIL — `Unresolved reference: GraphDto`

- [ ] **Step 3: Написать DTO и мапперы**

```kotlin
package com.example.generator2.features.nodes.model

import com.example.generator2.features.script.CompareOp
import com.example.generator2.features.script.Operand
import com.example.generator2.features.script.parseOperand
import com.example.generator2.features.script.toToken

/** Файл .ng не прочитан: не тот json, неизвестный тип, битый операнд, чужая версия */
class GraphFormatException(message: String) : Exception(message)

const val GRAPH_FORMAT_VERSION = 1

/**
 * Представление графа на диске.
 *
 * Все поля nullable намеренно: Gson создаёт объект в обход конструктора,
 * значения по умолчанию не срабатывают, и любое отсутствующее поле пришло бы
 * нулём или null мимо системы типов Kotlin. Проверки живут в мапперах.
 *
 * Операнд хранится тем же токеном, что уходит в скрипт: "1000.0" или "F1".
 */
data class GraphDto(
    val version: Int? = null,
    val nodes: List<NodeDto>? = null,
    val edges: List<EdgeDto>? = null,
    /** Счётчик выданных id. Нет в файле — берём максимум по нодам. */
    val lastId: Int? = null,
)

data class NodeDto(
    val id: Int? = null,
    val type: String? = null,
    val title: String? = null,
    val x: Float? = null,
    val y: Float? = null,
    // STEP
    val delayMs: Long? = null,
    val ch1: ChannelDto? = null,
    val ch2: ChannelDto? = null,
    // REGISTER
    val regOp: String? = null,
    val regDst: Int? = null,
    val regSrc: String? = null,
    // CONDITION
    val condLeft: Int? = null,
    val condOp: String? = null,
    val condRight: String? = null,
)

data class ChannelDto(
    val carrierEnabled: Boolean? = null,
    val carrierFr: String? = null,
    val carrierMod: String? = null,
    val amEnabled: Boolean? = null,
    val amFr: String? = null,
    val amMod: String? = null,
    val fmEnabled: Boolean? = null,
    val fmBase: String? = null,
    val fmDev: String? = null,
    val fmFr: String? = null,
    val fmMod: String? = null,
)

data class EdgeDto(val from: Int? = null, val port: String? = null, val to: Int? = null)

//╭─ Диск -> домен ───────────────────────────────────────────────────────╮

fun GraphDto.toDomain(): NodeGraph {
    val v = version ?: throw GraphFormatException("в файле нет поля version")
    if (v > GRAPH_FORMAT_VERSION) {
        throw GraphFormatException("файл версии $v, приложение понимает до $GRAPH_FORMAT_VERSION")
    }
    val domainNodes = (nodes ?: emptyList()).map { it.toDomain() }
    val maxNodeId = domainNodes.maxOfOrNull { it.id.value } ?: 0
    return NodeGraph(
        nodes = domainNodes,
        edges = (edges ?: emptyList()).map { it.toDomain() },
        //Старый файл без lastId — берём максимум по нодам; заодно защищаемся
        //от битого файла, где счётчик меньше номера какой-то ноды
        lastId = maxOf(lastId ?: 0, maxNodeId),
    )
}

private fun NodeDto.toDomain(): GraphNode {
    val nodeId = id ?: throw GraphFormatException("у ноды нет id")
    return GraphNode(
        id = NodeId(nodeId),
        title = title.orEmpty(),
        x = x ?: 0f,
        y = y ?: 0f,
        body = when (type) {
            "START" -> NodeBody.Start
            "STOP" -> NodeBody.Stop

            "STEP" -> NodeBody.Step(
                params = StepParams(
                    ch1 = (ch1 ?: ChannelDto()).toDomain(nodeId),
                    ch2 = (ch2 ?: ChannelDto()).toDomain(nodeId),
                ),
                delayMs = delayMs ?: 0L,
            )

            "REGISTER" -> NodeBody.Register(
                op = RegOp.entries.firstOrNull { it.name == regOp }
                    ?: throw GraphFormatException("нода $nodeId: неизвестная операция regOp=$regOp"),
                dst = regDst ?: throw GraphFormatException("нода $nodeId: нет regDst"),
                src = operand(regSrc, nodeId, "regSrc"),
            )

            "CONDITION" -> NodeBody.Condition(
                left = condLeft ?: throw GraphFormatException("нода $nodeId: нет condLeft"),
                op = CompareOp.entries.firstOrNull { it.text == condOp }
                    ?: throw GraphFormatException("нода $nodeId: неизвестное сравнение condOp=$condOp"),
                right = operand(condRight, nodeId, "condRight"),
            )

            else -> throw GraphFormatException("нода $nodeId: неизвестный тип $type")
        },
    )
}

private fun ChannelDto.toDomain(nodeId: Int) = ChannelParams(
    carrierEnabled = carrierEnabled,
    carrierFr = carrierFr?.let { operand(it, nodeId, "carrierFr") },
    carrierMod = carrierMod,
    amEnabled = amEnabled,
    amFr = amFr?.let { operand(it, nodeId, "amFr") },
    amMod = amMod,
    fmEnabled = fmEnabled,
    fmBase = fmBase?.let { operand(it, nodeId, "fmBase") },
    fmDev = fmDev?.let { operand(it, nodeId, "fmDev") },
    fmFr = fmFr?.let { operand(it, nodeId, "fmFr") },
    fmMod = fmMod,
)

private fun operand(token: String?, nodeId: Int, field: String): Operand {
    val text = token ?: throw GraphFormatException("нода $nodeId: нет поля $field")
    return parseOperand(text)
        ?: throw GraphFormatException("нода $nodeId: поле $field не число и не регистр: $text")
}

private fun EdgeDto.toDomain(): GraphEdge = GraphEdge(
    from = NodeId(from ?: throw GraphFormatException("у связи нет from")),
    port = Port.entries.firstOrNull { it.name == port }
        ?: throw GraphFormatException("у связи неизвестный порт: $port"),
    to = NodeId(to ?: throw GraphFormatException("у связи нет to")),
)

//╭─ Домен -> диск ───────────────────────────────────────────────────────╮

fun NodeGraph.toDto(): GraphDto = GraphDto(
    version = GRAPH_FORMAT_VERSION,
    nodes = nodes.map { it.toDto() },
    edges = edges.map { EdgeDto(it.from.value, it.port.name, it.to.value) },
    lastId = lastId,
)

private fun GraphNode.toDto(): NodeDto {
    val base = NodeDto(id = id.value, title = title, x = x, y = y)
    return when (val b = body) {
        is NodeBody.Start -> base.copy(type = "START")
        is NodeBody.Stop -> base.copy(type = "STOP")

        is NodeBody.Step -> base.copy(
            type = "STEP",
            delayMs = b.delayMs,
            ch1 = b.params.ch1.toDto(),
            ch2 = b.params.ch2.toDto(),
        )

        is NodeBody.Register -> base.copy(
            type = "REGISTER",
            regOp = b.op.name,
            regDst = b.dst,
            regSrc = b.src.toToken(),
        )

        is NodeBody.Condition -> base.copy(
            type = "CONDITION",
            condLeft = b.left,
            condOp = b.op.text,
            condRight = b.right.toToken(),
        )
    }
}

private fun ChannelParams.toDto() = ChannelDto(
    carrierEnabled = carrierEnabled,
    carrierFr = carrierFr?.toToken(),
    carrierMod = carrierMod,
    amEnabled = amEnabled,
    amFr = amFr?.toToken(),
    amMod = amMod,
    fmEnabled = fmEnabled,
    fmBase = fmBase?.toToken(),
    fmDev = fmDev?.toToken(),
    fmFr = fmFr?.toToken(),
    fmMod = fmMod,
)
```

- [ ] **Step 4: Запустить тесты**

Run: `./gradlew :app:testDebugUnitTest --tests "*GraphDtoTest*"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/generator2/features/nodes/model/GraphDto.kt app/src/test/java/com/example/generator2/features/nodes/GraphDtoTest.kt
git commit -m "feat(nodes): формат .ng — DTO под Gson и мапперы"
```

---

### Task 5: Валидатор — ошибки

Ошибки блокируют «Пуск». Валидатор остаётся чистой функцией: имена форм сигнала приходят параметром, а не вытаскиваются из генератора.

**Files:**
- Create: `app/src/main/java/com/example/generator2/features/nodes/NodeGraphValidator.kt`
- Test: `app/src/test/java/com/example/generator2/features/nodes/NodeGraphValidatorTest.kt`

- [ ] **Step 1: Написать падающий тест**

```kotlin
package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.*
import com.example.generator2.features.script.CompareOp
import com.example.generator2.features.script.Operand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NodeGraphValidatorTest {

    private fun errors(graph: NodeGraph) =
        validate(graph).filter { it.severity == Severity.ERROR }

    @Test
    fun `новый граф без ошибок`() {
        assertEquals(emptyList<Issue>(), errors(newGraph()))
    }

    @Test
    fun `граф без Старта это ошибка`() {
        val g = newGraph().withoutNode(NodeId(1))
        assertTrue(errors(g).any { it.text.contains("нет ноды Старт") })
    }

    @Test
    fun `два Старта это ошибка`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Старт 2", 0f, 0f, NodeBody.Start))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))
        assertTrue(errors(g).any { it.text.contains("Старт должен быть один") })
    }

    @Test
    fun `у Старта без связи ошибка`() {
        val g = newGraph().withoutEdge(NodeId(1), Port.OUT)
        assertTrue(errors(g).any { it.nodeId == NodeId(1) && it.text.contains("исходящей связи") })
    }

    @Test
    fun `у Шага без связи ошибка`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, step()))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
        assertTrue(errors(g).any { it.nodeId == NodeId(3) && it.text.contains("исходящей связи") })
    }

    @Test
    fun `у Условия пустой выход да это ошибка`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Если", 0f, 0f, condition()))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.NO, NodeId(2))
        assertTrue(errors(g).any { it.text.contains("«да»") })
    }

    @Test
    fun `недостижимая нода это ошибка`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Сирота", 0f, 0f, step()))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))
        assertTrue(errors(g).any { it.nodeId == NodeId(3) && it.text.contains("недостижима") })
    }

    @Test
    fun `связь в несуществующую ноду это ошибка`() {
        val g = newGraph().withEdge(NodeId(1), Port.OUT, NodeId(77))
        assertTrue(errors(g).any { it.text.contains("несуществующую") })
    }

    @Test
    fun `связь в Старт это ошибка`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, step()))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(1))
        assertTrue(errors(g).any { it.text.contains("нельзя вести связь") })
    }

    @Test
    fun `отрицательная задержка это ошибка`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, step(delayMs = -5L)))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))
        assertTrue(errors(g).any { it.text.contains("Отрицательная задержка") })
    }

    @Test
    fun `регистр вне диапазона это ошибка`() {
        val g = newGraph()
            .withNode(
                GraphNode(NodeId(3), "Рег", 0f, 0f, NodeBody.Register(RegOp.LOAD, 42, Operand.Const(1f)))
            )
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))
        assertTrue(errors(g).any { it.text.contains("вне F0..F9") })
    }

    private fun step(delayMs: Long = 100L) =
        NodeBody.Step(StepParams(ChannelParams(), ChannelParams()), delayMs)

    private fun condition() =
        NodeBody.Condition(1, CompareOp.LESS, Operand.Const(5f))
}
```

- [ ] **Step 2: Запустить и убедиться, что падает**

Run: `./gradlew :app:testDebugUnitTest --tests "*NodeGraphValidatorTest*"`
Expected: FAIL — `Unresolved reference: validate`

- [ ] **Step 3: Написать валидатор**

```kotlin
package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.GraphNode
import com.example.generator2.features.nodes.model.NodeBody
import com.example.generator2.features.nodes.model.NodeGraph
import com.example.generator2.features.nodes.model.NodeId
import com.example.generator2.features.nodes.model.Port
import com.example.generator2.features.nodes.model.node
import com.example.generator2.features.nodes.model.ports
import com.example.generator2.features.nodes.model.target
import com.example.generator2.features.script.REGISTER_COUNT

enum class Severity { ERROR, WARNING }

/**
 * Замечание к графу. nodeId == null — замечание про граф целиком,
 * центрировать холст не на чем.
 */
data class Issue(val nodeId: NodeId?, val severity: Severity, val text: String)

/**
 * Проверка графа. Чистая функция: имена форм сигнала приходят параметром,
 * потому что живут они в памяти генератора (gen.itemlistCarrier и
 * gen.itemlistAM), а тянуть сюда Generator значило бы потерять тестируемость.
 *
 * @param carrierNames имена, известные генератору для несущей
 * @param modNames имена, известные генератору для AM и FM — список у них общий
 */
fun validate(
    graph: NodeGraph,
    carrierNames: Set<String> = emptySet(),
    modNames: Set<String> = emptySet(),
): List<Issue> = structureErrors(graph)

//╭─ Ошибки ──────────────────────────────────────────────────────────────╮

private fun structureErrors(graph: NodeGraph): List<Issue> = buildList {

    val starts = graph.nodes.filter { it.body is NodeBody.Start }
    when {
        starts.isEmpty() -> add(Issue(null, Severity.ERROR, "В графе нет ноды Старт"))
        starts.size > 1 -> starts.forEach {
            add(Issue(it.id, Severity.ERROR, "Старт должен быть один, их ${starts.size}"))
        }
    }

    graph.edges.forEach { e ->
        if (graph.node(e.to) == null) {
            add(Issue(e.from, Severity.ERROR, "Связь ведёт в несуществующую ноду ${e.to.value}"))
        }
    }

    graph.nodes.forEach { n ->
        n.body.ports().forEach { port ->
            if (graph.target(n.id, port) == null) {
                add(Issue(n.id, Severity.ERROR, emptyPortText(port)))
            }
        }
    }

    val startIds = starts.map { it.id }.toSet()
    graph.edges.filter { it.to in startIds }.forEach {
        add(Issue(it.from, Severity.ERROR, "В ноду Старт нельзя вести связь"))
    }

    graph.nodes.forEach { n -> registerAndDelayErrors(n).forEach(::add) }

    //Недостижимость считаем только при единственном Старте: иначе
    //непонятно, от какого мерить, и сообщение будет врать
    if (starts.size == 1) {
        val reachable = reachableFrom(graph, starts.first().id)
        graph.nodes.filterNot { it.id in reachable }.forEach {
            add(Issue(it.id, Severity.ERROR, "Нода недостижима от Старта"))
        }
    }
}

private fun emptyPortText(port: Port): String = when (port) {
    Port.OUT -> "У ноды нет исходящей связи"
    Port.YES -> "У Условия не заполнен выход «да»"
    Port.NO -> "У Условия не заполнен выход «нет»"
}

private fun registerAndDelayErrors(node: GraphNode): List<Issue> = buildList {
    val range = 0 until REGISTER_COUNT
    val outOfRange = "вне F0..F${REGISTER_COUNT - 1}"

    when (val b = node.body) {
        is NodeBody.Step ->
            if (b.delayMs < 0) {
                add(Issue(node.id, Severity.ERROR, "Отрицательная задержка ${b.delayMs}"))
            }

        is NodeBody.Register ->
            if (b.dst !in range) {
                add(Issue(node.id, Severity.ERROR, "Регистр F${b.dst} $outOfRange"))
            }

        is NodeBody.Condition ->
            if (b.left !in range) {
                add(Issue(node.id, Severity.ERROR, "Регистр F${b.left} $outOfRange"))
            }

        else -> Unit
    }
}

/** Обход в ширину: до кого можно добраться от ноды start */
internal fun reachableFrom(graph: NodeGraph, start: NodeId): Set<NodeId> {
    val seen = linkedSetOf(start)
    val queue = ArrayDeque(listOf(start))
    while (queue.isNotEmpty()) {
        val id = queue.removeFirst()
        val node = graph.node(id) ?: continue
        node.body.ports().forEach { port ->
            graph.target(id, port)?.let { if (seen.add(it)) queue.addLast(it) }
        }
    }
    return seen
}
```

- [ ] **Step 4: Запустить тесты**

Run: `./gradlew :app:testDebugUnitTest --tests "*NodeGraphValidatorTest*"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/generator2/features/nodes/NodeGraphValidator.kt app/src/test/java/com/example/generator2/features/nodes/NodeGraphValidatorTest.kt
git commit -m "feat(nodes): валидатор графа — ошибки, блокирующие запуск"
```

---

### Task 6: Валидатор — предупреждения

Предупреждения не блокируют запуск. Самое интересное — цикл без единой задержки: чтобы не перебирать циклы, ищем сильно связные компоненты алгоритмом Тарьяна.

**Files:**
- Modify: `app/src/main/java/com/example/generator2/features/nodes/NodeGraphValidator.kt`
- Test: `app/src/test/java/com/example/generator2/features/nodes/NodeGraphWarningsTest.kt`

- [ ] **Step 1: Написать падающий тест**

```kotlin
package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.*
import com.example.generator2.features.script.Operand
import org.junit.Assert.assertTrue
import org.junit.Test

class NodeGraphWarningsTest {

    private fun warnings(
        graph: NodeGraph,
        carrier: Set<String> = emptySet(),
        mod: Set<String> = emptySet(),
    ) = validate(graph, carrier, mod).filter { it.severity == Severity.WARNING }

    /** Старт -> Шаг -> сам себя. Задержку задаём параметром. */
    private fun selfLoop(delayMs: Long, params: StepParams = empty()): NodeGraph =
        newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, NodeBody.Step(params, delayMs)))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(3))
            .withoutNode(NodeId(2))

    @Test
    fun `цикл без задержки это предупреждение`() {
        assertTrue(warnings(selfLoop(0L)).any { it.text.contains("без задержки") })
    }

    @Test
    fun `цикл с задержкой не ругается`() {
        assertTrue(warnings(selfLoop(100L)).none { it.text.contains("без задержки") })
    }

    @Test
    fun `линейный граф без циклов не ругается`() {
        assertTrue(warnings(newGraph()).none { it.text.contains("без задержки") })
    }

    @Test
    fun `пустой шаг с нулевой задержкой это предупреждение`() {
        assertTrue(warnings(selfLoop(0L)).any { it.text.contains("ничего не делает") })
    }

    @Test
    fun `CR FR и FM BASE вместе это предупреждение`() {
        val params = StepParams(
            ch1 = ChannelParams(carrierFr = Operand.Const(1000f), fmBase = Operand.Const(2000f)),
            ch2 = ChannelParams(),
        )
        assertTrue(warnings(selfLoop(100L, params)).any { it.text.contains("одно и то же поле") })
    }

    @Test
    fun `неизвестное имя формы это предупреждение`() {
        val params = StepParams(
            ch1 = ChannelParams(carrierMod = "Небывалая", amMod = "02_HWave"),
            ch2 = ChannelParams(),
        )
        val w = warnings(selfLoop(100L, params), carrier = setOf("Sine"), mod = setOf("02_HWave"))
        assertTrue(w.any { it.text.contains("Небывалая") })
        assertTrue(w.none { it.text.contains("02_HWave") })
    }

    private fun empty() = StepParams(ChannelParams(), ChannelParams())
}
```

- [ ] **Step 2: Запустить и убедиться, что падает**

Run: `./gradlew :app:testDebugUnitTest --tests "*NodeGraphWarningsTest*"`
Expected: FAIL — предупреждений нет, `validate` возвращает только ошибки

- [ ] **Step 3: Добавить предупреждения в `validate`**

Заменить тело `validate` на:

```kotlin
fun validate(
    graph: NodeGraph,
    carrierNames: Set<String> = emptySet(),
    modNames: Set<String> = emptySet(),
): List<Issue> = structureErrors(graph) + warnings(graph, carrierNames, modNames)
```

и дописать в конец файла:

```kotlin
//╭─ Предупреждения ──────────────────────────────────────────────────────╮

private fun warnings(
    graph: NodeGraph,
    carrierNames: Set<String>,
    modNames: Set<String>,
): List<Issue> = buildList {

    //Цикл без единой задержки. Устройство не повиснет — в Script есть
    //YIELD_EVERY, — но ядро будет греться, поэтому предупреждение, не ошибка
    stronglyConnected(graph).forEach { component ->
        val delaySum = component
            .mapNotNull { graph.node(it)?.body as? NodeBody.Step }
            .sumOf { it.delayMs }
        if (delaySum == 0L) {
            component.forEach {
                add(Issue(it, Severity.WARNING, "Цикл без задержки: будет крутиться на полной скорости"))
            }
        }
    }

    graph.nodes.forEach { n ->
        val step = n.body as? NodeBody.Step ?: return@forEach

        if (step.params.checkedCount == 0 && step.delayMs == 0L) {
            add(Issue(n.id, Severity.WARNING, "Шаг ничего не делает: ни одного параметра, задержка ноль"))
        }

        listOf(1 to step.params.ch1, 2 to step.params.ch2).forEach { (ch, p) ->
            if (p.carrierFr != null && p.fmBase != null) {
                add(
                    Issue(
                        n.id, Severity.WARNING,
                        "CR$ch FR и FM$ch BASE пишут одно и то же поле, победит второе",
                    )
                )
            }
            unknownWaveforms(p, carrierNames, modNames).forEach { name ->
                add(Issue(n.id, Severity.WARNING, "Форма «$name» генератору неизвестна"))
            }
        }
    }
}

/**
 * Имена форм, которых генератор не знает.
 *
 * Несущая ищется в gen.itemlistCarrier, AM и FM — оба в gen.itemlistAM
 * (см. Spinner_Send_Buffer). Неизвестное имя движок молча игнорирует,
 * поэтому и предупреждение, а не ошибка.
 */
private fun unknownWaveforms(
    p: ChannelParams,
    carrierNames: Set<String>,
    modNames: Set<String>,
): List<String> = buildList {
    p.carrierMod?.let { if (carrierNames.isNotEmpty() && it !in carrierNames) add(it) }
    p.amMod?.let { if (modNames.isNotEmpty() && it !in modNames) add(it) }
    p.fmMod?.let { if (modNames.isNotEmpty() && it !in modNames) add(it) }
}

/**
 * Нетривиальные сильно связные компоненты по Тарьяну: больше одной ноды
 * либо петля на себя. Ровно они и есть циклы графа, без перебора самих циклов.
 */
internal fun stronglyConnected(graph: NodeGraph): List<Set<NodeId>> {
    var counter = 0
    val index = HashMap<NodeId, Int>()
    val low = HashMap<NodeId, Int>()
    val stack = ArrayDeque<NodeId>()
    val onStack = HashSet<NodeId>()
    val result = mutableListOf<Set<NodeId>>()

    fun successors(id: NodeId): List<NodeId> {
        val node = graph.node(id) ?: return emptyList()
        return node.body.ports().mapNotNull { graph.target(id, it) }
    }

    fun strongConnect(v: NodeId) {
        index[v] = counter
        low[v] = counter
        counter++
        stack.addLast(v)
        onStack.add(v)

        successors(v).forEach { w ->
            when {
                w !in index -> {
                    strongConnect(w)
                    low[v] = minOf(low.getValue(v), low.getValue(w))
                }

                w in onStack -> low[v] = minOf(low.getValue(v), index.getValue(w))
            }
        }

        if (low.getValue(v) == index.getValue(v)) {
            val component = linkedSetOf<NodeId>()
            while (true) {
                val w = stack.removeLast()
                onStack.remove(w)
                component.add(w)
                if (w == v) break
            }
            val isLoop = component.size == 1 && v in successors(v)
            if (component.size > 1 || isLoop) result.add(component)
        }
    }

    graph.nodes.forEach { if (it.id !in index) strongConnect(it.id) }
    return result
}
```

- [ ] **Step 4: Запустить тесты**

Run: `./gradlew :app:testDebugUnitTest --tests "*NodeGraph*"`
Expected: PASS — и предупреждения, и старые тесты ошибок

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/generator2/features/nodes/NodeGraphValidator.kt app/src/test/java/com/example/generator2/features/nodes/NodeGraphWarningsTest.kt
git commit -m "feat(nodes): предупреждения валидатора, циклы через сильно связные компоненты"
```

---

### Task 7: Компилятор — линейная цепочка

Сначала обход, адреса и две простых ноды: Шаг и Стоп. Ветвление добавим следующей задачей.

**Files:**
- Create: `app/src/main/java/com/example/generator2/features/nodes/NodeGraphCompiler.kt`
- Test: `app/src/test/java/com/example/generator2/features/nodes/NodeGraphCompilerTest.kt`

- [ ] **Step 1: Написать падающий тест**

```kotlin
package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.*
import com.example.generator2.features.script.Operand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NodeGraphCompilerTest {

    private fun ok(graph: NodeGraph): CompileResult.Ok {
        val result = compile(graph)
        assertTrue("ожидался Ok, получено $result", result is CompileResult.Ok)
        return result as CompileResult.Ok
    }

    @Test
    fun `новый граф это одна строка END`() {
        val r = ok(newGraph())
        assertEquals(listOf("END"), r.lines)
        assertEquals(listOf(NodeId(2)), r.lineToNode)
    }

    @Test
    fun `битый граф не компилируется`() {
        val result = compile(newGraph().withoutEdge(NodeId(1), Port.OUT))
        assertTrue(result is CompileResult.Failed)
    }

    @Test
    fun `шаг печатает значения потом переключатели потом задержку и переход`() {
        val params = StepParams(
            ch1 = ChannelParams(
                carrierEnabled = true,
                carrierFr = Operand.Const(1000f),
                carrierMod = "Sine",
            ),
            ch2 = ChannelParams(),
        )
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, NodeBody.Step(params, 500L)))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))

        val r = ok(g)
        assertEquals(
            listOf(
                "CR1 MOD Sine",
                "CR1 FR 1000.0",
                "CH1 CR ON",
                "DELAY 500",
                "GOTO 5",
                "END",
            ),
            r.lines,
        )
        assertEquals(
            listOf(NodeId(3), NodeId(3), NodeId(3), NodeId(3), NodeId(3), NodeId(2)),
            r.lineToNode,
        )
    }

    @Test
    fun `нулевая задержка не печатается`() {
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, NodeBody.Step(empty(), 0L)))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))

        assertEquals(listOf("GOTO 1", "END"), ok(g).lines)
    }

    @Test
    fun `второй канал печатается после первого`() {
        val params = StepParams(
            ch1 = ChannelParams(carrierFr = Operand.Const(100f)),
            ch2 = ChannelParams(amFr = Operand.Reg(3)),
        )
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, NodeBody.Step(params, 0L)))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))

        assertEquals(
            listOf("CR1 FR 100.0", "AM2 FR F3", "GOTO 3", "END"),
            ok(g).lines,
        )
    }

    private fun empty() = StepParams(ChannelParams(), ChannelParams())
}
```

- [ ] **Step 2: Запустить и убедиться, что падает**

Run: `./gradlew :app:testDebugUnitTest --tests "*NodeGraphCompilerTest*"`
Expected: FAIL — `Unresolved reference: compile`

- [ ] **Step 3: Написать компилятор**

```kotlin
package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.ChannelParams
import com.example.generator2.features.nodes.model.GraphNode
import com.example.generator2.features.nodes.model.NodeBody
import com.example.generator2.features.nodes.model.NodeGraph
import com.example.generator2.features.nodes.model.NodeId
import com.example.generator2.features.nodes.model.Port
import com.example.generator2.features.nodes.model.StepParams
import com.example.generator2.features.nodes.model.node
import com.example.generator2.features.nodes.model.ports
import com.example.generator2.features.nodes.model.startNode
import com.example.generator2.features.nodes.model.target
import com.example.generator2.features.script.toToken

sealed interface CompileResult {

    /**
     * @param lines строки для Script.list
     * @param lineToNode владелец каждой строки, индексы совпадают с lines
     */
    data class Ok(
        val lines: List<String>,
        val lineToNode: List<NodeId>,
        val warnings: List<Issue>,
    ) : CompileResult

    data class Failed(val errors: List<Issue>, val warnings: List<Issue>) : CompileResult
}

/**
 * Граф в строки скрипта.
 *
 * Обход в глубину от преемника Старта, преемники в порядке портов.
 * Порядок влияет только на читаемость: все переходы — явные GOTO,
 * семантика от него не зависит. Зато он детерминирован, и golden-тесты
 * не разъезжаются от прогона к прогону.
 */
fun compile(
    graph: NodeGraph,
    carrierNames: Set<String> = emptySet(),
    modNames: Set<String> = emptySet(),
): CompileResult {

    val issues = validate(graph, carrierNames, modNames)
    val errors = issues.filter { it.severity == Severity.ERROR }
    val warnings = issues.filter { it.severity == Severity.WARNING }
    if (errors.isNotEmpty()) return CompileResult.Failed(errors, warnings)

    val order = emissionOrder(graph)

    //Проход 1: размер блока каждой ноды даёт её адрес
    val address = HashMap<NodeId, Int>()
    var cursor = 0
    order.forEach { node ->
        address[node.id] = cursor
        cursor += node.lineCount()
    }

    //Проход 2: строки с уже известными номерами переходов
    val lines = ArrayList<String>(cursor)
    val owners = ArrayList<NodeId>(cursor)
    order.forEach { node ->
        val block = emit(node, graph, address)
        lines.addAll(block)
        repeat(block.size) { owners.add(node.id) }
    }

    return CompileResult.Ok(lines, owners, warnings)
}

/**
 * Ноды в порядке печати. Старт не попадает: он печатает ноль строк,
 * а вход в граф — это первая строка его преемника, то есть pc = 0.
 */
private fun emissionOrder(graph: NodeGraph): List<GraphNode> {
    val start = graph.startNode() ?: return emptyList()
    val first = graph.target(start.id, Port.OUT) ?: return emptyList()

    val visited = LinkedHashSet<NodeId>()

    fun walk(id: NodeId) {
        if (!visited.add(id)) return
        val node = graph.node(id) ?: return
        node.body.ports().forEach { port -> graph.target(id, port)?.let { walk(it) } }
    }

    walk(first)
    return visited.mapNotNull { graph.node(it) }
}

private fun GraphNode.lineCount(): Int = when (val b = body) {
    is NodeBody.Start -> 0
    is NodeBody.Stop -> 1
    is NodeBody.Register -> 2
    is NodeBody.Condition -> 5
    is NodeBody.Step -> b.params.assignmentLines().size + (if (b.delayMs > 0) 1 else 0) + 1
}

private fun emit(node: GraphNode, graph: NodeGraph, address: Map<NodeId, Int>): List<String> {

    fun jump(port: Port): String {
        val to = requireNotNull(graph.target(node.id, port)) {
            "нода ${node.id.value}: порт $port пуст, валидатор должен был это поймать"
        }
        return "GOTO ${requireNotNull(address[to]) { "нет адреса ноды ${to.value}" }}"
    }

    return when (val b = node.body) {
        is NodeBody.Start -> emptyList()
        is NodeBody.Stop -> listOf("END")

        is NodeBody.Step -> buildList {
            addAll(b.params.assignmentLines())
            if (b.delayMs > 0) add("DELAY ${b.delayMs}")
            add(jump(Port.OUT))
        }

        is NodeBody.Register -> error("Регистр появится в следующей задаче")
        is NodeBody.Condition -> error("Условие появится в следующей задаче")
    }
}

/**
 * Строки Шага в каноническом порядке: сначала значения, потом переключатели,
 * сначала первый канал, потом второй.
 *
 * Значения раньше включения не случайны: блок оживает уже на нужной частоте,
 * а не на прежней с последующим скачком.
 */
internal fun StepParams.assignmentLines(): List<String> = ch1.lines(1) + ch2.lines(2)

private fun ChannelParams.lines(ch: Int): List<String> = buildList {
    carrierMod?.let { add("CR$ch MOD $it") }
    carrierFr?.let { add("CR$ch FR ${it.toToken()}") }
    amMod?.let { add("AM$ch MOD $it") }
    amFr?.let { add("AM$ch FR ${it.toToken()}") }
    fmMod?.let { add("FM$ch MOD $it") }
    fmBase?.let { add("FM$ch BASE ${it.toToken()}") }
    fmDev?.let { add("FM$ch DEV ${it.toToken()}") }
    fmFr?.let { add("FM$ch FR ${it.toToken()}") }

    carrierEnabled?.let { add("CH$ch CR ${if (it) "ON" else "OFF"}") }
    amEnabled?.let { add("CH$ch AM ${if (it) "ON" else "OFF"}") }
    fmEnabled?.let { add("CH$ch FM ${if (it) "ON" else "OFF"}") }
}
```

- [ ] **Step 4: Запустить тесты**

Run: `./gradlew :app:testDebugUnitTest --tests "*NodeGraphCompilerTest*"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/generator2/features/nodes/NodeGraphCompiler.kt app/src/test/java/com/example/generator2/features/nodes/NodeGraphCompilerTest.kt
git commit -m "feat(nodes): компилятор графа — обход, адреса, Шаг и Стоп"
```

---

### Task 8: Компилятор — Регистр, Условие и самопроверка

У Условия ровно пять строк, потому что движок на истинном условии ставит `pc = current + 1`, а на ложном через `findPairLine` попадает на строку после `ELSE`. `ENDIF` не исполняется никогда, но обязан быть, иначе `findPairLine` бросит «не найден парный ENDIF».

**Files:**
- Modify: `app/src/main/java/com/example/generator2/features/nodes/NodeGraphCompiler.kt`
- Test: `app/src/test/java/com/example/generator2/features/nodes/NodeGraphCompilerBranchTest.kt`

- [ ] **Step 1: Написать падающий тест**

```kotlin
package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.*
import com.example.generator2.features.script.Cmd
import com.example.generator2.features.script.CompareOp
import com.example.generator2.features.script.Operand
import com.example.generator2.features.script.findPairLine
import com.example.generator2.features.script.parseCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NodeGraphCompilerBranchTest {

    /**
     * Старт -> Шаг(CR1 FR F1, 100 мс) -> Регистр(F1 += 50) -> Условие(F1 < 1200)
     * «да» обратно в Шаг, «нет» в Стоп.
     */
    private fun sweep(): NodeGraph {
        val params = StepParams(
            ch1 = ChannelParams(carrierFr = Operand.Reg(1)),
            ch2 = ChannelParams(),
        )
        return newGraph()
            .withNode(GraphNode(NodeId(3), "Разгон", 0f, 0f, NodeBody.Step(params, 100L)))
            .withNode(
                GraphNode(NodeId(4), "шаг", 0f, 0f, NodeBody.Register(RegOp.PLUS, 1, Operand.Const(50f)))
            )
            .withNode(
                GraphNode(
                    NodeId(5), "предел", 0f, 0f,
                    NodeBody.Condition(1, CompareOp.LESS, Operand.Const(1200f)),
                )
            )
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(4))
            .withEdge(NodeId(4), Port.OUT, NodeId(5))
            .withEdge(NodeId(5), Port.YES, NodeId(3))
            .withEdge(NodeId(5), Port.NO, NodeId(2))
    }

    private fun ok(graph: NodeGraph): CompileResult.Ok {
        val result = compile(graph)
        assertTrue("ожидался Ok, получено $result", result is CompileResult.Ok)
        return result as CompileResult.Ok
    }

    @Test
    fun `свип компилируется в ожидаемые строки`() {
        assertEquals(
            listOf(
                "CR1 FR F1",
                "DELAY 100",
                "GOTO 3",
                "PLUS F1 50.0",
                "GOTO 5",
                "IF F1 < 1200.0",
                "GOTO 0",
                "ELSE",
                "GOTO 10",
                "ENDIF",
                "END",
            ),
            ok(sweep()).lines,
        )
    }

    @Test
    fun `каждая строка знает свою ноду`() {
        assertEquals(
            listOf(3, 3, 3, 4, 4, 5, 5, 5, 5, 5, 2).map { NodeId(it) },
            ok(sweep()).lineToNode,
        )
    }

    @Test
    fun `движок на истинном условии уходит в ветку да`() {
        val lines = ok(sweep()).lines
        val ifLine = lines.indexOfFirst { it.startsWith("IF") }
        //Движок ставит pc = current + 1, там стоит переход ветки «да»
        val cmd = parseCommand(lines[ifLine + 1], ifLine + 1)
        assertEquals(Cmd.Goto(0), cmd)
    }

    @Test
    fun `движок на ложном условии уходит в ветку нет`() {
        val lines = ok(sweep()).lines
        val ifLine = lines.indexOfFirst { it.startsWith("IF") }
        //Ровно так движок ищет ложную ветку
        val target = findPairLine(lines, ifLine, stopOnElse = true)
        assertEquals(Cmd.Goto(10), parseCommand(lines[target], target))
    }

    @Test
    fun `все строки разбираются и переходы попадают в диапазон`() {
        val r = ok(sweep())
        r.lines.forEachIndexed { i, line ->
            val cmd = parseCommand(line, i)
            if (cmd is Cmd.Goto) assertTrue("GOTO ${cmd.target} вне диапазона", cmd.target in r.lines.indices)
        }
        assertEquals(r.lines.size, r.lineToNode.size)
    }
}
```

- [ ] **Step 2: Запустить и убедиться, что падает**

Run: `./gradlew :app:testDebugUnitTest --tests "*NodeGraphCompilerBranchTest*"`
Expected: FAIL — `IllegalStateException: Регистр появится в следующей задаче`

- [ ] **Step 3: Дописать эмиссию Регистра и Условия**

В `emit` заменить две заглушки на:

```kotlin
        is NodeBody.Register -> listOf(
            when (b.op) {
                RegOp.LOAD -> "LOAD F${b.dst} ${b.src.toToken()}"
                RegOp.PLUS -> "PLUS F${b.dst} ${b.src.toToken()}"
                RegOp.MINUS -> "MINUS F${b.dst} ${b.src.toToken()}"
            },
            jump(Port.OUT),
        )

        //Пять строк не от щедрости: на истинном условии движок ставит
        //pc = current + 1 и попадает на переход ветки «да», на ложном
        //через findPairLine попадает на строку после ELSE. ENDIF не
        //исполняется никогда, но без него findPairLine бросит исключение.
        is NodeBody.Condition -> listOf(
            "IF F${b.left} ${b.op.text} ${b.right.toToken()}",
            jump(Port.YES),
            "ELSE",
            jump(Port.NO),
            "ENDIF",
        )
```

Добавить импорт:

```kotlin
import com.example.generator2.features.nodes.model.RegOp
```

- [ ] **Step 4: Запустить тесты**

Run: `./gradlew :app:testDebugUnitTest --tests "*NodeGraphCompiler*"`
Expected: PASS

- [ ] **Step 5: Написать падающий тест на самопроверку**

Дописать в `NodeGraphCompilerBranchTest`:

```kotlin
    @Test
    fun `самопроверка ловит строку, которую движок не разбирает`() {
        //Форма сигнала с пробелом развалит разбор: "CR1 MOD 02 Hz" — лишний токен
        val params = StepParams(
            ch1 = ChannelParams(carrierMod = "02 Hz"),
            ch2 = ChannelParams(),
        )
        val g = newGraph()
            .withNode(GraphNode(NodeId(3), "Шаг", 0f, 0f, NodeBody.Step(params, 0L)))
            .withEdge(NodeId(1), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(2))

        val result = compile(g)
        assertTrue("ожидался Failed, получено $result", result is CompileResult.Failed)
        assertTrue(
            (result as CompileResult.Failed).errors.any { it.text.contains("Внутренняя ошибка") }
        )
    }
```

- [ ] **Step 6: Запустить и убедиться, что падает**

Run: `./gradlew :app:testDebugUnitTest --tests "*NodeGraphCompilerBranchTest*"`
Expected: FAIL — компилятор вернул `Ok`, самопроверки ещё нет

Примечание: `parseCommand("CR1 MOD 02 Hz")` лишний токен не заметит — он читает `arg(2)` и остальное игнорирует. Тест всё равно нужен: он фиксирует, что самопроверка вызывается. Если он проходит и без самопроверки, замените форму на `"02\nHz"` — перевод строки разваливает разбор наверняка.

- [ ] **Step 7: Добавить самопроверку**

В `compile` перед `return CompileResult.Ok(...)` вставить:

```kotlin
    selfCheck(lines, owners)?.let { return CompileResult.Failed(listOf(it), warnings) }
```

и дописать в конец файла:

```kotlin
/**
 * Строки печатал компилятор, поэтому любая ошибка разбора в рантайме —
 * его баг, а не пользователя. Дешёвая проверка на выходе ловит регрессию
 * на устройстве, а в тестах становится сильным инвариантом.
 *
 * @return замечание, если что-то не сошлось; null, если всё в порядке
 */
private fun selfCheck(lines: List<String>, owners: List<NodeId>): Issue? {

    fun internal(text: String) =
        Issue(null, Severity.ERROR, "Внутренняя ошибка компилятора: $text")

    if (lines.size != owners.size) {
        return internal("строк ${lines.size}, владельцев ${owners.size}")
    }

    lines.forEachIndexed { i, line ->
        val cmd = try {
            parseCommand(line, i)
        } catch (e: ScriptException) {
            return internal("строка $i не разбирается: $line (${e.message})")
        }
        if (cmd is Cmd.Goto && cmd.target !in lines.indices) {
            return internal("GOTO ${cmd.target} вне диапазона 0..${lines.lastIndex}")
        }
    }

    return null
}
```

Добавить импорты:

```kotlin
import com.example.generator2.features.script.Cmd
import com.example.generator2.features.script.ScriptException
import com.example.generator2.features.script.parseCommand
```

- [ ] **Step 8: Запустить все тесты ядра**

Run: `./gradlew :app:testDebugUnitTest --tests "*features.nodes*"`
Expected: PASS

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/example/generator2/features/nodes/NodeGraphCompiler.kt app/src/test/java/com/example/generator2/features/nodes/NodeGraphCompilerBranchTest.kt
git commit -m "feat(nodes): компиляция Регистра и Условия, самопроверка компилятора"
```

---

### Task 9: Сквозной тест — граф реально крутит генератор

Главный тест плана. `Generator` — обычный класс без Android-зависимостей, а `Script(gen)` берёт только его, поэтому настоящий движок запускается прямо в JVM-тесте. Проверяем не «строки похожи на ожидаемые», а «частота несущей действительно прошла по ступеням и прогон закончился».

Новых зависимостей не нужно: `runBlocking` и `withTimeout` живут в `kotlinx-coroutines-core`, который уже подключён в основной набор, а он попадает и на тестовый classpath.

**Files:**
- Test: `app/src/test/java/com/example/generator2/features/nodes/NodeGraphEndToEndTest.kt`

- [ ] **Step 1: Написать тест**

```kotlin
package com.example.generator2.features.nodes

import com.example.generator2.features.generator.Generator
import com.example.generator2.features.nodes.model.*
import com.example.generator2.features.script.CompareOp
import com.example.generator2.features.script.Operand
import com.example.generator2.features.script.Script
import com.example.generator2.features.script.StateCommandScript
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NodeGraphEndToEndTest {

    /**
     * Старт -> Регистр(LOAD F1 1000) -> Шаг(CR1 FR F1, 100 мс)
     *       -> Регистр(F1 += 50) -> Условие(F1 < 1200)
     * «да» обратно в Шаг, «нет» в Стоп.
     *
     * Частота несущей должна пройти 1000, 1050, 1100, 1150 и встать:
     * на пятом круге F1 = 1200, условие ложно, идём в Стоп.
     */
    private fun sweep(): NodeGraph {
        val params = StepParams(
            ch1 = ChannelParams(carrierFr = Operand.Reg(1)),
            ch2 = ChannelParams(),
        )
        return newGraph()
            .withNode(
                GraphNode(NodeId(6), "старт F1", 0f, 0f, NodeBody.Register(RegOp.LOAD, 1, Operand.Const(1000f)))
            )
            .withNode(GraphNode(NodeId(3), "Разгон", 0f, 0f, NodeBody.Step(params, 100L)))
            .withNode(
                GraphNode(NodeId(4), "шаг", 0f, 0f, NodeBody.Register(RegOp.PLUS, 1, Operand.Const(50f)))
            )
            .withNode(
                GraphNode(
                    NodeId(5), "предел", 0f, 0f,
                    NodeBody.Condition(1, CompareOp.LESS, Operand.Const(1200f)),
                )
            )
            .withEdge(NodeId(1), Port.OUT, NodeId(6))
            .withEdge(NodeId(6), Port.OUT, NodeId(3))
            .withEdge(NodeId(3), Port.OUT, NodeId(4))
            .withEdge(NodeId(4), Port.OUT, NodeId(5))
            .withEdge(NodeId(5), Port.YES, NodeId(3))
            .withEdge(NodeId(5), Port.NO, NodeId(2))
    }

    @Test
    fun `свип компилируется в ожидаемые адреса`() {
        val r = compile(sweep()) as CompileResult.Ok
        assertEquals(
            listOf(
                "LOAD F1 1000.0",
                "GOTO 2",
                "CR1 FR F1",
                "DELAY 100",
                "GOTO 5",
                "PLUS F1 50.0",
                "GOTO 7",
                "IF F1 < 1200.0",
                "GOTO 2",
                "ELSE",
                "GOTO 12",
                "ENDIF",
                "END",
            ),
            r.lines,
        )
    }

    @Test
    fun `граф свипа проводит несущую по ступеням и останавливается`() = runBlocking {

        val gen = Generator()
        val script = Script(gen)
        val compiled = compile(sweep()) as CompileResult.Ok

        script.list.clear()
        compiled.lines.forEach { script.list.add(it) }

        val seen = mutableListOf<Float>()
        val collector = launch(Dispatchers.Default) {
            gen.liveData.ch1_Carrier_Fr.collect { seen.add(it) }
        }

        script.command(StateCommandScript.START)

        //Движок крутится на своём Dispatchers.Default, виртуального времени
        //ему не подсунуть — ждём по-настоящему, но с потолком
        withTimeout(5_000) {
            while (script.state != StateCommandScript.ISTOPPING) delay(10)
        }
        collector.cancel()

        //400 — значение по умолчанию, оно приходит подписчику первым
        assertEquals(listOf(400f, 1000f, 1050f, 1100f, 1150f), seen.distinct())
        assertEquals(0, script.pc.value)
    }

    @Test
    fun `стоп посреди прогона обрывает движение частоты`() = runBlocking {

        val gen = Generator()
        val script = Script(gen)
        val compiled = compile(sweep()) as CompileResult.Ok

        script.list.clear()
        compiled.lines.forEach { script.list.add(it) }

        script.command(StateCommandScript.START)
        withTimeout(2_000) {
            while (gen.liveData.ch1_Carrier_Fr.value < 1000f) delay(5)
        }
        script.command(StateCommandScript.STOP)

        val frozen = gen.liveData.ch1_Carrier_Fr.value
        delay(400)
        assertEquals(frozen, gen.liveData.ch1_Carrier_Fr.value)
        assertTrue(script.state == StateCommandScript.ISTOPPING)
    }
}
```

- [ ] **Step 2: Запустить**

Run: `./gradlew :app:testDebugUnitTest --tests "*NodeGraphEndToEndTest*"`
Expected: PASS

Если первый тест падает на `assertEquals` со списком частот — сравните полученный список с ожидаемым: расхождение в первом элементе означает, что `StateFlow` успел проглотить значение по умолчанию, и его надо убрать из ожидания; расхождение в середине означает настоящую ошибку в адресах переходов, и чинить надо компилятор.

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/example/generator2/features/nodes/NodeGraphEndToEndTest.kt
git commit -m "test(nodes): сквозной прогон графа через живой движок Script"
```

---

### Task 10: Файлы `.ng`

**Files:**
- Modify: `app/src/main/java/com/example/generator2/AppPath.kt`
- Create: `app/src/main/java/com/example/generator2/features/nodes/NodeGraphUtils.kt`

- [ ] **Step 1: Добавить папку в `AppPath`**

В `private enum class Folder` добавить строку после `SCRIPT("Script"),`:

```kotlin
    NODES("Nodes"),
```

Рядом с `val script = ...` добавить:

```kotlin
    val nodes = envoriment?.absolutePath + "/${appMain}/${Folder.NODES.value}"
```

В блоке `init` рядом с `File(script).mkdirs()` добавить:

```kotlin
        File(nodes).mkdirs()
```

- [ ] **Step 2: Написать утилиту**

Зеркало `ScriptUtils`, только формат другой.

```kotlin
package com.example.generator2.features.nodes

import com.example.generator2.AppPath
import com.example.generator2.features.nodes.model.GraphDto
import com.example.generator2.features.nodes.model.GraphFormatException
import com.example.generator2.features.nodes.model.NodeGraph
import com.example.generator2.features.nodes.model.toDomain
import com.example.generator2.features.nodes.model.toDto
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import timber.log.Timber
import java.io.File

/**
 * Файлы графов в /Gen3/Nodes. Повторяет ScriptUtils: тот же набор операций,
 * другой формат.
 */
class NodeGraphUtils(private val appPath: AppPath, private val gson: Gson = Gson()) {

    /** Имена графов в папке без расширения */
    fun list(): List<String> =
        File(appPath.nodes).list()
            ?.filter { it.endsWith(GRAPH_EXT) }
            ?.map { it.removeSuffix(GRAPH_EXT) }
            ?.sorted()
            ?: emptyList()

    fun save(graph: NodeGraph, name: String) {
        File(appPath.nodes, "$name$GRAPH_EXT").writeText(gson.toJson(graph.toDto()))
    }

    /**
     * @throws GraphFormatException файл не читается: не тот json, неизвестный
     * тип ноды, битый операнд, версия новее нашей
     */
    fun read(name: String): NodeGraph {
        val text = File(appPath.nodes, "$name$GRAPH_EXT").readText()
        val dto = try {
            gson.fromJson(text, GraphDto::class.java)
        } catch (e: JsonSyntaxException) {
            throw GraphFormatException("файл $name$GRAPH_EXT не похож на json: ${e.message}")
        } ?: throw GraphFormatException("файл $name$GRAPH_EXT пуст")

        return dto.toDomain()
    }

    fun delete(name: String) {
        File(appPath.nodes, "$name$GRAPH_EXT").delete()
    }

    /** Новый файл пишется до удаления старого: при сбое записи граф не пропадёт */
    fun rename(from: String, to: String) {
        if (from == to) return
        try {
            save(read(from), to)
            delete(from)
        } catch (e: Exception) {
            Timber.e(e, "rename($from -> $to)")
        }
    }

    private companion object {
        const val GRAPH_EXT = ".ng"
    }
}
```

- [ ] **Step 3: Собрать**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/generator2/AppPath.kt app/src/main/java/com/example/generator2/features/nodes/NodeGraphUtils.kt
git commit -m "feat(nodes): папка Nodes и файловые операции над .ng"
```

---

### Task 11: Арбитр генератора

Экран скриптов и экран графа пишут в одни и те же `gen.liveData`. Если оба прогона пойдут разом, значения будут драться, и результат непредсказуем.

**Files:**
- Create: `app/src/main/java/com/example/generator2/features/nodes/GeneratorArbiter.kt`
- Test: `app/src/test/java/com/example/generator2/features/nodes/GeneratorArbiterTest.kt`

- [ ] **Step 1: Написать падающий тест**

```kotlin
package com.example.generator2.features.nodes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratorArbiterTest {

    @Test
    fun `сначала генератором никто не владеет`() {
        assertEquals(RunOwner.NONE, GeneratorArbiter().owner)
    }

    @Test
    fun `захват графом глушит скрипт`() {
        val arbiter = GeneratorArbiter()
        var scriptStopped = false
        arbiter.register(RunOwner.SCRIPT) { scriptStopped = true }

        arbiter.acquire(RunOwner.SCRIPT)
        arbiter.acquire(RunOwner.NODES)

        assertTrue(scriptStopped)
        assertEquals(RunOwner.NODES, arbiter.owner)
    }

    @Test
    fun `повторный захват тем же владельцем себя не глушит`() {
        val arbiter = GeneratorArbiter()
        var stops = 0
        arbiter.register(RunOwner.NODES) { stops++ }

        arbiter.acquire(RunOwner.NODES)
        arbiter.acquire(RunOwner.NODES)

        assertEquals(0, stops)
    }

    @Test
    fun `освобождение чужим владельцем ничего не меняет`() {
        val arbiter = GeneratorArbiter()
        arbiter.acquire(RunOwner.NODES)
        arbiter.release(RunOwner.SCRIPT)
        assertEquals(RunOwner.NODES, arbiter.owner)
    }

    @Test
    fun `после освобождения владельца нет`() {
        val arbiter = GeneratorArbiter()
        arbiter.acquire(RunOwner.NODES)
        arbiter.release(RunOwner.NODES)
        assertEquals(RunOwner.NONE, arbiter.owner)
    }

    @Test
    fun `незарегистрированный владелец не роняет захват`() {
        val arbiter = GeneratorArbiter()
        arbiter.acquire(RunOwner.SCRIPT)
        arbiter.acquire(RunOwner.NODES)
        assertFalse(arbiter.owner == RunOwner.SCRIPT)
    }
}
```

- [ ] **Step 2: Запустить и убедиться, что падает**

Run: `./gradlew :app:testDebugUnitTest --tests "*GeneratorArbiterTest*"`
Expected: FAIL — `Unresolved reference: GeneratorArbiter`

- [ ] **Step 3: Написать арбитра**

```kotlin
package com.example.generator2.features.nodes

enum class RunOwner { NONE, SCRIPT, NODES }

/**
 * Кто сейчас крутит генератор.
 *
 * Арбитр не знает ни про Script, ни про NodeRunner: каждый владелец сам
 * оставляет здесь обработчик «остановить себя». Иначе получилась бы
 * круговая зависимость — арбитр знает про движки, движки про арбитра.
 */
class GeneratorArbiter {

    private val stoppers = mutableMapOf<RunOwner, () -> Unit>()

    @Volatile
    var owner: RunOwner = RunOwner.NONE
        private set

    fun register(who: RunOwner, stop: () -> Unit) {
        synchronized(this) { stoppers[who] = stop }
    }

    /** Забрать генератор себе, остановив прежнего владельца */
    fun acquire(who: RunOwner) {
        //Обработчик зовём вне synchronized: он останавливает чужой движок,
        //и держать на этом замок арбитра незачем
        val loser = synchronized(this) {
            val previous = owner
            owner = who
            previous.takeIf { it != RunOwner.NONE && it != who }
        }
        loser?.let { stoppers[it]?.invoke() }
    }

    fun release(who: RunOwner) {
        synchronized(this) { if (owner == who) owner = RunOwner.NONE }
    }
}
```

- [ ] **Step 4: Запустить тесты**

Run: `./gradlew :app:testDebugUnitTest --tests "*GeneratorArbiterTest*"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/generator2/features/nodes/GeneratorArbiter.kt app/src/test/java/com/example/generator2/features/nodes/GeneratorArbiterTest.kt
git commit -m "feat(nodes): арбитр генератора — один прогон за раз"
```

---

### Task 12: `NodeRunner` и подключение к DI

**Files:**
- Create: `app/src/main/java/com/example/generator2/features/nodes/NodeRunner.kt`
- Create: `app/src/main/java/com/example/generator2/features/nodes/NodesModule.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/scripting/vm/vmscripting.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/scripting/bottom/BottomAppBarScript.kt`

- [ ] **Step 1: Написать `NodeRunner`**

```kotlin
package com.example.generator2.features.nodes

import com.example.generator2.features.generator.Generator
import com.example.generator2.features.nodes.model.NodeId
import com.example.generator2.features.script.Script
import com.example.generator2.features.script.StateCommandScript
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Прогон графа.
 *
 * Держит собственный экземпляр Script: у синглтона в list и name лежит
 * текстовый скрипт, открытый пользователем на соседнем экране, и затирать
 * его запуском графа нельзя.
 */
class NodeRunner(gen: Generator, private val arbiter: GeneratorArbiter) {

    private val script = Script(gen)

    private var lineToNode: List<NodeId> = emptyList()

    val state: StateCommandScript get() = script.state

    val registers: StateFlow<List<Float>> get() = script.registers

    val pc: StateFlow<Int> get() = script.pc

    /**
     * Нода, чья строка сейчас под pc.
     *
     * На DELAY движок держит pc на самой строке задержки, а она принадлежит
     * своему Шагу — значит нода светится ровно столько, сколько длится пауза.
     */
    val activeNode: Flow<NodeId?> = script.pc.map { lineToNode.getOrNull(it) }

    /** Куда движок пишет сообщения. Экран подставляет сюда свою консоль. */
    var logger: (String) -> Unit
        get() = script.logger
        set(value) {
            script.logger = value
        }

    init {
        arbiter.register(RunOwner.NODES) { script.command(StateCommandScript.STOP) }
    }

    fun start(compiled: CompileResult.Ok) {
        arbiter.acquire(RunOwner.NODES)
        lineToNode = compiled.lineToNode
        script.list.clear()
        compiled.lines.forEach { script.list.add(it) }
        script.command(StateCommandScript.START)
    }

    fun pause() {
        script.command(StateCommandScript.PAUSE)
    }

    fun resume() {
        arbiter.acquire(RunOwner.NODES)
        script.command(StateCommandScript.RESUME)
    }

    fun stop() {
        script.command(StateCommandScript.STOP)
        arbiter.release(RunOwner.NODES)
    }

    /** Нода, которой принадлежит строка — для сообщений об ошибках движка */
    fun nodeOfLine(line: Int): NodeId? = lineToNode.getOrNull(line)
}
```

- [ ] **Step 2: Написать DI-модуль**

```kotlin
package com.example.generator2.features.nodes

import com.example.generator2.AppPath
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.script.Script
import com.example.generator2.features.script.StateCommandScript
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NodesModule {

    /**
     * Обработчик остановки текстового скрипта вешаем здесь, а не в его
     * ScreenModel: Script — синглтон и продолжает крутиться, даже когда
     * экран скриптов закрыт, а ScreenModel к этому времени уже мёртв.
     */
    @Provides
    @Singleton
    fun provideGeneratorArbiter(script: Script): GeneratorArbiter =
        GeneratorArbiter().apply {
            register(RunOwner.SCRIPT) { script.command(StateCommandScript.STOP) }
        }

    @Provides
    @Singleton
    fun provideNodeGraphUtils(appPath: AppPath): NodeGraphUtils = NodeGraphUtils(appPath)

    @Provides
    @Singleton
    fun provideNodeRunner(gen: Generator, arbiter: GeneratorArbiter): NodeRunner =
        NodeRunner(gen, arbiter)
}
```

- [ ] **Step 3: Научить экран скриптов забирать генератор себе**

В `vmscripting.kt` добавить арбитра в конструктор `VMScripting`:

```kotlin
class VMScripting @Inject constructor(
    @ApplicationContext val contextActivity: Context,
    val script: Script,
    val utils: ScriptUtils,
    val keyboard: ScriptKeyboard,
    private val arbiter: GeneratorArbiter,
) : ScreenModel {
```

и добавить импорт:

```kotlin
import com.example.generator2.features.nodes.GeneratorArbiter
import com.example.generator2.features.nodes.RunOwner
```

Рядом с `bEditClick()` добавить:

```kotlin
    /**
     * Запуск скрипта забирает генератор у графа: писать в gen.liveData
     * одновременно с двух прогонов нельзя
     */
    fun bStartClick() {
        arbiter.acquire(RunOwner.SCRIPT)
        script.command(StateCommandScript.START)
    }

    fun bResumeClick() {
        arbiter.acquire(RunOwner.SCRIPT)
        script.command(StateCommandScript.RESUME)
    }
```

- [ ] **Step 4: Перевести кнопки экрана скриптов на новые методы**

В `BottomAppBarScript.kt` заменить:

- `vm.script.command(StateCommandScript.RESUME)` → `vm.bResumeClick()`
- `vm.script.command(StateCommandScript.START)` → `vm.bStartClick()`

Остальные вызовы (`PAUSE`, `STOP`) не трогать: они не начинают прогон.

- [ ] **Step 5: Собрать**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Проверить, что старый экран скриптов не сломался**

Run: `./gradlew :app:installDebug`

На устройстве открыть экран скриптов, запустить любой скрипт, поставить на паузу, снять с паузы, остановить. Поведение должно остаться прежним.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/generator2/features/nodes/NodeRunner.kt app/src/main/java/com/example/generator2/features/nodes/NodesModule.kt app/src/main/java/com/example/generator2/screens/scripting/vm/vmscripting.kt app/src/main/java/com/example/generator2/screens/scripting/bottom/BottomAppBarScript.kt
git commit -m "feat(nodes): NodeRunner со своим движком и подключение к DI"
```

---

## Интерфейс

Дальше автотестов нет: инструментальных тестов в проекте не заведено, и разворачивать стенд ради одного экрана несоразмерно. Вместо «запустить тест» в каждой задаче стоит «собрать и проверить руками» с конкретным списком проверок.

---

### Task 13: Общие диалоги имени файла

`DialogSaveAs` и `DialogDeleteRename` уже принимают только колбэки — они про имя файла, а не про скрипты. Переезд чисто механический: `import` в `DialogSaveAs.kt` на `VMScripting` не используется и уезжает вместе с мусором.

**Files:**
- Move: `screens/scripting/dialog/DialogSaveAs.kt` → `screens/common/dialog/DialogSaveAs.kt`
- Move: `screens/scripting/dialog/DialogDeleteRename.kt` → `screens/common/dialog/DialogDeleteRename.kt`
- Modify: `screens/scripting/ui/ScriptTable.kt:24-25`

- [ ] **Step 1: Перенести файлы**

```bash
git mv app/src/main/java/com/example/generator2/screens/scripting/dialog/DialogSaveAs.kt app/src/main/java/com/example/generator2/screens/common/dialog/DialogSaveAs.kt
git mv app/src/main/java/com/example/generator2/screens/scripting/dialog/DialogDeleteRename.kt app/src/main/java/com/example/generator2/screens/common/dialog/DialogDeleteRename.kt
```

- [ ] **Step 2: Поправить пакет в обоих файлах**

В каждом заменить первую строку на:

```kotlin
package com.example.generator2.screens.common.dialog
```

В `DialogSaveAs.kt` удалить неиспользуемый импорт:

```kotlin
import com.example.generator2.screens.scripting.vm.VMScripting
```

- [ ] **Step 3: Поправить импорты на месте вызова**

В `ScriptTable.kt` заменить строки 24–25 на:

```kotlin
import com.example.generator2.screens.common.dialog.DialogDeleteRename
import com.example.generator2.screens.common.dialog.DialogSaveAs
```

- [ ] **Step 4: Собрать**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Проверить руками**

Run: `./gradlew :app:installDebug`

На экране скриптов: «Сохранить как» открывает диалог со списком имён и сохраняет; переименование и удаление работают как раньше.

- [ ] **Step 6: Commit**

```bash
git add -A app/src/main/java/com/example/generator2/screens
git commit -m "refactor(ui): диалоги имени файла переехали в screens/common/dialog"
```

---

### Task 14: `VMNodes` — состояние графа и правки

**Files:**
- Create: `app/src/main/java/com/example/generator2/screens/nodes/vm/VMNodes.kt`
- Modify: `app/src/main/java/com/example/generator2/di/ScreenModelModule.kt`

- [ ] **Step 1: Написать модель экрана**

```kotlin
package com.example.generator2.screens.nodes.vm

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import com.example.generator2.element.Console2
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.nodes.Issue
import com.example.generator2.features.nodes.NodeGraphUtils
import com.example.generator2.features.nodes.NodeRunner
import com.example.generator2.features.nodes.Severity
import com.example.generator2.features.nodes.model.ChannelParams
import com.example.generator2.features.nodes.model.GraphNode
import com.example.generator2.features.nodes.model.NodeBody
import com.example.generator2.features.nodes.model.NodeGraph
import com.example.generator2.features.nodes.model.NodeId
import com.example.generator2.features.nodes.model.Port
import com.example.generator2.features.nodes.model.RegOp
import com.example.generator2.features.nodes.model.StepParams
import com.example.generator2.features.nodes.model.newGraph
import com.example.generator2.features.nodes.model.nextId
import com.example.generator2.features.nodes.model.node
import com.example.generator2.features.nodes.model.withEdge
import com.example.generator2.features.nodes.model.withNode
import com.example.generator2.features.nodes.model.withoutEdge
import com.example.generator2.features.nodes.model.withoutNode
import com.example.generator2.features.nodes.validate
import com.example.generator2.features.script.CompareOp
import com.example.generator2.features.script.Operand
import javax.inject.Inject

/** Какую ноду создаёт «+» */
enum class NodeKind { STEP, REGISTER, CONDITION, STOP }

@Stable
class VMNodes @Inject constructor(
    private val utils: NodeGraphUtils,
    val runner: NodeRunner,
    private val gen: Generator,
) : ScreenModel {

    var graph by mutableStateOf(newGraph())
        private set

    var name by mutableStateOf(NEW_NAME)
        internal set

    /** Есть несохранённые правки. Взводится и перемещением ноды: координаты в файле. */
    var dirty by mutableStateOf(false)
        internal set

    var selected by mutableStateOf<NodeId?>(null)

    /** Не null — идёт выбор цели для связи из этого порта */
    var linkFrom by mutableStateOf<Pair<NodeId, Port>?>(null)
        private set

    /** Своя консоль: consoleLog экрана скриптов глобальный, логи бы смешались */
    val console = Console2()

    private val issuesState = derivedStateOf { validate(graph, carrierNames(), modNames()) }

    val issues: List<Issue> get() = issuesState.value

    val errors: List<Issue> get() = issues.filter { it.severity == Severity.ERROR }

    val canRun: Boolean get() = errors.isEmpty()

    init {
        runner.logger = { console.println(it) }
    }

    /** Имена форм несущей, известные генератору */
    fun carrierNames(): Set<String> = gen.itemlistCarrier.map { it.name }.toSet()

    /** Имена форм модуляции: у AM и FM список общий */
    fun modNames(): Set<String> = gen.itemlistAM.map { it.name }.toSet()

    //╭─ Правка графа ────────────────────────────────────────────────────╮

    private fun edit(mutate: (NodeGraph) -> NodeGraph) {
        graph = mutate(graph)
        dirty = true
    }

    /**
     * Новая нода встаёт правее выделенной, а если ничего не выделено —
     * правее всех. Холст своих координат наружу не отдаёт, поэтому «в центр
     * экрана» положить нечем, да и предсказуемое место удобнее случайного.
     */
    fun addNode(kind: NodeKind) {
        val anchor = selected?.let { graph.node(it) }
        val x = (anchor?.x ?: graph.nodes.maxOfOrNull { it.x } ?: 0f) + 220f
        val y = anchor?.y ?: graph.nodes.minOfOrNull { it.y } ?: 0f

        val id = graph.nextId()
        edit { it.withNode(GraphNode(id, defaultTitle(kind, id), x, y, defaultBody(kind))) }
        selected = id
    }

    /** Счётчик просьб вписать граф в экран. Холст следит за его сменой. */
    var fitRequest by mutableStateOf(0)
        private set

    fun requestFit() {
        fitRequest++
    }

    fun moveNode(id: NodeId, dx: Float, dy: Float) {
        val node = graph.node(id) ?: return
        edit { it.withNode(node.copy(x = node.x + dx, y = node.y + dy)) }
    }

    fun replaceBody(id: NodeId, body: NodeBody) {
        val node = graph.node(id) ?: return
        edit { it.withNode(node.copy(body = body)) }
    }

    fun rename(id: NodeId, title: String) {
        val node = graph.node(id) ?: return
        edit { it.withNode(node.copy(title = title)) }
    }

    fun deleteSelected() {
        val id = selected ?: return
        edit { it.withoutNode(id) }
        selected = null
    }

    /** Копия тела без связей, со смещением, чтобы не легла ровно поверх оригинала */
    fun duplicateSelected() {
        val source = selected?.let { graph.node(it) } ?: return
        val id = graph.nextId()
        edit {
            it.withNode(source.copy(id = id, x = source.x + 24f, y = source.y + 24f))
        }
        selected = id
    }

    //╭─ Связи ───────────────────────────────────────────────────────────╮

    fun startLink(port: Port) {
        val from = selected ?: return
        linkFrom = from to port
    }

    fun cancelLink() {
        linkFrom = null
    }

    /** В Старт входить нельзя — он и не предлагается как цель */
    fun canBeTarget(id: NodeId): Boolean = graph.node(id)?.body !is NodeBody.Start

    fun completeLink(to: NodeId) {
        val (from, port) = linkFrom ?: return
        if (canBeTarget(to)) edit { it.withEdge(from, port, to) }
        linkFrom = null
    }

    fun unlink(port: Port) {
        val from = selected ?: return
        edit { it.withoutEdge(from, port) }
    }

    //╭─ Загрузка графа целиком ──────────────────────────────────────────╮

    internal fun replaceGraph(value: NodeGraph, graphName: String) {
        graph = value
        name = graphName
        selected = null
        linkFrom = null
        dirty = false
        requestFit()
    }

    private fun defaultBody(kind: NodeKind): NodeBody = when (kind) {
        NodeKind.STEP -> NodeBody.Step(StepParams(ChannelParams(), ChannelParams()), 0L)
        NodeKind.REGISTER -> NodeBody.Register(RegOp.LOAD, 0, Operand.Const(0f))
        NodeKind.CONDITION -> NodeBody.Condition(0, CompareOp.LESS, Operand.Const(0f))
        NodeKind.STOP -> NodeBody.Stop
    }

    private fun defaultTitle(kind: NodeKind, id: NodeId): String = when (kind) {
        NodeKind.STEP -> "Шаг ${id.value}"
        NodeKind.REGISTER -> "Регистр ${id.value}"
        NodeKind.CONDITION -> "Условие ${id.value}"
        NodeKind.STOP -> "Стоп"
    }

    companion object {
        const val NEW_NAME = "New"
    }
}
```

- [ ] **Step 2: Зарегистрировать в Hilt**

В `ScreenModelModule.kt` добавить импорт:

```kotlin
import com.example.generator2.screens.nodes.vm.VMNodes
```

и внутри класса:

```kotlin
    @Binds
    @IntoMap
    @ScreenModelKey(VMNodes::class)
    abstract fun bindVMNodes(screenModel: VMNodes): ScreenModel
```

- [ ] **Step 3: Собрать**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL



- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/generator2/screens/nodes/vm/VMNodes.kt app/src/main/java/com/example/generator2/di/ScreenModelModule.kt
git commit -m "feat(nodes): VMNodes — состояние графа, выделение, режим связи"
```

---

### Task 15: Холст — карточки, рёбра, зум и панорама

**Files:**
- Create: `app/src/main/java/com/example/generator2/screens/nodes/canvas/NodeCard.kt`
- Create: `app/src/main/java/com/example/generator2/screens/nodes/canvas/EdgeLayer.kt`
- Create: `app/src/main/java/com/example/generator2/screens/nodes/canvas/NodeCanvas.kt`
- Delete: `app/src/main/java/com/example/generator2/screens/nodes/SpikeCanvas.kt`

- [ ] **Step 1: Карточка ноды**

```kotlin
package com.example.generator2.screens.nodes.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.features.nodes.model.GraphNode
import com.example.generator2.features.nodes.model.NodeBody
import com.example.generator2.features.nodes.model.RegOp
import com.example.generator2.features.script.toToken

/**
 * Размер карточки фиксирован намеренно: якоря рёбер тогда считаются
 * арифметикой, без onSizeChanged, без карты размеров и без дёрганья
 * рёбер на первом кадре.
 */
val CARD_W = 168.dp
val CARD_H = 72.dp

private val ColorStep = Color(0xFF3A7BD5)
private val ColorRegister = Color(0xFFA06CD5)
private val ColorCondition = Color(0xFFFF9F0A)
private val ColorStart = Color(0xFF34C759)
private val ColorStop = Color(0xFFE5553A)

fun NodeBody.accent(): Color = when (this) {
    is NodeBody.Start -> ColorStart
    is NodeBody.Stop -> ColorStop
    is NodeBody.Step -> ColorStep
    is NodeBody.Register -> ColorRegister
    is NodeBody.Condition -> ColorCondition
}

@Composable
fun NodeCard(
    node: GraphNode,
    isSelected: Boolean,
    isActive: Boolean,
    isDimmed: Boolean,
    modifier: Modifier = Modifier,
) {
    val border = when {
        isActive -> ColorStart
        isSelected -> ColorStep
        else -> Color(0xFF424245)
    }

    Row(
        modifier
            .size(CARD_W, CARD_H)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF2D2D2F).copy(alpha = if (isDimmed) 0.35f else 1f))
            .border(if (isSelected || isActive) 2.dp else 1.dp, border, RoundedCornerShape(10.dp))
    ) {
        Box(
            Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(node.body.accent().copy(alpha = if (isDimmed) 0.35f else 1f))
        )

        Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp).fillMaxWidth()) {
            Text(
                node.title,
                color = Color.White.copy(alpha = if (isDimmed) 0.4f else 1f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            summary(node).forEach {
                Text(
                    it,
                    color = Color(0xFF9A9AA0),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Две строки под именем: что нода сделает. Больше не влезает в 72 dp. */
private fun summary(node: GraphNode): List<String> = when (val b = node.body) {
    is NodeBody.Start -> listOf("вход в граф")
    is NodeBody.Stop -> listOf("конец")

    is NodeBody.Step -> buildList {
        val n = b.params.checkedCount
        add(if (n == 0) "параметры не заданы" else "параметров: $n")
        if (b.delayMs > 0) add("задержка ${b.delayMs} мс")
    }

    is NodeBody.Register -> listOf(
        when (b.op) {
            RegOp.LOAD -> "F${b.dst} = ${b.src.toToken()}"
            RegOp.PLUS -> "F${b.dst} += ${b.src.toToken()}"
            RegOp.MINUS -> "F${b.dst} -= ${b.src.toToken()}"
        }
    )

    is NodeBody.Condition -> listOf(
        "F${b.left} ${b.op.text} ${b.right.toToken()}",
        "да ↗   нет ↘",
    )
}
```

- [ ] **Step 2: Слой рёбер**

```kotlin
package com.example.generator2.screens.nodes.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.generator2.features.nodes.model.GraphNode
import com.example.generator2.features.nodes.model.NodeGraph
import com.example.generator2.features.nodes.model.Port
import com.example.generator2.features.nodes.model.node
import kotlin.math.abs
import kotlin.math.max

private val EdgeColor = Color(0xFF86868B)
private val YesColor = Color(0xFF34C759)
private val NoColor = Color(0xFFE5553A)

/**
 * Рёбра рисуются под карточками в тех же мировых координатах.
 * Слой не обрезается по своим границам, поэтому связь, уходящая за экран,
 * рисуется целиком и не «обрубается» на краю.
 */
@Composable
fun EdgeLayer(graph: NodeGraph, modifier: Modifier = Modifier) {

    val density = LocalDensity.current
    val cardW = with(density) { CARD_W.toPx() }
    val cardH = with(density) { CARD_H.toPx() }

    Canvas(modifier.fillMaxSize()) {

        fun px(node: GraphNode): Offset =
            with(density) { Offset(node.x.dp.toPx(), node.y.dp.toPx()) }

        graph.edges.forEach { edge ->
            val from = graph.node(edge.from) ?: return@forEach
            val to = graph.node(edge.to) ?: return@forEach

            val a = px(from)
            val b = px(to)

            val start = Offset(
                x = a.x + cardW,
                y = a.y + when (edge.port) {
                    Port.OUT -> cardH / 2f
                    Port.YES -> cardH * 0.3f
                    Port.NO -> cardH * 0.7f
                },
            )
            val end = Offset(b.x, b.y + cardH / 2f)

            //Горизонтальные усы: провод выходит вбок, как в node-red,
            //и не превращается в прямую диагональ
            val bend = max(with(density) { 48.dp.toPx() }, abs(end.x - start.x) / 2f)

            val path = Path().apply {
                moveTo(start.x, start.y)
                cubicTo(start.x + bend, start.y, end.x - bend, end.y, end.x, end.y)
            }

            drawPath(
                path = path,
                color = when (edge.port) {
                    Port.OUT -> EdgeColor
                    Port.YES -> YesColor
                    Port.NO -> NoColor
                },
                style = Stroke(width = with(density) { 2.dp.toPx() }),
            )
        }
    }
}
```

- [ ] **Step 3: Холст**

```kotlin
package com.example.generator2.screens.nodes.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.generator2.features.nodes.model.NodeGraph
import com.example.generator2.features.nodes.model.NodeId
import kotlin.math.roundToInt

private const val MIN_SCALE = 0.4f
private const val MAX_SCALE = 2.5f

/**
 * Холст графа.
 *
 * Ноды — обычные composable внутри одного слоя с graphicsLayer.
 * Начало трансформации сдвинуто в левый верхний угол (TransformOrigin(0,0)),
 * поэтому мировая точка p оказывается на экране в p * scale + offset —
 * без этого зум считался бы от центра и вся арифметика была бы вдвое сложнее.
 *
 * @param fitKey смена значения вписывает граф в экран: подаётся имя графа
 * @param onMove дельты перемещения ноды в dp
 */
@Composable
fun NodeCanvas(
    graph: NodeGraph,
    selected: NodeId?,
    activeNode: NodeId?,
    linking: Boolean,
    canBeTarget: (NodeId) -> Boolean,
    fitKey: Any,
    onSelect: (NodeId) -> Unit,
    onTapEmpty: () -> Unit,
    onMove: (NodeId, Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }

    //Вписать граф при открытии: масштаб и смещение в файле не хранятся
    LaunchedEffect(fitKey, viewport) {
        if (viewport.width == 0 || graph.nodes.isEmpty()) return@LaunchedEffect

        val minX = graph.nodes.minOf { it.x }
        val minY = graph.nodes.minOf { it.y }
        val maxX = graph.nodes.maxOf { it.x } + CARD_W.value
        val maxY = graph.nodes.maxOf { it.y } + CARD_H.value

        val margin = with(density) { 24.dp.toPx() }
        val w = with(density) { (maxX - minX).dp.toPx() } + margin * 2
        val h = with(density) { (maxY - minY).dp.toPx() } + margin * 2

        scale = minOf(viewport.width / w, viewport.height / h).coerceIn(MIN_SCALE, 1f)
        offset = Offset(
            x = margin - with(density) { minX.dp.toPx() } * scale,
            y = margin - with(density) { minY.dp.toPx() } * scale,
        )
    }

    Box(
        modifier
            .fillMaxSize()
            .background(Color(0xFF1D1D1F))
            .onSizeChanged { viewport = it }
            .pointerInput(Unit) {
                detectTapGestures { onTapEmpty() }
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val next = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                    //Держим точку под пальцами на месте: она не должна уезжать при зуме
                    offset = centroid + pan - (centroid - offset) * (next / scale)
                    scale = next
                }
            }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                    transformOrigin = TransformOrigin(0f, 0f),
                )
        ) {
            EdgeLayer(graph)

            graph.nodes.forEach { node ->
                val dimmed = linking && !canBeTarget(node.id)

                NodeCard(
                    node = node,
                    isSelected = node.id == selected,
                    isActive = node.id == activeNode,
                    isDimmed = dimmed,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                with(density) { node.x.dp.toPx() }.roundToInt(),
                                with(density) { node.y.dp.toPx() }.roundToInt(),
                            )
                        }
                        .pointerInput(node.id, linking) {
                            detectTapGestures { onSelect(node.id) }
                        }
                        .pointerInput(node.id, linking) {
                            if (linking) return@pointerInput
                            detectDragGestures { change, drag ->
                                change.consume()
                                //Дельта приходит в локальных координатах слоя,
                                //то есть уже без масштаба: делить на scale не надо.
                                //Если на устройстве нода при зуме отстаёт от пальца
                                //или обгоняет его — поделите на scale здесь.
                                onMove(
                                    node.id,
                                    drag.x / density.density,
                                    drag.y / density.density,
                                )
                            }
                        },
                )
            }
        }
    }
}
```

- [ ] **Step 4: Удалить спайк**

```bash
git rm app/src/main/java/com/example/generator2/screens/nodes/SpikeCanvas.kt
```

- [ ] **Step 5: Собрать**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

Экран, который это показывает, появится в следующей задаче — проверять руками пока нечего.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/generator2/screens/nodes/canvas
git commit -m "feat(nodes): холст графа — карточки, рёбра, зум и панорама"
```

---

### Task 16: Экран, навигация, добавление нод

Первая точка, где всё видно на устройстве: холст открывается, ноды добавляются и двигаются.

**Files:**
- Create: `app/src/main/java/com/example/generator2/screens/nodes/ScreenNodes.kt`
- Create: `app/src/main/java/com/example/generator2/screens/nodes/dialog/NodePickerSheet.kt`
- Modify: `app/src/main/java/com/example/generator2/Navigation.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/mainscreen4/mainscreen4.kt:89`
- Modify: `app/src/main/java/com/example/generator2/screens/mainscreen4/bottom/BottomAppBarComponent.kt:38,101`

- [ ] **Step 1: Выбор типа ноды**

```kotlin
package com.example.generator2.screens.nodes.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.screens.nodes.vm.NodeKind

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodePickerSheet(onPick: (NodeKind) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = 32.dp)) {
            listOf(
                NodeKind.STEP to "Шаг — параметры генератора и задержка",
                NodeKind.REGISTER to "Регистр — присвоить, прибавить, вычесть",
                NodeKind.CONDITION to "Условие — два выхода: да и нет",
                NodeKind.STOP to "Стоп — конец прогона",
            ).forEach { (kind, text) ->
                Text(
                    text,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(kind) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                )
            }
        }
    }
}
```

- [ ] **Step 2: Каркас экрана**

Топбар и панель действий появятся в следующих задачах — пока минимум, чтобы было что открыть.

```kotlin
package com.example.generator2.screens.nodes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.generator2.screens.nodes.canvas.NodeCanvas
import com.example.generator2.screens.nodes.dialog.NodePickerSheet
import com.example.generator2.screens.nodes.vm.VMNodes

@Composable
fun ScreenNodes(vm: VMNodes) {

    var pickerOpen by remember { mutableStateOf(false) }
    val activeNode by vm.runner.activeNode.collectAsState(initial = null)

    Scaffold(
        floatingActionButton = {
            if (vm.linkFrom == null) {
                FloatingActionButton(onClick = { pickerOpen = true }) {
                    Text("+", fontSize = 24.sp)
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            NodeCanvas(
                graph = vm.graph,
                selected = vm.selected,
                activeNode = activeNode,
                linking = vm.linkFrom != null,
                canBeTarget = vm::canBeTarget,
                fitKey = vm.fitRequest,
                onSelect = { id ->
                    if (vm.linkFrom != null) vm.completeLink(id) else vm.selected = id
                },
                onTapEmpty = {
                    if (vm.linkFrom != null) vm.cancelLink() else vm.selected = null
                },
                onMove = vm::moveNode,
            )
        }
    }

    if (pickerOpen) {
        NodePickerSheet(
            onPick = {
                vm.addNode(it)
                pickerOpen = false
            },
            onDismiss = { pickerOpen = false },
        )
    }
}
```

- [ ] **Step 3: Экран в навигацию**

В `Navigation.kt` добавить импорты:

```kotlin
import com.example.generator2.screens.nodes.ScreenNodes
import com.example.generator2.screens.nodes.vm.VMNodes
```

и внутри `sealed class AppScreen`:

```kotlin
    data object Nodes : AppScreen() {
        private fun readResolve(): Any = Nodes

        @Composable
        override fun Content() {
            val screenModel: VMNodes = getScreenModel()
            ScreenNodes(screenModel)
        }
    }
```

- [ ] **Step 4: Кнопка входа**

В `BottomAppBarComponent.kt` добавить параметр после `navigateToScript`:

```kotlin
    navigateToNodes:   ()->Unit = {},
```

и рядом с кнопкой скриптов (строка 101) — свою:

```kotlin
        IconButton(modifier = Modifier.testTag("buttonM4GoToNodes"),
            onClick = navigateToNodes ) {
            Icon(painter = painterResource(R.drawable.script3), contentDescription = null)
        }
```

Своей иконки для графа в проекте нет, поэтому пока берём ту же `script3`. Отдельную можно нарисовать позже — на работу это не влияет.

В `mainscreen4.kt` в вызов `M4BottomAppBarComponent` дописать:

```kotlin
                navigateToNodes = { navigator.push(AppScreen.Nodes) }
```

- [ ] **Step 5: Собрать и проверить руками**

Run: `./gradlew :app:installDebug`

На устройстве:
1. Кнопка в нижней панели главного экрана открывает пустой холст: Старт и Стоп, связь между ними.
2. «+» показывает список типов; выбранный тип появляется правее выделенной ноды.
3. Тап по ноде выделяет её — рамка становится толще и синей.
4. Перетаскивание ноды двигает её, связь тянется следом.
5. Щипок масштабирует, точка под пальцами не уезжает.
6. Перетаскивание по пустому месту двигает весь холст.
7. **Отдельно проверить на зуме 50% и 200%:** нода при перетаскивании держится под пальцем. Если отстаёт или обгоняет — в `NodeCanvas` разделить `drag.x` и `drag.y` на `scale`, как написано в комментарии.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/generator2/screens/nodes app/src/main/java/com/example/generator2/Navigation.kt app/src/main/java/com/example/generator2/screens/mainscreen4
git commit -m "feat(nodes): экран графа, навигация и добавление нод"
```

---

### Task 17: Панель действий и режим связи

**Files:**
- Create: `app/src/main/java/com/example/generator2/screens/nodes/bottom/NodeActionBar.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/nodes/ScreenNodes.kt`

- [ ] **Step 1: Панель действий**

```kotlin
package com.example.generator2.screens.nodes.bottom

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.features.nodes.model.GraphNode
import com.example.generator2.features.nodes.model.NodeBody
import com.example.generator2.features.nodes.model.Port
import com.example.generator2.features.nodes.model.target
import com.example.generator2.features.nodes.model.NodeGraph

/**
 * Панель появляется, когда что-то выделено.
 *
 * Тапа по кривой в дизайне нет: правило «один порт — одна связь» означает,
 * что связь однозначно задаётся парой «нода и порт», поэтому и создаётся,
 * и переподключается, и удаляется она отсюда.
 */
@Composable
fun NodeActionBar(
    graph: NodeGraph,
    node: GraphNode,
    onParams: () -> Unit,
    onLink: (Port) -> Unit,
    onUnlink: (Port) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF2D2D2F))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        val hasParams = node.body !is NodeBody.Start && node.body !is NodeBody.Stop
        if (hasParams) Action("⚙ Параметры", onParams)

        when (node.body) {
            is NodeBody.Condition -> {
                LinkAction(graph, node, Port.YES, "да", onLink, onUnlink)
                LinkAction(graph, node, Port.NO, "нет", onLink, onUnlink)
            }

            is NodeBody.Stop -> Unit

            else -> LinkAction(graph, node, Port.OUT, null, onLink, onUnlink)
        }

        if (node.body !is NodeBody.Start) {
            Action("⧉ Дублировать", onDuplicate)
            Action("🗑 Удалить", onDelete, Color(0xFFE5553A))
        }
    }
}

@Composable
private fun LinkAction(
    graph: NodeGraph,
    node: GraphNode,
    port: Port,
    label: String?,
    onLink: (Port) -> Unit,
    onUnlink: (Port) -> Unit,
) {
    val connected = graph.target(node.id, port) != null
    val suffix = label?.let { " «$it»" }.orEmpty()

    Action(if (connected) "→ Переподключить$suffix" else "→ Связь$suffix") { onLink(port) }
    if (connected) Action("✕ Отвязать$suffix") { onUnlink(port) }
}

@Composable
private fun Action(text: String, onClick: () -> Unit, color: Color = Color.White) {
    TextButton(onClick = onClick) {
        Text(text, color = color, fontSize = 13.sp, maxLines = 1)
    }
}
```

- [ ] **Step 2: Подключить панель и подсказку режима связи**

В `ScreenNodes.kt` добавить в `Scaffold` параметр `bottomBar`:

```kotlin
        bottomBar = {
            val node = vm.selected?.let { vm.graph.node(it) }
            if (node != null && vm.linkFrom == null) {
                NodeActionBar(
                    graph = vm.graph,
                    node = node,
                    onParams = { vm.openParams(node.id) },
                    onLink = { vm.startLink(it) },
                    onUnlink = { vm.unlink(it) },
                    onDuplicate = { vm.duplicateSelected() },
                    onDelete = { vm.deleteSelected() },
                )
            }
        },
```

и внутрь `Box` под `NodeCanvas`:

```kotlin
            if (vm.linkFrom != null) {
                Text(
                    "Выберите ноду, куда ведёт связь. Тап по пустому месту — отмена",
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp)
                        .background(Color(0xCC2D2D2F))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
```

Добавить импорты: `androidx.compose.ui.Alignment`, `androidx.compose.ui.graphics.Color`, `androidx.compose.foundation.background`, `androidx.compose.foundation.layout.padding`, `androidx.compose.ui.unit.dp`, `com.example.generator2.features.nodes.model.node`, `com.example.generator2.screens.nodes.bottom.NodeActionBar`.

- [ ] **Step 3: Заглушка открытия параметров**

В `VMNodes` добавить — диалоги придут в задачах 18–20:

```kotlin
    /** Какой ноде открыт диалог параметров */
    var paramsFor by mutableStateOf<NodeId?>(null)
        private set

    fun openParams(id: NodeId) {
        paramsFor = id
    }

    fun closeParams() {
        paramsFor = null
    }
```

- [ ] **Step 4: Собрать и проверить руками**

Run: `./gradlew :app:installDebug`

1. Тап по ноде показывает панель снизу; тап по пустому месту её прячет.
2. У Условия в панели две пары кнопок — «да» и «нет», у Шага одна.
3. «Связь» затемняет Старт (в него входить нельзя) и ждёт выбора; тап по ноде создаёт связь, тап по пустому месту отменяет.
4. Вторая связь из того же порта заменяет первую, а не добавляется рядом.
5. «Отвязать» появляется только у занятого порта и убирает связь.
6. «Дублировать» кладёт копию со смещением и без связей.
7. «Удалить» убирает ноду вместе со всеми её связями.
8. У Старта в панели только «Связь», у Стопа нет «Параметров».

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/generator2/screens/nodes
git commit -m "feat(nodes): панель действий и режим связи в два тапа"
```

---

### Task 18: Поле значения с чипом `123` ⇄ `F`

Каждое числовое поле формы обязано уметь и число, и регистр: `CR1 FR 1000.0` и `CR1 FR F1` — обе легальные строки, и без второй Шаг не умеет свип.

**Files:**
- Create: `app/src/main/java/com/example/generator2/screens/nodes/dialog/OperandField.kt`

- [ ] **Step 1: Написать поле**

```kotlin
package com.example.generator2.screens.nodes.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.features.script.Operand
import com.example.generator2.features.script.REGISTER_COUNT

private val ConstColor = Color(0xFF3A7BD5)
private val RegColor = Color(0xFFA06CD5)

/**
 * Значение параметра: число либо регистр.
 *
 * Чип слева переключает режим. Так видно с одного взгляда, что в поле,
 * и в режиме числа можно показать цифровую клавиатуру — с одним общим
 * текстовым полем пришлось бы держать буквенную ради «F1».
 */
@Composable
fun OperandField(
    value: Operand,
    onChange: (Operand) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isReg = value is Operand.Reg

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {

        Text(
            text = if (isReg) "F" else "123",
            color = if (isReg) Color.White else ConstColor,
            fontSize = 11.sp,
            modifier = Modifier
                .background(
                    if (isReg) RegColor else Color.Transparent,
                    RoundedCornerShape(5.dp),
                )
                .border(1.dp, if (isReg) RegColor else ConstColor, RoundedCornerShape(5.dp))
                .clickable {
                    //Значение при переключении не переносим: число в номер
                    //регистра не превратить осмысленно, и наоборот
                    onChange(if (isReg) Operand.Const(0f) else Operand.Reg(0))
                }
                .padding(horizontal = 6.dp, vertical = 2.dp),
        )

        Box(Modifier.width(8.dp))

        if (isReg) {
            RegisterPicker((value as Operand.Reg).index) { onChange(Operand.Reg(it)) }
        } else {
            ConstInput((value as Operand.Const).value) { onChange(Operand.Const(it)) }
        }
    }
}

@Composable
private fun ConstInput(value: Float, onChange: (Float) -> Unit) {
    //Своя строка нужна, чтобы промежуточные "10." и "-" не затирались
    //обратной конвертацией через Float
    var text by remember(value) { mutableStateOf(value.toString()) }

    BasicTextField(
        value = text,
        onValueChange = {
            text = it
            it.toFloatOrNull()?.let(onChange)
        },
        singleLine = true,
        textStyle = TextStyle(color = Color.White, fontSize = 13.sp, textAlign = TextAlign.End),
        cursorBrush = SolidColor(Color.White),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .width(84.dp)
            .background(Color(0xFF3D3D3F), RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
    )
}

@Composable
private fun RegisterPicker(index: Int, onPick: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }

    Box {
        Text(
            "F$index ▾",
            color = RegColor,
            fontSize = 13.sp,
            modifier = Modifier
                .width(84.dp)
                .background(Color(0xFF3D3D3F), RoundedCornerShape(5.dp))
                .clickable { open = true }
                .padding(horizontal = 6.dp, vertical = 4.dp),
            textAlign = TextAlign.End,
        )

        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            (0 until REGISTER_COUNT).forEach { i ->
                DropdownMenuItem(
                    text = { Text("F$i") },
                    onClick = {
                        onPick(i)
                        open = false
                    },
                )
            }
        }
    }
}

/** Выпадающий список известных генератору форм сигнала */
@Composable
fun WaveformPicker(value: String, names: List<String>, onPick: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }

    Box {
        Text(
            if (value.isEmpty()) "выбрать ▾" else "$value ▾",
            color = Color.White,
            fontSize = 13.sp,
            modifier = Modifier
                .width(120.dp)
                .background(Color(0xFF3D3D3F), RoundedCornerShape(5.dp))
                .clickable { open = true }
                .padding(horizontal = 6.dp, vertical = 4.dp),
            textAlign = TextAlign.End,
        )

        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            names.forEach { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onPick(name)
                        open = false
                    },
                )
            }
        }
    }
}
```

- [ ] **Step 2: Собрать**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/generator2/screens/nodes/dialog/OperandField.kt
git commit -m "feat(nodes): поле значения с переключателем число/регистр"
```

---

### Task 19: Диалог Шага — форма 22 полей

**Files:**
- Create: `app/src/main/java/com/example/generator2/screens/nodes/dialog/StepDialog.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/nodes/ScreenNodes.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/nodes/vm/VMNodes.kt`

- [ ] **Step 1: Написать форму**

```kotlin
package com.example.generator2.screens.nodes.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.generator2.features.nodes.model.ChannelParams
import com.example.generator2.features.nodes.model.NodeBody
import com.example.generator2.features.nodes.model.StepParams
import com.example.generator2.features.script.Operand

/**
 * Диалог ноды Шаг: 22 поля, у каждого галочка «менять».
 * Снятая галочка означает null — параметр не попадёт в скрипт вовсе.
 */
@Composable
fun StepDialog(
    title: String,
    step: NodeBody.Step,
    carrierNames: List<String>,
    modNames: List<String>,
    onSnapshot: () -> StepParams,
    onDone: (title: String, step: NodeBody.Step) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(title) }
    var params by remember { mutableStateOf(step.params) }
    var delay by remember { mutableStateOf(step.delayMs.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .background(Color(0xFF2D2D2F), RoundedCornerShape(14.dp))
                .padding(12.dp)
                .heightIn(max = 560.dp),
        ) {
            BasicTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3D3D3F), RoundedCornerShape(6.dp))
                    .padding(8.dp),
            )

            Column(
                Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
            ) {
                listOf(1 to params.ch1, 2 to params.ch2).forEach { (ch, p) ->

                    val update: (ChannelParams) -> Unit = { updated ->
                        params = if (ch == 1) params.copy(ch1 = updated) else params.copy(ch2 = updated)
                    }

                    Group("CH$ch · Несущая", listOf(p.carrierEnabled, p.carrierFr, p.carrierMod)) {
                        BoolRow("Канал включён", p.carrierEnabled) { update(p.copy(carrierEnabled = it)) }
                        OperandRow("Частота, Гц", p.carrierFr) { update(p.copy(carrierFr = it)) }
                        ModRow("Форма", p.carrierMod, carrierNames) { update(p.copy(carrierMod = it)) }
                    }

                    Group("CH$ch · AM", listOf(p.amEnabled, p.amFr, p.amMod)) {
                        BoolRow("AM включена", p.amEnabled) { update(p.copy(amEnabled = it)) }
                        OperandRow("Частота, Гц", p.amFr) { update(p.copy(amFr = it)) }
                        ModRow("Форма", p.amMod, modNames) { update(p.copy(amMod = it)) }
                    }

                    Group("CH$ch · FM", listOf(p.fmEnabled, p.fmBase, p.fmDev, p.fmFr, p.fmMod)) {
                        BoolRow("FM включена", p.fmEnabled) { update(p.copy(fmEnabled = it)) }
                        OperandRow("Несущая (BASE), Гц", p.fmBase) { update(p.copy(fmBase = it)) }
                        OperandRow("Девиация, Гц", p.fmDev) { update(p.copy(fmDev = it)) }
                        OperandRow("Частота, Гц", p.fmFr) { update(p.copy(fmFr = it)) }
                        ModRow("Форма", p.fmMod, modNames) { update(p.copy(fmMod = it)) }
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Задержка после шага", color = Color.White, fontSize = 14.sp)
                Box(Modifier.weight(1f))
                BasicTextField(
                    value = delay,
                    onValueChange = { delay = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp, textAlign = TextAlign.End),
                    cursorBrush = SolidColor(Color.White),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .width(72.dp)
                        .background(Color(0xFF3D3D3F), RoundedCornerShape(5.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                )
                Text(" мс", color = Color(0xFF9A9AA0), fontSize = 13.sp)
            }

            TextButton(onClick = { params = onSnapshot() }, Modifier.fillMaxWidth()) {
                Text("↓ Снять с генератора", color = Color(0xFF34C759), fontSize = 14.sp)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Отмена", color = Color(0xFF9A9AA0)) }
                TextButton(
                    onClick = {
                        onDone(name, NodeBody.Step(params, delay.toLongOrNull() ?: 0L))
                    }
                ) { Text("Готово", color = Color.White) }
            }
        }
    }
}

/** Заголовок группы со счётчиком отмеченных полей; свёрнута по умолчанию */
@Composable
private fun Group(title: String, fields: List<Any?>, content: @Composable () -> Unit) {
    val checked = fields.count { it != null }
    var expanded by remember { mutableStateOf(checked > 0) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .background(Color(0xFF3D3D3F), RoundedCornerShape(6.dp))
            .clickable { expanded = !expanded }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(if (expanded) "▾" else "▸", color = Color(0xFF9A9AA0), fontSize = 12.sp)
        Text("  $title", color = Color(0xFF3A7BD5), fontSize = 13.sp)
        Box(Modifier.weight(1f))
        Text("$checked из ${fields.size}", color = Color(0xFF9A9AA0), fontSize = 11.sp)
    }

    if (expanded) content()
}

@Composable
private fun FieldRow(label: String, on: Boolean, onToggle: (Boolean) -> Unit, value: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = on,
            onCheckedChange = onToggle,
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3A7BD5)),
        )
        Text(
            label,
            color = if (on) Color.White else Color(0xFF6B6B70),
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
        )
        if (on) value() else Text("—", color = Color(0xFF6B6B70), fontSize = 13.sp)
    }
}

@Composable
private fun BoolRow(label: String, value: Boolean?, onChange: (Boolean?) -> Unit) {
    FieldRow(label, value != null, { onChange(if (it) false else null) }) {
        val on = value == true
        Text(
            if (on) "ON" else "OFF",
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier
                .background(
                    if (on) Color(0xFF34C759) else Color(0xFF6B6B70),
                    RoundedCornerShape(5.dp),
                )
                .clickable { onChange(!on) }
                .padding(horizontal = 10.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun OperandRow(label: String, value: Operand?, onChange: (Operand?) -> Unit) {
    FieldRow(label, value != null, { onChange(if (it) Operand.Const(0f) else null) }) {
        OperandField(value = value!!, onChange = onChange)
    }
}

@Composable
private fun ModRow(label: String, value: String?, names: List<String>, onChange: (String?) -> Unit) {
    FieldRow(label, value != null, { onChange(if (it) names.firstOrNull().orEmpty() else null) }) {
        WaveformPicker(value = value!!, names = names, onPick = onChange)
    }
}
```

- [ ] **Step 2: Научить `VMNodes` снимать состояние с генератора**

Добавить в `VMNodes`:

```kotlin
    /**
     * Текущее состояние генератора в параметры Шага: накрутил на главном
     * экране — зашёл в ноду — нажал. Ставятся все галочки сразу.
     */
    fun snapshotFromGenerator(): StepParams {
        val d = gen.liveData
        return StepParams(
            ch1 = ChannelParams(
                carrierEnabled = d.ch1_EN.value,
                carrierFr = Operand.Const(d.ch1_Carrier_Fr.value),
                carrierMod = d.ch1_Carrier_Filename.value,
                amEnabled = d.ch1_AM_EN.value,
                amFr = Operand.Const(d.ch1_AM_Fr.value),
                amMod = d.ch1_AM_Filename.value,
                fmEnabled = d.ch1_FM_EN.value,
                //BASE и есть частота несущей: движок пишет их в одно поле,
                //поэтому в снимке её не дублируем
                fmBase = null,
                fmDev = Operand.Const(d.ch1_FM_Dev.value),
                fmFr = Operand.Const(d.ch1_FM_Fr.value),
                fmMod = d.ch1_FM_Filename.value,
            ),
            ch2 = ChannelParams(
                carrierEnabled = d.ch2_EN.value,
                carrierFr = Operand.Const(d.ch2_Carrier_Fr.value),
                carrierMod = d.ch2_Carrier_Filename.value,
                amEnabled = d.ch2_AM_EN.value,
                amFr = Operand.Const(d.ch2_AM_Fr.value),
                amMod = d.ch2_AM_Filename.value,
                fmEnabled = d.ch2_FM_EN.value,
                fmBase = null,
                fmDev = Operand.Const(d.ch2_FM_Dev.value),
                fmFr = Operand.Const(d.ch2_FM_Fr.value),
                fmMod = d.ch2_FM_Filename.value,
            ),
        )
    }
```

- [ ] **Step 3: Показать диалог с экрана**

В конец `ScreenNodes` добавить:

```kotlin
    val editing = vm.paramsFor?.let { vm.graph.node(it) }
    if (editing != null && editing.body is NodeBody.Step) {
        StepDialog(
            title = editing.title,
            step = editing.body as NodeBody.Step,
            carrierNames = vm.carrierNames().sorted(),
            modNames = vm.modNames().sorted(),
            onSnapshot = { vm.snapshotFromGenerator() },
            onDone = { newTitle, newStep ->
                vm.rename(editing.id, newTitle)
                vm.replaceBody(editing.id, newStep)
                vm.closeParams()
            },
            onDismiss = { vm.closeParams() },
        )
    }
```

- [ ] **Step 4: Собрать и проверить руками**

Run: `./gradlew :app:installDebug`

1. «Параметры» на Шаге открывают форму; группы с отмеченными полями раскрыты, пустые свёрнуты.
2. Галочка включает поле; счётчик «2 из 3» в заголовке группы меняется.
3. Чип у частоты переключает поле между числом и `F0..F9`.
4. «Форма» предлагает реальные имена: у несущей свой список, у AM и FM общий.
5. «Снять с генератора» заполняет всю форму текущим состоянием.
6. «Готово» закрывает диалог, на карточке меняется счётчик параметров и задержка.
7. «Отмена» ничего не меняет.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/generator2/screens/nodes
git commit -m "feat(nodes): диалог Шага — форма параметров генератора и задержка"
```

---

### Task 20: Диалоги Регистра и Условия

**Files:**
- Create: `app/src/main/java/com/example/generator2/screens/nodes/dialog/RegisterDialog.kt`
- Create: `app/src/main/java/com/example/generator2/screens/nodes/dialog/ConditionDialog.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/nodes/ScreenNodes.kt`

- [ ] **Step 1: Диалог Регистра**

```kotlin
package com.example.generator2.screens.nodes.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.generator2.features.nodes.model.NodeBody
import com.example.generator2.features.nodes.model.RegOp
import com.example.generator2.features.script.REGISTER_COUNT

@Composable
fun RegisterDialog(
    body: NodeBody.Register,
    onDone: (NodeBody.Register) -> Unit,
    onDismiss: () -> Unit,
) {
    var op by remember { mutableStateOf(body.op) }
    var dst by remember { mutableStateOf(body.dst) }
    var src by remember { mutableStateOf(body.src) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .background(Color(0xFF2D2D2F), RoundedCornerShape(14.dp))
                .padding(16.dp),
        ) {
            Text("Регистр", color = Color.White, fontSize = 16.sp)

            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Picker("F$dst", (0 until REGISTER_COUNT).map { "F$it" }) { dst = it }

                Picker(
                    when (op) {
                        RegOp.LOAD -> "="
                        RegOp.PLUS -> "+="
                        RegOp.MINUS -> "-="
                    },
                    listOf("=", "+=", "-="),
                ) { op = RegOp.entries[it] }

                OperandField(value = src, onChange = { src = it })
            }

            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Отмена", color = Color(0xFF9A9AA0)) }
                TextButton(onClick = { onDone(NodeBody.Register(op, dst, src)) }) {
                    Text("Готово", color = Color.White)
                }
            }
        }
    }
}

/** Список коротких вариантов; наружу отдаёт индекс выбранного */
@Composable
internal fun Picker(current: String, options: List<String>, onPick: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }

    Text(
        "$current ▾",
        color = Color.White,
        fontSize = 14.sp,
        modifier = Modifier
            .background(Color(0xFF3D3D3F), RoundedCornerShape(5.dp))
            .clickable { open = true }
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )

    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        options.forEachIndexed { i, text ->
            DropdownMenuItem(
                text = { Text(text) },
                onClick = {
                    onPick(i)
                    open = false
                },
            )
        }
    }
}
```

Порядок `listOf("=", "+=", "-=")` совпадает с порядком `RegOp.entries` — `LOAD`, `PLUS`, `MINUS`. Если в `RegOp` когда-нибудь поменяется порядок констант, сломается именно здесь.

- [ ] **Step 2: Диалог Условия**

```kotlin
package com.example.generator2.screens.nodes.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.generator2.features.nodes.model.NodeBody
import com.example.generator2.features.script.CompareOp
import com.example.generator2.features.script.REGISTER_COUNT

@Composable
fun ConditionDialog(
    body: NodeBody.Condition,
    onDone: (NodeBody.Condition) -> Unit,
    onDismiss: () -> Unit,
) {
    var left by remember { mutableStateOf(body.left) }
    var op by remember { mutableStateOf(body.op) }
    var right by remember { mutableStateOf(body.right) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .background(Color(0xFF2D2D2F), RoundedCornerShape(14.dp))
                .padding(16.dp),
        ) {
            Text("Условие", color = Color.White, fontSize = 16.sp)

            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Picker("F$left", (0 until REGISTER_COUNT).map { "F$it" }) { left = it }
                Picker(op.text, CompareOp.entries.map { it.text }) { op = CompareOp.entries[it] }
                OperandField(value = right, onChange = { right = it })
            }

            Text(
                "Выход «да» — условие верно, «нет» — неверно",
                color = Color(0xFF9A9AA0),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 10.dp),
            )

            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Отмена", color = Color(0xFF9A9AA0)) }
                TextButton(onClick = { onDone(NodeBody.Condition(left, op, right)) }) {
                    Text("Готово", color = Color.White)
                }
            }
        }
    }
}
```

- [ ] **Step 3: Подключить к экрану**

В `ScreenNodes` рядом с показом `StepDialog` добавить две ветки:

```kotlin
    if (editing != null && editing.body is NodeBody.Register) {
        RegisterDialog(
            body = editing.body as NodeBody.Register,
            onDone = {
                vm.replaceBody(editing.id, it)
                vm.closeParams()
            },
            onDismiss = { vm.closeParams() },
        )
    }

    if (editing != null && editing.body is NodeBody.Condition) {
        ConditionDialog(
            body = editing.body as NodeBody.Condition,
            onDone = {
                vm.replaceBody(editing.id, it)
                vm.closeParams()
            },
            onDismiss = { vm.closeParams() },
        )
    }
```

- [ ] **Step 4: Собрать и проверить руками**

Run: `./gradlew :app:installDebug`

1. «Параметры» на Регистре открывают три списка: регистр, операция, значение.
2. Значение переключается между числом и регистром тем же чипом.
3. «Параметры» на Условии дают регистр, сравнение из шести вариантов и значение.
4. На карточках после «Готово» видно новое содержимое: `F1 += 50`, `F1 < 5000`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/generator2/screens/nodes
git commit -m "feat(nodes): диалоги Регистра и Условия"
```

---

### Task 21: Топбар и работа с файлами

**Files:**
- Create: `app/src/main/java/com/example/generator2/screens/nodes/top/NodesTopBar.kt`
- Create: `app/src/main/java/com/example/generator2/screens/nodes/dialog/DialogOpenGraph.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/nodes/vm/VMNodes.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/nodes/ScreenNodes.kt`

- [ ] **Step 1: Файловые операции в `VMNodes`**

Добавить в класс:

```kotlin
    var openDialogSaveAs by mutableStateOf(false)
    var openDialogDeleteRename by mutableStateOf(false)
    var openDialogOpen by mutableStateOf(false)

    /** Действие, которое ждёт ответа «а несохранённое куда?» */
    var pendingDiscard by mutableStateOf<(() -> Unit)?>(null)
        private set

    fun graphNames(): List<String> = utils.list()

    /** Всё, что теряет текущий граф, проходит через это */
    private fun guard(action: () -> Unit) {
        if (dirty) pendingDiscard = action else action()
    }

    fun discardAndRun() {
        val action = pendingDiscard ?: return
        pendingDiscard = null
        action()
    }

    fun cancelDiscard() {
        pendingDiscard = null
    }

    fun newFile() = guard { replaceGraph(newGraph(), NEW_NAME) }

    fun openFile(fileName: String) = guard {
        try {
            replaceGraph(utils.read(fileName), fileName)
        } catch (e: GraphFormatException) {
            //Текущий граф не трогаем: неудачное открытие не должно стирать работу
            SnackBar.error(e.message ?: "Файл $fileName не читается")
        } catch (e: Exception) {
            SnackBar.error("Не удалось открыть $fileName: ${e.message}")
        }
    }

    fun save() {
        if (name == NEW_NAME) {
            openDialogSaveAs = true
            return
        }
        saveAs(name)
    }

    fun saveAs(fileName: String) {
        try {
            utils.save(graph, fileName)
            name = fileName
            dirty = false
            openDialogSaveAs = false
            SnackBar.success("Сохранено")
        } catch (e: Exception) {
            //dirty остаётся взведён: правки не в файле
            SnackBar.error("Не удалось сохранить: ${e.message}")
        }
    }

    fun renameFile(fileName: String) {
        utils.rename(name, fileName)
        name = fileName
        openDialogDeleteRename = false
    }

    fun deleteFile() {
        utils.delete(name)
        openDialogDeleteRename = false
        replaceGraph(newGraph(), NEW_NAME)
    }
```

Импорты: `com.example.generator2.common.snackbar.SnackBar`, `com.example.generator2.features.nodes.model.GraphFormatException`.

- [ ] **Step 2: Диалог открытия**

```kotlin
package com.example.generator2.screens.nodes.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun DialogOpenGraph(names: List<String>, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .background(Color(0xFF2D2D2F), RoundedCornerShape(14.dp))
                .padding(12.dp)
                .heightIn(max = 420.dp),
        ) {
            Text("Открыть граф", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(8.dp))

            if (names.isEmpty()) {
                Text("Сохранённых графов пока нет", color = Color(0xFF9A9AA0), fontSize = 13.sp,
                    modifier = Modifier.padding(8.dp))
            }

            LazyColumn {
                items(names) { name ->
                    Text(
                        name,
                        color = Color.White,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(name) }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 3: Топбар**

Кнопки прогона появятся в следующей задаче — здесь имя, меню файлов и «вписать граф».

```kotlin
package com.example.generator2.screens.nodes.top

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NodesTopBar(
    name: String,
    dirty: Boolean,
    onNew: () -> Unit,
    onOpen: () -> Unit,
    onSave: () -> Unit,
    onSaveAs: () -> Unit,
    onDeleteRename: () -> Unit,
    onFit: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }

    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF2D2D2F))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            if (dirty) "$name •" else name,
            color = Color.White,
            fontSize = 16.sp,
        )

        Box(Modifier.weight(1f))

        Text("⤢", color = Color.White, fontSize = 18.sp, modifier = Modifier.clickable(onClick = onFit).padding(8.dp))

        Box {
            Text("☰", color = Color.White, fontSize = 18.sp,
                modifier = Modifier.clickable { menu = true }.padding(8.dp))

            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                listOf(
                    "Новый" to onNew,
                    "Открыть" to onOpen,
                    "Сохранить" to onSave,
                    "Сохранить как" to onSaveAs,
                    "Переименовать или удалить" to onDeleteRename,
                ).forEach { (text, action) ->
                    DropdownMenuItem(
                        text = { Text(text) },
                        onClick = {
                            menu = false
                            action()
                        },
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 4: Собрать всё вместе на экране**

В `ScreenNodes` добавить в `Scaffold`:

```kotlin
        topBar = {
            NodesTopBar(
                name = vm.name,
                dirty = vm.dirty,
                onNew = { vm.newFile() },
                onOpen = { vm.openDialogOpen = true },
                onSave = { vm.save() },
                onSaveAs = { vm.openDialogSaveAs = true },
                onDeleteRename = { vm.openDialogDeleteRename = true },
                onFit = { vm.requestFit() },
            )
        },
```

и в конец функции — диалоги:

```kotlin
    if (vm.openDialogOpen) {
        DialogOpenGraph(
            names = vm.graphNames(),
            onPick = {
                vm.openDialogOpen = false
                vm.openFile(it)
            },
            onDismiss = { vm.openDialogOpen = false },
        )
    }

    if (vm.openDialogSaveAs) {
        DialogSaveAs(
            onDismissRequest = { vm.openDialogSaveAs = false },
            onDone = { vm.saveAs(it) },
            onScan = { vm.graphNames() },
        )
    }

    if (vm.openDialogDeleteRename) {
        DialogDeleteRename(
            name = vm.name,
            onDone = { vm.renameFile(it) },
            onDismissRequest = { vm.openDialogDeleteRename = false },
            onClickDelete = { vm.deleteFile() },
        )
    }

    if (vm.pendingDiscard != null) {
        AlertDialog(
            onDismissRequest = { vm.cancelDiscard() },
            title = { Text("Граф не сохранён") },
            text = { Text("Правки в «${vm.name}» потеряются") },
            confirmButton = {
                TextButton(onClick = { vm.discardAndRun() }) { Text("Не сохранять") }
            },
            dismissButton = {
                TextButton(onClick = { vm.cancelDiscard() }) { Text("Отмена") }
            },
        )
    }
```

Импорты: `androidx.compose.material3.AlertDialog`, `androidx.compose.material3.TextButton`, `com.example.generator2.screens.common.dialog.DialogSaveAs`, `com.example.generator2.screens.common.dialog.DialogDeleteRename`, `com.example.generator2.screens.nodes.dialog.DialogOpenGraph`, `com.example.generator2.screens.nodes.top.NodesTopBar`.

- [ ] **Step 5: Собрать и проверить руками**

Run: `./gradlew :app:installDebug`

1. Собрать граф из трёх нод, «Сохранить» — спрашивает имя, сохраняет; точка рядом с именем пропадает.
2. «Новый» при несохранённых правках спрашивает; «Отмена» оставляет граф на месте.
3. «Открыть» показывает список; выбранный граф открывается и сам вписывается в экран.
4. Перетащить ноду — точка рядом с именем появляется снова.
5. «Переименовать» меняет имя файла, «Удалить» стирает файл и открывает пустой граф.
6. Файл лежит в `/Gen3/Nodes/<имя>.ng` и читается глазами.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/generator2/screens/nodes
git commit -m "feat(nodes): топбар, файловое меню и защита несохранённого графа"
```

---

### Task 22: Прогон, подсветка активной ноды и список проблем

**Files:**
- Modify: `app/src/main/java/com/example/generator2/screens/nodes/vm/VMNodes.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/nodes/top/NodesTopBar.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/nodes/canvas/NodeCanvas.kt`
- Create: `app/src/main/java/com/example/generator2/screens/nodes/bottom/IssuesSheet.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/nodes/ScreenNodes.kt`

- [ ] **Step 1: Прогон в `VMNodes`**

```kotlin
    var compiled by mutableStateOf<CompileResult.Ok?>(null)
        private set

    /** Нода, на которую просят навести холст, и счётчик просьб */
    var focusNode by mutableStateOf<NodeId?>(null)
        private set

    var focusRequest by mutableStateOf(0)
        private set

    val isRunning: Boolean get() = runner.state == StateCommandScript.ISRUNNING
    val isPaused: Boolean get() = runner.state == StateCommandScript.ISPAUSE

    fun focusOn(id: NodeId) {
        selected = id
        focusNode = id
        focusRequest++
    }

    /**
     * Правка во время прогона запрещена: движок исполняет уже скомпилированные
     * строки, и менять граф под ними бессмысленно — изменения всё равно не
     * доедут до текущего прогона
     */
    fun requireStopped(): Boolean {
        if (isRunning || isPaused) {
            SnackBar.warning("Сначала остановите прогон")
            return false
        }
        return true
    }

    fun run() {
        when (val result = compile(graph, carrierNames(), modNames())) {
            is CompileResult.Ok -> {
                compiled = result
                result.warnings.forEach { console.println("! ${it.text}") }
                console.println("Пуск графа «$name»")
                runner.start(result)
            }

            is CompileResult.Failed -> {
                compiled = null
                result.errors.forEach { console.println(it.text) }
                SnackBar.error(result.errors.first().text)
            }
        }
    }

    fun pauseOrResume() {
        if (isRunning) runner.pause() else runner.resume()
    }

    fun stop() {
        runner.stop()
    }
```

Импорты: `com.example.generator2.features.nodes.CompileResult`, `com.example.generator2.features.nodes.compile`, `com.example.generator2.features.script.StateCommandScript`.

Обернуть правки, которые нельзя делать на ходу:

```kotlin
    fun addNode(kind: NodeKind) {
        if (!requireStopped()) return
        ...
    }

    fun deleteSelected() {
        if (!requireStopped()) return
        ...
    }

    fun duplicateSelected() {
        if (!requireStopped()) return
        ...
    }

    fun openParams(id: NodeId) {
        if (!requireStopped()) return
        paramsFor = id
    }
```

Перемещение ноды и панораму холста не трогаем: смотреть на граф во время прогона надо, а координаты на исполнение не влияют.

- [ ] **Step 2: Наведение холста на ноду**

В `NodeCanvas` добавить параметры:

```kotlin
    focusNode: NodeId?,
    focusKey: Int,
```

и после `LaunchedEffect(fitKey, viewport)` — второй эффект:

```kotlin
    //Навести холст на конкретную ноду: тап по проблеме в списке
    LaunchedEffect(focusKey) {
        val n = focusNode?.let { id -> graph.nodes.firstOrNull { it.id == id } } ?: return@LaunchedEffect
        if (viewport.width == 0) return@LaunchedEffect

        val cx = with(density) { n.x.dp.toPx() } + with(density) { CARD_W.toPx() } / 2f
        val cy = with(density) { n.y.dp.toPx() } + with(density) { CARD_H.toPx() } / 2f

        offset = Offset(viewport.width / 2f - cx * scale, viewport.height / 2f - cy * scale)
    }
```

- [ ] **Step 3: Кнопки прогона и бейдж проблем в топбаре**

Добавить в `NodesTopBar` параметры:

```kotlin
    isRunning: Boolean,
    isPaused: Boolean,
    errorCount: Int,
    warningCount: Int,
    onRun: () -> Unit,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    onShowIssues: () -> Unit,
    onShowScript: () -> Unit,
```

и вставить перед `Box(Modifier.weight(1f))`:

```kotlin
        Box(Modifier.width(12.dp))

        if (errorCount + warningCount > 0) {
            Text(
                if (errorCount > 0) "⛔ $errorCount" else "⚠ $warningCount",
                color = if (errorCount > 0) Color(0xFFE5553A) else Color(0xFFFF9F0A),
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onShowIssues).padding(6.dp),
            )
        }
```

а после имени, перед `⤢`:

```kotlin
        //Пуск неактивен, пока есть ошибки: гонять генератор по половине графа
        //не стоит
        Text(
            "▶",
            color = if (errorCount == 0 && !isRunning) Color(0xFF34C759) else Color(0xFF6B6B70),
            fontSize = 18.sp,
            modifier = Modifier
                .clickable(enabled = errorCount == 0 && !isRunning, onClick = onRun)
                .padding(8.dp),
        )

        Text(
            if (isPaused) "▷" else "❚❚",
            color = if (isRunning || isPaused) Color(0xFFFF9F0A) else Color(0xFF6B6B70),
            fontSize = 16.sp,
            modifier = Modifier
                .clickable(enabled = isRunning || isPaused, onClick = onPauseResume)
                .padding(8.dp),
        )

        Text(
            "■",
            color = if (isRunning || isPaused) Color(0xFFE5553A) else Color(0xFF6B6B70),
            fontSize = 16.sp,
            modifier = Modifier
                .clickable(enabled = isRunning || isPaused, onClick = onStop)
                .padding(8.dp),
        )

        Text(
            "{ }",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.clickable(onClick = onShowScript).padding(8.dp),
        )
```

Импорты: `androidx.compose.foundation.layout.width`.

- [ ] **Step 4: Список проблем**

```kotlin
package com.example.generator2.screens.nodes.bottom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.features.nodes.Issue
import com.example.generator2.features.nodes.Severity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssuesSheet(issues: List<Issue>, onPick: (Issue) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = 32.dp)) {
            Text(
                "Что мешает запуску",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            LazyColumn {
                //Сначала ошибки: они блокируют пуск, предупреждения нет
                items(issues.sortedBy { it.severity != Severity.ERROR }) { issue ->
                    Text(
                        (if (issue.severity == Severity.ERROR) "⛔ " else "⚠ ") + issue.text,
                        color = if (issue.severity == Severity.ERROR) Color(0xFFE5553A) else Color(0xFFFF9F0A),
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(issue) }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 5: Свести на экране**

В `ScreenNodes` добавить состояние `var issuesOpen by remember { mutableStateOf(false) }`, передать в `NodesTopBar` новые параметры (`isRunning = vm.isRunning`, `isPaused = vm.isPaused`, `errorCount = vm.errors.size`, `warningCount = vm.issues.size - vm.errors.size`, `onRun = { vm.run() }`, `onPauseResume = { vm.pauseOrResume() }`, `onStop = { vm.stop() }`, `onShowIssues = { issuesOpen = true }`, `onShowScript = { scriptOpen = true }`), в `NodeCanvas` — `focusNode = vm.focusNode`, `focusKey = vm.focusRequest`, и добавить:

```kotlin
    if (issuesOpen) {
        IssuesSheet(
            issues = vm.issues,
            onPick = { issue ->
                issuesOpen = false
                issue.nodeId?.let { vm.focusOn(it) }
            },
            onDismiss = { issuesOpen = false },
        )
    }
```

Переменную `scriptOpen` объявить сейчас (`var scriptOpen by remember { mutableStateOf(false) }`), пользоваться ею будет Task 24.

- [ ] **Step 6: Собрать и проверить руками**

Run: `./gradlew :app:installDebug`

1. Собрать граф свипа: Старт → Регистр(`F1 = 1000`) → Шаг(`CR1 FR F1`, 200 мс) → Регистр(`F1 += 50`) → Условие(`F1 < 1500`), «да» в Шаг, «нет» в Стоп.
2. Пока у какой-то ноды пустой выход — «▶» серая, рядом бейдж с числом ошибок; тап по проблеме наводит холст на виноватую ноду.
3. Как только ошибок нет — «▶» зелёная. Запуск: активная нода светится зелёной рамкой и переходит по цепочке, частота несущей на главном экране ползёт вверх.
4. Пауза замораживает подсветку, повторное нажатие продолжает, «■» останавливает.
5. Во время прогона «Параметры» и «+» показывают «Сначала остановите прогон».
6. Запустить текстовый скрипт на экране скриптов — прогон графа останавливается сам.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/generator2/screens/nodes
git commit -m "feat(nodes): прогон графа, подсветка активной ноды и список проблем"
```

---

### Task 23: Шторка с регистрами и консолью

**Files:**
- Create: `app/src/main/java/com/example/generator2/screens/nodes/bottom/RunSheet.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/nodes/ScreenNodes.kt`

- [ ] **Step 1: Шторка**

```kotlin
package com.example.generator2.screens.nodes.bottom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.element.Console2
import com.example.generator2.screens.scripting.ui.RegisterViewDraw

/**
 * Свёрнутая шторка — одна строка с занятыми регистрами. Развёрнутая —
 * все регистры и консоль.
 *
 * Разворачивается тапом по полоске, а не свайпом: полоска высотой в одну
 * строку — маленькая цель для перетаскивания, и по той же причине, что
 * и связи в два тапа, надёжнее нажатие.
 */
@Composable
fun RunSheet(registers: List<Float>, console: Console2, modifier: Modifier = Modifier) {

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier
            .fillMaxWidth()
            .background(Color(0xFF2D2D2F)),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val busy = registers.withIndex().filter { it.value != 0f }

            Row(Modifier.weight(1f).horizontalScroll(rememberScrollState())) {
                if (busy.isEmpty()) {
                    Text("регистры пусты", color = Color(0xFF6B6B70), fontSize = 12.sp)
                } else {
                    busy.forEach { (i, v) ->
                        Text(
                            "F$i $v   ",
                            color = Color(0xFFA06CD5),
                            fontSize = 12.sp,
                            maxLines = 1,
                        )
                    }
                }
            }

            Text(
                if (expanded) "консоль ▾" else "консоль ▴",
                color = Color(0xFF9A9AA0),
                fontSize = 12.sp,
            )
        }

        AnimatedVisibility(expanded) {
            Column {
                RegisterViewDraw(registers)
                Box(Modifier.height(180.dp).fillMaxWidth().padding(horizontal = 8.dp)) {
                    console.Draw(Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
```

- [ ] **Step 2: Подключить**

В `ScreenNodes` собрать `bottomBar` из двух частей:

```kotlin
        bottomBar = {
            Column {
                val registers by vm.runner.registers.collectAsState()
                RunSheet(registers = registers, console = vm.console)

                val node = vm.selected?.let { vm.graph.node(it) }
                if (node != null && vm.linkFrom == null) {
                    NodeActionBar(...)   // как в Task 17
                }
            }
        },
```

- [ ] **Step 3: Собрать и проверить руками**

Run: `./gradlew :app:installDebug`

1. Внизу видна полоска: до запуска «регистры пусты».
2. Во время прогона в полоске бегут занятые регистры.
3. Тап по полоске разворачивает регистры целиком и консоль; в консоли видны «Пуск графа», предупреждения и «Скрипт окончен».
4. Повторный тап сворачивает.
5. Лог графа не смешивается с логом экрана скриптов.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/generator2/screens/nodes
git commit -m "feat(nodes): шторка с регистрами и консолью прогона"
```

---

### Task 24: Экран сгенерированного скрипта

Тот самый инструмент отладки компилятора, ради которого и выбрана компиляция.

Отдельным `AppScreen` его не делаем: Voyager выдаёт `ScreenModel` на каждый `Screen` свой, и новый экран получил бы чужой пустой `VMNodes` без скомпилированных строк. Полноэкранный диалог внутри `ScreenNodes` решает это без единой лишней сущности.

**Files:**
- Create: `app/src/main/java/com/example/generator2/screens/nodes/ui/GeneratedScriptView.kt`
- Modify: `app/src/main/java/com/example/generator2/screens/nodes/ScreenNodes.kt`

- [ ] **Step 1: Написать просмотр**

```kotlin
package com.example.generator2.screens.nodes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.generator2.features.nodes.CompileResult
import com.example.generator2.features.nodes.Issue

/**
 * Скомпилированный скрипт только для чтения, с подсветкой текущей строки.
 * Если граф не компилируется — вместо текста список ошибок.
 */
@Composable
fun GeneratedScriptView(
    result: CompileResult,
    pc: Int,
    nodeTitleOfLine: (Int) -> String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF1D1D1F))
                .padding(12.dp),
        ) {
            Row(Modifier.fillMaxWidth()) {
                Text("Сгенерированный скрипт", color = Color.White, fontSize = 16.sp)
                Column(Modifier.weight(1f)) {}
                TextButton(onClick = onDismiss) { Text("Закрыть", color = Color.White) }
            }

            when (result) {
                is CompileResult.Failed -> Errors(result.errors + result.warnings)

                is CompileResult.Ok -> LazyColumn(Modifier.fillMaxSize()) {
                    itemsIndexed(result.lines) { i, line ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(if (i == pc) Color(0xFF2E4A2E) else Color.Transparent)
                                .padding(vertical = 1.dp),
                        ) {
                            Text(
                                i.toString().padStart(3),
                                color = Color(0xFF6B6B70),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                            )
                            Text(
                                "  $line",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                nodeTitleOfLine(i),
                                color = Color(0xFF6B6B70),
                                fontSize = 11.sp,
                                maxLines = 1,
                                modifier = Modifier.width(96.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Errors(issues: List<Issue>) {
    LazyColumn {
        itemsIndexed(issues) { _, issue ->
            Text(
                issue.text,
                color = Color(0xFFE5553A),
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }
    }
}
```

- [ ] **Step 2: Открывать по кнопке `{ }`**

В `ScreenNodes` добавить:

```kotlin
    if (scriptOpen) {
        val pc by vm.runner.pc.collectAsState()
        GeneratedScriptView(
            //Показываем свежую компиляцию: граф мог измениться после прогона
            result = vm.compileNow(),
            pc = pc,
            nodeTitleOfLine = { vm.nodeTitleOfLine(it) },
            onDismiss = { scriptOpen = false },
        )
    }
```

и в `VMNodes`:

```kotlin
    fun compileNow(): CompileResult = compile(graph, carrierNames(), modNames())

    /** Имя ноды, которой принадлежит строка последней компиляции */
    fun nodeTitleOfLine(line: Int): String {
        val id = compiled?.lineToNode?.getOrNull(line) ?: return ""
        return graph.node(id)?.title.orEmpty()
    }
```

- [ ] **Step 3: Собрать и проверить руками**

Run: `./gradlew :app:installDebug`

1. `{ }` показывает строки скрипта с номерами и именем ноды справа.
2. Во время прогона подсвечена текущая строка, и она принадлежит той же ноде, что светится на холсте.
3. На битом графе вместо текста список ошибок.
4. Номера в `GOTO` совпадают с номерами строк слева — переходы ведут туда, куда ведут связи на холсте.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/generator2/screens/nodes
git commit -m "feat(nodes): просмотр сгенерированного скрипта с подсветкой строки"
```

---

## Готово

После Task 24 закрыты все требования спеки. Что стоит прогнать напоследок:

```bash
./gradlew :app:testDebugUnitTest
```

и вручную — сценарий целиком: собрать граф свипа, сохранить, выйти на главный экран, вернуться, открыть сохранённый граф, запустить, убедиться, что генератор звучит и частота ползёт.
