/*
   Copyright 2017 Charles Korn.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package batect.model.events

import batect.config.BuildImage
import batect.config.Container
import batect.config.PullImage
import batect.docker.DockerImage
import batect.docker.DockerNetwork
import batect.model.steps.CreateContainerStep
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object ImagePulledEventSpec : Spek({
    describe("an 'image pulled' event") {
        val image = DockerImage("image-1")
        val event = ImagePulledEvent(image)

        describe("being applied") {
            on("when the task network has not been created yet") {
                val context = mock<TaskEventContext> {
                    on { getSinglePastEventOfType<TaskNetworkCreatedEvent>() } doReturn null as TaskNetworkCreatedEvent?
                }

                event.apply(context)

                it("does not queue any further work") {
                    verify(context, never()).queueStep(any())
                }
            }

            on("when the task network has already been created") {
                val containerWithImageToBuild = Container("container-1", BuildImage("/container-1-build-dir"))
                val containerWithThisImage1 = Container("container-2", PullImage(image.id))
                val containerWithThisImage2 = Container("container-3", PullImage(image.id))
                val containerWithAnotherImageToPull = Container("container-4", PullImage("other-image"))

                val network = DockerNetwork("the-network")
                val context = mock<TaskEventContext> {
                    on { getSinglePastEventOfType<TaskNetworkCreatedEvent>() } doReturn TaskNetworkCreatedEvent(network)
                    on { commandForContainer(containerWithThisImage1) } doReturn "do-stuff"
                    on { commandForContainer(containerWithThisImage2) } doReturn "do-other-stuff"
                    on { allTaskContainers } doReturn setOf(
                        containerWithImageToBuild, containerWithThisImage1, containerWithThisImage2, containerWithAnotherImageToPull
                    )
                }

                event.apply(context)

                it("queues a 'create container' step for each container that requires the image") {
                    verify(context).queueStep(CreateContainerStep(containerWithThisImage1, "do-stuff", image, network))
                    verify(context).queueStep(CreateContainerStep(containerWithThisImage2, "do-other-stuff", image, network))
                }

                it("does not queue any other work") {
                    verify(context, times(2)).queueStep(any())
                }
            }

            on("when the task is aborting") {
                val context = mock<TaskEventContext> {
                    on { isAborting } doReturn true
                }

                event.apply(context)

                it("does not queue any further work") {
                    verify(context, never()).queueStep(any())
                }
            }
        }

        on("toString()") {
            it("returns a human-readable representation of itself") {
                com.natpryce.hamkrest.assertion.assertThat(event.toString(), equalTo("ImagePulledEvent(image: 'image-1')"))
            }
        }
    }
})