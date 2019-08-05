package spring.boot.hello.world.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Service;
import spring.boot.hello.world.jpa.PersonRepository;
import spring.boot.hello.world.model.Person;

import java.util.List;

@Service
public class DbPersonWriter implements ItemWriter<Person> {
    private final Logger logger = LoggerFactory.getLogger(DbPersonWriter.class);
    private final PersonRepository personRepository;

    public DbPersonWriter(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public void write(List<? extends Person> persons) throws Exception {
        logger.info(String.format("Save %s items", persons.size()));
        personRepository.saveAll(persons);
    }
}
