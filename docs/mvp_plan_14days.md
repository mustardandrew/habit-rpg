# План розробки MVP: 14 днів по 2 години

## Загальна концепція MVP

**Що буде:**
- Один користувач (ти)
- 4 зафіксовані активності
- Базові стати (STR, INT, DEX, END)
- Простий лічильник XP та рівнів
- Кнопки "Виконано" / "Пропущено"
- Мінімальний UI

**Чого НЕ буде:**
- Реєстрації/логіну
- Вибору активностей
- Титулів
- Монстрів
- Деградації (поки)
- Складного дизайну

**Стек:**
- Kotlin
- Android (Jetpack Compose або XML)
- Room Database (SQLite)
- Без backend (все локально)

---

## ТИЖДЕНЬ 1: Фундамент

### День 1: Налаштування проекту (2 год)

**Завдання:**
1. Створити новий Android проект
2. Додати залежності
3. Налаштувати Room Database
4. Створити структуру папок

**Детально:**

```
1. Android Studio → New Project → Empty Activity
   Назва: HabitRPG (або своя)
   Package: com.yourname.habitrpg
   Language: Kotlin
   Minimum SDK: API 26 (Android 8.0)

2. build.gradle.kts (app) - додати:

dependencies {
    // Room
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    ksp("androidx.room:room-compiler:2.6.0")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

3. Структура папок:
app/src/main/java/com/yourname/habitrpg/
├── data/
│   ├── database/
│   ├── entities/
│   └── dao/
├── ui/
│   ├── screens/
│   └── components/
├── viewmodel/
└── MainActivity.kt
```

**Очікуваний результат:**
- ✅ Проект компілюється
- ✅ Структура папок створена
- ✅ Залежності додані

**Що може пійти не так:**
- Помилки компіляції → перевір версії Gradle
- Проси AI: "fix Room compilation error Android"

---

### День 2: База даних - Entities (2 год)

**Завдання:**
Створити основні сутності бази даних

**Файли для створення:**

**1. `data/entities/User.kt`**
```kotlin
package com.yourname.habitrpg.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int = 1, // Завжди 1, бо один користувач
    val username: String = "Hero",
    val overallLevel: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)
```

**2. `data/entities/Stat.kt`**
```kotlin
package com.yourname.habitrpg.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stats")
data class Stat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int = 1,
    val statType: String, // "STR", "INT", "DEX", "END"
    val currentXp: Int = 0,
    val currentLevel: Int = 1
)
```

**3. `data/entities/Activity.kt`**
```kotlin
package com.yourname.habitrpg.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class Activity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val statType: String, // "STR", "INT", "DEX", "END"
    val baseXpReward: Int,
    val isActive: Boolean = true
)
```

**4. `data/entities/ActivityLog.kt`**
```kotlin
package com.yourname.habitrpg.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val activityId: Int,
    val completedAt: Long = System.currentTimeMillis(),
    val xpEarned: Int,
    val wasCompleted: Boolean // true = виконано, false = пропущено
)
```

**Очікуваний результат:**
- ✅ 4 файли створені
- ✅ Entities скомпільовані
- ✅ Немає помилок

**Промпт для AI якщо застряг:**
"Create Room entities for habit tracking app with User, Stat, Activity, ActivityLog"

---

### День 3: База даних - DAO (2 год)

**Завдання:**
Створити Data Access Objects для роботи з базою

**Файли для створення:**

**1. `data/dao/UserDao.kt`**
```kotlin
package com.yourname.habitrpg.data.dao

import androidx.room.*
import com.yourname.habitrpg.data.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 1")
    fun getUser(): Flow<User?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Query("UPDATE users SET overallLevel = :level WHERE id = 1")
    suspend fun updateLevel(level: Int)
}
```

**2. `data/dao/StatDao.kt`**
```kotlin
package com.yourname.habitrpg.data.dao

import androidx.room.*
import com.yourname.habitrpg.data.entities.Stat
import kotlinx.coroutines.flow.Flow

@Dao
interface StatDao {
    @Query("SELECT * FROM stats WHERE userId = 1")
    fun getAllStats(): Flow<List<Stat>>
    
    @Query("SELECT * FROM stats WHERE statType = :type AND userId = 1")
    fun getStatByType(type: String): Flow<Stat?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stats: List<Stat>)
    
    @Query("UPDATE stats SET currentXp = :xp, currentLevel = :level WHERE statType = :type")
    suspend fun updateStat(type: String, xp: Int, level: Int)
}
```

