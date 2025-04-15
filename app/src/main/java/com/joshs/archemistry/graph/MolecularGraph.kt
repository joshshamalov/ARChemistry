package com.joshs.archemistry.graph

import java.io.Serializable
import java.util.ArrayList

/**
 * Represents a molecular graph.
 * This is a simplified implementation that doesn't rely on JGraphT.
 */
class MolecularGraph : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }

    private val atoms = ArrayList<Atom>()
    private val bonds = ArrayList<Bond>()

    /**
     * Adds a vertex (atom) to the graph.
     */
    fun addVertex(atom: Atom) {
        atoms.add(atom)
    }

    /**
     * Adds an edge (bond) between two atoms.
     */
    fun addEdge(source: Atom, target: Atom, order: Int = 1): Bond {
        val bond = Bond(source, target, order)
        bonds.add(bond)
        return bond
    }

    /**
     * Adds a bond between two atoms with the specified order.
     * Alias for addEdge for backward compatibility.
     */
    fun addBond(source: Atom, target: Atom, order: Int = 1): Bond {
        return addEdge(source, target, order)
    }

    /**
     * Gets all vertices (atoms) in the graph.
     */
    fun vertexSet(): List<Atom> {
        return atoms
    }

    /**
     * Gets all edges (bonds) in the graph.
     */
    fun edgeSet(): List<Bond> {
        return bonds
    }

    /**
     * Gets the source atom of a bond.
     */
    fun getEdgeSource(bond: Bond): Atom {
        return bond.source
    }

    /**
     * Gets the target atom of a bond.
     */
    fun getEdgeTarget(bond: Bond): Atom {
        return bond.target
    }

    /**
     * Gets the weight (bond order) of a bond.
     */
    fun getEdgeWeight(bond: Bond): Double {
        return bond.order.toDouble()
    }

    /**
     * Sets the weight (bond order) of a bond.
     */
    fun setEdgeWeight(bond: Bond, weight: Double) {
        bond.order = weight.toInt()
    }

    /**
     * Gets all bonds connected to an atom.
     */
    fun edgesOf(atom: Atom): List<Bond> {
        return bonds.filter { it.source == atom || it.target == atom }
    }

    /**
     * Adds implicit hydrogens to the graph based on atom valences.
     */
    fun addImplicitHydrogens() {
        for (atom in atoms) {
            if (atom.element == "C") {
                // Carbon typically forms 4 bonds
                val currentBonds = edgesOf(atom).sumOf { getEdgeWeight(it).toInt() }
                atom.implicitHydrogens = 4 - currentBonds
            } else if (atom.element == "O") {
                // Oxygen typically forms 2 bonds
                val currentBonds = edgesOf(atom).sumOf { getEdgeWeight(it).toInt() }
                atom.implicitHydrogens = 2 - currentBonds
            } else if (atom.element == "N") {
                // Nitrogen typically forms 3 bonds
                val currentBonds = edgesOf(atom).sumOf { getEdgeWeight(it).toInt() }
                atom.implicitHydrogens = 3 - currentBonds
            }
            // Add more elements as needed
        }
    }

    /**
     * Creates a deep copy of this graph.
     */
    fun clone(): MolecularGraph {
        val clone = MolecularGraph()

        // Map from original atoms to cloned atoms
        val atomMap = mutableMapOf<Atom, Atom>()

        // Clone vertices
        for (atom in atoms) {
            val clonedAtom = atom.clone()
            clone.addVertex(clonedAtom)
            atomMap[atom] = clonedAtom
        }

        // Clone edges
        for (bond in bonds) {
            val source = bond.source
            val target = bond.target
            val weight = bond.order

            val clonedSource = atomMap[source]!!
            val clonedTarget = atomMap[target]!!

            clone.addEdge(clonedSource, clonedTarget, weight)
        }

        return clone
    }
}

/**
 * Represents an atom in a molecular graph.
 */
data class Atom(
    val element: String,
    var x: Float = 0f,
    var y: Float = 0f,
    var x3d: Float = 0f,
    var y3d: Float = 0f,
    var z3d: Float = 0f,
    var implicitHydrogens: Int = 0
) : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }

    /**
     * Creates a deep copy of this atom.
     */
    fun clone(): Atom {
        return Atom(
            element = element,
            x = x,
            y = y,
            x3d = x3d,
            y3d = y3d,
            z3d = z3d,
            implicitHydrogens = implicitHydrogens
        )
    }
}

/**
 * Represents a bond in a molecular graph.
 */
data class Bond(
    val source: Atom,
    val target: Atom,
    var order: Int = 1
) : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }
}
