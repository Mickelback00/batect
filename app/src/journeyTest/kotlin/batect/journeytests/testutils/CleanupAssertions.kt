/*
   Copyright 2017-2019 Charles Korn.

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

package batect.journeytests.testutils

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.isEmpty
import org.jetbrains.spek.api.dsl.TestContainer
import org.jetbrains.spek.api.dsl.it

fun TestContainer.itCleansUpAllContainersItCreates(result: ApplicationResult) {
    it("cleans up all containers it creates") {
        assertThat(result.potentiallyOrphanedContainers, isEmpty)
    }
}

fun TestContainer.itCleansUpAllNetworksItCreates(result: ApplicationResult) {
    it("cleans up all networks it creates") {
        assertThat(result.potentiallyOrphanedNetworks, isEmpty)
    }
}
