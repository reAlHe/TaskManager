package entities

import java.util.*

/**
 * entities.Process representing a process in an operating system.
 *
 * @property pid the unique process id
 * @property priority
 * @constructor Create empty entities.Process
 */
data class Process(val pid: UUID, val priority: Priority) {

    /**
     * Kills the current process. Please note, that the is no logic associated yet.
     */
    fun kill() {
        // add some logic here
    }
}
