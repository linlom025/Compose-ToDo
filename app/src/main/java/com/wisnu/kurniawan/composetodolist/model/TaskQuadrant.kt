package com.wisnu.kurniawan.composetodolist.model

enum class TaskQuadrant(val dbValue: Int) {
    Q1(1),
    Q2(2),
    Q3(3),
    Q4(4);

    companion object {
        private const val DEFAULT_DB_VALUE = 2

        fun fromDbValue(value: Int): TaskQuadrant {
            return entries.firstOrNull { it.dbValue == value } ?: fromDbDefault()
        }

        fun fromDbDefault(): TaskQuadrant {
            return entries.first { it.dbValue == DEFAULT_DB_VALUE }
        }
    }
}
