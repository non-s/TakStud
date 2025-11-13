# 🧪 ITEM 12: Aumentar Test Coverage para 70%

**Data:** 14/11/2025
**Status:** EM ANDAMENTO
**Objetivo:** 40% → 70% coverage (Item 8, 9, 10 - critical paths)

---

## 📊 Situação Atual

**Cobertura Atual:** ~40% (140+ testes)
**Target:** 70%
**Gap:** 30% adicional

**Breakdown por módulo:**

| Módulo | Atual | Target | Gap |
|--------|-------|--------|-----|
| Offline Sync (Item 8) | 45% | 80% | +35% |
| Deduplicação (Item 9) | 50% | 85% | +35% |
| Batch Grades (Item 10) | 50% | 85% | +35% |
| Utils | 25% | 75% | +50% |
| **TOTAL** | **40%** | **70%** | **+30%** |

---

## 📋 Testes Necessários

### PRIORIDADE ALTA (182 testes - Critical Paths)

#### 1. AttendanceDeduplicationManager (15 testes)

**Métodos sem cobertura:**
- `performIntegrityCheck()` - 5 testes
  - Detecta duplicatas no banco
  - Remove registros antigos
  - Valida chaves únicas
  - Retorna relatório de saúde
  - Trata erros de DAO

- `validateBatch()` - 8 testes
  - Valida lista inteira
  - Detecta duplicatas na lista
  - Valida formato de cada item
  - Retorna lista de validações
  - Trata items nulos
  - Performance com lista grande

- `generateDeduplicationReport()` - 2 testes
  - Gera relatório formatado
  - Inclui estatísticas corretas

**Caminhos de erro (5 testes adicionais):**
- Mutex contention
- DAO insert failures
- Corrupted data
- Concurrency issues
- Out of memory

---

#### 2. AttendanceDeduplicationIntegration (20 testes)

**Funções untestadas:**
- `saveAttendanceWithDeduplicationAndQueue()` - 8 testes
  - Save success
  - Duplicate detection
  - Queue add failure
  - Concurrent calls
  - Rollback on error

- `processSyncWithDeduplication()` - 8 testes
  - Sync workflow
  - Dedup during sync
  - Error handling
  - Partial failures

- `cleanupDuplicateAttendance()` - 4 testes
  - Clean old records
  - Preserve recent
  - Atomic cleanup

---

#### 3. GradeBatchManager (18 testes)

**Métodos com cobertura parcial:**
- `saveGradesBatch()` - 6 testes adicionais
  - 500+ grades (chunking)
  - Firebase batch limit
  - Partial failures
  - Retry mechanism
  - Timeout handling
  - Large data sets

- `updateGradesBatch()` - 5 testes
  - Update multiple
  - Validate before
  - WriteBatch atomicity
  - Conflict resolution

- `deleteGradesBatch()` - 5 testes
  - Delete with audit
  - Firestore cleanup
  - Cascade rules
  - Recovery on fail

- `curveGrades()` - 2 testes
  - Cap at 100
  - Percentage application

**Caminhos de erro (8 testes):**
- Firebase connection loss
- Partial batch failures
- Invalid grades in batch
- Timeout on commit
- Firestore quota exceeded

---

#### 4. GradeBatchIntegration (20 testes)

**Funções untestadas:**
- `validateAndSaveGradesBatch()` - 8 testes
  - Validation flow
  - Save on valid
  - Queue on success
  - Report generation

- `bulkReleaseWithQueue()` - 8 testes
  - Release workflow
  - Multiple students
  - Queue integration
  - Error handling

- `curveGradesWithQueue()` - 4 testes
  - Apply curve
  - Queue operation
  - Audit logging

---

#### 5. OfflineSyncQueueImpl (22 testes)

**Métodos com cobertura parcial:**
- `syncAll()` - 8 testes
  - Full sync workflow
  - Callback handling
  - Retry on error
  - Partial failures
  - Timeout
  - Max retries exceeded
  - All operation types
  - Concurrent sync

- `getStats()` - 4 testes
  - Calcula estatísticas
  - Contagem correta
  - Timestamp accuracy

- `deserialization` - 6 testes
  - JSON parsing
  - Type conversion
  - Null handling
  - Corrupt data

- `retry mechanism` - 4 testes
  - Backoff calculation
  - Max retries
  - Error state tracking

---

#### 6. ConnectivityMonitorImpl (18 testes)

**Métodos com cobertura parcial:**
- `waitUntilOnline()` - 5 testes
  - Already online (immediate)
  - Waiting for connection
  - Timeout handling
  - Cancellation

- `getNetworkType()` - 7 testes
  - WiFi detection
  - Cellular detection
  - Bluetooth detection
  - No connection
  - API compatibility (pre-24)
  - Null network state

