package com.example.memotrip_kroniq.ui.home

import android.util.Log
import com.example.memotrip_kroniq.data.datastore.AuthSessionStore
import com.example.memotrip_kroniq.data.datastore.PendingPayment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.memotrip_kroniq.data.payments.DummyPaymentPlan
import com.example.memotrip_kroniq.data.payments.PaymentApiException
import com.example.memotrip_kroniq.data.remote.dto.TripLimitsResponse
import com.example.memotrip_kroniq.ui.home.model.TripHistoryItem
import com.memotrip_kroniq.BuildConfig
import com.example.memotrip_kroniq.ui.home.payments.isAllowedDummyPayUrl
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

class HomeViewModel(
    private val authRepository: HomeAuthDataSource,
    private val tripsRepository: HomeTripsDataSource,
    private val paymentsRepository: HomePaymentsDataSource,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
    private val sessionStore: AuthSessionStore? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(
        isDummyPayRecoveryComplete = sessionStore == null || BuildConfig.FLAVOR == "production"
    ))
    val uiState: StateFlow<HomeUiState> = _uiState
    private var refreshHomeJob: Job? = null
    private var addTripAccessJob: Job? = null
    private var paymentReturnRefreshJob: Job? = null
    private var createPaymentJob: Job? = null
    private var loadMeJob: Job? = null
    // Serialize account/limit reads and their application, including initial loading.
    private val accountRefreshMutex = Mutex()
    private val pendingQueue = linkedMapOf<String, PendingPayment>()
    private var paymentEpoch = 0L
    private var paymentUrl: String? = null // Never persisted: contains a bearer capability.
    private var userId: String? = null
    private var sessionGeneration: String? = null

    private val restoredPlan = savedStateHandle.get<String>("dummy_plan")
        ?.let { value -> DummyPaymentPlan.entries.firstOrNull { it.name == value } }

    private data class HomeRefreshPayload(
        val trips: List<TripHistoryItem>?,
        val limits: TripLimitsResponse?
    )

    init {
        if (sessionStore == null) {
            restorePayment(restoredPlan, savedStateHandle["dummy_purchase"])
            loadMe()
            refreshHomeData(showTripLoader = true)
        } else viewModelScope.launch {
            try {
                // Read durable metadata before enabling any purchase. Network failure cannot lose it.
                var snapshot = sessionStore.snapshot()
                val savedOwner = savedStateHandle.get<String>("dummy_owner")
                val savedPurchase = savedStateHandle.get<String>("dummy_purchase")
                if (snapshot.legacyPendingImportAllowed && savedOwner != null &&
                    restoredPlan != null && savedPurchase != null) {
                    if (sessionStore.importSavedPendingPayment(
                        PendingPayment(savedPurchase, restoredPlan.name, savedOwner), snapshot.generation
                    )) snapshot = sessionStore.snapshot()
                }
                val owner = snapshot.session?.userId
                userId = owner
                sessionGeneration = snapshot.generation
                val pending = owner?.let { snapshot.pendingPayments[it] }
                if (pending != null) {
                    savedStateHandle["dummy_owner"] = pending.userId
                    restorePayment(DummyPaymentPlan.entries.firstOrNull { it.name == pending.plan }, pending.purchaseId)
                } else if (owner != null && owner == savedOwner && savedPurchase == null &&
                    savedStateHandle.get<String>("dummy_session_generation") == snapshot.generation) {
                    // Preserve an ambiguous POST only within its original session; never replay it.
                    restorePayment(restoredPlan, null)
                } else {
                    clearSavedPayment()
                }
                if (snapshot.session?.hasUsableAccessToken() != true) {
                    _uiState.update { it.copy(requiresSignIn = true) }
                    return@launch
                }
                loadMe()
                refreshHomeData(showTripLoader = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Keep SavedState intact and purchases disabled if durable migration fails.
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun restorePayment(plan: DummyPaymentPlan?, purchaseId: String?) {
        if (plan == null) return
        savedStateHandle["dummy_plan"] = plan.name
        savedStateHandle["dummy_purchase"] = purchaseId
        _uiState.update { it.copy(
            dummyPayPlan = plan, dummyPayPurchaseId = purchaseId,
            isDummyPayNoticeDismissed = savedStateHandle.get<Boolean>("dummy_notice_dismissed") == true,
            dummyPayState = DummyPayState.UNCONFIRMED, isAwaitingDummyPayReturn = true
        ) }
    }

    private fun pendingMetadata(): PendingPayment? {
        val owner = savedStateHandle.get<String>("dummy_owner") ?: return null
        val purchase = savedStateHandle.get<String>("dummy_purchase") ?: return null
        val plan = savedStateHandle.get<String>("dummy_plan") ?: return null
        return PendingPayment(purchase, plan, owner)
    }

    private fun loadMe() {
        if (loadMeJob?.isActive == true) return
        loadMeJob = viewModelScope.launch {
            accountRefreshMutex.withLock {
                try {
                    val me = authRepository.getMe()
                    val owner = savedStateHandle.get<String>("dummy_owner")
                    if (owner != null && owner != me.id) clearDummyPay(cancelJobs = false)
                    userId = me.id
                    applyUserState(me, isLoading = false)
                    if (sessionStore != null && BuildConfig.FLAVOR != "production") recoverPendingDummyPay()
                    else if (_uiState.value.dummyPayPlan != null) verifyDummyPay()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            dummyPayState = if (it.dummyPayPlan != null)
                                DummyPayState.VERIFICATION_ERROR else it.dummyPayState,
                            isDummyPayVerificationNoticePending = it.dummyPayPlan != null
                        )
                    }
                }
            }
        }
    }


    fun refreshHomeData(showTripLoader: Boolean = false) {
        if (_uiState.value.dummyPayState == DummyPayState.VERIFYING) return
        if (refreshHomeJob?.isActive == true) return

        refreshHomeJob = viewModelScope.launch {
            if (showTripLoader) {
                _uiState.update {
                    it.copy(
                        isTripsLoading = true,
                        isRefreshingHome = false
                    )
                }
            } else {
                _uiState.update { it.copy(isRefreshingHome = true) }
            }

            try {
                accountRefreshMutex.withLock {
                    val payload = fetchHomeRefreshPayload()
                    applyHomeRefreshPayload(payload)

                    if (payload.limits == null && showTripLoader) {
                        val fallbackEnabled = _uiState.value.isKroniq
                        _uiState.update { it.copy(isAddTripEnabled = fallbackEnabled) }
                    }
                }
            } finally {
                _uiState.update { it.copy(isTripsLoading = false, isRefreshingHome = false) }
            }
        }
    }

    fun verifyAddTripAccess(
        onAllowed: () -> Unit,
        onBlocked: () -> Unit = {}
    ) {
        if (_uiState.value.dummyPayState == DummyPayState.VERIFYING) return
        if (addTripAccessJob?.isActive == true) return

        addTripAccessJob = viewModelScope.launch {
            _uiState.update { it.copy(isCheckingAddTripAccess = true) }

            try {
                accountRefreshMutex.withLock {
                    val payload = fetchHomeRefreshPayload()
                    applyHomeRefreshPayload(payload)

                    val limits = payload.limits
                    val isAllowed = resolveIsAddTripEnabled(limits)

                    if (isAllowed == true) {
                        onAllowed()
                    } else if (isAllowed == false) {
                        onBlocked()
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Keep the current state on transient verification failures.
            } finally {
                _uiState.update { it.copy(isCheckingAddTripAccess = false) }
            }
        }
    }

    fun onThemeClick(theme: String) {
        if (BuildConfig.DEBUG) {
            Log.d("HOME_THEME", "Theme selected")
        }

        _uiState.update {
            it.copy(
                themesContentState = ThemesContentState.ThemeTrips(theme)
            )
        }
    }

    fun onThemesBackClick() {
        if (BuildConfig.DEBUG) {
            Log.d("HOME_THEME", "Returned to theme grid")
        }

        _uiState.update {
            it.copy(
                themesContentState = ThemesContentState.Grid
            )
        }
    }

    fun getTripsForSelectedTheme(): List<TripHistoryItem> {
        val contentState = _uiState.value.themesContentState

        return if (contentState is ThemesContentState.ThemeTrips) {
            _uiState.value.trips.filter { trip ->
                trip.theme.equals(contentState.theme, ignoreCase = true)
            }
        } else {
            emptyList()
        }
    }

    fun startPremiumDummyPay() {
        startDummyPay(DummyPaymentPlan.PREMIUM)
    }

    fun startKroniqDummyPay() {
        startDummyPay(DummyPaymentPlan.KRONIQ)
    }

    fun dismissDummyPayNotice() {
        if (!_uiState.value.isPendingPaymentNotice) return
        savedStateHandle["dummy_notice_dismissed"] = true
        _uiState.update { it.copy(isDummyPayNoticeDismissed = true) }
    }

    fun showDummyPayNotice() {
        if (!_uiState.value.isPendingPaymentNotice) return
        savedStateHandle.remove<Boolean>("dummy_notice_dismissed")
        _uiState.update { it.copy(isDummyPayNoticeDismissed = false) }
    }

    fun onDummyPayBrowserOpened() {
        _uiState.update { it.copy(isAwaitingDummyPayReturn = true, dummyPayState = DummyPayState.WAITING) }
    }

    fun onDummyPayOpenFailed() {
        _uiState.update { it.copy(dummyPayState = DummyPayState.OPEN_ERROR) }
    }

    fun reopenDummyPay() = continueDummyPay()

    fun refreshUserFromPaymentReturnIfNeeded() {
        if (!_uiState.value.isAwaitingDummyPayReturn) return
        verifyDummyPay()
    }

    private enum class PaymentAction { VERIFY, CONTINUE, CANCEL }

    fun continueDummyPay() = performPaymentAction(PaymentAction.CONTINUE)
    fun cancelDummyPay() = performPaymentAction(PaymentAction.CANCEL)
    fun verifyDummyPay() = performPaymentAction(PaymentAction.VERIFY)

    private suspend fun isCurrentOwner(owner: String, epoch: Long): Boolean {
        if (paymentEpoch != epoch || userId != owner) return false
        val store = sessionStore ?: return true
        val snapshot = store.snapshot()
        return paymentEpoch == epoch && userId == owner && snapshot.generation == sessionGeneration &&
            snapshot.session?.userId == owner
    }

    // A failed metadata write does not invalidate a known server purchase.
    // SavedState restoration remains fenced separately against logout and stale imports.
    private suspend fun isCurrentPayment(metadata: PendingPayment, epoch: Long): Boolean =
        pendingMetadata() == metadata && isCurrentOwner(metadata.userId, epoch) && pendingMetadata() == metadata

    private fun selectPendingPayment(metadata: PendingPayment) {
        val samePurchase = _uiState.value.dummyPayPurchaseId == metadata.purchaseId
        if (!samePurchase) {
            paymentUrl = null
            savedStateHandle.remove<Boolean>("dummy_notice_dismissed")
        }
        savedStateHandle["dummy_owner"] = metadata.userId
        savedStateHandle["dummy_session_generation"] = sessionGeneration
        restorePayment(DummyPaymentPlan.entries.first { it.apiValue == metadata.plan }, metadata.purchaseId)
        _uiState.update { it.copy(dummyPayUrlToOpen = null, canReopenDummyPay = samePurchase && paymentUrl != null,
            dummyPayPendingCount = pendingQueue.size) }
    }

    private suspend fun persistKnownPayment(metadata: PendingPayment) {
        try {
            // Recovery is authoritative on restart even if this local write fails.
            sessionStore?.savePendingPayment(metadata, sessionGeneration)
        } catch (e: CancellationException) { throw e } catch (_: Exception) {
            // Never discard the known purchase or repeat its creation after a failed write.
        }
    }

    private fun scheduleSelectedVerification(metadata: PendingPayment, epoch: Long) {
        viewModelScope.launch {
            yield() // Let the operation assigning paymentReturnRefreshJob finish first.
            paymentReturnRefreshJob?.join()
            if (paymentEpoch == epoch && pendingMetadata() == metadata &&
                _uiState.value.dummyPayState == DummyPayState.UNCONFIRMED) verifyDummyPay()
        }
    }

    private fun recoverPendingDummyPay() {
        if (BuildConfig.FLAVOR == "production" || paymentReturnRefreshJob?.isActive == true ||
            _uiState.value.dummyPayState == DummyPayState.CREATING) return
        val owner = userId ?: return
        val epoch = paymentEpoch
        _uiState.update { it.copy(isRecoveringDummyPay = true, isDummyPayRecoveryComplete = false,
            dummyPayState = if (it.dummyPayPlan != null) DummyPayState.VERIFYING else DummyPayState.IDLE,
            dummyPayUrlToOpen = null,
            isDummyPayVerificationNoticePending = false) }
        paymentReturnRefreshJob = viewModelScope.launch {
            accountRefreshMutex.withLock {
                try {
                    if (!isCurrentOwner(owner, epoch)) {
                        if (paymentEpoch == epoch) clearDummyPay(cancelJobs = false)
                        return@withLock
                    }
                    val recovered = paymentsRepository.getAllPendingDummyPayments()
                    if (!isCurrentOwner(owner, epoch)) {
                        if (paymentEpoch == epoch) clearDummyPay(cancelJobs = false)
                        return@withLock
                    }
                    // Keep a known ID even if absent from PENDING: only its status GET can
                    // distinguish COMPLETED from FAILED and refresh entitlements correctly.
                    val known = pendingMetadata()
                    check(known == null || known.userId == owner) { "Payment owner changed." }
                    pendingQueue.clear()
                    known?.let { pendingQueue[it.purchaseId] = it }
                    recovered.forEach { item ->
                        check(item.provider == "DUMMY_PAY" && item.status == "PENDING" &&
                            item.purchaseId.isNotBlank() && DummyPaymentPlan.entries.any { it.apiValue == item.plan })
                        val metadata = PendingPayment(item.purchaseId, item.plan, owner)
                        check(known?.takeIf { it.purchaseId == item.purchaseId }?.plan?.let { it == item.plan } != false)
                        pendingQueue[item.purchaseId] = metadata
                    }
                    val next = pendingQueue.values.firstOrNull()
                    if (next == null) {
                        // An unknown create may already have completed and disappeared from
                        // PENDING. Refresh actual entitlements before enabling another purchase.
                        val me = authRepository.getMe()
                        val limits = authRepository.getTripLimits()
                        if (!isCurrentOwner(owner, epoch)) {
                            if (paymentEpoch == epoch) clearDummyPay(cancelJobs = false)
                            return@withLock
                        }
                        check(me.id == owner) { "Payment account changed." }
                        applyUserState(me)
                        applyHomeRefreshPayload(HomeRefreshPayload(trips = null, limits = limits))
                        paymentUrl = null
                        clearSavedPayment()
                        _uiState.update { it.copy(dummyPayState = DummyPayState.IDLE,
                            dummyPayPlan = null, dummyPayPurchaseId = null, dummyPayLoadingPlan = null,
                            dummyPayPendingCount = 0, canReopenDummyPay = false,
                            isAwaitingDummyPayReturn = false, isDummyPayNoticeDismissed = false,
                            isDummyPayRecoveryComplete = true) }
                    } else {
                        selectPendingPayment(next)
                        persistKnownPayment(next)
                        if (!isCurrentPayment(next, epoch)) {
                            if (paymentEpoch == epoch) clearDummyPay(cancelJobs = false)
                            return@withLock
                        }
                        _uiState.update { it.copy(isDummyPayRecoveryComplete = true) }
                        scheduleSelectedVerification(next, epoch)
                    }
                } catch (e: CancellationException) { throw e } catch (_: Exception) {
                    if (paymentEpoch == epoch) _uiState.update { it.copy(
                        dummyPayState = DummyPayState.VERIFICATION_ERROR,
                        isDummyPayVerificationNoticePending = true) }
                } finally {
                    if (paymentEpoch == epoch) _uiState.update { it.copy(isRecoveringDummyPay = false) }
                }
            }
        }
    }

    // Claim and revalidate immediately before launching. An old URL event cannot survive logout,
    // another account, a terminal response, or the start of another payment operation.
    suspend fun claimDummyPayUrl(): String? {
        val url = _uiState.value.dummyPayUrlToOpen ?: return null
        val metadata = pendingMetadata() ?: return null
        val epoch = paymentEpoch
        return try {
            if (isCurrentPayment(metadata, epoch) &&
                _uiState.value.dummyPayState == DummyPayState.WAITING &&
                _uiState.value.dummyPayUrlToOpen == url && isAllowedDummyPayUrl(url, BuildConfig.BASE_URL)) {
                consumeDummyPayUrl()
                url
            } else { consumeDummyPayUrl(); null }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            if (paymentEpoch == epoch && pendingMetadata() == metadata) {
                consumeDummyPayUrl()
                _uiState.update { it.copy(dummyPayState = DummyPayState.VERIFICATION_ERROR,
                    isDummyPayVerificationNoticePending = true) }
            }
            null
        }
    }

    private fun performPaymentAction(action: PaymentAction) {
        if (_uiState.value.isRecoveringDummyPay) return
        if (action == PaymentAction.VERIFY && !_uiState.value.isDummyPayRecoveryComplete) {
            if (!_uiState.value.isAccountVerified) loadMe() else recoverPendingDummyPay()
            return
        }
        if (_uiState.value.dummyPayPlan == null) {
            if (action == PaymentAction.VERIFY && !_uiState.value.isAccountVerified) loadMe()
            return
        }
        if (_uiState.value.dummyPayState in setOf(DummyPayState.CREATING, DummyPayState.VERIFYING)) return
        if (paymentReturnRefreshJob?.isActive == true || BuildConfig.FLAVOR == "production") return
        val metadata = pendingMetadata()
        if (metadata == null) {
            // Never repeat an ambiguous create without a purchase ID.
            if (action == PaymentAction.VERIFY) recoverPendingDummyPay()
            return
        }
        val epoch = paymentEpoch
        // Reserve synchronously, including clearing an unclaimed URL before Cancel/Continue.
        if (action != PaymentAction.VERIFY) paymentUrl = null
        _uiState.update { it.copy(
            dummyPayState = DummyPayState.VERIFYING, dummyPayUrlToOpen = null,
            canReopenDummyPay = paymentUrl != null, isDummyPayVerificationNoticePending = false
        ) }
        paymentReturnRefreshJob = viewModelScope.launch {
            accountRefreshMutex.withLock {
                try {
                    if (!isCurrentPayment(metadata, epoch)) {
                        if (paymentEpoch == epoch) clearDummyPay(cancelJobs = false)
                        return@withLock
                    }
                    val payment = when (action) {
                        PaymentAction.VERIFY -> paymentsRepository.getDummyPaymentStatus(metadata.purchaseId)
                        PaymentAction.CONTINUE -> paymentsRepository.continueDummyPayment(metadata.purchaseId)
                        PaymentAction.CANCEL -> paymentsRepository.cancelDummyPayment(metadata.purchaseId)
                    }
                    if (!isCurrentPayment(metadata, epoch)) {
                        if (paymentEpoch == epoch) clearDummyPay(cancelJobs = false)
                        return@withLock
                    }
                    check(payment.purchaseId == metadata.purchaseId && payment.provider == "DUMMY_PAY" &&
                        payment.plan == metadata.plan) { "Unexpected payment response." }
                    when (payment.status) {
                        "PENDING" -> {
                            if (action == PaymentAction.CONTINUE) {
                                val url = payment.paymentUrl
                                check(url != null && isAllowedDummyPayUrl(url, BuildConfig.BASE_URL)) {
                                    "Invalid payment URL."
                                }
                                // Also bind the returned URL to the requested purchase, not just its host.
                                check(java.net.URI(url).path == "/payments/dummy/${metadata.purchaseId}") {
                                    "Unexpected payment URL."
                                }
                                paymentUrl = url
                            }
                            pendingQueue[metadata.purchaseId] = metadata
                            persistKnownPayment(metadata)
                            if (!isCurrentPayment(metadata, epoch)) {
                                if (paymentEpoch == epoch) clearDummyPay(cancelJobs = false)
                                return@withLock
                            }
                            _uiState.update { it.copy(
                                dummyPayPendingCount = pendingQueue.size,
                                dummyPayState = DummyPayState.WAITING,
                                dummyPayUrlToOpen = if (action == PaymentAction.CONTINUE) paymentUrl else null,
                                canReopenDummyPay = paymentUrl != null,
                                isAwaitingDummyPayReturn = true
                            ) }
                        }
                        "FAILED" -> {
                            sessionStore?.clearPendingPayment(metadata)
                            if (isCurrentPayment(metadata, epoch)) finishDummyPay(DummyPayState.FAILED)
                            else if (paymentEpoch == epoch) clearDummyPay(cancelJobs = false)
                        }
                        "COMPLETED" -> {
                            val me = authRepository.getMe()
                            val limits = authRepository.getTripLimits()
                            if (!isCurrentPayment(metadata, epoch)) {
                                if (paymentEpoch == epoch) clearDummyPay(cancelJobs = false)
                                return@withLock
                            }
                            check(me.id == metadata.userId) { "Payment account changed." }
                            // Account responses alone grant entitlements.
                            userId = me.id
                            applyUserState(me)
                            applyHomeRefreshPayload(HomeRefreshPayload(trips = null, limits = limits))
                            sessionStore?.clearPendingPayment(metadata)
                            if (isCurrentPayment(metadata, epoch)) finishDummyPay(DummyPayState.CONFIRMED)
                            else if (paymentEpoch == epoch) clearDummyPay(cancelJobs = false)
                        }
                        else -> error("Unknown payment status.")
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Unknown outcomes retain durable metadata. Retry is always explicit.
                    if (paymentEpoch == epoch && pendingMetadata() == metadata) {
                        val current = try { isCurrentPayment(metadata, epoch) }
                            catch (e: CancellationException) { throw e } catch (_: Exception) { true }
                        if (current) _uiState.update { it.copy(
                            dummyPayState = DummyPayState.VERIFICATION_ERROR,
                            isAwaitingDummyPayReturn = true,
                            isDummyPayVerificationNoticePending = true
                        ) }
                    }
                } finally {
                    if (paymentEpoch == epoch && pendingMetadata() == metadata &&
                        _uiState.value.dummyPayState == DummyPayState.VERIFYING) {
                        _uiState.update { it.copy(dummyPayState = DummyPayState.VERIFICATION_ERROR,
                            isAwaitingDummyPayReturn = true, isDummyPayVerificationNoticePending = true) }
                    }
                }
            }
        }
    }

    private fun finishDummyPay(state: DummyPayState) {
        _uiState.value.dummyPayPurchaseId?.let { pendingQueue.remove(it) }
        paymentEpoch++
        paymentUrl = null
        clearSavedPayment()
        _uiState.update { it.copy(
            dummyPayPendingCount = pendingQueue.size,
            dummyPayState = state, dummyPayPlan = null, dummyPayPurchaseId = null,
            dummyPayUrlToOpen = null, dummyPayLoadingPlan = null,
            canReopenDummyPay = false, isAwaitingDummyPayReturn = false,
            isDummyPayNoticeDismissed = false,
            isDummyPayVerificationNoticePending = false
        ) }
    }

    // Claim before displaying: navigation, backgrounding or a new collector cannot replay it.
    fun consumeDummyPayResult(): DummyPayState? {
        val result = _uiState.value.dummyPayState
        if (result != DummyPayState.CONFIRMED && result != DummyPayState.FAILED) return null
        _uiState.update { it.copy(dummyPayState = DummyPayState.IDLE) }
        val next = pendingQueue.values.firstOrNull()
        if (next != null) {
            selectPendingPayment(next)
            scheduleSelectedVerification(next, paymentEpoch)
        } else if (!_uiState.value.isDummyPayRecoveryComplete) {
            val epoch = paymentEpoch
            viewModelScope.launch {
                yield()
                paymentReturnRefreshJob?.join()
                if (paymentEpoch == epoch && !_uiState.value.isDummyPayRecoveryComplete) recoverPendingDummyPay()
            }
        }
        return result
    }

    fun consumeDummyPayVerificationNotice(): Boolean {
        if (!_uiState.value.isDummyPayVerificationNoticePending) return false
        _uiState.update { it.copy(isDummyPayVerificationNoticePending = false) }
        return true
    }

    private fun clearSavedPayment() {
        savedStateHandle.remove<String>("dummy_plan")
        savedStateHandle.remove<String>("dummy_purchase")
        savedStateHandle.remove<String>("dummy_owner")
        savedStateHandle.remove<Boolean>("dummy_notice_dismissed")
        savedStateHandle.remove<String>("dummy_session_generation")
    }

    fun clearDummyPay(cancelJobs: Boolean = true) {
        pendingQueue.clear()
        paymentEpoch++
        if (cancelJobs) {
            createPaymentJob?.cancel()
            paymentReturnRefreshJob?.cancel()
            loadMeJob?.cancel()
            refreshHomeJob?.cancel()
            addTripAccessJob?.cancel()
            userId = null
        }
        paymentUrl = null
        clearSavedPayment()
        _uiState.update { it.copy(
            dummyPayPendingCount = 0, isRecoveringDummyPay = false,
            isDummyPayRecoveryComplete = sessionStore == null || BuildConfig.FLAVOR == "production",
            dummyPayState = DummyPayState.IDLE, dummyPayPlan = null,
            dummyPayPurchaseId = null, dummyPayUrlToOpen = null,
            dummyPayLoadingPlan = null, canReopenDummyPay = false,
            isAwaitingDummyPayReturn = false, isAccountVerified = false,
            isDummyPayNoticeDismissed = false,
            isDummyPayVerificationNoticePending = false
        ) }
    }

    fun consumeDummyPayUrl() {
        _uiState.update { it.copy(dummyPayUrlToOpen = null) }
    }

    private suspend fun fetchHomeRefreshPayload(): HomeRefreshPayload = coroutineScope {
        val limitsDeferred = async { optionalRead { authRepository.getTripLimits() } }
        val tripsDeferred = async { optionalRead { tripsRepository.getMyTrips() } }

        val trips = tripsDeferred.await()?.map { trip ->
            TripHistoryItem(
                id = trip.id,
                title = trip.title,
                coverImageUrl = trip.coverImageUrl,
                theme = trip.theme,
                isSharedInKroniq = trip.isSharedInKroniQ
            )
        }

        HomeRefreshPayload(
            trips = trips,
            limits = limitsDeferred.await()
        )
    }

    private suspend fun <T> optionalRead(read: suspend () -> T): T? = try {
        read()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    private fun applyHomeRefreshPayload(payload: HomeRefreshPayload) {
        payload.trips?.let { trips ->
            if (BuildConfig.DEBUG && trips.isNotEmpty()) {
                Log.d("HOME_TRIPS", "Loaded trips count=${trips.size}")
            }

            _uiState.update { state ->
                state.copy(trips = trips)
            }
        }

        payload.limits?.let { limits ->
            val isAddTripEnabled = resolveIsAddTripEnabled(limits) ?: false
            _uiState.update {
                it.copy(
                    // `limit == null` is the backend contract for unlimited trips (KroniQ).
                    isAddTripEnabled = isAddTripEnabled,
                    tripLimitPlan = limits.plan,
                    tripLimitUsed = limits.used,
                    tripLimitLimit = limits.limit
                )
            }
        }
    }

    private fun applyUserState(
        me: com.example.memotrip_kroniq.data.model.UserMe,
        isLoading: Boolean = _uiState.value.isLoading
    ) {
        _uiState.update {
            it.copy(
                userEmail = me.email,
                isPremium = me.isPremium,
                isKroniq = me.isKroniq,
                isThemesLocked = !me.isKroniq,
                isAccountVerified = true,
                isLoading = isLoading
            )
        }
    }

    private fun resolveIsAddTripEnabled(limits: TripLimitsResponse?): Boolean? {
        if (limits == null) return null
        if (limits.limit == null) return true
        return limits.allowed
    }

    private fun startDummyPay(plan: DummyPaymentPlan) {
        if (BuildConfig.FLAVOR == "production") {
            _uiState.update {
                it.copy(dummyPayState = DummyPayState.UNAVAILABLE)
            }
            return
        }

        if (isPlanAlreadyActive(plan)) return
        if (_uiState.value.isPurchaseBlocked) return

        // Reserve synchronously, before launching, to reject even same-frame double taps.
        savedStateHandle["dummy_plan"] = plan.name
        savedStateHandle["dummy_owner"] = userId
        sessionGeneration?.let { savedStateHandle["dummy_session_generation"] = it }
        savedStateHandle.remove<String>("dummy_purchase")
        savedStateHandle.remove<Boolean>("dummy_notice_dismissed")
        paymentUrl = null
        _uiState.update { it.copy(
            dummyPayLoadingPlan = plan, dummyPayPlan = plan,
            dummyPayPurchaseId = null, dummyPayState = DummyPayState.CREATING,
            canReopenDummyPay = false, isAwaitingDummyPayReturn = false,
            isDummyPayNoticeDismissed = false, dummyPayUrlToOpen = null,
            isDummyPayVerificationNoticePending = false
        ) }

        val createEpoch = paymentEpoch
        val createOwner = userId
        suspend fun currentCreation(): Boolean {
            if (paymentEpoch != createEpoch || userId != createOwner) return false
            val snapshot = sessionStore?.snapshot() ?: return true
            return snapshot.generation == sessionGeneration && snapshot.session?.userId == createOwner
        }
        createPaymentJob = viewModelScope.launch {
            try {
                if (!currentCreation()) return@launch
                val response = paymentsRepository.createDummyPayment(plan)
                if (!currentCreation()) return@launch
                savedStateHandle["dummy_purchase"] = response.purchaseId
                _uiState.update { it.copy(dummyPayPurchaseId = response.purchaseId, dummyPayPendingCount = 1) }
                pendingMetadata()?.let { pendingQueue[it.purchaseId] = it }
                pendingMetadata()?.let { payment ->
                    check(sessionStore?.savePendingPayment(payment, sessionGeneration) != false) { "Payment session changed" }
                }
                if (!currentCreation()) return@launch
                paymentUrl = response.paymentUrl.takeIf { it.isNotBlank() }

                _uiState.update {
                    it.copy(
                        dummyPayLoadingPlan = null,
                        dummyPayPurchaseId = response.purchaseId,
                        dummyPayState = if (paymentUrl != null) DummyPayState.WAITING else DummyPayState.OPEN_ERROR,
                        canReopenDummyPay = paymentUrl?.let { isAllowedDummyPayUrl(it, BuildConfig.BASE_URL) } == true,
                        dummyPayUrlToOpen = paymentUrl
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val current = try { currentCreation() }
                    catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { paymentEpoch == createEpoch && userId == createOwner }
                if (!current) return@launch
                if (e is PaymentApiException && e.statusCode == 409 && e.apiCode == "DUMMY_PAY_PENDING_EXISTS") {
                    _uiState.update { it.copy(dummyPayState = DummyPayState.CREATION_UNKNOWN,
                        dummyPayLoadingPlan = null, isDummyPayRecoveryComplete = false) }
                    recoverPendingDummyPay()
                    return@launch
                }
                // Only an explicit rejecting response is safe to retry. Transport/5xx outcomes
                // may have created a purchase already; never automatically repeat POST.
                val rejected = e is PaymentApiException && e.statusCode in setOf(400, 401, 404)
                if (rejected) clearSavedPayment()
                _uiState.update {
                    it.copy(
                        dummyPayLoadingPlan = null,
                        dummyPayState = if (rejected) DummyPayState.START_ERROR
                            else if (savedStateHandle.get<String>("dummy_purchase") != null) DummyPayState.VERIFICATION_ERROR
                            else DummyPayState.CREATION_UNKNOWN,
                        dummyPayPlan = if (rejected) null else plan
                    )
                }
            }
        }
    }

    private fun isPlanAlreadyActive(plan: DummyPaymentPlan): Boolean {
        return when (plan) {
            DummyPaymentPlan.PREMIUM -> _uiState.value.currentPlan == HomePlanTier.PREMIUM
            DummyPaymentPlan.KRONIQ -> _uiState.value.currentPlan == HomePlanTier.KRONIQ
        }
    }

    override fun onCleared() {
        paymentUrl = null
        clearSavedPayment()
        super.onCleared()
    }

}
