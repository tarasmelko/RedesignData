package dp.ws.popcorntime.googlecast;

public interface CastPopcornListener {
	public void onCastConnection();

	public void onCastRouteSelected();

	public void onCastRouteUnselected(long position);

	public void onCastStatePlaying();

	public void onCastStatePaused();

	public void onCastStateIdle();

	public void onCastStateBuffering();

	public void onCastMediaLoadSuccess();

	public void onCastMediaLoadCancelInterrupt();

	public void teardown();
}