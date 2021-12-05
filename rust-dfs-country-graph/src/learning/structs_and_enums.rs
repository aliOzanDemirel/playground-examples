use core::fmt;

// basically named tuples
#[derive(Debug)]
pub struct Person {
    pub name: String,
    pub age: u8,
}

// ToString trait is implemented through fmt::Display
impl fmt::Display for Person {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "My name is {} and I am {} years old", self.name, self.age)
    }
}

pub enum Printer {
    PrintPerson(Person),
    PrintChar(char),
    DoNothing,
}

pub fn example() {
    let name = String::from("Ozan");
    let age = 28;
    let ozzy = Person { name, age };
    println!("ozzy -> {:?}", ozzy);

    // sorta like javascript spread
    let ozzy_after_a_year = Person { age: 29, ..ozzy };
    println!("ozzy grows -> {:?}", ozzy_after_a_year);

    // use works like static import
    use Printer::PrintPerson;
    let person_enum = PrintPerson(ozzy_after_a_year);
    match person_enum {
        PrintPerson(p) => println!("still ozzy -> {:?}", p),
        Printer::PrintChar(c) => println!("char '{}'", c),
        Printer::DoNothing => println!("doing nothing")
    }

    // aliases are used by keyword 'type'
    type Human = Person;
    let age = 15;
    let kiddo: Human =
        if age < 18 {
            // fields with same name need not to be assigned
            Person { age, name: String::from("Kiddo") }
        } else {
            Person { age: 18, name: String::from("Big O") }
        };
    println!("Who are you? -> {:?}", kiddo.to_string());
}

pub const CODE: &'static str = r#"
use core::fmt;

// basically named tuples
#[derive(Debug)]
pub struct Person {
    pub name: String,
    pub age: u8,
}

// ToString trait is implemented through fmt::Display
impl fmt::Display for Person {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "My name is {} and I am {} years old", self.name, self.age)
    }
}

pub enum Printer {
    PrintPerson(Person),
    PrintChar(char),
    DoNothing,
}

pub fn example() {
    let name = String::from("Ozan");
    let age = 28;
    let ozzy = Person { name, age };
    println!("ozzy -> {:?}", ozzy);

    // sorta like javascript spread
    let ozzy_after_a_year = Person { age: 29, ..ozzy };
    println!("ozzy grows -> {:?}", ozzy_after_a_year);

    // use works like static import
    use Printer::PrintPerson;
    let person_enum = PrintPerson(ozzy_after_a_year);
    match person_enum {
        PrintPerson(p) => println!("still ozzy -> {:?}", p),
        Printer::PrintChar(c) => println!("char '{}'", c),
        Printer::DoNothing => println!("doing nothing")
    }

    // aliases are used by keyword 'type'
    type Human = Person;
    let age = 15;
    let kiddo: Human =
        if age < 18 {
            // fields with same name need not to be assigned
            Person { age, name: String::from("Kiddo") }
        } else {
            Person { age: 18, name: String::from("Big O") }
        };
    println!("Who are you? -> {:?}", kiddo.to_string());
}
"#;