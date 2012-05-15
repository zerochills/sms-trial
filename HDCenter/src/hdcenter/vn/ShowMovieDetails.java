package hdcenter.vn;

import hdcenter.vn.data.DataCenter;
import hdcenter.vn.data.IRequestListener;
import hdcenter.vn.entities.MovieDetailsItem;
import hdcenter.vn.entities.MovieItem;
import hdcenter.vn.ui.SecondaryFrame;
import hdcenter.vn.utils.Helpers;
import hdcenter.vn.utils.Logger;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import dtd.phs.lib.ui.images_loader.ImageLoader;

public class ShowMovieDetails 
extends Activity 
implements IRequestListener 
{
	private static final int DIALOG_WAIT = 0;

	private static final String DEFAULT_CINEMA_ID = "1001";
	
	public static final String EXTRA_MODE = "extra_mode";
	public static final String MODE_DETAILS = "mode_detail";
	
	public static MovieItem passedSummaryItem = null;
	private MovieItem summItem = null;
	private ImageView ivAvatar;
	private TextView tvName;
	private TextView tvVnName;
	private TextView tvRating;
	private Button btTrailer;
	private TextView tvStarring;
	private ImageLoader imageLoader;
	private String id;
	private Handler handler = new Handler();
	private MovieDetailsItem movieDetails;
	private OnTrailerClickListener onTrailerButtonClickListener;

	private TextView tvDirector;

	private SecondaryFrame secondaryFrames;


	private boolean isCinemaMovie;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.movie_details);
		imageLoader = ImageLoader.getInstance(this,R.drawable.movie_stub, handler);
		extractInput();
		createViews();
		preloadSummaryInformation();



	}

	private void createViews() {
		ivAvatar = (ImageView) findViewById(R.id.ivAvatar);
		tvName = (TextView) findViewById(R.id.tvName);
		tvVnName = (TextView) findViewById(R.id.tvVnName);
		tvRating = (TextView) findViewById(R.id.tvRating);
		tvDirector = (TextView) findViewById(R.id.tvDirector);
		
		btTrailer = (Button) findViewById(R.id.btTrailer);
		onTrailerButtonClickListener = new OnTrailerClickListener();
		btTrailer.setOnClickListener(onTrailerButtonClickListener);
				
		tvStarring = (TextView) findViewById(R.id.tvStarring);
		
//		tvDescription = (TextView) findViewById(R.id.tvDescription);
		secondaryFrames = new SecondaryFrame((LinearLayout) findViewById(R.id.int_tab_host), handler);
		if ( ! isCinemaMovie ) secondaryFrames.displayOnlyDescription();
	}

	private void preloadSummaryInformation() {
		if ( this.summItem != null ) {
			tvName.setText(summItem.getName());
			tvVnName.setText(summItem.getVnName());
			tvRating.setText(summItem.getRating());
		} else {
			Logger.logError("Summary item is NULL !");
		}
	}

	private void extractInput() {
		this.summItem = passedSummaryItem;		
		passedSummaryItem = null;
		Intent src = getIntent();
		if ( src.getStringExtra(EXTRA_MODE) != null ) {
			this.isCinemaMovie = true;
		} else this.isCinemaMovie = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			id = summItem.getId();
			DataCenter.requestMovieDetails(this,id,handler);
		} catch (NullPointerException e) {
			showErrorToast(e);
		}
	}
	
	@Override
	protected void onPause() {		
		super.onPause();
		imageLoader.clear();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	public void onBackPressed() {	
		super.onBackPressed();
		Helpers.exitTransition(this);
	}

	private void showErrorToast(Exception e) {
		Logger.logError(e);
		Helpers.showToast(getApplicationContext(),R.string.error_please_retry);
	}

	@Override
	public void onRequestError(Exception e) {
		showErrorToast(e);
	}

	@Override
	public void onRequestSuccess(Object data) {
		movieDetails = (MovieDetailsItem)data;
		imageLoader.loadImage(movieDetails.getImageURL(), ivAvatar);
		tvVnName.setText(movieDetails.getVnName() + " (" + movieDetails.getYear() + ")");
		tvDirector.setText(movieDetails.getDirector());
		tvStarring.setText(movieDetails.getStarrings());
		
		secondaryFrames.setDescription(movieDetails.getDescription());
		if ( isCinemaMovie )
			secondaryFrames.requestCalendar(lastChoosenCinema(),movieDetails.getMovieCalendarId());
		
		onTrailerButtonClickListener.setVideoId(movieDetails.getYoutubeId());
	}

	
	private String lastChoosenCinema() {
		return DEFAULT_CINEMA_ID;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_WAIT:
			return ProgressDialog.show(this, "", getResources().getString(R.string.loading_please_wait)); 
		}
		return super.onCreateDialog(id);
	}
	public class OnTrailerClickListener implements View.OnClickListener {


		private String id;

		public OnTrailerClickListener() {
			super();
			id = null;
		}
		public void setVideoId(String id) {
			this.id = id;
		}

		@Override
		public void onClick(View v) {
			if(id == null || id.trim().equals("")){
				Helpers.toast(getApplicationContext(),R.string.no_trailer);
				return;
			}
			
			//Use Youtube app
			startActivity(
					new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v="+id)));

			
//			Intent lVideoIntent = new Intent(null, Uri.parse("ytv://"+id), ShowMovieDetails.this, OpenYouTubePlayerActivity.class);
//			startActivity(lVideoIntent);
			
			//Use default video player
//			ProcessYoutubeIdTask task = new ProcessYoutubeIdTask(ShowMovieDetails.this,DIALOG_WAIT);
//			task.execute(id);
			

		}

	}

}
