package com.example.ui.tour

import androidx.compose.ui.geometry.Rect
import com.example.data.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GuidedTourManager(
    private val repository: AppRepository,
    private val scope: CoroutineScope
) {
    private val _registeredTargets = MutableStateFlow<Map<String, Rect>>(emptyMap())
    val registeredTargets: StateFlow<Map<String, Rect>> = _registeredTargets.asStateFlow()

    private val _activeTour = MutableStateFlow<GuidedTourConfig?>(null)
    val activeTour: StateFlow<GuidedTourConfig?> = _activeTour.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    private val _tourStatuses = MutableStateFlow<Map<String, TourStatus>>(emptyMap())
    val tourStatuses: StateFlow<Map<String, TourStatus>> = _tourStatuses.asStateFlow()

    fun loadTourStatuses(userId: String) {
        scope.launch {
            repository.getTourSettingsFlow(userId).collect { list ->
                val map = mutableMapOf<String, TourStatus>()
                list.forEach { setting ->
                    val tourId = setting.key.removePrefix("tour_")
                    val status = try {
                        TourStatus.valueOf(setting.value)
                    } catch (e: Exception) {
                        TourStatus.NOT_STARTED
                    }
                    map[tourId] = status
                }
                _tourStatuses.value = map
            }
        }
    }

    fun registerTargetBounds(key: String, bounds: Rect) {
        val current = _registeredTargets.value.toMutableMap()
        current[key] = bounds
        _registeredTargets.value = current
    }

    fun checkAndStartTour(userId: String, config: GuidedTourConfig) {
        scope.launch {
            val statusStr = repository.getTourStatus(userId, config.tourId)
            val currentStatus = try {
                if (statusStr != null) TourStatus.valueOf(statusStr) else TourStatus.NOT_STARTED
            } catch (e: Exception) {
                TourStatus.NOT_STARTED
            }

            if (currentStatus == TourStatus.NOT_STARTED && _activeTour.value == null) {
                _activeTour.value = config
                _currentStepIndex.value = 0
            }
        }
    }

    fun startTour(userId: String, config: GuidedTourConfig, forceReplay: Boolean = true) {
        _activeTour.value = config
        _currentStepIndex.value = 0
        if (forceReplay) {
            scope.launch {
                repository.setTourStatus(userId, config.tourId, TourStatus.NOT_STARTED.name)
            }
        }
    }

    fun nextStep(userId: String) {
        val tour = _activeTour.value ?: return
        val nextIdx = _currentStepIndex.value + 1
        if (nextIdx < tour.steps.size) {
            _currentStepIndex.value = nextIdx
        } else {
            completeTour(userId)
        }
    }

    fun previousStep() {
        if (_currentStepIndex.value > 0) {
            _currentStepIndex.value -= 1
        }
    }

    fun skipTour(userId: String) {
        val tour = _activeTour.value ?: return
        val tourId = tour.tourId
        _activeTour.value = null
        _currentStepIndex.value = 0
        scope.launch {
            repository.setTourStatus(userId, tourId, TourStatus.SKIPPED.name)
            val map = _tourStatuses.value.toMutableMap()
            map[tourId] = TourStatus.SKIPPED
            _tourStatuses.value = map
        }
    }

    fun completeTour(userId: String) {
        val tour = _activeTour.value ?: return
        val tourId = tour.tourId
        _activeTour.value = null
        _currentStepIndex.value = 0
        scope.launch {
            repository.setTourStatus(userId, tourId, TourStatus.COMPLETED.name)
            val map = _tourStatuses.value.toMutableMap()
            map[tourId] = TourStatus.COMPLETED
            _tourStatuses.value = map
        }
    }

    fun resetAllTours(userId: String) {
        _activeTour.value = null
        _currentStepIndex.value = 0
        _tourStatuses.value = emptyMap()
        scope.launch {
            repository.clearAllTours(userId)
        }
    }
}