**3. `data/dao/ActivityDao.kt`**
```kotlin
package com.yourname.habitrpg.data.dao

import androidx.room.*
import com.yourname.habitrpg.data.entities.Activity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities WHERE isActive = 1")
    fun getActiveActivities(): Flow<List<Activity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activities: List<Activity>)
    
    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityById(id: Int): Activity?
}
```

**4. `data/dao/ActivityLogDao.kt`**
```kotlin
package com.yourname.habitrpg.data.dao

import androidx.room.*
import com.yourname.habitrpg.data.entities.ActivityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY completedAt DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<ActivityLog>>
    
    @Insert
    suspend fun insert(log: ActivityLog)
    
    @Query("SELECT COUNT(*) FROM activity_logs WHERE activityId = :activityId AND wasCompleted = 1")
    suspend fun getCompletionCount(activityId: Int): Int
}
```

**Очікуваний результат:**
- ✅ 4 DAO створені
- ✅ Компілюється без помилок

---

### День 4: База даних - Database class (2 год)

**Завдання:**
Створити клас бази даних та ініціалізувати початкові дані

**1. `data/database/AppDatabase.kt`**
```kotlin
package com.yourname.habitrpg.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yourname.habitrpg.data.dao.*
import com.yourname.habitrpg.data.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Stat::class, Activity::class, ActivityLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun statDao(): StatDao
    abstract fun activityDao(): ActivityDao
    abstract fun activityLogDao(): ActivityLogDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_rpg_database"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }
    }
}

private suspend fun populateDatabase(db: AppDatabase) {
    val userDao = db.userDao()
    val statDao = db.statDao()
    val activityDao = db.activityDao()
    
    // Створити користувача
    userDao.insertUser(User(id = 1, username = "Hero"))
    
    // Створити стати
    val stats = listOf(
        Stat(userId = 1, statType = "STR", currentXp = 0, currentLevel = 1),
        Stat(userId = 1, statType = "INT", currentXp = 0, currentLevel = 1),
        Stat(userId = 1, statType = "DEX", currentXp = 0, currentLevel = 1),
        Stat(userId = 1, statType = "END", currentXp = 0, currentLevel = 1)
    )
    statDao.insertAll(stats)
    
    // Створити 4 базові активності (твої!)
    val activities = listOf(
        Activity(name = "Віджимання 20 разів", statType = "STR", baseXpReward = 10),
        Activity(name = "Читання 30 хв", statType = "INT", baseXpReward = 15),
        Activity(name = "Програмування 1 год", statType = "DEX", baseXpReward = 20),
        Activity(name = "Ранкова зарядка", statType = "END", baseXpReward = 12)
    )
    activityDao.insertAll(activities)
}
```

**Очікуваний результат:**
- ✅ База даних створюється
- ✅ При першому запуску дані ініціалізуються
- ✅ Можна перевірити через Database Inspector в Android Studio

**Тестування:**
```
Run → Debug → View → Tool Windows → App Inspection → Database Inspector
Переглянь таблиці: users, stats, activities
```

---

### День 5: Repository + ViewModel основа (2 год)

**Завдання:**
Створити Repository для доступу до даних та базовий ViewModel

**1. `data/repository/HabitRepository.kt`**
```kotlin
package com.yourname.habitrpg.data.repository

import com.yourname.habitrpg.data.database.AppDatabase
import com.yourname.habitrpg.data.entities.*
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val database: AppDatabase) {
    
    // User
    fun getUser(): Flow<User?> = database.userDao().getUser()
    
    // Stats
    fun getAllStats(): Flow<List<Stat>> = database.statDao().getAllStats()
    
    fun getStatByType(type: String): Flow<Stat?> = 
        database.statDao().getStatByType(type)
    
    // Activities
    fun getActiveActivities(): Flow<List<Activity>> = 
        database.activityDao().getActiveActivities()
    
    suspend fun getActivityById(id: Int): Activity? = 
        database.activityDao().getActivityById(id)
    
    // Activity Logs
    fun getRecentLogs(): Flow<List<ActivityLog>> = 
        database.activityLogDao().getRecentLogs()
    
    suspend fun logActivity(activityId: Int, xpEarned: Int, wasCompleted: Boolean) {
        val log = ActivityLog(
            activityId = activityId,
            xpEarned = xpEarned,
            wasCompleted = wasCompleted
        )
        database.activityLogDao().insert(log)
    }
    
    // Нарахування XP
    suspend fun addXpToStat(statType: String, xp: Int) {
        val stat = database.statDao().getStatByType(statType)
        // Тут буде логіка розрахунку (додамо пізніше)
    }
}
```

