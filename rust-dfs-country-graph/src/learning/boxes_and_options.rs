use std::mem;

use crate::learning::structs_and_enums::{Person, Printer};

// const -> unchangeable value, represent a constant value (not memory address)
// static -> possibly mutable variable with 'static lifetime, represent global variable as memory address (inlined)
// values with 'static lifetime are stored directly in final binary
const VERY_OLD_AGE: u8 = 137;

fn optional_person(printer: Printer) -> Option<Person> {
    match printer {
        Printer::PrintPerson(p) => Some(Person { age: p.age, name: p.name }),
        Printer::PrintChar(_) => None,
        Printer::DoNothing => panic!("Exit program if ever person is requested with DoNothing! Programming error!")
    }
}

pub fn example() {
    let person_in_stack: Person = Person {
        age: VERY_OLD_AGE,
        name: "Survivor".to_string(),
    };
    println!("person_in_stack is whole structure that occupies {} bytes on the stack", mem::size_of_val(&person_in_stack));

    // copy person with new allocation and box it
    // boxed indirection allocates in heap
    let person_enum = Printer::PrintPerson(person_in_stack);
    let copied_person = optional_person(person_enum);
    let person_in_heap: Box<Person> = Box::new(copied_person.unwrap());
    println!("person_in_heap is reference to heap that occupies {} bytes on the heap", mem::size_of_val(&person_in_heap));
}

pub const CODE: &'static str = r#"
use std::mem;

use crate::learning::structs_and_enums::{Person, Printer};

// const -> unchangeable value, represent a constant value (not memory address)
// static -> possibly mutable variable with 'static lifetime, represent global variable as memory address (inlined)
// values with 'static lifetime are stored directly in final binary
const VERY_OLD_AGE: u8 = 137;

fn optional_person(printer: Printer) -> Option<Person> {
    match printer {
        Printer::PrintPerson(p) => Some(Person { age: p.age, name: p.name }),
        Printer::PrintChar(_) => None,
        Printer::DoNothing => panic!("Exit program if ever person is requested with DoNothing! Programming error!")
    }
}

pub fn example() {
    let person_in_stack: Person = Person {
        age: VERY_OLD_AGE,
        name: "Survivor".to_string(),
    };
    println!("person_in_stack is whole structure that occupies {} bytes on the stack", mem::size_of_val(&person_in_stack));

    // copy person with new allocation and box it
    // boxed indirection allocates in heap
    let person_enum = Printer::PrintPerson(person_in_stack);
    let copied_person = optional_person(person_enum);
    let person_in_heap: Box<Person> = Box::new(copied_person.unwrap());
    println!("person_in_heap is reference to heap that occupies {} bytes on the heap", mem::size_of_val(&person_in_heap));
}
"#;