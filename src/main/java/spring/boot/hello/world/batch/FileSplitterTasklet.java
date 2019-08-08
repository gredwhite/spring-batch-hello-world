package spring.boot.hello.world.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import spring.boot.hello.world.utils.FileSplitter;

import java.io.File;

public class FileSplitterTasklet implements Tasklet {
    private final Logger logger = LoggerFactory.getLogger(FileSplitterTasklet.class);
    private File file;

    public FileSplitterTasklet(File file) {
        this.file = file;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        int count = FileSplitter.splitTextFiles(file, 100);
        logger.info("File was split on {} files", count);
        return RepeatStatus.FINISHED;

    }
}