**2. `viewmodel/MainViewModel.kt`**
```kotlin
package com.yourname.habitrpg.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.habitrpg.data.database.AppDatabase
import com.yourname.habitrpg.data.entities.*
import com.yourname.habitrpg.data.repository.HabitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = HabitRepository(database)
    
    // States
    val user = repository.getUser()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    
    val stats = repository.getAllStats()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val activities = repository.getActiveActivities()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Поки пусті функції - заповнимо завтра
    fun completeActivity(activityId: Int) {
        viewModelScope.launch {
            // TODO: Додамо логіку
        }
    }
    
    fun skipActivity(activityId: Int) {
        viewModelScope.launch {
            // TODO: Додамо логіку
        }
    }
}
```

**Очікуваний результат:**
- ✅ Repository створений
- ✅ ViewModel створений
- ✅ Компілюється

---

### День 6: Логіка нарахування XP (2 год)

**Завдання:**
Реалізувати логіку розрахунку XP та рівнів

**1. Додати в `HabitRepository.kt` логіку XP:**

```kotlin
// Додати import
import kotlinx.coroutines.flow.first

// Додати в клас HabitRepository

suspend fun addXpToStat(statType: String, xp: Int): Boolean {
    // Отримати поточну стату
    val currentStat = database.statDao().getStatByType(statType).first()
    
    if (currentStat != null) {
        var newXp = currentStat.currentXp + xp
        var newLevel = currentStat.currentLevel
        var leveledUp = false
        
        // Розрахунок рівня: кожен рівень = 100 XP
        val xpPerLevel = 100
        while (newXp >= xpPerLevel) {
            newXp -= xpPerLevel
            newLevel++
            leveledUp = true
        }
        
        // Оновити в базі
        database.statDao().updateStat(statType, newXp, newLevel)
        
        // Оновити загальний рівень користувача
        updateOverallLevel()
        
        return leveledUp
    }
    return false
}

private suspend fun updateOverallLevel() {
    val stats = database.statDao().getAllStats().first()
    val avgLevel = stats.map { it.currentLevel }.average().toInt()
    database.userDao().updateLevel(avgLevel)
}
```

**2. Оновити `MainViewModel.kt` - додати логіку кнопок:**

```kotlin
fun completeActivity(activityId: Int) {
    viewModelScope.launch {
        // Отримати активність
        val activity = repository.getActivityById(activityId)
        if (activity != null) {
            // Додати XP
            repository.addXpToStat(activity.statType, activity.baseXpReward)
            
            // Залогувати
            repository.logActivity(activityId, activity.baseXpReward, true)
        }
    }
}

fun skipActivity(activityId: Int) {
    viewModelScope.launch {
        // Просто залогувати пропуск (XP не додаємо)
        repository.logActivity(activityId, 0, false)
    }
}
```

**Очікуваний результат:**
- ✅ XP нараховується
- ✅ Рівні підвищуються
- ✅ Записується в лог

**Тест через Database Inspector:**
- Викликати `completeActivity(1)`
- Перевірити що `stats.currentXp` збільшився
- При 100+ XP перевірити що `currentLevel` = 2

---

### День 7: Базовий UI - Екран статів (2 год)

**Завдання:**
Створити простий UI для відображення статів

**`ui/screens/MainScreen.kt`**
```kotlin
package com.yourname.habitrpg.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourname.habitrpg.data.entities.Stat

@Composable
fun MainScreen(
    stats: List<Stat>,
    userName: String,
    userLevel: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок
        Text(
            text = "$userName - Рівень $userLevel",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Стати
        Text("Характеристики:", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        
        stats.forEach { stat ->
            StatCard(stat)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun StatCard(stat: Stat) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = getStatName(stat.statType),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Рівень ${stat.currentLevel}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Прогрес-бар
            LinearProgressIndicator(
                progress = { stat.currentXp / 100f },
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "${stat.currentXp} / 100 XP",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

fun getStatName(type: String): String {
    return when(type) {
        "STR" -> "💪 Сила"
        "INT" -> "🧠 Розум"
        "DEX" -> "⚡ Спритність"
        "END" -> "❤️ Витривалість"
        else -> type
    }
}
```

