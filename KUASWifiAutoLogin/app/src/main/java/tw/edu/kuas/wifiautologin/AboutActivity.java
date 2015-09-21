package tw.edu.kuas.wifiautologin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AboutActivity extends ActionBarActivity implements OnItemClickListener {

	@InjectView(R.id.listView_main) ListView mMainListView;

	private int[] resItems =
			{R.string.open_source_info, R.string.usage_policy, R.string.open_source_licenses};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		ButterKnife.inject(this);
		setUpViews();
	}

	private void setUpViews() {
		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
		for (int resItem : resItems) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("title", getText(resItem));
			listItems.add(map);
		}
		mMainListView.setAdapter(
				new SimpleAdapter(this, listItems, android.R.layout.simple_list_item_1,
						new String[]{"title"}, new int[]{android.R.id.text1}));
		mMainListView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position == 0) {
			final SpannableString message =
					new SpannableString(getText(R.string.open_source_info_content));
			Linkify.addLinks(message, Linkify.WEB_URLS);

			AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.open_source_info)
					.setMessage(message).setPositiveButton(android.R.string.ok, null).show();
			// Make the link clickable
			((TextView) dialog.findViewById(android.R.id.message))
					.setMovementMethod(LinkMovementMethod.getInstance());
		} else if (position == 1) {
			WebView webView = new WebView(this);
			webView.loadUrl("file:///android_asset/usage_policy.html");
			new AlertDialog.Builder(this).setTitle(R.string.usage_policy).setView(webView)
					.setPositiveButton(android.R.string.ok, null).show();
		} else if (position == 2) {
			WebView webView = new WebView(this);
			webView.loadUrl("file:///android_asset/used_licenses.html");
			new AlertDialog.Builder(this).setTitle(R.string.open_source_licenses).setView(webView)
					.setPositiveButton(android.R.string.ok, null).show();
		}
	}
}
