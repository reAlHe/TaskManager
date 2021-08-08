package services

import entities.Ordering
import entities.Priority
import entities.Process
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

internal class PriorityTaskManagerTest {

    private lateinit var sut: PriorityTaskManager

    private lateinit var processList: MutableList<Process>

    @BeforeEach
    fun setUp() {
        processList = mutableListOf()
        sut = PriorityTaskManager(3, processList)
    }

    @Test
    fun `add process to task manager with sufficient capacity successfully`() {
        val process1 = Process(UUID.randomUUID(), Priority.HIGH)

        sut.add(process1)

        assertThat(processList).containsExactly(process1)
    }

    @Test
    fun `add process to task manager pops out the oldest process with lower priority and adds the new one`() {
        val process1 = spyk(Process(UUID.randomUUID(), Priority.HIGH))
        val process2 = spyk(Process(UUID.randomUUID(), Priority.LOW))
        val process3 = spyk(Process(UUID.randomUUID(), Priority.LOW))
        val process4 = spyk(Process(UUID.randomUUID(), Priority.MEDIUM))

        sut.add(process1)
        sut.add(process2)
        sut.add(process3)
        sut.add(process4)

        assertThat(processList).containsExactly(process1, process3, process4)
        verify(exactly = 0) { process1.kill() }
        verify(exactly = 1) { process2.kill() }
        verify(exactly = 0) { process3.kill() }
        verify(exactly = 0) { process4.kill() }
    }

    @Test
    fun `add process skips new process when no lower prioritized process is found`() {
        val process1 = spyk(Process(UUID.randomUUID(), Priority.HIGH))
        val process2 = spyk(Process(UUID.randomUUID(), Priority.MEDIUM))
        val process3 = spyk(Process(UUID.randomUUID(), Priority.LOW))
        val process4 = spyk(Process(UUID.randomUUID(), Priority.LOW))

        sut.add(process1)
        sut.add(process2)
        sut.add(process3)
        sut.add(process4)

        assertThat(processList).containsExactly(process1, process2, process3)
        verify(exactly = 0) { process1.kill() }
        verify(exactly = 0) { process2.kill() }
        verify(exactly = 0) { process3.kill() }
        verify(exactly = 0) { process4.kill() }
    }

    @Test
    fun `list running processes by pid lists all running processes successfully`() {
        val uuid1 = UUID.fromString("947f6932-0e6b-4211-a8f1-92f1c623308f") // first position uuid
        val uuid2 = UUID.fromString("4b620541-7160-4bc2-9d85-6248d99509da") // second position uuid
        val process1 = Process(uuid1, Priority.HIGH)
        val process2 = Process(uuid2, Priority.MEDIUM)

        sut.add(process1)
        sut.add(process2)

        val result = sut.list(Ordering.PID)

        assertThat(result).containsExactly(process1, process2)
    }

    @Test
    fun `list running processes by creation time lists all running processes successfully`() {
        val uuid1 = UUID.fromString("947f6932-0e6b-4211-a8f1-92f1c623308f") // first position uuid
        val uuid2 = UUID.fromString("4b620541-7160-4bc2-9d85-6248d99509da") // second position uuid
        val process1 = Process(uuid1, Priority.MEDIUM)
        val process2 = Process(uuid2, Priority.HIGH)

        sut.add(process2)
        sut.add(process1)

        val result = sut.list(Ordering.TIME)

        assertThat(result).containsExactly(process2, process1)
    }

    @Test
    fun `list running processes by priority lists all running processes successfully`() {
        val uuid1 = UUID.fromString("947f6932-0e6b-4211-a8f1-92f1c623308f") // first position uuid
        val uuid2 = UUID.fromString("4b620541-7160-4bc2-9d85-6248d99509da") // second position uuid
        val process1 = Process(uuid1, Priority.HIGH)
        val process2 = Process(uuid2, Priority.MEDIUM)

        sut.add(process1)
        sut.add(process2)

        val result = sut.list(Ordering.PRIORITY)

        assertThat(result).containsExactly(process2, process1)
    }

    @Test
    fun `kill specific process successfully`() {
        val process1 = spyk(Process(UUID.randomUUID(), Priority.HIGH))

        sut.add(process1)

        sut.killProcess(process1)
        verify(exactly = 1) { process1.kill() }
        assertThat(processList).isEmpty()
    }

    @Test
    fun `kill specific process fails with NoSuchElementException if no matching process found`() {
        val process1 = spyk(Process(UUID.randomUUID(), Priority.HIGH))
        val process2 = spyk(Process(UUID.randomUUID(), Priority.HIGH))

        sut.add(process1)

        assertThatExceptionOfType(NoSuchElementException::class.java).isThrownBy { sut.killProcess(process2) }
        verify(exactly = 0) { process1.kill() }
        assertThat(processList).containsExactly(process1)
    }

    @Test
    fun `kill all processes with matching priority successfully`() {
        val process1 = spyk(Process(UUID.randomUUID(), Priority.HIGH))
        val process2 = spyk(Process(UUID.randomUUID(), Priority.HIGH))
        val process3 = spyk(Process(UUID.randomUUID(), Priority.MEDIUM))

        sut.add(process1)
        sut.add(process2)
        sut.add(process3)

        sut.killAllProcessesWithPriority(Priority.HIGH)

        verify(exactly = 1) { process1.kill() }
        verify(exactly = 1) { process2.kill() }
        verify(exactly = 0) { process3.kill() }

        assertThat(processList).containsExactly(process3)
    }

    @Test
    fun `kill all processes with matching priority does not fail for no matching processes`() {
        val process1 = spyk(Process(UUID.randomUUID(), Priority.HIGH))
        val process2 = spyk(Process(UUID.randomUUID(), Priority.HIGH))
        val process3 = spyk(Process(UUID.randomUUID(), Priority.MEDIUM))

        sut.add(process1)
        sut.add(process2)
        sut.add(process3)

        sut.killAllProcessesWithPriority(Priority.LOW)

        verify(exactly = 0) { process1.kill() }
        verify(exactly = 0) { process2.kill() }
        verify(exactly = 0) { process3.kill() }

        assertThat(processList).containsExactly(process1, process2, process3)
    }

    @Test
    fun `kill all processes successfully`() {
        val process1 = spyk(Process(UUID.randomUUID(), Priority.HIGH))
        val process2 = spyk(Process(UUID.randomUUID(), Priority.HIGH))
        val process3 = spyk(Process(UUID.randomUUID(), Priority.MEDIUM))

        sut.add(process1)
        sut.add(process2)
        sut.add(process3)

        sut.killAllProcesses()

        verify(exactly = 1) { process1.kill() }
        verify(exactly = 1) { process2.kill() }
        verify(exactly = 1) { process3.kill() }

        assertThat(processList).isEmpty()
    }

    @Test
    fun `kill all processes does not fail for no running processes`() {
        sut.killAllProcesses()

        assertThat(processList).isEmpty()
    }
}