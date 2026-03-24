package com.wisnu.kurniawan.composetodolist.model

data class AppDisplayNameConfig(
    val appTitle: String,
    val quadrantTitles: QuadrantDisplayNames
) {
    companion object {
        fun default(): AppDisplayNameConfig {
            return AppDisplayNameConfig(
                appTitle = DEFAULT_APP_TITLE,
                quadrantTitles = QuadrantDisplayNames.default()
            )
        }
    }
}

data class QuadrantDisplayNames(
    val q1: String,
    val q2: String,
    val q3: String,
    val q4: String
) {
    fun titleOf(quadrant: TaskQuadrant): String {
        return when (quadrant) {
            TaskQuadrant.Q1 -> q1
            TaskQuadrant.Q2 -> q2
            TaskQuadrant.Q3 -> q3
            TaskQuadrant.Q4 -> q4
        }
    }

    companion object {
        fun default(): QuadrantDisplayNames {
            return QuadrantDisplayNames(
                q1 = DEFAULT_Q1_TITLE,
                q2 = DEFAULT_Q2_TITLE,
                q3 = DEFAULT_Q3_TITLE,
                q4 = DEFAULT_Q4_TITLE
            )
        }
    }
}

const val DEFAULT_APP_TITLE = "ll-todo"
const val DEFAULT_Q1_TITLE = "重要且紧急"
const val DEFAULT_Q2_TITLE = "重要不紧急"
const val DEFAULT_Q3_TITLE = "不重要但紧急"
const val DEFAULT_Q4_TITLE = "不重要不紧急"