**Оновити `MainActivity.kt`:**
```kotlin
package com.yourname.habitrpg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourname.habitrpg.ui.screens.MainScreen
import com.yourname.habitrpg.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel = viewModel()
            val user by viewModel.user.collectAsState()
            val stats by viewModel.stats.collectAsState()
            
            MainScreen(
                stats = stats,
                userName = user?.username ?: "Hero",
                userLevel = user?.overallLevel ?: 1
            )
        }
    }
}
```

**Очікуваний результат:**
- ✅ Екран показує ім'я та рівень
- ✅ Відображаються 4 стати з прогрес-барами
- ✅ XP та рівні видимі

**Що може не працювати:**
- Compose не налаштований → додай в `build.gradle.kts`:
```kotlin
buildFeatures {
    compose = true
}
composeOptions {
    kotlinCompilerExtensionVersion = "1.5.3"
}
```

---

## ТИЖДЕНЬ 2: Функціонал

### День 8: UI - Список активностей (2 год)

**Завдання:**
Додати список активностей з кнопками

**Додати в `MainScreen.kt`:**

```kotlin
@Composable
fun MainScreen(
    stats: List<Stat>,
    activities: List<Activity>, // Додано
    userName: String,
    userLevel: Int,
    onCompleteActivity: (Int) -> Unit, // Додано
    onSkipActivity: (Int) -> Unit // Додано
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ... попередній код ...
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Активності
        Text("Сьогоднішні завдання:", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        
        activities.forEach { activity ->
            ActivityCard(
                activity = activity,
                onComplete = { onCompleteActivity(activity.id) },
                onSkip = { onSkipActivity(activity.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ActivityCard(
    activity: Activity,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = activity.name,
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "${getStatName(activity.statType)} • +${activity.baseXpReward} XP",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("✓ Виконано")
                }
                
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("⏭ Пропустити")
                }
            }
        }
    }
}
```

**Оновити `MainActivity.kt`:**
```kotlin
setContent {
    val viewModel: MainViewModel = viewModel()
    val user by viewModel.user.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val activities by viewModel.activities.collectAsState() // Додано
    
    MainScreen(
        stats = stats,
        activities = activities, // Додано
        userName = user?.username ?: "Hero",
        userLevel = user?.overallLevel ?: 1,
        onCompleteActivity = { viewModel.completeActivity(it) }, // Додано
        onSkipActivity = { viewModel.skipActivity(it) } // Додано
    )
}
```

**Очікуваний результат:**
- ✅ Під статами список з 4 активностей
- ✅ Кожна має 2 кнопки
- ✅ Кнопки поки не працюють (логіка завтра)

---

### День 9: Інтеграція - Кнопки працюють! (2 год)

**Завдання:**
Зв'язати кнопки з логікою, додати feedback

**1. Оновити `MainViewModel.kt` - додати Toast/Snackbar:**

```kotlin
// Додати import
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Додати в клас
private val _message = MutableStateFlow<String?>(null)
val message: StateFlow<String?> = _message

fun completeActivity(activityId: Int) {
    viewModelScope.launch {
        val activity = repository.getActivityById(activityId)
        if (activity != null) {
            val leveledUp = repository.addXpToStat(activity.statType, activity.baseXpReward)
            repository.logActivity(activityId, activity.baseXpReward, true)
            
            // Повідомлення
            if (leveledUp) {
                _message.value = "🎉 LEVEL UP! ${getStatName(activity.statType)} підвищено!"
            } else {
                _message.value = "✅ ${activity.name} виконано! +${activity.baseXpReward} XP"
            }
            delay(2000)
            _message.value = null
        }
    }
}

fun skipActivity(activityId: Int) {
    viewModelScope.launch {
        val activity = repository.getActivityById(activityId)
        if (activity != null) {
            repository.logActivity(activityId, 0, false)
            _message.value = "⏭ ${activity.name} пропущено"
            delay(2000)
            _message.value = null
        }
    }
}

private fun getStatName(type: String): String {
    return when(type) {
        "STR" -> "Сила"
        "INT" -> "Розум"
        "DEX" -> "Спритність"
        "END" -> "Витривалість"
        else -> type
    }
}
```

**2. Додати Snackbar в `MainScreen.kt`:**

```kotlin
@Composable
fun MainScreen(
    // ... параметри ...
    message: String? // Додано
) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Показувати Snackbar при зміні message
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // ... весь попередній код ...
        }
    }
}
```

