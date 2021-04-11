package nl.robertlemmens.application;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController("/persons")
public class MyCrudController {

    private static final AtomicLong sequence = new AtomicLong();
    private static final ConcurrentHashMap<Long, Person> personMap = new ConcurrentHashMap<>();

    @GetMapping
    public List<Person> getAllPersons() {
        return new ArrayList<>(personMap.values());
    }

    @GetMapping("/{id}")
    public Person getPersonById(@PathVariable long id) {
        return personMap.get(id);
    }

    @PostMapping
    public Person createPerson(@RequestBody Map<String, String> params) {
        String name = params.get("name");
        int age = Integer.parseInt(params.get("age"));
        long id = sequence.getAndIncrement();
        Person p = Person.of(id, name, age);
        personMap.put(id, p);
        return p;
    }

    @PutMapping
    public Person updatePersonById(@RequestBody Map<String, String> params) {
        long id = Long.parseLong(params.get("id"));
        personMap.get(id).setName(params.get("name"));
        personMap.get(id).setAge(Integer.parseInt(params.get("age")));
        return personMap.get(id);
    }

    @DeleteMapping("/{id}")
    public boolean deletePerson(@PathVariable long id) {
        return personMap.remove(id) != null;
    }

}
