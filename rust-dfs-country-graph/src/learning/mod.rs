pub mod handler;
mod mutexes_arcs_and_threads;
mod primitives_and_strings;
mod tuples_and_arrays;
mod structs_and_enums;
mod loops_and_iterators;
mod boxes_and_options;
mod methods_and_closures;

// can be used from main to experiment with
// fn trigger_examples() {
//     primitives_and_strings::example();
//     tuples_and_arrays::example();
//     structs_and_enums::example();
//     loops_and_iterators::example();
//     boxes_and_options::example();
//     methods_and_closures::example();
//     mutexes_arcs_and_threads::count_concurrently(5);
// }