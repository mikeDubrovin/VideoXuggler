/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package videoxuggler;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Mike
 */
public class XugglerVideoSeparator {
    private final static String filename = "c:/temp/han.avi";
    private final static File outdir = new File("c:/temp/han");
    public static void main(String[] args){
        if(!outdir.exists())
            outdir.mkdir();
        IContainer container = IContainer.make();

        if (container.open(filename, IContainer.Type.READ, null) < 0) {
            throw new IllegalArgumentException("could not open file: "
                    + filename);
        }
        System.out.println(container.getDuration());
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(new VideoSeparator(0l, container.getDuration()/8, filename, outdir));
        executor.submit(new VideoSeparator(((container.getDuration()/8)+1), container.getDuration()/4, filename, outdir));
        executor.submit(new VideoSeparator(((container.getDuration()/4) + 1), 3*(container.getDuration()/8), filename, outdir));
        executor.submit(new VideoSeparator((3*(container.getDuration()/8) + 1), container.getDuration()/2, filename, outdir));
        executor.submit(new VideoSeparator(container.getDuration()/2, 5*(container.getDuration()/8), filename, outdir));
        executor.submit(new VideoSeparator(((5*(container.getDuration()/8))+1), 6*(container.getDuration()/8), filename, outdir));
        executor.submit(new VideoSeparator(((6*(container.getDuration()/8)) + 1), 7*(container.getDuration()/8), filename, outdir));
        executor.submit(new VideoSeparator((7*(container.getDuration()/8) + 1), container.getDuration(), filename, outdir));
    }
}
