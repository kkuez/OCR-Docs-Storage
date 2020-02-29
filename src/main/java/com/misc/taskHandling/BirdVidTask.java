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
import java.time.LocalDateTime;
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
        rawVids.forEach(rawVid -> {
            File processedVid = null;
            try {
                processedVid = convertStreamFile(rawVid);
                File processedVidCopy = new File(processedVid.getParentFile().getParentFile().getAbsolutePath() + File.separator + "BirdVid", "Vogel_" + LocalDateTime.now().toString().replace(":", "-") + ".mp4");
                FileUtils.copyFile(processedVid, processedVidCopy);
                processedVid.delete();
                rawVid.delete();

                ObjectHub.getInstance().getAllowedUsersMap().values().forEach(user -> {
                    getBot().sendVideoFromURL(user, processedVidCopy.getAbsolutePath(), "(:");
                });

            } catch (IOException | InterruptedException e) {
                logger.error(e);
            }
        });
        return true;
    }

    private File convertStreamFile(File rawFile) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();

        //Install fucking ffmpeg to path variable first!!
        processBuilder.command("ffmpeg", "-i", rawFile.getAbsolutePath(), "-f", "mp4", rawFile.getParent() + File.separator + rawFile.getName() + ".mp4");
        processBuilder.redirectOutput();
        processBuilder.redirectError();
        Process convertProcess = processBuilder.start();

        while(convertProcess.isAlive()) {
            if(convertProcess.getInputStream().available() > 0) {
                String inputStream = new String(convertProcess.getInputStream().readAllBytes());
                System.out.println(inputStream);
            }
            if(convertProcess.getErrorStream().available() > 0) {
                String errorStream = new String(convertProcess.getErrorStream().readAllBytes());
                System.out.println(errorStream);
            }
        }

        return new File(rawFile.getParent(), rawFile.getName() + ".mp4");




       /* FFmpeg fFmpeg = new FFmpeg(rawFile.getAbsolutePath());

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
        */
    }
}
