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

package viewer.common;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;

/**
 * This class provides an interface for getting the global icons.
 * 
 * @author Julian M. Kunkel
 */
public class IconManager{	
	private static final int TOOLBAR_ICON_SIZE = 32;
	private static final int MENUITEM_ICON_SIZE = 13;
	
	private static class InternalIcon{
		final int size;
		
		final ImageIcon active;
		
		private ImageIcon disabled = null;
		
		public InternalIcon(int size, ImageIcon icon) {
			this.size = size;
			this.active = icon;
		
		}
		
		public ImageIcon getDisabled(){
			if (disabled == null){
				disabled  = new ImageIcon( GrayFilter.createDisabledImage( active.getImage() ) );
			}
			return disabled;
		}
		
		@Override
		public boolean equals(Object obj) {
			return ((InternalIcon) obj).size == size;
		}

	}
	
	public enum IconType{
		AddFile,
		AutoRefresh,
		Close,
		FrameHelp,
		FrameLegend,
		FramePreferences,
		FrameTimeline,
		FrameTimelineProfile,
		ImagesImages,
		Left,
		Hand,
		Expand,
		Up,
		Down,
		Open,
		Redo,
		Refresh,
		RemoveTimeline,
		Right,
		Screenshot,
		SearchLeft,
		Search,
		SearchRight,
		Undo,
		UpLeft,
		UpRight,
		ZoomHome,
		ZoomIn,
		ZoomOut,
		Zoom,
		DisableAll,
		DisableSelected,
		EnableAll,
		EnableSelected,
		ToggleAll,
		ToggleSelected
	}
	
	HashMap<IconType, LinkedList<InternalIcon>> cachedIcons = new HashMap<IconType, LinkedList<InternalIcon>>();
	
	/**
	 * The theme from which we have to load the icons.
	 */
	private final String themeName;  	
		
	/**
	 * Loads the image into RAM if not loaded and cache it. 
	 * @param icon
	 * @return
	 */
	public ImageIcon getActiveIcon(IconType icon, int size){		
		InternalIcon cached = getInternalIconData(icon, size);
		return cached.active;
	}
	
	/**
	 * Loads a toolbar image into RAM if not loaded and cache it. 
	 * @param icon
	 * @return
	 */
	public ImageIcon getActiveToolbarIcon(IconType icon){		
		InternalIcon cached = getInternalIconData(icon, TOOLBAR_ICON_SIZE);
		return cached.active;
	}
	
	
	public ImageIcon getDisabledToolbarIcon(IconType icon){		
		InternalIcon cached = getInternalIconData(icon, TOOLBAR_ICON_SIZE);
		return cached.getDisabled();
	}
	
	
	/**
	 * Loads the image into RAM if not loaded and cache it. 
	 * @param icon
	 * @return
	 */
	public ImageIcon getActiveMenuItemIcon(IconType icon){		
		InternalIcon cached = getInternalIconData(icon, MENUITEM_ICON_SIZE);
		return cached.active;
	}
	
	
	/**
	 * Loads the image into RAM if not loaded and cache it. 
	 * @param icon
	 * @return
	 */
	public ImageIcon getDisabledIcon(IconType icon, int size){		
		InternalIcon cached = getInternalIconData(icon, size);
		return cached.disabled;	
	}
	
	private InternalIcon getInternalIconData(IconType icon, int size){		
		LinkedList<InternalIcon> cachedList = cachedIcons.get(icon);
		if( cachedList == null){
			cachedList = new LinkedList<InternalIcon>();
			cachedIcons.put(icon, cachedList);
		}
		
		
		InternalIcon cached = null;
		for(InternalIcon ico: cachedList){
			if(ico.size == size){
				cached = ico;
				break;
			}
		}
		
		if(cached == null){
			File f = null;
			// try several extensions:
			for (String extension: new String[] {"png", "gif"}){
				f = new File(Const.IMG_PATH + themeName + "/" + icon.toString()+ "." + extension);
				if(f.exists())
					break;
			}
			// now a file should be loadable:
			if (! f.exists()){
				throw new IllegalArgumentException("Icon file " + f.getAbsolutePath() + " does not exist!");
			}
			if (! f.canRead()){
				throw new IllegalArgumentException("Icon file " + f.getAbsolutePath() + " is not readable!");
			}
			
			// load and resize the image if necessary:
			final BufferedImage originalImg;
			try{
				originalImg = ImageIO.read(f.getAbsoluteFile());			
			}catch(IOException e){
				throw new IllegalArgumentException(e);
			}
			
			// resize the image:
			final BufferedImage resizedImg;
			resizedImg = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

			Graphics2D g = resizedImg.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setComposite(AlphaComposite.Src);
			g.drawImage(originalImg, 0, 0, size, size, null);
			// free system ressources:
			g.dispose();
			
			// add it to the list:
			final ImageIcon active = new ImageIcon( resizedImg );
			cached = new InternalIcon(size, active);
			cachedList.add(cached);
		}
		
		return cached;
	}
	
	public IconManager(String themeName) {
		this.themeName = themeName;
	}
	
	public String getThemeName() {
		return themeName;
	}
}
