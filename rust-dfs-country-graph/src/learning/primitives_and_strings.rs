pub fn example() {

    // macro that formats string and writes to standard output
    println!("Hello {0}!", "world".to_string());

    // default_float: `f64`
    // default_integer: `i32`

    let logical: bool = true;
    println!("logical: {}", logical);

    // regular annotation
    let a_float: f64 = 1.0;
    println!("a_float: {}", a_float);

    // suffix annotation
    let an_integer = 5i32;
    println!("an_integer: {}", an_integer);

    // variable's type can be inferred
    let mut inferred_type = 12;
    inferred_type = 4294967296i64;
    println!("inferred_type: {}", inferred_type);

    // variables can be overwritten with shadowing
    let inferred_type = true;
    println!("shadowed inferred_type: true == {}", inferred_type);

    // vector of bytes that grows dynamically in heap
    let heap_str: String = String::from("'String' is in heap");
    println!("heap_str {}", heap_str);

    let stack_str: &str = "'str' is in stack";
    println!("stack_str {}", stack_str);
}

pub const CODE: &'static str = r#"
pub fn example() {

    // macro that formats string and writes to standard output
    println!("Hello {0}!", "world".to_string());

    // default_float: `f64`
    // default_integer: `i32`

    let logical: bool = true;
    println!("logical: {}", logical);

    // regular annotation
    let a_float: f64 = 1.0;
    println!("a_float: {}", a_float);

    // suffix annotation
    let an_integer = 5i32;
    println!("an_integer: {}", an_integer);

    // variable's type can be inferred
    let mut inferred_type = 12;
    inferred_type = 4294967296i64;
    println!("inferred_type: {}", inferred_type);

    // variables can be overwritten with shadowing
    let inferred_type = true;
    println!("shadowed inferred_type: true == {}", inferred_type);

    // vector of bytes that grows dynamically in heap
    let heap_str: String = String::from("'String' is in heap");
    println!("heap_str {}", heap_str);

    let stack_str: &str = "'str' is in stack";
    println!("stack_str {}", stack_str);
}
"#;