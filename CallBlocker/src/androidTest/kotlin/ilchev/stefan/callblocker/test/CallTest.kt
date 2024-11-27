package ilchev.stefan.callblocker.test

import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import ilchev.stefan.callblocker.BlockPredicate
import ilchev.stefan.callblocker.MainActivity
import ilchev.stefan.callblocker.sharedPreferences
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
		instrumentation.grantRequestedPermissions()
		return activityScenarioRule<MainActivity>()
	}

	private fun assertBlocked() {
		val blockPredicate = BlockPredicate(targetContext.sharedPreferences) { isContact }
		Assert.assertEquals(isBlockNonContacts, blockPredicate.isBlockNonContacts)
		Assert.assertEquals(isExcludeContacts, blockPredicate.isExcludeContacts)
		Assert.assertEquals(regex, blockPredicate.regex)
		Assert.assertEquals(isMatches, blockPredicate.isMatches)
		Assert.assertEquals(isBlockedExpected, blockPredicate(incomingNumber))
	}

	@Test
	fun test() {
		val device = UiDevice.getInstance(instrumentation)
		val block = device.wait(Until.findObject("block".toBy()), TIMEOUT)
		block.scrollUntil(Direction.DOWN, Until.findObject("block_non_contacts".toBy())).takeUnless {
			it.isChecked == isBlockNonContacts
		}?.also {
			it.click()
			Assert.assertTrue(it.wait(Until.checked(isBlockNonContacts), TIMEOUT))
		}
		block.scrollUntil(Direction.DOWN, Until.findObject("exclude_contacts".toBy())).takeUnless {
			it.isChecked == isExcludeContacts
		}?.also {
			it.click()
			Assert.assertTrue(it.wait(Until.checked(isExcludeContacts), TIMEOUT))
		}
		block.scrollUntil(Direction.DOWN, Until.findObject("regex".toBy())).takeUnless {
			it.text == regex
		}?.also {
			it.text = regex
			Assert.assertTrue(it.wait(Until.textEquals(regex), TIMEOUT))
		}
		block.scrollUntil(Direction.DOWN, Until.findObject(isMatches.toBy())).takeUnless {
			it.isChecked
		}?.also {
			it.click()
			Assert.assertTrue(it.wait(Until.checked(true), TIMEOUT))
		}
		assertBlocked()
	}

	companion object {

		private fun Boolean?.toBy() = when (this) {
			null -> "block_none"
			true -> "block_matches"
			else -> "block_others"
		}.toBy()

		@Parameterized.Parameters
		@JvmStatic
		fun data() = arrayOf<Array<Any?>>(
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
