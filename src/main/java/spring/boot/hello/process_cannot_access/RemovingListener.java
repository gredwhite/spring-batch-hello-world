package spring.boot.hello.process_cannot_access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;

@StepScope
@Component
public class RemovingListener extends StepExecutionListenerSupport {
    private final Logger logger = LoggerFactory.getLogger(RemovingListener.class);
    private final Resource resource;

    public RemovingListener(@Value("#{stepExecutionContext['fileName']}") Resource resource) {
        this.resource = resource;
    }

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        try {
            Files.deleteIfExists(resource.getFile().toPath());
        } catch (IOException e) {
            logger.warn("Failed to remove chunk {}", resource.getFilename(), e);
        }
        return stepExecution.getExitStatus();
    }
}