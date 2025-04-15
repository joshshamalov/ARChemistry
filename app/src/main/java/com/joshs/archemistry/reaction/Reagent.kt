package com.joshs.archemistry.reaction

import java.io.Serializable

/**
 * Represents a chemical reagent.
 */
data class Reagent(
    val name: String,
    val reactionType: String
) : Serializable {
    
    companion object {
        private const val serialVersionUID = 1L
    }
}