**3. Оновити `MainActivity.kt`:**
```kotlin
val message by viewModel.message.collectAsState()

MainScreen(
    // ... інші параметри ...
    message = message
)
```

**Очікуваний результат:**
- ✅ Натискання "Виконано" → XP додається
- ✅ Прогрес-бар стату оновлюється
- ✅ Показується повідомлення
- ✅ При 100 XP → рівень +1

**Тестування:**
- Натисни "Виконано" на віджиманнях 10 разів
- STR має бути рівень 2 (100 XP досягнуто)

---

### День 10: Покращення UI - Level Up анімація (2 год)

**Завдання:**
Додати помітне сповіщення при підвищенні рівня

**1.Додати в MainViewModel.kt перевірку level up:**
```kotlin
kotlin// Замінити функцію в HabitRepository
suspend fun addXpToStat(statType: String, xp: Int): Boolean {
val currentStat = database.statDao().getStatByType(statType).first()

    if (currentStat != null) {
        var newXp = currentStat.currentXp + xp
        var newLevel = currentStat.currentLevel
        var leveledUp = false
        
        val xpPerLevel = 100
        while (newXp >= xpPerLevel) {
            newXp -= xpPerLevel
            newLevel++
            leveledUp = true
        }

        database.statDao().updateStat(statType, newXp, newLevel)
        updateOverallLevel()

        return leveledUp // Повертаємо чи був level up
    }
    return false
}
```

**2. Оновити `MainViewModel.kt`:**
```kotlin
fun completeActivity(activityId: Int) {
    viewModelScope.launch {
        val activity = repository.getActivityById(activityId)
        if (activity != null) {
            // Перевірити чи був level up
            val leveledUp = repository.addXpToStat(activity.statType, activity.baseXpReward)
            repository.logActivity(activityId, activity.baseXpReward, true)
            
            // Різні повідомлення
            if (leveledUp) {
                _message.value = "🎉 LEVEL UP! ${getStatName(activity.statType)} підвищено!"
            } else {
                _message.value = "✅ ${activity.name} виконано! +${activity.baseXpReward} XP"
            }
            delay(3000)
            _message.value = null
        }
    }
}

private fun getStatName(type: String): String {
    return when(type) {
        "STR" -> "Сила"
        "INT" -> "Розум"
        "DEX" -> "Спритність"
        "END" -> "Витривалість"
        else -> type
    }
}
```

**3. Додати Dialog для Level Up в `MainScreen.kt`:**

```kotlin
@Composable
fun MainScreen(
    // ... параметри ...
) {
    var showLevelUpDialog by remember { mutableStateOf(false) }
    var levelUpStat by remember { mutableStateOf("") }
    
    if (showLevelUpDialog) {
        AlertDialog(
            onDismissRequest = { showLevelUpDialog = false },
            title = { Text("🎉 LEVEL UP!") },
            text = { Text("Твій $levelUpStat став сильнішим!") },
            confirmButton = {
                Button(onClick = { showLevelUpDialog = false }) {
                    Text("Круто!")
                }
            }
        )
    }
    
    // ... решта коду ...
}
```

**Очікуваний результат:**
- ✅ При досягненні 100 XP показується спеціальне повідомлення
- ✅ Level up помітний
- ✅ Мотивує продовжувати

---

### День 11: Історія - Екран логів (2 год)

**Завдання:**
Додати простий екран історії виконань

**1. Створити `ui/screens/HistoryScreen.kt`:**

```kotlin
package com.yourname.habitrpg.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourname.habitrpg.data.entities.ActivityLog
import com.yourname.habitrpg.data.entities.Activity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    logs: List<ActivityLog>,
    activities: List<Activity>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Історія",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (logs.isEmpty()) {
            Text("Поки немає записів")
        } else {
            LazyColumn {
                items(logs) { log ->
                    LogCard(log, activities)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun LogCard(log: ActivityLog, activities: List<Activity>) {
    val activity = activities.find { it.id == log.activityId }
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (log.wasCompleted) 
                MaterialTheme.colorScheme.primaryContainer
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = activity?.name ?: "Невідома активність",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = if (log.wasCompleted) "✓" else "⏭",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Text(
                text = dateFormat.format(Date(log.completedAt)),
                style = MaterialTheme.typography.bodySmall
            )
            
            if (log.wasCompleted) {
                Text(
                    text = "+${log.xpEarned} XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
```

