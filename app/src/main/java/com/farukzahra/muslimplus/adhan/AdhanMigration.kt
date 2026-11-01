package com.farukzahra.muslimplus.adhan

/**
 * Plano puro da migracao da 1.2.0 para a 1.3.0. Ate a 1.2.0 as duas vozes de
 * Madinah com tathwib podiam ser escolhidas como voz global. Agora elas so
 * podem tocar no Fajr.
 *
 * `null` em [globalId], [fajrId] ou [override] significa "nao mexer na chave".
 */
data class AdhanMigration(
    val globalId: String?,
    val fajrId: String?,
    val override: Boolean?,
    val showNotice: Boolean,
)

object AdhanMigrationPlan {
    fun plan(globalId: String?, fajrId: String?): AdhanMigration {
        val global = globalId?.let { id -> AdhanSound.entries.find { it.id == id } }
        val migratingGlobal = global?.fajrOnly == true
        val newGlobal = when {
            migratingGlobal -> AdhanSound.default.id
            globalId != null && global == null -> AdhanSound.default.id
            else -> null
        }

        val storedFajr = fajrId?.let { id -> AdhanSound.entries.find { it.id == id } }
        val newFajr = when {
            migratingGlobal -> global.id
            fajrId != null && storedFajr?.fajrOnly != true -> AdhanSound.defaultFajr.id
            else -> null
        }

        return AdhanMigration(
            globalId = newGlobal,
            fajrId = newFajr,
            override = if (migratingGlobal) true else null,
            showNotice = migratingGlobal,
        )
    }
}
