package ilchev.stefan.callblocker.test

import android.content.Context
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import ilchev.stefan.callblocker.BlockPredicate
import ilchev.stefan.callblocker.BuildConfig
import ilchev.stefan.callblocker.MainActivity
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CallTest(
	private val isBlockNonContacts: Boolean,
	private val isExcludeContacts: Boolean,
	private val regex: String,
	private val isMatches: Boolean?,
	private val incomingNumber: String,
	private val isContact: Boolean?,
	private val isBlockedExpected: Boolean
) {

	@Rule
	fun rule(): TestRule {
		InstrumentationRegistry.getInstrumentation().grantRequestedPermissions()
		return activityScenarioRule<MainActivity>()
	}

	private fun assertBlocked(context: Context) {
		val sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
		val blockPredicate = BlockPredicate(sharedPreferences) { isContact }
		Assert.assertEquals(isBlockNonContacts, blockPredicate.isBlockNonContacts)
		Assert.assertEquals(isExcludeContacts, blockPredicate.isExcludeContacts)
		Assert.assertEquals(regex, blockPredicate.regex)
		Assert.assertEquals(isMatches, blockPredicate.isMatches)
		Assert.assertEquals(isBlockedExpected, blockPredicate(incomingNumber))
	}

	@Test
	fun test() {
		val instrumentation = InstrumentationRegistry.getInstrumentation()
		val device = UiDevice.getInstance(instrumentation)
		val block = device.wait("block".toUntil(), 1_000L)
		block.scrollUntil(Direction.DOWN, "block_non_contacts".toUntil()).takeUnless {
			it.isChecked == isBlockNonContacts
		}?.click()
		block.scrollUntil(Direction.DOWN, "exclude_contacts".toUntil()).takeUnless {
			it.isChecked == isExcludeContacts
		}?.click()
		block.scrollUntil(Direction.DOWN, "regex".toUntil()).text = regex
		block.scrollUntil(Direction.DOWN, isMatches.toResourceId().toUntil()).takeUnless {
			it.isChecked
		}?.click()
		assertBlocked(instrumentation.targetContext)
	}

	companion object {

		private fun Boolean?.toResourceId() = when (this) {
			null -> "block_none"
			true -> "block_matches"
			else -> "block_others"
		}

		@Parameterized.Parameters
		@JvmStatic
		fun data() = arrayOf(
			arrayOf(false, false, "a", null, "a", null, false),
			arrayOf(false, false, "a", null, "a", true, false),
			arrayOf(false, false, "a", null, "a", false, false),
			arrayOf(false, false, "a", null, "b", null, false),
			arrayOf(false, false, "a", null, "b", true, false),
			arrayOf(false, false, "a", null, "b", false, false),
			arrayOf(false, false, "a", true, "a", null, true),
			arrayOf(false, false, "a", true, "a", true, true),
			arrayOf(false, false, "a", true, "a", false, true),
			arrayOf(false, false, "a", true, "b", null, false),
			arrayOf(false, false, "a", true, "b", true, false),
			arrayOf(false, false, "a", true, "b", false, false),
			arrayOf(false, false, "a", false, "a", null, false),
			arrayOf(false, false, "a", false, "a", true, false),
			arrayOf(false, false, "a", false, "a", false, false),
			arrayOf(false, false, "a", false, "b", null, true),
			arrayOf(false, false, "a", false, "b", true, true),
			arrayOf(false, false, "a", false, "b", false, true),
			arrayOf(false, true, "a", null, "a", null, false),
			arrayOf(false, true, "a", null, "a", true, false),
			arrayOf(false, true, "a", null, "a", false, false),
			arrayOf(false, true, "a", null, "b", null, false),
			arrayOf(false, true, "a", null, "b", true, false),
			arrayOf(false, true, "a", null, "b", false, false),
			arrayOf(false, true, "a", true, "a", null, true),
			arrayOf(false, true, "a", true, "a", true, false),
			arrayOf(false, true, "a", true, "a", false, true),
			arrayOf(false, true, "a", true, "b", null, false),
			arrayOf(false, true, "a", true, "b", true, false),
			arrayOf(false, true, "a", true, "b", false, false),
			arrayOf(false, true, "a", false, "a", null, false),
			arrayOf(false, true, "a", false, "a", true, false),
			arrayOf(false, true, "a", false, "a", false, false),
			arrayOf(false, true, "a", false, "b", null, true),
			arrayOf(false, true, "a", false, "b", true, false),
			arrayOf(false, true, "a", false, "b", false, true),
			arrayOf(true, false, "a", null, "a", null, false),
			arrayOf(true, false, "a", null, "a", true, false),
			arrayOf(true, false, "a", null, "a", false, true),
			arrayOf(true, false, "a", null, "b", null, false),
			arrayOf(true, false, "a", null, "b", true, false),
			arrayOf(true, false, "a", null, "b", false, true),
			arrayOf(true, false, "a", true, "a", null, true),
			arrayOf(true, false, "a", true, "a", true, true),
			arrayOf(true, false, "a", true, "a", false, true),
			arrayOf(true, false, "a", true, "b", null, false),
			arrayOf(true, false, "a", true, "b", true, false),
			arrayOf(true, false, "a", true, "b", false, true),
			arrayOf(true, false, "a", false, "a", null, false),
			arrayOf(true, false, "a", false, "a", true, false),
			arrayOf(true, false, "a", false, "a", false, true),
			arrayOf(true, false, "a", false, "b", null, true),
			arrayOf(true, false, "a", false, "b", true, true),
			arrayOf(true, false, "a", false, "b", false, true),
			arrayOf(true, true, "a", null, "a", null, false),
			arrayOf(true, true, "a", null, "a", true, false),
			arrayOf(true, true, "a", null, "a", false, true),
			arrayOf(true, true, "a", null, "b", null, false),
			arrayOf(true, true, "a", null, "b", true, false),
			arrayOf(true, true, "a", null, "b", false, true),
			arrayOf(true, true, "a", true, "a", null, true),
			arrayOf(true, true, "a", true, "a", true, false),
			arrayOf(true, true, "a", true, "a", false, true),
			arrayOf(true, true, "a", true, "b", null, false),
			arrayOf(true, true, "a", true, "b", true, false),
			arrayOf(true, true, "a", true, "b", false, true),
			arrayOf(true, true, "a", false, "a", null, false),
			arrayOf(true, true, "a", false, "a", true, false),
			arrayOf(true, true, "a", false, "a", false, true),
			arrayOf(true, true, "a", false, "b", null, true),
			arrayOf(true, true, "a", false, "b", true, false),
			arrayOf(true, true, "a", false, "b", false, true)
		)
	}
}
