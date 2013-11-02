/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package videoxuggler;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.Utils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Mike
 */
public class VideoSeparator implements Runnable{
    private final Long startTimestamp;
    private final Long endTimestamp;
    private final String pathToMovie;
    private final File pathToFrames;
    
    public VideoSeparator(){
        throw new RuntimeException("You cannot invoke VideoSeparator constructor without params!");
    }
    
    public VideoSeparator(Long startTimestamp, Long endTimestamp, final String pathToMovie, final File pathToFrames){
        if(null == startTimestamp || null == endTimestamp || null == pathToMovie || null == pathToFrames)
            throw new RuntimeException("startTimestamp, endTimestamp or pathToMovie must not be null!");
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.pathToMovie = pathToMovie;
        this.pathToFrames = pathToFrames;
    }

    @Override
    public void run() {
        System.out.println("Thread id:" + this);
        long start = startTimestamp;
        long end = endTimestamp;
        IContainer container = IContainer.make();

        if (container.open(pathToMovie, IContainer.Type.READ, null) < 0) {
            throw new IllegalArgumentException("could not open file: "
                    + pathToMovie);
        }
        int numStreams = container.getNumStreams();
        int videoStreamId = -1;
        IStreamCoder videoCoder = null;

        // нужно найти видео поток
        for (int i = 0; i < numStreams; i++) {
            IStream stream = container.getStream(i);
            IStreamCoder coder = stream.getStreamCoder();
            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                videoStreamId = i;
                videoCoder = coder;
                break;
            }
        }
        if (videoStreamId == -1) // кажись не нашли
        {
            throw new RuntimeException("could not find video stream in container: "
                    + pathToMovie);
        }

        // пытаемся открыть кодек
        if (videoCoder.open() < 0) {
            throw new RuntimeException(
                    "could not open video decoder for container: " + pathToMovie);
        }

        IPacket packet = IPacket.make();
        // с 3-ей по 5-ую микросекунду
        //long start = 0 * 1000 * 1000;
        //long end = 400 * 1000 * 1000;
        // с разницей в 100 милисекунд
        //long step = 1;
        //long counter = 0;
        //System.out.println(container.getBitRate());

        END:
        while (container.readNextPacket(packet) >= 0) {

            if (packet.getStreamIndex() == videoStreamId) {
                IVideoPicture picture = IVideoPicture.make(
                        videoCoder.getPixelType(), videoCoder.getWidth(),
                        videoCoder.getHeight());
                int offset = 0;
                while (offset < packet.getSize()) {
                    int bytesDecoded = videoCoder.decodeVideo(picture, packet,
                            offset);
                    //System.out.println(videoCoder.getFrameRate().getValue());
                    // Если что-то пошло не так
                    if (bytesDecoded < 0) {
                        throw new RuntimeException("got error decoding video in: "
                                + pathToMovie);
                    }
                    offset += bytesDecoded;
                    // В общем случае, нужно будет использовать Resampler. См.
                    // tutorials!
                    if (picture.isComplete()) {
                        IVideoPicture newPic = picture;
                        // в микросекундах
                        long timestamp = picture.getTimeStamp();
                        if (timestamp > start) {
                            //counter++;
                            // Получаем стандартный BufferedImage
                            BufferedImage javaImage = Utils
                                    .videoPictureToImage(newPic);
                            String fileName = String.format("%07d.png",
                                    timestamp);
                            try {
                                ImageIO.write(javaImage, "PNG", new File(pathToFrames,
                                        fileName));
                                System.out.println("Thread: " + this);
                            } catch (IOException ex) {
                                Logger.getLogger(VideoSeparator.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            //start += step;
                        }
                        if (timestamp > end) {
                            break END;
                        }
                    }
                }
            }
        }
        if (videoCoder != null) {
            videoCoder.close();
            videoCoder = null;
        }
        if (container != null) {
            container.close();
            container = null;
        }

    }
  }
   