- Network quality (6 testes)
  - WiFi quality
  - Cellular quality
  - Signal strength
  - Bandwidth estimation
  - Stability analysis

---

### PRIORIDADE MÉDIA (125 testes - Edge Cases & Integration)

#### 7. FirestoreFlowHelper (14 testes)

**Funções completamente untestadas:**
- `firestoreCollectionFlow<T>()` - 7 testes
  - Real snapshot listener
  - Multiple emissions
  - Error propagation
  - Cleanup (awaitClose)
  - Type safety
  - Null handling
  - Empty collections

- `firestoreQueryFlow<T>()` - 7 testes
  - Filtered query handling
  - Multiple filters
  - Query ordering
  - Listener lifecycle
  - Error on query fail
  - Large datasets

---

#### 8. AdvancedValidator (18 testes)

**Métodos com cobertura insuficiente:**
- String validation (8 testes)
  - Unicode characters
  - Special characters
  - Accents
  - Emoji
  - Length boundaries
  - Whitespace
  - SQL injection attempts
  - HTML injection attempts

- Date validation (5 testes)
  - Leap years
  - Boundary dates
  - Format variations
  - Timezone handling
  - Past vs future

- Time range validation (5 testes)
  - Valid ranges
  - Overlap detection
  - Edge cases

---

#### 9. AttendanceReportGenerator (25 testes)

**Funções completamente untestadas:**
- `generateAttendanceReport()` - 8 testes
  - Calculate percentages
  - Group by student
  - Date filtering
  - Formatting

- `generateClassStatistics()` - 8 testes
  - Average attendance
  - Min/max
  - Trends
  - Comparative analysis

- `exportToCsv()` - 5 testes
  - CSV formatting
  - Header generation
  - Data escaping
  - File writing

- `detectPatterns()` - 4 testes
  - Absence patterns
  - Anomalies
  - Trends

---

#### 10. GradeBatchOperations (20 testes)

**Funções completamente untestadas:**
- `createBatch()` - 5 testes
  - Empty batch
  - Large batch
  - Null elements
  - Type validation

- `executeBatch()` - 10 testes
  - Batch commit
  - Chunking logic
  - Firestore integration
  - Error handling
  - Rollback
  - Concurrency
  - Network failure
  - Partial failures
  - Retry logic

- `getBatchStatus()` - 5 testes
  - Status tracking
  - Progress reporting

---

#### 11. Integration Scenarios (25 testes)

**Fluxos completos:**
- Attendance Workflow (5 testes)
  1. Save attendance offline
  2. Queue operation
  3. Detect duplicates
  4. Sync to Firestore
  5. Validate result

- Grade Management (5 testes)
  1. Import grades batch
  2. Validate all grades
  3. Queue operations
  4. Sync with dedup
  5. Generate report

- Concurrent Operations (5 testes)
  1. Add while syncing
  2. Update while deleting
  3. Multiple users
  4. Race conditions
  5. Deadlock prevention

- Data Consistency (5 testes)
  1. Before/after sync
  2. Dedup accuracy
  3. Calculation correctness
  4. Audit trail
  5. Recovery

- Offline to Online (5 testes)
  1. No internet save
  2. Reconnect trigger
  3. Batch sync
  4. Error recovery
  5. Final consistency

---

### PRIORIDADE BAIXA (23 testes - Performance & Stress)

#### Performance Tests (12 testes)

- OfflineSyncQueue
  - 1000 item sync: < 5s
  - Memory usage: < 50MB
  - No memory leaks

- GradeBatchManager
  - 500 grade batch: < 2s
  - Chunking efficiency
  - Firestore quota handling

- AttendanceDedup
  - 10000 records: < 3s
  - Lock contention: < 100ms
  - Reflection overhead

#### Stress Tests (11 testes)

- Concurrent operations
  - 100 simultaneous adds
  - 50 concurrent syncs
  - Mixed CRUD operations

- Memory limits
  - Large batches (5000 items)
  - Long running queues
  - Memory pressure scenarios

---

## 🎯 Plano de Implementação

### FASE 1: Testes Críticos (Semana 1)
**Objetivo:** 55-60% coverage

**Testes a adicionar (80 testes):**
1. AttendanceDeduplicationManager (15 testes)
   - Integridade check
   - Batch validation
   - Report generation
   - Error paths

2. GradeBatchManager (18 testes)
   - Chunking (500+ items)
   - Atomicity
   - Error handling
   - Retry logic

3. AttendanceDeduplicationIntegration (20 testes)
   - Full integration
   - Queue integration
   - Error scenarios

4. Error paths and edge cases (27 testes)
   - Mutex contention
   - Database failures
   - Network errors
   - Serialization issues

**Tempo:** ~60 horas
**Resultado:** +15-20% coverage

---

### FASE 2: Integração e Utilities (Semana 2)
**Objetivo:** 65-70% coverage