**2. Додати навігацію - створити `ui/Navigation.kt`:**

```kotlin
package com.yourname.habitrpg.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.yourname.habitrpg.ui.screens.HistoryScreen
import com.yourname.habitrpg.ui.screens.MainScreen
import com.yourname.habitrpg.viewmodel.MainViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    
    val user by viewModel.user.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val logs by viewModel.recentLogs.collectAsState()
    val message by viewModel.message.collectAsState()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, "Головна") },
                    label = { Text("Головна") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.List, "Історія") },
                    label = { Text("Історія") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> MainScreen(
                    stats = stats,
                    activities = activities,
                    userName = user?.username ?: "Hero",
                    userLevel = user?.overallLevel ?: 1,
                    onCompleteActivity = { viewModel.completeActivity(it) },
                    onSkipActivity = { viewModel.skipActivity(it) },
                    message = message
                )
                1 -> HistoryScreen(
                    logs = logs,
                    activities = activities
                )
            }
        }
    }
}
```

**3. Додати в `MainViewModel.kt`:**

```kotlin
val recentLogs = repository.getRecentLogs()
    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
```

**4. Оновити `MainActivity.kt`:**

```kotlin
setContent {
    val viewModel: MainViewModel = viewModel()
    AppNavigation(viewModel)
}
```

**Очікуваний результат:**
- ✅ Нижня навігація з 2 вкладками
- ✅ Вкладка "Історія" показує всі дії
- ✅ Зелені карточки = виконано
- ✅ Червоні карточки = пропущено

---

### День 12: Статистика - Базові лічильники (2 год)

**Завдання:**
Додати базову статистику на головний екран

**1. Додати в `ActivityLogDao.kt`:**

```kotlin
@Query("SELECT COUNT(*) FROM activity_logs WHERE wasCompleted = 1 AND completedAt >= :since")
suspend fun getCompletedCountSince(since: Long): Int
```

**2. Додати в `HabitRepository.kt`:**

```kotlin
import java.util.Calendar

suspend fun getTodayCompletedCount(): Int {
    val startOfDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.timeInMillis
    
    return database.activityLogDao().getCompletedCountSince(startOfDay)
}

suspend fun getStreak(): Int {
    // Спрощена версія - порахувати скільки днів підряд є записи
    val logs = database.activityLogDao().getRecentLogs().first()
    
    var streak = 0
    var currentDate = Calendar.getInstance()
    
    for (i in 0..30) { // Перевірити останні 30 днів
        val dayStart = currentDate.timeInMillis
        currentDate.add(Calendar.DAY_OF_MONTH, -1)
        val dayEnd = currentDate.timeInMillis
        
        val hasActivity = logs.any { 
            it.completedAt in dayEnd..dayStart && it.wasCompleted 
        }
        
        if (hasActivity) {
            streak++
        } else {
            break
        }
    }
    
    return streak
}
```

**3. Додати в `MainViewModel.kt`:**

```kotlin
private val _todayCount = MutableStateFlow(0)
val todayCount: StateFlow<Int> = _todayCount

private val _streak = MutableStateFlow(0)
val streak: StateFlow<Int> = _streak

init {
    viewModelScope.launch {
        _todayCount.value = repository.getTodayCompletedCount()
        _streak.value = repository.getStreak()
    }
}

// Оновити після виконання активності
fun completeActivity(activityId: Int) {
    viewModelScope.launch {
        val activity = repository.getActivityById(activityId)
        if (activity != null) {
            val leveledUp = repository.addXpToStat(activity.statType, activity.baseXpReward)
            repository.logActivity(activityId, activity.baseXpReward, true)
            
            // Оновити статистику
            _todayCount.value = repository.getTodayCompletedCount()
            _streak.value = repository.getStreak()
            
            if (leveledUp) {
                _message.value = "🎉 LEVEL UP! ${getStatName(activity.statType)} підвищено!"
            } else {
                _message.value = "✅ ${activity.name} виконано! +${activity.baseXpReward} XP"
            }
            delay(3000)
            _message.value = null
        }
    }
}
```

**4. Додати в `MainScreen.kt` статистику згори:**

