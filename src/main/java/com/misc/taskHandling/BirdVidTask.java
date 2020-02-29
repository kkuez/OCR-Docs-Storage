package com.misc.taskHandling;

import com.Main;
import com.ObjectHub;
import com.objectTemplates.User;
import com.telegram.Bot;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BirdVidTask extends Task {

    private Logger logger = Main.getLogger();

    public BirdVidTask(List<User> userList, Bot bot, String actionName) {
        super(userList, bot, actionName);
    }

    @Override
    public boolean perform(){
        File birdVidRawFolder = new File(ObjectHub.getInstance().getProperties().getProperty("birdVidRawFolder"));
        File birdVidFolder = new File(birdVidRawFolder.getParentFile(), "BirdVid");

        if(!birdVidFolder.exists()){
            birdVidFolder.mkdir();
        }

        Collection<File> rawVids = FileUtils.listFiles(birdVidRawFolder, new String[]{"raw", "mjpeg"}, false);
        List<File> processedVids = new ArrayList<>(rawVids.size());
        rawVids.forEach(rawVid -> {
            File processedvid = null;
            try {
                processedvid = convertStreamFile(rawVid);
            } catch (IOException e) {
                logger.error(e);
            }
            processedVids.add(processedvid);
        });

        return true;
    }

    private File convertStreamFile(File rawFile) throws IOException {
        FFmpeg fFmpeg = new FFmpeg(rawFile.getAbsolutePath());

        FFmpegBuilder fFmpegBuilder = new FFmpegBuilder()
                .setInput(rawFile.getName())
                .addOutput(rawFile.getName() + ".mp4")
                .setFormat("mp4")
                .setTargetSize(rawFile.getTotalSpace())
                .setVideoCodec("libx264")
                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                .done();

        FFmpegExecutor fFmpegExecutor = new FFmpegExecutor(fFmpeg);
        FFmpegJob fFmpegJob = fFmpegExecutor.createTwoPassJob(fFmpegBuilder);

        while(fFmpegJob.getState() != FFmpegJob.State.FINISHED){
            if(fFmpegJob.getState() == FFmpegJob.State.FAILED){
                break;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }
        return new File(rawFile.getParent(), rawFile.getName() + ".mp4");
    }
}
