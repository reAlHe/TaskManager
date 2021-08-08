package services

import entities.Ordering
import entities.Priority
import entities.Process

/**
 * Task manager for managing processes.
 *
 * @property maximumSize the count of processes that can be handled concurrently
 * @property runningProcesses a list containing all managed processes
 * @constructor Create empty Task manager
 */
open class TaskManager(
    protected val maximumSize: Int,
    protected val runningProcesses: MutableList<Process> = mutableListOf()
) {

    /**
     * Adds the given process to the list of running processes. In case that the maximal capacity is already reached
     * a <tt> services.MaximumProcessSizeExceededException <tt> is thrown.
     *
     * @param process the process to add
     */
    open fun add(process: Process) {
        if (runningProcesses.size >= maximumSize) {
            throw MaximumProcessSizeExceededException()
        }
        runningProcesses.add(process)
    }

    /**
     * Sorts the managed processes either by pid, creation time or priority. Please note, that the sorting happens ascending, i.e.
     * starting with the lowest pid, earliest creation time or lowest priority.
     *
     * @param ordering the sorting property to use, i.e. pid, creation time or priority
     * @return a sorted list containing all managed processes
     */
    fun list(ordering: Ordering): Set<Process> {
        return when (ordering) {
            Ordering.PID ->
                runningProcesses.sortedBy { it.pid }.toSet()
            Ordering.PRIORITY ->
                runningProcesses.sortedBy { it.priority.ordinal }.toSet()
            Ordering.TIME ->
                runningProcesses.toSet()
        }
    }

    /**
     * Kills the specific process and removes it from the list of managed processes. Throws a
     * <tt> NoSuchElementException <tt> if the particular process is not managed.
     *
     * @param process the process to kill and remove
     */
    fun killProcess(process: Process) {
        val matchingProcess = runningProcesses.first { e -> e == process }
        matchingProcess.kill()
        runningProcesses.remove(matchingProcess)
    }

    /**
     * Kills all processes with the particular priority and removes them from the list of managed processes. If no process matches the priority, nothing happens.
     *
     * @param priority the priority to remove
     */
    fun killAllProcessesWithPriority(priority: Priority) {
        val matchingProcesses = runningProcesses.filter { e -> e.priority == priority }
        matchingProcesses.onEach { it.kill() }
        runningProcesses.removeAll(matchingProcesses)
    }

    /**
     * Kills all processes and removes them from the list of managed processes.
     */
    fun killAllProcesses() {
        runningProcesses.onEach { it.kill() }
        runningProcesses.clear()
    }
}