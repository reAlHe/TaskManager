package services

import entities.Priority
import entities.Process
import java.util.*

/**
 * entities.Priority task manager. Task manager that brings specific logic for adding process based on the processes' priority.
 *
 * @constructor
 *
 * @property maximumSize the count of processes that can be handled concurrently
 * @property runningProcesses a list containing all managed processes
 */
class PriorityTaskManager(maximumSize: Int, runningProcesses: MutableList<Process> = mutableListOf()) :
    TaskManager(maximumSize, runningProcesses) {

    /**
     * Adds the given process to the list of running processes. In case that the maximal capacity is already reached
     * the oldest and lowest prioritized process, that has a priority lower than the current process, is removed.
     *
     * @param process the process to add
     */
    override fun add(process: Process) {
        if (runningProcesses.size >= maximumSize) {
            val lowerPrioritizedProcess = findOldestAndLowestPrioritizedProcessComparedToCurrent(process.priority)
            lowerPrioritizedProcess.ifPresent { killExistingProcessAndAddNewProcess(it, process) }
        } else {
            runningProcesses.add(process)
        }
    }

    /**
     * Kills the existing process, removes it from the list of managed processes and adds the new process to the list.
     *
     * @param existingProcess the process to be removed from the list of managed processes
     * @param processToInsert the new process
     */
    private fun killExistingProcessAndAddNewProcess(existingProcess: Process, processToInsert: Process) {
        existingProcess.kill()
        runningProcesses.remove(existingProcess)
        runningProcesses.add(processToInsert)
    }

    /**
     * Finds the oldest and lowest prioritized process that has a lower priority than the current one.
     *
     * @param currentPriority the priority of the process
     * @return an Optional containing the matching process, or if no match an empty Optional
     */
    private fun findOldestAndLowestPrioritizedProcessComparedToCurrent(currentPriority: Priority): Optional<Process> {
        return runningProcesses.stream()
            .filter { e -> e.priority.ordinal < currentPriority.ordinal }
            .sorted { e1, e2 -> e1.priority.ordinal.compareTo(e2.priority.ordinal) }
            .findFirst()
    }
}