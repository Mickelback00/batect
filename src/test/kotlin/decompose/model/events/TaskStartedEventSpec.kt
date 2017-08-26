package decompose.model.events

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import decompose.BuildImageStep
import decompose.CreateTaskNetworkStep
import decompose.config.Container
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object TaskStartedEventSpec : Spek({
    describe("a 'task started' event") {
        val event = TaskStartedEvent

        on("being applied") {
            val container1 = Container("container-1", "/container-1-build-dir")
            val container2 = Container("container-2", "/container-2-build-dir")

            val context = mock<TaskEventContext> {
                on { allContainers } doReturn setOf(container1, container2)
            }

            event.apply(context)

            it("queues build image steps for each container in the task dependency graph") {
                verify(context).queueStep(BuildImageStep(container1))
                verify(context).queueStep(BuildImageStep(container2))
            }

            it("queues a step to create the task network") {
                verify(context).queueStep(CreateTaskNetworkStep)
            }
        }

        on("toString()") {
            it("returns a human-readable representation of itself") {
                assertThat(event.toString(), equalTo("TaskStartedEvent"))
            }
        }
    }
})
