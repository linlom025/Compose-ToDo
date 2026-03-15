package com.wisnu.kurniawan.composetodolist.features.todo.main.data

import com.wisnu.kurniawan.composetodolist.model.TaskQuadrant
import com.wisnu.kurniawan.composetodolist.model.ToDoColor

data class QuadrantSystemList(
    val quadrant: TaskQuadrant,
    val listId: String,
    val listName: String,
    val color: ToDoColor
)

object QuadrantSystemLists {
    val all = listOf(
        QuadrantSystemList(
            quadrant = TaskQuadrant.Q1,
            listId = "system_quadrant_q1",
            listName = "__quadrant_q1_internal__",
            color = ToDoColor.RED
        ),
        QuadrantSystemList(
            quadrant = TaskQuadrant.Q2,
            listId = "system_quadrant_q2",
            listName = "__quadrant_q2_internal__",
            color = ToDoColor.BLUE
        ),
        QuadrantSystemList(
            quadrant = TaskQuadrant.Q3,
            listId = "system_quadrant_q3",
            listName = "__quadrant_q3_internal__",
            color = ToDoColor.ORANGE
        ),
        QuadrantSystemList(
            quadrant = TaskQuadrant.Q4,
            listId = "system_quadrant_q4",
            listName = "__quadrant_q4_internal__",
            color = ToDoColor.BROWN
        )
    )

    fun listIdOf(quadrant: TaskQuadrant): String {
        return all.first { it.quadrant == quadrant }.listId
    }
}
