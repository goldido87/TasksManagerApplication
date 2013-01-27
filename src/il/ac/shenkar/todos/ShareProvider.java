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

import android.content.Intent;
import android.view.ContextMenu;
import android.widget.ShareActionProvider;

/**
 * Creates a share menu 
 */
public class ShareProvider 
{
	private ShareActionProvider mShareActionProvider;
	
	public ShareProvider(ContextMenu menu)
	{
		// Fetch and store ShareActionProvider
		mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.menu_item_share).getActionProvider();
	}
	
	/**
	 * Opens the share menu
	 * 
	 * @param title - task title to share
	 * @param description - task description to share
	 */
	public void makeShareMenu(String title, String description)
	{
		setShareIntent(createShareIntent(title,description));
	}
	
	/**
	 * Call to update the share intent
	 * 
	 * @param shareIntent
	 */
	public void setShareIntent(Intent shareIntent) 
	{
	    if (mShareActionProvider != null)
	    {
	        mShareActionProvider.setShareIntent(shareIntent);
	    }
	}
	
	/** 
	 * Create the share intent.
	 * 
	 * @param shareTitleText
	 * @param shareDescriptionText
	 * @return the share intent
	 */
	public Intent createShareIntent(String shareTitleText, String shareDescriptionText)
	{
		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_TEXT, "Title: " + shareTitleText + 
				"\nDescrirpion: " + shareDescriptionText);
		shareIntent.setType("text/plain");
		return shareIntent;
	}
}
