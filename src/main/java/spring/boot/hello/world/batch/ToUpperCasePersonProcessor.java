package spring.boot.hello.world.batch;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;
import spring.boot.hello.world.model.Person;
import spring.boot.hello.world.model.PersonCopy;

@Service
public class ToUpperCasePersonProcessor implements ItemProcessor<Person, PersonCopy> {

    @Override
    public PersonCopy process(Person person) throws Exception {
        PersonCopy personCopy = new PersonCopy();
        personCopy.setFirstName(person.getFirstName().toUpperCase());
        personCopy.setLastName(person.getLastName().toUpperCase());
        return personCopy;
    }
}
