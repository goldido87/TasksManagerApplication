/*
 * Copyright (C) 2013 Ido Gold & Sahar Rehani
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package il.ac.shenkar.todos;

import java.util.Calendar;

import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Application utilities
 */
public class Utils
{
	// the default task description
	public static final String DEFAULT_DESCRIPTION = "No Description";
	// the default task location alert
	public static final String DEFUALT_LOCATION = "No Location";
	// the default task notification text
	public static final String DEFUALT_NOTIFICATION = "No Notification";
	// code for google analytics purpose
	public static final String GOOGLE_ANALYTICS_CODE = "UA-37448489-1";
	public static final String GPS_ALERT = "Enable GPS ?";
	public static final String DELETE_ALL_ALERT = "Delete all tasks ?";
	public static final String EDIT_TITLE = "Edit Title";
	public static final String EDIT_DESCRIPTION = "Edit Description";
	public static final String SET_LOCATION = "Set Location";

	public static final int DELETE_SOUND = R.raw.recycle;
	public static final int ALARM_ON_IMAGE = R.drawable.alarm_turquoise;
	public static final int ALARM_OFF_IMAGE = R.drawable.alarm_gray3;
	// number of similar GPS search results 
	public static final int NUMBER_OF_GPS_RESULTS = 3;
	// GPS search radius in meters
	public static final int SEARCH_RADIUS = 1000;
	
	// alert dialog options
	public static final int DIALOG_YES_NO_MESSAGE = 1;
	public static final int DIALOG_TASK_DETAILS = 2;
	public static final int DIALOG_LONG_TEXT_ENTRY = 3;
	public static final int DIALOG_TEXT_ENTRY = 4;

	/**
	 * Make long toast message
	 * 
	 * @param context
	 * @param text - string to show
	 */
	public static void makeToast(Context context, String text)
	{
		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
	}

	/**
	 * Shows and hides the no tasks image
	 * according to list size
	 */
	public static void checkAlertImageTrigger(int listSize)
	{
		if (listSize == 0)
		{
			MainActivity.noTasksImage.setVisibility(View.VISIBLE);
		}
		else if (listSize == 1)
		{
			MainActivity.noTasksImage.setVisibility(View.GONE);
		}
	}

	/**
	 * For handling non hardware menu button phones.
	 * shows the software button if necessary
	 * 
	 * @param window - activity's window
	 */
	public static void addLegacyOverflowButton(Window window)
	{
		if (window.peekDecorView() == null) 
		{
			throw new RuntimeException("Must call addLegacyOverflowButton() after setContentView()");
		}

		try 
		{
			window.addFlags(WindowManager.LayoutParams.class.getField("FLAG_NEEDS_MENU_KEY").getInt(null));
		}
		catch (NoSuchFieldException e)
		{
			// Ignore since this field won't exist in most versions of Android
		}
		catch (IllegalAccessException e) 
		{

		}
	}

	/**
	 * Create's a random number between two given numbers
	 * 
	 * @param first - lowest number
	 * @param second - highest number
	 * @return the random number
	 */
	public static int randomNumber(int first, int second)
	{
		int randomNum;

		if (first < second)
		{
			randomNum = (int) (second * Math.random() + first);
			return randomNum;
		}

		else if (second < first)
		{
			randomNum = (int) (first * Math.random() + second);
			return randomNum;
		}

		else // first and second are equal
		{
			System.out.println("The two numbers are equal");
			return first;
		}
	}
	
	/**
     * Checks if two calendars represent the same day ignoring time.
     * 
     * @param cal1  the first calendar, not altered, not null
     * @param cal2  the second calendar, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either calendar is <code>null</code>
     */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) 
    {
        if (cal1 == null || cal2 == null)
        {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }
}
