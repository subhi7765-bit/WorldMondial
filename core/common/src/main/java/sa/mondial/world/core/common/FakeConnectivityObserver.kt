package sa.mondial.world.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeConnectivityObserver : ConnectivityObserver {
    private val _isConnected = MutableStateFlow(true)
    override val isConnected: Flow<Boolean> = _isConnected

    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }
}