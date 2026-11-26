package com.nullo.voidapp.feature.chat.presentation.store

import com.nullo.voidapp.core.fakes.FakeConversationRepository
import com.nullo.voidapp.core.model.Conversation
import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.feature.chat.presentation.store.SideMenuStore.Intent
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
import voidapp.core.utils.generated.resources.Res as CoreRes
import voidapp.core.utils.generated.resources.error_unknown
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.error_blank_conversation_name
import voidapp.feature.chat.generated.resources.error_cannot_access_conversation
import voidapp.feature.chat.generated.resources.error_cannot_rename_conversation

private fun conversation(id: Long, title: String) = Conversation(
    id = id,
    title = title,
    createdAt = 0L,
    updatedAt = 0L
)

val SideMenuStoreTest by testSuite {

    testSuite("observe") {

        test("conversations of the fake appear in State") {
            val conversations = listOf(
                conversation(id = 1, title = "Alpha chat"),
                conversation(id = 2, title = "Beta chat")
            )
            withSideMenuStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = conversations
                )
            ) {
                idle()

                store.state.conversations shouldBe conversations
            }
        }

        test("a later external conversation change is reflected") {
            val initialConversations = listOf(conversation(id = 1, title = "Alpha chat"))
            val updatedConversations = initialConversations + conversation(id = 2, title = "Beta chat")
            withSideMenuStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = initialConversations
                )
            ) {
                idle()

                conversationRepository.changeConversationsExternally(updatedConversations)
                idle()

                store.state.conversations shouldBe updatedConversations
            }
        }

        test("the active conversation id follows the holder") {
            withSideMenuStore {
                idle()

                activeConversation.set(5)
                idle()

                store.state.activeConversationId shouldBe 5

                activeConversation.set(null)
                idle()

                store.state.activeConversationId shouldBe null
            }
        }
    }

    testSuite("conversationIntent") {

        test("OpenConversation sets the holder id") {
            withSideMenuStore {
                idle()

                store.accept(Intent.OpenConversation(42))
                idle()

                activeConversation.id.value shouldBe 42
                store.state.activeConversationId shouldBe 42
            }
        }

        test("StartNewConversation clears the holder id") {
            withSideMenuStore {
                activeConversation.set(42)
                idle()

                store.accept(Intent.StartNewConversation)
                idle()

                activeConversation.id.value shouldBe null
                store.state.activeConversationId shouldBe null
            }
        }
    }

    testSuite("search") {

        test("ChangeConversationSearchQuery updates the query immediately and filters by title after 300 ms") {
            val conversations = listOf(
                conversation(id = 1, title = "Alpha chat"),
                conversation(id = 2, title = "Beta chat"),
                conversation(id = 3, title = "Gamma chat")
            )
            withSideMenuStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = conversations
                )
            ) {
                idle()

                store.accept(Intent.ChangeConversationSearchQuery("ALPHA"))
                idle()

                store.state.conversationSearchQuery shouldBe "ALPHA"
                store.state.conversations.map { it.id } shouldBe listOf(1, 2, 3)

                advanceBy(299.milliseconds)

                store.state.conversations.map { it.id } shouldBe listOf(1, 2, 3)

                advanceBy(1.milliseconds)

                store.state.conversations.map { it.id } shouldBe listOf(1)
                conversationRepository.calls shouldBe listOf(
                    "findConversationIdsByText:",
                    "findConversationIdsByText:ALPHA"
                )
            }
        }

        test("a conversation is shown when its id is in initialConversationIdsByText") {
            val conversations = listOf(
                conversation(id = 1, title = "Alpha chat"),
                conversation(id = 2, title = "Beta chat"),
                conversation(id = 3, title = "Gamma chat")
            )
            withSideMenuStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = conversations,
                    initialConversationIdsByText = setOf(2)
                )
            ) {
                idle()

                store.accept(Intent.ChangeConversationSearchQuery("zzz"))
                idle()
                advanceBy(300.milliseconds)

                store.state.conversations.map { it.id } shouldBe listOf(2)
                conversationRepository.calls shouldBe listOf(
                    "findConversationIdsByText:",
                    "findConversationIdsByText:zzz"
                )
            }
        }

        test("two queries 200 ms apart are debounced to the second query") {
            val conversations = listOf(
                conversation(id = 1, title = "Alpha chat"),
                conversation(id = 2, title = "Beta chat"),
                conversation(id = 3, title = "Gamma chat")
            )
            withSideMenuStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = conversations
                )
            ) {
                idle()

                store.accept(Intent.ChangeConversationSearchQuery("Alpha"))
                idle()
                advanceBy(200.milliseconds)

                store.accept(Intent.ChangeConversationSearchQuery("Beta"))
                idle()
                advanceBy(299.milliseconds)

                store.state.conversations.map { it.id } shouldBe listOf(1, 2, 3)
                conversationRepository.calls shouldBe listOf("findConversationIdsByText:")

                advanceBy(1.milliseconds)

                store.state.conversations.map { it.id } shouldBe listOf(2)
                conversationRepository.calls shouldBe listOf(
                    "findConversationIdsByText:",
                    "findConversationIdsByText:Beta"
                )
            }
        }

        test("an empty query restores the whole list immediately without a debounce") {
            val conversations = listOf(
                conversation(id = 1, title = "Alpha chat"),
                conversation(id = 2, title = "Beta chat"),
                conversation(id = 3, title = "Gamma chat")
            )
            withSideMenuStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = conversations
                )
            ) {
                idle()

                store.accept(Intent.ChangeConversationSearchQuery("Alpha"))
                idle()
                advanceBy(300.milliseconds)

                store.state.conversations.map { it.id } shouldBe listOf(1)

                store.accept(Intent.ChangeConversationSearchQuery(""))
                idle()

                store.state.conversationSearchQuery shouldBe ""
                store.state.conversations.map { it.id } shouldBe listOf(1, 2, 3)
                conversationRepository.calls shouldBe listOf(
                    "findConversationIdsByText:",
                    "findConversationIdsByText:Alpha",
                    "findConversationIdsByText:"
                )
            }
        }

        test("a failing search does not stop conversations from appearing") {
            val conversationRepository = FakeConversationRepository(
                initialConversations = listOf(
                    conversation(id = 1, title = "Alpha chat"),
                    conversation(id = 2, title = "Beta chat")
                )
            )
            conversationRepository.findConversationIdsByTextFailure = Exception("db")
            withSideMenuStore(conversationRepository = conversationRepository) {
                idle()

                store.state.conversations.map { it.id } shouldBe listOf(1, 2)

                conversationRepository.changeConversationsExternally(
                    listOf(
                        conversation(id = 1, title = "Alpha chat"),
                        conversation(id = 2, title = "Beta chat"),
                        conversation(id = 3, title = "Gamma chat")
                    )
                )
                idle()

                store.state.conversations.map { it.id } shouldBe listOf(1, 2, 3)

                store.accept(Intent.ChangeConversationSearchQuery("alpha"))
                idle()
                advanceBy(300.milliseconds)

                store.state.conversations.map { it.id } shouldBe listOf(1)
            }
        }
    }

    testSuite("rename") {

        test("OpenRenameConversationDialog sets the conversation id") {
            withSideMenuStore {
                idle()

                store.accept(Intent.OpenRenameConversationDialog(2))
                idle()

                store.state.renamingConversationId shouldBe 2
            }
        }

        test("ChangeConversationNameInput stores the input") {
            withSideMenuStore {
                idle()

                store.accept(Intent.OpenRenameConversationDialog(1))
                store.accept(Intent.ChangeConversationNameInput("New title"))
                idle()

                store.state.renamingConversationInput shouldBe "New title"
            }
        }

        test("ConfirmConversationRename renames the conversation and closes the dialog") {
            withSideMenuStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = listOf(conversation(id = 1, title = "Old title"))
                )
            ) {
                idle()

                store.accept(Intent.OpenRenameConversationDialog(1))
                store.accept(Intent.ChangeConversationNameInput("  New title  "))
                store.accept(Intent.ConfirmConversationRename)
                idle()

                conversationRepository.calls shouldBe listOf(
                    "findConversationIdsByText:",
                    "rename:1:  New title  "
                )
                store.state.renamingConversationId shouldBe null
                store.state.renamingConversationInput shouldBe ""
                store.state.conversations.map { it.title } shouldBe listOf("  New title  ")
            }
        }

        test("a blank name shows an error and keeps the dialog open") {
            withSideMenuStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = listOf(conversation(id = 1, title = "Old title"))
                )
            ) {
                idle()

                store.accept(Intent.OpenRenameConversationDialog(1))
                store.accept(Intent.ChangeConversationNameInput("   "))
                store.accept(Intent.ConfirmConversationRename)
                idle()

                store.state.errorDialogText shouldBe UiText(Res.string.error_blank_conversation_name)
                store.state.renamingConversationId shouldBe 1
                store.state.renamingConversationInput shouldBe "   "
                conversationRepository.calls shouldBe listOf("findConversationIdsByText:")
            }
        }

        test("confirming without an open dialog shows an error") {
            withSideMenuStore {
                idle()

                store.accept(Intent.ConfirmConversationRename)
                idle()

                store.state.errorDialogText shouldBe UiText(Res.string.error_cannot_access_conversation)
                conversationRepository.calls shouldBe listOf("findConversationIdsByText:")
            }
        }

        test("a rename failure shows an error and keeps the dialog open") {
            val conversationRepository = FakeConversationRepository(
                initialConversations = listOf(conversation(id = 1, title = "Old title"))
            )
            conversationRepository.renameFailure = Exception("x")
            withSideMenuStore(conversationRepository = conversationRepository) {
                idle()

                store.accept(Intent.OpenRenameConversationDialog(1))
                store.accept(Intent.ChangeConversationNameInput("New title"))
                store.accept(Intent.ConfirmConversationRename)
                idle()

                store.state.errorDialogText shouldBe UiText(Res.string.error_cannot_rename_conversation)
                store.state.renamingConversationId shouldBe 1
                store.state.renamingConversationInput shouldBe "New title"
            }
        }

        test("CloseRenameConversationDialog clears the id and the input") {
            withSideMenuStore {
                idle()

                store.accept(Intent.OpenRenameConversationDialog(1))
                store.accept(Intent.ChangeConversationNameInput("Draft"))
                store.accept(Intent.CloseRenameConversationDialog)
                idle()

                store.state.renamingConversationId shouldBe null
                store.state.renamingConversationInput shouldBe ""
            }
        }

        test("DismissErrorDialog clears the error") {
            withSideMenuStore {
                idle()

                store.accept(Intent.ConfirmConversationRename)
                idle()
                store.state.errorDialogText shouldBe UiText(Res.string.error_cannot_access_conversation)

                store.accept(Intent.DismissErrorDialog)
                idle()

                store.state.errorDialogText shouldBe null
            }
        }
    }

    testSuite("delete") {

        test("DeleteConversation calls the repository and removes the conversation") {
            withSideMenuStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = listOf(
                        conversation(id = 1, title = "Alpha chat"),
                        conversation(id = 2, title = "Beta chat")
                    )
                )
            ) {
                idle()

                store.accept(Intent.DeleteConversation(2))
                idle()

                conversationRepository.calls shouldBe listOf(
                    "findConversationIdsByText:",
                    "delete:2"
                )
                store.state.conversations.map { it.id } shouldBe listOf(1)
            }
        }

        test("a delete failure shows an unknown error") {
            val conversationRepository = FakeConversationRepository(
                initialConversations = listOf(conversation(id = 1, title = "Alpha chat"))
            )
            conversationRepository.deleteFailure = Exception("x")
            withSideMenuStore(conversationRepository = conversationRepository) {
                idle()

                store.accept(Intent.DeleteConversation(1))
                idle()

                store.state.errorDialogText shouldBe UiText(CoreRes.string.error_unknown)
                store.state.conversations.map { it.id } shouldBe listOf(1)
            }
        }
    }
}
