package insulator.viewmodel.main.topic

import arrow.core.left
import arrow.core.right
import helper.FxContext
import insulator.lib.kafka.AdminApi
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.util.concurrent.CompletableFuture

class ListTopicViewModelTest : StringSpec({

    val errorMessage = "Example error"

    "topicList error on loading the list of topics" {
        FxContext().use {
            // arrange
            val adminApi = mockk<AdminApi> {
                every { listTopics() } returns CompletableFuture.completedFuture(Throwable(errorMessage).left())
            }
            val sut = ListTopicViewModel(it.cluster, adminApi, mockk(), mockk())
            // act
            val res = sut.filteredTopicsProperty
            // assert
            sut.refresh().get() // force and wait refresh
            res.size shouldBe 0
            sut.error.value!!.message shouldBe errorMessage
        }
    }

    "happy path" {
        FxContext().use {
            // arrange
            val adminApi = mockk<AdminApi> {
                every { listTopics() } returns CompletableFuture.completedFuture(listOf("tppic1", "topic2").right())
            }
            val sut = ListTopicViewModel(it.cluster, adminApi, mockk(), mockk())
            // act
            val res = sut.filteredTopicsProperty
            // assert
            sut.refresh().get() // force and wait refresh
            res.size shouldBe 2
            sut.error.value shouldBe null
        }
    }
})
