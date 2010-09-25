import java.io.File;
import java.io.FilenameFilter;


//
//  VisibleFilesOnly.java
//  FrequencyDigester
//
//  Created by Zarkonnen on 19/10/05.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//

public class VisibleFilesOnly implements FilenameFilter {

	public boolean accept(File dir, String name) {
		return (name.substring(0, 1).equals(".") == false);
	}

}