```kotlin
@Composable
fun MainScreen(
    stats: List<Stat>,
    activities: List<Activity>,
    userName: String,
    userLevel: Int,
    todayCount: Int, // Додано
    streak: Int, // Додано
    onCompleteActivity: (Int) -> Unit,
    onSkipActivity: (Int) -> Unit,
    message: String?
) {
    // ... код Scaffold ...
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        // Заголовок
        Text(
            text = "$userName - Рівень $userLevel",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // ДОДАТИ СТАТИСТИКУ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatChip(
                label = "Сьогодні",
                value = "$todayCount/${activities.size}",
                modifier = Modifier.weight(1f)
            )
            StatChip(
                label = "Streak",
                value = "$streak 🔥",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // ... решта коду ...
    }
}

@Composable
fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
```

**5. Оновити `AppNavigation.kt`:**

```kotlin
val todayCount by viewModel.todayCount.collectAsState()
val streak by viewModel.streak.collectAsState()

// Передати в MainScreen
MainScreen(
    // ... інші параметри ...
    todayCount = todayCount,
    streak = streak,
    // ...
)
```

**Очікуваний результат:**
- ✅ Зверху показується "Сьогодні 2/4"
- ✅ Показується streak (кількість днів підряд)
- ✅ Після виконання активності лічильники оновлюються

---

### День 13: Поліровка UI (2 год)

**Завдання:**
Покращити дизайн, додати кольори, іконки

**1. Створити `ui/theme/Color.kt`:**

```kotlin
package com.yourname.habitrpg.ui.theme

import androidx.compose.ui.graphics.Color

// Кольори для статів
val StrengthColor = Color(0xFFE53935) // Червоний
val IntelligenceColor = Color(0xFF1E88E5) // Синій
val DexterityColor = Color(0xFF43A047) // Зелений
val EnduranceColor = Color(0xFFFB8C00) // Помаранчевий

fun getStatColor(statType: String): Color {
    return when(statType) {
        "STR" -> StrengthColor
        "INT" -> IntelligenceColor
        "DEX" -> DexterityColor
        "END" -> EnduranceColor
        else -> Color.Gray
    }
}
```

**2. Оновити `StatCard` в `MainScreen.kt`:**

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import com.yourname.habitrpg.ui.theme.getStatColor

@Composable
fun StatCard(stat: Stat) {
    val statColor = getStatColor(stat.statType)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = statColor.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = getStatName(stat.statType),
                    style = MaterialTheme.typography.titleMedium,
                    color = statColor
                )
                Text(
                    text = "Рівень ${stat.currentLevel}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = statColor
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { stat.currentXp / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = statColor,
                trackColor = statColor.copy(alpha = 0.3f)
            )
            
            Text(
                text = "${stat.currentXp} / 100 XP",
                style = MaterialTheme.typography.bodySmall,
                color = statColor.copy(alpha = 0.7f)
            )
        }
    }
}
```

**3. Оновити `ActivityCard`:**

```kotlin
@Composable
fun ActivityCard(
    activity: Activity,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    val statColor = getStatColor(activity.statType)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(statColor, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${getStatName(activity.statType)} • +${activity.baseXpReward} XP",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = statColor
                    )
                ) {
                    Text("✓ Виконано")
                }
                
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("⏭ Пропустити")
                }
            }
        }
    }
}
```

**Очікуваний результат:**
- ✅ Кожен стат має свій колір
- ✅ UI виглядає набагато краще
- ✅ Все зрозуміло та привабливо

---

### День 14: Тестування та фінальні правки (2 год)

**Завдання:**
Протестувати весь функціонал, виправити баги

**Чеклист тестування:**

```
□ Запуск застосунку - стати показуються
□ 4 активності присутні
□ Натискання "Виконано" - XP додається
□ Прогрес-бар оновлюється
□ При 100 XP - рівень підвищується
□ Показується повідомлення Level Up
□ Натискання "Пропустити" - записується в історію
□ Вкладка "Історія" показує всі дії
□ Лічильник "Сьогодні" працює
□ Streak рахується правильно
□ Різні стати мають різні кольори
□ Загальний рівень оновлюється
□ При закритті та відкритті - дані зберігаються
```

**Типові проблеми та рішення:**

**1. XP не додається:**
```
Перевір Database Inspector - чи викликається updateStat
Додай логи: Log.d("MVP", "XP added: $xp")
```

**2. UI не оновлюється:**
```
Перевір чи використовуєш collectAsState()
Перевір чи StateFlow правильно емітить
```

**3. Застосунок крашиться:**
```
Перевір Logcat
Найчастіше: nullable values, suspend функції не в coroutine
```

**Додаткові покращення (якщо залишився час):**

**1. Додати підтвердження при пропуску:**
```kotlin
var showSkipDialog by remember { mutableStateOf(false) }

