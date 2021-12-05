use crate::learning::structs_and_enums::Person;

impl Person {
    // associated function that is associated with a particular type: Person
    fn new() -> Person {
        Person { age: 0, name: "Unknown".to_string() }
    }

    // self is the caller object (struct instance), gives access to the fields via the dot operator
    fn is_adult(&self) -> bool {
        if self.age >= 18 { true } else { false }
    }

    fn give_name(&mut self, name: &str) {
        self.name = name.to_string()
    }

    fn divide_age(&mut self, divide_by: u8) -> Result<u8, &str> {
        if divide_by < 1 {
            Err("can only divide by positive number")
        } else {
            Ok(self.age / divide_by)
        }
    }
}

pub fn example() {
    let mut newborn = Person::new();
    newborn.give_name("Baby");
    println!("{} is adult: {}", newborn.name, newborn.is_adult());

    // type Result returns errors for explicit handling
    newborn.age = 12;
    let result = newborn.divide_age(0);
    match result {
        Err(err_msg) => println!("Reason: {}", err_msg),
        Ok(result) => println!("Division result: {}", result)
    }

    let result = newborn.divide_age(3);
    match result {
        Err(err_msg) => println!("Reason: {}", err_msg),
        Ok(result) => println!("Division result: {}", result)
    }

    // if this name capturing closure is moved before any newborn.name, then there would be compiler error
    let name_from_closure = || newborn.name;
    let years_to_be_adult = |age| -> u8 { 18 - age };
    let years = years_to_be_adult(newborn.age);
    println!("{} needs {} years to be adult", name_from_closure(), years);
}

pub const CODE: &'static str = r#"
use crate::learning::structs_and_enums::Person;

impl Person {
    // associated function that is associated with a particular type: Person
    fn new() -> Person {
        Person { age: 0, name: "Unknown".to_string() }
    }

    // self is the caller object (struct instance), gives access to the fields via the dot operator
    fn is_adult(&self) -> bool {
        if self.age >= 18 { true } else { false }
    }

    fn give_name(&mut self, name: &str) {
        self.name = name.to_string()
    }

    fn divide_age(&mut self, divide_by: u8) -> Result<u8, &str> {
        if divide_by < 1 {
            Err("can only divide by positive number")
        } else {
            Ok(self.age / divide_by)
        }
    }
}

pub fn example() {
    let mut newborn = Person::new();
    newborn.give_name("Baby");
    println!("{} is adult: {}", newborn.name, newborn.is_adult());

    // type Result returns errors for explicit handling
    newborn.age = 12;
    let result = newborn.divide_age(0);
    match result {
        Err(err_msg) => println!("Reason: {}", err_msg),
        Ok(result) => println!("Division result: {}", result)
    }

    let result = newborn.divide_age(3);
    match result {
        Err(err_msg) => println!("Reason: {}", err_msg),
        Ok(result) => println!("Division result: {}", result)
    }

    // if this name capturing closure is moved before any newborn.name, then there would be compiler error
    let name_from_closure = || newborn.name;
    let years_to_be_adult = |age| -> u8 { 18 - age };
    let years = years_to_be_adult(newborn.age);
    println!("{} needs {} years to be adult", name_from_closure(), years);
}
"#;