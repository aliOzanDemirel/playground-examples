use std::mem;

pub fn example() {

    // tuple is a collection of values of different types
    let mixed_tuple = (-1i8, 16u16, 32u32, 64u64, 'a', true);

    // values can be extracted from the tuple using tuple indexing
    println!("values: {0} / {1} / {2} / {3} / true == {5} / 'a' == {4}", mixed_tuple.0, mixed_tuple.1,
             mixed_tuple.2, mixed_tuple.3, mixed_tuple.4, mixed_tuple.5);

    // type signature is superfluous
    let fixed_size_array: [i32; 5] = [1, 2, 3, 4, 5];

    // all elements can be initialized with same value
    let all_99_array: [i8; 100] = [99; 100];

    // arrays are stack allocated
    println!("occupies bytes -> all_99_array: 100 == {} - fixed_size_array: 20 == {}",
             mem::size_of_val(&all_99_array), mem::size_of_val(&fixed_size_array));

    // arrays can be automatically borrowed as slices
    println!("borrow the whole array as a slice");
    analyze_borrowed_slice(&all_99_array);

    // slices can point to a section of an array: [starting_index..ending_index)
    println!("borrow a section of the array as a slice");
    analyze_borrowed_slice(&all_99_array[50..100]);
}

fn analyze_borrowed_slice(slice: &[i8]) {
    println!("slice has {} elements and first element is {}", slice.len(), slice[0]);
}

pub const CODE: &'static str = r#"
use std::mem;

pub fn example() {

    // tuple is a collection of values of different types
    let mixed_tuple = (-1i8, 16u16, 32u32, 64u64, 'a', true);

    // values can be extracted from the tuple using tuple indexing
    println!("values: {0} / {1} / {2} / {3} / true == {5} / 'a' == {4}", mixed_tuple.0, mixed_tuple.1,
             mixed_tuple.2, mixed_tuple.3, mixed_tuple.4, mixed_tuple.5);

    // type signature is superfluous
    let fixed_size_array: [i32; 5] = [1, 2, 3, 4, 5];

    // all elements can be initialized with same value
    let all_99_array: [i8; 100] = [99; 100];

    // arrays are stack allocated
    println!("occupies bytes -> all_99_array: 100 == {} - fixed_size_array: 20 == {}",
             mem::size_of_val(&all_99_array), mem::size_of_val(&fixed_size_array));

    // arrays can be automatically borrowed as slices
    println!("borrow the whole array as a slice");
    analyze_borrowed_slice(&all_99_array);

    // slices can point to a section of an array: [starting_index..ending_index)
    println!("borrow a section of the array as a slice");
    analyze_borrowed_slice(&all_99_array[50..100]);
}

fn analyze_borrowed_slice(slice: &[i8]) {
    println!("slice has {} elements and first element is {}", slice.len(), slice[0]);
}
"#;