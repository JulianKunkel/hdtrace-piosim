
/** Version Control Information $Id: ScrollableObject.java 261 2009-05-02 11:39:11Z kunkel $
 * @lastmodified    $Date: 2009-05-02 13:39:11 +0200 (Sa, 02. Mai 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 261 $ 
 */

//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.


/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author Anthony Chan (Jumpshot 4), Julian M. Kunkel
 */

package de.viewer.zoomable;


import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import de.drawable.TimeBoundingBox;
import de.hd.pvs.TraceFormat.util.Epoch;
import de.viewer.common.Debug;
import de.viewer.common.IAutoRefreshable;
import de.viewer.common.ModelTime;
import de.viewer.common.Parameters;
import de.viewer.dialog.InfoDialog;
import de.viewer.dialog.InfoDialogForTime;

/**
 * Realizes a object which can be zoomed and scrolled.
 * The drawing of complex content is by default done by a worker thread.
 * Also additional computation to prepare the drawing can be done by the worker thread.
 * 
 * @author julian
 *
 */
public abstract class ScrollableObject extends JComponent
implements ScrollableView, IAutoRefreshable
{
	private static final long serialVersionUID = 8964666662265862335L;

	//  The least number of images for this class to work is "3"
	public    static final int   NumImages = 3;
	protected static final int   NumViewsPerImage = 1;
	protected static final int   NumViewsTotal = NumImages * NumViewsPerImage;

	final private   ModelTime          modelTime;
	final private   ScrollbarTimeModel scrollbarTimeModel;
	private   BufferedImage      offscreenImages[ /* NumImages */ ];

	// The start and end of the image(s) in the user time coordinates
	private   TimeBoundingBox    tImages[ /* NumImages  */ ];
	private   double             tImage_extent;
	private   TimeBoundingBox    tImages_all;  // extremes of tImages[]

	// shorthand for some convenient constant
	private   int                half_NumImages;

	/**
	 * The viewport this object is added to.
	 */
	final private ViewportTime viewport;

	// There are 2 kinds of indexes to label the image in the array buffer.
	// Both of the indexes are in the range of {0 : NumImages-1}.
	// - The 1st kind is called image order.  The image order for the
	//   image where viewport is in is always half_NumImages
	// - The 2nd kind is called image index.  The image index for the
	//   image where viewport is in can be any integer in {0 : NumImages-1}
	//   The variable, cur_img_idx, serves to keep track where this image
	//   is in the image array buffer.
	private   int                cur_img_idx;

	// The size of an image in pixel coordinates
	private   Dimension          image_size;
	// The size of this JCompoent in pixel coordinates
	private   Dimension          component_size;
	
	// screen properties
	private int iViewWidth;// No. of View pixel per unit time
	

	// decides whether a call of redrawIfAutoRedraw refreshes
	boolean autoRefresh = Parameters.ACTIVE_REFRESH;

	// if enabled then a background thread is used to prepare the images and to compute additional work
	boolean useBackgroundThread = true;

	// contains the pending rendering jobs and process with FIFO order.
	private LinkedList<BackgroundRendering> renderingJobs =  new LinkedList<BackgroundRendering>();

	private BackgroundRendering currentTask = null;
	private BackgroundThread backgroundThread = null;


	/**
	 * Return the real height the image has (not the viewport height).
	 */
	public abstract int getRealImageHeight();

	/**
	 * Get an object of a given type from the clicked position:
	 * @param view_click
	 * @return
	 */
	public abstract Object getObjectAt( final Point view_click );

	public InfoDialog getPropertyAt( final Point  view_click){
		final CoordPixelImage coord_xform;  // Local Coordinate Transform
		coord_xform = new CoordPixelImage( this );
		final Epoch realTime =  getModelTime().getGlobalMinimum().add(coord_xform.convertPixelToTime( view_click.x ));
		return getTimePropertyAt(realTime);
	}


	public ScrollableObject(ScrollbarTimeModel scrollbarTimeModel, ViewportTime viewport )
	{
		this.scrollbarTimeModel = scrollbarTimeModel;
		this.viewport = viewport;

		this.modelTime = scrollbarTimeModel.getModelTime();
		offscreenImages = new BufferedImage[ NumImages ];

		tImages         = new TimeBoundingBox[ NumImages ];
		for ( int idx = 0; idx < NumImages; idx++ )
			tImages[ idx ] = new TimeBoundingBox();

		tImages_all     = new TimeBoundingBox();		

		super.setDoubleBuffered( false );
		setOpaque(true);

		// Initialize the current image index and each image's time bound
		half_NumImages = NumImages / 2;
		setImagesInitTimeBounds();

		image_size     = new Dimension( 0, 0 );
		component_size = super.getSize();

		// Enable debugging graphics option
		//setDebugGraphicsOptions( DebugGraphics.LOG_OPTION
		//		| DebugGraphics.BUFFERED_OPTION
		//		| DebugGraphics.FLASH_OPTION );		

		// Check if the number of images is an ODD number
		if ( NumImages % 2 == 0 || NumImages < 3 ) {
			String err_msg = "ScrollableObject(): NumImages = "
				+ NumImages + " which is invalid, "
				+ "i.e. either an EVEN number or < 3.";
			throw new IllegalStateException( err_msg );
			// System.exit( 1 );
		}
	}

	// tImages_all needs to be synchronized with tImages[]
	private void setImagesInitTimeBounds()
	{
		double model_view_extent = modelTime.getViewExtent();
		tImage_extent            = NumViewsPerImage * model_view_extent;
		tImages_all.reinitialize();

		int img_idx = 0;
		tImages[ img_idx ].setEarliestTime( modelTime.getViewPosition()
				- 0.5 * model_view_extent * (NumViewsTotal - 1) );
		tImages[ img_idx ].setLatestFromEarliest( tImage_extent );
		tImages_all.affectTimeBounds( tImages[ img_idx ] );
		for ( img_idx = 1; img_idx < NumImages; img_idx++ ) {
			tImages[ img_idx ].setEarliestTime(
					tImages[ img_idx - 1 ].getLatestTime() );
			tImages[ img_idx ].setLatestFromEarliest( tImage_extent );
			tImages_all.affectTimeBounds( tImages[ img_idx ] );
		}
		// initialize cur_img_idx in offscreenImages[]
		cur_img_idx = half_NumImages;
	}

	public TimeBoundingBox getTimeBoundsOfImages()
	{
		return new TimeBoundingBox( tImages_all );
	}


	// getValidImageIndex() convert an index to be
	// { 0 <= image index < NumImages }
	//  i.e. implements periodic boundary condition
	private int getValidImageIndex( int img_idx )
	{
		int adj_img_idx;
		adj_img_idx = img_idx % NumImages;
		if ( adj_img_idx < 0 )
			return adj_img_idx + NumImages;
		else
			return adj_img_idx;
	}

	// getPrevImageIndex() and getNextImageIndex() implement circular buffer[]
	private int getNearPastImageIndex( int img_idx )
	{
		if(img_idx - 1 < 0){
			return NumImages -1;
		}
		return img_idx - 1;
	}

	private int getNearFutureImageIndex( int img_idx )
	{
		return (img_idx + 1) % NumImages;
	}

	private int getNumImagesMoved()
	{
		double cur_tView_init   = modelTime.getViewPosition();
		double cur_tView_extent = modelTime.getViewExtent();
		double cur_tView_final  = cur_tView_init + cur_tView_extent;

		if ( Debug.isActive() ) {
			Debug.println( "ScrollableObject: getNumImagesMoved()'s START: " );
			Debug.println( "cur_tView_init  = " + cur_tView_init + ",  "
					+ "cur_tView_final = " + cur_tView_final );
			Debug.println( "tImages[ cur ] = " + tImages[ cur_img_idx ] );
		}

		double view_init_in_imgs, view_final_in_imgs;
		int Nimages_moved_fwd, Nimages_moved_back, Nimages_moved;
		double tImages_init;

		// compute the beginning image index in the image buffer
		tImages_init = tImages_all.getEarliestTime();
		if ( Debug.isActive() )
			Debug.println( "ScrollableObject: getNumImagesMoved() "
					+ "tImages_init = " + tImages_init );

		// the integer part of view_init_in_imgs is the image order of
		// the image where cur_tView_init is in.  Nimages_moved_fwd is
		// the relative image order w.r.t. center image in the buffer.
		view_init_in_imgs  = ( cur_tView_init  - tImages_init )
		/ tImage_extent;
		Nimages_moved_fwd  = (int) Math.floor( view_init_in_imgs )
		- half_NumImages;
		// the integer part of view_final_in_imgs is the image order of
		// the image where cur_tView_final is in.  Nimages_moved_back is
		// the relative image order w.r.t. center image in the buffer.
		view_final_in_imgs = ( cur_tView_final - tImages_init )
		/ tImage_extent;
		Nimages_moved_back = (int) Math.floor( view_final_in_imgs )
		- half_NumImages;

		Nimages_moved = 0;
		if ( Nimages_moved_fwd > 0 )
			Nimages_moved = Nimages_moved_fwd;
		if ( Nimages_moved_back < 0 )
			Nimages_moved = Nimages_moved_back;

		if ( Debug.isActive() ) {
			Debug.println( "ScrollableObject: getNumImagesMoved() "
					+ "Nmages_moved = " + Nimages_moved );
			Debug.println( "ScrollableObject: getNumImagesMoved()'s END: " );
		}

		return Nimages_moved;
	}

	// scrollable_image interface when the view is zoomed in or out.
	public boolean checkToZoomView()
	{
		if ( Debug.isActive() )
			Debug.println( "ScrollableObject: checkToZoomView()'s START: " );
		double cur_tView_extent = modelTime.getViewExtent();
		if ( cur_tView_extent * NumViewsPerImage != tImage_extent ) {
			setImagesInitTimeBounds();

			for ( int img_idx = 0; img_idx < NumImages; img_idx++ )
				scheduleToDrawOneImageInBackground(  img_idx);

			executeBackgroundThread();
			return true;
		}

		return false;
	}

	// scrollable_image interface when the view is scrolled by the scrollbar.
	public boolean checkToScrollView()
	{
		int Nimages_moved;
		int img_mv_dir, img_idx;
		int past_img_idx, future_img_idx;
		int start_idx;
		int idx;

		if ( Debug.isActive() )
			Debug.println( "ScrollableObject: checkToScrollView()'s START: " );
		//  Using the old cur_img_idx as the center of images to locate
		//  the images needed to be redrawn
		img_mv_dir = 0;
		Nimages_moved = getNumImagesMoved();

		if ( Nimages_moved != 0 ) {
			if ( Math.abs( Nimages_moved ) <= NumImages ) {
				img_mv_dir = Nimages_moved / Math.abs( Nimages_moved );

				// locate the end image index in same direction as img_mv_dir
				start_idx = getValidImageIndex( cur_img_idx	+ img_mv_dir * half_NumImages);

				// Determine tImages_all first before invoking
				// initializeAllOffImages() and finalizeAllOffImages()
				for ( idx = 1; idx <= Math.abs( Nimages_moved ); idx++ ) {
					img_idx = getValidImageIndex( start_idx	+ img_mv_dir * idx );
					if ( Debug.isActive() )
						Debug.println( "ScrollableObject: checkToScrollView() "
								+ "cur_img_idx = " + cur_img_idx + ", "
								+ "start_idx = " + start_idx + ", "
								+ "img_idx = " + img_idx );

					// synchronize tImages_all with tImages[]
					// remove unneeded tImage[ img_idx ] from tImages_all
					if ( ! tImages_all.remove( tImages[ img_idx ] ) )
						System.err.println( "ScrollableObject: "
								+ "checkToScrollView() "
								+ "tImages[" + img_idx + "] = "
								+ tImages[ img_idx ] + " does NOT "
								+ "cover the end of tImages_all = "
								+ tImages_all );
					if ( img_mv_dir > 0 ) {
						past_img_idx = getNearPastImageIndex( img_idx );
						tImages[ img_idx ].setEarliestTime(
								tImages[ past_img_idx ].getLatestTime() );
						tImages[ img_idx ].setLatestFromEarliest(
								tImage_extent );
					}
					else { // img_mv_dir < 0
						future_img_idx = getNearFutureImageIndex( img_idx );
						tImages[ img_idx ].setLatestTime(
								tImages[ future_img_idx ].getEarliestTime() );
						tImages[ img_idx ].setEarliestFromLatest(
								tImage_extent );
					}
					// update tImages_all to reflect changes in tImages[]
					// so drawOneOffImage() can use tImages_all
					tImages_all.affectTimeBounds( tImages[ img_idx ] );
				}

				// Update the offscreenImages[] of those scrolled
				if ( img_mv_dir > 0 ){
					//for ( idx = 1; idx <= Math.abs( Nimages_moved ); idx++ ) {
					for ( idx = Math.abs( Nimages_moved ); idx >=1; idx-- ) {
						img_idx = getValidImageIndex( start_idx	+ img_mv_dir * idx );
						scheduleToDrawOneImageInBackground( img_idx );
					}
				}else{
					for ( idx = 1; idx <= Math.abs( Nimages_moved ); idx++ ) {
						img_idx = getValidImageIndex( start_idx
								+ img_mv_dir * idx );
						scheduleToDrawOneImageInBackground( img_idx );
					}
				}

				// Update cur_img_idx in the offscreenImages[]
				cur_img_idx = getValidImageIndex( cur_img_idx + Nimages_moved );

				executeBackgroundThread();
				return true;
			}
			else {  // Math.abs( Nimages_moved ) > NumImages
				if ( Debug.isActive() ) {
					Debug.println( "****************************************" );
					Debug.println( "ScrollableObject: checkToScrollView() "
							+ "| Nimages_moved( " + Nimages_moved
							+ " ) | >= NumImages( " + NumImages + " )" );
				}
				setImagesInitTimeBounds();

				cancelRedrawing();
				for ( img_idx = 0; img_idx < NumImages; img_idx++ )
					scheduleToDrawOneImageInBackground( img_idx );

				executeBackgroundThread();

				return true;
			}


		}   // Endof if ( Nimages_moved != 0 )
		return false;
	}

	protected int time2pixel( double time_coord )
	{
		return (int) Math.round( ( time_coord - tImages_all.getEarliestTime() )
				* getViewPixelsPerUnitTime() );
	}
	
	public double getViewPixelsPerUnitTime(){
		return iViewWidth / modelTime.getViewExtent();
	}

	protected double pixel2time( int pixel_coord )
	{
		return (double) pixel_coord / getViewPixelsPerUnitTime() + tImages_all.getEarliestTime();
	}

	// scrollable_image interface. This returns pixel coordinate in the image
	// buffer measured from the far left of the buffer.
	public int getXaxisViewPosition()
	{
		if ( Debug.isActive() )
			Debug.println( "ScrollableObject: getViewPosition() : "
					+ "model.getTimeViewPosition()="
					+ modelTime.getViewPosition() );
		//System.out.println(tImages_all.getEarliestTime());
		return time2pixel( modelTime.getViewPosition()  );
	}

	/**
	 * image_endtimes:  endtimes of the OffScreenImage, image
	 * This function is called by a worker thread.
	 */
	protected abstract void drawOneImageInBackground( Image image, final TimeBoundingBox  image_endtimes );

	static class BackgroundRendering{
		final int imagePos;
		final TimeBoundingBox box;
		final BufferedImage image;

		public BackgroundRendering(int imagePos, BufferedImage image, TimeBoundingBox box) {
			this.box = box;
			this.imagePos = imagePos;
			this.image = image;
		}
	}

	/**
	 * At most one of this thread is executed.  
	 * Prepares the images and does additional computation.
	 * 
	 * @author julian
	 */
	class BackgroundThread extends SwingWorker<Void, Void>{
		boolean abortCurrentJob = false;

		@Override
		protected Void doInBackground() {
			while (true) {
				final BackgroundRendering job = getNextJob();
				if(job == null){
					break;
				}

				// for testing:
				//try{Thread.sleep(1000);}catch(Exception e){}

				try{
					drawOneImageInBackground(job.image, job.box);
				}catch(Throwable e){
					e.printStackTrace();
				}
				if(abortCurrentJob){
					abortCurrentJob = false;
					continue;
				}

				// repaint viewport to show marks (added by viewport) correctly.
				viewport.repaint();
			}

			return null;
		}
	}

	/**
	 * Called by the background Thread
	 * @return 
	 */
	private synchronized BackgroundRendering getNextJob(){
		while(renderingJobs.isEmpty()){
			try{
				wait();
			}catch(InterruptedException e){
				System.out.println("Thread interrupted");
				backgroundThread = null;
				return null;
			}
		}
		currentTask = renderingJobs.pollLast(); 
		return currentTask;
	}

	/**
	 * Cancel redrawing of the image with the given position
	 * @param imagePos
	 */
	private synchronized void cancelRedrawing(int imagePos){
		renderingJobs.remove( new BackgroundRendering(imagePos, null, null));

		if(backgroundThread != null && (currentTask == null || currentTask.imagePos == imagePos)){
			backgroundThread.abortCurrentJob = true;
		}

		// clear the image to ensure the user sees correct information:
		offscreenImages[imagePos].getGraphics().clearRect(0, 0, 
				offscreenImages[imagePos].getWidth(), offscreenImages[imagePos].getHeight());
	}

	/**
	 * Cancel redrawing of all pending jobs
	 */
	public synchronized void cancelRedrawing(){
		renderingJobs.clear();

		for(int imagePos = 0; imagePos < NumImages; imagePos++){
			// clear the image to ensure the user sees correct information:
			offscreenImages[imagePos].getGraphics().clearRect(0, 0, 
					offscreenImages[imagePos].getWidth(), offscreenImages[imagePos].getHeight());
		}
	}

	private synchronized void scheduleToDrawOneImageInBackground( int imagePos ){
		final BufferedImage image = offscreenImages[imagePos];

		cancelRedrawing(imagePos);

		renderingJobs.push(new BackgroundRendering(imagePos, image, tImages[ imagePos ]));

		notify();
	}

	/**
	 * Activate the background thread if necessary to redraw the pending jobs.
	 */
	private void executeBackgroundThread(){
		if( isUseBackgroundThread()){
			if(backgroundThread == null || backgroundThread.isDone() ){
				// create background thread:
				backgroundThread = new BackgroundThread(); 
				backgroundThread.execute();
			}			
		}else{ // do not start background thread:
			for(BackgroundRendering task: renderingJobs){
				drawOneImageInBackground(task.image, task.box);
			}
		}		
	}

	@Override
	protected void paintComponent(Graphics g)
	{	
		final BufferedImage images[] = offscreenImages;
		//final boolean isThreadFinished = isBackgroundThreadFinished(); 

		if(images[ 0 ] == null)
			return;

		if ( Debug.isActive() ) {
			Debug.println( "ScrollableObject : paintComponent()'s START : " );
			Debug.println( "ScrollableObject : paintComponent() "
					+ "g.getClipBounds() = " + g.getClipBounds() );
			Debug.println( "ScrollableObject : paintComponent() "
					+ "this = " + this );
		}

		int img_idx, screen_img_pos;
		int side_idx, side_bit, side_offset;		


		// draw Image in the middle of offscreenImages[]
		img_idx = cur_img_idx;
		screen_img_pos =  half_NumImages * image_size.width;

		g.drawImage( images[ img_idx ], screen_img_pos, 0, this );

		// Images are drawn alternatively around the middle of
		// offscreenImages[].  The drawing starts with image in the
		// same direction as viewport moving direction(i.e. opposite
		// to image moving direction), then jumps to the image on the
		// other side of the middle image in the image buffer.
		//
		// The order of drawing aims to optimize the refresh rate of
		// image in the buffer, so that user does NOT notice the image
		// is being redrawn.  The code is written to anticipate what
		// user wants to see next in the image buffer.  The assumption
		// is that when viewport moves, it will be more likely to keep
		// moving in the same direction.
		for ( side_idx = 1; side_idx <= half_NumImages; side_idx++ ) {
			for ( side_bit = 1; side_bit >= -1; side_bit -= 2 ) {
				// viewport_move_direction = -1 * image_move_direction
				side_offset = side_bit * side_idx;
				img_idx = getValidImageIndex( cur_img_idx + side_offset );

				screen_img_pos = ( half_NumImages + side_offset ) * image_size.width;

				g.drawImage( images[ img_idx ],	screen_img_pos, 0, this );
			}
		}
		if ( Debug.isActive() )
			Debug.println( "ScrollableObject : paintComponent()'s END : " );
	}

	final public void redrawIfAutoRedraw(){
		if(isAutoRefresh())
			forceRedraw();
	}

	/**
	 * force to redraw the scrollable object
	 */
	final public void forceRedraw(){
		// compute the last image index in the image buffer
		int img_idx = getValidImageIndex( cur_img_idx + half_NumImages + 1 );

		cancelRedrawing();

		for ( int idx = 0; idx < NumImages; idx++ ) {
			scheduleToDrawOneImageInBackground( img_idx );
			img_idx = getNearFutureImageIndex( img_idx );
		}

		executeBackgroundThread();
	}

	@Override
	final public void resized() {
		final int visWidth = viewport.getWidth();
		final int newWidth = visWidth * NumViewsPerImage;
		final int newHeight = getRealImageHeight();


		if(image_size.getSize().width == newWidth && image_size.getSize().height == newHeight ){
			// not resized at all, but does not work for some cases
			//return;
		}
		
		// update local visible width
		this.iViewWidth = visWidth;
		
		image_size.setSize( newWidth, newHeight );

		/*
     It is very IMPORTANT to setSize() to indicate the width
     of this JComponent is longer than the viewport size, so
     the JViewport.setViewPosition() will work when it scrolls
     to position wider than the viewport size, i.e. without
     cutoff.  Defining getSize() for this class does NOT seem
     to help during initialization.  setSize() is a must here
     in componentResize()
		 */
		component_size.setSize( newWidth * NumImages,	newHeight );
		setSize( component_size );


		for ( int img_idx = 0; img_idx < NumImages; img_idx++ ) {
			int type = BufferedImage.TYPE_4BYTE_ABGR;  // see api for options

			if(offscreenImages[img_idx] != null){ // free it:
				offscreenImages[img_idx].getGraphics().dispose();
			}


			offscreenImages[ img_idx ] = new BufferedImage(newWidth, newHeight, type);
		}
		
		forceRedraw();
	}

	/*
        Defining getPreferredSize() seems to make HierarchyBoundsListener
        for ViewportTime unnecessary.
	 */
	@Override
	public Dimension getPreferredSize()
	{
		if ( Debug.isActive() )
			Debug.println( "ScrollableObject: pref_size = " + component_size );
		return component_size;
	}

	@Override
	public Dimension getMinimumSize() {	
		return new Dimension(0, getRealImageHeight());
	}

	protected InfoDialog getTimePropertyAt( Epoch realTime )
	{
		final Epoch clickedTime = realTime.subtract(modelTime.getGlobalMinimum());
		Window          window;
		window = SwingUtilities.windowForComponent( this );
		return new InfoDialogForTime( (Frame) window, clickedTime, realTime);
	}

	public ModelTime getModelTime() {
		return modelTime;
	}

	public boolean isAutoRefresh() {
		return autoRefresh;
	}

	public void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
	}

	public boolean isUseBackgroundThread() {
		return useBackgroundThread;
	}

	/**
	 * Enable or disable the usage of a background thread for rendering and processing of 
	 * additional work.
	 * @param useBackgroundThread
	 */
	public void setUseBackgroundThread(boolean useBackgroundThread) {
		this.useBackgroundThread = useBackgroundThread;
	}
}
