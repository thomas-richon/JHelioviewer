package org.helioviewer.jhv.viewmodel.view.jp2view.newjpx;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.helioviewer.jhv.JHelioviewer;
import org.helioviewer.jhv.base.FileUtils;
import org.helioviewer.jhv.viewmodel.view.jp2view.JP2Image;
import org.helioviewer.jhv.viewmodel.view.jp2view.image.JP2ImageParameter;
import org.helioviewer.jhv.viewmodel.view.jp2view.image.ResolutionSet;
import org.helioviewer.jhv.viewmodel.view.jp2view.image.ResolutionSet.ResolutionLevel;
import org.helioviewer.jhv.viewmodel.view.jp2view.image.SubImage;
import org.helioviewer.jhv.viewmodel.view.jp2view.io.http.HTTPRequest.Method;
import org.helioviewer.jhv.viewmodel.view.jp2view.io.jpip.JPIPQuery;
import org.helioviewer.jhv.viewmodel.view.jp2view.io.jpip.JPIPRequest;
import org.helioviewer.jhv.viewmodel.view.jp2view.io.jpip.JPIPSocket;

public class JPXLayer {
	private JPIPSocket socket;
	private JP2Image jp2Image;

	public enum LAYER_QUALITY{
		NEXT_POSIBILE, MAX;
	}
	
	public JPXLayer(JP2Image jp2Image) {
		this.jp2Image = jp2Image;
	}
	
	
	public static void main(String[] args) {
	    
		System.loadLibrary("kdu_jni");
	    
		URI uri;
		try {
			uri = new URI("jpip://helioviewer.org:8090/AIA/2015/02/23/171/2015_02_23__15_42_59_34__SDO_AIA_AIA_171.jp2");
			URI downloadURI = new URI("http://helioviewer.org/api/index.php?action=getJP2Image&observatory=SDO&instrument=AIA&detector=AIA&measurement=171&date=2015-02-23T16:07:26Z&json=true");
			
			NewReader newReader = new NewReader(uri);
			
			//Subi
			SubImage _roi = new SubImage(0, 0, 4096, 4096);
			ResolutionSet resolutionSet = new ResolutionSet(8);
			resolutionSet.addResolutionLevel(7, new Rectangle(32, 32));
			resolutionSet.addResolutionLevel(6, new Rectangle(64, 64));
			resolutionSet.addResolutionLevel(5, new Rectangle(128, 128));
			resolutionSet.addResolutionLevel(4, new Rectangle(256, 256));
			resolutionSet.addResolutionLevel(3, new Rectangle(512, 512));
			resolutionSet.addResolutionLevel(2, new Rectangle(1024, 1024));
			resolutionSet.addResolutionLevel(1, new Rectangle(2048, 2048));
			resolutionSet.addResolutionLevel(0, new Rectangle(4096, 4096));
			ResolutionLevel _resolution = resolutionSet.getResolutionLevel(0);
			System.out.println("res : " + _resolution);
			JPIPQuery query = newReader.createQuery(new JP2ImageParameter(_roi, _resolution, 0, 0), 0, 8);
			System.out.println("query : " + query);
			JPIPRequest request = new JPIPRequest(Method.GET, query);
			newReader.addRequest(request);

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
