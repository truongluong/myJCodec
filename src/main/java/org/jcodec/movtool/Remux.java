package org.jcodec.movtool;

import static org.jcodec.common.NIOUtils.readableFileChannel;
import static org.jcodec.common.NIOUtils.writableFileChannel;
import static org.jcodec.containers.mp4.TrackType.VIDEO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jcodec.common.SeekableByteChannel;
import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.boxes.AudioSampleEntry;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.jcodec.containers.mp4.demuxer.AbstractMP4DemuxerTrack;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.jcodec.containers.mp4.muxer.PCMMP4MuxerTrack;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * @author The JCodec project
 * 
 */
public class Remux {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("remux <movie>");
            return;
        }

        File tgt = new File(args[0]);
        File src = hidFile(tgt);
        tgt.renameTo(src);

        try {
            new Remux().remux(tgt, src);
        } catch (Throwable t) {
            tgt.renameTo(new File(tgt.getParentFile(), tgt.getName() + ".error"));
            src.renameTo(tgt);
        }
    }

    public void remux(File tgt, File src) throws IOException {
        SeekableByteChannel input = null;
        SeekableByteChannel output = null;
        try {
            input = readableFileChannel(src);
            output = writableFileChannel(tgt);
            MP4Demuxer demuxer = new MP4Demuxer(input);
            MP4Muxer muxer = new MP4Muxer(output, Brand.MOV);

            List<AbstractMP4DemuxerTrack> at = demuxer.getAudioTracks();
            List<PCMMP4MuxerTrack> audioTracks = new ArrayList<PCMMP4MuxerTrack>();
            for (AbstractMP4DemuxerTrack demuxerTrack : at) {
                PCMMP4MuxerTrack att = muxer.addUncompressedAudioTrack(((AudioSampleEntry) demuxerTrack
                        .getSampleEntries()[0]).getFormat());
                audioTracks.add(att);
                att.setEdits(demuxerTrack.getEdits());
                att.setName(demuxerTrack.getName());
            }

            AbstractMP4DemuxerTrack vt = demuxer.getVideoTrack();
            FramesMP4MuxerTrack video = muxer.addTrackForCompressed(VIDEO, (int) vt.getTimescale());
            // vt.open(input);
            video.setTimecode(muxer.addTimecodeTrack((int) vt.getTimescale()));
            video.setEdits(vt.getEdits());
            video.addSampleEntries(vt.getSampleEntries());
            MP4Packet pkt = null;
            while ((pkt = (MP4Packet)vt.nextFrame()) != null) {
                pkt = processFrame(pkt);
                video.addFrame(pkt);

                for (int i = 0; i < at.size(); i++) {
                    AudioSampleEntry ase = (AudioSampleEntry) at.get(i).getSampleEntries()[0];
                    int frames = (int) (ase.getSampleRate() * pkt.getDuration() / vt.getTimescale());
                    MP4Packet apkt = (MP4Packet)at.get(i).nextFrame();
                    audioTracks.get(i).addSamples(apkt.getData());
                }
            }

            muxer.writeHeader();
        } finally {
            if (input != null)
                input.close();
            if (output != null)
                output.close();
        }
    }

    protected MP4Packet processFrame(MP4Packet pkt) {
        return pkt;
    }

    public static File hidFile(File tgt) {
        File src = new File(tgt.getParentFile(), "." + tgt.getName());
        if (src.exists()) {
            int i = 1;
            do {
                src = new File(tgt.getParentFile(), "." + tgt.getName() + "." + (i++));
            } while (src.exists());
        }
        return src;
    }
}
