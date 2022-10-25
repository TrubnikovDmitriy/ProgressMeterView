package dv.trubnikov.coolometer.ui.screens.debug

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import dv.trubnikov.coolometer.tools.exhaustive
import dv.trubnikov.coolometer.ui.screens.debug.SecretClickListener.Tap.*
import java.util.*

/**
 * Click listener is watching for the user's taps in order to find the secret gestures.
 *
 * @param secretSequence sequence of taps that user must perform to call the [onDebugOpen]
 * @param onDebugOpen called when a user made the correct taps sequence
 * @param onHintOpen called when a user made a lot of incorrect taps
 */
class SecretClickListener(
    private val secretSequence: List<Tap>,
    private val onHintOpen: (() -> Unit)? = null,
    private val onDebugOpen: () -> Unit,
) : View.OnTouchListener {

    constructor(
        vararg secretSequence: Tap,
        onHintOpen: (() -> Unit)? = null,
        onDebugOpen: () -> Unit,
    ) : this(secretSequence.toList(), onHintOpen, onDebugOpen)

    private val handler = Handler(Looper.getMainLooper())
    private val userSequence = LinkedList<Tap>()
    private val flushTask = Runnable { userSequence.clear() }
    private var incorrectTapCounter = 0

    init {
        if (secretSequence.isEmpty()) {
            throw IllegalArgumentException("Secret sequence of taps can't be empty")
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) {
            return true
        }
        v.performClick()
        handler.removeCallbacks(flushTask)

        val userTap = identifyTheTap(v, event)
        val targetTap = secretSequence[userSequence.size]

        val isNext = userTap == targetTap
        val isFirst = userTap == secretSequence.first()
        when {
            isNext -> {
                userSequence.add(targetTap)
                Unit
            }
            isFirst -> {
                userSequence.clear()
                userSequence.add(userTap)
                Unit
            }
            else -> {
                userSequence.clear()
            }
        }.exhaustive

        // Check for incorrect type count
        if (isNext || isFirst) {
            incorrectTapCounter = 0
        } else {
            ++incorrectTapCounter
        }
        if (incorrectTapCounter == INCORRECT_TAP_THRESHOLD) {
            incorrectTapCounter = 0
            onHintOpen?.invoke()
        }

        if (userSequence == secretSequence) {
            userSequence.clear()
            onDebugOpen()
        } else {
            handler.postDelayed(flushTask, FLUSH_TIMER_MS)
        }
        return true
    }

    private fun identifyTheTap(v: View, event: MotionEvent): Tap {
        val y = event.y
        val topBorder = v.height * BORDER_THRESHOLD
        val bottomBorder = v.height * (1f - BORDER_THRESHOLD)
        val raw = when {
            y <= topBorder -> setOf(LEFT_TOP, TOP, RIGHT_TOP)
            y < bottomBorder -> setOf(LEFT, CENTER, RIGHT)
            bottomBorder <= y -> setOf(LEFT_BOTTOM, BOTTOM, RIGHT_BOTTOM)
            else -> error("Math said that it is impossible")
        }

        val x = event.x
        val leftBorder = v.width * BORDER_THRESHOLD
        val rightBorder = v.width * (1f - BORDER_THRESHOLD)
        val col = when {
            x <= leftBorder -> setOf(LEFT_TOP, LEFT, LEFT_BOTTOM)
            x < rightBorder -> setOf(TOP, CENTER, BOTTOM)
            rightBorder <= x -> setOf(RIGHT_TOP, RIGHT, RIGHT_BOTTOM)
            else -> error("Math said that it is impossible")
        }

        return raw.intersect(col).first()
    }

    enum class Tap {
        LEFT_TOP,
        TOP,
        RIGHT_TOP,
        RIGHT,
        RIGHT_BOTTOM,
        BOTTOM,
        LEFT_BOTTOM,
        LEFT,
        CENTER
    }

    companion object {
        private const val BORDER_THRESHOLD = 0.2f
        private const val FLUSH_TIMER_MS = 1_000L
        private const val INCORRECT_TAP_THRESHOLD = 10
    }
}