**Testes a adicionar (90 testes):**
1. OfflineSyncQueueImpl (22 testes)
   - syncAll() workflow
   - Stats calculation
   - Retry mechanism
   - Deserialization

2. GradeBatchIntegration (20 testes)
   - Validate + save
   - Bulk release
   - Grade curve

3. AttendanceReportGenerator (25 testes)
   - Report generation
   - Statistics
   - CSV export
   - Pattern detection

4. ConnectivityMonitorImpl (18 testes)
   - Network detection
   - Quality assessment
   - API compatibility
   - State transitions

5. Integration scenarios (5 testes)
   - Complete workflows
   - Data consistency

**Tempo:** ~70 horas
**Resultado:** +10-15% coverage (total 70%)

---

### FASE 3: Cobertura Máxima (Semana 3)
**Objetivo:** 70%+ coverage

**Testes a adicionar (70 testes):**
1. FirestoreFlowHelper (14 testes)
   - Real listeners
   - Multiple emissions
   - Error handling

2. AdvancedValidator (18 testes)
   - Unicode/accents
   - Boundaries
   - Security checks

3. GradeBatchOperations (20 testes)
   - Batch operations
   - Chunking
   - Firestore integration

4. Performance tests (10 testes)
   - 1000 item sync
   - 500 grade batch
   - 10000 record dedup

5. Stress tests (8 testes)
   - 100 concurrent ops
   - Memory limits

**Tempo:** ~55 horas
**Resultado:** +5-10% coverage (total 75%+)

---

## ✅ Critérios de Sucesso

### Coverage Metrics

```
Target: 70% overall

By module:
✓ AttendanceDeduplicationManager: 85%+
✓ GradeBatchManager: 85%+
✓ OfflineSyncQueueImpl: 80%+
✓ AttendanceDeduplicationIntegration: 80%+
✓ GradeBatchIntegration: 80%+
✓ ConnectivityMonitorImpl: 75%+
✓ FirestoreFlowHelper: 80%+
✓ Utilities: 70%+
```

### Test Quality

- ✓ All tests have Arrange/Act/Assert
- ✓ 100% error paths covered
- ✓ Edge cases identified and tested
- ✓ Mock usage appropriate (no over-mocking)
- ✓ Async tests properly handled (runBlocking/delay)
- ✓ No flaky tests (deterministic)

### Code Quality

- ✓ No warnings in test code
- ✓ Consistent naming convention
- ✓ Documentation comments
- ✓ DRY (no copy-paste)
- ✓ Proper cleanup (runBlocking, mocks, etc)

---

## 📚 Test Template

```kotlin
class FeatureTest {

    private lateinit var mockDependency: Dependency
    private lateinit var sut: SystemUnderTest

    @Before
    fun setUp() {
        mockDependency = mockk()
        sut = SystemUnderTest(mockDependency)
    }

    @Test
    fun `description of what is tested`() = runBlocking {
        // Arrange: Setup test data and mocks
        coEvery { mockDependency.method() } returns expectedValue

        // Act: Execute the code being tested
        val result = sut.methodUnderTest()

        // Assert: Verify the result
        assertEquals(expectedValue, result)
        coVerify { mockDependency.method() }
    }
}
```

---

## 🔧 Dependências de Teste

```gradle
testImplementation "junit:junit:4.13.2"
testImplementation "io.mockk:mockk:1.13.5"
testImplementation "org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version"
testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1"
testImplementation "androidx.arch.core:core-testing:2.2.0"
testImplementation "com.google.firebase:firebase-firestore:24.8.1"
```

---

## 📈 Progresso Esperado

**Semana 1:** ~55-60% coverage (80 testes adicionados)
**Semana 2:** ~65-70% coverage (90 testes adicionados)
**Semana 3:** ~70-75% coverage (70 testes adicionados)

**Total:** 240 testes adicionados
**Esforço:** 185-200 horas (3-4 semanas)

---

## ⚠️ Desafios Esperados

1. **Mocking Firestore:** WriteBatch é complexo
   - Solução: Usar mockk com proper setup

2. **Async/Coroutines:** Timing issues
   - Solução: runBlocking + explicit delays

3. **Network Conditions:** ConnectivityMonitor APIs
   - Solução: Mock NetworkCallback interface

4. **Concurrency:** Mutex e race conditions
   - Solução: Multiple coroutines + Thread.sleep

5. **Large Data:** Performance with big datasets
   - Solução: Use realistic test data, measure

---

## 🎓 Referências

- JUnit 4 docs: https://junit.org/junit4/
- MockK docs: https://mockk.io/
- Kotlin coroutines testing: https://kotlin.github.io/kotlinx.coroutines/
- Firebase emulator: https://firebase.google.com/docs/emulator-suite

---

**Status:** 🚀 Pronto para implementação
**Próximo:** Começar com FASE 1 - AttendanceDeduplicationManager
