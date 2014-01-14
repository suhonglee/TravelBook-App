package com.travelbook.utility;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FlushedInputstream extends FilterInputStream{

	public FlushedInputstream(InputStream in) {
		super(in);
		// TODO Auto-generated constructor stub
	}
	
	@Override
    public long skip(long n) throws IOException {
        long totalBytesSkipped = 0L;
        while (totalBytesSkipped < n) {
            long bytesSkipped = in.skip(n - totalBytesSkipped);
            if (bytesSkipped == 0L) {
                  int byte1 = read();
                  if (byte1 < 0) {
                      break;  // we reached EOF
                  } else {
                      bytesSkipped = 1; // we read one byte
                  }
           }
            totalBytesSkipped += bytesSkipped;
        }
        return totalBytesSkipped;
    }



}
