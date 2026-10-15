package com.igorwojda.jellymobile.feature.base.presentation.flowmvi

import androidx.lifecycle.ViewModel
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

/**
 * Base ViewModel that owns a FlowMVI [Container.store], so the store lives as long as the screen's back stack entry.
 *
 * The Compose UI consumes the store directly: `store.subscribe { action -> … }` and `store.intent(…)`.
 */
abstract class StoreViewModel<S : MVIState, I : MVIIntent, A : MVIAction> :
    ViewModel(),
    Container<S, I, A>