if (showSkipDialog) {
    AlertDialog(
        onDismissRequest = { showSkipDialog = false },
        title = { Text("Пропустити?") },
        text = { Text("Впевнений що хочеш пропустити цю активність?") },
        confirmButton = {
            Button(onClick = { 
                onSkip()
                showSkipDialog = false 
            }) {
                Text("Так")
            }
        },
        dismissButton = {
            TextButton(onClick = { showSkipDialog = false }) {
                Text("Ні")
            }
        }
    )
}
```

**2. Додати можливість скидання даних (для тестування):**
```kotlin
// В MainActivity додати кнопку в топ-бар
TopAppBar(
    actions = {
        IconButton(onClick = { 
            // Показати діалог підтвердження
            // viewModel.resetAllData()
        }) {
            Icon(Icons.Default.Refresh, "Reset")
        }
    }
)
```

---

## Результат після 14 днів

**Що маєш:**
- ✅ Працюючий застосунок
- ✅ 4 активності
- ✅ Система XP і рівнів
- ✅ Візуалізація прогресу
- ✅ Історія виконань
- ✅ Базова статистика
- ✅ Приємний UI

**Що можна тестувати:**
- Виконувати активності щодня
- Спостерігати за прогресом
- Розуміти що працює, що ні
- Збирати ідеї для покращень

---

## Поради для роботи зі штучним інтелектом

**Добрі промпти:**

```
✅ "Fix Room database compilation error in Android Kotlin"
✅ "How to collect StateFlow in Jetpack Compose"
✅ "Create progress bar with percentage in Compose"
✅ "Calculate level from XP with formula level = xp / 100"

❌ "Зроби щоб все працювало"
❌ "Чому не працює?" (без коду)
```

**Як ефективно працювати:**

1. **Копіюй помилки повністю** - AI краще допоможе
2. **Давай контекст** - покажи весь клас, не лише функцію
3. **Питай крок за кроком** - не "зроби весь застосунок", а "як додати цю одну функцію"
4. **Перевіряй код** - AI може помилятись, розумій що вставляєш

---

## Що далі (після MVP)

**Тиждень 3:**
- День 15-16: Деградація XP
- День 17-18: Streak бонуси
- День 19-20: Перші титули
- День 21: Тестування

**Тиждень 4:**
- Монстри (візуалізація)
- Щоденник нотаток
- Графіки прогресу

**Але це потім. Спочатку зроби MVP і користуйся ним 2 тижні!**

---

## Корисні ресурси

**Документація:**
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

**Інструменти:**
- Database Inspector (Android Studio)
- Logcat для дебагу
- Layout Inspector для UI

**Промпти для AI помічника:**
- "Explain Room database entity relationships"
- "How to debug Jetpack Compose state updates"
- "Best practices for Android ViewModel"

---

## Фінальні поради

1. **Не намагайся зробити все ідеально** - спочатку зроби щоб працювало
2. **Тестуй після кожного дня** - не накопичуй баги
3. **Коміть код щодня** - щоб можна було відкотитись
4. **Роби перерви** - свіжа голова = менше помилок
5. **Використовуй застосунок сам** - найкращий спосіб знайти проблеми

**Головне:** Після 14 днів у тебе буде працюючий застосунок, який ти зможеш використовувати для відстеження своїх звичок. Це вже велике досягнення! 🎉

Все інше - покращення, які додаватимеш поступово, базуючись на реальному досвіді використання.


Тиждень 1. Основи
[x] День 1: Налаштування проекту (2 год)
[ ] День 2: База даних - Entities (2 год)
[ ] День 3: База даних - DAO (2 год)
[ ] День 4: База даних - Database class (2 год)
[ ] День 5: Repository + ViewModel основа (2 год)
[ ] День 6: Логіка нарахування XP (2 год)
[ ] День 7: Базовий UI - Екран статів (2 год)
Тиждень 2. Функціонал
[ ] День 8: UI - Список активностей (2 год)
[ ] День 9: Інтеграція - Кнопки працюють! (2 год)
[ ] День 10: Покращення UI - Level Up анімація (2 год)
[ ] День 11: Історія - Екран логів (2 год)
[ ] День 12: Статистика - Базові лічильники (2 год)
[ ] День 13: Поліровка UI (2 год)
[ ] День 14: Тестування та фінальні правки (2 год)