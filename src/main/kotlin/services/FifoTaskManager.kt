package services

import entities.Process

/**
 * Fifo task manager. Task manager that brings specific logic for adding process based on a FIFO logic.
 *
 * @constructor
 *
 * @property maximumSize the count of processes that can be handled concurrently
 * @property runningProcesses a list containing all managed processes
 */
class FifoTaskManager(maximumSize: Int, runningProcesses: MutableList<Process> = mutableListOf()) :
    TaskManager(maximumSize, runningProcesses) {

    /**
     * Adds the given process to the list of running processes. In case that the maximal capacity is already reached
     * the oldest process is killed & removed.
     *
     * @param process the process to add
     */
    override fun add(process: Process) {
        if (runningProcesses.size >= maximumSize) {
            runningProcesses[0].kill()
            runningProcesses.removeAt(0)
        }
        runningProcesses.add(process)
    }
}