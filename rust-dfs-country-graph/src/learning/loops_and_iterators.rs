pub fn example() {

    // loop with counter index and put '*' character as many times as loop index
    let limit = 5;
    let mut counter = 1;
    let mut returned_after_break = loop {
        println!("{}", "*".repeat(counter));
        if (counter % limit) == 0 {
            break counter * 10;
        }
        counter += 1;
    };
    assert_eq!(returned_after_break, 50, "returned_after_break is not 50!");

    while returned_after_break >= 50 {
        println!("returned_after_break -> {}", returned_after_break);
        returned_after_break = 49;
    }

    let mut exclusive_sum = 0;
    for i in 1..11 {
        exclusive_sum += i;
    }
    let mut inclusive_sum = 0;
    for i in 1..=10 {
        inclusive_sum += i;
    }
    println!("[1,11) -> {} / [1,10] -> {}", exclusive_sum, inclusive_sum);

    // vector is like arraylist, dynamically grows
    let names = vec!["Ali", "Ozan", "Ozzy", "Rusty"];
    println!("Will match within names: {:?}", names);

    for name in names.iter() {
        match name {
            &"Rusty" => println!("No more rusty!"),
            _ => println!("Hello {}", name),
        }
    }
}

pub const CODE: &'static str = r#"
pub fn example() {

    // loop with counter index and put '*' character as many times as loop index
    let limit = 5;
    let mut counter = 1;
    let mut returned_after_break = loop {
        println!("{}", "*".repeat(counter));
        if (counter % limit) == 0 {
            break counter * 10;
        }
        counter += 1;
    };
    assert_eq!(returned_after_break, 50, "returned_after_break is not 50!");

    while returned_after_break >= 50 {
        println!("returned_after_break -> {}", returned_after_break);
        returned_after_break = 49;
    }

    let mut exclusive_sum = 0;
    for i in 1..11 {
        exclusive_sum += i;
    }
    let mut inclusive_sum = 0;
    for i in 1..=10 {
        inclusive_sum += i;
    }
    println!("[1,11) -> {} / [1,10] -> {}", exclusive_sum, inclusive_sum);

    // vector is like arraylist, dynamically grows
    let names = vec!["Ali", "Ozan", "Ozzy", "Rusty"];
    println!("Will match within names: {:?}", names);

    for name in names.iter() {
        match name {
            &"Rusty" => println!("No more rusty!"),
            _ => println!("Hello {}", name),
        }
    }
}
"#;