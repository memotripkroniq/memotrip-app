package com.example.memotrip_kroniq.ui.home

import org.junit.Assert.*
import org.junit.Test

class DummyPayNoticeTest {
    @Test fun pendingStatesSharePanelAndPreserveHiddenStateWhileVerifying() {
        for (state in listOf(DummyPayState.UNCONFIRMED, DummyPayState.VERIFYING, DummyPayState.WAITING)) {
            val ui = HomeUiState(dummyPayState = state, isDummyPayNoticeDismissed = true)
            assertTrue(ui.isPendingPaymentNotice)
            assertTrue(ui.isDummyPayNoticeCollapsed)
            assertTrue(ui.isPurchaseBlocked)
        }
    }
    @Test fun terminalResultsAndActionableErrorsAreNotPendingPanels() {
        for (state in listOf(DummyPayState.CONFIRMED, DummyPayState.FAILED, DummyPayState.VERIFICATION_ERROR)) {
            val ui = HomeUiState(dummyPayState = state, isDummyPayNoticeDismissed = true)
            assertFalse(ui.isPendingPaymentNotice)
            assertFalse(ui.isDummyPayNoticeCollapsed)
        }
    }
}
